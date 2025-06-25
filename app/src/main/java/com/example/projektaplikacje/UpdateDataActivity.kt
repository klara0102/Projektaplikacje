package com.example.projektaplikacje
import com.example.projektaplikacje.firebasee.User
import com.example.projektaplikacje.firebasee.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide

/**
 * Aktywność do aktualizacji danych użytkownika w bazie danych Firestore.
 */
class UpdateDataActivity : AppCompatActivity() {

    // Komponenty UI do zbierania i wyświetlania danych użytkownika
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var profileImageView: ImageView

    // Instancje Firebase Authentication i klasy Firestore
    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_date)

        // Inicjalizacja komponentów UI
        initializeUI()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Asynchroniczne ładowanie danych użytkownika z FirestoreClass
            lifecycleScope.launch {
                try {
                    // Pobierz dane użytkownika z Firestore
                    val data = firestoreClass.loadUserData(userId) // Funkcja zawieszona
                    if (data != null) {
                        val user = User.fromMap(data) // Konwertuj dane Firestore do obiektu User
                        populateUI(user) // Wypełnij UI danymi użytkownika
                    } else {
                        Toast.makeText(this@UpdateDataActivity, "Brak danych użytkownika.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@UpdateDataActivity, "Błąd ładowania danych: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Obsługa kliknięcia przycisku zatwierdzenia – zapis zaktualizowanych danych
        submitButton.setOnClickListener {
            if (userId != null) {
                lifecycleScope.launch {
                    updateUserData(userId) // Zapisz dane asynchronicznie
                }
            } else {
                Toast.makeText(this, "Użytkownik niezalogowany.", Toast.LENGTH_SHORT).show()
            }
        }

        // Obsługa kliknięcia przycisku anulowania – wyjdź bez zapisu
        cancelButton.setOnClickListener {
            finish() // Zamknij aktywność bez zmian
        }
    }

    /**
     * Inicjalizuje komponenty UI poprzez powiązanie ich z widokami z layoutu.
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
     * Wypełnia interfejs użytkownika danymi.
     *
     * @param user Obiekt User zawierający dane do wyświetlenia.
     */
    private fun populateUI(user: User) {
        nameInput.setText(user.name ?: "") // Ustaw imię użytkownika w polu EditText
        emailInput.setText(user.email) // Ustaw e-mail użytkownika
        phoneInput.setText(user.phoneNumber) // Ustaw numer telefonu

        // Konwertuj mapę adresu na pojedynczy ciąg i wyświetl
        val address = user.address.values.joinToString(", ")
        addressInput.setText(address)

        // Konwertuj listę zainteresowań na ciąg rozdzielany przecinkami i wyświetl
        interestsInput.setText(user.interests.joinToString(", "))

        // Załaduj zdjęcie profilowe przy użyciu Glide z obrazem zastępczym
        if (user.profilePictureUrl.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.profilePictureUrl))
                .placeholder(R.drawable.obrazpng) // Obraz zastępczy podczas ładowania
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.obrazpng) // Domyślny obraz, jeśli brak zdjęcia profilowego
        }
    }

    /**
     * Zbiera zaktualizowane dane z UI i zapisuje je w Firestore.
     *
     * @param userId ID użytkownika, którego dane są aktualizowane.
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
            mapOf()
        }

        val updatedData = mapOf(
            "name" to nameInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phoneNumber" to phoneInput.text.toString(),
            "address" to addressMap,
            "interests" to interestsInput.text.toString().split(",").map { it.trim() }
        )

        try {
            Log.d("UpdateDataActivity", "Próba aktualizacji danych: $updatedData")
            firestoreClass.updateUserData(userId, updatedData)
            Log.d("UpdateDataActivity", "Aktualizacja zakończona sukcesem")
            Toast.makeText(this, "Dane zaktualizowane pomyślnie!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e("UpdateDataActivity", "Błąd aktualizacji: ${e.message}")
            Toast.makeText(this, "Błąd aktualizacji danych: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
