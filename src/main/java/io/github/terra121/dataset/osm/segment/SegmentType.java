package io.github.terra121.dataset.osm.segment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

import java.util.EnumSet;
import java.util.Set;

import static java.lang.Math.*;

@AllArgsConstructor
@Getter
public enum SegmentType {
    IGNORE(SegmentFill.NONE, null, false),
    STREAM(SegmentFill.NARROW, Blocks.WATER.getDefaultState(), false),
    RIVER(SegmentFill.WIDE, Blocks.WATER.getDefaultState(), false) {
        @Override
        public double computeRadius(int lanes) {
            return 5.0d;
        }
    },
    ROAD(SegmentFill.NARROW, Blocks.GRASS_PATH.getDefaultState(), true),
    MINOR(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true) {
        @Override
        public double computeRadius(int lanes) {
            return lanes;
        }
    },
    SIDE(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true) {
        @Override
        public double computeRadius(int lanes) {
            return (3 * lanes + 1) >> 1;
        }
    },
    MAIN(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true) {
        @Override
        public double computeRadius(int lanes) {
            return lanes << 1;
        }
    },
    INTERCHANGE(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true) {
        @Override
        public double computeRadius(int lanes) {
            return (3 * max(2, lanes)) >> 1;
        }
    },
    LIMITEDACCESS(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true) {
        @Override
        public double computeRadius(int lanes) {
            return ((6 * lanes) >> 1) + 2;
        }
    },
    FREEWAY(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true) {
        @Override
        public double computeRadius(int lanes) {
            return ((6 * lanes) >> 1) + 2;
        }
    },
    BUILDING(SegmentFill.NARROW, Blocks.BRICK_BLOCK.getDefaultState(), true),
    RAIL(SegmentFill.NONE, null, true);

    public static final Set<SegmentType> USABLE_TYPES = EnumSet.complementOf(EnumSet.of(IGNORE, RAIL));
    public static final Set<SegmentType> NOT_BUILDING_TYPES = EnumSet.complementOf(EnumSet.of(BUILDING));
    public static final Set<SegmentType> NOT_WATER_TYPES = EnumSet.complementOf(EnumSet.of(STREAM, RIVER));
    public static final Set<SegmentType> NOT_ROAD_TYPES = EnumSet.of(BUILDING, STREAM, RIVER);

    @NonNull
    private final SegmentFill fillType;
    private final IBlockState state;
    private final boolean allowInWater;

    public double computeRadius(int lanes) {
        return 1.0d;
    }
}
