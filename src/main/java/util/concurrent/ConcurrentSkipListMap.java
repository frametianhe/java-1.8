/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A scalable concurrent {@link ConcurrentNavigableMap} implementation.
 * The map is sorted according to the {@linkplain Comparable natural
 * ordering} of its keys, or by a {@link Comparator} provided at map
 * creation time, depending on which constructor is used.
 *
 * <p>This class implements a concurrent variant of <a
 * href="http://en.wikipedia.org/wiki/Skip_list" target="_top">SkipLists</a>
 * providing expected average <i>log(n)</i> time cost for the
 * {@code containsKey}, {@code get}, {@code put} and
 * {@code remove} operations and their variants.  Insertion, removal,
 * update, and access operations safely execute concurrently by
 * multiple threads.
 *
 * <p>Iterators and spliterators are
 * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
 *
 * <p>Ascending key ordered views and their iterators are faster than
 * descending ones.
 *
 * <p>All {@code Map.Entry} pairs returned by methods in this class
 * and its views represent snapshots of mappings at the time they were
 * produced. They do <em>not</em> support the {@code Entry.setValue}
 * method. (Note however that it is possible to change mappings in the
 * associated map using {@code put}, {@code putIfAbsent}, or
 * {@code replace}, depending on exactly which effect you need.)
 *
 * <p>Beware that, unlike in most collections, the {@code size}
 * method is <em>not</em> a constant-time operation. Because of the
 * asynchronous nature of these maps, determining the current number
 * of elements requires a traversal of the elements, and so may report
 * inaccurate results if this collection is modified during traversal.
 * Additionally, the bulk operations {@code putAll}, {@code equals},
 * {@code toArray}, {@code containsValue}, and {@code clear} are
 * <em>not</em> guaranteed to be performed atomically. For example, an
 * iterator operating concurrently with a {@code putAll} operation
 * might view only some of the added elements.
 *
 * <p>This class and its views and iterators implement all of the
 * <em>optional</em> methods of the {@link Map} and {@link Iterator}
 * interfaces. Like most other concurrent collections, this class does
 * <em>not</em> permit the use of {@code null} keys or values because some
 * null return values cannot be reliably distinguished from the absence of
 * elements.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @since 1.6
 * 一个可伸缩的并发ConcurrentNavigableMap实现。映射根据其键的自然顺序进行排序，或者根据使用的构造函数在创建映射时提供的比较器进行排序。
这个类实现了SkipLists的并发变体，为containsKey、get、put和删除操作及其变体提供了预期的平均日志(n)时间成本。多个线程可以安全地同时执行插入、删除、更新和访问操作。
迭代器和spliterator是弱一致的。
升序键有序视图及其迭代器比降序视图快。
所有的地图。该类中的方法返回的条目对及其视图表示生成映射时的快照。它们不支持入口。setValue方法。(不过请注意，可以使用put、putIfAbsent或replace更改关联映射，具体取决于您需要的效果)。
注意，与大多数集合不同，size方法不是常量时间操作。由于这些映射的异步特性，确定当前元素的数量需要遍历元素，因此如果在遍历期间修改该集合，可能会报告不准确的结果。此外，不保证以原子方式执行批量操作putAll、equals、toArray、containsValue和clear。例如，与putAll操作同时操作的迭代器可能只查看一些添加的元素。
这个类及其视图和迭代器实现了映射和迭代器接口的所有可选方法。与大多数并发集合一样，这个类不允许使用空键或值，因为某些空返回值不能可靠地与缺少元素区分开来。
这个类是Java集合框架的成员。
 */
//自然排序或者按比较器排序，线程安全，不允许null键和null值
public class ConcurrentSkipListMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentNavigableMap<K,V>, Cloneable, Serializable {
    /*
     * This class implements a tree-like two-dimensionally linked skip
     * list in which the index levels are represented in separate
     * nodes from the base nodes holding data.  There are two reasons
     * for taking this approach instead of the usual array-based
     * structure: 1) Array based implementations seem to encounter
     * more complexity and overhead 2) We can use cheaper algorithms
     * for the heavily-traversed index lists than can be used for the
     * base lists.  Here's a picture of some of the basics for a
     * possible list with 2 levels of index:
     *
     * Head nodes          Index nodes
     * +-+    right        +-+                      +-+
     * |2|---------------->| |--------------------->| |->null
     * +-+                 +-+                      +-+
     *  | down              |                        |
     *  v                   v                        v
     * +-+            +-+  +-+       +-+            +-+       +-+
     * |1|----------->| |->| |------>| |----------->| |------>| |->null
     * +-+            +-+  +-+       +-+            +-+       +-+
     *  v              |    |         |              |         |
     * Nodes  next     v    v         v              v         v
     * +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+
     * | |->|A|->|B|->|C|->|D|->|E|->|F|->|G|->|H|->|I|->|J|->|K|->null
     * +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+
     *
     * The base lists use a variant of the HM linked ordered set
     * algorithm. See Tim Harris, "A pragmatic implementation of
     * non-blocking linked lists"
     * http://www.cl.cam.ac.uk/~tlh20/publications.html and Maged
     * Michael "High Performance Dynamic Lock-Free Hash Tables and
     * List-Based Sets"
     * http://www.research.ibm.com/people/m/michael/pubs.htm.  The
     * basic idea in these lists is to mark the "next" pointers of
     * deleted nodes when deleting to avoid conflicts with concurrent
     * insertions, and when traversing to keep track of triples
     * (predecessor, node, successor) in order to detect when and how
     * to unlink these deleted nodes.
     *
     * Rather than using mark-bits to mark list deletions (which can
     * be slow and space-intensive using AtomicMarkedReference), nodes
     * use direct CAS'able next pointers.  On deletion, instead of
     * marking a pointer, they splice in another node that can be
     * thought of as standing for a marked pointer (indicating this by
     * using otherwise impossible field values).  Using plain nodes
     * acts roughly like "boxed" implementations of marked pointers,
     * but uses new nodes only when nodes are deleted, not for every
     * link.  This requires less space and supports faster
     * traversal. Even if marked references were better supported by
     * JVMs, traversal using this technique might still be faster
     * because any search need only read ahead one more node than
     * otherwise required (to check for trailing marker) rather than
     * unmasking mark bits or whatever on each read.
     *
     * This approach maintains the essential property needed in the HM
     * algorithm of changing the next-pointer of a deleted node so
     * that any other CAS of it will fail, but implements the idea by
     * changing the pointer to point to a different node, not by
     * marking it.  While it would be possible to further squeeze
     * space by defining marker nodes not to have key/value fields, it
     * isn't worth the extra type-testing overhead.  The deletion
     * markers are rarely encountered during traversal and are
     * normally quickly garbage collected. (Note that this technique
     * would not work well in systems without garbage collection.)
     *
     * In addition to using deletion markers, the lists also use
     * nullness of value fields to indicate deletion, in a style
     * similar to typical lazy-deletion schemes.  If a node's value is
     * null, then it is considered logically deleted and ignored even
     * though it is still reachable. This maintains proper control of
     * concurrent replace vs delete operations -- an attempted replace
     * must fail if a delete beat it by nulling field, and a delete
     * must return the last non-null value held in the field. (Note:
     * Null, rather than some special marker, is used for value fields
     * here because it just so happens to mesh with the Map API
     * requirement that method get returns null if there is no
     * mapping, which allows nodes to remain concurrently readable
     * even when deleted. Using any other marker value here would be
     * messy at best.)
     *
     * Here's the sequence of events for a deletion of node n with
     * predecessor b and successor f, initially:
     *
     *        +------+       +------+      +------+
     *   ...  |   b  |------>|   n  |----->|   f  | ...
     *        +------+       +------+      +------+
     *
     * 1. CAS n's value field from non-null to null.
     *    From this point on, no public operations encountering
     *    the node consider this mapping to exist. However, other
     *    ongoing insertions and deletions might still modify
     *    n's next pointer.
     *
     * 2. CAS n's next pointer to point to a new marker node.
     *    From this point on, no other nodes can be appended to n.
     *    which avoids deletion errors in CAS-based linked lists.
     *
     *        +------+       +------+      +------+       +------+
     *   ...  |   b  |------>|   n  |----->|marker|------>|   f  | ...
     *        +------+       +------+      +------+       +------+
     *
     * 3. CAS b's next pointer over both n and its marker.
     *    From this point on, no new traversals will encounter n,
     *    and it can eventually be GCed.
     *        +------+                                    +------+
     *   ...  |   b  |----------------------------------->|   f  | ...
     *        +------+                                    +------+
     *
     * A failure at step 1 leads to simple retry due to a lost race
     * with another operation. Steps 2-3 can fail because some other
     * thread noticed during a traversal a node with null value and
     * helped out by marking and/or unlinking.  This helping-out
     * ensures that no thread can become stuck waiting for progress of
     * the deleting thread.  The use of marker nodes slightly
     * complicates helping-out code because traversals must track
     * consistent reads of up to four nodes (b, n, marker, f), not
     * just (b, n, f), although the next field of a marker is
     * immutable, and once a next field is CAS'ed to point to a
     * marker, it never again changes, so this requires less care.
     *
     * Skip lists add indexing to this scheme, so that the base-level
     * traversals start close to the locations being found, inserted
     * or deleted -- usually base level traversals only traverse a few
     * nodes. This doesn't change the basic algorithm except for the
     * need to make sure base traversals start at predecessors (here,
     * b) that are not (structurally) deleted, otherwise retrying
     * after processing the deletion.
     *
     * Index levels are maintained as lists with volatile next fields,
     * using CAS to link and unlink.  Races are allowed in index-list
     * operations that can (rarely) fail to link in a new index node
     * or delete one. (We can't do this of course for data nodes.)
     * However, even when this happens, the index lists remain sorted,
     * so correctly serve as indices.  This can impact performance,
     * but since skip lists are probabilistic anyway, the net result
     * is that under contention, the effective "p" value may be lower
     * than its nominal value. And race windows are kept small enough
     * that in practice these failures are rare, even under a lot of
     * contention.
     *
     * The fact that retries (for both base and index lists) are
     * relatively cheap due to indexing allows some minor
     * simplifications of retry logic. Traversal restarts are
     * performed after most "helping-out" CASes. This isn't always
     * strictly necessary, but the implicit backoffs tend to help
     * reduce other downstream failed CAS's enough to outweigh restart
     * cost.  This worsens the worst case, but seems to improve even
     * highly contended cases.
     *
     * Unlike most skip-list implementations, index insertion and
     * deletion here require a separate traversal pass occurring after
     * the base-level action, to add or remove index nodes.  This adds
     * to single-threaded overhead, but improves contended
     * multithreaded performance by narrowing interference windows,
     * and allows deletion to ensure that all index nodes will be made
     * unreachable upon return from a public remove operation, thus
     * avoiding unwanted garbage retention. This is more important
     * here than in some other data structures because we cannot null
     * out node fields referencing user keys since they might still be
     * read by other ongoing traversals.
     *
     * Indexing uses skip list parameters that maintain good search
     * performance while using sparser-than-usual indices: The
     * hardwired parameters k=1, p=0.5 (see method doPut) mean
     * that about one-quarter of the nodes have indices. Of those that
     * do, half have one level, a quarter have two, and so on (see
     * Pugh's Skip List Cookbook, sec 3.4).  The expected total space
     * requirement for a map is slightly less than for the current
     * implementation of java.util.TreeMap.
     *
     * Changing the level of the index (i.e, the height of the
     * tree-like structure) also uses CAS. The head index has initial
     * level/height of one. Creation of an index with height greater
     * than the current level adds a level to the head index by
     * CAS'ing on a new top-most head. To maintain good performance
     * after a lot of removals, deletion methods heuristically try to
     * reduce the height if the topmost levels appear to be empty.
     * This may encounter races in which it possible (but rare) to
     * reduce and "lose" a level just as it is about to contain an
     * index (that will then never be encountered). This does no
     * structural harm, and in practice appears to be a better option
     * than allowing unrestrained growth of levels.
     *
     * The code for all this is more verbose than you'd like. Most
     * operations entail locating an element (or position to insert an
     * element). The code to do this can't be nicely factored out
     * because subsequent uses require a snapshot of predecessor
     * and/or successor and/or value fields which can't be returned
     * all at once, at least not without creating yet another object
     * to hold them -- creating such little objects is an especially
     * bad idea for basic internal search operations because it adds
     * to GC overhead.  (This is one of the few times I've wished Java
     * had macros.) Instead, some traversal code is interleaved within
     * insertion and removal operations.  The control logic to handle
     * all the retry conditions is sometimes twisty. Most search is
     * broken into 2 parts. findPredecessor() searches index nodes
     * only, returning a base-level predecessor of the key. findNode()
     * finishes out the base-level search. Even with this factoring,
     * there is a fair amount of near-duplication of code to handle
     * variants.
     *
     * To produce random values without interference across threads,
     * we use within-JDK thread local random support (via the
     * "secondary seed", to avoid interference with user-level
     * ThreadLocalRandom.)
     *
     * A previous version of this class wrapped non-comparable keys
     * with their comparators to emulate Comparables when using
     * comparators vs Comparables.  However, JVMs now appear to better
     * handle infusing comparator-vs-comparable choice into search
     * loops. Static method cpr(comparator, x, y) is used for all
     * comparisons, which works well as long as the comparator
     * argument is set up outside of loops (thus sometimes passed as
     * an argument to internal methods) to avoid field re-reads.
     *
     * For explanation of algorithms sharing at least a couple of
     * features with this one, see Mikhail Fomitchev's thesis
     * (http://www.cs.yorku.ca/~mikhail/), Keir Fraser's thesis
     * (http://www.cl.cam.ac.uk/users/kaf24/), and Hakan Sundell's
     * thesis (http://www.cs.chalmers.se/~phs/).
     *
     * Given the use of tree-like index nodes, you might wonder why
     * this doesn't use some kind of search tree instead, which would
     * support somewhat faster search operations. The reason is that
     * there are no known efficient lock-free insertion and deletion
     * algorithms for search trees. The immutability of the "down"
     * links of index nodes (as opposed to mutable "left" fields in
     * true trees) makes this tractable using only CAS operations.
     *
     * Notation guide for local variables
     * Node:         b, n, f    for  predecessor, node, successor
     * Index:        q, r, d    for index node, right, down.
     *               t          for another index node
     * Head:         h
     * Levels:       j
     * Keys:         k, key
     * Values:       v, value
     * Comparisons:  c
     */

