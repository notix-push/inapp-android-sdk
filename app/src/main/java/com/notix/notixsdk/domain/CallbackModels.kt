package com.notix.notixsdk.domain


sealed class NotixCallback

class NotixSubscriptionCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixImpressionCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixClickCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixRefreshDataCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixManageAudienceCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixPushDataLoadCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixConfigLoadCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()

class NotixGetTokenCallback(val status: NotixCallbackStatus, val token: String?) :
    NotixCallback()

class NotixMetricsCallback(val status: NotixCallbackStatus, val data: String?) :
    NotixCallback()
