package pitheguy.waveform.util.rolling;

import java.util.*;

public class RollingList<T> implements Collection<T> {
    private final int maxSize;
    private final ArrayDeque<T> deque;

    public RollingList(int maxSize) {
        this.maxSize = maxSize;
        this.deque = new ArrayDeque<>(maxSize);
    }

    public int maxSize() {
        return maxSize;
    }

    public boolean add(T element) {
        while (deque.size() >= maxSize()) deque.removeFirst();
        deque.addLast(element);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return deque.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return deque.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c.isEmpty()) return false;
        int itemsToAdd = Math.min(c.size(), maxSize());
        int overflow = deque.size() + itemsToAdd - maxSize();
        for (int i = 0; i < overflow; i++) deque.removeFirst();
        return deque.addAll(c.stream().skip(c.size() - itemsToAdd).toList());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return deque.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return deque.retainAll(c);
    }

    @Override
    public void clear() {
        deque.clear();
    }

    public T get(int index) {
        return (T) deque.toArray()[index];
    }

    public int size() {
        return deque.size();
    }

    @Override
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return deque.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return deque.iterator();
    }

    @Override
    public Object[] toArray() {
        return deque.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return deque.toArray(a);
    }

    @Override
    public String toString() {
        return deque.toString();
    }
}
