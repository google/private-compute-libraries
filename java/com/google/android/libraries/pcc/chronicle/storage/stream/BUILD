load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = [
    "//visibility:public",
])

kt_android_library(
    name = "stream",
    srcs = [
        "EntityStream.kt",
        "EntityStreamProvider.kt",
        "ManagedEntityStreamServer.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_code_findbugs_annotations",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
