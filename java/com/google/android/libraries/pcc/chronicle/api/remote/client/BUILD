load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(
    default_visibility = [
        "//visibility:public",
    ],
)

kt_android_library(
    name = "client",
    srcs = [
        "AidlTransport.kt",
        "BaseRemoteClient.kt",
        "ChronicleServiceConnector.kt",
        "DefaultChronicleServiceConnector.kt",
        "DefaultRemoteComputeClient.kt",
        "DefaultRemoteStoreClient.kt",
        "DefaultRemoteStreamClient.kt",
        "ManualChronicleServiceConnector.kt",
        "RemoteClient.kt",
        "RemoteComputeClient.kt",
        "RemoteStoreClient.kt",
        "RemoteStreamClient.kt",
        "Transport.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "//third_party/java/proto:empty_java_proto_lite",
        "@maven//:com_google_flogger_flogger",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
