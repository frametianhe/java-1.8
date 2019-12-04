/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;

/**
 * A byte channel that maintains a current <i>position</i> and allows the
 * position to be changed.
 *
 * <p> A seekable byte channel is connected to an entity, typically a file,
 * that contains a variable-length sequence of bytes that can be read and
 * written. The current position can be {@link #position() <i>queried</i>} and
 * {@link #position(long) <i>modified</i>}. The channel also provides access to
 * the current <i>size</i> of the entity to which the channel is connected. The
 * size increases when bytes are written beyond its current size; the size
 * decreases when it is {@link #truncate <i>truncated</i>}.
 *
 * <p> The {@link #position(long) position} and {@link #truncate truncate} methods
 * which do not otherwise have a value to return are specified to return the
 * channel upon which they are invoked. This allows method invocations to be
 * chained. Implementations of this interface should specialize the return type
 * so that method invocations on the implementation class can be chained.
 * 一种字节通道，维护当前位置并允许位置被更改。
 可查找字节通道连接到一个实体(通常是一个文件)，该实体包含可读可写的可变长度字节序列。可以查询和修改当前位置。通道还提供对通道所连接的实体的当前大小的访问。当字节超过当前大小时，大小会增加;当它被截断时，大小减小。
 指定位置和截断方法(否则没有返回值)以返回调用它们的通道。这允许将方法调用链接起来。这个接口的实现应该专门化返回类型，以便可以链接实现类上的方法调用。
 *
 * @since 1.7
 * @see java.nio.file.Files#newByteChannel
 */

public interface SeekableByteChannel
    extends ByteChannel
{
    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> Bytes are read starting at this channel's current position, and
     * then the position is updated with the number of bytes actually read.
     * Otherwise this method behaves exactly as specified in the {@link
     * ReadableByteChannel} interface.
     * 将该通道中的字节序列读入给定的缓冲区。
     从这个通道的当前位置开始读取字节，然后用实际读取的字节数更新位置。否则，此方法的行为与ReadableByteChannel接口中指定的完全一样。
     */
//    从这个通道的当前位置开始读取字节，然后用实际读取的字节数更新位置
    @Override
    int read(ByteBuffer dst) throws IOException;

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> Bytes are written starting at this channel's current position, unless
     * the channel is connected to an entity such as a file that is opened with
     * the {@link java.nio.file.StandardOpenOption#APPEND APPEND} option, in
     * which case the position is first advanced to the end. The entity to which
     * the channel is connected is grown, if necessary, to accommodate the
     * written bytes, and then the position is updated with the number of bytes
     * actually written. Otherwise this method behaves exactly as specified by
     * the {@link WritableByteChannel} interface.
     * 从给定的缓冲区向该通道写入字节序列。
     字节从该通道的当前位置开始写入，除非该通道连接到一个实体，如使用APPEND选项打开的文件，在这种情况下，位置首先被推进到末尾。连接通道的实体被扩展(如果需要的话)，以适应所写的字节，然后位置被更新为实际写入的字节数。否则，该方法的行为完全由WritableByteChannel接口指定。
     */
//    字节从该通道的当前位置开始写入，除非该通道连接到一个实体，如使用APPEND选项打开的文件
    @Override
    int write(ByteBuffer src) throws IOException;

    /**
     * Returns this channel's position.返回此通道的位置。
     *
     * @return  This channel's position,
     *          a non-negative integer counting the number of bytes
     *          from the beginning of the entity to the current position
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If some other I/O error occurs
     */
    long position() throws IOException;

    /**
     * Sets this channel's position.
     *
     * <p> Setting the position to a value that is greater than the current size
     * is legal but does not change the size of the entity.  A later attempt to
     * read bytes at such a position will immediately return an end-of-file
     * indication.  A later attempt to write bytes at such a position will cause
     * the entity to grow to accommodate the new bytes; the values of any bytes
     * between the previous end-of-file and the newly-written bytes are
     * unspecified.
     *
     * <p> Setting the channel's position is not recommended when connected to
     * an entity, typically a file, that is opened with the {@link
     * java.nio.file.StandardOpenOption#APPEND APPEND} option. When opened for
     * append, the position is first advanced to the end before writing.
     * 设置这个通道的位置。
     将位置设置为大于当前大小的值是合法的，但不会更改实体的大小。稍后在这种位置读取字节的尝试将立即返回文件结束指示。以后在这种位置写入字节的尝试将导致实体增长以适应新的字节;前一个文件末端和新写入的字节之间的任何字节的值都是不指定的。
     当连接到使用APPEND选项打开的实体(通常是文件)时，不建议设置通道的位置。当为附加打开时，这个位置在写之前首先被提升到末尾。
     *
     * @param  newPosition
     *         The new position, a non-negative integer counting
     *         the number of bytes from the beginning of the entity
     *
     * @return  This channel
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IllegalArgumentException
     *          If the new position is negative
     * @throws  IOException
     *          If some other I/O error occurs
     */
    SeekableByteChannel position(long newPosition) throws IOException;

    /**
     * Returns the current size of entity to which this channel is connected.返回该通道所连接的实体的当前大小。
     *
     * @return  The current size, measured in bytes
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If some other I/O error occurs
     */
    long size() throws IOException;

    /**
     * Truncates the entity, to which this channel is connected, to the given
     * size.
     *
     * <p> If the given size is less than the current size then the entity is
     * truncated, discarding any bytes beyond the new end. If the given size is
     * greater than or equal to the current size then the entity is not modified.
     * In either case, if the current position is greater than the given size
     * then it is set to that size.
     *
     * <p> An implementation of this interface may prohibit truncation when
     * connected to an entity, typically a file, opened with the {@link
     * java.nio.file.StandardOpenOption#APPEND APPEND} option.
     * 将该通道连接到的实体截断到给定的大小。
     如果给定的大小小于当前的大小，那么实体将被截断，丢弃新端之外的任何字节。如果给定的大小大于或等于当前的大小，则不修改实体。无论哪种情况，如果当前位置大于给定的大小，则将其设置为该大小。
     当连接到使用APPEND选项打开的实体(通常是文件)时，该接口的实现可能禁止截断。
     *
     * @param  size
     *         The new size, a non-negative byte count
     *
     * @return  This channel
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IllegalArgumentException
     *          If the new size is negative
     * @throws  IOException
     *          If some other I/O error occurs
     */
//    将该通道连接到的实体截断到给定的大小
    SeekableByteChannel truncate(long size) throws IOException;
}
