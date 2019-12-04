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

/**
 * {@code Lock} implementations provide more extensive locking
 * operations than can be obtained using {@code synchronized} methods
 * and statements.  They allow more flexible structuring, may have
 * quite different properties, and may support multiple associated
 * {@link Condition} objects.
 *
 * <p>A lock is a tool for controlling access to a shared resource by
 * multiple threads. Commonly, a lock provides exclusive access to a
 * shared resource: only one thread at a time can acquire the lock and
 * all access to the shared resource requires that the lock be
 * acquired first. However, some locks may allow concurrent access to
 * a shared resource, such as the read lock of a {@link ReadWriteLock}.
 *
 * <p>The use of {@code synchronized} methods or statements provides
 * access to the implicit monitor lock associated with every object, but
 * forces all lock acquisition and release to occur in a block-structured way:
 * when multiple locks are acquired they must be released in the opposite
 * order, and all locks must be released in the same lexical scope in which
 * they were acquired.
 *
 * <p>While the scoping mechanism for {@code synchronized} methods
 * and statements makes it much easier to program with monitor locks,
 * and helps avoid many common programming errors involving locks,
 * there are occasions where you need to work with locks in a more
 * flexible way. For example, some algorithms for traversing
 * concurrently accessed data structures require the use of
 * &quot;hand-over-hand&quot; or &quot;chain locking&quot;: you
 * acquire the lock of node A, then node B, then release A and acquire
 * C, then release B and acquire D and so on.  Implementations of the
 * {@code Lock} interface enable the use of such techniques by
 * allowing a lock to be acquired and released in different scopes,
 * and allowing multiple locks to be acquired and released in any
 * order.
 *
 * <p>With this increased flexibility comes additional
 * responsibility. The absence of block-structured locking removes the
 * automatic release of locks that occurs with {@code synchronized}
 * methods and statements. In most cases, the following idiom
 * should be used:
 *
 *  <pre> {@code
 * Lock l = ...;
 * l.lock();
 * try {
 *   // access the resource protected by this lock
 * } finally {
 *   l.unlock();
 * }}</pre>
 *
 * When locking and unlocking occur in different scopes, care must be
 * taken to ensure that all code that is executed while the lock is
 * held is protected by try-finally or try-catch to ensure that the
 * lock is released when necessary.
 *
 * <p>{@code Lock} implementations provide additional functionality
 * over the use of {@code synchronized} methods and statements by
 * providing a non-blocking attempt to acquire a lock ({@link
 * #tryLock()}), an attempt to acquire the lock that can be
 * interrupted ({@link #lockInterruptibly}, and an attempt to acquire
 * the lock that can timeout ({@link #tryLock(long, TimeUnit)}).
 *
 * <p>A {@code Lock} class can also provide behavior and semantics
 * that is quite different from that of the implicit monitor lock,
 * such as guaranteed ordering, non-reentrant usage, or deadlock
 * detection. If an implementation provides such specialized semantics
 * then the implementation must document those semantics.
 *
 * <p>Note that {@code Lock} instances are just normal objects and can
 * themselves be used as the target in a {@code synchronized} statement.
 * Acquiring the
 * monitor lock of a {@code Lock} instance has no specified relationship
 * with invoking any of the {@link #lock} methods of that instance.
 * It is recommended that to avoid confusion you never use {@code Lock}
 * instances in this way, except within their own implementation.
 *
 * <p>Except where noted, passing a {@code null} value for any
 * parameter will result in a {@link NullPointerException} being
 * thrown.
 *
 * <h3>Memory Synchronization</h3>
 *
 * <p>All {@code Lock} implementations <em>must</em> enforce the same
 * memory synchronization semantics as provided by the built-in monitor
 * lock, as described in
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * The Java Language Specification (17.4 Memory Model)</a>:
 * <ul>
 * <li>A successful {@code lock} operation has the same memory
 * synchronization effects as a successful <em>Lock</em> action.
 * <li>A successful {@code unlock} operation has the same
 * memory synchronization effects as a successful <em>Unlock</em> action.
 * </ul>
 *
 * Unsuccessful locking and unlocking operations, and reentrant
 * locking/unlocking operations, do not require any memory
 * synchronization effects.
 *
 * <h3>Implementation Considerations</h3>
 *
 * <p>The three forms of lock acquisition (interruptible,
 * non-interruptible, and timed) may differ in their performance
 * characteristics, ordering guarantees, or other implementation
 * qualities.  Further, the ability to interrupt the <em>ongoing</em>
 * acquisition of a lock may not be available in a given {@code Lock}
 * class.  Consequently, an implementation is not required to define
 * exactly the same guarantees or semantics for all three forms of
 * lock acquisition, nor is it required to support interruption of an
 * ongoing lock acquisition.  An implementation is required to clearly
 * document the semantics and guarantees provided by each of the
 * locking methods. It must also obey the interruption semantics as
 * defined in this interface, to the extent that interruption of lock
 * acquisition is supported: which is either totally, or only on
 * method entry.
 *
 * <p>As interruption generally implies cancellation, and checks for
 * interruption are often infrequent, an implementation can favor responding
 * to an interrupt over normal method return. This is true even if it can be
 * shown that the interrupt occurred after another action may have unblocked
 * the thread. An implementation should document this behavior.
 *
 * @see ReentrantLock
 * @see Condition
 * @see ReadWriteLock
 *
 * @since 1.5
 * @author Doug Lea
 * 锁实现提供了比使用同步方法和语句获得的更广泛的锁操作。它们允许更灵活的结构，可能具有完全不同的属性，并且可能支持多个相关联的条件对象。
锁是一种工具，用于控制多个线程对共享资源的访问。通常，锁提供对共享资源的独占访问:每次只有一个线程可以获取锁，对共享资源的所有访问都需要首先获取锁。但是，一些锁可能允许并发访问共享资源，例如ReadWriteLock的读锁。
使用同步方法或语句提供与每个对象关联的隐式监视器锁,但部队所有锁获取和释放发生在结构方式:在获得多个锁时,它们必须被释放在相反的顺序,和所有的锁都必须在相同的词法作用域的。
虽然同步方法和语句的范围机制使使用monitor锁编程更容易，并且有助于避免许多涉及锁的常见编程错误，但是在某些情况下，您需要以更灵活的方式处理锁。例如，一些针对并发访问的数据结构的算法需要使用“手动操作”或“链锁”:您获得节点A的锁，然后是节点B，然后释放A，获取C，然后释放B，获取D等。锁接口的实现允许在不同的作用域中获取和释放锁，并允许以任何顺序获取和释放多个锁，从而允许使用这些技术。
增加的灵活性带来了额外的责任。缺少块结构的锁定可以消除同步方法和语句所产生的锁的自动释放。在大多数情况下，应该使用以下习语:
当锁定和解锁发生在不同的作用域时，必须注意确保在持有锁时执行的所有代码都受到try-finally或try-catch的保护，以确保锁在必要时释放。
锁实现通过提供非阻塞的尝试来获取锁(tryLock())，尝试获取可以被中断的锁(lockinterruptily)，并尝试获取可以超时的锁(tryLock(long, TimeUnit))，从而在使用同步方法和语句时提供附加功能。
锁类还可以提供与隐式监视锁完全不同的行为和语义，如保证排序、不可重入使用或死锁检测。如果实现提供了这种专门的语义，那么实现必须记录这些语义。
注意，锁实例只是普通的对象，它们本身可以作为同步语句中的目标。获取锁实例的监视锁与调用该实例的任何锁方法没有指定的关系。建议避免混淆，除非在它们自己的实现中，否则不要用这种方式使用锁实例。
除非注意到，为任何参数传递null值将导致抛出NullPointerException。
 */
