package net.buildtheearth.terraplusplus.util;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.minecraft.util.math.ChunkPos;

/**
 * Representation of a tile position (a 2D position and a zoom level).
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
@Getter
@ToString
public class TilePos {
    protected final int x;
    protected final int z;
    protected final int zoom;

    public TilePos(@NonNull ChunkPos src) {
        this(src.x, src.z, 0);
    }

    public TilePos(@NonNull CubePos src) {
        this(src.getX(), src.getZ(), 0);
    }

    public int blockX() {
        return Coords.cubeToMinBlock(this.x << this.zoom);
    }

    public int blockZ() {
        return Coords.cubeToMinBlock(this.z << this.zoom);
    }

    public int sizeBlocks() {
        return Coords.cubeToMinBlock(1 << this.zoom);
    }

    @Override
    public int hashCode() {
        return (int) (((3241L * 3457689L + this.x) * 8734625L + this.z) * 2873465L + this.zoom);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TilePos) {
            TilePos pos = (TilePos) obj;
            return this.x == pos.x && this.z == pos.z && this.zoom == pos.zoom;
        } else {
            return false;
        }
    }
}
