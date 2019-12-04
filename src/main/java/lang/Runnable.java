/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * The <code>Runnable</code> interface should be implemented by any
 * class whose instances are intended to be executed by a thread. The
 * class must define a method of no arguments called <code>run</code>.
 * <p>
 * This interface is designed to provide a common protocol for objects that
 * wish to execute code while they are active. For example,
 * <code>Runnable</code> is implemented by class <code>Thread</code>.
 * Being active simply means that a thread has been started and has not
 * yet been stopped.
 * <p>
 * In addition, <code>Runnable</code> provides the means for a class to be
 * active while not subclassing <code>Thread</code>. A class that implements
 * <code>Runnable</code> can run without subclassing <code>Thread</code>
 * by instantiating a <code>Thread</code> instance and passing itself in
 * as the target.  In most cases, the <code>Runnable</code> interface should
 * be used if you are only planning to override the <code>run()</code>
 * method and no other <code>Thread</code> methods.
 * This is important because classes should not be subclassed
 * unless the programmer intends on modifying or enhancing the fundamental
 * behavior of the class.
 *
 * @author  Arthur van Hoff
 * @see     java.lang.Thread
 * @see     java.util.concurrent.Callable
 * @since   JDK1.0
 *
可运行的接口应该由任何一个要被线程执行的类来实现。这个类必须定义一个不被称为run的参数的方法。
这个接口的设计目的是为希望在活动时执行代码的对象提供一个通用的协议。例如，Runnable由类线程实现。活动仅仅意味着线程已经启动，并且还没有停止。
此外，Runnable还提供了一种方法，用于在不子类化线程的情况下进行活动。一个实现Runnable的类可以通过实例化一个线程实例并将自己作为目标传递，而无需子类化线程。在大多数情况下，如果您只打算重写run()方法，而没有其他线程方法，则应该使用Runnable接口。这很重要，因为类不应该被子类化，除非程序员打算修改或增强类的基本行为。
 */
@FunctionalInterface
public interface Runnable {
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     * 当一个对象实现接口Runnable用于创建线程时，启动线程会导致该对象的运行方法在单独执行的线程中被调用。
     该方法的一般契约是，它可以采取任何行动。
     *
     * @see     java.lang.Thread#run()
     */
    public abstract void run();
}
