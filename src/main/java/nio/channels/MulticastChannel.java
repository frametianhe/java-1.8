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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.io.IOException;
import java.net.ProtocolFamily;             // javadoc
import java.net.StandardProtocolFamily;     // javadoc
import java.net.StandardSocketOptions;      // javadoc

/**
 * A network channel that supports Internet Protocol (IP) multicasting.
 *
 * <p> IP multicasting is the transmission of IP datagrams to members of
 * a <em>group</em> that is zero or more hosts identified by a single destination
 * address.
 *
 * <p> In the case of a channel to an {@link StandardProtocolFamily#INET IPv4} socket,
 * the underlying operating system supports <a href="http://www.ietf.org/rfc/rfc2236.txt">
 * <i>RFC&nbsp;2236: Internet Group Management Protocol, Version 2 (IGMPv2)</i></a>.
 * It may optionally support source filtering as specified by <a
 * href="http://www.ietf.org/rfc/rfc3376.txt"> <i>RFC&nbsp;3376: Internet Group
 * Management Protocol, Version 3 (IGMPv3)</i></a>.
 * For channels to an {@link StandardProtocolFamily#INET6 IPv6} socket, the equivalent
 * standards are <a href="http://www.ietf.org/rfc/rfc2710.txt"> <i>RFC&nbsp;2710:
 * Multicast Listener Discovery (MLD) for IPv6</i></a> and <a
 * href="http://www.ietf.org/rfc/rfc3810.txt"> <i>RFC&nbsp;3810: Multicast Listener
 * Discovery Version 2 (MLDv2) for IPv6</i></a>.
 *
 * <p> The {@link #join(InetAddress,NetworkInterface)} method is used to
 * join a group and receive all multicast datagrams sent to the group. A channel
 * may join several multicast groups and may join the same group on several
 * {@link NetworkInterface interfaces}. Membership is dropped by invoking the {@link
 * MembershipKey#drop drop} method on the returned {@link MembershipKey}. If the
 * underlying platform supports source filtering then the {@link MembershipKey#block
 * block} and {@link MembershipKey#unblock unblock} methods can be used to block or
 * unblock multicast datagrams from particular source addresses.
 *
 * <p> The {@link #join(InetAddress,NetworkInterface,InetAddress)} method
 * is used to begin receiving datagrams sent to a group whose source address matches
 * a given source address. This method throws {@link UnsupportedOperationException}
 * if the underlying platform does not support source filtering.  Membership is
 * <em>cumulative</em> and this method may be invoked again with the same group
 * and interface to allow receiving datagrams from other source addresses. The
 * method returns a {@link MembershipKey} that represents membership to receive
 * datagrams from the given source address. Invoking the key's {@link
 * MembershipKey#drop drop} method drops membership so that datagrams from the
 * source address can no longer be received.
 *
 * <h2>Platform dependencies</h2>
 *
 * The multicast implementation is intended to map directly to the native
 * multicasting facility. Consequently, the following items should be considered
 * when developing an application that receives IP multicast datagrams:
 *
 * <ol>
 *
 * <li><p> The creation of the channel should specify the {@link ProtocolFamily}
 * that corresponds to the address type of the multicast groups that the channel
 * will join. There is no guarantee that a channel to a socket in one protocol
 * family can join and receive multicast datagrams when the address of the
 * multicast group corresponds to another protocol family. For example, it is
 * implementation specific if a channel to an {@link StandardProtocolFamily#INET6 IPv6}
 * socket can join an {@link StandardProtocolFamily#INET IPv4} multicast group and receive
 * multicast datagrams sent to the group. </p></li>
 *
 * <li><p> The channel's socket should be bound to the {@link
 * InetAddress#isAnyLocalAddress wildcard} address. If the socket is bound to
 * a specific address, rather than the wildcard address then it is implementation
 * specific if multicast datagrams are received by the socket. </p></li>
 *
 * <li><p> The {@link StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} option should be
 * enabled prior to {@link NetworkChannel#bind binding} the socket. This is
 * required to allow multiple members of the group to bind to the same
 * address. </p></li>
 *
 * </ol>
 *
 * <p> <b>Usage Example:</b>
 * <pre>
 *     // join multicast group on this interface, and also use this
 *     // interface for outgoing multicast datagrams
 *     NetworkInterface ni = NetworkInterface.getByName("hme0");
 *
 *     DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
 *         .setOption(StandardSocketOptions.SO_REUSEADDR, true)
 *         .bind(new InetSocketAddress(5000))
 *         .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
 *
 *     InetAddress group = InetAddress.getByName("225.4.5.6");
 *
 *     MembershipKey key = dc.join(group, ni);
 * </pre>
 *
 * @since 1.7
 * 支持Internet协议(IP)多播的网络通道。
IP多播是指将IP数据报传输到由一个目标地址标识为零或多个主机的组的成员。
对于IPv4套接字的通道，底层操作系统支持RFC 2236: Internet组管理协议，版本2 (IGMPv2)。它可以根据RFC 3376: Internet Group Management Protocol, Version 3 (IGMPv3)指定可选地支持源过滤。对于IPv6套接字的通道，等效的标准是RFC 2710: IPv6的多播侦听器发现(MLD)和RFC 3810: IPv6的多播侦听器发现版本2 (MLDv2)。
join(InetAddress, NetworkInterface)方法用于加入一个组，并接收发送给组的所有组播数据报。通道可以连接几个多播组，也可以在几个接口上加入同一个组。通过调用返回的MembershipKey上的drop方法来删除成员资格。如果底层平台支持源过滤，那么块和解块方法可以用于从特定源地址阻塞或解阻塞多播数据报。
join(InetAddress、NetworkInterface、InetAddress)方法用于开始接收发送到源地址与给定源地址匹配的组的数据报。如果底层平台不支持源过滤，此方法将抛出UnsupportedOperationException。成员关系是累积的，可以使用相同的组和接口再次调用此方法，以允许从其他源地址接收数据报。该方法返回一个MembershipKey，它表示从给定的源地址接收数据报的成员。调用密钥的drop方法会降低成员身份，因此无法接收来自源地址的数据报。
 */
