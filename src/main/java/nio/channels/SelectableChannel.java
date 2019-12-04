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

import java.io.IOException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;


/**
 * A channel that can be multiplexed via a {@link Selector}.
 *
 * <p> In order to be used with a selector, an instance of this class must
 * first be <i>registered</i> via the {@link #register(Selector,int,Object)
 * register} method.  This method returns a new {@link SelectionKey} object
 * that represents the channel's registration with the selector.
 *
 * <p> Once registered with a selector, a channel remains registered until it
 * is <i>deregistered</i>.  This involves deallocating whatever resources were
 * allocated to the channel by the selector.
 *
 * <p> A channel cannot be deregistered directly; instead, the key representing
 * its registration must be <i>cancelled</i>.  Cancelling a key requests that
 * the channel be deregistered during the selector's next selection operation.
 * A key may be cancelled explicitly by invoking its {@link
 * SelectionKey#cancel() cancel} method.  All of a channel's keys are cancelled
 * implicitly when the channel is closed, whether by invoking its {@link
 * Channel#close close} method or by interrupting a thread blocked in an I/O
 * operation upon the channel.
 *
 * <p> If the selector itself is closed then the channel will be deregistered,
 * and the key representing its registration will be invalidated, without
 * further delay.
 *
 * <p> A channel may be registered at most once with any particular selector.
 *
 * <p> Whether or not a channel is registered with one or more selectors may be
 * determined by invoking the {@link #isRegistered isRegistered} method.
 *
 * <p> Selectable channels are safe for use by multiple concurrent
 * threads. </p>
 *
 *
 * <a name="bm"></a>
 * <h2>Blocking mode</h2>
 *
 * A selectable channel is either in <i>blocking</i> mode or in
 * <i>non-blocking</i> mode.  In blocking mode, every I/O operation invoked
 * upon the channel will block until it completes.  In non-blocking mode an I/O
 * operation will never block and may transfer fewer bytes than were requested
 * or possibly no bytes at all.  The blocking mode of a selectable channel may
 * be determined by invoking its {@link #isBlocking isBlocking} method.
 *
 * <p> Newly-created selectable channels are always in blocking mode.
 * Non-blocking mode is most useful in conjunction with selector-based
 * multiplexing.  A channel must be placed into non-blocking mode before being
 * registered with a selector, and may not be returned to blocking mode until
 * it has been deregistered.
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see SelectionKey
 * @see Selector
 * 可以通过选择器进行多路复用的通道。
为了与选择器一起使用，必须首先通过register方法注册该类的实例。此方法返回一个新的SelectionKey对象，该对象用选择器表示通道的注册。
一旦注册了一个选择器，一个通道将被注册，直到它被撤销注册。这涉及到释放选择器分配给通道的任何资源。
渠道不能直接撤销注册;相反，代表其注册的密钥必须被取消。取消在选择器的下一个选择操作期间取消对通道的请求。可以通过调用其cancel方法显式地取消密钥。当通道关闭时，无论是通过调用它的关闭方法，还是通过中断通道上I/O操作中阻塞的线程，都会隐式地取消通道的所有键。
如果选择器本身被关闭，那么通道将被取消注册，代表它的注册的键将被无效，不再延迟。
通道可以在任何特定的选择器中最多注册一次。
是否向一个或多个选择器注册通道可以通过调用isregister方法来确定。
可选择通道对于多个并发线程来说是安全的。
阻塞模式
可选择的通道要么处于阻塞模式，要么处于非阻塞模式。在阻塞模式下，在通道上调用的每个I/O操作都将被阻塞，直到它完成。在非阻塞模式下，I/O操作永远不会阻塞，可能传输的字节比请求的少，或者可能根本没有字节。可选择通道的阻塞模式可以通过调用其isBlocking方法来确定。
新创建的可选择通道总是处于阻塞模式。非阻塞模式与基于选择器的多路复用结合在一起是最有用的。在向选择器注册之前，必须将通道放置到非阻塞模式，并且在取消注册之前不能返回到阻塞模式。
 */
