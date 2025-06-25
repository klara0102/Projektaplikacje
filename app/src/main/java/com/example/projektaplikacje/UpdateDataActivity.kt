package com.example.projektaplikacje

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.projektaplikacje.firebasee.FirestoreClass
import com.example.projektaplikacje.firebasee.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Aktywność umożliwiająca użytkownikowi aktualizację danych profilowych.
 *
 * Dane są pobierane z Firestore po zalogowaniu i prezentowane w interfejsie graficznym.
 * Po edycji dane mogą być ponownie zapisane w bazie.
 */
class UpdateDataActivity : AppCompatActivity() {

    // Pola UI
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var profileImageView: ImageView

    // Instancje Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    /**
     * Wywoływana przy tworzeniu aktywności.
     * Inicjalizuje interfejs i ładuje dane użytkownika z Firestore, jeśli jest zalogowany.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_date)

        initializeUI()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val data = firestoreClass.loadUserData(userId)
                    if (data != null) {
                        val user = User.fromMap(data)
                        populateUI(user)
                    } else {
                        showToast("Brak danych użytkownika.")
                    }
                } catch (e: Exception) {
                    showToast("Błąd ładowania danych: ${e.message}")
                    Log.e("UpdateDataActivity", "Load error", e)
                }
            }
        }

        // Zapisanie danych użytkownika po edycji
        submitButton.setOnClickListener {
            if (userId != null) {
                lifecycleScope.launch {
                    updateUserData(userId)
                }
            } else {
                showToast("Użytkownik niezalogowany.")
            }
        }

        // Powrót bez zapisu
        cancelButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Inicjalizuje wszystkie elementy interfejsu użytkownika.
     */
    private fun initializeUI() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton)
        profileImageView = findViewById(R.id.profileImageView)
    }

    /**
     * Wypełnia interfejs użytkownika danymi pobranymi z Firestore.
     *
     * @param user Obiekt użytkownika zawierający dane do wyświetlenia.
     */
    private fun populateUI(user: User) {
        nameInput.setText(user.name ?: "")
        emailInput.setText(user.email)
        phoneInput.setText(user.phoneNumber)

        val address = listOfNotNull(
            user.address["city"],
            user.address["street"],
            user.address["postcode"]
        ).joinToString(", ")
        addressInput.setText(address)

        interestsInput.setText(user.interests.joinToString(", "))

        if (user.profilePictureUrl.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.profilePictureUrl))
                .placeholder(R.drawable.obrazpng)
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.obrazpng)
        }
    }

    /**
     * Aktualizuje dane użytkownika w Firestore na podstawie danych z formularza.
     *
     * @param userId Identyfikator użytkownika w Firebase.
     */
    private suspend fun updateUserData(userId: String) {
        val addressParts = addressInput.text.toString().split(",").map { it.trim() }
        val addressMap = if (addressParts.size == 3) {
            mapOf(
                "city" to addressParts[0],
                "street" to addressParts[1],
                "postcode" to addressParts[2]
            )
        } else {
            emptyMap()
        }

        val updatedData = mapOf(
            "name" to nameInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phoneNumber" to phoneInput.text.toString(),
            "address" to addressMap,
            "interests" to interestsInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )

        Log.d("UpdateDataActivity", "Aktualizacja z danymi: $updatedData")

        try {
            firestoreClass.updateUserData(userId, updatedData)
            showToast("Dane zaktualizowane pomyślnie!")
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e("UpdateDataActivity", "Błąd aktualizacji danych", e)
            showToast("Błąd aktualizacji danych: ${e.message}")
        }
    }

    /**
     * Wyświetla krótką wiadomość Toast.
     *
     * @param message Treść wiadomości do wyświetlenia.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
