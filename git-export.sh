#!/bin/sh

set -e

if [ -z "$1" ] ; then
	echo "Usage: $0 <version>" >&2
	exit 1
fi

b="hyperfind"

tag="v$1"

git archive --format tar "$tag" "--prefix=$b-$1/" -o "${b}_$1.orig.tar"
gzip -9f "${b}_$1.orig.tar"
