package com.gemini.multiverse.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextDim
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography

@Composable
fun ProfileScreen() {
    var nickname by remember { mutableStateOf("Dost") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "User Profile", style = Typography.displayLarge, color = TextSnow)
        Text(text = "How Lia refers to you during voice and chat sessions", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Preferred Nickname", style = Typography.titleLarge, color = TextSnow)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Lia will address you with this name naturally in conversations.", color = TextSubtle, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Your Nickname", color = TextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextSnow,
                        unfocusedTextColor = TextSnow,
                        focusedBorderColor = MarigoldPrimary,
                        unfocusedBorderColor = IndigoBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
