"""Rules to helper with Chronicle code generation"""

load("@bazel_rules_android//android:rules.bzl", "android_library")
load(":proto.bzl", "chronicle_data_proto_library_helper")

def chronicle_data_proto_library(
        name,
        src,
        deps = {},
        testonly = False,
        has_services = False):
    """
    Builds a Chronicle Data library based on the proto (also builds the proto).

    Args:
        name: the name of the library to generate.
        src: the .proto file to generate the library from.
        testonly: OPTIONAL.
            Whether the generated library is for tests-only.
        deps: any other proto to java_proto_lite protobuf dependencies needed
            by the src. Dict from proto_library target to
            java_lite_proto_library target.
        has_services: OPTIONAL.
            Whether the src file contains gRPC service defs. This should usually
            be false.
    """

    proto_library_name = "%s_DO_NOT_DEPEND_proto" % name
    java_proto_library_name = "%s_DO_NOT_DEPEND_java_proto" % name
    java_proto_lite_library_name = "%s_java_proto_lite" % name
    generator_name = "%s_DO_NOT_DEPEND_generator" % name

    # Generate the base proto library.
    native.proto_library(
        name = proto_library_name,
        srcs = [src],
        deps = deps.keys(),
        visibility = ["//visibility:private"],
    )

    # Create a full java-proto-library from the proto. This will be used by
    # the code generator to inspect the generated classes' proto Descriptors.
    native.java_proto_library(
        name = java_proto_library_name,
        deps = [":%s" % proto_library_name],
        visibility = ["//visibility:private"],
    )

    # Create a java-lite proto library from the proto. This is what will be
    # provided as an "export" from the generated library and what will be usable
    # by chronicle data stewards and feature devs.
    native.java_lite_proto_library(
        name = java_proto_lite_library_name,
        deps = [":%s" % proto_library_name],
    )

    # Use the proto_chronicle_data_generator_lib to generate a new code
    # generation binary. Provide the full java proto library as a runtime
    # dependency to the binary so that it can use Class.forName(..) to fetch
    # the generated proto class for inspection.
    native.java_binary(
        name = generator_name,
        main_class = "com.google.android.libraries.pcc.chronicle.codegen.tool.ProtoChronicleDataGenerator",
        runtime_deps = [
            ":%s" % java_proto_library_name,
            "//java/com/google/android/libraries/pcc/chronicle/codegen/tool:proto_chronicle_data_generator_lib",
        ],
        visibility = ["//visibility:private"],
    )

    # Use the binary to generate DTD sources from the proto file
    chronicle_data_proto_library_helper(
        name = "%s_srcs" % name,
        generator = ":%s" % generator_name,
        proto_deps = [":%s" % proto_library_name],
    )

    # Put together a final library for use.
    android_library(
        name = name,
        srcs = [":%s_srcs" % name],
        testonly = testonly,
        exports = [
            ":%s" % java_proto_lite_library_name,
        ],
        manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
        deps = [
            ":%s" % java_proto_lite_library_name,
            "//java/com/google/android/libraries/pcc/chronicle/api",
            "//java/com/google/android/libraries/pcc/chronicle/api/optics",
            "@maven//:com_google_dagger_dagger",
            "@maven//:com_google_dagger_hilt_android",
            "@maven//:javax_inject_javax_inject",
        ] + deps.values(),
    )

def chronicle_data_library(
        name,
        srcs,
        deps = [],
        visibility = [],
        **kwargs):
    """
    Generates android_library from classes annotated with @ChronicleData or @ChronicleConnection.

    Args:
        name: The name of the rule that will be generated.

        srcs: List of Kotlin or Java files containing data classes
          annotated with @ChronicleData or @ChronicleConnection.

        deps: Any dependencies needed to build the provided source file.

        visibility: Controls whether the target can be used in other packages: go/blaze-visibility.

        **kwargs: Any additional keyword arguments needed, e.g. testonly,
          visibility, etc.
    """

    # Generate a placeholder Kotlin file. Otherwise the target directory for generated Kotlin files cannot be determined, see b/141189083#comment7 .
    placeholder_rule_name = name + "_auto_gen_hack"
    placeholder_file_name = name + "AutoGenHack.kt"
    package = native.package_name()
    package = package.replace("/", ".")
    native.genrule(
        name = placeholder_rule_name,
        outs = [placeholder_file_name],
        cmd = 'echo "package {package}\n" > $@'.format(package = package),
    )
    all_srcs = srcs + [placeholder_file_name]

    if "manifest" not in kwargs:
        kwargs["manifest"] = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml"

    android_library(
        name = name,
        srcs = all_srcs,
        deps = deps + [
            "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_connection_android",
            "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_data_android",
            "//java/com/google/android/libraries/pcc/chronicle/annotation:data_cache_store_android",
            "//java/com/google/android/libraries/pcc/chronicle/api",
            "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
            "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
            "@maven//:com_google_dagger_dagger",
            "@maven//:com_google_dagger_hilt_android",
        ],
        visibility = visibility + [
            "//visibility:public",
        ],
        **kwargs
    )
