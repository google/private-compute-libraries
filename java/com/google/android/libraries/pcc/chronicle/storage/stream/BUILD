load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = [
    "//java/com/google/android/libraries/pcc/chronicle:integration_visibility",
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
        "@maven//:javax_annotation_javax_annotation_api",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
