load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = [
    "//java/com/google/android/libraries/pcc/chronicle:integration_visibility",
])

kt_android_library(
    name = "impl",
    srcs = [
        "EntityStreamImpl.kt",
        "EntityStreamProviderImpl.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream",
        "@maven//:javax_annotation_javax_annotation_api",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
