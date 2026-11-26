# Proyecto B · Farmacia y Almacén (Sprint 2)
**Asignatura**: Estructura de Datos (4DSM1)

## 👥 Integrantes - Equipo 1 (Catálogo)
- **Jimeno Lorenzo Isaac Orlando**
- **Ozuna Ramos Gibran**
- **Flores Vera Cristian**

---

## 📌 Descripción del Proyecto
Este módulo forma parte del sistema integrador para el área de **Farmacia y Almacén**. Se encarga de gestionar el catálogo de medicamentos, consultar inventarios de forma ordenada y conectarse con los servicios web del profesor.

### 🎯 Épica e Historia de Usuario (Sprint 2)
* **Épica**: `EB1 · Catálogo`
* **Historia de Usuario (HUMBP5)**:
  > *"Como farmacéutica, quiero listar el catálogo ordenado por clave, para revisarlo e imprimirlo."*
* **Prioridad**: Debe (Sprint 2)

---

## 🌳 Estructura de Datos Implementada
Para cumplir con los criterios de aceptación requeridos por la materia, se programó un **Árbol Binario de Búsqueda (ABB)** ordenado por la clave del medicamento:

1. **`ArbolBinarioBusqueda.kt`**: Clase que implementa la estructura de datos del árbol binario de búsqueda (`Nodo`, inserción ordenada por clave, búsqueda recursiva por clave, eliminación de nodos y recorrido `inorden`).
2. **`DiccionarioMedicamentos.kt`**: Clase de servicio que encapsula las operaciones del catálogo utilizando los métodos de la estructura del árbol binario.
3. **Criterios de Aceptación Cumplidos**:
   - [x] El catálogo se guarda en un árbol binario de búsqueda por clave (`clave_medicamento`).
   - [x] El listado se obtiene mediante **recorrido inorden** (Subárbol Izquierdo → Raíz → Subárbol Derecho).
   - [x] Se permite la **búsqueda de claves** en el árbol.

---

## 🌐 Configuración del Servicio (API)
La URL base de los servicios de prueba se encuentra centralizada en la configuración del cliente HTTP:

* **URL Base**: `https://servicios-proyecto-ed.vercel.app/api/v1`
* **Endpoint del Catálogo**: `GET {BASE}/catalogo_medicamentos`

---

## 🚀 Cómo Ejecutar la Aplicación

### Requisitos Previos
* **Android Studio** (Ladybug / Jellyfish o superior).
* **JDK 17** o superior.
* Dispositivo móvil físico o emulador con **Android 8.0 (API level 26)** o superior.
* Conexión a Internet para cargar los datos desde la API.

### Pasos para Ejecutar
1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/gibran6132/FarmaciaAPI.git
   ```
2. **Abrir en Android Studio**:
   - Seleccionar `File` > `Open...` y elegir la carpeta del proyecto.
   - Esperar a que Gradle finalice la sincronización.
3. **Compilar y Ejecutar**:
   - Seleccionar un emulador o dispositivo conectado.
   - Presionar el botón ▶️ (**Run 'app'**).

---

## 📁 Estructura Principal del Código (`app/src/main/java/com/farm/farmacia/`)
* **`ArbolBinarioBusqueda.kt`**: Estructura del Árbol Binario de Búsqueda (ABB).
* **`DiccionarioMedicamentos.kt`**: Implementación de capa de negocio sobre el Árbol.
* **`Medicamento.kt`**: Modelo de datos del medicamento.
* **`Catalogo.kt`**: Modelo de respuesta para la envoltura JSON de la API.
* **`CRUDCatalogo.kt`**: ViewModel que administra el estado de la UI (`StateFlow`).
* **`FarmaciaAPI.kt` / `Retrofit.kt`**: Cliente Retrofit para consumo REST.
* **`MainActivity.kt`**: Interfaz de usuario y adaptadores de la lista.
