/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/**
 * An {@link ExecutorService} that executes each submitted task using
 * one of possibly several pooled threads, normally configured
 * using {@link Executors} factory methods.
 *
 * <p>Thread pools address two different problems: they usually
 * provide improved performance when executing large numbers of
 * asynchronous tasks, due to reduced per-task invocation overhead,
 * and they provide a means of bounding and managing the resources,
 * including threads, consumed when executing a collection of tasks.
 * Each {@code ThreadPoolExecutor} also maintains some basic
 * statistics, such as the number of completed tasks.
 *
 * <p>To be useful across a wide range of contexts, this class
 * provides many adjustable parameters and extensibility
 * hooks. However, programmers are urged to use the more convenient
 * {@link Executors} factory methods {@link
 * Executors#newCachedThreadPool} (unbounded thread pool, with
 * automatic thread reclamation), {@link Executors#newFixedThreadPool}
 * (fixed size thread pool) and {@link
 * Executors#newSingleThreadExecutor} (single background thread), that
 * preconfigure settings for the most common usage
 * scenarios. Otherwise, use the following guide when manually
 * configuring and tuning this class:
 *
 * <dl>
 *
 * <dt>Core and maximum pool sizes</dt>
 *
 * <dd>A {@code ThreadPoolExecutor} will automatically adjust the
 * pool size (see {@link #getPoolSize})
 * according to the bounds set by
 * corePoolSize (see {@link #getCorePoolSize}) and
 * maximumPoolSize (see {@link #getMaximumPoolSize}).
 *
 * When a new task is submitted in method {@link #execute(Runnable)},
 * and fewer than corePoolSize threads are running, a new thread is
 * created to handle the request, even if other worker threads are
 * idle.  If there are more than corePoolSize but less than
 * maximumPoolSize threads running, a new thread will be created only
 * if the queue is full.  By setting corePoolSize and maximumPoolSize
 * the same, you create a fixed-size thread pool. By setting
 * maximumPoolSize to an essentially unbounded value such as {@code
 * Integer.MAX_VALUE}, you allow the pool to accommodate an arbitrary
 * number of concurrent tasks. Most typically, core and maximum pool
 * sizes are set only upon construction, but they may also be changed
 * dynamically using {@link #setCorePoolSize} and {@link
 * #setMaximumPoolSize}. </dd>
 *
 * <dt>On-demand construction</dt>
 *
 * <dd>By default, even core threads are initially created and
 * started only when new tasks arrive, but this can be overridden
 * dynamically using method {@link #prestartCoreThread} or {@link
 * #prestartAllCoreThreads}.  You probably want to prestart threads if
 * you construct the pool with a non-empty queue. </dd>
 *
 * <dt>Creating new threads</dt>
 *
 * <dd>New threads are created using a {@link ThreadFactory}.  If not
 * otherwise specified, a {@link Executors#defaultThreadFactory} is
 * used, that creates threads to all be in the same {@link
 * ThreadGroup} and with the same {@code NORM_PRIORITY} priority and
 * non-daemon status. By supplying a different ThreadFactory, you can
 * alter the thread's name, thread group, priority, daemon status,
 * etc. If a {@code ThreadFactory} fails to create a thread when asked
 * by returning null from {@code newThread}, the executor will
 * continue, but might not be able to execute any tasks. Threads
 * should possess the "modifyThread" {@code RuntimePermission}. If
 * worker threads or other threads using the pool do not possess this
 * permission, service may be degraded: configuration changes may not
 * take effect in a timely manner, and a shutdown pool may remain in a
 * state in which termination is possible but not completed.</dd>
 *
 * <dt>Keep-alive times</dt>
 *
 * <dd>If the pool currently has more than corePoolSize threads,
 * excess threads will be terminated if they have been idle for more
 * than the keepAliveTime (see {@link #getKeepAliveTime(TimeUnit)}).
 * This provides a means of reducing resource consumption when the
 * pool is not being actively used. If the pool becomes more active
 * later, new threads will be constructed. This parameter can also be
 * changed dynamically using method {@link #setKeepAliveTime(long,
 * TimeUnit)}.  Using a value of {@code Long.MAX_VALUE} {@link
 * TimeUnit#NANOSECONDS} effectively disables idle threads from ever
 * terminating prior to shut down. By default, the keep-alive policy
 * applies only when there are more than corePoolSize threads. But
 * method {@link #allowCoreThreadTimeOut(boolean)} can be used to
 * apply this time-out policy to core threads as well, so long as the
 * keepAliveTime value is non-zero. </dd>
 *
 * <dt>Queuing</dt>
 *
 * <dd>Any {@link BlockingQueue} may be used to transfer and hold
 * submitted tasks.  The use of this queue interacts with pool sizing:
 *
 * <ul>
 *
 * <li> If fewer than corePoolSize threads are running, the Executor
 * always prefers adding a new thread
 * rather than queuing.</li>
 *
 * <li> If corePoolSize or more threads are running, the Executor
 * always prefers queuing a request rather than adding a new
 * thread.</li>
 *
 * <li> If a request cannot be queued, a new thread is created unless
 * this would exceed maximumPoolSize, in which case, the task will be
 * rejected.</li>
 *
 * </ul>
 *
 * There are three general strategies for queuing:
 * <ol>
 *
 * <li> <em> Direct handoffs.</em> A good default choice for a work
 * queue is a {@link SynchronousQueue} that hands off tasks to threads
 * without otherwise holding them. Here, an attempt to queue a task
 * will fail if no threads are immediately available to run it, so a
 * new thread will be constructed. This policy avoids lockups when
 * handling sets of requests that might have internal dependencies.
 * Direct handoffs generally require unbounded maximumPoolSizes to
 * avoid rejection of new submitted tasks. This in turn admits the
 * possibility of unbounded thread growth when commands continue to
 * arrive on average faster than they can be processed.  </li>
 *
 * <li><em> Unbounded queues.</em> Using an unbounded queue (for
 * example a {@link LinkedBlockingQueue} without a predefined
 * capacity) will cause new tasks to wait in the queue when all
 * corePoolSize threads are busy. Thus, no more than corePoolSize
 * threads will ever be created. (And the value of the maximumPoolSize
 * therefore doesn't have any effect.)  This may be appropriate when
 * each task is completely independent of others, so tasks cannot
 * affect each others execution; for example, in a web page server.
 * While this style of queuing can be useful in smoothing out
 * transient bursts of requests, it admits the possibility of
 * unbounded work queue growth when commands continue to arrive on
 * average faster than they can be processed.  </li>
 *
 * <li><em>Bounded queues.</em> A bounded queue (for example, an
 * {@link ArrayBlockingQueue}) helps prevent resource exhaustion when
 * used with finite maximumPoolSizes, but can be more difficult to
 * tune and control.  Queue sizes and maximum pool sizes may be traded
 * off for each other: Using large queues and small pools minimizes
 * CPU usage, OS resources, and context-switching overhead, but can
 * lead to artificially low throughput.  If tasks frequently block (for
 * example if they are I/O bound), a system may be able to schedule
 * time for more threads than you otherwise allow. Use of small queues
 * generally requires larger pool sizes, which keeps CPUs busier but
 * may encounter unacceptable scheduling overhead, which also
 * decreases throughput.  </li>
 *
 * </ol>
 *
 * </dd>
 *
 * <dt>Rejected tasks</dt>
 *
 * <dd>New tasks submitted in method {@link #execute(Runnable)} will be
 * <em>rejected</em> when the Executor has been shut down, and also when
 * the Executor uses finite bounds for both maximum threads and work queue
 * capacity, and is saturated.  In either case, the {@code execute} method
 * invokes the {@link
 * RejectedExecutionHandler#rejectedExecution(Runnable, ThreadPoolExecutor)}
 * method of its {@link RejectedExecutionHandler}.  Four predefined handler
 * policies are provided:
 *
 * <ol>
 *
 * <li> In the default {@link ThreadPoolExecutor.AbortPolicy}, the
 * handler throws a runtime {@link RejectedExecutionException} upon
 * rejection. </li>
 *
 * <li> In {@link ThreadPoolExecutor.CallerRunsPolicy}, the thread
 * that invokes {@code execute} itself runs the task. This provides a
 * simple feedback control mechanism that will slow down the rate that
 * new tasks are submitted. </li>
 *
 * <li> In {@link ThreadPoolExecutor.DiscardPolicy}, a task that
 * cannot be executed is simply dropped.  </li>
 *
 * <li>In {@link ThreadPoolExecutor.DiscardOldestPolicy}, if the
 * executor is not shut down, the task at the head of the work queue
 * is dropped, and then execution is retried (which can fail again,
 * causing this to be repeated.) </li>
 *
 * </ol>
 *
 * It is possible to define and use other kinds of {@link
 * RejectedExecutionHandler} classes. Doing so requires some care
 * especially when policies are designed to work only under particular
 * capacity or queuing policies. </dd>
 *
 * <dt>Hook methods</dt>
 *
 * <dd>This class provides {@code protected} overridable
 * {@link #beforeExecute(Thread, Runnable)} and
 * {@link #afterExecute(Runnable, Throwable)} methods that are called
 * before and after execution of each task.  These can be used to
 * manipulate the execution environment; for example, reinitializing
 * ThreadLocals, gathering statistics, or adding log entries.
 * Additionally, method {@link #terminated} can be overridden to perform
 * any special processing that needs to be done once the Executor has
 * fully terminated.
 *
 * <p>If hook or callback methods throw exceptions, internal worker
 * threads may in turn fail and abruptly terminate.</dd>
 *
 * <dt>Queue maintenance</dt>
 *
 * <dd>Method {@link #getQueue()} allows access to the work queue
 * for purposes of monitoring and debugging.  Use of this method for
 * any other purpose is strongly discouraged.  Two supplied methods,
 * {@link #remove(Runnable)} and {@link #purge} are available to
 * assist in storage reclamation when large numbers of queued tasks
 * become cancelled.</dd>
 *
 * <dt>Finalization</dt>
 *
 * <dd>A pool that is no longer referenced in a program <em>AND</em>
 * has no remaining threads will be {@code shutdown} automatically. If
 * you would like to ensure that unreferenced pools are reclaimed even
 * if users forget to call {@link #shutdown}, then you must arrange
 * that unused threads eventually die, by setting appropriate
 * keep-alive times, using a lower bound of zero core threads and/or
 * setting {@link #allowCoreThreadTimeOut(boolean)}.  </dd>
 *
 * </dl>
 *
 * <p><b>Extension example</b>. Most extensions of this class
 * override one or more of the protected hook methods. For example,
 * here is a subclass that adds a simple pause/resume feature:
 *
 *  <pre> {@code
 * class PausableThreadPoolExecutor extends ThreadPoolExecutor {
 *   private boolean isPaused;
 *   private ReentrantLock pauseLock = new ReentrantLock();
 *   private Condition unpaused = pauseLock.newCondition();
 *
 *   public PausableThreadPoolExecutor(...) { super(...); }
 *
 *   protected void beforeExecute(Thread t, Runnable r) {
 *     super.beforeExecute(t, r);
 *     pauseLock.lock();
 *     try {
 *       while (isPaused) unpaused.await();
 *     } catch (InterruptedException ie) {
 *       t.interrupt();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void pause() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = true;
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void resume() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = false;
 *       unpaused.signalAll();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 * 一个ExecutorService，使用可能的几个池线程之一执行每个提交的任务，通常使用Executors工厂方法配置。
线程池解决了两个不同的问题:它们通常在执行大量异步任务时提供更好的性能，这是由于减少了每个任务的调用开销，它们还提供了一种绑定和管理资源(包括在执行任务集合时消耗的线程)的方法。每个ThreadPoolExecutor还维护一些基本统计信息，比如完成任务的数量。
为了在广泛的上下文中发挥作用，该类提供了许多可调参数和可扩展性挂钩。但是，我们敦促程序员使用更方便的执行程序工厂方法执行程序。newCachedThreadPool(具有自动线程回收的无界线程池)，执行程序。newFixedThreadPool(固定大小的线程池)和执行程序。newSingleThreadExecutor(单后台线程)，为最常见的使用场景预配置设置。否则，在手动配置和调优该类时，请使用以下指南:
核心和最大池大小
ThreadPoolExecutor将根据corePoolSize(参见getCorePoolSize)和maximumPoolSize(参见getMaximumPoolSize)设置的边界自动调整池大小(请参见getMaximumPoolSize)。当在方法执行(Runnable)中提交一个新任务时，即使其他工作线程空闲，也会创建一个新线程来处理请求。如果运行的线程超过了corePoolSize但小于maximumPoolSize，那么只有当队列满时，才会创建一个新的线程。通过设置corePoolSize和maximumPoolSize，您可以创建一个固定大小的线程池。通过将极大值设置为一个本质上无界的值(如整数)。MAX_VALUE允许池容纳任意数量的并发任务。大多数情况下，只在构造时设置核心和最大池大小，但是也可以使用setCorePoolSize和setMaximumPoolSize动态地更改它们。
核心和最大池大小
按需建设
默认情况下，甚至连核心线程都是最初创建的，并且只在新任务到达时才启动，但是可以使用方法prestartCoreThread或prestartAllCoreThreads动态覆盖这个线程。如果使用非空队列构造池，您可能希望预先启动线程。
创建新线程
使用ThreadFactory创建新线程。如果没有指定，则使用Executors.defaultThreadFactory，它创建的线程都位于同一个ThreadGroup中，并且具有相同的NORM_PRIORITY优先级和非守护进程状态。通过提供不同的ThreadFactory，您可以更改线程的名称、线程组、优先级、守护进程状态等。如果当从newThread返回null时，ThreadFactory未能创建线程，那么执行程序将继续执行，但可能无法执行任何任务。线程应该具有“modifyThread”运行时权限。如果使用池的工作线程或其他线程不具有此权限，则服务可能会被降级:配置更改可能不会及时生效，而关闭池可能仍然处于可能终止但尚未完成的状态。
保活时间
如果池当前拥有超过corePoolSize的线程，那么如果空闲时间超过了keepAliveTime(参见getKeepAliveTime(TimeUnit)))，多余的线程将被终止。当池没有被积极使用时，这提供了一种减少资源消耗的方法。如果池稍后变得更活跃，将构造新的线程。该参数也可以使用方法setKeepAliveTime(long, TimeUnit)动态更改。使用长值。MAX_VALUE TimeUnit。纳秒有效地禁止空闲线程在关闭之前终止。默认情况下，keep-alive策略只适用于有大于corePoolSize线程的情况。但是，方法allowCoreThreadTimeOut(boolean)可以用于将这个超时策略应用到核心线程上，只要keepAliveTime值不为零。
排队
任何阻塞队列都可以用来传输和保存提交的任务。这个队列的使用与池大小交互:
如果运行的线程少于corePoolSize的线程，那么执行程序总是喜欢添加新线程，而不是排队。
如果corePoolSize或更多的线程正在运行，执行程序总是喜欢排队请求，而不是添加一个新的线程。
如果请求不能排队，那么将创建一个新的线程，除非这个线程超过maximumPoolSize，在这种情况下，该任务将被拒绝。
排队的一般策略有三种:
直接的传递。对于工作队列来说，一个很好的默认选择是同步队列，它将任务交给线程，而不需要其他方式来保存它们。在这里，如果没有立即可用的线程来运行任务，那么对任务进行排队的尝试将失败，因此将构造一个新的线程。当处理可能具有内部依赖关系的请求集时，此策略避免了锁定。直接传递通常需要无界的极大值，以避免拒绝新提交的任务。反过来，当命令继续以平均快于它们可以被处理的速度到达时，这就允许了无限制的线程增长的可能性。
无界队列。使用无界队列(例如没有预定义容量的LinkedBlockingQueue)将导致在所有corePoolSize线程都繁忙时在队列中等待新的任务。因此，只会创建corePoolSize线程。(因此最大容量的值没有任何影响。)当每个任务完全独立于其他任务时，这可能是适当的，因此任务不能影响彼此的执行;例如，在web页面服务器中。尽管这种排队方式在消除请求的短暂爆发方面很有用，但它承认，当命令继续以平均快于处理速度到达时，可能会出现无限制的工作队列增长。
有界的队列。有界队列(例如ArrayBlockingQueue)在使用有限的最大字号时有助于防止资源耗尽，但可能更难调优和控制。队列大小和最大池大小可以相互交换:使用大型队列和小池可以最小化CPU使用、操作系统资源和上下文切换开销，但是会导致人为的低吞吐量。如果任务经常被阻塞(例如，如果它们是I/O绑定的)，那么系统可能可以为更多的线程调度时间，而您不允许这样做。使用小队列通常需要更大的池大小，这使cpu更忙，但可能会遇到无法接受的调度开销，这也会降低吞吐量。

拒绝接受任务
在方法execute(Runnable)中提交的新任务将在执行程序被关闭时被拒绝，当执行程序对最大线程和工作队列容量都使用有限的界限并且饱和时也被拒绝。无论哪种情况，execute方法都调用RejectedExecutionHandler。rejectedExecution(Runnable, ThreadPoolExecutor)方法的RejectedExecutionHandler。提供了四个预定义的处理程序策略:
在默认ThreadPoolExecutor。处理程序在拒绝时抛出运行时RejectedExecutionException。
在ThreadPoolExecutor。调用执行本身的线程CallerRunsPolicy运行任务。这提供了一个简单的反馈控制机制，可以降低提交新任务的速度。
在ThreadPoolExecutor。无法执行的任务将被删除。
在ThreadPoolExecutor。丢弃doldestpolicy，如果执行程序没有关闭，那么将删除工作队列前面的任务，然后重试执行(它可能再次失败，导致重复执行)。
可以定义和使用其他类型的RejectedExecutionHandler类。这样做需要特别小心，特别是当策略被设计为仅在特定的容量或队列策略下工作时。
钩方法
这个类提供了在执行每个任务之前和之后调用的(线程、可运行的)和afterExecute(可运行的、可抛出的)方法的保护可重写的方法。这些可用于操作执行环境;例如，重新初始化threadlocal、收集统计信息或添加日志条目。此外，终止方法可以被重写，以便在执行程序完全终止后执行任何需要执行的特殊处理。
如果钩子或回调方法抛出异常，内部工作线程可能反过来失败并突然终止。
队列的维护
getQueue()方法允许为监视和调试目的访问工作队列。强烈反对将这种方法用于任何其他目的。当大量排队任务被取消时，可以使用两种提供的方法:删除(Runnable)和清除来帮助进行存储回收。
终结
一个在程序中不再被引用并且没有剩余线程的池将自动关闭。如果您希望确保即使用户忘记调用shutdown，也要回收未引用的池，那么您必须通过设置适当的保持活动时间、使用较低的零核心线程和/或设置allowCoreThreadTimeOut(boolean)来安排未使用的线程最终死亡。
 */
