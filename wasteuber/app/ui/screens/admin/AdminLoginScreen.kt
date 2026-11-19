package hu.wasteuber.app.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
fun AdminLoginScreen(
    authViewModel: AuthViewModel,
    onAdminLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
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

    var isLoading by remember {
        mutableStateOf(false)
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
            text = "Adminisztrátori bejelentkezés",
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
                Text("Admin e-mail")
            },
            singleLine = true,
            modifier = Modifier.padding(top = 24.dp)
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

                    return@Button
                }

                isLoading = true
                errorMessage = ""

                authViewModel.login(
                    email = email.trim(),
                    password = password,

                    onSuccess = {

                        authViewModel.getCurrentUser(

                            onSuccess = { user ->

                                if (
                                    user.role == "ADMIN"
                                ) {

                                    isLoading = false
                                    onAdminLoginSuccess()

                                } else {

                                    authViewModel.logout()

                                    isLoading = false

                                    errorMessage =
                                        "Ez a fiók nem rendelkezik admin jogosultsággal."
                                }
                            },

                            onError = { error ->

                                authViewModel.logout()

                                isLoading = false
                                errorMessage = error
                            }
                        )
                    },

                    onError = { error ->

                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            enabled = !isLoading,
            modifier = Modifier.padding(top = 20.dp)
        ) {

            if (isLoading) {

                CircularProgressIndicator()

            } else {

                Text("Admin belépés")
            }
        }

        Button(
            onClick = onBackClick,
            enabled = !isLoading,
            modifier = Modifier.padding(top = 12.dp)
        ) {

            Text("Vissza")
        }
    }
}