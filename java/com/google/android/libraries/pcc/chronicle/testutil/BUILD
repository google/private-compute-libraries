load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//visibility:public"])

kt_android_library(
    name = "chronicle_module",
    testonly = True,
    srcs = ["ChronicleModule.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy:default_policy_conformance_check",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "//third_party/java/dagger",
        "//third_party/java/dagger/hilt:install_in",
        "@maven//:com_google_flogger_flogger",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
