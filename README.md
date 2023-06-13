# Notix InApp SDK

<img align="right" width="80px" src="https://img.cdnotix.com/notix-static/readme-icon.png">  

NOTIX is an audience re-engagement service based on push notifications that work for both desktop and mobile devices.

## Contents

- [Setup](#setup)
- [Notix](#notix)
    - [Handle Notix callbacks](#handle-notix-callbacks)
    - [Managing logs](#managing-logs)
- [NotixPush](#notixpush)
    - [Audiences](#audiences)
    - [Modify incoming push messages](#modify-incoming-push-messages)
    - [Handle Target Events](#handle-target-events)
- [NotixInterstitial](#notixinterstitial)
    - [InterstitialLoader](#interstitialloader)

## Setup

Start with [SDK setup](SETUP.md) if you haven't done it already.

## Notix

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

2. And register it by calling

    ```kotlin  
    Notix.setCallbackHandler(CallbackHandler()) 
    ```

### Managing logs

You can manage logging done by Notix SDK.

```kotlin  
Notix.setLogLevel(FULL) // can be FULL/IMPORTANT/NONE.  
```  

Call `setLogLevel` before `init` to filter all the logs. Log level is set to `IMPORTANT` by default.

## NotixPush

> *For setup instructions see [NotixPush setup](SETUP.md#notixpush-setup)*

### Audiences

You can manage user audiences

```kotlin  
NotixPush.addAudience("custom-audience")  
NotixPush.deleteAudience("custom-audience")  
```

### Modify incoming push messages

You can modify the content of incoming push messages.

1. Implement `NotixNotificationModifier` interface

    ```kotlin  
    class NotificationModifier : NotixNotificationModifier {  
        override fun modify(context: Context, notificationOverrides: NotificationOverrides) =  
            notificationOverrides
    }
    ```  

2. And register it by calling:

    ```kotlin  
    NotixPush.setNotificationModifier(NotificationModifier())
    ```  

### Handle Target Events

1. Implement `NotixTargetEventHandler` interface

    ```kotlin  
    class TargetEventHandler : NotixTargetEventHandler {  
        override fun handle(context: Context, eventName: String?) {  
            val activityClass = when (eventName) {  
                "first" -> FirstActivity::class.java
                "second" -> SecondActivity::class.java
                else -> OtherActivity::class.java
            }  
            context.startActivity(Intent(context, activityClass))  
        }  
    } 
    ```  

2. And register it by calling:

    ```kotlin  
    NotixPush.setTargetEventHandler(TargetEventHandler())
    ```

## NotixInterstitial

> *For setup instructions see [NotixInterstitial setup](SETUP.md#notixinterstitial-setup)*

### InterstitialLoader

`InterstitialLoader`s were introduced to make it easier for a developer to deal with threading, loading, caching, and error handling when loading interstitial ads.

You can get an `Interstitial` from `InterstitialLoader` using one of 3 methods:
* `hasNext`/`getNext` is the simplest method. These functions work only with cached items and are fast.
* `awaitNext` is a suspend function, which accepts `timeout`. If the loader has at least one entry in the queue, it will immediately return it. Otherwise it will continue trying to fetch an ad for another `timeout`ms, and will return an `Error` if that `timeout` is exceeded *(which is very unlikely)*.
* `doOnNextAvailable` is the callback-way, which is Java-friendly. It works in the same way as `awaitNext`.