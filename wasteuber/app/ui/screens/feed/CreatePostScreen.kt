package hu.wasteuber.app.ui.screens.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.viewmodel.AuthViewModel
import hu.wasteuber.app.viewmodel.PostMediaViewModel
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreatePostScreen(
    postViewModel: PostViewModel,
    postMediaViewModel: PostMediaViewModel,
    authViewModel: AuthViewModel,
    onPostCreated: () -> Unit,
    onBackClick: () -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    var currentUser by remember {
        mutableStateOf<User?>(null)
    }

    var selectedMediaUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedMediaType by remember {
        mutableStateOf("NONE")
    }

    var isLoadingUser by remember {
        mutableStateOf(true)
    }

    var isPosting by remember {
        mutableStateOf(false)
    }

    var uploadStatus by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val imageLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                selectedMediaUri = uri
                selectedMediaType = "IMAGE"
                errorMessage = ""
            }
        }

    val gifLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                selectedMediaUri = uri
                selectedMediaType = "GIF"
                errorMessage = ""
            }
        }

    val videoLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                selectedMediaUri = uri
                selectedMediaType = "VIDEO"
                errorMessage = ""
            }
        }

    LaunchedEffect(Unit) {

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
            .padding(24.dp),
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
                                "Új poszt",
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
                        !isPosting
                ) {
                    Text("Vissza")
                }
            }

            if (isLoadingUser) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.padding(top = 24.dp)
                )

                return@Column
            }

            val user =
                currentUser

            if (user == null) {

                Text(
                    text =
                        "Nem sikerült betölteni a felhasználói adatokat.",
                    modifier =
                        Modifier.padding(top = 20.dp)
                )

                return@Column
            }

            val currentTime =
                System.currentTimeMillis()

            val permanentlyBanned =
                user.postBanUntil ==
                        Long.MAX_VALUE

            val temporarilyBanned =
                user.postBanUntil >
                        currentTime &&
                        !permanentlyBanned

            if (
                permanentlyBanned ||
                temporarilyBanned
            ) {

                Text(
                    text =
                        "Jelenleg nem hozhatsz létre új posztot.",
                    modifier =
                        Modifier.padding(top = 24.dp)
                )

                if (permanentlyBanned) {

                    Text(
                        text =
                            "Posztolási tiltás: Végleges",
                        modifier =
                            Modifier.padding(top = 8.dp)
                    )

                } else {

                    Text(
                        text =
                            "Tiltás vége: ${
                                formatCreatePostDate(
                                    user.postBanUntil
                                )
                            }",
                        modifier =
                            Modifier.padding(top = 8.dp)
                    )
                }

                if (
                    user.postBanReason.isNotBlank()
                ) {

                    Text(
                        text =
                            "Tiltás oka: ${user.postBanReason}",
                        modifier =
                            Modifier.padding(top = 8.dp)
                    )
                }

                if (
                    user.postBannedByName.isNotBlank()
                ) {

                    Text(
                        text =
                            "Kitiltotta: ${user.postBannedByName}",
                        modifier =
                            Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text =
                        "Korábbi posztolási tiltások: ${user.postBanCount}",
                    modifier =
                        Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text("Vissza a főoldalra")
                }

                return@Column
            }

            OutlinedTextField(
                value = text,

                onValueChange = {
                    text = it
                    errorMessage = ""
                },

                label = {
                    Text(
                        "Mit szeretnél megosztani?"
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),

                minLines = 6,
                maxLines = 12
            )

            MentionSection(
                user = user,
                text = text,

                onTextChange = { newText ->
                    text = newText
                    errorMessage = ""
                },

                enabled = !isPosting
            )

            HorizontalDivider(
                modifier =
                    Modifier.padding(top = 20.dp)
            )

            Text(
                text = "Média csatolása",
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
                    enabled = !isPosting
                ) {
                    Text("Kép")
                }

                Button(
                    onClick = {
                        gifLauncher.launch(
                            "image/gif"
                        )
                    },
                    enabled = !isPosting
                ) {
                    Text("GIF")
                }

                Button(
                    onClick = {
                        videoLauncher.launch(
                            "video/*"
                        )
                    },
                    enabled = !isPosting
                ) {
                    Text("Videó")
                }
            }

            selectedMediaUri?.let {

                val mediaName =
                    when (
                        selectedMediaType
                    ) {

                        "IMAGE" ->
                            "📷 Kép kiválasztva"

                        "GIF" ->
                            "GIF kiválasztva"

                        "VIDEO" ->
                            "🎬 Videó kiválasztva (max. 500 MB)"

                        else ->
                            "Média kiválasztva"
                    }

                Text(
                    text = mediaName,
                    modifier =
                        Modifier.padding(top = 16.dp)
                )

                Button(
                    onClick = {

                        selectedMediaUri = null
                        selectedMediaType =
                            "NONE"

                        uploadStatus = ""
                    },

                    enabled = !isPosting,

                    modifier =
                        Modifier.padding(top = 8.dp)
                ) {
                    Text("Média eltávolítása")
                }
            }

            if (
                uploadStatus.isNotEmpty()
            ) {

                Text(
                    text = uploadStatus,
                    modifier =
                        Modifier.padding(top = 12.dp)
                )
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

            Button(
                onClick = {

                    if (
                        text.isBlank() &&
                        selectedMediaUri == null
                    ) {

                        errorMessage =
                            "Írj valamit vagy csatolj médiát."

                        return@Button
                    }

                    isPosting = true
                    errorMessage = ""

                    val mediaUri =
                        selectedMediaUri

                    if (
                        mediaUri == null ||
                        selectedMediaType ==
                        "NONE"
                    ) {

                        postViewModel.createPost(
                            text = text.trim(),
                            mediaUrl = "",
                            mediaType = "NONE",

                            onSuccess = {
                                isPosting = false
                                onPostCreated()
                            },

                            onError = { error ->
                                isPosting = false
                                errorMessage = error
                            }
                        )

                    } else {

                        uploadStatus =
                            "Média feltöltése..."

                        postMediaViewModel.uploadMedia(
                            fileUri = mediaUri,
                            mediaType =
                                selectedMediaType,

                            onSuccess = { downloadUrl ->

                                uploadStatus =
                                    "Poszt létrehozása..."

                                postViewModel.createPost(
                                    text =
                                        text.trim(),

                                    mediaUrl =
                                        downloadUrl,

                                    mediaType =
                                        selectedMediaType,

                                    onSuccess = {

                                        isPosting = false
                                        uploadStatus = ""

                                        onPostCreated()
                                    },

                                    onError = { error ->

                                        isPosting = false
                                        uploadStatus = ""
                                        errorMessage = error
                                    }
                                )
                            },

                            onError = { error ->

                                isPosting = false
                                uploadStatus = ""
                                errorMessage = error
                            }
                        )
                    }
                },

                enabled = !isPosting,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {

                if (isPosting) {

                    CircularProgressIndicator()

                } else {

                    Text(
                        "Poszt közzététele"
                    )
                }
            }

            Button(
                onClick = onBackClick,
                enabled = !isPosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp,
                        bottom = 24.dp
                    )
            ) {
                Text("Mégse")
            }
        }
    }
}