//
public interface MulticastChannel
    extends NetworkChannel
{
    /**
     * Closes this channel.
     *
     * <p> If the channel is a member of a multicast group then the membership
     * is {@link MembershipKey#drop dropped}. Upon return, the {@link
     * MembershipKey membership-key} will be {@link MembershipKey#isValid
     * invalid}.
     *
     * <p> This method otherwise behaves exactly as specified by the {@link
     * Channel} interface.
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          关闭这个通道。
    如果通道是多播组的成员，那么成员关系将被删除。返回时，成员密钥无效。
    此方法的行为与通道接口指定的完全相同。
     */
    @Override void close() throws IOException;

    /**
     * Joins a multicast group to begin receiving all datagrams sent to the group,
     * returning a membership key.
     *
     * <p> If this channel is currently a member of the group on the given
     * interface to receive all datagrams then the membership key, representing
     * that membership, is returned. Otherwise this channel joins the group and
     * the resulting new membership key is returned. The resulting membership key
     * is not {@link MembershipKey#sourceAddress source-specific}.
     *
     * <p> A multicast channel may join several multicast groups, including
     * the same group on more than one interface. An implementation may impose a
     * limit on the number of groups that may be joined at the same time.
     *
     * @param   group
     *          The multicast address to join
     * @param   interf
     *          The network interface on which to join the group
     *
     * @return  The membership key
     *
     * @throws  IllegalArgumentException
     *          If the group parameter is not a {@link InetAddress#isMulticastAddress
     *          multicast} address, or the group parameter is an address type
     *          that is not supported by this channel
     * @throws  IllegalStateException
     *          If the channel already has source-specific membership of the
     *          group on the interface
     * @throws  UnsupportedOperationException
     *          If the channel's socket is not an Internet Protocol socket
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is set, and its
     *          {@link SecurityManager#checkMulticast(InetAddress) checkMulticast}
     *          method denies access to the multiast group
     *          加入多播组，开始接收发送到组的所有数据报，并返回成员键。
    如果该通道当前是给定接口上接收所有数据报的组的成员，则返回代表该成员关系的成员关系键。否则，该通道将加入组，并返回生成的新成员关系键。结果的成员关系键不是特定于源的。
    多播通道可以连接多个多播组，包括多个接口上的同一组。实现可以限制同时加入的组的数量。
     */
    MembershipKey join(InetAddress group, NetworkInterface interf)
        throws IOException;

    /**
     * Joins a multicast group to begin receiving datagrams sent to the group
     * from a given source address.
     *
     * <p> If this channel is currently a member of the group on the given
     * interface to receive datagrams from the given source address then the
     * membership key, representing that membership, is returned. Otherwise this
     * channel joins the group and the resulting new membership key is returned.
     * The resulting membership key is {@link MembershipKey#sourceAddress
     * source-specific}.
     *
     * <p> Membership is <em>cumulative</em> and this method may be invoked
     * again with the same group and interface to allow receiving datagrams sent
     * by other source addresses to the group.
     *
     * @param   group
     *          The multicast address to join
     * @param   interf
     *          The network interface on which to join the group
     * @param   source
     *          The source address
     *
     * @return  The membership key
     *
     * @throws  IllegalArgumentException
     *          If the group parameter is not a {@link
     *          InetAddress#isMulticastAddress multicast} address, the
     *          source parameter is not a unicast address, the group
     *          parameter is an address type that is not supported by this channel,
     *          or the source parameter is not the same address type as the group
     * @throws  IllegalStateException
     *          If the channel is currently a member of the group on the given
     *          interface to receive all datagrams
     * @throws  UnsupportedOperationException
     *          If the channel's socket is not an Internet Protocol socket or
     *          source filtering is not supported
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is set, and its
     *          {@link SecurityManager#checkMulticast(InetAddress) checkMulticast}
     *          method denies access to the multiast group
     *          加入多播组，开始接收从给定源地址发送到组的数据报。
    如果该通道当前是给定接口上接收来自给定源地址的数据报的组的成员，则返回代表该成员关系的成员关系键。否则，该通道将加入组，并返回生成的新成员关系键。结果的成员关系键是特定于源的。
    成员关系是累积的，可以使用相同的组和接口再次调用此方法，以允许接收由其他源地址发送到组的数据报。
     */
    MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source)
        throws IOException;
}
