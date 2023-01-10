load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_jvm_library(
    name = "processing_env",
    srcs = ["ProcessingEnv.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
    ],
)

kt_jvm_library(
    name = "util",
    srcs = [
        "ProtoDescriptorUtils.kt",
        "StringUtils.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_java",
    ],
)
