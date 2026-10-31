package com.gemini.multiverse.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.data.chat.ChatRepository
import com.gemini.multiverse.domain.chat.ChatMessage
import com.gemini.multiverse.domain.chat.MessageSender
import com.gemini.multiverse.domain.chat.MessageStatus
import com.gemini.multiverse.domain.personality.PersonalitiesCatalog
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoElevated
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldLight
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextDim
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val container = remember { GeminiMultiverseApp.instance.container }
    val chatRepo = remember { ChatRepository(container.settingsRepository) }
    val messages by chatRepo.messages.collectAsState()
    val settingsState by container.settingsRepository.settingsState.collectAsState()
    val activePersona = remember(settingsState.selectedPersonalityId) {
        PersonalitiesCatalog.getById(settingsState.selectedPersonalityId)
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
    ) {
        // Chat Header with Active Personality Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndigoSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(activePersona.auraColor.copy(alpha = 0.2f))
                        .border(1.dp, activePersona.auraColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = activePersona.avatarEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Lia • ${activePersona.name}",
                        style = Typography.titleLarge,
                        color = TextSnow,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${activePersona.hindiName} Mode",
                        color = activePersona.auraColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(onClick = { chatRepo.clearChat() }) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = TextDim
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message, personaAura = activePersona.auraColor)
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndigoSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("বাংলা, ইংরেজি বা Hinglish-এ লিখুন...", color = TextDim, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextSnow,
                    unfocusedTextColor = TextSnow,
                    focusedBorderColor = MarigoldPrimary,
                    unfocusedBorderColor = IndigoBorder
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val textToSend = inputText
                        inputText = ""
                        scope.launch { chatRepo.sendMessage(textToSend) }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MarigoldPrimary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = IndigoVoid
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    personaAura: Color
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(if (isUser) MarigoldPrimary.copy(alpha = 0.2f) else IndigoElevated)
                .border(
                    width = 1.dp,
                    color = if (isUser) MarigoldPrimary else personaAura.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (message.status == MessageStatus.SENDING && message.text == "...") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = personaAura,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Jarvis ভাবছে...", color = TextSubtle, fontSize = 13.sp)
                }
            } else {
                Text(
                    text = message.text,
                    color = if (isUser) MarigoldLight else TextSnow,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
