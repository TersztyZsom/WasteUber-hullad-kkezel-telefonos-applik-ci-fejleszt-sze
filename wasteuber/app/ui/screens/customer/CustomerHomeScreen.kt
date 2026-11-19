package hu.wasteuber.app.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomerHomeScreen(
    onCreateRequestClick: () -> Unit = {},
    onMyRequestsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Megrendelői kezdőlap")

        Button(
            onClick = onCreateRequestClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Új elszállítási igény")
        }

        Button(
            onClick = onMyRequestsClick,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Megrendeléseim")
        }

        Button(
            onClick = onProfileClick,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Profil")
        }
    }
}