/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.ref;


/**
 * Phantom reference objects, which are enqueued after the collector
 * determines that their referents may otherwise be reclaimed.  Phantom
 * references are most often used for scheduling pre-mortem cleanup actions in
 * a more flexible way than is possible with the Java finalization mechanism.
 *
 * <p> If the garbage collector determines at a certain point in time that the
 * referent of a phantom reference is <a
 * href="package-summary.html#reachability">phantom reachable</a>, then at that
 * time or at some later time it will enqueue the reference.
 *
 * <p> In order to ensure that a reclaimable object remains so, the referent of
 * a phantom reference may not be retrieved: The <code>get</code> method of a
 * phantom reference always returns <code>null</code>.
 *
 * <p> Unlike soft and weak references, phantom references are not
 * automatically cleared by the garbage collector as they are enqueued.  An
 * object that is reachable via phantom references will remain so until all
 * such references are cleared or themselves become unreachable.
 *
 * @author   Mark Reinhold
 * @since    1.2
 * 幻像引用对象，在收集器确定它们的引用可能被回收之后将其加入队列。幻像引用通常用于以比Java终结机制更灵活的方式调度死前清理操作。
如果垃圾收集器在某个时间点确定了虚引用的引用是虚可访问的，那么在那个时间或稍后的某个时间，它将会对引用进行排队。
为了确保reclaimable对象保持原样，可能无法检索到phantom引用的引用:phantom引用的get方法总是返回null。
与软引用和弱引用不同，幻影引用在入列时不会被垃圾收集器自动清除。通过虚引用可以访问的对象将保持这样的状态，直到所有这些引用被清除或它们本身变得不可访问为止。
 */

public class PhantomReference<T> extends Reference<T> {

    /**
     * Returns this reference object's referent.  Because the referent of a
     * phantom reference is always inaccessible, this method always returns
     * <code>null</code>.返回这个引用对象的referent。因为幽灵引用的引用总是不可访问的，所以这个方法总是返回null。
     *
     * @return  <code>null</code>
     */
    public T get() {
        return null;
    }

    /**
     * Creates a new phantom reference that refers to the given object and
     * is registered with the given queue.
     *
     * <p> It is possible to create a phantom reference with a <tt>null</tt>
     * queue, but such a reference is completely useless: Its <tt>get</tt>
     * method will always return null and, since it does not have a queue, it
     * will never be enqueued.创建一个引用给定对象的新幻像引用，并在给定队列中注册。
     使用空队列创建幻像引用是可能的，但是这样的引用是完全无用的:它的get方法总是返回空，而且由于它没有队列，所以永远不会被加入队列。
     *
     * @param referent the object the new phantom reference will refer to
     * @param q the queue with which the reference is to be registered,
     *          or <tt>null</tt> if registration is not required
     */
    public PhantomReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
