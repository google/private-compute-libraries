load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_android_library(
    name = "optics",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
    ],
)

kt_jvm_library(
    name = "optics_jvm",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/api/operation:operation_jvm",
    ],
)
