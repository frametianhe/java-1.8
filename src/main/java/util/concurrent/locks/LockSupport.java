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

package java.util.concurrent.locks;
import sun.misc.Unsafe;

/**
 * Basic thread blocking primitives for creating locks and other
 * synchronization classes.
 *
 * <p>This class associates, with each thread that uses it, a permit
 * (in the sense of the {@link java.util.concurrent.Semaphore
 * Semaphore} class). A call to {@code park} will return immediately
 * if the permit is available, consuming it in the process; otherwise
 * it <em>may</em> block.  A call to {@code unpark} makes the permit
 * available, if it was not already available. (Unlike with Semaphores
 * though, permits do not accumulate. There is at most one.)
 *
 * <p>Methods {@code park} and {@code unpark} provide efficient
 * means of blocking and unblocking threads that do not encounter the
 * problems that cause the deprecated methods {@code Thread.suspend}
 * and {@code Thread.resume} to be unusable for such purposes: Races
 * between one thread invoking {@code park} and another thread trying
 * to {@code unpark} it will preserve liveness, due to the
 * permit. Additionally, {@code park} will return if the caller's
 * thread was interrupted, and timeout versions are supported. The
 * {@code park} method may also return at any other time, for "no
 * reason", so in general must be invoked within a loop that rechecks
 * conditions upon return. In this sense {@code park} serves as an
 * optimization of a "busy wait" that does not waste as much time
 * spinning, but must be paired with an {@code unpark} to be
 * effective.
 *
 * <p>The three forms of {@code park} each also support a
 * {@code blocker} object parameter. This object is recorded while
 * the thread is blocked to permit monitoring and diagnostic tools to
 * identify the reasons that threads are blocked. (Such tools may
 * access blockers using method {@link #getBlocker(Thread)}.)
 * The use of these forms rather than the original forms without this
 * parameter is strongly encouraged. The normal argument to supply as
 * a {@code blocker} within a lock implementation is {@code this}.
 *
 * <p>These methods are designed to be used as tools for creating
 * higher-level synchronization utilities, and are not in themselves
 * useful for most concurrency control applications.  The {@code park}
 * method is designed for use only in constructions of the form:
 *
 *  <pre> {@code
 * while (!canProceed()) { ... LockSupport.park(this); }}</pre>
 *
 * where neither {@code canProceed} nor any other actions prior to the
 * call to {@code park} entail locking or blocking.  Because only one
 * permit is associated with each thread, any intermediary uses of
 * {@code park} could interfere with its intended effects.
 *
 * <p><b>Sample Usage.</b> Here is a sketch of a first-in-first-out
 * non-reentrant lock class:
 *  <pre> {@code
 * class FIFOMutex {
 *   private final AtomicBoolean locked = new AtomicBoolean(false);
 *   private final Queue<Thread> waiters
 *     = new ConcurrentLinkedQueue<Thread>();
 *
 *   public void lock() {
 *     boolean wasInterrupted = false;
 *     Thread current = Thread.currentThread();
 *     waiters.add(current);
 *
 *     // Block while not first in queue or cannot acquire lock
 *     while (waiters.peek() != current ||
 *            !locked.compareAndSet(false, true)) {
 *       LockSupport.park(this);
 *       if (Thread.interrupted()) // ignore interrupts while waiting
 *         wasInterrupted = true;
 *     }
 *
 *     waiters.remove();
 *     if (wasInterrupted)          // reassert interrupt status on exit
 *       current.interrupt();
 *   }
 *
 *   public void unlock() {
 *     locked.set(false);
 *     LockSupport.unpark(waiters.peek());
 *   }
 * }}</pre>
 * 用于创建锁和其他同步类的基本线程阻塞原语。
 这个类与使用它的每个线程关联一个许可证(在信号量类的意义上)。如果有许可证，立即给park打电话，在过程中使用许可证;否则可能会阻止。如果还没有许可证的话，致电unpark可以获得许可证。(与信号量不同的是，许可证不会累积。最多有一个。
 方法park和unpark提供了有效的阻塞和解除阻塞的方法，这些方法不会遇到导致废弃方法线程的问题。暂停和线程。为了这样的目的，恢复是不可用的:调用park的一个线程与试图取消park的另一个线程之间的竞争将保持活力，由于许可。此外，如果调用者的线程被中断，park将返回，并且支持超时版本。park方法也可以在任何其他时间返回，因为“没有原因”，所以通常必须在返回时重新检查条件的循环中调用。从这个意义上说，park是“忙碌等待”的优化，它不会浪费太多时间，但必须与unpark一起使用才能有效。
 park的三种形式都支持块对象参数。当线程被阻塞时，该对象被记录，以允许监视和诊断工具识别线程被阻塞的原因。(这些工具可以使用方法getBlocker(Thread)访问阻滞剂。)强烈鼓励使用这些表单而不是没有此参数的原始表单。在锁实现中作为屏蔽器提供的通常参数是这样的。
 这些方法被设计成用于创建高级同步实用程序的工具，它们本身对大多数并发控制应用程序都没有用处。公园方法只适用于以下形式的建筑:
 而(! canProceed()){…LockSupport.park(这个);}
 在呼叫停车之前，既不能继续进行，也不能采取任何其他行动，这时就需要锁定或阻塞。因为每个线程只关联一个许可，所以park的任何中介使用都可能会影响其预期的效果。
 */