//线程池工作原理
// 设置 corePoolSize 要小于 maximumPoolSize，corePoolSize+queueSize <= maximumPoolSize
//   线程池接收到任务的时候，如果任务数小于等于 corePoolSize，会立即创建线程来执行任务，corePoolSize的线程等任务进来后才会创建，可以调用prestartCoreThread or prestartAllCoreThreads方法立即创建
//    corePoolSize 数量用完以后，假设corePoolSize的线程任务还在执行，再有任务进来，进来的任务数小于等于 queueSize，进来的任务会存进队列（假设队列是有界队列）
//    假设corePoolSize的线程任务还在执行，队列长度也用完了，这时会再进来的任务数+corePoolSize+queueSize小于等于 maximumPoolSize，再进来的任务线程池会立即创建线程去执行，如果大于maximumPoolSize线程池走拒绝策略
//    如果池当前拥有超过corePoolSize的线程，那么如果空闲时间超过了keepAliveTime(参见getKeepAliveTime(TimeUnit)))，多余的线程将被终止，keep-alive策略只适用于有大于corePoolSize线程的情况。但是，方法allowCoreThreadTimeOut(boolean)可以用于将这个超时策略应用到核心线程上，只要keepAliveTime值不为零
public class ThreadPoolExecutor extends AbstractExecutorService {
    /**
     * The main pool control state, ctl, is an atomic integer packing
     * two conceptual fields
     *   workerCount, indicating the effective number of threads
     *   runState,    indicating whether running, shutting down etc
     *
     * In order to pack them into one int, we limit workerCount to
     * (2^29)-1 (about 500 million) threads rather than (2^31)-1 (2
     * billion) otherwise representable. If this is ever an issue in
     * the future, the variable can be changed to be an AtomicLong,
     * and the shift/mask constants below adjusted. But until the need
     * arises, this code is a bit faster and simpler using an int.
     *
     * The workerCount is the number of workers that have been
     * permitted to start and not permitted to stop.  The value may be
     * transiently different from the actual number of live threads,
     * for example when a ThreadFactory fails to create a thread when
     * asked, and when exiting threads are still performing
     * bookkeeping before terminating. The user-visible pool size is
     * reported as the current size of the workers set.
     *
     * The runState provides the main lifecycle control, taking on values:
     *
     *   RUNNING:  Accept new tasks and process queued tasks
     *   SHUTDOWN: Don't accept new tasks, but process queued tasks
     *   STOP:     Don't accept new tasks, don't process queued tasks,
     *             and interrupt in-progress tasks
     *   TIDYING:  All tasks have terminated, workerCount is zero,
     *             the thread transitioning to state TIDYING
     *             will run the terminated() hook method
     *   TERMINATED: terminated() has completed
     *
     * The numerical order among these values matters, to allow
     * ordered comparisons. The runState monotonically increases over
     * time, but need not hit each state. The transitions are:
     *
     * RUNNING -> SHUTDOWN
     *    On invocation of shutdown(), perhaps implicitly in finalize()
     * (RUNNING or SHUTDOWN) -> STOP
     *    On invocation of shutdownNow()
     * SHUTDOWN -> TIDYING
     *    When both queue and pool are empty
     * STOP -> TIDYING
     *    When pool is empty
     * TIDYING -> TERMINATED
     *    When the terminated() hook method has completed
     *
     * Threads waiting in awaitTermination() will return when the
     * state reaches TERMINATED.
     *
     * Detecting the transition from SHUTDOWN to TIDYING is less
     * straightforward than you'd like because the queue may become
     * empty after non-empty and vice versa during SHUTDOWN state, but
     * we can only terminate if, after seeing that it is empty, we see
     * that workerCount is 0 (which sometimes entails a recheck -- see
     * below).
     * 主池控制状态、细胞毒性t淋巴细胞是一个原子整数包装两个概念字段workerCount、指示的有效数量的线程runState,指示是否运行,关闭等为了装成一个int,我们限制workerCount(2 ^ 29)1(约5亿)线程而不是(2 ^ 31)1(20亿)否则能上演的。
     * 如果将来这是一个问题，可以将变量改为AtomicLong，并调整下面的shift/mask常量。但是在需要之前，使用int类型的代码会更快、更简单一些，workerCount是允许开始而不允许停止的工人数量。这个值可能与实际的活动线程数有短暂的不同，
     * 例如当线程工厂在被请求时没有创建线程，当退出的线程在终止之前仍在执行簿记时。用户可见池大小报告为当前大小的工人。runState提供主要的生命周期控制,在价值观:运行:接受新任务和过程队列任务关闭:不接受新任务,但进程队列任务停止:
     * 不接受新任务,不过程排队任务,整理和中断正在进行的任务:所有任务终止,workerCount是零,过渡到状态整理会终止的线程()挂钩方法终止:终止()完成了这些值之间的数值排序，以允许有序比较。运行状态会随着时间单调地增加，但不需要触及每个状态。
     * 转换:在调用运行- >关闭关闭(),也许隐含在finalize()(运行或关闭)- >站调用shutdownNow关闭()- >整理当队列和池是空的停止- >整理池为空时整理- >终止时终止()挂钩方法完成线程等待awaitTermination()将返回当国家达到终止。
     * 检测从关闭过渡到整理不如你想因为简单队列可能成为空后非空,反之亦然在关闭状态,但我们只能终止,如果在看到它是空的,我们看到workerCount 0(有时需要复核,见下文)。
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));//-536870912
    private static final int COUNT_BITS = Integer.SIZE - 3;//29
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;//536870911

    // runState is stored in the high-order bits
    private static final int RUNNING    = -1 << COUNT_BITS;//-536870912
    private static final int SHUTDOWN   =  0 << COUNT_BITS;//0
    private static final int STOP       =  1 << COUNT_BITS;//536870912
    private static final int TIDYING    =  2 << COUNT_BITS;//1073741824
    private static final int TERMINATED =  3 << COUNT_BITS;//1610612736

    // Packing and unpacking ctl
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    /*
     * Bit field accessors that don't require unpacking ctl.
     * These depend on the bit layout and on workerCount being never negative.
     */

    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }

    /**
     * Attempts to CAS-increment the workerCount field of ctl.尝试增加ctl的workerCount字段。
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    /**
     * Attempts to CAS-decrement the workerCount field of ctl.尝试对ctl的workerCount字段进行递减。
     */
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    /**
     * Decrements the workerCount field of ctl. This is called only on
     * abrupt termination of a thread (see processWorkerExit). Other
     * decrements are performed within getTask.对ctl的工作计数场进行了衰减。这只在线程突然终止时调用(参见processWorkerExit)。在getTask内执行其他减数。
     */
    private void decrementWorkerCount() {
        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
    }

    /**
     * The queue used for holding tasks and handing off to worker
     * threads.  We do not require that workQueue.poll() returning
     * null necessarily means that workQueue.isEmpty(), so rely
     * solely on isEmpty to see if the queue is empty (which we must
     * do for example when deciding whether to transition from
     * SHUTDOWN to TIDYING).  This accommodates special-purpose
     * queues such as DelayQueues for which poll() is allowed to
     * return null even if it may later return non-null when delays
     * expire.
     * 用于保存任务并将任务分发给工作线程的队列。我们不需要workQueue.poll()返回null，这就意味着workQueue.isEmpty()，
     * 因此只能依赖isEmpty来查看队列是否为空(例如，在决定是否从关机过渡到清理时，我们必须这样做)。这可以容纳特殊目的的队列，比如可以返回null的DelayQueues()，即使它在延迟到期时可能返回非null。
     */
    private final BlockingQueue<Runnable> workQueue;

    /**
     * Lock held on access to workers set and related bookkeeping.
     * While we could use a concurrent set of some sort, it turns out
     * to be generally preferable to use a lock. Among the reasons is
     * that this serializes interruptIdleWorkers, which avoids
     * unnecessary interrupt storms, especially during shutdown.
     * Otherwise exiting threads would concurrently interrupt those
     * that have not yet interrupted. It also simplifies some of the
     * associated statistics bookkeeping of largestPoolSize etc. We
     * also hold mainLock on shutdown and shutdownNow, for the sake of
     * ensuring workers set is stable while separately checking
     * permission to interrupt and actually interrupting.
     * 锁上了工人设置和相关的簿记。虽然我们可以使用某种并发集合，但通常最好使用锁。原因之一是它序列化中断工作程序，这避免了不必要的中断风暴，
     * 特别是在关闭期间。否则退出线程将同时中断尚未中断的线程。它还简化了一些与之相关的最大的统计信息簿记等。我们还在关机和关闭时保持主锁，以确保工作人员设置是稳定的，同时分别检查中断权限和实际中断。
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * Set containing all worker threads in pool. Accessed only when
     * holding mainLock.设置包含池中的所有工作线程。只在保持主锁时访问。
     */
    private final HashSet<Worker> workers = new HashSet<Worker>();

    /**
     * Wait condition to support awaitTermination等待状态支持等待终止
     */
    private final Condition termination = mainLock.newCondition();

    /**
     * Tracks largest attained pool size. Accessed only under
     * mainLock.跟踪最大的池大小。只有在mainLock访问。
     */
    private int largestPoolSize;

    /**
     * Counter for completed tasks. Updated only on termination of
     * worker threads. Accessed only under mainLock.计数器来完成任务。只在工作线程终止时更新。只有在mainLock访问。
     */
    private long completedTaskCount;

    /*
     * All user control parameters are declared as volatiles so that
     * ongoing actions are based on freshest values, but without need
     * for locking, since no internal invariants depend on them
     * changing synchronously with respect to other actions.所有用户控制参数都声明为挥发物，以便
正在进行的行动基于最新的价值观，但没有必要
*用于锁定，因为没有内部不变量依赖于它们
*与其他动作同步改变。
     */

    /**
     * Factory for new threads. All threads are created using this
     * factory (via method addWorker).  All callers must be prepared
     * for addWorker to fail, which may reflect a system or user's
     * policy limiting the number of threads.  Even though it is not
     * treated as an error, failure to create threads may result in
     * new tasks being rejected or existing ones remaining stuck in
     * the queue.
     *
     * We go further and preserve pool invariants even in the face of
     * errors such as OutOfMemoryError, that might be thrown while
     * trying to create threads.  Such errors are rather common due to
     * the need to allocate a native stack in Thread.start, and users
     * will want to perform clean pool shutdown to clean up.  There
     * will likely be enough memory available for the cleanup code to
     * complete without encountering yet another OutOfMemoryError.
     * 工厂对新线程。所有线程都是使用这个工厂(通过方法addWorker)创建的。所有调用者都必须为addWorker的失败做好准备，
     * 这可能反映了系统或用户限制线程数量的策略。即使没有将其视为错误，创建线程的失败也可能导致拒绝新的任务，或者将现有任务留在队列中。我们进一步保留了池不变量，
     * 即使遇到诸如OutOfMemoryError之类的错误，在尝试创建线程时可能会抛出这些错误。由于需要在线程中分配本机堆栈，所以这种错误非常常见。
     * 启动时，用户将希望执行清理池关闭来清理。清理代码可能有足够的内存来完成，而不会遇到另一个OutOfMemoryError。
     */
    private volatile ThreadFactory threadFactory;

    /**
     * Handler called when saturated or shutdown in execute.处理程序在执行中饱和或关闭时调用。
     */
    private volatile RejectedExecutionHandler handler;

    /**
     * Timeout in nanoseconds for idle threads waiting for work.
     * Threads use this timeout when there are more than corePoolSize
     * present or if allowCoreThreadTimeOut. Otherwise they wait
     * forever for new work.等待工作的空闲线程的超时(以纳秒为单位)。当出现大于corePoolSize的时候，或者允许corethreadtimeout时，线程会使用这个超时。否则他们会永远等待新的工作。
     */
    private volatile long keepAliveTime;

    /**
     * If false (default), core threads stay alive even when idle.
     * If true, core threads use keepAliveTime to time out waiting
     * for work.如果为false(缺省值)，那么即使在空闲时，核心线程仍然保持活动状态。如果是，核心线程使用keepAliveTime来超时等待工作。
     */
    private volatile boolean allowCoreThreadTimeOut;

    /**
     * Core pool size is the minimum number of workers to keep alive
     * (and not allow to time out etc) unless allowCoreThreadTimeOut
     * is set, in which case the minimum is zero.核心池大小是保持工作的最小数量(不允许超时等)，除非设置allowCoreThreadTimeOut，在这种情况下最小值为零。
     */
    private volatile int corePoolSize;

    /**
     * Maximum pool size. Note that the actual maximum is internally
     * bounded by CAPACITY.最大池大小。注意，实际的最大容量在内部受到容量的限制。
     */
    private volatile int maximumPoolSize;

    /**
     * The default rejected execution handler默认的拒绝执行处理程序。
     */
