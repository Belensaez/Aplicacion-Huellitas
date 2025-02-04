package com.example.huellitas

import Registro
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CrecimientoYProgreso : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.crecimiento)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val pageRegistro = findViewById<View>(R.id.registroDATOS)
        val botonRegistro = findViewById<Button>(R.id.botonAñadirRegistro)
        var email = intent.getStringExtra("email") ?: "nulo"
        db = DatabaseHelper(this)
        botonRegistro.setOnClickListener {
            val idUsuario = db.getUsuarioPorEmail(email)?.id
            val pesoEdit = findViewById<EditText>(R.id.nombreEditar)
            val alturaEdit = findViewById<EditText>(R.id.altura)
            val perimetroEdit = findViewById<EditText>(R.id.perimetro)
            val horasSueñoEdit = findViewById<EditText>(R.id.horasSueño)
            val unidadesComidaEdit = findViewById<EditText>(R.id.unidadesComida)

            val peso = pesoEdit.text.toString()
            val altura = alturaEdit.text.toString()
            val perimetro = perimetroEdit.text.toString()
            val horasSueño = horasSueñoEdit.text.toString()
            val unidadesComida = unidadesComidaEdit.text.toString()

            if (peso.isNotBlank() && altura.isNotBlank() && perimetro.isNotBlank() && horasSueño.isNotBlank() && unidadesComida.isNotBlank()) {

                try {
                    val horasSueñoTexto = horasSueñoEdit.text.toString()
                    val horasSueño = horasSueñoTexto.toFloatOrNull() ?: 0.0f
                    val registro = Registro(
                        idUserRegistro = idUsuario!!,
                        peso = peso.toFloat(),
                        altura = altura.toFloat(),
                        perimetroCefalico = perimetro.toFloat(),
                        horasSueño = horasSueño,
                        cantidadComidas = unidadesComida.toInt(),
                    )
                    db.insertRegistro(registro)
                    Toast.makeText(this, "Registro añadido", Toast.LENGTH_LONG).show()
                    pesoEdit.text.clear()
                    alturaEdit.text.clear()
                    perimetroEdit.text.clear()
                    horasSueñoEdit.text.clear()
                    unidadesComidaEdit.text.clear()
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        this,
                        "Error en los valores numéricos. Asegúrate de ingresar números válidos.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                Toast.makeText(this, "Rellenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }

        val pageHistorial = findViewById<View>(R.id.recycler)
        val pageSalud = findViewById<View>(R.id.salud)


        // Escuchar eventos de selección de pestañas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Primera pestaña (Registro de datos)
                        pageRegistro.visibility = View.VISIBLE
                        pageHistorial.visibility = View.GONE
                        pageSalud.visibility = View.GONE

                    }

                    1 -> { // Segunda pestaña (Historial)
                        pageRegistro.visibility = View.GONE
                        pageHistorial.visibility = View.VISIBLE
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
                        val adapter = findViewById<RecyclerView>(R.id.recycler)
                        recyclerView.layoutManager = LinearLayoutManager(this@CrecimientoYProgreso)
                        val idUsuario = db.getUsuarioPorEmail(email)?.id
                        var listaRegistros = db.getAllRegistrosPorIdUser(idUsuario!!)
                        recyclerView.adapter =
                            RegistroAdapter(listaRegistros, db, this@CrecimientoYProgreso)
                        pageSalud.visibility = View.GONE
                    }

                    2 -> { // Tercera pestaña (Gráfica)
                        pageRegistro.visibility = View.GONE
                        pageHistorial.visibility = View.GONE
                        pageSalud.visibility = View.VISIBLE
                        val idUsuario = db.getUsuarioPorEmail(email)?.id
                        val ultimoRegistro = db.obtenerUltimoRegistro(idUsuario!!)

// Si no hay registros, crear un "registro vacío" con valores por defecto
                        val registro = ultimoRegistro ?: Registro(
                            idRegistro = 0,
                            idUserRegistro = idUsuario,
                            peso = 0f,
                            altura = 0f,
                            perimetroCefalico = 0f,
                            horasSueño = 0f,
                            cantidadComidas = 0,
                            fecha = "Sin registros"
                        )

// Mostrar la pantalla de salud aunque no haya registros
                        pageRegistro.visibility = View.GONE
                        pageHistorial.visibility = View.GONE
                        pageSalud.visibility = View.VISIBLE

                        val usuario = db.getUsuarioPorEmail(email)
                        val mensajeSalud = if (ultimoRegistro != null) {

                            validarSaludBebe(registro, usuario!!)
                        } else {
                            "No hay registros aún. Ingresa datos para ver recomendaciones."
                        }

// Mostrar mensaje por defecto si no hay registros
                        val imcMessage = findViewById<TextView>(R.id.textIMC)
                        val sleepMessage = findViewById<TextView>(R.id.textSueño)
                        val weightMessage = findViewById<TextView>(R.id.textPeso)

                        val imcIcon = findViewById<ImageView>(R.id.imageIMC)
                        val sleepIcon = findViewById<ImageView>(R.id.imageSueño)
                        val weightIcon = findViewById<ImageView>(R.id.imagePeso)

                        if (ultimoRegistro != null) {
                            // Si hay datos, mostramos las imágenes y los mensajes adecuados
                            imcIcon.visibility = View.VISIBLE
                            sleepIcon.visibility = View.VISIBLE
                            weightIcon.visibility = View.VISIBLE

                            if (mensajeSalud.contains("IMC normal")) {
                                imcIcon.setImageResource(R.drawable.circuloverde)
                                imcMessage.text = "Normal"
                            } else if (mensajeSalud.contains("IMC bajo")) {
                                imcIcon.setImageResource(R.drawable.circulorojo)
                                imcMessage.text = "Bajo. Revisar su dieta."
                            } else {
                                imcIcon.setImageResource(R.drawable.circuloamarillo)
                                imcMessage.text = "Alto. Controlar el peso."
                            }

                            if (mensajeSalud.contains("Sueño adecuado")) {
                                sleepIcon.setImageResource(R.drawable.circuloverde)
                                sleepMessage.text = "Adecuado"
                            } else if (mensajeSalud.contains("Sueño insuficiente")) {
                                sleepIcon.setImageResource(R.drawable.circulorojo)
                                sleepMessage.text = "Insuficiente. Más horas de sueño necesarias."
                            } else {
                                sleepIcon.setImageResource(R.drawable.circuloamarillo)
                                sleepMessage.text = "Excesivo. Dormir demasiado puede afectar la rutina."
                            }

                            if (mensajeSalud.contains("Perímetro cefálico normal")) {
                                weightIcon.setImageResource(R.drawable.circuloverde)
                                weightMessage.text = "Normal"
                            } else if (mensajeSalud.contains("Perímetro cefálico bajo")) {
                                weightIcon.setImageResource(R.drawable.circulorojo)
                                weightMessage.text = "Bajo. Puede requerir consulta médica."
                            } else {
                                weightIcon.setImageResource(R.drawable.circuloamarillo)
                                weightMessage.text = "Elevado. Podría necesitar evaluación médica."
                            }
                        } else {
                            // Si no hay registros, ocultamos las imágenes y ponemos "No hay datos"
                            imcIcon.visibility = View.GONE
                            sleepIcon.visibility = View.GONE
                            weightIcon.visibility = View.GONE

                            imcMessage.text = "No hay datos aún"
                            sleepMessage.text = "No hay datos aún"
                            weightMessage.text = "No hay datos aún"
                        }


                    }
                }
            }


            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }


            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    fun validarSaludBebe(registro: Registro, usuario: Usuario): String {

        val formatoFecha = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val fechaNacimiento: Date = try {
            formatoFecha.parse(usuario.edad_bebe)!!
        } catch (e: Exception) {
            return "Fecha de nacimiento del bebé no válida"
        }

        val fechaActual = Calendar.getInstance().time
        val diferenciaMeses = getEdadEnMeses(fechaNacimiento, fechaActual)
        if (diferenciaMeses < 0 || diferenciaMeses > 36) {
            return "Fecha de nacimiento fuera del rango permitido (0 a 3 años)"
        }
        val alturaMetros = registro.altura / 100
        val imc = registro.peso / (alturaMetros * alturaMetros)
        val mensajeImc = when {
            imc in 12.0f..18.0f -> "IMC normal"
            imc < 12.0f -> "IMC bajo"
            else -> "IMC alto"
        }
        val mensajeSueño = when {

            diferenciaMeses == 0 -> {  // 🔥 Ahora validamos si tiene MENOS de 1 mes con diferencia en días
                val diferenciaDias = (fechaActual.time - fechaNacimiento.time) / (1000 * 60 * 60 * 24)

                when {
                    diferenciaDias < 30 && registro.horasSueño in 16.0f..20.0f -> "Sueño adecuado"
                    diferenciaDias < 30 && registro.horasSueño < 16.0f -> "Sueño insuficiente"
                    diferenciaDias < 30 -> "Sueño excesivo"
                    else -> "Sueño adecuado" // Para bebés de 0 meses, pero de más de 30 días
                }
            }
            diferenciaMeses <= 12 -> when {
                registro.horasSueño in 12.0f..16.0f -> "Sueño adecuado"
                registro.horasSueño < 12.0f -> "Sueño insuficiente"
                else -> "Sueño excesivo"
            }
            else -> when {
                registro.horasSueño in 10.0f..14.0f -> "Sueño adecuado"
                registro.horasSueño < 10.0f -> "Sueño insuficiente"
                else -> "Sueño excesivo"
            }
        }


        val mensajePerimetroCefalico = when {
            diferenciaMeses <= 12 && registro.perimetroCefalico in 34.0f..38.0f -> "Perímetro cefálico normal"
            diferenciaMeses <= 12 && registro.perimetroCefalico < 34.0f -> "Perímetro cefálico bajo"
            diferenciaMeses <= 12 && registro.perimetroCefalico > 38.0f -> "Perímetro cefálico elevado"
            diferenciaMeses in 13..36 && registro.perimetroCefalico in 44.0f..50.0f -> "Perímetro cefálico normal"
            diferenciaMeses in 13..36 && registro.perimetroCefalico < 44.0f -> "Perímetro cefálico bajo"
            diferenciaMeses in 13..36 && registro.perimetroCefalico > 50.0f -> "Perímetro cefálico elevado"
            else -> "Perímetro cefálico fuera del rango esperado para la edad, se recomienda evaluación médica."
        }

        return "$mensajeImc | $mensajeSueño | $mensajePerimetroCefalico"
    }

    // Función para calcular la edad en meses
    fun getEdadEnMeses(fechaNacimiento: Date, fechaActual: Date): Int {
        val calendarNacimiento = Calendar.getInstance()
        calendarNacimiento.time = fechaNacimiento

        val calendarActual = Calendar.getInstance()
        calendarActual.time = fechaActual

        val años = calendarActual.get(Calendar.YEAR) - calendarNacimiento.get(Calendar.YEAR)
        val meses = calendarActual.get(Calendar.MONTH) - calendarNacimiento.get(Calendar.MONTH)
        val dias = calendarActual.get(Calendar.DAY_OF_MONTH) - calendarNacimiento.get(Calendar.DAY_OF_MONTH)

        var edadMeses = años * 12 + meses
        if (dias < 0) edadMeses-- // Si aún no ha cumplido el mes, restamos 1

        return edadMeses
    }

}


