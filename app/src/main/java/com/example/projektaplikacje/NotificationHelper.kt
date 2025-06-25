package com.example.projektaplikacje

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Klasa pomocnicza do tworzenia i wyświetlania powiadomień.
 *
 * @param context Kontekst aplikacji, używany do uzyskania dostępu do systemowych usług powiadomień.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        /**
         * ID kanału powiadomień używanego do identyfikacji kanału systemowego.
         */
        const val CHANNEL_ID = "appointment_channel"
    }

    /**
     * Tworzy kanał powiadomień, jeśli system operacyjny obsługuje kanały (Android 8.0+).
     *
     * Kanał jest wymagany do wyświetlania powiadomień na nowszych wersjach Androida.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Appointment Channel"
            val descriptionText = "Kanał powiadomień o wizytach"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Wyświetla powiadomienie z podanym tytułem i treścią.
     *
     * @param title Tytuł powiadomienia.
     * @param message Treść wiadomości w powiadomieniu.
     */
    fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // możesz zamienić na swoją ikonę
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1001, builder.build())
        }
    }
}
