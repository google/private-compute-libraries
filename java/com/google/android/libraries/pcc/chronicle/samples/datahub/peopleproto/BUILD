load("@bazel_rules_android//android:rules.bzl", "android_library")
load("//java/com/google/android/libraries/pcc/chronicle/build_defs:chronicle_codegen.bzl", "chronicle_data_proto_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle/samples:sample_visibility"])

chronicle_data_proto_library(
    name = "person_proto",
    src = "person.proto",
)

android_library(
    name = "peopleproto",
    srcs = glob(["*.kt"]),
    exports = [
        ":person_proto",
    ],
    deps = [
        ":person_proto",
        "//java/com/google/android/libraries/pcc/chronicle/api:connection",
    ],
)
