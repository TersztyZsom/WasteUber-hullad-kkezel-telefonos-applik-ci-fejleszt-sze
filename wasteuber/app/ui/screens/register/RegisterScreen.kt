package hu.wasteuber.app.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.R
import hu.wasteuber.app.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordAgain by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(140.dp)
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
                    .size(140.dp)
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
            text = "Regisztráció",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            label = {
                Text("Név")
            },
            singleLine = true,
            modifier = Modifier.padding(top = 20.dp)
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.padding(top = 12.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { newValue ->

                phoneNumber = newValue
                    .filter {
                        it.isDigit()
                    }
                    .take(9)

                errorMessage = ""
            },
            label = {
                Text("Telefonszám")
            },
            prefix = {
                Text("+36 ")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            modifier = Modifier.padding(top = 12.dp)
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
            visualTransformation =
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier = Modifier.padding(top = 12.dp)
        )

        OutlinedTextField(
            value = passwordAgain,
            onValueChange = {
                passwordAgain = it
                errorMessage = ""
            },
            label = {
                Text("Jelszó újra")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
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

                when {

                    name.isBlank() ||
                            email.isBlank() ||
                            phoneNumber.isBlank() ||
                            password.isBlank() ||
                            passwordAgain.isBlank() -> {

                        errorMessage =
                            "Minden mezőt ki kell tölteni."
                    }

                    phoneNumber.length != 9 -> {

                        errorMessage =
                            "A telefonszámnak 9 számjegyből kell állnia."
                    }

                    password != passwordAgain -> {

                        errorMessage =
                            "A két jelszó nem egyezik."
                    }

                    password.length < 6 -> {

                        errorMessage =
                            "A jelszónak legalább 6 karakteresnek kell lennie."
                    }

                    else -> {

                        authViewModel.register(
                            name = name.trim(),
                            email = email.trim(),
                            phoneNumber = phoneNumber,
                            password = password,

                            onSuccess = {
                                errorMessage = ""
                                onRegisterSuccess()
                            },

                            onError = { error ->
                                errorMessage = error
                            }
                        )
                    }
                }
            },
            modifier = Modifier.padding(
                top = 20.dp,
                bottom = 24.dp
            )
        ) {
            Text("Regisztráció")
        }
    }
}