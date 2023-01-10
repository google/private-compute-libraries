load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_jvm_library(
    name = "testutil",
    testonly = True,
    srcs = [
        "TestAnnotationProcessor.kt",
        "Util.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_testing_compile_compile_testing",
    ],
)
