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

package java.nio.channels.spi;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;
import sun.security.action.GetPropertyAction;


/**
 * Service-provider class for selectors and selectable channels.
 *
 * <p> A selector provider is a concrete subclass of this class that has a
 * zero-argument constructor and implements the abstract methods specified
 * below.  A given invocation of the Java virtual machine maintains a single
 * system-wide default provider instance, which is returned by the {@link
 * #provider() provider} method.  The first invocation of that method will locate
 * the default provider as specified below.
 *
 * <p> The system-wide default provider is used by the static <tt>open</tt>
 * methods of the {@link java.nio.channels.DatagramChannel#open
 * DatagramChannel}, {@link java.nio.channels.Pipe#open Pipe}, {@link
 * java.nio.channels.Selector#open Selector}, {@link
 * java.nio.channels.ServerSocketChannel#open ServerSocketChannel}, and {@link
 * java.nio.channels.SocketChannel#open SocketChannel} classes.  It is also
 * used by the {@link java.lang.System#inheritedChannel System.inheritedChannel()}
 * method. A program may make use of a provider other than the default provider
 * by instantiating that provider and then directly invoking the <tt>open</tt>
 * methods defined in this class.
 *
 * <p> All of the methods in this class are safe for use by multiple concurrent
 * threads.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 用于选择器和可选择通道的服务提供者类。
选择器提供程序是该类的一个具体子类，它具有一个零参数构造函数并实现下面指定的抽象方法。Java虚拟机的给定调用维护一个系统范围的默认提供程序实例，该实例由提供程序方法返回。该方法的第一次调用将定位如下所指定的默认提供者。
系统范围的默认提供程序由DatagramChannel、Pipe、Selector、ServerSocketChannel和SocketChannel类的静态打开方法使用。它也被System.inheritedChannel()方法使用。一个程序可以通过实例化那个提供程序，然后直接调用这个类中定义的开放方法来使用除默认提供程序之外的提供程序。
该类中的所有方法都可以安全地用于多个并发线程。
 */

public abstract class SelectorProvider {

    private static final Object lock = new Object();
    private static SelectorProvider provider = null;

