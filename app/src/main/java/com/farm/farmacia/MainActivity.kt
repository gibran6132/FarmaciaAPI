package com.farm.farmacia
//Jimeno Lorenzo Isaac Orlando
//Ozuna Ramos Gibran
//Flores Vera Cristian

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.farm.farmacia.databinding.ActivityCatalogoBinding
import com.farm.farmacia.databinding.HeaderCatalogoBinding
import com.farm.farmacia.databinding.ItemMedicamentoBinding
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var header: HeaderCatalogoBinding
    private lateinit var model: CRUDCatalogo
    private val medicamentosAdapter = MedicamentosAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        header = HeaderCatalogoBinding.inflate(layoutInflater, binding.medicamentosList, false)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
        binding.medicamentosList.addHeaderView(header.root, null, false)
        binding.medicamentosList.adapter = medicamentosAdapter
        model = ViewModelProvider(this)[CRUDCatalogo::class.java]
        savedInstanceState?.getStringArrayList("formulario")?.let { values ->
            campos().zip(values).forEach { (campo, valor) -> campo.setText(valor) }
        }
        header.cargarButton.setOnClickListener { model.cargar() }
        header.buscarButton.setOnClickListener {
            if (validarObligatorio(header.claveInput)) {
                val medicamento = model.buscar(header.claveInput.text.toString())
                if (medicamento != null) mostrarFormulario(medicamento)
                else campos().drop(1).forEach { it.text.clear() }
            }
        }
        header.guardarButton.setOnClickListener { guardar() }
        header.eliminarButton.setOnClickListener {
            if (validarObligatorio(header.claveInput)) {
                val clave = header.claveInput.text.toString().trim()
                AlertDialog.Builder(this)
                    .setTitle(R.string.eliminar_title)
                    .setMessage(getString(R.string.eliminar_confirmacion, clave))
                    .setNegativeButton(R.string.cancelar, null)
                    .setPositiveButton(R.string.eliminar) { _, _ ->
                        model.eliminar(clave)
                    }.show()
            }
        }
        binding.medicamentosList.setOnItemClickListener { parent, _, position, _ ->
            (parent.getItemAtPosition(position) as? Medicamento)?.let {
                model.buscar(it.clave_medicamento)
                mostrarFormulario(it)
                binding.medicamentosList.smoothScrollToPosition(0)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.estado.collect { estado ->
                    medicamentosAdapter.actualizar(estado.medicamentos)
                    header.estadoText.text = estado.mensaje
                    header.cargaProgress.isVisible = estado.cargando
                    header.cargarButton.isEnabled = !estado.cargando
                    header.guardarButton.isEnabled = !estado.cargando
                    header.eliminarButton.isEnabled = !estado.cargando
                    header.listaTitle.text = getString(R.string.lista_total, estado.medicamentos.size)
                    header.vacioText.isVisible = estado.medicamentos.isEmpty() && !estado.cargando
                    header.resultadoText.isVisible = estado.seleccionado != null
                    estado.seleccionado?.let {
                        header.resultadoText.text = getString(
                            R.string.resultado, it.nombre, it.categoria, it.presentacion, it.existencia
                        )
                    }
                }
            }
        }
    }

    private fun campos() = listOf(
        header.claveInput, header.nombreInput, header.categoriaInput,
        header.presentacionInput, header.existenciaInput
    )

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("formulario", ArrayList(campos().map { it.text.toString() }))
        super.onSaveInstanceState(outState)
    }

    private fun validarObligatorio(campo: EditText): Boolean {
        campo.error = null
        if (campo.text.isNotBlank()) return true
        campo.error = getString(R.string.campo_obligatorio)
        campo.requestFocus()
        return false
    }

    private fun guardar() {
        if (!campos().take(4).all { validarObligatorio(it) }) return
        val existencia = header.existenciaInput.text.toString().trim().toIntOrNull()
        if (existencia == null || existencia < 0) {
            header.existenciaInput.error = getString(R.string.existencia_invalida)
            header.existenciaInput.requestFocus()
            return
        }
        header.existenciaInput.error = null
        model.guardar(Medicamento(
            clave_medicamento = header.claveInput.text.toString().trim(),
            nombre = header.nombreInput.text.toString().trim(),
            categoria = header.categoriaInput.text.toString().trim(),
            presentacion = header.presentacionInput.text.toString().trim(),
            existencia = existencia
        ))
    }

    private fun mostrarFormulario(med: Medicamento) {
        campos().zip(listOf(med.clave_medicamento, med.nombre, med.categoria, med.presentacion, med.existencia.toString()))
            .forEach { (campo, valor) -> campo.setText(valor); campo.error = null }
    }

    private class MedicamentosAdapter : BaseAdapter() {
        private var medicamentos = emptyList<Medicamento>()
        fun actualizar(lista: List<Medicamento>) {
            if (medicamentos == lista) return
            medicamentos = lista
            notifyDataSetChanged()
        }
        override fun getCount() = medicamentos.size
        override fun getItem(position: Int) = medicamentos[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val fila = if (convertView == null) {
                ItemMedicamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    .also { it.root.tag = it }
            } else convertView.tag as ItemMedicamentoBinding
            val med = getItem(position)
            fila.nombreText.text = parent.context.getString(R.string.fila_nombre, med.clave_medicamento, med.nombre)
            fila.detalleText.text = parent.context.getString(R.string.fila_detalle, med.categoria, med.presentacion)
            fila.stockText.text = parent.context.getString(R.string.stock, med.existencia)
            return fila.root
        }
    }
}
