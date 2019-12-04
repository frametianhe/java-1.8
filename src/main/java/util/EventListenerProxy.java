/*
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * An abstract wrapper class for an {@code EventListener} class
 * which associates a set of additional parameters with the listener.
 * Subclasses must provide the storage and accessor methods
 * for the additional arguments or parameters.
 * <p>
 * For example, a bean which supports named properties
 * would have a two argument method signature for adding
 * a {@code PropertyChangeListener} for a property:
 * <pre>
 * public void addPropertyChangeListener(String propertyName,
 *                                       PropertyChangeListener listener)
 * </pre>
 * If the bean also implemented the zero argument get listener method:
 * <pre>
 * public PropertyChangeListener[] getPropertyChangeListeners()
 * </pre>
 * then the array may contain inner {@code PropertyChangeListeners}
 * which are also {@code PropertyChangeListenerProxy} objects.
 * <p>
 * If the calling method is interested in retrieving the named property
 * then it would have to test the element to see if it is a proxy class.
 *
 * @since 1.4
 * EventListener类的一个抽象包装类，它将一组附加参数与侦听器相关联。子类必须为额外的参数或参数提供存储和访问方法。
例如，一个支持命名属性的bean将有两个参数方法签名，用于为属性添加PropertyChangeListener:
如果bean也实现了zero参数get listener方法:
公共PropertyChangeListener[]getPropertyChangeListeners()

然后数组可能包含内部PropertyChangeListeners，它也是PropertyChangeListenerProxy对象。
如果调用方法有兴趣检索命名的属性，那么它必须测试该元素，看看它是否是一个代理类。
 */
public abstract class EventListenerProxy<T extends EventListener>
        implements EventListener {

    private final T listener;

    /**
     * Creates a proxy for the specified listener.
     *
     * @param listener  the listener object
     */
    public EventListenerProxy(T listener) {
        this.listener = listener;
    }

    /**
     * Returns the listener associated with the proxy.
     *
     * @return  the listener associated with the proxy
     */
    public T getListener() {
        return this.listener;
    }
}
