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
import java.util.Collection;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * A {@link java.util.Set} that uses an internal {@link CopyOnWriteArrayList}
 * for all of its operations.  Thus, it shares the same basic properties:
 * <ul>
 *  <li>It is best suited for applications in which set sizes generally
 *       stay small, read-only operations
 *       vastly outnumber mutative operations, and you need
 *       to prevent interference among threads during traversal.
 *  <li>It is thread-safe.
 *  <li>Mutative operations ({@code add}, {@code set}, {@code remove}, etc.)
 *      are expensive since they usually entail copying the entire underlying
 *      array.
 *  <li>Iterators do not support the mutative {@code remove} operation.
 *  <li>Traversal via iterators is fast and cannot encounter
 *      interference from other threads. Iterators rely on
 *      unchanging snapshots of the array at the time the iterators were
 *      constructed.
 * </ul>
 *
 * <p><b>Sample Usage.</b> The following code sketch uses a
 * copy-on-write set to maintain a set of Handler objects that
 * perform some action upon state updates.
 *
 *  <pre> {@code
 * class Handler { void handle(); ... }
 *
 * class X {
 *   private final CopyOnWriteArraySet<Handler> handlers
 *     = new CopyOnWriteArraySet<Handler>();
 *   public void addHandler(Handler h) { handlers.add(h); }
 *
 *   private long internalState;
 *   private synchronized void changeState() { internalState = ...; }
 *
 *   public void update() {
 *     changeState();
 *     for (Handler handler : handlers)
 *       handler.handle();
 *   }
 * }}</pre>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @see CopyOnWriteArrayList
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 *           对所有操作使用内部CopyOnWriteArrayList的集合。因此，它具有相同的基本性质:
它最适合应用程序，在这些应用程序中，设置大小通常都很小，只读操作大大超过了可变操作，并且您需要在遍历过程中防止线程之间的干扰。
它是线程安全的。
可变操作(添加、设置、删除等)非常昂贵，因为它们通常需要复制整个底层数组。
迭代器不支持更改删除操作。
通过迭代器进行遍历非常快，并且不会受到其他线程的干扰。迭代器依赖于构建迭代器时数组的不变快照。
样品使用。下面的代码草图使用了一个副本-写集来维护一组处理状态更新的处理程序对象。
这个类是Java集合框架的成员。
 */
