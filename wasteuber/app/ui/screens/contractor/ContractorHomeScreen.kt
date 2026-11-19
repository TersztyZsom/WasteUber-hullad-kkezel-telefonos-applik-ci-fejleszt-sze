package hu.wasteuber.app.ui.screens.contractor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
fun ContractorHomeScreen(
    onAvailableJobsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(96.dp)
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
                    .size(96.dp)
                    .scale(3.2f)
            )
        }

        Text(
            text = "WasteUber",
            style =
                MaterialTheme.typography.headlineMedium,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        )

        Text(
            text =
                "WasteUberes kezdőlap",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    top = 4.dp
                )
        )

        Button(
            onClick =
                onAvailableJobsClick,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {
            Text(
                "Elérhető fuvarok"
            )
        }

        Button(
            onClick =
                onProfileClick,
            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        ) {
            Text(
                "Profil"
            )
        }
    }
}