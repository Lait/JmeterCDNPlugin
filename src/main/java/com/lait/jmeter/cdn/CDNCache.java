package com.lait.jmeter.cdn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CDNCache<K, V> implements Cache<K, V> {

    private final String name;
    private final HashMap<K, V> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    
    public CDNCache(String name) {
        this.name = name;
        cache = new HashMap<K, V>();
    }
    
    public CDNCache(String name, int initialCapacity) {
        this.name = name;
        cache = new HashMap<K, V>(initialCapacity);
    }
    
    public String getName() {
        return name;
    }

    public V get(K key) {
        readLock.lock();
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public Map<? extends K, ? extends V> getAll(Iterator<? extends K> keys) {
        readLock.lock();
        try {
            Map<K, V> map = new HashMap<K, V>();
            List<K> noEntryKeys = new ArrayList<K>();
            while(keys.hasNext()) {
                K key = keys.next();
                if(isPresent(key)) {
                    map.put(key, cache.get(key));
                } else {
                    noEntryKeys.add(key);
                }
            }
            
            if(!noEntryKeys.isEmpty()) {
                return null;
            }
            
            return map;
        } finally {
            readLock.unlock();
        }
    }

    public boolean isPresent(K key) {
        readLock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    public void put(K key, V value) {
        writeLock.lock();
        try {
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public void putAll(Map<? extends K, ? extends V> entries) {
        writeLock.lock();
        try {
            cache.putAll(entries);
        } finally {
            writeLock.unlock();
        }
    }

    /*
     * Removes the mapping for the specified key from cache if present.
     * @return 
     */
    public V remove(K key) {
        writeLock.lock();
        try {
            if(!isPresent(key)) {
                return null;
            }
            return cache.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    public void invalidateAll(Iterator<? extends K> keys) throws Exception {
        writeLock.lock();
        try {
            List<K> noEntryKeys = new ArrayList<K>();
            while(keys.hasNext()) {
                K key = keys.next();
                if(!isPresent(key)) {
                    noEntryKeys.add(key);
                }
            }
            if(!noEntryKeys.isEmpty()) {
            	throw new Exception("keys not found!");
            }
            
            while(keys.hasNext()) {
                K key = keys.next();
                remove(key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void invalidateAll() {
        writeLock.lock();
        try {
            cache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public int size() {
        readLock.lock();
        try {
            return cache.size();
        } finally {
            readLock.unlock();
        }
    }

    public void clear() {
        writeLock.lock();
        try {
            cache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public Map<? extends K, ? extends V> asMap() {
        readLock.lock();
        try {
            return new ConcurrentHashMap<K, V>(cache);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isEmpty() {
        readLock.lock();
        try {
            return cache.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

	public void printAll() {
		// TODO Auto-generated method stub
		 Iterator<Entry<K, V>> it = cache.entrySet().iterator();
		 while(it.hasNext()) {
			 Entry<K, V> e = it.next();
			 System.out.println(e.getKey());
			 System.out.println(((CacheEntry)e.getValue()).getResponse());
		 }
	}

}