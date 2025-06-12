package com.example.projektaplikacje
import com.example.projektaplikacje.firebasee.User
import com.example.projektaplikacje.firebasee.FirestoreClass
import com.example.projektaplikacje.osrodki.OsrodkiActivity
import com.google.firebase.auth.FirebaseAuth
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar
import com.bumptech.glide.Glide

/**
 * Główna aktywność aplikacji, gdzie wyświetlane i zarządzane są dane użytkownika.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var phoneInput: EditText
    private lateinit var dobText: TextView
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var updateButton: Button
    private lateinit var submitButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var dobButton: Button
    private lateinit var ageText: TextView
    private lateinit var openOsrodkiButton: Button  // <-- nowy przycisk

    private var userName: String? = null
    private var selectedDateOfBirth: String = ""
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance() // Instancja Firebase Authentication
    private val firestoreClass = FirestoreClass() // Pomocnicza klasa do interakcji z Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja komponentów UI
        initializeUI()

        // Załaduj istniejące dane użytkownika, jeśli jest zalogowany
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserData(userId)
        }

        // Otwórz galerię w celu wyboru zdjęcia profilowego
        selectImageButton.setOnClickListener { openGallery() }

        // Otwórz DatePicker w celu wyboru daty urodzenia
        dobButton.setOnClickListener { openDatePicker() }

        // Zapisz dane użytkownika do Firestore
        submitButton.setOnClickListener {
            lifecycleScope.launch {
                saveUserData()
            }
        }

        // Przejście do aktywności UpdateDataActivity
        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateDataActivity::class.java)
            updateDataLauncher.launch(intent)
        }

        // Obsługa kliknięcia przycisku otwierającego listę ośrodków
        openOsrodkiButton.setOnClickListener {
            val intent = Intent(this, OsrodkiActivity::class.java)
            startActivity(intent)
        }
    }

    private val updateDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    loadUserData(userId) // Ponowne załadowanie danych po aktualizacji
                }
            }
        }

    private fun initializeUI() {
        profileImageView = findViewById(R.id.profileImageView)
        phoneInput = findViewById(R.id.phoneInput)
        dobText = findViewById(R.id.dobText)
        addressInput = findViewById(R.id.addressInput)
        interestsInput = findViewById(R.id.interestsInput)
        updateButton = findViewById(R.id.updateDataButton)
        submitButton = findViewById(R.id.submitButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        dobButton = findViewById(R.id.dobButton)
        ageText = findViewById(R.id.ageText)
        openOsrodkiButton = findViewById(R.id.buttonOpenOsrodki)  // <-- dodane tutaj
    }

    /**
     * Ładuje dane użytkownika z Firestore i uzupełnia UI.
     *
     * @param userId Unikalny identyfikator użytkownika, którego dane są ładowane.
     */
    private fun loadUserData(userId: String) {
        lifecycleScope.launch {
            try {
                // Pobierz dane użytkownika z Firestore
                val data = firestoreClass.loadUserData(userId)
                if (data != null) {
                    val user = User.fromMap(data) // Konwersja danych Firestore do obiektu User
                    populateUI(user) // Uzupełnienie UI danymi użytkownika
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Brak danych użytkownika.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Błąd ładowania danych: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Zapisuje dane użytkownika do Firestore.
     */
    private suspend fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return

        // Parsowanie adresu na zmapowaną strukturę
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

        // Podział zainteresowań na listę
        val interests = interestsInput.text.toString().split(",").map { it.trim() }

        // Pobierz istniejące dane, aby uzupełnić brakujące pola
        val data = firestoreClass.loadUserData(userId)
        if (data != null) {
            userName = User.fromMap(data).name.toString()
        }
        if (selectedDateOfBirth == "" && data != null) {
            selectedDateOfBirth = User.fromMap(data).dateOfBirth
        }

        // Utwórz nowy obiekt User z aktualnymi danymi
        val user = User(
            email = FirebaseAuth.getInstance().currentUser?.email.toString(),
            name = userName,
            id = userId,
            phoneNumber = phoneInput.text.toString(),
            dateOfBirth = selectedDateOfBirth,
            address = addressMap,
            interests = interests,
            profilePictureUrl = selectedImageUri?.toString()
                ?: User.fromMap(data!!).profilePictureUrl
        )

        // Zapisz dane użytkownika do Firestore
        try {
            firestoreClass.registerOrUpdateUser(user)
            Toast.makeText(this@MainActivity, "Dane zapisane pomyślnie!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(
                this@MainActivity,
                "Błąd zapisu danych: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Uzupełnia interfejs danymi użytkownika.
     *
     * @param user Obiekt użytkownika z danymi do wyświetlenia.
     */
    private fun populateUI(user: User) {
        try {
            phoneInput.setText(user.phoneNumber)

            // Konwertuj adres z mapy na pojedynczy ciąg tekstowy
            val address = user.address.values.joinToString(", ")
            addressInput.setText(address)

            interestsInput.setText(user.interests.joinToString(", "))

            // Ustaw datę urodzenia i oblicz/wyswietl wiek
            if (user.dateOfBirth.isNotEmpty()) {
                dobText.text = user.dateOfBirth
                val age = calculateAge(user.dateOfBirth)
                ageText.text = "Wiek: $age"
            } else {
                dobText.text = "Wybierz datę urodzenia"
                ageText.text = ""
            }

            // Załaduj zdjęcie profilowe do ImageView za pomocą Glide
            if (user.profilePictureUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(user.profilePictureUrl))
                    .placeholder(R.drawable.obrazpng) // Obraz zastępczy
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.obrazpng) // Domyślny obraz
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Błąd wyświetlania danych użytkownika.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Otwiera galerię w celu wyboru zdjęcia profilowego.
     */
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView.setImageURI(it) // Wyświetl wybrany obraz
            }
        }

    /**
     * Otwiera okno DatePicker do wyboru daty urodzenia.
     */
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDateOfBirth = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                dobText.text = selectedDateOfBirth

                // Oblicz i wyświetl wiek
                val age = calculateAge(selectedDateOfBirth)
                ageText.text = "Wiek: $age"
            },
            year,
            month,
            day
        )

        // Ograniczenie wyboru daty do przeszłości
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    /**
     * Oblicza wiek na podstawie daty urodzenia.
     *
     * @param dateOfBirth Data urodzenia w formacie "YYYY-MM-DD".
     * @return Obliczony wiek.
     */
    private fun calculateAge(dateOfBirth: String): Int {
        val parts = dateOfBirth.split("-")
        if (parts.size != 3) return 0

        val birthYear = parts[0].toInt()
        val birthMonth = parts[1].toInt()
        val birthDay = parts[2].toInt()

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthYear

        // Korekta wieku, jeśli aktualna data jest przed urodzinami
        if (today.get(Calendar.MONTH) < birthMonth - 1 ||
            (today.get(Calendar.MONTH) == birthMonth - 1 && today.get(Calendar.DAY_OF_MONTH) < birthDay)
        ) {
            age--
        }

        return age
    }
}