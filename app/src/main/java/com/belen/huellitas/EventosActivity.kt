package com.belen.huellitas

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EventosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var eventAdapter: EventoAdapter
    private lateinit var tituloTextView: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.evento_recycler)

        recyclerView = findViewById(R.id.recycler)
        //recyclerView.layoutManager = LinearLayoutManager(this)
        dbHelper = DatabaseHelper(this)
        val eventType = intent.getStringExtra("EVENT_TYPE") ?: "todos"

        // Referencia al TextView del título
        tituloTextView = findViewById(R.id.tituloEvento)
        if(eventType=="Cita Médica \uD83C\uDFE5"){
            tituloTextView.setText("Citas médicas")
        }
        if(eventType=="Hito Importante \uD83C\uDF89"){
            tituloTextView.setText("Hitos importantes")
        }
        if(eventType=="Vacuna \uD83D\uDC89"){
            tituloTextView.setText("Vacunas")
        }
        if(eventType=="Otro ✏\uFE0F"){
            tituloTextView.setText("Otros")
        }

        // Se recibe el filtro de tipo de evento. Si no se especifica o es "todos", se mostrarán todos los eventos.

        val events = if (eventType != null && eventType != "todos") {
            dbHelper.getEventsByType(eventType)
        } else {
            dbHelper.getEvents()
        }
        eventAdapter = EventoAdapter(events.toMutableList(),dbHelper,this)
        // Dentro de tu actividad o fragmento:

        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columnas

        recyclerView.adapter = eventAdapter

        recyclerView.adapter = eventAdapter
    }
}
