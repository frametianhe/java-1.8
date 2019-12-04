/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.IOException;


/**
 * A channel that can be asynchronously closed and interrupted.
 *
 * <p> A channel that implements this interface is <i>asynchronously
 * closeable:</i> If a thread is blocked in an I/O operation on an
 * interruptible channel then another thread may invoke the channel's {@link
 * #close close} method.  This will cause the blocked thread to receive an
 * {@link AsynchronousCloseException}.
 *
 * <p> A channel that implements this interface is also <i>interruptible:</i>
 * If a thread is blocked in an I/O operation on an interruptible channel then
 * another thread may invoke the blocked thread's {@link Thread#interrupt()
 * interrupt} method.  This will cause the channel to be closed, the blocked
 * thread to receive a {@link ClosedByInterruptException}, and the blocked
 * thread's interrupt status to be set.
 *
 * <p> If a thread's interrupt status is already set and it invokes a blocking
 * I/O operation upon a channel then the channel will be closed and the thread
 * will immediately receive a {@link ClosedByInterruptException}; its interrupt
 * status will remain set.
 *
 * <p> A channel supports asynchronous closing and interruption if, and only
 * if, it implements this interface.  This can be tested at runtime, if
 * necessary, via the <tt>instanceof</tt> operator.
 * 可以异步关闭和中断的通道。
 实现该接口的通道是异步关闭的:如果一个线程在可中断通道的I/O操作中被阻塞，那么另一个线程可能调用该通道的close方法。这将导致阻塞的线程接收异步scloseexception。
 实现此接口的通道也是可中断的:如果一个线程在可中断通道的I/O操作中被阻塞，那么另一个线程可能调用阻塞线程的中断方法。这将导致通道关闭，阻塞的线程接收ClosedByInterruptException，并设置阻塞的线程的中断状态。
 如果一个线程的中断状态已经被设置，并且它调用了一个通道上的阻塞I/O操作，那么该通道将被关闭，该线程将立即收到一个ClosedByInterruptException;它的中断状态将保持不变。
 通道支持异步关闭和中断，当且仅当它实现此接口时。如果需要，可以通过instanceof操作符在运行时进行测试。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
//
public interface InterruptibleChannel
    extends Channel
{

    /**
     * Closes this channel.
     *
     * <p> Any thread currently blocked in an I/O operation upon this channel
     * will receive an {@link AsynchronousCloseException}.
     *
     * <p> This method otherwise behaves exactly as specified by the {@link
     * Channel#close Channel} interface.  </p>
     * 关闭这个通道。
     在此通道上的I/O操作中阻塞的任何线程都将接收异步scloseexception。
     此方法的行为与通道接口指定的完全相同。
     *
     * @throws  IOException  If an I/O error occurs
     */
    public void close() throws IOException;

}
