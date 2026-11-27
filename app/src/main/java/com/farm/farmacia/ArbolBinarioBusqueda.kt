package com.farm.farmacia

// Jimeno Lorenzo Isaac Orlando

class ArbolBinarioBusqueda {

    class Nodo(
        var medicamento: Medicamento,
        var izquierdo: Nodo? = null,
        var derecho: Nodo? = null
    )

    private var raiz: Nodo? = null

    fun insertar(medicamento: Medicamento) {
        raiz = insertarRecursivo(raiz, medicamento)
    }

    private fun insertarRecursivo(nodo: Nodo?, medicamento: Medicamento): Nodo {
        if (nodo == null) return Nodo(medicamento)

        val comp = medicamento.clave_medicamento.compareTo(nodo.medicamento.clave_medicamento, ignoreCase = true)
        if (comp < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, medicamento)
        } else if (comp > 0) {
            nodo.derecho = insertarRecursivo(nodo.derecho, medicamento)
        } else {
            nodo.medicamento = medicamento
        }
        return nodo
    }

    fun buscar(clave: String): Medicamento? {
        return buscarRecursivo(raiz, clave.trim())
    }

    private fun buscarRecursivo(nodo: Nodo?, clave: String): Medicamento? {
        if (nodo == null) return null
        val comp = clave.compareTo(nodo.medicamento.clave_medicamento, ignoreCase = true)
        return when {
            comp == 0 -> nodo.medicamento
            comp < 0 -> buscarRecursivo(nodo.izquierdo, clave)
            else -> buscarRecursivo(nodo.derecho, clave)
        }
    }

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
                    if (nodo.izquierdo == null) return nodo.derecho
                    if (nodo.derecho == null) return nodo.izquierdo

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
