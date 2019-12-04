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

package java.nio;

import java.io.FileDescriptor;
import sun.misc.Unsafe;


/**
 * A direct byte buffer whose content is a memory-mapped region of a file.
 *
 * <p> Mapped byte buffers are created via the {@link
 * java.nio.channels.FileChannel#map FileChannel.map} method.  This class
 * extends the {@link ByteBuffer} class with operations that are specific to
 * memory-mapped file regions.
 *
 * <p> A mapped byte buffer and the file mapping that it represents remain
 * valid until the buffer itself is garbage-collected.
 *
 * <p> The content of a mapped byte buffer can change at any time, for example
 * if the content of the corresponding region of the mapped file is changed by
 * this program or another.  Whether or not such changes occur, and when they
 * occur, is operating-system dependent and therefore unspecified.
 *
 * <a name="inaccess"></a><p> All or part of a mapped byte buffer may become
 * inaccessible at any time, for example if the mapped file is truncated.  An
 * attempt to access an inaccessible region of a mapped byte buffer will not
 * change the buffer's content and will cause an unspecified exception to be
 * thrown either at the time of the access or at some later time.  It is
 * therefore strongly recommended that appropriate precautions be taken to
 * avoid the manipulation of a mapped file by this program, or by a
 * concurrently running program, except to read or write the file's content.
 *
 * <p> Mapped byte buffers otherwise behave no differently than ordinary direct
 * byte buffers. </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 一种直接字节缓冲区，其内容是文件的内存映射区域。
映射字节缓冲区是通过FileChannel创建的。地图的方法。这个类使用特定于内存映射文件区域的操作扩展ByteBuffer类。
映射字节缓冲区及其表示的文件映射在缓冲区本身被垃圾收集之前仍然有效。
映射字节缓冲区的内容可以随时更改，例如，如果映射文件的相应区域的内容被这个程序或另一个程序更改的话。这种变化是否发生，何时发生，取决于操作系统，因此不确定。
映射字节缓冲区的所有或部分在任何时候都可能无法访问，例如，如果映射文件被截断。试图访问映射字节缓冲区的不可访问区域不会更改缓冲区的内容，并将导致在访问时或稍后的某个时候抛出未指定的异常。因此，强烈建议采取适当的预防措施，以避免该程序或并发运行的程序对映射文件的操作，除非读取或写入文件的内容。
映射的字节缓冲区与普通的直接字节缓冲区没有区别。
 */

