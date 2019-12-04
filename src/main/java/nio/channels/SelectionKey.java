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

package java.nio.channels;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.io.IOException;


/**
 * A token representing the registration of a {@link SelectableChannel} with a
 * {@link Selector}.
 *
 * <p> A selection key is created each time a channel is registered with a
 * selector.  A key remains valid until it is <i>cancelled</i> by invoking its
 * {@link #cancel cancel} method, by closing its channel, or by closing its
 * selector.  Cancelling a key does not immediately remove it from its
 * selector; it is instead added to the selector's <a
 * href="Selector.html#ks"><i>cancelled-key set</i></a> for removal during the
 * next selection operation.  The validity of a key may be tested by invoking
 * its {@link #isValid isValid} method.
 *
 * <a name="opsets"></a>
 *
 * <p> A selection key contains two <i>operation sets</i> represented as
 * integer values.  Each bit of an operation set denotes a category of
 * selectable operations that are supported by the key's channel.
 *
 * <ul>
 *
 *   <li><p> The <i>interest set</i> determines which operation categories will
 *   be tested for readiness the next time one of the selector's selection
 *   methods is invoked.  The interest set is initialized with the value given
 *   when the key is created; it may later be changed via the {@link
 *   #interestOps(int)} method. </p></li>
 *
 *   <li><p> The <i>ready set</i> identifies the operation categories for which
 *   the key's channel has been detected to be ready by the key's selector.
 *   The ready set is initialized to zero when the key is created; it may later
 *   be updated by the selector during a selection operation, but it cannot be
 *   updated directly. </p></li>
 *
 * </ul>
 *
 * <p> That a selection key's ready set indicates that its channel is ready for
 * some operation category is a hint, but not a guarantee, that an operation in
 * such a category may be performed by a thread without causing the thread to
 * block.  A ready set is most likely to be accurate immediately after the
 * completion of a selection operation.  It is likely to be made inaccurate by
 * external events and by I/O operations that are invoked upon the
 * corresponding channel.
 *
 * <p> This class defines all known operation-set bits, but precisely which
 * bits are supported by a given channel depends upon the type of the channel.
 * Each subclass of {@link SelectableChannel} defines an {@link
 * SelectableChannel#validOps() validOps()} method which returns a set
 * identifying just those operations that are supported by the channel.  An
 * attempt to set or test an operation-set bit that is not supported by a key's
 * channel will result in an appropriate run-time exception.
 *
 * <p> It is often necessary to associate some application-specific data with a
 * selection key, for example an object that represents the state of a
 * higher-level protocol and handles readiness notifications in order to
 * implement that protocol.  Selection keys therefore support the
 * <i>attachment</i> of a single arbitrary object to a key.  An object can be
 * attached via the {@link #attach attach} method and then later retrieved via
 * the {@link #attachment() attachment} method.
 *
 * <p> Selection keys are safe for use by multiple concurrent threads.  The
 * operations of reading and writing the interest set will, in general, be
 * synchronized with certain operations of the selector.  Exactly how this
 * synchronization is performed is implementation-dependent: In a naive
 * implementation, reading or writing the interest set may block indefinitely
 * if a selection operation is already in progress; in a high-performance
 * implementation, reading or writing the interest set may block briefly, if at
 * all.  In any case, a selection operation will always use the interest-set
 * value that was current at the moment that the operation began.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see SelectableChannel
 * @see Selector
 * 表示带有选择器的SelectableChannel注册的令牌。
每次向选择器注册通道时都会创建一个选择键。密钥仍然有效，直到通过调用它的cancel方法、关闭它的通道或关闭它的选择器来取消它。取消键不会立即从选择器中删除它;而是将它添加到选择器的cancelkey集中，以便在下一个选择操作中删除。可以通过调用密钥的isValid方法来测试密钥的有效性。
选择键包含两个以整数值表示的操作集。操作集的每个位都表示密钥通道支持的可选择操作的类别。
兴趣集确定在下一次调用选择器的选择方法时，将测试哪些操作类别以准备就绪。用创建键时给定的值初始化兴趣集;以后可以通过利息操作(int)方法更改它。
就绪集标识被密钥的选择器检测到准备就绪的操作类别。当创建密钥时，准备集初始化为零;在选择操作期间，选择器可能稍后更新它，但不能直接更新它。
选择键的就绪集表明它的通道已经为某些操作类别做好了准备，这是一个提示，但不是保证，这样的类别中的操作可以由线程执行，而不会导致线程阻塞。在完成选择操作之后，准备好的集合最有可能是准确的。它可能会因为外部事件和在相应通道上调用的I/O操作而变得不准确。
这个类定义所有已知的操作集位，但是特定通道支持的位的确切数量取决于通道的类型。SelectableChannel的每个子类定义了一个validOps()方法，该方法返回一个集合，它只识别由通道支持的操作。尝试设置或测试密钥通道不支持的操作集位将导致适当的运行时异常。
通常需要将特定于应用程序的数据与选择键相关联，例如，对象表示高级协议的状态并处理就绪通知，以便实现该协议。因此，选择键支持将单个任意对象连接到一个键。对象可以通过attach方法附加，然后通过attachment方法获取。
选择键是安全的，可以由多个并发线程使用。通常，读取和写入感兴趣集的操作将与选择器的某些操作同步。确切地说，这种同步是如何执行的取决于实现:在一个简单的实现中，如果选择操作已经在进行中，读取或写入兴趣集可能会无限期地阻塞;在高性能实现中，读取或编写兴趣集可能会短暂地阻塞(如果有的话)。无论如何，选择操作总是使用操作开始时当前的计息集值。
 */

