package hu.wasteuber.app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.R
import hu.wasteuber.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onAdminLoginClick: () -> Unit
) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(180.dp)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.wasteuber_logo
                ),
                contentDescription = "WasteUber logó",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(180.dp)
                    .scale(3.2f)
            )
        }

        Text(
            text = "WasteUber",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Bejelentkezés",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = {
                Text("E-mail")
            },
            singleLine = true,
            modifier = Modifier.padding(top = 20.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = {
                Text("Jelszó")
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = {

                if (
                    email.isBlank() ||
                    password.isBlank()
                ) {

                    errorMessage =
                        "Add meg az e-mail címet és a jelszót."

                } else {

                    authViewModel.login(
                        email = email.trim(),
                        password = password,

                        onSuccess = {
                            errorMessage = ""
                            onLoginSuccess()
                        },

                        onError = { error ->
                            errorMessage = error
                        }
                    )
                }
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("Belépés")
        }

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Regisztráció")
        }

        Button(
            onClick = onAdminLoginClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Admin belépés")
        }
    }
}