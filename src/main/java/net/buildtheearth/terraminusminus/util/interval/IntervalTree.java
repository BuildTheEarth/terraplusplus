package net.buildtheearth.terraminusminus.util.interval;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.InternalThreadLocalMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Implementation of a <a href="https://en.wikipedia.org/wiki/Segment_tree">segment tree</a> along a single axis of a series of 2-dimensional line segments.
 *
 * @author DaPorkchop_
 */
@ToString
public class IntervalTree<V extends Interval> {
    protected static final FastThreadLocal<List<?>> LIST_CACHE = new FastThreadLocal<>();

    /**
     * The number of values that must be present in a node in order for it to become eligible for splitting.
     */
    protected static final int NODE_SPLIT_CAPACITY = Integer.parseUnsignedInt(System.getProperty("terraminusminus.interval_tree_split_capacity", "8"));

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
     * Gets a {@link List} containing every value that contains the given point.
     *
     * @param point the interval that values must contain
     * @return the values
     */
    public List<V> getAllIntersecting(double point) {
        InternalThreadLocalMap map = InternalThreadLocalMap.get();
        List<V> result = uncheckedCast(LIST_CACHE.get(map));
        if (result == null) { //create new list
            result = new ArrayList<>();
        }

        this.forEachIntersecting(point, result::add);

        if (result.isEmpty()) { //no intersections, cache list to be re-used on a subsequent invocation of this method
            LIST_CACHE.set(map, result);
            return Collections.emptyList();
        } else {
            LIST_CACHE.set(map, null);
            return result;
        }
    }

    /**
     * Runs the given function on every value that contains the given point.
     *
     * @param point    the interval that values must contain
     * @param callback the callback function to run
     */
    public void forEachIntersecting(double point, @NonNull Consumer<V> callback) {
        if (this.root != null && this.root.contains(point)) {
            this.root.forEachIntersecting(point, callback);
        }
    }

    /**
     * Runs the given function on every value in the tree.
     *
     * @param callback the callback function to run
     */
    public void forEach(@NonNull Consumer<V> callback) {
        if (this.root != null) {
            this.root.forEach(callback);
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
        protected int size; //only used during construction

        public Node(double min, double max) {
            super(min, max);
        }

        protected void insert(V value) {
            //empty segments can occur sometimes, either because the line segment itself is actually horizontal, or due to floating-point errors in the projection code.
            //  we'll simply discard them, as it's not like they'll ever intersect anything anyway.
            if (value.length() == 0.0d) {
                return;
            }

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

        protected void forEachIntersecting(double point, Consumer<V> callback) {
            if (this.children != null) { //not a leaf node
                for (Node<V> child : this.children) {
                    if (child != null && child.contains(point)) {
                        child.forEachIntersecting(point, callback);
                        break;
                    }
                }
            }

            if (this.values != null) { //this node contains some values
                for (Object _value : this.values) { //check intersection with each value individually
                    V value = uncheckedCast(_value);
                    if (value.contains(point)) {
                        callback.accept(value);
                    }
                }
            }
        }

        protected void forEach(Consumer<V> callback) {
            if (this.children != null) { //not a leaf node
                for (Node<V> child : this.children) {
                    if (child != null) {
                        child.forEach(callback);
                    }
                }
            }

            if (this.values != null) { //this node contains some values
                for (Object value : this.values) { //check intersection with each value individually
                    callback.accept(uncheckedCast(value));
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
