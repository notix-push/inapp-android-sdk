package co.notix.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response

internal val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

internal inline fun <T> Response.fold(
    onSuccess: (response: Response) -> T,
    onFailure: (response: Response) -> T
): T = if (isSuccessful) onSuccess(this) else onFailure(this)

internal fun Response.throwIfBadStatusCode() = fold(
    onSuccess = { it },
    onFailure = { error("unsuccessful response: $code, ${body?.string()} ") }
)