load("@bazel_rules_android//android:rules.bzl", "android_library")

android_library(
    name = "expected_generated_java_types",
    testonly = True,
    srcs = glob(["*Java*.kt"]),
    visibility = [
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor:__pkg__",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/annotatedtypes:chronicle_data_java",
    ],
)

android_library(
    name = "expected_generated_kotlin_types",
    testonly = True,
    srcs = glob(["*Kotlin*.kt"]),
    visibility = [
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor:__pkg__",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/annotatedtypes:chronicle_data_kt",
    ],
)
