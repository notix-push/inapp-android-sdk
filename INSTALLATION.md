# Notix InApp SDK

<img align="right" width="80px" src="https://img.cdnotix.com/notix-static/readme-icon.png">

NOTIX is an audience re-engagement service based on push notifications that work for both desktop and mobile devices.

## Installation

### Connect jitpack
Add the JitPack repository to your build file (settings.gradle)

[jitpack integration documentation](https://jitpack.io/)

```xml
pluginManagement {
    ...
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}

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
