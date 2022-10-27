load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"])

kt_android_library(
    name = "serialization",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_entity",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)
