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

/**
 * A {@code ReadWriteLock} maintains a pair of associated {@link
 * Lock locks}, one for read-only operations and one for writing.
 * The {@link #readLock read lock} may be held simultaneously by
 * multiple reader threads, so long as there are no writers.  The
 * {@link #writeLock write lock} is exclusive.
 *
 * <p>All {@code ReadWriteLock} implementations must guarantee that
 * the memory synchronization effects of {@code writeLock} operations
 * (as specified in the {@link Lock} interface) also hold with respect
 * to the associated {@code readLock}. That is, a thread successfully
 * acquiring the read lock will see all updates made upon previous
 * release of the write lock.
 *
 * <p>A read-write lock allows for a greater level of concurrency in
 * accessing shared data than that permitted by a mutual exclusion lock.
 * It exploits the fact that while only a single thread at a time (a
 * <em>writer</em> thread) can modify the shared data, in many cases any
 * number of threads can concurrently read the data (hence <em>reader</em>
 * threads).
 * In theory, the increase in concurrency permitted by the use of a read-write
 * lock will lead to performance improvements over the use of a mutual
 * exclusion lock. In practice this increase in concurrency will only be fully
 * realized on a multi-processor, and then only if the access patterns for
 * the shared data are suitable.
 *
 * <p>Whether or not a read-write lock will improve performance over the use
 * of a mutual exclusion lock depends on the frequency that the data is
 * read compared to being modified, the duration of the read and write
 * operations, and the contention for the data - that is, the number of
 * threads that will try to read or write the data at the same time.
 * For example, a collection that is initially populated with data and
 * thereafter infrequently modified, while being frequently searched
 * (such as a directory of some kind) is an ideal candidate for the use of
 * a read-write lock. However, if updates become frequent then the data
 * spends most of its time being exclusively locked and there is little, if any
 * increase in concurrency. Further, if the read operations are too short
 * the overhead of the read-write lock implementation (which is inherently
 * more complex than a mutual exclusion lock) can dominate the execution
 * cost, particularly as many read-write lock implementations still serialize
 * all threads through a small section of code. Ultimately, only profiling
 * and measurement will establish whether the use of a read-write lock is
 * suitable for your application.
 *
 *
 * <p>Although the basic operation of a read-write lock is straight-forward,
 * there are many policy decisions that an implementation must make, which
 * may affect the effectiveness of the read-write lock in a given application.
 * Examples of these policies include:
 * <ul>
 * <li>Determining whether to grant the read lock or the write lock, when
 * both readers and writers are waiting, at the time that a writer releases
 * the write lock. Writer preference is common, as writes are expected to be
 * short and infrequent. Reader preference is less common as it can lead to
 * lengthy delays for a write if the readers are frequent and long-lived as
 * expected. Fair, or &quot;in-order&quot; implementations are also possible.
 *
 * <li>Determining whether readers that request the read lock while a
 * reader is active and a writer is waiting, are granted the read lock.
 * Preference to the reader can delay the writer indefinitely, while
 * preference to the writer can reduce the potential for concurrency.
 *
 * <li>Determining whether the locks are reentrant: can a thread with the
 * write lock reacquire it? Can it acquire a read lock while holding the
 * write lock? Is the read lock itself reentrant?
 *
 * <li>Can the write lock be downgraded to a read lock without allowing
 * an intervening writer? Can a read lock be upgraded to a write lock,
 * in preference to other waiting readers or writers?
 *
 * </ul>
 * You should consider all of these things when evaluating the suitability
 * of a given implementation for your application.
 *
 * @see ReentrantReadWriteLock
 * @see Lock
 * @see ReentrantLock
 *
 * @since 1.5
 * @author Doug Lea
 *

ReadWriteLock维护一对相关联的锁，一个用于只读操作，一个用于写。读取锁可以由多个读取器线程同时持有，只要没有写入器。写入锁是独占的。
所有ReadWriteLock实现必须保证writeLock操作(如锁接口中指定的)的内存同步效果也与相关的readLock相关。也就是说，一个成功获取读锁的线程将看到在写锁之前发布的所有更新。
读写锁允许在访问共享数据时比互斥锁允许的并发程度更高。它利用了这样一个事实，即每次只有一个线程(编写线程)可以修改共享数据，在许多情况下，任意数量的线程都可以同时读取数据(因此读取线程)。理论上，使用读-写锁允许的并发性的增加将导致对使用互斥锁的性能改进。实际上，只有当共享数据的访问模式合适时，才能在多处理器上充分实现并发性的增加。
读写锁是否会提高性能的使用互斥锁的频率取决于读取数据被修改相比,读和写操作的持续时间,和争用数据——也就是说,线程的数量,将尝试读或写数据在同一时间。例如，最初使用数据填充的集合，之后不经常修改，而频繁搜索(例如某种目录)是使用读写锁的理想候选对象。但是，如果更新变得频繁，那么数据将花费大部分时间被独占锁定，并且如果并发性有任何增加，那么几乎没有增加。此外，如果读操作太短，那么读-写锁实现的开销(它本质上比互斥锁更复杂)可以控制执行成本，特别是许多读-写锁实现仍然通过一小段代码序列化所有线程。最终，只有分析和度量才能确定使用读写锁是否适合您的应用程序。
尽管读写锁的基本操作是直接进行的，但是实现必须做出许多策略决策，这可能会影响给定应用程序中的读写锁的有效性。这些政策的例子包括:
决定是授予读锁还是授予写锁，当读者和作者都在等待时，当作者释放写锁时。作者偏好是常见的，因为写预期是短和很少。读者偏好不太常见，因为如果读者频繁且寿命长，就会导致写作延迟。Fair或“in-order”实现也是可能的。
确定在读取器处于活动状态和写入器正在等待时请求读锁的读取器是否被授予读锁。对阅读器的首选项可以无限期地延迟写入器，而对写入器的首选项可以减少并发的可能性。
确定锁是否可重入:具有写锁的线程可以重新获取它吗?它能在保持写锁的同时获得读锁吗?读锁本身是可重入的吗?
可以将写锁降级为读锁而不允许插入写入器吗?读锁可以升级为写锁吗?
在评估给定应用程序实现的适用性时，应该考虑所有这些因素。
 */∑
public interface ReadWriteLock {
    /**
     * Returns the lock used for reading.返回用于读取的锁。
     *
     * @return the lock used for reading
     */
    Lock readLock();

    /**
     * Returns the lock used for writing.返回用于写入的锁。
     *
     * @return the lock used for writing
     */
    Lock writeLock();
}
