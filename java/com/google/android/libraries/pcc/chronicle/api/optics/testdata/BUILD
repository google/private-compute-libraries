load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "testdata",
    testonly = True,
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_data_android",
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt_android",
        "@maven//:javax_inject_javax_inject",
    ],
)
