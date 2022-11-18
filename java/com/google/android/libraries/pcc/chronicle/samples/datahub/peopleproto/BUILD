load("//java/com/google/android/libraries/pcc/chronicle/build_defs:chronicle_codegen.bzl", "chronicle_data_proto_library")
load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle/samples:sample_visibility"])

chronicle_data_proto_library(
    name = "person_proto",
    src = "person.proto",
)

kt_android_library(
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