public interface Lock {

    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation may be able to detect erroneous use
     * of the lock, such as an invocation that would cause deadlock, and
     * may throw an (unchecked) exception in such circumstances.  The
     * circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     * 获得锁。
     如果锁不可用，则为线程调度目的禁用当前线程，并处于休眠状态，直到获得锁为止。
     实现注意事项
     锁定实现可以检测到错误使用锁的情况，例如会导致死锁的调用，并可能在这种情况下抛出(未检查的)异常。这种情况和异常类型必须由锁实现记录。
     */
    void lock();

    /**
     * Acquires the lock unless the current thread is
     * {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires the lock if it is available and returns immediately.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * one of two things happens:
     *
     * <ul>
     * <li>The lock is acquired by the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of lock acquisition is supported.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
     * lock, and interruption of lock acquisition is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The ability to interrupt a lock acquisition in some
     * implementations may not be possible, and if possible may be an
     * expensive operation.  The programmer should be aware that this
     * may be the case. An implementation should document when this is
     * the case.
     *
     * <p>An implementation can favor responding to an interrupt over
     * normal method return.
     *
     * <p>A {@code Lock} implementation may be able to detect
     * erroneous use of the lock, such as an invocation that would
     * cause deadlock, and may throw an (unchecked) exception in such
     * circumstances.  The circumstances and the exception type must
     * be documented by that {@code Lock} implementation.
     *
     * @throws InterruptedException if the current thread is
     *         interrupted while acquiring the lock (and interruption
     *         of lock acquisition is supported)
     *         获取锁，除非当前线程被中断。
    如果锁可用，则获取锁并立即返回。
    如果锁不可用，那么为了线程调度目的，当前线程将被禁用，并处于休眠状态，直到发生以下两种情况之一:
    锁由当前线程获取;或
    另一些线程中断当前线程，并支持锁捕获的中断。
    如果当前线程:
    其中断状态设置为该方法的入口;或
    获取锁时被中断，支持锁获取中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。
    实现注意事项
    在某些实现中中断锁获取的能力可能是不可能的，如果可能的话，可能是一个昂贵的操作。程序员应该意识到这可能是事实。实现应该在这种情况下记录。
    实现可以支持对中断的响应，而不是普通的方法返回。
    锁定实现可以检测到错误使用锁的情况，例如会导致死锁的调用，并可能在这种情况下抛出(未检查的)异常。这种情况和异常类型必须由锁实现记录。
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * Acquires the lock only if it is free at the time of invocation.
     *
     * <p>Acquires the lock if it is available and returns immediately
     * with the value {@code true}.
     * If the lock is not available then this method will return
     * immediately with the value {@code false}.
     *
     * <p>A typical usage idiom for this method would be:
     *  <pre> {@code
     * Lock lock = ...;
     * if (lock.tryLock()) {
     *   try {
     *     // manipulate protected state
     *   } finally {
     *     lock.unlock();
     *   }
     * } else {
     *   // perform alternative actions
     * }}</pre>
     *
     * This usage ensures that the lock is unlocked if it was acquired, and
     * doesn't try to unlock if the lock was not acquired.
     *
     * @return {@code true} if the lock was acquired and
     *         {@code false} otherwise
     *         只有在调用时锁是空闲的时才获取锁。
    如果锁是可用的，则获取锁，并立即返回值为true。如果锁不可用，那么该方法将立即返回值false。
    这种方法的典型用法是:
    锁锁=…;
    如果(lock.tryLock()){
    尝试{
    / /操作保护状态
    最后} {
    lock.unlock();
    }
    其他} {
    / /执行选择操作
    }
    这种用法确保在获得锁时解锁，在未获得锁时不会尝试解锁。
     */
    boolean tryLock();

