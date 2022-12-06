"""Simple way to alias the locations of the android build rules from bazel so
they mirror how they are organized internally."""

android_library = native.android_library

android_local_test = native.android_local_test
