load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//javatests/com/google/android/libraries/pcc/chronicle/api/remote:__pkg__"])

proto_library(
    name = "testutil_proto",
    testonly = True,
    srcs = [
        "testutil.proto",
    ],
)

java_lite_proto_library(
    name = "testutil_java_proto_lite",
    testonly = True,
    deps = [":testutil_proto"],
)

kt_android_library(
    name = "testutil",
    testonly = True,
    srcs = glob(["*.kt"]),
    exports = [
        ":testutil_java_proto_lite",
    ],
    deps = [
        ":testutil_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)
