load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

bzl_library(
    name = "chronicle_codegen_bzl",
    srcs = ["chronicle_codegen.bzl"],
    visibility = ["//visibility:private"],
    deps = [
        ":proto_bzl",
        "//tools/build_defs/kotlin:rules_bzl",
    ],
)

bzl_library(
    name = "proto_bzl",
    srcs = ["proto.bzl"],
    visibility = ["//visibility:private"],
    deps = [],
)
