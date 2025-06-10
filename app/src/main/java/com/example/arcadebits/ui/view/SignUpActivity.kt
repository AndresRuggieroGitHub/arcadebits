package com.example.arcadebits.ui.view

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arcadebits.R
import com.example.arcadebits.databinding.ActivitySignUpBinding
import com.example.arcadebits.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setListeners()
    }

    private fun setListeners() {
        binding.btnSignup.setOnClickListener {

            binding.etName.error = null
            binding.etSurname.error = null
            binding.etEmail.error = null
            binding.etPassword.error = null
            binding.etConfirmPassword.error = null

            val name = binding.etName.text.toString()
            val surname = binding.etSurname.text.toString()
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            var allValid = true


            if (!ValidationUtils.isNonEmpty(name)) {
                binding.etName.error = getString(R.string.signup_name_required)
                allValid = false
            } else if (!ValidationUtils.isValidName(name)) {
                binding.etName.error = getString(R.string.signup_name_error)
                allValid = false
            }

            if (!ValidationUtils.isNonEmpty(surname)) {
                binding.etSurname.error = getString(R.string.signup_surname_required)
                allValid = false
            } else if (!ValidationUtils.isValidName(surname)) {
                binding.etSurname.error = getString(R.string.signup_surname_error)
                allValid = false
            }

            if (!ValidationUtils.isNonEmpty(email)) {
                binding.etEmail.error = getString(R.string.signup_email_required)
                allValid = false
            } else if (!ValidationUtils.isValidEmail(email)) {
                binding.etEmail.error = getString(R.string.signup_email_error)
                allValid = false
            }

            if (!ValidationUtils.isNonEmpty(pass)) {
                binding.etPassword.error = getString(R.string.signup_password_required)
                allValid = false
            } else if (!ValidationUtils.isPasswordComplex(pass)) {
                binding.etPassword.error = getString(R.string.signup_password_complex_error)
                allValid = false
            }

            if (!ValidationUtils.isNonEmpty(confirm)) {
                binding.etConfirmPassword.error = getString(R.string.signup_confirm_password_required)
                allValid = false
            } else if (!ValidationUtils.isMatching(pass, confirm)) {
                binding.etConfirmPassword.error = getString(R.string.signup_confirm_password_error)
                allValid = false
            }

            if (!allValid) return@setOnClickListener


            binding.pbLoading.visibility = View.VISIBLE
            binding.btnSignup.isEnabled = false

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    binding.pbLoading.visibility = View.GONE
                    binding.btnSignup.isEnabled = true

                    if (task.isSuccessful) {
                        auth.currentUser?.updateProfile(
                            userProfileChangeRequest {
                                displayName = "$name $surname"
                            }
                        )


                        val db = FirebaseFirestore.getInstance()
                        val userId = auth.currentUser?.uid

                        val user = hashMapOf(
                            "name" to name,
                            "surname" to surname,
                            "email" to email
                        )

                        if (userId != null) {
                            db.collection("users").document(userId).set(user)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Usuario guardado correctamente")
                                    Toast.makeText(this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show()
                                    goHome()
                                }
                                .addOnFailureListener {
                                    Log.e("Firestore", "Error al guardar usuario: ${it.message}")
                                    showAlert("Error al guardar usuario en la base de datos.")
                                }
                        }


                    } else {
                        showAlert(task.exception?.message ?: getString(R.string.signup_error_generic))
                    }
                }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        binding.etName.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etName
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.signup_name_hint)
            }
        }
        binding.etSurname.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etSurname
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.signup_surname_hint)
            }
        }
        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etEmail
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.signup_email_hint)
            }
        }
        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etPassword
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.signup_password_hint)
            }
        }
        binding.etConfirmPassword.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etConfirmPassword
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.signup_confirm_password_hint)
            }
        }


    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.signup_error_title))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {

            currentFocus?.let { focusedView ->
                if (focusedView is EditText) {

                    val outRect = Rect()
                    focusedView.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {

                        focusedView.clearFocus()

                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


}