import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Registro(
    val idRegistro: Int? = null, // ID autoincremental
    val idUserRegistro: Int, // Relación con el usuario
    val peso: Float,
    val altura: Float,
    val perimetroCefalico: Float,
    val horasSueño: Float,
    val cantidadComidas: Int,
    val fecha: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Obtener la fecha directamente

)


