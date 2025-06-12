package com.example.projektaplikacje.osrodki

import com.example.projektaplikacje.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val osrodkiList: List<Osrodek>) : RecyclerView.Adapter<Adapter.OsrodekViewHolder>() {

    inner class OsrodekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNazwa: TextView = itemView.findViewById(R.id.tvNazwa)
        val tvAdres: TextView = itemView.findViewById(R.id.tvAdres)
        val tvTelefon: TextView = itemView.findViewById(R.id.tvTelefon)
        val tvOpis: TextView = itemView.findViewById(R.id.tvOpis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OsrodekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_osrodek, parent, false)
        return OsrodekViewHolder(view)
    }

    override fun onBindViewHolder(holder: OsrodekViewHolder, position: Int) {
        val osrodek = osrodkiList[position]
        holder.tvNazwa.text = osrodek.Ośrodek
        holder.tvAdres.text = osrodek.Adres
        holder.tvTelefon.text = osrodek.Telefon
        holder.tvOpis.text = osrodek.Schorzenie
    }

    override fun getItemCount(): Int = osrodkiList.size
}