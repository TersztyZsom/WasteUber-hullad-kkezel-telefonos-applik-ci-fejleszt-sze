package hu.wasteuber.app.ui.screens.contractor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun JobDetailsScreen(
    onAcceptClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Top,
        horizontalAlignment =
            Alignment.Start
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
                        "Fuvar részletei",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Text(
            text =
                "Hulladék típusa:",
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        )

        Text(
            text =
                "Mennyiség:",
            modifier =
                Modifier.padding(
                    top = 8.dp
                )
        )

        Text(
            text =
                "Cím:",
            modifier =
                Modifier.padding(
                    top = 8.dp
                )
        )

        Text(
            text =
                "Leírás:",
            modifier =
                Modifier.padding(
                    top = 8.dp
                )
        )

        Button(
            onClick =
                onAcceptClick,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {

            Text(
                "Fuvar elfogadása"
            )
        }
    }
}