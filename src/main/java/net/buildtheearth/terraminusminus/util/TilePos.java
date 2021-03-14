package net.buildtheearth.terraminusminus.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

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
