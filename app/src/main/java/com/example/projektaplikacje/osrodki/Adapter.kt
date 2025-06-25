package com.example.projektaplikacje.osrodki

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.projektaplikacje.R

/**
 * Adapter RecyclerView do wyświetlania listy ośrodków medycznych.
 * Obsługuje rozwijanie szczegółów oraz przycisk do zapisania się na wizytę.
 *
 * @param osrodkiList Lista obiektów [Osrodek] do wyświetlenia.
 */
class Adapter(private val osrodkiList: List<Osrodek>) : RecyclerView.Adapter<Adapter.OsrodekViewHolder>() {

    /** Indeks aktualnie rozwiniętego elementu. -1 oznacza brak rozwinięcia. */
    private var expandedPosition = -1

    /**
     * ViewHolder reprezentujący pojedynczy widok ośrodka w liście.
     */
    inner class OsrodekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNazwa: TextView = itemView.findViewById(R.id.tvNazwa)
        val tvAdres: TextView = itemView.findViewById(R.id.tvAdres)
        val tvTelefon: TextView = itemView.findViewById(R.id.tvTelefon)
        val tvOpis: TextView = itemView.findViewById(R.id.tvOpis)
        val expandableLayout: View = itemView.findViewById(R.id.expandableLayout)
        val imageViewOsrodek: ImageView = itemView.findViewById(R.id.imageViewOsrodek)
        val buttonZapiszSie: Button = itemView.findViewById(R.id.buttonZapiszSie)
    }

    /**
     * Tworzy nowy widok ViewHolder na podstawie layoutu XML.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OsrodekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_osrodek, parent, false)
        return OsrodekViewHolder(view)
    }

    /**
     * Zwraca liczbę elementów w liście.
     */
    override fun getItemCount(): Int = osrodkiList.size

    /**
     * Wypełnia widok danymi oraz obsługuje kliknięcia.
     * Pokazuje szczegóły po kliknięciu w nazwę oraz wysyła powiadomienie po kliknięciu "Zapisz się".
     */
    override fun onBindViewHolder(holder: OsrodekViewHolder, position: Int) {
        val osrodek = osrodkiList[position]
        holder.tvNazwa.text = osrodek.Ośrodek
        holder.tvAdres.text = osrodek.Adres
        holder.tvTelefon.text = osrodek.Telefon
        holder.tvOpis.text = osrodek.Schorzenie

        // Pokazuje/ukrywa szczegóły
        val isExpanded = position == expandedPosition
        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.imageViewOsrodek.setImageResource(R.drawable.mapa)

        // Obsługa kliknięcia w nazwę – rozwija/zamyka szczegóły
        holder.tvNazwa.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else position
            notifyDataSetChanged()
        }

        // Obsługa kliknięcia przycisku "Zapisz się"
        holder.buttonZapiszSie.setOnClickListener {
            val context = holder.itemView.context
            val channelId = "visit_channel"
            val notificationId = System.currentTimeMillis().toInt()

            // Android 13+ – sprawdzenie uprawnień
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, "Brak zgody na powiadomienia", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Tworzenie kanału powiadomień (Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Kanał powiadomień wizyt",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            // Budowanie powiadomienia
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Rejestracja zakończona")
                .setContentText("Udało ci się zapisać na wizytę!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Wysłanie powiadomienia po 60 sekundach (1 minuta)
            Handler(Looper.getMainLooper()).postDelayed({
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }, 60_000)

            // Informacja natychmiastowa (Toast)
            Toast.makeText(context, "Zapisano na badania w ${osrodek.Ośrodek}", Toast.LENGTH_SHORT).show()
        }
    }
}

