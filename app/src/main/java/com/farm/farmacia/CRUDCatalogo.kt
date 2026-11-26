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

    fun cargar() {
        if (mutableEstado.value.cargando) return
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Cargando desde la API...")
        viewModelScope.launch {
            try {
                val respuesta = Retrofit.api.obtenerMedicamentos()
                diccionario.cargarLista(respuesta.datos)
                publicar("Cargados ${diccionario.obtenerLista().size} medicamentos de la API.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al cargar la API: ${e.message}"
                )
            }
        }
    }

    fun buscar(clave: String): Medicamento? {
        val medicamento = diccionario.buscar(clave)
        mutableEstado.value = mutableEstado.value.copy(
            seleccionado = medicamento?.copy(),
            mensaje = if (medicamento == null) "No se encontró el medicamento '${clave.trim()}'."
                else "Medicamento encontrado."
        )
        return medicamento
    }

    fun guardar(medicamento: Medicamento) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = medicamento.clave_medicamento.trim()
        val existePrevio = diccionario.buscar(claveLimpia) != null

        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Enviando a la API...")
        viewModelScope.launch {
            try {
                if (existePrevio) {
                    Retrofit.api.actualizarMedicamento(claveLimpia, medicamento)
                } else {
                    Retrofit.api.nuevoMedicamento(medicamento)
                }
                val respuesta = Retrofit.api.obtenerMedicamentos()
                diccionario.cargarLista(respuesta.datos)
                mutableEstado.value = mutableEstado.value.copy(seleccionado = medicamento.copy())
                publicar("Guardado con éxito en la API: $claveLimpia")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error en la API al guardar '$claveLimpia': ${e.message}"
                )
            }
        }
    }

    fun eliminar(clave: String) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = clave.trim()
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Eliminando en la API...")

        viewModelScope.launch {
            try {
                Retrofit.api.eliminarMedicamento(claveLimpia)
                val respuesta = Retrofit.api.obtenerMedicamentos()
                diccionario.cargarLista(respuesta.datos)
                mutableEstado.value = mutableEstado.value.copy(seleccionado = null)
                publicar("Eliminado con éxito de la API: $claveLimpia")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error en la API al eliminar '$claveLimpia': ${e.message}"
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
