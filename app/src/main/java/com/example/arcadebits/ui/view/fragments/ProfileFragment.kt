package com.example.arcadebits.ui.view.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.arcadebits.R
import com.example.arcadebits.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileInfo()
        setListeners()
    }

    private fun fadeIn(view: View) {
        view.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(200).start()
        }
    }

    private fun fadeOut(view: View) {
        view.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction { view.visibility = View.GONE }
            .start()
    }

    private fun showLoading() {
        fadeIn(binding.overlay)
        fadeIn(binding.progressBar)
    }

    private fun hideLoading() {
        fadeOut(binding.progressBar)
        fadeOut(binding.overlay)
    }



    private fun loadProfileInfo() {
        val uid = auth.currentUser?.uid ?: return

        showLoading()


        binding.tvUsername.text = auth.currentUser?.displayName
            ?: auth.currentUser?.email.orEmpty()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                doc.getString("image")?.let { base64 ->
                    try {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.profileImage.setImageBitmap(bmp)
                    } catch (_: Exception) {  }
                }
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "No se pudo cargar la foto", Snackbar.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                hideLoading()
            }
    }
    private fun setListeners() {
        binding.btnEditProfileImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            imagePickerLauncher.launch(intent)
        }

        binding.btnUserData.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.flContent, UserDataFragment())
                .addToBackStack(null)   // Para que el botón “atrás” regrese al perfil
                .commit()
        }



        binding.btnOrdersMade.setOnClickListener {
            Snackbar.make(binding.root, "Sección en desarrollo", Snackbar.LENGTH_SHORT).show()
        }


        binding.btnReturnsMade.setOnClickListener {
            Snackbar.make(binding.root, "Sección en desarrollo", Snackbar.LENGTH_SHORT).show()
        }


        binding.btnBirthdayReservations.setOnClickListener {
            Snackbar.make(binding.root, "Sección en desarrollo", Snackbar.LENGTH_SHORT).show()
        }


        binding.btnEventsTickets.setOnClickListener {
            Snackbar.make(binding.root, "Sección en desarrollo", Snackbar.LENGTH_SHORT).show()
        }



        binding.btnWishList.setOnClickListener {
            val current = parentFragmentManager.findFragmentById(R.id.flContent)
            if (current !is FavoritesListFragment) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.flContent, FavoritesListFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveProfileImageToFirestore(uri)
            }
        }
    }

    private fun saveProfileImageToFirestore(uri: Uri) {
        showLoading()
        // 1) Uri -> Bitmap
        val stream = requireContext().contentResolver.openInputStream(uri)
        val bmp = BitmapFactory.decodeStream(stream) ?: run {
            hideLoading(); return
        }
        // 2) Bitmap -> Base64
        val baos = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 80, it) }
        val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        // 3) Update Firestore
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("image", base64)
            .addOnSuccessListener {
                binding.profileImage.setImageBitmap(bmp)
                Snackbar.make(binding.root, "Foto actualizada", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error guardando foto", Snackbar.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                hideLoading()
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




