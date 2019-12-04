/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio;

import java.util.Spliterator;

/**
 * A container for data of a specific primitive type.
 *
 * <p> A buffer is a linear, finite sequence of elements of a specific
 * primitive type.  Aside from its content, the essential properties of a
 * buffer are its capacity, limit, and position: </p>
 *
 * <blockquote>
 *
 *   <p> A buffer's <i>capacity</i> is the number of elements it contains.  The
 *   capacity of a buffer is never negative and never changes.  </p>
 *
 *   <p> A buffer's <i>limit</i> is the index of the first element that should
 *   not be read or written.  A buffer's limit is never negative and is never
 *   greater than its capacity.  </p>
 *
 *   <p> A buffer's <i>position</i> is the index of the next element to be
 *   read or written.  A buffer's position is never negative and is never
 *   greater than its limit.  </p>
 *
 * </blockquote>
 *
 * <p> There is one subclass of this class for each non-boolean primitive type.
 *
 *
 * <h2> Transferring data </h2>
 *
 * <p> Each subclass of this class defines two categories of <i>get</i> and
 * <i>put</i> operations: </p>
 *
 * <blockquote>
 *
 *   <p> <i>Relative</i> operations read or write one or more elements starting
 *   at the current position and then increment the position by the number of
 *   elements transferred.  If the requested transfer exceeds the limit then a
 *   relative <i>get</i> operation throws a {@link BufferUnderflowException}
 *   and a relative <i>put</i> operation throws a {@link
 *   BufferOverflowException}; in either case, no data is transferred.  </p>
 *
 *   <p> <i>Absolute</i> operations take an explicit element index and do not
 *   affect the position.  Absolute <i>get</i> and <i>put</i> operations throw
 *   an {@link IndexOutOfBoundsException} if the index argument exceeds the
 *   limit.  </p>
 *
 * </blockquote>
 *
 * <p> Data may also, of course, be transferred in to or out of a buffer by the
 * I/O operations of an appropriate channel, which are always relative to the
 * current position.
 *
 *
 * <h2> Marking and resetting </h2>
 *
 * <p> A buffer's <i>mark</i> is the index to which its position will be reset
 * when the {@link #reset reset} method is invoked.  The mark is not always
 * defined, but when it is defined it is never negative and is never greater
 * than the position.  If the mark is defined then it is discarded when the
 * position or the limit is adjusted to a value smaller than the mark.  If the
 * mark is not defined then invoking the {@link #reset reset} method causes an
 * {@link InvalidMarkException} to be thrown.
 *
 *
 * <h2> Invariants </h2>
 *
 * <p> The following invariant holds for the mark, position, limit, and
 * capacity values:
 *
 * <blockquote>
 *     <tt>0</tt> <tt>&lt;=</tt>
 *     <i>mark</i> <tt>&lt;=</tt>
 *     <i>position</i> <tt>&lt;=</tt>
 *     <i>limit</i> <tt>&lt;=</tt>
 *     <i>capacity</i>
 * </blockquote>
 *
 * <p> A newly-created buffer always has a position of zero and a mark that is
 * undefined.  The initial limit may be zero, or it may be some other value
 * that depends upon the type of the buffer and the manner in which it is
 * constructed.  Each element of a newly-allocated buffer is initialized
 * to zero.
 *
 *
 * <h2> Clearing, flipping, and rewinding </h2>
 *
 * <p> In addition to methods for accessing the position, limit, and capacity
 * values and for marking and resetting, this class also defines the following
 * operations upon buffers:
 *
 * <ul>
 *
 *   <li><p> {@link #clear} makes a buffer ready for a new sequence of
 *   channel-read or relative <i>put</i> operations: It sets the limit to the
 *   capacity and the position to zero.  </p></li>
 *
 *   <li><p> {@link #flip} makes a buffer ready for a new sequence of
 *   channel-write or relative <i>get</i> operations: It sets the limit to the
 *   current position and then sets the position to zero.  </p></li>
 *
 *   <li><p> {@link #rewind} makes a buffer ready for re-reading the data that
 *   it already contains: It leaves the limit unchanged and sets the position
 *   to zero.  </p></li>
 *
 * </ul>
 *
 *
 * <h2> Read-only buffers </h2>
 *
 * <p> Every buffer is readable, but not every buffer is writable.  The
 * mutation methods of each buffer class are specified as <i>optional
 * operations</i> that will throw a {@link ReadOnlyBufferException} when
 * invoked upon a read-only buffer.  A read-only buffer does not allow its
 * content to be changed, but its mark, position, and limit values are mutable.
 * Whether or not a buffer is read-only may be determined by invoking its
 * {@link #isReadOnly isReadOnly} method.
 *
 *
 * <h2> Thread safety </h2>
 *
 * <p> Buffers are not safe for use by multiple concurrent threads.  If a
 * buffer is to be used by more than one thread then access to the buffer
 * should be controlled by appropriate synchronization.
 *
 *
 * <h2> Invocation chaining </h2>
 *
 * <p> Methods in this class that do not otherwise have a value to return are
 * specified to return the buffer upon which they are invoked.  This allows
 * method invocations to be chained; for example, the sequence of statements
 *
 * <blockquote><pre>
 * b.flip();
 * b.position(23);
 * b.limit(42);</pre></blockquote>
 *
 * can be replaced by the single, more compact statement
 *
 * <blockquote><pre>
 * b.flip().position(23).limit(42);</pre></blockquote>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 特定基元类型数据的容器。
缓冲区是特定原始类型的元素的线性、有限序列。除了内容之外，缓冲区的基本属性是它的容量、限制和位置:
缓冲区的容量是它包含的元素的数量。缓冲区的容量永远不会是负的，也不会改变。
缓冲区的限制是第一个不应该被读写的元素的索引。缓冲区的极限永远不会是负的，也永远不会大于它的容量。
缓冲区的位置是要读取或写入的下一个元素的索引。缓冲区的位置永远不会是负的，也不会大于它的极限。
对于每个非布尔基元类型，这个类有一个子类。

传输数据
该类的每个子类定义了两类get和put操作:
相对操作读取或写入一个或多个元素，从当前位置开始，然后通过传输的元素数量增加位置。如果请求的传输超过限制，那么相对get操作抛出BufferUnderflowException，而相对put操作抛出BufferOverflowException;无论哪种情况，都不会传输数据。
绝对操作采用显式元素索引，不影响位置。如果索引参数超过限制，则绝对get和put操作抛出IndexOutOfBoundsException。
当然，数据也可以通过适当通道的I/O操作传输到缓冲区或从缓冲区中传输出去，这些操作总是相对于当前位置的。

标记和重置
缓冲区的标记是当调用reset方法时它的位置将被重置的索引。标记并不总是被定义，但是当它被定义时，它永远不会是负的，也永远不会大于位置。如果标记被定义，那么当位置或限制被调整为小于标记的值时，它就被丢弃。如果没有定义标记，那么调用reset方法将引发一个InvalidMarkException。

不变量
以下不变式适用于标记、位置、限制和容量值:
0 <= mark <= position <= limit > = capacity
新创建的缓冲区的位置总是为0，标记是未定义的。初始限制可能为零，也可能是依赖于缓冲区的类型及其构造方式的其他值。新分配缓冲区的每个元素都被初始化为零。

清算、翻转和复卷
除了访问位置、限制和容量值以及标记和重置的方法外，这个类还定义了缓冲区上的以下操作:
clear使缓冲区为通道读取或相对放置操作的新序列做好准备:它将容量限制和位置设置为零。
flip使缓冲区为新的通道写入或相对get操作序列做好准备:它将限制设置为当前位置，然后将位置设置为0。
rewind使一个缓冲区准备好重新读取它已经包含的数据:它保持限制不变，并将位置设置为0。

只读缓冲区
每个缓冲区都是可读的，但不是每个缓冲区都是可写的。每个缓冲区类的突变方法都被指定为可选操作，当在只读缓冲区上调用时将抛出ReadOnlyBufferException。只读缓冲区不允许更改其内容，但其标记、位置和限制值是可变的。是否一个缓冲区是只读的可以通过调用它的isReadOnly方法来确定。

线程安全
对于多个并发线程来说，缓冲区不安全。如果一个缓冲区被多个线程使用，那么对缓冲区的访问应该由适当的同步控制。

调用链接
这个类中没有返回值的方法被指定为返回被调用的缓冲区。这允许对方法调用进行链接;例如，语句序列
b.flip();
b.position(23);
b.limit(42);
可以用更简洁的语句代替吗
b.flip().position(23).limit(42);
 */

