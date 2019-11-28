# 要想Java玩得好，HashMap怎能少

## 简言

> 作为Java程序员, HashMap是一个必知必会的数据类型。

> 无论是从开发中的使用频率还是在面试中考察的频率都足以证明这一点。

## HashMap的前世今生

HashMap诞生于JDK1.2, 随着JDK版本的更新以及为了解决JDK1.7中HashMap中hash碰撞问题,
Oracle团队于[JEP 180](http://openjdk.java.net/jeps/180)：使用平衡树(Balanced Trees, 即我们所知的红黑树)处理频繁的HashMap冲突。官方文档如下：

- [JEP 180: Handle Frequent HashMap Collisions with Balanced Trees](http://openjdk.java.net/jeps/180)
- [JDK-8023463 : Improvements to HashMap/LinkedHashMap use of bins/buckets and trees (red/black)](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8023463)

## HashMap优化前后的数据结构对比

* JDK 1.7中的HashMap

大方向上，HashMap 里面是一个数组，然后数组中每个元素是一个单向链表。下图中，每个绿色
的实体是嵌套类 Entry 的实例，Entry 包含四个属性：key, value, hash 值和用于单向链表的 next。

1. capacity：当前数组容量，始终保持 2^n，可以扩容，扩容后数组大小为当前的 2 倍。
2. loadFactor：负载因子，默认为 0.75。
3. threshold：扩容的阈值，等于 capacity * loadFactor

![Java7 HashMap结构](https://github.com/Almost-Famous-CN/Java-Practice/blob/master/static/images/jdk7-hashmap.jpg?raw=true)

* JDK 1.8中的HashMap

Java8 对 HashMap 进行了一些修改，最大的不同就是利用了红黑树，所以其由 数组+链表+红黑
树 组成。
根据 Java7 HashMap 的介绍，我们知道，查找的时候，根据 hash 值我们能够快速定位到数组的
具体下标，但是之后的话，需要顺着链表一个个比较下去才能找到我们需要的，时间复杂度取决
于链表的长度，为 O(n)。为了降低这部分的开销，在 Java8 中，当链表中的元素超过了 8 个以后，
会将链表转换为红黑树，在这些位置进行查找的时候可以降低时间复杂度为 O(logN)。

![Java8 HashMap结构](https://github.com/Almost-Famous-CN/Java-Practice/blob/master/static/images/jdk8-hashmap.jpg?raw=true)

## HashMap优化前后的性能对比


## Talk is cheap. Show me the code: 手写HashMap

```java
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
    transient Node<K, V>[] table;

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

    }

}

```



## HashMap中的常用函数解析

### put(K key, V value)

![HashMap#put](https://github.com/Almost-Famous-CN/Java-Practice/blob/master/static/images/gaitubao_put.png?raw=true)

### get()

### resize()



## HashMap中的衍生题


1. 为什么平衡树使用的是红黑树?


2. 链表or红黑树?


3. 何为Hash碰撞? 怎么避免？


## finally

1. JDK1.8中的HashMap基于JDK1.7中的HashMap(数组+链表), 新增了红黑树提升性能
