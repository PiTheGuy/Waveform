package pitheguy.waveform.util;

import org.junit.jupiter.api.Test;
import pitheguy.waveform.util.rolling.RollingList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RollingListTest {
    @Test
    public void testRemove() {
        RollingList<Integer> list = new RollingList<>(3);
        list.addAll(List.of(1, 2, 3));
        assertTrue(list.remove(1));
        assertFalse(list.remove(1));
        assertEquals(2, list.size());
    }

    @Test
    void testAutomaticRemoval() {
        RollingList<Integer> list = new RollingList<>(3);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        assertEquals(3, list.size());
        assertFalse(list.contains(1));
        assertTrue(list.contains(4));
    }

    @Test
    void testAddAll() {
        RollingList<Integer> list = new RollingList<>(3);
        list.addAll(List.of(1, 2, 3, 4));
        assertAll(
                () -> assertEquals(3, list.size()),
                () -> assertFalse(list.contains(1)),
                () -> assertTrue(list.containsAll(List.of(2, 3, 4)))
        );

    }

    @Test
    void testGet() {
        RollingList<Integer> list = new RollingList<>(3);
        list.addAll(List.of(1, 2, 3));
        assertEquals(1, list.get(0));
    }

    @Test
    void testGet_outOfBounds() {
        RollingList<Integer> list = new RollingList<>(3);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        list.addAll(List.of(1, 2, 3));
        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(4)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1)),
                () -> assertDoesNotThrow(() -> list.get(1))
        );
    }
}