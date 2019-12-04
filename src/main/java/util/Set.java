/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * A collection that contains no duplicate elements.  More formally, sets
 * contain no pair of elements <code>e1</code> and <code>e2</code> such that
 * <code>e1.equals(e2)</code>, and at most one null element.  As implied by
 * its name, this interface models the mathematical <i>set</i> abstraction.
 *
 * <p>The <tt>Set</tt> interface places additional stipulations, beyond those
 * inherited from the <tt>Collection</tt> interface, on the contracts of all
 * constructors and on the contracts of the <tt>add</tt>, <tt>equals</tt> and
 * <tt>hashCode</tt> methods.  Declarations for other inherited methods are
 * also included here for convenience.  (The specifications accompanying these
 * declarations have been tailored to the <tt>Set</tt> interface, but they do
 * not contain any additional stipulations.)
 *
 * <p>The additional stipulation on constructors is, not surprisingly,
 * that all constructors must create a set that contains no duplicate elements
 * (as defined above).
 *
 * <p>Note: Great care must be exercised if mutable objects are used as set
 * elements.  The behavior of a set is not specified if the value of an object
 * is changed in a manner that affects <tt>equals</tt> comparisons while the
 * object is an element in the set.  A special case of this prohibition is
 * that it is not permissible for a set to contain itself as an element.
 *
 * <p>Some set implementations have restrictions on the elements that
 * they may contain.  For example, some implementations prohibit null elements,
 * and some have restrictions on the types of their elements.  Attempting to
 * add an ineligible element throws an unchecked exception, typically
 * <tt>NullPointerException</tt> or <tt>ClassCastException</tt>.  Attempting
 * to query the presence of an ineligible element may throw an exception,
 * or it may simply return false; some implementations will exhibit the former
 * behavior and some will exhibit the latter.  More generally, attempting an
 * operation on an ineligible element whose completion would not result in
 * the insertion of an ineligible element into the set may throw an
 * exception or it may succeed, at the option of the implementation.
 * Such exceptions are marked as "optional" in the specification for this
 * interface.
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements maintained by this set
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see List
 * @see SortedSet
 * @see HashSet
 * @see TreeSet
 * @see AbstractSet
 * @see Collections#singleton(java.lang.Object)
 * @see Collections#EMPTY_SET
 * @since 1.2
 * 不包含重复元素的集合。更正式地说，集合中不包含对e1和e2的元素e1和e2，比如e1 = (e2)和最多一个空元素。顾名思义，这个接口模拟数学集合抽象。
Set接口在所有构造函数的契约和add、equals和hashCode方法的契约上，除了从集合接口继承的约定外，还对其他约定进行了规定。为了方便起见，这里还包含了其他继承方法的声明。(随这些声明而来的规范已经针对Set接口进行了调整，但是它们不包含任何附加的规定。)
对于构造函数的附加规定是，毫不奇怪，所有构造函数都必须创建一个不包含重复元素的集合(如上所述)。
注意:如果将可变对象用作集合元素，则必须非常小心。一组的行为没有指定如果一个对象的值改变的方式影响=比较对象是一组元素。这个禁止的一种特殊情况是不允许包含一组本身为一个元素。
有些集合实现对它们可能包含的元素有限制。例如，有些实现禁止空元素，有些实现限制元素的类型。试图添加一个不合格的元素会抛出一个未检查的异常，通常是NullPointerException或ClassCastException。试图查询不合格元素的存在可能会抛出异常，或者它可能只是返回false;一些实现将展示前者的行为，而一些实现将展示后者。更一般地说，尝试对不符合要求的元素执行操作，如果该元素的完成不会导致将不符合要求的元素插入到集合中，可能会抛出异常，或者在实现的选项下成功。在这个接口的规范中，这些异常被标记为“可选”。
这个接口是Java集合框架的一个成员。
 */
// 值可以为空，集合元素是无序的，元素不能重复
public interface Set<E> extends Collection<E> {
    // Query Operations

    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.返回该集合中的元素数量(其基数)。如果这个集合包含多个整数。MAX_VALUE元素,返回Integer.MAX_VALUE。这个接口是Java集合框架的一个成员。
     *
     * @return the number of elements in this set (its cardinality)
     */
    int size();

