package com.example.huellitas;
import Registro
import android.content.Context
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

class RegistroAdapter(private val listaRegistros: MutableList<Registro>,private val db: DatabaseHelper, private val context: Context) :
    RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder>() {

    class RegistroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewFecha: TextView = view.findViewById(R.id.txtDate)
        val textViewPeso: TextView = view.findViewById(R.id.txtType)
        val textViewAltura: TextView = view.findViewById(R.id.txtDescription)
        val textViewPerimetroCefalico: TextView = view.findViewById(R.id.textViewPerimetroCefalico)
        val textViewHorasSueno: TextView = view.findViewById(R.id.textViewHorasSueno)
        val textViewCantidadComidas: TextView = view.findViewById(R.id.textViewCantidadComidas)
        val cardView: CardView = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_recycler, parent, false)
        return RegistroViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        val registro = listaRegistros[position]
        holder.textViewFecha.text = "📅 Fecha: ${registro.fecha}"
        holder.textViewPeso.text = "⚖️ Peso: ${registro.peso} kg"
        holder.textViewAltura.text = "📏 Altura: ${registro.altura} m"
        holder.textViewPerimetroCefalico.text = "📏 Perímetro Cefálico: ${registro.perimetroCefalico} cm"
        holder.textViewHorasSueno.text = "😴 Horas de sueño: ${registro.horasSueño} h"
        holder.textViewCantidadComidas.text = "🍽️ Comidas al día: ${registro.cantidadComidas}"
        holder.cardView.setOnLongClickListener {
            mostrarMenuFlotante(it, position, registro)
            true
        }
    }

    override fun getItemCount(): Int {
        return listaRegistros.size
    }
    private fun mostrarDialogoEliminacion(position: Int, event: Registro) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Evento")
            .setMessage("¿Estás seguro de que deseas eliminar este registro?")
            .setPositiveButton("Sí") { _, _ ->
                eliminarRegistro(position, event)
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun eliminarRegistro(position: Int, registro: Registro) {
        // Eliminar de la base de datos
        val deleted = db.eliminarRegistro(registro.idRegistro!!) // Asegúrate de tener un método en DatabaseHelper
        if (deleted) {
            // Eliminar de la lista y actualizar la UI
            listaRegistros.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listaRegistros.size)
        } else {
            Log.e("RegistroAdapter", "Error al eliminar el registro de la base de datos")
        }
    }
    private fun mostrarMenuFlotante(view: View, position: Int, registro: Registro) {
        val popupMenu = PopupMenu(context, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.menu_evento, popupMenu.menu) // Archivo XML del menú

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_eliminar -> {
                    mostrarDialogoEliminacion(position, registro)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
