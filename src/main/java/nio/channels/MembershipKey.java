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

/**
 * A token representing the membership of an Internet Protocol (IP) multicast
 * group.
 *
 * <p> A membership key may represent a membership to receive all datagrams sent
 * to the group, or it may be <em>source-specific</em>, meaning that it
 * represents a membership that receives only datagrams from a specific source
 * address. Whether or not a membership key is source-specific may be determined
 * by invoking its {@link #sourceAddress() sourceAddress} method.
 *
 * <p> A membership key is valid upon creation and remains valid until the
 * membership is dropped by invoking the {@link #drop() drop} method, or
 * the channel is closed. The validity of the membership key may be tested
 * by invoking its {@link #isValid() isValid} method.
 *
 * <p> Where a membership key is not source-specific and the underlying operation
 * system supports source filtering, then the {@link #block block} and {@link
 * #unblock unblock} methods can be used to block or unblock multicast datagrams
 * from particular source addresses.
 *
 * @see MulticastChannel
 *
 * @since 1.7
 * 表示Internet协议(IP)多播组成员的一种令牌。
成员关系键可以表示接收发送到组的所有数据报的成员关系，也可以表示特定于源的成员关系，这意味着它表示只从特定源地址接收数据报的成员关系。成员关系键是否特定于源，可以通过调用它的sourceAddress方法来确定。
成员关系键在创建时是有效的，并且在通过调用drop方法删除成员关系或关闭通道之前保持有效。成员密钥的有效性可以通过调用它的isValid方法来测试。
如果成员关系键不是特定于源的，底层操作系统支持源过滤，那么块和unblock方法可以用于从特定的源地址阻塞或取消阻塞多播数据报。
 */
public abstract class MembershipKey {

    /**
     * Initializes a new instance of this class.
     */
    protected MembershipKey() {
    }

    /**
     * Tells whether or not this membership is valid.
     *
     * <p> A multicast group membership is valid upon creation and remains
     * valid until the membership is dropped by invoking the {@link #drop() drop}
     * method, or the channel is closed.
     *
     * @return  {@code true} if this membership key is valid, {@code false}
     *          otherwise
     *          说明此成员资格是否有效。
    多播组成员关系在创建时是有效的，在通过调用drop方法删除成员关系或关闭通道之前都是有效的。
     */
    public abstract boolean isValid();

    /**
     * Drop membership.
     *
     * <p> If the membership key represents a membership to receive all datagrams
     * then the membership is dropped and the channel will no longer receive any
     * datagrams sent to the group. If the membership key is source-specific
     * then the channel will no longer receive datagrams sent to the group from
     * that source address.
     *
     * <p> After membership is dropped it may still be possible to receive
     * datagrams sent to the group. This can arise when datagrams are waiting to
     * be received in the socket's receive buffer. After membership is dropped
     * then the channel may {@link MulticastChannel#join join} the group again
     * in which case a new membership key is returned.
     *
     * <p> Upon return, this membership object will be {@link #isValid() invalid}.
     * If the multicast group membership is already invalid then invoking this
     * method has no effect. Once a multicast group membership is invalid,
     * it remains invalid forever.
     * 放弃会员资格。
     如果成员关系键表示要接收所有数据报的成员关系，那么成员关系将被删除，通道将不再接收发送给组的任何数据报。如果成员关系键是特定于源的，那么通道将不再接收从源地址发送到组的数据报。
     在取消成员资格之后，仍然可以接收发送到组的数据报。当datagrams等待在套接字的接收缓冲区中接收时，就会出现这种情况。在删除成员后，通道可能再次加入该组，在这种情况下，返回一个新的成员密钥。
     返回时，该成员对象将无效。如果多播组成员已经无效，那么调用此方法没有效果。一旦多播组成员无效，它将永远无效。
     */
    public abstract void drop();

    /**
     * Block multicast datagrams from the given source address.
     *
     * <p> If this membership key is not source-specific, and the underlying
     * operating system supports source filtering, then this method blocks
     * multicast datagrams from the given source address. If the given source
     * address is already blocked then this method has no effect.
     * After a source address is blocked it may still be possible to receive
     * datagrams from that source. This can arise when datagrams are waiting to
     * be received in the socket's receive buffer.
     *
     * @param   source
     *          The source address to block
     *
     * @return  This membership key
     *
     * @throws  IllegalArgumentException
     *          If the {@code source} parameter is not a unicast address or
     *          is not the same address type as the multicast group
     * @throws  IllegalStateException
     *          If this membership key is source-specific or is no longer valid
     * @throws  UnsupportedOperationException
     *          If the underlying operating system does not support source
     *          filtering
     * @throws  IOException
     *          If an I/O error occurs
     *          从给定的源地址块多播数据报。
    如果这个成员关系键不是特定于源的，并且底层操作系统支持源过滤，那么这个方法将从给定的源地址阻塞多播数据报。如果给定的源地址已经被阻塞，那么该方法没有任何效果。在源地址被阻塞之后，仍然可以从该源接收数据报。当在套接字的接收缓冲区中等待接收数据报时，可能会出现这种情况。
     */
    public abstract MembershipKey block(InetAddress source) throws IOException;

    /**
     * Unblock multicast datagrams from the given source address that was
     * previously blocked using the {@link #block(InetAddress) block} method.
     *
     * @param   source
     *          The source address to unblock
     *
     * @return  This membership key
     *
     * @throws  IllegalStateException
     *          If the given source address is not currently blocked or the
     *          membership key is no longer valid
     *          从先前使用块方法阻塞的给定源地址的Unblock多播数据报。
     */
    public abstract MembershipKey unblock(InetAddress source);

    /**
     * Returns the channel for which this membership key was created. This
     * method will continue to return the channel even after the membership
     * becomes {@link #isValid invalid}.
     *
     * @return  the channel
     * 返回创建此成员关系键的通道。该方法将继续返回通道，即使成员关系无效。
     */
    public abstract MulticastChannel channel();

    /**
     * Returns the multicast group for which this membership key was created.
     * This method will continue to return the group even after the membership
     * becomes {@link #isValid invalid}.
     *
     * @return  the multicast group
     * 返回创建此成员关系键的多播组。此方法将继续返回组，即使成员关系无效。
     */
    public abstract InetAddress group();

    /**
     * Returns the network interface for which this membership key was created.
     * This method will continue to return the network interface even after the
     * membership becomes {@link #isValid invalid}.
     *
     * @return  the network interface
     * 返回创建此成员关系键的网络接口。此方法将继续返回网络接口，即使成员关系无效。
     */
    public abstract NetworkInterface networkInterface();

    /**
     * Returns the source address if this membership key is source-specific,
     * or {@code null} if this membership is not source-specific.
     *
     * @return  The source address if this membership key is source-specific,
     *          otherwise {@code null}
     *          如果此成员密钥是源特定的，则返回源地址，如果此成员身份不是源特定的，则返回null。
     */
    public abstract InetAddress sourceAddress();
}
