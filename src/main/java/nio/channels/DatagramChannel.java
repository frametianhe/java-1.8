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
import java.net.ProtocolFamily;
import java.net.DatagramSocket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A selectable channel for datagram-oriented sockets.
 *
 * <p> A datagram channel is created by invoking one of the {@link #open open} methods
 * of this class. It is not possible to create a channel for an arbitrary,
 * pre-existing datagram socket. A newly-created datagram channel is open but not
 * connected. A datagram channel need not be connected in order for the {@link #send
 * send} and {@link #receive receive} methods to be used.  A datagram channel may be
 * connected, by invoking its {@link #connect connect} method, in order to
 * avoid the overhead of the security checks are otherwise performed as part of
 * every send and receive operation.  A datagram channel must be connected in
 * order to use the {@link #read(java.nio.ByteBuffer) read} and {@link
 * #write(java.nio.ByteBuffer) write} methods, since those methods do not
 * accept or return socket addresses.
 *
 * <p> Once connected, a datagram channel remains connected until it is
 * disconnected or closed.  Whether or not a datagram channel is connected may
 * be determined by invoking its {@link #isConnected isConnected} method.
 *
 * <p> Socket options are configured using the {@link #setOption(SocketOption,Object)
 * setOption} method. A datagram channel to an Internet Protocol socket supports
 * the following options:
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
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> Re-use address </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_BROADCAST SO_BROADCAST} </td>
 *     <td> Allow transmission of broadcast datagrams </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_TOS IP_TOS} </td>
 *     <td> The Type of Service (ToS) octet in the Internet Protocol (IP) header </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_IF IP_MULTICAST_IF} </td>
 *     <td> The network interface for Internet Protocol (IP) multicast datagrams </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_TTL
 *       IP_MULTICAST_TTL} </td>
 *     <td> The <em>time-to-live</em> for Internet Protocol (IP) multicast
 *       datagrams </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_LOOP
 *       IP_MULTICAST_LOOP} </td>
 *     <td> Loopback for Internet Protocol (IP) multicast datagrams </td>
 *   </tr>
 * </table>
 * </blockquote>
 * Additional (implementation specific) options may also be supported.
 *
 * <p> Datagram channels are safe for use by multiple concurrent threads.  They
 * support concurrent reading and writing, though at most one thread may be
 * reading and at most one thread may be writing at any given time.  </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 一个可选择的面向数据的通道。
通过调用这个类的开放方法之一创建一个数据报通道。不可能为任意的、已存在的数据报套接字创建通道。新创建的数据报通道是打开的，但没有连接。为了使用发送和接收方法，不需要连接数据报通道。通过调用它的连接方法，可以连接数据报通道，以避免安全检查的开销作为每个发送和接收操作的一部分。为了使用读和写方法，必须连接数据报通道，因为这些方法不接受或返回套接字地址。
一旦连接，数据报通道将一直连接到断开或关闭为止。是否连接数据报通道可以通过调用其isConnected方法来确定。
套接字选项使用setOption方法进行配置。Internet协议套接字的数据报通道支持以下选项:
SO_SNDBUF
套接字发送缓冲区的大小
SO_RCVBUF
套接字接收缓冲区的大小
SO_REUSEADDR
重用地址
SO_BROADCAST
允许传输广播数据报
IP_TOS
Internet协议头中的服务类型(ToS)八隅体
IP_MULTICAST_IF
Internet协议(IP)多播数据报的网络接口
IP_MULTICAST_TTL
互联网协议(IP)多播数据报的生存时间。
IP_MULTICAST_LOOP
网络协议(IP)多播数据报的回送
还可以支持其他(特定于实现的)选项。
对于多个并发线程来说，数据报通道是安全的。他们支持并发的阅读和写作，尽管最多一个线程可能正在阅读，而且最多一个线程在任何给定的时间都可以写作。
 */

public abstract class DatagramChannel
    extends AbstractSelectableChannel
    implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, MulticastChannel
{

    /**
     * Initializes a new instance of this class.
     *
     * @param  provider
     *         The provider that created this channel
     */
    protected DatagramChannel(SelectorProvider provider) {
        super(provider);
    }

