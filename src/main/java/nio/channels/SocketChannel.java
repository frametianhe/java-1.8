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

package java.nio.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A selectable channel for stream-oriented connecting sockets.
 *
 * <p> A socket channel is created by invoking one of the {@link #open open}
 * methods of this class.  It is not possible to create a channel for an arbitrary,
 * pre-existing socket. A newly-created socket channel is open but not yet
 * connected.  An attempt to invoke an I/O operation upon an unconnected
 * channel will cause a {@link NotYetConnectedException} to be thrown.  A
 * socket channel can be connected by invoking its {@link #connect connect}
 * method; once connected, a socket channel remains connected until it is
 * closed.  Whether or not a socket channel is connected may be determined by
 * invoking its {@link #isConnected isConnected} method.
 *
 * <p> Socket channels support <i>non-blocking connection:</i>&nbsp;A socket
 * channel may be created and the process of establishing the link to the
 * remote socket may be initiated via the {@link #connect connect} method for
 * later completion by the {@link #finishConnect finishConnect} method.
 * Whether or not a connection operation is in progress may be determined by
 * invoking the {@link #isConnectionPending isConnectionPending} method.
 *
 * <p> Socket channels support <i>asynchronous shutdown,</i> which is similar
 * to the asynchronous close operation specified in the {@link Channel} class.
 * If the input side of a socket is shut down by one thread while another
 * thread is blocked in a read operation on the socket's channel, then the read
 * operation in the blocked thread will complete without reading any bytes and
 * will return <tt>-1</tt>.  If the output side of a socket is shut down by one
 * thread while another thread is blocked in a write operation on the socket's
 * channel, then the blocked thread will receive an {@link
 * AsynchronousCloseException}.
 *
 * <p> Socket options are configured using the {@link #setOption(SocketOption,Object)
 * setOption} method. Socket channels support the following options:
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
 *     <td> {@link java.net.StandardSocketOptions#SO_LINGER SO_LINGER} </td>
 *     <td> Linger on close if data is present (when configured in blocking mode
 *          only) </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#TCP_NODELAY TCP_NODELAY} </td>
 *     <td> Disable the Nagle algorithm </td>
 *   </tr>
 * </table>
 * </blockquote>
 * Additional (implementation specific) options may also be supported.
 *
 * <p> Socket channels are safe for use by multiple concurrent threads.  They
 * support concurrent reading and writing, though at most one thread may be
 * reading and at most one thread may be writing at any given time.  The {@link
 * #connect connect} and {@link #finishConnect finishConnect} methods are
 * mutually synchronized against each other, and an attempt to initiate a read
 * or write operation while an invocation of one of these methods is in
 * progress will block until that invocation is complete.  </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 面向流的连接套接字的可选择通道。
套接字通道是通过调用该类的一个开放方法创建的。不可能为任意的预先存在的套接字创建一个通道。新创建的套接字通道是打开的，但尚未连接。尝试在未连接的通道上调用I/O操作将导致抛出NotYetConnectedException。套接字通道可通过调用其连接方法进行连接;一旦连接，套接字通道保持连接直到关闭。是否连接套接字通道可以通过调用其isConnected方法来确定。
套接字通道支持非阻塞连接:可以创建套接字通道，并可以通过connect方法启动到远程套接字的连接过程，以便以后使用finishConnect方法完成。是否正在进行连接操作可以通过调用isConnectionPending方法来确定。
套接字通道支持异步关闭，这类似于通道类中指定的异步关闭操作。如果套接字的输入端被一个线程关闭，而另一个线程在套接字通道的读操作中被阻塞，那么阻塞线程中的读操作将在不读取任何字节的情况下完成，并返回-1。如果套接字的输出端被一个线程关闭，而另一个线程在套接字通道的写操作中被阻塞，那么被阻塞的线程将接收异步scloseexception。
套接字选项使用setOption方法进行配置。套接字通道支持以下选项:
SO_SNDBUF
套接字发送缓冲区的大小
SO_RCVBUF
套接字接收缓冲区的大小
SO_KEEPALIVE
保持连接
SO_REUSEADDR
重用地址
SO_LINGER
如果数据存在(仅在阻塞模式下配置时)，请在关闭状态徘徊
TCP_NODELAY
纳格尔禁用算法
还可以支持其他(特定于实现的)选项。
套接字通道对于多个并发线程来说是安全的。它们支持并发的读和写，尽管最多一个线程可能正在读，最多一个线程可能在任何给定的时间正在写。连接和finishConnect方法是相互同步的，当其中一个方法的调用正在进行时，尝试发起读或写操作将被阻塞，直到调用完成。
 */
//套接字通道对于多个并发线程来说是安全的。它们支持并发的读和写，尽管最多一个线程可能正在读，最多一个线程可能在任何给定的时间正在写。连接和finishConnect方法是相互同步的，当其中一个方法的调用正在进行时，尝试发起读或写操作将被阻塞，直到调用完成
public abstract class SocketChannel
    extends AbstractSelectableChannel
    implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel
{

    /**
     * Initializes a new instance of this class.
     *
     * @param  provider
     *         The provider that created this channel
     */
    protected SocketChannel(SelectorProvider provider) {
        super(provider);
    }

    /**
     * Opens a socket channel.
     *
     * <p> The new channel is created by invoking the {@link
     * java.nio.channels.spi.SelectorProvider#openSocketChannel
     * openSocketChannel} method of the system-wide default {@link
     * java.nio.channels.spi.SelectorProvider} object.  </p>
     *
     * @return  A new socket channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个套接字通道。
    通过调用系统范围内的默认SelectorProvider对象的openSocketChannel方法创建新通道。
     */
    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    /**
     * Opens a socket channel and connects it to a remote address.
     *
     * <p> This convenience method works as if by invoking the {@link #open()}
     * method, invoking the {@link #connect(SocketAddress) connect} method upon
     * the resulting socket channel, passing it <tt>remote</tt>, and then
     * returning that channel.  </p>
     *
     * @param  remote
     *         The remote address to which the new channel is to be connected
     *
     * @return  A new, and connected, socket channel
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the connect operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the connect operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  UnresolvedAddressException
     *          If the given remote address is not fully resolved
     *
     * @throws  UnsupportedAddressTypeException
     *          If the type of the given remote address is not supported
     *
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit access to the given remote endpoint
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          打开套接字通道并将其连接到远程地址。
    这个方便的方法就像调用open()方法一样工作，在结果套接字通道上调用connect方法，远程传递它，然后返回那个通道。
     */
    public static SocketChannel open(SocketAddress remote)
        throws IOException
    {
        SocketChannel sc = open();
        try {
            sc.connect(remote);
        } catch (Throwable x) {
            try {
                sc.close();
            } catch (Throwable suppressed) {
                x.addSuppressed(suppressed);
            }
            throw x;
        }
        assert sc.isConnected();
        return sc;
    }

    /**
     * Returns an operation set identifying this channel's supported
     * operations.
     *
     * <p> Socket channels support connecting, reading, and writing, so this
     * method returns <tt>(</tt>{@link SelectionKey#OP_CONNECT}
     * <tt>|</tt>&nbsp;{@link SelectionKey#OP_READ} <tt>|</tt>&nbsp;{@link
     * SelectionKey#OP_WRITE}<tt>)</tt>.  </p>
     *
     * @return  The valid-operation set
     * 返回标识该通道支持的操作的操作集。
    套接字通道支持连接、读取和写入，因此该方法返回(SelectionKey)。OP_CONNECT | SelectionKey。OP_READ | SelectionKey.OP_WRITE)。
     */
    public final int validOps() {
        return (SelectionKey.OP_READ
                | SelectionKey.OP_WRITE
                | SelectionKey.OP_CONNECT);
    }


    // -- Socket-specific operations --

    /**
     * @throws  ConnectionPendingException
     *          If a non-blocking connect operation is already in progress on
     *          this channel
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          If a security manager has been installed and its
     *          {@link SecurityManager#checkListen checkListen} method denies
     *          the operation
     *
     * @since 1.7
     * 将通道的套接字绑定到本地地址。
    此方法用于在套接字和本地地址之间建立关联。一旦建立了关联，套接字将保持绑定，直到通道关闭。如果本地参数具有值null，那么套接字将绑定到自动分配的地址。
     */
    @Override
    public abstract SocketChannel bind(SocketAddress local)
        throws IOException;

    /**
     * @throws  UnsupportedOperationException           {@inheritDoc}
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     *
     * @since 1.7
     * 设置套接字选项的值。
     */
    @Override
    public abstract <T> SocketChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * Shutdown the connection for reading without closing the channel.
     *
     * <p> Once shutdown for reading then further reads on the channel will
     * return {@code -1}, the end-of-stream indication. If the input side of the
     * connection is already shutdown then invoking this method has no effect.
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
     * @since 1.7
     *
    在不关闭通道的情况下关闭读取连接。
    一旦停止读取，则通道上的进一步读取将返回-1，即流结束指示。如果连接的输入端已经关闭，则调用此方法无效。
     */
//    在不关闭通道的情况下关闭读取连接
    public abstract SocketChannel shutdownInput() throws IOException;

    /**
     * Shutdown the connection for writing without closing the channel.
     *
     * <p> Once shutdown for writing then further attempts to write to the
     * channel will throw {@link ClosedChannelException}. If the output side of
     * the connection is already shutdown then invoking this method has no
     * effect.
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
     * @since 1.7
     * 在不关闭通道的情况下关闭写入连接。
    一旦停止写入，然后进一步尝试写入通道将抛出ClosedChannelException。如果连接的输出端已经关闭，则调用此方法无效。
     */
//    在不关闭通道的情况下关闭写入连接
    public abstract SocketChannel shutdownOutput() throws IOException;

    /**
     * Retrieves a socket associated with this channel.
     *
     * <p> The returned object will not declare any public methods that are not
     * declared in the {@link java.net.Socket} class.  </p>
     *
     * @return  A socket associated with this channel
     * 检索与此通道关联的套接字。
    返回的对象不会声明套接字类中未声明的任何公共方法。
     */
    public abstract Socket socket();

    /**
     * Tells whether or not this channel's network socket is connected.告诉该通道的网络套接字是否连接。
     *
     * @return  <tt>true</tt> if, and only if, this channel's network socket
     *          is {@link #isOpen open} and connected
     */
    public abstract boolean isConnected();

    /**
     * Tells whether or not a connection operation is in progress on this
     * channel.告诉该通道上的连接操作是否正在进行。
     *
     * @return  <tt>true</tt> if, and only if, a connection operation has been
     *          initiated on this channel but not yet completed by invoking the
     *          {@link #finishConnect finishConnect} method
     */
    public abstract boolean isConnectionPending();

    /**
     * Connects this channel's socket.
     *
     * <p> If this channel is in non-blocking mode then an invocation of this
     * method initiates a non-blocking connection operation.  If the connection
     * is established immediately, as can happen with a local connection, then
     * this method returns <tt>true</tt>.  Otherwise this method returns
     * <tt>false</tt> and the connection operation must later be completed by
     * invoking the {@link #finishConnect finishConnect} method.
     *
     * <p> If this channel is in blocking mode then an invocation of this
     * method will block until the connection is established or an I/O error
     * occurs.
     *
     * <p> This method performs exactly the same security checks as the {@link
     * java.net.Socket} class.  That is, if a security manager has been
     * installed then this method verifies that its {@link
     * java.lang.SecurityManager#checkConnect checkConnect} method permits
     * connecting to the address and port number of the given remote endpoint.
     *
     * <p> This method may be invoked at any time.  If a read or write
     * operation upon this channel is invoked while an invocation of this
     * method is in progress then that operation will first block until this
     * invocation is complete.  If a connection attempt is initiated but fails,
     * that is, if an invocation of this method throws a checked exception,
     * then the channel will be closed.  </p>
     *
     * @param  remote
     *         The remote address to which this channel is to be connected
     *
     * @return  <tt>true</tt> if a connection was established,
     *          <tt>false</tt> if this channel is in non-blocking mode
     *          and the connection operation is in progress
     *
     * @throws  AlreadyConnectedException
     *          If this channel is already connected
     *
     * @throws  ConnectionPendingException
     *          If a non-blocking connection operation is already in progress
     *          on this channel
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the connect operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the connect operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  UnresolvedAddressException
     *          If the given remote address is not fully resolved
     *
     * @throws  UnsupportedAddressTypeException
     *          If the type of the given remote address is not supported
     *
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit access to the given remote endpoint
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          此频道的套接字连接。
    如果该通道处于非阻塞模式，则该方法的调用将启动非阻塞连接操作。如果立即建立连接(本地连接可能发生这种情况)，则此方法返回true。否则，此方法将返回false，稍后将通过调用finishConnect方法完成连接操作。
    如果该通道处于阻塞模式，那么该方法的调用将被阻塞，直到建立连接或发生I/O错误。
    此方法执行与套接字类完全相同的安全性检查。也就是说，如果安装了安全管理器，那么该方法将验证其checkConnect方法是否允许连接到给定远程端点的地址和端口号。
    此方法可以随时调用。如果在调用该方法的过程中调用该通道上的读或写操作，那么该操作将首先阻塞，直到该调用完成。如果启动了连接尝试，但失败了，也就是说，如果该方法的调用抛出一个检查异常，那么通道将被关闭。
     */
    public abstract boolean connect(SocketAddress remote) throws IOException;

    /**
     * Finishes the process of connecting a socket channel.
     *
     * <p> A non-blocking connection operation is initiated by placing a socket
     * channel in non-blocking mode and then invoking its {@link #connect
     * connect} method.  Once the connection is established, or the attempt has
     * failed, the socket channel will become connectable and this method may
     * be invoked to complete the connection sequence.  If the connection
     * operation failed then invoking this method will cause an appropriate
     * {@link java.io.IOException} to be thrown.
     *
     * <p> If this channel is already connected then this method will not block
     * and will immediately return <tt>true</tt>.  If this channel is in
     * non-blocking mode then this method will return <tt>false</tt> if the
     * connection process is not yet complete.  If this channel is in blocking
     * mode then this method will block until the connection either completes
     * or fails, and will always either return <tt>true</tt> or throw a checked
     * exception describing the failure.
     *
     * <p> This method may be invoked at any time.  If a read or write
     * operation upon this channel is invoked while an invocation of this
     * method is in progress then that operation will first block until this
     * invocation is complete.  If a connection attempt fails, that is, if an
     * invocation of this method throws a checked exception, then the channel
     * will be closed.  </p>
     *
     * @return  <tt>true</tt> if, and only if, this channel's socket is now
     *          connected
     *
     * @throws  NoConnectionPendingException
     *          If this channel is not connected and a connection operation
     *          has not been initiated
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the connect operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the connect operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          完成连接套接字通道的过程。
    将套接字通道置于非阻塞模式，然后调用其连接方法，从而启动非阻塞连接操作。一旦建立了连接，或者尝试失败，套接字通道将成为可连接的，可以调用此方法来完成连接序列。如果连接操作失败，那么调用此方法将导致抛出一个适当的IOException。
    如果该通道已经连接，那么该方法将不会阻塞，并将立即返回true。如果该通道处于非阻塞模式，那么如果连接过程尚未完成，该方法将返回false。如果该通道处于阻塞模式，则此方法将阻塞，直到连接完成或失败，并且将始终返回true或抛出一个描述失败的检查异常。
    此方法可以随时调用。如果在调用该方法的过程中调用该通道上的读或写操作，那么该操作将首先阻塞，直到该调用完成。如果连接尝试失败，也就是说，如果该方法的调用抛出一个检查异常，那么通道将被关闭。
     */
    public abstract boolean finishConnect() throws IOException;

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
     *
     * @since 1.7
     * 返回该通道的套接字连接的远程地址。
    当通道被绑定并连接到Internet协议套接字地址时，该方法的返回值是java.net.InetSocketAddress类型。
     */
    public abstract SocketAddress getRemoteAddress() throws IOException;

    // -- ByteChannel operations --

    /**
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     *          将该通道中的字节序列读入给定缓冲区的子序列。
    该方法的调用尝试从该通道读取最多r个字节，其中r是剩余给定缓冲数组指定子序列的字节总数，即，
    此时调用此方法。
    假设一个字节序列长度为n的阅读,在0 < = n < = r。到第一个数据(抵消).remaining()字节的顺序转移到缓冲区数据的(抵消),到下一个数据的[抵消+ 1].remaining()字节转移到缓冲区数据的偏移量+ 1,等等,直到整个字节序列转换成给定的缓冲区。尽可能多的字节被传输到每个缓冲区中，因此每个更新缓冲区的最终位置(除了最后一个更新的缓冲区之外)保证等于缓冲区的限制。
    此方法可以随时调用。如果另一个线程已经在该通道上启动了读取操作，那么该方法的调用将被阻塞，直到第一个操作完成。
     */
//    此方法可以随时调用。如果另一个线程已经在该通道上启动了读取操作，那么该方法的调用将被阻塞，直到第一个操作完成
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     *          将该通道中的字节序列读入给定的缓冲区。
    form c.read(dsts)的这种方法的调用行为与调用完全相同
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    /**
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     *          从给定缓冲区的子序列向该通道写入字节序列。
    尝试将最多r个字节写入这个通道，其中r是给定缓冲数组子序列中剩余的字节总数，即，
    此时调用此方法。
    假设一个字节序列长度n是写,其中0 < = n < = r。到第一个src(抵消).remaining()字节的序列从缓冲区写src(抵消),到下一个src[抵消+ 1].remaining()从缓冲区字节写src(抵消+ 1),等等,直到整个字节序列。从每个缓冲区写入尽可能多的字节，因此每个更新缓冲区的最终位置(除了最后一个更新的缓冲区之外)保证等于缓冲区的限制。
    除非另有规定，写入操作将只在写入所有请求的r字节之后返回。某些类型的通道，取决于它们的状态，可能只写一些字节，或者可能根本不写。例如，非阻塞模式下的套接字通道不能写入比套接字输出缓冲区中空闲的任何字节。
    此方法可以随时调用。如果另一个线程已经在该通道上启动了写操作，那么该方法的调用将被阻塞，直到第一个操作完成。
     */
//    如果另一个线程已经在该通道上启动了写操作，那么该方法的调用将被阻塞，直到第一个操作完成
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          If this channel is not yet connected
     *          从给定的缓冲区向该通道写入字节序列。
    form c.write(srcs)的此方法的调用与调用的行为完全相同
     */
    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

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
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
