package com.frontend.educationalapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.frontend.educationalapplication.network.AuthResponse
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.ui.theme.EducationalApplicationTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // setContent is the Compose version of "put the UI on the screen".
            // Everything inside this block describes what Android should draw.
            EducationalApplicationTheme {
                StudyApp()
            }
        }
    }
}

@Composable
fun StudyApp() {
    // remember keeps the same object/value while Compose redraws this screen.
    // Without remember, Compose could recreate the API client on every redraw.
    val api = remember { EducationalApi() }
    // CoroutineScope lets button clicks run network calls without freezing the UI thread.
    val scope = rememberCoroutineScope()
    // mutableStateOf is like a variable that also tells Compose: "redraw when I change".
    var user by remember { mutableStateOf<AuthResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var authScreen by remember { mutableStateOf(AuthScreen.Onboarding) }
    var isGuest by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // If no user is logged in, show LoginScreen. After login, show HomeScreen.
        if (user == null) {
            when (authScreen) {
                AuthScreen.Onboarding -> OnboardingScreen(
                    onLoginClick = {
                        error = null
                        authScreen = AuthScreen.Login
                    },
                    onRegisterClick = {
                        error = null
                        authScreen = AuthScreen.Register
                    },
                    onSkip = {
                        isGuest = true
                        user = AuthResponse(0L, "Convidado", "convidado@local", 0)
                    }
                )
                AuthScreen.Login -> LoginScreen(
                    error = error,
                    onLogin = { email, password ->
                        error = null
                        scope.apiCall(
                            action = { api.login(email, password) },
                            onSuccess = {
                                isGuest = false
                                user = it
                            },
                            onError = { error = it }
                        )
                    },
                    onRegisterClick = {
                        error = null
                        authScreen = AuthScreen.Register
                    }
                )
                AuthScreen.Register -> RegisterScreen(
                    error = error,
                    onRegister = { name, email, password ->
                        error = null
                        scope.apiCall(
                            action = { api.register(name, email, password) },
                            onSuccess = {
                                isGuest = false
                                user = it
                            },
                            onError = { error = it }
                        )
                    },
                    onLoginClick = {
                        error = null
                        authScreen = AuthScreen.Login
                    }
                )
            }
        } else {
            HomeScreen(api = api, user = user!!, isGuest = isGuest, onUserUpdated = { user = it })
        }
    }
}

enum class AuthScreen {
    Onboarding,
    Login,
    Register
}


