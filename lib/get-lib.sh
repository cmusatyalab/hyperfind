#!/bin/sh

cd "$(dirname "$0")" || exit

[ -e jai_imageio.jar ] || wget -nv https://github.com/jai-imageio/jai-imageio-core/releases/download/jai-imageio-core-1.3.1/jai-imageio-core-1.3.1.jar -O jai_imageio.jar
[ -e swingx.jar ] || wget -nv http://repo1.maven.org/maven2/org/swinglabs/swingx/swingx-all/1.6.4/swingx-all-1.6.4.jar -O swingx.jar

md5sum -c MD5SUMS
