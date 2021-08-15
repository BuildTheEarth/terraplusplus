package net.buildtheearth.terraplusplus.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class StabbingTreeTest {
    
    @Test
    public void testSetAndGet() {
        StabbingTree<Integer, Integer> tree = new StabbingTree<>();
        assertNull(tree.get(0));
        
        tree.setBetween(0, 100, 1);
        
        assertEquals(1, tree.sections());
        assertNull(tree.get(-1));
        for (int i = 0; i < 100; i++) assertEquals(1, tree.get(i).intValue());
        assertNull(tree.get(100));
        
        tree.setBetween(-50, 50, 2);
        
        assertEquals(2, tree.sections());
        assertNull(tree.get(-51));
        for (int i = -50; i < 50; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = 50; i < 100; i++) assertEquals(1, tree.get(i).intValue());
        assertNull(tree.get(100));
        
        tree.setBetween(40, 60, 3);
        
        assertEquals(3, tree.sections());
        assertNull(tree.get(-51));
        for (int i = -50; i < 40; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = 40; i < 60; i++) assertEquals(3, tree.get(i).intValue());
        for (int i = 60; i < 100; i++) assertEquals(1, tree.get(i).intValue());
        assertNull(tree.get(100));
        
        tree.setBetween(100, 150, 4);
        
        assertEquals(4, tree.sections());
        assertNull(tree.get(-51));
        for (int i = -50; i < 40; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = 40; i < 60; i++) assertEquals(3, tree.get(i).intValue());
        for (int i = 60; i < 100; i++) assertEquals(1, tree.get(i).intValue());
        for (int i = 100; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(-100, -50, 5);
        
        assertEquals(5, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < 40; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = 40; i < 60; i++) assertEquals(3, tree.get(i).intValue());
        for (int i = 60; i < 100; i++) assertEquals(1, tree.get(i).intValue());
        for (int i = 100; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(-10, 120, 6);
        
        assertEquals(4, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < -10; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = -10; i < 120; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 120; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(20, 80, 7);
        
        assertEquals(6, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < -10; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = -10; i < 20; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 20; i < 80; i++) assertEquals(7, tree.get(i).intValue());
        for (int i = 80; i < 120; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 120; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(80, 100, 8);
        
        assertEquals(7, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < -10; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = -10; i < 20; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 20; i < 80; i++) assertEquals(7, tree.get(i).intValue());
        for (int i = 80; i < 100; i++) assertEquals(8, tree.get(i).intValue());
        for (int i = 100; i < 120; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 120; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(0, 20, 9);
        
        assertEquals(8, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < -10; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = -10; i < 0; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 0; i < 20; i++) assertEquals(9, tree.get(i).intValue());
        for (int i = 20; i < 80; i++) assertEquals(7, tree.get(i).intValue());
        for (int i = 80; i < 100; i++) assertEquals(8, tree.get(i).intValue());
        for (int i = 100; i < 120; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 120; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(60, 80, 8);
        
        assertEquals(8, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < -10; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = -10; i < 0; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 0; i < 20; i++) assertEquals(9, tree.get(i).intValue());
        for (int i = 20; i < 60; i++) assertEquals(7, tree.get(i).intValue());
        for (int i = 60; i < 100; i++) assertEquals(8, tree.get(i).intValue());
        for (int i = 100; i < 120; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 120; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
        
        tree.setBetween(100, 130, 8);
        
        assertEquals(7, tree.sections());
        assertNull(tree.get(-101));
        for (int i = -100; i < -50; i++) assertEquals(5, tree.get(i).intValue());
        for (int i = -50; i < -10; i++) assertEquals(2, tree.get(i).intValue());
        for (int i = -10; i < 0; i++) assertEquals(6, tree.get(i).intValue());
        for (int i = 0; i < 20; i++) assertEquals(9, tree.get(i).intValue());
        for (int i = 20; i < 60; i++) assertEquals(7, tree.get(i).intValue());
        for (int i = 60; i < 130; i++) assertEquals(8, tree.get(i).intValue());
        for (int i = 130; i < 150; i++) assertEquals(4, tree.get(i).intValue());
        assertNull(tree.get(150));
    }
    
    @Test
    public void testForEach() {
        StabbingTree<Integer, Integer> tree = new StabbingTree<>();
        this.testForEach(tree);
        
        tree.setBetween(0, 100, 1);
        
        this.testForEach(tree,
                0, 1, 100, null);
        
        tree.setBetween(-50, 50, 2);
        
        this.testForEach(tree,
                -50, 2, 50, 1,
                50, 1, 100, null);
        
        tree.setBetween(40, 60, 3);
        
        this.testForEach(tree,
                -50, 2, 40, 3,
                40, 3, 60, 1,
                60, 1, 100, null);
        
        tree.setBetween(100, 150, 4);
        tree.setBetween(-100, -50, 5);
        tree.setBetween(-10, 120, 6);
        tree.setBetween(20, 80, 7);
        tree.setBetween(80, 100, 8);
        tree.setBetween(0, 20, 9);
        tree.setBetween(60, 80, 8);
        
        this.testForEach(tree,
                -100, 5, -50, 2,
                -50, 2, -10, 6,
                -10, 6, 0, 9,
                0, 9, 20, 7,
                20, 7, 60, 8,
                60, 8, 100, 6,
                100, 6, 120, 4,
                120, 4, 150, null);
        this.testForEach(-210, -200, tree);
        this.testForEach(500, 510, tree);
        this.testForEach(-50, 60, tree,
                -50, 2, -10, 6,
                -10, 6, 0, 9,
                0, 9, 20, 7,
                20, 7, 60, 8);
        this.testForEach(50, 130, tree,
                50, 7, 60, 8,
                60, 8, 100, 6,
                100, 6, 120, 4,
                120, 4, 130, 4);
        this.testForEach(80, 160, tree,
                80, 8, 100, 6,
                100, 6, 120, 4,
                120, 4, 150, null);
        this.testForEach(-200, 10, tree,
                -100, 5, -50, 2,
                -50, 2, -10, 6,
                -10, 6, 0, 9,
                0, 9, 10, 9);
        this.testForEach(30, 40, tree,
                30, 7, 40, 7);
        
    }
    
    private void testForEach(StabbingTree<Integer, Integer> tree, Integer... values) {
        List<Integer> list = new ArrayList<>();
        tree.forEachSection((startNode, endNode) -> {
            list.add(startNode.start());
            list.add(startNode.value());
            list.add(endNode.start());
            list.add(endNode.value());
        });
        assertEquals(values.length, list.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], list.get(i));
        }
    }
    
    private void testForEach(int start, int end, StabbingTree<Integer, Integer> tree, Integer... values) {
        List<Integer> list = new ArrayList<>();
        tree.forEachSection(start, end, (startNode, endNode) -> {
            System.out.println(startNode.start() + " " + endNode.start());
            list.add(startNode.start());
            list.add(startNode.value());
            list.add(endNode.start());
            list.add(endNode.value());
        });
        System.out.println();
        assertEquals(values.length, list.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], list.get(i));
        }
    }

}
