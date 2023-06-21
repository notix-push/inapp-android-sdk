## Contents

* [Setup](#setup)
  * [1. Add SDK dependency](#1-add-sdk-dependency)
  * [2. Extend the Application class](#2-extend-the-application-class)
* [Push notifications setup](#push-notifications-setup)
* [Interstitial setup](#interstitial-setup)
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

## Push notifications setup

Call `NotixPush.init()` from `Application.onCreate()` using your **notixAppId** and **notixToken**. These credentials can be found on the page of your In-App Android source https://app.notix.co/sites/list

```kotlin  
NotixPush.init(
    context = this,  
    notixAppId = /* your notixAppId */ 
    notixToken = /* your notixToken */
) 
```

Run the app and send your first notification using Notix! https://app.notix.co/messages/create

## Interstitial setup

Create a loader that will be accessible throughout your app:
```kotlin
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    /* ... */
    interstitialLoader = NotixInterstitial.createLoader(zoneId = /* your Zone ID */)
    interstitialLoader.startLoading()
  }

  companion object {
    lateinit var interstitialLoader: InterstitialLoader
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

For additional settings please visit [this page](README.md)