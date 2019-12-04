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

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;


/**
 * A multiplexor of {@link SelectableChannel} objects.
 *
 * <p> A selector may be created by invoking the {@link #open open} method of
 * this class, which will use the system's default {@link
 * java.nio.channels.spi.SelectorProvider selector provider} to
 * create a new selector.  A selector may also be created by invoking the
 * {@link java.nio.channels.spi.SelectorProvider#openSelector openSelector}
 * method of a custom selector provider.  A selector remains open until it is
 * closed via its {@link #close close} method.
 *
 * <a name="ks"></a>
 *
 * <p> A selectable channel's registration with a selector is represented by a
 * {@link SelectionKey} object.  A selector maintains three sets of selection
 * keys:
 *
 * <ul>
 *
 *   <li><p> The <i>key set</i> contains the keys representing the current
 *   channel registrations of this selector.  This set is returned by the
 *   {@link #keys() keys} method. </p></li>
 *
 *   <li><p> The <i>selected-key set</i> is the set of keys such that each
 *   key's channel was detected to be ready for at least one of the operations
 *   identified in the key's interest set during a prior selection operation.
 *   This set is returned by the {@link #selectedKeys() selectedKeys} method.
 *   The selected-key set is always a subset of the key set. </p></li>
 *
 *   <li><p> The <i>cancelled-key</i> set is the set of keys that have been
 *   cancelled but whose channels have not yet been deregistered.  This set is
 *   not directly accessible.  The cancelled-key set is always a subset of the
 *   key set. </p></li>
 *
 * </ul>
 *
 * <p> All three sets are empty in a newly-created selector.
 *
 * <p> A key is added to a selector's key set as a side effect of registering a
 * channel via the channel's {@link SelectableChannel#register(Selector,int)
 * register} method.  Cancelled keys are removed from the key set during
 * selection operations.  The key set itself is not directly modifiable.
 *
 * <p> A key is added to its selector's cancelled-key set when it is cancelled,
 * whether by closing its channel or by invoking its {@link SelectionKey#cancel
 * cancel} method.  Cancelling a key will cause its channel to be deregistered
 * during the next selection operation, at which time the key will removed from
 * all of the selector's key sets.
 *
 * <a name="sks"></a><p> Keys are added to the selected-key set by selection
 * operations.  A key may be removed directly from the selected-key set by
 * invoking the set's {@link java.util.Set#remove(java.lang.Object) remove}
 * method or by invoking the {@link java.util.Iterator#remove() remove} method
 * of an {@link java.util.Iterator iterator} obtained from the
 * set.  Keys are never removed from the selected-key set in any other way;
 * they are not, in particular, removed as a side effect of selection
 * operations.  Keys may not be added directly to the selected-key set. </p>
 *
 *
 * <a name="selop"></a>
 * <h2>Selection</h2>
 *
 * <p> During each selection operation, keys may be added to and removed from a
 * selector's selected-key set and may be removed from its key and
 * cancelled-key sets.  Selection is performed by the {@link #select()}, {@link
 * #select(long)}, and {@link #selectNow()} methods, and involves three steps:
 * </p>
 *
 * <ol>
 *
 *   <li><p> Each key in the cancelled-key set is removed from each key set of
 *   which it is a member, and its channel is deregistered.  This step leaves
 *   the cancelled-key set empty. </p></li>
 *
 *   <li><p> The underlying operating system is queried for an update as to the
 *   readiness of each remaining channel to perform any of the operations
 *   identified by its key's interest set as of the moment that the selection
 *   operation began.  For a channel that is ready for at least one such
 *   operation, one of the following two actions is performed: </p>
 *
 *   <ol>
 *
 *     <li><p> If the channel's key is not already in the selected-key set then
 *     it is added to that set and its ready-operation set is modified to
 *     identify exactly those operations for which the channel is now reported
 *     to be ready.  Any readiness information previously recorded in the ready
 *     set is discarded.  </p></li>
 *
 *     <li><p> Otherwise the channel's key is already in the selected-key set,
 *     so its ready-operation set is modified to identify any new operations
 *     for which the channel is reported to be ready.  Any readiness
 *     information previously recorded in the ready set is preserved; in other
 *     words, the ready set returned by the underlying system is
 *     bitwise-disjoined into the key's current ready set. </p></li>
 *
 *   </ol>
 *
 *   If all of the keys in the key set at the start of this step have empty
 *   interest sets then neither the selected-key set nor any of the keys'
 *   ready-operation sets will be updated.
 *
 *   <li><p> If any keys were added to the cancelled-key set while step (2) was
 *   in progress then they are processed as in step (1). </p></li>
 *
 * </ol>
 *
 * <p> Whether or not a selection operation blocks to wait for one or more
 * channels to become ready, and if so for how long, is the only essential
 * difference between the three selection methods. </p>
 *
 *
 * <h2>Concurrency</h2>
 *
 * <p> Selectors are themselves safe for use by multiple concurrent threads;
 * their key sets, however, are not.
 *
 * <p> The selection operations synchronize on the selector itself, on the key
 * set, and on the selected-key set, in that order.  They also synchronize on
 * the cancelled-key set during steps (1) and (3) above.
 *
 * <p> Changes made to the interest sets of a selector's keys while a
 * selection operation is in progress have no effect upon that operation; they
 * will be seen by the next selection operation.
 *
 * <p> Keys may be cancelled and channels may be closed at any time.  Hence the
 * presence of a key in one or more of a selector's key sets does not imply
 * that the key is valid or that its channel is open.  Application code should
 * be careful to synchronize and check these conditions as necessary if there
 * is any possibility that another thread will cancel a key or close a channel.
 *
 * <p> A thread blocked in one of the {@link #select()} or {@link
 * #select(long)} methods may be interrupted by some other thread in one of
 * three ways:
 *
 * <ul>
 *
 *   <li><p> By invoking the selector's {@link #wakeup wakeup} method,
 *   </p></li>
 *
 *   <li><p> By invoking the selector's {@link #close close} method, or
 *   </p></li>
 *
 *   <li><p> By invoking the blocked thread's {@link
 *   java.lang.Thread#interrupt() interrupt} method, in which case its
 *   interrupt status will be set and the selector's {@link #wakeup wakeup}
 *   method will be invoked. </p></li>
 *
 * </ul>
 *
 * <p> The {@link #close close} method synchronizes on the selector and all
 * three key sets in the same order as in a selection operation.
 *
 * <a name="ksc"></a>
 *
 * <p> A selector's key and selected-key sets are not, in general, safe for use
 * by multiple concurrent threads.  If such a thread might modify one of these
 * sets directly then access should be controlled by synchronizing on the set
 * itself.  The iterators returned by these sets' {@link
 * java.util.Set#iterator() iterator} methods are <i>fail-fast:</i> If the set
 * is modified after the iterator is created, in any way except by invoking the
 * iterator's own {@link java.util.Iterator#remove() remove} method, then a
 * {@link java.util.ConcurrentModificationException} will be thrown. </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see SelectableChannel
 * @see SelectionKey
 *
选择通道对象的多路复用器。
可以通过调用该类的open方法来创建一个选择器，该方法将使用系统的默认选择器提供程序来创建一个新的选择器。还可以通过调用自定义选择器提供程序的openSelector方法来创建选择器。选择器保持打开状态，直到通过关闭方法关闭为止。
选择键对象表示具有选择器的可选择通道的注册。选择器维护三组选择键:
密钥集包含表示此选择器的当前通道注册的密钥。这个集合由keys方法返回。
选择键集是一组键，这样每一个键的通道都被检测到，以准备至少一个在优先选择操作过程中确定的键值的操作。这个集合由selectedKeys方法返回。选择键集始终是键集的子集。
取消键集是已取消但尚未取消注册通道的键集。这个集合不能直接访问。取消键集始终是键集的子集。
在新创建的选择器中，所有三个集合都为空。
键被添加到选择器的键集中，作为通过通道的寄存器方法注册通道的副作用。在选择操作期间，取消的键将从键集中删除。密钥集本身不是直接可修改的。
当它被取消时，一个键被添加到它的选择器的取消键集中，无论是通过关闭它的通道还是通过调用它的取消方法。取消一个键将使它的通道在下一次选择操作时被撤销，此时键将从所有选择器的键集中移除。
通过选择操作将键添加到选择键集。可以通过调用集合的删除方法或通过调用从集合中获得的迭代器的删除方法，直接从选择键集中删除密钥。从不以任何其他方式从选择键集中删除密钥;特别地，它们不是作为选择操作的副作用而被移除的。不能将键直接添加到选择键集。

选择
在每次选择操作期间，可以向选择器的选择键集添加和删除键，也可以从选择键集和取消键集中删除键。选择由select()、select(long)和selectNow()方法执行，包括三个步骤:
在cancelled-key集中，每个密钥集中的每个密钥集都被删除，每个密钥集中都是成员，它的通道被取消注册。此步骤将使已取消的密钥集为空。
查询底层操作系统，以便更新每个剩余通道在选择操作开始时执行其键的兴趣集所标识的任何操作。对于已准备好至少进行一次此类操作的通道，将执行以下两个操作之一:
如果通道的键不在选择键集中，那么它将被添加到该集合中，并且它的直接操作集将被修改以确定通道现在报告为准备的操作。任何先前记录在准备集中的准备信息都将被丢弃。
否则，通道的键已经在selectedkey集中，因此已经修改了它的已操作集，以确定该通道已准备就绪的任何新操作。预先记录在准备集中的任何准备就绪信息将被保存;换句话说，底层系统返回的准备集是位断开连接到键的当前准备集的。
如果这个步骤开始时键集中的所有键都有空的兴趣集，那么选择键集和任何键的准备操作集都不会被更新。
如果在步骤(2)进行时将任何键添加到已取消的键集，则按照步骤(1)处理它们。
选择操作块是否等待一个或多个通道准备就绪，如果需要等待多长时间，则是这三种选择方法之间唯一的本质区别。

并发性
选择器本身对于多个并发线程来说是安全的;然而，它们的关键集却不是。
选择操作在选择器本身、密钥集和选择密钥集上同步。它们也同步在步骤(1)和(3)上的取消键集中。
在进行选择操作时对选择器键的兴趣集所做的更改对该操作没有影响;它们将在下一个选择操作中被看到。
钥匙可以被取消，通道可以随时关闭。因此，在选择器的一个或多个键集中出现键并不意味着键是有效的，或者它的通道是打开的。如果有其他线程可能取消密钥或关闭通道，应用程序代码应该小心地同步和检查这些条件。
在select()或select(long)方法中阻塞的线程可能被其他线程以以下三种方式之一中断:
通过调用选择器的唤醒方法，
通过调用选择器的close方法，或
通过调用阻塞线程的中断方法，在这种情况下，将设置其中断状态，并调用选择器的唤醒方法。
close方法以与选择操作相同的顺序同步选择器和所有三个键集。
一般来说，选择器的键和选择键集对于多个并发线程来说并不安全。如果这样的线程可以直接修改其中的一个集合，那么应该通过对集合本身进行同步来控制访问。这些集合的迭代器的迭代器方法返回的迭代器是快速失败的:如果在创建迭代器之后，以任何方式修改集合，除了通过调用迭代器自己的删除方法，然后调用java.util。就会抛出ConcurrentModificationException。
 */

