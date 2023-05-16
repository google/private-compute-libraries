load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "serialization",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_entity",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)
