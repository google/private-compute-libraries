load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

[
    [
        kt_jvm_library(
            name = "%s_LIB" % file_name[:-3],
            srcs = [file_name],
            deps = [
                "//java/com/google/android/libraries/pcc/chronicle/codegen/util",
                "@maven//:com_google_truth_truth",
                "@maven//:junit_junit",
            ],
        ),
        java_test(
            name = file_name[:-3],
            runtime_deps = [
                ":%s_LIB" % file_name[:-3],
            ],
        ),
    ]
    for file_name in glob(["*.kt"])
]
