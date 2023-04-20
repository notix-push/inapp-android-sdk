# Notix InApp SDK

<img align="right" width="80px" src="https://img.cdnotix.com/notix-static/readme-icon.png">

NOTIX is an audience re-engagement service based on push notifications that work for both desktop and mobile devices.

## Contents

- [Setup](#setup)
- [Enable push notifications](#enable-push-notifications)
- [Features](#features)
	- [Handle Notix callbacks](#handle-notix-callbacks)
	- [Modify incoming push messages](#modify-incoming-push-messages) 
	- [Handle Target Events](#handle-target-events)
	- [Audiences](#audiences)
	- [Managing logs](#managing-logs)

## Setup

Start with [SDK setup](SETUP.md) if you haven't done it already.

## Enable push notifications

Enable push notifications:

```kotlin
Notix.enablePushNotifications(context)
```

Run the app and send your first notification using Notix! [https://app.notix.co/messages/create](https://app.notix.co/messages/create)

## Features

### Handle Notix callbacks

1. Implement `NotixCallbackHandler` interface

```kotlin
class CallbackHandler : NotixCallbackHandler {
    override fun handle(context: Context, callback: NotixCallback) {
        when (callback) {
            is NotixCallback.Impression -> Log.i(APP_TAG, callback.toString())
            is NotixCallback.Subscription -> Log.i(APP_TAG, callback.toString())
            else -> Unit
        }
    }
}
```

2. And register it in `init`

```kotlin
Notix.init(
	/* ... */
	callbackHandler = CallbackHandler(),
	/* ... */
)
```

### Modify incoming push messages

You can modify the content of incoming push messages.

1. Implement `NotixNotificationModifier` interface 

```kotlin
class NotificationModifier : NotixNotificationModifier {
	override fun modify(context: Context, notificationParameters: NotificationParameters) =
		notificationParameters.copy(
			title = "1| " + notificationParameters.title,
			text = context.packageName + "\n" + notificationParameters.text
		)
}
```

2. And register it in `enablePushNotifications`
```kotlin
Notix.enablePushNotifications(
	/* ... */
	notificationModifier = NotificationModifier(),
	/* ... */
)
```

### Handle Target Events

1. Implement `NotixTargetEventHandler` interface

```kotlin
class TargetEventHandler : NotixTargetEventHandler {
    override fun handle(context: Context, eventName: String?) {
        val activityClass = when (eventName) {
            "main" -> MainActivity::class.java
            "other" -> OtherActivity::class.java
            else -> MainActivity::class.java
        }
        context.startActivity(Intent(context, activityClass))
    }
}
```

2. And register it in `enablePushNotifications`
```kotlin
Notix.enablePushNotifications(
	/* ... */
	targetEventHandler = TargetEventHandler(),
	/* ... */
)
```

### Audiences
You can manage user audiences (see *link to audiences*)

```kotlin
Notix.addAudience("custom-audience")
Notix.deleteAudience("custom-audience")
```

### Managing logs
You can manage logging done by Notix SDK. 

```kotlin
Notix.setLogLevel(FULL) // can be FULL/IMPORTANT/NONE.
```

Call `setLogLevel` before `init` to filter all the logs. Log level is set to `IMPORTANT` by default.