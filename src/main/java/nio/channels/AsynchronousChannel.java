/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.Future;  // javadoc

/**
 * A channel that supports asynchronous I/O operations. Asynchronous I/O
 * operations will usually take one of two forms:
 *
 * <ol>
 * <li><pre>{@link Future}&lt;V&gt; <em>operation</em>(<em>...</em>)</pre></li>
 * <li><pre>void <em>operation</em>(<em>...</em> A attachment, {@link
 *   CompletionHandler}&lt;V,? super A&gt; handler)</pre></li>
 * </ol>
 *
 * where <i>operation</i> is the name of the I/O operation (read or write for
 * example), <i>V</i> is the result type of the I/O operation, and <i>A</i> is
 * the type of an object attached to the I/O operation to provide context when
 * consuming the result. The attachment is important for cases where a
 * <em>state-less</em> {@code CompletionHandler} is used to consume the result
 * of many I/O operations.
 *
 * <p> In the first form, the methods defined by the {@link Future Future}
 * interface may be used to check if the operation has completed, wait for its
 * completion, and to retrieve the result. In the second form, a {@link
 * CompletionHandler} is invoked to consume the result of the I/O operation when
 * it completes or fails.
 *
 * <p> A channel that implements this interface is <em>asynchronously
 * closeable</em>: If an I/O operation is outstanding on the channel and the
 * channel's {@link #close close} method is invoked, then the I/O operation
 * fails with the exception {@link AsynchronousCloseException}.
 *
 * <p> Asynchronous channels are safe for use by multiple concurrent threads.
 * Some channel implementations may support concurrent reading and writing, but
 * may not allow more than one read and one write operation to be outstanding at
 * any given time.
 *
 * <h2>Cancellation</h2>
 *
 * <p> The {@code Future} interface defines the {@link Future#cancel cancel}
 * method to cancel execution. This causes all threads waiting on the result of
 * the I/O operation to throw {@link java.util.concurrent.CancellationException}.
 * Whether the underlying I/O operation can be cancelled is highly implementation
 * specific and therefore not specified. Where cancellation leaves the channel,
 * or the entity to which it is connected, in an inconsistent state, then the
 * channel is put into an implementation specific <em>error state</em> that
 * prevents further attempts to initiate I/O operations that are <i>similar</i>
 * to the operation that was cancelled. For example, if a read operation is
 * cancelled but the implementation cannot guarantee that bytes have not been
 * read from the channel then it puts the channel into an error state; further
 * attempts to initiate a {@code read} operation cause an unspecified runtime
 * exception to be thrown. Similarly, if a write operation is cancelled but the
 * implementation cannot guarantee that bytes have not been written to the
 * channel then subsequent attempts to initiate a {@code write} will fail with
 * an unspecified runtime exception.
 *
 * <p> Where the {@link Future#cancel cancel} method is invoked with the {@code
 * mayInterruptIfRunning} parameter set to {@code true} then the I/O operation
 * may be interrupted by closing the channel. In that case all threads waiting
 * on the result of the I/O operation throw {@code CancellationException} and
 * any other I/O operations outstanding on the channel complete with the
 * exception {@link AsynchronousCloseException}.
 *
 * <p> Where the {@code cancel} method is invoked to cancel read or write
 * operations then it is recommended that all buffers used in the I/O operations
 * be discarded or care taken to ensure that the buffers are not accessed while
 * the channel remains open.
 *
 *  @since 1.7
 *  支持异步I/O操作的通道。异步I/O操作通常采用两种形式之一:
未来的< V >操作(…)
无效的操作(…一个附件,CompletionHandler < V,?超级>处理程序)
操作是I/O操作的名称(例如读或写)，V是I/O操作的结果类型，A是连接到I/O操作的对象的类型，以便在使用结果时提供上下文。对于使用状态不太复杂的处理程序来使用许多I/O操作的结果的情况，附件是很重要的。
在第一种形式中，未来接口定义的方法可以用来检查操作是否完成，等待操作完成，并检索结果。在第二种形式中，调用CompletionHandler来在I/O操作完成或失败时使用其结果。
实现该接口的通道是异步关闭的:如果通道上未执行I/O操作，并且调用了通道的close方法，那么I/O操作将失败，异常为AsynchronousCloseException。
异步通道对于多个并发线程来说是安全的。有些通道实现可能支持并发读写，但在任何给定的时间内，可能不允许超过一个读写操作。
取消
未来的接口定义了取消执行的取消方法。这将导致所有等待I/O操作结果的线程抛出java.util.concurrent. cancel异常。是否可以取消底层的I/O操作是高度特定于实现的，因此没有指定。如果取消使通道或它所连接的实体处于不一致的状态，则该通道将进入实现特定的错误状态，以防止进一步尝试发起与被取消的操作类似的I/O操作。例如，如果一个读取操作被取消，但是实现不能保证没有从通道中读取字节，那么它就会使通道处于错误状态;进一步尝试启动读操作将导致抛出未指定的运行时异常。类似地，如果取消了写操作，但是实现不能保证没有将字节写入通道，那么随后发起写操作的尝试将失败，并出现未指定的运行时异常。
如果使用将mayInterruptIfRunning参数设置为true来调用cancel方法，则可以通过关闭通道来中断I/O操作。在这种情况下，等待I/O操作结果的所有线程抛出松塞异常，以及通道上未执行的任何其他I/O操作，并带有异常AsynchronousCloseException。
如果调用cancel方法来取消读或写操作，则建议丢弃I/O操作中使用的所有缓冲区，或者注意确保在通道保持打开时不访问缓冲区。
 */

//异步通道对于多个并发线程来说是安全的
public interface AsynchronousChannel
    extends Channel
{
    /**
     * Closes this channel.
     *
     * <p> Any outstanding asynchronous operations upon this channel will
     * complete with the exception {@link AsynchronousCloseException}. After a
     * channel is closed, further attempts to initiate asynchronous I/O
     * operations complete immediately with cause {@link ClosedChannelException}.
     *
     * <p>  This method otherwise behaves exactly as specified by the {@link
     * Channel} interface.
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          关闭这个通道。
    该通道上的任何未完成的异步操作都将在异常AsynchronousCloseException下完成。通道关闭后，进一步尝试启动异步I/O操作，立即完成，导致ClosedChannelException异常。
    此方法的行为与通道接口指定的完全相同。
     */
    @Override
    void close() throws IOException;
}
