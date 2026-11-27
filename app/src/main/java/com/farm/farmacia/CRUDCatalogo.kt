package com.farm.farmacia
//Flores Vera Cristian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

data class CatalogoEstado(
    val medicamentos: List<Medicamento> = emptyList(),
    val mensaje: String = "",
    val cargando: Boolean = false,
    val seleccionado: Medicamento? = null
)

class CRUDCatalogo : ViewModel() {
    private val diccionario = DiccionarioMedicamentos()
    private val mutableEstado = MutableStateFlow(CatalogoEstado())
    val estado = mutableEstado.asStateFlow()

    init { cargar() }

    // GET: Cargar catálogo desde la API
    fun cargar() {
        if (mutableEstado.value.cargando) return
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Cargando desde la API...")
        viewModelScope.launch {
            try {
                val respuesta = Retrofit.api.obtenerMedicamentos()
                diccionario.cargarLista(respuesta.datos)
                publicar("Cargados ${diccionario.obtenerLista().size} medicamentos desde la API.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al conectar con la API: ${e.message}"
                )
            }
        }
    }

    // BUSCAR: Búsqueda por clave en el Árbol Binario
    fun buscar(clave: String): Medicamento? {
        val medicamento = diccionario.buscar(clave)
        mutableEstado.value = mutableEstado.value.copy(
            seleccionado = medicamento?.copy(),
            mensaje = if (medicamento == null) "No se encontró el medicamento '${clave.trim()}'."
                else "Medicamento encontrado en el árbol."
        )
        return medicamento
    }

    // POST / PUT: Guardar/Actualizar en la API
    fun guardar(medicamento: Medicamento) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = medicamento.clave_medicamento.trim()
        val existePrevio = diccionario.buscar(claveLimpia) != null

        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Enviando HTTP a la API...")
        viewModelScope.launch {
            try {
                val respuesta: Response<ResponseBody> = if (existePrevio) {
                    Retrofit.api.actualizarMedicamento(claveLimpia, medicamento)
                } else {
                    Retrofit.api.nuevoMedicamento(medicamento)
                }
                val codigo = respuesta.code()
                val respuestaTexto = respuesta.body()?.string() ?: ""

                if (respuesta.isSuccessful) {
                    diccionario.guardar(medicamento)
                    mutableEstado.value = mutableEstado.value.copy(seleccionado = medicamento.copy())
                    publicar("HTTP $codigo [${if (existePrevio) "PUT" else "POST"} OK]: $claveLimpia | Servidor: $respuestaTexto")
                } else {
                    val errorTexto = respuesta.errorBody()?.string() ?: "Error de servidor"
                    mutableEstado.value = mutableEstado.value.copy(
                        cargando = false,
                        mensaje = "HTTP $codigo [Error API]: $errorTexto"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error de red al conectar con la API: ${e.message}"
                )
            }
        }
    }

    // DELETE: Eliminar en la API
    fun eliminar(clave: String) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = clave.trim()
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Enviando DELETE HTTP a la API...")

        viewModelScope.launch {
            try {
                val respuesta = Retrofit.api.eliminarMedicamento(claveLimpia)
                val codigo = respuesta.code()
                val respuestaTexto = respuesta.body()?.string() ?: ""

                if (respuesta.isSuccessful) {
                    val eliminado = diccionario.eliminar(claveLimpia)
                    if (eliminado) {
                        mutableEstado.value = mutableEstado.value.copy(seleccionado = null)
                    }
                    publicar("HTTP $codigo [DELETE OK]: $claveLimpia | Servidor: $respuestaTexto")
                } else {
                    val errorTexto = respuesta.errorBody()?.string() ?: "Error de servidor"
                    mutableEstado.value = mutableEstado.value.copy(
                        cargando = false,
                        mensaje = "HTTP $codigo [Error DELETE]: $errorTexto"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error de red al eliminar en la API: ${e.message}"
                )
            }
        }
    }

    private fun publicar(mensaje: String) {
        val seleccion = mutableEstado.value.seleccionado?.clave_medicamento
        mutableEstado.value = CatalogoEstado(
            medicamentos = diccionario.obtenerLista(),
            mensaje = mensaje,
            cargando = false,
            seleccionado = seleccion?.let { diccionario.buscar(it)?.copy() }
        )
    }
}
