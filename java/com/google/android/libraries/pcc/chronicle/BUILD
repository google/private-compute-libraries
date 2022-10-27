package(default_visibility = ["//visibility:private"])

exports_files(["AndroidManifest.xml"])

package_group(
    name = "api_visibility",
    includes = [
        ":internal_visibility",
    ],
)

# Visibility group used to allow client code that integrates Chronicle into
# their app to access implementations of the APIs needed to bootstrap Chronicle.
# The packages listed here should be as focused as possible, to not allow wider
# access to implementation classes to feature code.
package_group(
    name = "integration_visibility",
    includes = [
        ":internal_visibility",
    ],
)

package_group(
    name = "internal_visibility",
    packages = [
        "//java/com/google/android/libraries/pcc/chronicle/...",
        "//java/com/google/android/libraries/pcc/plugin/samples/...",
        "//javatests/com/google/android/libraries/pcc/chronicle/...",
        "//javatests/com/google/android/libraries/pcc/plugin/samples/...",
    ],
)

package_group(
    name = "datacache_visibility",
    includes = [
        ":internal_visibility",
    ],
)

package_group(
    name = "datacache_impl_visibility",
    includes = [
        ":internal_visibility",
    ],
)

# List of packages that may access the policy proto api.
package_group(
    name = "policy_proto_visibility",
    includes = [
        ":internal_visibility",
    ],
)
