#!/usr/bin/env bash

set -e

# Default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_HOME="$$   (cd "   $$(dirname "$0")" && pwd -P)"

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

JAVACMD=java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"