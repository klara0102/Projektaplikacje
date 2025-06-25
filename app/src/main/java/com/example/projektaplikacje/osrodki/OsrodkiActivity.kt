package com.example.projektaplikacje.osrodki

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektaplikacje.R
import com.google.firebase.firestore.FirebaseFirestore
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager






class OsrodkiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val osrodkiList = mutableListOf<Osrodek>()
    private lateinit var adapter: Adapter

    private val db = FirebaseFirestore.getInstance()
    private var selectedConditions: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_osrodki)

        createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "visit_channel",
                "Kanał powiadomień wizyt",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }




        recyclerView = findViewById(R.id.recyclerViewOsrodki)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(osrodkiList)
        recyclerView.adapter = adapter

        val backButton = findViewById<Button>(R.id.button_back)
        backButton.setOnClickListener { finish() }

        selectedConditions = intent.getStringArrayListExtra("filters")
        loadOsrodkiFromFirestore()
    }

    private fun loadOsrodkiFromFirestore() {
        db.collection("Ośrodki")
            .get()
            .addOnSuccessListener { result ->
                osrodkiList.clear()
                for (document in result) {
                    val osrodek = document.toObject(Osrodek::class.java)
                    osrodek.schorzenia = listOf(osrodek.Schorzenie)
                    if (shouldIncludeOsrodek(osrodek)) {
                        osrodkiList.add(osrodek)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("OsrodkiActivity", "Błąd pobierania danych: ", exception)
            }
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Wizyty"
            val descriptionText = "Powiadomienia o zapisach"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("visit_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun shouldIncludeOsrodek(osrodek: Osrodek): Boolean {
        val filters = selectedConditions
        if (filters.isNullOrEmpty()) return true
        return osrodek.schorzenia.any { filters.contains(it) }
    }
}

// ⬇ DODAJ TUTAJ NA DOLE (poza klasą!)
private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Wizyty"
        val descriptionText = "Powiadomienia o zapisach"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("visit_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}