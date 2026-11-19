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
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.viewmodel.UserProfileViewModel

@Composable
fun PublicUserProfileScreen(
    userId: String,
    userProfileViewModel: UserProfileViewModel,
    onBackClick: () -> Unit
) {
    var user by remember(userId) {
        mutableStateOf<User?>(null)
    }

    var isLoading by remember(userId) {
        mutableStateOf(true)
    }

    var errorMessage by remember(userId) {
        mutableStateOf("")
    }

    fun loadUser() {

        isLoading = true
        errorMessage = ""

        userProfileViewModel.getUserById(
            userId = userId,

            onSuccess = { loadedUser ->

                user = loadedUser
                isLoading = false
            },

            onError = { error ->

                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(userId) {

        loadUser()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 800.dp)
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
                                "Felhasználói profil",
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

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.padding(
                                top = 32.dp
                            )
                    )
                }

                errorMessage.isNotBlank() -> {

                    Text(
                        text =
                            errorMessage,
                        modifier =
                            Modifier.padding(
                                top = 24.dp
                            )
                    )

                    Button(
                        onClick = {

                            loadUser()
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

                user != null -> {

                    PublicProfileContent(
                        user = user!!
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicProfileContent(
    user: User
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                text =
                    if (
                        user.name.isBlank()
                    ) {
                        "Felhasználó"
                    } else {
                        user.name
                    },
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    getPublicRoleName(
                        user.role
                    ),
                style =
                    MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            if (
                user.isUberPlus
            ) {

                Text(
                    text =
                        "WasteUber Plus",
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 20.dp
                    )
            )

            Text(
                text =
                    "Profilinformációk",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 14.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        "Felhasználótípus"
                )

                Text(
                    text =
                        getPublicRoleName(
                            user.role
                        ),
                    fontWeight =
                        FontWeight.Medium
                )
            }

            if (
                user.role ==
                "CONTRACTOR"
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            "WasteUber partner"
                    )

                    Text(
                        text =
                            "Igen",
                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }

            if (
                user.role ==
                "ADMIN"
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            "WasteUber admin"
                    )

                    Text(
                        text =
                            "Igen",
                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }

            if (
                user.role ==
                "CUSTOMER"
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            "Ügyfél"
                    )

                    Text(
                        text =
                            "Igen",
                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 20.dp
                    )
            )

            Text(
                text =
                    "A felhasználó privát adatai nem nyilvánosak.",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun getPublicRoleName(
    role: String
): String {

    return when (
        role
    ) {

        "ADMIN" ->
            "Admin"

        "CONTRACTOR" ->
            "WasteUber partner"

        "CUSTOMER" ->
            "Ügyfél"

        else ->
            "Felhasználó"
    }
}