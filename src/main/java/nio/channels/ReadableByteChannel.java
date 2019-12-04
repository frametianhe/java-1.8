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
 * A channel that can read bytes.
 *
 * <p> Only one read operation upon a readable channel may be in progress at
 * any given time.  If one thread initiates a read operation upon a channel
 * then any other thread that attempts to initiate another read operation will
 * block until the first operation is complete.  Whether or not other kinds of
 * I/O operations may proceed concurrently with a read operation depends upon
 * the type of the channel. </p>
 * 一个可以读取字节的通道。
 在任何给定的时间，在可读通道上只能进行一个读操作。如果一个线程在通道上启动一个读操作，那么任何试图启动另一个读操作的其他线程都将阻塞，直到第一个操作完成。其他类型的I/O操作是否可以与读操作同时进行，取决于通道的类型。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
//在可读通道上只能进行一个读操作。如果一个线程在通道上启动一个读操作，那么任何试图启动另一个读操作的其他线程都将阻塞，直到第一个操作完成
public interface ReadableByteChannel extends Channel {

    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> An attempt is made to read up to <i>r</i> bytes from the channel,
     * where <i>r</i> is the number of bytes remaining in the buffer, that is,
     * <tt>dst.remaining()</tt>, at the moment this method is invoked.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is read, where
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * This byte sequence will be transferred into the buffer so that the first
     * byte in the sequence is at index <i>p</i> and the last byte is at index
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>,
     * where <i>p</i> is the buffer's position at the moment this method is
     * invoked.  Upon return the buffer's position will be equal to
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>; its limit will not have changed.
     *
     * <p> A read operation might not fill the buffer, and in fact it might not
     * read any bytes at all.  Whether or not it does so depends upon the
     * nature and state of the channel.  A socket channel in non-blocking mode,
     * for example, cannot read any more bytes than are immediately available
     * from the socket's input buffer; similarly, a file channel cannot read
     * any more bytes than remain in the file.  It is guaranteed, however, that
     * if a channel is in blocking mode and there is at least one byte
     * remaining in the buffer then this method will block until at least one
     * byte is read.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a read operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. </p>
     *
     将该通道中的字节序列读入给定的缓冲区。
     尝试从通道读取最多r个字节，其中r是缓冲区中剩余的字节数，即dst.remaining()，此时调用此方法。
     假设一个字节序列长度为n的阅读,在0 < = n < = r。这个字节序列将被转移到缓冲区,以便序列中的第一个字节是指数p和最后一个字节是指数p + n - 1,p是缓冲区的位置目前调用该方法。返回时，缓冲区的位置将等于p + n;它的极限不会改变。
     读取操作可能不会填充缓冲区，实际上它可能根本不会读取任何字节。它是否这样做取决于通道的性质和状态。例如，在非阻塞模式下的套接字通道不能读取从套接字的输入缓冲区立即可用的任何字节;类似地，文件通道不能读取文件中剩余的字节。但是，可以保证，如果通道处于阻塞模式，并且在缓冲区中至少有一个字节，那么该方法将阻塞，直到读取至少一个字节为止。
     此方法可以随时调用。如果另一个线程已经在该通道上启动了读取操作，那么该方法的调用将被阻塞，直到第一个操作完成。
     *
     * @param  dst
     *         The buffer into which bytes are to be transferred
     *
     * @return  The number of bytes read, possibly zero, or <tt>-1</tt> if the
     *          channel has reached end-of-stream
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
     */
    public int read(ByteBuffer dst) throws IOException;

}
