package com.example.arcadebits.ui.view

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arcadebits.R
import com.example.arcadebits.databinding.ActivityRecoverAccountBinding
import com.google.firebase.auth.FirebaseAuth

class RecoverAccountActivity : AppCompatActivity() {


    private lateinit var binding:ActivityRecoverAccountBinding
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecoverAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setListeners()
    }

    private fun setListeners() {

        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etEmail
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.login_email_hint)
            }
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSend.setOnClickListener {
            validateEmail()
        }
    }


    private fun validateEmail() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.recover_account_invalid_email)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.recover_account_invalid_email)
            return
        }


        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {

                        binding.etEmail.error = getString(R.string.recover_account_not_registered_email)
                    } else {

                        proceedWithRecovery(email)
                    }
                } else {

                    binding.etEmail.error = getString(R.string.recover_account_network_error)
                }
            }
    }

    private fun proceedWithRecovery(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.recover_account_confirm_message),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.recover_account_network_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }






    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            // ¿Hay alguna vista con foco?
            currentFocus?.let { focusedView ->
                if (focusedView is EditText) {
                    // ¿Se ha tocado fuera de esa vista?
                    val outRect = Rect()
                    focusedView.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        // quitamos foco
                        focusedView.clearFocus()
                        // ocultamos el teclado
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


}