package hu.wasteuber.app.ui.screens.contractor

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.data.model.WasteRequest
import hu.wasteuber.app.viewmodel.AuthViewModel
import hu.wasteuber.app.viewmodel.WasteRequestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AvailableJobsScreen(
    wasteRequestViewModel: WasteRequestViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {

    var availableRequests by remember {
        mutableStateOf<List<WasteRequest>>(
            emptyList()
        )
    }

    var activeRequests by remember {
        mutableStateOf<List<WasteRequest>>(
            emptyList()
        )
    }

    var currentUser by remember {
        mutableStateOf<User?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var successMessage by remember {
        mutableStateOf("")
    }

    var processingRequestId by remember {
        mutableStateOf("")
    }

    var rejectionRequest by remember {
        mutableStateOf<WasteRequest?>(null)
    }

    var rejectionReason by remember {
        mutableStateOf("")
    }

    var rejectionError by remember {
        mutableStateOf("")
    }

    val isAdmin =
        currentUser?.role == "ADMIN"

    // -------------------------------------------------------------------------
    // LOAD REQUESTS
    // -------------------------------------------------------------------------

    fun loadRequests() {

        isLoading = true
        errorMessage = ""

        wasteRequestViewModel.getAvailableRequests(

            onSuccess = { loadedAvailableRequests ->

                availableRequests =
                    loadedAvailableRequests

                wasteRequestViewModel.getActiveRequests(

                    onSuccess = { loadedActiveRequests ->

                        activeRequests =
                            loadedActiveRequests

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
            },

            onError = { error ->

                errorMessage =
                    error

                isLoading =
                    false
            }
        )
    }

    fun loadCurrentUser() {

        authViewModel.getCurrentUser(

            onSuccess = { user ->

                currentUser =
                    user
            },

            onError = {

                currentUser =
                    null
            }
        )
    }

    LaunchedEffect(Unit) {

        loadCurrentUser()
        loadRequests()
    }

    // -------------------------------------------------------------------------
    // SCREEN
    // -------------------------------------------------------------------------

    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp)
                .padding(16.dp)
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
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                        Text(
                            text =
                                "Fuvarok",

                            style =
                                MaterialTheme
                                    .typography
                                    .headlineSmall,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick =
                        onBackClick,

                    enabled =
                        processingRequestId.isBlank()
                ) {

                    Text(
                        "Vissza"
                    )
                }
            }

            Text(
                text =
                    "Itt kezelheted a szabad és a már elfogadott hulladékelszállítási fuvarokat.",

                modifier =
                    Modifier.padding(
                        top = 12.dp,
                        bottom = 16.dp
                    )
            )

            if (
                successMessage.isNotBlank()
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 12.dp
                        )
                ) {

                    Text(
                        text =
                            successMessage,

                        modifier =
                            Modifier.padding(
                                16.dp
                            )
                    )
                }
            }

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
                                MaterialTheme
                                    .colorScheme
                                    .error
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

                else -> {

                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        // -----------------------------------------------------
                        // AVAILABLE
                        // -----------------------------------------------------

                        item {

                            Text(
                                text =
                                    "Szabad fuvarok",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge
                            )
                        }

                        if (
                            availableRequests.isEmpty()
                        ) {

                            item {

                                EmptyJobsCard(
                                    text =
                                        "Jelenleg nincs szabad elszállítási igény."
                                )
                            }

                        } else {

                            items(
                                items =
                                    availableRequests,

                                key = { request ->

                                    "available_${request.id}"
                                }
                            ) { request ->

                                AvailableJobCard(
                                    request =
                                        request,

                                    isProcessing =
                                        processingRequestId ==
                                                request.id,

                                    actionsEnabled =
                                        processingRequestId
                                            .isBlank(),

                                    isAdmin =
                                        isAdmin,

                                    onAcceptClick = {

                                        errorMessage =
                                            ""

                                        successMessage =
                                            ""

                                        processingRequestId =
                                            request.id

                                        wasteRequestViewModel
                                            .acceptWasteRequest(

                                                requestId =
                                                    request.id,

                                                onSuccess = {

                                                    processingRequestId =
                                                        ""

                                                    successMessage =
                                                        "Az elszállítási igényt sikeresen elfogadtad."

                                                    loadRequests()
                                                },

                                                onError = { error ->

                                                    processingRequestId =
                                                        ""

                                                    errorMessage =
                                                        error
                                                }
                                            )
                                    },

                                    onRejectClick = {

                                        rejectionRequest =
                                            request

                                        rejectionReason =
                                            ""

                                        rejectionError =
                                            ""
                                    }
                                )
                            }
                        }

                        // -----------------------------------------------------
                        // ACTIVE
                        // -----------------------------------------------------

                        item {

                            HorizontalDivider(
                                modifier =
                                    Modifier.padding(
                                        top = 12.dp,
                                        bottom = 4.dp
                                    )
                            )
                        }

                        item {

                            Text(
                                text =
                                    "Aktív fuvarok",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge
                            )
                        }

                        if (
                            activeRequests.isEmpty()
                        ) {

                            item {

                                EmptyJobsCard(
                                    text =
                                        "Jelenleg nincs folyamatban lévő fuvar."
                                )
                            }

                        } else {

                            items(
                                items =
                                    activeRequests,

                                key = { request ->

                                    "active_${request.id}"
                                }
                            ) { request ->

                                ActiveJobCard(
                                    request =
                                        request,

                                    isProcessing =
                                        processingRequestId ==
                                                request.id,

                                    actionsEnabled =
                                        processingRequestId
                                            .isBlank(),

                                    onCompleteClick = {

                                        errorMessage =
                                            ""

                                        successMessage =
                                            ""

                                        processingRequestId =
                                            request.id

                                        wasteRequestViewModel
                                            .completeWasteRequest(

                                                requestId =
                                                    request.id,

                                                onSuccess = {

                                                    processingRequestId =
                                                        ""

                                                    successMessage =
                                                        "A fuvar sikeresen teljesítve lett."

                                                    loadRequests()
                                                },

                                                onError = { error ->

                                                    processingRequestId =
                                                        ""

                                                    errorMessage =
                                                        error
                                                }
                                            )
                                    }
                                )
                            }
                        }

                        item {

                            Button(
                                onClick = {

                                    successMessage =
                                        ""

                                    loadRequests()
                                },

                                enabled =
                                    processingRequestId
                                        .isBlank(),

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 8.dp,
                                        bottom = 16.dp
                                    )
                            ) {

                                Text(
                                    "Frissítés"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // REJECTION DIALOG
    // -------------------------------------------------------------------------

    rejectionRequest?.let { request ->

        AlertDialog(
            onDismissRequest = {

                if (
                    processingRequestId.isBlank()
                ) {

                    rejectionRequest =
                        null

                    rejectionReason =
                        ""

                    rejectionError =
                        ""
                }
            },

            title = {

                Text(
                    "Elszállítási igény elutasítása"
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "Add meg, miért utasítod el az igényt."
                    )

                    OutlinedTextField(
                        value =
                            rejectionReason,

                        onValueChange = {

                            rejectionReason =
                                it

                            rejectionError =
                                ""
                        },

                        label = {

                            Text(
                                "Elutasítás oka"
                            )
                        },

                        enabled =
                            processingRequestId
                                .isBlank(),

                        minLines =
                            3,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 12.dp
                            )
                    )

                    if (
                        rejectionError
                            .isNotBlank()
                    ) {

                        Text(
                            text =
                                rejectionError,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,

                            modifier =
                                Modifier.padding(
                                    top = 8.dp
                                )
                        )
                    }

                    if (
                        processingRequestId ==
                        request.id
                    ) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier
                                    .align(
                                        Alignment.CenterHorizontally
                                    )
                                    .padding(
                                        top = 16.dp
                                    )
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    enabled =
                        processingRequestId
                            .isBlank(),

                    onClick = {

                        val cleanedReason =
                            rejectionReason.trim()

                        if (
                            cleanedReason.isBlank()
                        ) {

                            rejectionError =
                                "Az elutasítás indoklása kötelező."

                            return@TextButton
                        }

                        processingRequestId =
                            request.id

                        rejectionError =
                            ""

                        wasteRequestViewModel
                            .rejectWasteRequest(

                                requestId =
                                    request.id,

                                reason =
                                    cleanedReason,

                                onSuccess = {

                                    processingRequestId =
                                        ""

                                    rejectionRequest =
                                        null

                                    rejectionReason =
                                        ""

                                    successMessage =
                                        "Az elszállítási igényt elutasítottad."

                                    loadRequests()
                                },

                                onError = { error ->

                                    processingRequestId =
                                        ""

                                    rejectionError =
                                        error
                                }
                            )
                    }
                ) {

                    Text(
                        "Elutasítás"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    enabled =
                        processingRequestId
                            .isBlank(),

                    onClick = {

                        rejectionRequest =
                            null

                        rejectionReason =
                            ""

                        rejectionError =
                            ""
                    }
                ) {

                    Text(
                        "Mégse"
                    )
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// AVAILABLE JOB CARD
// -----------------------------------------------------------------------------

@Composable
private fun AvailableJobCard(
    request: WasteRequest,
    isProcessing: Boolean,
    actionsEnabled: Boolean,
    isAdmin: Boolean,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit
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

            JobDetails(
                request =
                    request
            )

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 14.dp,
                        bottom = 12.dp
                    )
            )

            if (isProcessing) {

                JobProcessingIndicator()

            } else {

                Button(
                    onClick =
                        onAcceptClick,

                    enabled =
                        actionsEnabled &&
                                request.status ==
                                WasteRequest.STATUS_OPEN,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Fuvar elfogadása"
                    )
                }

                /*
                 * Elutasítani továbbra is csak
                 * ADMIN tud.
                 */
                if (isAdmin) {

                    OutlinedButton(
                        onClick =
                            onRejectClick,

                        enabled =
                            actionsEnabled &&
                                    request.status ==
                                    WasteRequest.STATUS_OPEN,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp
                            )
                    ) {

                        Text(
                            "Elutasítás"
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ACTIVE JOB CARD
// -----------------------------------------------------------------------------

@Composable
private fun ActiveJobCard(
    request: WasteRequest,
    isProcessing: Boolean,
    actionsEnabled: Boolean,
    onCompleteClick: () -> Unit
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

            JobDetails(
                request =
                    request
            )

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
                        "Elfogadva: ${
                            formatAvailableJobDate(
                                request.acceptedAt
                            )
                        }",

                    modifier =
                        Modifier.padding(
                            top = 6.dp
                        )
                )
            }

            Text(
                text =
                    "Állapot: Folyamatban",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 14.dp,
                        bottom = 12.dp
                    )
            )

            if (isProcessing) {

                JobProcessingIndicator()

            } else {

                Button(
                    onClick =
                        onCompleteClick,

                    enabled =
                        actionsEnabled &&
                                request.status ==
                                WasteRequest.STATUS_ACCEPTED,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Teljesítve"
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SHARED JOB DETAILS
// -----------------------------------------------------------------------------

@Composable
private fun JobDetails(
    request: WasteRequest
) {

    Text(
        text =
            request.wasteType
                .ifBlank {
                    "Ismeretlen hulladék"
                },

        style =
            MaterialTheme
                .typography
                .titleMedium
    )

    if (
        request.customerName
            .isNotBlank()
    ) {

        Text(
            text =
                "Megrendelő: ${request.customerName}",

            modifier =
                Modifier.padding(
                    top = 8.dp
                )
        )
    }

    Text(
        text =
            "Mennyiség: ${
                formatAvailableJobAmount(
                    request.amount
                )
            }",

        modifier =
            Modifier.padding(
                top = 6.dp
            )
    )

    Text(
        text =
            "Elszállítás címe: ${request.address}",

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
                    formatAvailableJobDate(
                        request.createdAt
                    )
                }",

            modifier =
                Modifier.padding(
                    top = 6.dp
                )
        )
    }
}

// -----------------------------------------------------------------------------
// PROCESSING
// -----------------------------------------------------------------------------

@Composable
private fun JobProcessingIndicator() {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        CircularProgressIndicator()

        Text(
            text =
                "Művelet folyamatban...",

            modifier =
                Modifier.padding(
                    start = 12.dp
                )
        )
    }
}

// -----------------------------------------------------------------------------
// EMPTY STATE
// -----------------------------------------------------------------------------

@Composable
private fun EmptyJobsCard(
    text: String
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                text,

            modifier =
                Modifier.padding(
                    20.dp
                )
        )
    }
}

// -----------------------------------------------------------------------------
// FORMATTERS
// -----------------------------------------------------------------------------

private fun formatAvailableJobDate(
    timestamp: Long
): String {

    if (
        timestamp <= 0L
    ) {
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

private fun formatAvailableJobAmount(
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
