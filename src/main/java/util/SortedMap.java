/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * A {@link Map} that further provides a <em>total ordering</em> on its keys.
 * The map is ordered according to the {@linkplain Comparable natural
 * ordering} of its keys, or by a {@link Comparator} typically
 * provided at sorted map creation time.  This order is reflected when
 * iterating over the sorted map's collection views (returned by the
 * {@code entrySet}, {@code keySet} and {@code values} methods).
 * Several additional operations are provided to take advantage of the
 * ordering.  (This interface is the map analogue of {@link SortedSet}.)
 *
 * <p>All keys inserted into a sorted map must implement the {@code Comparable}
 * interface (or be accepted by the specified comparator).  Furthermore, all
 * such keys must be <em>mutually comparable</em>: {@code k1.compareTo(k2)} (or
 * {@code comparator.compare(k1, k2)}) must not throw a
 * {@code ClassCastException} for any keys {@code k1} and {@code k2} in
 * the sorted map.  Attempts to violate this restriction will cause the
 * offending method or constructor invocation to throw a
 * {@code ClassCastException}.
 *
 * <p>Note that the ordering maintained by a sorted map (whether or not an
 * explicit comparator is provided) must be <em>consistent with equals</em> if
 * the sorted map is to correctly implement the {@code Map} interface.  (See
 * the {@code Comparable} interface or {@code Comparator} interface for a
 * precise definition of <em>consistent with equals</em>.)  This is so because
 * the {@code Map} interface is defined in terms of the {@code equals}
 * operation, but a sorted map performs all key comparisons using its
 * {@code compareTo} (or {@code compare}) method, so two keys that are
 * deemed equal by this method are, from the standpoint of the sorted map,
 * equal.  The behavior of a tree map <em>is</em> well-defined even if its
 * ordering is inconsistent with equals; it just fails to obey the general
 * contract of the {@code Map} interface.
 *
 * <p>All general-purpose sorted map implementation classes should provide four
 * "standard" constructors. It is not possible to enforce this recommendation
 * though as required constructors cannot be specified by interfaces. The
 * expected "standard" constructors for all sorted map implementations are:
 * <ol>
 *   <li>A void (no arguments) constructor, which creates an empty sorted map
 *   sorted according to the natural ordering of its keys.</li>
 *   <li>A constructor with a single argument of type {@code Comparator}, which
 *   creates an empty sorted map sorted according to the specified comparator.</li>
 *   <li>A constructor with a single argument of type {@code Map}, which creates
 *   a new map with the same key-value mappings as its argument, sorted
 *   according to the keys' natural ordering.</li>
 *   <li>A constructor with a single argument of type {@code SortedMap}, which
 *   creates a new sorted map with the same key-value mappings and the same
 *   ordering as the input sorted map.</li>
 * </ol>
 *
 * <p><strong>Note</strong>: several methods return submaps with restricted key
 * ranges. Such ranges are <em>half-open</em>, that is, they include their low
 * endpoint but not their high endpoint (where applicable).  If you need a
 * <em>closed range</em> (which includes both endpoints), and the key type
 * allows for calculation of the successor of a given key, merely request
 * the subrange from {@code lowEndpoint} to
 * {@code successor(highEndpoint)}.  For example, suppose that {@code m}
 * is a map whose keys are strings.  The following idiom obtains a view
 * containing all of the key-value mappings in {@code m} whose keys are
 * between {@code low} and {@code high}, inclusive:<pre>
 *   SortedMap&lt;String, V&gt; sub = m.subMap(low, high+"\0");</pre>
 *
 * A similar technique can be used to generate an <em>open range</em>
 * (which contains neither endpoint).  The following idiom obtains a
 * view containing all of the key-value mappings in {@code m} whose keys
 * are between {@code low} and {@code high}, exclusive:<pre>
 *   SortedMap&lt;String, V&gt; sub = m.subMap(low+"\0", high);</pre>
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author  Josh Bloch
 * @see Map
 * @see TreeMap
 * @see SortedSet
 * @see Comparator
 * @see Comparable
 * @see Collection
 * @see ClassCastException
 * @since 1.2
 * 进一步在其键上提供总的排序的映射。映射是根据它的键的自然顺序来排序的，或者由在排序映射创建时通常提供的比较器来排序的。当遍历排序映射的集合视图(由entrySet、keySet和values方法返回)时，将反映此顺序。为了利用订购的优势，还提供了几个附加操作。(此界面为SortedSet的地图模拟。)
