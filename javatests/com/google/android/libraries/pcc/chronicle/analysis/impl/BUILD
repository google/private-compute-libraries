load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "AnnotationTest",
    srcs = ["AnnotationTest.kt"],
    associates = [
        # '_kt' suffix references the actual kotlin dependency from bazel_kotlin to access 'internal' members
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl:impl_kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:sandbox_processor_node",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/capabilities",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_mockito_kotlin_mockito_kotlin",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "CantripFactoryImplTest",
    srcs = [
        "CantripFactoryImplTest.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/cantrip",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics/testdata",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt_android",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_mockito_kotlin_mockito_kotlin",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "ChroniclePolicyEngineTest",
    srcs = ["ChroniclePolicyEngineTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:sandbox_processor_node",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/capabilities",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "//javatests/com/google/android/libraries/pcc/chronicle/analysis/impl/testdata",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_mockito_kotlin_mockito_kotlin",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "ManagementStrategyValidationTest",
    srcs = ["ManagementStrategyValidationTest.kt"],
    associates = [
        # '_kt' suffix references the actual kotlin dependency from bazel_kotlin to access 'internal' members
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl:impl_kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:sandbox_processor_node",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/capabilities",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_mockito_kotlin_mockito_kotlin",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "PolicyContextVerificationTest",
    srcs = ["PolicyContextVerificationTest.kt"],
    associates = [
        # '_kt' suffix references the actual kotlin dependency from bazel_kotlin to access 'internal' members
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl:impl_kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "//javatests/com/google/android/libraries/pcc/chronicle/analysis/impl/testdata",
        "@maven//:com_google_truth_truth",
    ],
)