//    默认拒绝策略，直接抛出拒绝异常
    private static final RejectedExecutionHandler defaultHandler =
        new AbortPolicy();

    /**
     * Permission required for callers of shutdown and shutdownNow.
     * We additionally require (see checkShutdownAccess) that callers
     * have permission to actually interrupt threads in the worker set
     * (as governed by Thread.interrupt, which relies on
     * ThreadGroup.checkAccess, which in turn relies on
     * SecurityManager.checkAccess). Shutdowns are attempted only if
     * these checks pass.
     *
     * All actual invocations of Thread.interrupt (see
     * interruptIdleWorkers and interruptWorkers) ignore
     * SecurityExceptions, meaning that the attempted interrupts
     * silently fail. In the case of shutdown, they should not fail
     * unless the SecurityManager has inconsistent policies, sometimes
     * allowing access to a thread and sometimes not. In such cases,
     * failure to actually interrupt threads may disable or delay full
     * termination. Other uses of interruptIdleWorkers are advisory,
     * and failure to actually interrupt will merely delay response to
     * configuration changes so is not handled exceptionally.
     * 关闭和关闭呼叫者所需的权限。我们还要求(参见checkShutdownAccess)调用者具有在worker集中实际中断线程的权限(由依赖于ThreadGroup的Thread.interrupt管理)。
     * checkAccess反过来依赖于SecurityManager.checkAccess)。只有当这些检查通过时，才会尝试关闭。所有对Thread.interrupt的实际调用(请参阅interruptidleworker和interruptworker)都忽略securityexception，
     * 这意味着尝试的中断会无声地失败。在关闭的情况下，除非SecurityManager具有不一致的策略(有时允许访问线程，有时不允许访问)，否则它们不应该失败。在这种情况下，未能真正中断线程可能会禁用或延迟完全终止。
     * interruptidleworker的其他用途是建议性的，如果不能实际中断，只会延迟对配置更改的响应，所以不会被异常处理。
     */
    private static final RuntimePermission shutdownPerm =
        new RuntimePermission("modifyThread");

    /* The context to be used when executing the finalizer, or null. */
    private final AccessControlContext acc;

    /**
     * Class Worker mainly maintains interrupt control state for
     * threads running tasks, along with other minor bookkeeping.
     * This class opportunistically extends AbstractQueuedSynchronizer
     * to simplify acquiring and releasing a lock surrounding each
     * task execution.  This protects against interrupts that are
     * intended to wake up a worker thread waiting for a task from
     * instead interrupting a task being run.  We implement a simple
     * non-reentrant mutual exclusion lock rather than use
     * ReentrantLock because we do not want worker tasks to be able to
     * reacquire the lock when they invoke pool control methods like
     * setCorePoolSize.  Additionally, to suppress interrupts until
     * the thread actually starts running tasks, we initialize lock
     * state to a negative value, and clear it upon start (in
     * runWorker).
     * 类Worker主要维护线程运行任务的中断控制状态，以及其他小簿记。这个类机会主义地扩展了AbstractQueuedSynchronizer，以简化获取和释放一个围绕每个任务执行的锁。
     * 这可以防止旨在唤醒等待任务的工作线程而不是中断正在运行的任务的中断。我们实现了一个简单的不可重入的互斥锁，而不是使用ReentrantLock，因为我们不希望工人任务在调用setCorePoolSize这样的池控制方法时能够重新获得锁。
     * 此外，为了抑制中断直到线程真正开始运行任务，我们将锁状态初始化为负值，并在启动时清除它(在runWorker中)。
     */
    private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        /**
         * This class will never be serialized, but we provide a
         * serialVersionUID to suppress a javac warning.
         */
        private static final long serialVersionUID = 6138294804551838833L;

        /** Thread this worker is running in.  Null if factory fails. 这个worker正在运行。如果工厂没有空。*/
        final Thread thread;
        /** Initial task to run.  Possibly null. 最初的任务运行。可能是零。*/
        Runnable firstTask;
        /** Per-thread task counter 线程任务计数器*/
        volatile long completedTasks;

        /**
         * Creates with given first task and thread from ThreadFactory.使用给定的第一个任务和线程从ThreadFactory创建。
         * @param firstTask the first task (null if none)
         */
        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker 禁止中断，直到运行工作程序
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /** Delegates main run loop to outer runWorker  将主运行循环委托给外部运行程序*/
        public void run() {
            runWorker(this);
        }

        // Lock methods
        //
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /*
     * Methods for setting control state
     */

    /**
     * Transitions runState to given target, or leaves it alone if
     * already at least the given target.将runState转换为给定的目标，或者至少保留给定的目标。
     *
     * @param targetState the desired state, either SHUTDOWN or STOP
     *        (but not TIDYING or TERMINATED -- use tryTerminate for that)
     */
    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    /**
     * Transitions to TERMINATED state if either (SHUTDOWN and pool
     * and queue empty) or (STOP and pool empty).  If otherwise
     * eligible to terminate but workerCount is nonzero, interrupts an
     * idle worker to ensure that shutdown signals propagate. This
     * method must be called following any action that might make
     * termination possible -- reducing worker count or removing tasks
     * from the queue during shutdown. The method is non-private to
     * allow access from ScheduledThreadPoolExecutor.
     * 如果(关闭和池和队列为空)或(停止和池为空)，则转换为终止状态。如果有资格终止，但workerCount是非零的，则中断一个空闲的worker，
     * 以确保关闭信号传播。必须在任何可能导致终止的操作之后调用此方法——在关闭期间减少工作人员数量或从队列中删除任务。该方法是非私有的，允许从ScheduledThreadPoolExecutor访问。
     */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
