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

package java.nio.channels.spi;

import java.nio.channels.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;
import java.util.concurrent.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Service-provider class for asynchronous channels.
 *
 * <p> An asynchronous channel provider is a concrete subclass of this class that
 * has a zero-argument constructor and implements the abstract methods specified
 * below.  A given invocation of the Java virtual machine maintains a single
 * system-wide default provider instance, which is returned by the {@link
 * #provider() provider} method.  The first invocation of that method will locate
 * the default provider as specified below.
 *
 * <p> All of the methods in this class are safe for use by multiple concurrent
 * threads.  </p>
 * 异步通道的服务提供者类。
 异步通道提供程序是该类的一个具体子类，它具有一个零参数构造函数并实现下面指定的抽象方法。Java虚拟机的给定调用维护一个系统范围的默认提供程序实例，该实例由提供程序方法返回。该方法的第一次调用将定位如下所指定的默认提供者。
 该类中的所有方法都可以安全地用于多个并发线程。
 *
 * @since 1.7
 */

public abstract class AsynchronousChannelProvider {
    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("asynchronousChannelProvider"));
        return null;
    }
    private AsynchronousChannelProvider(Void ignore) { }

    /**
     * Initializes a new instance of this class.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("asynchronousChannelProvider")</tt>
     */
    protected AsynchronousChannelProvider() {
        this(checkPermission());
    }

    // lazy initialization of default provider
    private static class ProviderHolder {
        static final AsynchronousChannelProvider provider = load();

        private static AsynchronousChannelProvider load() {
            return AccessController
                .doPrivileged(new PrivilegedAction<AsynchronousChannelProvider>() {
                    public AsynchronousChannelProvider run() {
                        AsynchronousChannelProvider p;
                        p = loadProviderFromProperty();
                        if (p != null)
                            return p;
                        p = loadProviderAsService();
                        if (p != null)
                            return p;
                        return sun.nio.ch.DefaultAsynchronousChannelProvider.create();
                    }});
        }

        private static AsynchronousChannelProvider loadProviderFromProperty() {
            String cn = System.getProperty("java.nio.channels.spi.AsynchronousChannelProvider");
            if (cn == null)
                return null;
            try {
                Class<?> c = Class.forName(cn, true,
                                           ClassLoader.getSystemClassLoader());
                return (AsynchronousChannelProvider)c.newInstance();
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

        private static AsynchronousChannelProvider loadProviderAsService() {
            ServiceLoader<AsynchronousChannelProvider> sl =
                ServiceLoader.load(AsynchronousChannelProvider.class,
                                   ClassLoader.getSystemClassLoader());
            Iterator<AsynchronousChannelProvider> i = sl.iterator();
            for (;;) {
                try {
                    return (i.hasNext()) ? i.next() : null;
                } catch (ServiceConfigurationError sce) {
                    if (sce.getCause() instanceof SecurityException) {
                        // Ignore the security exception, try the next provider
                        continue;
                    }
                    throw sce;
                }
            }
        }
    }

    /**
     * Returns the system-wide default asynchronous channel provider for this
     * invocation of the Java virtual machine.
     *
     * <p> The first invocation of this method locates the default provider
     * object as follows: </p>
     *
     * <ol>
     *
     *   <li><p> If the system property
     *   <tt>java.nio.channels.spi.AsynchronousChannelProvider</tt> is defined
     *   then it is taken to be the fully-qualified name of a concrete provider class.
     *   The class is loaded and instantiated; if this process fails then an
     *   unspecified error is thrown.  </p></li>
     *
     *   <li><p> If a provider class has been installed in a jar file that is
     *   visible to the system class loader, and that jar file contains a
     *   provider-configuration file named
     *   <tt>java.nio.channels.spi.AsynchronousChannelProvider</tt> in the resource
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
     * @return  The system-wide default AsynchronousChannel provider
     * 返回调用Java虚拟机的系统范围的默认异步通道提供程序。
    该方法的第一次调用定位默认提供者对象如下:
    如果系统属性为java.nio.channels.spi。定义了AsynchronousChannelProvider，然后将其作为具体提供程序类的完全限定名。类被加载和实例化;如果此进程失败，则抛出未指定的错误。
    如果提供程序类已安装在系统类装入器可见的jar文件中，并且该jar文件包含一个名为java.nio.channels.spi的提供程序配置文件。在资源目录META-INF/services中使用AsynchronousChannelProvider，然后获取该文件中指定的第一个类名。类被加载和实例化;如果此进程失败，则抛出未指定的错误。
    最后，如果上面任何一种方法都没有指定提供程序，那么系统默认提供程序类将被实例化并返回结果。
    该方法的后续调用返回第一次调用返回的提供者。
     */
    public static AsynchronousChannelProvider provider() {
        return ProviderHolder.provider;
    }

    /**
     * Constructs a new asynchronous channel group with a fixed thread pool.
     *
     * @param   nThreads
     *          The number of threads in the pool
     * @param   threadFactory
     *          The factory to use when creating new threads
     *
     * @return  A new asynchronous channel group
     *
     * @throws  IllegalArgumentException
     *          If {@code nThreads <= 0}
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @see AsynchronousChannelGroup#withFixedThreadPool
     * 构造一个带有固定线程池的新的异步通道组。
     */
    public abstract AsynchronousChannelGroup
        openAsynchronousChannelGroup(int nThreads, ThreadFactory threadFactory) throws IOException;

    /**
     * Constructs a new asynchronous channel group with the given thread pool.
     *
     * @param   executor
     *          The thread pool
     * @param   initialSize
     *          A value {@code >=0} or a negative value for implementation
     *          specific default
     *
     * @return  A new asynchronous channel group
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @see AsynchronousChannelGroup#withCachedThreadPool
     * 使用给定的线程池构造一个新的异步通道组。
     */
    public abstract AsynchronousChannelGroup
        openAsynchronousChannelGroup(ExecutorService executor, int initialSize) throws IOException;

    /**
     * Opens an asynchronous server-socket channel.
     *
     * @param   group
     *          The group to which the channel is bound, or {@code null} to
     *          bind to the default group
     *
     * @return  The new channel
     *
     * @throws  IllegalChannelGroupException
     *          If the provider that created the group differs from this provider
     * @throws  ShutdownChannelGroupException
     *          The group is shutdown
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract AsynchronousServerSocketChannel openAsynchronousServerSocketChannel
        (AsynchronousChannelGroup group) throws IOException;

    /**
     * Opens an asynchronous socket channel.
     *
     * @param   group
     *          The group to which the channel is bound, or {@code null} to
     *          bind to the default group
     *
     * @return  The new channel
     *
     * @throws  IllegalChannelGroupException
     *          If the provider that created the group differs from this provider
     * @throws  ShutdownChannelGroupException
     *          The group is shutdown
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract AsynchronousSocketChannel openAsynchronousSocketChannel
        (AsynchronousChannelGroup group) throws IOException;
}
