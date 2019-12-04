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

import java.io.IOException;

/**
 * An object that may be registered with a watch service so that it can be
 * <em>watched</em> for changes and events.
 *
 * <p> This interface defines the {@link #register register} method to register
 * the object with a {@link WatchService} returning a {@link WatchKey} to
 * represent the registration. An object may be registered with more than one
 * watch service. Registration with a watch service is cancelled by invoking the
 * key's {@link WatchKey#cancel cancel} method.
 *
 * @since 1.7
 *
 * @see Path#register
 * 可以向监视服务注册的对象，以便监视它以查看更改和事件。
这个接口定义了注册方法，该方法将对象注册到一个WatchService，该WatchService返回一个WatchKey来表示注册。对象可以注册到多个监视服务。通过调用密钥的取消方法取消与监视服务的注册。
 */

public interface Watchable {

    /**
     * Registers an object with a watch service.
     *
     * <p> If the file system object identified by this object is currently
     * registered with the watch service then the watch key, representing that
     * registration, is returned after changing the event set or modifiers to
     * those specified by the {@code events} and {@code modifiers} parameters.
     * Changing the event set does not cause pending events for the object to be
     * discarded. Objects are automatically registered for the {@link
     * StandardWatchEventKinds#OVERFLOW OVERFLOW} event. This event is not
     * required to be present in the array of events.
     *
     * <p> Otherwise the file system object has not yet been registered with the
     * given watch service, so it is registered and the resulting new key is
     * returned.
     *
     * <p> Implementations of this interface should specify the events they
     * support.
     *
     * @param   watcher
     *          the watch service to which this object is to be registered
     * @param   events
     *          the events for which this object should be registered
     * @param   modifiers
     *          the modifiers, if any, that modify how the object is registered
     *
     * @return  a key representing the registration of this object with the
     *          given watch service
     *
     * @throws  UnsupportedOperationException
     *          if unsupported events or modifiers are specified
     * @throws  IllegalArgumentException
     *          if an invalid of combination of events are modifiers are specified
     * @throws  ClosedWatchServiceException
     *          if the watch service is closed
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission required to monitor this object. Implementations of
     *          this interface should specify the permission checks.
     *          用监视服务注册对象。
    如果该对象标识的文件系统对象当前已被监视服务注册，那么在将事件集或修饰符更改为事件和修饰符参数指定的内容之后，将返回表示该注册的监视键。更改事件集不会导致对象被丢弃的挂起事件。对象将自动注册为溢出事件。这个事件不需要出现在事件数组中。
    否则，文件系统对象还没有在给定的监视服务中注册，因此它被注册并返回生成的新键。
    这个接口的实现应该指定它们支持的事件。
     */
    WatchKey register(WatchService watcher,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
        throws IOException;


    /**
     * Registers an object with a watch service.
     *
     * <p> An invocation of this method behaves in exactly the same way as the
     * invocation
     * <pre>
     *     watchable.{@link #register(WatchService,WatchEvent.Kind[],WatchEvent.Modifier[]) register}(watcher, events, new WatchEvent.Modifier[0]);
     * </pre>
     *
     * @param   watcher
     *          the watch service to which this object is to be registered
     * @param   events
     *          the events for which this object should be registered
     *
     * @return  a key representing the registration of this object with the
     *          given watch service
     *
     * @throws  UnsupportedOperationException
     *          if unsupported events are specified
     * @throws  IllegalArgumentException
     *          if an invalid of combination of events are specified
     * @throws  ClosedWatchServiceException
     *          if the watch service is closed
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission required to monitor this object. Implementations of
     *          this interface should specify the permission checks.
     *          用监视服务注册对象。
    此方法的调用行为与调用完全相同
    值得一看的。注册(观察者、事件、新WatchEvent.Modifier[0]);
     */
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
        throws IOException;
}
