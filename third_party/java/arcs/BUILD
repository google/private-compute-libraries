package(default_visibility = ["//visibility:public"])

proto_library(
    name = "policy_proto",
    srcs = ["policy.proto"],
    deps = ["annotation_proto"],
)

proto_library(
    name = "annotation_proto",
    srcs = ["annotation.proto"],
)

proto_library(
    name = "manifest_proto",
    srcs = ["manifest.proto"],
    deps = [
        ":annotation_proto",
        ":policy_proto",
    ],
)
