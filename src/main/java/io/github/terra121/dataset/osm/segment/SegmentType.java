package io.github.terra121.dataset.osm.segment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
@Getter
public enum SegmentType {
    IGNORE(SegmentFill.NONE, null),
    STREAM(SegmentFill.NARROW, Blocks.WATER.getDefaultState()),
    RIVER(SegmentFill.WIDE, Blocks.WATER.getDefaultState()) {
        @Override
        public double computeRadius(int lanes) {
            return 5.0d;
        }
    },
    ROAD(SegmentFill.NARROW, Blocks.GRASS_PATH.getDefaultState()),
    MINOR(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) {
        @Override
        public double computeRadius(int lanes) {
            return lanes;
        }
    },
    SIDE(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) {
        @Override
        public double computeRadius(int lanes) {
            return (3 * lanes + 1) >> 1;
        }
    },
    MAIN(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) {
        @Override
        public double computeRadius(int lanes) {
            return lanes << 1;
        }
    },
    INTERCHANGE(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) {
        @Override
        public double computeRadius(int lanes) {
            return (3 * lanes) >> 1;
        }
    },
    LIMITEDACCESS(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) {
        @Override
        public double computeRadius(int lanes) {
            return ((6 * lanes) >> 1) + 2;
        }
    },
    FREEWAY(SegmentFill.WIDE, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) {
        @Override
        public double computeRadius(int lanes) {
            return ((6 * lanes) >> 1) + 2;
        }
    },
    BUILDING(SegmentFill.NARROW, Blocks.BRICK_BLOCK.getDefaultState()),
    RAIL(SegmentFill.NONE, null);

    @NonNull
    private final SegmentFill fillType;
    private final IBlockState state;

    public double computeRadius(int lanes) {
        return 1.0d;
    }
}
