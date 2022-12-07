load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "EmptiesTest",
    srcs = ["EmptiesTest.kt"],
    associates = [
        # '_kt' suffix references the actual kotlin dependency from bazel_kotlin to access 'internal' members
        "//java/com/google/android/libraries/pcc/chronicle/operation:empties_kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/operation:empties",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)
