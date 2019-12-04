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

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * An asynchronous channel that can read and write bytes.
 *
 * <p> Some channels may not allow more than one read or write to be outstanding
 * at any given time. If a thread invokes a read method before a previous read
 * operation has completed then a {@link ReadPendingException} will be thrown.
 * Similarly, if a write method is invoked before a previous write has completed
 * then {@link WritePendingException} is thrown. Whether or not other kinds of
 * I/O operations may proceed concurrently with a read operation depends upon
 * the type of the channel.
 *
 * <p> Note that {@link java.nio.ByteBuffer ByteBuffers} are not safe for use by
 * multiple concurrent threads. When a read or write operation is initiated then
 * care must be taken to ensure that the buffer is not accessed until the
 * operation completes.
 *
 * @see Channels#newInputStream(AsynchronousByteChannel)
 * @see Channels#newOutputStream(AsynchronousByteChannel)
 *
 * @since 1.7
 * 可以读写字节的异步通道。
有些渠道可能不允许在任何给定的时间有超过一个的读写是杰出的。如果一个线程在之前的读操作完成之前调用一个读方法，那么将抛出一个ReadPendingException。类似地，如果在以前的写完成之前调用了写方法，则抛出WritePendingException。其他类型的I/O操作是否可以与读操作同时进行，取决于通道的类型。
注意，ByteBuffers对于多个并发线程来说不安全。当开始读或写操作时，必须注意确保在操作完成之前不会访问缓冲区。
 */
