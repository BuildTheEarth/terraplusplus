package net.buildtheearth.terraminusminus.substitutes;

/**
 * The position of a Chunk (a vertical 16*worldHeight*16 blocks column), in a Minecraft world.
 */
public record ChunkPos(int x, int z) {

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

}