//缓冲区的基本属性是它的 capacity、limit 和 position
//    缓冲区的capacity是它包含的元素的数量
//缓冲区的limit是第一个不应该被读写的元素的索引。不会大于它的capacity。
//        缓冲区的position是要读取或写入的下一个元素的索引。不会大于它的limit。0 <= mark <= position <= limit > = capacity
//    clear 它将limit和position设置为零，为通道的读取和写入操作做好准备
//    flip 它将limit设置为当前位置，然后将position设置为0。为通道写入或get操作做好准备
//    rewind 它保持limit不变，并将position设置为0，缓冲区重新读取，非线程安全
public abstract class Buffer {

    /**
     * The characteristics of Spliterators that traverse and split elements
     * maintained in Buffers.在缓冲区中遍历和拆分元素的spliterator的特性。
     */
    static final int SPLITERATOR_CHARACTERISTICS =
        Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;

    // Invariants: mark <= position <= limit <= capacity
    private int mark = -1;
    private int position = 0;
    private int limit;
    private int capacity;

    // Used only by direct buffers仅用于直接缓冲区
    // NOTE: hoisted here for speed in JNI GetDirectBufferAddress在JNI GetDirectBufferAddress中提升速度
    long address;

    // Creates a new buffer with the given mark, position, limit, and capacity,使用给定的标记、位置、限制和容量创建一个新的缓冲区，
    // after checking invariants.后检查不变量。
    //
    Buffer(int mark, int pos, int lim, int cap) {       // package-private
        if (cap < 0)
            throw new IllegalArgumentException("Negative capacity: " + cap);
        this.capacity = cap;
        limit(lim);
        position(pos);
        if (mark >= 0) {
//            mark不能大于position
            if (mark > pos)
                throw new IllegalArgumentException("mark > position: ("
                                                   + mark + " > " + pos + ")");
            this.mark = mark;
        }
    }

