#!/bin/sh
CLASSPATH="hyperfind.jar:/usr/share/java/opendiamond.jar:lib/swingx.jar:lib/jai_imageio.jar"
BUNDLES="$HOME/.diamond/codecs:$HOME/.diamond/predicates"
FILTERS="$HOME/.diamond/filters"
java -cp $CLASSPATH edu.cmu.cs.diamond.hyperfind.Main $BUNDLES $FILTERS

