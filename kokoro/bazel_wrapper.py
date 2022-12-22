#!/usr/bin/python
"""bazel_wrapper.py helps user populate configs for BES and foundry.

This script will read environment variables from build VM, and populate the
bazel command line flags accordingly. The environment variables will be injected
by Kokoro build config.
"""

import os
import subprocess
import sys
import uuid


def GetFoundryFlags():
  """Read the foundry setting from local environment variables.

  Returns:
    String list that contains flags for Foundry. Each of the item will be in the
    format of --<option>=<value>
  """
  result = []
  if os.environ.get('KOKORO_FOUNDRY_BACKEND_ADDRESS'):
    result.append('--remote_cache=' +
                  os.environ.get('KOKORO_FOUNDRY_BACKEND_ADDRESS'))
    if not os.environ.get('KOKORO_BAZEL_LOCAL_EXECUTION'):
      result.append('--remote_executor=' +
                    os.environ.get('KOKORO_FOUNDRY_BACKEND_ADDRESS'))
  if os.environ.get('KOKORO_FOUNDRY_PROJECT_ID'):
    result.append('--remote_instance_name=' +
                  os.environ.get('KOKORO_FOUNDRY_PROJECT_ID'))

  if result:
    result.append('--spawn_strategy=remote')
    result.append('--remote_timeout=3600')
    result.append('--strategy=Javac=remote')
    result.append('--strategy=Closure=remote')
    result.append('--genrule_strategy=remote')
  return result


def GetBesFlags():
  """Read the BES setting from local environment variables.

  Returns:
    String list that contains flags for BES. Each of the item will be in the
    format of --<option>=<value>
  """
  result = []
  if os.environ.get('KOKORO_BES_BACKEND_ADDRESS'):
    result.append('--bes_backend=' +
                  os.environ.get('KOKORO_BES_BACKEND_ADDRESS'))
    result.append('--bes_timeout=600s')
  if os.environ.get('KOKORO_BES_PROJECT_ID'):
    result.append('--project_id=' + os.environ.get('KOKORO_BES_PROJECT_ID'))
  return result


def GetAuthFlags():
  """Read the shared auth settings from local environment variables.

  Returns:
    String list that contains flags for authentication. Each of the item will be
    in the format of --<option>=<value>.
  """
  result = []
  if os.environ.get('KOKORO_BAZEL_AUTH_CREDENTIAL'):
    result.append('--auth_enabled=true')
    result.append('--auth_credentials=' +
                  os.environ.get('KOKORO_BAZEL_AUTH_CREDENTIAL'))
    result.append('--auth_scope='
                  'https://www.googleapis.com/auth/cloud-source-tools')
    result.append('--tls_enabled=true')
  return result


def BuildBazelCommand(argv, invocation_id):
  """Build bazel command that can be executed.

  Args:
    argv: string list that contains the command line argument.
    invocation_id: string invocation ID

  Returns:
    String list that contains bazel commands and flags.
  """
  cmd_flags = argv[1:]

  # Use the default bazel from $PATH
  cmd = ['bazel']
  # bazel use '--' to prevent the '-//foo' target be interpreted as an option.
  # any option added after '--' will treated as targets.
  if '--' not in cmd_flags:
    # Add all existing command line flags and options.
    cmd.extend(cmd_flags)
    cmd.extend(GetAuthFlags())
    cmd.extend(GetBesFlags())
    cmd.extend(GetFoundryFlags())
    cmd.append('--invocation_id=' + invocation_id)
  else:
    index = cmd_flags.index('--')
    bazel_flags = cmd_flags[:index]
    bazel_targets = cmd_flags[index:]
    cmd.extend(bazel_flags)
    cmd.extend(GetAuthFlags())
    cmd.extend(GetBesFlags())
    cmd.extend(GetFoundryFlags())
    cmd.append('--invocation_id=' + invocation_id)
    cmd.extend(bazel_targets)
  # TODO(scottzhu): Might need to check whether flag has duplications.
  return cmd


def InjectInvocationId():
  """Create an invocation ID for the bazel, and write it as an artifact.

  Kokoro will later on use that to post the bazel invocation details.

  Returns:
    String UUID to be used as invocation ID.
  """
  invocation_id = str(uuid.uuid4())
  bazel_invocation_artifacts = os.path.join(
      os.environ.get('KOKORO_ARTIFACTS_DIR'), 'bazel_invocation_ids')
  with open(bazel_invocation_artifacts, 'a') as f:
    f.write(invocation_id + '\n')

  return invocation_id


def main(argv):
  invocation_id = InjectInvocationId()
  cmd = BuildBazelCommand(argv, invocation_id)
  print('executing following commands:')
  print(cmd)
  sys.exit(subprocess.call(cmd))


if __name__ == '__main__':
  main(sys.argv)
