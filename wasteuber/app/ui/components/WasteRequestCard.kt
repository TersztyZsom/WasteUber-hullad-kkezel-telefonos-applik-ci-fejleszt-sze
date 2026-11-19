package hu.wasteuber.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.data.model.WasteRequest

@Composable
fun WasteRequestCard(
    request: WasteRequest
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Hulladék típusa: ${request.wasteType}")
            Text(text = "Mennyiség: ${request.amount}")
            Text(text = "Cím: ${request.address}")
            Text(text = "Státusz: ${request.status}")
        }
    }
}