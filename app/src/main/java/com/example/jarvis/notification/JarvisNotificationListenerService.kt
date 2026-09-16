package com.example.jarvis.notification

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.example.jarvis.security.SensitiveDataFilter
import com.example.jarvis.storage.db.JarvisDatabase
import com.example.jarvis.storage.db.NotificationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationSummary(
    val id: String,
    val packageName: String,
    val appTitle: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        private var instance: JarvisNotificationListenerService? = null
        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _liveNotifications = MutableStateFlow<List<NotificationSummary>>(emptyList())
        val liveNotifications: StateFlow<List<NotificationSummary>> = _liveNotifications.asStateFlow()

        fun isNotificationAccessEnabled(context: Context): Boolean {
            val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
            return packageNames.contains(context.packageName)
        }

        fun getInstance(): JarvisNotificationListenerService? = instance
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isConnected.value = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        _isConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val pkgName = sbn.packageName ?: ""
        // Do not intercept JARVIS's own alarm notifications to avoid feedback loops
        if (pkgName == packageName) return

        val rawTitle = extras.getString(Notification.EXTRA_TITLE)
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val rawText = extras.getString(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (rawTitle.isBlank() && rawText.isBlank()) return

        // Sanitize sensitive info (e.g. 2FA codes, cards) from persistent memory
        val sanitizedTitle = SensitiveDataFilter.sanitizeForDisplay(rawTitle)
        val sanitizedText = SensitiveDataFilter.sanitizeForDisplay(rawText)

        val pm = applicationContext.packageManager
        val appTitle = try {
            val appInfo = pm.getApplicationInfo(pkgName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkgName
        }

        val item = NotificationSummary(
            id = sbn.id.toString() + "_" + sbn.postTime,
            packageName = pkgName,
            appTitle = appTitle,
            title = sanitizedTitle,
            text = sanitizedText,
            timestamp = sbn.postTime
        )

        val current = _liveNotifications.value.toMutableList()
        current.add(0, item)
        if (current.size > 50) current.removeAt(current.size - 1)
        _liveNotifications.value = current

        // Persist to Room database
        serviceScope.launch {
            try {
                val db = JarvisDatabase.getInstance(applicationContext)
                db.notificationDao().insertNotification(
                    NotificationLogEntity(
                        id = item.id,
                        packageName = item.packageName,
                        appTitle = item.appTitle,
                        title = item.title,
                        text = item.text,
                        timestamp = item.timestamp
                    )
                )
            } catch (_: Exception) {}
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Handled passively
    }
}