    /**
     * Opens a datagram channel.
     *
     * <p> The new channel is created by invoking the {@link
     * java.nio.channels.spi.SelectorProvider#openDatagramChannel()
     * openDatagramChannel} method of the system-wide default {@link
     * java.nio.channels.spi.SelectorProvider} object.  The channel will not be
     * connected.
     *
     * <p> The {@link ProtocolFamily ProtocolFamily} of the channel's socket
     * is platform (and possibly configuration) dependent and therefore unspecified.
     * The {@link #open(ProtocolFamily) open} allows the protocol family to be
     * selected when opening a datagram channel, and should be used to open
     * datagram channels that are intended for Internet Protocol multicasting.
     *
     * @return  A new datagram channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个数据报频道。
    通过调用系统范围内默认SelectorProvider对象的openDatagramChannel方法创建新通道。通道将不会被连接。
    通道套接字的protocol族依赖于平台(可能还有配置)，因此未指定。open允许在打开数据报通道时选择协议族，并且应该用于打开用于Internet协议多播的数据报通道。
     */
    public static DatagramChannel open() throws IOException {
        return SelectorProvider.provider().openDatagramChannel();
    }

    /**
     * Opens a datagram channel.
     *
     * <p> The {@code family} parameter is used to specify the {@link
     * ProtocolFamily}. If the datagram channel is to be used for IP multicasting
     * then this should correspond to the address type of the multicast groups
     * that this channel will join.
     *
     * <p> The new channel is created by invoking the {@link
     * java.nio.channels.spi.SelectorProvider#openDatagramChannel(ProtocolFamily)
     * openDatagramChannel} method of the system-wide default {@link
     * java.nio.channels.spi.SelectorProvider} object.  The channel will not be
     * connected.
     *
     * @param   family
     *          The protocol family
     *
     * @return  A new datagram channel
     *
     * @throws  UnsupportedOperationException
     *          If the specified protocol family is not supported. For example,
     *          suppose the parameter is specified as {@link
     *          java.net.StandardProtocolFamily#INET6 StandardProtocolFamily.INET6}
     *          but IPv6 is not enabled on the platform.
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @since   1.7
     * 打开一个数据报频道。
    家庭参数用于指定ProtocolFamily。如果要将数据报通道用于IP多播，那么它应该对应于该通道将加入的多播组的地址类型。
    通过调用系统范围内默认SelectorProvider对象的openDatagramChannel方法创建新通道。通道将不会被连接。
     */
    public static DatagramChannel open(ProtocolFamily family) throws IOException {
        return SelectorProvider.provider().openDatagramChannel(family);
    }

    /**
     * Returns an operation set identifying this channel's supported
     * operations.
     *
     * <p> Datagram channels support reading and writing, so this method
     * returns <tt>(</tt>{@link SelectionKey#OP_READ} <tt>|</tt>&nbsp;{@link
     * SelectionKey#OP_WRITE}<tt>)</tt>.  </p>
     *
     * @return  The valid-operation set
     * 返回标识该通道支持的操作的操作集。
    数据报通道支持读取和写入，因此该方法返回(SelectionKey)。OP_READ | SelectionKey.OP_WRITE)。
     */
    public final int validOps() {
        return (SelectionKey.OP_READ
                | SelectionKey.OP_WRITE);
    }


    // -- Socket-specific operations --

    /**
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          If a security manager has been installed and its {@link
     *          SecurityManager#checkListen checkListen} method denies the
     *          operation
     *
     * @since 1.7
     * 将通道的套接字绑定到本地地址。
    此方法用于在套接字和本地地址之间建立关联。一旦建立了关联，套接字将保持绑定，直到通道关闭。如果本地参数具有值null，那么套接字将绑定到自动分配的地址。
     */
    public abstract DatagramChannel bind(SocketAddress local)
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
    public abstract <T> DatagramChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * Retrieves a datagram socket associated with this channel.
     *
     * <p> The returned object will not declare any public methods that are not
     * declared in the {@link java.net.DatagramSocket} class.  </p>
     *
     * @return  A datagram socket associated with this channel
     * 检索与此通道关联的数据报套接字。
    返回的对象不会声明任何未在DatagramSocket类中声明的公共方法。
     */
    public abstract DatagramSocket socket();

    /**
     * Tells whether or not this channel's socket is connected.告诉该通道的套接字是否连接。
     *
     * @return  {@code true} if, and only if, this channel's socket
     *          is {@link #isOpen open} and connected
     */
    public abstract boolean isConnected();

