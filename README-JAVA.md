# Notix InApp SDK Java

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

```java
package YOUR-APPLICATION-PACKAGE

import android.content.Intent;
import androidx.annotation.NonNull;
import com.notix.notixsdk.INotificationActivityResolver;

public class NotificationResolver implements INotificationActivityResolver {
    @NonNull
    @Override
    public Class<?> resolveActivity(@NonNull Intent intent) {
        switch (intent.getStringExtra("event")) {
            case "main":
                return MainActivity.class;
            default:
                return MainActivity.class;
        }
    }
}
```

##### [NotixMessagingServiceImplementation.kt](https://img.cdnotix.com/notix-static/NotixMessagingServiceImplementation.kt)

```java
package YOUR-APPLICATION-PACKAGE

import android.annotation.SuppressLint;
import android.app.Notification;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.RemoteMessage;
import com.notix.notixsdk.NotificationParameters;
import com.notix.notixsdk.NotificationsService;
import com.notix.notixsdk.NotixFirebaseMessagingService;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class NotixMessagingServiceImplementation extends NotixFirebaseMessagingService {
    @NonNull
    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        NotificationParameters notification = new NotificationParameters();
        notification.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
        notification.setTitle(intent.getStringExtra("title"));
        notification.setText(intent.getStringExtra("text"));
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setLargeIcon(R.mipmap.ic_launcher);

        new NotificationsService().handleNotification(this, new NotificationResolver(), intent, notification);
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

### Integration code
Add variable to Launcher activity
```java
private final NotixSDK sdk = new NotixSDK();
```

Add it to onCreate method in your launcher activity (for example MainActivity)


```java
sdk.init(this, "notix-app-id", "notix-auth-token", null);
```

Replace notix-app-id and notix-auth-token with your

###Done!

Run app and just send your first notification in [https://app.notix.co/messages/create](https://app.notix.co/messages/create)

## Features

#### Audiences
You can also manage user audiences (see *link to audiences*)

just use:

```java
notixSdk.getAudiences().add(this, "custom-audience");
```

```java
notixSdk.getAudiences().delete(this, "custom-audience");
```

#### Events

You can specify which screen to open when you click on the push notification. To do this, you need to set a set of Events on the tag settings page and use it when creating a mailing list on the mailing list creation page.

In the code, it will be enough for you to specify in the NotificationResolver.kt file the correspondence event-name -> ConcreteActivity

For example:

```java

@NonNull
@Override
public Class<?> resolveActivity(@NonNull Intent intent) {
    switch (intent.getStringExtra("event")) {
        case "main":
            return MainActivity.class;
        case "buy":
            return BuyActivity.class;
        case "news":
            return NewsActivity;
        default:
            return MainActivity.class;
    }
}
```
You can see that matches have been added for the buy and news events
