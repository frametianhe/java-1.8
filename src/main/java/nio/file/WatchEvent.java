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

/**
 * An event or a repeated event for an object that is registered with a {@link
 * WatchService}.
 *
 * <p> An event is classified by its {@link #kind() kind} and has a {@link
 * #count() count} to indicate the number of times that the event has been
 * observed. This allows for efficient representation of repeated events. The
 * {@link #context() context} method returns any context associated with
 * the event. In the case of a repeated event then the context is the same for
 * all events.
 *
 * <p> Watch events are immutable and safe for use by multiple concurrent
 * threads.
 *
 * @param   <T>     The type of the context object associated with the event
 *
 * @since 1.7
 * 在WatchService中注册的对象的事件或重复事件。
事件按其类型进行分类，并有一个计数来指示事件被观察的次数。这允许有效地表示重复的事件。上下文方法返回与事件关联的任何上下文。在重复事件的情况下，所有事件的上下文都是相同的。
监视事件是不可变的，对于多个并发线程来说是安全的。
 */

public interface WatchEvent<T> {

    /**
     * An event kind, for the purposes of identification.一种事件类型，用于标识的目的。
     *
     * @since 1.7
     * @see StandardWatchEventKinds
     */
    public static interface Kind<T> {
        /**
         * Returns the name of the event kind.
         *
         * @return the name of the event kind
         */
        String name();

        /**
         * Returns the type of the {@link WatchEvent#context context} value.
         *
         *
         * @return the type of the context value
         */
        Class<T> type();
    }

    /**
     * An event modifier that qualifies how a {@link Watchable} is registered
     * with a {@link WatchService}.
     *
     * <p> This release does not define any <em>standard</em> modifiers.
     *
     * @since 1.7
     * @see Watchable#register
     * 事件修饰符，限定可监视对象如何在WatchService中注册。
    这个版本没有定义任何标准的修饰符。
     */
    public static interface Modifier {
        /**
         * Returns the name of the modifier.
         *
         * @return the name of the modifier
         */
        String name();
    }

    /**
     * Returns the event kind.
     *
     * @return  the event kind
     */
    Kind<T> kind();

    /**
     * Returns the event count. If the event count is greater than {@code 1}
     * then this is a repeated event.
     *
     * @return  the event count
     * 返回事件计数。如果事件计数大于1，则这是一个重复事件。
     */
    int count();

    /**
     * Returns the context for the event.
     *
     * <p> In the case of {@link StandardWatchEventKinds#ENTRY_CREATE ENTRY_CREATE},
     * {@link StandardWatchEventKinds#ENTRY_DELETE ENTRY_DELETE}, and {@link
     * StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} events the context is
     * a {@code Path} that is the {@link Path#relativize relative} path between
     * the directory registered with the watch service, and the entry that is
     * created, deleted, or modified.
     *
     * @return  the event context; may be {@code null}
     * 返回事件的上下文。
    对于ENTRY_CREATE、ENTRY_DELETE和ENTRY_MODIFY事件，上下文是在使用watch服务注册的目录和创建、删除或修改的条目之间的相对路径。
     */
    T context();
}