//            线程池正在运行 || 线程池状态终止 || （线程池关闭 && 队列不为空）
            if (isRunning(c) ||
                runStateAtLeast(c, TIDYING) ||
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            if (workerCountOf(c) != 0) { // Eligible to terminate
//                中断一个正在等待任务的线程
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }

    /*
     * Methods for controlling interrupts to worker threads.
     */

    /**
     * If there is a security manager, makes sure caller has
     * permission to shut down threads in general (see shutdownPerm).
     * If this passes, additionally makes sure the caller is allowed
     * to interrupt each worker thread. This might not be true even if
     * first check passed, if the SecurityManager treats some threads
     * specially.如果有一个安全管理器，请确保调用者通常拥有关闭线程的权限(参见shutdownPerm)。如果通过，还要确保允许调用者中断每个worker线程。如果SecurityManager专门处理某些线程，即使第一次检查通过，也可能不是这样。
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * Interrupts all threads, even if active. Ignores SecurityExceptions
     * (in which case some threads may remain uninterrupted).中断所有线程，即使是活动的。忽略securityexception(在这种情况下，某些线程可能保持不间断)。
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Interrupts threads that might be waiting for tasks (as
     * indicated by not being locked) so they can check for
     * termination or configuration changes. Ignores
     * SecurityExceptions (in which case some threads may remain
     * uninterrupted).
     *
     * @param onlyOne If true, interrupt at most one worker. This is
     * called only from tryTerminate when termination is otherwise
     * enabled but there are still other workers.  In this case, at
     * most one waiting worker is interrupted to propagate shutdown
     * signals in case all threads are currently waiting.
     * Interrupting any arbitrary thread ensures that newly arriving
     * workers since shutdown began will also eventually exit.
     * To guarantee eventual termination, it suffices to always
     * interrupt only one idle worker, but shutdown() interrupts all
     * idle workers so that redundant workers exit promptly, not
     * waiting for a straggler task to finish.中断可能正在等待任务的线程(如未被锁定所示)，以便检查终止或配置更改。忽略securityexception(在这种情况下，某些线程可能保持不间断)。
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Common form of interruptIdleWorkers, to avoid having to
     * remember what the boolean argument means.中断处理程序的常见形式，以避免必须记住布尔参数的含义。
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * Misc utilities, most of which are also exported to
     * ScheduledThreadPoolExecutor
     */

    /**
     * Invokes the rejected execution handler for the given command.
     * Package-protected for use by ScheduledThreadPoolExecutor.为给定的命令调用被拒绝的执行处理程序。为ScheduledThreadPoolExecutor使用的包保护。
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * Performs any further cleanup following run state transition on
     * invocation of shutdown.  A no-op here, but used by
     * ScheduledThreadPoolExecutor to cancel delayed tasks.在调用关机时执行运行状态转换之后执行任何进一步的清理。这里有一个no-op，但是ScheduledThreadPoolExecutor用于取消延迟的任务。
     */
    void onShutdown() {
    }

    /**
     * State check needed by ScheduledThreadPoolExecutor to
     * enable running tasks during shutdown.ScheduledThreadPoolExecutor需要的状态检查，以便在关机期间启用运行任务。
     *
     * @param shutdownOK true if should return true if SHUTDOWN
     */
    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    /**
     * Drains the task queue into a new list, normally using
     * drainTo. But if the queue is a DelayQueue or any other kind of
     * queue for which poll or drainTo may fail to remove some
     * elements, it deletes them one by one.将任务队列放入一个新的列表中，通常使用drainTo。但是，如果队列是一个DelayQueue或任何其他类型的队列，而轮询或排水器可能无法删除某些元素，那么它将逐个删除它们。
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * Methods for creating, running and cleaning up after workers
     */

    /**
     * Checks if a new worker can be added with respect to current
     * pool state and the given bound (either core or maximum). If so,
     * the worker count is adjusted accordingly, and, if possible, a
     * new worker is created and started, running firstTask as its
     * first task. This method returns false if the pool is stopped or
     * eligible to shut down. It also returns false if the thread
     * factory fails to create a thread when asked.  If the thread
     * creation fails, either due to the thread factory returning
     * null, or due to an exception (typically OutOfMemoryError in
     * Thread.start()), we roll back cleanly.
     *
     * @param firstTask the task the new thread should run first (or
     * null if none). Workers are created with an initial first task
     * (in method execute()) to bypass queuing when there are fewer
     * than corePoolSize threads (in which case we always start one),
     * or when the queue is full (in which case we must bypass queue).
     * Initially idle threads are usually created via
     * prestartCoreThread or to replace other dying workers.
     *
     * @param core if true use corePoolSize as bound, else
     * maximumPoolSize. (A boolean indicator is used here rather than a
     * value to ensure reads of fresh values after checking other pool
     * state).
     * @return true if successful
     * 检查是否可以针对当前池状态和给定的范围(核心或最大值)添加新worker。如果是这样，那么将相应地调整worker计数，如果可能的话，将创建并启动一个新的worker，并运行firstTask作为它的第一个任务。
     * 如果池停止或有资格关闭，此方法将返回false。如果线程工厂在请求时没有创建线程，则返回false。
     * 如果线程创建失败，要么是由于线程工厂返回null，要么是由于异常(通常是线程中的OutOfMemoryError)，所以我们将干净地回滚。
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
//            线程池终止或停止 && （线程池关闭 && 第一个任务为null && 队列不为空）
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.持锁时重新检查。
//退出ThreadFactory故障或if
//锁定前关闭。
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
//                    添加完线程就开始执行
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    /**
     * Rolls back the worker thread creation.
     * - removes worker from workers, if present
     * - decrements worker count
     * - rechecks for termination, in case the existence of this
     *   worker was holding up termination回滚工作线程创建。-将工人从工人中除名，如果是在场的-减少工人的数量-重新检查终止，如果这个工人的存在阻碍终止
     */
    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null)
                workers.remove(w);
            decrementWorkerCount();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Performs cleanup and bookkeeping for a dying worker. Called
     * only from worker threads. Unless completedAbruptly is set,
     * assumes that workerCount has already been adjusted to account
     * for exit.  This method removes thread from worker set, and
     * possibly terminates the pool or replaces the worker if either
     * it exited due to user task exception or if fewer than
     * corePoolSize workers are running or queue is non-empty but
     * there are no workers.
     *
     * @param w the worker
     * @param completedAbruptly if the worker died due to user exception
     *                          为垂死的工人执行清理和记帐。只从工作线程调用。除非已经设置了complete，否则假设workerCount已经被调整为考虑退出。
     *                          该方法从worker集合中删除线程，并可能终止池或替换该worker，如果它由于用户任务异常而退出，
     *                          或者如果少于corePoolSize的工作人员正在运行或队列是非空的，但是没有工作人员。
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

