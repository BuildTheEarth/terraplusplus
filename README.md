<img src="https://github.com/BuildTheEarth/terraplusplus/blob/master/docs/images/terraplusplus_logo.png" width="500" height="174">


[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?)](https://github.com/BuildTheEarth/terraplusplus/blob/master/LICENSE.MD)
[![GitHub Release](https://img.shields.io/github/release/buildtheearth/terraplusplus.svg?style=flat)](https://github.com/BuildTheEarth/terraplusplus/releases)
[![Discord Chat](https://img.shields.io/discord/706317564904472627.svg)](https://discord.gg/BGpmp3sfH5)  

## What is it?

Terra++ is a fork of Terra 1 to 1 (a mod that utilizes public online datasets to generate the Earth's structures and features without any unfamiliar blocks or biomes at a 1 to 1 scale) which aims to greatly improve the performance, fix bugs and add new, useful and fast features to the original mod. 

The mod is constantly being worked on and being updated as we add more features and fine tune the mod to achieve a better connection between the real world and the world of blocks.

The latest version can always be compiled from [our Github](https://github.com/BuildTheEarth/terraplusplus), however a version of it will always be included with the BTE modpack, and we'll do our best to keep it up to date.

## Features

- Major roads, minor roads and Grass Pathways from [OpenStreetMap](https://www.openstreetmap.org/)
- Elevation and biome placement using public terrain datasets.
- Procedural Tree placement
- Oceans based on elevation with depth data
- "Customize World" GUI that does not require MalisisCore
- Teleport by latitude and longitude using `/tpll latitude longitude [elevation]`
- Ore spawning just below surface
- Caves, Dungeons, and other standard underground features are still available and procedurally generated using CubicWorldGen as a baseline. Though, they are left disabled by default unless modified in the generator settings.

## Commands and Permissions
- [Click Here](https://github.com/BuildTheEarth/terraplusplus/wiki/Commands) to view a list of all commands and permissions

## APIs:

- Tree cover data: [ARCGIS REST TreeCover2000 Image Server hosted by the World Resources Institute](https://gis-treecover.wri.org/arcgis/rest/services/TreeCover2000/ImageServer) hosted by [@daPorkchop_](https://github.com/DaMatrix)
- Road data: [OpenStreetMap](https://www.openstreetmap.org/) under the [Open Database License](https://www.openstreetmap.org/copyright). It is downloaded in real-time using [TerraPlusPlusOSMTileGen](https://github.com/DaMatrix/TerraPlusPlusOSMTileGen) hosted by [@daPorkchop_](https://github.com/DaMatrix). (© OpenStreetMap contributors)

## Prerequisites

- **REQUIRED**: [Minecraft Forge](https://files.minecraftforge.net/)
- **REQUIRED**: [CubicChunks](https://github.com/OpenCubicChunks/CubicChunks/)
- **REQUIRED**: [CubicWorldGen](https://github.com/OpenCubicChunks/CubicWorldGen/)

## Obtaining
Releases will not be found from within GitHub, but rather on our [Jenkins Continuous Integration server](https://jenkins.daporkchop.net/job/BuildTheEarth) kindly provided by DaPorkchop. Everything is automatically built and compiled for every single commit, so you can always snag the latest release.
### [Latest Release Download Link](https://jenkins.daporkchop.net/job/BuildTheEarth/job/terraplusplus/job/master/lastSuccessfulBuild/artifact/build/libs/)

If you want to build it yourself manually, even though Jenkins does it for you, here are the [building instructions](BUILD_INSTRUCTIONS.md).
