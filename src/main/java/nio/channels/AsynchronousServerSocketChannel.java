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
import java.net.SocketOption;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.io.IOException;

/**
 * An asynchronous channel for stream-oriented listening sockets.
 *
 * <p> An asynchronous server-socket channel is created by invoking the
 * {@link #open open} method of this class.
 * A newly-created asynchronous server-socket channel is open but not yet bound.
 * It can be bound to a local address and configured to listen for connections
 * by invoking the {@link #bind(SocketAddress,int) bind} method. Once bound,
 * the {@link #accept(Object,CompletionHandler) accept} method
 * is used to initiate the accepting of connections to the channel's socket.
 * An attempt to invoke the <tt>accept</tt> method on an unbound channel will
 * cause a {@link NotYetBoundException} to be thrown.
 *
 * <p> Channels of this type are safe for use by multiple concurrent threads
 * though at most one accept operation can be outstanding at any time.
 * If a thread initiates an accept operation before a previous accept operation
 * has completed then an {@link AcceptPendingException} will be thrown.
 *
 * <p> Socket options are configured using the {@link #setOption(SocketOption,Object)
 * setOption} method. Channels of this type support the following options:
 * <blockquote>
 * <table border summary="Socket options">
 *   <tr>
 *     <th>Option Name</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_RCVBUF SO_RCVBUF} </td>
 *     <td> The size of the socket receive buffer </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> Re-use address </td>
 *   </tr>
 * </table>
 * </blockquote>
 * Additional (implementation specific) options may also be supported.
 *
 * <p> <b>Usage Example:</b>
 * <pre>
 *  final AsynchronousServerSocketChannel listener =
 *      AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(5000));
 *
 *  listener.accept(null, new CompletionHandler&lt;AsynchronousSocketChannel,Void&gt;() {
 *      public void completed(AsynchronousSocketChannel ch, Void att) {
 *          // accept the next connection
 *          listener.accept(null, this);
 *
 *          // handle this connection
 *          handle(ch);
 *      }
 *      public void failed(Throwable exc, Void att) {
 *          ...
 *      }
 *  });
 * </pre>
 *
 * @since 1.7
 * 面向流的监听套接字的异步通道。
通过调用该类的open方法创建一个异步服务器-套接字通道。新创建的异步服务器-套接字通道是打开的，但尚未绑定。它可以绑定到本地地址，并通过调用bind方法侦听连接。一旦绑定，accept方法用于初始化接受到通道套接字的连接。尝试在未绑定的通道上调用accept方法将导致抛出NotYetBoundException。
这种类型的通道对于多个并发线程来说是安全的，尽管最多一次接受操作在任何时候都是未执行的。如果一个线程在先前的接受操作完成之前启动了一个接受操作，那么就会抛出一个AcceptPendingException。
套接字选项使用setOption方法进行配置。这种类型的通道支持以下选项:
SO_RCVBUF
套接字接收缓冲区的大小
SO_REUSEADDR
重用地址
还可以支持其他(特定于实现的)选项。
 */