    /**
     * Returns this buffer's capacity.
     *
     * @return  The capacity of this buffer
     */
    public final int capacity() {
        return capacity;
    }

    /**
     * Returns this buffer's position.
     *
     * @return  The position of this buffer
     */
    public final int position() {
        return position;
    }

    /**
     * Sets this buffer's position.  If the mark is defined and larger than the
     * new position then it is discarded.
     *
     * @param  newPosition
     *         The new position value; must be non-negative
     *         and no larger than the current limit
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newPosition</tt> do not hold
     *          设置这个缓冲区的位置。如果标记被定义并且大于新位置，那么它将被丢弃。
     */
    public final Buffer position(int newPosition) {
//        position不能大于limit
        if ((newPosition > limit) || (newPosition < 0))
            throw new IllegalArgumentException();
        position = newPosition;
//        mark不能大于position
        if (mark > position) mark = -1;
        return this;
    }

    /**
     * Returns this buffer's limit.
     *
     * @return  The limit of this buffer
     */
    public final int limit() {
        return limit;
    }

    /**
     * Sets this buffer's limit.  If the position is larger than the new limit
     * then it is set to the new limit.  If the mark is defined and larger than
     * the new limit then it is discarded.
     *
     * @param  newLimit
     *         The new limit value; must be non-negative
     *         and no larger than this buffer's capacity
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newLimit</tt> do not hold
     *          设置这个缓冲区的限制。如果这个位置大于新的极限，那么它就会被设置为新的极限。如果标记被定义并且大于新的限制，那么它将被丢弃。
     */
    public final Buffer limit(int newLimit) {
//        limit不能大于capacity
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        limit = newLimit;
//        position不能大于limit
        if (position > limit) position = limit;
//        mark不能大于limit
        if (mark > limit) mark = -1;
        return this;
    }

    /**
     * Sets this buffer's mark at its position.
     *
     * @return  This buffer
     */
    public final Buffer mark() {
        mark = position;
        return this;
    }

    /**
     * Resets this buffer's position to the previously-marked position.
     *
     * <p> Invoking this method neither changes nor discards the mark's
     * value. </p>
     *
     * @return  This buffer
     *
     * @throws  InvalidMarkException
     *          If the mark has not been set
     *          将缓冲区的位置重置为先前标记的位置。
    调用此方法既不更改也不丢弃标记的值。
     */
    public final Buffer reset() {
        int m = mark;
        if (m < 0)
            throw new InvalidMarkException();
        position = m;
        return this;
    }

