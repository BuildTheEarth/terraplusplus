package net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math;

public class BlockPos {
	
	public final int x, y, z;

	public BlockPos(int x, int y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getZ() {
		return this.z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (!(other instanceof BlockPos)) return false;
		BlockPos otherBlockPos = (BlockPos) other;
		if (x != otherBlockPos.x || y != otherBlockPos.y || z != otherBlockPos.z) return false;
		return true;
	}
	
	
}