public class LockSupport {
    private LockSupport() {} // Cannot be instantiated.

    private static void setBlocker(Thread t, Object arg) {
        // Even though volatile, hotspot doesn't need a write barrier here.
        UNSAFE.putObject(t, parkBlockerOffset, arg);
    }

    /**
     * Makes available the permit for the given thread, if it
     * was not already available.  If the thread was blocked on
     * {@code park} then it will unblock.  Otherwise, its next call
     * to {@code park} is guaranteed not to block. This operation
     * is not guaranteed to have any effect at all if the given
     * thread has not been started.
     *
     * @param thread the thread to unpark, or {@code null}, in which case
     *        this operation has no effect
     *               为给定的线程提供许可，如果该线程尚未可用。如果线程在park上被阻塞，那么它将解除阻塞。否则，它对park的下一个调用将保证不会被阻塞。如果给定的线程尚未启动，则不保证此操作会有任何效果。
     */
    public static void unpark(Thread thread) {
        if (thread != null)
            UNSAFE.unpark(thread);
    }

    /**
     * Disables the current thread for thread scheduling purposes unless the
     * permit is available.
     *
     * <p>If the permit is available then it is consumed and the call returns
     * immediately; otherwise
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread upon return.
     *
     * @param blocker the synchronization object responsible for this
     *        thread parking
     * @since 1.6
     *
    为线程调度目的禁用当前线程，除非许可是可用的。
    如果许可证是可用的，那么它将被消费，并且调用将立即返回;否则，当前线程为线程调度目的而被禁用，并处于休眠状态，直到以下三种情况之一发生:
    其他一些线程以当前线程为目标调用unpark;或
    其他一些线程中断当前线程;或
    虚假的调用(也就是说，没有理由)返回。
    此方法不报告导致该方法返回的这其中的哪一个。调用者应该首先重新检查导致线程停止的条件。调用者还可以确定，例如，返回时线程的中断状态。
     */
    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(false, 0L);
        setBlocker(t, null);
    }

    /**
     * Disables the current thread for thread scheduling purposes, for up to
     * the specified waiting time, unless the permit is available.
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     *
     * <li>The specified waiting time elapses; or
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the elapsed time
     * upon return.
     *
     * @param blocker the synchronization object responsible for this
     *        thread parking
     * @param nanos the maximum number of nanoseconds to wait
     * @since 1.6
     * 为线程调度目的禁用当前线程，直到指定的等待时间，除非许可证可用。
    如果许可证是可用的，那么它将被消费，并且调用将立即返回;否则，当前线程为线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一:
    其他一些线程以当前线程为目标调用unpark;或
    其他一些线程中断当前线程;或
    指定的等待时间运行;或
    虚假的调用(也就是说，没有理由)返回。
    此方法不报告导致该方法返回的这其中的哪一个。调用者应该首先重新检查导致线程停止的条件。调用者还可以确定线程的中断状态或返回时的运行时间。
     */
    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            UNSAFE.park(false, nanos);
            setBlocker(t, null);
        }
    }

    /**
     * Disables the current thread for thread scheduling purposes, until
     * the specified deadline, unless the permit is available.
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread; or
     *
     * <li>The specified deadline passes; or
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the current time
     * upon return.
     *
     * @param blocker the synchronization object responsible for this
     *        thread parking
     * @param deadline the absolute time, in milliseconds from the Epoch,
     *        to wait until
     * @since 1.6
     * 为线程调度目的禁用当前线程，直到指定的截止日期，除非许可是可用的。
    如果许可证是可用的，那么它将被消费，并且调用将立即返回;否则，当前线程为线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一:
    其他一些线程以当前线程为目标调用unpark;或
    其他一些线程中断当前线程;或
    指定的期限经过;或
    虚假的调用(也就是说，没有理由)返回。
    此方法不报告导致该方法返回的这其中的哪一个。调用者应该首先重新检查导致线程停止的条件。调用者还可以确定，例如，线程的中断状态，或者返回时的当前时间。
     */
    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(true, deadline);
        setBlocker(t, null);
    }

    /**
     * Returns the blocker object supplied to the most recent
     * invocation of a park method that has not yet unblocked, or null
     * if not blocked.  The value returned is just a momentary
     * snapshot -- the thread may have since unblocked or blocked on a
     * different blocker object.
     *
     * @param t the thread
     * @return the blocker
     * @throws NullPointerException if argument is null
     * @since 1.6
     * 返回提供给park方法的最近一次调用的拦截器对象，该方法尚未被解除阻塞，如果未被阻塞，则返回null。返回的值只是一个瞬间的快照——从那时起，线程可能已经在另一个拦截器对象上解除了阻塞或阻塞。
     */
    public static Object getBlocker(Thread t) {
        if (t == null)
            throw new NullPointerException();
        return UNSAFE.getObjectVolatile(t, parkBlockerOffset);
    }

    /**
     * Disables the current thread for thread scheduling purposes unless the
     * permit is available.
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of three
     * things happens:
     *
     * <ul>
     *
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread upon return.
     * 为线程调度目的禁用当前线程，除非许可是可用的。
     如果许可证是可用的，那么它将被消费，并且调用将立即返回;否则，当前线程为线程调度目的而被禁用，并处于休眠状态，直到以下三种情况之一发生:
     其他一些线程以当前线程为目标调用unpark;或
     其他一些线程中断当前线程;或
     虚假的调用(也就是说，没有理由)返回。
     此方法不报告导致该方法返回的这其中的哪一个。调用者应该首先重新检查导致线程停止的条件。调用者还可以确定，例如，返回时线程的中断状态。
     */
    public static void park() {
        UNSAFE.park(false, 0L);
    }

    /**
     * Disables the current thread for thread scheduling purposes, for up to
     * the specified waiting time, unless the permit is available.
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     *
     * <li>The specified waiting time elapses; or
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the elapsed time
     * upon return.
     *
     * @param nanos the maximum number of nanoseconds to wait
     *              为线程调度目的禁用当前线程，直到指定的等待时间，除非许可证可用。
    如果许可证是可用的，那么它将被消费，并且调用将立即返回;否则，当前线程为线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一:
    其他一些线程以当前线程为目标调用unpark;或
    其他一些线程中断当前线程;或
    指定的等待时间运行;或
    虚假的调用(也就是说，没有理由)返回。
    此方法不报告导致该方法返回的这其中的哪一个。调用者应该首先重新检查导致线程停止的条件。调用者还可以确定线程的中断状态或返回时的运行时间。
     */
    public static void parkNanos(long nanos) {
        if (nanos > 0)
            UNSAFE.park(false, nanos);
    }

    /**
     * Disables the current thread for thread scheduling purposes, until
     * the specified deadline, unless the permit is available.
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     *
     * <li>The specified deadline passes; or
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the current time
     * upon return.
     *
     * @param deadline the absolute time, in milliseconds from the Epoch,
     *        to wait until
     *                 为线程调度目的禁用当前线程，直到指定的截止日期，除非许可是可用的。
    如果许可证是可用的，那么它将被消费，并且调用将立即返回;否则，当前线程为线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一:
    其他一些线程以当前线程为目标调用unpark;或
    其他一些线程中断当前线程;或
    指定的期限经过;或
    虚假的调用(也就是说，没有理由)返回。
    此方法不报告导致该方法返回的这其中的哪一个。调用者应该首先重新检查导致线程停止的条件。调用者还可以确定，例如，线程的中断状态，或者返回时的当前时间。
     */
    public static void parkUntil(long deadline) {
        UNSAFE.park(true, deadline);
    }

    /**
     * Returns the pseudo-randomly initialized or updated secondary seed.
     * Copied from ThreadLocalRandom due to package access restrictions.返回伪随机初始化或更新的辅助种子。由于包访问限制，从ThreadLocalRandom复制。
     */
    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else if ((r = java.util.concurrent.ThreadLocalRandom.current().nextInt()) == 0)
            r = 1; // avoid zero
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }

    // Hotspot implementation via intrinsics API
    private static final sun.misc.Unsafe UNSAFE;
    private static final long parkBlockerOffset;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            parkBlockerOffset = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            SEED = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception ex) { throw new Error(ex); }
    }

}
