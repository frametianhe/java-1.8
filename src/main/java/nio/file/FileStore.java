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

import java.nio.file.attribute.*;
import java.io.IOException;

/**
 * Storage for files. A {@code FileStore} represents a storage pool, device,
 * partition, volume, concrete file system or other implementation specific means
 * of file storage. The {@code FileStore} for where a file is stored is obtained
 * by invoking the {@link Files#getFileStore getFileStore} method, or all file
 * stores can be enumerated by invoking the {@link FileSystem#getFileStores
 * getFileStores} method.
 *
 * <p> In addition to the methods defined by this class, a file store may support
 * one or more {@link FileStoreAttributeView FileStoreAttributeView} classes
 * that provide a read-only or updatable view of a set of file store attributes.
 *
 * @since 1.7
 * 存储的文件。FileStore表示存储池、设备、分区、卷、具体文件系统或其他实现特定的文件存储方式。通过调用getFileStore方法来获得文件存储位置的FileStore，或者通过调用getFileStores方法来枚举所有的文件存储。
除了这个类定义的方法之外，文件存储库还可以支持一个或多个FileStoreAttributeView类，这些类为一组文件存储属性提供只读或可更新的视图。
 */

public abstract class FileStore {

    /**
     * Initializes a new instance of this class.
     */
    protected FileStore() {
    }

    /**
     * Returns the name of this file store. The format of the name is highly
     * implementation specific. It will typically be the name of the storage
     * pool or volume.
     *
     * <p> The string returned by this method may differ from the string
     * returned by the {@link Object#toString() toString} method.
     *
     * @return  the name of this file store
     * 返回此文件存储库的名称。名称的格式是高度实现的。它通常是存储池或卷的名称。
    此方法返回的字符串可能与toString方法返回的字符串不同。
     */
    public abstract String name();

    /**
     * Returns the <em>type</em> of this file store. The format of the string
     * returned by this method is highly implementation specific. It may
     * indicate, for example, the format used or if the file store is local
     * or remote.
     *
     * @return  a string representing the type of this file store
     * 返回此文件存储的类型。此方法返回的字符串的格式是高度实现的。例如，它可以指示所使用的格式，或者文件存储是本地的还是远程的。
     */
    public abstract String type();

    /**
     * Tells whether this file store is read-only. A file store is read-only if
     * it does not support write operations or other changes to files. Any
     * attempt to create a file, open an existing file for writing etc. causes
     * an {@code IOException} to be thrown.
     *
     * @return  {@code true} if, and only if, this file store is read-only
     * 说明此文件存储是否为只读。如果不支持对文件进行写操作或其他更改，则文件存储是只读的。任何试图创建文件、打开现有文件以进行写入等操作的尝试都会引发IOException。
     */
    public abstract boolean isReadOnly();

    /**
     * Returns the size, in bytes, of the file store.
     *
     * @return  the size of the file store, in bytes
     *
     * @throws  IOException
     *          if an I/O error occurs
     *          返回文件存储的大小(以字节为单位)。
     */
    public abstract long getTotalSpace() throws IOException;

    /**
     * Returns the number of bytes available to this Java virtual machine on the
     * file store.
     *
     * <p> The returned number of available bytes is a hint, but not a
     * guarantee, that it is possible to use most or any of these bytes.  The
     * number of usable bytes is most likely to be accurate immediately
     * after the space attributes are obtained. It is likely to be made inaccurate
     * by any external I/O operations including those made on the system outside
     * of this Java virtual machine.
     *
     * @return  the number of bytes available
     *
     * @throws  IOException
     *          if an I/O error occurs
     *          返回文件存储中此Java虚拟机可用的字节数。
    可用字节的返回数量是一个提示，但不是保证，它可以使用大多数或任何这些字节。在获得空间属性之后，可用字节的数量最有可能是准确的。任何外部I/O操作(包括在此Java虚拟机之外的系统上进行的操作)都可能使其不准确。
     */
    public abstract long getUsableSpace() throws IOException;

    /**
     * Returns the number of unallocated bytes in the file store.
     *
     * <p> The returned number of unallocated bytes is a hint, but not a
     * guarantee, that it is possible to use most or any of these bytes.  The
     * number of unallocated bytes is most likely to be accurate immediately
     * after the space attributes are obtained. It is likely to be
     * made inaccurate by any external I/O operations including those made on
     * the system outside of this virtual machine.
     *
     * @return  the number of unallocated bytes
     *
     * @throws  IOException
     *          if an I/O error occurs
     *          返回文件存储中未分配的字节数。
    返回的未分配字节数是一个提示，但不能保证可以使用这些字节中的大多数或任何一个。在获得空间属性之后，未分配的字节数很可能立即准确。任何外部I/O操作(包括在此虚拟机之外的系统上所做的操作)可能都不准确。
     */
    public abstract long getUnallocatedSpace() throws IOException;

