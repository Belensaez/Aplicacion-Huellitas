package com.belen.huellitas

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class CalendarioActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var calendarView: CalendarView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnCitaMedica: Button
    private lateinit var btnHito: Button
    private lateinit var btnVacuna: Button
    private lateinit var btnOtro: Button
    private lateinit var colorMap: MutableMap<String, Int>

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendario)
        dbHelper = DatabaseHelper(this)
        calendarView = findViewById(R.id.calendarView)
        mediaPlayer = MediaPlayer.create(this, R.raw.boton_sonido);


       btnCitaMedica = findViewById(R.id.citasMedicas)
        btnCitaMedica.setOnClickListener { val intent = Intent(this, EventosActivity::class.java)
            intent.putExtra("EVENT_TYPE", "Cita Médica 🏥")
            startActivity(intent) }
        btnHito = findViewById(R.id.hito)
        btnHito.setOnClickListener { val intent = Intent(this, EventosActivity::class.java)
            intent.putExtra("EVENT_TYPE", "Hito Importante \uD83C\uDF89")
            startActivity(intent) }
        btnVacuna = findViewById(R.id.vacuna)
        btnVacuna.setOnClickListener { val intent = Intent(this, EventosActivity::class.java)
            intent.putExtra("EVENT_TYPE", "Vacuna \uD83D\uDC89")
            startActivity(intent) }
        btnOtro = findViewById(R.id.otro)
        btnOtro.setOnClickListener { val intent = Intent(this, EventosActivity::class.java)
            intent.putExtra("EVENT_TYPE", "Otro ✏\uFE0F")
            startActivity(intent) }
        colorMap = HashMap()
        colorMap["Cita Médica 🏥"] = Color.BLUE
        colorMap["Vacuna 💉"] = Color.GREEN
        colorMap["Hito Importante 🎉"] = Color.RED
        colorMap["Otro ✏️"] = Color.YELLOW
        calendarView.setOnDateChangeListener(OnDateChangeListener { view: CalendarView?, year: Int, month: Int, dayOfMonth: Int ->
            val selectedDate =
                LocalDate.of(year, month + 1, dayOfMonth)
            showEventDialog(selectedDate)
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showEventDialog(selectedDate: LocalDate) {
        val eventTypes = arrayOf("Cita Médica 🏥", "Vacuna 💉", "Hito Importante 🎉", "Otro ✏️")
        AlertDialog.Builder(this)
            .setTitle("Selecciona un tipo de evento")
            .setItems(
                eventTypes
            ) { dialog: DialogInterface?, which: Int ->
                showTimePickerDialog(
                    selectedDate,
                    eventTypes[which]
                )

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showTimePickerDialog(selectedDate: LocalDate, eventType: String) {
        val timePickerDialog = TimePickerDialog(this,
            { view: TimePicker?, hourOfDay: Int, minute: Int ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                showDescriptionDialog(selectedDate, selectedTime, eventType)
            }, 12, 0, true
        )
        timePickerDialog.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showDescriptionDialog(
        selectedDate: LocalDate,
        selectedTime: LocalTime,
        eventType: String
    ) {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Describe el evento")
            .setView(input)
            .setPositiveButton("Guardar") { dialog: DialogInterface?, which: Int ->
                val description = input.text.toString()
                if (!description.isEmpty()) {
                    val formattedDateTime =
                        selectedDate.format(DateTimeFormatter.ISO_DATE) + " " + selectedTime.format(
                            DateTimeFormatter.ISO_TIME
                        )
                    dbHelper.insertEvent(formattedDateTime, eventType, description)
                    mediaPlayer?.start()
                    Toast.makeText(this, "Evento guardado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


}
