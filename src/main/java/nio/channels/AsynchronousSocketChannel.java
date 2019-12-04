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

import java.nio.channels.spi.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.io.IOException;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * An asynchronous channel for stream-oriented connecting sockets.
 *
 * <p> Asynchronous socket channels are created in one of two ways. A newly-created
 * {@code AsynchronousSocketChannel} is created by invoking one of the {@link
 * #open open} methods defined by this class. A newly-created channel is open but
 * not yet connected. A connected {@code AsynchronousSocketChannel} is created
 * when a connection is made to the socket of an {@link AsynchronousServerSocketChannel}.
 * It is not possible to create an asynchronous socket channel for an arbitrary,
 * pre-existing {@link java.net.Socket socket}.
 *
 * <p> A newly-created channel is connected by invoking its {@link #connect connect}
 * method; once connected, a channel remains connected until it is closed.  Whether
 * or not a socket channel is connected may be determined by invoking its {@link
 * #getRemoteAddress getRemoteAddress} method. An attempt to invoke an I/O
 * operation upon an unconnected channel will cause a {@link NotYetConnectedException}
 * to be thrown.
 *
 * <p> Channels of this type are safe for use by multiple concurrent threads.
 * They support concurrent reading and writing, though at most one read operation
 * and one write operation can be outstanding at any time.
 * If a thread initiates a read operation before a previous read operation has
 * completed then a {@link ReadPendingException} will be thrown. Similarly, an
 * attempt to initiate a write operation before a previous write has completed
 * will throw a {@link WritePendingException}.
 *
 * <p> Socket options are configured using the {@link #setOption(SocketOption,Object)
 * setOption} method. Asynchronous socket channels support the following options:
 * <blockquote>
 * <table border summary="Socket options">
 *   <tr>
 *     <th>Option Name</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_SNDBUF SO_SNDBUF} </td>
 *     <td> The size of the socket send buffer </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_RCVBUF SO_RCVBUF} </td>
 *     <td> The size of the socket receive buffer </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_KEEPALIVE SO_KEEPALIVE} </td>
 *     <td> Keep connection alive </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> Re-use address </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#TCP_NODELAY TCP_NODELAY} </td>
 *     <td> Disable the Nagle algorithm </td>
 *   </tr>
 * </table>
 * </blockquote>
 * Additional (implementation specific) options may also be supported.
 *
 * <h2>Timeouts</h2>
 *
 * <p> The {@link #read(ByteBuffer,long,TimeUnit,Object,CompletionHandler) read}
 * and {@link #write(ByteBuffer,long,TimeUnit,Object,CompletionHandler) write}
 * methods defined by this class allow a timeout to be specified when initiating
 * a read or write operation. If the timeout elapses before an operation completes
 * then the operation completes with the exception {@link
 * InterruptedByTimeoutException}. A timeout may leave the channel, or the
 * underlying connection, in an inconsistent state. Where the implementation
 * cannot guarantee that bytes have not been read from the channel then it puts
 * the channel into an implementation specific <em>error state</em>. A subsequent
 * attempt to initiate a {@code read} operation causes an unspecified runtime
 * exception to be thrown. Similarly if a {@code write} operation times out and
 * the implementation cannot guarantee bytes have not been written to the
 * channel then further attempts to {@code write} to the channel cause an
 * unspecified runtime exception to be thrown. When a timeout elapses then the
 * state of the {@link ByteBuffer}, or the sequence of buffers, for the I/O
 * operation is not defined. Buffers should be discarded or at least care must
 * be taken to ensure that the buffers are not accessed while the channel remains
 * open. All methods that accept timeout parameters treat values less than or
 * equal to zero to mean that the I/O operation does not timeout.
 *
 * @since 1.7
 * 面向流的连接套接字的异步通道。
异步套接字通道的创建有两种方式。新创建的AsynchronousSocketChannel通过调用该类定义的开放方法之一来创建。新创建的通道是打开的，但尚未连接。当连接到异步serversocketchannel的套接字时，将创建一个已连接的AsynchronousSocketChannel。为任意已存在的套接字创建异步套接字通道是不可能的。
通过调用其连接方法连接新创建的通道;通道一旦连接，就会一直连接到关闭为止。是否通过调用其getRemoteAddress方法来确定套接字通道是否连接。试图在一个未连接的通道上调用I/O操作将导致抛出一个NotYetConnectedException。
这种类型的通道对于多个并发线程来说是安全的。它们支持并发读和写，不过最多一次读操作和一次写操作可以在任何时候都是出色的。如果一个线程在之前的读操作之前启动了一个读操作，那么将抛出一个ReadPendingException。类似地，尝试在前一次写操作完成之前发起写操作将抛出WritePendingException。
SO_SNDBUF
套接字发送缓冲区的大小
SO_RCVBUF
套接字接收缓冲区的大小
SO_KEEPALIVE
保持连接
SO_REUSEADDR
重用地址
TCP_NODELAY
纳格尔禁用算法
还可以支持其他(特定于实现的)选项。
超时
这个类定义的读和写方法允许在初始化读或写操作时指定超时。如果在操作完成之前超时运行，那么操作将在异常被timeoutexception中断的情况下完成。超时可能使通道或底层连接处于不一致的状态。如果实现不能保证没有从通道读取字节，那么它会将通道放入实现特定的错误状态。发起读操作的后续尝试将引发未指定的运行时异常。类似地，如果写入操作超时，并且实现不能保证字节没有被写入通道，那么进一步尝试写入通道将导致抛出未指定的运行时异常。当超时运行时，则未定义I/O操作的ByteBuffer或缓冲区序列的状态。应该丢弃缓冲区，或者至少必须注意确保在通道保持打开时不访问缓冲区。接受超时参数的所有方法处理小于或等于零的值，意味着I/O操作没有超时。
 */

public abstract class AsynchronousSocketChannel
    implements AsynchronousByteChannel, NetworkChannel
{
    private final AsynchronousChannelProvider provider;

    /**
     * Initializes a new instance of this class.
     *
     * @param  provider
     *         The provider that created this channel
     */
    protected AsynchronousSocketChannel(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the provider that created this channel.
     *
     * @return  The provider that created this channel
     */
    public final AsynchronousChannelProvider provider() {
        return provider;
    }

    /**
     * Opens an asynchronous socket channel.
     *
     * <p> The new channel is created by invoking the {@link
     * AsynchronousChannelProvider#openAsynchronousSocketChannel
     * openAsynchronousSocketChannel} method on the {@link
     * AsynchronousChannelProvider} that created the group. If the group parameter
     * is {@code null} then the resulting channel is created by the system-wide
     * default provider, and bound to the <em>default group</em>.
     *
     * @param   group
     *          The group to which the newly constructed channel should be bound,
     *          or {@code null} for the default group
     *
     * @return  A new asynchronous socket channel
     *
     * @throws  ShutdownChannelGroupException
     *          If the channel group is shutdown
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个异步套接字通道。
    新通道是通过在创建组的AsynchronousChannelProvider上调用openAsynchronousSocketChannel方法创建的。如果组参数为null，那么生成的通道将由系统范围的默认提供程序创建，并绑定到默认组。
     */
    public static AsynchronousSocketChannel open(AsynchronousChannelGroup group)
        throws IOException
    {
        AsynchronousChannelProvider provider = (group == null) ?
            AsynchronousChannelProvider.provider() : group.provider();
        return provider.openAsynchronousSocketChannel(group);
    }

    /**
     * Opens an asynchronous socket channel.
     *
     * <p> This method returns an asynchronous socket channel that is bound to
     * the <em>default group</em>.This method is equivalent to evaluating the
     * expression:
     * <blockquote><pre>
     * open((AsynchronousChannelGroup)null);
     * </pre></blockquote>
     *
     * @return  A new asynchronous socket channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个异步套接字通道。
    此方法返回绑定到默认组的异步套接字通道。该方法等价于评价表达式:
    打开((AsynchronousChannelGroup)零);
     */
    public static AsynchronousSocketChannel open()
        throws IOException
    {
        return open(null);
    }


    // -- socket options and related --

    /**
     * @throws  ConnectionPendingException
     *          If a connection operation is already in progress on this channel
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          If a security manager has been installed and its
     *          {@link SecurityManager#checkListen checkListen} method denies
     *          the operation
     *          将通道的套接字绑定到本地地址。
    此方法用于在套接字和本地地址之间建立关联。一旦建立了关联，套接字将保持绑定，直到通道关闭。如果本地参数具有值null，那么套接字将绑定到自动分配的地址。
     */
    @Override
    public abstract AsynchronousSocketChannel bind(SocketAddress local)
        throws IOException;

    /**
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     */
    @Override
    public abstract <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * Shutdown the connection for reading without closing the channel.
     *
     * <p> Once shutdown for reading then further reads on the channel will
     * return {@code -1}, the end-of-stream indication. If the input side of the
     * connection is already shutdown then invoking this method has no effect.
     * The effect on an outstanding read operation is system dependent and
     * therefore not specified. The effect, if any, when there is data in the
     * socket receive buffer that has not been read, or data arrives subsequently,
     * is also system dependent.
     *
     * @return  The channel
     *
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If some other I/O error occurs
     *          在不关闭通道的情况下关闭读取连接。
    一旦停止读取，则通道上的进一步读取将返回-1，即流结束指示。如果连接的输入端已经关闭，则调用此方法无效。对未完成的读操作的影响依赖于系统，因此没有指定。如果套接字接收缓冲区中有未被读取的数据，或者数据随后到达，那么这种影响(如果有的话)也与系统相关。
     */
    public abstract AsynchronousSocketChannel shutdownInput() throws IOException;

    /**
     * Shutdown the connection for writing without closing the channel.
     *
     * <p> Once shutdown for writing then further attempts to write to the
     * channel will throw {@link ClosedChannelException}. If the output side of
     * the connection is already shutdown then invoking this method has no
     * effect. The effect on an outstanding write operation is system dependent
     * and therefore not specified.
     *
     * @return  The channel
     *
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If some other I/O error occurs
     *
    在不关闭通道的情况下关闭写入连接。
    一旦停止写入，然后进一步尝试写入通道将抛出ClosedChannelException。如果连接的输出端已经关闭，则调用此方法无效。对未完成的写操作的影响依赖于系统，因此没有指定。
     */
//    关闭写入连接，不关闭通道
    public abstract AsynchronousSocketChannel shutdownOutput() throws IOException;

    // -- state --

    /**
     * Returns the remote address to which this channel's socket is connected.
     *
     * <p> Where the channel is bound and connected to an Internet Protocol
     * socket address then the return value from this method is of type {@link
     * java.net.InetSocketAddress}.
     *
     * @return  The remote address; {@code null} if the channel's socket is not
     *          connected
     *
     * @throws  ClosedChannelException
     *          If the channel is closed
     * @throws  IOException
     *          If an I/O error occurs
     *          返回该通道的套接字连接的远程地址。
    当通道被绑定并连接到Internet协议套接字地址时，该方法的返回值是java.net.InetSocketAddress类型。
     */
    public abstract SocketAddress getRemoteAddress() throws IOException;

    // -- asynchronous operations --

    /**
     * Connects this channel.
     *
     * <p> This method initiates an operation to connect this channel. The
     * {@code handler} parameter is a completion handler that is invoked when
     * the connection is successfully established or connection cannot be
     * established. If the connection cannot be established then the channel is
     * closed.
     *
     * <p> This method performs exactly the same security checks as the {@link
     * java.net.Socket} class.  That is, if a security manager has been
     * installed then this method verifies that its {@link
     * java.lang.SecurityManager#checkConnect checkConnect} method permits
     * connecting to the address and port number of the given remote endpoint.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   remote
     *          The remote address to which this channel is to be connected
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The handler for consuming the result
     *
     * @throws  UnresolvedAddressException
     *          If the given remote address is not fully resolved
     * @throws  UnsupportedAddressTypeException
     *          If the type of the given remote address is not supported
     * @throws  AlreadyConnectedException
     *          If this channel is already connected
     * @throws  ConnectionPendingException
     *          If a connection operation is already in progress on this channel
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit access to the given remote endpoint
     *
     * @see #getRemoteAddress
     * 连接这个通道。
    此方法启动连接该通道的操作。处理程序参数是一个完成处理程序，在成功建立连接或无法建立连接时调用它。如果连接无法建立，则通道关闭。
    此方法执行与java.net.Socket类完全相同的安全性检查。也就是说，如果安装了安全管理器，那么该方法将验证其checkConnect方法是否允许连接到给定远程端点的地址和端口号。
     */
    public abstract <A> void connect(SocketAddress remote,
                                     A attachment,
                                     CompletionHandler<Void,? super A> handler);

    /**
     * Connects this channel.
     *
     * <p> This method initiates an operation to connect this channel. This
     * method behaves in exactly the same manner as the {@link
     * #connect(SocketAddress, Object, CompletionHandler)} method except that
     * instead of specifying a completion handler, this method returns a {@code
     * Future} representing the pending result. The {@code Future}'s {@link
     * Future#get() get} method returns {@code null} on successful completion.
     *
     * @param   remote
     *          The remote address to which this channel is to be connected
     *
     * @return  A {@code Future} object representing the pending result
     *
     * @throws  UnresolvedAddressException
     *          If the given remote address is not fully resolved
     * @throws  UnsupportedAddressTypeException
     *          If the type of the given remote address is not supported
     * @throws  AlreadyConnectedException
     *          If this channel is already connected
     * @throws  ConnectionPendingException
     *          If a connection operation is already in progress on this channel
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit access to the given remote endpoint
     *          连接这个通道。
    此方法启动连接该通道的操作。此方法的行为方式与connect(SocketAddress、Object、CompletionHandler)方法完全相同，只是该方法不指定完成处理程序，而是返回一个表示等待结果的未来。未来的get方法在成功完成时返回null。
     */
    public abstract Future<Void> connect(SocketAddress remote);

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
     * <p> If a timeout is specified and the timeout elapses before the operation
     * completes then the operation completes with the exception {@link
     * InterruptedByTimeoutException}. Where a timeout occurs, and the
     * implementation cannot guarantee that bytes have not been read, or will not
     * be read from the channel into the given buffer, then further attempts to
     * read from the channel will cause an unspecific runtime exception to be
     * thrown.
     *
     * <p> Otherwise this method works in the same manner as the {@link
     * AsynchronousByteChannel#read(ByteBuffer,Object,CompletionHandler)}
     * method.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   dst
     *          The buffer into which bytes are to be transferred
     * @param   timeout
     *          The maximum time for the I/O operation to complete
     * @param   unit
     *          The time unit of the {@code timeout} argument
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The handler for consuming the result
     *
     * @throws  IllegalArgumentException
     *          If the buffer is read-only
     * @throws  ReadPendingException
     *          If a read operation is already in progress on this channel
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     *          将该通道中的字节序列读入给定的缓冲区。
    这个方法启动一个异步读取操作，从这个通道读取字节序列到给定的缓冲区。处理程序参数是一个完成处理程序，在读取操作完成(或失败)时调用它。传递给完成处理程序的结果是读取的字节数或-1(如果由于通道已到达流结束而无法读取字节的话)。
    如果指定了超时，并且在操作完成之前超时运行，那么操作将在timeoutexception异常中断的情况下完成。如果出现超时，并且实现不能保证字节没有被读取，或者不会从通道读取到给定的缓冲区，那么进一步尝试从通道读取将导致抛出一个非特定的运行时异常。
    否则，此方法与异步bytechannel的工作方式相同。读(ByteBuffer,对象,CompletionHandler)方法。
     */
    public abstract <A> void read(ByteBuffer dst,
                                  long timeout,
                                  TimeUnit unit,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler);

    /**
     * @throws  IllegalArgumentException        {@inheritDoc}
     * @throws  ReadPendingException            {@inheritDoc}
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     *          将该通道中的字节序列读入给定的缓冲区。
    这个方法启动一个异步读取操作，从这个通道读取字节序列到给定的缓冲区。处理程序参数是一个完成处理程序，在读取操作完成(或失败)时调用它。传递给完成处理程序的结果是读取的字节数或-1(如果由于通道已到达流结束而无法读取字节的话)。
    读取操作可以从通道读取最多r个字节，其中r是缓冲区中剩余的字节数，即尝试读取时的dst.remaining()。当r为0时，读取操作立即完成，结果为0，而不启动I/O操作。
    假设一个字节序列长度为n的阅读,在0 < n < = r。这个字节序列将被转移到缓冲区,以便序列中的第一个字节是指数p和最后一个字节是指数p + n - 1,p是缓冲区的位置目前执行读取。完成后，缓冲区的位置将等于p + n;它的极限不会改变。
    对于多个并发线程来说，缓冲区不安全，所以在操作完成之前，应该注意不要访问缓冲区。
    此方法可以随时调用。某些通道类型可能不允许超过一个读取在任何给定的时间是突出的。如果一个线程在之前的读操作完成之前启动一个读操作，那么将抛出一个ReadPendingException。
     */
//    线程不安全，操作完成不要访问缓冲区
    @Override
    public final <A> void read(ByteBuffer dst,
                               A attachment,
                               CompletionHandler<Integer,? super A> handler)
    {
        read(dst, 0L, TimeUnit.MILLISECONDS, attachment, handler);
    }

    /**
     * @throws  IllegalArgumentException        {@inheritDoc}
     * @throws  ReadPendingException            {@inheritDoc}
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     *          将该通道中的字节序列读入给定的缓冲区。
    这个方法启动一个异步读取操作，从这个通道读取字节序列到给定的缓冲区。该方法的行为方式与read(ByteBuffer、Object、CompletionHandler)方法完全相同，只是该方法不指定完成处理程序，而是返回表示等待结果的未来。未来的get方法返回读取的字节数或-1，如果因为通道已到达流结束而无法读取字节数。
     */
//    返回读取的字节数
    @Override
    public abstract Future<Integer> read(ByteBuffer dst);

    /**
     * Reads a sequence of bytes from this channel into a subsequence of the
     * given buffers. This operation, sometimes called a <em>scattering read</em>,
     * is often useful when implementing network protocols that group data into
     * segments consisting of one or more fixed-length headers followed by a
     * variable-length body. The {@code handler} parameter is a completion
     * handler that is invoked when the read operation completes (or fails). The
     * result passed to the completion handler is the number of bytes read or
     * {@code -1} if no bytes could be read because the channel has reached
     * end-of-stream.
     *
     * <p> This method initiates a read of up to <i>r</i> bytes from this channel,
     * where <i>r</i> is the total number of bytes remaining in the specified
     * subsequence of the given buffer array, that is,
     *
     * <blockquote><pre>
     * dsts[offset].remaining()
     *     + dsts[offset+1].remaining()
     *     + ... + dsts[offset+length-1].remaining()</pre></blockquote>
     *
     * at the moment that the read is attempted.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is read, where
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * Up to the first <tt>dsts[offset].remaining()</tt> bytes of this sequence
     * are transferred into buffer <tt>dsts[offset]</tt>, up to the next
     * <tt>dsts[offset+1].remaining()</tt> bytes are transferred into buffer
     * <tt>dsts[offset+1]</tt>, and so forth, until the entire byte sequence
     * is transferred into the given buffers.  As many bytes as possible are
     * transferred into each buffer, hence the final position of each updated
     * buffer, except the last updated buffer, is guaranteed to be equal to
     * that buffer's limit. The underlying operating system may impose a limit
     * on the number of buffers that may be used in an I/O operation. Where the
     * number of buffers (with bytes remaining), exceeds this limit, then the
     * I/O operation is performed with the maximum number of buffers allowed by
     * the operating system.
     *
     * <p> If a timeout is specified and the timeout elapses before the operation
     * completes then it completes with the exception {@link
     * InterruptedByTimeoutException}. Where a timeout occurs, and the
     * implementation cannot guarantee that bytes have not been read, or will not
     * be read from the channel into the given buffers, then further attempts to
     * read from the channel will cause an unspecific runtime exception to be
     * thrown.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   dsts
     *          The buffers into which bytes are to be transferred
     * @param   offset
     *          The offset within the buffer array of the first buffer into which
     *          bytes are to be transferred; must be non-negative and no larger than
     *          {@code dsts.length}
     * @param   length
     *          The maximum number of buffers to be accessed; must be non-negative
     *          and no larger than {@code dsts.length - offset}
     * @param   timeout
     *          The maximum time for the I/O operation to complete
     * @param   unit
     *          The time unit of the {@code timeout} argument
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The handler for consuming the result
     *
     * @throws  IndexOutOfBoundsException
     *          If the pre-conditions for the {@code offset}  and {@code length}
     *          parameter aren't met
     * @throws  IllegalArgumentException
     *          If the buffer is read-only
     * @throws  ReadPendingException
     *          If a read operation is already in progress on this channel
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     *          将该通道中的字节序列读入给定缓冲区的子序列。这种操作有时被称为“散射读取”，在实现将数据分组为由一个或多个固定长度标头和一个可变长度主体组成的段的网络协议时，通常是有用的。处理程序参数是一个完成处理程序，在读取操作完成(或失败)时调用它。传递给完成处理程序的结果是读取的字节数或-1(如果由于通道已到达流结束而无法读取字节的话)。
    该方法从这个通道启动一个到r字节的读取，其中r是给定缓冲区数组的指定子序列中剩余的字节数，即，
    正在尝试读取。
    假设一个字节序列长度为n的阅读,在0 < n < = r。到第一个数据(抵消).remaining()字节的顺序转移到缓冲区数据(抵消),到下一个数据的[抵消+ 1].remaining()字节转移到缓冲区数据的偏移量+ 1,等等,直到整个字节序列转换成给定的缓冲区。尽可能多的字节被传输到每个缓冲区中，因此每个更新缓冲区的最终位置(除了最后一个更新的缓冲区之外)保证等于缓冲区的限制。底层操作系统可能对可能在I/O操作中使用的缓冲区数量施加限制。如果缓冲区的数量(剩余的字节)超过这个限制，那么I/O操作将在操作系统允许的最大缓冲区数量下执行。
    如果指定了一个超时，并且在操作完成之前超时已运行，那么它将以异常InterruptedByTimeoutException完成。如果出现超时，并且实现不能保证字节没有被读取，或者不会从通道读取到给定的缓冲区，那么进一步尝试从通道读取将导致抛出一个非特定的运行时异常。
     */
//    scatter散射读取
    public abstract <A> void read(ByteBuffer[] dsts,
                                  int offset,
                                  int length,
                                  long timeout,
                                  TimeUnit unit,
                                  A attachment,
                                  CompletionHandler<Long,? super A> handler);

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> This method initiates an asynchronous write operation to write a
     * sequence of bytes to this channel from the given buffer. The {@code
     * handler} parameter is a completion handler that is invoked when the write
     * operation completes (or fails). The result passed to the completion
     * handler is the number of bytes written.
     *
     * <p> If a timeout is specified and the timeout elapses before the operation
     * completes then it completes with the exception {@link
     * InterruptedByTimeoutException}. Where a timeout occurs, and the
     * implementation cannot guarantee that bytes have not been written, or will
     * not be written to the channel from the given buffer, then further attempts
     * to write to the channel will cause an unspecific runtime exception to be
     * thrown.
     *
     * <p> Otherwise this method works in the same manner as the {@link
     * AsynchronousByteChannel#write(ByteBuffer,Object,CompletionHandler)}
     * method.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   src
     *          The buffer from which bytes are to be retrieved
     * @param   timeout
     *          The maximum time for the I/O operation to complete
     * @param   unit
     *          The time unit of the {@code timeout} argument
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The handler for consuming the result
     *
     * @throws  WritePendingException
     *          If a write operation is already in progress on this channel
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     *          从给定的缓冲区向该通道写入字节序列。
    该方法启动一个异步写操作，从给定的缓冲区向该通道写入字节序列。处理程序参数是一个完成处理程序，在写操作完成(或失败)时调用它。传递给完成处理程序的结果是写入的字节数。
    如果指定了一个超时，并且在操作完成之前超时已运行，那么它将以异常InterruptedByTimeoutException完成。如果出现超时，并且实现不能保证字节没有被写入，或者不会从给定的缓冲区写入到通道，那么进一步尝试写入通道将导致抛出一个非特定的运行时异常。
    否则，此方法与异步bytechannel的工作方式相同。写(ByteBuffer,对象,CompletionHandler)方法。
     */
    public abstract <A> void write(ByteBuffer src,
                                   long timeout,
                                   TimeUnit unit,
                                   A attachment,
                                   CompletionHandler<Integer,? super A> handler);

    /**
     * @throws  WritePendingException          {@inheritDoc}
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     *          从给定的缓冲区向该通道写入字节序列。
    该方法启动一个异步写操作，从给定的缓冲区向该通道写入字节序列。处理程序参数是一个完成处理程序，在写操作完成(或失败)时调用它。传递给完成处理程序的结果是写入的字节数。
    写入操作可以向通道写入最多r个字节，其中r是缓冲区中剩余的字节数，即在尝试写入时的src.remaining()。当r为0时，写操作立即完成，结果为0，无需启动I/O操作。
    假设写了一个长度为n的字节序列，其中0 < n <= r，这个字节序列将从索引p处的缓冲区传输，其中p是执行写操作时缓冲区的位置;最后一个字节的索引将是p + n - 1。完成后，缓冲区的位置将等于p + n;它的极限不会改变。
    对于多个并发线程来说，缓冲区不安全，所以在操作完成之前，应该注意不要访问缓冲区。
    此方法可以随时调用。某些通道类型可能不允许在任何给定的时间有多个写入是未完成的。如果一个线程在之前的写操作完成之前启动了一个写操作，那么会抛出一个WritePendingException。
     */
//    缓冲区线程不安全,缓冲区操作完成之间不能操作缓冲区
    @Override
    public final <A> void write(ByteBuffer src,
                                A attachment,
                                CompletionHandler<Integer,? super A> handler)

    {
        write(src, 0L, TimeUnit.MILLISECONDS, attachment, handler);
    }

    /**
     * @throws  WritePendingException       {@inheritDoc}
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     *          从给定的缓冲区向该通道写入字节序列。
    该方法启动一个异步写操作，从给定的缓冲区向该通道写入字节序列。该方法的行为方式与write(ByteBuffer、Object、CompletionHandler)方法完全相同，只是该方法不指定完成处理程序，而是返回一个表示等待结果的未来。未来的get方法返回写入的字节数。
     */
//    返回写入的字节数
    @Override
    public abstract Future<Integer> write(ByteBuffer src);

    /**
     * Writes a sequence of bytes to this channel from a subsequence of the given
     * buffers. This operation, sometimes called a <em>gathering write</em>, is
     * often useful when implementing network protocols that group data into
     * segments consisting of one or more fixed-length headers followed by a
     * variable-length body. The {@code handler} parameter is a completion
     * handler that is invoked when the write operation completes (or fails).
     * The result passed to the completion handler is the number of bytes written.
     *
     * <p> This method initiates a write of up to <i>r</i> bytes to this channel,
     * where <i>r</i> is the total number of bytes remaining in the specified
     * subsequence of the given buffer array, that is,
     *
     * <blockquote><pre>
     * srcs[offset].remaining()
     *     + srcs[offset+1].remaining()
     *     + ... + srcs[offset+length-1].remaining()</pre></blockquote>
     *
     * at the moment that the write is attempted.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is written, where
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * Up to the first <tt>srcs[offset].remaining()</tt> bytes of this sequence
     * are written from buffer <tt>srcs[offset]</tt>, up to the next
     * <tt>srcs[offset+1].remaining()</tt> bytes are written from buffer
     * <tt>srcs[offset+1]</tt>, and so forth, until the entire byte sequence is
     * written.  As many bytes as possible are written from each buffer, hence
     * the final position of each updated buffer, except the last updated
     * buffer, is guaranteed to be equal to that buffer's limit. The underlying
     * operating system may impose a limit on the number of buffers that may be
     * used in an I/O operation. Where the number of buffers (with bytes
     * remaining), exceeds this limit, then the I/O operation is performed with
     * the maximum number of buffers allowed by the operating system.
     *
     * <p> If a timeout is specified and the timeout elapses before the operation
     * completes then it completes with the exception {@link
     * InterruptedByTimeoutException}. Where a timeout occurs, and the
     * implementation cannot guarantee that bytes have not been written, or will
     * not be written to the channel from the given buffers, then further attempts
     * to write to the channel will cause an unspecific runtime exception to be
     * thrown.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   srcs
     *          The buffers from which bytes are to be retrieved
     * @param   offset
     *          The offset within the buffer array of the first buffer from which
     *          bytes are to be retrieved; must be non-negative and no larger
     *          than {@code srcs.length}
     * @param   length
     *          The maximum number of buffers to be accessed; must be non-negative
     *          and no larger than {@code srcs.length - offset}
     * @param   timeout
     *          The maximum time for the I/O operation to complete
     * @param   unit
     *          The time unit of the {@code timeout} argument
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The handler for consuming the result
     *
     * @throws  IndexOutOfBoundsException
     *          If the pre-conditions for the {@code offset}  and {@code length}
     *          parameter aren't met
     * @throws  WritePendingException
     *          If a write operation is already in progress on this channel
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated
     *          从给定缓冲区的子序列向该通道写入字节序列。这种操作有时被称为收集写操作，在实现将数据分组为由一个或多个固定长度的标头和一个可变长度主体组成的段的网络协议时，通常是有用的。处理程序参数是一个完成处理程序，在写操作完成(或失败)时调用它。传递给完成处理程序的结果是写入的字节数。
    该方法开始将r字节写入这个通道，其中r是给定缓冲区数组的指定子序列中剩余的字节数，即，
    在尝试写的那一刻。
    假设一个字节序列长度为n的写,在0 < n < = r。到第一个src(抵消).remaining()字节的序列从缓冲区写src(抵消),到下一个src[抵消+ 1].remaining()从缓冲区字节写src(抵消+ 1),等等,直到整个字节序列。从每个缓冲区写入尽可能多的字节，因此每个更新缓冲区的最终位置(除了最后一个更新的缓冲区之外)保证等于缓冲区的限制。底层操作系统可能对可能在I/O操作中使用的缓冲区数量施加限制。如果缓冲区的数量(剩余的字节)超过这个限制，那么I/O操作将在操作系统允许的最大缓冲区数量下执行。
    如果指定了一个超时，并且在操作完成之前超时已运行，那么它将以异常InterruptedByTimeoutException完成。如果发生超时，并且实现不能保证字节没有被写入，或者不会被写入到来自给定缓冲区的通道，那么进一步尝试写入通道将导致抛出一个非特定的运行时异常。
     */
//    gathering收集写操作
    public abstract <A> void write(ByteBuffer[] srcs,
                                   int offset,
                                   int length,
                                   long timeout,
                                   TimeUnit unit,
                                   A attachment,
                                   CompletionHandler<Long,? super A> handler);

    /**
     * {@inheritDoc}
     * <p>
     * If there is a security manager set, its {@code checkConnect} method is
     * called with the local address and {@code -1} as its arguments to see
     * if the operation is allowed. If the operation is not allowed,
     * a {@code SocketAddress} representing the
     * {@link java.net.InetAddress#getLoopbackAddress loopback} address and the
     * local port of the channel's socket is returned.
     *
     * @return  The {@code SocketAddress} that the socket is bound to, or the
     *          {@code SocketAddress} representing the loopback address if
     *          denied by the security manager, or {@code null} if the
     *          channel's socket is not bound
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     * 返回该通道的套接字绑定到的套接字地址。
    如果通道绑定到Internet协议套接字地址，则该方法的返回值为java.net.InetSocketAddress类型。
    如果有一个安全管理器集，那么它的checkConnect方法将以本地地址和-1作为参数，以查看操作是否被允许。如果不允许操作，则返回表示该信道套接字的环回地址和本地端口的SocketAddress。
     */
    public abstract SocketAddress getLocalAddress() throws IOException;
}
