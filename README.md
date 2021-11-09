# HyperFind

HyperFind is a GUI client application for performing interactive searches on non-indexed data in the [OpenDiamond](http://diamond.cs.cmu.edu/) system. It is primarily designed to search images but can also be used for other data types.

## Compile from Source

Clone and compile:
```bash
# Create directory used by hyperfind
mkdir -p $HOME/.diamond

git clone https://github.com/cmusatyalab/hyperfind.git
cd hyperfind
```

To run a search:
```bash
./gradlew run
```


## Other Artifacts for Running HyperFind

### ScopeCookie

`$HOME/.diamond/NEWSCOPE`: a [ScopeCookie](https://github.com/cmusatyalab/opendiamond/wiki/ScopeCookie) typically 
generated by a scope server or by the `cookiecutter` script from [OpenDiamond](http://diamond.cs.cmu.edu/). It contains information about the dataset to be searched and the backend servers.

### Codecs, Predicates and Filters

HyperFind looks for these in the following directories:

+ `/usr/share/diamond/{codecs,predicates,filters}`
+ `/usr/local/share/diamond/{codecs,predicates,filters}`
+ `$HOME/.diamond/{codecs,predicates,filters}`

Typically, codecs/predicates/filters come in a bundle developed separately.

*For definitions of codec, predicate and filter, see https://github.com/cmusatyalab/opendiamond/wiki/StructureOfDiamond.*


## Tips

### Viewing ScopeCookie
[Recommended] Install opendiamond-scope locally with pipx:

```bash
pip install --user pipx             # not needed if pipx is already installed
export PATH=$HOME/.local/bin:$PATH  # make sure to add this to .bashrc as well
pipx install opendiamond-scope
```

You can then view the contents of the scope cookie with

```bash
opendiamond-scope verify $HOME/.diamond/NEWSCOPE
```

### Auto placing ScopeCookie (Linux Only)
Install opendiamond-scope on your computer to install system hooks that automatically rename and place
the ScopeCookie properly when you double-click the downloaded file from the browser.

```bash
opendiamond-scope install
```

### Obtaining filters
The common type of downloadable filters (short for saying codecs/predicates/filters) is
a tar ball containing the directories `codecs/`, `predicates/` and `filters/`.
Decompress the tar ball and place files under `$HOME/.diamond/` as mentioned above.

### Running HyperFind remotely with X11 forwarding
It has been successful to run HyperFind in a Linux host and forward the GUI to Windows/MacOS.
To do so, you should enable X11 forwarding in your SSH connection and install a X11 server on your OS
(e.g., Xming on Windows, Xquartz on MacOS).

### Hyperfind Configuration Settings
The OpenDiamond connector specific configuration settings are stored locally in `$HOME/.diamond/hyperfind-diamond.properties`.

```
#hyperfind settings for diamond connector
useProxy=false          # doesn't actually seem to get used...
downloadResults=false   # doesn't actually seem to get used either...
# proxyIP=              # if defined used when creating a new search scope
# downloadDirectory=    # defaults to user.home system property
```

### Depending on a local build of OpenDiamond-Java

Option 1: Use Local Maven

- Publish your local copy to by running `./gradlew publishToMavenLocal` in the opendiamond-java repo
- Uncomment the `mavenLocal()` line in build.gradle in this repository
- Update the version of the `edu.cmu.cs.diamond.opendiamond:opendiamond-java` dependency in the version.props file in this repository to that of your local copy

Option 2: Directly use build directory

- Build your local copy to by running `./gradlew build` in the opendiamond-java repo
- Uncomment the last two lines in the dependency block in connection-diamond/build.gradle
