package hu.wasteuber.app.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.R

@Composable
fun AdminHomeScreen(
    onFeedClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    onUsersModerationClick: () -> Unit,
    onPostsModerationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onPendingPostsClick: () -> Unit = {},
    onAvailableJobsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
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
                        .size(72.dp)
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
                            .size(72.dp)
                            .scale(3.2f)
                    )
                }

                Column {

                    Text(
                        text = "WasteUber Admin",
                        style =
                            MaterialTheme.typography.headlineMedium,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text =
                            "Adminisztrációs vezérlőpult",
                        style =
                            MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )

            AdminMenuCard(
                title =
                    "WasteUber feed",

                description =
                    "A közösségi feed megnyitása és admin poszt létrehozása.",

                buttonText =
                    "Feed megnyitása",

                onClick =
                    onFeedClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            AdminMenuCard(
                title =
                    "Elérhető fuvarok",

                description =
                    "A még szabad hulladékelszállítási igények megtekintése, elfogadása vagy elutasítása.",

                buttonText =
                    "Fuvarok megnyitása",

                onClick =
                    onAvailableJobsClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            AdminMenuCard(
                title =
                    "Értesítések",

                description =
                    "Az admin fiókhoz tartozó értesítések megtekintése.",

                buttonText =
                    "Értesítések megnyitása",

                onClick =
                    onNotificationsClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            AdminMenuCard(
                title =
                    "Jóváhagyásra váró posztok",

                description =
                    "Kredit nélkül létrehozott posztok jóváhagyása vagy elutasítása.",

                buttonText =
                    "Függő posztok",

                onClick =
                    onPendingPostsClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            AdminMenuCard(
                title =
                    "WasteUber jelentkezések",

                description =
                    "A WasteUber partnernek jelentkező felhasználók kezelése.",

                buttonText =
                    "Jelentkezések",

                onClick =
                    onApplicationsClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            AdminMenuCard(
                title =
                    "Felhasználók moderálása",

                description =
                    "Felhasználók posztolási tiltásainak és moderációjának kezelése.",

                buttonText =
                    "Felhasználók",

                onClick =
                    onUsersModerationClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            AdminMenuCard(
                title =
                    "Posztok moderálása",

                description =
                    "Már közzétett posztok moderálása és adminisztrátori törlése.",

                buttonText =
                    "Posztok",

                onClick =
                    onPostsModerationClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )

            OutlinedButton(
                onClick =
                    onLogoutClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Kijelentkezés"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )
        }
    }
}

@Composable
private fun AdminMenuCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(
                text =
                    title,

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    description,

                style =
                    MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick =
                    onClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    buttonText
                )
            }
        }
    }
}