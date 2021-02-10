package net.buildtheearth.terraplusplus.util;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A run-length-encoded byte array with random access.
 *
 * @author DaPorkchop_
 */
public class RLEByteArray {
    public static Builder builder() {
        return new Builder();
    }

    protected final int[] index;
    protected final byte[] data;
    @Getter
    protected final int size;

    protected RLEByteArray(@NonNull IntList index, @NonNull ByteList data, int size) {
        this.index = index.toIntArray();
        this.data = data.toByteArray();
        this.size = size;
    }

    public byte get(int i) {
        checkIndex(this.size, i);

        i = Arrays.binarySearch(this.index, i);
        int mask = i >> 31;
        i = (i ^ mask) + mask;

        return this.data[i];
    }

    public static class Builder {
        protected final IntList index = new IntArrayList();
        protected final ByteList data = new ByteArrayList();
        protected int size = 0;
        protected byte last;

        /**
         * Appends a value to this builder.
         * @param val the value to append
         */
        public void append(byte val) {
            int size = this.size++;
            if (size == 0 || this.last != val) {
                this.index.add(size);
                this.data.add(this.last = val);
            }
        }

        public RLEByteArray build() {
            return new RLEByteArray(this.index, this.data, this.size);
        }
    }
}