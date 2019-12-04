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
import java.util.concurrent.TimeUnit;
import java.util.Date;

/**
 * {@code Condition} factors out the {@code Object} monitor
 * methods ({@link Object#wait() wait}, {@link Object#notify notify}
 * and {@link Object#notifyAll notifyAll}) into distinct objects to
 * give the effect of having multiple wait-sets per object, by
 * combining them with the use of arbitrary {@link Lock} implementations.
 * Where a {@code Lock} replaces the use of {@code synchronized} methods
 * and statements, a {@code Condition} replaces the use of the Object
 * monitor methods.
 *
 * <p>Conditions (also known as <em>condition queues</em> or
 * <em>condition variables</em>) provide a means for one thread to
 * suspend execution (to &quot;wait&quot;) until notified by another
 * thread that some state condition may now be true.  Because access
 * to this shared state information occurs in different threads, it
 * must be protected, so a lock of some form is associated with the
 * condition. The key property that waiting for a condition provides
 * is that it <em>atomically</em> releases the associated lock and
 * suspends the current thread, just like {@code Object.wait}.
 *
 * <p>A {@code Condition} instance is intrinsically bound to a lock.
 * To obtain a {@code Condition} instance for a particular {@link Lock}
 * instance use its {@link Lock#newCondition newCondition()} method.
 *
 * <p>As an example, suppose we have a bounded buffer which supports
 * {@code put} and {@code take} methods.  If a
 * {@code take} is attempted on an empty buffer, then the thread will block
 * until an item becomes available; if a {@code put} is attempted on a
 * full buffer, then the thread will block until a space becomes available.
 * We would like to keep waiting {@code put} threads and {@code take}
 * threads in separate wait-sets so that we can use the optimization of
 * only notifying a single thread at a time when items or spaces become
 * available in the buffer. This can be achieved using two
 * {@link Condition} instances.
 * <pre>
 * class BoundedBuffer {
 *   <b>final Lock lock = new ReentrantLock();</b>
 *   final Condition notFull  = <b>lock.newCondition(); </b>
 *   final Condition notEmpty = <b>lock.newCondition(); </b>
 *
 *   final Object[] items = new Object[100];
 *   int putptr, takeptr, count;
 *
 *   public void put(Object x) throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == items.length)
 *         <b>notFull.await();</b>
 *       items[putptr] = x;
 *       if (++putptr == items.length) putptr = 0;
 *       ++count;
 *       <b>notEmpty.signal();</b>
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 *
 *   public Object take() throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == 0)
 *         <b>notEmpty.await();</b>
 *       Object x = items[takeptr];
 *       if (++takeptr == items.length) takeptr = 0;
 *       --count;
 *       <b>notFull.signal();</b>
 *       return x;
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 * }
 * </pre>
 *
 * (The {@link java.util.concurrent.ArrayBlockingQueue} class provides
 * this functionality, so there is no reason to implement this
 * sample usage class.)
 *
 * <p>A {@code Condition} implementation can provide behavior and semantics
 * that is
 * different from that of the {@code Object} monitor methods, such as
 * guaranteed ordering for notifications, or not requiring a lock to be held
 * when performing notifications.
 * If an implementation provides such specialized semantics then the
 * implementation must document those semantics.
 *
 * <p>Note that {@code Condition} instances are just normal objects and can
 * themselves be used as the target in a {@code synchronized} statement,
 * and can have their own monitor {@link Object#wait wait} and
 * {@link Object#notify notification} methods invoked.
 * Acquiring the monitor lock of a {@code Condition} instance, or using its
 * monitor methods, has no specified relationship with acquiring the
 * {@link Lock} associated with that {@code Condition} or the use of its
 * {@linkplain #await waiting} and {@linkplain #signal signalling} methods.
 * It is recommended that to avoid confusion you never use {@code Condition}
 * instances in this way, except perhaps within their own implementation.
 *
 * <p>Except where noted, passing a {@code null} value for any parameter
 * will result in a {@link NullPointerException} being thrown.
 *
 * <h3>Implementation Considerations</h3>
 *
 * <p>When waiting upon a {@code Condition}, a &quot;<em>spurious
 * wakeup</em>&quot; is permitted to occur, in
 * general, as a concession to the underlying platform semantics.
 * This has little practical impact on most application programs as a
 * {@code Condition} should always be waited upon in a loop, testing
 * the state predicate that is being waited for.  An implementation is
 * free to remove the possibility of spurious wakeups but it is
 * recommended that applications programmers always assume that they can
 * occur and so always wait in a loop.
 *
 * <p>The three forms of condition waiting
 * (interruptible, non-interruptible, and timed) may differ in their ease of
 * implementation on some platforms and in their performance characteristics.
 * In particular, it may be difficult to provide these features and maintain
 * specific semantics such as ordering guarantees.
 * Further, the ability to interrupt the actual suspension of the thread may
 * not always be feasible to implement on all platforms.
 *
 * <p>Consequently, an implementation is not required to define exactly the
 * same guarantees or semantics for all three forms of waiting, nor is it
 * required to support interruption of the actual suspension of the thread.
 *
 * <p>An implementation is required to
 * clearly document the semantics and guarantees provided by each of the
 * waiting methods, and when an implementation does support interruption of
 * thread suspension then it must obey the interruption semantics as defined
 * in this interface.
 *
 * <p>As interruption generally implies cancellation, and checks for
 * interruption are often infrequent, an implementation can favor responding
 * to an interrupt over normal method return. This is true even if it can be
 * shown that the interrupt occurred after another action that may have
 * unblocked the thread. An implementation should document this behavior.
 *
 * @since 1.5
 * @author Doug Lea
 * Condition将对象监视方法(wait、notify和notifyAll)分解为不同的对象，通过将它们与使用任意锁实现相结合，使每个对象具有多个等待集的效果。当一个锁替代了同步方法和语句的使用时，一个条件将取代使用对象监视器方法。
条件(也称为条件队列或条件变量)为一个线程提供了暂停执行(“等待”)的方法，直到另一个线程通知某个状态条件现在可能为真。因为对这个共享状态信息的访问发生在不同的线程中，所以必须保护它，所以某种形式的锁与该条件相关联。等待条件提供的关键属性是，它自动释放关联的锁并挂起当前线程，就像Object.wait一样。
条件实例本质上是与锁绑定的。为获取特定锁实例的条件实例，使用它的newCondition()方法。
例如，假设我们有一个有界缓冲区，它支持put和take方法。如果尝试在空缓冲区中执行，则线程将阻塞，直到有项可用为止;如果在一个完整的缓冲区上尝试put，那么线程将阻塞，直到空间可用。我们希望继续等待，将线程放在不同的等待集中，以便当缓冲区中的项或空间可用时，我们可以使用只通知单个线程的优化。这可以通过使用两个条件实例来实现。
(java . util . concurrent。ArrayBlockingQueue类提供了这个功能，因此没有理由实现这个示例用法类)。
条件实现可以提供与对象监视器方法不同的行为和语义，比如保证通知的排序，或者在执行通知时不需要锁。如果实现提供了这种专门的语义，那么实现必须记录这些语义。
注意，条件实例只是普通的对象，它们本身可以作为同步语句中的目标，并且可以拥有自己的监视等待和通知方法。获取条件实例的监视锁，或使用其监视方法，与获取与该条件相关的锁或使用其等待和信号方法没有指定的关系。为了避免混淆，建议您永远不要以这种方式使用条件实例，除非是在它们自己的实现中。
除非注意到，为任何参数传递null值将导致抛出NullPointerException。
实现注意事项
在等待条件时，通常允许出现“伪唤醒”，作为对底层平台语义的让步。这对大多数应用程序几乎没有实际影响，因为条件应该始终在循环中等待，测试正在等待的状态谓词。实现可以自由地删除伪wakeups的可能性，但建议应用程序程序员始终假设它们可以发生，因此总是在循环中等待。
三种等待条件的形式(可中断的、不可中断的和计时的)可能在某些平台上实现的易用性和性能特征上有所不同。特别是，可能很难提供这些特性并维护特定的语义，比如排序保证。此外，在所有平台上实现中断线程的实际挂起的能力可能并不总是可行的。
因此，不需要实现为所有三种等待形式定义完全相同的保证或语义，也不需要实现支持线程实际暂停的中断。
需要实现清楚地记录每个等待方法提供的语义和保证，并且当一个实现支持线程暂停的中断时，它必须遵守这个接口中定义的中断语义。
由于中断通常意味着取消，而且对中断的检查通常很少，所以实现可以优先于对中断的响应，而不是常规的方法返回。这是正确的，即使可以显示中断发生在另一个动作之后，而这个动作可能已经解除了线程的阻塞。实现应该记录这种行为。
 */
