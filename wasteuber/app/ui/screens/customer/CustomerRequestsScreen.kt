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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.WasteRequest
import hu.wasteuber.app.viewmodel.WasteRequestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerRequestsScreen(
    wasteRequestViewModel: WasteRequestViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {

    var requests by remember {
        mutableStateOf<List<WasteRequest>>(
            emptyList()
        )
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    fun loadRequests() {

        isLoading = true
        errorMessage = ""

        wasteRequestViewModel.getMyRequests(

            onSuccess = { loadedRequests ->

                requests =
                    loadedRequests

                isLoading =
                    false
            },

            onError = { error ->

                errorMessage =
                    error

                isLoading =
                    false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadRequests()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                "Megrendeléseim",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick =
                        onBackClick
                ) {

                    Text(
                        "Vissza"
                    )
                }
            }

            Text(
                text =
                    "Itt láthatod a korábban létrehozott hulladékelszállítási igényeidet.",
                modifier = Modifier.padding(
                    top = 12.dp,
                    bottom = 16.dp
                )
            )

            when {

                isLoading -> {

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                errorMessage.isNotBlank() -> {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                errorMessage,
                            color =
                                MaterialTheme.colorScheme.error
                        )

                        Button(
                            onClick = {
                                loadRequests()
                            },
                            modifier =
                                Modifier.padding(
                                    top = 16.dp
                                )
                        ) {

                            Text(
                                "Újrapróbálás"
                            )
                        }
                    }
                }

                requests.isEmpty() -> {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "Még nincs elszállítási igényed."
                            )

                            Text(
                                text =
                                    "Az újonnan létrehozott igények itt fognak megjelenni.",
                                modifier =
                                    Modifier.padding(
                                        top = 8.dp
                                    )
                            )
                        }
                    }
                }

                else -> {

                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        items(
                            items = requests,
                            key = { request ->
                                request.id
                            }
                        ) { request ->

                            CustomerRequestCard(
                                request = request
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRequestCard(
    request: WasteRequest
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text =
                    request.wasteType.ifBlank {
                        "Ismeretlen hulladék"
                    },
                style =
                    MaterialTheme.typography.titleMedium
            )

            Text(
                text =
                    "Mennyiség: ${formatWasteAmount(request.amount)}",
                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            Text(
                text =
                    "Cím: ${request.address}",
                modifier =
                    Modifier.padding(
                        top = 6.dp
                    )
            )

            if (
                request.description
                    .isNotBlank()
            ) {

                Text(
                    text =
                        "Leírás: ${request.description}",
                    modifier =
                        Modifier.padding(
                            top = 6.dp
                        )
                )
            }

            if (
                request.createdAt > 0L
            ) {

                Text(
                    text =
                        "Létrehozva: ${
                            formatWasteRequestDate(
                                request.createdAt
                            )
                        }",
                    modifier =
                        Modifier.padding(
                            top = 6.dp
                        )
                )
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 14.dp,
                        bottom = 12.dp
                    )
            )

            Text(
                text =
                    "Állapot: ${
                        getWasteRequestStatusText(
                            request.status
                        )
                    }"
            )

            /*
             * ELFOGADOTT IGÉNY
             */
            if (
                request.status ==
                WasteRequest.STATUS_ACCEPTED
            ) {

                if (
                    request.acceptedByName
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            "Elfogadta: ${request.acceptedByName}",
                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )
                }

                if (
                    request.acceptedAt > 0L
                ) {

                    Text(
                        text =
                            "Elfogadás időpontja: ${
                                formatWasteRequestDate(
                                    request.acceptedAt
                                )
                            }",
                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }
            }

            /*
             * ELUTASÍTOTT IGÉNY
             */
            if (
                request.status ==
                WasteRequest.STATUS_REJECTED
            ) {

                if (
                    request.rejectedByName
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            "Elutasította: ${request.rejectedByName}",
                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )
                }

                if (
                    request.rejectionReason
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            "Elutasítás oka: ${request.rejectionReason}",
                        color =
                            MaterialTheme.colorScheme.error,
                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )
                }

                if (
                    request.rejectedAt > 0L
                ) {

                    Text(
                        text =
                            "Elutasítás időpontja: ${
                                formatWasteRequestDate(
                                    request.rejectedAt
                                )
                            }",
                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }
            }

            /*
             * TELJESÍTETT IGÉNY
             */
            if (
                request.status ==
                WasteRequest.STATUS_COMPLETED
            ) {

                if (
                    request.acceptedByName
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            "Elszállította: ${request.acceptedByName}",
                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )
                }

                if (
                    request.completedAt > 0L
                ) {

                    Text(
                        text =
                            "Teljesítve: ${
                                formatWasteRequestDate(
                                    request.completedAt
                                )
                            }",
                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }
            }
        }
    }
}

private fun getWasteRequestStatusText(
    status: String
): String {

    return when (status) {

        WasteRequest.STATUS_OPEN ->
            "Várakozik elszállítóra"

        WasteRequest.STATUS_ACCEPTED ->
            "Elfogadva"

        WasteRequest.STATUS_REJECTED ->
            "Elutasítva"

        WasteRequest.STATUS_COMPLETED ->
            "Teljesítve"

        else ->
            status.ifBlank {
                "Ismeretlen"
            }
    }
}

private fun formatWasteRequestDate(
    timestamp: Long
): String {

    if (timestamp <= 0L) {
        return "-"
    }

    val formatter =
        SimpleDateFormat(
            "yyyy.MM.dd. HH:mm",
            Locale.getDefault()
        )

    return formatter.format(
        Date(timestamp)
    )
}

private fun formatWasteAmount(
    amount: Double
): String {

    return if (
        amount % 1.0 == 0.0
    ) {

        amount
            .toLong()
            .toString()

    } else {

        amount.toString()
    }
}