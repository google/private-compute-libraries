"""
Defines a macro to generate chronicle ledgers from application builds.
"""

load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

def chronicle_ledger(name, src, test_class, manifest, app_library_dep, additional_deps, ledger_map_g3_path, ledger_map_file):
    """
    Creates Chronicle Datahub Ledger targets.

    This macro is intended for use by Chronicle app integration owners only, it
    is not considered API intended for data stewards or feature developers.

    Args:
        name: string. Build name. This is the name of the build variant for the
            app. Actual value is not important as long as it's unique across all
            variants for the app and is understandable.
        src: file. Source file for a JUnit test which uses LedgerBuildAndTest.
        test_class: string. Fully qualified name of the test class in `src`.
        manifest: file. An AndroidManifest to use with the build.
        app_library_dep: android_java_library built to include all Chronicle
            dependencies for the `app` being tested.
        additional_deps: dependencies needed to build out the ledger. Often,
            this may include things like dagger qualifiers needed to extract the
            Chronicle configuration from the app's dagger graph.
        ledger_map_g3_path: string. Path rooted at /google3 to the location of
            the merged ledger map json file.
        ledger_map_file: string. Exported file blaze target for the merged
            ledger map json file.
    """

    # Runtime properties required by the BaseLedgerBuilderAndTester class.
    jvm_flags = [
        "-Dname=\"%s\"" % name,
        "-Dmap_name=\"%s\"" % ledger_map_g3_path,
    ]

    kt_android_local_test(
        name = "LedgerBuilder-%s" % (name),
        srcs = [src],
        # Tack on the is_test=0 flag to ensure LedgerBuildAndTest writes the
        # ledger to the OUTPUT_DIR path (specified as an environment variable).
        jvm_flags = jvm_flags + ["-Dis_test=0"],
        manifest = manifest,
        test_class = test_class,
        data = [ledger_map_file],
        tags = ["notap"],  # We do not need to run the build-mode during tap.
        deps = [
            "//java/com/google/android/libraries/pcc/chronicle/tools/ledger",
            "@maven//:com_google_dagger_dagger",
            "@maven//:com_google_dagger_dagger/hilt:install_in",
            "@maven//:com_google_dagger_dagger/hilt/testing",
            "@maven//:com_google_code_gson_gson",
            "@maven//:com_google_truth_truth",
        ] + [app_library_dep] + additional_deps,
    )

    kt_android_local_test(
        name = "LedgerTest-%s" % (name),
        srcs = [src],
        # Tack on the is_test=1 flag to ensure LedgerBuildAndTest does
        # verification.
        jvm_flags = jvm_flags + ["-Dis_test=1"],
        manifest = manifest,
        test_class = test_class,
        data = [ledger_map_file],
        deps = [
            "//java/com/google/android/libraries/pcc/chronicle/tools/ledger",
            "@maven//:com_google_truth_truth",
            "@maven//:com_google_dagger_dagger",
            "@maven//:com_google_dagger_dagger/hilt:install_in",
            "@maven//:com_google_dagger_dagger/hilt/testing",
            "@maven//:com_google_code_gson_gson",
        ] + [app_library_dep] + additional_deps,
    )
