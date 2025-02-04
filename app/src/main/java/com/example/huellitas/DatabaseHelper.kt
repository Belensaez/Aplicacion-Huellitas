package com.example.huellitas



import Registro
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "huellitas.db"
        private const val DATABASE_VERSION = 15
        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "nombre"
        const val COLUMN_CONTRASEÑA = "contraseña"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_NAME_BEBE = "nombre_bebe"
        const val COLUMN_EDAD_BEBE = "edad_bebe"
        const val COLUMN_IMAGEN = "imagen"


        const val TABLE_REGISTROS = "registros"
        const val COLUMN_ID_REGISTRO = "idRegistro"
        const val COLUMN_ID_USER_REGISTRO = "idUserRegistro"
        const val COLUMN_PESO = "peso"
        const val COLUMN_ALTURA = "altura"
        const val COLUMN_PERIMETRO_CEFALICO = "perimetroCefalico"
        const val COLUMN_HORAS_SUEÑO = "horasSueño"
        const val COLUMN_CANTIDAD_COMIDAS = "cantidadComidas"
        const val COLUMN_FECHA = "fecha"


        const val TABLE_EVENTS = "events"
        const val COLUMN_ID_EVENTS = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DESCRIPTION = "description"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $TABLE_USUARIOS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NAME TEXT,$COLUMN_CONTRASEÑA TEXT,$COLUMN_EMAIL TEXT, $COLUMN_NAME_BEBE TEXT,$COLUMN_EDAD_BEBE TEXT,$COLUMN_IMAGEN TEXT)"
        db.execSQL(createTable)
        val createTableQuery = """
        CREATE TABLE IF NOT EXISTS registros (
            idRegistro INTEGER PRIMARY KEY AUTOINCREMENT,
            idUserRegistro INTEGER,
            peso FLOAT,
            altura FLOAT,
            perimetroCefalico FLOAT,
            horasSueño FLOAT,
            cantidadComidas INTEGER,
            fecha TEXT,
            FOREIGN KEY(idUserRegistro) REFERENCES usuarios(id) ON DELETE CASCADE
        )
    """
        db.execSQL(createTableQuery)
        val createTableEvents =
            "CREATE TABLE $TABLE_EVENTS ($COLUMN_ID_EVENTS INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_DATE TEXT,$COLUMN_TYPE TEXT,$COLUMN_DESCRIPTION TEXT)"
        db.execSQL(createTableEvents)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REGISTROS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")



        onCreate(db)
    }

    fun actualizarImagenUsuario(email: String, nombreImagen: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("imagen", nombreImagen) // Asegúrate de que la columna "imagen" existe

        db.update("usuarios", values, "email = ?", arrayOf(email))
        db.close()
    }

    fun verificarUsuario(email: String, contraseña: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM usuarios WHERE email = ? AND contraseña = ?",
            arrayOf(email, contraseña)
        )
        cursor.moveToFirst()
        val existe = cursor.getInt(0) > 0
        cursor.close()
        return existe
    }

    fun getAllUsuarios(): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USUARIOS", null)
        if (cursor.moveToFirst()) {
            do {
                val usuario = Usuario(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    contraseña = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRASEÑA)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    nombre_bebe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_BEBE)),
                    edad_bebe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDAD_BEBE)),
                    imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN))

                )

                usuarios.add(usuario)
            } while (cursor.moveToNext())
        }
        cursor.close() //he cerrado el cursor aqui
        return usuarios
    }

    fun getAllRegistrosPorIdUser(idUser: Int): MutableList<Registro> {
        val registros = mutableListOf<Registro>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_REGISTROS WHERE $COLUMN_ID_USER_REGISTRO = ? ORDER BY $COLUMN_FECHA DESC",
            arrayOf(idUser.toString())
        )

        try {
            while (cursor.moveToNext()) {
                val registro = Registro(
                    idRegistro = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_REGISTRO)),
                    idUserRegistro = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_ID_USER_REGISTRO
                        )
                    ),
                    peso = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PESO)),
                    altura = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ALTURA)),
                    perimetroCefalico = cursor.getFloat(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_PERIMETRO_CEFALICO
                        )
                    ),
                    horasSueño = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HORAS_SUEÑO)),
                    cantidadComidas = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_CANTIDAD_COMIDAS
                        )
                    ),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
                )
                registros.add(registro)
            }
        } finally {
            cursor.close()
            db.close()
        }

        return registros
    }


    fun getUsuarioPorEmail(email: String): Usuario? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USUARIOS WHERE $COLUMN_EMAIL = ?",
            arrayOf(email) // Parámetro de búsqueda, reemplaza '?' por el valor de email
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                contraseña = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRASEÑA)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                nombre_bebe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_BEBE)),
                edad_bebe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDAD_BEBE)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN))

            )
        }
        cursor.close() // Cerrar el cursor después de su uso
        return usuario
    }

    fun insertUsuario(usuario: Usuario): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, usuario.nombre)
            put(COLUMN_CONTRASEÑA, usuario.contraseña)
            put(COLUMN_EMAIL, usuario.email)
            put(COLUMN_NAME_BEBE, usuario.nombre_bebe)
            put(COLUMN_EDAD_BEBE, usuario.edad_bebe)
            put(COLUMN_IMAGEN, usuario.imagen)

        }
        return db.insert(TABLE_USUARIOS, null, values)
    }

    fun insertRegistro(registro: Registro): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(
                COLUMN_ID_USER_REGISTRO,
                registro.idUserRegistro
            ) // Asociar el registro con un usuario
            put(COLUMN_PESO, registro.peso)
            put(COLUMN_ALTURA, registro.altura)
            put(COLUMN_PERIMETRO_CEFALICO, registro.perimetroCefalico)
            put(COLUMN_HORAS_SUEÑO, registro.horasSueño)
            put(COLUMN_CANTIDAD_COMIDAS, registro.cantidadComidas)
            put(COLUMN_FECHA, registro.fecha)
        }
        return db.insert(TABLE_REGISTROS, null, values)
    }

    fun insertEvent(date: String, type: String, description: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_TYPE, type)
            put(COLUMN_DESCRIPTION, description)
        }
        db.insert(TABLE_EVENTS, null, values)
        db.close()
    }

    // ...

    fun getEvents(): List<Event> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EVENTS", null)
        val events = mutableListOf<Event>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val eventType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            events.add(Event(id, date, eventType, description))
        }

        cursor.close()
        db.close()
        return events
    }

    fun getEventsByType(eventType: String): List<Event> {
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT * FROM $TABLE_EVENTS WHERE $COLUMN_TYPE = ?", arrayOf(eventType))
        val events = mutableListOf<Event>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            events.add(Event(id, date, type, description))
        }

        cursor.close()
        db.close()
        return events
    }

    fun eliminarEvento(eventId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("events", "id=?", arrayOf(eventId.toString()))
        db.close()
        return result > 0 // Devuelve true si se eliminó correctamente
    }

    fun eliminarRegistro(eventId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("registros", "idRegistro=?", arrayOf(eventId.toString()))
        db.close()
        return result > 0 // Devuelve true si se eliminó correctamente
    }

    fun actualizarUsuario(usuario: Usuario) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, usuario.nombre)
            put(COLUMN_CONTRASEÑA, usuario.contraseña)
            put(COLUMN_EMAIL, usuario.email)
            put(COLUMN_NAME_BEBE, usuario.nombre_bebe)
            put(COLUMN_EDAD_BEBE, usuario.edad_bebe)
            put(COLUMN_IMAGEN, usuario.imagen)
            // ... otros campos
        }
        db.update(TABLE_USUARIOS, values, "$COLUMN_ID = ?", arrayOf(usuario.id.toString()))
        db.close()
    }

    fun obtenerUltimoRegistro(idUsuario: Int): Registro? {
        val db = this.readableDatabase
        // Consulta para obtener el último registro de un usuario específico, ordenado por fecha (descendente)
        val query = """ SELECT * FROM $TABLE_REGISTROS WHERE $COLUMN_ID_USER_REGISTRO = ? ORDER BY $COLUMN_ID_REGISTRO DESC LIMIT 1 """
        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))

        var ultimoRegistro: Registro? = null

        if (cursor.moveToFirst()) {
            // Si encontramos el último registro, creamos un objeto Registro
            ultimoRegistro = Registro(
                idRegistro = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_REGISTRO)),
                idUserRegistro = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_USER_REGISTRO)),
                peso = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PESO)),
                altura = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ALTURA)),
                perimetroCefalico = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PERIMETRO_CEFALICO)),
                horasSueño = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HORAS_SUEÑO)),
                cantidadComidas = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD_COMIDAS)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
            )
        }

        cursor.close()
        db.close()

        return ultimoRegistro
    }
}