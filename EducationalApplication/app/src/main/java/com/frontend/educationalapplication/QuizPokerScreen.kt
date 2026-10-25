package com.frontend.educationalapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val pokerFelt = Color(0xFF0D2B1A)
private val pokerGold = Color(0xFFFFD700)
private val pokerPanel = Color(0xFF1A1A2E)

@Composable
fun QuizPokerMainScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var screen by remember { mutableStateOf(PokerScreen.Home) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF17472C), pokerFelt, Color(0xFF07130C))))
            .padding(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("♠", color = pokerGold, fontSize = 72.sp)
            Text("QUIZ POKER", color = pokerGold, fontSize = 32.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
            Text("Aposte no que você sabe.", color = Color.White.copy(alpha = .8f), fontSize = 16.sp)
            Spacer(Modifier.height(28.dp))

            when (screen) {
                PokerScreen.Home -> {
                    PokerButton("CRIAR MESA") { screen = PokerScreen.Create }
                    Spacer(Modifier.height(12.dp))
                    PokerButton("ENTRAR COM CÓDIGO", outlined = true) { screen = PokerScreen.Join }
                    Spacer(Modifier.height(16.dp))
                    Text("2–5 jogadores • 10 rodadas • 1000 fichas", color = Color.White.copy(alpha = .65f))
                }
                PokerScreen.Create -> PokerForm(
                    title = "CRIAR UMA MESA", name = name, roomCode = null,
                    onNameChange = { name = it }, onRoomCodeChange = {},
                    action = "CRIAR MESA", onAction = { screen = PokerScreen.Lobby }, onBack = { screen = PokerScreen.Home }
                )
                PokerScreen.Join -> PokerForm(
                    title = "ENTRAR NA MESA", name = name, roomCode = roomCode,
                    onNameChange = { name = it }, onRoomCodeChange = { roomCode = it.uppercase().take(6) },
                    action = "ENTRAR NA MESA", onAction = { screen = PokerScreen.Lobby }, onBack = { screen = PokerScreen.Home }
                )
                PokerScreen.Lobby -> PokerLobby(
                    name = name.ifBlank { "Você" },
                    onStart = { screen = PokerScreen.Table },
                    onBack = { screen = PokerScreen.Home }
                )
                PokerScreen.Table -> PokerTable(
                    name = name.ifBlank { "Você" },
                    onBack = { screen = PokerScreen.Lobby }
                )
            }
        }
        PokerBackText(
            text = "< VOLTAR AO APP",
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

private enum class PokerScreen { Home, Create, Join, Lobby, Table }

@Composable
private fun PokerForm(
    title: String, name: String, roomCode: String?, onNameChange: (String) -> Unit,
    onRoomCodeChange: (String) -> Unit, action: String, onAction: () -> Unit, onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(pokerPanel, RoundedCornerShape(22.dp)).border(1.dp, pokerGold.copy(alpha = .45f), RoundedCornerShape(22.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(title, color = pokerGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        PokerField(name, onNameChange, "Seu nome na mesa")
        roomCode?.let { PokerField(it, onRoomCodeChange, "Código da mesa") }
        PokerButton(action, onClick = onAction)
        PokerBackText(text = "< VOLTAR", onBack = onBack)
    }
}

@Composable
private fun PokerLobby(name: String, onStart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(pokerPanel, RoundedCornerShape(22.dp))
            .border(1.dp, pokerGold.copy(alpha = .45f), RoundedCornerShape(22.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MESA QP-7K2M", color = pokerGold, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        Text("Aguardando jogadores", color = Color.White.copy(alpha = .72f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PokerPlayerIcon(name, "🧠", Color(0xFF635BFF), true, Modifier.weight(1f))
            PokerPlayerIcon("Luna", "🌙", Color(0xFFE86B9A), false, Modifier.weight(1f))
            PokerPlayerIcon("Rafa", "⚡", Color(0xFFFFA726), false, Modifier.weight(1f))
        }
        Text("3 de 5 jogadores", color = pokerGold, fontWeight = FontWeight.Bold)
        PokerButton("INICIAR PARTIDA", onClick = onStart)
        PokerBackText(text = "< VOLTAR", onBack = onBack)
    }
}

@Composable
private fun PokerTable(name: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RODADA 1 / 10", color = pokerGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        Text("Qual é o resultado de 7 × 8?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        PokerTableSurface(name)
        Text("Escolha sua aposta", color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("10", "25", "50").forEach { value ->
                Button(
                    onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = pokerGold, contentColor = Color(0xFF10151A)),
                    shape = CircleShape, modifier = Modifier.size(58.dp)
                ) { Text(value, fontWeight = FontWeight.Black) }
            }
        }
        PokerButton("RESPONDER: 56", onClick = {})
        Text("Modo visual local — partida online em breve.", color = Color.White.copy(alpha = .55f), fontSize = 12.sp, textAlign = TextAlign.Center)
        PokerBackText(text = "< SAIR DA MESA", onBack = onBack, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun PokerBackText(text: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        color = Color.White.copy(alpha = .78f),
        fontWeight = FontWeight.Bold,
        modifier = modifier.clickable(onClick = onBack)
    )
}

@Composable
private fun PokerTableSurface(name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .background(Color(0xFF123D25), RoundedCornerShape(50))
            .border(3.dp, pokerGold.copy(alpha = .72f), RoundedCornerShape(50))
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            PokerPlayerIcon("Luna", "🌙", Color(0xFFE86B9A), false)
            PokerPlayerIcon("Rafa", "⚡", Color(0xFFFFA726), false)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PokerCard("?", Color.White)
            PokerCard("?", Color.White)
            PokerCard("?", Color.White)
        }
        PokerPlayerIcon(name, "🧠", Color(0xFF635BFF), true)
    }
}

@Composable
private fun PokerPlayerIcon(name: String, emoji: String, color: Color, isYou: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(color, CircleShape).border(if (isYou) 2.dp else 1.dp, pokerGold, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 25.sp) }
        Spacer(Modifier.height(4.dp))
        Text(if (isYou) "$name (você)" else name, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun PokerCard(value: String, color: Color) {
    Box(
        modifier = Modifier.width(50.dp).height(70.dp).background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) { Text(value, color = pokerPanel, fontWeight = FontWeight.Black, fontSize = 26.sp) }
}

@Composable
private fun PokerField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = pokerGold, unfocusedBorderColor = Color.White.copy(alpha = .4f),
            focusedLabelColor = pokerGold, unfocusedLabelColor = Color.White.copy(alpha = .7f), cursorColor = pokerGold
        )
    )
}

@Composable
private fun PokerButton(text: String, outlined: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (outlined) pokerPanel else pokerGold, contentColor = if (outlined) Color.White else Color(0xFF10151A)),
        border = if (outlined) androidx.compose.foundation.BorderStroke(1.dp, pokerGold) else null
    ) { Text(text, fontWeight = FontWeight.Black) }
}
