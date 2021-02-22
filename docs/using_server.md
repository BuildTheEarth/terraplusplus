# Server setup instructions

Setup a Forge server (which can be installed through the [standard Forge installer](http://files.minecraftforge.net/)), and install the CubicChunks, CubicWorldGen, and Terra++ jars to the `/mods/` directory.

Inside of the `server.properties` file, change:

```properties
level-type=default
```

to

```properties
level-type=EarthCubic
```

And change the default world name ("world"):

```properties
level-name=world
```

To something else, so that a new world is generated with Terra++.

## Recommended

You should now have a Terra++ server loaded and generating properly. But to limit load on the server, change:

```properties
view-distance=16
```

And:

```properties
vertical-view-distance=-1
```

To a number of chunks that your server can handle, and re-run your server (using `-1` is whatever the player's vertical distance is, for all players), you may need to tweak these numbers until you get a stable server setting.

## Allocating RAM

We also recommend launching your Forge server with more RAM in the JVM arguments:

    <JAVA_HOME>/bin/java -Xmx#### -jar forge-<MCVERSION>-<FORGEVERSION>.jar

Here, replace `####` with the amount you need. Remember to use a capital letter (`G` instead of `g`).

-   `-Xmx1G`: 1 gigabyte
-   `-Xmx8G`: 8 gigabytes
-   `-Xmx16G`: 16 gigabytes
-   ...

Join the server with the required mods. If you end up spawning on the mushroom island, then you have properly loaded Terra++ on your server.
