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

package java.nio.file.attribute;

import java.io.IOException;

/**
 * A file attribute view that provides a view of a <em>basic set</em> of file
 * attributes common to many file systems. The basic set of file attributes
 * consist of <em>mandatory</em> and <em>optional</em> file attributes as
 * defined by the {@link BasicFileAttributes} interface.

 * <p> The file attributes are retrieved from the file system as a <em>bulk
 * operation</em> by invoking the {@link #readAttributes() readAttributes} method.
 * This class also defines the {@link #setTimes setTimes} method to update the
 * file's time attributes.
 *
 * <p> Where dynamic access to file attributes is required, the attributes
 * supported by this attribute view have the following names and types:
 * <blockquote>
 *  <table border="1" cellpadding="8" summary="Supported attributes">
 *   <tr>
 *     <th> Name </th>
 *     <th> Type </th>
 *   </tr>
 *  <tr>
 *     <td> "lastModifiedTime" </td>
 *     <td> {@link FileTime} </td>
 *   </tr>
 *   <tr>
 *     <td> "lastAccessTime" </td>
 *     <td> {@link FileTime} </td>
 *   </tr>
 *   <tr>
 *     <td> "creationTime" </td>
 *     <td> {@link FileTime} </td>
 *   </tr>
 *   <tr>
 *     <td> "size" </td>
 *     <td> {@link Long} </td>
 *   </tr>
 *   <tr>
 *     <td> "isRegularFile" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "isDirectory" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "isSymbolicLink" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "isOther" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "fileKey" </td>
 *     <td> {@link Object} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> The {@link java.nio.file.Files#getAttribute getAttribute} method may be
 * used to read any of these attributes as if by invoking the {@link
 * #readAttributes() readAttributes()} method.
 *
 * <p> The {@link java.nio.file.Files#setAttribute setAttribute} method may be
 * used to update the file's last modified time, last access time or create time
 * attributes as if by invoking the {@link #setTimes setTimes} method.
 *
 * @since 1.7
 * 文件属性视图，提供许多文件系统共同的一组基本文件属性的视图。基本的文件属性集由BasicFileAttributes接口定义的强制性和可选的文件属性组成。
通过调用readAttributes方法，文件属性作为批量操作从文件系统中检索。这个类还定义了setTimes方法来更新文件的时间属性。
如果需要动态访问文件属性，则此属性视图支持的属性具有以下名称和类型:
getAttribute方法可以像调用readAttributes()方法一样用于读取任何这些属性。
可以使用setAttribute方法更新文件的最后修改时间、最后访问时间或创建时间属性，就像调用setTimes方法一样。
 */

public interface BasicFileAttributeView
    extends FileAttributeView
{
    /**
     * Returns the name of the attribute view. Attribute views of this type
     * have the name {@code "basic"}.
     * 返回属性视图的名称。这种类型的属性视图的名称为“basic”。
     */
    @Override
    String name();

    /**
     * Reads the basic file attributes as a bulk operation.
     *
     * <p> It is implementation specific if all file attributes are read as an
     * atomic operation with respect to other file system operations.
     *
     * @return  the file attributes
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file
     *          读取基本文件属性作为批量操作。
    如果所有文件属性都被作为与其他文件系统操作相关的原子操作读取，则它是特定于实现的。
     */
    BasicFileAttributes readAttributes() throws IOException;

    /**
     * Updates any or all of the file's last modified time, last access time,
     * and create time attributes.
     *
     * <p> This method updates the file's timestamp attributes. The values are
     * converted to the epoch and precision supported by the file system.
     * Converting from finer to coarser granularities result in precision loss.
     * The behavior of this method when attempting to set a timestamp that is
     * not supported or to a value that is outside the range supported by the
     * underlying file store is not defined. It may or not fail by throwing an
     * {@code IOException}.
     *
     * <p> If any of the {@code lastModifiedTime}, {@code lastAccessTime},
     * or {@code createTime} parameters has the value {@code null} then the
     * corresponding timestamp is not changed. An implementation may require to
     * read the existing values of the file attributes when only some, but not
     * all, of the timestamp attributes are updated. Consequently, this method
     * may not be an atomic operation with respect to other file system
     * operations. Reading and re-writing existing values may also result in
     * precision loss. If all of the {@code lastModifiedTime}, {@code
     * lastAccessTime} and {@code createTime} parameters are {@code null} then
     * this method has no effect.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to change a file's last access time.
     * <pre>
     *    Path path = ...
     *    FileTime time = ...
     *    Files.getFileAttributeView(path, BasicFileAttributeView.class).setTimes(null, time, null);
     * </pre>
     *
     * @param   lastModifiedTime
     *          the new last modified time, or {@code null} to not change the
     *          value
     * @param   lastAccessTime
     *          the last access time, or {@code null} to not change the value
     * @param   createTime
     *          the file's create time, or {@code null} to not change the value
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, its  {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file
     *
     * @see java.nio.file.Files#setLastModifiedTime
     * 更新文件的最后修改时间、最后访问时间和创建时间属性。
    此方法更新文件的时间戳属性。这些值被转换为文件系统支持的纪元和精度。从细粒度到粗粒度的粒度转换会导致精度损失。在试图设置不受支持的时间戳或在底层文件存储支持的范围之外的值时，该方法的行为没有定义。它可能会抛出IOException，也可能不会失败。
    如果lastModifiedTime、lastAccessTime或createTime的任何一个参数的值为null，那么相应的时间戳就不会改变。当只更新时间戳属性时，一个实现可能需要读取文件属性的现有值，但不是全部。因此，该方法可能不是相对于其他文件系统操作的原子操作。读取和重写现有值也可能导致精度损失。如果所有的lastModifiedTime、lastAccessTime和createTime参数都是null，那么这个方法就没有效果。
     */
    void setTimes(FileTime lastModifiedTime,
                  FileTime lastAccessTime,
                  FileTime createTime) throws IOException;
}