public abstract class SelectionKey {

    /**
     * Constructs an instance of this class.
     */
    protected SelectionKey() { }


    // -- Channel and selector operations --

    /**
     * Returns the channel for which this key was created.  This method will
     * continue to return the channel even after the key is cancelled.返回创建此键的通道。这个方法将继续返回通道，即使在密钥被取消之后。
     *
     * @return  This key's channel
     */
    public abstract SelectableChannel channel();

    /**
     * Returns the selector for which this key was created.  This method will
     * continue to return the selector even after the key is cancelled.返回为此键创建的选择器。这个方法将继续返回选择器，即使在键被取消之后。
     *
     * @return  This key's selector
     */
    public abstract Selector selector();

    /**
     * Tells whether or not this key is valid.
     *
     * <p> A key is valid upon creation and remains so until it is cancelled,
     * its channel is closed, or its selector is closed.  </p>
     *
     * @return  <tt>true</tt> if, and only if, this key is valid
     * 告诉此键是否有效。
    一个键在创建时是有效的，直到它被取消，它的通道被关闭，或者它的选择器被关闭。
     */
    public abstract boolean isValid();

    /**
     * Requests that the registration of this key's channel with its selector
     * be cancelled.  Upon return the key will be invalid and will have been
     * added to its selector's cancelled-key set.  The key will be removed from
     * all of the selector's key sets during the next selection operation.
     *
     * <p> If this key has already been cancelled then invoking this method has
     * no effect.  Once cancelled, a key remains forever invalid. </p>
     *
     * <p> This method may be invoked at any time.  It synchronizes on the
     * selector's cancelled-key set, and therefore may block briefly if invoked
     * concurrently with a cancellation or selection operation involving the
     * same selector.  </p>
     * 请求取消该密钥的通道的注册。在返回时，该键将无效，并将被添加到其选择器的cancelkey集中。在下一次选择操作中，该键将从所有选择器的键集中删除。
     如果这个键已经被取消，那么调用这个方法没有任何效果。一旦取消，钥匙将永远无效。
     此方法可以随时调用。它对选择器的取消键集进行同步，因此，如果与涉及相同选择器的取消或选择操作同时调用时，可能会短暂地阻塞。
     */
    public abstract void cancel();


    // -- Operation-set accessors --

    /**
     * Retrieves this key's interest set.
     *
     * <p> It is guaranteed that the returned set will only contain operation
     * bits that are valid for this key's channel.
     *
     * <p> This method may be invoked at any time.  Whether or not it blocks,
     * and for how long, is implementation-dependent.  </p>
     *
     * @return  This key's interest set
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          检索此键的兴趣集。
    它保证返回的集合将只包含对该密钥的通道有效的操作位。
    此方法可以随时调用。它是否阻塞，以及多长时间依赖于实现。
     */
    public abstract int interestOps();

