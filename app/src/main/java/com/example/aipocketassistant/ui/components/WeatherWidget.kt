package com.example.aipocketassistant.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherWidget(modifier: Modifier = Modifier) {
    // Stub for weather data
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "☀️",
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "72°F",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