//        尝试中断线程池
        tryTerminate();

        int c = ctl.get();
//        线程池关闭或者运行
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            addWorker(null, false);
        }
    }

    /**
     * Performs blocking or timed wait for a task, depending on
     * current configuration settings, or returns null if this worker
     * must exit because of any of:
     * 1. There are more than maximumPoolSize workers (due to
     *    a call to setMaximumPoolSize).
     * 2. The pool is stopped.
     * 3. The pool is shutdown and the queue is empty.
     * 4. This worker timed out waiting for a task, and timed-out
     *    workers are subject to termination (that is,
     *    {@code allowCoreThreadTimeOut || workerCount > corePoolSize})
     *    both before and after the timed wait, and if the queue is
     *    non-empty, this worker is not the last thread in the pool.
     *
     * @return task, or null if the worker must exit, in which case
     *         workerCount is decremented
     *         根据当前的配置设置，执行阻塞或计时等待任务，如果这个worker必须因为以下任何一个而退出，则返回null。有多个极大值处理程序(由于调用了setMaximumPoolSize)。
     *         2。池是停了。
     *         3所示。池被关闭，队列为空。
     *         4所示。这个worker超时等待一个任务，并且超时的worker会在超时等待之前和之后终止(也就是说，allowCoreThreadTimeOut || workerCount > corePoolSize)，
     *         如果队列不是空的，那么这个worker就不是池中的最后一个线程。
     */
    private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.线程池关闭 && (线程池停止 || 队列为空)
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
//                任务返回null
                return null;
            }

            int wc = workerCountOf(c);

            // Are workers subject to culling? 允许核心线程超时 || 工作线程数大于核心线程
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

