package com.example.projektaplikacje.osrodki

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.projektaplikacje.R

class OsrodkiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val osrodkiList = mutableListOf<Osrodek>()
    private lateinit var adapter: Adapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_osrodki)

        // Inicjalizacja RecyclerView
        recyclerView = findViewById(R.id.recyclerViewOsrodki)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(osrodkiList)
        recyclerView.adapter = adapter

        // Obsługa przycisku WRÓĆ
        val backButton = findViewById<Button>(R.id.button_back)
        backButton.setOnClickListener {
            finish() // zamyka tę aktywność i wraca do poprzedniej
        }

        loadOsrodkiFromFirestore()
    }

    private fun loadOsrodkiFromFirestore() {
        db.collection("Ośrodki")
            .get()
            .addOnSuccessListener { result ->
                osrodkiList.clear()
                for (document in result) {
                    val osrodek = document.toObject(Osrodek::class.java)
                    osrodkiList.add(osrodek)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("OsrodkiActivity", "Błąd pobierania danych: ", exception)
            }
    }
}
