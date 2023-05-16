load("@bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_visibility = [
        "//visibility:public",
    ],
)

android_library(
    name = "integration",
    srcs = [
        "DefaultChronicle.kt",
        "DefaultDataTypeDescriptorSet.kt",
        "NoOpChronicle.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_guava_guava",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
