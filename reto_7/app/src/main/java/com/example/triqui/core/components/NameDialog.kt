package com.example.triqui.core.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private val bgColor = Color(0xFF132F20)
private val neonGreen = Color(0xFF4ADE80)

/**
 * Pop up que pide un nombre y busca la partida.
 * - onSearch: operación (suspend) que devuelve true si encontró la partida.
 * - onFound:  se llama cuando se encontró; el padre cierra el diálogo.
 * - onDismiss: Cancelar o tocar fuera.
 */
@Composable
fun NameDialog(
    onDismiss: () -> Unit,
    onSearch: suspend (String) -> Boolean,
    onFound: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val canSend = name.isNotBlank() && !isLoading

    fun send() {
        if (!canSend) return
        isLoading = true
        scope.launch {
            try {
                val found = onSearch(name.trim())
                if (found) onFound(name.trim()) else showError = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError = true // cualquier fallo se trata como "no encontrada"
            } finally {
                isLoading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = bgColor,
        titleContentColor = neonGreen,
        textContentColor = neonGreen,
        title = { Text("Escribe tu nombre") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { send() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = neonGreen,
                    unfocusedTextColor = neonGreen,
                    disabledTextColor = neonGreen.copy(alpha = 0.6f),
                    cursorColor = neonGreen,
                    focusedBorderColor = neonGreen,
                    unfocusedBorderColor = neonGreen.copy(alpha = 0.5f),
                    disabledBorderColor = neonGreen.copy(alpha = 0.3f),
                    focusedLabelColor = neonGreen,
                    unfocusedLabelColor = neonGreen.copy(alpha = 0.7f),
                    disabledLabelColor = neonGreen.copy(alpha = 0.4f)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { send() },
                enabled = canSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonGreen,
                    contentColor = bgColor,
                    disabledContainerColor = neonGreen.copy(alpha = 0.3f),
                    disabledContentColor = bgColor.copy(alpha = 0.6f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = bgColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = neonGreen,
                    disabledContentColor = neonGreen.copy(alpha = 0.4f)
                )
            ) { Text("Cancelar") }
        }
    )

    // Segundo diálogo: aparece encima si la operación falló.
    // Al aceptar, vuelves al diálogo del nombre con el texto intacto.
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            containerColor = bgColor,
            titleContentColor = neonGreen,
            textContentColor = neonGreen,
            title = { Text("Error") },
            text = { Text("No se pudo encontrar partida") },
            confirmButton = {
                TextButton(
                    onClick = { showError = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = neonGreen)
                ) { Text("Aceptar") }
            }
        )
    }
}