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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.Comment
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostDetailsScreen(
    postId: String,
    postViewModel: PostViewModel,
    onBackClick: () -> Unit,
    onEditHistoryClick: (String) -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    var post by remember {
        mutableStateOf<Post?>(null)
    }

    var comments by remember {
        mutableStateOf<List<Comment>>(emptyList())
    }

    var commentText by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isCommentSending by remember {
        mutableStateOf(false)
    }

    var isLiked by remember {
        mutableStateOf(false)
    }

    var likeCount by remember {
        mutableStateOf(0)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    fun loadPost() {
        postViewModel.getPost(
            postId = postId,

            onSuccess = { loadedPost ->
                post = loadedPost
                likeCount = loadedPost.likeCount
            },

            onError = { error ->
                errorMessage = error
            }
        )
    }

    fun loadComments() {
        postViewModel.getComments(
            postId = postId,

            onSuccess = { loadedComments ->
                comments = loadedComments
                isLoading = false
            },

            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(postId) {

        loadPost()

        postViewModel.isPostLiked(
            postId = postId,

            onSuccess = { liked ->
                isLiked = liked
            },

            onError = { error ->
                errorMessage = error
            }
        )

        loadComments()
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
                                "Poszt részletei",

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

            if (isLoading) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.padding(top = 24.dp)
                )

            } else {

                post?.let { currentPost ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Text(
                                text = currentPost.userName,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier.clickable {

                                        if (
                                            currentPost.userId
                                                .isNotBlank()
                                        ) {
                                            onUserProfileClick(
                                                currentPost.userId
                                            )
                                        }
                                    }
                            )

                            Text(
                                text =
                                    formatPostDetailsDate(
                                        currentPost.createdAt
                                    ),
                                modifier =
                                    Modifier.padding(top = 4.dp)
                            )

                            if (currentPost.isEdited) {

                                TextButton(
                                    onClick = {

                                        onEditHistoryClick(
                                            currentPost.id
                                        )
                                    },
                                    modifier =
                                        Modifier.padding(top = 4.dp)
                                ) {

                                    Text(
                                        text =
                                            "Szerkesztve – előzmények megtekintése"
                                    )
                                }
                            }

                            if (
                                currentPost
                                    .roleMentions
                                    .isNotEmpty()
                            ) {

                                Text(
                                    text =
                                        buildPostDetailsMentionSummary(
                                            currentPost.roleMentions
                                        ),

                                    modifier =
                                        Modifier.padding(top = 8.dp)
                                )
                            }

                            if (
                                currentPost.text.isNotBlank()
                            ) {

                                PostDetailsMentionText(
                                    text =
                                        currentPost.text,
                                    modifier =
                                        Modifier.padding(
                                            top = 16.dp
                                        )
                                )
                            }

                            PostDetailsMedia(
                                post = currentPost
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
                                    .padding(top = 8.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                TextButton(
                                    onClick = {

                                        postViewModel.toggleLike(
                                            postId =
                                                currentPost.id,

                                            onSuccess = {
                                                    liked,
                                                    newLikeCount ->

                                                isLiked = liked
                                                likeCount =
                                                    newLikeCount
                                            },

                                            onError = { error ->

                                                errorMessage =
                                                    error
                                            }
                                        )
                                    }
                                ) {

                                    if (isLiked) {

                                        Text(
                                            "❤️ $likeCount"
                                        )

                                    } else {

                                        Text(
                                            "♡ $likeCount"
                                        )
                                    }
                                }

                                Text(
                                    text =
                                        "💬 ${currentPost.commentCount}"
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Kommentek",
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.padding(top = 24.dp)
                )

                OutlinedTextField(
                    value = commentText,

                    onValueChange = {
                        commentText = it
                        errorMessage = ""
                    },

                    label = {
                        Text(
                            "Írj kommentet..."
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),

                    minLines = 2,
                    maxLines = 5
                )

                Button(
                    onClick = {

                        if (
                            commentText.isBlank()
                        ) {

                            errorMessage =
                                "A komment nem lehet üres."

                            return@Button
                        }

                        isCommentSending =
                            true

                        errorMessage =
                            ""

                        postViewModel.addComment(
                            postId = postId,
                            text =
                                commentText.trim(),

                            onSuccess = {

                                commentText = ""
                                isCommentSending =
                                    false

                                loadPost()
                                loadComments()
                            },

                            onError = { error ->

                                isCommentSending =
                                    false

                                errorMessage =
                                    error
                            }
                        )
                    },

                    enabled =
                        !isCommentSending,

                    modifier =
                        Modifier.padding(top = 12.dp)
                ) {

                    if (isCommentSending) {

                        CircularProgressIndicator()

                    } else {

                        Text(
                            "Komment elküldése"
                        )
                    }
                }

                if (
                    errorMessage.isNotEmpty()
                ) {

                    Text(
                        text = errorMessage,
                        modifier =
                            Modifier.padding(top = 12.dp)
                    )
                }

                if (comments.isEmpty()) {

                    Text(
                        text =
                            "Még nincs komment.",
                        modifier =
                            Modifier.padding(top = 20.dp)
                    )

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = comments,
                            key = { comment ->
                                comment.id
                            }
                        ) { comment ->

                            CommentThreadItem(
                                postId = postId,
                                comment = comment,
                                postViewModel =
                                    postViewModel,

                                onCommentChanged = {
                                    loadPost()
                                    loadComments()
                                },

                                onUserProfileClick =
                                    onUserProfileClick,

                                onError = { error ->
                                    errorMessage =
                                        error
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
private fun CommentThreadItem(
    postId: String,
    comment: Comment,
    postViewModel: PostViewModel,
    onCommentChanged: () -> Unit,
    onUserProfileClick: (String) -> Unit,
    onError: (String) -> Unit
) {
    var replies by remember(
        comment.id
    ) {
        mutableStateOf<List<Comment>>(
            emptyList()
        )
    }

    var showReplies by remember(
        comment.id
    ) {
        mutableStateOf(false)
    }

    var showReplyField by remember(
        comment.id
    ) {
        mutableStateOf(false)
    }

    var replyText by remember(
        comment.id
    ) {
        mutableStateOf("")
    }

    var isReplySending by remember(
        comment.id
    ) {
        mutableStateOf(false)
    }

    var isLiked by remember(
        comment.id
    ) {
        mutableStateOf(false)
    }

    var likeCount by remember(
        comment.id
    ) {
        mutableStateOf(
            comment.likeCount
        )
    }

    var isLikeLoading by remember(
        comment.id
    ) {
        mutableStateOf(false)
    }

    fun loadReplies() {

        postViewModel.getCommentReplies(
            postId = postId,
            parentCommentId =
                comment.id,

            onSuccess = { loadedReplies ->
                replies =
                    loadedReplies
            },

            onError = onError
        )
    }

    LaunchedEffect(
        comment.id
    ) {

        postViewModel.isCommentLiked(
            postId = postId,
            commentId = comment.id,

            onSuccess = { liked ->
                isLiked = liked
            },

            onError = onError
        )

        if (
            comment.replyCount > 0
        ) {
            loadReplies()
        }
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            Text(
                text =
                    comment.userName,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.clickable {

                        if (
                            comment.userId
                                .isNotBlank()
                        ) {

                            onUserProfileClick(
                                comment.userId
                            )
                        }
                    }
            )

            Text(
                text =
                    formatPostDetailsDate(
                        comment.createdAt
                    ),
                modifier =
                    Modifier.padding(top = 4.dp)
            )

            Text(
                text =
                    comment.text,
                modifier =
                    Modifier.padding(top = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
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

                        isLikeLoading =
                            true

                        postViewModel
                            .toggleCommentLike(
                                postId =
                                    postId,
                                commentId =
                                    comment.id,

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

                                    isLikeLoading =
                                        false

                                    onError(
                                        error
                                    )
                                }
                            )
                    },

                    enabled =
                        !isLikeLoading
                ) {

                    if (isLiked) {

                        Text(
                            "❤️ $likeCount"
                        )

                    } else {

                        Text(
                            "♡ $likeCount"
                        )
                    }
                }

                TextButton(
                    onClick = {

                        showReplyField =
                            !showReplyField
                    }
                ) {
                    Text("Válasz")
                }

                if (
                    comment.replyCount > 0
                ) {

                    TextButton(
                        onClick = {

                            showReplies =
                                !showReplies

                            if (
                                showReplies
                            ) {
                                loadReplies()
                            }
                        }
                    ) {

                        Text(
                            if (showReplies) {
                                "Válaszok elrejtése"
                            } else {
                                "Válaszok (${comment.replyCount})"
                            }
                        )
                    }
                }
            }

            if (showReplyField) {

                OutlinedTextField(
                    value =
                        replyText,

                    onValueChange = {
                        replyText = it
                    },

                    label = {

                        Text(
                            "Válasz ${comment.userName} kommentjére"
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),

                    minLines = 2,
                    maxLines = 4
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    Button(
                        onClick = {

                            if (
                                replyText.isBlank()
                            ) {

                                onError(
                                    "A válasz nem lehet üres."
                                )

                                return@Button
                            }

                            isReplySending =
                                true

                            postViewModel
                                .addCommentReply(
                                    postId =
                                        postId,

                                    parentCommentId =
                                        comment.id,

                                    text =
                                        replyText.trim(),

                                    onSuccess = {

                                        replyText =
                                            ""

                                        isReplySending =
                                            false

                                        showReplyField =
                                            false

                                        showReplies =
                                            true

                                        loadReplies()

                                        onCommentChanged()
                                    },

                                    onError = { error ->

                                        isReplySending =
                                            false

                                        onError(
                                            error
                                        )
                                    }
                                )
                        },

                        enabled =
                            !isReplySending
                    ) {

                        if (
                            isReplySending
                        ) {

                            CircularProgressIndicator()

                        } else {

                            Text(
                                "Válasz elküldése"
                            )
                        }
                    }

                    Button(
                        onClick = {

                            replyText =
                                ""

                            showReplyField =
                                false
                        },

                        enabled =
                            !isReplySending
                    ) {

                        Text("Mégse")
                    }
                }
            }

            if (
                showReplies &&
                replies.isNotEmpty()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            top = 12.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    replies.forEach { reply ->

                        CommentReplyItem(
                            postId =
                                postId,

                            reply =
                                reply,

                            postViewModel =
                                postViewModel,

                            onUserProfileClick =
                                onUserProfileClick,

                            onError =
                                onError
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentReplyItem(
    postId: String,
    reply: Comment,
    postViewModel: PostViewModel,
    onUserProfileClick: (String) -> Unit,
    onError: (String) -> Unit
) {
    var isLiked by remember(
        reply.id
    ) {
        mutableStateOf(false)
    }

    var likeCount by remember(
        reply.id
    ) {
        mutableStateOf(
            reply.likeCount
        )
    }

    var isLikeLoading by remember(
        reply.id
    ) {
        mutableStateOf(false)
    }

    LaunchedEffect(
        reply.id
    ) {

        postViewModel.isCommentLiked(
            postId = postId,
            commentId = reply.id,

            onSuccess = { liked ->
                isLiked = liked
            },

            onError = onError
        )
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Text(
                text =
                    reply.userName,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.clickable {

                        if (
                            reply.userId
                                .isNotBlank()
                        ) {

                            onUserProfileClick(
                                reply.userId
                            )
                        }
                    }
            )

            Text(
                text =
                    formatPostDetailsDate(
                        reply.createdAt
                    ),
                modifier =
                    Modifier.padding(top = 4.dp)
            )

            Text(
                text =
                    reply.text,
                modifier =
                    Modifier.padding(top = 8.dp)
            )

            TextButton(
                onClick = {

                    if (
                        isLikeLoading
                    ) {
                        return@TextButton
                    }

                    isLikeLoading =
                        true

                    postViewModel
                        .toggleCommentLike(
                            postId =
                                postId,

                            commentId =
                                reply.id,

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

                                isLikeLoading =
                                    false

                                onError(
                                    error
                                )
                            }
                        )
                },

                enabled =
                    !isLikeLoading,

                modifier =
                    Modifier.padding(top = 4.dp)
            ) {

                if (isLiked) {

                    Text(
                        "❤️ $likeCount"
                    )

                } else {

                    Text(
                        "♡ $likeCount"
                    )
                }
            }
        }
    }
}

private fun buildPostDetailsMentionSummary(
    roleMentions: List<String>
): String {

    val labels =
        buildList {

            if (
                "CUSTOMERS" in roleMentions
            ) {
                add("Ügyfelek")
            }

            if (
                "CONTRACTORS" in roleMentions
            ) {
                add("WasteUberesek")
            }

            if (
                SubscriptionPlans.MENTION_WASTE_GO
                in roleMentions
            ) {
                add("WasteGo")
            }

            if (
                SubscriptionPlans.MENTION_WASTE_PLUS
                in roleMentions
            ) {
                add("WastePlus")
            }

            if (
                SubscriptionPlans.MENTION_WASTE_MAX
                in roleMentions
            ) {
                add("WasteMax")
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
private fun PostDetailsMentionText(
    text: String,
    modifier: Modifier = Modifier
) {
    val mentionColor =
        MaterialTheme.colorScheme.primary

    val annotatedText =
        remember(
            text,
            mentionColor
        ) {

            buildPostDetailsMentionText(
                text = text,
                mentionColor =
                    mentionColor
            )
        }

    Text(
        text = annotatedText,
        modifier = modifier
    )
}

private fun buildPostDetailsMentionText(
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
                    style = SpanStyle(
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
private fun PostDetailsMedia(
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
                        min = 200.dp,
                        max = 650.dp
                    )
                    .padding(top = 16.dp),

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
                        min = 200.dp,
                        max = 650.dp
                    )
                    .padding(top = 16.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        "VIDEO" -> {

            PostDetailsVideoPlayer(
                videoUrl =
                    post.mediaUrl
            )
        }
    }
}

@Composable
private fun PostDetailsVideoPlayer(
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
            .padding(top = 16.dp)
    )
}

private fun formatPostDetailsDate(
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