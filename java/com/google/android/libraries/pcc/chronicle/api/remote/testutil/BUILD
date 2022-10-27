package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

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
