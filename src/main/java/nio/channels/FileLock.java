/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.IOException;

/**
 * A token representing a lock on a region of a file.
 *
 * <p> A file-lock object is created each time a lock is acquired on a file via
 * one of the {@link FileChannel#lock(long,long,boolean) lock} or {@link
 * FileChannel#tryLock(long,long,boolean) tryLock} methods of the
 * {@link FileChannel} class, or the {@link
 * AsynchronousFileChannel#lock(long,long,boolean,Object,CompletionHandler) lock}
 * or {@link AsynchronousFileChannel#tryLock(long,long,boolean) tryLock}
 * methods of the {@link AsynchronousFileChannel} class.
 *
 * <p> A file-lock object is initially valid.  It remains valid until the lock
 * is released by invoking the {@link #release release} method, by closing the
 * channel that was used to acquire it, or by the termination of the Java
 * virtual machine, whichever comes first.  The validity of a lock may be
 * tested by invoking its {@link #isValid isValid} method.
 *
 * <p> A file lock is either <i>exclusive</i> or <i>shared</i>.  A shared lock
 * prevents other concurrently-running programs from acquiring an overlapping
 * exclusive lock, but does allow them to acquire overlapping shared locks.  An
 * exclusive lock prevents other programs from acquiring an overlapping lock of
 * either type.  Once it is released, a lock has no further effect on the locks
 * that may be acquired by other programs.
 *
 * <p> Whether a lock is exclusive or shared may be determined by invoking its
 * {@link #isShared isShared} method.  Some platforms do not support shared
 * locks, in which case a request for a shared lock is automatically converted
 * into a request for an exclusive lock.
 *
 * <p> The locks held on a particular file by a single Java virtual machine do
 * not overlap.  The {@link #overlaps overlaps} method may be used to test
 * whether a candidate lock range overlaps an existing lock.
 *
 * <p> A file-lock object records the file channel upon whose file the lock is
 * held, the type and validity of the lock, and the position and size of the
 * locked region.  Only the validity of a lock is subject to change over time;
 * all other aspects of a lock's state are immutable.
 *
 * <p> File locks are held on behalf of the entire Java virtual machine.
 * They are not suitable for controlling access to a file by multiple
 * threads within the same virtual machine.
 *
 * <p> File-lock objects are safe for use by multiple concurrent threads.
 *
 *
 * <a name="pdep"></a><h2> Platform dependencies </h2>
 *
 * <p> This file-locking API is intended to map directly to the native locking
 * facility of the underlying operating system.  Thus the locks held on a file
 * should be visible to all programs that have access to the file, regardless
 * of the language in which those programs are written.
 *
 * <p> Whether or not a lock actually prevents another program from accessing
 * the content of the locked region is system-dependent and therefore
 * unspecified.  The native file-locking facilities of some systems are merely
 * <i>advisory</i>, meaning that programs must cooperatively observe a known
 * locking protocol in order to guarantee data integrity.  On other systems
 * native file locks are <i>mandatory</i>, meaning that if one program locks a
 * region of a file then other programs are actually prevented from accessing
 * that region in a way that would violate the lock.  On yet other systems,
 * whether native file locks are advisory or mandatory is configurable on a
 * per-file basis.  To ensure consistent and correct behavior across platforms,
 * it is strongly recommended that the locks provided by this API be used as if
 * they were advisory locks.
 *
 * <p> On some systems, acquiring a mandatory lock on a region of a file
 * prevents that region from being {@link java.nio.channels.FileChannel#map
 * <i>mapped into memory</i>}, and vice versa.  Programs that combine
 * locking and mapping should be prepared for this combination to fail.
 *
 * <p> On some systems, closing a channel releases all locks held by the Java
 * virtual machine on the underlying file regardless of whether the locks were
 * acquired via that channel or via another channel open on the same file.  It
 * is strongly recommended that, within a program, a unique channel be used to
 * acquire all locks on any given file.
 *
 * <p> Some network filesystems permit file locking to be used with
 * memory-mapped files only when the locked regions are page-aligned and a
 * whole multiple of the underlying hardware's page size.  Some network
 * filesystems do not implement file locks on regions that extend past a
 * certain position, often 2<sup>30</sup> or 2<sup>31</sup>.  In general, great
 * care should be taken when locking files that reside on network filesystems.
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
表示文件区域上的锁的令牌。
文件锁对象是在每次通过FileChannel类的锁或tryLock方法或异步FileChannel类的锁或tryLock方法获取文件锁时创建的。
文件锁定对象最初是有效的。它一直有效，直到通过调用发布方法释放锁、通过关闭用于获取锁的通道、或者通过Java虚拟机的终止释放锁为止，无论哪个是先释放的。可以通过调用锁的isValid方法来测试锁的有效性。
文件锁定要么是独占的，要么是共享的。共享锁可以防止其他同步运行的程序获得重叠的独占锁，但是允许它们获得重叠的共享锁。独占锁可以防止其他程序获得任何类型的重叠锁。一旦被释放，锁对其他程序可能获得的锁没有进一步的影响。
无论锁是独占的还是共享的，都可以通过调用它的isShared方法来确定。有些平台不支持共享锁，在这种情况下，对共享锁的请求会自动转换为对独占锁的请求。
单个Java虚拟机保存在特定文件上的锁不会重叠。重叠方法可用于测试候选锁范围是否与现有锁重叠。
文件锁定对象记录持有锁的文件的文件通道、锁的类型和有效性、锁区域的位置和大小。只有锁的有效性会随时间而改变;锁状态的所有其他方面都是不可变的。
文件锁定代表整个Java虚拟机。它们不适用于控制同一虚拟机中的多个线程对文件的访问。
文件锁定对象对于多个并发线程来说是安全的。
平台的依赖
这个文件锁定API打算直接映射到底层操作系统的本机锁定功能。因此，文件上的锁应该对所有能够访问文件的程序可见，而不管这些程序是用什么语言编写的。
锁是否实际上阻止另一个程序访问被锁区域的内容是与系统相关的，因此是不指定的。一些系统的本机文件锁定功能只是咨询，这意味着程序必须合作遵守已知的锁定协议，以保证数据的完整性。在其他系统上，本机文件锁是强制性的，这意味着如果一个程序锁住了文件的一个区域，那么其他程序实际上被阻止访问该区域，这种方式会违反锁。在其他系统中，无论本机文件锁是通知锁还是强制锁，都可以在每个文件的基础上进行配置。为了确保跨平台的一致和正确行为，强烈建议将此API提供的锁用作咨询锁。
在某些系统中，获取文件区域上的强制锁将阻止该区域映射到内存中，反之亦然。将锁和映射结合在一起的程序应该为这种组合失败做好准备。
在某些系统中，关闭通道会释放底层文件上Java虚拟机持有的所有锁，而不管这些锁是通过该通道获得的，还是通过同一文件上打开的另一个通道获得的。强烈建议在一个程序中使用一个唯一的通道来获取任何给定文件上的所有锁。
有些网络文件系统只允许将文件锁定与内存映射文件一起使用，只有在锁定区域与页面对齐，并且基础硬件的页面大小的整个倍数时才允许使用。有些网络文件系统不会在扩展到某个位置(通常是230或231)的区域上实现文件锁。通常，在锁定驻留在网络文件系统上的文件时应该非常小心。
 */

