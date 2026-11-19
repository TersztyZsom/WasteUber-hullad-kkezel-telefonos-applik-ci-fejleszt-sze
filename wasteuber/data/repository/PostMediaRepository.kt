package hu.wasteuber.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PostMediaRepository(
    private val context: Context
) {

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()

    private val storage: FirebaseStorage =
        FirebaseStorage.getInstance()

    companion object {
        private const val MAX_VIDEO_SIZE_BYTES =
            500L * 1024L * 1024L
    }

    fun uploadMedia(
        fileUri: Uri,
        mediaType: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            auth.currentUser?.uid

        if (userId == null) {
            onError(
                "Nincs bejelentkezett felhasználó."
            )
            return
        }

        if (mediaType == "VIDEO") {

            val fileSize =
                getFileSize(fileUri)

            if (fileSize == null) {
                onError(
                    "Nem sikerült meghatározni a videó méretét."
                )
                return
            }

            if (fileSize > MAX_VIDEO_SIZE_BYTES) {
                onError(
                    "A videó maximum 500 MB lehet."
                )
                return
            }
        }

        val extension =
            getFileExtension(
                fileUri = fileUri,
                mediaType = mediaType
            )

        if (extension == null) {
            onError(
                "Nem támogatott médiaformátum."
            )
            return
        }

        val fileName =
            "${UUID.randomUUID()}.$extension"

        val storageReference =
            storage.reference
                .child("post_media")
                .child(userId)
                .child(fileName)

        storageReference
            .putFile(fileUri)
            .addOnSuccessListener {

                storageReference
                    .downloadUrl
                    .addOnSuccessListener { downloadUri ->

                        onSuccess(
                            downloadUri.toString()
                        )
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            exception.message
                                ?: "Nem sikerült lekérni a média URL-jét."
                        )
                    }
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült feltölteni a médiát."
                )
            }
    }

    private fun getFileSize(
        uri: Uri
    ): Long? {

        val cursor =
            context.contentResolver.query(
                uri,
                arrayOf(
                    OpenableColumns.SIZE
                ),
                null,
                null,
                null
            )

        cursor?.use {

            val sizeIndex =
                it.getColumnIndex(
                    OpenableColumns.SIZE
                )

            if (
                sizeIndex >= 0 &&
                it.moveToFirst()
            ) {
                return it.getLong(
                    sizeIndex
                )
            }
        }

        return null
    }

    private fun getFileExtension(
        fileUri: Uri,
        mediaType: String
    ): String? {

        val mimeType =
            context.contentResolver
                .getType(fileUri)

        return when {

            mediaType == "GIF" ->
                "gif"

            mediaType == "IMAGE" &&
                    mimeType == "image/png" ->
                "png"

            mediaType == "IMAGE" &&
                    mimeType == "image/webp" ->
                "webp"

            mediaType == "IMAGE" ->
                "jpg"

            mediaType == "VIDEO" &&
                    mimeType == "video/webm" ->
                "webm"

            mediaType == "VIDEO" &&
                    mimeType == "video/quicktime" ->
                "mov"

            mediaType == "VIDEO" ->
                "mp4"

            else ->
                null
        }
    }
}