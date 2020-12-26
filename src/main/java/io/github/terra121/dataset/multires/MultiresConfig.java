package io.github.terra121.dataset.multires;

import com.google.gson.GsonBuilder;
import io.github.terra121.util.MathUtils;
import io.github.terra121.util.bvh.BVHi;
import io.github.terra121.util.bvh.Bounds2i;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class MultiresConfig {
    protected final BVHi<WrappedUrl> allUrls;
    protected final Int2ObjectMap<BVHi<WrappedUrl>> urlsByLevel = new Int2ObjectOpenHashMap<>();

    protected final int minZoom;
    protected final int maxZoom;

    public MultiresConfig(@NonNull InputStream src) throws IOException {

        List<TempWrappedUrl> tempUrlsIn;
        try (Reader reader = new UTF8FileReader(src)) {
            tempUrlsIn = Arrays.asList(new GsonBuilder().setLenient().create().fromJson(reader, TempWrappedUrl[].class));
        }

        checkState(!tempUrlsIn.isEmpty(), "no datasets found in \"%s\"", src);

        this.maxZoom = tempUrlsIn.stream().mapToInt(TempWrappedUrl::zoom).max().getAsInt();

        NavigableMap<Integer, List<WrappedUrl>> urlsIn = tempUrlsIn.stream()
                    .map(t -> t.toWrapped(this.maxZoom))
                    .collect(Collectors.groupingBy(WrappedUrl::zoom, TreeMap::new, Collectors.toList()));

        List<WrappedUrl> allUrlsIn = urlsIn.values().stream().flatMap(List::stream).collect(Collectors.toList());

        this.allUrls = new BVHi<>(allUrlsIn);

        int minZoom = this.minZoom = urlsIn.firstKey();
        int maxZoom = urlsIn.lastKey();
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            List<WrappedUrl> urls = urlsIn.get(zoom);
            if (urls != null) {
                this.urlsByLevel.put(zoom, new BVHi<>(urls));
            }
        }
    }

    public Collection<WrappedUrl> getAllIntersecting(@NonNull Bounds2i bounds) {
        return this.allUrls.getAllIntersecting(bounds);
    }

    public String[] getUrls(int tileX, int tileZ, int zoom) {
        BVHi<WrappedUrl> bvh = this.urlsByLevel.get(zoom);
        if (bvh == null) {
            return null;
        }

        int shift = this.maxZoom - zoom;
        int minX = MathUtils.safeDirectionalShift(tileX, shift);
        int minZ = MathUtils.safeDirectionalShift(tileZ, shift);
        int add = MathUtils.safeDirectionalShift(1, shift);
        if (add != 0) { //only subtract 1 if non-zero
            add--;
        }

        Bounds2i bb = Bounds2i.of(minX, minX + add, minZ, minZ + add);
        return bvh.getAllIntersecting(bb)
                .stream()
                .sorted()
                .map(WrappedUrl::url)
                .toArray(String[]::new);
    }

    private static final class TempWrappedUrl {
        private String url;
        private int minX = Integer.MIN_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int minZ = Integer.MIN_VALUE;
        private int maxZ = Integer.MIN_VALUE;
        @Getter
        private int zoom = -1;
        private double priority = 0.0d;

        public WrappedUrl toWrapped(int baseZoom) {
            checkState(this.url != null, "url must be set!");
            checkState(this.minX != Integer.MIN_VALUE, "minX must be set!");
            checkState(this.maxX != Integer.MIN_VALUE, "maxX must be set!");
            checkState(this.minZ != Integer.MIN_VALUE, "minZ must be set!");
            checkState(this.maxZ != Integer.MIN_VALUE, "maxZ must be set!");
            checkState(this.zoom >= 0, "zoom must be set!");

            int shift = baseZoom - this.zoom;
            int minX = MathUtils.safeDirectionalShift(min(this.minX, this.maxX), shift);
            int maxX = MathUtils.safeDirectionalShift(max(this.minX, this.maxX), shift);
            int minZ = MathUtils.safeDirectionalShift(min(this.minZ, this.maxZ), shift);
            int maxZ = MathUtils.safeDirectionalShift(max(this.minZ, this.maxZ), shift);

            return new WrappedUrl(this.url, minX, maxX, minZ, maxZ, this.zoom, this.priority);
        }
    }
}
