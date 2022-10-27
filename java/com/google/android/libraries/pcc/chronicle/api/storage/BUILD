load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_android_library(
    name = "storage",
    srcs = glob(["*.kt"]),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    exports = [
        ":entity_metadata_java_proto_lite",
        "//third_party/java/proto:timestamp_java_proto_lite",
    ],
    deps = [
        ":entity_metadata_java_proto_lite",
        "//third_party/java/proto:timestamp_java_proto_lite",
    ],
)

proto_library(
    name = "entity_metadata_proto",
    srcs = ["entity_metadata.proto"],
    visibility = ["//visibility:private"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_lite_proto_library(
    name = "entity_metadata_java_proto_lite",
    visibility = ["//visibility:private"],
    deps = [":entity_metadata_proto"],
)
