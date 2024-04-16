package com.example.app_trainning_leal.ui.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.app_trainning_leal.R
import com.example.app_trainning_leal.databinding.ActivityRegisterLoginBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.firestore

class RegisterLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterLoginBinding
    private var auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.colorSecondary)

/*        val toolbar: MaterialToolbar = findViewById(R.id.main_toolbar)
        toolbar.setTitle(getString(R.string.app_name))
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolbar.setCollapseIcon(R.drawable.baseline_history_24)
        toolbar.setBackgroundColor(getColor(R.color.colorSecondary))*/

        binding.registerButton.setOnClickListener { view ->

            var alerta: String = ""
            val i = Intent(Intent.ACTION_VIEW)

            val name = binding.inputNameRegister.text.toString()
            val email = binding.inputEmailRegister.text.toString()
            val password = binding.inputPasswordRegister.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (name.isNotEmpty() && email.isEmpty() || password.isEmpty()) {

                val errorMessage = "Preencha todos os campos"
                createSnackBar(view, errorMessage, Color.RED)

                if (password.isEmpty()) {
                    val errorMessage = "Digite uma senha com no mínimo 6 caracteres"
                    createSnackBar(view, errorMessage, Color.RED)
                }

                return@setOnClickListener
            }

            if (isValidEmail(email)) {
                if (checkPassword(password)) {
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "password" to password
                    )

                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                ContentValues.TAG,
                                "Adicionado Usuário ID: ${documentReference.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Erro ao adicionar usuário", e)
                        }

                } else {
                    val errorMessage = "Digite uma senha com no mínimo 6 caracteres"
                    createSnackBar(view, errorMessage, Color.RED)
                    return@setOnClickListener
                }

            } else {
                val errorMessage = "Digite um email válido"
                createSnackBar(view, errorMessage, Color.RED)
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                alerta = "Preencha todos os campos"
                var color: Int = Color.RED
                createSnackBar(view, alerta, color)
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { register ->
                        if (register.isSuccessful) {
                            alerta = "Usuário ${name} criado com sucesso\nFaça login para continuar"
                            val color: Int = Color.rgb(13, 142, 9)
                            createSnackBarLoginSuccesful(view, alerta, color)
                            binding.inputEmailRegister.text?.clear()
                            binding.inputPasswordRegister.text?.clear()
                            binding.inputNameRegister.text?.clear()
                        }
                    }.addOnFailureListener { exception ->
                        val errorMessage = when (exception) {
                            is FirebaseAuthWeakPasswordException -> "Digite uma senha com no mínimo 6 caracteres"
                            is FirebaseAuthInvalidCredentialsException -> "Digite um email válido"
                            is FirebaseAuthUserCollisionException -> "Email já cadastrado"
                            is FirebaseNetworkException -> "Sem conexão com a internet"
                            else -> "Erro ao cadastrar usuário"
                        }
                        createSnackBar(view, errorMessage, Color.RED)
                    }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return email.matches(emailRegex.also { it.matches(email) })

    }

    private fun checkPassword(password: String): Boolean {
        var passwordRegex = Regex("^[0-9a-zA-Z\$*&@#]{6,}\$")
        return password.matches(passwordRegex.also { it.matches(password) })
    }

    private fun createSnackBarLoginSuccesful(view: View, alerta: String, color: Int = Color.RED) {
        val snackbar =
            Snackbar.make(view, alerta, Snackbar.LENGTH_SHORT).setAction("Fazer login") {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        val view: View = snackbar.getView()
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        view.layoutParams = params
        snackbar.setBackgroundTint(color)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }

    private fun createSnackBar(view: View, alerta: String, color: Int = Color.RED) {
        val snackbar = Snackbar.make(view, alerta, Snackbar.LENGTH_SHORT)
        val view: View = snackbar.getView()
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        view.layoutParams = params
        snackbar.setBackgroundTint(color)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}

