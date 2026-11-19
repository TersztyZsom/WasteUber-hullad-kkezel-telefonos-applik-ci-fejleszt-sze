package hu.wasteuber.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPendingPostsScreen(
    postViewModel: PostViewModel,
    onBackClick: () -> Unit
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

    var selectedPost by remember {
        mutableStateOf<Post?>(null)
    }

    var rejectionReason by remember {
        mutableStateOf("")
    }

    var isRejectDialogVisible by remember {
        mutableStateOf(false)
    }

    var actionPostId by remember {
        mutableStateOf<String?>(null)
    }

    fun loadPendingPosts() {
        isLoading = true
        errorMessage = ""

        postViewModel.getPendingPosts(
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

    LaunchedEffect(Unit) {
        loadPendingPosts()
    }

    if (
        isRejectDialogVisible &&
        selectedPost != null
    ) {
        AlertDialog(
            onDismissRequest = {
                if (actionPostId == null) {
                    isRejectDialogVisible = false
                    selectedPost = null
                    rejectionReason = ""
                }
            },

            title = {
                Text(
                    text = "Poszt elutasítása"
                )
            },

            text = {
                Column {

                    Text(
                        text =
                            "Add meg, miért utasítod el ${selectedPost!!.userName} posztját."
                    )

                    OutlinedTextField(
                        value = rejectionReason,

                        onValueChange = {
                            rejectionReason = it
                        },

                        label = {
                            Text("Elutasítás oka")
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),

                        minLines = 3,
                        maxLines = 6
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val post =
                            selectedPost
                                ?: return@Button

                        if (
                            rejectionReason
                                .isBlank()
                        ) {
                            errorMessage =
                                "Az elutasítás okát kötelező megadni."

                            return@Button
                        }

                        actionPostId =
                            post.id

                        postViewModel.rejectPendingPost(
                            postId =
                                post.id,

                            reason =
                                rejectionReason.trim(),

                            onSuccess = {

                                posts =
                                    posts.filterNot {
                                        it.id ==
                                                post.id
                                    }

                                actionPostId =
                                    null

                                isRejectDialogVisible =
                                    false

                                selectedPost =
                                    null

                                rejectionReason =
                                    ""
                            },

                            onError = { error ->

                                actionPostId =
                                    null

                                errorMessage =
                                    error
                            }
                        )
                    },

                    enabled =
                        actionPostId == null
                ) {

                    if (
                        actionPostId != null
                    ) {
                        CircularProgressIndicator()
                    } else {
                        Text("Elutasítás")
                    }
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        isRejectDialogVisible = false
                        selectedPost = null
                        rejectionReason = ""
                    },

                    enabled =
                        actionPostId == null
                ) {
                    Text("Mégse")
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
                .widthIn(max = 900.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Button(
                    onClick = onBackClick
                ) {
                    Text("Vissza")
                }

                Button(
                    onClick = {
                        loadPendingPosts()
                    },
                    enabled =
                        !isLoading &&
                                actionPostId == null
                ) {
                    Text("Frissítés")
                }
            }

            Text(
                text =
                    "Jóváhagyásra váró posztok",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.padding(top = 20.dp)
            )

            Text(
                text =
                    "Itt jelennek meg azok a posztok, amelyeket kredit nélkül hoztak létre.",
                modifier =
                    Modifier.padding(top = 8.dp)
            )

            if (
                errorMessage.isNotBlank()
            ) {
                Text(
                    text = errorMessage,
                    modifier =
                        Modifier.padding(top = 16.dp)
                )
            }

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.padding(top = 32.dp)
                    )
                }

                posts.isEmpty() -> {

                    Text(
                        text =
                            "Jelenleg nincs jóváhagyásra váró poszt.",
                        modifier =
                            Modifier.padding(top = 32.dp)
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = posts,
                            key = { post ->
                                post.id
                            }
                        ) { post ->

                            PendingPostCard(
                                post = post,
                                isActionRunning =
                                    actionPostId ==
                                            post.id,

                                onApproveClick = {

                                    if (
                                        actionPostId != null
                                    ) {
                                        return@PendingPostCard
                                    }

                                    actionPostId =
                                        post.id

                                    errorMessage =
                                        ""

                                    postViewModel
                                        .approvePendingPost(
                                            postId =
                                                post.id,

                                            onSuccess = {

                                                posts =
                                                    posts.filterNot {
                                                        it.id ==
                                                                post.id
                                                    }

                                                actionPostId =
                                                    null
                                            },

                                            onError = { error ->

                                                actionPostId =
                                                    null

                                                errorMessage =
                                                    error
                                            }
                                        )
                                },

                                onRejectClick = {

                                    if (
                                        actionPostId != null
                                    ) {
                                        return@PendingPostCard
                                    }

                                    selectedPost =
                                        post

                                    rejectionReason =
                                        ""

                                    isRejectDialogVisible =
                                        true
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
private fun PendingPostCard(
    post: Post,
    isActionRunning: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text =
                    post.userName.ifBlank {
                        "Felhasználó"
                    },
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    formatPendingPostDate(
                        post.createdAt
                    ),
                modifier =
                    Modifier.padding(top = 4.dp)
            )

            Text(
                text =
                    "Státusz: Jóváhagyásra vár",
                color =
                    MaterialTheme.colorScheme.primary,
                fontWeight =
                    FontWeight.Medium,
                modifier =
                    Modifier.padding(top = 8.dp)
            )

            if (
                post.text.isNotBlank()
            ) {
                Text(
                    text = post.text,
                    modifier =
                        Modifier.padding(top = 16.dp)
                )
            }

            if (
                post.mediaUrl.isNotBlank()
            ) {

                Text(
                    text =
                        when (
                            post.mediaType
                        ) {
                            "IMAGE" ->
                                "📷 Kép csatolva"

                            "GIF" ->
                                "GIF csatolva"

                            "VIDEO" ->
                                "🎬 Videó csatolva"

                            else ->
                                "Média csatolva"
                        },
                    modifier =
                        Modifier.padding(top = 12.dp)
                )
            }

            if (
                post.roleMentions.isNotEmpty()
            ) {

                Text(
                    text =
                        "Role mention: ${
                            post.roleMentions.joinToString(
                                separator = ", "
                            )
                        }",
                    modifier =
                        Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick =
                        onApproveClick,

                    enabled =
                        !isActionRunning
                ) {

                    if (isActionRunning) {
                        CircularProgressIndicator()
                    } else {
                        Text("Jóváhagyás")
                    }
                }

                Button(
                    onClick =
                        onRejectClick,

                    enabled =
                        !isActionRunning
                ) {
                    Text("Elutasítás")
                }
            }
        }
    }
}

private fun formatPendingPostDate(
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