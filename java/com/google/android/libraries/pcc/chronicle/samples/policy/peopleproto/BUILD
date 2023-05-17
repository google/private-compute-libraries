load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle/samples:sample_visibility"])

android_library(
    name = "peopleproto",
    srcs = [
        "PeopleProtoPolicy.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/samples/datahub/peopleproto",
    ],
)
