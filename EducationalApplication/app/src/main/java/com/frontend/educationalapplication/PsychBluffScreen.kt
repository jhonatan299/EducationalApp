package com.frontend.educationalapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val psychBackground = Color(0xFF1A0533)
private val psychSurface = Color(0xFF2D1054)
private val psychGold = Color(0xFFFFD700)
private val psychGreen = Color(0xFF22C55E)
private val psychRed = Color(0xFFEF4444)

@Composable
fun PsychBluffMainScreen(onBack: () -> Unit) {
    var screen by remember { mutableStateOf(PsychScreen.Welcome) }
    var playerName by remember { mutableStateOf("") }
    var gameMode by remember { mutableStateOf("Trivia") }
    var fakeAnswer by remember { mutableStateOf("") }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3A126B), psychBackground, Color(0xFF10021F))))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PSYCH!", color = psychGold, fontWeight = FontWeight.Black, fontSize = 34.sp)
            Text("Bluff Trivia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            when (screen) {
                PsychScreen.Welcome -> PsychWelcome(
                    name = playerName,
                    onNameChange = { playerName = it },
                    onCreate = { screen = PsychScreen.Lobby },
                    onJoin = { screen = PsychScreen.Lobby }
                )
                PsychScreen.Lobby -> PsychLobby(
                    name = playerName.ifBlank { "Você" }, mode = gameMode,
                    onModeChange = { gameMode = it }, onStart = { screen = PsychScreen.Writing }
                )
                PsychScreen.Writing -> PsychWriting(
                    mode = gameMode, answer = fakeAnswer, onAnswerChange = { fakeAnswer = it },
                    onSubmit = { screen = PsychScreen.Voting }
                )
                PsychScreen.Voting -> PsychVoting(
                    selectedAnswer = selectedAnswer,
                    onVote = { selectedAnswer = it },
                    onContinue = { screen = PsychScreen.Reveal }
                )
                PsychScreen.Reveal -> PsychReveal(onContinue = { screen = PsychScreen.Score })
                PsychScreen.Score -> PsychScore(onNextRound = { screen = PsychScreen.Writing })
            }
        }
        PsychBackText(
            text = "< VOLTAR A JOGOS",
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun PsychBackText(text: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        color = Color.White.copy(alpha = .78f),
        fontWeight = FontWeight.Bold,
        modifier = modifier.clickable(onClick = onBack)
    )
}

private enum class PsychScreen { Welcome, Lobby, Writing, Voting, Reveal, Score }