public abstract class Selector implements Closeable {

    /**
     * Initializes a new instance of this class.
     */
    protected Selector() { }

    /**
     * Opens a selector.
     *
     * <p> The new selector is created by invoking the {@link
     * java.nio.channels.spi.SelectorProvider#openSelector openSelector} method
     * of the system-wide default {@link
     * java.nio.channels.spi.SelectorProvider} object.  </p>
     *
     * @return  A new selector
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个选择器。
    通过调用系统范围内默认SelectorProvider对象的openSelector方法创建新的选择器。
     */
    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    /**
     * Tells whether or not this selector is open.告诉选择器是否打开。
     *
     * @return <tt>true</tt> if, and only if, this selector is open
     */
    public abstract boolean isOpen();

    /**
     * Returns the provider that created this channel.返回创建该通道的提供者。
     *
     * @return  The provider that created this channel
     */
    public abstract SelectorProvider provider();

    /**
     * Returns this selector's key set.
     *
     * <p> The key set is not directly modifiable.  A key is removed only after
     * it has been cancelled and its channel has been deregistered.  Any
     * attempt to modify the key set will cause an {@link
     * UnsupportedOperationException} to be thrown.
     *
     * <p> The key set is <a href="#ksc">not thread-safe</a>. </p>
     *
     * @return  This selector's key set
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     *          返回选择器的键集。
    密钥集不能直接修改。一个密钥只有在它被取消和它的通道被取消注册后才被删除。任何修改密钥集的尝试都将导致抛出UnsupportedOperationException。
     */
    public abstract Set<SelectionKey> keys();

