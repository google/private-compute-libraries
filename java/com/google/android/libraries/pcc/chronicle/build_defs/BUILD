load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

bzl_library(
    name = "chronicle_codegen_bzl",
    srcs = ["chronicle_codegen.bzl"],
    visibility = ["//visibility:private"],
    deps = [
        ":proto_bzl",
        "//third_party/bazel_rules/rules_java/java:core_rules",
        "//third_party/protobuf/bazel:java_lite_proto_library_bzl",
        "//third_party/protobuf/bazel:java_proto_library_bzl",
        "//third_party/protobuf/bazel:proto_library_bzl",
        "//third_party/protobuf/build_defs:kt_jvm_proto_library",
        "//tools/build_defs/android:bzl",
    ],
)

bzl_library(
    name = "proto_bzl",
    srcs = ["proto.bzl"],
    visibility = ["//visibility:private"],
    deps = ["//third_party/protobuf/bazel/common:proto_info_bzl"],
)
