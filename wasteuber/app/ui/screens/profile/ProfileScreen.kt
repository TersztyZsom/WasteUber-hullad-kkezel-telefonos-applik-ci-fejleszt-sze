package hu.wasteuber.app.ui.screens.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.ContractorApplication
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.viewmodel.AuthViewModel
import hu.wasteuber.app.viewmodel.ContractorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    contractorViewModel: ContractorViewModel,
    onContractorApplicationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSubscriptionClick: () -> Unit = {}
) {
    var user by remember {
        mutableStateOf<User?>(null)
    }

    var contractorApplication by remember {
        mutableStateOf<ContractorApplication?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    fun loadProfile() {
        isLoading = true
        errorMessage = ""

        authViewModel.getCurrentUser(
            onSuccess = { loadedUser ->

                user = loadedUser

                contractorViewModel.getCurrentUserApplication(
                    onSuccess = { application ->

                        contractorApplication = application
                        isLoading = false
                    },

                    onError = {

                        contractorApplication = null
                        isLoading = false
                    }
                )
            },

            onError = { error ->

                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 800.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
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
                                id = R.drawable.wasteuber_logo
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
                        text = "WasteUber",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Profil",
                        style =
                            MaterialTheme.typography.headlineMedium,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }

                errorMessage.isNotEmpty() -> {

                    Column(
                        modifier = Modifier.padding(top = 24.dp)
                    ) {

                        Text(
                            text = errorMessage
                        )

                        Button(
                            onClick = {
                                loadProfile()
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Újrapróbálás")
                        }
                    }
                }

                user != null -> {

                    val currentUser =
                        user!!

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Text(
                                text = currentUser.name
                            )

                            Text(
                                text = currentUser.email,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Text(
                                text = currentUser.phoneNumber,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Text(
                                text =
                                    "Szerepkör: ${
                                        getRoleName(
                                            currentUser.role
                                        )
                                    }",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    ReputationSection(
                        user = currentUser
                    )

                    SubscriptionSection(
                        user = currentUser,
                        onSubscriptionClick =
                            onSubscriptionClick
                    )

                    PostModerationSection(
                        user = currentUser
                    )

                    ContractorSection(
                        user = currentUser,
                        application = contractorApplication,
                        onContractorApplicationClick =
                            onContractorApplicationClick
                    )

                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 24.dp,
                                bottom = 24.dp
                            )
                    ) {
                        Text("Kijelentkezés")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReputationSection(
    user: User
) {
    val automaticPostingAvailable =
        user.firstPostApproved &&
                (
                        user.role == "ADMIN" ||
                                user.reputationPoints >= 50
                        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Reputáció"
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 12.dp,
                    bottom = 12.dp
                )
            )

            Text(
                text =
                    "Reputációs pontok: ${user.reputationPoints}"
            )

            Text(
                text =
                    if (user.firstPostApproved) {
                        "Első poszt ellenőrzése: ✓ Jóváhagyva"
                    } else {
                        "Első poszt ellenőrzése: Még nem történt meg"
                    },
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text =
                    if (automaticPostingAvailable) {
                        "✓ Automatikus posztolás aktív"
                    } else {
                        "Automatikus posztolás: Nem aktív"
                    },
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!user.firstPostApproved) {

                Text(
                    text =
                        "Az első poszt minden fióknál moderátori jóváhagyást igényel.",
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else if (
                user.role != "ADMIN" &&
                user.reputationPoints < 50
            ) {

                Text(
                    text =
                        "Az automatikus posztoláshoz még ${
                            50 - user.reputationPoints
                        } pont szükséges.",
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else {

                Text(
                    text =
                        "A posztjaid moderátori előzetes jóváhagyás nélkül megjelenhetnek.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text =
                    "A reputációs pontok posztoláskor nem fogynak el.",
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun SubscriptionSection(
    user: User,
    onSubscriptionClick: () -> Unit
) {
    val currentTime =
        System.currentTimeMillis()

    val subscriptionCurrentlyValid =
        user.subscriptionActive &&
                user.subscriptionPackage !=
                SubscriptionPlans.PACKAGE_NONE &&
                user.subscriptionEndAt > currentTime

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Előfizetés"
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 12.dp,
                    bottom = 12.dp
                )
            )

            if (
                user.subscriptionPackage ==
                SubscriptionPlans.PACKAGE_NONE
            ) {

                Text(
                    text =
                        "Jelenleg nincs előfizetési csomagod."
                )

            } else {

                Text(
                    text =
                        "Csomag: ${
                            SubscriptionPlans.getPackageName(
                                user.subscriptionPackage
                            )
                        }"
                )

                Text(
                    text =
                        "Időszak: ${
                            SubscriptionPlans.getPeriodName(
                                user.subscriptionPeriod
                            )
                        }",
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text =
                        if (subscriptionCurrentlyValid) {
                            "Állapot: ✓ Aktív"
                        } else {
                            "Állapot: Lejárt / inaktív"
                        },
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (
                    user.subscriptionStartAt > 0L
                ) {

                    Text(
                        text =
                            "Kezdete: ${
                                formatProfileDate(
                                    user.subscriptionStartAt
                                )
                            }",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (
                    user.subscriptionEndAt > 0L
                ) {

                    Text(
                        text =
                            "Lejárat: ${
                                formatProfileDate(
                                    user.subscriptionEndAt
                                )
                            }",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text =
                        "Csomag tag: ${
                            SubscriptionPlans.getPackageTag(
                                user.subscriptionPackage
                            )
                        }",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = onSubscriptionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {

                Text(
                    text =
                        if (subscriptionCurrentlyValid) {
                            "Előfizetés kezelése"
                        } else {
                            "Előfizetési csomagok megtekintése"
                        }
                )
            }
        }
    }
}

@Composable
private fun PostModerationSection(
    user: User
) {
    val currentTime =
        System.currentTimeMillis()

    val permanentlyBanned =
        user.postBanUntil == Long.MAX_VALUE

    val temporarilyBanned =
        user.postBanUntil > currentTime &&
                !permanentlyBanned

    val hasActiveBan =
        permanentlyBanned ||
                temporarilyBanned

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Posztolási moderáció"
            )

            Text(
                text =
                    "Korábbi posztolási tiltások: ${user.postBanCount}",
                modifier = Modifier.padding(top = 10.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 12.dp,
                    bottom = 12.dp
                )
            )

            if (hasActiveBan) {

                Text(
                    text =
                        "Aktív posztolási tiltás: Igen"
                )

                Text(
                    text =
                        if (permanentlyBanned) {
                            "Tiltás vége: Végleges"
                        } else {
                            "Tiltás vége: ${
                                formatProfileDate(
                                    user.postBanUntil
                                )
                            }"
                        },
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (
                    user.postBanReason.isNotBlank()
                ) {

                    Text(
                        text =
                            "Tiltás oka: ${user.postBanReason}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (
                    user.postBannedByName.isNotBlank()
                ) {

                    Text(
                        text =
                            "Kitiltotta: ${user.postBannedByName}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (
                    user.postBannedAt > 0L
                ) {

                    Text(
                        text =
                            "Kitiltás időpontja: ${
                                formatProfileDate(
                                    user.postBannedAt
                                )
                            }",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

            } else {

                Text(
                    text =
                        "Aktív posztolási tiltás: Nincs"
                )

                if (
                    user.postBanCount > 0 &&
                    (
                            user.postBanReason.isNotBlank() ||
                                    user.postBannedByName.isNotBlank() ||
                                    user.postBannedAt > 0L
                            )
                ) {

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 12.dp,
                            bottom = 12.dp
                        )
                    )

                    Text(
                        text = "Legutóbbi tiltás"
                    )

                    if (
                        user.postBanReason.isNotBlank()
                    ) {

                        Text(
                            text =
                                "Ok: ${user.postBanReason}",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (
                        user.postBannedByName.isNotBlank()
                    ) {

                        Text(
                            text =
                                "Kitiltotta: ${user.postBannedByName}",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (
                        user.postBannedAt > 0L
                    ) {

                        Text(
                            text =
                                "Időpont: ${
                                    formatProfileDate(
                                        user.postBannedAt
                                    )
                                }",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractorSection(
    user: User,
    application: ContractorApplication?,
    onContractorApplicationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "WasteUber kolléga"
            )

            when {

                user.role == "CONTRACTOR" -> {

                    Text(
                        text =
                            "✓ Elfogadott WasteUber kolléga",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                application?.status == "PENDING" -> {

                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        CircularProgressIndicator()

                        Text(
                            text =
                                "Jelentkezés elfogadásra vár"
                        )
                    }

                    ContractorApplicationDetails(
                        application = application
                    )
                }

                application?.status == "APPROVED" -> {

                    Text(
                        text =
                            "✓ Jelentkezés elfogadva",
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    ContractorApplicationDetails(
                        application = application
                    )
                }

                application?.status == "REJECTED" -> {

                    Text(
                        text =
                            "A korábbi jelentkezés elutasításra került.",
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Button(
                        onClick =
                            onContractorApplicationClick,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Új jelentkezés")
                    }
                }

                else -> {

                    Text(
                        text =
                            "Jelenleg ügyfélként használod a WasteUbert.",
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Button(
                        onClick =
                            onContractorApplicationClick,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            "Jelentkezés WasteUber kollégának"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractorApplicationDetails(
    application: ContractorApplication
) {
    HorizontalDivider(
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = 12.dp
        )
    )

    Text(
        text =
            "Jármű: ${application.vehicleBrand} ${application.vehicleModel}"
    )

    Text(
        text =
            "Rendszám: ${application.licensePlate}",
        modifier = Modifier.padding(top = 6.dp)
    )

    Text(
        text =
            "Jármű típusa: ${application.vehicleType}",
        modifier = Modifier.padding(top = 6.dp)
    )

    Text(
        text =
            "Max. terhelhetőség: ${application.maxLoadKg} kg",
        modifier = Modifier.padding(top = 6.dp)
    )

    Text(
        text =
            "Utánfutó: ${
                if (application.hasTrailer) {
                    "Van"
                } else {
                    "Nincs"
                }
            }",
        modifier = Modifier.padding(top = 6.dp)
    )
}

private fun getRoleName(
    role: String
): String {

    return when (role) {

        "ADMIN" ->
            "Admin"

        "CONTRACTOR" ->
            "WasteUber kolléga"

        "CUSTOMER" ->
            "Ügyfél"

        else ->
            role
    }
}

private fun formatProfileDate(
    timestamp: Long
): String {

    if (timestamp == 0L) {
        return ""
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