package com.pos.core.result

/**
 * 异步操作的结果包装类
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    inline fun <R> mapSuccess(block: (T) -> R): Result<R> = when (this) {
        is Success -> Success(block(data))
        is Error -> Error(exception)
        is Loading -> Loading
    }

    inline fun fold(
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit,
        onLoading: () -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception)
            is Loading -> onLoading()
        }
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Exception? = when (this) {
        is Error -> exception
        else -> null
    }

    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
}
