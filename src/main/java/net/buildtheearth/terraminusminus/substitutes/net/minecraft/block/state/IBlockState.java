package net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.state;

import java.util.Collection;

import com.google.common.collect.ImmutableMap;

import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.Block;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.properties.IProperty;

public interface IBlockState {
	
    Collection <IProperty<?>> getPropertyKeys();

    <T extends Comparable<T>> T getValue(IProperty<T> property);

    <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value);

    <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property);

    ImmutableMap <IProperty<?>, Comparable<? >> getProperties();

    Block getBlock();
    
//   IBlockState withRotation(Rotation rot); TODO

//    IBlockState withMirror(Mirror mirrorIn); TODO

//    IBlockState getActualState(IBlockAccess blockAccess, BlockPos pos); TODO
    
//    MapColor getMapColor(IBlockAccess access, BlockPos pos); TODO
    
}