    /**
     * Returns this selector's selected-key set.
     *
     * <p> Keys may be removed from, but not directly added to, the
     * selected-key set.  Any attempt to add an object to the key set will
     * cause an {@link UnsupportedOperationException} to be thrown.
     *
     * <p> The selected-key set is <a href="#ksc">not thread-safe</a>. </p>
     *
     * @return  This selector's selected-key set
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     *          返回选择器的选择键集。
    可以从选择键集中删除键，但不能直接添加到选择键集中。任何向键集中添加对象的尝试都将导致抛出UnsupportedOperationException。
    选择键集不是线程安全的。
     */
    public abstract Set<SelectionKey> selectedKeys();

    /**
     * Selects a set of keys whose corresponding channels are ready for I/O
     * operations.
     *
     * <p> This method performs a non-blocking <a href="#selop">selection
     * operation</a>.  If no channels have become selectable since the previous
     * selection operation then this method immediately returns zero.
     *
     * <p> Invoking this method clears the effect of any previous invocations
     * of the {@link #wakeup wakeup} method.  </p>
     *
     * @return  The number of keys, possibly zero, whose ready-operation sets
     *          were updated by the selection operation
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     *          选择一组键，其对应的通道已准备好进行I/O操作。
    此方法执行非阻塞选择操作。如果自先前的选择操作以来没有通道可选，则此方法立即返回零。
    调用此方法将清除以前调用唤醒方法的任何调用的效果。
     */
    public abstract int selectNow() throws IOException;

