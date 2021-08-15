package net.buildtheearth.terraplusplus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.base.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PValidation;

/**
 * A data structure to keep track of sections to which values have been assigned.
 *
 * @param <K> - section key type
 * @param <V> - section value type
 * 
 * @author SmylerMC
 */
public class StabbingTree<K extends Comparable<K>, V> {
    
    private List<Node> nodes = new ArrayList<>();
    private final V defaultValue;
    
    /**
     * Creates a new {@link StabbingTree}.
     * 
     * @param defaultValue - the value to return when requesting a value at a point that isn't in any section
     */
    public StabbingTree(V defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * Creates a new {@link StabbingTree} that returns null when requesting a value at a point that isn't in any section.
     */
    public StabbingTree() {
        this.defaultValue = null;
    }
    
    /**
     * Creates a new section with a given value.
     * 
     * @param start - starting point of the section (inclusive)
     * @param end - ending point of the section (exclusive)
     * @param value - the value for this section
     * 
     * @throws NullPointerException if either start or end is null
     * @throws IllegalArgumentException if start is grater or equal to end
     */
    public void setBetween(K start, K end, V value) {
        this.validateInterval(start, end);
        int nodeCount = this.nodes.size();
        if (nodeCount == 0) {
            this.nodes.add(new Node(start, value));
            this.nodes.add(new Node(end, this.defaultValue));
        } else {
            int lower = this.getInternalIndexFor(start);
            int upper = this.getInternalIndexFor(end);
            V continueVal;
            if (upper < nodeCount && this.nodes.get(upper).start.equals(end)){
                // The start next of the previous section is just where ours end, do not look at the previous one
                continueVal = this.nodes.get(upper).value;
            } else if (upper == 0) {
                // We are before the first node
                continueVal = this.defaultValue;
            } else {
                // Once we end, we can continue the section that was there before us
                continueVal = this.nodes.get(--upper).value;
            }
            // Remove the sections we are overwriting
            for(; upper >= lower; upper--) {
                this.nodes.remove(upper);
            }
            if (!Objects.equal(value, continueVal)) {
                // We do not need to add a node if the section that follows has the same value, in that case we just merge
                this.nodes.add(lower, new Node(end, continueVal));
            }
            if (lower == 0 || !Objects.equal(value, this.nodes.get(lower - 1).value)) {
                // If the section that precedes has the same value, we just need to extend it, no need for a starting node
                this.nodes.add(lower, new Node(start, value));
            }            
        }
    }
    
    /**
     * Gets the value at the given point.
     * If there is no section there, the default value is returned.
     * 
     * @param index - where to get he value
     * 
     * @return the value at the given point
     */
    public V get(K index) {
        int nodeCount = this.nodes.size();
        if (nodeCount >= 2) {
            int i = this.getInternalIndexFor(index);
            if (i < nodeCount && this.nodes.get(i).start.equals(index)) {
                return this.nodes.get(i).value;
            } else if (i == 0) {
                // We are before the first node
                return this.defaultValue;
            } else {
                Node node = this.nodes.get(i - 1);
                return node.value;
            }
        } else {
            return this.defaultValue;
        }
    }
    
    /**
     * @return the number of section
     */
    public int sections() {
        int c = this.nodes.size();
        return c == 0 ? c : c - 1;
    }
    
    /**
     * Clears all sections, resets this {@link StabbingTree} as if if was just created
     */
    public void clear() {
        this.nodes.clear();
    }
    
    /**
     * Calls a consumer for all sections in the tree.
     * The consumer will take the start node and the end node of each section as inputs.
     * The start node holds the value for the section.
     * The end node holds the value for the section that follows.
     * 
     * @param consumer - the method to call
     */
    public void forEachSection(BiConsumer<Node, Node> consumer) {
        int nodeCount = this.nodes.size();
        if (nodeCount > 1) {
            Node previousNode = this.nodes.get(0);
            for (int i = 1; i < nodeCount; i++) {
                Node node = this.nodes.get(i);
                consumer.accept(previousNode, node);
                previousNode = node;
            }
        }
    }
    
    /**
     * Calls a consumer for all sections in the tree intersecting with the given interval.
     * The consumer will take the start node and the end node of each section as inputs.
     * The start node holds the value for the section.
     * The end node holds the value for the section that follows.
     * 
     * @param consumer - the method to call
     * 
     * @throws NullPointerException if either start or end is null
     * @throws IllegalArgumentException if start is grater or equal to end
     */
    public void forEachSection(K start, K end, BiConsumer<Node, Node> consumer) {
        this.validateInterval(start, end);
        int si = this.getInternalIndexFor(start);
        int ei = this.getInternalIndexFor(end);
        int nodeCount = this.nodes.size();
        if (si != ei || (si != 0 && si != nodeCount)) {
            // We do intersect
            Node previousNode = this.nodes.get(si);
            if (!previousNode.start.equals(start) && si > 0) {
                // We only keep a portion of the previous section
                previousNode = new Node(start, this.nodes.get(si - 1).value);
            } else {
                si++;
            }
            for (; si < ei; si++) {
                Node node = this.nodes.get(si);
                consumer.accept(previousNode, node);
                previousNode = node;
            }
            Node endNode;
            if (ei < nodeCount) {
                endNode = this.nodes.get(ei);
                if(!endNode.start.equals(end)) {
                    // What follows still has the same value
                    endNode = new Node(end, previousNode.value);
                }
                consumer.accept(previousNode, endNode);
            }
        }
    }
    
    /**
     * Finds the index at which a node would have to be inserted to keep the list sorted
     * 
     * @param key
     * 
     * @return the proper index for the given key
     */
    private int getInternalIndexFor(K key) {
        int lower = -1, upper = this.nodes.size();
        while (upper - lower > 1) {
            int mid = (upper + lower) / 2;
            K midKey = this.nodes.get(mid).start;
            int comparison = key.compareTo(midKey);
            if (comparison < 0) {
                upper = mid;
            } else if (comparison > 0) {
                lower = mid;
            } else {
                return mid;
            }
        }
        return lower + 1;
    }
    
    private void validateInterval(@NonNull K start, @NonNull K end) {
        PValidation.checkArg(start.compareTo(end) < 0, "Start of section cannot greater than or equal to the end of the section");
    }

    /**
     * Represents the boundaries of sections in a {@link StabbingTree}.
     * A {@link Node} is the end of a first section, in which it is excluded,
     * and the beginning of the section that follows, in which it is included,
     * and for which it holds the value stored in the section.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public class Node {
        private final K start;
        private final V value;
    }
    
}
