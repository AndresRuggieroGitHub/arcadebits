package com.example.arcadebits.ui.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.arcadebits.databinding.FragmentUserDataBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDataFragment : Fragment() {

    private var _binding: FragmentUserDataBinding? = null
    private val binding get() = _binding!!

    private var isEditingTelefono = false
    private var isEditingDireccion = false

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.ivEditTelefono.setOnClickListener { if (!isEditingTelefono) enterEditTelefonoMode() }
        binding.ivEditDireccion.setOnClickListener { if (!isEditingDireccion) enterEditDireccionMode() }
        binding.btnGuardarDatos.setOnClickListener { saveChanges() }

        binding.scrollRoot.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && (isEditingTelefono || isEditingDireccion)) {
                exitEditMode()
                true
            } else false
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.scrollRoot.visibility = View.GONE
        val userDoc = db.collection("users").document(uid)
        userDoc.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {

                    binding.tvNombre.text    = doc.getString("name")    ?: ""
                    binding.tvApellido.text  = doc.getString("surname") ?: ""
                    binding.tvCorreo.text    = doc.getString("email")   ?: ""
                    binding.tvTelefono.text  = doc.getString("phone")   ?: ""
                    binding.tvDireccion.text = doc.getString("address") ?: ""
                } else {

                    val user = auth.currentUser!!

                    val fullName = user.displayName ?: ""
                    val parts = fullName.split(" ", limit = 2)
                    val name    = parts.getOrNull(0) ?: ""
                    val surname = parts.getOrNull(1) ?: ""
                    val email   = user.email ?: ""

                    binding.tvNombre.text   = name
                    binding.tvApellido.text = surname
                    binding.tvCorreo.text   = email
                    binding.tvTelefono.text = ""
                    binding.tvDireccion.text = ""


                    userDoc.set(
                        mapOf(
                            "name"    to name,
                            "surname" to surname,
                            "email"   to email,
                            "phone"   to "",
                            "address" to ""
                        )
                    )
                }
                binding.progressBar.visibility = View.GONE
                binding.scrollRoot.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Snackbar.make(binding.coordinatorRoot,
                    "Error cargando datos de usuario",
                    Snackbar.LENGTH_LONG).show()

                binding.progressBar.visibility = View.GONE
                binding.scrollRoot.visibility = View.VISIBLE
            }
    }

    private fun enterEditTelefonoMode() {
        isEditingTelefono = true
        binding.tvTelefono.visibility = View.GONE
        binding.ivEditTelefono.visibility = View.GONE
        binding.tilTelefonoEdit.visibility = View.VISIBLE
        binding.etTelefonoEdit.setText(binding.tvTelefono.text)
        binding.btnGuardarDatos.visibility = View.VISIBLE
    }

    private fun enterEditDireccionMode() {
        isEditingDireccion = true
        binding.tvDireccion.visibility = View.GONE
        binding.ivEditDireccion.visibility = View.GONE
        binding.tilDireccionEdit.visibility = View.VISIBLE
        binding.etDireccionEdit.setText(binding.tvDireccion.text)
        binding.btnGuardarDatos.visibility = View.VISIBLE
    }

    private fun saveChanges() {
        val updates = mutableMapOf<String, Any>()
        if (isEditingTelefono) {
            val nuevoTel = binding.etTelefonoEdit.text.toString().trim()
            if (!TextUtils.isEmpty(nuevoTel)) {
                binding.tvTelefono.text = nuevoTel
                updates["phone"] = nuevoTel
            }
        }
        if (isEditingDireccion) {
            val nuevaDir = binding.etDireccionEdit.text.toString().trim()
            if (!TextUtils.isEmpty(nuevaDir)) {
                binding.tvDireccion.text = nuevaDir
                updates["address"] = nuevaDir
            }
        }

        if (updates.isNotEmpty()) {
            val uid = auth.currentUser?.uid ?: return
            db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Snackbar.make(binding.coordinatorRoot,
                        "Datos actualizados correctamente",
                        Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Snackbar.make(binding.coordinatorRoot,
                        "Error al guardar cambios",
                        Snackbar.LENGTH_LONG).show()
                }
        }
        exitEditMode()
    }

    private fun exitEditMode() {
        if (isEditingTelefono) {
            binding.tilTelefonoEdit.visibility = View.GONE
            binding.tvTelefono.visibility = View.VISIBLE
            binding.ivEditTelefono.visibility = View.VISIBLE
            isEditingTelefono = false
        }
        if (isEditingDireccion) {
            binding.tilDireccionEdit.visibility = View.GONE
            binding.tvDireccion.visibility = View.VISIBLE
            binding.ivEditDireccion.visibility = View.VISIBLE
            isEditingDireccion = false
        }
        if (!isEditingTelefono && !isEditingDireccion) {
            binding.btnGuardarDatos.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
