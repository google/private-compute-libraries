load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_jvm_library(
    name = "lens",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/optics:optics_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/typeconversion",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/util",
        "@maven//:com_squareup_javapoet",
        "@maven//:com_squareup_kotlinpoet",
    ],
)