//可以通过选择器进行多路复用的通道，须首先通过register方法注册该类的实例。此方法返回一个新的SelectionKey对象，取消在选择器的下一个选择操作期间取消对通道的请求。
// 可以通过调用其cancel方法显式地取消，当通道关闭时都会隐式地取消通道的所有键，线程安全，非阻塞模式与基于选择器的多路复用结合在一起是最有用的。在向选择器注册之前，必须将通道放置到非阻塞模式，并且在取消注册之前不能返回到阻塞模式
public abstract class SelectableChannel
    extends AbstractInterruptibleChannel
    implements Channel
{

    /**
     * Initializes a new instance of this class.
     */
    protected SelectableChannel() { }

    /**
     * Returns the provider that created this channel.返回创建此通道的提供者。
     *
     * @return  The provider that created this channel
     */
    public abstract SelectorProvider provider();

    /**
     * Returns an <a href="SelectionKey.html#opsets">operation set</a>
     * identifying this channel's supported operations.  The bits that are set
     * in this integer value denote exactly the operations that are valid for
     * this channel.  This method always returns the same value for a given
     * concrete channel class.返回标识该通道支持的操作的操作集。在这个整数值中设置的位表示对这个通道有效的操作。对于给定的具体通道类，此方法总是返回相同的值。
     *
     * @return  The valid-operation set
     */
    public abstract int validOps();

    // Internal state:内部状态:
    //   keySet, may be empty but is never null, typ. a tiny array键集，可能是空的，但从不为空，typ。一个微小的数组
    //   boolean isRegistered, protected by key set布尔值被注册，由密钥集保护
    //   regLock, lock object to prevent duplicate registrations regLock，锁定对象，以防止重复注册
    //   boolean isBlocking, protected by regLock 布尔值被regLock保护

    /**
     * Tells whether or not this channel is currently registered with any
     * selectors.  A newly-created channel is not registered.
     *
     * <p> Due to the inherent delay between key cancellation and channel
     * deregistration, a channel may remain registered for some time after all
     * of its keys have been cancelled.  A channel may also remain registered
     * for some time after it is closed.  </p>
     *
     * @return <tt>true</tt> if, and only if, this channel is registered
     * 告诉该通道当前是否已注册到任何选择器。未注册新创建的通道。
    由于密钥取消和通道取消注册之间固有的延迟，在所有的密钥被取消之后，通道可能会继续注册一段时间。通道也可以在关闭后继续注册一段时间。
     */
//    告诉该通道当前是否已注册到任何选择器。未注册新创建的通道。
    public abstract boolean isRegistered();
    //
    // sync(keySet) { return isRegistered; }

    /**
     * Retrieves the key representing the channel's registration with the given
     * selector.使用给定的选择器检索表示通道注册的键。
     *
     * @param   sel
     *          The selector
     *
     * @return  The key returned when this channel was last registered with the
     *          given selector, or <tt>null</tt> if this channel is not
     *          currently registered with that selector
     */
    public abstract SelectionKey keyFor(Selector sel);
    //
    // sync(keySet) { return findKey(sel); }

    /**
     * Registers this channel with the given selector, returning a selection
     * key.
     *
     * <p> If this channel is currently registered with the given selector then
     * the selection key representing that registration is returned.  The key's
     * interest set will have been changed to <tt>ops</tt>, as if by invoking
     * the {@link SelectionKey#interestOps(int) interestOps(int)} method.  If
     * the <tt>att</tt> argument is not <tt>null</tt> then the key's attachment
     * will have been set to that value.  A {@link CancelledKeyException} will
     * be thrown if the key has already been cancelled.
     *
     * <p> Otherwise this channel has not yet been registered with the given
     * selector, so it is registered and the resulting new key is returned.
     * The key's initial interest set will be <tt>ops</tt> and its attachment
     * will be <tt>att</tt>.
     *
     * <p> This method may be invoked at any time.  If this method is invoked
     * while another invocation of this method or of the {@link
     * #configureBlocking(boolean) configureBlocking} method is in progress
     * then it will first block until the other operation is complete.  This
     * method will then synchronize on the selector's key set and therefore may
     * block if invoked concurrently with another registration or selection
     * operation involving the same selector. </p>
     *
     * <p> If this channel is closed while this operation is in progress then
     * the key returned by this method will have been cancelled and will
     * therefore be invalid. </p>
     *
     * @param  sel
     *         The selector with which this channel is to be registered
     *
     * @param  ops
     *         The interest set for the resulting key
     *
     * @param  att
     *         The attachment for the resulting key; may be <tt>null</tt>
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  ClosedSelectorException
     *          If the selector is closed
     *
     * @throws  IllegalBlockingModeException
     *          If this channel is in blocking mode
     *
     * @throws  IllegalSelectorException
     *          If this channel was not created by the same provider
     *          as the given selector
     *
     * @throws  CancelledKeyException
     *          If this channel is currently registered with the given selector
     *          but the corresponding key has already been cancelled
     *
     * @throws  IllegalArgumentException
     *          If a bit in the <tt>ops</tt> set does not correspond to an
     *          operation that is supported by this channel, that is, if
     *          {@code set & ~validOps() != 0}
     *
     * @return  A key representing the registration of this channel with
     *          the given selector
     *          用给定的选择器注册这个通道，返回一个选择键。
    如果该通道当前已向给定的选择器注册，则返回表示该注册的选择键。键的兴趣集将被更改为ops，就像调用了interestOps(int)方法一样。如果att参数不是null，那么键的附件将被设置为该值。如果密钥已经被取消，则将抛出CancelledKeyException。
    否则，这个通道还没有在给定的选择器中注册，所以它是注册的，结果返回的新密钥。钥匙的初始兴趣集将是操作，它的附件将是att。
    此方法可以随时调用。如果在此方法或configure reblock方法的另一个调用正在进行时调用此方法，那么它将首先阻塞，直到其他操作完成。然后，此方法将同步选择器的键集，因此如果与涉及相同选择器的另一个注册或选择操作同时调用，则可能会阻塞。
    如果该通道在此操作进行期间被关闭，那么该方法返回的键将被取消，因此将无效。
     */
//    用给定的选择器注册这个通道，返回一个选择键
    public abstract SelectionKey register(Selector sel, int ops, Object att)
        throws ClosedChannelException;
    //
    // sync(regLock) {
    //   sync(keySet) { look for selector }
    //   if (channel found) { set interest ops -- may block in selector;
    //                        return key; }
    //   create new key -- may block somewhere in selector;
    //   sync(keySet) { add key; }
    //   attach(attachment);
    //   return key;
    // }

    /**
     * Registers this channel with the given selector, returning a selection
     * key.
     *
     * <p> An invocation of this convenience method of the form
     *
     * <blockquote><tt>sc.register(sel, ops)</tt></blockquote>
     *
     * behaves in exactly the same way as the invocation
     *
     * <blockquote><tt>sc.{@link
     * #register(java.nio.channels.Selector,int,java.lang.Object)
     * register}(sel, ops, null)</tt></blockquote>
     *
     * @param  sel
     *         The selector with which this channel is to be registered
     *
     * @param  ops
     *         The interest set for the resulting key
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  ClosedSelectorException
     *          If the selector is closed
     *
     * @throws  IllegalBlockingModeException
     *          If this channel is in blocking mode
     *
     * @throws  IllegalSelectorException
     *          If this channel was not created by the same provider
     *          as the given selector
     *
     * @throws  CancelledKeyException
     *          If this channel is currently registered with the given selector
     *          but the corresponding key has already been cancelled
     *
     * @throws  IllegalArgumentException
     *          If a bit in <tt>ops</tt> does not correspond to an operation
     *          that is supported by this channel, that is, if {@code set &
     *          ~validOps() != 0}
     *
     * @return  A key representing the registration of this channel with
     *          the given selector
     *          用给定的选择器注册这个通道，返回一个选择键。
    对窗体的这种方便方法的调用
    sc.register(选取、运维)
    行为与调用完全相同
    sc.register(选取、ops、空)
     */
    public final SelectionKey register(Selector sel, int ops)
        throws ClosedChannelException
    {
        return register(sel, ops, null);
    }

    /**
     * Adjusts this channel's blocking mode.
     *
     * <p> If this channel is registered with one or more selectors then an
     * attempt to place it into blocking mode will cause an {@link
     * IllegalBlockingModeException} to be thrown.
     *
     * <p> This method may be invoked at any time.  The new blocking mode will
     * only affect I/O operations that are initiated after this method returns.
     * For some implementations this may require blocking until all pending I/O
     * operations are complete.
     *
     * <p> If this method is invoked while another invocation of this method or
     * of the {@link #register(Selector, int) register} method is in progress
     * then it will first block until the other operation is complete. </p>
     *
     * @param  block  If <tt>true</tt> then this channel will be placed in
     *                blocking mode; if <tt>false</tt> then it will be placed
     *                non-blocking mode
     *
     * @return  This selectable channel
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  IllegalBlockingModeException
     *          If <tt>block</tt> is <tt>true</tt> and this channel is
     *          registered with one or more selectors
     *
     * @throws IOException
     *         If an I/O error occurs
     *         调整通道的阻塞模式。
    如果该通道在一个或多个选择器中注册，则试图将其放入阻塞模式，将导致抛出IllegalBlockingModeException异常。
    此方法可以随时调用。新的阻塞模式只会影响在此方法返回后启动的I/O操作。对于某些实现，这可能需要阻塞，直到所有挂起的I/O操作完成。
    如果在此方法或寄存器方法的另一个调用进行时调用此方法，那么它将首先阻塞，直到完成另一个操作。
     */
//    调整通道的阻塞模式，新的阻塞模式只会影响在此方法返回后启动的I/O操作，新的阻塞模式只会影响在此方法返回后启动的I/O操作。对于某些实现，这可能需要阻塞，直到所有挂起的I/O操作完成
    public abstract SelectableChannel configureBlocking(boolean block)
        throws IOException;
    //
    // sync(regLock) {
    //   sync(keySet) { throw IBME if block && isRegistered; }
    //   change mode;
    // }

    /**
     * Tells whether or not every I/O operation on this channel will block
     * until it completes.  A newly-created channel is always in blocking mode.
     *
     * <p> If this channel is closed then the value returned by this method is
     * not specified. </p>
     *
     * @return <tt>true</tt> if, and only if, this channel is in blocking mode
     * 告诉该通道上的每个I/O操作是否会阻塞，直到它完成为止。新创建的通道总是处于阻塞模式。
    如果关闭该通道，则不指定此方法返回的值。
     */
    public abstract boolean isBlocking();

    /**
     * Retrieves the object upon which the {@link #configureBlocking
     * configureBlocking} and {@link #register register} methods synchronize.
     * This is often useful in the implementation of adaptors that require a
     * specific blocking mode to be maintained for a short period of time.
     *
     * @return  The blocking-mode lock object
     * 检索配置重新阻塞和注册方法同步的对象。这对于需要在短时间内维护特定阻塞模式的适配器实现通常是有用的。
     */
    public abstract Object blockingLock();

}