//            (工作线程大于最大线程数 || ((允许核心线程超时 || 工作线程数大于核心线程) && 获取任务超时) && (工作线程大于1 || 队列为空)
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
//                    返回任务null
                    return null;
                continue;
            }

            try {
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    /**
     * Main worker run loop.  Repeatedly gets tasks from queue and
     * executes them, while coping with a number of issues:
     *
     * 1. We may start out with an initial task, in which case we
     * don't need to get the first one. Otherwise, as long as pool is
     * running, we get tasks from getTask. If it returns null then the
     * worker exits due to changed pool state or configuration
     * parameters.  Other exits result from exception throws in
     * external code, in which case completedAbruptly holds, which
     * usually leads processWorkerExit to replace this thread.
     *
     * 2. Before running any task, the lock is acquired to prevent
     * other pool interrupts while the task is executing, and then we
     * ensure that unless pool is stopping, this thread does not have
     * its interrupt set.
     *
     * 3. Each task run is preceded by a call to beforeExecute, which
     * might throw an exception, in which case we cause thread to die
     * (breaking loop with completedAbruptly true) without processing
     * the task.
     *
     * 4. Assuming beforeExecute completes normally, we run the task,
     * gathering any of its thrown exceptions to send to afterExecute.
     * We separately handle RuntimeException, Error (both of which the
     * specs guarantee that we trap) and arbitrary Throwables.
     * Because we cannot rethrow Throwables within Runnable.run, we
     * wrap them within Errors on the way out (to the thread's
     * UncaughtExceptionHandler).  Any thrown exception also
     * conservatively causes thread to die.
     *
     * 5. After task.run completes, we call afterExecute, which may
     * also throw an exception, which will also cause thread to
     * die. According to JLS Sec 14.20, this exception is the one that
     * will be in effect even if task.run throws.
     *
     * The net effect of the exception mechanics is that afterExecute
     * and the thread's UncaughtExceptionHandler have as accurate
     * information as we can provide about any problems encountered by
     * user code.
     *
     * @param w the worker
     *          主要工作循环运行。重复地从队列中获取任务并执行它们，同时处理一些问题:
     *          1。我们可以从一个初始任务开始，在这种情况下，我们不需要得到第一个任务。否则，只要池运行，我们就从getTask获得任务。如果返回null，那么由于池状态或配置参数的改变，worker将退出。
     *          其他出口源于异常抛出外部代码，在这种情况下，completedhalt会突然持有，这通常会导致processWorkerExit替换这个线程。
     *          2。在运行任何任务之前，在任务执行时，该锁被获取以防止其他池中断，然后我们确保除非池停止，否则该线程没有它的中断集。每个任务运行之前都有一个对beforeExecute的调用，这可能会抛出一个异常，
     *          在这种情况下，我们会导致线程在没有处理任务的情况下，导致线程死亡(这是一个突然发生的错误循环)。4所示。假设beforeExecute正常完成，我们将运行任务，收集它抛出的任何异常以发送给afterExecute。
     *          我们分别处理运行时异常、错误(这两个规范都保证了我们的陷阱)和任意可扔的东西。因为我们不能在runnable. .run中重新抛出抛出的东西，所以我们在输出的时候将它们封装在错误中(对于线程的UncaughtExceptionHandler)。
     *          任何抛出的异常也会导致线程死亡。5。在task.run完成后，我们调用afterExecute，它也可能抛出异常，这也将导致线程死亡。根据JLS的第14.20条，即使task.run抛出，这个例外也将生效。
     *          异常机制的净效果是，在执行后，线程的UncaughtExceptionHandler可以提供关于用户代码遇到的任何问题的准确信息。
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt 如果池停止，确保线程被中断;
//如果没有，请确保线程没有中断。这
//需要在第二种情况下重新检查
//关闭现在比赛，同时清除中断
//                (线程池停止或被中断 || (当前线程被中断 && 线程池被停止或中断)) && 当前线程没被中断
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
//                    线程任务执行前执行的方法，钩子方法，可以对上下文做一些操作
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
//                        线程任务执行后的方法，钩子方法，可以对上下文做一些操作
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    // Public constructors and methods

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory and rejected execution handler.
     * It may be more convenient to use one of the {@link Executors} factory
     * methods instead of this general purpose constructor.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} is null
     * 使用给定的初始参数和默认的线程工厂并拒绝执行处理程序创建一个新的ThreadPoolExecutor。使用一个Executors factory方法而不是这个通用构造函数可能更方便。
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default rejected execution handler.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} is null
     *         创建具有给定初始参数和默认拒绝执行处理程序的新ThreadPoolExecutor。
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory, defaultHandler);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code handler} is null使用给定的初始参数和默认的线程工厂创建一个新的ThreadPoolExecutor。
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * Executes the given task sometime in the future.  The task
     * may execute in a new thread or in an existing pooled thread.
     *
     * If the task cannot be submitted for execution, either because this
     * executor has been shutdown or because its capacity has been reached,
     * the task is handled by the current {@code RejectedExecutionHandler}.
     *
     * @param command the task to execute
     * @throws RejectedExecutionException at discretion of
     *         {@code RejectedExecutionHandler}, if the task
     *         cannot be accepted for execution
     * @throws NullPointerException if {@code command} is null
     * 在将来的某个时候执行给定的任务。任务可以在新线程或现有池线程中执行。如果任务不能提交执行，要么是因为这个执行器已经关闭，要么是因为它的容量已经到达，任务由当前的RejectedExecutionHandler处理。
     */
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * Proceed in 3 steps:
         *
         * 1. If fewer than corePoolSize threads are running, try to
         * start a new thread with the given command as its first
         * task.  The call to addWorker atomically checks runState and
         * workerCount, and so prevents false alarms that would add
         * threads when it shouldn't, by returning false.
         *
         * 2. If a task can be successfully queued, then we still need
         * to double-check whether we should have added a thread
         * (because existing ones died since last checking) or that
         * the pool shut down since entry into this method. So we
         * recheck state and if necessary roll back the enqueuing if
         * stopped, or start a new thread if there are none.
         *
         * 3. If we cannot queue task, then we try to add a new
         * thread.  If it fails, we know we are shut down or saturated
         * and so reject the task.分三步进行:
*
* 1。如果运行的线程小于corePoolSize，请尝试这样做
*用给定的命令作为第一个线程启动一个新线程
*任务。对addWorker的调用将自动检查runState和
* workerCount，这样可以防止添加错误警报
*线程当它不应该时，返回false。
*
* 2。如果一个任务可以成功排队，那么我们仍然需要
*再次检查是否应该添加线程
*(因为上次检查后现有的已经死亡)或其他
*进入此方法后，池关闭。所以我们
*重新检查状态，如果需要，回滚排队
*停止，如果没有线程，则启动一个新线程。
*
* 3。如果无法对任务排队，则尝试添加一个新任务
*线程。如果它失败了，我们知道我们被关闭或饱和了
所以拒绝这个任务。
         */
        int c = ctl.get();
//        工作线程数小于线程池核心线程数就直接添加任务线程执行
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
//        工作线程大于线程池核心线程数从队列中获取任务
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
//            如果线程池关闭或者中断且删除工作线程
            if (! isRunning(recheck) && remove(command))
//                拒绝任务
                reject(command);
//            如果没有工作线程
            else if (workerCountOf(recheck) == 0)
//                添加工作线程并执行
                addWorker(null, false);
        }