//hashSet的线程安全实现，可变操作昂贵，需要复制整个底层数组，通过迭代器进行遍历非常快，并且不会受到其他线程的干扰。迭代器依赖于构建迭代器时数组的不变快照。
public class CopyOnWriteArraySet<E> extends AbstractSet<E>
        implements java.io.Serializable {
    private static final long serialVersionUID = 5457747651344034263L;

    private final CopyOnWriteArrayList<E> al;

    /**
     * Creates an empty set.
     */
    public CopyOnWriteArraySet() {
//        底层是CopyOnWriteArrayList实现，读多写少场景性能很高，写的场景也多的话建议使用concurrentHashMap实现
        al = new CopyOnWriteArrayList<E>();
    }

    /**
     * Creates a set containing all of the elements of the specified
     * collection.创建包含指定集合的所有元素的集合。
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection is null
     */
    public CopyOnWriteArraySet(Collection<? extends E> c) {
        if (c.getClass() == CopyOnWriteArraySet.class) {
            @SuppressWarnings("unchecked") CopyOnWriteArraySet<E> cc =
                (CopyOnWriteArraySet<E>)c;
            al = new CopyOnWriteArrayList<E>(cc.al);
        }
        else {
            al = new CopyOnWriteArrayList<E>();
            al.addAllAbsent(c);
        }
    }

    /**
     * Returns the number of elements in this set.返回此集合中的元素数量。
     *
     * @return the number of elements in this set
     */
    public int size() {
        return al.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.如果该集合不包含任何元素，则返回true。
     *
     * @return {@code true} if this set contains no elements
     */
    public boolean isEmpty() {
        return al.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     * More formally, returns {@code true} if and only if this set
     * contains an element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     * 如果该集合包含指定元素，则返回true。更正式地说，如果且仅当该集合包含一个元素e (o==null)时，返回true。e = = null:o.equals(e))。
     */
    public boolean contains(Object o) {
        return al.contains(o);
    }

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
     *
    返回包含此集合中所有元素的数组。如果该集合对其迭代器返回元素的顺序作出任何保证，则该方法必须以相同的顺序返回元素。
    返回的数组将是“安全的”，因为没有对它的引用被这个集合维护(换句话说，这个方法必须分配一个新的数组，即使这个集合是由数组支持的)。因此，调用者可以自由地修改返回的数组。
    该方法充当基于数组和基于集合的api之间的桥梁。
     */
    public Object[] toArray() {
        return al.toArray();
    }

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
     * {@code null}.  (This is useful in determining the length of this
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
     * <p>Suppose {@code x} is a set known to contain only strings.
     * The following code can be used to dump the set into a newly allocated
     * array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of this set are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this
     *         set
     * @throws NullPointerException if the specified array is null
     * 返回包含此集合中的所有元素的数组;返回数组的运行时类型是指定数组的运行时类型。如果集合在指定的数组中，则返回它。否则，将使用指定数组的运行时类型和该集合的大小分配新的数组。
    如果这个集合在指定的数组中，并且有空余的空间(例如。，数组的元素比这个集合要多)，集合结束后数组中的元素被设置为null。(只有在调用者知道该集合不包含任何空元素时，才可以确定该集合的长度。)
    如果该集合对其迭代器返回的元素的顺序作出任何保证，则该方法必须以相同的顺序返回元素。
    与toArray()方法一样，该方法充当基于数组和基于集合的api之间的桥梁。此外，该方法允许对输出数组的运行时类型进行精确控制，并且在某些情况下，可以用于节省分配成本。
    假设x是一个只包含字符串的集合。可以使用以下代码将集合转储到新分配的字符串数组中:
    String[]y = x。toArray(新的字符串[0]);
    注意toArray(新对象[0])在函数中与toArray()相同。
     */
    public <T> T[] toArray(T[] a) {
        return al.toArray(a);
    }

    /**
     * Removes all of the elements from this set.
     * The set will be empty after this call returns.从这个集合中移除所有元素。这个调用返回后，集合将为空。
     */
    public void clear() {
        al.clear();
    }

    /**
     * Removes the specified element from this set if it is present.
     * More formally, removes an element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>,
     * if this set contains such an element.  Returns {@code true} if
     * this set contained the element (or equivalently, if this set
     * changed as a result of the call).  (This set will not contain the
     * element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     * 如果指定的元素存在，则从该集合中删除它。更正式地说，删除这样的元素e (o==null ?)e==null: o = (e))，如果这个集合包含这样的元素。如果该集合包含元素(或等效地，如果此设置因调用而更改)，则返回true。(一旦调用返回，该集合将不包含元素。)
     */
    public boolean remove(Object o) {
        return al.remove(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element {@code e} to this set if
     * the set contains no element {@code e2} such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns {@code false}.
     *
     * @param e element to be added to this set
     * @return {@code true} if this set did not already contain the specified
     *         element
     *         将指定的元素添加到该集合中，如果该集合不存在。更正式地说，如果集合中不包含元素e2 (e==null ?e2 = = null:e.equals(e2))。如果该集合已经包含元素，那么调用将保持该集合不变并返回false。
     */
    public boolean add(E e) {
        return al.addIfAbsent(e);
    }

    /**
     * Returns {@code true} if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns {@code true} if it is a <i>subset</i> of this set.
     *
     * @param  c collection to be checked for containment in this set
     * @return {@code true} if this set contains all of the elements of the
     *         specified collection
     * @throws NullPointerException if the specified collection is null
     * @see #contains(Object)
     * 如果该集合包含指定集合的所有元素，则返回true。如果指定的集合也是一个集合，如果它是这个集合的子集，那么这个方法将返回true。
     */
    public boolean containsAll(Collection<?> c) {
        return al.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present.  If the specified collection is also a
     * set, the {@code addAll} operation effectively modifies this set so
     * that its value is the <i>union</i> of the two sets.  The behavior of
     * this operation is undefined if the specified collection is modified
     * while the operation is in progress.
     *
     * @param  c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #add(Object)
     * 将指定集合中的所有元素添加到这个集合中，如果它们还没有出现。如果指定的集合也是一个集合，则addAll操作将有效地修改该集合，使其值为两个集合的联合。如果在运行过程中修改指定的集合，则该操作的行为是未定义的。
     */
    public boolean addAll(Collection<? extends E> c) {
        return al.addAllAbsent(c) > 0;
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection.  If the specified collection is also a set,
     * this operation effectively modifies this set so that its value is the
     * <i>asymmetric set difference</i> of the two sets.
     *
     * @param  c collection containing elements to be removed from this set
     * @return {@code true} if this set changed as a result of the call
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection (optional)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements (optional),
     *         or if the specified collection is null
     * @see #remove(Object)
     * 从这个集合中删除包含在指定集合中的所有元素。如果指定的集合也是一个集合，此操作将有效地修改该集合，使其值为两个集合的不对称集差。
     */
    public boolean removeAll(Collection<?> c) {
        return al.removeAll(c);
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection.  In other words, removes from this set all of
     * its elements that are not contained in the specified collection.  If
     * the specified collection is also a set, this operation effectively
     * modifies this set so that its value is the <i>intersection</i> of the
     * two sets.
     *
     * @param  c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection (optional)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements (optional),
     *         or if the specified collection is null
     * @see #remove(Object)
     * 只保留该集合中包含在指定集合中的元素。换句话说，从该集合中删除指定集合中不包含的所有元素。如果指定的集合也是一个集合，这个操作将有效地修改这个集合，使它的值是两个集合的交集。
     */
    public boolean retainAll(Collection<?> c) {
        return al.retainAll(c);
    }

    /**
     * Returns an iterator over the elements contained in this set
     * in the order in which these elements were added.
     *
     * <p>The returned iterator provides a snapshot of the state of the set
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove} method.
     *
     * @return an iterator over the elements in this set
     * 在添加这些元素的顺序中，返回该集合中包含的元素的迭代器。
    在构造迭代器时，返回的迭代器提供了设置状态的快照。遍历迭代器时不需要同步。迭代器不支持删除方法。
     */
    public Iterator<E> iterator() {
        return al.iterator();
    }

    /**
     * Compares the specified object with this set for equality.
     * Returns {@code true} if the specified object is the same object
     * as this object, or if it is also a {@link Set} and the elements
     * returned by an {@linkplain Set#iterator() iterator} over the
     * specified set are the same as the elements returned by an
     * iterator over this set.  More formally, the two iterators are
     * considered to return the same elements if they return the same
     * number of elements and for every element {@code e1} returned by
     * the iterator over the specified set, there is an element
     * {@code e2} returned by the iterator over this set such that
     * {@code (e1==null ? e2==null : e1.equals(e2))}.
     *
     * @param o object to be compared for equality with this set
     * @return {@code true} if the specified object is equal to this set
     * 将指定的对象与此集合进行比较，以获得相等性。返回true,如果指定的对象是相同的对象,这个对象,或者也是一个集和迭代器返回的元素在指定的设置是一样的迭代器返回的元素在这集。
     * 更正式,这两个迭代器返回相同的元素被认为如果他们返回相同数量的元素和迭代器返回的每一个元素e1在指定的组,迭代器在这个集合上返回一个元素e2，这样(e1==null ?e2 = = null:. equals(e2))。
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> set = (Set<?>)(o);
        Iterator<?> it = set.iterator();

        // Uses O(n^2) algorithm that is only appropriate
        // for small sets, which CopyOnWriteArraySets should be.

        //  Use a single snapshot of underlying array
        Object[] elements = al.getArray();
        int len = elements.length;
        // Mark matched elements to avoid re-checking
        boolean[] matched = new boolean[len];
        int k = 0;
        outer: while (it.hasNext()) {
            if (++k > len)
                return false;
            Object x = it.next();
            for (int i = 0; i < len; ++i) {
                if (!matched[i] && eq(x, elements[i])) {
                    matched[i] = true;
                    continue outer;
                }
            }
            return false;
        }
        return k == len;
    }

    public boolean removeIf(Predicate<? super E> filter) {
        return al.removeIf(filter);
    }

    public void forEach(Consumer<? super E> action) {
        al.forEach(action);
    }

    /**
     * Returns a {@link Spliterator} over the elements in this set in the order
     * in which these elements were added.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#IMMUTABLE},
     * {@link Spliterator#DISTINCT}, {@link Spliterator#SIZED}, and
     * {@link Spliterator#SUBSIZED}.
     *
     * <p>The spliterator provides a snapshot of the state of the set
     * when the spliterator was constructed. No synchronization is needed while
     * operating on the spliterator.
     *
     * @return a {@code Spliterator} over the elements in this set
     * @since 1.8
     *
    返回一个Spliterator，以添加这些元素的顺序覆盖这个集合中的元素。
    Spliterator Spliterator报告。不可变的,Spliterator。明显,Spliterator。大小,Spliterator.SUBSIZED。
    在构造spliterator时，spliterator提供了设置状态的快照。在spliterator上操作时不需要同步。
     */
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator
            (al.getArray(), Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    /**
     * Tests for equality, coping with nulls.平等的测试，应对无效。
     */
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }
}
