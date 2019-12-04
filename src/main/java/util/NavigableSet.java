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
 * Written by Doug Lea and Josh Bloch with assistance from members of JCP
 * JSR-166 Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util;

/**
 * A {@link SortedSet} extended with navigation methods reporting
 * closest matches for given search targets. Methods {@code lower},
 * {@code floor}, {@code ceiling}, and {@code higher} return elements
 * respectively less than, less than or equal, greater than or equal,
 * and greater than a given element, returning {@code null} if there
 * is no such element.  A {@code NavigableSet} may be accessed and
 * traversed in either ascending or descending order.  The {@code
 * descendingSet} method returns a view of the set with the senses of
 * all relational and directional methods inverted. The performance of
 * ascending operations and views is likely to be faster than that of
 * descending ones.  This interface additionally defines methods
 * {@code pollFirst} and {@code pollLast} that return and remove the
 * lowest and highest element, if one exists, else returning {@code
 * null}.  Methods {@code subSet}, {@code headSet},
 * and {@code tailSet} differ from the like-named {@code
 * SortedSet} methods in accepting additional arguments describing
 * whether lower and upper bounds are inclusive versus exclusive.
 * Subsets of any {@code NavigableSet} must implement the {@code
 * NavigableSet} interface.
 *
 * <p> The return values of navigation methods may be ambiguous in
 * implementations that permit {@code null} elements. However, even
 * in this case the result can be disambiguated by checking
 * {@code contains(null)}. To avoid such issues, implementations of
 * this interface are encouraged to <em>not</em> permit insertion of
 * {@code null} elements. (Note that sorted sets of {@link
 * Comparable} elements intrinsically do not permit {@code null}.)
 *
 * <p>Methods
 * {@link #subSet(Object, Object) subSet(E, E)},
 * {@link #headSet(Object) headSet(E)}, and
 * {@link #tailSet(Object) tailSet(E)}
 * are specified to return {@code SortedSet} to allow existing
 * implementations of {@code SortedSet} to be compatibly retrofitted to
 * implement {@code NavigableSet}, but extensions and implementations
 * of this interface are encouraged to override these methods to return
 * {@code NavigableSet}.
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @author Josh Bloch
 * @param <E> the type of elements maintained by this set
 * @since 1.6
 * 扩展导航方法的排序集，报告与给定搜索目标最接近的匹配。方法分别小于、小于或等于、大于或等于、大于或等于、大于给定元素，如果不存在该元素，则返回null。可以以升序或降序访问和遍历NavigableSet。descent dingset方法返回集合的视图，并将所有关系和方向方法的感觉颠倒。上升操作和视图的性能可能比下降操作和视图的性能快。该接口还定义了方法pollFirst和pollLast，它们返回并删除最低和最高的元素(如果存在的话)，否则返回null。方法子集、耳机和尾集与命名为SortedSet的方法在接受其他参数时是不同的，这些参数描述下界和上界是包含的还是独占的。任何NavigableSet的子集都必须实现NavigableSet接口。
导航方法的返回值在允许空元素的实现中可能是不明确的。然而，即使在这种情况下，结果也可以通过检查contains(null)来消除歧义。为了避免此类问题，鼓励此接口的实现不允许插入空元素。(注意，在本质上，排序的可比较元素集不允许null。)
方法子集(E, E)、耳机(E)和尾集(E)被指定返回SortedSet，以允许对SortedSet的现有实现进行兼容改造，以实现NavigableSet，但鼓励此接口的扩展和实现覆盖这些方法以返回NavigableSet。
这个接口是Java集合框架的一个成员。
 */
//建议不插入null元素，排序的元素不允许为null
public interface NavigableSet<E> extends SortedSet<E> {
    /**
     * Returns the greatest element in this set strictly less than the
     * given element, or {@code null} if there is no such element.返回该集合中最大的元素严格小于给定的元素，如果没有该元素，则返回null。
     *
     * @param e the value to match
     * @return the greatest element less than {@code e},
     *         or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be
     *         compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *         and this set does not permit null elements
     */
    E lower(E e);

    /**
     * Returns the greatest element in this set less than or equal to
     * the given element, or {@code null} if there is no such element.返回该集合中小于或等于给定元素的最大元素，如果没有该元素，则返回null。
     *
     * @param e the value to match
     * @return the greatest element less than or equal to {@code e},
     *         or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be
     *         compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *         and this set does not permit null elements
     */
    E floor(E e);

    /**
     * Returns the least element in this set greater than or equal to
     * the given element, or {@code null} if there is no such element.返回该集合中大于或等于给定元素的最小元素，如果没有该元素，则返回null。
     *
     * @param e the value to match
     * @return the least element greater than or equal to {@code e},
     *         or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be
     *         compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *         and this set does not permit null elements
     */
    E ceiling(E e);