    /**
     * Initializes a new instance of this class.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("selectorProvider")</tt>
     */
    protected SelectorProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("selectorProvider"));
    }

    private static boolean loadProviderFromProperty() {
        String cn = System.getProperty("java.nio.channels.spi.SelectorProvider");
        if (cn == null)
            return false;
        try {
            Class<?> c = Class.forName(cn, true,
                                       ClassLoader.getSystemClassLoader());
            provider = (SelectorProvider)c.newInstance();
            return true;
        } catch (ClassNotFoundException x) {
            throw new ServiceConfigurationError(null, x);
        } catch (IllegalAccessException x) {
            throw new ServiceConfigurationError(null, x);
        } catch (InstantiationException x) {
            throw new ServiceConfigurationError(null, x);
        } catch (SecurityException x) {
            throw new ServiceConfigurationError(null, x);
        }
    }

    private static boolean loadProviderAsService() {

        ServiceLoader<SelectorProvider> sl =
            ServiceLoader.load(SelectorProvider.class,
                               ClassLoader.getSystemClassLoader());
        Iterator<SelectorProvider> i = sl.iterator();
        for (;;) {
            try {
                if (!i.hasNext())
                    return false;
                provider = i.next();
                return true;
            } catch (ServiceConfigurationError sce) {
                if (sce.getCause() instanceof SecurityException) {
                    // Ignore the security exception, try the next provider
                    continue;
                }
                throw sce;
            }
        }
    }

    /**
     * Returns the system-wide default selector provider for this invocation of
     * the Java virtual machine.
     *
     * <p> The first invocation of this method locates the default provider
     * object as follows: </p>
     *
     * <ol>
     *
     *   <li><p> If the system property
     *   <tt>java.nio.channels.spi.SelectorProvider</tt> is defined then it is
     *   taken to be the fully-qualified name of a concrete provider class.
     *   The class is loaded and instantiated; if this process fails then an
     *   unspecified error is thrown.  </p></li>
     *
     *   <li><p> If a provider class has been installed in a jar file that is
     *   visible to the system class loader, and that jar file contains a
     *   provider-configuration file named
     *   <tt>java.nio.channels.spi.SelectorProvider</tt> in the resource
     *   directory <tt>META-INF/services</tt>, then the first class name
     *   specified in that file is taken.  The class is loaded and
     *   instantiated; if this process fails then an unspecified error is
     *   thrown.  </p></li>
     *
     *   <li><p> Finally, if no provider has been specified by any of the above
     *   means then the system-default provider class is instantiated and the
     *   result is returned.  </p></li>
     *
     * </ol>
     *
     * <p> Subsequent invocations of this method return the provider that was
     * returned by the first invocation.  </p>
     *
     * @return  The system-wide default selector provider
     *
    返回调用Java虚拟机的系统范围的默认选择器提供程序。
    该方法的第一次调用定位默认提供者对象如下:
    如果系统属性为java.nio.channels.spi。SelectorProvider被定义为一个具体的provider类的完全限定名。类装入并实例化;如果此进程失败，则抛出未指定的错误。
    如果提供程序类已安装在系统类装入器可见的jar文件中，并且该jar文件包含一个名为java.nio.channels.spi的提供程序配置文件。资源目录META-INF/services中的SelectorProvider，然后在该文件中指定的第一个类名。类被加载和实例化;如果此进程失败，则抛出未指定的错误。
    最后，如果上面任何一种方法都没有指定提供程序，那么系统默认提供程序类将被实例化并返回结果。
    该方法的后续调用返回第一次调用返回的提供者。
     */
    public static SelectorProvider provider() {
        synchronized (lock) {
            if (provider != null)
                return provider;
            return AccessController.doPrivileged(
                new PrivilegedAction<SelectorProvider>() {
                    public SelectorProvider run() {
                            if (loadProviderFromProperty())
                                return provider;
                            if (loadProviderAsService())
                                return provider;
                            provider = sun.nio.ch.DefaultSelectorProvider.create();
                            return provider;
                        }
                    });
        }
    }

    /**
     * Opens a datagram channel.
     *
     * @return  The new channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract DatagramChannel openDatagramChannel()
        throws IOException;

    /**
     * Opens a datagram channel.
     *
     * @param   family
     *          The protocol family
     *
     * @return  A new datagram channel
     *
     * @throws  UnsupportedOperationException
     *          If the specified protocol family is not supported
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @since 1.7
     */
    public abstract DatagramChannel openDatagramChannel(ProtocolFamily family)
        throws IOException;

    /**
     * Opens a pipe.
     *
     * @return  The new pipe
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract Pipe openPipe()
        throws IOException;

    /**
     * Opens a selector.
     *
     * @return  The new selector
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract AbstractSelector openSelector()
        throws IOException;

    /**
     * Opens a server-socket channel.
     *
     * @return  The new channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract ServerSocketChannel openServerSocketChannel()
        throws IOException;

    /**
     * Opens a socket channel.
     *
     * @return  The new channel
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract SocketChannel openSocketChannel()
        throws IOException;

    /**
     * Returns the channel inherited from the entity that created this
     * Java virtual machine.
     *
     * <p> On many operating systems a process, such as a Java virtual
     * machine, can be started in a manner that allows the process to
     * inherit a channel from the entity that created the process. The
     * manner in which this is done is system dependent, as are the
     * possible entities to which the channel may be connected. For example,
     * on UNIX systems, the Internet services daemon (<i>inetd</i>) is used to
     * start programs to service requests when a request arrives on an
     * associated network port. In this example, the process that is started,
     * inherits a channel representing a network socket.
     *
     * <p> In cases where the inherited channel represents a network socket
     * then the {@link java.nio.channels.Channel Channel} type returned
     * by this method is determined as follows:
     *
     * <ul>
     *
     *  <li><p> If the inherited channel represents a stream-oriented connected
     *  socket then a {@link java.nio.channels.SocketChannel SocketChannel} is
     *  returned. The socket channel is, at least initially, in blocking
     *  mode, bound to a socket address, and connected to a peer.
     *  </p></li>
     *
     *  <li><p> If the inherited channel represents a stream-oriented listening
     *  socket then a {@link java.nio.channels.ServerSocketChannel
     *  ServerSocketChannel} is returned. The server-socket channel is, at
     *  least initially, in blocking mode, and bound to a socket address.
     *  </p></li>
     *
     *  <li><p> If the inherited channel is a datagram-oriented socket
     *  then a {@link java.nio.channels.DatagramChannel DatagramChannel} is
     *  returned. The datagram channel is, at least initially, in blocking
     *  mode, and bound to a socket address.
     *  </p></li>
     *
     * </ul>
     *
     * <p> In addition to the network-oriented channels described, this method
     * may return other kinds of channels in the future.
     *
     * <p> The first invocation of this method creates the channel that is
     * returned. Subsequent invocations of this method return the same
     * channel. </p>
     *
     * @return  The inherited channel, if any, otherwise <tt>null</tt>.
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("inheritedChannel")</tt>
     *
     * @since 1.5
     * 返回从创建此Java虚拟机的实体继承的通道。
    在许多操作系统中，可以以允许流程从创建流程的实体继承通道的方式启动流程，例如Java虚拟机。这样做的方式是依赖于系统的，通道可以连接到的可能的实体也是如此。例如，在UNIX系统上，当请求到达相关的网络端口时，Internet服务守护进程(inetd)用于启动程序来服务请求。在本例中，启动的进程继承表示网络套接字的通道。
    如果继承的通道表示网络套接字，则该方法返回的通道类型确定如下:
    如果继承的通道表示面向流的连接套接字，则返回一个SocketChannel。套接字通道至少在开始时处于阻塞模式，绑定到套接字地址，并连接到对等点。
    如果继承的通道表示面向流的侦听套接字，则返回一个ServerSocketChannel。服务器套接字通道至少在开始时处于阻塞模式，并绑定到套接字地址。
    如果继承的通道是一个面向数据集的套接字，则返回一个DatagramChannel。至少一开始，数据报通道处于阻塞模式，并绑定到套接字地址。
    除了所描述的面向网络的通道之外，这种方法将来还可能返回其他类型的通道。
    该方法的第一次调用创建返回的通道。该方法的后续调用返回相同的通道。
     */
   public Channel inheritedChannel() throws IOException {
        return null;
   }

}
