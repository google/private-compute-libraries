load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_android_library(
    name = "testdata",
    testonly = True,
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_data_android",
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//third_party/java/dagger",
        "//third_party/java/dagger/hilt:install_in",
        "@maven//:javax_inject_javax_inject",
    ],
)
