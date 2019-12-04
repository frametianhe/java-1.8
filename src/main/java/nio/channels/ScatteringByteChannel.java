/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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
import java.nio.ByteBuffer;


/**
 * A channel that can read bytes into a sequence of buffers.
 *
 * <p> A <i>scattering</i> read operation reads, in a single invocation, a
 * sequence of bytes into one or more of a given sequence of buffers.
 * Scattering reads are often useful when implementing network protocols or
 * file formats that, for example, group data into segments consisting of one
 * or more fixed-length headers followed by a variable-length body.  Similar
 * <i>gathering</i> write operations are defined in the {@link
 * GatheringByteChannel} interface.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 一种可以将字节读入缓冲区序列的通道。
在一次调用中，一个分散的读操作将一个字节序列读入一个或多个给定的缓冲区序列。在实现网络协议或文件格式(例如，将数据分组为由一个或多个固定长度的标头和一个可变长度的主体组成的段)时，分散读通常是有用的。在collect bytechannel接口中定义了类似的收集写操作。
 */
//一个分散的读操作将一个字节序列读入一个或多个给定的缓冲区序列
public interface ScatteringByteChannel
    extends ReadableByteChannel
{

    /**
     * Reads a sequence of bytes from this channel into a subsequence of the
     * given buffers.
     *
     * <p> An invocation of this method attempts to read up to <i>r</i> bytes
     * from this channel, where <i>r</i> is the total number of bytes remaining
     * the specified subsequence of the given buffer array, that is,
     *
     * <blockquote><pre>
     * dsts[offset].remaining()
     *     + dsts[offset+1].remaining()
     *     + ... + dsts[offset+length-1].remaining()</pre></blockquote>
     *
     * at the moment that this method is invoked.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is read, where
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * Up to the first <tt>dsts[offset].remaining()</tt> bytes of this sequence
     * are transferred into buffer <tt>dsts[offset]</tt>, up to the next
     * <tt>dsts[offset+1].remaining()</tt> bytes are transferred into buffer
     * <tt>dsts[offset+1]</tt>, and so forth, until the entire byte sequence
     * is transferred into the given buffers.  As many bytes as possible are
     * transferred into each buffer, hence the final position of each updated
     * buffer, except the last updated buffer, is guaranteed to be equal to
     * that buffer's limit.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a read operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. </p>
     *
     * @param  dsts
     *         The buffers into which bytes are to be transferred
     *
     * @param  offset
     *         The offset within the buffer array of the first buffer into
     *         which bytes are to be transferred; must be non-negative and no
     *         larger than <tt>dsts.length</tt>
     *
     * @param  length
     *         The maximum number of buffers to be accessed; must be
     *         non-negative and no larger than
     *         <tt>dsts.length</tt>&nbsp;-&nbsp;<tt>offset</tt>
     *
     * @return The number of bytes read, possibly zero,
     *         or <tt>-1</tt> if the channel has reached end-of-stream
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     *
     * @throws  NonReadableChannelException
     *          If this channel was not opened for reading
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the read operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the read operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          将该通道中的字节序列读入给定缓冲区的子序列。
    该方法的调用尝试从该通道读取最多r个字节，其中r是剩余给定缓冲数组指定子序列的字节总数，即，
    此时调用此方法。
    假设一个字节序列长度为n的阅读,在0 < = n < = r。到第一个数据(抵消).remaining()字节的顺序转移到缓冲区数据的(抵消),到下一个数据的[抵消+ 1].remaining()字节转移到缓冲区数据的偏移量+ 1,等等,直到整个字节序列转换成给定的缓冲区。尽可能多的字节被传输到每个缓冲区中，因此每个更新缓冲区的最终位置(除了最后一个更新的缓冲区之外)保证等于缓冲区的限制。
    此方法可以随时调用。如果另一个线程已经在该通道上启动了读取操作，那么该方法的调用将被阻塞，直到第一个操作完成。
     */
//    如果另一个线程已经在该通道上启动了读取操作，那么该方法的调用将被阻塞，直到第一个操作完成
    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * Reads a sequence of bytes from this channel into the given buffers.
     *
     * <p> An invocation of this method of the form <tt>c.read(dsts)</tt>
     * behaves in exactly the same manner as the invocation
     *
     * <blockquote><pre>
     * c.read(dsts, 0, dsts.length);</pre></blockquote>
     *
     * @param  dsts
     *         The buffers into which bytes are to be transferred
     *
     * @return The number of bytes read, possibly zero,
     *         or <tt>-1</tt> if the channel has reached end-of-stream
     *
     * @throws  NonReadableChannelException
     *          If this channel was not opened for reading
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the read operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the read operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          将该通道中的字节序列读入给定的缓冲区。
    form c.read(dsts)的这种方法的调用行为与调用完全相同
    c。读(数据0 dsts.length);
     */
    public long read(ByteBuffer[] dsts) throws IOException;

}
