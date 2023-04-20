# Notix InApp SDK

<img align="right" width="80px" src="https://img.cdnotix.com/notix-static/readme-icon.png">

NOTIX is an audience re-engagement service based on push notifications that work for both desktop and mobile devices.

## Contents

* [Setup](#setup)
  * [1. Add JitPack repository](#1-add-jitpack-repository)
  * [2. Add SDK dependency](#2-add-sdk-dependency)
  * [3. Extend the Application class](#3-extend-the-application-class)
  * [4. Initialize the SDK](#4-initialize-the-sdk)
* [Next steps](#next-steps)


## Setup

### 1. Add JitPack repository
Add the JitPack repository to your project in one of 2 ways:

* in **settings.gradle** if you use `dependencyResolutionManagement`
```groovy
dependencyResolutionManagement {
    repositories {
        /* ... */
        maven { url 'https://jitpack.io' }
    }
}
```

* in top level **build.gradle** file if you use the old way of declaring repositories
```groovy
allprojects {
    repositories {
        /* ... */
        maven { url 'https://jitpack.io' }
    } 
}
```

[jitpack official documentation](https://jitpack.io/)

### 2. Add SDK dependency

Add the sdk dependency to your module's **build.gradle** file, in the `dependencies` block

```groovy
implementation 'com.github.notix-push:inapp-android-sdk:+'
```

### 3. Extend the Application class

3.1 Create a new file **App.java** and extend `Application`
    
```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /* ... */
    }
}
```

3.2. Register the newly created `App` class in your **AndroidManifest.md**.

```xml
<manifest ...>
    <application 
        ...
        android:name=".App">
        ...
    </application>
</manifest>
```

### 4. Initialize the SDK

Call `Notix.Companion.init()` from `Application.onCreate()` using your **notix app id** and **notix auth token**. 
These credentials can be found on the page of your In-App Android source. https://app.notix.co/sites/list

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    
        NotixSDKConfig config = new NotixSDKConfig();
        config.setInterstitialStartupEnabled(false);
    
        Notix.Companion.init(
                this,
                NOTIX_APP_ID,
                NOTIX_TOKEN,
                config,
                new CallbackHandler()
        );
    }
}
```

## Next steps
[Setup push notifications](README-JAVA.md)