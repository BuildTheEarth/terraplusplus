<div align="center">
<h1>TerraMinusMinus</h1>
</div>

[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?style=flat-square)](https://github.com/BuildTheEarth/terraplusplus/blob/master/LICENSE.MD)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SmylerMC/terraminusminus/Java%20CI%20with%20Gradle?style=flat-square)

## What is it?

Terra-- is a fork of Terra++ intended to strip down dependencies to Minecraft and Forge so it can be used safely in as dependency for other projects (this is not a mod).

## How to use ?

### :warning: This project is still experimental

Just add Jitpack as a maven repository to your `build.gradle`, as well as the repositories needed for Terra--'s dependencies, and declare this repository as a dependency to your own project.

E.g. :
```
repositories {
    
    // Classic JCenter repository that has most of what we need
    jcenter()
    
    // JitPack will build this repo and provide it as a dependency
    maven {
        name = "JitPack"
        url = "https://jitpack.io/"
    }
    
    // DaPorchop's repo for PorkLib
    maven {
        name = "DaPorkchop_"
        url = "https://maven.daporkchop.net/"
    }
    
    // This is for leveldb
    maven {
        name = "OpenCollab Snapshots"
        url = "https://repo.opencollab.dev/snapshot/"
    }
    
}

dependencies {

    // Include this repository as a dependency through Jitpack
    // master-SNAPSHOT indicates to use the latest commit on master,
    // you can replace this with a commit hash or a reference to anoter branch 
    compile 'com.github.SmylerMC:terraminusminus:master-SNAPSHOT'
    
    
    // Your other depencencies would go down there...
    
}
```

## APIs:

- Tree cover data: [treecover2000 v1.7](https://earthenginepartners.appspot.com/science-2013-global-forest/download_v1.7.html) hosted by [@DaPorkchop_](https://github.com/DaMatrix)
- Building+Road+Water data: [OpenStreetMap](https://www.openstreetmap.org/) under the [Open Database License](https://www.openstreetmap.org/copyright). It is downloaded in real-time using [TerraPlusPlusOSMTileGen](https://github.com/DaMatrix/TerraPlusPlusOSMTileGen) hosted by [@DaPorkchop_](https://github.com/DaMatrix). (© OpenStreetMap contributors)
