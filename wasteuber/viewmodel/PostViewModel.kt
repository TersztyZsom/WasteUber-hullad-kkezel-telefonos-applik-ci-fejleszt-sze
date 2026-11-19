package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.Comment
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.data.model.PostEdit
import hu.wasteuber.app.data.repository.PostRepository

class PostViewModel : ViewModel() {

    private val postRepository =
        PostRepository()

    fun createPost(
        text: String,
        mediaUrl: String,
        mediaType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.createPost(
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getPosts(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getMyPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getMyPosts(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getPost(
        postId: String,
        onSuccess: (Post) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getPost(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getPendingPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getPendingPosts(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun approvePendingPost(
        postId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.approvePendingPost(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun rejectPendingPost(
        postId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.rejectPendingPost(
            postId = postId,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun editPost(
        postId: String,
        newText: String,
        newMediaUrl: String,
        newMediaType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.editPost(
            postId = postId,
            newText = newText,
            newMediaUrl = newMediaUrl,
            newMediaType = newMediaType,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getEditHistory(
        postId: String,
        onSuccess: (List<PostEdit>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getEditHistory(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun deleteOwnPost(
        postId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.deleteOwnPost(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun adminDeletePost(
        postId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.adminDeletePost(
            postId = postId,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun isPostLiked(
        postId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.isPostLiked(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun toggleLike(
        postId: String,
        onSuccess: (Boolean, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.toggleLike(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getComments(
        postId: String,
        onSuccess: (List<Comment>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getComments(
            postId = postId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getCommentReplies(
        postId: String,
        parentCommentId: String,
        onSuccess: (List<Comment>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.getCommentReplies(
            postId = postId,
            parentCommentId = parentCommentId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun addComment(
        postId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.addComment(
            postId = postId,
            text = text,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun addCommentReply(
        postId: String,
        parentCommentId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.addCommentReply(
            postId = postId,
            parentCommentId = parentCommentId,
            text = text,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun isCommentLiked(
        postId: String,
        commentId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.isCommentLiked(
            postId = postId,
            commentId = commentId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun toggleCommentLike(
        postId: String,
        commentId: String,
        onSuccess: (Boolean, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        postRepository.toggleCommentLike(
            postId = postId,
            commentId = commentId,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}