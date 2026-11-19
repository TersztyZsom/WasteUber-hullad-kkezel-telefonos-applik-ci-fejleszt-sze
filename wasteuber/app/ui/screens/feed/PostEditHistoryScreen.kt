package hu.wasteuber.app.ui.screens.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.PostEdit
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostEditHistoryScreen(
    postId: String,
    postViewModel: PostViewModel,
    onBackClick: () -> Unit
) {
    var editHistory by remember {
        mutableStateOf<List<PostEdit>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    fun loadHistory() {
        isLoading = true
        errorMessage = ""

        postViewModel.getEditHistory(
            postId = postId,

            onSuccess = { history ->
                editHistory = history
                isLoading = false
            },

            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(postId) {
        loadHistory()
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
                            text =
                                "Szerkesztési előzmények",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackClick
                ) {
                    Text("Vissza")
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
                                loadHistory()
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Újrapróbálás")
                        }
                    }
                }

                editHistory.isEmpty() -> {

                    Text(
                        text =
                            "Ehhez a poszthoz még nincs szerkesztési előzmény.",
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        items(
                            items = editHistory,
                            key = { edit ->
                                edit.id
                            }
                        ) { edit ->

                            PostEditHistoryItem(
                                edit = edit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostEditHistoryItem(
    edit: PostEdit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Szerkesztés időpontja"
            )

            Text(
                text = formatEditHistoryDate(
                    edit.editedAt
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 16.dp,
                    bottom = 16.dp
                )
            )

            Text(
                text = "Korábbi verzió"
            )

            if (edit.oldText.isNotBlank()) {

                Text(
                    text = edit.oldText,
                    modifier = Modifier.padding(top = 10.dp)
                )

            } else {

                Text(
                    text = "Nem volt szöveg.",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            EditHistoryMediaPreview(
                mediaUrl = edit.oldMediaUrl,
                mediaType = edit.oldMediaType
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 16.dp
                )
            )

            Text(
                text = "Módosított verzió"
            )

            if (edit.newText.isNotBlank()) {

                Text(
                    text = edit.newText,
                    modifier = Modifier.padding(top = 10.dp)
                )

            } else {

                Text(
                    text = "Nincs szöveg.",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            EditHistoryMediaPreview(
                mediaUrl = edit.newMediaUrl,
                mediaType = edit.newMediaType
            )

            if (
                edit.oldText == edit.newText &&
                edit.oldMediaUrl != edit.newMediaUrl
            ) {

                Text(
                    text = "Csak a csatolt média változott.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (
                edit.oldText != edit.newText &&
                edit.oldMediaUrl == edit.newMediaUrl &&
                edit.oldMediaType == edit.newMediaType
            ) {

                Text(
                    text = "Csak a poszt szövege változott.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditHistoryMediaPreview(
    mediaUrl: String,
    mediaType: String
) {
    if (
        mediaUrl.isBlank() ||
        mediaType == "NONE"
    ) {

        Text(
            text = "Nincs csatolt média.",
            modifier = Modifier.padding(top = 10.dp)
        )

        return
    }

    when (mediaType) {

        "IMAGE" -> {

            Text(
                text = "Kép:",
                modifier = Modifier.padding(top = 12.dp)
            )

            AsyncImage(
                model = mediaUrl,
                contentDescription =
                    "Szerkesztési előzmény képe",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 180.dp,
                        max = 600.dp
                    )
                    .padding(top = 8.dp),
                contentScale = ContentScale.Fit
            )
        }

        "GIF" -> {

            Text(
                text = "GIF:",
                modifier = Modifier.padding(top = 12.dp)
            )

            AsyncImage(
                model = mediaUrl,
                contentDescription =
                    "Szerkesztési előzmény GIF-je",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 180.dp,
                        max = 600.dp
                    )
                    .padding(top = 8.dp),
                contentScale = ContentScale.Fit
            )
        }

        "VIDEO" -> {

            Text(
                text = "Videó:",
                modifier = Modifier.padding(top = 12.dp)
            )

            EditHistoryVideoPlayer(
                videoUrl = mediaUrl
            )
        }

        else -> {

            Text(
                text =
                    "Ismeretlen médiatípus: $mediaType",
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun EditHistoryVideoPlayer(
    videoUrl: String
) {
    val context =
        LocalContext.current

    val exoPlayer =
        remember(videoUrl) {

            ExoPlayer.Builder(context)
                .build()
                .apply {

                    setMediaItem(
                        MediaItem.fromUri(
                            videoUrl
                        )
                    )

                    playWhenReady = false

                    prepare()
                }
        }

    DisposableEffect(exoPlayer) {

        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { viewContext ->

            PlayerView(viewContext).apply {
                player = exoPlayer
                useController = true
            }
        },

        update = { playerView ->

            playerView.player =
                exoPlayer
        },

        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .padding(top = 8.dp)
    )
}

private fun formatEditHistoryDate(
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