    /**
     * Returns the least element in this set strictly greater than the
     * given element, or {@code null} if there is no such element.返回该集合中比给定元素大的最小元素，如果没有该元素，则返回null。
     *
     * @param e the value to match
     * @return the least element greater than {@code e},
     *         or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be
     *         compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *         and this set does not permit null elements
     */
    E higher(E e);

    /**
     * Retrieves and removes the first (lowest) element,
     * or returns {@code null} if this set is empty.检索并删除第一个(最低)元素，如果该集合为空，则返回null。
     *
     * @return the first element, or {@code null} if this set is empty
     */
    E pollFirst();

    /**
     * Retrieves and removes the last (highest) element,
     * or returns {@code null} if this set is empty.检索并删除最后一个(最高的)元素，如果该集合为空，则返回null。
     *
     * @return the last element, or {@code null} if this set is empty
     */
    E pollLast();

    /**
     * Returns an iterator over the elements in this set, in ascending order.按升序返回该集合中的元素的迭代器。
     *
     * @return an iterator over the elements in this set, in ascending order
     */
    Iterator<E> iterator();

    /**
     * Returns a reverse order view of the elements contained in this set.
     * The descending set is backed by this set, so changes to the set are
     * reflected in the descending set, and vice-versa.  If either set is
     * modified while an iteration over either set is in progress (except
     * through the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.
     *
     * <p>The returned set has an ordering equivalent to
     * <tt>{@link Collections#reverseOrder(Comparator) Collections.reverseOrder}(comparator())</tt>.
     * The expression {@code s.descendingSet().descendingSet()} returns a
     * view of {@code s} essentially equivalent to {@code s}.
     *
     * @return a reverse order view of this set
     * 返回包含在此集合中的元素的反向顺序视图。降序集由该集合支持，因此对该集合的更改反映在降序集中，反之亦然。如果任何一个集合在迭代过程中被修改(除了迭代器自己的删除操作)，则迭代的结果是未定义的。
    返回的集合具有与Collections.reverseOrder(comparator())等价的排序。派生集().下降集()返回的视图本质上等同于s。
     */
    NavigableSet<E> descendingSet();

    /**
     * Returns an iterator over the elements in this set, in descending order.
     * Equivalent in effect to {@code descendingSet().iterator()}.按降序返回该集合中的元素的迭代器。等效于下降集().iterator()。
     *
     * @return an iterator over the elements in this set, in descending order
     */
    Iterator<E> descendingIterator();

