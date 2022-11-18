load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle/samples:sample_visibility"])

kt_android_library(
    name = "proxy",
    srcs = ["PeopleProxy.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/client",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/samples/datahub/peopleproto",
        "//java/com/google/android/libraries/pcc/chronicle/samples/datahub/peopleproto:person_proto_java_proto_lite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
