package hu.wasteuber.app.ui.screens.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.viewmodel.AuthViewModel
import hu.wasteuber.app.viewmodel.PostMediaViewModel
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditPostScreen(
    postId: String,
    postViewModel: PostViewModel,
    postMediaViewModel: PostMediaViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val authViewModel: AuthViewModel =
        viewModel()

    var post by remember {
        mutableStateOf<Post?>(null)
    }

    var currentUser by remember {
        mutableStateOf<User?>(null)
    }

    var text by remember {
        mutableStateOf("")
    }

    var currentMediaUrl by remember {
        mutableStateOf("")
    }

    var currentMediaType by remember {
        mutableStateOf("NONE")
    }

    var selectedMediaUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedMediaType by remember {
        mutableStateOf("NONE")
    }

    var removeCurrentMedia by remember {
        mutableStateOf(false)
    }

    var isLoadingPost by remember {
        mutableStateOf(true)
    }

    var isLoadingUser by remember {
        mutableStateOf(true)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var statusMessage by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val isLoading =
        isLoadingPost ||
                isLoadingUser

    val imageLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                selectedMediaUri = uri
                selectedMediaType = "IMAGE"
                removeCurrentMedia = false
                errorMessage = ""
            }
        }

    val gifLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                selectedMediaUri = uri
                selectedMediaType = "GIF"
                removeCurrentMedia = false
                errorMessage = ""
            }
        }

    val videoLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                selectedMediaUri = uri
                selectedMediaType = "VIDEO"
                removeCurrentMedia = false
                errorMessage = ""
            }
        }

    LaunchedEffect(postId) {

        postViewModel.getPost(
            postId = postId,

            onSuccess = { loadedPost ->

                post = loadedPost
                text = loadedPost.text

                currentMediaUrl =
                    loadedPost.mediaUrl

                currentMediaType =
                    loadedPost.mediaType

                isLoadingPost = false
            },

            onError = { error ->

                errorMessage = error
                isLoadingPost = false
            }
        )

        authViewModel.getCurrentUser(

            onSuccess = { user ->

                currentUser = user
                isLoadingUser = false
            },

            onError = { error ->

                errorMessage = error
                isLoadingUser = false
            }
        )
    }

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
                                "Poszt szerkesztése",

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
                        !isSaving
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

                return@Column
            }

            val currentPost =
                post

            if (currentPost == null) {

                Text(
                    text =
                        errorMessage.ifBlank {
                            "A poszt nem található."
                        },

                    modifier =
                        Modifier.padding(top = 20.dp)
                )

                return@Column
            }

            OutlinedTextField(
                value = text,

                onValueChange = {
                    text = it
                    errorMessage = ""
                },

                label = {
                    Text("Poszt szövege")
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),

                minLines = 5,
                maxLines = 12
            )

            currentUser?.let { user ->

                EditPostMentionSection(
                    user = user,
                    originalText =
                        currentPost.text,
                    text = text,

                    onTextChange = { newText ->
                        text = newText
                        errorMessage = ""
                    },

                    enabled = !isSaving
                )
            }

            if (
                currentMediaUrl.isNotBlank() &&
                !removeCurrentMedia &&
                selectedMediaUri == null
            ) {

                Text(
                    text = "Jelenlegi média",

                    modifier =
                        Modifier.padding(top = 20.dp)
                )

                EditPostExistingMedia(
                    mediaUrl =
                        currentMediaUrl,

                    mediaType =
                        currentMediaType
                )

                Button(
                    onClick = {

                        removeCurrentMedia = true

                        selectedMediaUri = null
                        selectedMediaType = "NONE"
                    },

                    enabled = !isSaving,

                    modifier =
                        Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        "Jelenlegi média eltávolítása"
                    )
                }
            }

            if (removeCurrentMedia) {

                Text(
                    text =
                        "A jelenlegi média eltávolításra kerül.",

                    modifier =
                        Modifier.padding(top = 16.dp)
                )

                Button(
                    onClick = {
                        removeCurrentMedia = false
                    },

                    enabled = !isSaving,

                    modifier =
                        Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        "Mégse, média megtartása"
                    )
                }
            }

            selectedMediaUri?.let {

                Text(
                    text =
                        when (
                            selectedMediaType
                        ) {

                            "IMAGE" ->
                                "📷 Új kép kiválasztva"

                            "GIF" ->
                                "Új GIF kiválasztva"

                            "VIDEO" ->
                                "🎬 Új videó kiválasztva (max. 500 MB)"

                            else ->
                                "Új média kiválasztva"
                        },

                    modifier =
                        Modifier.padding(top = 16.dp)
                )

                Button(
                    onClick = {

                        selectedMediaUri = null
                        selectedMediaType =
                            "NONE"
                    },

                    enabled = !isSaving,

                    modifier =
                        Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        "Új média kiválasztásának törlése"
                    )
                }
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(top = 20.dp)
            )

            Text(
                text = "Média cseréje",

                modifier =
                    Modifier.padding(top = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        imageLauncher.launch(
                            "image/*"
                        )
                    },

                    enabled = !isSaving
                ) {
                    Text("Kép")
                }

                Button(
                    onClick = {
                        gifLauncher.launch(
                            "image/gif"
                        )
                    },

                    enabled = !isSaving
                ) {
                    Text("GIF")
                }

                Button(
                    onClick = {
                        videoLauncher.launch(
                            "video/*"
                        )
                    },

                    enabled = !isSaving
                ) {
                    Text("Videó")
                }
            }

            if (
                statusMessage.isNotEmpty()
            ) {

                Text(
                    text =
                        statusMessage,

                    modifier =
                        Modifier.padding(top = 12.dp)
                )
            }

            if (
                errorMessage.isNotEmpty()
            ) {

                Text(
                    text =
                        errorMessage,

                    modifier =
                        Modifier.padding(top = 12.dp)
                )
            }

            Button(
                onClick = {

                    val newMediaUri =
                        selectedMediaUri

                    if (
                        text.isBlank() &&
                        newMediaUri == null &&
                        (
                                removeCurrentMedia ||
                                        currentMediaUrl.isBlank()
                                )
                    ) {

                        errorMessage =
                            "A poszt nem lehet teljesen üres."

                        return@Button
                    }

                    isSaving = true
                    errorMessage = ""

                    if (
                        newMediaUri != null
                    ) {

                        statusMessage =
                            "Új média feltöltése..."

                        postMediaViewModel.uploadMedia(
                            fileUri =
                                newMediaUri,

                            mediaType =
                                selectedMediaType,

                            onSuccess = { downloadUrl ->

                                statusMessage =
                                    "Módosítások mentése..."

                                saveEditedPost(
                                    postId =
                                        postId,

                                    text =
                                        text,

                                    mediaUrl =
                                        downloadUrl,

                                    mediaType =
                                        selectedMediaType,

                                    postViewModel =
                                        postViewModel,

                                    onSuccess = {

                                        isSaving = false
                                        statusMessage = ""

                                        onSaveSuccess()
                                    },

                                    onError = { error ->

                                        isSaving = false
                                        statusMessage = ""
                                        errorMessage = error
                                    }
                                )
                            },

                            onError = { error ->

                                isSaving = false
                                statusMessage = ""
                                errorMessage = error
                            }
                        )

                    } else {

                        val finalMediaUrl =
                            if (
                                removeCurrentMedia
                            ) {
                                ""
                            } else {
                                currentMediaUrl
                            }

                        val finalMediaType =
                            if (
                                removeCurrentMedia
                            ) {
                                "NONE"
                            } else {
                                currentMediaType
                            }

                        statusMessage =
                            "Módosítások mentése..."

                        saveEditedPost(
                            postId =
                                postId,

                            text =
                                text,

                            mediaUrl =
                                finalMediaUrl,

                            mediaType =
                                finalMediaType,

                            postViewModel =
                                postViewModel,

                            onSuccess = {

                                isSaving = false
                                statusMessage = ""

                                onSaveSuccess()
                            },

                            onError = { error ->

                                isSaving = false
                                statusMessage = ""
                                errorMessage = error
                            }
                        )
                    }
                },

                enabled = !isSaving,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp,
                        bottom = 24.dp
                    )
            ) {

                if (isSaving) {

                    CircularProgressIndicator()

                } else {

                    Text(
                        "Módosítások mentése"
                    )
                }
            }
        }
    }
}

