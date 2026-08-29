package com.brahma.jarvis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    initialApiKey: String,
    initialAssistantName: String,
    onBack: () -> Unit,
    onSave: (apiKey: String, assistantName: String) -> Unit
) {
    var apiKey by remember { mutableStateOf(initialApiKey) }
    var assistantName by remember { mutableStateOf(initialAssistantName) }
    var showKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Gemini API Key",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "aistudio.google.com se free API key le sakte ho. Ye key sirf tumhare phone pe encrypted rehti hai.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { showKey = !showKey }) {
                Text(if (showKey) "Key chhupao" else "Key dikhao")
            }

            HorizontalDivider()

            Text("Assistant ka naam", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = assistantName,
                onValueChange = { assistantName = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onSave(apiKey, assistantName) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
