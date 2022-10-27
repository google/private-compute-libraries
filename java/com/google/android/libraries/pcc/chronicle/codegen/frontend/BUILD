load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_jvm_library(
    name = "frontend",
    srcs = glob(
        ["*.kt"],
    ),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/util",
        "@maven//:com_google_protobuf_protobuf_java",
    ],
)
