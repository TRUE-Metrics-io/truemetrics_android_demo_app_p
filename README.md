# Truemetrics Android SDK

## Setup

Project requires Java 17. If necessary, adjust Java version in IDE's `File` -> `Project structure` -> `SDK Location`.

## API KEY

TODO: Add info how to get an API KEY

### Usage

#### Declare dependency on TruemetricsSDK

Inside your `build.gradle`:

```
implementation 'io.truemetrics:truemetricssdk:0.0.1'
```

#### Foreground service and notification

In order to reliably collect data, SDK starts a Foreground service and shows a notification while recording is in progress.

SDK manages Foreground service but needs existing notification channel and a notification that will be shown so integrating your app needs to create a notification channel, provide a notification in `Config` parameter and ask user for notification permission if device runs Android 13 or later.

You can find example snippets for creating a notification channel and asking permission to post notifications in the Demo app.

#### Initialize SDK

```
TruemetricsSDK.initialize(
    this, 
    Config(
        apiKey = "YOUR-API-KEY",
        foregroundNotification = notification
    )
)
```

`foregroundNotification` parameter is a notification that will be shown when Foreground service is running, i.e. when recording is in progress.

#### Status callback

Set SDK status callback to get notified about SDK state changes and potential errors:

```
TruemetricsSDK.setStatusListener(object : StatusListener{
    override fun onStateChange(state: State) {
        
    }

    override fun onError(errorCode: ErrorCode, message: String?) {
        
    }
})
```

##### Start recording

```
TruemetricsSDK.startRecording()
```

Starting the recording also starts Foreground service and shows notification supplied in `Config` in system's status bar.

#### Stop recording

```
TruemetricsSDK.stopRecording()
```

Stopping recording also stops Foreground service and removes notification.

#### Log metatada

```
TruemetricsSDK.logMetadata(payload: String)
```

#### Deinitialize SDK

```
TruemetricsSDK.deinitialize()
```

De-initialization stops recording if in progress and tears down the SDK and cancels all scheduled uploads.





 
