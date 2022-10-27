load(
    "//java/com/google/android/libraries/pcc/chronicle/build_defs:chronicle_codegen.bzl",
    "chronicle_data_library",
)

chronicle_data_library(
    name = "chronicle_data_java",
    testonly = True,
    srcs = glob(
        ["*.java"],
        exclude = ["Erroneous*"],
    ),
    deps = [
        "//third_party/java/auto:auto_value",
        "@maven//:com_google_guava_guava",
    ],
)

chronicle_data_library(
    name = "chronicle_data_kt",
    testonly = True,
    srcs = glob(
        ["*.kt"],
        exclude = ["Erroneous*"],
    ),
)

filegroup(
    name = "annotatedtypes",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor:__pkg__"],
)