插入到排序映射中的所有键必须实现可比较的接口(或者被指定的比较器接受)。此外，所有这些键都必须是相互可比性的:k1. compareto (k2)(或comparator.compare(k1, k2))不能对排序映射中的任何键k1和k2抛出ClassCastException。试图违反此限制将导致违规的方法或构造函数调用抛出ClassCastException。
注意，如果排序映射要正确地实现映射接口，那么由排序映射维护的排序(无论是否提供显式比较器)必须与equals保持一致。(请参阅可比接口或比较器接口，以获得与=一致的精确定义。)这是因为Map接口是根据equals操作定义的，但是排序映射使用它的compareTo(或compare)方法执行所有的关键比较，所以从排序映射的角度来看，这个方法认为相等的两个键是相等的。即使树映射的排序与等号不一致，它的行为也是定义良好的;它只是不遵守Map接口的一般约定。
所有通用的排序映射实现类都应该提供四个“标准”构造函数。虽然由于接口不能指定所需的构造函数，因此不可能强制执行此建议。所有已排序映射实现的预期“标准”构造函数是:
一个空的(无参数)构造函数，它创建一个按其键的自然顺序排序的空排序映射。
具有比较器类型的单个参数的构造函数，它创建一个根据指定比较器排序的空排序映射。
具有单一类型映射参数的构造函数，该构造函数根据键的自然顺序创建具有与其参数相同键值映射的新映射。
具有排序映射类型为SortedMap的单个参数的构造函数，该构造函数创建具有相同键值映射和与输入排序映射具有相同排序顺序的新排序映射。
注意:一些方法返回限制键范围的子映射。这样的范围是半开放的，也就是说，它们包括它们的低端点，但不包括它们的高端点(如果适用的话)。如果您需要一个闭合范围(包括两个端点)，并且密钥类型允许计算给定密钥的后续，那么只需请求从lowEndpoint到继承者(highEndpoint)的子例程。例如，假设m是一个键为字符串的映射。下面的习惯用法获取一个视图，该视图包含m中所有键值映射，其键值介于低和高之间，包括:
SortedMap<String, V> sub = m。子映射(低,高+ \ 0);
可以使用类似的技术生成开放范围(其中不包含端点)。下面的习惯用法获取一个视图，该视图包含m中所有键值映射，其键值介于低和高之间，独占:
SortedMap<String, V> sub = m。子映射(低+“\ 0”,高);
这个接口是Java集合框架的一个成员。
 */
//元素是按自然顺序排序或者按传入的比较器排序
public interface SortedMap<K,V> extends Map<K,V> {
    /**
     * Returns the comparator used to order the keys in this map, or
     * {@code null} if this map uses the {@linkplain Comparable
     * natural ordering} of its keys.
     *
     * @return the comparator used to order the keys in this map,
     *         or {@code null} if this map uses the natural ordering
     *         of its keys
     *         返回用于在此映射中排序键的comparator，如果该映射使用其键的自然顺序，则返回null。
     */
    Comparator<? super K> comparator();

    /**
     * Returns a view of the portion of this map whose keys range from
     * {@code fromKey}, inclusive, to {@code toKey}, exclusive.  (If
     * {@code fromKey} and {@code toKey} are equal, the returned map
     * is empty.)  The returned map is backed by this map, so changes
     * in the returned map are reflected in this map, and vice-versa.
     * The returned map supports all optional map operations that this
     * map supports.
     *
     * <p>The returned map will throw an {@code IllegalArgumentException}
     * on an attempt to insert a key outside its range.
     *
     * @param fromKey low endpoint (inclusive) of the keys in the returned map
     * @param toKey high endpoint (exclusive) of the keys in the returned map
     * @return a view of the portion of this map whose keys range from
     *         {@code fromKey}, inclusive, to {@code toKey}, exclusive
     * @throws ClassCastException if {@code fromKey} and {@code toKey}
     *         cannot be compared to one another using this map's comparator
     *         (or, if the map has no comparator, using natural ordering).
     *         Implementations may, but are not required to, throw this
     *         exception if {@code fromKey} or {@code toKey}
     *         cannot be compared to keys currently in the map.
     * @throws NullPointerException if {@code fromKey} or {@code toKey}
     *         is null and this map does not permit null keys
     * @throws IllegalArgumentException if {@code fromKey} is greater than
     *         {@code toKey}; or if this map itself has a restricted
     *         range, and {@code fromKey} or {@code toKey} lies
     *         outside the bounds of the range
     *         返回该映射的一部分的视图，其键的范围从键(包括键)到键(排除键)。(如果fromKey和toKey相等，则返回的映射为空。)返回的映射由该映射支持，因此返回的映射中的更改反映在该映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
    返回的映射将抛出一个IllegalArgumentException，用于尝试在其范围之外插入一个键。
     */
    SortedMap<K,V> subMap(K fromKey, K toKey);

