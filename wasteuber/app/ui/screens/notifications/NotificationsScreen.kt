package hu.wasteuber.app.ui.screens.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import hu.wasteuber.app.data.model.Notification
import hu.wasteuber.app.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notificationViewModel: NotificationViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {

    var notifications by remember {
        mutableStateOf<List<Notification>>(
            emptyList()
        )
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isMarkingAllAsRead by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    fun loadNotifications() {

        isLoading = true
        errorMessage = ""

        notificationViewModel.getMyNotifications(

            onSuccess = { loadedNotifications ->

                notifications =
                    loadedNotifications

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

        loadNotifications()
    }

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
                                MaterialTheme.typography.titleMedium,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text =
                                "Értesítések",
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
                        !isMarkingAllAsRead
                ) {

                    Text(
                        "Vissza"
                    )
                }
            }

            val unreadCount =
                notifications.count {
                    !it.read
                }

            if (
                unreadCount > 0
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            bottom = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            "Olvasatlan: $unreadCount"
                    )

                    Button(
                        enabled =
                            !isMarkingAllAsRead,

                        onClick = {

                            isMarkingAllAsRead =
                                true

                            errorMessage =
                                ""

                            notificationViewModel
                                .markAllAsRead(

                                    onSuccess = {

                                        notifications =
                                            notifications.map {
                                                    notification ->

                                                notification.copy(
                                                    read = true
                                                )
                                            }

                                        isMarkingAllAsRead =
                                            false
                                    },

                                    onError = { error ->

                                        errorMessage =
                                            error

                                        isMarkingAllAsRead =
                                            false
                                    }
                                )
                        }
                    ) {

                        if (
                            isMarkingAllAsRead
                        ) {

                            Text(
                                "Folyamatban..."
                            )

                        } else {

                            Text(
                                "Összes olvasottra"
                            )
                        }
                    }
                }

            } else {

                Text(
                    text =
                        "Nincs olvasatlan értesítésed.",
                    modifier =
                        Modifier.padding(
                            top = 16.dp,
                            bottom = 12.dp
                        )
                )
            }

            if (
                errorMessage.isNotBlank()
            ) {

                Text(
                    text =
                        errorMessage,
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    modifier =
                        Modifier.padding(
                            bottom = 12.dp
                        )
                )
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        bottom = 12.dp
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

                notifications.isEmpty() -> {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "Még nincs értesítésed."
                            )

                            Text(
                                text =
                                    "Az alkalmazással kapcsolatos fontos események itt fognak megjelenni.",
                                modifier =
                                    Modifier.padding(
                                        top = 8.dp
                                    )
                            )

                            Button(
                                onClick = {

                                    loadNotifications()
                                },
                                modifier =
                                    Modifier.padding(
                                        top = 16.dp
                                    )
                            ) {

                                Text(
                                    "Frissítés"
                                )
                            }
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
                            items =
                                notifications,

                            key = { notification ->

                                notification.id
                            }
                        ) { notification ->

                            NotificationCard(
                                notification =
                                    notification,

                                onClick = {

                                    /*
                                     * Ha már olvasott,
                                     * nincs további teendő.
                                     */
                                    if (
                                        notification.read
                                    ) {
                                        return@NotificationCard
                                    }

                                    notificationViewModel
                                        .markAsRead(

                                            notificationId =
                                                notification.id,

                                            onSuccess = {

                                                notifications =
                                                    notifications.map {
                                                            current ->

                                                        if (
                                                            current.id ==
                                                            notification.id
                                                        ) {

                                                            current.copy(
                                                                read = true
                                                            )

                                                        } else {

                                                            current
                                                        }
                                                    }
                                            },

                                            onError = { error ->

                                                errorMessage =
                                                    error
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
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
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

                Text(
                    text =
                        notification.title
                            .ifBlank {
                                "Értesítés"
                            },

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        if (
                            notification.read
                        ) {
                            FontWeight.Normal
                        } else {
                            FontWeight.Bold
                        }
                )

                if (
                    !notification.read
                ) {

                    Text(
                        text =
                            "ÚJ",
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            if (
                notification.message
                    .isNotBlank()
            ) {

                Text(
                    text =
                        notification.message,

                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )
            }

            if (
                notification.createdAt > 0L
            ) {

                Text(
                    text =
                        formatNotificationDate(
                            notification.createdAt
                        ),

                    style =
                        MaterialTheme.typography.bodySmall,

                    modifier =
                        Modifier.padding(
                            top = 10.dp
                        )
                )
            }

            if (
                !notification.read
            ) {

                Text(
                    text =
                        "Koppints az olvasottra jelöléshez.",

                    style =
                        MaterialTheme.typography.bodySmall,

                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )
            }
        }
    }
}

private fun formatNotificationDate(
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