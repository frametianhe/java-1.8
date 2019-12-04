/*
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * A channel that can write bytes.
 *
 * <p> Only one write operation upon a writable channel may be in progress at
 * any given time.  If one thread initiates a write operation upon a channel
 * then any other thread that attempts to initiate another write operation will
 * block until the first operation is complete.  Whether or not other kinds of
 * I/O operations may proceed concurrently with a write operation depends upon
 * the type of the channel. </p>
 * 可以写入字节的通道。
 在任何给定的时间，在可写通道上只能进行一个写操作。如果一个线程在通道上启动一个写操作，那么任何试图启动另一个写操作的其他线程都将阻塞，直到第一个操作完成。其他类型的I/O操作是否可以与写操作同时进行，取决于通道的类型。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
//在可写通道上只能进行一个写操作。如果一个线程在通道上启动一个写操作，那么任何试图启动另一个写操作的其他线程都将阻塞，直到第一个操作完成
public interface WritableByteChannel
    extends Channel
{

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> An attempt is made to write up to <i>r</i> bytes to the channel,
     * where <i>r</i> is the number of bytes remaining in the buffer, that is,
     * <tt>src.remaining()</tt>, at the moment this method is invoked.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is written, where
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * This byte sequence will be transferred from the buffer starting at index
     * <i>p</i>, where <i>p</i> is the buffer's position at the moment this
     * method is invoked; the index of the last byte written will be
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>.
     * Upon return the buffer's position will be equal to
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>; its limit will not have changed.
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
     * 从给定的缓冲区向该通道写入字节序列。
     尝试向通道写入最多r个字节，其中r是缓冲区中剩余的字节数，即src.remaining()，此时调用此方法。
     假设写入一个长度为n的字节序列，其中0 <= n <= r。这个字节序列将从索引p处的缓冲区传输，其中p是调用该方法时缓冲区的位置;最后一个字节的索引将是p + n - 1。返回时，缓冲区的位置将等于p + n;它的极限不会改变。
     除非另有规定，写入操作将只在写入所有请求的r字节之后返回。某些类型的通道，取决于它们的状态，可能只写一些字节，或者可能根本不写。例如，非阻塞模式下的套接字通道不能写入比套接字输出缓冲区中空闲的任何字节。
     此方法可以随时调用。如果另一个线程已经在该通道上启动了写操作，那么该方法的调用将被阻塞，直到第一个操作完成。
     *
     * @param  src
     *         The buffer from which bytes are to be retrieved
     *
     * @return The number of bytes written, possibly zero
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
     */
    public int write(ByteBuffer src) throws IOException;

}
