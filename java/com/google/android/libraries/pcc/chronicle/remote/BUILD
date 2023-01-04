load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = [
    "//java/com/google/android/libraries/pcc/chronicle:integration_visibility",
])

kt_android_library(
    name = "remote",
    srcs = [
        "ClientDetails.kt",
        "ClientDetailsProvider.kt",
        "RemoteContext.kt",
        "RemotePolicyChecker.kt",
        "RemoteRouter.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "@maven//:com_google_flogger_flogger",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
