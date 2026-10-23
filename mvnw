#!/bin/sh
# Apache Maven Wrapper 3.3.4 — see https://maven.apache.org/wrapper/
set -euf
[ "${MVNW_VERBOSE-}" != debug ] || set -x

native_path() { printf %s\\n "$1"; }
case "$(uname)" in
CYGWIN* | MINGW*)
  [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  native_path() { cygpath --path --windows "$1"; }
  ;;
esac

set_java_home() {
  if [ -n "${JAVA_HOME-}" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
    JAVACCMD="$JAVA_HOME/bin/javac"
    if [ ! -x "$JAVACMD" ] || [ ! -x "$JAVACCMD" ]; then
      echo "JAVA_HOME is not defined correctly." >&2
      return 1
    fi
  else
    JAVACMD="$(command -v java)" || :
    JAVACCMD="$(command -v javac)" || :
    if [ ! -x "${JAVACMD-}" ] || [ ! -x "${JAVACCMD-}" ]; then
      echo "java/javac not found in PATH and JAVA_HOME is unset." >&2
      return 1
    fi
  fi
}

hash_string() {
  str="${1:-}" h=0
  while [ -n "$str" ]; do
    char="${str%"${str#?}"}"
    h=$(((h * 31 + $(LC_CTYPE=C printf %d "'$char")) % 4294967296))
    str="${str#?}"
  done
  printf %x\\n $h
}

trim() { printf "%s" "${1}" | tr -d '[:space:]'; }

scriptDir="$(dirname "$0")"
scriptName="$(basename "$0")"

while IFS="=" read -r key value; do
  case "${key-}" in
  distributionUrl) distributionUrl=$(trim "${value-}") ;;
  distributionSha256Sum) distributionSha256Sum=$(trim "${value-}") ;;
  esac
done <"$scriptDir/.mvn/wrapper/maven-wrapper.properties"
[ -n "${distributionUrl-}" ] || { echo "cannot read distributionUrl" >&2; exit 1; }

MVN_CMD="mvn${scriptName#mvnw}"
_MVNW_REPO_PATTERN=/org/apache/maven/
[ -z "${MVNW_REPOURL-}" ] || distributionUrl="$MVNW_REPOURL$_MVNW_REPO_PATTERN${distributionUrl#*"$_MVNW_REPO_PATTERN"}"
distributionUrlName="${distributionUrl##*/}"
distributionUrlNameMain="${distributionUrlName%.*}"
distributionUrlNameMain="${distributionUrlNameMain%-bin}"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
MAVEN_HOME="${MAVEN_USER_HOME}/wrapper/dists/${distributionUrlNameMain-}/$(hash_string "$distributionUrl")"

exec_maven() {
  unset MVNW_VERBOSE MVNW_USERNAME MVNW_PASSWORD MVNW_REPOURL || :
  exec "$MAVEN_HOME/bin/$MVN_CMD" "$@"
}

if [ -d "$MAVEN_HOME" ]; then
  exec_maven "$@"
fi

if TMP_DOWNLOAD_DIR="$(mktemp -d)" && [ -d "$TMP_DOWNLOAD_DIR" ]; then
  clean() { rm -rf -- "$TMP_DOWNLOAD_DIR"; }
  trap clean HUP INT TERM EXIT
else
  echo "cannot create temp dir" >&2
  exit 1
fi

mkdir -p -- "${MAVEN_HOME%/*}"
if command -v curl >/dev/null; then
  curl -fsSL -o "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl"
elif command -v wget >/dev/null; then
  wget -q -O "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl"
else
  echo "curl or wget is required to download Maven" >&2
  exit 1
fi

if command -v unzip >/dev/null; then
  unzip -q "$TMP_DOWNLOAD_DIR/$distributionUrlName" -d "$TMP_DOWNLOAD_DIR"
else
  tar xzf "$TMP_DOWNLOAD_DIR/$distributionUrlName" -C "$TMP_DOWNLOAD_DIR"
fi

mv -- "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" "$MAVEN_HOME" || [ -d "$MAVEN_HOME" ]
clean || :
exec_maven "$@"