    /**
     * Connects this channel's socket.
     *
     * <p> The channel's socket is configured so that it only receives
     * datagrams from, and sends datagrams to, the given remote <i>peer</i>
     * address.  Once connected, datagrams may not be received from or sent to
     * any other address.  A datagram socket remains connected until it is
     * explicitly disconnected or until it is closed.
     *
     * <p> This method performs exactly the same security checks as the {@link
     * java.net.DatagramSocket#connect connect} method of the {@link
     * java.net.DatagramSocket} class.  That is, if a security manager has been
     * installed then this method verifies that its {@link
     * java.lang.SecurityManager#checkAccept checkAccept} and {@link
     * java.lang.SecurityManager#checkConnect checkConnect} methods permit
     * datagrams to be received from and sent to, respectively, the given
     * remote address.
     *
     * <p> This method may be invoked at any time.  It will not have any effect
     * on read or write operations that are already in progress at the moment
     * that it is invoked. If this channel's socket is not bound then this method
     * will first cause the socket to be bound to an address that is assigned
     * automatically, as if invoking the {@link #bind bind} method with a
     * parameter of {@code null}. </p>
     *
     * @param  remote
     *         The remote address to which this channel is to be connected
     *
     * @return  This datagram channel
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
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit access to the given remote address
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          此频道的套接字连接。
    通道的套接字被配置为只接收来自给定远程对等地址的数据报，并将数据报发送到给定的远程对等地址。一旦连接，数据报可能不会收到或发送到任何其他地址。数据报套接字仍然保持连接，直到它被显式地断开或关闭为止。
    此方法执行与DatagramSocket类的connect方法完全相同的安全性检查。也就是说，如果安装了安全管理器，那么该方法将验证其checkAccept和checkConnect方法是否允许分别从给定的远程地址接收和发送数据报。
    此方法可以随时调用。它不会对调用它时已经在进行中的读写操作产生任何影响。如果该通道的套接字没有绑定，那么该方法将首先将套接字绑定到自动分配的地址，就像调用绑定方法时使用的参数为null一样。
     */
    public abstract DatagramChannel connect(SocketAddress remote)
        throws IOException;

    /**
     * Disconnects this channel's socket.
     *
     * <p> The channel's socket is configured so that it can receive datagrams
     * from, and sends datagrams to, any remote address so long as the security
     * manager, if installed, permits it.
     *
     * <p> This method may be invoked at any time.  It will not have any effect
     * on read or write operations that are already in progress at the moment
     * that it is invoked.
     *
     * <p> If this channel's socket is not connected, or if the channel is
     * closed, then invoking this method has no effect.  </p>
     *
     * @return  This datagram channel
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          断开该频道的套接字。
    通道的套接字被配置为可以从任何远程地址接收数据报，并向任何远程地址发送数据报，只要安全管理器(如果安装的话)允许。
    此方法可以随时调用。它不会对调用它时已经在进行中的读写操作产生任何影响。
    如果该通道的套接字未连接，或者该通道已关闭，则调用此方法无效。
     */
    public abstract DatagramChannel disconnect() throws IOException;

    /**
     * Returns the remote address to which this channel's socket is connected.
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
     */
    public abstract SocketAddress getRemoteAddress() throws IOException;

    /**
     * Receives a datagram via this channel.
     *
     * <p> If a datagram is immediately available, or if this channel is in
     * blocking mode and one eventually becomes available, then the datagram is
     * copied into the given byte buffer and its source address is returned.
     * If this channel is in non-blocking mode and a datagram is not
     * immediately available then this method immediately returns
     * <tt>null</tt>.
     *
     * <p> The datagram is transferred into the given byte buffer starting at
     * its current position, as if by a regular {@link
     * ReadableByteChannel#read(java.nio.ByteBuffer) read} operation.  If there
     * are fewer bytes remaining in the buffer than are required to hold the
     * datagram then the remainder of the datagram is silently discarded.
     *
     * <p> This method performs exactly the same security checks as the {@link
     * java.net.DatagramSocket#receive receive} method of the {@link
     * java.net.DatagramSocket} class.  That is, if the socket is not connected
     * to a specific remote address and a security manager has been installed
     * then for each datagram received this method verifies that the source's
     * address and port number are permitted by the security manager's {@link
     * java.lang.SecurityManager#checkAccept checkAccept} method.  The overhead
     * of this security check can be avoided by first connecting the socket via
     * the {@link #connect connect} method.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a read operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. If this channel's socket is not bound then this method will
     * first cause the socket to be bound to an address that is assigned
     * automatically, as if invoking the {@link #bind bind} method with a
     * parameter of {@code null}. </p>
     *
     * @param  dst
     *         The buffer into which the datagram is to be transferred
     *
     * @return  The datagram's source address,
     *          or <tt>null</tt> if this channel is in non-blocking mode
     *          and no datagram was immediately available
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
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit datagrams to be accepted
     *          from the datagram's sender
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          通过此通道接收数据报。
    如果一个数据报是即时可用的，或者这个通道处于阻塞模式，并且最终有一个可用，那么数据报将被复制到给定的字节缓冲区中，并且返回它的源地址。如果该通道处于非阻塞模式，且数据报不可立即使用，则此方法立即返回null。
    数据报从当前位置开始传输到给定的字节缓冲区，就像通过常规的读取操作一样。如果缓冲区中剩余的字节比保存数据报所需的字节数少，那么数据报的其余部分将被静静地丢弃。
    此方法执行与DatagramSocket类的receive方法完全相同的安全性检查。也就是说，如果套接字没有连接到特定的远程地址，并且安装了安全管理器，那么对于每个接收到的数据报，此方法将验证安全管理器的checkAccept方法是否允许源地址和端口号。通过首先通过connect方法连接套接字，可以避免这种安全检查的开销。
    此方法可以随时调用。如果另一个线程已经在该通道上启动了读取操作，那么该方法的调用将被阻塞，直到第一个操作完成。如果该通道的套接字没有绑定，那么该方法将首先将套接字绑定到自动分配的地址，就像调用绑定方法时使用的参数为null一样。
     */
    public abstract SocketAddress receive(ByteBuffer dst) throws IOException;

