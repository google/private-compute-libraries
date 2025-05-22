load("@bazel_rules_android//android:rules.bzl", "android_library")
load("@bazel_skylib//:bzl_library.bzl", "bzl_library")
load("//third_party/bazel_rules/rules_java/java:java_binary.bzl", "java_binary")

package(default_visibility = ["//visibility:public"])

bzl_library(
    name = "build_defs_bzl",
    srcs = ["build_defs.bzl"],
    visibility = ["//visibility:private"],
    deps = ["//tools/build_defs/kotlin:rules_bzl"],
)

android_library(
    name = "ledger",
    testonly = True,
    srcs = glob(["*.kt"]),
    exports = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_code_gson_gson:gson-android",
        "@maven//:com_google_truth_truth",
    ],
)

java_binary(
    name = "LedgerMerge",
    testonly = True,
    srcs = ["LedgerMerge.java"],
    deps = [":ledger"],
)
