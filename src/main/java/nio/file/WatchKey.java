/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;

/**
 * A token representing the registration of a {@link Watchable watchable} object
 * with a {@link WatchService}.
 *
 * <p> A watch key is created when a watchable object is registered with a watch
 * service. The key remains {@link #isValid valid} until:
 * <ol>
 *   <li> It is cancelled, explicitly, by invoking its {@link #cancel cancel}
 *     method, or</li>
 *   <li> Cancelled implicitly, because the object is no longer accessible,
 *     or </li>
 *   <li> By {@link WatchService#close closing} the watch service. </li>
 * </ol>
 *
 * <p> A watch key has a state. When initially created the key is said to be
 * <em>ready</em>. When an event is detected then the key is <em>signalled</em>
 * and queued so that it can be retrieved by invoking the watch service's {@link
 * WatchService#poll() poll} or {@link WatchService#take() take} methods. Once
 * signalled, a key remains in this state until its {@link #reset reset} method
 * is invoked to return the key to the ready state. Events detected while the
 * key is in the signalled state are queued but do not cause the key to be
 * re-queued for retrieval from the watch service. Events are retrieved by
 * invoking the key's {@link #pollEvents pollEvents} method. This method
 * retrieves and removes all events accumulated for the object. When initially
 * created, a watch key has no pending events. Typically events are retrieved
 * when the key is in the signalled state leading to the following idiom:
 *
 * <pre>
 *     for (;;) {
 *         // retrieve key
 *         WatchKey key = watcher.take();
 *
 *         // process events
 *         for (WatchEvent&lt;?&gt; event: key.pollEvents()) {
 *             :
 *         }
 *
 *         // reset the key
 *         boolean valid = key.reset();
 *         if (!valid) {
 *             // object no longer registered
 *         }
 *     }
 * </pre>
 *
 * <p> Watch keys are safe for use by multiple concurrent threads. Where there
 * are several threads retrieving signalled keys from a watch service then care
 * should be taken to ensure that the {@code reset} method is only invoked after
 * the events for the object have been processed. This ensures that one thread
 * is processing the events for an object at any time.
 *
 * @since 1.7
 * 表示可监视对象与WatchService的注册的令牌。
当可监视对象与监视服务注册时，将创建一个监视键。钥匙仍然有效，直至:
通过调用其cancel方法，显式地取消它
隐式取消，因为对象不再可访问，或
通过关闭手表服务。
表键有一个状态。当最初创建密钥时，据说是准备就绪。当检测到一个事件时，密钥会被发送信号并排队，以便通过调用监视服务的轮询或获取方法来检索它。一旦发出信号，一个键将保持在这个状态，直到调用它的reset方法将键返回到就绪状态。当密钥处于信号状态时检测到的事件将被排队，但不会导致从监视服务检索密钥重新排队。通过调用密钥的pollEvents方法检索事件。此方法检索并删除对象积累的所有事件。最初创建时，watch键没有等待事件。通常情况下，当键处于信号状态，导致以下习语:
监视键可以安全地用于多个并发线程。如果有几个线程从表服务中检索信号键，则应该注意确保在处理对象事件之后才调用reset方法。这确保一个线程在任何时候都在处理对象的事件。
 */

public interface WatchKey {

    /**
     * Tells whether or not this watch key is valid.
     *
     * <p> A watch key is valid upon creation and remains until it is cancelled,
     * or its watch service is closed.
     *
     * @return  {@code true} if, and only if, this watch key is valid
     * 告诉此表键是否有效。
    一个表键在创建时是有效的，直到它被取消，或者它的表服务被关闭为止。
     */
    boolean isValid();

    /**
     * Retrieves and removes all pending events for this watch key, returning
     * a {@code List} of the events that were retrieved.
     *
     * <p> Note that this method does not wait if there are no events pending.
     *
     * @return  the list of the events retrieved; may be empty
     * 检索并删除此表键的所有挂起事件，返回检索到的事件的列表。
    注意，如果没有事件等待，此方法不会等待。
     */
    List<WatchEvent<?>> pollEvents();

    /**
     * Resets this watch key.
     *
     * <p> If this watch key has been cancelled or this watch key is already in
     * the ready state then invoking this method has no effect. Otherwise
     * if there are pending events for the object then this watch key is
     * immediately re-queued to the watch service. If there are no pending
     * events then the watch key is put into the ready state and will remain in
     * that state until an event is detected or the watch key is cancelled.
     *
     * @return  {@code true} if the watch key is valid and has been reset, and
     *          {@code false} if the watch key could not be reset because it is
     *          no longer {@link #isValid valid}
     *          重置这块手表的关键。
    如果这个监视键已经被取消，或者这个监视键已经处于就绪状态，那么调用这个方法没有任何效果。否则，如果对象有等待事件，则此监视键将立即重新排队到监视服务。如果没有未处理的事件，则将手表键放入就绪状态，并将保持该状态，直到检测到事件或手表键被取消。
     */
    boolean reset();

    /**
     * Cancels the registration with the watch service. Upon return the watch key
     * will be invalid. If the watch key is enqueued, waiting to be retrieved
     * from the watch service, then it will remain in the queue until it is
     * removed. Pending events, if any, remain pending and may be retrieved by
     * invoking the {@link #pollEvents pollEvents} method after the key is
     * cancelled.
     *
     * <p> If this watch key has already been cancelled then invoking this
     * method has no effect.  Once cancelled, a watch key remains forever invalid.
     * 取消与手表服务的注册。返回时，手表钥匙将无效。如果监视键被编入队列，等待从监视服务检索，那么它将一直保留在队列中，直到被删除为止。挂起事件(如果有的话)仍然是挂起的，可以通过在取消密钥之后调用pollEvents方法来获取。
     如果此监视键已被取消，则调用此方法无效。一旦取消，手表钥匙将永远无效。
     */
    void cancel();

    /**
     * Returns the object for which this watch key was created. This method will
     * continue to return the object even after the key is cancelled.
     *
     * <p> As the {@code WatchService} is intended to map directly on to the
     * native file event notification facility (where available) then many of
     * details on how registered objects are watched is highly implementation
     * specific. When watching a directory for changes for example, and the
     * directory is moved or renamed in the file system, there is no guarantee
     * that the watch key will be cancelled and so the object returned by this
     * method may no longer be a valid path to the directory.
     *
     * @return the object for which this watch key was created
     * 返回创建此监视键的对象。即使在取消了密钥之后，该方法仍将返回对象。
    由于WatchService打算直接映射到本地文件事件通知功能(如果有的话)，那么关于如何监视已注册对象的许多细节都是高度特定于实现的。例如，当查看目录以查找更改时，并且该目录在文件系统中被移动或重命名时，不能保证监视键将被取消，因此该方法返回的对象可能不再是该目录的有效路径。
     */
    Watchable watchable();
}