    /**
     * Acquires the lock if it is free within the given waiting time and the
     * current thread has not been {@linkplain Thread#interrupt interrupted}.
     *
     * <p>If the lock is available this method returns immediately
     * with the value {@code true}.
     * If the lock is not available then
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     * <ul>
     * <li>The lock is acquired by the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of lock acquisition is supported; or
     * <li>The specified waiting time elapses
     * </ul>
     *
     * <p>If the lock is acquired then the value {@code true} is returned.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring
     * the lock, and interruption of lock acquisition is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.
     * If the time is
     * less than or equal to zero, the method will not wait at all.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The ability to interrupt a lock acquisition in some implementations
     * may not be possible, and if possible may
     * be an expensive operation.
     * The programmer should be aware that this may be the case. An
     * implementation should document when this is the case.
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return, or reporting a timeout.
     *
     * <p>A {@code Lock} implementation may be able to detect
     * erroneous use of the lock, such as an invocation that would cause
     * deadlock, and may throw an (unchecked) exception in such circumstances.
     * The circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     *
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     *         if the waiting time elapsed before the lock was acquired
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while acquiring the lock (and interruption of lock
     *         acquisition is supported)
     *         如果锁在给定的等待时间内是空闲的，并且当前线程没有被中断，则获取锁。
    如果锁是可用的，此方法立即返回值true。如果锁不可用，那么为了线程调度目的，当前线程将被禁用，并处于休眠状态，直到发生以下三种情况之一:
    锁由当前线程获取;或
    其他线程中断当前线程，支持锁获取中断;或
    指定的等待时间运行
    如果获取锁，则返回值true。
    如果当前线程:
    其中断状态设置为该方法的入口;或
    获取锁时被中断，支持锁获取中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。
    如果指定的等待时间过去，则返回值false。如果时间小于或等于零，则该方法根本不会等待。
    实现注意事项
    在某些实现中中断锁获取的能力可能是不可能的，如果可能的话，可能是一个昂贵的操作。程序员应该意识到这可能是事实。实现应该在这种情况下记录。
    实现可以优先响应中断，而不是常规方法返回，或者报告超时。
    锁定实现可以检测到错误使用锁的情况，例如会导致死锁的调用，并可能在这种情况下抛出(未检查的)异常。这种情况和异常类型必须由锁实现记录。
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    /**
     * Releases the lock.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation will usually impose
     * restrictions on which thread can release a lock (typically only the
     * holder of the lock can release it) and may throw
     * an (unchecked) exception if the restriction is violated.
     * Any restrictions and the exception
     * type must be documented by that {@code Lock} implementation.
     * 释放锁。
     实现注意事项
     锁实现通常会对线程释放锁(通常只有锁的持有者才能释放锁)施加限制，如果违反了限制，则可能抛出(未检查的)异常。任何限制和异常类型都必须由锁实现记录。
     */
    void unlock();

    /**
     * Returns a new {@link Condition} instance that is bound to this
     * {@code Lock} instance.
     *
     * <p>Before waiting on the condition the lock must be held by the
     * current thread.
     * A call to {@link Condition#await()} will atomically release the lock
     * before waiting and re-acquire the lock before the wait returns.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The exact operation of the {@link Condition} instance depends on
     * the {@code Lock} implementation and must be documented by that
     * implementation.
     *
     * @return A new {@link Condition} instance for this {@code Lock} instance
     * @throws UnsupportedOperationException if this {@code Lock}
     *         implementation does not support conditions
     *         返回绑定到这个锁实例的新条件实例。
    在等待条件之前，锁必须由当前线程持有。对condition . wait()的调用将在等待之前自动释放锁，并在等待返回之前重新获取锁。
    实现注意事项
    条件实例的确切操作依赖于锁实现，并且必须由该实现记录。
     */
    Condition newCondition();
}
