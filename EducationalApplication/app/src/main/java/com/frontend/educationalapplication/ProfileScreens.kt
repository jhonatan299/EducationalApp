package com.frontend.educationalapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.frontend.educationalapplication.network.AuthResponse
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.ReviewReminderResponse
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    api: EducationalApi,
    user: AuthResponse,
    reviews: List<ReviewReminderResponse>,
    blockOtherAppsDuringPomodoro: Boolean,
    onBlockOtherAppsDuringPomodoroChanged: (Boolean) -> Unit,
    onUserUpdated: (AuthResponse) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember(user.name) { mutableStateOf(user.name) }
    var focusReminders by remember { mutableStateOf(true) }
    var weeklySummary by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pendingReviews = reviews.filter { !it.completed }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileHeader(onBack = onBack)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(LearnerPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileGlyph(color = Color.White)
                }
                Column {
                    Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = LearnerMutedText)
                }
            }
        }

        ProfileSectionTitle("Dados da conta")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Seu nome") },
            singleLine = true
        )
        Button(
            onClick = {
                if (name.trim().isBlank() || saving) return@Button
                saving = true
                status = null
                scope.launch {
                    runCatching { api.updateUserName(user.userId, name.trim()) }
                        .onSuccess { updated ->
                            onUserUpdated(updated)
                            status = "Nome atualizado."
                        }
                        .onFailure { status = "Nao foi possivel atualizar o nome." }
                    saving = false
                }
            },
            enabled = name.trim().isNotBlank() && name.trim() != user.name && !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Salvando..." else "Salvar nome")
        }
        status?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = LearnerMutedText) }

        ProfileSectionTitle("Seguranca")
        ProfileActionRow(
            title = "Alterar senha",
            subtitle = "Atualize sua senha com confirmacao em duas etapas",
            accent = LearnerPrimary,
            onClick = onChangePassword
        )

        ProfileSectionTitle("Preferencias")
        ProfileToggleRow(
            title = "Lembretes de foco",
            subtitle = "Avisos para iniciar uma sessão de estudo",
            checked = focusReminders,
            onCheckedChange = { focusReminders = it }
        )
        ProfileToggleRow(
            title = "Resumo semanal",
            subtitle = "Atualizacoes sobre o seu ritmo de estudo",
            checked = weeklySummary,
            onCheckedChange = { weeklySummary = it }
        )

        ProfileSectionTitle("Notificacoes")
        ProfileToggleRow(
            title = "Bloquear notificações externas",
            subtitle = "Usar Não perturbe durante o Pomodoro",
            checked = blockOtherAppsDuringPomodoro,
            onCheckedChange = { enabled ->
                if (enabled && !hasDoNotDisturbAccess(context)) {
                    openDoNotDisturbAccessSettings(context)
                    status = "Autorize o acesso ao Não perturbe para bloquear notificações de outros apps."
                }
                onBlockOtherAppsDuringPomodoroChanged(enabled)
            }
        )
        if (pendingReviews.isEmpty()) {
            ProfileEmptyNotification()
        } else {
            pendingReviews.forEach { review ->
                NotificationItemCard(
                    title = review.title,
                    subtitle = "Revisao pendente - ${review.subject}",
                    accent = LearnerPrimary
                )
            }
        }

        ProfileSectionTitle("Sessao")
        ProfileActionRow(
            title = "Sair da conta",
            subtitle = "Voltar para a tela de entrada do app",
            accent = LearnerCoral,
            onClick = onLogout
        )
        BottomBackButton(onBack = onBack)
    }
}

@Composable
private fun ProfileHeader(onBack: () -> Unit) {
    Text(
        text = "Perfil e configuracoes",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun ProfileToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = LearnerMutedText)
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ProfileActionRow(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accent.copy(alpha = .16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(">", color = accent, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = LearnerMutedText)
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val canSave = currentPassword.isNotBlank() &&
        newPassword.length >= 6 &&
        confirmPassword.isNotBlank() &&
        newPassword == confirmPassword

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Alterar senha",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(LearnerPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    LockGlyph(color = Color.White)
                }
                Text("Configure uma nova senha", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(
                    "Digite a senha atual e confirme a nova senha duas vezes para evitar erro de digitacao.",
                    color = LearnerMutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                message = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha atual") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                message = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nova senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                message = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Repetir nova senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPassword.isNotBlank() && newPassword != confirmPassword,
            supportingText = {
                when {
                    confirmPassword.isNotBlank() && newPassword != confirmPassword -> Text("As senhas nao coincidem.")
                    newPassword.isNotBlank() && newPassword.length < 6 -> Text("Use pelo menos 6 caracteres.")
                }
            }
        )

        message?.let { Text(it, color = LearnerMutedText, style = MaterialTheme.typography.bodyMedium) }

        Button(
            onClick = {
                message = "Senha atualizada nesta demo. Quando o backend tiver o endpoint, conectamos aqui."
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar nova senha")
        }
        BottomBackButton(onBack = onBack, label = "Cancelar")
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileEmptyNotification() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = "Nenhuma notificacao por enquanto.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = LearnerMutedText
        )
    }
}

@Composable
private fun LockGlyph(color: Color) {
    val keyholeColor = MaterialTheme.colorScheme.primaryContainer
    Canvas(modifier = Modifier.size(26.dp)) {
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * .24f, size.height * .46f),
            size = Size(size.width * .52f, size.height * .38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * .28f, size.height * .16f),
            size = Size(size.width * .44f, size.height * .48f),
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = keyholeColor,
            radius = size.minDimension * .045f,
            center = Offset(size.width * .5f, size.height * .64f)
        )
    }
}

@Composable
fun ProfileGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.19f,
            center = Offset(size.width * 0.5f, size.height * 0.31f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.22f, size.height * 0.56f),
            size = Size(size.width * 0.56f, size.height * 0.27f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
    }
}
