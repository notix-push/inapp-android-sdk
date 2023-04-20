package co.notix.push.data

internal data class PushDataResponse(
    val title: String?,
    val text: String?,
    val clickData: String?,
    val impressionData: String?,
    val targetUrl: String?,
    val iconUrl: String?,
    val imageUrl: String?
)