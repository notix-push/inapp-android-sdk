package co.notix.utils

internal interface Mapper<F, T> {
    fun map(from: F): T
}

internal fun <F, T> Mapper<F, T>.mapOrNull(from: F) = runCatching { map(from) }.getOrNull()