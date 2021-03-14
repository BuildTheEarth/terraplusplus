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
}
