package com.farm.farmacia

// Jimeno Lorenzo Isaac Orlando

class DiccionarioMedicamentos {
    private val arbol = ArbolBinarioBusqueda()

    fun cargarLista(lista: List<Medicamento>) {
        arbol.limpiar()
        for (med in lista) {
            arbol.insertar(med)
        }
    }

    fun guardar(med: Medicamento) {
        arbol.insertar(med)
    }

    fun buscar(clave: String): Medicamento? {
        return arbol.buscar(clave)
    }

    fun eliminar(clave: String): Boolean {
        return arbol.eliminar(clave)
    }

    fun obtenerLista(): List<Medicamento> {
        return arbol.inorden()
    }
}