@Composable
private fun EditPostMentionSection(
    user: User,
    originalText: String,
    text: String,
    onTextChange: (String) -> Unit,
    enabled: Boolean
) {
    val canUseRoleMentions =
        user.role == "ADMIN" ||
                user.role == "CONTRACTOR"

    val canUseSubscriptionMentions =
        user.role == "ADMIN" ||
                user.role == "CONTRACTOR" ||
                user.isUberPlus

    if (
        !canUseRoleMentions &&
        !canUseSubscriptionMentions
    ) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        if (canUseRoleMentions) {

            EditPostRoleMentionSection(
                user = user,
                originalText =
                    originalText,
                text = text,
                onTextChange =
                    onTextChange,
                enabled = enabled
            )
        }

        if (canUseSubscriptionMentions) {

            EditPostSubscriptionMentionSection(
                user = user,
                originalText =
                    originalText,
                text = text,
                onTextChange =
                    onTextChange,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun EditPostRoleMentionSection(
    user: User,
    originalText: String,
    text: String,
    onTextChange: (String) -> Unit,
    enabled: Boolean
) {
    Text(
        text = "Role megjelölés"
    )

    when (user.role) {

        "ADMIN" -> {

            Text(
                text =
                    "Adminként a csoportos megjelölések korlátlanul használhatók.",

                modifier =
                    Modifier.padding(top = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {

                        onTextChange(
                            addEditMentionToText(
                                text = text,
                                mention =
                                    "@ügyfelek"
                            )
                        )
                    },

                    enabled =
                        enabled &&
                                !containsEditMention(
                                    text = text,
                                    mention =
                                        "@ügyfelek"
                                )
                ) {
                    Text("@ügyfelek")
                }

                Button(
                    onClick = {

                        onTextChange(
                            addEditMentionToText(
                                text = text,
                                mention =
                                    "@wasteuberesek"
                            )
                        )
                    },

                    enabled =
                        enabled &&
                                !containsEditMention(
                                    text = text,
                                    mention =
                                        "@wasteuberesek"
                                )
                ) {
                    Text("@wasteuberesek")
                }
            }
        }

        "CONTRACTOR" -> {

            val today =
                getEditPostTodayDate()

            val usedToday =
                if (
                    user.contractorCustomerMentionDate ==
                    today
                ) {
                    user.contractorCustomerMentionCount
                } else {
                    0
                }

            val remaining =
                (
                        2 - usedToday
                        ).coerceAtLeast(0)

            val existedOriginally =
                containsEditMention(
                    text =
                        originalText,

                    mention =
                        "@ügyfelek"
                )

            Text(
                text =
                    "Ma még $remaining/2 új @ügyfelek megjelölést használhatsz.",

                modifier =
                    Modifier.padding(top = 6.dp)
            )

            Button(
                onClick = {

                    onTextChange(
                        addEditMentionToText(
                            text = text,
                            mention =
                                "@ügyfelek"
                        )
                    )
                },

                enabled =
                    enabled &&
                            !containsEditMention(
                                text = text,
                                mention =
                                    "@ügyfelek"
                            ) &&
                            (
                                    remaining > 0 ||
                                            existedOriginally
                                    ),

                modifier =
                    Modifier.padding(top = 10.dp)
            ) {
                Text("@ügyfelek")
            }

            if (
                remaining == 0 &&
                !existedOriginally
            ) {

                Text(
                    text =
                        "A mai @ügyfelek limited elfogyott.",

                    modifier =
                        Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EditPostSubscriptionMentionSection(
    user: User,
    originalText: String,
    text: String,
    onTextChange: (String) -> Unit,
    enabled: Boolean
) {
    HorizontalDivider(
        modifier =
            Modifier.padding(top = 16.dp)
    )

    Text(
        text =
            "Előfizetési csomag megjelölés",

        modifier =
            Modifier.padding(top = 16.dp)
    )

    if (
        user.role == "ADMIN"
    ) {

        Text(
            text =
                "Adminként a csomagokat korlátozás nélkül megjelölheted.",

            modifier =
                Modifier.padding(top = 6.dp)
        )

    } else {

        val today =
            getEditPostTodayDate()

        val usedToday =
            if (
                user.subscriptionMentionDate ==
                today
            ) {
                user.subscriptionMentionCount
            } else {
                0
            }

        val newMentions =
            countNewSubscriptionMentions(
                originalText =
                    originalText,

                currentText =
                    text
            )

        val remaining =
            (
                    2 - usedToday
                    ).coerceAtLeast(0)

        val remainingAfterCurrentEdit =
            (
                    remaining -
                            newMentions
                    ).coerceAtLeast(0)

        Text(
            text =
                "Ma még $remaining/2 új csomagmegjelölést használhatsz.",

            modifier =
                Modifier.padding(top = 6.dp)
        )

        if (
            newMentions > 0
        ) {

            Text(
                text =
                    "Ebben a szerkesztésben $newMentions új csomagmegjelölést adtál hozzá.",

                modifier =
                    Modifier.padding(top = 6.dp)
            )
        }

        if (
            remainingAfterCurrentEdit == 0 &&
            newMentions > 0
        ) {

            Text(
                text =
                    "Ehhez a szerkesztéshez már nem adhatsz további új csomagmegjelölést.",

                modifier =
                    Modifier.padding(top = 6.dp)
            )
        }
    }

    EditPostSubscriptionMentionButton(
        mention = "@wastego",
        user = user,
        originalText =
            originalText,
        text = text,
        enabled = enabled,
        onTextChange =
            onTextChange
    )

    EditPostSubscriptionMentionButton(
        mention = "@wasteplus",
        user = user,
        originalText =
            originalText,
        text = text,
        enabled = enabled,
        onTextChange =
            onTextChange
    )

    EditPostSubscriptionMentionButton(
        mention = "@wastemax",
        user = user,
        originalText =
            originalText,
        text = text,
        enabled = enabled,
        onTextChange =
            onTextChange
    )
}

@Composable
private fun EditPostSubscriptionMentionButton(
    mention: String,
    user: User,
    originalText: String,
    text: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit
) {
    val isAdmin =
        user.role == "ADMIN"

    val today =
        getEditPostTodayDate()

    val usedToday =
        if (
            user.subscriptionMentionDate ==
            today
        ) {
            user.subscriptionMentionCount
        } else {
            0
        }

    val existedOriginally =
        containsEditMention(
            text =
                originalText,

            mention =
                mention
        )

    val newMentions =
        countNewSubscriptionMentions(
            originalText =
                originalText,

            currentText =
                text
        )

    val canAdd =
        isAdmin ||
                existedOriginally ||
                (
                        usedToday +
                                newMentions
                        ) < 2

    Button(
        onClick = {

            onTextChange(
                addEditMentionToText(
                    text = text,
                    mention =
                        mention
                )
            )
        },

        enabled =
            enabled &&
                    canAdd &&
                    !containsEditMention(
                        text = text,
                        mention = mention
                    ),

        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(mention)
    }
}

private fun countNewSubscriptionMentions(
    originalText: String,
    currentText: String
): Int {
    val mentions =
        listOf(
            "@wastego",
            "@wasteplus",
            "@wastemax"
        )

    return mentions.count { mention ->

        containsEditMention(
            text =
                currentText,

            mention =
                mention
        ) &&
                !containsEditMention(
                    text =
                        originalText,

                    mention =
                        mention
                )
    }
}

private fun addEditMentionToText(
    text: String,
    mention: String
): String {

    if (
        containsEditMention(
            text = text,
            mention = mention
        )
    ) {
        return text
    }

    return if (
        text.isBlank()
    ) {

        "$mention "

    } else {

        "$text $mention "
    }
}

private fun containsEditMention(
    text: String,
    mention: String
): Boolean =
    text.lowercase(
        Locale.ROOT
    ).contains(
        mention.lowercase(
            Locale.ROOT
        )
    )

private fun getEditPostTodayDate(): String =
    SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(
        Date()
    )

private fun saveEditedPost(
    postId: String,
    text: String,
    mediaUrl: String,
    mediaType: String,
    postViewModel: PostViewModel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    postViewModel.editPost(
        postId = postId,
        newText = text.trim(),
        newMediaUrl = mediaUrl,
        newMediaType = mediaType,
        onSuccess = onSuccess,
        onError = onError
    )
}

@Composable
private fun EditPostExistingMedia(
    mediaUrl: String,
    mediaType: String
) {

    when (mediaType) {

        "IMAGE" -> {

            AsyncImage(
                model = mediaUrl,

                contentDescription =
                    "Jelenlegi posztkép",

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 180.dp,
                        max = 500.dp
                    )
                    .padding(top = 12.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        "GIF" -> {

            AsyncImage(
                model = mediaUrl,

                contentDescription =
                    "Jelenlegi GIF",

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 180.dp,
                        max = 500.dp
                    )
                    .padding(top = 12.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        "VIDEO" -> {

            EditPostVideoPlayer(
                videoUrl = mediaUrl
            )
        }
    }
}

@Composable
private fun EditPostVideoPlayer(
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

                    setMediaItem(
                        MediaItem.fromUri(
                            videoUrl
                        )
                    )

                    playWhenReady = false
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
            .aspectRatio(
                16f / 9f
            )
            .padding(top = 12.dp)
    )
}