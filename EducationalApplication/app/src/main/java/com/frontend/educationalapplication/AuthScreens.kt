package com.frontend.educationalapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frontend.educationalapplication.network.AuthResponse
import com.frontend.educationalapplication.network.CalendarDayResponse
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.GoalResponse
import com.frontend.educationalapplication.network.MissionResponse
import com.frontend.educationalapplication.network.ProgressResponse
import com.frontend.educationalapplication.network.ReviewReminderResponse
import com.frontend.educationalapplication.network.StudySessionResponse
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import com.frontend.educationalapplication.ui.theme.LearnerTeal
import com.frontend.educationalapplication.ui.theme.LearnerYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSkip: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        IntroPage("Organize seus estudos", "Planeje sessões, registre seu foco e acompanhe sua evolução com clareza.", LearnerPrimary),
        IntroPage("Aprenda no seu ritmo", "Use Pomodoro, metas e relatórios para transformar pequenas rotinas em progresso real.", LearnerTeal),
        IntroPage("Seu caminho começa aqui", "Entre, crie uma conta ou explore como convidado enquanto decidimos as próximas funções.", LearnerCoral)
    )
    val current = pages[page]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (page == pages.lastIndex) {
                TextButton(onClick = onSkip) {
                    Text("Saltar", color = LearnerMutedText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        IntroIllustration(current.accent, page)
        Spacer(Modifier.height(38.dp))
        Text(
            text = current.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = current.subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .width(if (index == page) 28.dp else 8.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (index == page) LearnerPrimary else LearnerPrimaryTint)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (page < pages.lastIndex) {
            PrimaryAuthButton("Próximo", onClick = { page += 1 })
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Criar conta", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onSkip) {
                Text("Saltar e usar sem login", color = LearnerPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun IntroIllustration(accent: Color, page: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        drawRoundRect(
            color = accent.copy(alpha = 0.16f),
            topLeft = Offset(size.width * 0.15f, size.height * 0.08f),
            size = Size(size.width * 0.70f, size.height * 0.78f),
            cornerRadius = CornerRadius(64f, 64f)
        )
        drawCircle(LearnerYellow, radius = 18f, center = Offset(size.width * 0.73f, size.height * 0.18f))
        drawCircle(LearnerTeal, radius = 15f, center = Offset(size.width * 0.22f, size.height * 0.72f))
        drawCircle(LearnerCoral.copy(alpha = 0.85f), radius = 22f, center = Offset(size.width * 0.68f, size.height * 0.76f))
        drawRoundRect(
            color = LearnerPrimary,
            topLeft = Offset(size.width * 0.36f, size.height * 0.34f),
            size = Size(size.width * 0.28f, size.height * 0.32f),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawCircle(Color(0xFF111827), radius = 22f, center = Offset(size.width * 0.5f, size.height * 0.26f))
        when (page) {
            0 -> {
                drawLine(LearnerPrimary, Offset(size.width * 0.28f, size.height * 0.24f), Offset(size.width * 0.38f, size.height * 0.34f), strokeWidth = 5f)
                drawLine(LearnerPrimary, Offset(size.width * 0.72f, size.height * 0.24f), Offset(size.width * 0.62f, size.height * 0.34f), strokeWidth = 5f)
            }
            1 -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.62f, size.height * 0.38f),
                    size = Size(size.width * 0.15f, size.height * 0.22f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
            else -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.32f, size.height * 0.64f),
                    size = Size(size.width * 0.36f, size.height * 0.08f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
            }
        }
    }
}

data class IntroPage(val title: String, val subtitle: String, val accent: Color)

@Composable
fun PrimaryAuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AuthScreenContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Criar conta", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Informe seus dados para começar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        content()
    }
}

@Composable
fun RegisterScreen(
    error: String?,
    onRegister: (String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScreenContainer {
        AuthTextField("Nome", name, { name = it })
        Spacer(Modifier.height(12.dp))
        AuthTextField("E-mail", email, { email = it })
        Spacer(Modifier.height(12.dp))
        AuthTextField("Senha", password, { password = it }, password = true)
        Spacer(Modifier.height(18.dp))
        PrimaryAuthButton("Criar conta", onClick = { onRegister(name, email, password) })
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Já tem uma conta? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Entrar", color = LearnerPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onLoginClick))
        }
    }
}

@Composable
fun LoginScreen(
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    // These values are connected to the text fields below.
    // When the user types, onValueChange updates the variable and Compose redraws.
    var email by remember { mutableStateOf("demo@educational.app") }
    var password by remember { mutableStateOf("123456") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Aplicativo Educacional", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Cronômetro de estudo, metas, progresso e recompensas", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Não tem uma conta? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Criar conta",
                color = LearnerPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onRegisterClick)
            )
        }
    }
}

