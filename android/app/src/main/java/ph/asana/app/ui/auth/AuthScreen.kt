package ph.asana.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onSignedIn: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AuthEvent.SignedIn) onSignedIn()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.mode == AuthMode.SIGN_IN) "Sign in" else "Create account") },
                navigationIcon = { OutlinedButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.mode == AuthMode.REGISTER) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            if (uiState.mode == AuthMode.REGISTER) {
                OutlinedTextField(
                    value = uiState.passwordConfirmation,
                    onValueChange = viewModel::onPasswordConfirmationChange,
                    label = { Text("Confirm password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            }

            uiState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = viewModel::submit, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.mode == AuthMode.SIGN_IN) "Sign in" else "Create account")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.onModeChange(
                            if (uiState.mode == AuthMode.SIGN_IN) AuthMode.REGISTER else AuthMode.SIGN_IN,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (uiState.mode == AuthMode.SIGN_IN) {
                            "New here? Create an account"
                        } else {
                            "Already have an account? Sign in"
                        },
                    )
                }
            }
        }
    }
}
