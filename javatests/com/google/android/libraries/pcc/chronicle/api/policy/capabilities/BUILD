load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "CapabilityTest",
    srcs = ["CapabilityTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/capabilities",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "CapabilitiesTest",
    srcs = ["CapabilitiesTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/capabilities",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)
