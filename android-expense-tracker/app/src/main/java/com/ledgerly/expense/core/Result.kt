package com.ledgerly.expense.core

/**
 * A lightweight result wrapper used across the domain and data layers so that
 * callers can handle success/failure without exceptions leaking into the UI.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val throwable: Throwable, val message: String? = throwable.message) :
        AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onError(block: (AppResult.Error) -> Unit): AppResult<T> {
    if (this is AppResult.Error) block(this)
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

/** Wrap a suspending/blocking call, capturing exceptions as [AppResult.Error]. */
inline fun <T> runCatchingResult(block: () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (ce: kotlinx.coroutines.CancellationException) {
    throw ce // never swallow cancellation
} catch (t: Throwable) {
    AppResult.Error(t)
}
