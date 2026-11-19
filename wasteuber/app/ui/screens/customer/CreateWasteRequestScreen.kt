package hu.wasteuber.app.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.wasteuber.app.R
import hu.wasteuber.app.viewmodel.WasteRequestViewModel

@Composable
fun CreateWasteRequestScreen(
    wasteRequestViewModel: WasteRequestViewModel = viewModel(),
    onCreateSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {

    var wasteType by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var address by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var successMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 800.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
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
                                "Új elszállítási igény",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick =
                        onBackClick,
                    enabled =
                        !isLoading
                ) {

                    Text(
                        "Vissza"
                    )
                }
            }

            Text(
                text =
                    "Add meg az elszállítandó hulladék adatait.",
                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            OutlinedTextField(
                value =
                    wasteType,

                onValueChange = {

                    wasteType =
                        it

                    errorMessage =
                        ""

                    successMessage =
                        ""
                },

                label = {

                    Text(
                        "Hulladék típusa"
                    )
                },

                placeholder = {

                    Text(
                        "Pl. lom, elektronikai hulladék"
                    )
                },

                enabled =
                    !isLoading,

                singleLine =
                    true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value =
                    amount,

                onValueChange = { newValue ->

                    /*
                     * Számot, pontot és vesszőt
                     * engedünk beírni.
                     */
                    if (
                        newValue.all { character ->

                            character.isDigit() ||
                                    character == '.' ||
                                    character == ','
                        }
                    ) {

                        amount =
                            newValue
                    }

                    errorMessage =
                        ""

                    successMessage =
                        ""
                },

                label = {

                    Text(
                        "Mennyiség"
                    )
                },

                placeholder = {

                    Text(
                        "Pl. 25"
                    )
                },

                supportingText = {

                    Text(
                        "A mennyiséget számmal add meg."
                    )
                },

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),

                enabled =
                    !isLoading,

                singleLine =
                    true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value =
                    address,

                onValueChange = {

                    address =
                        it

                    errorMessage =
                        ""

                    successMessage =
                        ""
                },

                label = {

                    Text(
                        "Elszállítás címe"
                    )
                },

                placeholder = {

                    Text(
                        "Pl. 1111 Budapest, Példa utca 10."
                    )
                },

                enabled =
                    !isLoading,

                singleLine =
                    true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value =
                    description,

                onValueChange = {

                    description =
                        it

                    errorMessage =
                        ""

                    successMessage =
                        ""
                },

                label = {

                    Text(
                        "Leírás"
                    )
                },

                placeholder = {

                    Text(
                        "További információ az elszállításhoz"
                    )
                },

                enabled =
                    !isLoading,

                minLines =
                    4,

                modifier =
                    Modifier.fillMaxWidth()
            )

            if (
                errorMessage.isNotBlank()
            ) {

                Text(
                    text =
                        errorMessage,
                    color =
                        MaterialTheme.colorScheme.error,
                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }

            if (
                successMessage.isNotBlank()
            ) {

                Text(
                    text =
                        successMessage,
                    color =
                        MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }

            if (
                isLoading
            ) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .align(
                            Alignment.CenterHorizontally
                        )
                        .padding(
                            top = 12.dp
                        )
                )

            } else {

                Button(
                    onClick = {

                        errorMessage =
                            ""

                        successMessage =
                            ""

                        val cleanedWasteType =
                            wasteType.trim()

                        val cleanedAddress =
                            address.trim()

                        /*
                         * Magyar billentyűzet miatt
                         * a vesszőt ponttá alakítjuk.
                         */
                        val amountValue =
                            amount
                                .replace(
                                    ",",
                                    "."
                                )
                                .toDoubleOrNull()

                        when {

                            cleanedWasteType.isBlank() -> {

                                errorMessage =
                                    "Add meg a hulladék típusát."
                            }

                            amountValue == null ||
                                    amountValue <= 0.0 -> {

                                errorMessage =
                                    "Adj meg érvényes, nullánál nagyobb mennyiséget."
                            }

                            cleanedAddress.isBlank() -> {

                                errorMessage =
                                    "Add meg az elszállítás címét."
                            }

                            else -> {

                                isLoading =
                                    true

                                wasteRequestViewModel
                                    .createWasteRequest(

                                        wasteType =
                                            cleanedWasteType,

                                        amount =
                                            amountValue,

                                        address =
                                            cleanedAddress,

                                        description =
                                            description.trim(),

                                        /*
                                         * Képfeltöltést később
                                         * kötünk hozzá.
                                         */
                                        imageUrl =
                                            "",

                                        onSuccess = {

                                            isLoading =
                                                false

                                            successMessage =
                                                "Az elszállítási igény sikeresen létrejött."

                                            /*
                                             * Kitakarítjuk az űrlapot.
                                             */
                                            wasteType =
                                                ""

                                            amount =
                                                ""

                                            address =
                                                ""

                                            description =
                                                ""

                                            onCreateSuccess()
                                        },

                                        onError = { error ->

                                            isLoading =
                                                false

                                            errorMessage =
                                                error
                                        }
                                    )
                            }
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Igény létrehozása"
                    )
                }
            }
        }
    }
}