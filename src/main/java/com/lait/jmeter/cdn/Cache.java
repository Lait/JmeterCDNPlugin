package com.lait.jmeter.cdn;

import java.util.Iterator;
import java.util.Map;

public interface Cache<K, V> {
    public String getName();
    public V get(K key);
    public Map<? extends K, ? extends V> getAll(Iterator<? extends K> keys);
    public boolean isPresent(K key);
    public void put(K key, V value);
    public void putAll(Map<? extends K, ? extends V> entries);
    public V remove(K key) throws Exception;
    public void invalidateAll(Iterator<? extends K> keys) throws Exception;
    public void invalidateAll();
    public boolean isEmpty();
    public int size();
    public void clear();
    public Map<? extends K, ? extends V> asMap();
}