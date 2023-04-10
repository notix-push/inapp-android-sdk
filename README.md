# Notix InApp SDK

<img align="right" width="80px" src="https://img.cdnotix.com/notix-static/readme-icon.png">

NOTIX is an audience re-engagement service based on push notifications that work for both desktop and mobile devices.


## Contents

- [Notix InApp SDK](#notix-inapp-sdk)
	- [Contents](#contents)
	- [Quick start](#quick-start)
	- [Installation](#installation)
		- [Connect jitpack](#connect-jitpack)
		- [SDK dependency](#sdk-dependency) 
		- [Create SDK files](#create-sdk-files)
		- [Configure AndroidManifest](#configure-androidmanifest)
		- [Integration code](#integration-code)
	- [Features](#features)
        - [Audiences](#audiences)
        - [Events](#events)
        - [Enabling logs](#enabling-logs)

## Quick start
Watch the video and follow the steps indicated there
[Coming soon]


## Installation

### Connect jitpack
Add the JitPack repository to your build file 

[jitpack integration documentation](https://jitpack.io/)

```xml
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### SDK dependency

Add it to a module build.gradle file in dependencies

```xml
implementation ('com.google.firebase:firebase-messaging:23.0.6') {
    exclude group: "org.apache.httpcomponents", module: "httpclient"
    exclude group: "org.apache.httpcomponents", module: "httpcore"
}
implementation 'com.github.notix-push:inapp-android-sdk:+'
```

### Create SDK files

##### [NotificationResolver.kt](https://img.cdnotix.com/notix-static/NotificationResolver.kt)

```kotlin
package YOUR-APPLICATION-PACKAGE

import android.content.Intent
import com.notix.notixsdk.INotificationActivityResolver

class NotificationResolver: INotificationActivityResolver {
    override fun resolveActivity(intent: Intent): Class<*> {
        return when(intent.getStringExtra("event")) {
            "main" -> MainActivity::class.java
            //"second" -> SecondActivity::class.java
            else -> MainActivity::class.java
        }
    }
}
```

##### [NotixMessagingServiceImplementation.kt](https://img.cdnotix.com/notix-static/NotixMessagingServiceImplementation.kt)

```kotlin
package YOUR-APPLICATION-PACKAGE

import android.annotation.SuppressLint
import android.app.Notification
import com.google.firebase.messaging.RemoteMessage
import com.notix.notixsdk.NotificationParameters
import com.notix.notixsdk.NotificationsService
import com.notix.notixsdk.NotixFirebaseMessagingService

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotixMessagingServiceImplementation: NotixFirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notification = NotificationParameters().apply {
            defaults = Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND
            title = intent.getStringExtra("title")
            text = intent.getStringExtra("text")
            smallIcon = R.mipmap.ic_notix
            largeIcon = R.mipmap.ic_notix
        }

        NotificationsService().handleNotification(this, NotificationResolver(), intent, notification)
    }
}
```

Replace YOUR-APPLICATION-PACKAGE with your app package

### Configure AndroidManifest

Add it to AndroidManifest.xml file in application part

```xml
<service
    android:name=".NotixMessagingServiceImplementation"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```
**If you have already declared firebase service - replace it**

### Integration code
Add it to onCreate method in your launcher activity (for example MainActivity)

```kotlin
NotixSDK.instance.init(
    this, "notix-app-id", "notix-auth-token", NotixSDKConfig(interstitialStartupEnabled = false)
)
```
And enable push notifications:

```kotlin
NotixSDK.instance.enablePushNotifications(this)
```

**Replace notix-app-id and notix-auth-token with your**

### Done!

Run app and just send your first notification in [https://app.notix.co/messages/create](https://app.notix.co/messages/create)

## Features

#### Audiences
You can also manage user audiences (see *link to audiences*)

just use:

```kotlin
NotixSDK.instance.audiences.add(this, "custom-audience")
```

```kotlin
NotixSDK.instance.audiences.delete(this, "custom-audience")
```

#### Events

You can specify which screen to open when you click on the push notification. To do this, you need to set a set of Events on the tag settings page and use it when creating a mailing list on the mailing list creation page.

In the code, it will be enough for you to specify in the NotificationResolver.kt file the correspondence event-name -> ConcreteActivity

For example:

```kotlin
override fun resolveActivity(intent: Intent): Class<*> {
        return when(intent.getStringExtra("event")) {
            "main" -> MainActivity::class.java
            "buy" -> BuyActivity::class.java
            "news" -> NewsActivity::class.java
            else -> MainActivity::class.java
        }
    }
```
You can see that matches have been added for the buy and news events

#### Enabling logs
In order to enable logging, call 
```kotlin
NotixSDK.instance.setLogLevel(IMPORTANT) // could also be FULL/IMPORTANT/NONE.
```
log level is set to `NONE` by default