    /**
     * Returns a view of the portion of this map whose keys are
     * strictly less than {@code toKey}.  The returned map is backed
     * by this map, so changes in the returned map are reflected in
     * this map, and vice-versa.  The returned map supports all
     * optional map operations that this map supports.
     *
     * <p>The returned map will throw an {@code IllegalArgumentException}
     * on an attempt to insert a key outside its range.
     *
     * @param toKey high endpoint (exclusive) of the keys in the returned map
     * @return a view of the portion of this map whose keys are strictly
     *         less than {@code toKey}
     * @throws ClassCastException if {@code toKey} is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if {@code toKey} does not implement {@link Comparable}).
     *         Implementations may, but are not required to, throw this
     *         exception if {@code toKey} cannot be compared to keys
     *         currently in the map.
     * @throws NullPointerException if {@code toKey} is null and
     *         this map does not permit null keys
     * @throws IllegalArgumentException if this map itself has a
     *         restricted range, and {@code toKey} lies outside the
     *         bounds of the range
     *         返回映射中键值严格小于toKey的部分的视图。返回的映射由该映射支持，因此返回的映射中的更改反映在该映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
    返回的映射将抛出一个IllegalArgumentException，用于尝试在其范围之外插入一个键。
     */
    SortedMap<K,V> headMap(K toKey);

    /**
     * Returns a view of the portion of this map whose keys are
     * greater than or equal to {@code fromKey}.  The returned map is
     * backed by this map, so changes in the returned map are
     * reflected in this map, and vice-versa.  The returned map
     * supports all optional map operations that this map supports.
     *
     * <p>The returned map will throw an {@code IllegalArgumentException}
     * on an attempt to insert a key outside its range.
     *
     * @param fromKey low endpoint (inclusive) of the keys in the returned map
     * @return a view of the portion of this map whose keys are greater
     *         than or equal to {@code fromKey}
     * @throws ClassCastException if {@code fromKey} is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if {@code fromKey} does not implement {@link Comparable}).
     *         Implementations may, but are not required to, throw this
     *         exception if {@code fromKey} cannot be compared to keys
     *         currently in the map.
     * @throws NullPointerException if {@code fromKey} is null and
     *         this map does not permit null keys
     * @throws IllegalArgumentException if this map itself has a
     *         restricted range, and {@code fromKey} lies outside the
     *         bounds of the range
     *         返回该映射中键大于或等于fromKey的部分的视图。返回的映射由该映射支持，因此返回的映射中的更改反映在该映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
    返回的映射将抛出一个IllegalArgumentException，用于尝试在其范围之外插入一个键。
     */
    SortedMap<K,V> tailMap(K fromKey);

    /**
     * Returns the first (lowest) key currently in this map.返回当前映射中的第一个(最低)键。
     *
     * @return the first (lowest) key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    K firstKey();

    /**
     * Returns the last (highest) key currently in this map.返回当前映射中的最后一个(最高)键。
     *
     * @return the last (highest) key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    K lastKey();

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set's iterator returns the keys in ascending order.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear}
     * operations.  It does not support the {@code add} or {@code addAll}
     * operations.
     *
     * @return a set view of the keys contained in this map, sorted in
     *         ascending order
     *         返回此映射中包含的键的集合视图。集合的迭代器按升序返回键。集合由映射支持，因此映射的更改反映在集合中，反之亦然。如果在对集合进行迭代时修改了映射(除了迭代器自己的删除操作)，则迭代的结果是未定义的。该集合支持元素删除，通过迭代器从映射中删除相应的映射。删除，删除，删除，删除，保留，清除操作。它不支持add或addAll操作。
     */
    Set<K> keySet();

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection's iterator returns the values in ascending order
     * of the corresponding keys.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own {@code remove} operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Collection.remove}, {@code removeAll},
     * {@code retainAll} and {@code clear} operations.  It does not
     * support the {@code add} or {@code addAll} operations.
     *
     * @return a collection view of the values contained in this map,
     *         sorted in ascending key order
     *         返回包含在此映射中的值的集合视图。集合的迭代器以相应键的升序返回值。集合由映射支持，因此映射的更改反映在集合中，反之亦然。如果在集合上进行迭代时修改了映射(除了迭代器自己的删除操作)，则迭代的结果没有定义。该集合支持元素删除，通过迭代器从映射中删除相应的映射。删除收藏。移除、移除、保留和清除操作。它不支持add或addAll操作。
     */
    Collection<V> values();

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set's iterator returns the entries in ascending key order.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation, or through the
     * {@code setValue} operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll} and
     * {@code clear} operations.  It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * @return a set view of the mappings contained in this map,
     *         sorted in ascending key order
     *         返回该映射中包含的映射的集合视图。集合的迭代器以升序键顺序返回条目。集合由映射支持，因此映射的更改反映在集合中，反之亦然。如果在对集合进行迭代时修改了映射(除了通过迭代器自己的删除操作，或者通过迭代器返回的映射条目上的setValue操作)，则迭代的结果没有定义。该集合支持元素删除，通过迭代器从映射中删除相应的映射。删除，删除，删除，删除，保留，清除操作。它不支持add或addAll操作。
     */
    Set<Map.Entry<K, V>> entrySet();
}
