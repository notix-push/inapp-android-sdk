# Notix InApp SDK

<img align="right" width="80px" src="https://img.cdnotix.com/notix-static/readme-icon.png">  

NOTIX is an audience re-engagement service based on push notifications that work for both desktop and mobile devices.

## Contents

* [Setup](#setup)
  * [1. Add SDK dependency](#1-add-sdk-dependency)
  * [2. Extend the Application class](#2-extend-the-application-class)
* [NotixPush setup](#notixpush-setup)
* [NotixInterstitial setup](#notixinterstitial-setup)
* [Further steps](#further-steps)

## Setup

### 1. Add SDK dependency

Add the sdk dependency to your module's **build.gradle** file, in the `dependencies` block

```groovy  
implementation 'co.notix:android-sdk:+'  
```  

### 2. Extend the Application class

2.1. Create a new file **App.kt** and extend `Application`

```kotlin  
class App : Application() {
    override fun onCreate() {   
        super.onCreate()
        /* ... */ 
    }
}   
```  

2.2. Register the newly created `App` class in your **AndroidManifest.xml**.

```xml  
<manifest ...>
    <application
        ...
        android:name=".App"> 
        ... 
    </application>
</manifest>  
```  

## NotixPush setup

Call `NotixPush.init()` from `Application.onCreate()` using your **notix app id** and **notix auth token**. These credentials can be found on the page of your In-App Android source https://app.notix.co/sites/list

```kotlin  
NotixPush.init(
    context = this,  
    notixAppId = /* your Notix app id */ 
    notixToken = /* your Notix token */
) 
```

Run the app and send your first notification using Notix! https://app.notix.co/messages/create

## NotixInterstitial setup

Call `NotixInterstitial.init` and create a loader that will be accessible throughout your app:
```kotlin
class App : Application() {  
    override fun onCreate() {  
        super.onCreate()
        /* ... */
        NotixInterstitial.init(notixToken = /* your Notix token */)  
        loader.startLoading() 
    }  
  
    companion object {  
        val interstitialLoader = NotixInterstitial.createLoader(zoneId = /* your Zone ID */)    
    }  
}
```

At the show site get `Interstitial` instance from the loader and pass it to `NotixInterstitial.show`:
```kotlin
App.interstitialLoader.doOnNextAvailable { result -> 
    if (result is InterstitialLoader.Result.Data) {
        NotixInterstitial.show(  
            context = this,  
            interstitial = result.interstitial
        )
    }
}
```
> You do not need to manage the loader yourself. It does its best effort to load and cache exactly 3 entries.

## Further steps

For more Notix options please visit [this page](README.md)