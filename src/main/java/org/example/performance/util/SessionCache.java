package org.example.performance.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * SessionCache
 *
 * @author hlq
 * @description
 * @date 2024/3/6
 */
public class SessionCache<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<K, V> cache;
    private final ReentrantReadWriteLock lock;
    protected final Map<K, Lock> keyLockMap;

    public SessionCache() {
        this(new WeakHashMap());
    }

    public SessionCache(Map<K, V> initMap) {
        this.lock = new ReentrantReadWriteLock();
        this.keyLockMap = new ConcurrentHashMap();
        this.cache = initMap;
    }

    public V get(K key) {
        this.lock.readLock().lock();

        V var2;
        try {
            var2 = this.cache.get(key);
        } finally {
            this.lock.readLock().unlock();
        }

        return var2;
    }

    public V get(K key, Predicate<V> validPredicate, Supplier<V> supplier) {
        V v ;
        Lock keyLock = this.keyLockMap.computeIfAbsent(key, (k) -> new ReentrantLock());
        keyLock.lock();

        try {
            v = this.cache.get(key);
            if (null == v || !validPredicate.test(v)) {
                v = supplier.get();
                this.put(key, v);
            }
        } finally {
            keyLock.unlock();
            this.keyLockMap.remove(key);
        }

        return v;
    }

    public V put(K key, V value) {
        this.lock.writeLock().lock();

        try {
            this.cache.put(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }

        return value;
    }

    public V remove(K key) {
        this.lock.writeLock().lock();

        V var2;
        try {
            var2 = this.cache.remove(key);
        } finally {
            this.lock.writeLock().unlock();
        }

        return var2;
    }

    public void clear() {
        this.lock.writeLock().lock();

        try {
            this.cache.clear();
        } finally {
            this.lock.writeLock().unlock();
        }

    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return this.cache.entrySet().iterator();
    }
}

