package com.noseparte.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: Noseparte
 * @Date: 2019/10/18 11:07
 * @Description:
 *
 *          <p>深入剖析 HashMap</p>
 */
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    // 默认 初始容量
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    // 容器的最大值
    static final int MAXIMUM_CAPACITY = 1 << 30;
    // 默认 负载因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    // 链表转红黑树的阈值
    static final int TREEIFY_THRESHOLD = 8;

    transient int modCount;

    // Node 哈希桶
    transient Node[] table;

    // 负载因子
    final float loadFactor;

    // HashMap 所能存储键值对的**极限**
    int threshold;

    // HashMap 现有键值对的数量
    transient int size;

    public HashMap(float loadFactor) {
        this.loadFactor = loadFactor;
    }

    public HashMap(int initialCapacity){
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int initialCapacity, float loadFactor) {
        if(initialCapacity < 0){
            throw new IllegalArgumentException();
        }
        if(loadFactor <= 0){
            throw new IllegalArgumentException();
        }
        this.loadFactor = loadFactor;
        threshold = tableSizeFor(initialCapacity);
    }

    private static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    /**
     * 调用层 HashMap插入k-V键值对
     *
     * @param key key键
     * @param value value值
     * @return V
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 实现层 HashMap插入k-V键值对的底层实现
     *
     * @param hash key对应的hash值
     * @param key   key键
     * @param value value值
     * @param onlyIfAbsent 如果为true, 不改变现存的值
     * @param evict 如果为false，会新创建一个
     * @return
     */
    final public V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                          boolean evict) {
        Node<K, V>[] newTable;
        Node<K, V> node;
        int n, i;
        /** table是否为空, 或者长度是否为0 */
        if ((newTable = table) == null || (n = newTable.length) == 0)
            // 扩容
            n = (newTable = resize()).length;
        // 根据键值key计算hash值得到插入得到的数组索引 i
        if ((node = newTable[i = (n - 1) & hash]) == null) {
            // 如果为空, 直接插入
            newTable[i] = newNode(hash, key, value, null);
        } else {
            Node<K, V> e;
            K k;
            // 通过校验key与key的hash值
            if (node.hash == hash &&
                    ((k = node.key) == key || (key != null && key.equals(k)))) {
                // 存在则直接插入
                e = node;
                // table[i]是否为树节点
            } else if (node instanceof TreeNode) {
                // 红黑树直接插入键值对
                e = ((TreeNode<K, V>) node).putTreeVal(this, newTable, hash, key, value);
            } else {
                // 开始遍历链表准备插入
                for (int binCount = 0; ; ++binCount) {
                    if ((e = node.next) == null) {
                        node.next = newNode(hash, key, value, null);
                        // 链表长度是否大于8
                        if (binCount >= TREEIFY_THRESHOLD - 1)
                            // 转为红黑树，插入键值对
                            treeifyBin(newTable, hash);
                        break;
                    }
                    // 链表插入，若key存在则直接替换value
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    node = e;
                }
            }
            if (e != null) {
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null) {
                    e.value = value;
                }
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        // 判断是否需要扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }

    private void afterNodeInsertion(boolean evict) {

    }

    private void afterNodeAccess(Node<K,V> e) {
    }

    private void treeifyBin(Node<K,V>[] newTable, int hash) {

    }

    /**
     * 通过key获取value  k-v
     * @param key key值
     * @return value
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 查找HashMap中的某个K-V的底层实现
     * @param hash node#hash值
     * @param key node#key值
     * @return
     */
    private final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
                (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                    ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    /**
                     * 寻Node
                     * @param hash Node对应的hash值
                     * @param key 判断key是否相同
                     * @return Node
                     */
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * Hash算法 取key的hashCode值、高位运算、取模运算。
     *
     * @param key key键
     * @return
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 哈系桶数组 扩容
     * <p>
     *      if null: 初始化数组长度[12] DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY
     *      else:    2的指数幂扩容 table.length << 1
     * <p/>
     * <p>
     *     位运算 c为自然常数
     *     c << 1  ==> 2c
     *     c << 2  ==> 4c
     * <p/>
     * @return
     */
    final Node<K, V>[] resize(){
        /// <summary>
        /// 哈希桶数组 初始化
        /// <summary>
        Node<K, V>[] oldTable = this.table;
        int oldCapacity = (oldTable == null) ? 0 : oldTable.length;
        int oldThreshold = this.threshold;
        int newCapacity, newThreshold = 0;
        if(oldCapacity > 0){
            if(oldCapacity > MAXIMUM_CAPACITY){
                threshold = Integer.MAX_VALUE;
                return oldTable;
            }
            else if ((newCapacity = oldCapacity << 1) < MAXIMUM_CAPACITY &&
                    oldCapacity >= DEFAULT_INITIAL_CAPACITY){
                newThreshold = oldThreshold << 1;
            }
        }
        else if (oldThreshold > 0){ // 初始大小为threshold
            newCapacity = oldThreshold;
        }
        else {  // 如果threshold为0时, 使用默认值
            newCapacity = DEFAULT_INITIAL_CAPACITY;
            newThreshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        }
        if(newThreshold == 0){
            float currentThreshold = newCapacity * loadFactor;
            newThreshold = newCapacity < MAXIMUM_CAPACITY && currentThreshold < MAXIMUM_CAPACITY ?
                    (int) currentThreshold : Integer.MAX_VALUE;
        }
        threshold = newThreshold;
        Node[] newTable = new Node[newCapacity];
        table = newTable;
        if(oldTable != null){
            for(int j = 0; j < oldCapacity; ++j){
                Node<K, V> eachNode;
                if((eachNode = oldTable[j]) != null){
                    oldTable[j] = null;
                    if(eachNode.next == null){
                        newTable[eachNode.hash & (newCapacity - 1)] = eachNode;
                    }
                    else if (eachNode instanceof TreeNode){
                        ((TreeNode)eachNode).split(this, newTable, j, oldCapacity);
                    }
                    else {
                        Node<K, V> leftHead = null, leftTail = null;
                        Node<K, V> rightHead = null, rightTail = null;
                        Node<K, V> next;
                        do {
                            next = eachNode;
                            if((eachNode.hash & oldCapacity) == 0){
                                if(leftTail == null){
                                    leftHead = eachNode;
                                }else {
                                    leftTail.next = eachNode;
                                }
                                leftTail = eachNode;
                            }
                            else {
                                if (rightHead == null){
                                    rightHead = eachNode;
                                }else {
                                    rightTail.next = eachNode;
                                }
                                rightTail = eachNode;
                            }
                        }while ((eachNode = next) != null);
                        if (leftTail != null){
                            leftTail.next = null;
                            newTable[j] = leftHead;
                        }
                        if (rightTail != null){
                            rightTail.next = null;
                            newTable[j] = rightHead;
                        }
                    }
                }
            }
        }
        return newTable;
    }

    // Create a regular (non-tree) node
    Node<K,V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<K, V>(hash, key, value, next);
    }

    /**
     * @Auther: Noseparte
     * @Date: 2019/10/18 11:13
     * @Description:
     *
     *          <p>Node 维护一个链表</p>
     */
    static class Node<K, V> implements Map.Entry<K, V>{
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V newValue) {
            V oldValue = this.value;
            value = newValue;
            return oldValue;
        }


    }

    /**
     * @Auther: Noseparte
     * @Date: 2019/10/18 15:53
     * @Description:
     *
     *          <p>红黑树 Red-black tree</p>
     */
    static final class TreeNode<K, V> extends Node{
        TreeNode<K, V> parent;
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;
        boolean red;

        TreeNode(int hash, K key, V value, HashMap.Node<K, V> next) {
            super(hash, key, value, next);
        }

        final void split(HashMap<K,V> kvHashMap, Node[] newTable, int index, int oldCapacity) {

        }

        /**
         * 得到树节点
         *
         * @param hash key的hash值
         * @param key key键
         * @return
         */
        TreeNode<K, V> getTreeNode(int hash, Object key) {
            return ((parent != null) ? root() : this).find(hash, key, null);
        }

        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            // 略 感兴趣请查看源码
            return null;
        }

        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        Node<K,V> putTreeVal(HashMap<K, V> kvHashMap, Node<K, V>[] newTable, int hash, K key, V value) {
            // 红黑树直接插入键值对
            return null;
        }
    }

}