    /**
     * Sends a datagram via this channel.
     *
     * <p> If this channel is in non-blocking mode and there is sufficient room
     * in the underlying output buffer, or if this channel is in blocking mode
     * and sufficient room becomes available, then the remaining bytes in the
     * given buffer are transmitted as a single datagram to the given target
     * address.
     *
     * <p> The datagram is transferred from the byte buffer as if by a regular
     * {@link WritableByteChannel#write(java.nio.ByteBuffer) write} operation.
     *
     * <p> This method performs exactly the same security checks as the {@link
     * java.net.DatagramSocket#send send} method of the {@link
     * java.net.DatagramSocket} class.  That is, if the socket is not connected
     * to a specific remote address and a security manager has been installed
     * then for each datagram sent this method verifies that the target address
     * and port number are permitted by the security manager's {@link
     * java.lang.SecurityManager#checkConnect checkConnect} method.  The
     * overhead of this security check can be avoided by first connecting the
     * socket via the {@link #connect connect} method.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a write operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. If this channel's socket is not bound then this method will
     * first cause the socket to be bound to an address that is assigned
     * automatically, as if by invoking the {@link #bind bind} method with a
     * parameter of {@code null}. </p>
     *
     * @param  src
     *         The buffer containing the datagram to be sent
     *
     * @param  target
     *         The address to which the datagram is to be sent
     *
     * @return   The number of bytes sent, which will be either the number
     *           of bytes that were remaining in the source buffer when this
     *           method was invoked or, if this channel is non-blocking, may be
     *           zero if there was insufficient room for the datagram in the
     *           underlying output buffer
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
     * @throws  SecurityException
     *          If a security manager has been installed
     *          and it does not permit datagrams to be sent
     *          to the given address
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          通过该通道发送数据报。
    如果该通道处于非阻塞模式，并且底层输出缓冲区中有足够的空间，或者如果该通道处于阻塞模式，并且有足够的空间可用，那么给定缓冲区中的剩余字节将作为单个数据报传输到给定的目标地址。
    数据报从字节缓冲区传输，就像通过常规的写操作一样。
    此方法执行与DatagramSocket类的send方法完全相同的安全性检查。也就是说，如果套接字没有连接到特定的远程地址，并且已经安装了安全管理器，那么对于每个发送的数据报，此方法将验证安全管理器的checkConnect方法是否允许目标地址和端口号。通过首先通过connect方法连接套接字，可以避免这种安全检查的开销。
    此方法可以随时调用。如果另一个线程已经在该通道上启动了写操作，那么该方法的调用将被阻塞，直到第一个操作完成。如果该通道的套接字未被绑定，那么该方法将首先将套接字绑定到自动分配的地址，就像使用null参数调用bind方法一样。
     */
    public abstract int send(ByteBuffer src, SocketAddress target)
        throws IOException;


    // -- ByteChannel operations --