public abstract class MappedByteBuffer
    extends ByteBuffer
{

    // This is a little bit backwards: By rights MappedByteBuffer should be a 这有点倒退:MappedByteBuffer应该是a
    // subclass of DirectByteBuffer, but to keep the spec clear and simple, and DirectByteBuffer的子类，但是为了保持规范清晰和简单，并且
    // for optimization purposes, it's easier to do it the other way around. 出于优化的目的，用另一种方式来做会更容易。
    // This works because DirectByteBuffer is a package-private class. 这是因为DirectByteBuffer是一个包私有类。

    // For mapped buffers, a FileDescriptor that may be used for mapping对于映射缓冲区，可以用于映射的文件描述符。
    // operations if valid; null if the buffer is not mapped.操作如果有效;如果缓冲区没有映射，则为空。
    private final FileDescriptor fd;

    // This should only be invoked by the DirectByteBuffer constructors这应该只由DirectByteBuffer构造函数调用
    //
    MappedByteBuffer(int mark, int pos, int lim, int cap, // package-private
                     FileDescriptor fd)
    {
        super(mark, pos, lim, cap);
        this.fd = fd;
    }

    MappedByteBuffer(int mark, int pos, int lim, int cap) { // package-private
        super(mark, pos, lim, cap);
        this.fd = null;
    }

    private void checkMapped() {
        if (fd == null)
            // Can only happen if a luser explicitly casts a direct byte buffer只有当luser显式地抛出一个直接字节缓冲区时，才会发生这种情况
            throw new UnsupportedOperationException();
    }

    // Returns the distance (in bytes) of the buffer from the page aligned address返回缓冲区与页面对齐地址之间的距离(以字节为单位)
    // of the mapping. Computed each time to avoid storing in every direct buffer.的映射。每次计算以避免在每个直接缓冲区中存储。
    private long mappingOffset() {
        int ps = Bits.pageSize();
        long offset = address % ps;
        return (offset >= 0) ? offset : (ps + offset);
    }

    private long mappingAddress(long mappingOffset) {
        return address - mappingOffset;
    }

    private long mappingLength(long mappingOffset) {
        return (long)capacity() + mappingOffset;
    }

    /**
     * Tells whether or not this buffer's content is resident in physical
     * memory.
     *
     * <p> A return value of <tt>true</tt> implies that it is highly likely
     * that all of the data in this buffer is resident in physical memory and
     * may therefore be accessed without incurring any virtual-memory page
     * faults or I/O operations.  A return value of <tt>false</tt> does not
     * necessarily imply that the buffer's content is not resident in physical
     * memory.
     *
     * <p> The returned value is a hint, rather than a guarantee, because the
     * underlying operating system may have paged out some of the buffer's data
     * by the time that an invocation of this method returns.  </p>
     *
     * @return  <tt>true</tt> if it is likely that this buffer's content
     *          is resident in physical memory
     *          告诉缓冲区的内容是否驻留在物理内存中。
    返回值为true意味着这个缓冲区中的所有数据很可能都驻留在物理内存中，因此可以访问这些数据，而不会导致任何虚拟内存页错误或I/O操作。false的返回值并不一定意味着缓冲区的内容不驻留在物理内存中。
    返回的值是一个提示，而不是保证，因为底层操作系统可能在该方法的调用返回时已经调出了一些缓冲区的数据。
     */
    public final boolean isLoaded() {
        checkMapped();
        if ((address == 0) || (capacity() == 0))
            return true;
        long offset = mappingOffset();
        long length = mappingLength(offset);
        return isLoaded0(mappingAddress(offset), length, Bits.pageCount(length));
    }

    // not used, but a potential target for a store, see load() for details.不使用，但是作为商店的潜在目标，请参阅load()了解详细信息。
    private static byte unused;

    /**
     * Loads this buffer's content into physical memory.
     *
     * <p> This method makes a best effort to ensure that, when it returns,
     * this buffer's content is resident in physical memory.  Invoking this
     * method may cause some number of page faults and I/O operations to
     * occur. </p>
     *
     * @return  This buffer
     * 将缓冲区的内容加载到物理内存中。
    此方法尽力确保在返回时，缓冲区的内容驻留在物理内存中。调用此方法可能会导致一些页面错误和I/O操作。
     */
    public final MappedByteBuffer load() {
        checkMapped();
        if ((address == 0) || (capacity() == 0))
            return this;
        long offset = mappingOffset();
        long length = mappingLength(offset);
        load0(mappingAddress(offset), length);

        // Read a byte from each page to bring it into memory. A checksum 从每一页中读取一个字节，将其存入内存。一个校验和
        // is computed as we go along to prevent the compiler from otherwise 是在我们继续进行时计算的，以防止编译器出错吗
        // considering the loop as dead code. 将循环视为死代码。
        Unsafe unsafe = Unsafe.getUnsafe();
        int ps = Bits.pageSize();
        int count = Bits.pageCount(length);
        long a = mappingAddress(offset);
        byte x = 0;
        for (int i=0; i<count; i++) {
            x ^= unsafe.getByte(a);
            a += ps;
        }
        if (unused != 0)
            unused = x;

        return this;
    }

    /**
     * Forces any changes made to this buffer's content to be written to the
     * storage device containing the mapped file.
     *
     * <p> If the file mapped into this buffer resides on a local storage
     * device then when this method returns it is guaranteed that all changes
     * made to the buffer since it was created, or since this method was last
     * invoked, will have been written to that device.
     *
     * <p> If the file does not reside on a local device then no such guarantee
     * is made.
     *
     * <p> If this buffer was not mapped in read/write mode ({@link
     * java.nio.channels.FileChannel.MapMode#READ_WRITE}) then invoking this
     * method has no effect. </p>
     *
     * @return  This buffer
     * 将对该缓冲区内容的任何更改强制写入包含映射文件的存储设备。
    如果映射到这个缓冲区的文件驻留在本地存储设备上，那么当该方法返回时，它将保证自创建以来对缓冲区所做的所有更改，或者自上次调用该方法以来，将写入该设备。
    如果该文件不驻留在本地设备上，则不会作出这样的保证。
    如果这个缓冲区没有被映射到读/写模式(java.nio.channels.FileChannel.MapMode.READ_WRITE)，那么调用这个方法没有任何效果。
     */
    public final MappedByteBuffer force() {
        checkMapped();
        if ((address != 0) && (capacity() != 0)) {
            long offset = mappingOffset();
            force0(fd, mappingAddress(offset), mappingLength(offset));
        }
        return this;
    }

    private native boolean isLoaded0(long address, long length, int pageCount);
    private native void load0(long address, long length);
    private native void force0(FileDescriptor fd, long address, long length);
}
