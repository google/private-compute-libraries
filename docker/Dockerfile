FROM marketplace.gcr.io/google/ubuntu2004

# Install helper commands
RUN apt-get update && apt-get install -y \
    gcc \
    python3 \
    unzip \
    wget

# Set up Java
RUN apt-get update && apt-get install default-jdk -y --no-install-recommends
ENV JAVA_HOME /usr/lib/jvm/default-java

# Set up baselisk
ENV USE_BAZEL_VERSION=6.0.0
RUN wget https://github.com/bazelbuild/bazelisk/releases/download/v1.15.0/bazelisk-linux-amd64 \
    -O /usr/local/bin/bazel && chmod a+x /usr/local/bin/bazel

# Set up buildifier
RUN wget https://github.com/bazelbuild/buildtools/releases/download/5.1.0/buildifier-linux-amd64 \
    -O /usr/local/bin/buildifier && chmod a+x /usr/local/bin/buildifier

# Set up Android
ENV ANDROID_HOME /opt/android-sdk
ENV PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:/opt/android-studio/bin"

# Install SDK tools
RUN cd /opt
RUN curl -OL https://dl.google.com/android/repository/commandlinetools-linux-9123335_latest.zip
RUN mkdir /opt/android-sdk
RUN unzip commandlinetools-linux-9123335_latest.zip -d "${ANDROID_HOME}"
RUN rm commandlinetools-linux-9123335_latest.zip

# Create latest folder and move everything inside :
# https://developer.android.com/studio/command-line/sdkmanager
RUN mkdir /opt/android-sdk/tmp
RUN mv /opt/android-sdk/cmdline-tools/* /opt/android-sdk/tmp/
RUN mkdir /opt/android-sdk/cmdline-tools/latest
RUN mv /opt/android-sdk/tmp/* /opt/android-sdk/cmdline-tools/latest/
RUN rmdir /opt/android-sdk/tmp

# Accept licenses before installing components, no need to echo y for each component
# License is valid for all the standard components in versions installed from this file
# Non-standard components: MIPS system images, preview versions, GDK (Google Glass) and Android Google TV require separate licenses, not accepted there
RUN yes | sdkmanager --licenses

# Install platform tools, SDK platform, and other build tools
RUN yes | sdkmanager "platform-tools"
RUN yes | sdkmanager \
  "platforms;android-31" \
  "build-tools;31.0.0"
