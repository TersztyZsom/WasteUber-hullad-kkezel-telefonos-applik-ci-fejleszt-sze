package hu.wasteuber.app.ui.screens.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.viewmodel.AuthViewModel
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeFeedScreen(
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onProfileClick: () -> Unit,
    onMyPostsClick: () -> Unit,
    onCreateWasteRequestClick: () -> Unit,
    onMyRequestsClick: () -> Unit,
    onAvailableJobsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    onCreatePostClick: () -> Unit,
    onPostClick: (String) -> Unit = {},
    onUserProfileClick: (String) -> Unit = {}
) {
    var posts by remember {
        mutableStateOf<List<Post>>(emptyList())
    }

    var currentUser by remember {
        mutableStateOf<User?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val canHandleWasteRequests =
        currentUser?.let { user ->

            user.role == "ADMIN" ||
                    user.role == "CONTRACTOR" ||
                    user.isUberPlus

        } ?: false

    fun loadPosts() {
        isLoading = true
        errorMessage = ""

        postViewModel.getPosts(
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

    fun loadCurrentUser() {

        authViewModel.getCurrentUser(

            onSuccess = { user ->

                currentUser =
                    user
            },

            onError = {

                currentUser =
                    null
            }
        )
    }

    LaunchedEffect(Unit) {

        loadPosts()
        loadCurrentUser()
    }

    Scaffold(
        topBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(52.dp)
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
                                .size(52.dp)
                                .scale(3.2f)
                        )
                    }

                    Text(
                        text = "WasteUber",
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }

                Box {

                    Button(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {

                        Text("☰")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,

                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("Profil")
                            },

                            onClick = {

                                menuExpanded = false
                                onProfileClick()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Saját posztjaim")
                            },

                            onClick = {

                                menuExpanded = false
                                onMyPostsClick()
                            }
                        )

                        DropdownMenuItem(
                            text = {

                                Text(
                                    "Hulladék elszállítás kérése"
                                )
                            },

                            onClick = {

                                menuExpanded = false
                                onCreateWasteRequestClick()
                            }
                        )

                        DropdownMenuItem(
                            text = {

                                Text(
                                    "Saját megrendeléseim"
                                )
                            },

                            onClick = {

                                menuExpanded = false
                                onMyRequestsClick()
                            }
                        )

                        if (canHandleWasteRequests) {

                            DropdownMenuItem(
                                text = {

                                    Text(
                                        "Elérhető fuvarok"
                                    )
                                },

                                onClick = {

                                    menuExpanded = false
                                    onAvailableJobsClick()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {

                                Text(
                                    "Értesítések"
                                )
                            },

                            onClick = {

                                menuExpanded = false
                                onNotificationsClick()
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text("Kijelentkezés")
                            },

                            onClick = {

                                menuExpanded = false
                                onLogoutClick()
                            }
                        )
                    }
                }
            }
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = onCreatePostClick
            ) {

                Text("+")
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            contentAlignment =
                Alignment.TopCenter
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
            ) {

                when {

                    isLoading -> {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            CircularProgressIndicator()
                        }
                    }

                    errorMessage.isNotEmpty() -> {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = errorMessage
                            )

                            Button(
                                onClick = {
                                    loadPosts()
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
                    }

                    posts.isEmpty() -> {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "Még nincsenek bejegyzések."
                            )

                            Text(
                                text =
                                    "Légy te az első, aki posztol!",

                                modifier =
                                    Modifier.padding(
                                        top = 8.dp
                                    )
                            )

                            Button(
                                onClick =
                                    onCreatePostClick,

                                modifier =
                                    Modifier.padding(
                                        top = 16.dp
                                    )
                            ) {

                                Text(
                                    "Új poszt"
                                )
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
                                items = posts,

                                key = { post ->
                                    post.id
                                }
                            ) { post ->

                                PostFeedItem(
                                    post = post,

                                    postViewModel =
                                        postViewModel,

                                    onPostClick = {

                                        onPostClick(
                                            post.id
                                        )
                                    },

                                    onUserProfileClick =
                                        onUserProfileClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostFeedItem(
    post: Post,
    postViewModel: PostViewModel,
    onPostClick: () -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    var isLiked by remember(
        post.id
    ) {
        mutableStateOf(false)
    }

    var likeCount by remember(
        post.id
    ) {
        mutableStateOf(
            post.likeCount
        )
    }

    var isLikeLoading by remember(
        post.id
    ) {
        mutableStateOf(false)
    }

    var likeErrorMessage by remember(
        post.id
    ) {
        mutableStateOf("")
    }

    LaunchedEffect(
        post.id
    ) {

        postViewModel.isPostLiked(
            postId = post.id,

            onSuccess = { liked ->
                isLiked = liked
            },

            onError = { error ->
                likeErrorMessage = error
            }
        )
    }

    Card(
        onClick = onPostClick,

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text =
                    post.userName,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.primary,

                modifier =
                    Modifier.clickable {

                        if (
                            post.userId.isNotBlank()
                        ) {

                            onUserProfileClick(
                                post.userId
                            )
                        }
                    }
            )

            Text(
                text =
                    formatPostDate(
                        post.createdAt
                    ),

                modifier =
                    Modifier.padding(
                        top = 4.dp
                    )
            )

            if (
                post.isEdited
            ) {

                Text(
                    text = "Szerkesztve",

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }

            if (
                post.roleMentions
                    .isNotEmpty()
            ) {

                Text(
                    text =
                        buildMentionSummary(
                            post.roleMentions
                        ),

                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )
            }

            if (
                post.text.isNotBlank()
            ) {

                MentionText(
                    text = post.text,

                    modifier =
                        Modifier.padding(
                            top = 16.dp
                        )
                )
            }

            PostMediaContent(
                post = post
            )

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
                        top = 8.dp
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = {

                        if (
                            isLikeLoading
                        ) {

                            return@TextButton
                        }

                        isLikeLoading = true
                        likeErrorMessage = ""

                        postViewModel.toggleLike(
                            postId = post.id,

                            onSuccess = {
                                    liked,
                                    newLikeCount ->

                                isLiked =
                                    liked

                                likeCount =
                                    newLikeCount

                                isLikeLoading =
                                    false
                            },

                            onError = { error ->

                                likeErrorMessage =
                                    error

                                isLikeLoading =
                                    false
                            }
                        )
                    },

                    enabled =
                        !isLikeLoading
                ) {

                    if (isLiked) {

                        Text(
                            text =
                                "❤️ $likeCount"
                        )

                    } else {

                        Text(
                            text =
                                "♡ $likeCount"
                        )
                    }
                }

                Text(
                    text =
                        "💬 ${post.commentCount}"
                )
            }

            if (
                likeErrorMessage
                    .isNotEmpty()
            ) {

                Text(
                    text =
                        likeErrorMessage,

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }
        }
    }
}

private fun buildMentionSummary(
    roleMentions: List<String>
): String {

    val labels =
        buildList {

            if (
                "CUSTOMERS" in roleMentions
            ) {

                add(
                    "Ügyfelek"
                )
            }

            if (
                "CONTRACTORS" in roleMentions
            ) {

                add(
                    "WasteUberesek"
                )
            }

            if (
                SubscriptionPlans.MENTION_WASTE_GO
                in roleMentions
            ) {

                add(
                    "WasteGo"
                )
            }

            if (
                SubscriptionPlans.MENTION_WASTE_PLUS
                in roleMentions
            ) {

                add(
                    "WastePlus"
                )
            }

            if (
                SubscriptionPlans.MENTION_WASTE_MAX
                in roleMentions
            ) {

                add(
                    "WasteMax"
                )
            }
        }

    return if (
        labels.isEmpty()
    ) {

        "Csoportos megjelölés"

    } else {

        "Csoportos megjelölés: ${
            labels.joinToString(
                separator = " + "
            )
        }"
    }
}

@Composable
private fun MentionText(
    text: String,
    modifier: Modifier = Modifier
) {
    val mentionColor =
        MaterialTheme
            .colorScheme
            .primary

    val annotatedText =
        remember(
            text,
            mentionColor
        ) {

            buildMentionText(
                text = text,
                mentionColor =
                    mentionColor
            )
        }

    Text(
        text =
            annotatedText,

        modifier =
            modifier
    )
}

private fun buildMentionText(
    text: String,
    mentionColor:
    androidx.compose.ui.graphics.Color
): AnnotatedString {

    val mentionRegex =
        Regex(
            pattern =
                "(@ügyfelek|@wasteuberesek|@wastego|@wasteplus|@wastemax)",

            option =
                RegexOption.IGNORE_CASE
        )

    return buildAnnotatedString {

        var lastIndex =
            0

        mentionRegex
            .findAll(text)
            .forEach { match ->

                if (
                    match.range.first >
                    lastIndex
                ) {

                    append(
                        text.substring(
                            startIndex =
                                lastIndex,

                            endIndex =
                                match.range.first
                        )
                    )
                }

                withStyle(
                    style =
                        SpanStyle(
                            color =
                                mentionColor,

                            fontWeight =
                                FontWeight.Bold
                        )
                ) {

                    append(
                        match.value
                    )
                }

                lastIndex =
                    match.range.last + 1
            }

        if (
            lastIndex <
            text.length
        ) {

            append(
                text.substring(
                    startIndex =
                        lastIndex
                )
            )
        }
    }
}

@Composable
private fun PostMediaContent(
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

        "IMAGE" -> {

            AsyncImage(
                model =
                    post.mediaUrl,

                contentDescription =
                    "Poszthoz csatolt kép",

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

        "GIF" -> {

            AsyncImage(
                model =
                    post.mediaUrl,

                contentDescription =
                    "Poszthoz csatolt GIF",

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

            PostVideoPlayer(
                videoUrl =
                    post.mediaUrl
            )
        }
    }
}

@Composable
private fun PostVideoPlayer(
    videoUrl: String
) {
    val context =
        LocalContext.current

    val exoPlayer =
        remember(
            videoUrl
        ) {

            ExoPlayer
                .Builder(context)
                .build()
                .apply {

                    val mediaItem =
                        MediaItem.fromUri(
                            videoUrl
                        )

                    setMediaItem(
                        mediaItem
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

private fun formatPostDate(
    timestamp: Long
): String {

    if (
        timestamp == 0L
    ) {

        return ""
    }

    val dateFormat =
        SimpleDateFormat(
            "yyyy.MM.dd. HH:mm",
            Locale.getDefault()
        )

    return dateFormat.format(
        Date(timestamp)
    )
}