public abstract class FileLock implements AutoCloseable {

    private final Channel channel;
    private final long position;
    private final long size;
    private final boolean shared;

    /**
     * Initializes a new instance of this class.
     *
     * @param  channel
     *         The file channel upon whose file this lock is held
     *
     * @param  position
     *         The position within the file at which the locked region starts;
     *         must be non-negative
     *
     * @param  size
     *         The size of the locked region; must be non-negative, and the sum
     *         <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> must be non-negative
     *
     * @param  shared
     *         <tt>true</tt> if this lock is shared,
     *         <tt>false</tt> if it is exclusive
     *
     * @throws IllegalArgumentException
     *         If the preconditions on the parameters do not hold
     */
    protected FileLock(FileChannel channel,
                       long position, long size, boolean shared)
    {
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (size < 0)
            throw new IllegalArgumentException("Negative size");
        if (position + size < 0)
            throw new IllegalArgumentException("Negative position + size");
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    /**
     * Initializes a new instance of this class.
     *
     * @param  channel
     *         The channel upon whose file this lock is held
     *
     * @param  position
     *         The position within the file at which the locked region starts;
     *         must be non-negative
     *
     * @param  size
     *         The size of the locked region; must be non-negative, and the sum
     *         <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> must be non-negative
     *
     * @param  shared
     *         <tt>true</tt> if this lock is shared,
     *         <tt>false</tt> if it is exclusive
     *
     * @throws IllegalArgumentException
     *         If the preconditions on the parameters do not hold
     *
     * @since 1.7
     */
    protected FileLock(AsynchronousFileChannel channel,
                       long position, long size, boolean shared)
    {
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (size < 0)
            throw new IllegalArgumentException("Negative size");
        if (position + size < 0)
            throw new IllegalArgumentException("Negative position + size");
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    /**
     * Returns the file channel upon whose file this lock was acquired.
     *
     * <p> This method has been superseded by the {@link #acquiredBy acquiredBy}
     * method.
     *
     * @return  The file channel, or {@code null} if the file lock was not
     *          acquired by a file channel.
     *          返回获取此锁的文件通道。
    这一方法已被默许的方法所取代。
     */
    public final FileChannel channel() {
        return (channel instanceof FileChannel) ? (FileChannel)channel : null;
    }

    /**
     * Returns the channel upon whose file this lock was acquired.返回获取此锁的文件的通道。
     *
     * @return  The channel upon whose file this lock was acquired.
     *
     * @since 1.7
     */
    public Channel acquiredBy() {
        return channel;
    }

    /**
     * Returns the position within the file of the first byte of the locked
     * region.
     *
     * <p> A locked region need not be contained within, or even overlap, the
     * actual underlying file, so the value returned by this method may exceed
     * the file's current size.  </p>
     *
     * @return  The position
     * 返回锁定区域的第一个字节的文件中的位置。
    锁住的区域不需要包含在实际的底层文件中，甚至不需要重叠，因此该方法返回的值可能会超过文件的当前大小。
     */
    public final long position() {
        return position;
    }

    /**
     * Returns the size of the locked region in bytes.
     *
     * <p> A locked region need not be contained within, or even overlap, the
     * actual underlying file, so the value returned by this method may exceed
     * the file's current size.  </p>
     *
     * @return  The size of the locked region
     * 返回以字节为单位的锁定区域的大小。
    锁住的区域不需要包含在实际的底层文件中，甚至不需要重叠，因此该方法返回的值可能会超过文件的当前大小。
     */
    public final long size() {
        return size;
    }

    /**
     * Tells whether this lock is shared.告诉此锁是否共享。
     *
     * @return <tt>true</tt> if lock is shared,
     *         <tt>false</tt> if it is exclusive
     */
    public final boolean isShared() {
        return shared;
    }

    /**
     * Tells whether or not this lock overlaps the given lock range.说明该锁是否与给定的锁定范围重叠。
     *
     * @param   position
     *          The starting position of the lock range
     * @param   size
     *          The size of the lock range
     *
     * @return  <tt>true</tt> if, and only if, this lock and the given lock
     *          range overlap by at least one byte
     */
    public final boolean overlaps(long position, long size) {
        if (position + size <= this.position)
            return false;               // That is below this
        if (this.position + this.size <= position)
            return false;               // This is below that
        return true;
    }

    /**
     * Tells whether or not this lock is valid.
     *
     * <p> A lock object remains valid until it is released or the associated
     * file channel is closed, whichever comes first.  </p>
     *
     * @return  <tt>true</tt> if, and only if, this lock is valid
     * 判断这个锁是否有效。
    锁对象在释放或关闭相关文件通道之前都是有效的，以先出现的为准。
     */
    public abstract boolean isValid();

    /**
     * Releases this lock.
     *
     * <p> If this lock object is valid then invoking this method releases the
     * lock and renders the object invalid.  If this lock object is invalid
     * then invoking this method has no effect.  </p>
     *
     * @throws  ClosedChannelException
     *          If the channel that was used to acquire this lock
     *          is no longer open
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          释放该锁。
    如果这个锁对象是有效的，那么调用此方法将释放锁并使对象无效。如果此锁对象无效，则调用此方法无效。
     */
    public abstract void release() throws IOException;

    /**
     * This method invokes the {@link #release} method. It was added
     * to the class so that it could be used in conjunction with the
     * automatic resource management block construct.
     *
     * @since 1.7
     * 此方法调用发布方法。它被添加到类中，以便可以与自动资源管理块构造一起使用。
     */
    public final void close() throws IOException {
        release();
    }

    /**
     * Returns a string describing the range, type, and validity of this lock.
     *
     * @return  A descriptive string
     */
    public final String toString() {
        return (this.getClass().getName()
                + "[" + position
                + ":" + size
                + " " + (shared ? "shared" : "exclusive")
                + " " + (isValid() ? "valid" : "invalid")
                + "]");
    }

}
