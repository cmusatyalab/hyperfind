#!/bin/sh

cd "$(dirname "$0")" || exit

[ -e jai_imageio.jar ] || wget -nv https://github.com/jai-imageio/jai-imageio-core/releases/download/jai-imageio-core-1.3.1/jai-imageio-core-1.3.1.jar -O jai_imageio.jar
[ -e swingx.jar ] || wget -nv http://repo1.maven.org/maven2/org/swinglabs/swingx/swingx-all/1.6.4/swingx-all-1.6.4.jar -O swingx.jar
[ -e gson.jar ] || wget -nv http://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.1/gson-2.8.1.jar -O gson.jar
[ -d commons-io-2.6 ] || wget -nv http://mirrors.ibiblio.org/apache//commons/io/binaries/commons-io-2.6-bin.tar.gz
md5sum -c MD5SUMS

[ -d commons-io-2.6 ] || tar xzf commons-io-2.6-bin.tar.gz