    /**
     * Sets this key's interest set to the given value.
     *
     * <p> This method may be invoked at any time.  Whether or not it blocks,
     * and for how long, is implementation-dependent.  </p>
     *
     * @param  ops  The new interest set
     *
     * @return  This selection key
     *
     * @throws  IllegalArgumentException
     *          If a bit in the set does not correspond to an operation that
     *          is supported by this key's channel, that is, if
     *          {@code (ops & ~channel().validOps()) != 0}
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          将此键的兴趣集设置为给定值。
    此方法可以随时调用。它是否阻塞，以及多长时间依赖于实现。
     */
    public abstract SelectionKey interestOps(int ops);

    /**
     * Retrieves this key's ready-operation set.
     *
     * <p> It is guaranteed that the returned set will only contain operation
     * bits that are valid for this key's channel.  </p>
     *
     * @return  This key's ready-operation set
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          检索此键的预操作集。
    它保证返回的集合将只包含对该密钥的通道有效的操作位。
     */
    public abstract int readyOps();


    // -- Operation bits and bit-testing convenience methods --

    /**
     * Operation-set bit for read operations.
     *
     * <p> Suppose that a selection key's interest set contains
     * <tt>OP_READ</tt> at the start of a <a
     * href="Selector.html#selop">selection operation</a>.  If the selector
     * detects that the corresponding channel is ready for reading, has reached
     * end-of-stream, has been remotely shut down for further reading, or has
     * an error pending, then it will add <tt>OP_READ</tt> to the key's
     * ready-operation set and add the key to its selected-key&nbsp;set.  </p>
     * 读操作的操作设置位。
     假设选择键的兴趣集在选择操作开始时包含OP_READ。如果选择器检测到对应的通道已经准备好进行读取，已经到达流的末端，已经被远程关闭以进行进一步的读取，或者有一个错误挂起，那么它将把OP_READ添加到密钥的现成操作集，并将密钥添加到它的选择密钥集。
     */
    public static final int OP_READ = 1 << 0;

    /**
     * Operation-set bit for write operations.
     *
     * <p> Suppose that a selection key's interest set contains
     * <tt>OP_WRITE</tt> at the start of a <a
     * href="Selector.html#selop">selection operation</a>.  If the selector
     * detects that the corresponding channel is ready for writing, has been
     * remotely shut down for further writing, or has an error pending, then it
     * will add <tt>OP_WRITE</tt> to the key's ready set and add the key to its
     * selected-key&nbsp;set.  </p>
     * 写入操作的操作设置位。
     假设一个选择键的兴趣集包含OP_WRITE在选择操作的开始。如果选择器检测到相应的通道已准备好写入，已被远程关闭以进行进一步写入，或者有一个错误挂起，那么它将向键的准备集添加OP_WRITE，并将键添加到其选择键集。
     */
    public static final int OP_WRITE = 1 << 2;

    /**
     * Operation-set bit for socket-connect operations.
     *
     * <p> Suppose that a selection key's interest set contains
     * <tt>OP_CONNECT</tt> at the start of a <a
     * href="Selector.html#selop">selection operation</a>.  If the selector
     * detects that the corresponding socket channel is ready to complete its
     * connection sequence, or has an error pending, then it will add
     * <tt>OP_CONNECT</tt> to the key's ready set and add the key to its
     * selected-key&nbsp;set.  </p>
     * 串口连接操作的操作设置位。
     假设选择键的兴趣集在选择操作开始时包含OP_CONNECT。如果选择器检测到相应的套接字通道已准备好完成其连接序列，或者有一个错误挂起，那么它将向密钥的准备集添加OP_CONNECT，并将密钥添加到其选择密钥集。
     */
    public static final int OP_CONNECT = 1 << 3;

    /**
     * Operation-set bit for socket-accept operations.
     *
     * <p> Suppose that a selection key's interest set contains
     * <tt>OP_ACCEPT</tt> at the start of a <a
     * href="Selector.html#selop">selection operation</a>.  If the selector
     * detects that the corresponding server-socket channel is ready to accept
     * another connection, or has an error pending, then it will add
     * <tt>OP_ACCEPT</tt> to the key's ready set and add the key to its
     * selected-key&nbsp;set.  </p>
     * 套接操作的操作设置位。
     假设选择键的兴趣集在选择操作开始时包含OP_ACCEPT。如果选择器检测到相应的服务器-套接字通道准备接受另一个连接，或者有一个错误挂起，那么它将向密钥的准备集添加OP_ACCEPT，并将密钥添加到其选择密钥集。
     */
    public static final int OP_ACCEPT = 1 << 4;

