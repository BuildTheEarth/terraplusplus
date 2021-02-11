<img src="https://github.com/BuildTheEarth/terraplusplus/blob/master/docs/images/terraplusplus_logo.png" width="500" height="174">

### Developed by the BuildTheEarth Development Team

### Fork of the [Terra 1 to 1](https://github.com/orangeadam3/terra121) project and submod of [CubicChunks](https://github.com/OpenCubicChunks/CubicChunks) and [CubicWorldGen](https://github.com/OpenCubicChunks/CubicWorldGen/).

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

## Permissions
- `terraplusplus.admin` - Gives access to all terra++ features and commands
- `terraplusplus.commands.terra` - Gives access to `/terra`
- `terraplusplus.commands.terra.utilities` - Gives access to terra++ utilities
- `terraplusplus.commands.tpll` - Gives access to `/tpll`

## APIs:

- Elevation data: [AWS Terrain Tiles](https://registry.opendata.aws/terrain-tiles/). (© [Mapzen](https://www.mapzen.com/rights), and [others](https://github.com/tilezen/joerd/blob/master/docs/attribution.md))
- Tree cover data: [ARCGIS REST TreeCover2000 Image Server hosted by the World Resources Institute](https://gis-treecover.wri.org/arcgis/rest/services/TreeCover2000/ImageServer)
- Road data: [OpenStreetMap](https://www.openstreetmap.org/) under the [Open Database License](https://www.openstreetmap.org/copyright). It is downloaded in real-time using a public [Overpass API](http://overpass-api.de/) instance. (© OpenStreetMap contributors)
- Climate data: [The University of Delaware Center for Climatic Research's Climate Data Archive](http://climate.geog.udel.edu/~climate/html_pages/archive.html)
- Soil suborder data: [USDA Natural Resources Conservation Service's Global Soil Region Map](https://www.nrcs.usda.gov/wps/portal/nrcs/detail/soils/use/?cid=nrcs142p2_054013)

### [More comprehensive copyright and source description](SOURCES.md)

## Prerequisites

- **REQUIRED**: [Minecraft Forge](https://files.minecraftforge.net/)
- **REQUIRED**: [CubicChunks](https://github.com/OpenCubicChunks/CubicChunks/)
- **REQUIRED**: [CubicWorldGen](https://github.com/OpenCubicChunks/CubicWorldGen/)

## Obtaining
Releases will not be found from within GitHub, but rather on our [Jenkins Continuous Integration server](https://jenkins.daporkchop.net/job/BuildTheEarth) kindly provided by DaPorkchop. Everything is automatically built and compiled for every single commit, so you can always snag the latest release.
### [Latest Release Download Link](https://jenkins.daporkchop.net/job/BuildTheEarth/job/terraplusplus/job/master/lastSuccessfulBuild/artifact/build/libs/)

If you want to build it yourself manually, even though Jenkins does it for you, here are the [building instructions](BUILD_INSTRUCTIONS.md).
