# Sources

This project uses a set of online datasets, some of which require attribution. This file also provides more insight into the the data sources we use.

## [OpenStreetMap](https://www.openstreetmap.org/)

OpenSteetMap is a public database of maps built by a global community of mappers that contribute and maintain data about roads, trails, buildings, and more.

© OpenStreetMap contributors

Their data is licensed under the [Open Database License](https://en.wikipedia.org/wiki/Open_Database_License). Check their [Copyright guide](https://www.openstreetmap.org/copyright) for more information.

### [Overpass API](https://wiki.openstreetmap.org/wiki/Overpass_API)

We use the Overpass API to download data for generation. Different Overpass API instances are hosted by different companies and have different usage policies. The [default instance](https://overpass.kumi.systems/), hosted by [Kumi Systems](https://kumi.systems/), has no restrictions on usage. The instance can be changed, but make sure to follow their policies.

## [Climate](http://climate.geog.udel.edu/~climate/)

The temperature and precipitation datasets were obtained from [Willmott, Matsuura and Collaborators' Global Climate Resource Pages](http://climate.geog.udel.edu/~climate/) under the [University of Delaware](https://www.udel.edu/). They have a precision of 0.5° (30 arcminutes) and were created by interpolating the readings of many weather stations using a neural network.

These sources are provided as a service to the public and they do not guarantee that the information is correct or up to date. Here is their [disclaimer](http://climate.geog.udel.edu/~climate/html_pages/disclaimer.html).

## [Global Soil Suborder Map](https://www.nrcs.usda.gov/wps/portal/nrcs/detail/soils/use/?cid=nrcs142p2_054013)

This is a rasterized image with an accuracy of 0.033° (2 arcminutes) and is used along with climate data to estimate the biome of a region. It is provided by the [Natural Resources Conservation Service](https://www.nrcs.usda.gov/) under the United States Department of Agriculture.

## [Tree Cover Data](https://data.globalforestwatch.org/datasets/tree-cover-2000)

This dataset shows tree canopy data from around the year 2000.

> Source: Hansen/UMD/Google/USGS/NASA
>
> Hansen, M. C., P. V. Potapov, R. Moore, M. Hancher, S. A. Turubanova, A. Tyukavina, D. Thau, S. V. Stehman, S. J. Goetz, T. R. Loveland, A. Kommareddy, A. Egorov, L. Chini, C. O. Justice, and J. R. G. Townshend. 2013. “High-Resolution Global Maps of 21st-Century Forest Cover Change.” Science 342 (15 November): 850–53. Data available on-line from: http://earthenginepartners.appspot.com/science-2013-global-forest.

The [default host](https://gis-treecover.wri.org/arcgis/rest/services) is the [World Resources Institute](https://www.wri.org/) on their [ArcGis REST](https://developers.arcgis.com/rest/) [server](https://gis-treecover.wri.org/arcgis/rest/services). This may be moved to a GitHub-based file host in the future.

Their image server is used to produce TIFF files of the forest cover for a region, which is then used to guide the procedural placement of trees.

## Mapzen Terrain Tiles ([Joerd](https://github.com/tilezen/joerd/))

The [AWS Terrain Tiles](https://registry.opendata.aws/terrain-tiles/) service is used to download this data as the game runs. Their original source is Joerd, made for [Mapzen](https://www.mapzen.com/terms/), which is currently closed to new users.

These tiles are a conglomeration of several data sources, which must be [attributed](https://github.com/tilezen/joerd/blob/master/docs/attribution.md#the-fine-print).

> 3DEP, STRM, and GMTED2010 data courtesy of the U.S. Geological Survey

> DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce

> Land Information New Zeland Data: Copyright 2011 Crown copyright (c) Land Information New Zealand and the New Zealand Government. All rights reserved

> data.gov.uk LIDAR Composite Digital Terrain Model: © Environment Agency copyright and/or database right 2015. All rights reserved.

> data.gv.at Digitales Geländemodell (DGM) Österreich: © offene Daten Österreichs – Digitales Geländemodell (DGM) Österreich.

> data.kartverket.no Digital terrengmodell: © Kartverket

> Arctic Digital Elevation Model (ArcticDEM): DEM(s) were created from DigitalGlobe, Inc., imagery and funded under National Science Foundation awards 1043681, 1559691, and 1542736.

> Digital Terrain Model over Europe (EU-DEM): Produced using Copernicus data and information funded by the European Union - EU-DEM layers.

> Canadian Digital Elevation Model (CDEM): Contains information licensed under the Open Government Licence – Canada.

> National Institute of Statistics and Geography (INEGI): Source: INEGI, Continental relief, 2016

> Digital Elevation Model (DEM) of Australia derived from LiDAR 5 Metre Grid: © Commonwealth of Australia (Geoscience Australia) 2017.