@Composable
private fun MentionSection(
    user: User,
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

            RoleMentionSection(
                user = user,
                text = text,
                onTextChange = onTextChange,
                enabled = enabled
            )
        }

        if (canUseSubscriptionMentions) {

            SubscriptionMentionSection(
                user = user,
                text = text,
                onTextChange = onTextChange,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun RoleMentionSection(
    user: User,
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
                    "Adminként csoportosan is megjelölhetsz felhasználókat.",
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
                            addMentionToText(
                                text = text,
                                mention = "@ügyfelek"
                            )
                        )
                    },

                    enabled =
                        enabled &&
                                !containsMention(
                                    text = text,
                                    mention = "@ügyfelek"
                                )
                ) {
                    Text("@ügyfelek")
                }

                Button(
                    onClick = {

                        onTextChange(
                            addMentionToText(
                                text = text,
                                mention =
                                    "@wasteuberesek"
                            )
                        )
                    },

                    enabled =
                        enabled &&
                                !containsMention(
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
                getCreatePostTodayDate()

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

            Text(
                text =
                    "Ma még $remaining/2 alkalommal használhatod az @ügyfelek megjelölést.",
                modifier =
                    Modifier.padding(top = 6.dp)
            )

            Button(
                onClick = {

                    onTextChange(
                        addMentionToText(
                            text = text,
                            mention = "@ügyfelek"
                        )
                    )
                },

                enabled =
                    enabled &&
                            remaining > 0 &&
                            !containsMention(
                                text = text,
                                mention = "@ügyfelek"
                            ),

                modifier =
                    Modifier.padding(top = 10.dp)
            ) {
                Text("@ügyfelek")
            }

            if (remaining == 0) {

                Text(
                    text =
                        "A mai @ügyfelek megjelölési limited elfogyott.",
                    modifier =
                        Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionMentionSection(
    user: User,
    text: String,
    onTextChange: (String) -> Unit,
    enabled: Boolean
) {
    HorizontalDivider(
        modifier =
            Modifier.padding(top = 16.dp)
    )

    Text(
        text = "Előfizetési csomag megjelölés",
        modifier =
            Modifier.padding(top = 16.dp)
    )

    if (user.role == "ADMIN") {

        Text(
            text =
                "Adminként a csomagokat korlátozás nélkül megjelölheted.",
            modifier =
                Modifier.padding(top = 6.dp)
        )

    } else {

        val today =
            getCreatePostTodayDate()

        val usedToday =
            if (
                user.subscriptionMentionDate ==
                today
            ) {
                user.subscriptionMentionCount
            } else {
                0
            }

        val mentionsAlreadyInText =
            countSubscriptionMentions(
                text = text
            )

        val remainingStored =
            (
                    2 - usedToday
                    ).coerceAtLeast(0)

        val remainingWhileEditing =
            (
                    remainingStored -
                            mentionsAlreadyInText
                    ).coerceAtLeast(0)

        Text(
            text =
                "Ma még $remainingStored/2 csomagmegjelölést használhatsz.",
            modifier =
                Modifier.padding(top = 6.dp)
        )

        if (mentionsAlreadyInText > 0) {

            Text(
                text =
                    "Ebben a posztban jelenleg $mentionsAlreadyInText csomagmegjelölés van kiválasztva.",
                modifier =
                    Modifier.padding(top = 6.dp)
            )
        }

        if (remainingWhileEditing == 0) {

            Text(
                text =
                    "Ehhez a poszthoz már nem adhatsz további csomagmegjelölést.",
                modifier =
                    Modifier.padding(top = 6.dp)
            )
        }
    }

    SubscriptionMentionButton(
        mention = "@wastego",
        user = user,
        text = text,
        enabled = enabled,
        onTextChange = onTextChange
    )

    SubscriptionMentionButton(
        mention = "@wasteplus",
        user = user,
        text = text,
        enabled = enabled,
        onTextChange = onTextChange
    )

    SubscriptionMentionButton(
        mention = "@wastemax",
        user = user,
        text = text,
        enabled = enabled,
        onTextChange = onTextChange
    )
}

@Composable
private fun SubscriptionMentionButton(
    mention: String,
    user: User,
    text: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit
) {
    val isAdmin =
        user.role == "ADMIN"

    val today =
        getCreatePostTodayDate()

    val usedToday =
        if (
            user.subscriptionMentionDate ==
            today
        ) {
            user.subscriptionMentionCount
        } else {
            0
        }

    val mentionsAlreadyInText =
        countSubscriptionMentions(
            text = text
        )

    val canAddAnotherMention =
        isAdmin ||
                (
                        usedToday +
                                mentionsAlreadyInText
                        ) < 2

    Button(
        onClick = {

            onTextChange(
                addMentionToText(
                    text = text,
                    mention = mention
                )
            )
        },

        enabled =
            enabled &&
                    canAddAnotherMention &&
                    !containsMention(
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

private fun countSubscriptionMentions(
    text: String
): Int {
    var count = 0

    if (
        containsMention(
            text = text,
            mention = "@wastego"
        )
    ) {
        count++
    }

    if (
        containsMention(
            text = text,
            mention = "@wasteplus"
        )
    ) {
        count++
    }

    if (
        containsMention(
            text = text,
            mention = "@wastemax"
        )
    ) {
        count++
    }

    return count
}

private fun addMentionToText(
    text: String,
    mention: String
): String {

    if (
        containsMention(
            text = text,
            mention = mention
        )
    ) {
        return text
    }

    return if (text.isBlank()) {

        "$mention "

    } else {

        "$text $mention "
    }
}

private fun containsMention(
    text: String,
    mention: String
): Boolean {

    return text
        .lowercase(
            Locale.ROOT
        )
        .contains(
            mention.lowercase(
                Locale.ROOT
            )
        )
}

private fun getCreatePostTodayDate(): String {

    val formatter =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

    return formatter.format(
        Date()
    )
}

private fun formatCreatePostDate(
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