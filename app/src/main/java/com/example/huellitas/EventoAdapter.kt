package com.example.huellitas

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class EventoAdapter(private val eventList: MutableList<Event>, private val db: DatabaseHelper, private val context: Context) :
    RecyclerView.Adapter<EventoAdapter.EventViewHolder>() {

    // Mapa de colores para cada tipo de evento
    private val colorMap = mapOf(
        "Cita Médica 🏥" to Color.parseColor("#C4EAFF"), // Azul claro
        "Vacuna 💉" to Color.parseColor("#C0DD9D"), // Verde claro
        "Hito Importante 🎉" to Color.parseColor("#FFB6C1"), // Rosa claro
        "Otro ✏️" to Color.parseColor("#f6e1a4") // Amarillo oro
    )

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardview) // Referencia al CardView
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val txtType: TextView = view.findViewById(R.id.txtType)
        val txtDescription: TextView = view.findViewById(R.id.txtDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        // Dividir la fecha en "fecha" y "hora"
        val dateTimeParts = event.date.split(" ")
        val dateOnly = dateTimeParts.getOrNull(0) ?: event.date
        val timeOnly = dateTimeParts.getOrNull(1) ?: ""

        holder.txtDate.text = "📅 Fecha: $dateOnly"
        holder.txtTime.text = "⏰ Hora: $timeOnly"
        holder.txtType.text = "📌 Tipo: ${event.type}"
        holder.txtDescription.text = "📝 ${event.description}"

        // Cambiar color del CardView según el tipo de evento
        val color = colorMap[event.type] ?: Color.WHITE // Color por defecto si no hay coincidencia
        holder.cardView.setCardBackgroundColor(color)

        holder.cardView.setOnLongClickListener {
            mostrarMenuFlotante(it, position, event)
            true
        }
    }
    private fun mostrarDialogoEliminacion(position: Int, event: Event) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Evento")
            .setMessage("¿Estás seguro de que deseas eliminar este evento?")
            .setPositiveButton("Sí") { _, _ ->
                eliminarEvento(position, event)
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun eliminarEvento(position: Int, event: Event) {
        // Eliminar de la base de datos
        val deleted = db.eliminarEvento(event.id) // Asegúrate de tener un método en DatabaseHelper
        if (deleted) {
            // Eliminar de la lista y actualizar la UI
            eventList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, eventList.size)
        } else {
            Log.e("EventoAdapter", "Error al eliminar el evento de la base de datos")
        }
    }
    private fun mostrarMenuFlotante(view: View, position: Int, event: Event) {
        val popupMenu = PopupMenu(context, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.menu_evento, popupMenu.menu) // Archivo XML del menú

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_eliminar -> {
                    mostrarDialogoEliminacion(position, event)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int = eventList.size
}
