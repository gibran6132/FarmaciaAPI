package com.farm.farmacia

//Jimeno Lorenzo Isaac Orlando

class ArbolBinarioBusqueda {

    class Nodo(
        var medicamento: Medicamento,
        var izquierdo: Nodo? = null,
        var derecho: Nodo? = null
    )

    private var raiz: Nodo? = null

    // Insertar o actualizar un medicamento en el árbol binario de búsqueda por clave
    fun insertar(medicamento: Medicamento) {
        raiz = insertarRecursivo(raiz, medicamento)
    }

    private fun insertarRecursivo(nodo: Nodo?, medicamento: Medicamento): Nodo {
        if (nodo == null) {
            return Nodo(medicamento)
        }
        val comparacion = medicamento.clave_medicamento.compareTo(nodo.medicamento.clave_medicamento, ignoreCase = true)
        if (comparacion < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, medicamento)
        } else if (comparacion > 0) {
            nodo.derecho = insertarRecursivo(nodo.derecho, medicamento)
        } else {
            // Si la clave ya existe, actualizamos el medicamento
            nodo.medicamento = medicamento
        }
        return nodo
    }

    // Buscar un medicamento por clave en el árbol
    fun buscar(clave: String): Medicamento? {
        return buscarRecursivo(raiz, clave.trim())
    }

    private fun buscarRecursivo(nodo: Nodo?, clave: String): Medicamento? {
        if (nodo == null) return null
        val comparacion = clave.compareTo(nodo.medicamento.clave_medicamento, ignoreCase = true)
        return when {
            comparacion == 0 -> nodo.medicamento
            comparacion < 0 -> buscarRecursivo(nodo.izquierdo, clave)
            else -> buscarRecursivo(nodo.derecho, clave)
        }
    }

    // Obtener la lista ordenada mediante recorrido inorden (Izquierdo, Raíz, Derecho)
    fun inorden(): List<Medicamento> {
        val lista = mutableListOf<Medicamento>()
        inordenRecursivo(raiz, lista)
        return lista
    }

    private fun inordenRecursivo(nodo: Nodo?, lista: MutableList<Medicamento>) {
        if (nodo != null) {
            inordenRecursivo(nodo.izquierdo, lista)
            lista.add(nodo.medicamento)
            inordenRecursivo(nodo.derecho, lista)
        }
    }

    // Eliminar un medicamento por clave del árbol
    fun eliminar(clave: String): Boolean {
        var encontrado = false
        fun eliminarRec(nodo: Nodo?, claveBusqueda: String): Nodo? {
            if (nodo == null) return null
            val comp = claveBusqueda.compareTo(nodo.medicamento.clave_medicamento, ignoreCase = true)
            when {
                comp < 0 -> nodo.izquierdo = eliminarRec(nodo.izquierdo, claveBusqueda)
                comp > 0 -> nodo.derecho = eliminarRec(nodo.derecho, claveBusqueda)
                else -> {
                    encontrado = true
                    // Caso 1: Sin hijos o un solo hijo
                    if (nodo.izquierdo == null) return nodo.derecho
                    if (nodo.derecho == null) return nodo.izquierdo
                    // Caso 2: Dos hijos, obtener el sucesor inorden (menor del subárbol derecho)
                    var sucesorPadre = nodo
                    var sucesor = nodo.derecho
                    while (sucesor?.izquierdo != null) {
                        sucesorPadre = sucesor
                        sucesor = sucesor.izquierdo
                    }
                    if (sucesor != null) {
                        nodo.medicamento = sucesor.medicamento
                        if (sucesorPadre == nodo) {
                            nodo.derecho = eliminarRec(nodo.derecho, sucesor.medicamento.clave_medicamento)
                        } else {
                            sucesorPadre.izquierdo = eliminarRec(sucesorPadre.izquierdo, sucesor.medicamento.clave_medicamento)
                        }
                    }
                }
            }
            return nodo
        }
        raiz = eliminarRec(raiz, clave.trim())
        return encontrado
    }

    fun limpiar() {
        raiz = null
    }
}
