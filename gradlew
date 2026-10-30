#!/bin/sh

# Gradle startup script for POSIX systems
set -e

APP_BASE_NAME=${0##*/}
APP_HOME=$(cd "$(dirname "$0")" && pwd)

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
else
    JAVACMD=java
fi

exec "$JAVACMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || {
    echo "Starting Gradle Wrapper..."
    # Fallback to system gradle if jar not yet downloaded
    exec gradle "$@"
}
