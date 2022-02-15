package net.buildtheearth.terraplusplus.util.bvh;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
//this could probably be made even faster by using an STR/OTR-based R-tree, but it's already quite fast
@Getter
final class QuadtreeBVH<V extends Bounds2d> implements BVH<V> {
    /**
     * The number of values that must be present in a node in order for it to become eligible for splitting.
     */
    protected static final int NODE_SPLIT_CAPACITY = Integer.parseUnsignedInt(System.getProperty("terraplusplus.quadtree_split_capacity", "8"));

    /**
     * The minimum size of a leaf node along a single axis.
     */
    protected static final double MIN_LEAF_SIZE = 16.0d;

    protected static final Cached<ArrayDeque<ArrayDeque<?>>> STACK_CACHE = Cached.threadLocal(ArrayDeque::new, ReferenceStrength.SOFT);

    @Getter(AccessLevel.NONE)
    protected final Node<V> root;
    protected final int size;

    protected final double minX;
    protected final double maxX;
    protected final double minZ;
    protected final double maxZ;

    public QuadtreeBVH(@NonNull V[] values) {
        this.size = values.length;

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (V value : values) { //find bounds of input data
            minX = min(minX, value.minX());
            maxX = max(maxX, value.maxX());
            minZ = min(minZ, value.minZ());
            maxZ = max(maxZ, value.maxZ());
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        Class<V> componentType = uncheckedCast(values.getClass().getComponentType());
        this.root = new Node<>(minX, maxX, minZ, maxZ);
        for (V value : values) { //insert values
            this.root.insert(value, componentType);
        }
        this.root.cleanup();
    }

    @Override
    public List<V> getAllIntersecting(@NonNull Bounds2d bb) {
        List<V> result = new ArrayList<>();
        this.forEachIntersecting(bb, result::add);
        return result;
    }

    @Override
    public void forEachIntersecting(@NonNull Bounds2d bb, @NonNull Consumer<V> callback) {
        if (bb.intersects(this.root)) {
            this.root.forEachIntersecting(bb, callback);
        }
    }

    @Override
    public void forEach(@NonNull Consumer<? super V> callback) {
        ArrayDeque<ArrayDeque<?>> pool = STACK_CACHE.get();
        ArrayDeque<Node<V>> stack = pool.isEmpty() ? new ArrayDeque<>() : uncheckedCast(pool.pop());
        try {
            Node<V> node = this.root;
            do {
                if (node.children != null) { //push children onto stack
                    for (Node<V> child : node.children) {
                        stack.push(child);
                    }
                }

                if (node.values != null) { //handle values
                    for (Bounds2d value : node.values) {
                        callback.accept(uncheckedCast(value));
                    }
                }
            } while ((node = stack.pollFirst()) != null);
        } finally {
            stack.clear();
            pool.push(stack);
        }
    }

    @Override
    public Iterator<V> iterator() {
        return this.stream().iterator();
    }

    @Override
    public Spliterator<V> spliterator() {
        return this.stream().spliterator();
    }

    @Override
    public Stream<V> stream() {
        return this.root.stream();
    }

    protected static class Node<V extends Bounds2d> extends Bounds2dImpl {
        protected Node<V>[] children;

        protected V[] values;
        protected int size; //only used during construction

        public Node(double minX, double maxX, double minZ, double maxZ) {
            super(minX, maxX, minZ, maxZ);
        }

        protected void insert(@NonNull V value, @NonNull Class<V> componentType) {
            if (this.values == null) { //allocate initial value array
                this.values = uncheckedCast(Array.newInstance(componentType, NODE_SPLIT_CAPACITY));
            } else if (this.size == NODE_SPLIT_CAPACITY && this.canSplit()) { //we're at capacity and haven't split yet, attempt to split now
                this.split(componentType);
            }

            if (this.children != null) { //attempt to insert the value into a child node
                int childIndex = this.childIndex(value);
                if (childIndex >= 0) {
                    if (this.children[childIndex] == null) {
                        this.children[childIndex] = this.createChild(childIndex);
                    }
                    this.children[childIndex].insert(value, componentType);
                    return;
                }
            }

            int valueIndex = this.size++;
            if (valueIndex >= this.values.length) { //grow array if necessary
                V[] values = uncheckedCast(Array.newInstance(componentType, this.values.length << 1));
                System.arraycopy(this.values, 0, values, 0, this.values.length);
                this.values = values;
            }
            this.values[valueIndex] = value;
        }

        protected void split(@NonNull Class<V> componentType) {
            //store current values in temporary array and clear self
            V[] oldValues = Arrays.copyOf(this.values, this.size);
            Arrays.fill(this.values, 0, this.size, null);
            this.size = 0;

            this.children = uncheckedCast(new Node[4]); //create children array, also marking this node as having been split
            for (V value : oldValues) {
                //re-insert previous values into self
                //since the children array is now non-null, it will attempt to insert the values into one of the children first
                this.insert(value, componentType);
            }
        }

        protected void cleanup() {
            int size = 0;

            if (this.children != null) {
                for (Node<V> child : this.children) {
                    if (child != null) {
                        child.cleanup();
                        size += child.size;
                    }
                }
                if (size == 0) { //none of the children contain any values, so there's no need to keep the array object lying around
                    this.children = null;
                }
            }

            if (this.size == 0) { //empty values array can be pruned
                this.values = null;
            } else if (this.size != this.values.length) { //trim array to eliminate null padding
                this.values = Arrays.copyOf(this.values, this.size);
            }

            size += this.size;
            this.size = size;
        }

        protected void forEachIntersecting(Bounds2d bb, Consumer<V> callback) {
            if (bb.intersects(this)) {
                if (this.children != null) { //not a leaf node
                    for (Node<V> child : this.children) {
                        if (child != null && bb.intersects(child)) {
                            child.forEachIntersecting(bb, callback);
                        }
                    }
                }

                if (this.values != null) { //this node contains some values
                    if (bb.contains(this)) { //the query box contains this entire node, therefore we can assume that all of the values intersect it
                        for (Object value : this.values) {
                            callback.accept(uncheckedCast(value));
                        }
                    } else { //check intersection with each value individually
                        for (Bounds2d value : this.values) {
                            if (bb.intersects(value)) {
                                callback.accept(uncheckedCast(value));
                            }
                        }
                    }
                }
            }
        }

        /**
         * @return whether or not this node's contents are allowed to be split into children when it overflows
         */
        protected boolean canSplit() {
            return this.children == null && min(this.maxX - this.minX, this.maxZ - this.minZ) > MIN_LEAF_SIZE;
        }

        protected int childIndex(Bounds2d bb) { //we assume this node contains the given bounding box
            //compute midpoints
            double mx = (this.minX + this.maxX) * 0.5d;
            double mz = (this.minZ + this.maxZ) * 0.5d;

            int i = 0;
            if (bb.maxX() <= mx) {
                i |= 0; //lmao this does nothing
            } else if (bb.minX() >= mx) {
                i |= 1;
            } else {
                i |= -1;
            }

            if (bb.maxZ() <= mz) {
                i |= 0;
            } else if (bb.minZ() >= mz) {
                i |= 2;
            } else {
                i |= -1;
            }
            return i;
        }

        protected Node<V> createChild(int childIndex) {
            //this can't be implemented with lerp() due to floating-point inaccuracy :(

            //compute midpoints
            double mx = (this.minX + this.maxX) * 0.5d;
            double mz = (this.minZ + this.maxZ) * 0.5d;
            return new Node<>(
                    (childIndex & 1) != 0 ? mx : this.minX, (childIndex & 1) != 0 ? this.maxX : mx,
                    (childIndex & 2) != 0 ? mz : this.minZ, (childIndex & 2) != 0 ? this.maxZ : mz);
        }

        public Stream<V> stream() {
            Stream<V> valueStream = null;
            if (this.values != null) {
                valueStream = uncheckedCast(this.values.length == 1 ? Stream.of(this.values[0]) : Stream.of(this.values));
            }

            Stream<V> childValueStream = null;
            if (this.children != null) {
                childValueStream = Arrays.stream(this.children).flatMap(Node::stream);
            }

            if (valueStream != null && childValueStream != null) {
                return Stream.concat(valueStream, childValueStream);
            } else if (valueStream != null) {
                return valueStream;
            } else if (childValueStream != null) {
                return childValueStream;
            } else {
                throw new IllegalStateException("empty node?!?");
            }
        }
    }
}
