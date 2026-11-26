package com.farm.farmacia
//Jimeno Lorenzo Isaac Orlando
class DiccionarioMedicamentos {

    // Estructura de Datos: Árbol Binario de Búsqueda por clave
    private val arbol = ArbolBinarioBusqueda()

    // Cargar la lista obtenida del JSON al árbol
    fun cargarLista(lista: List<Medicamento>) {
        arbol.limpiar()
        for (med in lista) {
            arbol.insertar(med)
        }
    }

    // CREATE / UPDATE: Guardar o actualizar medicamento en el árbol
    fun guardar(med: Medicamento) {
        arbol.insertar(med)
    }

    // READ: Consultar por clave en el árbol
    fun buscar(clave: String): Medicamento? {
        return arbol.buscar(clave)
    }

    // DELETE: Eliminar por clave del árbol
    fun eliminar(clave: String): Boolean {
        return arbol.eliminar(clave)
    }

    // Listado obtenido con recorrido inorden
    fun obtenerLista(): List<Medicamento> {
        return arbol.inorden()
    }
}
