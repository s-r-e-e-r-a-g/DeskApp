package com.example.aipocketassistant.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aipocketassistant.ui.components.ClockWidget
import com.example.aipocketassistant.ui.components.WeatherWidget
import com.example.aipocketassistant.viewmodel.AssistantState
import com.example.aipocketassistant.viewmodel.AssistantViewModel

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val lastQuery by viewModel.lastQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            WeatherWidget()
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ClockWidget(modifier = Modifier.weight(1f))

        AssistantStatusIndicator(
            state = uiState,
            errorMessage = errorMessage,
            modifier = Modifier.weight(1f)
        )
        
        if (lastQuery.isNotBlank() && uiState != AssistantState.READY && uiState != AssistantState.LISTENING) {
            Text(
                text = "You: $lastQuery",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (lastResponse.isNotBlank() && uiState == AssistantState.SPEAKING) {
            Text(
                text = "AI: $lastResponse",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        TalkButton(
            state = uiState,
            onPress = { viewModel.startListening() },
            onRelease = { viewModel.stopListening() },
            modifier = Modifier.padding(bottom = 48.dp)
        )
    }
}

@Composable
fun AssistantStatusIndicator(state: AssistantState, errorMessage: String?, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "indicator_transition")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == AssistantState.LISTENING || state == AssistantState.THINKING) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    val color = when (state) {
        AssistantState.READY -> MaterialTheme.colorScheme.primary
        AssistantState.LISTENING -> MaterialTheme.colorScheme.tertiary
        AssistantState.PROCESSING -> MaterialTheme.colorScheme.secondary
        AssistantState.THINKING -> MaterialTheme.colorScheme.tertiaryContainer
        AssistantState.SPEAKING -> MaterialTheme.colorScheme.primaryContainer
        AssistantState.ERROR -> MaterialTheme.colorScheme.error
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = errorMessage ?: state.name,
            color = if (state == AssistantState.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TalkButton(
    state: AssistantState,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                if (state == AssistantState.LISTENING) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Hold to talk",
            modifier = Modifier.size(48.dp),
            tint = if (state == AssistantState.LISTENING) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
