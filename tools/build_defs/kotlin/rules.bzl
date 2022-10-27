"""Simple way to alias the locations of the kotlin build rules from bazel so
they mirror how they are organized internally."""

load(
    "@io_bazel_rules_kotlin//kotlin:jvm.bzl",
    _kt_jvm_import = "kt_jvm_import",
    _kt_jvm_library = "kt_jvm_library",
    _kt_jvm_test = "kt_jvm_test",
)
load(
    "@io_bazel_rules_kotlin//kotlin:android.bzl",
    _kt_android_library = "kt_android_library",
    _kt_android_local_test = "kt_android_local_test",
)

kt_android_library = _kt_android_library

def kt_android_local_test(**kwargs):
    kwargs["deps"] = kwargs["deps"] + ["@maven//:org_robolectric_robolectric", "@robolectric//bazel:android-all"]
    _kt_android_local_test(**kwargs)

kt_jvm_library = _kt_jvm_library

kt_jvm_test = _kt_jvm_test

kt_jvm_import = _kt_jvm_import
