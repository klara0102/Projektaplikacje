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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var phoneInput: EditText
    private lateinit var phoneDisplayText: TextView
    private lateinit var addressInput: EditText
    private lateinit var updateButton: Button
    private lateinit var selectConditionsButton: Button
    private lateinit var submitButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var dobButton: Button
    private lateinit var ageText: TextView
    private lateinit var addressDisplayText: TextView
    private lateinit var openOsrodkiButton: Button

    private var userName: String? = null
    private var selectedDateOfBirth: String = ""
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    private val updateDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    loadUserData(userId)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserData(userId)
        }

        selectConditionsButton.setOnClickListener {
            showConditionsDialog()
        }

        phoneDisplayText.visibility = View.GONE

        submitButton.setOnClickListener {
            val phone = phoneInput.text.toString().trim()
            val address = addressInput.text.toString().trim()

            if (phone.isEmpty() && address.isEmpty()) {
                Toast.makeText(this, "Proszę wpisać numer telefonu lub adres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isNotEmpty()) {
                phoneInput.visibility = View.GONE
                phoneDisplayText.visibility = View.VISIBLE
                phoneDisplayText.text = "Twój numer telefonu: $phone"
            }

            if (address.isNotEmpty()) {
                addressInput.visibility = View.GONE
                addressDisplayText.visibility = View.VISIBLE
                addressDisplayText.text = "Adres: $address"
            }

            lifecycleScope.launch {
                saveUserData()
            }

            submitButton.visibility = View.GONE
        }

        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateDataActivity::class.java)
            updateDataLauncher.launch(intent)
        }

        openOsrodkiButton.setOnClickListener {
            val intent = Intent(this, OsrodkiActivity::class.java)
            intent.putStringArrayListExtra("filters", ArrayList(selectedConditions))
            startActivity(intent)
        }

        dobButton.setOnClickListener {
            openDatePicker()
        }
    }

    private fun initializeUI() {
        profileImageView = findViewById(R.id.profileImageView)
        phoneInput = findViewById(R.id.phoneInput)
        phoneDisplayText = findViewById(R.id.phoneDisplayText)
        addressInput = findViewById(R.id.addressInput)
        updateButton = findViewById(R.id.updateDataButton)
        addressDisplayText = findViewById(R.id.addressDisplayText)
        submitButton = findViewById(R.id.submitButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        dobButton = findViewById(R.id.dobButton)
        ageText = findViewById(R.id.ageText)
        openOsrodkiButton = findViewById(R.id.buttonOpenOsrodki)
        selectConditionsButton = findViewById(R.id.buttonSelectConditions)
        phoneDisplayText.visibility = View.GONE
        addressDisplayText.visibility = View.GONE
    }

    private fun showConditionsDialog() {
        val conditionsArray = allConditions.toTypedArray()
        val selectedItems = BooleanArray(allConditions.size) { selectedConditions.contains(allConditions[it]) }

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Wybierz warunki")
        builder.setMultiChoiceItems(conditionsArray, selectedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedConditions.add(allConditions[which])
            } else {
                selectedConditions.remove(allConditions[which])
            }
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this, "Wybrane: ${selectedConditions.joinToString()}", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun loadUserData(userId: String) {
        lifecycleScope.launch {
            try {
                val data = firestoreClass.loadUserData(userId)
                if (data != null) {
                    val user = User.fromMap(data)
                    populateUI(user)
                } else {
                    Toast.makeText(this@MainActivity, "Brak danych użytkownika.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Błąd ładowania danych: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return

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

        val data = firestoreClass.loadUserData(userId)
        if (data != null) {
            userName = User.fromMap(data).name.toString()
        }
        if (selectedDateOfBirth == "" && data != null) {
            selectedDateOfBirth = User.fromMap(data).dateOfBirth
        }

        val user = User(
            email = FirebaseAuth.getInstance().currentUser?.email.toString(),
            name = userName,
            id = userId,
            phoneNumber = phoneInput.text.toString(),
            dateOfBirth = selectedDateOfBirth,
            address = addressMap,
            profilePictureUrl = selectedImageUri?.toString()
                ?: User.fromMap(data!!).profilePictureUrl
        )

        try {
            firestoreClass.registerOrUpdateUser(user)
            Toast.makeText(this@MainActivity, "Dane zapisane pomyślnie!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Błąd zapisu danych: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateUI(user: User) {
        try {
            phoneDisplayText.text = "Twój numer telefonu: ${user.phoneNumber}"
            if (!user.phoneNumber.isNullOrEmpty()) {
                phoneInput.visibility = View.GONE
                phoneDisplayText.visibility = View.VISIBLE
            } else {
                phoneInput.visibility = View.VISIBLE
                phoneDisplayText.visibility = View.GONE
            }

            val city = user.address["city"] ?: ""
            val street = user.address["street"] ?: ""
            val postcode = user.address["postcode"] ?: ""
            val address = listOf(city, street, postcode).filter { it.isNotEmpty() }.joinToString(", ")

            addressDisplayText.text = "Adres: $address"
            if (address.isNotEmpty()) {
                addressInput.visibility = View.GONE
                addressDisplayText.visibility = View.VISIBLE
            } else {
                addressInput.visibility = View.VISIBLE
                addressDisplayText.visibility = View.GONE
            }

            if (user.dateOfBirth.isNotEmpty()) {
                val age = calculateAge(user.dateOfBirth)
                ageText.text = "Wiek: $age"
            } else {
                ageText.text = ""
            }

            if (user.profilePictureUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(user.profilePictureUrl))
                    .placeholder(R.drawable.obrazpng)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.obrazpng)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Błąd wyświetlania danych użytkownika.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView.setImageURI(it)
            }
        }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDateOfBirth = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                val age = calculateAge(selectedDateOfBirth)
                ageText.text = "Wiek: $age"
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun calculateAge(dateOfBirth: String): Int {
        val parts = dateOfBirth.split("-")
        if (parts.size != 3) return 0

        val birthYear = parts[0].toInt()
        val birthMonth = parts[1].toInt()
        val birthDay = parts[2].toInt()

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthYear

        if (today.get(Calendar.MONTH) < birthMonth - 1 ||
            (today.get(Calendar.MONTH) == birthMonth - 1 && today.get(Calendar.DAY_OF_MONTH) < birthDay)
        ) {
            age--
        }

        return age
    }

    private val allConditions = listOf(
        "Migrena", "Choroby serca", "Choroby oczu", "Atopowe zapalenie skóry",
        "RZS", "Padaczka", "Cukrzyca", "Choroby tarczycy"
    )

    private val selectedConditions = mutableSetOf<String>()
}
