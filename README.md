
<p align="center">
<img src="https://github.com/gojek/clickstream-android/blob/main/docs/assets/clickstream-horizontal-black.svg#gh-light-mode-only" width="500"/>
</p>

<p align="center">
<img src="https://github.com/gojek/clickstream-android/blob/main/docs/assets/clickstream-horizontal-white.svg#gh-dark-mode-only" width="500"/>
</p>

#### A Modern, Fast, and Lightweight Android Ingestion Library

![CI](https://github.com/gojek/clickstream-android/workflows/Build/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gojek.clickstream/clickstream-android/badge.svg)](https://search.maven.org/artifact/com.gojek.android/clickstream)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Clickstream is an event agnostic, real-time data ingestion platform. Clickstream allows apps to maintain a long-running connection to send data in real-time.

The word “Clickstream” is a trail of digital breadcrumbs left by users as they click their way through a website or mobile app. It is loaded with valuable customer information for businesses and its analysis and usage has emerged as a powerful data source.

**To know more about Clickstream, you can read our [Medium post](https://www.gojek.io/blog/introducing-clickstream?utm_source=blog&utm_medium=medium%20blog&utm_campaign=blog_clickstream)**

**Clickstream provides an end to end solution for event ingestion. For setting up the backend infrastructure please check out [raccoon](https://github.com/goto/raccoon)**


## Architecture

![Clickstream Architecture](https://github.com/gojekfarm/clickstream-ios/blob/main/Resources/clickstream-architecture.png)

#### Mobile Library Architecture

![Clickstream HLD](https://github.com/gojekfarm/clickstream-ios/blob/main/Resources/clickstream-HLD.png)

## Key features

-   Simple and lightweight
-   Remotely Configurable
-   Support for real-time data
-   Multiple QoS support (QoS0 and QoS1)
-   Typesafe and reusable schemas
-   Efficient payloads
-   In-built data aggregation

### Getting Started with Clickstream

1. Add the maven repository URL to the root `build.gradle` of your project.

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
}
```

2. Add the following dependencies to your module `build.gradle`

```kotlin
dependencies {
    val version = "x.y.z"
    // Required
    implementation 'com.gojek.clickstream:clickstream-android:[latest_version]'
    implementation 'com.gojek.clickstream:clickstream-lifecycle:[latest_version]'

    // Optional
    implementation 'com.gojek.clickstream:clickstream-health-metrics:[latest_version]'
}
```

By default ClickStream embedded the rules, so not necessary client added the rules explicitly

```
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Scarlet
-if interface * { @com.tinder.scarlet.ws.* <methods>; }
-keep,allowobfuscation interface <1>
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @com.tinder.scarlet.ws.* <methods>;
}

# Keep the ProcessLifecycleInitializer meta
-keepresourcexmlelements manifest/application/provider/meta-data@name=androidx.lifecycle.ProcessLifecycleInitializer
-keepresourcexmlelements manifest/application/provider/meta-data@value=androidx.startup
```

Once you’ve added the dependencies and synchronized your Gradle project, the next step is to initialize Clickstream.

### Initialization

Initialization of the Clickstream can be done on the background thread or main-thread,
Invocation should be done on the Application class. So that the initialization happens only once.

To create a Clickstream instance you can do the following setup:

```kotlin
class App : Application() {

    override fun onCreate() {
        initClickStream()
    }

    private fun initClickStream() {
        ClickStream.initialize(
            configuration = CSConfiguration.Builder(
                context = context,
                info = CSInfo(
                    appInfo = appInfo,
                    locationInfo = locationInfo,
                    deviceInfo = csDeviceInfo,
                    customerInfo = customerInfo,
                    sessionInfo = sessionInfo
                ),
                config = getBuildConfig(config),
                appLifeCycle = DefaultCSAppLifeCycleObserver(context),
                healthGateway = DefaultOpCSHealthGateway.factory(/*args*/)
            ).apply {
                setLogLevel(DEBUG)
                /**/ 
                setCSSocketConnectionListener(connectionListener())
            }.build())
    }

    /**
    * @see [CSConnectionEvent] for more detail explanation
    */
    private fun onConnectionListener(): CSSocketConnectionListener {
        return object : CSSocketConnectionListener {
            override fun onEventChanged(event: CSConnectionEvent) {
                is OnConnectionConnecting -> {}
                is OnConnectionConnected -> {}
                is OnMessageReceived -> {}
                is OnConnectionClosing -> {}
                is OnConnectionClosed -> {}
                is OnConnectionFailed -> {}
 			}
    	}
    }
}
```


### Configuration

#### CSEventSchedulerConfig

Holds the configurations for Clickstream. These constraints allow for fine-grained control over the library behaviour like duration between retries, flush events when app goes in background, etc.

|  Description | Variable | Type |	Default value |
|--|--|--|--|
| Number of events to combine in a single request | eventsPerBatch | Int | 20 |
| Delay between two requests (in millis) | batchPeriod | Long | 10000 |
| Flag for enabling forced flushing of events | flushOnBackground | Boolean | false |
| Wait time after which socket gets disconnected | connectionTerminationTimerWaitTimeInMillis | Long | 5000 |
| Flag for enabling flushing of events by background task | backgroundTaskEnabled | Boolean	| false |
| Initial delay for background task (in hour) | workRequestDelayInHr | Long | 1 |

#### CSNetworkConfig
Holds the configuration for network related. e.g configure timeouts for network channel.

|  Description|  Variable |	Type |	Default value |
|--|--|--|--|
|  Endpoint for web socket server | endPoint | String | No Default Value |
|  Connect timeout to be used by okhttp (in seconds) | connectTimeout | Long | 10 |
|  Read timeout to be used by okhttp (in seconds) | readTimeout | Long | 10 |
|  Write timeout to be used by okhttp (in seconds) | writeTimeout | Long | 10 |
|  Interval between pings initiated by client (in seconds) | pingInterval | Long | 1 |
|  Initial retry duration to be used for retry backoff strategy (in milliseconds) | initialRetryDurationInMs | Long | 1000 |
|  Maximum retry duration for retry backoff strategy (in milliseconds) | maxConnectionRetryDurationInMs | Long | 6000 |
|  Maximum retries per batch request | maxRetriesPerBatch | Long | 20 |
|  Maximum timeout for a request to receive Ack (in milliseconds) | maxRequestAckTimeout | Long | 10000 |
|  OkHttpClient instance that passed from client | okHttpClient | OkHttpClient | No Default Value |

#### CSEventClassification
Holds the class name to be classify into InstantEvent (QoS) or RealtimeEvent (QoS1).

|  Description |  Variable | Type |	Default value |
|--|--|--|--|
|  Holds all the eventTypes | eventTypes |	EventClassifier | [EventClassifier(identifier: "realTime", eventNames: []), EventClassifier(identifier: "instant", eventNames: [])]|

### Cleanup
Destroy instance of Clickstream, for example can be called when user logs out of the app.

```kotlin
   ClickStream.release()
```

### Push an Event

#### Using Explicit Builder

As Clickstream use a proto definition on the client-side, you can build a MessageLite and send it directly through ClickstreamSDK.

For instance, you’ve defined a proto definition called `Rating.java` which has the following properties

```
rating: Float
reason: String
```

Thus we can build the rating object just by using a Builder Pattern.

```kotlin
val event = Rating.newBuilder()
    .setRating(4.5)
    .setReason("nice!")
    .build()

// wrap event in CSEvent
val csEvent = CSEvent(
    guid = UUID.randomUUID().toString(),
    timestamp = Timestamp.getDefaultInstance(),
    message = event
)

// track the event
ClickStream.getInstance().trackEvent(csEvent)
```

Congratulations! You’re done!.

#### Running Sample App

In order to running the sample app, please follow this instruction
1. git clone git@github.com:gojekfarm/clickstream-android.git
2. cd clickstream-android
3. `./gradlew :app:installDebug` or via play button in the Android Studio

| Figure 1 | Figure 2 |
| ------ | ------ |
| <img src="https://github.com/gojekfarm/clickstream-android/blob/main/docs/assets/clickstream_sample_1.jpg" width="300"/> | <img src="https://github.com/gojekfarm/clickstream-android/blob/main/docs/assets/clickstream_sample_2.jpg" width="300"/> |

## Event Visualiser

Event visualiser is an android tool to visualise the client events being sent to Clickstream.

### Adding dependency
Add following to your module's `build.gradle`.
```kotlin
dependencies {
    val latest_version = "x.y.z"
    implementation("com.gojek.clickstream:clickstream-event-visualiser:$latest_version")
    implementation("com.gojek.clickstream:clickstream-event-visualiser-ui:$latest_version")
}
```
### Initialising
1. In your Application class, add `CSEventVisualiserListener` to Clickstream.
2. Call `CSEventVisualiserUI.initialise(this)` to initialise Event visualiser.
```kotlin
class App : Application() {
    /**/
    private fun initClickStream() {
        ClickStream.initialize(/**/).apply {
                /**/
                addEventListener(CSEventVisualiserListener.getInstance())
            }.build()
        CSEventVisualiserUI.initialise(this)
    }
}
```

### Usage
1. Call `CSEventVisualiserUI.getInstance().show()`to show a floating window and start recording all the events from clickstream.
<p align="center">
<img src="https://github.com/gojek/clickstream-android/blob/main/docs/assets/ev_window.jpg" width="300"/>
</p>

2. Click on Settings icon (top-left corner) to show a bottom sheet with all the actions that you can take with event visualiser.
<p align="center">
<img src="https://github.com/gojek/clickstream-android/blob/main/docs/assets/ev_actions.jpg" width="300"/>
</p>

- START CAPTURING (Starts event recording in event visualiser)
- STOP CAPTURING (Stops event recording in event visualiser)
- CLEAR DATA (Clears the current data in event visualiser)
- CLOSE (Closes the event visualiser window)

3. Clicking on the window will land to event home screen where all unique events are listed. You can click on any event to check event details.

4. Events can have the following states -
   * Scheduled - Events are scheduled (cached locally) by clickstream.
   * Dispatched - Events are sent to racoon.
   * Acknowledged - Events are acknowledged by racoon.

<p align="center">
<img src="https://github.com/gojek/clickstream-android/blob/main/docs/assets/ev_home.jpg" width="300"/>
<img src="https://github.com/gojek/clickstream-android/blob/main/docs/assets/ev_event_list.jpg" width="300"/>
<img src="https://github.com/gojekfarm/clickstream-android/blob/main/docs/assets/ev_event_detail.jpg" width="300"/>
</p>

### Excluding Event visualiser from release builds
Since Event visualiser is a debug tool that will be used by developers and testing teams only, it should ideally **NOT** be bundled with release builds.  
For this purpose there are light-weight, alternative NoOp (No Operation) dependencies.

#### Configuring NoOp dependency

```kotlin
dependencies {
    val latest_version = "x.y.z"
    // Use main dependency for debug build types
    debugImplementation("com.gojek.clickstream:clickstream-event-visualiser:$latest_version")
    debugImplementation("com.gojek.clickstream:clickstream-event-visualiser-ui:$latest_version")

    // Use NoOp dependency for release build types
    releaseImplementation("com.gojek.clickstream:clickstream-event-visualiser-noop:$latest_version")
    releaseImplementation("com.gojek.clickstream:clickstream-event-visualiser-ui-noop:$latest_version")

}
```

License
--------
    Copyright 2022 GOJEK

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
