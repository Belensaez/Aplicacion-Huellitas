package com.example.huellitas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.huellitas.R.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class Principal : AppCompatActivity(){
    private lateinit var db: DatabaseHelper
    private var selectedImageUri: Uri? = null
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1
    private val IMAGE_PICK_REQUEST_CODE = 100
    private var imagenActualPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.principal)
        db = DatabaseHelper(this)

        var nombreBebeText = findViewById<TextView>(id.nombreBebe)
        var edadBebeText = findViewById<TextView>(id.edadBebe)
        var imagen = findViewById<ImageView>(id.fotoBebe)
        var cardCrecimiento = findViewById<CardView>(id.cardViewCrecimiento)
        var cardCalendario = findViewById<CardView>(id.cardViewCalendario)
        var cardSobre = findViewById<CardView>(id.cardViewSobre)
        var editarPerfil = findViewById<ImageView>(id.editarPerfil)
        var email = intent.getStringExtra("email") ?: "nulo"
        var edadBebeIntent = intent.getStringExtra("edadBebe") ?: "nulo"
        var usuario= db.getUsuarioPorEmail(email)
        val nombreBebe = usuario?.nombre_bebe
        imagenActualPath = usuario?.imagen

        // Grosor del borde
        editarPerfil.setOnClickListener {
            mostrarDialogoEditarPerfil()
        }
        cardCrecimiento.setOnClickListener {
            val intent = Intent(this, CrecimientoYProgreso::class.java)
            intent.putExtra("email", email)
            // Pasar el objeto Usuario
            startActivity(intent)
        }
        cardCalendario.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            intent.putExtra("email", email)
            // Pasar el objeto Usuario
            startActivity(intent)
        }

        cardSobre.setOnClickListener{
            dialogoAcercaDe()
        }
        if (usuario != null && usuario.imagen.isNotEmpty()) {
            val imagenGuardada = File(usuario.imagen)

            if (imagenGuardada.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagenGuardada.absolutePath)
                imagen.setImageBitmap(bitmap) // ✅ Reemplaza 'imageView' con el ID correcto de tu ImageView
            } else {
                Log.e("DEBUG_IMAGEN", "La imagen no existe en: ${usuario.imagen}")
            }
        } else {
            Log.e("DEBUG_IMAGEN", "No se encontró imagen en la base de datos.")
        }


        nombreBebeText.text=nombreBebe
        edadBebeText.text=edadBebeIntent

    }
    private fun mostrarDialogoEditarPerfil() {
        val dialogView = layoutInflater.inflate(R.layout.editar_perfil, null) // Infla el layout del diálogo
        var email = intent.getStringExtra("email") ?: "nulo"
        var usuario= db.getUsuarioPorEmail(email)
        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditar)
        val contraseñaEditText = dialogView.findViewById<EditText>(R.id.contraseñaEditar)
        val nombreBebeEditText = dialogView.findViewById<EditText>(R.id.nombreBebeEditar)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditar)
        val fechaNacimientoEditText = dialogView.findViewById<EditText>(R.id.fechaBebeEditar)
        val selectImageBtn: Button = dialogView.findViewById(R.id.btnImagen)
        // val previewImageView: ImageView = dialogoVista.findViewById(R.id.imagen)

        selectImageBtn.setOnClickListener {
            seleccionarImagen()
        }
        // Pre-cargar los datos actuales del usuario en el diálogo
        nombreEditText.setText(usuario!!.nombre)
        contraseñaEditText.setText(usuario.contraseña)
        nombreBebeEditText.setText(usuario.nombre_bebe)
        emailEditText.setText(usuario.email)
        fechaNacimientoEditText.setText(usuario.edad_bebe)
        if (selectedImageUri != null) {
            val nuevaRutaImagen = guardarImagenEnInterno(selectedImageUri!!, usuario!!.email)
            if (nuevaRutaImagen != null) {
                usuario.imagen = nuevaRutaImagen
                imagenActualPath = nuevaRutaImagen // Actualiza la ruta actual
                val imagen = findViewById<ImageView>(id.fotoBebe)
                val imagenGuardada = File(nuevaRutaImagen)

                if (imagenGuardada.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imagenGuardada.absolutePath)
                    imagen.setImageBitmap(bitmap) // ✅ Reemplaza 'imageView' con el ID correcto de tu ImageView
                } else {
                    Log.e("DEBUG_IMAGEN", "La imagen no existe en: ${usuario.imagen}")
                }
            }
        }


        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                // Obtener los nuevos valores de los EditTexts
                val nuevoNombre = nombreEditText.text.toString()
                val nuevoNombreBebe = nombreBebeEditText.text.toString()
                val nuevaContraseña = contraseñaEditText.text.toString()
                val nuevoEmail = emailEditText.text.toString()
                val nuevaFechaNacimiento = fechaNacimientoEditText.text.toString()


                // Actualizar los datos del usuario en la base de datos
                usuario.nombre = nuevoNombre
                usuario.contraseña = nuevaContraseña
                usuario.email = nuevoEmail
                usuario.nombre_bebe = nuevoNombreBebe
                usuario.edad_bebe = nuevaFechaNacimiento
                if (selectedImageUri != null) {
                    val nuevaRutaImagen = guardarImagenEnInterno(selectedImageUri!!, usuario.email)
                    if (nuevaRutaImagen != null) {
                        usuario.imagen = nuevaRutaImagen
                        imagenActualPath = nuevaRutaImagen
                    }
                }

                db.actualizarUsuario(usuario) // Debes implementar esta función en tu DatabaseHelper

                // Actualizar las vistas con los nuevos datos
                var nombreBebeText = findViewById<TextView>(id.nombreBebe)
                var edadBebeText = findViewById<TextView>(id.edadBebe)
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                var edadMensaje: String? =null
                nuevaFechaNacimiento?.let {
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

                    edadMensaje = when {
                        days < 7 -> "$days ${if (days == 1) "día" else "días"}"
                        weeks < 4 -> "$weeks ${if (weeks == 1) "semana" else "semanas"} y ${days % 7} ${if (days % 7 == 1) "día" else "días"}"
                        months < 12 -> "$months ${if (months == 1) "mes" else "meses"} y ${(days - (months * 30.44)).toInt()} ${if ((days - (months * 30.44)).toInt() == 1) "día" else "días"}"
                        years == 1 -> if (remainingMonths > 0) "1 año y $remainingMonths ${if (remainingMonths == 1) "mes" else "meses"}" else "1 año"
                        else -> if (remainingMonths > 0) "$years años y $remainingMonths ${if (remainingMonths == 1) "mes" else "meses"}" else "$years años"
                    }
                }

                nombreBebeText.text=nuevoNombreBebe
                edadBebeText.text=edadMensaje
                val imagen = findViewById<ImageView>(id.fotoBebe)
                if (imagenActualPath != null) {
                    val bitmap = BitmapFactory.decodeFile(imagenActualPath)
                    imagen.setImageBitmap(bitmap)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                selectedImageUri = null
            }

        val dialog = builder.create()
        dialog.setOnShowListener {
            // Acceder a los botones
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Cambiar el fondo del botón positivo
            val buttonPanel =
                dialog.findViewById<View>(com.google.android.material.R.id.buttonPanel)
            buttonPanel?.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Cambiar el fondo del botón negativo
            negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        dialog.show()
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
    private fun dialogoAcercaDe() {
        val dialogoVista = layoutInflater.inflate(R.layout.sobre_huellitas, null)

        val dialogo = AlertDialog.Builder(this)
            .setView(dialogoVista)
            .setPositiveButton("Aceptar") { _, _ -> }
            .create()

        dialogo.setOnShowListener {
            // Acceder al botón positivo
            val positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)

            // Cambiar el estilo del botón
            val buttonPanel = dialogo.findViewById<View>(com.google.android.material.R.id.buttonPanel)
            buttonPanel?.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.marron))
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        dialogo.show()
    }

}