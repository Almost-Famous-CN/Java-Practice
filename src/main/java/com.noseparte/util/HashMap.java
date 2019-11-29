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

    static final int tableSizeFor(int cap) {
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

    final public V put(K key, V value){
        Node<K,V>[] newTable; Node<K,V> node; int n, i;
        if(table == null || table.length == 0){
            n = (table = resize()).length;
        }


        return null;
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
 *     <p/>
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

    }

}