    private static final long serialVersionUID = -8627078645895051609L;

    /**
     * Special value used to identify base-level header用于识别基级标头的特殊值
     */
    private static final Object BASE_HEADER = new Object();

    /**
     * The topmost head index of the skiplist.skiplist的最顶端索引。
     */
    private transient volatile HeadIndex<K,V> head;

    /**
     * The comparator used to maintain order in this map, or null if
     * using natural ordering.  (Non-private to simplify access in
     * nested classes.)比较器用于在此映射中保持顺序，如果使用自然排序，则为null。(非私有的，以简化对嵌套类的访问。)
     * @serial
     */
    final Comparator<? super K> comparator;

    /** Lazily initialized key set 延迟初始化的主要组*/
    private transient KeySet<K> keySet;
    /** Lazily initialized entry set 延迟初始化入口设置*/
    private transient EntrySet<K,V> entrySet;
    /** Lazily initialized values collection 延迟初始化的值集合*/
    private transient Values<V> values;
    /** Lazily initialized descending key set 延迟初始化降序键集*/
    private transient ConcurrentNavigableMap<K,V> descendingMap;

    /**
     * Initializes or resets state. Needed by constructors, clone,
     * clear, readObject. and ConcurrentSkipListSet.clone.
     * (Note that comparator must be separately initialized.)初始化或重置状态。构造函数、克隆、清除、readObject所需要的。和ConcurrentSkipListSet.clone。(注意，比较器必须分别初始化。)
     */
    private void initialize() {
        keySet = null;
        entrySet = null;
        values = null;
        descendingMap = null;
        head = new HeadIndex<K,V>(new Node<K,V>(null, BASE_HEADER, null),
                                  null, null, 1);
    }

