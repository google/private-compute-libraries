load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

FILES = glob(["*.kt"])

kt_android_local_test(
    name = "DefaultOpticsManifestTest",
    srcs = ["DefaultOpticsManifestTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics/testdata",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "LensTest",
    srcs = ["LensTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics/testdata",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "OpticalAccessPathTest",
    srcs = ["OpticalAccessPathTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics/testdata",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "TraversalTest",
    srcs = ["TraversalTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics/testdata",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)
