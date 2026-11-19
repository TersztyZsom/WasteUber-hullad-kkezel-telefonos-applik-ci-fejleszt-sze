package hu.wasteuber.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.data.model.ContractorApplication
import hu.wasteuber.app.viewmodel.ContractorViewModel

@Composable
fun AdminApplicationsScreen(
    contractorViewModel: ContractorViewModel
) {
    var applications by remember {
        mutableStateOf<List<ContractorApplication>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    fun loadApplications() {

        isLoading = true
        errorMessage = ""

        contractorViewModel.getPendingApplications(
            onSuccess = { loadedApplications ->
                applications = loadedApplications
                isLoading = false
            },

            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadApplications()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(text = "Függő munkatársi jelentkezések")

        if (isLoading) {

            CircularProgressIndicator(
                modifier = Modifier.padding(top = 24.dp)
            )

        } else if (errorMessage.isNotEmpty()) {

            Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 24.dp)
            )

        } else if (applications.isEmpty()) {

            Text(
                text = "Jelenleg nincs elfogadásra váró jelentkezés.",
                modifier = Modifier.padding(top = 24.dp)
            )

        } else {

            LazyColumn(
                modifier = Modifier.padding(top = 20.dp)
            ) {

                items(applications) { application ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {

                        Text(
                            text = "Felhasználó ID: ${application.userId}"
                        )

                        Text(
                            text = "Jármű: ${application.vehicleBrand} ${application.vehicleModel}",
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "Rendszám: ${application.licensePlate}",
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "Jármű típusa: ${application.vehicleType}",
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "Terhelhetőség: ${application.maxLoadKg} kg",
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "Utánfutó: ${
                                if (application.hasTrailer) {
                                    "Van"
                                } else {
                                    "Nincs"
                                }
                            }",
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Button(
                                onClick = {

                                    contractorViewModel.approveApplication(
                                        userId = application.userId,

                                        onSuccess = {
                                            loadApplications()
                                        },

                                        onError = { error ->
                                            errorMessage = error
                                        }
                                    )
                                }
                            ) {
                                Text("Elfogadás")
                            }

                            Button(
                                onClick = {

                                    contractorViewModel.rejectApplication(
                                        userId = application.userId,

                                        onSuccess = {
                                            loadApplications()
                                        },

                                        onError = { error ->
                                            errorMessage = error
                                        }
                                    )
                                }
                            ) {
                                Text("Elutasítás")
                            }
                        }
                    }

                    HorizontalDivider()
                }
            }
        }
    }
}