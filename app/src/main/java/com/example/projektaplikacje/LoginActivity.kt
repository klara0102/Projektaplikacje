package com.example.projektaplikacje
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

/**
 * Aktywność odpowiedzialna za logowanie użytkownika przy użyciu Firebase Authentication.
 */
class LoginActivity : BaseActivity(), View.OnClickListener {

    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicjalizacja pól do wpisywania oraz przycisku logowania
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.loginButton)

        // Ustawienie nasłuchiwania kliknięcia na przycisk logowania
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }

        // Ustawienie nasłuchiwania kliknięcia na link rejestracji
        findViewById<View>(R.id.registerTextViewClickable)?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.registerTextViewClickable -> {
                    // Przejście do ekranu rejestracji po kliknięciu linku
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Waliduje dane logowania wprowadzone przez użytkownika.
     * @return True jeśli dane są poprawne, w przeciwnym razie False.
     */
    private fun validateLoginDetails(): Boolean {
        val email = inputEmail?.text.toString().trim { it <= ' ' }
        val password = inputPassword?.text.toString().trim { it <= ' ' }

        return when {
            email.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                // Opcjonalnie można wyświetlić komunikat o powodzeniu
                true
            }
        }
    }

    /**
     * Loguje zarejestrowanego użytkownika przy użyciu Firebase Authentication.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }

            // Logowanie za pomocą FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("You are logged in successfully.", false)
                        goToMainActivity()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Przechodzi do głównej aktywności po udanym logowaniu i przekazuje UID użytkownika.
     */
    open fun goToMainActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.orEmpty()

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("uID", email)
        }
        startActivity(intent)
    }
}