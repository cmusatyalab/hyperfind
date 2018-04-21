#!/bin/sh
CLASSPATH="hyperfind.jar:/usr/share/java/opendiamond.jar:lib/swingx.jar:lib/jai_imageio.jar:lib/gson.jar:lib/commons-io-2.6/*"
BUNDLES="$HOME/.diamond/codecs:$HOME/.diamond/predicates:/usr/local/share/diamond/codecs:/usr/local/share/diamond/predicates"
FILTERS="$HOME/.diamond/filters:/usr/local/share/diamond/filters"
java -cp $CLASSPATH edu.cmu.cs.diamond.hyperfind.Main $BUNDLES $FILTERS

