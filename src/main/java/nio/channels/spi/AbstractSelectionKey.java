/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels.spi;

import java.nio.channels.*;


/**
 * Base implementation class for selection keys.
 *
 * <p> This class tracks the validity of the key and implements cancellation.
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 选择键的基实现类。
这个类跟踪键的有效性并实现取消。
 */

public abstract class AbstractSelectionKey
    extends SelectionKey
{

    /**
     * Initializes a new instance of this class.初始化该类的一个新实例。
     */
    protected AbstractSelectionKey() { }

    private volatile boolean valid = true;

    public final boolean isValid() {
        return valid;
    }

    void invalidate() {                                 // package-private
        valid = false;
    }

    /**
     * Cancels this key.
     *
     * <p> If this key has not yet been cancelled then it is added to its
     * selector's cancelled-key set while synchronized on that set.  </p>
     * 取消这个关键。
     如果这个键还没有被取消，那么它将被添加到它的选择器的cancelkey集中，同时在该集中进行同步。
     */
    public final void cancel() {
        // Synchronizing "this" to prevent this key from getting canceled 同步“this”以防止取消此键
        // multiple times by different threads, which might cause race 多次被不同的线程，这可能导致种族
        // condition between selector's select() and channel's close(). 选择器的select()和通道的close()之间的条件。
        synchronized (this) {
            if (valid) {
                valid = false;
                ((AbstractSelector)selector()).cancel(this);
            }
        }
    }
}
