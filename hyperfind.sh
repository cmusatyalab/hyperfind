#!/bin/sh
echo using bundle dirs: ${BUNDLES:="$HOME/.diamond/codecs:$HOME/.diamond/predicates:/usr/local/share/diamond/codecs:/usr/local/share/diamond/predicates"}
echo using filters dir: ${FILTERS:="$HOME/.diamond/filters:/usr/local/share/diamond/filters"}
java -jar $(dirname "$0")/hyperfind.jar $BUNDLES $FILTERS
