package io.github.terra121.util.bvh;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * A simple, immutable, quadtree-based implementation of a BVH (Bounding Volume Hierarchy) on arbitrary values implementing {@link Bounds2d}.
 *
 * @author DaPorkchop_
 */
//this could probably be made even faster by using an STR/OTR-based R-tree, but it's already quite fast
public class BVH<V extends Bounds2d> {
    /**
     * The number of values that must be present in a node in order for it to become eligible for splitting.
     */
    protected static final int NODE_SPLIT_CAPACITY = 8;

    /**
     * The minimum size of a leaf node along a single axis.
     */
    protected static final double MIN_LEAF_SIZE = 16.0d;

    @SuppressWarnings("unchecked")
    protected static <V extends Bounds2d> Node<V>[] createNodeArray(int length) {
        return (Node<V>[]) new Node[length];
    }

    protected final Node<V> root;
    @Getter
    protected final int size;

    public BVH(@NonNull Iterable<V> values) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        int size = 0;

        for (V value : values) { //find bounds of input data
            minX = min(minX, value.minX());
            maxX = max(maxX, value.maxX());
            minZ = min(minZ, value.minZ());
            maxZ = max(maxZ, value.maxZ());
            size++;
        }

        if ((this.size = size) == 0) {
            this.root = null;
            return;
        }

        this.root = new Node<>(minX, maxX, minZ, maxZ);
        values.forEach(this.root::insert);
        this.root.cleanup();
    }

    /**
     * Gets a {@link Collection} containing every value that intersects with the given bounding box.
     *
     * @param bb the bounding box that values must intersect with
     * @return the values
     */
    public Collection<V> getAllIntersecting(@NonNull Bounds2d bb) {
        Collection<V> result = new ArrayList<>();
        this.forEachIntersecting(bb, result::add);
        return result;
    }

    /**
     * Runs the given function on every value that intersects with the given bounding box.
     *
     * @param bb       the bounding box that values must intersect with
     * @param callback the callback function to run
     */
    public void forEachIntersecting(@NonNull Bounds2d bb, @NonNull Consumer<V> callback) {
        if (this.root != null && bb.intersects(this.root)) {
            this.root.forEachIntersecting(bb, callback);
        }
    }

    protected static class Node<V extends Bounds2d> extends Bounds2dImpl {
        protected Node<V>[] children;

        protected Object[] values;
        protected int size = 0; //only used during construction

        public Node(double minX, double maxX, double minZ, double maxZ) {
            super(minX, maxX, minZ, maxZ);
        }

        protected void insert(V value) {
            if (this.values == null) { //allocate initial value array
                this.values = new Object[NODE_SPLIT_CAPACITY];
            } else if (this.size == NODE_SPLIT_CAPACITY && this.canSplit()) { //we're at capacity and haven't split yet, attempt to split now
                this.split();
            }

            if (this.children != null) { //attempt to insert the value into a child node
                int childIndex = this.childIndex(value);
                if (childIndex >= 0) {
                    if (this.children[childIndex] == null) {
                        this.children[childIndex] = this.createChild(childIndex);
                    }
                    this.children[childIndex].insert(value);
                    return;
                }
            }

            int valueIndex = this.size++;
            if (valueIndex >= this.values.length) { //grow array if necessary
                Object[] values = new Object[this.values.length << 1];
                System.arraycopy(this.values, 0, values, 0, this.values.length);
                this.values = values;
            }
            this.values[valueIndex] = value;
        }

        @SuppressWarnings("unchecked")
        protected void split() {
            //store current values in temporary array and clear self
            Object[] oldValues = Arrays.copyOf(this.values, this.size);
            Arrays.fill(this.values, 0, this.size, null);
            this.size = 0;

            this.children = createNodeArray(4); //create children array, also marking this node as having been split
            for (Object value : oldValues) {
                //re-insert previous values into self
                //since the children array is now non-null, it will attempt to insert the values into one of the children first
                this.insert((V) value);
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

        @SuppressWarnings("unchecked")
        protected void forEachIntersecting(Bounds2d bb, Consumer<V> callback) {
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
                        callback.accept((V) value);
                    }
                } else { //check intersection with each value individually
                    for (Object value : this.values) {
                        if (bb.intersects((V) value)) {
                            callback.accept((V) value);
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
    }
}