    /**
     * Tests whether this key's channel is ready for reading.
     *
     * <p> An invocation of this method of the form <tt>k.isReadable()</tt>
     * behaves in exactly the same way as the expression
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_READ != 0
     * }</pre></blockquote>
     *
     * <p> If this key's channel does not support read operations then this
     * method always returns <tt>false</tt>.  </p>
     *
     * @return  <tt>true</tt> if, and only if,
                {@code readyOps() & OP_READ} is nonzero
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          测试这个键的通道是否为读取做好了准备。
    函数k. is可读性()的这种方法的调用与表达式的行为完全相同
    如果这个键的通道不支持读操作，那么这个方法总是返回false。
     */
    public final boolean isReadable() {
        return (readyOps() & OP_READ) != 0;
    }

    /**
     * Tests whether this key's channel is ready for writing.
     *
     * <p> An invocation of this method of the form <tt>k.isWritable()</tt>
     * behaves in exactly the same way as the expression
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_WRITE != 0
     * }</pre></blockquote>
     *
     * <p> If this key's channel does not support write operations then this
     * method always returns <tt>false</tt>.  </p>
     *
     * @return  <tt>true</tt> if, and only if,
     *          {@code readyOps() & OP_WRITE} is nonzero
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          测试这个密钥的通道是否可以写入。
    对form k.isWritable()的此方法的调用与表达式的行为完全相同
    如果这个键的通道不支持写操作，那么这个方法总是返回false。
     */
    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) != 0;
    }

    /**
     * Tests whether this key's channel has either finished, or failed to
     * finish, its socket-connection operation.
     *
     * <p> An invocation of this method of the form <tt>k.isConnectable()</tt>
     * behaves in exactly the same way as the expression
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_CONNECT != 0
     * }</pre></blockquote>
     *
     * <p> If this key's channel does not support socket-connect operations
     * then this method always returns <tt>false</tt>.  </p>
     *
     * @return  <tt>true</tt> if, and only if,
     *          {@code readyOps() & OP_CONNECT} is nonzero
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          测试此键的通道是否已完成或未能完成其套接连接操作。
    对form k.isConnectable()的这个方法的调用与表达式的行为完全相同
    如果这个键的通道不支持socket-connect操作，那么这个方法总是返回false。
     */
    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) != 0;
    }

    /**
     * Tests whether this key's channel is ready to accept a new socket
     * connection.
     *
     * <p> An invocation of this method of the form <tt>k.isAcceptable()</tt>
     * behaves in exactly the same way as the expression
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_ACCEPT != 0
     * }</pre></blockquote>
     *
     * <p> If this key's channel does not support socket-accept operations then
     * this method always returns <tt>false</tt>.  </p>
     *
     * @return  <tt>true</tt> if, and only if,
     *          {@code readyOps() & OP_ACCEPT} is nonzero
     *
     * @throws  CancelledKeyException
     *          If this key has been cancelled
     *          测试这个密钥的通道是否已经准备好接受一个新的套接字连接。
    函数k. isaccep()的这种方法的调用行为与表达式完全相同
    如果这个键的通道不支持套接操作，那么这个方法总是返回false。
     */
    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) != 0;
    }


    // -- Attachments --

    private volatile Object attachment = null;

    private static final AtomicReferenceFieldUpdater<SelectionKey,Object>
        attachmentUpdater = AtomicReferenceFieldUpdater.newUpdater(
            SelectionKey.class, Object.class, "attachment"
        );

    /**
     * Attaches the given object to this key.
     *
     * <p> An attached object may later be retrieved via the {@link #attachment()
     * attachment} method.  Only one object may be attached at a time; invoking
     * this method causes any previous attachment to be discarded.  The current
     * attachment may be discarded by attaching <tt>null</tt>.  </p>
     *
     * @param  ob
     *         The object to be attached; may be <tt>null</tt>
     *
     * @return  The previously-attached object, if any,
     *          otherwise <tt>null</tt>
     *          将给定的对象附加到这个键上。
    随后可以通过附件方法检索所附加的对象。每次只能附加一个对象;调用此方法会导致任何以前的附件被丢弃。可以通过附加null来丢弃当前的附件。
     */
    public final Object attach(Object ob) {
        return attachmentUpdater.getAndSet(this, ob);
    }

    /**
     * Retrieves the current attachment.
     *
     * @return  The object currently attached to this key,
     *          or <tt>null</tt> if there is no attachment
     */
    public final Object attachment() {
        return attachment;
    }

}