    /**
     * Clears this buffer.  The position is set to zero, the limit is set to
     * the capacity, and the mark is discarded.
     *
     * <p> Invoke this method before using a sequence of channel-read or
     * <i>put</i> operations to fill this buffer.  For example:
     *
     * <blockquote><pre>
     * buf.clear();     // Prepare buffer for reading
     * in.read(buf);    // Read data</pre></blockquote>
     *
     * <p> This method does not actually erase the data in the buffer, but it
     * is named as if it did because it will most often be used in situations
     * in which that might as well be the case. </p>
     *
     * @return  This buffer
     * 清除这个缓冲区。位置被设置为0，极限被设置为容量，标记被丢弃。
    在使用通道读取或放置操作序列填充此缓冲区之前调用此方法。例如:
    这个方法实际上并没有删除缓冲区中的数据，但是它被命名为它，因为它经常被用于可能也是这样的情况。
     */
//    准备读取和写入
    public final Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    /**
     * Flips this buffer.  The limit is set to the current position and then
     * the position is set to zero.  If the mark is defined then it is
     * discarded.
     *
     * <p> After a sequence of channel-read or <i>put</i> operations, invoke
     * this method to prepare for a sequence of channel-write or relative
     * <i>get</i> operations.  For example:
     *
     * <blockquote><pre>
     * buf.put(magic);    // Prepend header
     * in.read(buf);      // Read data into rest of buffer
     * buf.flip();        // Flip buffer
     * out.write(buf);    // Write header + data to channel</pre></blockquote>
     *
     * <p> This method is often used in conjunction with the {@link
     * java.nio.ByteBuffer#compact compact} method when transferring data from
     * one place to another.  </p>
     *
     * @return  This buffer
     * 翻转这个缓冲区。将极限设置为当前位置，然后将位置设置为零。如果标记被定义，那么它就被丢弃。
    在一系列通道读取或放置操作之后，调用此方法以准备通道写入或相对get操作序列。例如:
    在将数据从一个地方传输到另一个地方时，这种方法通常与compact方法一起使用。
     */
//    反转缓冲区，准备通道写入和get操作
    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    /**
     * Rewinds this buffer.  The position is set to zero and the mark is
     * discarded.
     *
     * <p> Invoke this method before a sequence of channel-write or <i>get</i>
     * operations, assuming that the limit has already been set
     * appropriately.  For example:
     *
     * <blockquote><pre>
     * out.write(buf);    // Write remaining data
     * buf.rewind();      // Rewind buffer
     * buf.get(array);    // Copy data into array</pre></blockquote>
     *
     * @return  This buffer
     * 倒带这个缓冲区。位置被设置为0，标记被丢弃。
    在一系列通道写操作或get操作之前调用此方法，假设已经适当地设置了限制。例如:
     */
//    倒带缓冲区，在通道写操作和get方法调用之前调用
    public final Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

//    clear、flip、rewind方法的区别
//    clear 用于写模式，清空buffer，limit==capacity，position = 0，mark = -1;
//    flip 将写模式转换成读模式
//    rewind 读写模式，position = 0; mark = -1;写模式重新开始写，读模式重新读

    /**
     * Returns the number of elements between the current position and the
     * limit.返回当前位置和极限之间的元素个数。
     *
     * @return  The number of elements remaining in this buffer
     */
//    返回position和limit之前的元素个数
    public final int remaining() {
        return limit - position;
    }

    /**
     * Tells whether there are any elements between the current position and
     * the limit.告诉当前位置和极限之间是否有任何元素。
     *
     * @return  <tt>true</tt> if, and only if, there is at least one element
     *          remaining in this buffer
     */
//    是否还可以写入数据
    public final boolean hasRemaining() {
        return position < limit;
    }

    /**
     * Tells whether or not this buffer is read-only.告诉此缓冲区是否为只读。
     *
     * @return  <tt>true</tt> if, and only if, this buffer is read-only
     */
    public abstract boolean isReadOnly();

    /**
     * Tells whether or not this buffer is backed by an accessible
     * array.
     *
     * <p> If this method returns <tt>true</tt> then the {@link #array() array}
     * and {@link #arrayOffset() arrayOffset} methods may safely be invoked.
     * </p>
     *
     * @return  <tt>true</tt> if, and only if, this buffer
     *          is backed by an array and is not read-only
     *
     * @since 1.6
     * 告诉此缓冲区是否由可访问数组支持。
    如果该方法返回true，那么可以安全地调用数组和arrayOffset方法。
     */
    public abstract boolean hasArray();

