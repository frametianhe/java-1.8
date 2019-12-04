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

/*
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.nio.ch.Interruptible;


/**
 * Base implementation class for interruptible channels.
 *
 * <p> This class encapsulates the low-level machinery required to implement
 * the asynchronous closing and interruption of channels.  A concrete channel
 * class must invoke the {@link #begin begin} and {@link #end end} methods
 * before and after, respectively, invoking an I/O operation that might block
 * indefinitely.  In order to ensure that the {@link #end end} method is always
 * invoked, these methods should be used within a
 * <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> block:
 *
 * <blockquote><pre>
 * boolean completed = false;
 * try {
 *     begin();
 *     completed = ...;    // Perform blocking I/O operation
 *     return ...;         // Return result
 * } finally {
 *     end(completed);
 * }</pre></blockquote>
 *
 * <p> The <tt>completed</tt> argument to the {@link #end end} method tells
 * whether or not the I/O operation actually completed, that is, whether it had
 * any effect that would be visible to the invoker.  In the case of an
 * operation that reads bytes, for example, this argument should be
 * <tt>true</tt> if, and only if, some bytes were actually transferred into the
 * invoker's target buffer.
 *
 * <p> A concrete channel class must also implement the {@link
 * #implCloseChannel implCloseChannel} method in such a way that if it is
 * invoked while another thread is blocked in a native I/O operation upon the
 * channel then that operation will immediately return, either by throwing an
 * exception or by returning normally.  If a thread is interrupted or the
 * channel upon which it is blocked is asynchronously closed then the channel's
 * {@link #end end} method will throw the appropriate exception.
 *
 * <p> This class performs the synchronization required to implement the {@link
 * java.nio.channels.Channel} specification.  Implementations of the {@link
 * #implCloseChannel implCloseChannel} method need not synchronize against
 * other threads that might be attempting to close the channel.  </p>
 * 可中断通道的基本实现类。
 这个类封装了实现通道异步关闭和中断所需的底层机制。具体的通道类必须分别调用开始和结束方法，分别调用可能阻塞的I/O操作。为了确保始终调用end方法，应该在try中使用这些方法。finally块:
 结束方法的完成参数告诉I/O操作是否实际完成，也就是说，它是否具有调用者可以看到的任何效果。例如，对于读取字节的操作，这个参数应该是正确的，只有当，一些字节实际上被传输到调用者的目标缓冲区。
 具体的通道类还必须实现implCloseChannel方法，其方式是，如果在通道上的本机I/O操作中阻塞另一个线程时调用它，那么该操作将通过抛出异常或正常返回立即返回。如果线程被中断，或者被阻塞的通道被异步关闭，那么通道的end方法将抛出适当的异常。
 该类执行实现通道规范所需的同步。implCloseChannel方法的实现不需要与试图关闭通道的其他线程进行同步。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
//
public abstract class AbstractInterruptibleChannel
    implements Channel, InterruptibleChannel
{

    private final Object closeLock = new Object();
    private volatile boolean open = true;

    /**
     * Initializes a new instance of this class.
     */
    protected AbstractInterruptibleChannel() { }

    /**
     * Closes this channel.
     *
     * <p> If the channel has already been closed then this method returns
     * immediately.  Otherwise it marks the channel as closed and then invokes
     * the {@link #implCloseChannel implCloseChannel} method in order to
     * complete the close operation.  </p>
     * 关闭这个通道。
     如果通道已经关闭，则该方法立即返回。否则，它将通道标记为关闭，然后调用implCloseChannel方法，以便完成关闭操作。
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public final void close() throws IOException {
        synchronized (closeLock) {
            if (!open)
                return;
            open = false;
            implCloseChannel();
        }
    }

    /**
     * Closes this channel.
     *
     * <p> This method is invoked by the {@link #close close} method in order
     * to perform the actual work of closing the channel.  This method is only
     * invoked if the channel has not yet been closed, and it is never invoked
     * more than once.
     *
     * <p> An implementation of this method must arrange for any other thread
     * that is blocked in an I/O operation upon this channel to return
     * immediately, either by throwing an exception or by returning normally.
     * </p>
     * 关闭这个通道。
     这个方法由close方法调用，以执行关闭通道的实际工作。此方法仅在通道尚未关闭时才被调用，而且它不会被多次调用。
     此方法的实现必须安排在此通道上的I/O操作中阻塞的任何其他线程立即返回，要么抛出异常，要么返回正常。
     *
     * @throws  IOException
     *          If an I/O error occurs while closing the channel
     */
    protected abstract void implCloseChannel() throws IOException;

    public final boolean isOpen() {
        return open;
    }


    // -- Interruption machinery --

    private Interruptible interruptor;
    private volatile Thread interrupted;

    /**
     * Marks the beginning of an I/O operation that might block indefinitely.
     *
     * <p> This method should be invoked in tandem with the {@link #end end}
     * method, using a <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> block as
     * shown <a href="#be">above</a>, in order to implement asynchronous
     * closing and interruption for this channel.  </p>
     * 标记可能无限期阻塞的I/O操作的开始。
     这个方法应该与end方法一起调用，使用try…最后如上图所示，为了实现该通道的异步关闭和中断。
     */
    protected final void begin() {
        if (interruptor == null) {
            interruptor = new Interruptible() {
                    public void interrupt(Thread target) {
                        synchronized (closeLock) {
                            if (!open)
                                return;
                            open = false;
                            interrupted = target;
                            try {
                                AbstractInterruptibleChannel.this.implCloseChannel();
                            } catch (IOException x) { }
                        }
                    }};
        }
        blockedOn(interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted())
            interruptor.interrupt(me);
    }

    /**
     * Marks the end of an I/O operation that might block indefinitely.
     *
     * <p> This method should be invoked in tandem with the {@link #begin
     * begin} method, using a <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> block
     * as shown <a href="#be">above</a>, in order to implement asynchronous
     * closing and interruption for this channel.  </p>
     * 标记可能无限期阻塞的I/O操作的结束。
     这个方法应该与begin方法一起调用，使用try…最后如上图所示，为了实现该通道的异步关闭和中断。
     *
     * @param  completed
     *         <tt>true</tt> if, and only if, the I/O operation completed
     *         successfully, that is, had some effect that would be visible to
     *         the operation's invoker
     *
     * @throws  AsynchronousCloseException
     *          If the channel was asynchronously closed
     *
     * @throws  ClosedByInterruptException
     *          If the thread blocked in the I/O operation was interrupted
     */
    protected final void end(boolean completed)
        throws AsynchronousCloseException
    {
        blockedOn(null);
        Thread interrupted = this.interrupted;
        if (interrupted != null && interrupted == Thread.currentThread()) {
            interrupted = null;
            throw new ClosedByInterruptException();
        }
        if (!completed && !open)
            throw new AsynchronousCloseException();
    }


    // -- sun.misc.SharedSecrets --
    static void blockedOn(Interruptible intr) {         // package-private
        sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(),
                                                             intr);
    }
}