public abstract class AsynchronousServerSocketChannel
    implements AsynchronousChannel, NetworkChannel
{
    private final AsynchronousChannelProvider provider;

    /**
     * Initializes a new instance of this class.
     *
     * @param  provider
     *         The provider that created this channel
     */
    protected AsynchronousServerSocketChannel(AsynchronousChannelProvider provider) {
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
     * Opens an asynchronous server-socket channel.
     *
     * <p> The new channel is created by invoking the {@link
     * java.nio.channels.spi.AsynchronousChannelProvider#openAsynchronousServerSocketChannel
     * openAsynchronousServerSocketChannel} method on the {@link
     * java.nio.channels.spi.AsynchronousChannelProvider} object that created
     * the given group. If the group parameter is <tt>null</tt> then the
     * resulting channel is created by the system-wide default provider, and
     * bound to the <em>default group</em>.
     *
     * @param   group
     *          The group to which the newly constructed channel should be bound,
     *          or <tt>null</tt> for the default group
     *
     * @return  A new asynchronous server socket channel
     *
     * @throws  ShutdownChannelGroupException
     *          If the channel group is shutdown
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个异步服务器-套接字通道。
    新通道是通过在创建给定组的AsynchronousChannelProvider对象上调用openAsynchronousServerSocketChannel方法创建的。如果组参数为null，那么生成的通道将由系统范围的默认提供程序创建，并绑定到默认组。
     */
    public static AsynchronousServerSocketChannel open(AsynchronousChannelGroup group)
        throws IOException
    {
        AsynchronousChannelProvider provider = (group == null) ?
            AsynchronousChannelProvider.provider() : group.provider();
        return provider.openAsynchronousServerSocketChannel(group);
    }

    /**
     * Opens an asynchronous server-socket channel.
     *
     * <p> This method returns an asynchronous server socket channel that is
     * bound to the <em>default group</em>. This method is equivalent to evaluating
     * the expression:
     * <blockquote><pre>
     * open((AsynchronousChannelGroup)null);
     * </pre></blockquote>
     *
     * @return  A new asynchronous server socket channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public static AsynchronousServerSocketChannel open()
        throws IOException
    {
        return open(null);
    }

    /**
     * Binds the channel's socket to a local address and configures the socket to
     * listen for connections.将通道的套接字绑定到本地地址，并配置套接字以侦听连接。
     *
     * <p> An invocation of this method is equivalent to the following:
     * <blockquote><pre>
     * bind(local, 0);
     * </pre></blockquote>
     *
     * @param   local
     *          The local address to bind the socket, or <tt>null</tt> to bind
     *          to an automatically assigned socket address
     *
     * @return  This channel
     *
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  SecurityException                   {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     */
    public final AsynchronousServerSocketChannel bind(SocketAddress local)
        throws IOException
    {
        return bind(local, 0);
    }

    /**
     * Binds the channel's socket to a local address and configures the socket to
     * listen for connections.
     *
     * <p> This method is used to establish an association between the socket and
     * a local address. Once an association is established then the socket remains
     * bound until the associated channel is closed.
     *
     * <p> The {@code backlog} parameter is the maximum number of pending
     * connections on the socket. Its exact semantics are implementation specific.
     * In particular, an implementation may impose a maximum length or may choose
     * to ignore the parameter altogther. If the {@code backlog} parameter has
     * the value {@code 0}, or a negative value, then an implementation specific
     * default is used.
     *
     * @param   local
     *          The local address to bind the socket, or {@code null} to bind
     *          to an automatically assigned socket address
     * @param   backlog
     *          The maximum number of pending connections
     *
     * @return  This channel
     *
     * @throws  AlreadyBoundException
     *          If the socket is already bound
     * @throws  UnsupportedAddressTypeException
     *          If the type of the given address is not supported
     * @throws  SecurityException
     *          If a security manager has been installed and its {@link
     *          SecurityManager#checkListen checkListen} method denies the operation
     * @throws  ClosedChannelException
     *          If the channel is closed
     * @throws  IOException
     *          If some other I/O error occurs将通道的套接字绑定到本地地址，并配置套接字以侦听连接。
    此方法用于在套接字和本地地址之间建立关联。一旦建立了关联，套接字将保持绑定，直到关闭关联的通道。
    backlog参数是套接字上挂起连接的最大数量。它的确切语义是特定于实现的。特别是，实现可能会强制设置最大长度，或者选择忽略参数altogther。如果backlog参数的值为0或负值，则使用特定于实现的缺省值。
     */
    public abstract AsynchronousServerSocketChannel bind(SocketAddress local, int backlog)
        throws IOException;

    /**
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     */
    public abstract <T> AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * Accepts a connection.
     *
     * <p> This method initiates an asynchronous operation to accept a
     * connection made to this channel's socket. The {@code handler} parameter is
     * a completion handler that is invoked when a connection is accepted (or
     * the operation fails). The result passed to the completion handler is
     * the {@link AsynchronousSocketChannel} to the new connection.
     *
     * <p> When a new connection is accepted then the resulting {@code
     * AsynchronousSocketChannel} will be bound to the same {@link
     * AsynchronousChannelGroup} as this channel. If the group is {@link
     * AsynchronousChannelGroup#isShutdown shutdown} and a connection is accepted,
     * then the connection is closed, and the operation completes with an {@code
     * IOException} and cause {@link ShutdownChannelGroupException}.
     *
     * <p> To allow for concurrent handling of new connections, the completion
     * handler is not invoked directly by the initiating thread when a new
     * connection is accepted immediately (see <a
     * href="AsynchronousChannelGroup.html#threading">Threading</a>).
     *
     * <p> If a security manager has been installed then it verifies that the
     * address and port number of the connection's remote endpoint are permitted
     * by the security manager's {@link SecurityManager#checkAccept checkAccept}
     * method. The permission check is performed with privileges that are restricted
     * by the calling context of this method. If the permission check fails then
     * the connection is closed and the operation completes with a {@link
     * SecurityException}.
     *
     * @param   <A>
     *          The type of the attachment
     * @param   attachment
     *          The object to attach to the I/O operation; can be {@code null}
     * @param   handler
     *          The handler for consuming the result
     *
     * @throws  AcceptPendingException
     *          If an accept operation is already in progress on this channel
     * @throws  NotYetBoundException
     *          If this channel's socket has not yet been bound
     * @throws  ShutdownChannelGroupException
     *          If the channel group has terminated接受一个连接。
    此方法启动异步操作以接受到此通道套接字的连接。处理程序参数是一个完成处理程序，当连接被接受(或操作失败)时调用该处理程序。传递给完成处理程序的结果是新连接的asynoussocketchannel。
    当接受新连接时，所生成的asynoussocketchannel将绑定到与此通道相同的asynouschannelgroup。如果组已关闭且连接已被接受，则连接将关闭，操作将使用IOException完成，并导致ShutdownChannelGroupException。
    为了允许并发处理新连接，当新连接被立即接受时，初始线程不会直接调用完成处理程序(请参阅线程)。
    如果已经安装了安全管理器，那么它将验证安全管理器的checkAccept方法是否允许连接的远程端点的地址和端口号。使用此方法的调用上下文所限制的特权执行权限检查。如果权限检查失败，则连接将关闭，操作将使用SecurityException完成。
     */
    public abstract <A> void accept(A attachment,
                                    CompletionHandler<AsynchronousSocketChannel,? super A> handler);

    /**
     * Accepts a connection.
     *
     * <p> This method initiates an asynchronous operation to accept a
     * connection made to this channel's socket. The method behaves in exactly
     * the same manner as the {@link #accept(Object, CompletionHandler)} method
     * except that instead of specifying a completion handler, this method
     * returns a {@code Future} representing the pending result. The {@code
     * Future}'s {@link Future#get() get} method returns the {@link
     * AsynchronousSocketChannel} to the new connection on successful completion.
     *
     * @return  a {@code Future} object representing the pending result
     *
     * @throws  AcceptPendingException
     *          If an accept operation is already in progress on this channel
     * @throws  NotYetBoundException
     *          If this channel's socket has not yet been bound接受一个连接。
    此方法启动异步操作以接受到此通道套接字的连接。该方法的行为方式与accept(Object, CompletionHandler)方法完全相同，只是该方法没有指定完成处理程序，而是返回一个表示挂起结果的Future。将来的get方法在成功完成后将asynoussocketchannel返回给新连接。
     */
    public abstract Future<AsynchronousSocketChannel> accept();

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
     * @throws  IOException                {@inheritDoc}返回此通道的套接字绑定到的套接字地址。
    当通道绑定到Internet协议套接字地址时，此方法的返回值类型为java.net.InetSocketAddress。
    如果存在安全管理器集，则使用本地地址和-1作为参数调用其checkConnect方法，以查看是否允许该操作。如果不允许该操作，则返回一个套接字套接字的套接字地址和本地端口。
     */
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;
}
