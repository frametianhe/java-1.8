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
import java.io.Closeable;


/**
 * A nexus for I/O operations.
 *
 * <p> A channel represents an open connection to an entity such as a hardware
 * device, a file, a network socket, or a program component that is capable of
 * performing one or more distinct I/O operations, for example reading or
 * writing.
 *
 * <p> A channel is either open or closed.  A channel is open upon creation,
 * and once closed it remains closed.  Once a channel is closed, any attempt to
 * invoke an I/O operation upon it will cause a {@link ClosedChannelException}
 * to be thrown.  Whether or not a channel is open may be tested by invoking
 * its {@link #isOpen isOpen} method.
 *
 * <p> Channels are, in general, intended to be safe for multithreaded access
 * as described in the specifications of the interfaces and classes that extend
 * and implement this interface.
 * 连接I/O操作。
 通道表示与实体(如硬件设备、文件、网络套接字或能够执行一个或多个不同的I/O操作(例如读写)的程序组件)的开放连接。
 通道不是打开就是关闭。通道在创建时是打开的，一旦关闭，通道仍然关闭。一旦通道被关闭，任何试图在其上调用I/O操作的尝试都会导致一个ClosedChannelException被抛出。通道是否打开可以通过调用其isOpen方法进行测试。
 一般来说，通道是为了安全进行多线程访问，正如扩展和实现该接口的接口和类的规范所描述的那样。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
//通道是为了安全进行多线程访问
public interface Channel extends Closeable {

    /**
     * Tells whether or not this channel is open.告诉此通道是否打开。
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     */
    public boolean isOpen();

    /**
     * Closes this channel.
     *
     * <p> After a channel is closed, any further attempt to invoke I/O
     * operations upon it will cause a {@link ClosedChannelException} to be
     * thrown.
     *
     * <p> If this channel is already closed then invoking this method has no
     * effect.
     *
     * <p> This method may be invoked at any time.  If some other thread has
     * already invoked it, however, then another invocation will block until
     * the first invocation is complete, after which it will return without
     * effect. </p>
     * 关闭这个通道。
     在通道关闭之后，任何进一步尝试在其上调用I/O操作都会导致一个ClosedChannelException被抛出。
     如果这个通道已经关闭，那么调用这个方法没有任何效果。
     此方法可以随时调用。但是，如果其他一些线程已经调用了它，那么另一个调用将会阻塞，直到第一个调用完成，然后它将返回而不产生任何效果。
     *
     * @throws  IOException  If an I/O error occurs
     */
//    如果这个通道已经关闭，那么调用这个方法没有任何效果。
//    此方法可以随时调用。但是，如果其他一些线程已经调用了它，那么另一个调用将会阻塞，直到第一个调用完成，然后它将返回而不产生任何效果。
    public void close() throws IOException;

}
