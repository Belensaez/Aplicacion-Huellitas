package com.example.huellitas

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var botonLogin: Button
    private lateinit var botonRegistro: Button
    private var selectedImageUri: Uri? = null
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1
    private val IMAGE_PICK_REQUEST_CODE = 100

    private lateinit var dialogoVista: View // Hacerlo accesible en toda la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bienvenida)
        db = DatabaseHelper(this)
        checkPermissions()
        botonLogin = findViewById(R.id.boton_login)
        botonRegistro = findViewById(R.id.boton_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botonRegistro.setOnClickListener {
            dialogoCrearJugador()
        }
        botonLogin.setOnClickListener {
            dialogoIniciarSesion()
        }
    }
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido

                }
                else -> {
                    // Solicitar permiso
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                }
            }
        }
    }



    private fun dialogoCrearJugador() {
        val dialogoVista = layoutInflater.inflate(R.layout.crear_usuario, null)
        val nombreEdit: EditText = dialogoVista.findViewById(R.id.nombreEditar)
        val contraseñaEdit: EditText = dialogoVista.findViewById(R.id.altura)
        val nombreBebeEdit: EditText = dialogoVista.findViewById(R.id.perimetro)
        val emailEdit: EditText = dialogoVista.findViewById(R.id.titulo)
        val edadBebeEdit  = dialogoVista.findViewById<EditText>(R.id.unidadesComida)
        val selectImageBtn: Button = dialogoVista.findViewById(R.id.botonAñadirRegistro)
       // val previewImageView: ImageView = dialogoVista.findViewById(R.id.imagen)

        selectImageBtn.setOnClickListener {
           seleccionarImagen()
        }


        edadBebeEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val calendarMin = Calendar.getInstance()
            calendarMin.add(Calendar.YEAR, -3)


            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Configurar la fecha seleccionada
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val formattedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                edadBebeEdit.setText(formattedDate)

                                                          }, year, month, day)
            val today = Calendar.getInstance()
            datePickerDialog.datePicker.maxDate = today.timeInMillis  // Limitar a la fecha actual
            datePickerDialog.datePicker.minDate = calendarMin.timeInMillis
            datePickerDialog.show()
        }
        val dialogo = AlertDialog.Builder(this)
            .setView(dialogoVista)

            .setPositiveButton("Crear usuario") { _, _ ->
                val nombre = nombreEdit.text.toString()
                val contraseña = contraseñaEdit.text.toString()
                val nombreBebe = nombreBebeEdit.text.toString()
                val email = emailEdit.text.toString()
                val fechaNacimientoBebe = edadBebeEdit.text.toString()

                if (nombre.isNotBlank() && nombreBebe.isNotBlank() && contraseña.isNotBlank() && fechaNacimientoBebe.isNotBlank() && email.isNotBlank()) {
                    var emailYaExiste = false
                    val listaUsuarios = db.getAllUsuarios()
                    for (usuario in listaUsuarios) {
                            if (usuario.email == email) {
                                emailYaExiste = true
                                break // Salir del bucle si ya encontramos el jugador
                            }
                        }
                    if (emailYaExiste) {
                        Toast.makeText(this, "Este correo electrónico ya existe", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        val imagePath = selectedImageUri?.let { guardarImagenEnInterno(it, email) } ?: guardarImagenPorDefecto(email)

                        val usuario = Usuario(
                            nombre = nombre,
                            contraseña = contraseña,
                            email = email,
                            nombre_bebe = nombreBebe,
                            edad_bebe = fechaNacimientoBebe,
                            imagen = imagePath
                        )
                        db.insertUsuario(usuario)
                        Toast.makeText(this, "Usuario añadido", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Rellenar todos los campos", Toast.LENGTH_LONG).show()
                }

            }

            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialogo.setOnShowListener {
            // Acceder a los botones
            val positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Cambiar el fondo del botón positivo
            val buttonPanel =
                dialogo.findViewById<View>(com.google.android.material.R.id.buttonPanel)
            buttonPanel?.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Cambiar el fondo del botón negativo
            negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        dialogo.show()
    }

    private fun dialogoIniciarSesion() {
        val dialogoVista = layoutInflater.inflate(R.layout.iniciar_sesion, null)
        val contraseñaEdit: EditText = dialogoVista.findViewById(R.id.altura)
        val emailEdit: EditText = dialogoVista.findViewById(R.id.titulo)
        val dialogo = AlertDialog.Builder(this)
            .setView(dialogoVista)

            .setPositiveButton("Entrar") { _, _ ->
                val contraseña = contraseñaEdit.text.toString()
                val email = emailEdit.text.toString()
                var verificacion = false
                if (email.isNotBlank() && contraseña.isNotBlank()) {
                    verificacion = db.verificarUsuario(email, contraseña)
                    if (verificacion) {

                        var usuario = db.getUsuarioPorEmail(email);
                        val fechaNacimientoBebe: String? = usuario?.edad_bebe;
                        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                        fechaNacimientoBebe?.let {
                            val fechaNacimiento = formatoFecha.parse(it);
                            val calendarNacimiento = Calendar.getInstance();
                            calendarNacimiento.time = fechaNacimiento;

                            val calendarActual = Calendar.getInstance();
                            val diff =
                                calendarActual.timeInMillis - calendarNacimiento.timeInMillis;

                            val days = TimeUnit.MILLISECONDS.toDays(diff).toInt();
                            val weeks = days / 7;
                            val months = (days / 30.44).toInt();
                            val years = months / 12;
                            val remainingMonths = months % 12;

                            val edadMensaje = when {
                                days < 7 -> "$days ${if (days == 1) "día" else "días"}"
                                weeks < 4 -> "$weeks ${if (weeks == 1) "semana" else "semanas"} y ${days % 7} ${if (days % 7 == 1) "día" else "días"}"
                                months < 12 -> "$months ${if (months == 1) "mes" else "meses"} y ${(days - (months * 30.44)).toInt()} ${if ((days - (months * 30.44)).toInt() == 1) "día" else "días"}"
                                years == 1 -> if (remainingMonths > 0) "1 año y $remainingMonths ${if (remainingMonths == 1) "mes" else "meses"}" else "1 año"
                                else -> if (remainingMonths > 0) "$years años y $remainingMonths ${if (remainingMonths == 1) "mes" else "meses"}" else "$years años"
                            }

                            val intent = Intent(this, Principal::class.java);
                            intent.putExtra("email", email);
                            intent.putExtra("edadBebe", edadMensaje);
                            startActivity(intent);
                        }



                } else {

                        Toast.makeText(this, "Usuario incorrecto", Toast.LENGTH_LONG).show()

                }
            } else {
            Toast.makeText(this, "Rellenar todos los campos", Toast.LENGTH_LONG).show()
        }
    }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialogo.setOnShowListener {
            // Acceder a los botones
            val positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Cambiar el fondo del botón positivo
            val buttonPanel =
                dialogo.findViewById<View>(com.google.android.material.R.id.buttonPanel)
            buttonPanel?.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Cambiar el fondo del botón negativo
            negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        dialogo.show()
    }
    private fun guardarImagenEnInterno(uri: Uri, email: String): String? {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val nombreArchivo = "imagen_${email.replace("@", "_").replace(".", "_")}.jpg"
            val directorio = getDir("imagenes", Context.MODE_PRIVATE)

            if (!directorio.exists()) {
                directorio.mkdirs()
            }

            val archivoImagen = File(directorio, nombreArchivo)

            FileOutputStream(archivoImagen).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // ✅ Guarda la ruta COMPLETA en la base de datos
            val rutaCompleta = archivoImagen.absolutePath
            db.actualizarImagenUsuario(email, rutaCompleta)

            Log.d("DEBUG_IMAGEN", "Imagen guardada en: $rutaCompleta")
            return rutaCompleta
        } catch (e: IOException) {
            Log.e("DEBUG_IMAGEN", "Error al guardar imagen", e)
            return null
        }
    }

    private fun seleccionarImagen() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    // Método para manejar la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data // Guardar la URI seleccionada

            if (selectedImageUri != null) {
                Toast.makeText(this, "Imagen seleccionada correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun guardarImagenPorDefecto(email: String): String {
        val nombreArchivo = "imagen_${email.replace("@", "_").replace(".", "_")}.jpg"
        val directorio = getDir("imagenes", Context.MODE_PRIVATE)
        val archivoImagen = File(directorio, nombreArchivo)

        if (!archivoImagen.exists()) {
            try {
                // Cargar la imagen en un tamaño reducido
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 1 // Reduce el tamaño de la imagen (ajustable)
                }
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.nopuedomas, options)

                val outputStream = FileOutputStream(archivoImagen)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Reducir calidad

                outputStream.flush()
                outputStream.close()

                return archivoImagen.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return archivoImagen.absolutePath
    }



}