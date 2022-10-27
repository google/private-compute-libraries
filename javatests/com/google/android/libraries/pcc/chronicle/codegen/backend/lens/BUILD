load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

kt_jvm_library(
    name = "lib",
    testonly = True,
    srcs = glob(["*.kt"]),
    resources = glob(["goldens/**"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/lens",
        "@maven//:com_google_truth_truth",
        "@maven//:com_squareup_javapoet",
        "@maven//:com_squareup_kotlinpoet",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

[
    java_test(
        name = file_name[:-3],
        test_class = "com.google.android.libraries.pcc.chronicle.codegen.backend.lens.%s" % file_name[:-3],
        runtime_deps = [":lib"],
    )
    for file_name in glob(["*.kt"])
]
