package com.farm.farmacia
//Flores Vera Cristian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    // POST / PUT: Guardar/Actualizar en la API y refrescar en el árbol
    fun guardar(medicamento: Medicamento) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = medicamento.clave_medicamento.trim()
        val existePrevio = diccionario.buscar(claveLimpia) != null

        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Enviando datos a la API...")
        viewModelScope.launch {
            try {
                if (existePrevio) {
                    // PUT al servidor
                    Retrofit.api.actualizarMedicamento(claveLimpia, medicamento)
                } else {
                    // POST al servidor
                    Retrofit.api.nuevoMedicamento(medicamento)
                }
                // Actualizamos el árbol local con la nueva información
                diccionario.guardar(medicamento)
                mutableEstado.value = mutableEstado.value.copy(seleccionado = medicamento.copy())
                publicar("Operación (POST/PUT) exitosa en la API para: $claveLimpia")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al enviar a la API: ${e.message}"
                )
            }
        }
    }

    // DELETE: Eliminar en la API y remover del árbol
    fun eliminar(clave: String) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = clave.trim()
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Enviando DELETE a la API...")

        viewModelScope.launch {
            try {
                // DELETE al servidor
                Retrofit.api.eliminarMedicamento(claveLimpia)
                val eliminado = diccionario.eliminar(claveLimpia)
                if (eliminado) {
                    mutableEstado.value = mutableEstado.value.copy(seleccionado = null)
                }
                publicar("Eliminado con éxito (DELETE) de la API: $claveLimpia")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al eliminar en la API: ${e.message}"
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
