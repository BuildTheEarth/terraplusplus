package io.github.terra121.util.interval;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * Implementation of a <a href="https://en.wikipedia.org/wiki/Segment_tree">segment tree</a> along a single axis of a series of 2-dimensional line segments.
 *
 * @author DaPorkchop_
 */
@ToString
public class IntervalTree<V extends Interval> {
    /**
     * The number of values that must be present in a node in order for it to become eligible for splitting.
     */
    protected static final int NODE_SPLIT_CAPACITY = 2;

    @SuppressWarnings("unchecked")
    protected static <V extends Interval> Node<V>[] createNodeArray(int length) {
        return (Node<V>[]) new Node[length];
    }

    protected final Node<V> root;

    @Getter
    protected final int size;

    public IntervalTree(@NonNull Iterable<V> values) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        int size = 0;

        for (V value : values) { //find bounds of input data
            min = min(min, value.min());
            max = max(max, value.max());
            size++;
        }

        if ((this.size = size) == 0) {
            this.root = null;
            return;
        }

        this.root = new Node<>(min, max);
        values.forEach(this.root::insert);
        this.root.cleanup();
    }

    /**
     * Gets a {@link Collection} containing every value that intersects with the given point.
     *
     * @param interval the interval that values must intersect with
     * @return the values
     */
    public Collection<V> getAllIntersecting(@NonNull Interval interval) {
        Collection<V> result = new ArrayList<>();
        this.forEachIntersecting(interval, result::add);
        return result;
    }

    /**
     * Runs the given function on every value that intersects with the given point.
     *
     * @param interval the interval that values must intersect with
     * @param callback the callback function to run
     */
    public void forEachIntersecting(@NonNull Interval interval, @NonNull Consumer<V> callback) {
        if (this.root != null && interval.intersects(this.root)) {
            this.root.forEachIntersecting(interval, callback);
        }
    }

    /**
     * A node in the segment tree (either a leaf or internal node).
     *
     * @author DaPorkchop_
     */
    protected static final class Node<V extends Interval> extends IntervalImpl {
        protected Node<V>[] children;

        protected Object[] values;
        protected int size = 0; //only used during construction

        public Node(double min, double max) {
            super(min, max);
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

        @SuppressWarnings("unchecked")
        protected void forEachIntersecting(Interval interval, Consumer<V> callback) {
            if (this.children != null) { //not a leaf node
                for (Node<V> child : this.children) {
                    if (child != null && interval.intersects(child)) {
                        child.forEachIntersecting(interval, callback);
                    }
                }
            }

            if (this.values != null) { //this node contains some values
                if (interval.contains(this)) { //the query box contains this entire node, therefore we can assume that all of the values intersect it
                    for (Object value : this.values) {
                        callback.accept((V) value);
                    }
                } else { //check intersection with each value individually
                    for (Object value : this.values) {
                        if (interval.intersects((V) value)) {
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
            return this.children == null;
        }

        protected int childIndex(Interval interval) { //we assume this node contains the given point
            //compute midpoints
            double m = (this.min + this.max) * 0.5d;

            if (interval.max() <= m) {
                return 0;
            } else if (interval.min() >= m) {
                return 1;
            } else {
                return -1;
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

        protected Node<V> createChild(int childIndex) {
            //this can't be implemented with lerp() due to floating-point inaccuracy :(

            //compute midpoints
            double m = (this.min + this.max) * 0.5d;
            return new Node<>((childIndex & 1) != 0 ? m : this.min, (childIndex & 1) != 0 ? this.max : m);
        }
    }
}
