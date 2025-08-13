package net.buildtheearth.terraminusminus.substitutes;

import java.util.Objects;

/**
 * Represents the position of a block in a minecraft world.
 */
public final class BlockPos {
    public final int x, y, z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int z() {
        return this.z;
    }

    @Override
    public String toString() {
        return "BlockPos[x=" + x + ",y=" + y + ",z=" + z + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockPos blockPos = (BlockPos) o;
        return this.x == blockPos.x && this.y == blockPos.y && this.z == blockPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }

}
