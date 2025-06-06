package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.State
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.runtime.getValue


class LoginActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        // 👇 Cambia el tema ANTES de setContent()
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", null)
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null && !userEmail.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            LoginScreen(
                onGoogleSignInClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                },
                onLoginSuccess = { email ->
                    sharedPreferences.edit().putString("USER_EMAIL", email).apply()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val email = firebaseAuth.currentUser?.email
                                if (!email.isNullOrEmpty()) {
                                    val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putString("USER_EMAIL", email).apply()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                Toast.makeText(this, "Error con Firebase", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val email by viewModel.email
    val password by viewModel.password

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            viewModel.login(
                onSuccess = { onLoginSuccess(email) },
                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Sesión")
        }

        Spacer(Modifier.height(10.dp))

        Button(onClick = {
            viewModel.register(
                onSuccess = { onLoginSuccess(email) },
                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar")
        }

        Spacer(Modifier.height(20.dp))

        Text("O")

        Spacer(Modifier.height(20.dp))

        Button(onClick = onGoogleSignInClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Iniciar con Google")
        }
    }
}

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _email = mutableStateOf("")
    val email: State<String> get() = _email

    private val _password = mutableStateOf("")
    val password: State<String> get() = _password

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    private fun validarEntradas(correo: String, contrasena: String): String? {
        return when {
            correo.isEmpty() -> "Correo vacío"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Correo no válido"
            contrasena.isEmpty() -> "Contraseña vacía"
            contrasena.length < 6 -> "Contraseña muy corta"
            else -> null
        }
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val error = validarEntradas(email.value, password.value)
        if (error != null) {
            onError(error)
            return
        }

        auth.signInWithEmailAndPassword(email.value, password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onError("Error al iniciar sesión")
            }
    }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val error = validarEntradas(email.value, password.value)
        if (error != null) {
            onError(error)
            return
        }

        auth.createUserWithEmailAndPassword(email.value, password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onError("Error al registrar usuario")
            }
    }
}