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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arcadebits.R
import com.example.arcadebits.databinding.ActivityLoginBinding
import com.example.arcadebits.utils.ValidationUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.firebase.auth.FacebookAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager


    private val responseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        binding.pbLoading.visibility = View.GONE

        if (it.resultCode == RESULT_OK) {
            val datos = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val cuenta = datos.getResult(ApiException::class.java)
                if (cuenta != null) {
                    val credenciales = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credenciales)
                        .addOnCompleteListener { authTask ->
                            binding.pbLoading.visibility = View.GONE

                            if (!authTask.isSuccessful) {
                                Toast.makeText(this, "Error autenticando: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }


                            val firebaseUser = FirebaseAuth.getInstance().currentUser!!
                            val uid = firebaseUser.uid
                            val db = FirebaseFirestore.getInstance()
                            val userRef = db.collection("users").document(uid)


                            userRef.get()
                                .addOnSuccessListener { doc ->
                                    if (!doc.exists()) {

                                        val fullName = firebaseUser.displayName.orEmpty()
                                        val parts = fullName.split(" ", limit = 2)
                                        val name    = parts.getOrNull(0).orEmpty()
                                        val surname = parts.getOrNull(1).orEmpty()
                                        val email   = firebaseUser.email.orEmpty()

                                        val userData = mapOf(
                                            "name"    to name,
                                            "surname" to surname,
                                            "email"   to email
                                        )
                                        userRef.set(userData)
                                            .addOnSuccessListener {
                                                Log.d("Firestore", "Usuario Google guardado")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Error guardando: ${e.message}")
                                            }
                                    }

                                    irActivityHome()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error leyendo usuario: ${e.message}")
                                    irActivityHome()
                                }
                        }
                        .addOnFailureListener { e ->
                            binding.pbLoading.visibility = View.GONE
                            Toast.makeText(this, "Error al autenticar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignInError", "Error al iniciar sesión: ${e.statusCode} - ${e.message}")
            }
        } else {
            Toast.makeText(this, "El usuario canceló.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = Firebase.auth



        try {
            val info = packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = java.security.MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP)
                Log.d("KeyHash:", keyHash)
            }
        } catch (e: Exception) {
            Log.e("KeyHashError", e.toString())
        }




        callbackManager = CallbackManager.Factory.create()


        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d("FacebookLogin", "Login exitoso con token: ${result.accessToken}")
                    handleFacebookAccessToken(result.accessToken.token)
                }

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "Login cancelado por el usuario.", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Log.e("FacebookLogin", "Error: ${error.message}")
                    Toast.makeText(this@LoginActivity, "Error en login: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )


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
        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            val et = binding.etPassword
            if (hasFocus) {
                et.hint = ""
            } else if (et.text.isEmpty()) {
                et.hint = getString(R.string.login_password_hint)
            }
        }


        binding.btnLogin.setOnClickListener {
            loginWithEmail()
        }

        binding.btnGoogle.setOnClickListener {
            loginWithGoogle()
        }

        binding.btnFacebook.setOnClickListener {
            binding.pbLoading.visibility = View.VISIBLE
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("email", "public_profile"))
        }


        binding.tvRecoverPassword.setOnClickListener {
            startActivity(Intent(this, RecoverAccountActivity::class.java))
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginWithEmail() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        var isValid = true


        binding.etEmail.error = null
        binding.etPassword.error = null

        // Validaciones
        if (email.isBlank()) {
            binding.etEmail.error = getString(R.string.signup_email_required)
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.error = getString(R.string.signup_email_error)
            isValid = false
        }

        if (password.isBlank()) {
            binding.etPassword.error = getString(R.string.signup_password_required)
            isValid = false
        } else if (!ValidationUtils.isPasswordComplex(password)) {
            binding.etPassword.error = getString(R.string.signup_password_complex_error)
            isValid = false
        }

        if (!isValid) return


        binding.pbLoading.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                binding.pbLoading.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (it.isSuccessful) {
                    goHome()
                } else {
                    showAlert(getString(R.string.login_error_credentials))
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

    private fun loginWithGoogle() {
        binding.pbLoading.visibility = View.VISIBLE

        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, googleConf)

        googleClient.signOut() // Para evitar inicio automático
        responseLauncher.launch(googleClient.signInIntent)
    }

    private fun irActivityHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }



    override fun onStart() {
        super.onStart()
        val usuario = auth.currentUser
        if (usuario != null) irActivityHome()
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


    private fun handleFacebookAccessToken(token: String) {
        binding.pbLoading.visibility = View.VISIBLE

        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.pbLoading.visibility = View.GONE
                if (task.isSuccessful) {

                    val firebaseUser = auth.currentUser!!
                    val uid = firebaseUser.uid
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(uid)


                    userRef.get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                val fullName = firebaseUser.displayName.orEmpty()
                                val parts = fullName.split(" ", limit = 2)
                                val name = parts.getOrNull(0).orEmpty()
                                val surname = parts.getOrNull(1).orEmpty()
                                val email = firebaseUser.email.orEmpty()

                                val userData = mapOf(
                                    "name" to name,
                                    "surname" to surname,
                                    "email" to email
                                )

                                userRef.set(userData)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Usuario de Facebook guardado")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error guardando usuario: ${e.message}")
                                    }
                            }
                            irActivityHome()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error leyendo usuario: ${e.message}")
                            irActivityHome()
                        }

                } else {
                    Log.w("FacebookAuth", "signInWithCredential:falló", task.exception)
                    Toast.makeText(this, "Error autenticando con Facebook", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


}
