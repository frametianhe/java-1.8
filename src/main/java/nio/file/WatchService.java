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

package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A watch service that <em>watches</em> registered objects for changes and
 * events. For example a file manager may use a watch service to monitor a
 * directory for changes so that it can update its display of the list of files
 * when files are created or deleted.
 *
 * <p> A {@link Watchable} object is registered with a watch service by invoking
 * its {@link Watchable#register register} method, returning a {@link WatchKey}
 * to represent the registration. When an event for an object is detected the
 * key is <em>signalled</em>, and if not currently signalled, it is queued to
 * the watch service so that it can be retrieved by consumers that invoke the
 * {@link #poll() poll} or {@link #take() take} methods to retrieve keys
 * and process events. Once the events have been processed the consumer
 * invokes the key's {@link WatchKey#reset reset} method to reset the key which
 * allows the key to be signalled and re-queued with further events.
 *
 * <p> Registration with a watch service is cancelled by invoking the key's
 * {@link WatchKey#cancel cancel} method. A key that is queued at the time that
 * it is cancelled remains in the queue until it is retrieved. Depending on the
 * object, a key may be cancelled automatically. For example, suppose a
 * directory is watched and the watch service detects that it has been deleted
 * or its file system is no longer accessible. When a key is cancelled in this
 * manner it is signalled and queued, if not currently signalled. To ensure
 * that the consumer is notified the return value from the {@code reset}
 * method indicates if the key is valid.
 *
 * <p> A watch service is safe for use by multiple concurrent consumers. To
 * ensure that only one consumer processes the events for a particular object at
 * any time then care should be taken to ensure that the key's {@code reset}
 * method is only invoked after its events have been processed. The {@link
 * #close close} method may be invoked at any time to close the service causing
 * any threads waiting to retrieve keys, to throw {@code
 * ClosedWatchServiceException}.
 *
 * <p> File systems may report events faster than they can be retrieved or
 * processed and an implementation may impose an unspecified limit on the number
 * of events that it may accumulate. Where an implementation <em>knowingly</em>
 * discards events then it arranges for the key's {@link WatchKey#pollEvents
 * pollEvents} method to return an element with an event type of {@link
 * StandardWatchEventKinds#OVERFLOW OVERFLOW}. This event can be used by the
 * consumer as a trigger to re-examine the state of the object.
 *
 * <p> When an event is reported to indicate that a file in a watched directory
 * has been modified then there is no guarantee that the program (or programs)
 * that have modified the file have completed. Care should be taken to coordinate
 * access with other programs that may be updating the file.
 * The {@link java.nio.channels.FileChannel FileChannel} class defines methods
 * to lock regions of a file against access by other programs.
 *
 * <h2>Platform dependencies</h2>
 *
 * <p> The implementation that observes events from the file system is intended
 * to map directly on to the native file event notification facility where
 * available, or to use a primitive mechanism, such as polling, when a native
 * facility is not available. Consequently, many of the details on how events
 * are detected, their timeliness, and whether their ordering is preserved are
 * highly implementation specific. For example, when a file in a watched
 * directory is modified then it may result in a single {@link
 * StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} event in some
 * implementations but several events in other implementations. Short-lived
 * files (meaning files that are deleted very quickly after they are created)
 * may not be detected by primitive implementations that periodically poll the
 * file system to detect changes.
 *
 * <p> If a watched file is not located on a local storage device then it is
 * implementation specific if changes to the file can be detected. In particular,
 * it is not required that changes to files carried out on remote systems be
 * detected.
 *
 * @since 1.7
 *
 * @see FileSystem#newWatchService
 * 监视服务，监视注册对象的更改和事件。例如，文件管理器可以使用监视服务来监视目录的更改，以便在创建或删除文件时更新文件列表的显示。
可监视对象通过调用其寄存器方法向监视服务注册，返回一个WatchKey来表示注册。当检测到一个对象的事件时，密钥就会被发出信号，如果不是当前发出的，那么它将排队到watch服务，以便调用轮询或使用方法检索密钥和处理事件的使用者能够检索到它。一旦事件被处理完，使用者将调用密钥的重置方法来重置密钥，该方法允许对密钥进行信号，并使用进一步的事件重新排队。
通过调用密钥的取消方法取消与监视服务的注册。在取消密钥时排队的密钥将保留在队列中，直到检索到它为止。根据对象的不同，可以自动取消键。例如，假设一个目录被监视，而监视服务检测到它已被删除，或者它的文件系统不再被访问。当以这种方式取消密钥时，它将被发送信号并排队(如果当前没有发送信号的话)。为了确保通知使用者，重置方法的返回值指示密钥是否有效。
监视服务对于多个并发使用者来说是安全的。要确保在任何时候只有一个使用者处理特定对象的事件，则应注意确保只有在处理了密钥的事件之后才调用该密钥的重置方法。可以随时调用close方法来关闭服务，从而导致任何线程等待检索密钥，从而抛出ClosedWatchServiceException。
文件系统报告事件的速度可能比检索或处理事件的速度要快，实现可能会对它可能积累的事件数量施加一个未指定的限制。如果实现故意丢弃事件，那么它将为键的pollEvents方法安排返回带有溢出事件类型的元素。使用者可以将此事件用作触发器，以重新检查对象的状态。
当报告一个事件以指示监视目录中的文件已被修改时，则不能保证修改该文件的程序(或程序)已经完成。应该注意协调与可能正在更新文件的其他程序的访问。FileChannel类定义了用于锁定文件区域的方法，以防止其他程序访问该文件。
平台的依赖
观察来自文件系统的事件的实现旨在将事件直接映射到可用的本地文件事件通知设施，或者在本地设施不可用时使用原始机制(如轮询)。因此，关于如何检测事件、事件的及时性和它们的顺序是否被保留的许多细节都是高度特定于实现的。例如，当监视目录中的文件被修改时，它可能会在某些实现中导致单个ENTRY_MODIFY事件，而在其他实现中会导致多个事件。短期文件(指创建后被迅速删除的文件)可能不会被定期轮询文件系统以检测更改的原始实现检测到。
如果被监视的文件不在本地存储设备上，那么如果可以检测到文件的更改，那么它是特定于实现的。特别是，不需要检测对远程系统上执行的文件的更改。
 */

public interface WatchService
    extends Closeable
{

    /**
     * Closes this watch service.
     *
     * <p> If a thread is currently blocked in the {@link #take take} or {@link
     * #poll(long,TimeUnit) poll} methods waiting for a key to be queued then
     * it immediately receives a {@link ClosedWatchServiceException}. Any
     * valid keys associated with this watch service are {@link WatchKey#isValid
     * invalidated}.
     *
     * <p> After a watch service is closed, any further attempt to invoke
     * operations upon it will throw {@link ClosedWatchServiceException}.
     * If this watch service is already closed then invoking this method
     * has no effect.
     *
     * @throws  IOException
     *          if an I/O error occurs
     *          关闭这个看服务。
    如果一个线程当前被阻塞在take或poll方法中，等待一个密钥排队，那么它会立即收到一个ClosedWatchServiceException异常。与此监视服务相关的任何有效密钥都无效。
    在一个监视服务被关闭之后，任何进一步尝试调用它的操作都会抛出ClosedWatchServiceException。如果此监视服务已经关闭，则调用此方法无效。
     */
    @Override
    void close() throws IOException;

    /**
     * Retrieves and removes the next watch key, or {@code null} if none are
     * present.
     *
     * @return  the next watch key, or {@code null}
     *
     * @throws  ClosedWatchServiceException
     *          if this watch service is closed
     *          检索并删除下一个表键，如果没有表键，则为null。
     */
    WatchKey poll();

    /**
     * Retrieves and removes the next watch key, waiting if necessary up to the
     * specified wait time if none are yet present.
     *
     * @param   timeout
     *          how to wait before giving up, in units of unit
     * @param   unit
     *          a {@code TimeUnit} determining how to interpret the timeout
     *          parameter
     *
     * @return  the next watch key, or {@code null}
     *
     * @throws  ClosedWatchServiceException
     *          if this watch service is closed, or it is closed while waiting
     *          for the next key
     * @throws  InterruptedException
     *          if interrupted while waiting
     *          检索并删除下一个监视键，如果需要的话，等待到指定的等待时间，如果没有的话。
     */
    WatchKey poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * Retrieves and removes next watch key, waiting if none are yet present.
     *
     * @return  the next watch key
     *
     * @throws  ClosedWatchServiceException
     *          if this watch service is closed, or it is closed while waiting
     *          for the next key
     * @throws  InterruptedException
     *          if interrupted while waiting
     *          检索和移除下一个表键，等待是否还没有出现。
     */
    WatchKey take() throws InterruptedException;
}
