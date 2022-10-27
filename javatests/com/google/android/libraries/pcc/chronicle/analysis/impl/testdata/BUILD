load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(
    default_visibility = [
        "//javatests/com/google/android/libraries/pcc/chronicle/analysis/impl:__subpackages__",
    ],
)

kt_android_library(
    name = "testdata",
    testonly = True,
    srcs = glob(["*.kt"]),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
    ],
)
