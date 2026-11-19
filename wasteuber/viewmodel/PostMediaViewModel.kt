package hu.wasteuber.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import hu.wasteuber.app.data.repository.PostMediaRepository

class PostMediaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val postMediaRepository =
        PostMediaRepository(
            context = application.applicationContext
        )

    fun uploadMedia(
        fileUri: Uri,
        mediaType: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        postMediaRepository.uploadMedia(
            fileUri = fileUri,
            mediaType = mediaType,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}