//
public interface AsynchronousByteChannel
    extends AsynchronousChannel
{
    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> This method initiates an asynchronous read operation to read a
     * sequence of bytes from this channel into the given buffer. The {@code
     * handler} parameter is a completion handler that is invoked when the read
     * operation completes (or fails). The result passed to the completion
     * handler is the number of bytes read or {@code -1} if no bytes could be
     * read because the channel has reached end-of-stream.
     *
     * <p> The read operation may read up to <i>r</i> bytes from the channel,
     * where <i>r</i> is the number of bytes remaining in the buffer, that is,
     * {@code dst.remaining()} at the time that the read is attempted. Where
     * <i>r</i> is 0, the read operation completes immediately with a result of
     * {@code 0} without initiating an I/O operation.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is read, where
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * This byte sequence will be transferred into the buffer so that the first
     * byte in the sequence is at index <i>p</i> and the last byte is at index
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>,
     * where <i>p</i> is the buffer's position at the moment the read is
     * performed. Upon completion the buffer's position will be equal to
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>; its limit will not have changed.
     *
     * <p> Buffers are not safe for use by multiple concurrent threads so care
     * should be taken to not access the buffer until the operation has
     * completed.
     *
     * <p> This method may be invoked at any time. Some channel types may not
     * allow more than one read to be outstanding at any given time. If a thread
     * initiates a read operation before a previous read operation has
     * completed then a {@link ReadPendingException} will be thrown.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   dst
     *          The buffer into which bytes are to be transferred
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The completion handler
     *
     * @throws  IllegalArgumentException
     *          If the buffer is read-only
     * @throws  ReadPendingException
     *          If the channel does not allow more than one read to be outstanding
     *          and a previous read has not completed
     * @throws  ShutdownChannelGroupException
     *          If the channel is associated with a {@link AsynchronousChannelGroup
     *          group} that has terminated
     *          将该通道中的字节序列读入给定的缓冲区。
    这个方法启动一个异步读取操作，从这个通道读取字节序列到给定的缓冲区。处理程序参数是一个完成处理程序，在读取操作完成(或失败)时调用它。传递给完成处理程序的结果是读取的字节数或-1(如果由于通道已到达流结束而无法读取字节的话)。
    读取操作可以从通道读取最多r个字节，其中r是缓冲区中剩余的字节数，即尝试读取时的dst.remaining()。当r为0时，读取操作立即完成，结果为0，而不启动I/O操作。
    假设一个字节序列长度为n的阅读,在0 < n < = r。这个字节序列将被转移到缓冲区,以便序列中的第一个字节是指数p和最后一个字节是指数p + n - 1,p是缓冲区的位置目前执行读取。完成后，缓冲区的位置将等于p + n;它的极限不会改变。
    对于多个并发线程来说，缓冲区不安全，所以在操作完成之前，应该注意不要访问缓冲区。
    此方法可以随时调用。某些通道类型可能不允许超过一个读取在任何给定的时间是突出的。如果一个线程在之前的读操作完成之前启动一个读操作，那么将抛出一个ReadPendingException。
     */
    <A> void read(ByteBuffer dst,
                  A attachment,
                  CompletionHandler<Integer,? super A> handler);

    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> This method initiates an asynchronous read operation to read a
     * sequence of bytes from this channel into the given buffer. The method
     * behaves in exactly the same manner as the {@link
     * #read(ByteBuffer,Object,CompletionHandler)
     * read(ByteBuffer,Object,CompletionHandler)} method except that instead
     * of specifying a completion handler, this method returns a {@code Future}
     * representing the pending result. The {@code Future}'s {@link Future#get()
     * get} method returns the number of bytes read or {@code -1} if no bytes
     * could be read because the channel has reached end-of-stream.
     *
     * @param   dst
     *          The buffer into which bytes are to be transferred
     *
     * @return  A Future representing the result of the operation
     *
     * @throws  IllegalArgumentException
     *          If the buffer is read-only
     * @throws  ReadPendingException
     *          If the channel does not allow more than one read to be outstanding
     *          and a previous read has not completed
     *          将该通道中的字节序列读入给定的缓冲区。
    这个方法启动一个异步读取操作，从这个通道读取字节序列到给定的缓冲区。该方法的行为方式与read(ByteBuffer、Object、CompletionHandler)方法完全相同，只是该方法不指定完成处理程序，而是返回表示等待结果的未来。未来的get方法返回读取的字节数或-1，如果因为通道已到达流结束而无法读取字节数。
     */
    Future<Integer> read(ByteBuffer dst);

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> This method initiates an asynchronous write operation to write a
     * sequence of bytes to this channel from the given buffer. The {@code
     * handler} parameter is a completion handler that is invoked when the write
     * operation completes (or fails). The result passed to the completion
     * handler is the number of bytes written.
     *
     * <p> The write operation may write up to <i>r</i> bytes to the channel,
     * where <i>r</i> is the number of bytes remaining in the buffer, that is,
     * {@code src.remaining()} at the time that the write is attempted. Where
     * <i>r</i> is 0, the write operation completes immediately with a result of
     * {@code 0} without initiating an I/O operation.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is written, where
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * This byte sequence will be transferred from the buffer starting at index
     * <i>p</i>, where <i>p</i> is the buffer's position at the moment the
     * write is performed; the index of the last byte written will be
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>.
     * Upon completion the buffer's position will be equal to
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>; its limit will not have changed.
     *
     * <p> Buffers are not safe for use by multiple concurrent threads so care
     * should be taken to not access the buffer until the operation has
     * completed.
     *
     * <p> This method may be invoked at any time. Some channel types may not
     * allow more than one write to be outstanding at any given time. If a thread
     * initiates a write operation before a previous write operation has
     * completed then a {@link WritePendingException} will be thrown.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   src
     *          The buffer from which bytes are to be retrieved
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The completion handler object
     *
     * @throws  WritePendingException
     *          If the channel does not allow more than one write to be outstanding
     *          and a previous write has not completed
     * @throws  ShutdownChannelGroupException
     *          If the channel is associated with a {@link AsynchronousChannelGroup
     *          group} that has terminated
     *          从给定的缓冲区向该通道写入字节序列。
    该方法启动一个异步写操作，从给定的缓冲区向该通道写入字节序列。处理程序参数是一个完成处理程序，在写操作完成(或失败)时调用它。传递给完成处理程序的结果是写入的字节数。
    写入操作可以向通道写入最多r个字节，其中r是缓冲区中剩余的字节数，即在尝试写入时的src.remaining()。当r为0时，写操作立即完成，结果为0，无需启动I/O操作。
    假设写了一个长度为n的字节序列，其中0 < n <= r，这个字节序列将从索引p处的缓冲区传输，其中p是执行写操作时缓冲区的位置;最后一个字节的索引将是p + n - 1。完成后，缓冲区的位置将等于p + n;它的极限不会改变。
    对于多个并发线程来说，缓冲区不安全，所以在操作完成之前，应该注意不要访问缓冲区。
    此方法可以随时调用。某些通道类型可能不允许在任何给定的时间有多个写入是未完成的。如果一个线程在之前的写操作完成之前启动了一个写操作，那么会抛出一个WritePendingException。
     */
    <A> void write(ByteBuffer src,
                   A attachment,
                   CompletionHandler<Integer,? super A> handler);

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> This method initiates an asynchronous write operation to write a
     * sequence of bytes to this channel from the given buffer. The method
     * behaves in exactly the same manner as the {@link
     * #write(ByteBuffer,Object,CompletionHandler)
     * write(ByteBuffer,Object,CompletionHandler)} method except that instead
     * of specifying a completion handler, this method returns a {@code Future}
     * representing the pending result. The {@code Future}'s {@link Future#get()
     * get} method returns the number of bytes written.
     *
     * @param   src
     *          The buffer from which bytes are to be retrieved
     *
     * @return A Future representing the result of the operation
     *
     * @throws  WritePendingException
     *          If the channel does not allow more than one write to be outstanding
     *          and a previous write has not completed
     *          从给定的缓冲区向该通道写入字节序列。
    该方法启动一个异步写操作，从给定的缓冲区向该通道写入字节序列。该方法的行为方式与write(ByteBuffer、Object、CompletionHandler)方法完全相同，只是该方法不指定完成处理程序，而是返回一个表示等待结果的未来。未来的get方法返回写入的字节数。
     */
    Future<Integer> write(ByteBuffer src);
}