    /**
     * Selects a set of keys whose corresponding channels are ready for I/O
     * operations.
     *
     * <p> This method performs a blocking <a href="#selop">selection
     * operation</a>.  It returns only after at least one channel is selected,
     * this selector's {@link #wakeup wakeup} method is invoked, the current
     * thread is interrupted, or the given timeout period expires, whichever
     * comes first.
     *
     * <p> This method does not offer real-time guarantees: It schedules the
     * timeout as if by invoking the {@link Object#wait(long)} method. </p>
     *
     * @param  timeout  If positive, block for up to <tt>timeout</tt>
     *                  milliseconds, more or less, while waiting for a
     *                  channel to become ready; if zero, block indefinitely;
     *                  must not be negative
     *
     * @return  The number of keys, possibly zero,
     *          whose ready-operation sets were updated
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     *
     * @throws  IllegalArgumentException
     *          If the value of the timeout argument is negative
     *          选择一组键，其对应的通道已准备好进行I/O操作。
    该方法执行一个阻塞选择操作。它仅在至少选择一个通道、调用此选择器的唤醒方法、中断当前线程或给定的超时周期过期之后返回，无论哪个出现在前面。
    这个方法不提供实时保证:它像调用Object.wait(long)方法一样调度超时。
     */
    public abstract int select(long timeout)
        throws IOException;

    /**
     * Selects a set of keys whose corresponding channels are ready for I/O
     * operations.
     *
     * <p> This method performs a blocking <a href="#selop">selection
     * operation</a>.  It returns only after at least one channel is selected,
     * this selector's {@link #wakeup wakeup} method is invoked, or the current
     * thread is interrupted, whichever comes first.  </p>
     *
     * @return  The number of keys, possibly zero,
     *          whose ready-operation sets were updated
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     *          选择一组键，其对应的通道已准备好进行I/O操作。
    该方法执行一个阻塞选择操作。它仅在至少选择一个通道、调用此选择器的唤醒方法或中断当前线程之后返回，无论哪个通道先出现。
     */
    public abstract int select() throws IOException;

    /**
     * Causes the first selection operation that has not yet returned to return
     * immediately.
     *
     * <p> If another thread is currently blocked in an invocation of the
     * {@link #select()} or {@link #select(long)} methods then that invocation
     * will return immediately.  If no selection operation is currently in
     * progress then the next invocation of one of these methods will return
     * immediately unless the {@link #selectNow()} method is invoked in the
     * meantime.  In any case the value returned by that invocation may be
     * non-zero.  Subsequent invocations of the {@link #select()} or {@link
     * #select(long)} methods will block as usual unless this method is invoked
     * again in the meantime.
     *
     * <p> Invoking this method more than once between two successive selection
     * operations has the same effect as invoking it just once.  </p>
     *
     * @return  This selector
     * 使尚未返回的第一个选择操作立即返回。
    如果在select()或select(long)方法的调用中，另一个线程当前被阻塞，那么该调用将立即返回。如果当前没有进行选择操作，那么其中一个方法的下一次调用将立即返回，除非同时调用selectNow()方法。无论如何，该调用返回的值可能是非零的。select()或select(long)方法的后续调用将像往常一样阻塞，除非同时再次调用此方法。
    在两次连续选择操作之间多次调用此方法，其效果与调用一次相同。
     */
    public abstract Selector wakeup();

    /**
     * Closes this selector.
     *
     * <p> If a thread is currently blocked in one of this selector's selection
     * methods then it is interrupted as if by invoking the selector's {@link
     * #wakeup wakeup} method.
     *
     * <p> Any uncancelled keys still associated with this selector are
     * invalidated, their channels are deregistered, and any other resources
     * associated with this selector are released.
     *
     * <p> If this selector is already closed then invoking this method has no
     * effect.
     *
     * <p> After a selector is closed, any further attempt to use it, except by
     * invoking this method or the {@link #wakeup wakeup} method, will cause a
     * {@link ClosedSelectorException} to be thrown. </p>
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          关闭这个选择器。
    如果一个线程当前被这个选择器的一个选择方法阻塞，那么它就会被中断，就像调用选择器的唤醒方法一样。
    与此选择器关联的任何未取消键都将失效，它们的通道将被取消注册，与此选择器关联的任何其他资源将被释放。
    如果这个选择器已经关闭，那么调用这个方法没有任何效果。
    关闭选择器后，任何进一步尝试使用它(除了调用此方法或唤醒方法)都将导致抛出ClosedSelectorException。
     */
    public abstract void close() throws IOException;

}
