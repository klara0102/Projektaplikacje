package com.example.projektaplikacje

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * Base class for all activities in the application.
 * Provides utility methods shared across activities, such as displaying a Snackbar.
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * Bazowa klasa dla wszystkich aktywności w aplikacji
     * Udostępnia metody pomocnicze współdzielone przez aktywności, takie jak wyświetlanie Snackbarów.
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        // Wyświetla Snackbar z podaną wiadomością
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Ustaw kolor tła Snackbar w zależności od typu wiadomości
        snackbarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                if (errorMessage) R.color.colorSnackBarError else R.color.colorSnackBarSuccess
            )
        )
        // Wyświetl Snackbar
        snackbar.show()
    }
}