    /**
     * Tells whether or not this file store supports the file attributes
     * identified by the given file attribute view.
     *
     * <p> Invoking this method to test if the file store supports {@link
     * BasicFileAttributeView} will always return {@code true}. In the case of
     * the default provider, this method cannot guarantee to give the correct
     * result when the file store is not a local storage device. The reasons for
     * this are implementation specific and therefore unspecified.
     *
     * @param   type
     *          the file attribute view type
     *
     * @return  {@code true} if, and only if, the file attribute view is
     *          supported
     *          告诉此文件存储库是否支持给定文件属性视图标识的文件属性。
    调用此方法来测试文件存储是否支持BasicFileAttributeView将总是返回true。在默认提供程序的情况下，当文件存储不是本地存储设备时，此方法不能保证给出正确的结果。造成这种情况的原因是具体的实现，因此没有具体说明。
     */
    public abstract boolean supportsFileAttributeView(Class<? extends FileAttributeView> type);

    /**
     * Tells whether or not this file store supports the file attributes
     * identified by the given file attribute view.
     *
     * <p> Invoking this method to test if the file store supports {@link
     * BasicFileAttributeView}, identified by the name "{@code basic}" will
     * always return {@code true}. In the case of the default provider, this
     * method cannot guarantee to give the correct result when the file store is
     * not a local storage device. The reasons for this are implementation
     * specific and therefore unspecified.
     *
     * @param   name
     *          the {@link FileAttributeView#name name} of file attribute view
     *
     * @return  {@code true} if, and only if, the file attribute view is
     *          supported
     *          告诉此文件存储库是否支持给定文件属性视图标识的文件属性。
    调用此方法来测试文件存储是否支持BasicFileAttributeView(由名称“basic”标识)，它总是返回true。在默认提供程序的情况下，当文件存储不是本地存储设备时，此方法不能保证给出正确的结果。造成这种情况的原因是具体的实现，因此没有具体说明。
     */
    public abstract boolean supportsFileAttributeView(String name);

    /**
     * Returns a {@code FileStoreAttributeView} of the given type.
     *
     * <p> This method is intended to be used where the file store attribute
     * view defines type-safe methods to read or update the file store attributes.
     * The {@code type} parameter is the type of the attribute view required and
     * the method returns an instance of that type if supported.
     *
     * @param   <V>
     *          The {@code FileStoreAttributeView} type
     * @param   type
     *          the {@code Class} object corresponding to the attribute view
     *
     * @return  a file store attribute view of the specified type or
     *          {@code null} if the attribute view is not available
     *          返回给定类型的FileStoreAttributeView。
    当file store属性视图定义用于读取或更新file store属性的类型安全方法时，将使用此方法。类型参数是所需的属性视图的类型，如果支持，该方法将返回该类型的实例。
     */
    public abstract <V extends FileStoreAttributeView> V
        getFileStoreAttributeView(Class<V> type);

    /**
     * Reads the value of a file store attribute.
     *
     * <p> The {@code attribute} parameter identifies the attribute to be read
     * and takes the form:
     * <blockquote>
     * <i>view-name</i><b>:</b><i>attribute-name</i>
     * </blockquote>
     * where the character {@code ':'} stands for itself.
     *
     * <p> <i>view-name</i> is the {@link FileStoreAttributeView#name name} of
     * a {@link FileStore AttributeView} that identifies a set of file attributes.
     * <i>attribute-name</i> is the name of the attribute.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to know if ZFS compression is enabled (assuming the "zfs"
     * view is supported):
     * <pre>
     *    boolean compression = (Boolean)fs.getAttribute("zfs:compression");
     * </pre>
     *
     * @param   attribute
     *          the attribute to read

     * @return  the attribute value; {@code null} may be a valid valid for some
     *          attributes
     *
     * @throws  UnsupportedOperationException
     *          if the attribute view is not available or it does not support
     *          reading the attribute
     * @throws  IOException
     *          if an I/O error occurs
     *          读取文件存储属性的值。
    属性参数标识要读取的属性，并采用以下形式:
    其中字符':'代表它自己。
    viewname是标识一组文件属性的AttributeView的名称。属性名是属性的名称。
     */
    public abstract Object getAttribute(String attribute) throws IOException;
}