    /**
     * Returns a view of the portion of this set whose elements range from
     * {@code fromElement} to {@code toElement}.  If {@code fromElement} and
     * {@code toElement} are equal, the returned set is empty unless {@code
     * fromInclusive} and {@code toInclusive} are both true.  The returned set
     * is backed by this set, so changes in the returned set are reflected in
     * this set, and vice-versa.  The returned set supports all optional set
     * operations that this set supports.
     *
     * <p>The returned set will throw an {@code IllegalArgumentException}
     * on an attempt to insert an element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param fromInclusive {@code true} if the low endpoint
     *        is to be included in the returned view
     * @param toElement high endpoint of the returned set
     * @param toInclusive {@code true} if the high endpoint
     *        is to be included in the returned view
     * @return a view of the portion of this set whose elements range from
     *         {@code fromElement}, inclusive, to {@code toElement}, exclusive
     * @throws ClassCastException if {@code fromElement} and
     *         {@code toElement} cannot be compared to one another using this
     *         set's comparator (or, if the set has no comparator, using
     *         natural ordering).  Implementations may, but are not required
     *         to, throw this exception if {@code fromElement} or
     *         {@code toElement} cannot be compared to elements currently in
     *         the set.
     * @throws NullPointerException if {@code fromElement} or
     *         {@code toElement} is null and this set does
     *         not permit null elements
     * @throws IllegalArgumentException if {@code fromElement} is
     *         greater than {@code toElement}; or if this set itself
     *         has a restricted range, and {@code fromElement} or
     *         {@code toElement} lies outside the bounds of the range.
     *         返回该集合中元素范围从元素到元素的部分的视图。如果fromElement和toElement相等，则返回的集合为空，除非from包容性和to包容性都为真。返回的集合是由这个集合支持的，所以返回集的更改在这个集合中反映出来，反之亦然。返回的集合支持该集合支持的所有可选的集合操作。
    返回的集合将抛出一个IllegalArgumentException，用于尝试插入超出其范围的元素。
     */
    NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                           E toElement,   boolean toInclusive);

    /**
     * Returns a view of the portion of this set whose elements are less than
     * (or equal to, if {@code inclusive} is true) {@code toElement}.  The
     * returned set is backed by this set, so changes in the returned set are
     * reflected in this set, and vice-versa.  The returned set supports all
     * optional set operations that this set supports.
     *
     * <p>The returned set will throw an {@code IllegalArgumentException}
     * on an attempt to insert an element outside its range.
     *
     * @param toElement high endpoint of the returned set
     * @param inclusive {@code true} if the high endpoint
     *        is to be included in the returned view
     * @return a view of the portion of this set whose elements are less than
     *         (or equal to, if {@code inclusive} is true) {@code toElement}
     * @throws ClassCastException if {@code toElement} is not compatible
     *         with this set's comparator (or, if the set has no comparator,
     *         if {@code toElement} does not implement {@link Comparable}).
     *         Implementations may, but are not required to, throw this
     *         exception if {@code toElement} cannot be compared to elements
     *         currently in the set.
     * @throws NullPointerException if {@code toElement} is null and
     *         this set does not permit null elements
     * @throws IllegalArgumentException if this set itself has a
     *         restricted range, and {@code toElement} lies outside the
     *         bounds of the range
     *         返回该集合中元素小于(或等于，如果包含为真)元素的部分的视图。返回的集合是由这个集合支持的，所以返回集的更改在这个集合中反映出来，反之亦然。返回的集合支持该集合支持的所有可选的集合操作。
    返回的集合将抛出一个IllegalArgumentException，用于尝试插入超出其范围的元素。
     */
    NavigableSet<E> headSet(E toElement, boolean inclusive);

    /**
     * Returns a view of the portion of this set whose elements are greater
     * than (or equal to, if {@code inclusive} is true) {@code fromElement}.
     * The returned set is backed by this set, so changes in the returned set
     * are reflected in this set, and vice-versa.  The returned set supports
     * all optional set operations that this set supports.
     *
     * <p>The returned set will throw an {@code IllegalArgumentException}
     * on an attempt to insert an element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param inclusive {@code true} if the low endpoint
     *        is to be included in the returned view
     * @return a view of the portion of this set whose elements are greater
     *         than or equal to {@code fromElement}
     * @throws ClassCastException if {@code fromElement} is not compatible
     *         with this set's comparator (or, if the set has no comparator,
     *         if {@code fromElement} does not implement {@link Comparable}).
     *         Implementations may, but are not required to, throw this
     *         exception if {@code fromElement} cannot be compared to elements
     *         currently in the set.
     * @throws NullPointerException if {@code fromElement} is null
     *         and this set does not permit null elements
     * @throws IllegalArgumentException if this set itself has a
     *         restricted range, and {@code fromElement} lies outside the
     *         bounds of the range
     *         返回该集合中元素大于(或等于，如果包含为true) fromElement的部分的视图。返回的集合是由这个集合支持的，所以返回集的更改在这个集合中反映出来，反之亦然。返回的集合支持该集合支持的所有可选的集合操作。
    返回的集合将抛出一个IllegalArgumentException，用于尝试插入超出其范围的元素。
     */
    NavigableSet<E> tailSet(E fromElement, boolean inclusive);

    /**
     * {@inheritDoc}
     *
     * <p>Equivalent to {@code subSet(fromElement, true, toElement, false)}.
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * 返回该集合的部分的视图，其元素范围从fromElement，包括，到toElement，独占。(如果fromElement和toElement相等，则返回的集合为空。)返回的集合是由这个集合支持的，所以返回集的更改在这个集合中反映出来，反之亦然。返回的集合支持该集合支持的所有可选的集合操作。
    返回的集合将抛出一个IllegalArgumentException，用于尝试插入超出其范围的元素。
    等价于子集(fromElement, true, toElement, false)。
     */
    SortedSet<E> subSet(E fromElement, E toElement);

    /**
     * {@inheritDoc}
     *
     * <p>Equivalent to {@code headSet(toElement, false)}.
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * 返回该集合中元素严格小于toElement的部分的视图。返回的集合是由这个集合支持的，所以返回集的更改在这个集合中反映出来，反之亦然。返回的集合支持该集合支持的所有可选的集合操作。
    返回的集合将抛出一个IllegalArgumentException，用于尝试插入超出其范围的元素。
    相当于耳机(toElement假)。
     */
    SortedSet<E> headSet(E toElement);

    /**
     * {@inheritDoc}
     *
     * <p>Equivalent to {@code tailSet(fromElement, true)}.
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * 返回该集合中元素大于或等于fromElement的部分的视图。返回的集合是由这个集合支持的，所以返回集的更改在这个集合中反映出来，反之亦然。返回的集合支持该集合支持的所有可选的集合操作。
    返回的集合将抛出一个IllegalArgumentException，用于尝试插入超出其范围的元素。
    相当于tailSet(fromElement,真)。
     */
    SortedSet<E> tailSet(E fromElement);
}
