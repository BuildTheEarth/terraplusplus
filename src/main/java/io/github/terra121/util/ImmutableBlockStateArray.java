package io.github.terra121.util;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.NonNull;
import net.daporkchop.lib.binary.bit.BitArray;
import net.daporkchop.lib.binary.bit.padded.PaddedBitArray;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * A compact, immutable array of {@link IBlockState}s.
 *
 * @author DaPorkchop_
 */
public final class ImmutableBlockStateArray {
    protected final BitArray data;
    protected final IBlockState[] palette;

    public ImmutableBlockStateArray(@NonNull IBlockState[] data) {
        Reference2IntMap<IBlockState> stateIds = new Reference2IntOpenHashMap<>();
        List<IBlockState> paletteBuilder = new ArrayList<>();
        int idCounter = 0;

        for (IBlockState state : data) {
            if (!stateIds.containsKey(state)) { //add state to palette
                stateIds.put(state, idCounter++);
                paletteBuilder.add(state);
            }
        }

        this.palette = paletteBuilder.toArray(new IBlockState[0]);
        this.data = new PaddedBitArray(max(32 - Integer.numberOfLeadingZeros(idCounter - 1), 1), data.length);

        for (int i = 0; i < data.length; i++) { //set values
            this.data.set(i, stateIds.get(data[i]));
        }
    }

    /**
     * Gets the {@link IBlockState} at the given index.
     *
     * @param i the index
     * @return {@link IBlockState}
     */
    public IBlockState get(int i) {
        return this.palette[this.data.get(i)];
    }
}