//        添加工作线程失败拒绝任务
        else if (!addWorker(command, false))
            reject(command);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     *
     * <p>This method does not wait for previously submitted tasks to
     * complete execution.  Use {@link #awaitTermination awaitTermination}
     * to do that.
     *
     * @throws SecurityException {@inheritDoc}
     * 启动有序关闭之前提交任务的执行,但是没有新的任务将被接受。如果已经关闭调用没有额外的效果。
    这种方法不执行等待先前提交的任务完成。awaitTermination使用。
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
//            修改线程池状态为关闭
            advanceRunState(SHUTDOWN);
//            打断等待接收任务的工作线程
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks
     * that were awaiting execution. These tasks are drained (removed)
     * from the task queue upon return from this method.
     *
     * <p>This method does not wait for actively executing tasks to
     * terminate.  Use {@link #awaitTermination awaitTermination} to
     * do that.
     *
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  This implementation
     * cancels tasks via {@link Thread#interrupt}, so any task that
     * fails to respond to interrupts may never terminate.
     *
     * @throws SecurityException {@inheritDoc}
     * 停止所有积极执行任务的尝试，暂停处理等待任务，并返回等待执行的任务列表。从此方法返回时，这些任务将从任务队列中删除。
    此方法不等待主动执行任务终止。使用可爱的终止来完成它。
    除了尽最大努力停止处理积极执行的任务之外，没有任何保证。此实现通过Thread.interrupt取消任务，因此任何未能响应中断的任务都可能永远不会终止。
     */
//    此实现通过Thread.interrupt取消任务，因此任何未能响应中断的任务都可能永远不会终止
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
//            修改线程池的状态为停止
            advanceRunState(STOP);
//            打断正在处理任务的工作线程
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    /**
     * Returns true if this executor is in the process of terminating
     * after {@link #shutdown} or {@link #shutdownNow} but has not
     * completely terminated.  This method may be useful for
     * debugging. A return of {@code true} reported a sufficient
     * period after shutdown may indicate that submitted tasks have
     * ignored or suppressed interruption, causing this executor not
     * to properly terminate.如果执行程序正在关机或关闭后终止，但尚未完全终止，则返回true。这个方法可能对调试有用。在关机后一个足够长的时间内返回true，可能表明提交的任务忽略或抑制了中断，导致执行程序不能正确终止。
     *
     * @return {@code true} if terminating but not yet terminated
     */
    public boolean isTerminating() {
        int c = ctl.get();
        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
//                等待超时时间终止线程池
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Invokes {@code shutdown} when this executor is no longer
     * referenced and it has no threads.当这个executor不再被引用并且它没有线程时，调用shutdown。
     */
    protected void finalize() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || acc == null) {
            shutdown();
        } else {
            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
            AccessController.doPrivileged(pa, acc);
        }
    }

    /**
     * Sets the thread factory used to create new threads.设置用于创建新线程的线程工厂。
     *
     * @param threadFactory the new thread factory
     * @throws NullPointerException if threadFactory is null
     * @see #getThreadFactory
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null)
            throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    /**
     * Returns the thread factory used to create new threads.返回用于创建新线程的线程工厂。
     *
     * @return the current thread factory
     * @see #setThreadFactory(ThreadFactory)
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * Sets a new handler for unexecutable tasks.为不可执行的任务设置一个新的处理程序。
     *
     * @param handler the new handler
     * @throws NullPointerException if handler is null
     * @see #getRejectedExecutionHandler
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null)
            throw new NullPointerException();
        this.handler = handler;
    }

    /**
     * Returns the current handler for unexecutable tasks.返回不可执行任务的当前处理程序。
     *
     * @return the current handler
     * @see #setRejectedExecutionHandler(RejectedExecutionHandler)
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    /**
     * Sets the core number of threads.  This overrides any value set
     * in the constructor.  If the new value is smaller than the
     * current value, excess existing threads will be terminated when
     * they next become idle.  If larger, new threads will, if needed,
     * be started to execute any queued tasks.设置线程的核心数量。这将覆盖构造函数中的任何值集。如果新值小于当前值，那么多余的现有线程将在下次空闲时终止。如果更大，新线程将在需要时开始执行任何队列任务。
     *
     * @param corePoolSize the new core size
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @see #getCorePoolSize
     */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0)
            throw new IllegalArgumentException();
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(ctl.get()) > corePoolSize)
            interruptIdleWorkers();
        else if (delta > 0) {
            // We don't really know how many new threads are "needed".
            // As a heuristic, prestart enough new workers (up to new
            // core size) to handle the current number of tasks in
            // queue, but stop if queue becomes empty while doing so.我们并不真正知道“需要”多少新线程。
//作为一种启发，先让足够多的新员工开始工作
//核心大小)来处理当前的任务数量
//队列，但如果队列在执行此操作时变为空，则停止。
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty())
                    break;
            }
        }
    }

    /**
     * Returns the core number of threads.
     *
     * @return the core number of threads
     * @see #setCorePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Starts a core thread, causing it to idly wait for work. This
     * overrides the default policy of starting core threads only when
     * new tasks are executed. This method will return {@code false}
     * if all core threads have already been started.启动一个核心线程，使其空闲地等待工作。这将覆盖只有在执行新任务时才启动核心线程的默认策略。如果所有核心线程都已启动，此方法将返回false。
     *
     * @return {@code true} if a thread was started
     */
    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&
            addWorker(null, true);
    }

    /**
     * Same as prestartCoreThread except arranges that at least one
     * thread is started even if corePoolSize is 0.与prestartCoreThread一样，除了安排至少一个线程启动，即使corePoolSize是0。
     */
    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }

    /**
     * Starts all core threads, causing them to idly wait for work. This
     * overrides the default policy of starting core threads only when
     * new tasks are executed.启动所有的核心线程，使它们等待工作。这将覆盖只有在执行新任务时才启动核心线程的默认策略。
     *
     * @return the number of threads started
     */
    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true))
            ++n;
        return n;
    }

    /**
     * Returns true if this pool allows core threads to time out and
     * terminate if no tasks arrive within the keepAlive time, being
     * replaced if needed when new tasks arrive. When true, the same
     * keep-alive policy applying to non-core threads applies also to
     * core threads. When false (the default), core threads are never
     * terminated due to lack of incoming tasks.如果这个线程池允许核心线程超时，如果在保持时间内没有任务到达，则返回true;如果需要，则在新任务到达时替换。当真实的,同样的维生政策申请非核心线程也适用于核心线程。当假(默认),核心线程永远不会终止由于缺乏传入的任务。
     *
     * @return {@code true} if core threads are allowed to time out,
     *         else {@code false}
     *
     * @since 1.6
     */
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    /**
     * Sets the policy governing whether core threads may time out and
     * terminate if no tasks arrive within the keep-alive time, being
     * replaced if needed when new tasks arrive. When false, core
     * threads are never terminated due to lack of incoming
     * tasks. When true, the same keep-alive policy applying to
     * non-core threads applies also to core threads. To avoid
     * continual thread replacement, the keep-alive time must be
     * greater than zero when setting {@code true}. This method
     * should in general be called before the pool is actively used.设置控制核心线程是否会超时并在保持活动时间内没有任务到达时终止的策略，在新任务到达时替换该策略。当为false时，核心线程不会因为缺少传入任务而终止。当为真时，同样适用于非核心线程的keep-alive策略也适用于核心线程。为了避免连续的线程替换，在设置true时，keep-alive时间必须大于零。通常应该在积极使用池之前调用此方法。
     *
     * @param value {@code true} if should time out, else {@code false}
     * @throws IllegalArgumentException if value is {@code true}
     *         and the current keep-alive time is not greater than zero
     *
     * @since 1.6
     */
    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value)
                interruptIdleWorkers();
        }
    }

    /**
     * Sets the maximum allowed number of threads. This overrides any
     * value set in the constructor. If the new value is smaller than
     * the current value, excess existing threads will be
     * terminated when they next become idle.设置允许的最大线程数。这将覆盖构造函数中的任何值集。如果新值小于当前值，那么多余的现有线程将在下次空闲时终止。
     *
     * @param maximumPoolSize the new maximum
     * @throws IllegalArgumentException if the new maximum is
     *         less than or equal to zero, or
     *         less than the {@linkplain #getCorePoolSize core pool size}
     * @see #getMaximumPoolSize
     */
//    最大线程数必须大于核心线程数
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
            throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
