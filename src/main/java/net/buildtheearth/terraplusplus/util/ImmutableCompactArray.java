package net.buildtheearth.terraplusplus.util;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.NonNull;
import net.daporkchop.lib.binary.bit.BitArray;
import net.daporkchop.lib.binary.bit.padded.PaddedBitArray;

/**
 * A compact, immutable array.
 *
 * @author DaPorkchop_
 */
public final class ImmutableCompactArray<T> {
    protected final BitArray data;
    protected final T[] palette;

    public ImmutableCompactArray(@NonNull T[] data) {
        Reference2IntMap<T> valueIds = new Reference2IntOpenHashMap<>();
        List<T> paletteBuilder = new ArrayList<>();
        int idCounter = 0;

        for (T value : data) {
            if (!valueIds.containsKey(value)) { //add value to palette
                valueIds.put(value, idCounter++);
                paletteBuilder.add(value);
            }
        }

        this.palette = paletteBuilder.toArray(Arrays.copyOf(data, paletteBuilder.size()));
        this.data = new PaddedBitArray(max(32 - Integer.numberOfLeadingZeros(idCounter - 1), 1), data.length);

        for (int i = 0; i < data.length; i++) { //set values
            this.data.set(i, valueIds.get(data[i]));
        }
    }

    /**
     * Gets the value at the given index.
     *
     * @param i the index
     * @return the value
     */
    public T get(int i) {
        return this.palette[this.data.get(i)];
    }
}
