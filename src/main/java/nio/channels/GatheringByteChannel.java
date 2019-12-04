/*
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
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
 * A channel that can write bytes from a sequence of buffers.
 *
 * <p> A <i>gathering</i> write operation writes, in a single invocation, a
 * sequence of bytes from one or more of a given sequence of buffers.
 * Gathering writes are often useful when implementing network protocols or
 * file formats that, for example, group data into segments consisting of one
 * or more fixed-length headers followed by a variable-length body.  Similar
 * <i>scattering</i> read operations are defined in the {@link
 * ScatteringByteChannel} interface.  </p>
 *
 一种可以从缓冲区序列中写入字节的通道。
 集合写操作在单个调用中写入一个或多个给定的缓冲区序列的字节序列。当实现网络协议或文件格式时，收集写操作通常是有用的，例如，将数据分组为由一个或多个固定长度的标头组成的段，然后是可变长度的主体。在散射bytechannel接口中定义了类似的散射读取操作。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
//集合写操作在单个调用中写入一个或多个给定的缓冲区序列的字节序列
public interface GatheringByteChannel
    extends WritableByteChannel
{

    /**
     * Writes a sequence of bytes to this channel from a subsequence of the
     * given buffers.
     *
     * <p> An attempt is made to write up to <i>r</i> bytes to this channel,
     * where <i>r</i> is the total number of bytes remaining in the specified
     * subsequence of the given buffer array, that is,
     *
     * <blockquote><pre>
     * srcs[offset].remaining()
     *     + srcs[offset+1].remaining()
     *     + ... + srcs[offset+length-1].remaining()</pre></blockquote>
     *
     * at the moment that this method is invoked.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is written, where
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * Up to the first <tt>srcs[offset].remaining()</tt> bytes of this sequence
     * are written from buffer <tt>srcs[offset]</tt>, up to the next
     * <tt>srcs[offset+1].remaining()</tt> bytes are written from buffer
     * <tt>srcs[offset+1]</tt>, and so forth, until the entire byte sequence is
     * written.  As many bytes as possible are written from each buffer, hence
     * the final position of each updated buffer, except the last updated
     * buffer, is guaranteed to be equal to that buffer's limit.
     *
     * <p> Unless otherwise specified, a write operation will return only after
     * writing all of the <i>r</i> requested bytes.  Some types of channels,
     * depending upon their state, may write only some of the bytes or possibly
     * none at all.  A socket channel in non-blocking mode, for example, cannot
     * write any more bytes than are free in the socket's output buffer.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a write operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. </p>
     *
     * @param  srcs
     *         The buffers from which bytes are to be retrieved
     *
     * @param  offset
     *         The offset within the buffer array of the first buffer from
     *         which bytes are to be retrieved; must be non-negative and no
     *         larger than <tt>srcs.length</tt>
     *
     * @param  length
     *         The maximum number of buffers to be accessed; must be
     *         non-negative and no larger than
     *         <tt>srcs.length</tt>&nbsp;-&nbsp;<tt>offset</tt>
     *
     * @return  The number of bytes written, possibly zero
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the write operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the write operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          从给定缓冲区的子序列向该通道写入字节序列。
    尝试将最多r个字节写入这个通道，其中r是给定缓冲数组子序列中剩余的字节总数，即，
    此时调用此方法。
    假设一个字节序列长度n是写,其中0 < = n < = r。到第一个src(抵消).remaining()字节的序列从缓冲区写src(抵消),到下一个src[抵消+ 1].remaining()从缓冲区字节写src(抵消+ 1),等等,直到整个字节序列。从每个缓冲区写入尽可能多的字节，因此每个更新缓冲区的最终位置(除了最后一个更新的缓冲区之外)保证等于缓冲区的限制。
    除非另有规定，写入操作将只在写入所有请求的r字节之后返回。某些类型的通道，取决于它们的状态，可能只写一些字节，或者可能根本不写。例如，非阻塞模式下的套接字通道不能写入比套接字输出缓冲区中空闲的任何字节。
    此方法可以随时调用。如果另一个线程已经在该通道上启动了写操作，那么该方法的调用将被阻塞，直到第一个操作完成。
     */
//    如果另一个线程已经在该通道上启动了写操作，那么该方法的调用将被阻塞，直到第一个操作完成
    public long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;


    /**
     * Writes a sequence of bytes to this channel from the given buffers.
     *
     * <p> An invocation of this method of the form <tt>c.write(srcs)</tt>
     * behaves in exactly the same manner as the invocation
     *
     * <blockquote><pre>
     * c.write(srcs, 0, srcs.length);</pre></blockquote>
     *
     * @param  srcs
     *         The buffers from which bytes are to be retrieved
     *
     * @return  The number of bytes written, possibly zero
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the write operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the write operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          从给定的缓冲区向该通道写入字节序列。
    form c.write(srcs)的此方法的调用与调用的行为完全相同
    c。写(src 0 srcs.length);
     */
    public long write(ByteBuffer[] srcs) throws IOException;

}
