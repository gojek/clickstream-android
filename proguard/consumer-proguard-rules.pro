## Protobuf lite
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

## Tinder Scarlet
-if interface * { @com.tinder.scarlet.ws.* <methods>; }
-keep,allowobfuscation interface <1>
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @com.tinder.scarlet.ws.* <methods>;
}

# Keep the ProcessLifecycleInitializer meta
-keepresourcexmlelements manifest/application/provider/meta-data@name=androidx.lifecycle.ProcessLifecycleInitializer
-keepresourcexmlelements manifest/application/provider/meta-data@value=androidx.startup