    /**
     * compareAndSet head nodecompareAndSet头节点
     */
    private boolean casHead(HeadIndex<K,V> cmp, HeadIndex<K,V> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    /* ---------------- Nodes -------------- */

    /**
     * Nodes hold keys and values, and are singly linked in sorted
     * order, possibly with some intervening marker nodes. The list is
     * headed by a dummy node accessible as head.node. The value field
     * is declared only as Object because it takes special non-V
     * values for marker and header nodes.节点持有键和值，并按排序的顺序单独链接，可能使用一些中间的标记节点。列表由一个可以作为head.node访问的虚拟节点领导。value字段被声明为对象，因为它对标记和头节点采用特殊的非v值。
     */
    static final class Node<K,V> {
        final K key;
        volatile Object value;
        volatile Node<K,V> next;

        /**
         * Creates a new regular node.创建一个新的规则节点。
         */
        Node(K key, Object value, Node<K,V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /**
         * Creates a new marker node. A marker is distinguished by
         * having its value field point to itself.  Marker nodes also
         * have null keys, a fact that is exploited in a few places,
         * but this doesn't distinguish markers from the base-level
         * header node (head.node), which also has a null key.创建一个新的标记节点。标记的区别在于它的值字段指向它自己。标记节点也有空键，这是在一些地方被利用的事实，但是这并不能区分标记和基级头节点(header .node)，后者也有一个空键。
         */
        Node(Node<K,V> next) {
            this.key = null;
            this.value = this;
            this.next = next;
        }

        /**
         * compareAndSet value fieldcompareAndSet value字段
         */
        boolean casValue(Object cmp, Object val) {
            return UNSAFE.compareAndSwapObject(this, valueOffset, cmp, val);
        }

        /**
         * compareAndSet next fieldcompareAndSet下一个字段
         */
        boolean casNext(Node<K,V> cmp, Node<K,V> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        /**
         * Returns true if this node is a marker. This method isn't
         * actually called in any current code checking for markers
         * because callers will have already read value field and need
         * to use that read (not another done here) and so directly
         * test if value points to node.如果该节点是一个标记，则返回true。实际上，在任何当前代码检查标记时都不会调用这个方法，因为调用者将已经读取了value字段，并且需要使用这个read(而不是这里的另一个read)，因此直接测试值是否指向node。
         *
         * @return true if this node is a marker node
         */
        boolean isMarker() {
            return value == this;
        }

        /**
         * Returns true if this node is the header of base-level list.如果该节点是基级列表的头，则返回true。
         * @return true if this node is header node
         */
        boolean isBaseHeader() {
            return value == BASE_HEADER;
        }

        /**
         * Tries to append a deletion marker to this node.尝试将删除标记附加到该节点。
         * @param f the assumed current successor of this node
         * @return true if successful
         */
        boolean appendMarker(Node<K,V> f) {
            return casNext(f, new Node<K,V>(f));
        }

        /**
         * Helps out a deletion by appending marker or unlinking from
         * predecessor. This is called during traversals when value
         * field seen to be null.通过附加标记或从前任取消链接来帮助删除。当值字段为空时，在遍历期间调用。
         * @param b predecessor
         * @param f successor
         */
        void helpDelete(Node<K,V> b, Node<K,V> f) {
            /*
             * Rechecking links and then doing only one of the
             * help-out stages per call tends to minimize CAS
             * interference among helping threads.
             */
            if (f == next && this == b.next) {
                if (f == null || f.value != f) // not already marked
                    casNext(f, new Node<K,V>(f));
                else
                    b.casNext(this, f.next);
            }
        }

        /**
         * Returns value if this node contains a valid key-value pair,
         * else null.如果该节点包含有效的键值对，则返回值else null。
         * @return this node's value if it isn't a marker or header or
         * is deleted, else null
         */
        V getValidValue() {
            Object v = value;
            if (v == this || v == BASE_HEADER)
                return null;
            @SuppressWarnings("unchecked") V vv = (V)v;
            return vv;
        }

        /**
         * Creates and returns a new SimpleImmutableEntry holding current
         * mapping if this node holds a valid value, else null.创建并返回一个新的SimpleImmutableEntry保存当前映射，如果该节点持有一个有效值，则为null。
         * @return new entry or null
         */
        AbstractMap.SimpleImmutableEntry<K,V> createSnapshot() {
            Object v = value;
            if (v == null || v == this || v == BASE_HEADER)
                return null;
            @SuppressWarnings("unchecked") V vv = (V)v;
            return new AbstractMap.SimpleImmutableEntry<K,V>(key, vv);
        }

        // UNSAFE mechanics

        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                valueOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("value"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /* ---------------- Indexing -------------- */

    /**
     * Index nodes represent the levels of the skip list.  Note that
     * even though both Nodes and Indexes have forward-pointing
     * fields, they have different types and are handled in different
     * ways, that can't nicely be captured by placing field in a
     * shared abstract class.索引节点表示跳过列表的级别。请注意，即使节点和索引都有向前指向的字段，但它们有不同的类型，并以不同的方式处理，这不能很好地通过在共享抽象类中放置字段来捕获。
     */
    static class Index<K,V> {
        final Node<K,V> node;
        final Index<K,V> down;
        volatile Index<K,V> right;

        /**
         * Creates index node with given values.使用给定的值创建索引节点。
         */
        Index(Node<K,V> node, Index<K,V> down, Index<K,V> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }

        /**
         * compareAndSet right fieldcompareAndSet右外野
         */
        final boolean casRight(Index<K,V> cmp, Index<K,V> val) {
            return UNSAFE.compareAndSwapObject(this, rightOffset, cmp, val);
        }

        /**
         * Returns true if the node this indexes has been deleted.如果已删除此索引的节点，则返回true。
         * @return true if indexed node is known to be deleted
         */
        final boolean indexesDeletedNode() {
            return node.value == null;
        }

        /**
         * Tries to CAS newSucc as successor.  To minimize races with
         * unlink that may lose this index node, if the node being
         * indexed is known to be deleted, it doesn't try to link in.试图继承纽苏克。为了将可能丢失这个索引节点的链接最小化，如果已知的节点被删除，它就不会尝试链接。
         * @param succ the expected current successor
         * @param newSucc the new successor
         * @return true if successful
         */
        final boolean link(Index<K,V> succ, Index<K,V> newSucc) {
            Node<K,V> n = node;
            newSucc.right = succ;
            return n.value != null && casRight(succ, newSucc);
        }

        /**
         * Tries to CAS right field to skip over apparent successor
         * succ.  Fails (forcing a retraversal by caller) if this node
         * is known to be deleted.尝试CAS右场跳过明显的继承succ。如果已知要删除此节点，则失败(由调用者强制执行一次遍历)。
         * @param succ the expected current successor
         * @return true if successful
         */
        final boolean unlink(Index<K,V> succ) {
            return node.value != null && casRight(succ, succ.right);
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long rightOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Index.class;
                rightOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("right"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /* ---------------- Head nodes -------------- */

    /**
     * Nodes heading each level keep track of their level.每个级别的节点都跟踪它们的级别。
     */
    static final class HeadIndex<K,V> extends Index<K,V> {
        final int level;
        HeadIndex(Node<K,V> node, Index<K,V> down, Index<K,V> right, int level) {
            super(node, down, right);
            this.level = level;
        }
    }

    /* ---------------- Comparison utilities -------------- */

    /**
     * Compares using comparator or natural ordering if null.
     * Called only by methods that have performed required type checks.如果无效，则使用比较器或自然排序进行比较。只通过执行所需类型检查的方法调用。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static final int cpr(Comparator c, Object x, Object y) {
        return (c != null) ? c.compare(x, y) : ((Comparable)x).compareTo(y);
    }

    /* ---------------- Traversal -------------- */

    /**
     * Returns a base-level node with key strictly less than given key,
     * or the base-level header if there is no such node.  Also
     * unlinks indexes to deleted nodes found along the way.  Callers
     * rely on this side-effect of clearing indices to deleted nodes.返回具有严格小于给定键的基级节点，如果没有基级节点，则返回基级头。还可以将索引取消链接到沿途找到的已删除节点。调用者依赖于清除索引到删除节点的这种副作用。
     * @param key the key
     * @return a predecessor of key
     */
    private Node<K,V> findPredecessor(Object key, Comparator<? super K> cmp) {
        if (key == null)
            throw new NullPointerException(); // don't postpone errors
        for (;;) {
            for (Index<K,V> q = head, r = q.right, d;;) {
                if (r != null) {
                    Node<K,V> n = r.node;
                    K k = n.key;
                    if (n.value == null) {
                        if (!q.unlink(r))
                            break;           // restart
                        r = q.right;         // reread r
                        continue;
                    }
                    if (cpr(cmp, key, k) > 0) {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }
                if ((d = q.down) == null)
                    return q.node;
                q = d;
                r = d.right;
            }
        }
    }

    /**
     * Returns node holding key or null if no such, clearing out any
     * deleted nodes seen along the way.  Repeatedly traverses at
     * base-level looking for key starting at predecessor returned
     * from findPredecessor, processing base-level deletions as
     * encountered. Some callers rely on this side-effect of clearing
     * deleted nodes.
     *
     * Restarts occur, at traversal step centered on node n, if:
     *
     *   (1) After reading n's next field, n is no longer assumed
     *       predecessor b's current successor, which means that
     *       we don't have a consistent 3-node snapshot and so cannot
     *       unlink any subsequent deleted nodes encountered.
     *
     *   (2) n's value field is null, indicating n is deleted, in
     *       which case we help out an ongoing structural deletion
     *       before retrying.  Even though there are cases where such
     *       unlinking doesn't require restart, they aren't sorted out
     *       here because doing so would not usually outweigh cost of
     *       restarting.
     *
     *   (3) n is a marker or n's predecessor's value field is null,
     *       indicating (among other possibilities) that
     *       findPredecessor returned a deleted node. We can't unlink
     *       the node because we don't know its predecessor, so rely
     *       on another call to findPredecessor to notice and return
     *       some earlier predecessor, which it will do. This check is
     *       only strictly needed at beginning of loop, (and the
     *       b.value check isn't strictly needed at all) but is done
     *       each iteration to help avoid contention with other
     *       threads by callers that will fail to be able to change
     *       links, and so will retry anyway.
     *
     * The traversal loops in doPut, doRemove, and findNear all
     * include the same three kinds of checks. And specialized
     * versions appear in findFirst, and findLast and their
     * variants. They can't easily share code because each uses the
     * reads of fields held in locals occurring in the orders they
     * were performed.
     * 返回持有key或null的节点，清除沿途看到的任何已删除节点。在base-level中反复遍历查找从find前辈返回的关键开始，处理基础级别的删除。一些调用者依赖于清除已删除节点的副作用。在以节点n为中心的遍历步骤中，如果:(1)在读取n的下一个字段后，n不再被假定为前任b的当前继承者，这意味着我们没有一个一致的3节点快照，因此不能取消所遇到的后续被删除节点的链接。(2) n的值字段为null，表示n被删除，在这种情况下，我们在重新尝试之前帮助进行结构删除。即使有些情况下，这样的unlink不需要重新启动，但它们并没有在这里进行排序，因为这样做通常不会超过重新启动的成本。(3) n是一个标记，或者n的前一个值字段为null，表明(在其他可能性中)findformer返回一个已删除的节点。我们不能取消对节点的链接，因为我们不知道它的前身，所以要依赖另一个调用findPredecessor，去注意并返回一些更早的前任，它将会这么做。这个检查只在循环的开始(和b)时严格需要。值检查根本不需要)，但是每次迭代都要进行，以帮助避免调用者与其他线程的争用，因为调用者无法更改链接，因此无论如何都要重试。doPut、doRemove和findNear中的遍历循环都包含相同的三种检查。专门的版本出现在findFirst和findLast以及它们的变体中。它们不能很容易地共享代码，因为每个代码都使用在本地执行的命令中所持有的字段的读取。
     *
     * @param key the key
     * @return node holding key, or null if no such
     */
    private Node<K,V> findNode(Object key) {
        if (key == null)
            throw new NullPointerException(); // don't postpone errors
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v; int c;
                if (n == null)
                    break outer;
                Node<K,V> f = n.next;
                if (n != b.next)                // inconsistent read
                    break;
                if ((v = n.value) == null) {    // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)  // b is deleted
                    break;
                if ((c = cpr(cmp, key, n.key)) == 0)
                    return n;
                if (c < 0)
                    break outer;
                b = n;
                n = f;
            }
        }
        return null;
    }

    /**
     * Gets value for key. Almost the same as findNode, but returns
     * the found value (to avoid retries during re-reads)价值的关键。几乎与findNode相同，但是返回找到的值(以避免在重新读取时重试)
     *
     * @param key the key
     * @return the value, or null if absent
     */
    private V doGet(Object key) {
        if (key == null)
            throw new NullPointerException();
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v; int c;
                if (n == null)
                    break outer;
                Node<K,V> f = n.next;
                if (n != b.next)                // inconsistent read
                    break;
                if ((v = n.value) == null) {    // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)  // b is deleted
                    break;
                if ((c = cpr(cmp, key, n.key)) == 0) {
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    return vv;
                }
                if (c < 0)
                    break outer;
                b = n;
                n = f;
            }
        }
        return null;
    }

    /* ---------------- Insertion -------------- */

    /**
     * Main insertion method.  Adds element if not present, or
     * replaces value if present and onlyIfAbsent is false.主要的插入方法。如果不存在则添加元素，如果存在则替换值，且只有ifabsent为false。
     * @param key the key
     * @param value the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private V doPut(K key, V value, boolean onlyIfAbsent) {
        Node<K,V> z;             // added node
        if (key == null)
            throw new NullPointerException();
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                if (n != null) {
                    Object v; int c;
                    Node<K,V> f = n.next;
                    if (n != b.next)               // inconsistent read
                        break;
                    if ((v = n.value) == null) {   // n is deleted
                        n.helpDelete(b, f);
                        break;
                    }
                    if (b.value == null || v == n) // b is deleted
                        break;
                    if ((c = cpr(cmp, key, n.key)) > 0) {
                        b = n;
                        n = f;
                        continue;
                    }
                    if (c == 0) {
                        if (onlyIfAbsent || n.casValue(v, value)) {
                            @SuppressWarnings("unchecked") V vv = (V)v;
                            return vv;
                        }
                        break; // restart if lost race to replace value
                    }
                    // else c < 0; fall through
                }

                z = new Node<K,V>(key, value, n);
                if (!b.casNext(n, z))
                    break;         // restart if lost race to append to b
                break outer;
            }
        }

        int rnd = ThreadLocalRandom.nextSecondarySeed();
        if ((rnd & 0x80000001) == 0) { // test highest and lowest bits
            int level = 1, max;
            while (((rnd >>>= 1) & 1) != 0)
                ++level;
            Index<K,V> idx = null;
            HeadIndex<K,V> h = head;
            if (level <= (max = h.level)) {
                for (int i = 1; i <= level; ++i)
                    idx = new Index<K,V>(z, idx, null);
            }
            else { // try to grow by one level
                level = max + 1; // hold in array and later pick the one to use
                @SuppressWarnings("unchecked")Index<K,V>[] idxs =
                    (Index<K,V>[])new Index<?,?>[level+1];
                for (int i = 1; i <= level; ++i)
                    idxs[i] = idx = new Index<K,V>(z, idx, null);
                for (;;) {
                    h = head;
                    int oldLevel = h.level;
                    if (level <= oldLevel) // lost race to add level
                        break;
                    HeadIndex<K,V> newh = h;
                    Node<K,V> oldbase = h.node;
                    for (int j = oldLevel+1; j <= level; ++j)
                        newh = new HeadIndex<K,V>(oldbase, newh, idxs[j], j);
                    if (casHead(h, newh)) {
                        h = newh;
                        idx = idxs[level = oldLevel];
                        break;
                    }
                }
            }
            // find insertion points and splice in
            splice: for (int insertionLevel = level;;) {
                int j = h.level;
                for (Index<K,V> q = h, r = q.right, t = idx;;) {
                    if (q == null || t == null)
                        break splice;
                    if (r != null) {
                        Node<K,V> n = r.node;
                        // compare before deletion check avoids needing recheck
                        int c = cpr(cmp, key, n.key);
                        if (n.value == null) {
                            if (!q.unlink(r))
                                break;
                            r = q.right;
                            continue;
                        }
                        if (c > 0) {
                            q = r;
                            r = r.right;
                            continue;
                        }
                    }

                    if (j == insertionLevel) {
                        if (!q.link(r, t))
                            break; // restart
                        if (t.node.value == null) {
                            findNode(key);
                            break splice;
                        }
                        if (--insertionLevel == 0)
                            break splice;
                    }

                    if (--j >= insertionLevel && j < level)
                        t = t.down;
                    q = q.down;
                    r = q.right;
                }
            }
        }
        return null;
    }

    /* ---------------- Deletion -------------- */

    /**
     * Main deletion method. Locates node, nulls value, appends a
     * deletion marker, unlinks predecessor, removes associated index
     * nodes, and possibly reduces head index level.
     *
     * Index nodes are cleared out simply by calling findPredecessor.
     * which unlinks indexes to deleted nodes found along path to key,
     * which will include the indexes to this node.  This is done
     * unconditionally. We can't check beforehand whether there are
     * index nodes because it might be the case that some or all
     * indexes hadn't been inserted yet for this node during initial
     * search for it, and we'd like to ensure lack of garbage
     * retention, so must call to be sure.
     *
     * @param key the key
     * @param value if non-null, the value that must be
     * associated with key
     * @return the node, or null if not found
     * 主要的删除方法。定位节点，null值，附加一个删除标记，unlinks的前身，删除相关的索引节点，并且可能降低了头索引级别。通过调用findformer，可以清除索引节点。它将索引与从路径到key的已删除节点断开连接，该节点将包含到此节点的索引。这样做是无条件的。我们不能预先检查是否有索引节点，因为在最初搜索这个节点时，可能有一些或所有的索引还没有被插入，我们希望确保没有垃圾保留，所以必须调用来确认。
     */
    final V doRemove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v; int c;
                if (n == null)
                    break outer;
                Node<K,V> f = n.next;
                if (n != b.next)                    // inconsistent read
                    break;
                if ((v = n.value) == null) {        // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)      // b is deleted
                    break;
                if ((c = cpr(cmp, key, n.key)) < 0)
                    break outer;
                if (c > 0) {
                    b = n;
                    n = f;
                    continue;
                }
                if (value != null && !value.equals(v))
                    break outer;
                if (!n.casValue(v, null))
                    break;
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(key);                  // retry via findNode
                else {
                    findPredecessor(key, cmp);      // clean index
                    if (head.right == null)
                        tryReduceLevel();
                }
                @SuppressWarnings("unchecked") V vv = (V)v;
                return vv;
            }
        }
        return null;
    }

    /**
     * Possibly reduce head level if it has no nodes.  This method can
     * (rarely) make mistakes, in which case levels can disappear even
     * though they are about to contain index nodes. This impacts
     * performance, not correctness.  To minimize mistakes as well as
     * to reduce hysteresis, the level is reduced by one only if the
     * topmost three levels look empty. Also, if the removed level
     * looks non-empty after CAS, we try to change it back quick
     * before anyone notices our mistake! (This trick works pretty
     * well because this method will practically never make mistakes
     * unless current thread stalls immediately before first CAS, in
     * which case it is very unlikely to stall again immediately
     * afterwards, so will recover.)
     *
     * We put up with all this rather than just let levels grow
     * because otherwise, even a small map that has undergone a large
     * number of insertions and removals will have a lot of levels,
     * slowing down access more than would an occasional unwanted
     * reduction.
     * 如果没有节点，则可能降低头部。这个方法(很少)会出错，在这种情况下，即使将包含索引节点，级别也会消失。这影响性能，而不是正确性。为了减少错误，也为了减少滞后，只有当最上面的三个层次看起来是空的时候，这个层次才会减少一个。另外，如果在CAS之后删除的级别看起来不是空的，我们会在别人注意到我们的错误之前尽快更改它。(这个技巧非常有效，因为这个方法几乎不会出错，除非当前线程在第一次CAS之前立即停止，在这种情况下，它不太可能在之后再次停止，因此将恢复。)我们忍受所有这些，而不是仅仅让层次增长，因为否则，即使是一个经过大量插入和删除的小地图，也会有很多层次，比偶尔不必要的减少慢得多。
     */
    private void tryReduceLevel() {
        HeadIndex<K,V> h = head;
        HeadIndex<K,V> d;
        HeadIndex<K,V> e;
        if (h.level > 3 &&
            (d = (HeadIndex<K,V>)h.down) != null &&
            (e = (HeadIndex<K,V>)d.down) != null &&
            e.right == null &&
            d.right == null &&
            h.right == null &&
            casHead(h, d) && // try to set
            h.right != null) // recheck
            casHead(d, h);   // try to backout
    }

    /* ---------------- Finding and removing first element -------------- */

    /**
     * Specialized variant of findNode to get first valid node.findNode的特殊变体，以获得第一个有效节点。
     * @return first node or null if empty
     */
    final Node<K,V> findFirst() {
        for (Node<K,V> b, n;;) {
            if ((n = (b = head.node).next) == null)
                return null;
            if (n.value != null)
                return n;
            n.helpDelete(b, n.next);
        }
    }

    /**
     * Removes first entry; returns its snapshot.删除第一个元素;返回其快照。
     * @return null if empty, else snapshot of first entry
     */
    private Map.Entry<K,V> doRemoveFirstEntry() {
        for (Node<K,V> b, n;;) {
            if ((n = (b = head.node).next) == null)
                return null;
            Node<K,V> f = n.next;
            if (n != b.next)
                continue;
            Object v = n.value;
            if (v == null) {
                n.helpDelete(b, f);
                continue;
            }
            if (!n.casValue(v, null))
                continue;
            if (!n.appendMarker(f) || !b.casNext(n, f))
                findFirst(); // retry
            clearIndexToFirst();
            @SuppressWarnings("unchecked") V vv = (V)v;
            return new AbstractMap.SimpleImmutableEntry<K,V>(n.key, vv);
        }
    }

    /**
     * Clears out index nodes associated with deleted first entry.清除与删除的第一个条目相关联的索引节点。
     */
    private void clearIndexToFirst() {
        for (;;) {
            for (Index<K,V> q = head;;) {
                Index<K,V> r = q.right;
                if (r != null && r.indexesDeletedNode() && !q.unlink(r))
                    break;
                if ((q = q.down) == null) {
                    if (head.right == null)
                        tryReduceLevel();
                    return;
                }
            }
        }
    }

    /**
     * Removes last entry; returns its snapshot.
     * Specialized variant of doRemove.删除条目;返回其快照。专业doRemove的变体。
     * @return null if empty, else snapshot of last entry
     */
    private Map.Entry<K,V> doRemoveLastEntry() {
        for (;;) {
            Node<K,V> b = findPredecessorOfLast();
            Node<K,V> n = b.next;
            if (n == null) {
                if (b.isBaseHeader())               // empty
                    return null;
                else
                    continue; // all b's successors are deleted; retry
            }
            for (;;) {
                Node<K,V> f = n.next;
                if (n != b.next)                    // inconsistent read
                    break;
                Object v = n.value;
                if (v == null) {                    // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)      // b is deleted
                    break;
                if (f != null) {
                    b = n;
                    n = f;
                    continue;
                }
                if (!n.casValue(v, null))
                    break;
                K key = n.key;
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(key);                  // retry via findNode
                else {                              // clean index
                    findPredecessor(key, comparator);
                    if (head.right == null)
                        tryReduceLevel();
                }
                @SuppressWarnings("unchecked") V vv = (V)v;
                return new AbstractMap.SimpleImmutableEntry<K,V>(key, vv);
            }
        }
    }

    /* ---------------- Finding and removing last element -------------- */

    /**
     * Specialized version of find to get last valid node.专用版本查找到最后一个有效节点。
     * @return last node or null if empty
     */
    final Node<K,V> findLast() {
        /*
         * findPredecessor can't be used to traverse index level
         * because this doesn't use comparisons.  So traversals of
         * both levels are folded together.
         */
        Index<K,V> q = head;
        for (;;) {
            Index<K,V> d, r;
            if ((r = q.right) != null) {
                if (r.indexesDeletedNode()) {
                    q.unlink(r);
                    q = head; // restart
                }
                else
                    q = r;
            } else if ((d = q.down) != null) {
                q = d;
            } else {
                for (Node<K,V> b = q.node, n = b.next;;) {
                    if (n == null)
                        return b.isBaseHeader() ? null : b;
                    Node<K,V> f = n.next;            // inconsistent read
                    if (n != b.next)
                        break;
                    Object v = n.value;
                    if (v == null) {                 // n is deleted
                        n.helpDelete(b, f);
                        break;
                    }
                    if (b.value == null || v == n)      // b is deleted
                        break;
                    b = n;
                    n = f;
                }
                q = head; // restart
            }
        }
    }

    /**
     * Specialized variant of findPredecessor to get predecessor of last
     * valid node.  Needed when removing the last entry.  It is possible
     * that all successors of returned node will have been deleted upon
     * return, in which case this method can be retried.findformer的专门变体获取上一个有效节点的前身。删除最后一个条目时需要。返回节点的所有后续节点都可能在返回时被删除，在这种情况下可以重试此方法。
     * @return likely predecessor of last node
     */
    private Node<K,V> findPredecessorOfLast() {
        for (;;) {
            for (Index<K,V> q = head;;) {
                Index<K,V> d, r;
                if ((r = q.right) != null) {
                    if (r.indexesDeletedNode()) {
                        q.unlink(r);
                        break;    // must restart
                    }
                    // proceed as far across as possible without overshooting
                    if (r.node.next != null) {
                        q = r;
                        continue;
                    }
                }
                if ((d = q.down) != null)
                    q = d;
                else
                    return q.node;
            }
        }
    }

    /* ---------------- Relational operations -------------- */

    // Control values OR'ed as arguments to findNear

    private static final int EQ = 1;
    private static final int LT = 2;
    private static final int GT = 0; // Actually checked as !LT

    /**
     * Utility for ceiling, floor, lower, higher methods.适用于天花板、地板、较低、较高的方法。
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return nearest node fitting relation, or null if no such
     */
    final Node<K,V> findNear(K key, int rel, Comparator<? super K> cmp) {
        if (key == null)
            throw new NullPointerException();
        for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v;
                if (n == null)
                    return ((rel & LT) == 0 || b.isBaseHeader()) ? null : b;
                Node<K,V> f = n.next;
                if (n != b.next)                  // inconsistent read
                    break;
                if ((v = n.value) == null) {      // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)      // b is deleted
                    break;
                int c = cpr(cmp, key, n.key);
                if ((c == 0 && (rel & EQ) != 0) ||
                    (c <  0 && (rel & LT) == 0))
                    return n;
                if ( c <= 0 && (rel & LT) != 0)
                    return b.isBaseHeader() ? null : b;
                b = n;
                n = f;
            }
        }
    }

    /**
     * Returns SimpleImmutableEntry for results of findNear.返回findNear结果的SimpleImmutableEntry。
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return Entry fitting relation, or null if no such
     */
    final AbstractMap.SimpleImmutableEntry<K,V> getNear(K key, int rel) {
        Comparator<? super K> cmp = comparator;
        for (;;) {
            Node<K,V> n = findNear(key, rel, cmp);
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /* ---------------- Constructors -------------- */

    /**
     * Constructs a new, empty map, sorted according to the构造一个新的空映射，按照键的自然顺序进行排序。
     * {@linkplain Comparable natural ordering} of the keys.
     */
    public ConcurrentSkipListMap() {
        this.comparator = null;
        initialize();
    }

    /**
     * Constructs a new, empty map, sorted according to the specified
     * comparator.构造一个新的空映射，根据指定的比较器进行排序。
     *
     * @param comparator the comparator that will be used to order this map.
     *        If {@code null}, the {@linkplain Comparable natural
     *        ordering} of the keys will be used.
     */
    public ConcurrentSkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        initialize();
    }

    /**
     * Constructs a new map containing the same mappings as the given map,
     * sorted according to the {@linkplain Comparable natural ordering} of
     * the keys.构造一个包含与给定映射相同的映射的新映射，按照键的自然顺序进行排序。
     *
     * @param  m the map whose mappings are to be placed in this map
     * @throws ClassCastException if the keys in {@code m} are not
     *         {@link Comparable}, or are not mutually comparable
     * @throws NullPointerException if the specified map or any of its keys
     *         or values are null
     */
    public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
        this.comparator = null;
        initialize();
        putAll(m);
    }

    /**
     * Constructs a new map containing the same mappings and using the
     * same ordering as the specified sorted map.构造包含相同映射的新映射，并使用与指定排序映射相同的排序。
     *
     * @param m the sorted map whose mappings are to be placed in this
     *        map, and whose comparator is to be used to sort this map
     * @throws NullPointerException if the specified sorted map or any of
     *         its keys or values are null
     */
    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        this.comparator = m.comparator();
        initialize();
        buildFromSorted(m);
    }

    /**
     * Returns a shallow copy of this {@code ConcurrentSkipListMap}
     * instance. (The keys and values themselves are not cloned.)返回这个ConcurrentSkipListMap实例的一个浅副本。(键和值本身没有被克隆。)
     *
     * @return a shallow copy of this map
     */
    public ConcurrentSkipListMap<K,V> clone() {
        try {
            @SuppressWarnings("unchecked")
            ConcurrentSkipListMap<K,V> clone =
                (ConcurrentSkipListMap<K,V>) super.clone();
            clone.initialize();
            clone.buildFromSorted(this);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Streamlined bulk insertion to initialize from elements of
     * given sorted map.  Call only from constructor or clone
     * method.简化批量插入以初始化给定排序映射的元素。只从构造函数或克隆方法调用。
     */
    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        if (map == null)
            throw new NullPointerException();

        HeadIndex<K,V> h = head;
        Node<K,V> basepred = h.node;

        // Track the current rightmost node at each level. Uses an
        // ArrayList to avoid committing to initial or maximum level.
        ArrayList<Index<K,V>> preds = new ArrayList<Index<K,V>>();

        // initialize
        for (int i = 0; i <= h.level; ++i)
            preds.add(null);
        Index<K,V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }

        Iterator<? extends Map.Entry<? extends K, ? extends V>> it =
            map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> e = it.next();
            int rnd = ThreadLocalRandom.current().nextInt();
            int j = 0;
            if ((rnd & 0x80000001) == 0) {
                do {
                    ++j;
                } while (((rnd >>>= 1) & 1) != 0);
                if (j > h.level) j = h.level + 1;
            }
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null)
                throw new NullPointerException();
            Node<K,V> z = new Node<K,V>(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K,V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K,V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K,V>(h.node, h, idx, i);

                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    /* ---------------- Serialization -------------- */

    /**
     * Saves this map to a stream (that is, serializes it).
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     * @serialData The key (Object) and value (Object) for each
     * key-value mapping represented by the map, followed by
     * {@code null}. The key-value mappings are emitted in key-order
     * (as determined by the Comparator, or by the keys' natural
     * ordering if no Comparator).
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out the Comparator and any hidden stuff
        s.defaultWriteObject();

        // Write out keys and values (alternating)
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                s.writeObject(n.key);
                s.writeObject(v);
            }
        }
        s.writeObject(null);
    }

    /**
     * Reconstitutes this map from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in the Comparator and any hidden stuff
        s.defaultReadObject();
        // Reset transients
        initialize();

        /*
         * This is nearly identical to buildFromSorted, but is
         * distinct because readObject calls can't be nicely adapted
         * as the kind of iterator needed by buildFromSorted. (They
         * can be, but doing so requires type cheats and/or creation
         * of adaptor classes.) It is simpler to just adapt the code.
         */

        HeadIndex<K,V> h = head;
        Node<K,V> basepred = h.node;
        ArrayList<Index<K,V>> preds = new ArrayList<Index<K,V>>();
        for (int i = 0; i <= h.level; ++i)
            preds.add(null);
        Index<K,V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }

        for (;;) {
            Object k = s.readObject();
            if (k == null)
                break;
            Object v = s.readObject();
            if (v == null)
                throw new NullPointerException();
            K key = (K) k;
            V val = (V) v;
            int rnd = ThreadLocalRandom.current().nextInt();
            int j = 0;
            if ((rnd & 0x80000001) == 0) {
                do {
                    ++j;
                } while (((rnd >>>= 1) & 1) != 0);
                if (j > h.level) j = h.level + 1;
            }
            Node<K,V> z = new Node<K,V>(key, val, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K,V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K,V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K,V>(h.node, h, idx, i);

                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    /* ------ Map API methods ------ */

    /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.如果此映射包含指定键的映射，则返回true。
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     */
    public boolean containsKey(Object key) {
        return doGet(key) != null;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code key} compares
     * equal to {@code k} according to the map's ordering, then this
     * method returns {@code v}; otherwise it returns {@code null}.
     * (There can be at most one such mapping.)
     *
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     * 返回指定键映射的值，如果此映射不包含键的映射，则返回null。
    更正式地说，如果这张地图包含从一个键k到一个值v的映射，那么根据映射的顺序，键比较等于k，那么这个方法返回v;否则返回null。(这种映射最多只能有一个)。
     */
    public V get(Object key) {
        return doGet(key);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or the given defaultValue if this map contains no mapping for the key.返回指定键被映射到的值，如果此映射不包含键的映射，则返回给定的defaultValue。
     *
     * @param key the key
     * @param defaultValue the value to return if this map contains
     * no mapping for the given key
     * @return the mapping for the key, if present; else the defaultValue
     * @throws NullPointerException if the specified key is null
     * @since 1.8
     */
    public V getOrDefault(Object key, V defaultValue) {
        V v;
        return (v = doGet(key)) == null ? defaultValue : v;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.将指定的值与此映射中的指定键关联。如果映射之前包含键的映射，则替换旧值。
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key or value is null
     */
    public V put(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, false);
    }

    /**
     * Removes the mapping for the specified key from this map if present.如果存在，则从该映射中删除指定键的映射。
     *
     * @param  key key for which mapping should be removed
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     */
    public V remove(Object key) {
        return doRemove(key, null);
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the
     * specified value.  This operation requires time linear in the
     * map size. Additionally, it is possible for the map to change
     * during execution of this method, in which case the returned
     * result may be inaccurate.如果该映射将一个或多个键映射到指定值，则返回true。此操作需要在地图大小中使用时间线性。此外，在执行此方法期间，映射可能会发生更改，在这种情况下，返回的结果可能不准确。
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if a mapping to {@code value} exists;
     *         {@code false} otherwise
     * @throws NullPointerException if the specified value is null
     */
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null && value.equals(v))
                return true;
        }
        return false;
    }

    /**
     * Returns the number of key-value mappings in this map.  If this map
     * contains more than {@code Integer.MAX_VALUE} elements, it
     * returns {@code Integer.MAX_VALUE}.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these maps, determining the current
     * number of elements requires traversing them all to count them.
     * Additionally, it is possible for the size to change during
     * execution of this method, in which case the returned result
     * will be inaccurate. Thus, this method is typically not very
     * useful in concurrent applications.
     *
     * @return the number of elements in this map
     * 返回该映射中键值映射的数量。如果该映射包含多个整数。MAX_VALUE元素，它返回Integer.MAX_VALUE。
    注意，与大多数集合不同，此方法不是一个常量时间操作。由于这些映射是异步的，所以确定当前元素的数量需要遍历它们来计算它们。此外，在执行此方法期间，可能会更改大小，在这种情况下，返回的结果将不准确。因此，这种方法在并发应用程序中通常不太有用。
     */
    public int size() {
        long count = 0;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            if (n.getValidValue() != null)
                ++count;
        }
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.如果此映射不包含键值映射，则返回true。
     * @return {@code true} if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return findFirst() == null;
    }

    /**
     * Removes all of the mappings from this map.从这个映射中删除所有映射。
     */
    public void clear() {
        for (;;) {
            Node<K,V> b, n;
            HeadIndex<K,V> h = head, d = (HeadIndex<K,V>)h.down;
            if (d != null)
                casHead(h, d);            // remove levels
            else if ((b = h.node) != null && (n = b.next) != null) {
                Node<K,V> f = n.next;     // remove values
                if (n == b.next) {
                    Object v = n.value;
                    if (v == null)
                        n.helpDelete(b, f);
                    else if (n.casValue(v, null) && n.appendMarker(f))
                        b.casNext(n, f);
                }
            }
            else
                break;
        }
    }

    /**
     * If the specified key is not already associated with a value,
     * attempts to compute its value using the given mapping function
     * and enters it into this map unless {@code null}.  The function
     * is <em>NOT</em> guaranteed to be applied once atomically only
     * if the value is not present.
     *
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with
     *         the specified key, or null if the computed value is null
     * @throws NullPointerException if the specified key is null
     *         or the mappingFunction is null
     *
     * @since 1.8
     * 如果指定的键尚未与值关联，则尝试使用给定的映射函数计算其值，并将其输入到此映射中，除非为null。只有当值不存在时，才能保证函数被原子化地应用一次。
     */
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null)
            throw new NullPointerException();
        V v, p, r;
        if ((v = doGet(key)) == null &&
            (r = mappingFunction.apply(key)) != null)
            v = (p = doPut(key, r, true)) == null ? r : p;
        return v;
    }

    /**
     * If the value for the specified key is present, attempts to
     * compute a new mapping given the key and its current mapped
     * value. The function is <em>NOT</em> guaranteed to be applied
     * once atomically.
     *
     * @param key key with which a value may be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null
     *         or the remappingFunction is null
     * @since 1.8
     * 如果指定键的值存在，则尝试计算给定键及其当前映射值的新映射。这个函数不能保证在原子上应用一次。
     */
    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> n; Object v;
        while ((n = findNode(key)) != null) {
            if ((v = n.value) != null) {
                @SuppressWarnings("unchecked") V vv = (V) v;
                V r = remappingFunction.apply(key, vv);
                if (r != null) {
                    if (n.casValue(vv, r))
                        return r;
                }
                else if (doRemove(key, vv) != null)
                    break;
            }
        }
        return null;
    }

    /**
     * Attempts to compute a mapping for the specified key and its
     * current mapped value (or {@code null} if there is no current
     * mapping). The function is <em>NOT</em> guaranteed to be applied
     * once atomically.
     *
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null
     *         or the remappingFunction is null
     * @since 1.8
     * 尝试计算指定键及其当前映射值的映射(如果没有当前映射，则为null)。这个函数不能保证在原子上应用一次。
     */
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v; V r;
            if ((n = findNode(key)) == null) {
                if ((r = remappingFunction.apply(key, null)) == null)
                    break;
                if (doPut(key, r, true) == null)
                    return r;
            }
            else if ((v = n.value) != null) {
                @SuppressWarnings("unchecked") V vv = (V) v;
                if ((r = remappingFunction.apply(key, vv)) != null) {
                    if (n.casValue(vv, r))
                        return r;
                }
                else if (doRemove(key, vv) != null)
                    break;
            }
        }
        return null;
    }

    /**
     * If the specified key is not already associated with a value,
     * associates it with the given value.  Otherwise, replaces the
     * value with the results of the given remapping function, or
     * removes if {@code null}. The function is <em>NOT</em>
     * guaranteed to be applied once atomically.
     *
     * @param key key with which the specified value is to be associated
     * @param value the value to use if absent
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key or value is null
     *         or the remappingFunction is null
     * @since 1.8
     * 如果指定的键尚未与值关联，则将其与给定值关联。否则，将该值替换为给定重新映射函数的结果，或者删除null。这个函数不能保证在原子上应用一次。
     */
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v; V r;
            if ((n = findNode(key)) == null) {
                if (doPut(key, value, true) == null)
                    return value;
            }
            else if ((v = n.value) != null) {
                @SuppressWarnings("unchecked") V vv = (V) v;
                if ((r = remappingFunction.apply(vv, value)) != null) {
                    if (n.casValue(vv, r))
                        return r;
                }
                else if (doRemove(key, vv) != null)
                    return null;
            }
        }
    }

    /* ---------------- View methods -------------- */

    /*
     * Note: Lazy initialization works for views because view classes
     * are stateless/immutable so it doesn't matter wrt correctness if
     * more than one is created (which will only rarely happen).  Even
     * so, the following idiom conservatively ensures that the method
     * returns the one it created if it does so, not one created by
     * another racing thread.
     */

    /**
     * Returns a {@link NavigableSet} view of the keys contained in this map.
     *
     * <p>The set's iterator returns the keys in ascending order.
     * The set's spliterator additionally reports {@link Spliterator#CONCURRENT},
     * {@link Spliterator#NONNULL}, {@link Spliterator#SORTED} and
     * {@link Spliterator#ORDERED}, with an encounter order that is ascending
     * key order.  The spliterator's comparator (see
     * {@link java.util.Spliterator#getComparator()}) is {@code null} if
     * the map's comparator (see {@link #comparator()}) is {@code null}.
     * Otherwise, the spliterator's comparator is the same as or imposes the
     * same total ordering as the map's comparator.
     *
     * <p>The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from the map,
     * via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear}
     * operations.  It does not support the {@code add} or {@code addAll}
     * operations.
     *
     * <p>The view's iterators and spliterators are
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>This method is equivalent to method {@code navigableKeySet}.
     *
     * @return a navigable set view of the keys in this map
     * 返回此映射中包含的键的NavigableSet视图。
    集合的迭代器按升序返回键。另外，set的spliterator会报告spliterator。并发,Spliterator。null,Spliterator。排序和Spliterator。有顺序的，遇到的顺序是上升键顺序。如果映射的比较器(请参见spliterator . getcomparator()))为空，则spliterator的比较器(请参见comparator()))为空。否则，spliterator的比较器与映射的比较器相同或强加相同的总排序。
    集合由映射支持，因此映射的更改反映在集合中，反之亦然。该集合支持元素删除，通过迭代器从映射中删除相应的映射。删除，删除，删除，保留所有，并清除操作。它不支持add或addAll操作。
    视图的迭代器和spliterator是弱一致的。
    该方法等价于navigableKeySet方法。
     */
    public NavigableSet<K> keySet() {
        KeySet<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet<K>(this));
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet<K>(this));
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * <p>The collection's iterator returns the values in ascending order
     * of the corresponding keys. The collections's spliterator additionally
     * reports {@link Spliterator#CONCURRENT}, {@link Spliterator#NONNULL} and
     * {@link Spliterator#ORDERED}, with an encounter order that is ascending
     * order of the corresponding keys.
     *
     * <p>The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Collection.remove}, {@code removeAll},
     * {@code retainAll} and {@code clear} operations.  It does not
     * support the {@code add} or {@code addAll} operations.
     *
     * <p>The view's iterators and spliterators are
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     * 返回包含在此映射中的值的集合视图。
     集合的迭代器以相应键的升序返回值。此外，集合的spliterator还报告了spliterator。并发,Spliterator。null和Spliterator。按顺序排列，按相应键的升序排列。
     集合由映射支持，因此对映射的更改反映在集合中，反之亦然。该集合支持元素删除，通过迭代器从映射中删除相应的映射。删除收藏。移除、移除、保留和清除操作。它不支持add或addAll操作。
     视图的迭代器和spliterator是弱一致的。
     */
    public Collection<V> values() {
        Values<V> vs = values;
        return (vs != null) ? vs : (values = new Values<V>(this));
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     *
     * <p>The set's iterator returns the entries in ascending key order.  The
     * set's spliterator additionally reports {@link Spliterator#CONCURRENT},
     * {@link Spliterator#NONNULL}, {@link Spliterator#SORTED} and
     * {@link Spliterator#ORDERED}, with an encounter order that is ascending
     * key order.
     *
     * <p>The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from the map,
     * via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll} and {@code clear}
     * operations.  It does not support the {@code add} or
     * {@code addAll} operations.
     *
     * <p>The view's iterators and spliterators are
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>The {@code Map.Entry} elements traversed by the {@code iterator}
     * or {@code spliterator} do <em>not</em> support the {@code setValue}
     * operation.
     *
     * @return a set view of the mappings contained in this map,
     *         sorted in ascending key order
     *         返回该映射中包含的映射的集合视图。
    集合的迭代器以升序键顺序返回条目。另外，set的spliterator会报告spliterator。并发,Spliterator。null,Spliterator。排序和Spliterator。有顺序的，遇到的顺序是上升键顺序。
    集合由映射支持，因此映射的更改反映在集合中，反之亦然。该集合支持元素删除，通过迭代器从映射中删除相应的映射。删除，删除，删除，删除，保留，清除操作。它不支持add或addAll操作。
    视图的迭代器和spliterator是弱一致的。
    地图。迭代器或spliterator遍历的条目元素不支持setValue操作。
     */
    public Set<Map.Entry<K,V>> entrySet() {
        EntrySet<K,V> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet<K,V>(this));
    }

    public ConcurrentNavigableMap<K,V> descendingMap() {
        ConcurrentNavigableMap<K,V> dm = descendingMap;
        return (dm != null) ? dm : (descendingMap = new SubMap<K,V>
                                    (this, null, false, null, false, true));
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    /* ---------------- AbstractMap Overrides -------------- */

    /**
     * Compares the specified object with this map for equality.
     * Returns {@code true} if the given object is also a map and the
     * two maps represent the same mappings.  More formally, two maps
     * {@code m1} and {@code m2} represent the same mappings if
     * {@code m1.entrySet().equals(m2.entrySet())}.  This
     * operation may return misleading results if either map is
     * concurrently modified during execution of this method.
     *
     * @param o object to be compared for equality with this map
     * @return {@code true} if the specified object is equal to this map
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        Map<?,?> m = (Map<?,?>) o;
        try {
            for (Map.Entry<K,V> e : this.entrySet())
                if (! e.getValue().equals(m.get(e.getKey())))
                    return false;
            for (Map.Entry<?,?> e : m.entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();
                if (k == null || v == null || !v.equals(get(k)))
                    return false;
            }
            return true;
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /* ------ ConcurrentMap API methods ------ */

    /**
     * {@inheritDoc}如果指定的键尚未与值关联(或映射为null)，则将其与给定值关联并返回null，否则将返回当前值。
     *
     * @return the previous value associated with the specified key,
     *         or {@code null} if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key or value is null
     */
    public V putIfAbsent(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, true);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     */
    public boolean remove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        return value != null && doRemove(key, value) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if any of the arguments are null
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v;
            if ((n = findNode(key)) == null)
                return false;
            if ((v = n.value) != null) {
                if (!oldValue.equals(v))
                    return false;
                if (n.casValue(v, newValue))
                    return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key,
     *         or {@code null} if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key or value is null
     */
    public V replace(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v;
            if ((n = findNode(key)) == null)
                return null;
            if ((v = n.value) != null && n.casValue(v, value)) {
                @SuppressWarnings("unchecked") V vv = (V)v;
                return vv;
            }
        }
    }

    /* ------ SortedMap API methods ------ */

    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public K firstKey() {
        Node<K,V> n = findFirst();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public K lastKey() {
        Node<K,V> n = findLast();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> subMap(K fromKey,
                                              boolean fromInclusive,
                                              K toKey,
                                              boolean toInclusive) {
        if (fromKey == null || toKey == null)
            throw new NullPointerException();
        return new SubMap<K,V>
            (this, fromKey, fromInclusive, toKey, toInclusive, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code toKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> headMap(K toKey,
                                               boolean inclusive) {
        if (toKey == null)
            throw new NullPointerException();
        return new SubMap<K,V>
            (this, null, false, toKey, inclusive, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> tailMap(K fromKey,
                                               boolean inclusive) {
        if (fromKey == null)
            throw new NullPointerException();
        return new SubMap<K,V>
            (this, fromKey, inclusive, null, false, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code toKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    /* ---------------- Relational operations -------------- */

    /**
     * Returns a key-value mapping associated with the greatest key
     * strictly less than the given key, or {@code null} if there is
     * no such key. The returned entry does <em>not</em> support the
     * {@code Entry.setValue} method.如果没有这样的键，则返回与最大键相关的键值映射，严格小于给定的键值。返回的条目不支持条目。setValue方法。
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public Map.Entry<K,V> lowerEntry(K key) {
        return getNear(key, LT);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public K lowerKey(K key) {
        Node<K,V> n = findNear(key, LT, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or {@code null} if there
     * is no such key. The returned entry does <em>not</em> support
     * the {@code Entry.setValue} method.返回与最大键相关联的键值映射，该键小于或等于给定键，如果没有该键，则返回null。返回的条目不支持条目。setValue方法。
     *
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public Map.Entry<K,V> floorEntry(K key) {
        return getNear(key, LT|EQ);
    }

    /**
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public K floorKey(K key) {
        Node<K,V> n = findNear(key, LT|EQ, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the least key
     * greater than or equal to the given key, or {@code null} if
     * there is no such entry. The returned entry does <em>not</em>
     * support the {@code Entry.setValue} method.返回与大于或等于给定键的最小键值关联的键值映射，如果没有这样的条目，则返回null。返回的条目不支持条目。setValue方法。
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public Map.Entry<K,V> ceilingEntry(K key) {
        return getNear(key, GT|EQ);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public K ceilingKey(K key) {
        Node<K,V> n = findNear(key, GT|EQ, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the least key
     * strictly greater than the given key, or {@code null} if there
     * is no such key. The returned entry does <em>not</em> support
     * the {@code Entry.setValue} method.返回与最小键相关的键值映射，该映射严格大于给定键，如果没有该键，则返回null。返回的条目不支持条目。setValue方法。
     *
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public Map.Entry<K,V> higherEntry(K key) {
        return getNear(key, GT);
    }

    /**
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public K higherKey(K key) {
        Node<K,V> n = findNear(key, GT, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the least
     * key in this map, or {@code null} if the map is empty.
     * The returned entry does <em>not</em> support
     * the {@code Entry.setValue} method.返回与此映射中的最小键值关联的键值映射，如果映射为空，则返回null。返回的条目不支持条目。setValue方法。
     */
    public Map.Entry<K,V> firstEntry() {
        for (;;) {
            Node<K,V> n = findFirst();
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * Returns a key-value mapping associated with the greatest
     * key in this map, or {@code null} if the map is empty.
     * The returned entry does <em>not</em> support
     * the {@code Entry.setValue} method.返回与此映射中最大键关联的键值映射，如果映射为空，则返回null。返回的条目不支持条目。setValue方法。
     */
    public Map.Entry<K,V> lastEntry() {
        for (;;) {
            Node<K,V> n = findLast();
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * Removes and returns a key-value mapping associated with
     * the least key in this map, or {@code null} if the map is empty.
     * The returned entry does <em>not</em> support
     * the {@code Entry.setValue} method.删除并返回与此映射中的最小键相关的键值映射，如果映射为空则为null。返回的条目不支持条目。setValue方法。
     */
    public Map.Entry<K,V> pollFirstEntry() {
        return doRemoveFirstEntry();
    }

    /**
     * Removes and returns a key-value mapping associated with
     * the greatest key in this map, or {@code null} if the map is empty.
     * The returned entry does <em>not</em> support
     * the {@code Entry.setValue} method.删除并返回与此映射中最大键相关联的键值映射，如果映射为空则为空。返回的条目不支持条目。setValue方法。
     */
    public Map.Entry<K,V> pollLastEntry() {
        return doRemoveLastEntry();
    }


    /* ---------------- Iterators -------------- */

    /**
     * Base of iterator classes:
     */
    abstract class Iter<T> implements Iterator<T> {
        /** the last node returned by next() */
        Node<K,V> lastReturned;
        /** the next node to return from next(); */
        Node<K,V> next;
        /** Cache of next value field to maintain weak consistency */
        V nextValue;

        /** Initializes ascending iterator for entire range. */
        Iter() {
            while ((next = findFirst()) != null) {
                Object x = next.value;
                if (x != null && x != next) {
                    @SuppressWarnings("unchecked") V vv = (V)x;
                    nextValue = vv;
                    break;
                }
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        /** Advances next to higher entry. */
        final void advance() {
            if (next == null)
                throw new NoSuchElementException();
            lastReturned = next;
            while ((next = next.next) != null) {
                Object x = next.value;
                if (x != null && x != next) {
                    @SuppressWarnings("unchecked") V vv = (V)x;
                    nextValue = vv;
                    break;
                }
            }
        }

        public void remove() {
            Node<K,V> l = lastReturned;
            if (l == null)
                throw new IllegalStateException();
            // It would not be worth all of the overhead to directly
            // unlink from here. Using remove is fast enough.
            ConcurrentSkipListMap.this.remove(l.key);
            lastReturned = null;
        }

    }

    final class ValueIterator extends Iter<V> {
        public V next() {
            V v = nextValue;
            advance();
            return v;
        }
    }

    final class KeyIterator extends Iter<K> {
        public K next() {
            Node<K,V> n = next;
            advance();
            return n.key;
        }
    }

    final class EntryIterator extends Iter<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            Node<K,V> n = next;
            V v = nextValue;
            advance();
            return new AbstractMap.SimpleImmutableEntry<K,V>(n.key, v);
        }
    }

    // Factory methods for iterators needed by ConcurrentSkipListSet etc

    Iterator<K> keyIterator() {
        return new KeyIterator();
    }

    Iterator<V> valueIterator() {
        return new ValueIterator();
    }

    Iterator<Map.Entry<K,V>> entryIterator() {
        return new EntryIterator();
    }

    /* ---------------- View Classes -------------- */

    /*
     * View classes are static, delegating to a ConcurrentNavigableMap
     * to allow use by SubMaps, which outweighs the ugliness of
     * needing type-tests for Iterator methods.
     */

    static final <E> List<E> toList(Collection<E> c) {
        // Using size() here would be a pessimization.
        ArrayList<E> list = new ArrayList<E>();
        for (E e : c)
            list.add(e);
        return list;
    }

    static final class KeySet<E>
            extends AbstractSet<E> implements NavigableSet<E> {
        final ConcurrentNavigableMap<E,?> m;
        KeySet(ConcurrentNavigableMap<E,?> map) { m = map; }
        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public boolean remove(Object o) { return m.remove(o) != null; }
        public void clear() { m.clear(); }
        public E lower(E e) { return m.lowerKey(e); }
        public E floor(E e) { return m.floorKey(e); }
        public E ceiling(E e) { return m.ceilingKey(e); }
        public E higher(E e) { return m.higherKey(e); }
        public Comparator<? super E> comparator() { return m.comparator(); }
        public E first() { return m.firstKey(); }
        public E last() { return m.lastKey(); }
        public E pollFirst() {
            Map.Entry<E,?> e = m.pollFirstEntry();
            return (e == null) ? null : e.getKey();
        }
        public E pollLast() {
            Map.Entry<E,?> e = m.pollLastEntry();
            return (e == null) ? null : e.getKey();
        }
        @SuppressWarnings("unchecked")
        public Iterator<E> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<E,Object>)m).keyIterator();
            else
                return ((ConcurrentSkipListMap.SubMap<E,Object>)m).keyIterator();
        }
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }
        public Object[] toArray()     { return toList(this).toArray();  }
        public <T> T[] toArray(T[] a) { return toList(this).toArray(a); }
        public Iterator<E> descendingIterator() {
            return descendingSet().iterator();
        }
        public NavigableSet<E> subSet(E fromElement,
                                      boolean fromInclusive,
                                      E toElement,
                                      boolean toInclusive) {
            return new KeySet<E>(m.subMap(fromElement, fromInclusive,
                                          toElement,   toInclusive));
        }
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new KeySet<E>(m.headMap(toElement, inclusive));
        }
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new KeySet<E>(m.tailMap(fromElement, inclusive));
        }
        public NavigableSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }
        public NavigableSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }
        public NavigableSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }
        public NavigableSet<E> descendingSet() {
            return new KeySet<E>(m.descendingMap());
        }
        @SuppressWarnings("unchecked")
        public Spliterator<E> spliterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<E,?>)m).keySpliterator();
            else
                return (Spliterator<E>)((SubMap<E,?>)m).keyIterator();
        }
    }

    static final class Values<E> extends AbstractCollection<E> {
        final ConcurrentNavigableMap<?, E> m;
        Values(ConcurrentNavigableMap<?, E> map) {
            m = map;
        }
        @SuppressWarnings("unchecked")
        public Iterator<E> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<?,E>)m).valueIterator();
            else
                return ((SubMap<?,E>)m).valueIterator();
        }
        public boolean isEmpty() {
            return m.isEmpty();
        }
        public int size() {
            return m.size();
        }
        public boolean contains(Object o) {
            return m.containsValue(o);
        }
        public void clear() {
            m.clear();
        }
        public Object[] toArray()     { return toList(this).toArray();  }
        public <T> T[] toArray(T[] a) { return toList(this).toArray(a); }
        @SuppressWarnings("unchecked")
        public Spliterator<E> spliterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<?,E>)m).valueSpliterator();
            else
                return (Spliterator<E>)((SubMap<?,E>)m).valueIterator();
        }
    }

    static final class EntrySet<K1,V1> extends AbstractSet<Map.Entry<K1,V1>> {
        final ConcurrentNavigableMap<K1, V1> m;
        EntrySet(ConcurrentNavigableMap<K1, V1> map) {
            m = map;
        }
        @SuppressWarnings("unchecked")
        public Iterator<Map.Entry<K1,V1>> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<K1,V1>)m).entryIterator();
            else
                return ((SubMap<K1,V1>)m).entryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            V1 v = m.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            return m.remove(e.getKey(),
                            e.getValue());
        }
        public boolean isEmpty() {
            return m.isEmpty();
        }
        public int size() {
            return m.size();
        }
        public void clear() {
            m.clear();
        }
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }
        public Object[] toArray()     { return toList(this).toArray();  }
        public <T> T[] toArray(T[] a) { return toList(this).toArray(a); }
        @SuppressWarnings("unchecked")
        public Spliterator<Map.Entry<K1,V1>> spliterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<K1,V1>)m).entrySpliterator();
            else
                return (Spliterator<Map.Entry<K1,V1>>)
                    ((SubMap<K1,V1>)m).entryIterator();
        }
    }

    /**
     * Submaps returned by {@link ConcurrentSkipListMap} submap operations
     * represent a subrange of mappings of their underlying
     * maps. Instances of this class support all methods of their
     * underlying maps, differing in that mappings outside their range are
     * ignored, and attempts to add mappings outside their ranges result
     * in {@link IllegalArgumentException}.  Instances of this class are
     * constructed only using the {@code subMap}, {@code headMap}, and
     * {@code tailMap} methods of their underlying maps.
     * ConcurrentSkipListMap操作返回的子映射表示它们的底层映射的一个子映射。该类的实例支持其底层映射的所有方法，不同之处在于忽略其范围之外的映射，并且尝试在其范围之外添加映射会导致IllegalArgumentException。该类的实例只使用其底层映射的子映射、头映射和尾映射方法构造。
     *
     * @serial include
     */
    static final class SubMap<K,V> extends AbstractMap<K,V>
        implements ConcurrentNavigableMap<K,V>, Cloneable, Serializable {
        private static final long serialVersionUID = -7647078645895051609L;

        /** Underlying map */
        private final ConcurrentSkipListMap<K,V> m;
        /** lower bound key, or null if from start */
        private final K lo;
        /** upper bound key, or null if to end */
        private final K hi;
        /** inclusion flag for lo */
        private final boolean loInclusive;
        /** inclusion flag for hi */
        private final boolean hiInclusive;
        /** direction */
        private final boolean isDescending;

        // Lazily initialized view holders
        private transient KeySet<K> keySetView;
        private transient Set<Map.Entry<K,V>> entrySetView;
        private transient Collection<V> valuesView;

        /**
         * Creates a new submap, initializing all fields.
         */
        SubMap(ConcurrentSkipListMap<K,V> map,
               K fromKey, boolean fromInclusive,
               K toKey, boolean toInclusive,
               boolean isDescending) {
            Comparator<? super K> cmp = map.comparator;
            if (fromKey != null && toKey != null &&
                cpr(cmp, fromKey, toKey) > 0)
                throw new IllegalArgumentException("inconsistent range");
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
        }

        /* ----------------  Utilities -------------- */

        boolean tooLow(Object key, Comparator<? super K> cmp) {
            int c;
            return (lo != null && ((c = cpr(cmp, key, lo)) < 0 ||
                                   (c == 0 && !loInclusive)));
        }

        boolean tooHigh(Object key, Comparator<? super K> cmp) {
            int c;
            return (hi != null && ((c = cpr(cmp, key, hi)) > 0 ||
                                   (c == 0 && !hiInclusive)));
        }

        boolean inBounds(Object key, Comparator<? super K> cmp) {
            return !tooLow(key, cmp) && !tooHigh(key, cmp);
        }

        void checkKeyBounds(K key, Comparator<? super K> cmp) {
            if (key == null)
                throw new NullPointerException();
            if (!inBounds(key, cmp))
                throw new IllegalArgumentException("key out of range");
        }

        /**
         * Returns true if node key is less than upper bound of range.
         */
        boolean isBeforeEnd(ConcurrentSkipListMap.Node<K,V> n,
                            Comparator<? super K> cmp) {
            if (n == null)
                return false;
            if (hi == null)
                return true;
            K k = n.key;
            if (k == null) // pass by markers and headers
                return true;
            int c = cpr(cmp, k, hi);
            if (c > 0 || (c == 0 && !hiInclusive))
                return false;
            return true;
        }

        /**
         * Returns lowest node. This node might not be in range, so
         * most usages need to check bounds.
         */
        ConcurrentSkipListMap.Node<K,V> loNode(Comparator<? super K> cmp) {
            if (lo == null)
                return m.findFirst();
            else if (loInclusive)
                return m.findNear(lo, GT|EQ, cmp);
            else
                return m.findNear(lo, GT, cmp);
        }

        /**
         * Returns highest node. This node might not be in range, so
         * most usages need to check bounds.
         */
        ConcurrentSkipListMap.Node<K,V> hiNode(Comparator<? super K> cmp) {
            if (hi == null)
                return m.findLast();
            else if (hiInclusive)
                return m.findNear(hi, LT|EQ, cmp);
            else
                return m.findNear(hi, LT, cmp);
        }

        /**
         * Returns lowest absolute key (ignoring directonality).
         */
        K lowestKey() {
            Comparator<? super K> cmp = m.comparator;
            ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
            if (isBeforeEnd(n, cmp))
                return n.key;
            else
                throw new NoSuchElementException();
        }

        /**
         * Returns highest absolute key (ignoring directonality).
         */
        K highestKey() {
            Comparator<? super K> cmp = m.comparator;
            ConcurrentSkipListMap.Node<K,V> n = hiNode(cmp);
            if (n != null) {
                K last = n.key;
                if (inBounds(last, cmp))
                    return last;
            }
            throw new NoSuchElementException();
        }

        Map.Entry<K,V> lowestEntry() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                if (!isBeforeEnd(n, cmp))
                    return null;
                Map.Entry<K,V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        Map.Entry<K,V> highestEntry() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                ConcurrentSkipListMap.Node<K,V> n = hiNode(cmp);
                if (n == null || !inBounds(n.key, cmp))
                    return null;
                Map.Entry<K,V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        Map.Entry<K,V> removeLowest() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                Node<K,V> n = loNode(cmp);
                if (n == null)
                    return null;
                K k = n.key;
                if (!inBounds(k, cmp))
                    return null;
                V v = m.doRemove(k, null);
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K,V>(k, v);
            }
        }

        Map.Entry<K,V> removeHighest() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                Node<K,V> n = hiNode(cmp);
                if (n == null)
                    return null;
                K k = n.key;
                if (!inBounds(k, cmp))
                    return null;
                V v = m.doRemove(k, null);
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K,V>(k, v);
            }
        }

        /**
         * Submap version of ConcurrentSkipListMap.getNearEntry
         */
        Map.Entry<K,V> getNearEntry(K key, int rel) {
            Comparator<? super K> cmp = m.comparator;
            if (isDescending) { // adjust relation for direction
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp))
                return ((rel & LT) != 0) ? null : lowestEntry();
            if (tooHigh(key, cmp))
                return ((rel & LT) != 0) ? highestEntry() : null;
            for (;;) {
                Node<K,V> n = m.findNear(key, rel, cmp);
                if (n == null || !inBounds(n.key, cmp))
                    return null;
                K k = n.key;
                V v = n.getValidValue();
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K,V>(k, v);
            }
        }

        // Almost the same as getNearEntry, except for keys
        K getNearKey(K key, int rel) {
            Comparator<? super K> cmp = m.comparator;
            if (isDescending) { // adjust relation for direction
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp)) {
                if ((rel & LT) == 0) {
                    ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                    if (isBeforeEnd(n, cmp))
                        return n.key;
                }
                return null;
            }
            if (tooHigh(key, cmp)) {
                if ((rel & LT) != 0) {
                    ConcurrentSkipListMap.Node<K,V> n = hiNode(cmp);
                    if (n != null) {
                        K last = n.key;
                        if (inBounds(last, cmp))
                            return last;
                    }
                }
                return null;
            }
            for (;;) {
                Node<K,V> n = m.findNear(key, rel, cmp);
                if (n == null || !inBounds(n.key, cmp))
                    return null;
                K k = n.key;
                V v = n.getValidValue();
                if (v != null)
                    return k;
            }
        }

        /* ----------------  Map API methods -------------- */

        public boolean containsKey(Object key) {
            if (key == null) throw new NullPointerException();
            return inBounds(key, m.comparator) && m.containsKey(key);
        }

        public V get(Object key) {
            if (key == null) throw new NullPointerException();
            return (!inBounds(key, m.comparator)) ? null : m.get(key);
        }

        public V put(K key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.put(key, value);
        }

        public V remove(Object key) {
            return (!inBounds(key, m.comparator)) ? null : m.remove(key);
        }

        public int size() {
            Comparator<? super K> cmp = m.comparator;
            long count = 0;
            for (ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                 isBeforeEnd(n, cmp);
                 n = n.next) {
                if (n.getValidValue() != null)
                    ++count;
            }
            return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)count;
        }

        public boolean isEmpty() {
            Comparator<? super K> cmp = m.comparator;
            return !isBeforeEnd(loNode(cmp), cmp);
        }

        public boolean containsValue(Object value) {
            if (value == null)
                throw new NullPointerException();
            Comparator<? super K> cmp = m.comparator;
            for (ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                 isBeforeEnd(n, cmp);
                 n = n.next) {
                V v = n.getValidValue();
                if (v != null && value.equals(v))
                    return true;
            }
            return false;
        }

        public void clear() {
            Comparator<? super K> cmp = m.comparator;
            for (ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                 isBeforeEnd(n, cmp);
                 n = n.next) {
                if (n.getValidValue() != null)
                    m.remove(n.key);
            }
        }

        /* ----------------  ConcurrentMap API methods -------------- */

        public V putIfAbsent(K key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.putIfAbsent(key, value);
        }

        public boolean remove(Object key, Object value) {
            return inBounds(key, m.comparator) && m.remove(key, value);
        }

        public boolean replace(K key, V oldValue, V newValue) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, oldValue, newValue);
        }

        public V replace(K key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, value);
        }

        /* ----------------  SortedMap API methods -------------- */

        public Comparator<? super K> comparator() {
            Comparator<? super K> cmp = m.comparator();
            if (isDescending)
                return Collections.reverseOrder(cmp);
            else
                return cmp;
        }

        /**
         * Utility to create submaps, where given bounds override
         * unbounded(null) ones and/or are checked against bounded ones.
         */
        SubMap<K,V> newSubMap(K fromKey, boolean fromInclusive,
                              K toKey, boolean toInclusive) {
            Comparator<? super K> cmp = m.comparator;
            if (isDescending) { // flip senses
                K tk = fromKey;
                fromKey = toKey;
                toKey = tk;
                boolean ti = fromInclusive;
                fromInclusive = toInclusive;
                toInclusive = ti;
            }
            if (lo != null) {
                if (fromKey == null) {
                    fromKey = lo;
                    fromInclusive = loInclusive;
                }
                else {
                    int c = cpr(cmp, fromKey, lo);
                    if (c < 0 || (c == 0 && !loInclusive && fromInclusive))
                        throw new IllegalArgumentException("key out of range");
                }
            }
            if (hi != null) {
                if (toKey == null) {
                    toKey = hi;
                    toInclusive = hiInclusive;
                }
                else {
                    int c = cpr(cmp, toKey, hi);
                    if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
                        throw new IllegalArgumentException("key out of range");
                }
            }
            return new SubMap<K,V>(m, fromKey, fromInclusive,
                                   toKey, toInclusive, isDescending);
        }

        public SubMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                  K toKey, boolean toInclusive) {
            if (fromKey == null || toKey == null)
                throw new NullPointerException();
            return newSubMap(fromKey, fromInclusive, toKey, toInclusive);
        }

        public SubMap<K,V> headMap(K toKey, boolean inclusive) {
            if (toKey == null)
                throw new NullPointerException();
            return newSubMap(null, false, toKey, inclusive);
        }

        public SubMap<K,V> tailMap(K fromKey, boolean inclusive) {
            if (fromKey == null)
                throw new NullPointerException();
            return newSubMap(fromKey, inclusive, null, false);
        }

        public SubMap<K,V> subMap(K fromKey, K toKey) {
            return subMap(fromKey, true, toKey, false);
        }

        public SubMap<K,V> headMap(K toKey) {
            return headMap(toKey, false);
        }

        public SubMap<K,V> tailMap(K fromKey) {
            return tailMap(fromKey, true);
        }

        public SubMap<K,V> descendingMap() {
            return new SubMap<K,V>(m, lo, loInclusive,
                                   hi, hiInclusive, !isDescending);
        }

        /* ----------------  Relational methods -------------- */

        public Map.Entry<K,V> ceilingEntry(K key) {
            return getNearEntry(key, GT|EQ);
        }

        public K ceilingKey(K key) {
            return getNearKey(key, GT|EQ);
        }

        public Map.Entry<K,V> lowerEntry(K key) {
            return getNearEntry(key, LT);
        }

        public K lowerKey(K key) {
            return getNearKey(key, LT);
        }

        public Map.Entry<K,V> floorEntry(K key) {
            return getNearEntry(key, LT|EQ);
        }

        public K floorKey(K key) {
            return getNearKey(key, LT|EQ);
        }

        public Map.Entry<K,V> higherEntry(K key) {
            return getNearEntry(key, GT);
        }

        public K higherKey(K key) {
            return getNearKey(key, GT);
        }

        public K firstKey() {
            return isDescending ? highestKey() : lowestKey();
        }

        public K lastKey() {
            return isDescending ? lowestKey() : highestKey();
        }

        public Map.Entry<K,V> firstEntry() {
            return isDescending ? highestEntry() : lowestEntry();
        }

        public Map.Entry<K,V> lastEntry() {
            return isDescending ? lowestEntry() : highestEntry();
        }

        public Map.Entry<K,V> pollFirstEntry() {
            return isDescending ? removeHighest() : removeLowest();
        }

        public Map.Entry<K,V> pollLastEntry() {
            return isDescending ? removeLowest() : removeHighest();
        }

        /* ---------------- Submap Views -------------- */

        public NavigableSet<K> keySet() {
            KeySet<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySet<K>(this));
        }

        public NavigableSet<K> navigableKeySet() {
            KeySet<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySet<K>(this));
        }

        public Collection<V> values() {
            Collection<V> vs = valuesView;
            return (vs != null) ? vs : (valuesView = new Values<V>(this));
        }

        public Set<Map.Entry<K,V>> entrySet() {
            Set<Map.Entry<K,V>> es = entrySetView;
            return (es != null) ? es : (entrySetView = new EntrySet<K,V>(this));
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        Iterator<K> keyIterator() {
            return new SubMapKeyIterator();
        }

        Iterator<V> valueIterator() {
            return new SubMapValueIterator();
        }

        Iterator<Map.Entry<K,V>> entryIterator() {
            return new SubMapEntryIterator();
        }

        /**
         * Variant of main Iter class to traverse through submaps.
         * Also serves as back-up Spliterator for views通过子映射遍历主要Iter类的变体。还可以作为视图的备份文件
         */
        abstract class SubMapIter<T> implements Iterator<T>, Spliterator<T> {
            /** the last node returned by next() */
            Node<K,V> lastReturned;
            /** the next node to return from next(); */
            Node<K,V> next;
            /** Cache of next value field to maintain weak consistency */
            V nextValue;

            SubMapIter() {
                Comparator<? super K> cmp = m.comparator;
                for (;;) {
                    next = isDescending ? hiNode(cmp) : loNode(cmp);
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (! inBounds(next.key, cmp))
                            next = null;
                        else {
                            @SuppressWarnings("unchecked") V vv = (V)x;
                            nextValue = vv;
                        }
                        break;
                    }
                }
            }

            public final boolean hasNext() {
                return next != null;
            }

            final void advance() {
                if (next == null)
                    throw new NoSuchElementException();
                lastReturned = next;
                if (isDescending)
                    descend();
                else
                    ascend();
            }

            private void ascend() {
                Comparator<? super K> cmp = m.comparator;
                for (;;) {
                    next = next.next;
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (tooHigh(next.key, cmp))
                            next = null;
                        else {
                            @SuppressWarnings("unchecked") V vv = (V)x;
                            nextValue = vv;
                        }
                        break;
                    }
                }
            }

            private void descend() {
                Comparator<? super K> cmp = m.comparator;
                for (;;) {
                    next = m.findNear(lastReturned.key, LT, cmp);
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (tooLow(next.key, cmp))
                            next = null;
                        else {
                            @SuppressWarnings("unchecked") V vv = (V)x;
                            nextValue = vv;
                        }
                        break;
                    }
                }
            }

            public void remove() {
                Node<K,V> l = lastReturned;
                if (l == null)
                    throw new IllegalStateException();
                m.remove(l.key);
                lastReturned = null;
            }

            public Spliterator<T> trySplit() {
                return null;
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                if (hasNext()) {
                    action.accept(next());
                    return true;
                }
                return false;
            }

            public void forEachRemaining(Consumer<? super T> action) {
                while (hasNext())
                    action.accept(next());
            }

            public long estimateSize() {
                return Long.MAX_VALUE;
            }

        }

        final class SubMapValueIterator extends SubMapIter<V> {
            public V next() {
                V v = nextValue;
                advance();
                return v;
            }
            public int characteristics() {
                return 0;
            }
        }

        final class SubMapKeyIterator extends SubMapIter<K> {
            public K next() {
                Node<K,V> n = next;
                advance();
                return n.key;
            }
            public int characteristics() {
                return Spliterator.DISTINCT | Spliterator.ORDERED |
                    Spliterator.SORTED;
            }
            public final Comparator<? super K> getComparator() {
                return SubMap.this.comparator();
            }
        }

        final class SubMapEntryIterator extends SubMapIter<Map.Entry<K,V>> {
            public Map.Entry<K,V> next() {
                Node<K,V> n = next;
                V v = nextValue;
                advance();
                return new AbstractMap.SimpleImmutableEntry<K,V>(n.key, v);
            }
            public int characteristics() {
                return Spliterator.DISTINCT;
            }
        }
    }

    // default Map method overrides

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) throw new NullPointerException();
        V v;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            if ((v = n.getValidValue()) != null)
                action.accept(n.key, v);
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) throw new NullPointerException();
        V v;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            while ((v = n.getValidValue()) != null) {
                V r = function.apply(n.key, v);
                if (r == null) throw new NullPointerException();
                if (n.casValue(v, r))
                    break;
            }
        }
    }

    /**
     * Base class providing common structure for Spliterators.
     * (Although not all that much common functionality; as usual for
     * view classes, details annoyingly vary in key, value, and entry
     * subclasses in ways that are not worth abstracting out for
     * internal classes.)
     *
     * The basic split strategy is to recursively descend from top
     * level, row by row, descending to next row when either split
     * off, or the end of row is encountered. Control of the number of
     * splits relies on some statistical estimation: The expected
     * remaining number of elements of a skip list when advancing
     * either across or down decreases by about 25%. To make this
     * observation useful, we need to know initial size, which we
     * don't. But we can just use Integer.MAX_VALUE so that we
     * don't prematurely zero out while splitting.
     * 为Spliterators提供公共结构的基类。(虽然并不是所有的通用功能;和通常的视图类一样，在键类、值类和条目子类中，细节的差异令人恼火，因为它们不值得抽象为内部类)。基本的分割策略是从顶层递归地下降，一行接一行地下降到下一行，当发生分割时，或者遇到行尾时。对分割数量的控制依赖于一些统计估计:当跨越或下降时，跳转列表中预期的剩余元素数量将减少约25%。为了使这个观察有用，我们需要知道初始大小，而我们不知道。但是我们可以用整数。MAX_VALUE，这样我们就不会在分割时过早地为零。
     */
    abstract static class CSLMSpliterator<K,V> {
        final Comparator<? super K> comparator;
        final K fence;     // exclusive upper bound for keys, or null if to end
        Index<K,V> row;    // the level to split out
        Node<K,V> current; // current traversal node; initialize at origin
        int est;           // pseudo-size estimate
        CSLMSpliterator(Comparator<? super K> comparator, Index<K,V> row,
                        Node<K,V> origin, K fence, int est) {
            this.comparator = comparator; this.row = row;
            this.current = origin; this.fence = fence; this.est = est;
        }

        public final long estimateSize() { return (long)est; }
    }

    static final class KeySpliterator<K,V> extends CSLMSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(Comparator<? super K> comparator, Index<K,V> row,
                       Node<K,V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public Spliterator<K> trySplit() {
            Node<K,V> e; K ek;
            Comparator<? super K> cmp = comparator;
            K f = fence;
            if ((e = current) != null && (ek = e.key) != null) {
                for (Index<K,V> q = row; q != null; q = row = q.down) {
                    Index<K,V> s; Node<K,V> b, n; K sk;
                    if ((s = q.right) != null && (b = s.node) != null &&
                        (n = b.next) != null && n.value != null &&
                        (sk = n.key) != null && cpr(cmp, sk, ek) > 0 &&
                        (f == null || cpr(cmp, sk, f) < 0)) {
                        current = n;
                        Index<K,V> r = q.down;
                        row = (s.right != null) ? s : s.down;
                        est -= est >>> 2;
                        return new KeySpliterator<K,V>(cmp, r, e, sk, est);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            current = null;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0)
                    break;
                if ((v = e.value) != null && v != e)
                    action.accept(k);
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if ((v = e.value) != null && v != e) {
                    current = e.next;
                    action.accept(k);
                    return true;
                }
            }
            current = e;
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.SORTED |
                Spliterator.ORDERED | Spliterator.CONCURRENT |
                Spliterator.NONNULL;
        }

        public final Comparator<? super K> getComparator() {
            return comparator;
        }
    }
    // factory method for KeySpliterator
    final KeySpliterator<K,V> keySpliterator() {
        Comparator<? super K> cmp = comparator;
        for (;;) { // ensure h corresponds to origin p
            HeadIndex<K,V> h; Node<K,V> p;
            Node<K,V> b = (h = head).node;
            if ((p = b.next) == null || p.value != null)
                return new KeySpliterator<K,V>(cmp, h, p, null, (p == null) ?
                                               0 : Integer.MAX_VALUE);
            p.helpDelete(b, p.next);
        }
    }

    static final class ValueSpliterator<K,V> extends CSLMSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(Comparator<? super K> comparator, Index<K,V> row,
                       Node<K,V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public Spliterator<V> trySplit() {
            Node<K,V> e; K ek;
            Comparator<? super K> cmp = comparator;
            K f = fence;
            if ((e = current) != null && (ek = e.key) != null) {
                for (Index<K,V> q = row; q != null; q = row = q.down) {
                    Index<K,V> s; Node<K,V> b, n; K sk;
                    if ((s = q.right) != null && (b = s.node) != null &&
                        (n = b.next) != null && n.value != null &&
                        (sk = n.key) != null && cpr(cmp, sk, ek) > 0 &&
                        (f == null || cpr(cmp, sk, f) < 0)) {
                        current = n;
                        Index<K,V> r = q.down;
                        row = (s.right != null) ? s : s.down;
                        est -= est >>> 2;
                        return new ValueSpliterator<K,V>(cmp, r, e, sk, est);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            current = null;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0)
                    break;
                if ((v = e.value) != null && v != e) {
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept(vv);
                }
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if ((v = e.value) != null && v != e) {
                    current = e.next;
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept(vv);
                    return true;
                }
            }
            current = e;
            return false;
        }

        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.ORDERED |
                Spliterator.NONNULL;
        }
    }

    // Almost the same as keySpliterator()
    final ValueSpliterator<K,V> valueSpliterator() {
        Comparator<? super K> cmp = comparator;
        for (;;) {
            HeadIndex<K,V> h; Node<K,V> p;
            Node<K,V> b = (h = head).node;
            if ((p = b.next) == null || p.value != null)
                return new ValueSpliterator<K,V>(cmp, h, p, null, (p == null) ?
                                                 0 : Integer.MAX_VALUE);
            p.helpDelete(b, p.next);
        }
    }

    static final class EntrySpliterator<K,V> extends CSLMSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(Comparator<? super K> comparator, Index<K,V> row,
                         Node<K,V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public Spliterator<Map.Entry<K,V>> trySplit() {
            Node<K,V> e; K ek;
            Comparator<? super K> cmp = comparator;
            K f = fence;
            if ((e = current) != null && (ek = e.key) != null) {
                for (Index<K,V> q = row; q != null; q = row = q.down) {
                    Index<K,V> s; Node<K,V> b, n; K sk;
                    if ((s = q.right) != null && (b = s.node) != null &&
                        (n = b.next) != null && n.value != null &&
                        (sk = n.key) != null && cpr(cmp, sk, ek) > 0 &&
                        (f == null || cpr(cmp, sk, f) < 0)) {
                        current = n;
                        Index<K,V> r = q.down;
                        row = (s.right != null) ? s : s.down;
                        est -= est >>> 2;
                        return new EntrySpliterator<K,V>(cmp, r, e, sk, est);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            current = null;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0)
                    break;
                if ((v = e.value) != null && v != e) {
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept
                        (new AbstractMap.SimpleImmutableEntry<K,V>(k, vv));
                }
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if ((v = e.value) != null && v != e) {
                    current = e.next;
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept
                        (new AbstractMap.SimpleImmutableEntry<K,V>(k, vv));
                    return true;
                }
            }
            current = e;
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.SORTED |
                Spliterator.ORDERED | Spliterator.CONCURRENT |
                Spliterator.NONNULL;
        }

        public final Comparator<Map.Entry<K,V>> getComparator() {
            // Adapt or create a key-based comparator
            if (comparator != null) {
                return Map.Entry.comparingByKey(comparator);
            }
            else {
                return (Comparator<Map.Entry<K,V>> & Serializable) (e1, e2) -> {
                    @SuppressWarnings("unchecked")
                    Comparable<? super K> k1 = (Comparable<? super K>) e1.getKey();
                    return k1.compareTo(e2.getKey());
                };
            }
        }
    }

    // Almost the same as keySpliterator()
    final EntrySpliterator<K,V> entrySpliterator() {
        Comparator<? super K> cmp = comparator;
        for (;;) { // almost same as key version
            HeadIndex<K,V> h; Node<K,V> p;
            Node<K,V> b = (h = head).node;
            if ((p = b.next) == null || p.value != null)
                return new EntrySpliterator<K,V>(cmp, h, p, null, (p == null) ?
                                                 0 : Integer.MAX_VALUE);
            p.helpDelete(b, p.next);
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long SECONDARY;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentSkipListMap.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            Class<?> tk = Thread.class;
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));

        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
