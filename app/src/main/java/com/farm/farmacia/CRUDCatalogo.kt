package com.farm.farmacia

// Flores Vera Cristian

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

    init {
        cargar()
    }

    fun cargar() {
        if (mutableEstado.value.cargando) return
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Cargando...")
        viewModelScope.launch {
            try {
                val respuesta = Retrofit.api.obtenerMedicamentos()
                diccionario.cargarLista(respuesta.datos)
                publicar("Catálogo cargado con éxito")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al cargar datos"
                )
            }
        }
    }

    fun buscar(clave: String): Medicamento? {
        val medicamento = diccionario.buscar(clave)
        mutableEstado.value = mutableEstado.value.copy(
            seleccionado = medicamento?.copy(),
            mensaje = if (medicamento == null) "No se encontró el medicamento"
                else "Medicamento encontrado"
        )
        return medicamento
    }

    fun guardar(medicamento: Medicamento) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = medicamento.clave_medicamento.trim()
        val existePrevio = diccionario.buscar(claveLimpia) != null

        diccionario.guardar(medicamento)
        mutableEstado.value = mutableEstado.value.copy(
            seleccionado = medicamento.copy(),
            cargando = true,
            mensaje = "Guardando..."
        )

        viewModelScope.launch {
            try {
                if (existePrevio) {
                    Retrofit.api.actualizarMedicamento(claveLimpia, medicamento)
                } else {
                    Retrofit.api.nuevoMedicamento(medicamento)
                }
            } catch (_: Exception) {
                // Si la red falla, se mantiene guardado localmente en el árbol
            } finally {
                publicar("Medicamento guardado con éxito")
            }
        }
    }

    fun eliminar(clave: String) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = clave.trim()
        val eliminadoLocal = diccionario.eliminar(claveLimpia)

        if (!eliminadoLocal) {
            mutableEstado.value = mutableEstado.value.copy(
                mensaje = "No se encontró el medicamento"
            )
            return
        }

        mutableEstado.value = mutableEstado.value.copy(
            seleccionado = null,
            cargando = true,
            mensaje = "Eliminando..."
        )

        viewModelScope.launch {
            try {
                Retrofit.api.eliminarMedicamento(claveLimpia)
            } catch (_: Exception) {
                // Si la red o API responde 404, la eliminación local ya se realizó
            } finally {
                publicar("Medicamento eliminado con éxito")
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
