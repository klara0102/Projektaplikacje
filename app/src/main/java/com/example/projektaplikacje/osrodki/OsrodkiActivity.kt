package com.example.projektaplikacje.osrodki

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektaplikacje.R
import com.google.firebase.firestore.FirebaseFirestore

class OsrodkiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val osrodkiList = mutableListOf<Osrodek>()
    private lateinit var adapter: Adapter

    private val db = FirebaseFirestore.getInstance()
    private var selectedConditions: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_osrodki)

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

    private fun shouldIncludeOsrodek(osrodek: Osrodek): Boolean {
        val filters = selectedConditions
        if (filters.isNullOrEmpty()) return true
        return osrodek.schorzenia.any { filters.contains(it) }
    }
}