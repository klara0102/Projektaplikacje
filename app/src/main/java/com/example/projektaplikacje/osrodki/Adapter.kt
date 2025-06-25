package com.example.projektaplikacje.osrodki
import android.widget.ImageView
import com.example.projektaplikacje.R
import android.view.LayoutInflater
import android.widget.Button
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val osrodkiList: List<Osrodek>) : RecyclerView.Adapter<Adapter.OsrodekViewHolder>() {

    private var expandedPosition = -1

    inner class OsrodekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNazwa: TextView = itemView.findViewById(R.id.tvNazwa)
        val tvAdres: TextView = itemView.findViewById(R.id.tvAdres)
        val tvTelefon: TextView = itemView.findViewById(R.id.tvTelefon)
        val tvOpis: TextView = itemView.findViewById(R.id.tvOpis)
        val expandableLayout: View = itemView.findViewById(R.id.expandableLayout)
        val imageViewOsrodek: ImageView = itemView.findViewById(R.id.imageViewOsrodek)
        val buttonZapiszSie: Button = itemView.findViewById(R.id.buttonZapiszSie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OsrodekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_osrodek, parent, false)
        return OsrodekViewHolder(view)
    }

    override fun getItemCount(): Int = osrodkiList.size

    override fun onBindViewHolder(holder: OsrodekViewHolder, position: Int) {
        val osrodek = osrodkiList[position]
        holder.tvNazwa.text = osrodek.Ośrodek
        holder.tvAdres.text = osrodek.Adres
        holder.tvTelefon.text = osrodek.Telefon
        holder.tvOpis.text = osrodek.Schorzenie

        val isExpanded = position == expandedPosition
        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.imageViewOsrodek.setImageResource(R.drawable.mapa)

        holder.tvNazwa.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else position
            notifyDataSetChanged()
        }

        holder.buttonZapiszSie.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Zapisano na badania w ${osrodek.Ośrodek}", Toast.LENGTH_SHORT).show()
        }
    }
}