public interface Condition {

    /**
     * Causes the current thread to wait until it is signalled or
     * {@linkplain Thread#interrupt interrupted}.
     *
     * <p>The lock associated with this {@code Condition} is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of four things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of thread suspension is supported; or
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * and interruption of thread suspension is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock
     * is released.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return in response to a signal. In that case the implementation
     * must ensure that the signal is redirected to another waiting thread, if
     * there is one.
     *
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         使当前线程等待，直到发出信号或中断。
    与此条件相关的锁被原子释放，当前线程出于线程调度目的被禁用，处于休眠状态，直到发生以下四种情况之一:
    另一些线程为该条件调用信号方法，而当前线程恰好被选择为要唤醒的线程;或
    其他一些线程为这个条件调用signalAll方法;或
    其他线程中断当前线程，支持线程暂停中断;或
    “虚假唤醒”发生。
    在所有情况下，在此方法返回当前线程之前，必须重新获得与此条件相关联的锁。当线程返回时，它保证持有这个锁。
    如果当前线程:
    其中断状态设置为该方法的入口;或
    在等待时中断，支持线程暂停中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。在第一种情况下，不指定是否在释放锁之前进行中断测试。
    实现注意事项
    当调用此方法时，假定当前线程持有与此条件相关联的锁。这取决于实现，以确定这种情况是否存在，如果不是，如何应对。通常会抛出一个异常(例如IllegalMonitorStateException)，而实现必须记录这个事实。
    实现可以支持对中断的响应，而不是响应信号的常规方法返回。在这种情况下，实现必须确保信号被重定向到另一个正在等待的线程(如果有的话)。
     */
    void await() throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled.
     *
     * <p>The lock associated with this condition is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of three things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     *
     * <p>If the current thread's interrupted status is set when it enters
     * this method, or it is {@linkplain Thread#interrupt interrupted}
     * while waiting, it will continue to wait until signalled. When it finally
     * returns from this method its interrupted status will still
     * be set.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     * 使当前线程等待，直到发出信号。
     与此条件相关的锁被原子释放，当前线程出于线程调度目的被禁用，处于休眠状态，直到发生以下三种情况之一:
     另一些线程为该条件调用信号方法，而当前线程恰好被选择为要唤醒的线程;或
     其他一些线程为这个条件调用signalAll方法;或
     “虚假唤醒”发生。
     在所有情况下，在此方法返回当前线程之前，必须重新获得与此条件相关联的锁。当线程返回时，它保证持有这个锁。
     如果当前线程在进入该方法时设置了中断状态，或者在等待时被中断，那么它将继续等待，直到发出信号。当它最终从这个方法返回时，它的中断状态仍将被设置。
     实现注意事项
     当调用此方法时，假定当前线程持有与此条件相关联的锁。这取决于实现，以确定这种情况是否存在，如果不是，如何应对。通常会抛出一个异常(例如IllegalMonitorStateException)，而实现必须记录这个事实。
     */
    void awaitUninterruptibly();

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified waiting time elapses.
     *
     * <p>The lock associated with this condition is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of five things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of thread suspension is supported; or
     * <li>The specified waiting time elapses; or
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * and interruption of thread suspension is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock
     * is released.
     *
     * <p>The method returns an estimate of the number of nanoseconds
     * remaining to wait given the supplied {@code nanosTimeout}
     * value upon return, or a value less than or equal to zero if it
     * timed out. This value can be used to determine whether and how
     * long to re-wait in cases where the wait returns but an awaited
     * condition still does not hold. Typical uses of this method take
     * the following form:
     *
     *  <pre> {@code
     * boolean aMethod(long timeout, TimeUnit unit) {
     *   long nanos = unit.toNanos(timeout);
     *   lock.lock();
     *   try {
     *     while (!conditionBeingWaitedFor()) {
     *       if (nanos <= 0L)
     *         return false;
     *       nanos = theCondition.awaitNanos(nanos);
     *     }
     *     // ...
     *   } finally {
     *     lock.unlock();
     *   }
     * }}</pre>
     *
     * <p>Design note: This method requires a nanosecond argument so
     * as to avoid truncation errors in reporting remaining times.
     * Such precision loss would make it difficult for programmers to
     * ensure that total waiting times are not systematically shorter
     * than specified when re-waits occur.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return in response to a signal, or over indicating the elapse
     * of the specified waiting time. In either case the implementation
     * must ensure that the signal is redirected to another waiting thread, if
     * there is one.
     *
     * @param nanosTimeout the maximum time to wait, in nanoseconds
     * @return an estimate of the {@code nanosTimeout} value minus
     *         the time spent waiting upon return from this method.
     *         A positive value may be used as the argument to a
     *         subsequent call to this method to finish waiting out
     *         the desired time.  A value less than or equal to zero
     *         indicates that no time remains.
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         使当前线程等待，直到发出信号或中断，或指定的等待时间过去。
    与此条件相关的锁被原子释放，当前线程出于线程调度目的被禁用，处于休眠状态，直到发生以下五种情况之一:
    另一些线程为该条件调用信号方法，而当前线程恰好被选择为要唤醒的线程;或
    其他一些线程为这个条件调用signalAll方法;或
    其他线程中断当前线程，支持线程暂停中断;或
    指定的等待时间运行;或
    “虚假唤醒”发生。
    在所有情况下，在此方法返回当前线程之前，必须重新获得与此条件相关联的锁。当线程返回时，它保证持有这个锁。
    如果当前线程:
    其中断状态设置为该方法的入口;或
    在等待时中断，支持线程暂停中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。在第一种情况下，不指定是否在释放锁之前进行中断测试。
    该方法返回给定返回时提供的nanosTimeout值，或者如果超时，则返回小于或等于零的值，则等待的纳秒数的估计值。这个值可以用来确定等待返回的情况下等待的时间和等待的时间，但是等待的条件仍然不成立。该方法的典型用途如下:
    设计注意:此方法需要一个纳秒参数，以避免报告剩余时间时的截断错误。这样的精度损失将使程序员很难确保在重新等待时总等待时间不会比指定的短。
    实现注意事项
    当调用此方法时，假定当前线程持有与此条件相关联的锁。这取决于实现，以确定这种情况是否存在，如果不是，如何应对。通常会抛出一个异常(例如IllegalMonitorStateException)，而实现必须记录这个事实。
    实现可以优先响应中断，而不是响应信号的普通方法返回，或者超过指定等待时间的时间。无论哪种情况，实现都必须确保信号被重定向到另一个正在等待的线程(如果有的话)。
     */
    long awaitNanos(long nanosTimeout) throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified waiting time elapses. This method is behaviorally
     * equivalent to:
     *  <pre> {@code awaitNanos(unit.toNanos(time)) > 0}</pre>
     *
     * @param time the maximum time to wait
     * @param unit the time unit of the {@code time} argument
     * @return {@code false} if the waiting time detectably elapsed
     *         before return from the method, else {@code true}
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         使当前线程等待，直到发出信号或中断，或指定的等待时间过去。这种方法在行为上等价于:
    awaitNanos(unit.toNanos(时间))> 0
     */
    boolean await(long time, TimeUnit unit) throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified deadline elapses.
     *
     * <p>The lock associated with this condition is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of five things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of thread suspension is supported; or
     * <li>The specified deadline elapses; or
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     *
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * and interruption of thread suspension is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock
     * is released.
     *
     *
     * <p>The return value indicates whether the deadline has elapsed,
     * which can be used as follows:
     *  <pre> {@code
     * boolean aMethod(Date deadline) {
     *   boolean stillWaiting = true;
     *   lock.lock();
     *   try {
     *     while (!conditionBeingWaitedFor()) {
     *       if (!stillWaiting)
     *         return false;
     *       stillWaiting = theCondition.awaitUntil(deadline);
     *     }
     *     // ...
     *   } finally {
     *     lock.unlock();
     *   }
     * }}</pre>
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return in response to a signal, or over indicating the passing
     * of the specified deadline. In either case the implementation
     * must ensure that the signal is redirected to another waiting thread, if
     * there is one.
     *
     * @param deadline the absolute time to wait until
     * @return {@code false} if the deadline has elapsed upon return, else
     *         {@code true}
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         使当前线程等待，直到发出信号或被中断，或指定的最后期限过去。
    与此条件相关的锁被原子释放，当前线程出于线程调度目的被禁用，处于休眠状态，直到发生以下五种情况之一:
    另一些线程为该条件调用信号方法，而当前线程恰好被选择为要唤醒的线程;或
    其他一些线程为这个条件调用signalAll方法;或
    其他线程中断当前线程，支持线程暂停中断;或
    指定的期限过后;或
    “虚假唤醒”发生。
    在所有情况下，在此方法返回当前线程之前，必须重新获得与此条件相关联的锁。当线程返回时，它保证持有这个锁。
    如果当前线程:
    其中断状态设置为该方法的入口;或
    在等待时中断，支持线程暂停中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。在第一种情况下，不指定是否在释放锁之前进行中断测试。
    实现注意事项
    当调用此方法时，假定当前线程持有与此条件相关联的锁。这取决于实现，以确定这种情况是否存在，如果不是，如何应对。通常会抛出一个异常(例如IllegalMonitorStateException)，而实现必须记录这个事实。
    实现可以支持对中断的响应，而不是响应信号的常规方法返回，也可以支持对指定期限的传递的响应。无论哪种情况，实现都必须确保信号被重定向到另一个正在等待的线程(如果有的话)。
     */
    boolean awaitUntil(Date deadline) throws InterruptedException;

    /**
     * Wakes up one waiting thread.
     *
     * <p>If any threads are waiting on this condition then one
     * is selected for waking up. That thread must then re-acquire the
     * lock before returning from {@code await}.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>An implementation may (and typically does) require that the
     * current thread hold the lock associated with this {@code
     * Condition} when this method is called. Implementations must
     * document this precondition and any actions taken if the lock is
     * not held. Typically, an exception such as {@link
     * IllegalMonitorStateException} will be thrown.
     * 唤醒一个等待的线程。
     如果有线程在此条件下等待，则选择一个线程作为唤醒。线程必须在等待返回之前重新获得锁。
     实现注意事项
     实现可能(通常是)要求当前线程在调用此方法时持有与此条件相关联的锁。实现必须记录这个先决条件以及如果锁不被持有所采取的任何操作。通常，会抛出一个异常，例如IllegalMonitorStateException。
     */
    void signal();

    /**
     * Wakes up all waiting threads.
     *
     * <p>If any threads are waiting on this condition then they are
     * all woken up. Each thread must re-acquire the lock before it can
     * return from {@code await}.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>An implementation may (and typically does) require that the
     * current thread hold the lock associated with this {@code
     * Condition} when this method is called. Implementations must
     * document this precondition and any actions taken if the lock is
     * not held. Typically, an exception such as {@link
     * IllegalMonitorStateException} will be thrown.
     * 唤醒所有等待的线程。
     如果有任何线程在等待这个条件，那么它们都将被唤醒。每个线程必须重新获得锁，然后才能返回等待。
     实现注意事项
     实现可能(通常是)要求当前线程在调用此方法时持有与此条件相关联的锁。实现必须记录这个先决条件以及如果锁不被持有所采取的任何操作。通常，会抛出一个异常，例如IllegalMonitorStateException。
     */
    void signalAll();
}
