package com.example.projektaplikacje

import com.example.projektaplikacje.firebasee.User
import com.example.projektaplikacje.firebasee.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseUser
/**
 * Obsługuje rejestrację użytkownika z walidacją danych oraz integracją z Firebase Authentication.
 */
class RegisterActivity : BaseActivity() {

    // Komponenty UI formularza rejestracji
    private var registerButton: Button? = null
    private var inputEmail: EditText? = null
    private var inputName: EditText? = null
    private var inputPassword: EditText? = null
    private var inputPasswordRepeat: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicjalizacja pól formularza oraz przycisku rejestracji
        registerButton = findViewById(R.id.registerButton)
        inputEmail = findViewById(R.id.inputEmail)
        inputName = findViewById(R.id.inputName)
        inputPassword = findViewById(R.id.inputPasswordRegister)
        inputPasswordRepeat = findViewById(R.id.inputPasswordRepeat)

        // Ustawienie nasłuchiwania kliknięcia przycisku rejestracji
        registerButton?.setOnClickListener {
            registerUser()
        }
    }

    /**
     * Waliduje dane rejestracyjne wprowadzone przez użytkownika.
     *
     * @return True jeśli dane są poprawne, w przeciwnym razie False.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            inputEmail?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }

            inputName?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_name), true)
                false
            }

            inputPassword?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }

            inputPasswordRepeat?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_reppassword), true)
                false
            }

            inputPassword?.text.toString().trim { it <= ' ' } != inputPasswordRepeat?.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }

            else -> true
        }
    }

    /**
     * Przechodzi do ekranu logowania.
     *
     * @param view Bieżący widok, który wywołał przejście.
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Zakończ tę aktywność, aby uniemożliwić powrót bez restartu aplikacji.
    }

    /**
     * Rejestruje użytkownika przy użyciu Firebase Authentication.
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }
            val name = inputName?.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "Zarejestrowano pomyślnie. Twój identyfikator to ${firebaseUser.uid}",
                            false
                        )

                        val user = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = email,
                            phoneNumber = "", // Domyślnie puste
                            dateOfBirth = "", // Domyślnie puste
                            address = mapOf(), // Domyślnie pusta mapa
                            interests = listOf(), // Domyślna pusta lista
                            profilePictureUrl = "" // Domyślnie puste
                        )

                        lifecycleScope.launch {
                            try {
                                val firestoreClass = FirestoreClass()
                                firestoreClass.registerOrUpdateUser(user)
                                Toast.makeText(this@RegisterActivity, "Dane zapisane pomyślnie!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@RegisterActivity, "Błąd zapisu danych: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        FirebaseAuth.getInstance().signOut()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Wywoływane po pomyślnej rejestracji użytkownika. Wyświetla komunikat za pomocą Toast.
     */
    fun userRegistrationSuccess() {
        Toast.makeText(
            this@RegisterActivity,
            getString(R.string.register_success),
            Toast.LENGTH_LONG
        ).show()
    }
}
