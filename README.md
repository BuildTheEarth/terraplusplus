<div align="center">
    <h1>TerraMinusMinus</h1>
    <img alt="GitHub License" src="https://img.shields.io/github/license/SmylerMC/terraminusminus?style=flat-square">
    <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/SmylerMC/terraminusminus/test.yml?style=flat-square">
</div>



## What is it?

Terra-- is a fork of Terra++ intended to strip down dependencies to Minecraft and Forge so it can be used safely in as dependency for other projects (this is not a mod).

In particular, it is ships the map projection code as well as the world generation engine and its HTTP client.

## How to use it in your project?

Terra-- is available at [maven.smyler.net](https://maven.smyler.net/releases).
You need to add that repository in your `pom.xml` or `build.gradle` build files,
and then declare 
Just add the required maven repositories to your `build.gradle`, and declare Terraminusminus as a dependency.

I.e.:
```groovy
repositories {

    // Smyler's repository has the Terraminusminus builds
    maven {
        name = "Smyler Snapshots"
        url = "https://maven.smyler.net/snapshots/"
    }
    
    // Classic JCenter repository that has most of what we need
    jcenter()
    
}

dependencies {

    // Include this repository as a dependency.
    // master-SNAPSHOT indicates to use the last commit built from master,
    // you can replace this with  a reference to another branch 
    compile 'net.buildtheart.terraminusminus:terraminusminus-core:master-SNAPSHOT'

    // Alternatively, if you want to include compatibility code to make Terra-- work with other projects,
    // you can include the relevant module (or multiple of them).
    // The terraminusminus-core module will be included as a transitive dependency.
    // For example, to include Bukkit API compatibility code:
    compile 'net.buildtheart.terraminusminus:terraminusminus-bukkit:master-SNAPSHOT'
    
    // Your other dependencies would go down there...
}
```

### Versioning

Terra-- versions are split into two parts, the first of which represents the semver-compliant Terra-- API version and the second the Minecraft content compatibility version.

For example, version `2.0.0-1.21.4` implements version `2.0.0` of the Terra-- API and aims to be compatible with Minecraft `1.21.4`.

The Minecraft compatibility version influences the content of the default data files (e.g. [`osm.json5`](src/main/resources/net/buildtheearth/terraminusminus/dataset/osm/osm.json5)),
as well as the versions of dependencies Terra-- has in common with Minecraft (e.g. Gson).


Snapshot builds are available at [maven.smyler.net](https://maven.smyler.net/#/snapshots/net/buildtheearth/terraminusminus).

## APIs:

- Tree cover data: [treecover2000 v1.7](https://earthenginepartners.appspot.com/science-2013-global-forest/download_v1.7.html) hosted by [@DaPorkchop_](https://github.com/DaMatrix)
- Building+Road+Water data: [OpenStreetMap](https://www.openstreetmap.org/) under the [Open Database License](https://www.openstreetmap.org/copyright). It is downloaded in real-time using [TerraPlusPlusOSMTileGen](https://github.com/DaMatrix/TerraPlusPlusOSMTileGen) hosted by [@DaPorkchop_](https://github.com/DaMatrix). (© OpenStreetMap contributors)
