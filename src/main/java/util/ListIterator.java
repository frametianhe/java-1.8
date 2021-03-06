/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * An iterator for lists that allows the programmer
 * to traverse the list in either direction, modify
 * the list during iteration, and obtain the iterator's
 * current position in the list. A {@code ListIterator}
 * has no current element; its <I>cursor position</I> always
 * lies between the element that would be returned by a call
 * to {@code previous()} and the element that would be
 * returned by a call to {@code next()}.
 * An iterator for a list of length {@code n} has {@code n+1} possible
 * cursor positions, as illustrated by the carets ({@code ^}) below:
 * <PRE>
 *                      Element(0)   Element(1)   Element(2)   ... Element(n-1)
 * cursor positions:  ^            ^            ^            ^                  ^
 * </PRE>
 * Note that the {@link #remove} and {@link #set(Object)} methods are
 * <i>not</i> defined in terms of the cursor position;  they are defined to
 * operate on the last element returned by a call to {@link #next} or
 * {@link #previous()}.
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @see Collection
 * @see List
 * @see Iterator
 * @see Enumeration
 * @see List#listIterator()
 * @since   1.2
 *
用于列表的迭代器，它允许程序员向任意方向遍历列表，在迭代期间修改列表，并获取列表中的迭代器当前位置。ListIterator无当前元素;它的光标位置总是位于调用previous()返回的元素和调用next()返回的元素之间。迭代器的长度n n + 1可能光标位置,在图中以克拉(^)如下:
元素元素(0)(1)(2)……元素(n - 1)
光标位置:^ ^ ^ ^ ^

注意，删除和设置(对象)方法没有根据光标位置定义;它们被定义为对通过调用next或previous()返回的最后一个元素进行操作。
这个接口是Java集合框架的一个成员。
 */
public interface ListIterator<E> extends Iterator<E> {
    // Query Operations

    /**
     * Returns {@code true} if this list iterator has more elements when
     * traversing the list in the forward direction. (In other words,
     * returns {@code true} if {@link #next} would return an element rather
     * than throwing an exception.)如果此列表迭代器在正向遍历列表时具有更多元素，则返回true。(换句话说，如果next返回一个元素而不是抛出一个异常，则返回true。)
     *
     * @return {@code true} if the list iterator has more elements when
     *         traversing the list in the forward direction
     */
    boolean hasNext();

    /**
     * Returns the next element in the list and advances the cursor position.
     * This method may be called repeatedly to iterate through the list,
     * or intermixed with calls to {@link #previous} to go back and forth.
     * (Note that alternating calls to {@code next} and {@code previous}
     * will return the same element repeatedly.)返回列表中的下一个元素并推进光标位置。这个方法可以被反复调用以遍历列表，或者与前一个方法的调用混合，以便来回执行。(注意，对next和previous的交替调用将重复返回相同的元素。)
     *
     * @return the next element in the list
     * @throws NoSuchElementException if the iteration has no next element
     */
    E next();

    /**
     * Returns {@code true} if this list iterator has more elements when
     * traversing the list in the reverse direction.  (In other words,
     * returns {@code true} if {@link #previous} would return an element
     * rather than throwing an exception.)如果这个列表迭代器在反方向遍历列表时具有更多元素，则返回true。(换句话说，如果先前返回的是元素而不是抛出异常，则返回true。)
     *
     * @return {@code true} if the list iterator has more elements when
     *         traversing the list in the reverse direction
     */
    boolean hasPrevious();

    /**
     * Returns the previous element in the list and moves the cursor
     * position backwards.  This method may be called repeatedly to
     * iterate through the list backwards, or intermixed with calls to
     * {@link #next} to go back and forth.  (Note that alternating calls
     * to {@code next} and {@code previous} will return the same
     * element repeatedly.)返回列表中的前一个元素并将光标位置向后移动。可以反复调用此方法以向后遍历列表，或与相邻的调用相互混合。(注意，对next和previous的交替调用将重复返回相同的元素。)
     *
     * @return the previous element in the list
     * @throws NoSuchElementException if the iteration has no previous
     *         element
     */
    E previous();

    /**
     * Returns the index of the element that would be returned by a
     * subsequent call to {@link #next}. (Returns list size if the list
     * iterator is at the end of the list.)返回元素的索引，该索引将由后续对next的调用返回。(如果列表迭代器位于列表末尾，则返回列表大小。)
     *
     * @return the index of the element that would be returned by a
     *         subsequent call to {@code next}, or list size if the list
     *         iterator is at the end of the list
     */
    int nextIndex();

    /**
     * Returns the index of the element that would be returned by a
     * subsequent call to {@link #previous}. (Returns -1 if the list
     * iterator is at the beginning of the list.)返回元素的索引，该索引将由后续对previous的调用返回。(如果列表迭代器位于列表的开头，则返回-1。)
     *
     * @return the index of the element that would be returned by a
     *         subsequent call to {@code previous}, or -1 if the list
     *         iterator is at the beginning of the list
     */
    int previousIndex();


    // Modification Operations

    /**
     * Removes from the list the last element that was returned by {@link
     * #next} or {@link #previous} (optional operation).  This call can
     * only be made once per call to {@code next} or {@code previous}.
     * It can be made only if {@link #add} has not been
     * called after the last call to {@code next} or {@code previous}.从列表中移除下一个或之前返回的元素(可选操作)。这个调用只能在下一次调用或之前调用一次。只有在对next或previous的最后一次调用之后没有调用add时，才可以进行此操作。
     *
     * @throws UnsupportedOperationException if the {@code remove}
     *         operation is not supported by this list iterator
     * @throws IllegalStateException if neither {@code next} nor
     *         {@code previous} have been called, or {@code remove} or
     *         {@code add} have been called after the last call to
     *         {@code next} or {@code previous}
     */
    void remove();

    /**
     * Replaces the last element returned by {@link #next} or
     * {@link #previous} with the specified element (optional operation).
     * This call can be made only if neither {@link #remove} nor {@link
     * #add} have been called after the last call to {@code next} or
     * {@code previous}.用指定的元素(可选操作)替换next或previous返回的最后一个元素。只有在对next或previous的最后一次调用之后没有调用remove或add，才可以进行此调用。
     *
     * @param e the element with which to replace the last element returned by
     *          {@code next} or {@code previous}
     * @throws UnsupportedOperationException if the {@code set} operation
     *         is not supported by this list iterator
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws IllegalArgumentException if some aspect of the specified
     *         element prevents it from being added to this list
     * @throws IllegalStateException if neither {@code next} nor
     *         {@code previous} have been called, or {@code remove} or
     *         {@code add} have been called after the last call to
     *         {@code next} or {@code previous}
     */
    void set(E e);

    /**
     * Inserts the specified element into the list (optional operation).
     * The element is inserted immediately before the element that
     * would be returned by {@link #next}, if any, and after the element
     * that would be returned by {@link #previous}, if any.  (If the
     * list contains no elements, the new element becomes the sole element
     * on the list.)  The new element is inserted before the implicit
     * cursor: a subsequent call to {@code next} would be unaffected, and a
     * subsequent call to {@code previous} would return the new element.
     * (This call increases by one the value that would be returned by a
     * call to {@code nextIndex} or {@code previousIndex}.)
     *
     * @param e the element to insert
     * @throws UnsupportedOperationException if the {@code add} method is
     *         not supported by this list iterator
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws IllegalArgumentException if some aspect of this element
     *         prevents it from being added to this list
     *         将指定的元素插入到列表中(可选操作)。元素会在next(如果有的话)返回的元素之前以及之前(如果有的话)返回的元素之后插入。(如果列表不包含元素，则新元素将成为列表中的唯一元素。)在隐式游标之前插入新元素:对next的后续调用不受影响，随后调用之前的调用将返回新元素。(这个调用增加了一个调用nextIndex或previousIndex的值。)
     */
    void add(E e);
}
