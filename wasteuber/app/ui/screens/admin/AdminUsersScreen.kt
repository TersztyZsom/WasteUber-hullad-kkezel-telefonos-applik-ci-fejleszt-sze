package hu.wasteuber.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.viewmodel.AdminModerationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminUsersScreen(
    adminModerationViewModel: AdminModerationViewModel,
    onBackClick: () -> Unit
) {
    var users by remember {
        mutableStateOf<List<User>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var actionUserId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedUser by remember {
        mutableStateOf<User?>(null)
    }

    var selectedBanType by remember {
        mutableStateOf("")
    }

    var banReason by remember {
        mutableStateOf("")
    }

    fun loadUsers() {
        isLoading = true
        errorMessage = ""

        adminModerationViewModel.getUsers(
            onSuccess = { loadedUsers ->
                users = loadedUsers
                isLoading = false
            },

            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 900.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onBackClick
                ) {
                    Text("Vissza")
                }

                Text("Felhasználók moderálása")
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
                                loadUsers()
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Újrapróbálás")
                        }
                    }
                }

                users.isEmpty() -> {
                    Text(
                        text = "Nincs megjeleníthető felhasználó.",
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = users,
                            key = { user ->
                                user.id
                            }
                        ) { user ->

                            AdminUserItem(
                                user = user,
                                isActionLoading =
                                    actionUserId == user.id,

                                onBanOneDay = {
                                    selectedUser = user
                                    selectedBanType = "ONE_DAY"
                                    banReason = ""
                                },

                                onBanOneWeek = {
                                    selectedUser = user
                                    selectedBanType = "ONE_WEEK"
                                    banReason = ""
                                },

                                onBanOneMonth = {
                                    selectedUser = user
                                    selectedBanType = "ONE_MONTH"
                                    banReason = ""
                                },

                                onBanPermanently = {
                                    selectedUser = user
                                    selectedBanType = "PERMANENT"
                                    banReason = ""
                                },

                                onRemoveBan = {
                                    actionUserId = user.id
                                    errorMessage = ""

                                    adminModerationViewModel.removePostingBan(
                                        userId = user.id,

                                        onSuccess = {
                                            actionUserId = null
                                            loadUsers()
                                        },

                                        onError = { error ->
                                            actionUserId = null
                                            errorMessage = error
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val userForBan =
        selectedUser

    if (userForBan != null) {

        AlertDialog(
            onDismissRequest = {
                if (actionUserId == null) {
                    selectedUser = null
                    selectedBanType = ""
                    banReason = ""
                }
            },

            title = {
                Text("Posztolási tiltás")
            },

            text = {

                Column {

                    Text(
                        text =
                            "Felhasználó: ${userForBan.name}"
                    )

                    Text(
                        text =
                            "Időtartam: ${
                                getBanTypeText(
                                    selectedBanType
                                )
                            }",
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    OutlinedTextField(
                        value = banReason,

                        onValueChange = {
                            banReason = it
                        },

                        label = {
                            Text("Kitiltás oka")
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),

                        minLines = 2,
                        maxLines = 5
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        if (banReason.isBlank()) {
                            errorMessage =
                                "A kitiltás okát kötelező megadni."

                            return@Button
                        }

                        actionUserId =
                            userForBan.id

                        errorMessage = ""

                        val onSuccess: () -> Unit = {

                            actionUserId = null
                            selectedUser = null
                            selectedBanType = ""
                            banReason = ""

                            loadUsers()
                        }

                        val onError: (String) -> Unit = { error ->

                            actionUserId = null
                            errorMessage = error
                        }

                        when (selectedBanType) {

                            "ONE_DAY" -> {
                                adminModerationViewModel.banForOneDay(
                                    userId =
                                        userForBan.id,
                                    reason =
                                        banReason.trim(),
                                    onSuccess =
                                        onSuccess,
                                    onError =
                                        onError
                                )
                            }

                            "ONE_WEEK" -> {
                                adminModerationViewModel.banForOneWeek(
                                    userId =
                                        userForBan.id,
                                    reason =
                                        banReason.trim(),
                                    onSuccess =
                                        onSuccess,
                                    onError =
                                        onError
                                )
                            }

                            "ONE_MONTH" -> {
                                adminModerationViewModel.banForOneMonth(
                                    userId =
                                        userForBan.id,
                                    reason =
                                        banReason.trim(),
                                    onSuccess =
                                        onSuccess,
                                    onError =
                                        onError
                                )
                            }

                            "PERMANENT" -> {
                                adminModerationViewModel.banPermanently(
                                    userId =
                                        userForBan.id,
                                    reason =
                                        banReason.trim(),
                                    onSuccess =
                                        onSuccess,
                                    onError =
                                        onError
                                )
                            }

                            else -> {
                                actionUserId = null
                                errorMessage =
                                    "Ismeretlen kitiltási időtartam."
                            }
                        }
                    },

                    enabled =
                        actionUserId == null &&
                                banReason.isNotBlank()
                ) {

                    if (actionUserId != null) {
                        CircularProgressIndicator()
                    } else {
                        Text("Kitiltás")
                    }
                }
            },

            dismissButton = {

                Button(
                    onClick = {
                        selectedUser = null
                        selectedBanType = ""
                        banReason = ""
                    },
                    enabled = actionUserId == null
                ) {
                    Text("Mégse")
                }
            }
        )
    }
}

@Composable
private fun AdminUserItem(
    user: User,
    isActionLoading: Boolean,
    onBanOneDay: () -> Unit,
    onBanOneWeek: () -> Unit,
    onBanOneMonth: () -> Unit,
    onBanPermanently: () -> Unit,
    onRemoveBan: () -> Unit
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
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = user.name
            )

            Text(
                text = user.email,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Szerepkör: ${user.role}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text =
                    "Összes posztolási tiltás: ${user.postBanCount}",
                modifier = Modifier.padding(top = 8.dp)
            )

            if (hasActiveBan) {

                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 12.dp
                    )
                )

                Text(
                    text = "Aktív posztolási tiltás"
                )

                Text(
                    text =
                        if (permanentlyBanned) {
                            "Időtartam: Végleges"
                        } else {
                            "Tiltás vége: ${
                                formatAdminUserDate(
                                    user.postBanUntil
                                )
                            }"
                        },
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (user.postBanReason.isNotBlank()) {

                    Text(
                        text =
                            "Ok: ${user.postBanReason}",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (user.postBannedByName.isNotBlank()) {

                    Text(
                        text =
                            "Kitiltotta: ${user.postBannedByName}",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (user.postBannedAt > 0L) {

                    Text(
                        text =
                            "Kitiltás időpontja: ${
                                formatAdminUserDate(
                                    user.postBannedAt
                                )
                            }",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            } else if (
                user.postBanReason.isNotBlank() ||
                user.postBannedByName.isNotBlank() ||
                user.postBannedAt > 0L
            ) {

                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 12.dp
                    )
                )

                Text(
                    text = "Legutóbbi moderáció"
                )

                if (user.postBanReason.isNotBlank()) {

                    Text(
                        text =
                            "Ok: ${user.postBanReason}",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (user.postBannedByName.isNotBlank()) {

                    Text(
                        text =
                            "Kitiltotta: ${user.postBannedByName}",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (user.postBannedAt > 0L) {

                    Text(
                        text =
                            "Időpont: ${
                                formatAdminUserDate(
                                    user.postBannedAt
                                )
                            }",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Text(
                    text = "A tiltás jelenleg nem aktív.",
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (user.role == "ADMIN") {

                Text(
                    text =
                        "Adminisztrátor nem tiltható ki.",
                    modifier = Modifier.padding(top = 16.dp)
                )

            } else {

                if (isActionLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp)
                    )

                } else {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = onBanOneDay
                        ) {
                            Text("1 nap")
                        }

                        Button(
                            onClick = onBanOneWeek
                        ) {
                            Text("1 hét")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = onBanOneMonth
                        ) {
                            Text("1 hónap")
                        }

                        Button(
                            onClick = onBanPermanently
                        ) {
                            Text("Végleges")
                        }
                    }

                    if (hasActiveBan) {

                        Button(
                            onClick = onRemoveBan,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Tiltás feloldása")
                        }
                    }
                }
            }
        }
    }
}

private fun getBanTypeText(
    banType: String
): String {

    return when (banType) {

        "ONE_DAY" ->
            "1 nap"

        "ONE_WEEK" ->
            "1 hét"

        "ONE_MONTH" ->
            "1 hónap"

        "PERMANENT" ->
            "Végleges"

        else ->
            "Ismeretlen"
    }
}

private fun formatAdminUserDate(
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