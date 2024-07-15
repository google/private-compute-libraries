"""Custom rule to generate ChronicleData from Protos"""

load("//third_party/protobuf/bazel/common:proto_info.bzl", "ProtoInfo")

def _chronicle_data_proto_library_helper(ctx):
    direct_descriptor_set = depset(
        [dep[ProtoInfo].direct_descriptor_set for dep in ctx.attr.proto_deps],
    )

    # Explicitly declare our output files rather than using
    # ctx.actions.declare_directory. We do this because the downstream
    # kt_android_library that uses these generated files needs explicit file
    # listings in the depset to handle the generated java correctly.
    label_as_classname = "".join([part.capitalize() for part in ctx.label.name.split("_")])
    kotlin_output_file = ctx.actions.declare_file("%s_Generated.kt" % label_as_classname)
    module_output_file = ctx.actions.declare_file("%s_GeneratedModule.java" % label_as_classname)

    # Build arguments for our code-generator.
    gensrc_args = ctx.actions.args()
    gensrc_args.add(kotlin_output_file.path)
    gensrc_args.add(module_output_file.path)
    gensrc_args.add_all(direct_descriptor_set)

    # Run the code-generator.
    progress_message = "Generating ChronicleData Library: %s" % ctx.label.name
    ctx.actions.run(
        outputs = [
            kotlin_output_file,
            module_output_file,
        ],
        inputs = depset(transitive = [direct_descriptor_set]),
        arguments = [gensrc_args],
        progress_message = progress_message,
        executable = ctx.executable.generator,
        mnemonic = "ProtoChronicleDataGen",
    )

    return [
        DefaultInfo(
            files = depset(
                [
                    kotlin_output_file,
                    module_output_file,
                ],
            ),
        ),
    ]

chronicle_data_proto_library_helper = rule(
    implementation = _chronicle_data_proto_library_helper,
    attrs = dict(
        # the proto libraries
        proto_deps = attr.label_list(
            providers = [ProtoInfo],
        ),
        # the actual code-generator binary
        generator = attr.label(
            cfg = "exec",
            executable = True,
        ),
    ),
    fragments = ["java"],
    outputs = dict(),
    provides = [DefaultInfo],
)
