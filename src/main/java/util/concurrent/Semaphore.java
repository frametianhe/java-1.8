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
import java.util.Collection;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A counting semaphore.  Conceptually, a semaphore maintains a set of
 * permits.  Each {@link #acquire} blocks if necessary until a permit is
 * available, and then takes it.  Each {@link #release} adds a permit,
 * potentially releasing a blocking acquirer.
 * However, no actual permit objects are used; the {@code Semaphore} just
 * keeps a count of the number available and acts accordingly.
 *
 * <p>Semaphores are often used to restrict the number of threads than can
 * access some (physical or logical) resource. For example, here is
 * a class that uses a semaphore to control access to a pool of items:
 *  <pre> {@code
 * class Pool {
 *   private static final int MAX_AVAILABLE = 100;
 *   private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
 *
 *   public Object getItem() throws InterruptedException {
 *     available.acquire();
 *     return getNextAvailableItem();
 *   }
 *
 *   public void putItem(Object x) {
 *     if (markAsUnused(x))
 *       available.release();
 *   }
 *
 *   // Not a particularly efficient data structure; just for demo
 *
 *   protected Object[] items = ... whatever kinds of items being managed
 *   protected boolean[] used = new boolean[MAX_AVAILABLE];
 *
 *   protected synchronized Object getNextAvailableItem() {
 *     for (int i = 0; i < MAX_AVAILABLE; ++i) {
 *       if (!used[i]) {
 *          used[i] = true;
 *          return items[i];
 *       }
 *     }
 *     return null; // not reached
 *   }
 *
 *   protected synchronized boolean markAsUnused(Object item) {
 *     for (int i = 0; i < MAX_AVAILABLE; ++i) {
 *       if (item == items[i]) {
 *          if (used[i]) {
 *            used[i] = false;
 *            return true;
 *          } else
 *            return false;
 *       }
 *     }
 *     return false;
 *   }
 * }}</pre>
 *
 * <p>Before obtaining an item each thread must acquire a permit from
 * the semaphore, guaranteeing that an item is available for use. When
 * the thread has finished with the item it is returned back to the
 * pool and a permit is returned to the semaphore, allowing another
 * thread to acquire that item.  Note that no synchronization lock is
 * held when {@link #acquire} is called as that would prevent an item
 * from being returned to the pool.  The semaphore encapsulates the
 * synchronization needed to restrict access to the pool, separately
 * from any synchronization needed to maintain the consistency of the
 * pool itself.
 *
 * <p>A semaphore initialized to one, and which is used such that it
 * only has at most one permit available, can serve as a mutual
 * exclusion lock.  This is more commonly known as a <em>binary
 * semaphore</em>, because it only has two states: one permit
 * available, or zero permits available.  When used in this way, the
 * binary semaphore has the property (unlike many {@link java.util.concurrent.locks.Lock}
 * implementations), that the &quot;lock&quot; can be released by a
 * thread other than the owner (as semaphores have no notion of
 * ownership).  This can be useful in some specialized contexts, such
 * as deadlock recovery.
 *
 * <p> The constructor for this class optionally accepts a
 * <em>fairness</em> parameter. When set false, this class makes no
 * guarantees about the order in which threads acquire permits. In
 * particular, <em>barging</em> is permitted, that is, a thread
 * invoking {@link #acquire} can be allocated a permit ahead of a
 * thread that has been waiting - logically the new thread places itself at
 * the head of the queue of waiting threads. When fairness is set true, the
 * semaphore guarantees that threads invoking any of the {@link
 * #acquire() acquire} methods are selected to obtain permits in the order in
 * which their invocation of those methods was processed
 * (first-in-first-out; FIFO). Note that FIFO ordering necessarily
 * applies to specific internal points of execution within these
 * methods.  So, it is possible for one thread to invoke
 * {@code acquire} before another, but reach the ordering point after
 * the other, and similarly upon return from the method.
 * Also note that the untimed {@link #tryAcquire() tryAcquire} methods do not
 * honor the fairness setting, but will take any permits that are
 * available.
 *
 * <p>Generally, semaphores used to control resource access should be
 * initialized as fair, to ensure that no thread is starved out from
 * accessing a resource. When using semaphores for other kinds of
 * synchronization control, the throughput advantages of non-fair
 * ordering often outweigh fairness considerations.
 *
 * <p>This class also provides convenience methods to {@link
 * #acquire(int) acquire} and {@link #release(int) release} multiple
 * permits at a time.  Beware of the increased risk of indefinite
 * postponement when these methods are used without fairness set true.
 *
 * <p>Memory consistency effects: Actions in a thread prior to calling
 * a "release" method such as {@code release()}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions following a successful "acquire" method such as {@code acquire()}
 * in another thread.
 *
 * @since 1.5
 * @author Doug Lea
 * 计数信号量。概念上，信号量维护一组许可证。如果有必要，每一个获取区块直到获得许可，然后获取。每个版本都添加了一个许可，可能会释放一个阻塞的收购者。但是，不使用实际的许可证对象;信号量只保留可用数字的计数，并相应地执行。
信号量通常用来限制线程的数量，而不能访问某些(物理或逻辑)资源。例如，这里有一个类，它使用信号量来控制对项目池的访问:
在获取一个项目之前，每个线程必须从信号量获得许可，以保证一个项目可以使用。当线程完成该项目时，它将返回到池中，并向信号量返回一个许可证，允许另一个线程获取该项目。请注意，当调用acquire时，不会持有同步锁，因为这会阻止一个条目返回到池中。信号量封装了限制对池访问所需的同步，与维护池本身一致性所需的任何同步分开。
在获取一个项目之前，每个线程必须从信号量获得许可，以保证一个项目可以使用。当线程完成该项目时，它将返回到池中，并向信号量返回一个许可证，允许另一个线程获取该项目。请注意，当调用acquire时，不会持有同步锁，因为这会阻止一个条目返回到池中。信号量封装了限制对池访问所需的同步，与维护池本身一致性所需的任何同步分开。
一个初始化为1的信号量，其使用方式是它最多只有一个可用的许可，可以作为互斥锁。这通常被称为二元信号量，因为它只有两个状态:一个允许可用，或者是零许可。以这种方式使用时，二进制信号量具有该属性(与许多java.util.concurrent.locks不同)。锁实现)，即“锁”可以由非所有者的线程释放(因为信号量没有所有权的概念)。这在某些特定的上下文中是有用的，比如死锁恢复。
该类的构造函数可选地接受一个公平参数。当设置false时，这个类不能保证线程获得许可的顺序。特别是，允许进行倒刺，也就是说，可以在正在等待的线程之前为调用acquire的线程分配许可证——逻辑上，新线程将自己放置在正在等待的线程队列的前端。当公平性设置为true时，信号量保证选择调用任何获取方法的线程以按照处理它们对这些方法的调用的顺序获得许可(先进先出;先进先出)。注意，FIFO排序必然适用于这些方法中特定的内部执行点。因此，一个线程可以在另一个线程之前调用acquire，但是可以在另一个线程之后调用排序点，并且在从方法返回时也是如此。还要注意的是，不定时的try获取方法不遵守公平设置，但是会获得任何可用的许可。
通常，用于控制资源访问的信号量应该被初始化为公平的，以确保没有线程因访问资源而耗尽。当对其他类型的同步控制使用信号量时，不公平排序的吞吐量优势常常超过公平性考虑。
这个类还提供了一次获取和释放多个许可证的便利方法。要注意，如果这些方法使用时没有设定公平，就会增加无限期延迟的风险。
内存一致性效应:在调用“发布”方法(如release()之前的线程中的操作——在成功的“获取”方法(如在另一个线程中获取())之后的操作。
 */
//线程同步工具信号量，信号量通常用来限制线程的数量，而不能访问某些(物理或逻辑)资源，一般用来做限流
//    一个初始化为1的信号量，其使用方式是它最多只有一个可用的许可，可以作为互斥锁
public class Semaphore implements java.io.Serializable {
    private static final long serialVersionUID = -3222578661600680210L;
    /** All mechanics via AbstractQueuedSynchronizer subclass 通过AbstractQueuedSynchronizer子类实现的所有力学*/
    private final Sync sync;

    /**
     * Synchronization implementation for semaphore.  Uses AQS state
     * to represent permits. Subclassed into fair and nonfair
     * versions.同步实现信号量。使用AQS状态来表示许可。细分为公平和不公平的版本。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1192457210091910933L;

        Sync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }

        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }

//        释放共享锁
        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                if (compareAndSetState(current, next))
                    return true;
            }
        }

        final void reducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // underflow
                    throw new Error("Permit count underflow");
                if (compareAndSetState(current, next))
                    return;
            }
        }

        final int drainPermits() {
            for (;;) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**
     * NonFair version
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -2694183684443567898L;

        NonfairSync(int permits) {
            super(permits);
        }

//        获取共享锁
        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

    /**
     * Fair version
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = 2014338818796000944L;

        FairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            for (;;) {
//                公平锁是aqs队列实现
                if (hasQueuedPredecessors())
                    return -1;
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }
    }

    /**
     * Creates a {@code Semaphore} with the given number of
     * permits and nonfair fairness setting.使用给定的许可数量和不公平的公平性设置创建信号量。
     *
     * @param permits the initial number of permits available.
     *        This value may be negative, in which case releases
     *        must occur before any acquires will be granted.
     */
    public Semaphore(int permits) {
//        默认是非共平锁
        sync = new NonfairSync(permits);
    }

    /**
     * Creates a {@code Semaphore} with the given number of
     * permits and the given fairness setting.使用给定的许可数量和给定的公平性设置创建一个信号量。
     *
     * @param permits the initial number of permits available.
     *        This value may be negative, in which case releases
     *        must occur before any acquires will be granted.
     * @param fair {@code true} if this semaphore will guarantee
     *        first-in first-out granting of permits under contention,
     *        else {@code false}
     */
//    可以设置公平锁
    public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }

    /**
     * Acquires a permit from this semaphore, blocking until one is
     * available, or the thread is {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * one of two things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #release} method for this
     * semaphore and the current thread is next to be assigned a permit; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * for a permit,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted
     * 从这个信号量获得许可，阻塞直到可用，或者线程被中断。
    获得许可证(如果有的话)，立即返回，减少一个许可证的数量。
    如果没有许可证，那么当前线程将被禁用以进行线程调度，并处于休眠状态，直到发生以下两种情况之一:
    另一些线程为这个信号量调用释放方法，当前线程接下来将被分配许可证;或
    其他一些线程中断当前线程。
    如果当前线程:
    在进入此方法时设置其中断状态;或
    在等待许可证时被打断，
    然后抛出InterruptedException，并清除当前线程的中断状态。
     */
//    阻塞
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Acquires a permit from this semaphore, blocking until one is
     * available.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * some other thread invokes the {@link #release} method for this
     * semaphore and the current thread is next to be assigned a permit.
     *
     * <p>If the current thread is {@linkplain Thread#interrupt interrupted}
     * while waiting for a permit then it will continue to wait, but the
     * time at which the thread is assigned a permit may change compared to
     * the time it would have received the permit had no interruption
     * occurred.  When the thread does return from this method its interrupt
     * status will be set.
     * 从这个信号量获得许可，直到一个信号量可用为止。
     获得许可证(如果有的话)，立即返回，减少一个许可证的数量。
     如果没有许可证，那么当前线程将被禁用以用于线程调度目的，并且处于休眠状态，直到其他线程调用此信号量的发布方法，当前线程将被分配许可证。
     如果当前线程在等待许可证时被中断，那么它将继续等待，但是线程被分配许可证的时间可能会与在没有中断的情况下收到许可证的时间相比发生变化。当线程从这个方法返回时，它的中断状态将被设置。
     */
    public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }

    /**
     * Acquires a permit from this semaphore, only if one is available at the
     * time of invocation.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * with the value {@code true},
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then this method will return
     * immediately with the value {@code false}.
     *
     * <p>Even when this semaphore has been set to use a
     * fair ordering policy, a call to {@code tryAcquire()} <em>will</em>
     * immediately acquire a permit if one is available, whether or not
     * other threads are currently waiting.
     * This &quot;barging&quot; behavior can be useful in certain
     * circumstances, even though it breaks fairness. If you want to honor
     * the fairness setting, then use
     * {@link #tryAcquire(long, TimeUnit) tryAcquire(0, TimeUnit.SECONDS) }
     * which is almost equivalent (it also detects interruption).
     *
     * @return {@code true} if a permit was acquired and {@code false}
     *         otherwise
     *         只有在调用时可用该信号量时，才能从该信号量获得许可。
    取得许可证，如果有，立即返回，价值为真，减少一个现有许可证的数量。
    如果没有许可证，则此方法将立即返回值false。
    即使这个信号量被设置为使用一个公平的订购策略，调用tryAcquire()将立即获得一个许可证，如果一个许可证是可用的，不管其他线程是否正在等待。这种“倒装”行为在某些情况下是有用的，即使它破坏了公平。如果你想要对公平设置表示尊重，那么使用tryAcquire(0, TimeUnit.SECONDS)，这几乎是等效的(它也可以检测到中断)。
     */
    public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }

    /**
     * Acquires a permit from this semaphore, if one becomes available
     * within the given waiting time and the current thread has not
     * been {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * with the value {@code true},
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * one of three things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #release} method for this
     * semaphore and the current thread is next to be assigned a permit; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     *
     * <p>If a permit is acquired then the value {@code true} is returned.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * to acquire a permit,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait for a permit
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if a permit was acquired and {@code false}
     *         if the waiting time elapsed before a permit was acquired
     * @throws InterruptedException if the current thread is interrupted
     * 如果一个信号量在给定的等待时间内可用并且当前线程没有被中断，则从该信号量获得许可。
    取得许可证，如果有，立即返回，价值为真，减少一个现有许可证的数量。
    如果没有许可证，那么当前线程将被禁用用于线程调度目的，并处于休眠状态，直到发生以下三种情况之一:
    另一些线程为这个信号量调用释放方法，当前线程接下来将被分配许可证;或
    其他一些线程中断当前线程;或
    指定的等待时间运行。
    如果获得许可证，则返回true值。
    如果当前线程:
    在进入此方法时设置其中断状态;或
    在等待取得许可证时被打断，
    然后抛出InterruptedException，并清除当前线程的中断状态。
    如果指定的等待时间过去，则返回值false。如果时间小于或等于零，方法将不再等待。
     */
    public boolean tryAcquire(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Releases a permit, returning it to the semaphore.
     *
     * <p>Releases a permit, increasing the number of available permits by
     * one.  If any threads are trying to acquire a permit, then one is
     * selected and given the permit that was just released.  That thread
     * is (re)enabled for thread scheduling purposes.
     *
     * <p>There is no requirement that a thread that releases a permit must
     * have acquired that permit by calling {@link #acquire}.
     * Correct usage of a semaphore is established by programming convention
     * in the application.
     * 释放许可证，将其返回信号量。
     发放许可证，增加一个许可证的数量。如果任何线程都试图获得许可证，那么就选择一个线程，并给出刚刚发布的许可证。该线程为线程调度目的启用(re)。
     不要求发布许可证的线程必须通过调用acquire获得许可证。通过应用程序中的编程约定来确定信号量的正确用法。
     */
    public void release() {
        sync.releaseShared(1);
    }

    /**
     * Acquires the given number of permits from this semaphore,
     * blocking until all are available,
     * or the thread is {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires the given number of permits, if they are available,
     * and returns immediately, reducing the number of available permits
     * by the given amount.
     *
     * <p>If insufficient permits are available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * one of two things happens:
     * <ul>
     * <li>Some other thread invokes one of the {@link #release() release}
     * methods for this semaphore, the current thread is next to be assigned
     * permits and the number of available permits satisfies this request; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * for a permit,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * Any permits that were to be assigned to this thread are instead
     * assigned to other threads trying to acquire permits, as if
     * permits had been made available by a call to {@link #release()}.
     *
     * @param permits the number of permits to acquire
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalArgumentException if {@code permits} is negative
     * 从这个信号量获取给定的许可数，阻塞直到所有许可都可用，或者线程被中断。
    获得给定的许可数量(如果有的话)，并立即返回，按给定的数量减少可获得的许可数量。
    如果没有足够的许可，那么当前线程将被禁用用于线程调度目的，并且处于休眠状态，直到发生以下两种情况之一:
    另一些线程为这个信号量调用一个释放方法，当前线程接下来被分配许可，可用许可的数量满足这个请求;或
    其他一些线程中断当前线程。
    如果当前线程:
    在进入此方法时设置其中断状态;或
    在等待许可证时被打断，
    然后抛出InterruptedException，并清除当前线程的中断状态。分配给这个线程的任何许可都被分配给试图获取许可的其他线程，就好像通过调用release()可以获得许可一样。
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }

    /**
     * Acquires the given number of permits from this semaphore,
     * blocking until all are available.
     *
     * <p>Acquires the given number of permits, if they are available,
     * and returns immediately, reducing the number of available permits
     * by the given amount.
     *
     * <p>If insufficient permits are available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * some other thread invokes one of the {@link #release() release}
     * methods for this semaphore, the current thread is next to be assigned
     * permits and the number of available permits satisfies this request.
     *
     * <p>If the current thread is {@linkplain Thread#interrupt interrupted}
     * while waiting for permits then it will continue to wait and its
     * position in the queue is not affected.  When the thread does return
     * from this method its interrupt status will be set.
     *
     * @param permits the number of permits to acquire
     * @throws IllegalArgumentException if {@code permits} is negative
     * 公共空间acquireUninterruptibly(int的凭许可证经营)
    从这个信号量中获取给定的许可数量，直到所有的许可都可用。
    获得给定的许可数量(如果有的话)，并立即返回，按给定的数量减少可获得的许可数量。
    如果没有足够的许可，那么当前线程就会因为线程调度的目的而被禁用，并且处于休眠状态，直到其他线程为这个信号量调用一个发布方法，当前线程接下来将被分配许可，可用许可的数量满足这个请求。
    如果当前线程在等待许可时被中断，那么它将继续等待，并且它在队列中的位置不会受到影响。当线程从这个方法返回时，它的中断状态将被设置。
     */
    public void acquireUninterruptibly(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireShared(permits);
    }

    /**
     * Acquires the given number of permits from this semaphore, only
     * if all are available at the time of invocation.
     *
     * <p>Acquires the given number of permits, if they are available, and
     * returns immediately, with the value {@code true},
     * reducing the number of available permits by the given amount.
     *
     * <p>If insufficient permits are available then this method will return
     * immediately with the value {@code false} and the number of available
     * permits is unchanged.
     *
     * <p>Even when this semaphore has been set to use a fair ordering
     * policy, a call to {@code tryAcquire} <em>will</em>
     * immediately acquire a permit if one is available, whether or
     * not other threads are currently waiting.  This
     * &quot;barging&quot; behavior can be useful in certain
     * circumstances, even though it breaks fairness. If you want to
     * honor the fairness setting, then use {@link #tryAcquire(int,
     * long, TimeUnit) tryAcquire(permits, 0, TimeUnit.SECONDS) }
     * which is almost equivalent (it also detects interruption).
     *
     * @param permits the number of permits to acquire
     * @return {@code true} if the permits were acquired and
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code permits} is negative
     * 只有在调用时所有许可都可用时，才能从这个信号量获取给定的许可数量。
    获得给定数量的许可(如果有的话)，并立即返回，值为true，减少给定数量的许可数量。
    如果没有足够的许可，那么该方法将立即返回，值为false，可用许可的数量不变。
    即使这个信号量被设置为使用一个公平的订购策略，调用tryAcquire将立即获得许可，如果一个许可是可用的，无论其他线程是否正在等待。这种“倒装”行为在某些情况下是有用的，即使它破坏了公平。如果您希望遵守公平设置，那么使用tryAcquire(allow, 0, TimeUnit.SECONDS)，它几乎是等价的(它还检测中断)。
     */
    public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.nonfairTryAcquireShared(permits) >= 0;
    }

    /**
     * Acquires the given number of permits from this semaphore, if all
     * become available within the given waiting time and the current
     * thread has not been {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires the given number of permits, if they are available and
     * returns immediately, with the value {@code true},
     * reducing the number of available permits by the given amount.
     *
     * <p>If insufficient permits are available then
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     * <ul>
     * <li>Some other thread invokes one of the {@link #release() release}
     * methods for this semaphore, the current thread is next to be assigned
     * permits and the number of available permits satisfies this request; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     *
     * <p>If the permits are acquired then the value {@code true} is returned.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * to acquire the permits,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * Any permits that were to be assigned to this thread, are instead
     * assigned to other threads trying to acquire permits, as if
     * the permits had been made available by a call to {@link #release()}.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.  Any permits that were to be assigned to this
     * thread, are instead assigned to other threads trying to acquire
     * permits, as if the permits had been made available by a call to
     * {@link #release()}.
     *
     * @param permits the number of permits to acquire
     * @param timeout the maximum time to wait for the permits
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if all permits were acquired and {@code false}
     *         if the waiting time elapsed before all permits were acquired
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalArgumentException if {@code permits} is negative
     * 如果在给定的等待时间内，并且当前线程没有被中断，则从这个信号量获取给定的许可数量。
    获得给定数量的许可(如果有的话)，并立即返回，值为true，按给定的金额减少可用许可的数量。
    如果没有足够的许可，那么当前线程就会因为线程调度而被禁用，并且处于休眠状态，直到发生以下三种情况:
    另一些线程为这个信号量调用一个释放方法，当前线程接下来被分配许可，可用许可的数量满足这个请求;或
    其他一些线程中断当前线程;或
    指定的等待时间运行。
    如果获得许可证，则返回值true。
    如果当前线程:
    在进入此方法时设置其中断状态;或
    在等待取得许可证时被打断，
    然后抛出InterruptedException，并清除当前线程的中断状态。分配给这个线程的任何许可，都被分配给试图获取许可的其他线程，就好像通过调用release()获得许可一样。
    如果指定的等待时间过去，则返回值false。如果时间小于或等于零，则该方法根本不会等待。分配给这个线程的任何许可，都被分配给试图获取许可的其他线程，就好像通过调用release()获得许可一样。
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }

    /**
     * Releases the given number of permits, returning them to the semaphore.
     *
     * <p>Releases the given number of permits, increasing the number of
     * available permits by that amount.
     * If any threads are trying to acquire permits, then one
     * is selected and given the permits that were just released.
     * If the number of available permits satisfies that thread's request
     * then that thread is (re)enabled for thread scheduling purposes;
     * otherwise the thread will wait until sufficient permits are available.
     * If there are still permits available
     * after this thread's request has been satisfied, then those permits
     * are assigned in turn to other threads trying to acquire permits.
     *
     * <p>There is no requirement that a thread that releases a permit must
     * have acquired that permit by calling {@link Semaphore#acquire acquire}.
     * Correct usage of a semaphore is established by programming convention
     * in the application.
     *
     * @param permits the number of permits to release
     * @throws IllegalArgumentException if {@code permits} is negative
     * 释放给定的许可数量，将它们返回到信号量。
    释放给定的许可数量，增加可用的许可数量。如果任何线程都试图获取许可，那么就选择一个线程，并给出刚刚发布的许可。如果可用许可的数量满足该线程的请求，则该线程为线程调度目的启用(re);否则，线程将等待直到获得足够的许可。如果在这个线程的请求得到满足之后仍然有可用的许可，那么这些许可将依次分配给试图获取许可的其他线程。
    不要求发布许可证的线程必须通过调用acquire获得许可证。通过应用程序中的编程约定来确定信号量的正确用法。
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

    /**
     * Returns the current number of permits available in this semaphore.
     *
     * <p>This method is typically used for debugging and testing purposes.
     *
     * @return the number of permits available in this semaphore
     * 返回此信号量中可用许可的当前数量。
    此方法通常用于调试和测试目的。
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     * Acquires and returns all permits that are immediately available.获取并返回所有可立即获得的许可。
     *
     * @return the number of permits acquired
     */
    public int drainPermits() {
        return sync.drainPermits();
    }

    /**
     * Shrinks the number of available permits by the indicated
     * reduction. This method can be useful in subclasses that use
     * semaphores to track resources that become unavailable. This
     * method differs from {@code acquire} in that it does not block
     * waiting for permits to become available.
     *
     * @param reduction the number of permits to remove
     * @throws IllegalArgumentException if {@code reduction} is negative
     * 减少可获得的许可的数量。这种方法在使用信号量跟踪不可用资源的子类中很有用。这种方法与获取方法的不同之处在于，它不会阻塞等待许可变得可用。
     */
    protected void reducePermits(int reduction) {
        if (reduction < 0) throw new IllegalArgumentException();
        sync.reducePermits(reduction);
    }

    /**
     * Returns {@code true} if this semaphore has fairness set true.如果这个信号量设置为true，则返回true。
     *
     * @return {@code true} if this semaphore has fairness set true
     */
    public boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * Queries whether any threads are waiting to acquire. Note that
     * because cancellations may occur at any time, a {@code true}
     * return does not guarantee that any other thread will ever
     * acquire.  This method is designed primarily for use in
     * monitoring of the system state.查询是否有线程等待获取。请注意，由于取消可能在任何时间发生，一个真实的回报不能保证任何其他线程将获得。该方法主要用于监控系统状态。
     *
     * @return {@code true} if there may be other threads waiting to
     *         acquire the lock
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire.
     * The value is only an estimate because the number of threads may
     * change dynamically while this method traverses internal data
     * structures.  This method is designed for use in monitoring of the
     * system state, not for synchronization control.
     *
     * @return the estimated number of threads waiting for this lock
     * 返回等待获取的线程数的估计值。这个值只是一个估计值，因为当这个方法遍历内部数据结构时，线程的数量可能会动态变化。这种方法用于监视系统状态，而不是用于同步控制。
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns a collection containing threads that may be waiting to acquire.
     * Because the actual set of threads may change dynamically while
     * constructing this result, the returned collection is only a best-effort
     * estimate.  The elements of the returned collection are in no particular
     * order.  This method is designed to facilitate construction of
     * subclasses that provide more extensive monitoring facilities.
     *
     * @return the collection of threads
     * 返回包含可能等待获取的线程的集合。因为实际的线程集在构造这个结果时可能会动态地变化，所以返回的集合只是一个最佳的估计。返回集合的元素没有特定的顺序。该方法的设计目的是为了方便提供更广泛的监测设施的子类。
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * Returns a string identifying this semaphore, as well as its state.
     * The state, in brackets, includes the String {@code "Permits ="}
     * followed by the number of permits.
     *
     * @return a string identifying this semaphore, as well as its state
     */
    public String toString() {
        return super.toString() + "[Permits = " + sync.getPermits() + "]";
    }
}
