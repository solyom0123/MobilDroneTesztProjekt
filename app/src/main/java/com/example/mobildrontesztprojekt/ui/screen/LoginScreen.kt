package com.example.mobildrontesztprojekt.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobildrontesztprojekt.ui.theme.*
import com.example.mobildrontesztprojekt.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) onLoginSuccess()
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    var formVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { formVisible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF050A14), DroneBackground))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = formVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Flight,
                        contentDescription = null,
                        tint = DroneSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "DRONTECH",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            brush = Brush.horizontalGradient(listOf(DroneSecondary, DroneAccent))
                        )
                    )
                    Text(
                        text = "Bejelentkezés",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            AnimatedVisibility(
                visible = formVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 80 })
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DroneSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; viewModel.clearError() },
                            label = { Text("Email cím") },
                            leadingIcon = { Icon(Icons.Filled.Email, null, tint = DroneSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DroneSecondary,
                                focusedLabelColor = DroneSecondary
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; viewModel.clearError() },
                            label = { Text("Jelszó") },
                            leadingIcon = { Icon(Icons.Filled.Lock, null, tint = DroneSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        null, tint = DroneSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login(email, password)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DroneSecondary,
                                focusedLabelColor = DroneSecondary
                            )
                        )

                        if (loginError != null) {
                            Text(
                                text = loginError!!,
                                color = DroneError,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = { viewModel.login(email, password) },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DronePrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Bejelentkezés",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Demo: admin@drontech.hu / admin123",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