    /**
     * Reads a datagram from this channel.
     *
     * <p> This method may only be invoked if this channel's socket is
     * connected, and it only accepts datagrams from the socket's peer.  If
     * there are more bytes in the datagram than remain in the given buffer
     * then the remainder of the datagram is silently discarded.  Otherwise
     * this method behaves exactly as specified in the {@link
     * ReadableByteChannel} interface.  </p>
     *
     * @throws  NotYetConnectedException
     *          If this channel's socket is not connected
     *          从该通道读取数据报。
    此方法只能在连接该通道的套接字时调用，并且它只接受来自套接字对等点的数据报。如果数据报中的字节数比给定缓冲区中的字节数多，那么该数据报的其余部分将被静静地丢弃。否则，此方法的行为与ReadableByteChannel接口中指定的完全一样。
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * Reads a datagram from this channel.
     *
     * <p> This method may only be invoked if this channel's socket is
     * connected, and it only accepts datagrams from the socket's peer.  If
     * there are more bytes in the datagram than remain in the given buffers
     * then the remainder of the datagram is silently discarded.  Otherwise
     * this method behaves exactly as specified in the {@link
     * ScatteringByteChannel} interface.  </p>
     *
     * @throws  NotYetConnectedException
     *          If this channel's socket is not connected
     *          从该通道读取数据报。
    此方法只能在连接该通道的套接字时调用，并且它只接受来自套接字对等点的数据报。如果数据报中的字节数比给定缓冲区中的字节数多，那么该数据报的其余部分将被静静地丢弃。否则，此方法的行为与散在bytechannel接口中指定的完全相同。
     */
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * Reads a datagram from this channel.
     *
     * <p> This method may only be invoked if this channel's socket is
     * connected, and it only accepts datagrams from the socket's peer.  If
     * there are more bytes in the datagram than remain in the given buffers
     * then the remainder of the datagram is silently discarded.  Otherwise
     * this method behaves exactly as specified in the {@link
     * ScatteringByteChannel} interface.  </p>
     *
     * @throws  NotYetConnectedException
     *          If this channel's socket is not connected
     *          从该通道读取数据报。
    此方法只能在连接该通道的套接字时调用，并且它只接受来自套接字对等点的数据报。如果数据报中的字节数比给定缓冲区中的字节数多，那么该数据报的其余部分将被静静地丢弃。否则，此方法的行为与散在bytechannel接口中指定的完全相同。
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    /**
     * Writes a datagram to this channel.
     *
     * <p> This method may only be invoked if this channel's socket is
     * connected, in which case it sends datagrams directly to the socket's
     * peer.  Otherwise it behaves exactly as specified in the {@link
     * WritableByteChannel} interface.  </p>
     *
     * @throws  NotYetConnectedException
     *          If this channel's socket is not connected
     *          将数据报写入该通道。
    只有连接了通道的套接字，才可以调用该方法，在这种情况下，它将数据报直接发送到套接字的对等点。否则，它的行为与WritableByteChannel接口中指定的完全一样。
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * Writes a datagram to this channel.
     *
     * <p> This method may only be invoked if this channel's socket is
     * connected, in which case it sends datagrams directly to the socket's
     * peer.  Otherwise it behaves exactly as specified in the {@link
     * GatheringByteChannel} interface.  </p>
     *
     * @return   The number of bytes sent, which will be either the number
     *           of bytes that were remaining in the source buffer when this
     *           method was invoked or, if this channel is non-blocking, may be
     *           zero if there was insufficient room for the datagram in the
     *           underlying output buffer
     *
     * @throws  NotYetConnectedException
     *          If this channel's socket is not connected
     *          将数据报写入该通道。
    只有连接了通道的套接字，才可以调用该方法，在这种情况下，它将数据报直接发送到套接字的对等点。否则，它的行为就像在采集技术中所指定的那样。
     */
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

    /**
     * Writes a datagram to this channel.
     *
     * <p> This method may only be invoked if this channel's socket is
     * connected, in which case it sends datagrams directly to the socket's
     * peer.  Otherwise it behaves exactly as specified in the {@link
     * GatheringByteChannel} interface.  </p>
     *
     * @return   The number of bytes sent, which will be either the number
     *           of bytes that were remaining in the source buffer when this
     *           method was invoked or, if this channel is non-blocking, may be
     *           zero if there was insufficient room for the datagram in the
     *           underlying output buffer
     *
     * @throws  NotYetConnectedException
     *          If this channel's socket is not connected
     *          将数据报写入该通道。
    只有连接了通道的套接字，才可以调用该方法，在这种情况下，它将数据报直接发送到套接字的对等点。否则，它的行为就像在采集技术中所指定的那样。
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
