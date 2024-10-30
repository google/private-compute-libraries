load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "testdata_proto",
    testonly = True,
    srcs = ["testdata.proto"],
)

java_lite_proto_library(
    name = "testdata_java_proto_lite",
    testonly = True,
    deps = [":testdata_proto"],
)
