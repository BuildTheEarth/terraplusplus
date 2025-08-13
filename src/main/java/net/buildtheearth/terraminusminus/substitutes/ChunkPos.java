package net.buildtheearth.terraminusminus.substitutes;

import java.util.Objects;

/**
 * The position of a Chunk (a vertical 16*worldHeight*16 blocks column), in a Minecraft world.
 */
public class ChunkPos {
    public final int x, z;

    public ChunkPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

    public int getMinBlockX() {
        return cubeToMinBlock(this.x);
    }

    public int getMinBlockZ() {
        return cubeToMinBlock(this.z);
    }

    public int getMaxBlockX() {
        return cubeToMaxBlock(this.x);
    }

    public int getMaxBlockZ() {
        return cubeToMaxBlock(this.z);
    }

    public static int blockToCube(int val) {
        return val >> 4;
    }

    public static int cubeToMinBlock(int val) {
        return val << 4;
    }

    public static int cubeToMaxBlock(int val) {
        return cubeToMinBlock(val) + 15;
    }

    public static ChunkPos atBlockPos(BlockPos block) {
        return new ChunkPos(blockToCube(block.x()), blockToCube(block.z()));
    }

    @Override
    public String toString() {
        return "ChunkPos[x=" + this.x + ",z=" + this.z + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return this.x == chunkPos.x && this.z == chunkPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.z);
    }

}
