package hu.wasteuber.app.ui.screens.contractor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.ContractorApplication
import hu.wasteuber.app.viewmodel.ContractorViewModel

@Composable
fun ContractorApplicationScreen(
    contractorViewModel: ContractorViewModel,
    onSubmitSuccess: () -> Unit
) {
    var vehicleBrand by remember {
        mutableStateOf("")
    }

    var vehicleModel by remember {
        mutableStateOf("")
    }

    var licensePlate by remember {
        mutableStateOf("")
    }

    var vehicleType by remember {
        mutableStateOf("")
    }

    var maxLoadKg by remember {
        mutableStateOf("")
    }

    var hasTrailer by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var isSubmitting by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clipToBounds(),
                contentAlignment =
                    Alignment.Center
            ) {

                Image(
                    painter =
                        painterResource(
                            id =
                                R.drawable.wasteuber_logo
                        ),
                    contentDescription =
                        "WasteUber logó",
                    contentScale =
                        ContentScale.Fit,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(3.2f)
                )
            }

            Column {

                Text(
                    text =
                        "WasteUber",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.primary
                )

                Text(
                    text =
                        "Munkatársi jelentkezés",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        OutlinedTextField(
            value =
                vehicleBrand,
            onValueChange = {

                vehicleBrand = it
                errorMessage = ""
            },
            label = {
                Text(
                    "Jármű márkája"
                )
            },
            singleLine = true,
            modifier =
                Modifier.padding(
                    top = 20.dp
                )
        )

        OutlinedTextField(
            value =
                vehicleModel,
            onValueChange = {

                vehicleModel = it
                errorMessage = ""
            },
            label = {
                Text(
                    "Jármű modellje"
                )
            },
            singleLine = true,
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        )

        OutlinedTextField(
            value =
                licensePlate,
            onValueChange = {

                licensePlate =
                    it.uppercase()

                errorMessage = ""
            },
            label = {
                Text(
                    "Rendszám"
                )
            },
            singleLine = true,
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        )

        OutlinedTextField(
            value =
                vehicleType,
            onValueChange = {

                vehicleType = it
                errorMessage = ""
            },
            label = {
                Text(
                    "Jármű típusa"
                )
            },
            singleLine = true,
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        )

        OutlinedTextField(
            value =
                maxLoadKg,
            onValueChange = { newValue ->

                maxLoadKg =
                    newValue.filter {
                        it.isDigit()
                    }

                errorMessage = ""
            },
            label = {
                Text(
                    "Terhelhetőség (kg)"
                )
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),
            singleLine = true,
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        )

        Column(
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        ) {

            Text(
                text =
                    "Rendelkezel utánfutóval?"
            )

            Checkbox(
                checked =
                    hasTrailer,
                onCheckedChange = {

                    hasTrailer = it
                }
            )
        }

        if (
            errorMessage.isNotEmpty()
        ) {

            Text(
                text =
                    errorMessage,
                modifier =
                    Modifier.padding(
                        top = 12.dp
                    )
            )
        }

        Button(
            onClick = {

                when {

                    vehicleBrand.isBlank() ||
                            vehicleModel.isBlank() ||
                            licensePlate.isBlank() ||
                            vehicleType.isBlank() ||
                            maxLoadKg.isBlank() -> {

                        errorMessage =
                            "Minden kötelező mezőt ki kell tölteni."
                    }

                    maxLoadKg.toIntOrNull() == null ||
                            maxLoadKg.toInt() <= 0 -> {

                        errorMessage =
                            "Adj meg érvényes terhelhetőséget."
                    }

                    else -> {

                        isSubmitting =
                            true

                        val application =
                            ContractorApplication(
                                vehicleBrand =
                                    vehicleBrand.trim(),

                                vehicleModel =
                                    vehicleModel.trim(),

                                licensePlate =
                                    licensePlate.trim(),

                                vehicleType =
                                    vehicleType.trim(),

                                maxLoadKg =
                                    maxLoadKg.toInt(),

                                hasTrailer =
                                    hasTrailer
                            )

                        contractorViewModel
                            .submitApplication(
                                application =
                                    application,

                                onSuccess = {

                                    isSubmitting =
                                        false

                                    onSubmitSuccess()
                                },

                                onError = { error ->

                                    isSubmitting =
                                        false

                                    errorMessage =
                                        error
                                }
                            )
                    }
                }
            },
            enabled =
                !isSubmitting,
            modifier =
                Modifier.padding(
                    top = 20.dp
                )
        ) {

            if (isSubmitting) {

                Text(
                    "Jelentkezés küldése..."
                )

            } else {

                Text(
                    "Jelentkezés elküldése"
                )
            }
        }
    }
}