package se.umu.calu0217.strive.core.error

/**
 * Sealed class representing different types of errors that can occur in the app.
 */
sealed class AppError {
    abstract val message: String

    data class NetworkError(
        override val message: String = "Network connection failed. Please check your internet connection."
    ) : AppError()

    data class DatabaseError(
        override val message: String = "Failed to access local data. Please try again."
    ) : AppError()

    data class ApiError(
        val code: Int,
        override val message: String
    ) : AppError()

    data class TimeoutError(
        override val message: String = "Request timed out. Please try again."
    ) : AppError()

    data class UnknownError(
        override val message: String = "An unexpected error occurred. Please try again."
    ) : AppError()
}

/**
 * Extension function to convert exceptions to user-friendly error messages.
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is java.io.IOException -> {
            if (this is java.net.SocketTimeoutException) {
                AppError.TimeoutError()
            } else {
                AppError.NetworkError()
            }
        }
        is retrofit2.HttpException -> AppError.ApiError(
            code = code(),
            message = "Server error (${code()}): ${message()}"
        )
        is android.database.sqlite.SQLiteException -> AppError.DatabaseError()
        else -> AppError.UnknownError(
            message = localizedMessage ?: "An unexpected error occurred"
        )
    }
}