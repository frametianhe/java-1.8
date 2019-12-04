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

import java.nio.channels.spi.AsynchronousChannelProvider;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A grouping of asynchronous channels for the purpose of resource sharing.
 *
 * <p> An asynchronous channel group encapsulates the mechanics required to
 * handle the completion of I/O operations initiated by {@link AsynchronousChannel
 * asynchronous channels} that are bound to the group. A group has an associated
 * thread pool to which tasks are submitted to handle I/O events and dispatch to
 * {@link CompletionHandler completion-handlers} that consume the result of
 * asynchronous operations performed on channels in the group. In addition to
 * handling I/O events, the pooled threads may also execute other tasks required
 * to support the execution of asynchronous I/O operations.
 *
 * <p> An asynchronous channel group is created by invoking the {@link
 * #withFixedThreadPool withFixedThreadPool} or {@link #withCachedThreadPool
 * withCachedThreadPool} methods defined here. Channels are bound to a group by
 * specifying the group when constructing the channel. The associated thread
 * pool is <em>owned</em> by the group; termination of the group results in the
 * shutdown of the associated thread pool.
 *
 * <p> In addition to groups created explicitly, the Java virtual machine
 * maintains a system-wide <em>default group</em> that is constructed
 * automatically. Asynchronous channels that do not specify a group at
 * construction time are bound to the default group. The default group has an
 * associated thread pool that creates new threads as needed. The default group
 * may be configured by means of system properties defined in the table below.
 * Where the {@link java.util.concurrent.ThreadFactory ThreadFactory} for the
 * default group is not configured then the pooled threads of the default group
 * are {@link Thread#isDaemon daemon} threads.
 *
 * <table border summary="System properties">
 *   <tr>
 *     <th>System property</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td> {@code java.nio.channels.DefaultThreadPool.threadFactory} </td>
 *     <td> The value of this property is taken to be the fully-qualified name
 *     of a concrete {@link java.util.concurrent.ThreadFactory ThreadFactory}
 *     class. The class is loaded using the system class loader and instantiated.
 *     The factory's {@link java.util.concurrent.ThreadFactory#newThread
 *     newThread} method is invoked to create each thread for the default
 *     group's thread pool. If the process to load and instantiate the value
 *     of the property fails then an unspecified error is thrown during the
 *     construction of the default group. </td>
 *   </tr>
 *   <tr>
 *     <td> {@code java.nio.channels.DefaultThreadPool.initialSize} </td>
 *     <td> The value of the {@code initialSize} parameter for the default
 *     group (see {@link #withCachedThreadPool withCachedThreadPool}).
 *     The value of the property is taken to be the {@code String}
 *     representation of an {@code Integer} that is the initial size parameter.
 *     If the value cannot be parsed as an {@code Integer} it causes an
 *     unspecified error to be thrown during the construction of the default
 *     group. </td>
 *   </tr>
 * </table>
 *
 * <a name="threading"></a><h2>Threading</h2>
 *
 * <p> The completion handler for an I/O operation initiated on a channel bound
 * to a group is guaranteed to be invoked by one of the pooled threads in the
 * group. This ensures that the completion handler is run by a thread with the
 * expected <em>identity</em>.
 *
 * <p> Where an I/O operation completes immediately, and the initiating thread
 * is one of the pooled threads in the group then the completion handler may
 * be invoked directly by the initiating thread. To avoid stack overflow, an
 * implementation may impose a limit as to the number of activations on the
 * thread stack. Some I/O operations may prohibit invoking the completion
 * handler directly by the initiating thread (see {@link
 * AsynchronousServerSocketChannel#accept(Object,CompletionHandler) accept}).
 *
 * <a name="shutdown"></a><h2>Shutdown and Termination</h2>
 *
 * <p> The {@link #shutdown() shutdown} method is used to initiate an <em>orderly
 * shutdown</em> of a group. An orderly shutdown marks the group as shutdown;
 * further attempts to construct a channel that binds to the group will throw
 * {@link ShutdownChannelGroupException}. Whether or not a group is shutdown can
 * be tested using the {@link #isShutdown() isShutdown} method. Once shutdown,
 * the group <em>terminates</em> when all asynchronous channels that are bound to
 * the group are closed, all actively executing completion handlers have run to
 * completion, and resources used by the group are released. No attempt is made
 * to stop or interrupt threads that are executing completion handlers. The
 * {@link #isTerminated() isTerminated} method is used to test if the group has
 * terminated, and the {@link #awaitTermination awaitTermination} method can be
 * used to block until the group has terminated.
 *
 * <p> The {@link #shutdownNow() shutdownNow} method can be used to initiate a
 * <em>forceful shutdown</em> of the group. In addition to the actions performed
 * by an orderly shutdown, the {@code shutdownNow} method closes all open channels
 * in the group as if by invoking the {@link AsynchronousChannel#close close}
 * method.
 *
 * @since 1.7
 *
 * @see AsynchronousSocketChannel#open(AsynchronousChannelGroup)
 * @see AsynchronousServerSocketChannel#open(AsynchronousChannelGroup)
 * 一组用于资源共享的异步通道。
异步通道组封装了处理由绑定到组的异步通道发起的I/O操作完成所需的机制。一个组有一个关联的线程池，任务被提交到该线程池中，以处理I/O事件，并被分派给使用组中通道上异步操作结果的完成处理程序。除了处理I/O事件外，池线程还可以执行支持异步I/O操作执行所需的其他任务。
异步通道组是通过调用这里定义的withFixedThreadPool或cachedthreadpool方法创建的。通道在构造通道时通过指定组来绑定到组。关联的线程池为组所有;组的终止导致关联线程池的关闭。
除了显式创建的组之外，Java虚拟机还维护一个自动构造的系统范围的默认组。在构造时不指定组的异步通道被绑定到默认组。默认组有一个关联的线程池，根据需要创建新的线程。默认组可以通过下面表中定义的系统属性进行配置。如果未配置默认组的ThreadFactory，那么默认组的池线程就是守护进程线程。
java.nio.channels.DefaultThreadPool.threadFactory
该属性的值被视为一个具体的ThreadFactory类的完全限定名。使用系统类装入器装入类并实例化。调用工厂的newThread方法，为默认组的线程池创建每个线程。如果加载和实例化属性值的进程失败，那么在构建默认组期间将抛出一个未指定的错误。
java.nio.channels.DefaultThreadPool.initialSize
默认组的initialSize参数的值(参见withCachedThreadPool)。属性的值被视为整数的字符串表示形式，该整数是初始大小参数。如果不能将值解析为整数，则会导致在构造默认组期间抛出未指定的错误。
线程
在绑定到组的通道上启动的I/O操作的完成处理程序保证被组中的一个池线程调用。这确保完成处理程序由具有预期标识的线程运行。
如果I/O操作立即完成，而发起线程是组中的池线程之一，则发起线程可以直接调用完成处理程序。为了避免堆栈溢出，实现可能对线程堆栈上的激活数量施加限制。一些I/O操作可能禁止直接由发起线程调用完成处理程序(请参阅accept)。
关闭和终止
关闭方法用于启动一个组的有序关闭。有序的关闭标志着该集团的关闭;进一步尝试构建绑定到组的通道将抛出ShutdownChannelGroupException。是否可以使用isShutdown方法测试组是否关闭。一旦关闭，当绑定到组的所有异步通道都关闭、所有正在执行的完成处理程序都运行到完成状态以及释放组使用的资源时，组将终止。没有尝试停止或中断正在执行完成处理程序的线程。isTerminated方法用于测试组是否已终止，awaitTermination方法用于阻塞，直到组结束。
shutdownNow方法可用于启动组的强制关闭。除了有序关闭所执行的操作之外，shutdownNow方法还会像调用close方法一样关闭组中的所有打开通道。
 */

public abstract class AsynchronousChannelGroup {
    private final AsynchronousChannelProvider provider;

    /**
     * Initialize a new instance of this class.
     *
     * @param   provider
     *          The asynchronous channel provider for this group
     */
    protected AsynchronousChannelGroup(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the provider that created this channel group.
     *
     * @return  The provider that created this channel group
     */
    public final AsynchronousChannelProvider provider() {
        return provider;
    }

    /**
     * Creates an asynchronous channel group with a fixed thread pool.
     *
     * <p> The resulting asynchronous channel group reuses a fixed number of
     * threads. At any point, at most {@code nThreads} threads will be active
     * processing tasks that are submitted to handle I/O events and dispatch
     * completion results for operations initiated on asynchronous channels in
     * the group.
     *
     * <p> The group is created by invoking the {@link
     * AsynchronousChannelProvider#openAsynchronousChannelGroup(int,ThreadFactory)
     * openAsynchronousChannelGroup(int,ThreadFactory)} method of the system-wide
     * default {@link AsynchronousChannelProvider} object.
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
     *          创建具有固定线程池的异步通道组。
    产生的异步通道组重用固定数量的线程。在任何时候，大多数nThreads线程都是主动处理任务，这些任务被提交来处理I/O事件并为组中异步通道上发起的操作分派完成结果。
    这个组是通过调用系统范围内默认的异步channelprovider对象的openAsynchronousChannelGroup(int,ThreadFactory)方法创建的。
     */
    public static AsynchronousChannelGroup withFixedThreadPool(int nThreads,
                                                               ThreadFactory threadFactory)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(nThreads, threadFactory);
    }

    /**
     * Creates an asynchronous channel group with a given thread pool that
     * creates new threads as needed.
     *
     * <p> The {@code executor} parameter is an {@code ExecutorService} that
     * creates new threads as needed to execute tasks that are submitted to
     * handle I/O events and dispatch completion results for operations initiated
     * on asynchronous channels in the group. It may reuse previously constructed
     * threads when they are available.
     *
     * <p> The {@code initialSize} parameter may be used by the implementation
     * as a <em>hint</em> as to the initial number of tasks it may submit. For
     * example, it may be used to indicate the initial number of threads that
     * wait on I/O events.
     *
     * <p> The executor is intended to be used exclusively by the resulting
     * asynchronous channel group. Termination of the group results in the
     * orderly  {@link ExecutorService#shutdown shutdown} of the executor
     * service. Shutting down the executor service by other means results in
     * unspecified behavior.
     *
     * <p> The group is created by invoking the {@link
     * AsynchronousChannelProvider#openAsynchronousChannelGroup(ExecutorService,int)
     * openAsynchronousChannelGroup(ExecutorService,int)} method of the system-wide
     * default {@link AsynchronousChannelProvider} object.
     *
     * @param   executor
     *          The thread pool for the resulting group
     * @param   initialSize
     *          A value {@code >=0} or a negative value for implementation
     *          specific default
     *
     * @return  A new asynchronous channel group
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @see java.util.concurrent.Executors#newCachedThreadPool
     * 创建一个具有给定线程池的异步通道组，该线程池根据需要创建新线程。
    executor参数是一个ExecutorService，它根据需要创建新线程，以执行提交的处理I/O事件的任务，并为组中的异步通道上启动的操作分派完成结果。当它们可用时，它可以重用以前构建的线程。
    该实现可以使用initialSize参数作为它可能提交的初始任务数量的提示。例如，它可以用来指示等待I/O事件的初始线程数。
    执行程序将被产生的异步通道组专用。组的终止导致执行器服务的有序关闭。以其他方式关闭执行程序服务将导致未指定的行为。
    组是通过调用系统范围内默认异步通道提供程序对象的openAsynchronousChannelGroup(ExecutorService,int)方法创建的。
     */
    public static AsynchronousChannelGroup withCachedThreadPool(ExecutorService executor,
                                                                int initialSize)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(executor, initialSize);
    }

    /**
     * Creates an asynchronous channel group with a given thread pool.
     *
     * <p> The {@code executor} parameter is an {@code ExecutorService} that
     * executes tasks submitted to dispatch completion results for operations
     * initiated on asynchronous channels in the group.
     *
     * <p> Care should be taken when configuring the executor service. It
     * should support <em>direct handoff</em> or <em>unbounded queuing</em> of
     * submitted tasks, and the thread that invokes the {@link
     * ExecutorService#execute execute} method should never invoke the task
     * directly. An implementation may mandate additional constraints.
     *
     * <p> The executor is intended to be used exclusively by the resulting
     * asynchronous channel group. Termination of the group results in the
     * orderly  {@link ExecutorService#shutdown shutdown} of the executor
     * service. Shutting down the executor service by other means results in
     * unspecified behavior.
     *
     * <p> The group is created by invoking the {@link
     * AsynchronousChannelProvider#openAsynchronousChannelGroup(ExecutorService,int)
     * openAsynchronousChannelGroup(ExecutorService,int)} method of the system-wide
     * default {@link AsynchronousChannelProvider} object with an {@code
     * initialSize} of {@code 0}.
     *
     * @param   executor
     *          The thread pool for the resulting group
     *
     * @return  A new asynchronous channel group
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          使用给定的线程池创建一个异步通道组。
    executor参数是一个ExecutorService，它执行提交给分派组中异步通道上启动的操作的完成结果的任务。
    在配置执行程序服务时应该小心。它应该支持提交任务的直接切换或无限制排队，调用execute方法的线程不应该直接调用任务。实施可能需要额外的约束。
    执行程序将被产生的异步通道组专用。组的终止导致执行器服务的有序关闭。以其他方式关闭执行程序服务将导致未指定的行为。
    组是通过调用系统范围内默认异步通道提供程序对象的openAsynchronousChannelGroup(ExecutorService,int)方法创建的，该方法的初始大小为0。
     */
    public static AsynchronousChannelGroup withThreadPool(ExecutorService executor)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(executor, 0);
    }

    /**
     * Tells whether or not this asynchronous channel group is shutdown.告诉此异步通道组是否已关闭。
     *
     * @return  {@code true} if this asynchronous channel group is shutdown or
     *          has been marked for shutdown.
     */
    public abstract boolean isShutdown();

    /**
     * Tells whether or not this group has terminated.
     *
     * <p> Where this method returns {@code true}, then the associated thread
     * pool has also {@link ExecutorService#isTerminated terminated}.
     *
     * @return  {@code true} if this group has terminated
     * 告诉此组是否已终止。
    当此方法返回true时，关联的线程池也已终止。
     */
    public abstract boolean isTerminated();

    /**
     * Initiates an orderly shutdown of the group.
     *
     * <p> This method marks the group as shutdown. Further attempts to construct
     * channel that binds to this group will throw {@link ShutdownChannelGroupException}.
     * The group terminates when all asynchronous channels in the group are
     * closed, all actively executing completion handlers have run to completion,
     * and all resources have been released. This method has no effect if the
     * group is already shutdown.
     * 发起组织有序的关闭。
     此方法将组标记为shutdown。进一步构建绑定到这个组的通道将抛出ShutdownChannelGroupException。当组中的所有异步通道都关闭时，组终止，所有正在执行的完成处理程序都运行到完成，并且释放所有资源。如果组已经关闭，则此方法无效。
     */
    public abstract void shutdown();

    /**
     * Shuts down the group and closes all open channels in the group.
     *
     * <p> In addition to the actions performed by the {@link #shutdown() shutdown}
     * method, this method invokes the {@link AsynchronousChannel#close close}
     * method on all open channels in the group. This method does not attempt to
     * stop or interrupt threads that are executing completion handlers. The
     * group terminates when all actively executing completion handlers have run
     * to completion and all resources have been released. This method may be
     * invoked at any time. If some other thread has already invoked it, then
     * another invocation will block until the first invocation is complete,
     * after which it will return without effect.
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          关闭组并关闭组中的所有开放通道。
    除了关闭方法执行的操作之外，该方法还调用组中所有打开通道上的close方法。此方法不尝试停止或中断正在执行完成处理程序的线程。当所有积极执行的完成处理程序运行到完成时，并且释放了所有资源时，组终止。此方法可以随时调用。如果其他一些线程已经调用了它，那么另一个调用将被阻塞，直到第一个调用完成，然后它将返回而不产生任何效果。
     */
    public abstract void shutdownNow() throws IOException;

    /**
     * Awaits termination of the group.

     * <p> This method blocks until the group has terminated, or the timeout
     * occurs, or the current thread is interrupted, whichever happens first.
     *
     * @param   timeout
     *          The maximum time to wait, or zero or less to not wait
     * @param   unit
     *          The time unit of the timeout argument
     *
     * @return  {@code true} if the group has terminated; {@code false} if the
     *          timeout elapsed before termination
     *
     * @throws  InterruptedException
     *          If interrupted while waiting
     *          等待组终止。
    此方法将阻塞，直到组终止，或超时发生，或当前线程被中断，无论发生哪种情况。
     */
    public abstract boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;
}