    /**
     * Returns the array that backs this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method is intended to allow array-backed buffers to be
     * passed to native code more efficiently. Concrete subclasses
     * provide more strongly-typed return values for this method.
     *
     * <p> Modifications to this buffer's content will cause the returned
     * array's content to be modified, and vice versa.
     *
     * <p> Invoke the {@link #hasArray hasArray} method before invoking this
     * method in order to ensure that this buffer has an accessible backing
     * array.  </p>
     *
     * @return  The array that backs this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is backed by an array but is read-only
     *
     * @throws  UnsupportedOperationException
     *          If this buffer is not backed by an accessible array
     *
     * @since 1.6
     * 返回支持此缓冲区的数组(可选操作)。
    这个方法的目的是让数组支持的缓冲区能够更有效地传递给本机代码。具体的子类为这个方法提供更强类型的返回值。
    对该缓冲区内容的修改将导致返回的数组内容被修改，反之亦然。
    在调用此方法之前调用hasArray方法，以确保此缓冲区具有可访问的支持数组。
     */
    public abstract Object array();

    /**
     * Returns the offset within this buffer's backing array of the first
     * element of the buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> If this buffer is backed by an array then buffer position <i>p</i>
     * corresponds to array index <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>.
     *
     * <p> Invoke the {@link #hasArray hasArray} method before invoking this
     * method in order to ensure that this buffer has an accessible backing
     * array.  </p>
     *
     * @return  The offset within this buffer's array
     *          of the first element of the buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is backed by an array but is read-only
     *
     * @throws  UnsupportedOperationException
     *          If this buffer is not backed by an accessible array
     *
     * @since 1.6
     * 返回缓冲区第一个元素(可选操作)在该缓冲区的支持数组中的偏移量。
    如果此缓冲区由数组支持，则缓冲区位置p对应于数组索引p + arrayOffset()。
    在调用此方法之前调用hasArray方法，以确保此缓冲区具有可访问的支持数组。
     */
    public abstract int arrayOffset();

    /**
     * Tells whether or not this buffer is
     * <a href="ByteBuffer.html#direct"><i>direct</i></a>.
     *
     * @return  <tt>true</tt> if, and only if, this buffer is direct
     *
     * @since 1.6
     * 告诉此缓冲区是否为直接的。
     */
    public abstract boolean isDirect();


    // -- Package-private methods for bounds checking, etc. --

    /**
     * Checks the current position against the limit, throwing a {@link
     * BufferUnderflowException} if it is not smaller than the limit, and then
     * increments the position.
     *
     * @return  The current position value, before it is incremented
     * 检查当前位置是否超过限制，如果不小于限制，则抛出BufferUnderflowException，然后增加位置。
     */
    final int nextGetIndex() {                          // package-private
        if (position >= limit)
            throw new BufferUnderflowException();
        return position++;
    }

    final int nextGetIndex(int nb) {                    // package-private
        if (limit - position < nb)
            throw new BufferUnderflowException();
        int p = position;
        position += nb;
        return p;
    }

    /**
     * Checks the current position against the limit, throwing a {@link
     * BufferOverflowException} if it is not smaller than the limit, and then
     * increments the position.
     *
     * @return  The current position value, before it is incremented
     * 检查当前位置是否超过限制，如果不小于限制，则抛出BufferOverflowException，然后增加该位置。
     */
    final int nextPutIndex() {                          // package-private
        if (position >= limit)
            throw new BufferOverflowException();
        return position++;
    }

    final int nextPutIndex(int nb) {                    // package-private
        if (limit - position < nb)
            throw new BufferOverflowException();
        int p = position;
        position += nb;
        return p;
    }

    /**
     * Checks the given index against the limit, throwing an {@link
     * IndexOutOfBoundsException} if it is not smaller than the limit
     * or is smaller than zero.
     * 检查给定的索引是否小于极限，如果它不小于极限或小于零，则抛出IndexOutOfBoundsException。
     */
    final int checkIndex(int i) {                       // package-private
        if ((i < 0) || (i >= limit))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int checkIndex(int i, int nb) {               // package-private
        if ((i < 0) || (nb > limit - i))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int markValue() {                             // package-private
        return mark;
    }

    final void truncate() {                             // package-private
        mark = -1;
        position = 0;
        limit = 0;
        capacity = 0;
    }

    final void discardMark() {                          // package-private
        mark = -1;
    }

    static void checkBounds(int off, int len, int size) { // package-private
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
    }

}
