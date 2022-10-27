load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_jvm_library(
    name = "element",
    srcs = glob(
        ["*.kt"],
    ),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/util",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/util:processing_env",
        "//third_party/java/auto:auto_value",
    ],
)
