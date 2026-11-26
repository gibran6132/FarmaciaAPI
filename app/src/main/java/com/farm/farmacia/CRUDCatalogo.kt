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
                    cargando = false, mensaje = "No se pudo cargar el catálogo. Revisa tu conexión e intenta de nuevo."
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
        diccionario.guardar(medicamento)
        mutableEstado.value = mutableEstado.value.copy(seleccionado = medicamento.copy())
        publicar("Guardado en el diccionario: ${medicamento.clave_medicamento}")
    }

    fun eliminar(clave: String) {
        if (mutableEstado.value.cargando) return
        val eliminado = diccionario.eliminar(clave)
        if (eliminado) mutableEstado.value = mutableEstado.value.copy(seleccionado = null)
        publicar(if (eliminado) "Eliminado del diccionario: ${clave.trim()}"
            else "No se encontró el medicamento '${clave.trim()}'.")
    }

    private fun publicar(mensaje: String) {
        val seleccion = mutableEstado.value.seleccionado?.clave_medicamento
        mutableEstado.value = CatalogoEstado(
            medicamentos = diccionario.obtenerLista().sortedBy { it.clave_medicamento },
            mensaje = mensaje,
            seleccionado = seleccion?.let { diccionario.buscar(it)?.copy() }
        )
    }
}
