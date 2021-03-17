package net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math;

public class ChunkPos {
	
	public final int x, z;
	
	public ChunkPos(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public ChunkPos(BlockPos block) {
		this(blockToCube(block.x), blockToCube(block.z));
	}
	
	public int getPosX() {
		return this.x;
	}
	
	public int getPosZ() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if(!(other instanceof ChunkPos)) return false;
		ChunkPos otherChunkPos = (ChunkPos) other;
		if (x != otherChunkPos.x || z != otherChunkPos.z) return false;
		return true;
	}
    
    

}
