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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPostsScreen(
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

    var deleteReason by remember {
        mutableStateOf("")
    }

    var deleteErrorMessage by remember {
        mutableStateOf("")
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

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

    LaunchedEffect(Unit) {
        loadPosts()
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
                modifier =
                    Modifier.fillMaxWidth(),

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

                Text(
                    "Posztok moderálása"
                )
            }

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.padding(
                                top = 32.dp
                            )
                    )
                }

                errorMessage.isNotEmpty() -> {

                    Column(
                        modifier =
                            Modifier.padding(
                                top = 24.dp
                            )
                    ) {

                        Text(
                            text =
                                errorMessage,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )

                        Button(
                            onClick = {
                                loadPosts()
                            },

                            modifier =
                                Modifier.padding(
                                    top = 12.dp
                                )
                        ) {

                            Text(
                                "Újrapróbálás"
                            )
                        }
                    }
                }

                posts.isEmpty() -> {

                    Text(
                        text =
                            "Nincs moderálható poszt.",

                        modifier =
                            Modifier.padding(
                                top = 32.dp
                            )
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
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

                                    Text(
                                        text =
                                            post.userName
                                    )

                                    Text(
                                        text =
                                            formatAdminPostDate(
                                                post.createdAt
                                            ),

                                        modifier =
                                            Modifier.padding(
                                                top = 4.dp
                                            )
                                    )

                                    if (
                                        post.text.isNotBlank()
                                    ) {

                                        Text(
                                            text =
                                                post.text,

                                            modifier =
                                                Modifier.padding(
                                                    top = 12.dp
                                                )
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
                                                        "📷 Kép"

                                                    "GIF" ->
                                                        "GIF"

                                                    "VIDEO" ->
                                                        "🎬 Videó"

                                                    else ->
                                                        "Média"
                                                },

                                            modifier =
                                                Modifier.padding(
                                                    top = 8.dp
                                                )
                                        )
                                    }

                                    Text(
                                        text =
                                            "❤️ ${post.likeCount}   💬 ${post.commentCount}",

                                        modifier =
                                            Modifier.padding(
                                                top = 12.dp
                                            )
                                    )

                                    Button(
                                        onClick = {

                                            selectedPost =
                                                post

                                            deleteReason =
                                                ""

                                            deleteErrorMessage =
                                                ""

                                            errorMessage =
                                                ""
                                        },

                                        modifier =
                                            Modifier.padding(
                                                top = 16.dp
                                            )
                                    ) {

                                        Text(
                                            "Poszt törlése"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedPost?.let { post ->

        AlertDialog(
            onDismissRequest = {

                if (
                    !isDeleting
                ) {

                    selectedPost =
                        null

                    deleteReason =
                        ""

                    deleteErrorMessage =
                        ""
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
                        "Add meg, hogy miért törlöd a posztot."
                    )

                    OutlinedTextField(
                        value =
                            deleteReason,

                        onValueChange = {
                            deleteReason = it
                            deleteErrorMessage = ""
                        },

                        label = {
                            Text(
                                "Törlés oka"
                            )
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 12.dp
                            ),

                        minLines = 2,
                        maxLines = 5,

                        enabled =
                            !isDeleting
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

                        if (
                            deleteReason.isBlank()
                        ) {

                            deleteErrorMessage =
                                "A törlés okát kötelező megadni."

                            return@Button
                        }

                        isDeleting =
                            true

                        deleteErrorMessage =
                            ""

                        errorMessage =
                            ""

                        postViewModel.adminDeletePost(
                            postId =
                                post.id,

                            reason =
                                deleteReason.trim(),

                            onSuccess = {

                                /*
                                 * Azonnal eltávolítjuk
                                 * a helyi listából.
                                 */
                                posts =
                                    posts.filterNot {
                                        it.id ==
                                                post.id
                                    }

                                isDeleting =
                                    false

                                selectedPost =
                                    null

                                deleteReason =
                                    ""

                                deleteErrorMessage =
                                    ""

                                /*
                                 * Utána Firestore-ból
                                 * is frissítünk.
                                 */
                                loadPosts()
                            },

                            onError = { error ->

                                isDeleting =
                                    false

                                /*
                                 * FONTOS:
                                 *
                                 * A hibát most közvetlenül
                                 * a törlési ablakban mutatjuk.
                                 */
                                deleteErrorMessage =
                                    error
                            }
                        )
                    },

                    enabled =
                        !isDeleting &&
                                deleteReason
                                    .isNotBlank()
                ) {

                    if (
                        isDeleting
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

                Button(
                    onClick = {

                        selectedPost =
                            null

                        deleteReason =
                            ""

                        deleteErrorMessage =
                            ""
                    },

                    enabled =
                        !isDeleting
                ) {

                    Text(
                        "Mégse"
                    )
                }
            }
        )
    }
}

private fun formatAdminPostDate(
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
        Date(timestamp)
    )
}