//        如果工作线程数大于最大线程数则中断正在等待接收任务的工作线程
        if (workerCountOf(ctl.get()) > maximumPoolSize)
            interruptIdleWorkers();
    }

    /**
     * Returns the maximum allowed number of threads.
     *
     * @return the maximum allowed number of threads
     * @see #setMaximumPoolSize
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Sets the time limit for which threads may remain idle before
     * being terminated.  If there are more than the core number of
     * threads currently in the pool, after waiting this amount of
     * time without processing a task, excess threads will be
     * terminated.  This overrides any value set in the constructor.设置线程在终止前可能保持空闲的时间限制。如果池中当前的线程数量超过了核心数量，那么在没有处理任务的情况下等待这段时间后，多余的线程将被终止。这将覆盖构造函数中的任何值集。
     *
     * @param time the time to wait.  A time value of zero will cause
     *        excess threads to terminate immediately after executing tasks.
     * @param unit the time unit of the {@code time} argument
     * @throws IllegalArgumentException if {@code time} less than zero or
     *         if {@code time} is zero and {@code allowsCoreThreadTimeOut}
     * @see #getKeepAliveTime(TimeUnit)
     */
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0)
            throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut())
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0)
            interruptIdleWorkers();
    }

    /**
     * Returns the thread keep-alive time, which is the amount of time
     * that threads in excess of the core pool size may remain
     * idle before being terminated.返回线程保持活动时间，即超过核心池大小的线程在终止之前可能保持空闲的时间。
     *
     * @param unit the desired time unit of the result
     * @return the time limit
     * @see #setKeepAliveTime(long, TimeUnit)
     */
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /* User-level queue utilities */

    /**
     * Returns the task queue used by this executor. Access to the
     * task queue is intended primarily for debugging and monitoring.
     * This queue may be in active use.  Retrieving the task queue
     * does not prevent queued tasks from executing.返回执行程序使用的任务队列。对任务队列的访问主要用于调试和监视。这个队列可能正在使用中。检索任务队列并不妨碍队列任务的执行。
     *
     * @return the task queue
     */
    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * Removes this task from the executor's internal queue if it is
     * present, thus causing it not to be run if it has not already
     * started.
     *
     * <p>This method may be useful as one part of a cancellation
     * scheme.  It may fail to remove tasks that have been converted
     * into other forms before being placed on the internal queue. For
     * example, a task entered using {@code submit} might be
     * converted into a form that maintains {@code Future} status.
     * However, in such cases, method {@link #purge} may be used to
     * remove those Futures that have been cancelled.
     *
     * @param task the task to remove
     * @return {@code true} if the task was removed
     * 如果该任务存在，则从executor的内部队列中移除该任务，从而导致它在尚未启动时不运行。
    这种方法作为取消方案的一部分可能是有用的。它可能无法删除已转换为其他表单的任务，然后再放到内部队列中。例如，使用submit输入的任务可以转换为保持未来状态的表单。然而，在这种情况下，方法清除可以用来删除那些已被取消的期货。
     */
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    /**
     * Tries to remove from the work queue all {@link Future}
     * tasks that have been cancelled. This method can be useful as a
     * storage reclamation operation, that has no other impact on
     * functionality. Cancelled tasks are never executed, but may
     * accumulate in work queues until worker threads can actively
     * remove them. Invoking this method instead tries to remove them now.
     * However, this method may fail to remove tasks in
     * the presence of interference by other threads.尝试从工作队列中删除所有被取消的未来任务。此方法可以作为存储回收操作使用，对功能没有其他影响。取消的任务永远不会执行，但可能会在工作队列中累积，直到工作线程能够主动删除它们。现在，调用此方法将尝试删除它们。但是，在其他线程的干扰下，此方法可能无法删除任务。
     */
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    q.remove(r);
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    /* Statistics */

    /**
     * Returns the current number of threads in the pool.返回池中当前的线程数。
     *
     * @return the number of threads
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // Remove rare and surprising possibility of
            // isTerminated() && getPoolSize() > 0
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                : workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate number of threads that are actively
     * executing tasks.返回正在积极执行任务的线程的大致数目。
     *
     * @return the number of threads
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked())
                    ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the largest number of threads that have ever
     * simultaneously been in the pool.返回池中同时存在的最大线程数。
     *
     * @return the number of threads
     */
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate total number of tasks that have ever been
     * scheduled for execution. Because the states of tasks and
     * threads may change dynamically during computation, the returned
     * value is only an approximation.返回计划执行的任务的总数。由于任务和线程的状态在计算过程中可能会动态变化，因此返回的值只是一个近似值。
     *
     * @return the number of tasks
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())
                    ++n;
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate total number of tasks that have
     * completed execution. Because the states of tasks and threads
     * may change dynamically during computation, the returned value
     * is only an approximation, but one that does not ever decrease
     * across successive calls.返回完成执行的任务的总数。由于任务和线程的状态在计算过程中可能会动态变化，因此返回的值仅仅是一个近似值，但在连续的调用中不会减少。
     *
     * @return the number of tasks
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns a string identifying this pool, as well as its state,
     * including indications of run state and estimated worker and
     * task counts.
     *
     * @return a string identifying this pool, as well as its state
     */
    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked())
                    ++nactive;
            }
        } finally {
            mainLock.unlock();
        }
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :
                     (runStateAtLeast(c, TERMINATED) ? "Terminated" :
                      "Shutting down"));
        return super.toString() +
            "[" + rs +
            ", pool size = " + nworkers +
            ", active threads = " + nactive +
            ", queued tasks = " + workQueue.size() +
            ", completed tasks = " + ncompleted +
            "]";
    }

    /* Extension hooks */

    /**
     * Method invoked prior to executing the given Runnable in the
     * given thread.  This method is invoked by thread {@code t} that
     * will execute task {@code r}, and may be used to re-initialize
     * ThreadLocals, or to perform logging.
     *
     * <p>This implementation does nothing, but may be customized in
     * subclasses. Note: To properly nest multiple overridings, subclasses
     * should generally invoke {@code super.beforeExecute} at the end of
     * this method.
     *
     * @param t the thread that will run task {@code r}
     * @param r the task that will be executed
     *
    在给定的线程中执行给定的Runnable之前调用的方法。这个方法由执行任务r的线程t调用，可以用来重新初始化threadlocal，或者用来执行日志记录。
    这个实现什么都不做，但是可以在子类中定制。注意:要正确地嵌套多个覆盖，子类通常应该调用super。在此方法的末尾执行beforeExecute。
     */
    protected void beforeExecute(Thread t, Runnable r) { }

    /**
     * Method invoked upon completion of execution of the given Runnable.
     * This method is invoked by the thread that executed the task. If
     * non-null, the Throwable is the uncaught {@code RuntimeException}
     * or {@code Error} that caused execution to terminate abruptly.
     *
     * <p>This implementation does nothing, but may be customized in
     * subclasses. Note: To properly nest multiple overridings, subclasses
     * should generally invoke {@code super.afterExecute} at the
     * beginning of this method.
     *
     * <p><b>Note:</b> When actions are enclosed in tasks (such as
     * {@link FutureTask}) either explicitly or via methods such as
     * {@code submit}, these task objects catch and maintain
     * computational exceptions, and so they do not cause abrupt
     * termination, and the internal exceptions are <em>not</em>
     * passed to this method. If you would like to trap both kinds of
     * failures in this method, you can further probe for such cases,
     * as in this sample subclass that prints either the direct cause
     * or the underlying exception if a task has been aborted:
     *
     *  <pre> {@code
     * class ExtendedExecutor extends ThreadPoolExecutor {
     *   // ...
     *   protected void afterExecute(Runnable r, Throwable t) {
     *     super.afterExecute(r, t);
     *     if (t == null && r instanceof Future<?>) {
     *       try {
     *         Object result = ((Future<?>) r).get();
     *       } catch (CancellationException ce) {
     *           t = ce;
     *       } catch (ExecutionException ee) {
     *           t = ee.getCause();
     *       } catch (InterruptedException ie) {
     *           Thread.currentThread().interrupt(); // ignore/reset
     *       }
     *     }
     *     if (t != null)
     *       System.out.println(t);
     *   }
     * }}</pre>
     *
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     * execution completed normally
     *          在执行给定的Runnable之后调用的方法。该方法由执行任务的线程调用。如果非空，则可抛出是未捕获的运行时异常或错误，导致执行突然终止。
    这个实现什么都不做，但是可以在子类中定制。注意:要正确地嵌套多个覆盖，子类通常应该调用super。在此方法的开头执行afterExecute。
    注意:当操作被显式地或通过提交等方法包含在任务(如FutureTask)中时，这些任务对象捕获并维护计算异常，因此它们不会导致突然终止，内部异常也不会传递给此方法。如果您想在此方法中捕获这两种失败，您可以进一步探查此类情况，如在本示例子类中，如果任务已被中止，则打印直接原因或底层异常:
     */
    protected void afterExecute(Runnable r, Throwable t) { }

    /**
     * Method invoked when the Executor has terminated.  Default
     * implementation does nothing. Note: To properly nest multiple
     * overridings, subclasses should generally invoke
     * {@code super.terminated} within this method.当执行程序终止时调用的方法。违约并没有实现。注意:要正确地嵌套多个覆盖，子类通常应该调用super。终止在这个方法。
     */
    protected void terminated() { }

    /* Predefined RejectedExecutionHandlers */

    /**
     * A handler for rejected tasks that runs the rejected task
     * directly in the calling thread of the {@code execute} method,
     * unless the executor has been shut down, in which case the task
     * is discarded.一个处理程序，用于处理直接在execute方法的调用线程中运行被拒绝任务的被拒绝任务，除非执行程序已经被关闭，在这种情况下，任务被丢弃。
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public CallerRunsPolicy() { }

        /**
         * Executes task r in the caller's thread, unless the executor
         * has been shut down, in which case the task is discarded.在调用者的线程中执行任务r，除非执行程序已经被关闭，在这种情况下，任务被丢弃。
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
//                同步执行被拒绝的任务
                r.run();
            }
        }
    }

    /**
     * A handler for rejected tasks that throws a
     * {@code RejectedExecutionException}.抛出RejectedExecutionException的被拒绝任务的处理程序。
     */
//    抛出RejectedExecutionException异常
    public static class AbortPolicy implements RejectedExecutionHandler {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public AbortPolicy() { }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }

    /**
     * A handler for rejected tasks that silently discards the
     * rejected task.一个处理程序，用于处理被拒绝的任务，这些任务会默默地丢弃被拒绝的任务。
     */
//    丢弃任务
    public static class DiscardPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardPolicy}.
         */
        public DiscardPolicy() { }

        /**
         * Does nothing, which has the effect of discarding task r.什么都不做，这有丢弃任务r的效果。
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    /**
     * A handler for rejected tasks that discards the oldest unhandled
     * request and then retries {@code execute}, unless the executor
     * is shut down, in which case the task is discarded.处理被拒绝的任务的处理程序，该处理程序丢弃最古老的未处理请求，然后重试执行，除非执行程序被关闭，在这种情况下，任务被丢弃。
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardOldestPolicy} for the given executor.
         */
        public DiscardOldestPolicy() { }

        /**
         * Obtains and ignores the next task that the executor
         * would otherwise execute, if one is immediately available,
         * and then retries execution of task r, unless the executor
         * is shut down, in which case task r is instead discarded.获取并忽略执行程序将执行的下一个任务(如果当前可用的话)，然后重试执行任务r，除非执行程序被关闭，在这种情况下，任务r将被丢弃。
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
//                丢弃老的任务执行走拒绝策略的任务
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
}
