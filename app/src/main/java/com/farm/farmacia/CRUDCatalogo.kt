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

        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Guardando...")
        viewModelScope.launch {
            try {
                val respuesta = if (existePrevio) {
                    Retrofit.api.actualizarMedicamento(claveLimpia, medicamento)
                } else {
                    Retrofit.api.nuevoMedicamento(medicamento)
                }

                if (respuesta.isSuccessful) {
                    diccionario.guardar(medicamento)
                    mutableEstado.value = mutableEstado.value.copy(seleccionado = medicamento.copy())
                    publicar("Medicamento guardado con éxito")
                } else {
                    mutableEstado.value = mutableEstado.value.copy(
                        cargando = false,
                        mensaje = "Error al guardar el medicamento"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al guardar el medicamento"
                )
            }
        }
    }

    fun eliminar(clave: String) {
        if (mutableEstado.value.cargando) return
        val claveLimpia = clave.trim()
        mutableEstado.value = mutableEstado.value.copy(cargando = true, mensaje = "Eliminando...")

        viewModelScope.launch {
            try {
                val respuesta = Retrofit.api.eliminarMedicamento(claveLimpia)
                if (respuesta.isSuccessful) {
                    val eliminado = diccionario.eliminar(claveLimpia)
                    if (eliminado) {
                        mutableEstado.value = mutableEstado.value.copy(seleccionado = null)
                    }
                    publicar("Medicamento eliminado con éxito")
                } else {
                    mutableEstado.value = mutableEstado.value.copy(
                        cargando = false,
                        mensaje = "Error al eliminar el medicamento"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableEstado.value = mutableEstado.value.copy(
                    cargando = false,
                    mensaje = "Error al eliminar el medicamento"
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