@Composable
private fun PsychWelcome(name: String, onNameChange: (String) -> Unit, onCreate: () -> Unit, onJoin: () -> Unit) {
    PsychPanel {
        Text("Blefe, vote e descubra quem conhece a verdade.", color = Color.White, textAlign = TextAlign.Center)
        PsychField(name, onNameChange, "Seu nome de exibição")
        PsychButton("CRIAR SALA", onCreate)
        PsychButton("ENTRAR EM UMA SALA", onJoin, outlined = true)
        Text("2 a 5 jogadores • 8 rodadas", color = Color.White.copy(alpha = .6f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 12.sp)
    }
}

@Composable
private fun PsychLobby(name: String, mode: String, onModeChange: (String) -> Unit, onStart: () -> Unit) {
    PsychPanel {
        Text("SALA BLUFF-7K2A", color = psychGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Escolha o modo", color = Color.White, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PsychChoice("Trivia", mode == "Trivia") { onModeChange("Trivia") }
            PsychChoice("Palavras", mode == "Palavras") { onModeChange("Palavras") }
        }
        Text("Jogadores na sala", color = Color.White, fontWeight = FontWeight.Bold)
        listOf(name to "🧠", "Luna" to "🌙", "Rafa" to "⚡", "Bia" to "🎨").forEach { (player, emoji) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(38.dp).background(Color(0xFF633B94), CircleShape), contentAlignment = Alignment.Center) { Text(emoji) }
                Text(player, color = Color.White, fontWeight = FontWeight.Bold)
                if (player == name) Text("HOST", color = psychGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
        PsychButton("INICIAR PARTIDA", onStart)
    }
}

@Composable
private fun PsychWriting(mode: String, answer: String, onAnswerChange: (String) -> Unit, onSubmit: () -> Unit) {
    PsychHeader("RODADA 1 DE 8", "00:45", .72f)
    PsychPanel {
        Text(if (mode == "Trivia") "CULTURA GERAL" else "DEFINIÇÃO DE PALAVRAS", color = psychGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text(if (mode == "Trivia") "Qual era o nome original da Nike?" else "O que significa PETRICHOR?", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
        Text("Invente uma resposta falsa, mas convincente.", color = Color.White.copy(alpha = .72f))
        PsychField(answer, onAnswerChange, "Sua resposta falsa")
        PsychButton("ENVIAR RESPOSTA", onSubmit)
        Text("3/4 jogadores responderam", color = psychGold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun PsychVoting(selectedAnswer: String?, onVote: (String) -> Unit, onContinue: () -> Unit) {
    PsychHeader("QUAL É A VERDADE?", "00:30", .52f)
    Text("Vote na resposta verdadeira", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
    val answers = listOf("Blue Ribbon Sports", "Athletic Star", "Swoosh Co. (Sua resposta)", "Oregon Runners")
    answers.forEach { answer ->
        val isMine = answer.contains("Sua resposta")
        val isSelected = selectedAnswer == answer
        Button(
            onClick = { if (!isMine) onVote(answer) }, enabled = !isMine && selectedAnswer == null,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) psychGold else psychSurface,
                contentColor = if (isSelected) psychBackground else Color.White,
                disabledContainerColor = if (isSelected) psychGold else psychSurface,
                disabledContentColor = if (isSelected) psychBackground else Color.White.copy(alpha = .55f)
            )
        ) { Text(answer, modifier = Modifier.padding(vertical = 6.dp), fontWeight = FontWeight.Bold) }
    }
    if (selectedAnswer != null) PsychButton("VER REVEAL (DEMO)", onContinue)
}

@Composable
private fun PsychReveal(onContinue: () -> Unit) {
    PsychHeader("REVEAL", "RESULTADO", 1f)
    PsychPanel {
        Text("A resposta correta era...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Blue Ribbon Sports ✓", color = psychGreen, fontSize = 25.sp, fontWeight = FontWeight.Black)
        PsychRevealAnswer("Athletic Star", "Luna", "enganou Rafa e Bia", psychRed)
        PsychRevealAnswer("Swoosh Co.", "Você", "não enganou ninguém", psychRed)
        PsychRevealAnswer("Oregon Runners", "Rafa", "enganou Luna", psychRed)
        Text("🎭 Perfect Bluff! Luna enganou todo mundo!", color = psychGold, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        PsychButton("VER PLACAR", onContinue)
    }
}

@Composable
private fun PsychScore(onNextRound: () -> Unit) {
    PsychPanel {
        Text("PLACAR", color = psychGold, fontWeight = FontWeight.Black, fontSize = 24.sp)
        listOf("1. Luna" to "3 pts  +3", "2. Rafa" to "2 pts  +2", "3. Você" to "0 pts", "4. Bia" to "0 pts").forEachIndexed { index, (name, score) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(score, color = if (index == 0) psychGold else Color.White)
            }
        }
        PsychButton("PRÓXIMA RODADA", onNextRound)
        Text("Demo local: toque para testar novamente o ciclo.", color = Color.White.copy(alpha = .55f), fontSize = 12.sp)
    }
}

@Composable
private fun PsychHeader(title: String, time: String, progress: Float) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = psychGold, fontWeight = FontWeight.Black)
        Text(time, color = Color.White, fontWeight = FontWeight.Black)
    }
    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp), color = psychGold, trackColor = psychSurface)
}

@Composable
private fun PsychRevealAnswer(answer: String, author: String, detail: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().background(color.copy(alpha = .16f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(answer, color = Color.White, fontWeight = FontWeight.Black)
        Text("Escrita por $author — $detail", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
    }
}

@Composable
private fun PsychPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(psychSurface, RoundedCornerShape(24.dp)).border(1.dp, psychGold.copy(alpha = .35f), RoundedCornerShape(24.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp), content = content
    )
}

@Composable
private fun PsychChoice(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = if (selected) psychGold else Color(0xFF472170), contentColor = if (selected) psychBackground else Color.White)) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
private fun PsychField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = psychGold, unfocusedBorderColor = Color.White.copy(alpha = .4f), focusedLabelColor = psychGold, unfocusedLabelColor = Color.White.copy(alpha = .7f), cursorColor = psychGold)
    )
}

@Composable
private fun PsychButton(text: String, onClick: () -> Unit, outlined: Boolean = false) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), border = if (outlined) androidx.compose.foundation.BorderStroke(1.dp, psychGold) else null, colors = ButtonDefaults.buttonColors(containerColor = if (outlined) psychSurface else psychGold, contentColor = if (outlined) Color.White else psychBackground)) { Text(text, fontWeight = FontWeight.Black) }
}
