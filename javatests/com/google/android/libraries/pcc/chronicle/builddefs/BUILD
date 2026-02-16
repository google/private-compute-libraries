load(
    "//java/com/google/android/libraries/pcc/chronicle/build_defs:chronicle_codegen.bzl",
    "chronicle_data_proto_library",
)
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

chronicle_data_proto_library(
    name = "proto_data",
    testonly = True,
    src = "chronicle_data_proto_test.proto",
    deps = {
        ":foreign_proto": ":foreign_java_proto_lite",
    },
)

proto_library(
    name = "foreign_proto",
    srcs = ["others.proto"],
)

java_lite_proto_library(
    name = "foreign_java_proto_lite",
    deps = [":foreign_proto"],
)

kt_android_local_test(
    name = "ProtoDataTest",
    srcs = ["ProtoDataTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":foreign_java_proto_lite",
        ":proto_data",
        "@maven//:com_google_truth_truth",
    ],
)