    /**
     * Returns <tt>true</tt> if this set contains no elements.如果该集合不包含任何元素，则返回true。
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this set
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *         set does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>)如果该集合包含指定的元素，则返回true。更正式地说，如果且仅当这个集合包含这样的元素e (o= null ?e = = null:o.equals(e))。
     */
    boolean contains(Object o);

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).返回一个迭代器，遍历这个集合中的元素。元素不会以特定的顺序返回(除非这个集合是某个提供保证的类的实例)。
     *
     * @return an iterator over the elements in this set
     */
    Iterator<E> iterator();

    /**
     * Returns an array containing all of the elements in this set.
     * If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the
     * elements in the same order.
     *
     * <p>The returned array will be "safe" in that no references to it
     * are maintained by this set.  (In other words, this method must
     * allocate a new array even if this set is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all the elements in this set
     * 返回一个包含该集合中所有元素的数组。如果该集合对其迭代器返回的元素的顺序有任何保证，则该方法必须以相同的顺序返回元素。
    返回的数组将是“安全的”，因为这个集合不维护对它的引用(换句话说，这个方法必须分配一个新的数组，即使这个集合是由一个数组支持的)。因此，调用者可以自由地修改返回的数组。
    该方法充当基于数组和基于集合的api之间的桥梁。
     */
    Object[] toArray();

    /**
     * Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     * If the set fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this set.
     *
     * <p>If this set fits in the specified array with room to spare
     * (i.e., the array has more elements than this set), the element in
     * the array immediately following the end of the set is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * set <i>only</i> if the caller knows that this set does not contain
     * any null elements.)
     *
     * <p>If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements
     * in the same order.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a set known to contain only strings.
     * The following code can be used to dump the set into a newly allocated
     * array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this set are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this
     *         set
     * @throws NullPointerException if the specified array is null
     * 返回包含此集合中的所有元素的数组;返回数组的运行时类型是指定数组的运行时类型。如果集合在指定的数组中，则返回它。否则，将使用指定数组的运行时类型和该集合的大小分配一个新的数组。
    如果这个集合在指定的数组中，并且有空余的空间(例如。，数组的元素比这个集合要多)，集合结束后数组中的元素被设置为null。(只有在调用者知道该集合不包含任何空元素时，才可以确定该集合的长度。)
    如果这个集合保证它的迭代器返回元素的顺序，那么这个方法必须以相同的顺序返回元素。
    与toArray()方法一样，该方法充当基于数组和基于集合的api之间的桥梁。此外，该方法允许对输出数组的运行时类型进行精确控制，并且在某些情况下，可以用于节省分配成本。
    假设x是一个只包含字符串的集合。下面的代码可以用来将集合转储到新分配的字符串数组中:
    String[]y = x。toArray(新的字符串[0]);
    注意，toArray(新对象[0])在函数toArray()中是相同的。
     */
    <T> T[] toArray(T[] a);


    // Modification Operations

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation).  More formally, adds the specified element
     * <tt>e</tt> to this set if the set contains no element <tt>e2</tt>
     * such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.  In combination with the
     * restriction on constructors, this ensures that sets never contain
     * duplicate elements.
     *
     * <p>The stipulation above does not imply that sets must accept all
     * elements; sets may refuse to add any particular element, including
     * <tt>null</tt>, and throw an exception, as described in the
     * specification for {@link Collection#add Collection.add}.
     * Individual set implementations should clearly document any
     * restrictions on the elements that they may contain.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this set
     * @throws NullPointerException if the specified element is null and this
     *         set does not permit null elements
     * @throws IllegalArgumentException if some property of the specified element
     *         prevents it from being added to this set
     *         如果尚未出现(可选操作)，则将指定元素添加到此集合中。更正式地说，如果集合中不包含元素e2 (e==null ?e2 = = null:e.equals(e2))。如果该集合已经包含元素，那么调用将保持该集合不变并返回false。结合对构造函数的限制，这将确保集合不会包含重复的元素。
    上述规定并不意味着集合必须接受所有元素;集合可以拒绝添加任何特定的元素，包括null，并抛出异常，如Collection.add规范所述。各个集合实现应该清楚地记录它们可能包含的元素的任何限制。
     */
    boolean add(E e);


    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  More formally, removes an element <tt>e</tt>
     * such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this set contains such an element.  Returns <tt>true</tt> if this set
     * contained the element (or equivalently, if this set changed as a
     * result of the call).  (This set will not contain the element once the
     * call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this set
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *         set does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *         is not supported by this set
     *         如果出现(可选操作)，从这个集合中移除指定的元素。更正式地说，删除这样的元素e (o==null ?)e==null: o = (e))，如果这个集合包含这样的元素。如果该集合包含元素，则返回true(或者如果该集合由于调用而更改)。(一旦调用返回，该集合将不包含元素。)
     */
    boolean remove(Object o);


    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     *
     * @param  c collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements of the
     *         specified collection
     * @throws ClassCastException if the types of one or more elements
     *         in the specified collection are incompatible with this
     *         set
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this set does not permit null
     *         elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see    #contains(Object)
     * 如果该集合包含指定集合的所有元素，则返回true。如果指定的集合也是一个集合，如果它是这个集合的子集，那么这个方法将返回true。
     */
    boolean containsAll(Collection<?> c);

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the <tt>addAll</tt> operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param  c collection containing elements to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     *
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of the
     *         specified collection prevents it from being added to this set
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this set does not permit null
     *         elements, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     *         specified collection prevents it from being added to this set
     * @see #add(Object)
     * 如果尚未出现(可选操作)，将指定集合中的所有元素添加到此集合中。如果指定的集合也是一个集合，则addAll操作将有效地修改该集合，使其值为两个集合的联合。如果在操作进行时修改了指定的集合，则不定义此操作的行为。
     */
    boolean addAll(Collection<? extends E> c);

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this set all of its elements that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     *
     * @param  c collection containing elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements
     *         (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see #remove(Object)
     * 只保留该集合中包含在指定集合中的元素(可选操作)。换句话说，从该集合中删除指定集合中不包含的所有元素。如果指定的集合也是一个集合，这个操作将有效地修改这个集合，使它的值是两个集合的交集。
     */
    boolean retainAll(Collection<?> c);

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * @param  c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements
     *         (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     * 从这个集合中删除包含在指定集合中的所有元素(可选操作)。如果指定的集合也是一个集合，此操作将有效地修改该集合，使其值为两个集合的不对称集差。
     */
    boolean removeAll(Collection<?> c);

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.从这个集合中删除所有元素(可选操作)。调用返回后，集合将为空。
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method
     *         is not supported by this set
     */
    void clear();


    // Comparison and hashing

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the specified object is also a set, the two sets
     * have the same size, and every member of the specified set is
     * contained in this set (or equivalently, every member of this set is
     * contained in the specified set).  This definition ensures that the
     * equals method works properly across different implementations of the
     * set interface.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     * 将指定的对象与此集合进行比较，以获得相等性。如果指定的对象也是一个集合，那么两个集合具有相同的大小，并且指定集合的每个成员都包含在这个集合中(或者等价地说，这个集合的每个成员都包含在指定的集合中)。这个定义确保了equals方法在set接口的不同实现中正常工作。
     */
//    比较的集合的元素相同
    boolean equals(Object o);

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set,
     * where the hash code of a <tt>null</tt> element is defined to be zero.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    int hashCode();

    /**
     * Creates a {@code Spliterator} over the elements in this set.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#DISTINCT}.
     * Implementations should document the reporting of additional
     * characteristic values.
     *
     * @implSpec
     * The default implementation creates a
     * <em><a href="Spliterator.html#binding">late-binding</a></em> spliterator
     * from the set's {@code Iterator}.  The spliterator inherits the
     * <em>fail-fast</em> properties of the set's iterator.
     * <p>
     * The created {@code Spliterator} additionally reports
     * {@link Spliterator#SIZED}.
     *
     * @implNote
     * The created {@code Spliterator} additionally reports
     * {@link Spliterator#SUBSIZED}.
     *
     * @return a {@code Spliterator} over the elements in this set
     * @since 1.8
     * 在这个集合的元素上创建一个Spliterator。
    Spliterator.DISTINCT Spliterator报告。实现应该记录附加特征值的报告。
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT);
    }
}
