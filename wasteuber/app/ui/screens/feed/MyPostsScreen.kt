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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyPostsScreen(
    postViewModel: PostViewModel,
    onBackClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onEditPostClick: (String) -> Unit
) {
    var posts by remember {
        mutableStateOf<List<Post>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var deletePost by remember {
        mutableStateOf<Post?>(null)
    }

    var deletingPostId by remember {
        mutableStateOf<String?>(null)
    }

    var deleteErrorMessage by remember {
        mutableStateOf("")
    }

    fun loadPosts() {
        isLoading = true
        errorMessage = ""

        postViewModel.getMyPosts(
            onSuccess = { loadedPosts ->
                posts = loadedPosts
                isLoading = false
            },

            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    val lifecycleOwner =
        LocalLifecycleOwner.current

    var hasLoadedOnce by remember {
        mutableStateOf(false)
    }

    DisposableEffect(
        lifecycleOwner
    ) {
        val observer =
            LifecycleEventObserver { _, event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {
                    loadPosts()
                    hasLoadedOnce = true
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(
                observer
            )

        if (
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(
                    Lifecycle.State.RESUMED
                ) &&
            !hasLoadedOnce
        ) {
            loadPosts()
            hasLoadedOnce = true
        }

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(
                    observer
                )
        }
    }

    if (
        deletePost != null
    ) {

        AlertDialog(
            onDismissRequest = {

                if (
                    deletingPostId == null
                ) {
                    deletePost = null
                    deleteErrorMessage = ""
                }
            },

            title = {
                Text(
                    "Poszt törlése"
                )
            },

            text = {

                Column {

                    Text(
                        "Biztosan törölni szeretnéd ezt a posztot?"
                    )

                    if (
                        deleteErrorMessage.isNotBlank()
                    ) {

                        Text(
                            text =
                                deleteErrorMessage,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,

                            modifier =
                                Modifier.padding(
                                    top = 12.dp
                                )
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val selectedPost =
                            deletePost
                                ?: return@Button

                        deletingPostId =
                            selectedPost.id

                        deleteErrorMessage =
                            ""

                        errorMessage =
                            ""

                        postViewModel.deleteOwnPost(
                            postId =
                                selectedPost.id,

                            onSuccess = {

                                /*
                                 * Azonnal eltávolítjuk
                                 * a képernyőn látható listából.
                                 */
                                posts =
                                    posts.filterNot {
                                        it.id ==
                                                selectedPost.id
                                    }

                                deletingPostId =
                                    null

                                deletePost =
                                    null

                                deleteErrorMessage =
                                    ""

                                /*
                                 * Firestore-ból is újratöltjük,
                                 * hogy biztosan a valós állapot
                                 * jelenjen meg.
                                 */
                                loadPosts()
                            },

                            onError = { error ->

                                deletingPostId =
                                    null

                                /*
                                 * A törlési hibát közvetlenül
                                 * a felugró ablakban mutatjuk.
                                 */
                                deleteErrorMessage =
                                    error
                            }
                        )
                    },

                    enabled =
                        deletingPostId == null
                ) {

                    if (
                        deletingPostId != null
                    ) {

                        CircularProgressIndicator()

                    } else {

                        Text(
                            "Törlés"
                        )
                    }
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        deletePost =
                            null

                        deleteErrorMessage =
                            ""
                    },

                    enabled =
                        deletingPostId == null
                ) {

                    Text(
                        "Mégse"
                    )
                }
            }
        )
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
                                "Saját posztjaim",

                            style =
                                MaterialTheme.typography.headlineSmall,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    OutlinedButton(
                        onClick =
                            onBackClick
                    ) {

                        Text(
                            "Vissza"
                        )
                    }

                    Button(
                        onClick = {
                            loadPosts()
                        },

                        enabled =
                            !isLoading
                    ) {

                        Text(
                            "Frissítés"
                        )
                    }
                }
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
                            top = 12.dp
                        )
                )
            }

            when {

                isLoading -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                40.dp
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                posts.isEmpty() -> {

                    Text(
                        text =
                            "Még nincs saját posztod.",

                        modifier =
                            Modifier.padding(
                                top = 32.dp
                            )
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 16.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        items(
                            items = posts,

                            key = { post ->
                                post.id
                            }
                        ) { post ->

                            MyPostCard(
                                post = post,

                                onPostClick = {

                                    onPostClick(
                                        post.id
                                    )
                                },

                                onEditClick = {

                                    onEditPostClick(
                                        post.id
                                    )
                                },

                                onDeleteClick = {

                                    deletePost =
                                        post

                                    deleteErrorMessage =
                                        ""
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
private fun MyPostCard(
    post: Post,
    onPostClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    16.dp
                )
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
                        formatMyPostDate(
                            post.createdAt
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )

                PostApprovalStatus(
                    post =
                        post
                )
            }

            if (
                post.isEdited
            ) {

                Text(
                    text =
                        "Szerkesztve",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }

            if (
                post.text.isNotBlank()
            ) {

                Text(
                    text =
                        post.text,

                    modifier =
                        Modifier.padding(
                            top = 16.dp
                        )
                )
            }

            MyPostMediaContent(
                post =
                    post
            )

            if (
                post.approvalStatus ==
                "REJECTED"
            ) {

                HorizontalDivider(
                    modifier =
                        Modifier.padding(
                            top = 16.dp
                        )
                )

                Text(
                    text =
                        "A poszt elutasításra került.",

                    fontWeight =
                        FontWeight.Bold,

                    modifier =
                        Modifier.padding(
                            top = 12.dp
                        )
                )

                if (
                    post.rejectionReason
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            "Indok: ${post.rejectionReason}",

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }

                if (
                    post.reviewedByName
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            "Elbírálta: ${post.reviewedByName}",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }
            }

            if (
                post.approvalStatus ==
                "PENDING"
            ) {

                Text(
                    text =
                        "A poszt jelenleg admin jóváhagyásra vár, ezért még nem látható a közösségi feedben.",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    modifier =
                        Modifier.padding(
                            top = 12.dp
                        )
                )
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 16.dp
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 10.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    onClick =
                        onPostClick
                ) {

                    Text(
                        "Megnyitás"
                    )
                }

                OutlinedButton(
                    onClick =
                        onEditClick
                ) {

                    Text(
                        "Szerkesztés"
                    )
                }

                TextButton(
                    onClick =
                        onDeleteClick
                ) {

                    Text(
                        "Törlés"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        16.dp
                    )
            ) {

                Text(
                    text =
                        "❤️ ${post.likeCount}"
                )

                Text(
                    text =
                        "💬 ${post.commentCount}"
                )
            }
        }
    }
}

@Composable
private fun PostApprovalStatus(
    post: Post
) {
    val text =
        when (
            post.approvalStatus
        ) {

            "PENDING" ->
                "⏳ Jóváhagyásra vár"

            "REJECTED" ->
                "❌ Elutasítva"

            else ->
                "✓ Jóváhagyva"
        }

    Text(
        text =
            text,

        fontWeight =
            FontWeight.Bold,

        color =
            when (
                post.approvalStatus
            ) {

                "PENDING" ->
                    MaterialTheme
                        .colorScheme
                        .tertiary

                "REJECTED" ->
                    MaterialTheme
                        .colorScheme
                        .error

                else ->
                    MaterialTheme
                        .colorScheme
                        .primary
            }
    )
}

@Composable
private fun MyPostMediaContent(
    post: Post
) {
    if (
        post.mediaUrl.isBlank()
    ) {
        return
    }

    when (
        post.mediaType
    ) {

        "IMAGE",
        "GIF" -> {

            AsyncImage(
                model =
                    post.mediaUrl,

                contentDescription =
                    "Poszt média",

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 180.dp,
                        max = 500.dp
                    )
                    .padding(
                        top = 16.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )
        }

        "VIDEO" -> {

            MyPostVideoPlayer(
                videoUrl =
                    post.mediaUrl
            )
        }
    }
}

@Composable
private fun MyPostVideoPlayer(
    videoUrl: String
) {
    val context =
        LocalContext.current

    val exoPlayer =
        remember(
            videoUrl
        ) {

            ExoPlayer
                .Builder(
                    context
                )
                .build()
                .apply {

                    setMediaItem(
                        MediaItem.fromUri(
                            videoUrl
                        )
                    )

                    playWhenReady =
                        false

                    prepare()
                }
        }

    DisposableEffect(
        exoPlayer
    ) {

        onDispose {

            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { viewContext ->

            PlayerView(
                viewContext
            ).apply {

                player =
                    exoPlayer

                useController =
                    true
            }
        },

        update = { playerView ->

            playerView.player =
                exoPlayer
        },

        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(
                16f / 9f
            )
            .padding(
                top = 16.dp
            )
    )
}

private fun formatMyPostDate(
    timestamp: Long
): String {

    if (
        timestamp == 0L
    ) {
        return ""
    }

    val formatter =
        SimpleDateFormat(
            "yyyy.MM.dd. HH:mm",
            Locale.getDefault()
        )

    return formatter.format(
        Date(
            timestamp
        )
    )
}
