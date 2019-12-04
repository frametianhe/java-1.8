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
import java.nio.channels.SeekableByteChannel;
import java.util.Set;
import java.io.IOException;

/**
 * A {@code DirectoryStream} that defines operations on files that are located
 * relative to an open directory. A {@code SecureDirectoryStream} is intended
 * for use by sophisticated or security sensitive applications requiring to
 * traverse file trees or otherwise operate on directories in a race-free manner.
 * Race conditions can arise when a sequence of file operations cannot be
 * carried out in isolation. Each of the file operations defined by this
 * interface specify a relative path. All access to the file is relative
 * to the open directory irrespective of if the directory is moved or replaced
 * by an attacker while the directory is open. A {@code SecureDirectoryStream}
 * may also be used as a virtual <em>working directory</em>.
 *
 * <p> A {@code SecureDirectoryStream} requires corresponding support from the
 * underlying operating system. Where an implementation supports this features
 * then the {@code DirectoryStream} returned by the {@link Files#newDirectoryStream
 * newDirectoryStream} method will be a {@code SecureDirectoryStream} and must
 * be cast to that type in order to invoke the methods defined by this interface.
 *
 * <p> In the case of the default {@link java.nio.file.spi.FileSystemProvider
 * provider}, and a security manager is set, then the permission checks are
 * performed using the path obtained by resolving the given relative path
 * against the <i>original path</i> of the directory (irrespective of if the
 * directory is moved since it was opened).
 *
 * @since   1.7
 * 一个DirectoryStream，定义相对于打开目录的文件的操作。SecureDirectoryStream用于复杂或安全敏感的应用程序，这些应用程序需要遍历文件树或以无竞争的方式对目录进行操作。当一个文件操作序列不能单独执行时，就会出现竞态条件。这个接口定义的每个文件操作都指定一个相对路径。对该文件的所有访问都是相对于打开的目录的，而不管该目录在打开时是否被攻击者移动或替换。SecureDirectoryStream也可以用作虚拟工作目录。
SecureDirectoryStream需要来自底层操作系统的相应支持。如果实现支持这个特性，那么newDirectoryStream方法返回的DirectoryStream将是一个SecureDirectoryStream，并且必须转换为该类型，以便调用这个接口定义的方法。
对于默认提供程序，并设置了安全管理器，然后使用根据目录的原始路径解析给定的相对路径所获得的路径执行权限检查(不管该目录是否在打开后被移动)。
 */

public interface SecureDirectoryStream<T>
    extends DirectoryStream<T>
{
    /**
     * Opens the directory identified by the given path, returning a {@code
     * SecureDirectoryStream} to iterate over the entries in the directory.
     *
     * <p> This method works in exactly the manner specified by the {@link
     * Files#newDirectoryStream(Path) newDirectoryStream} method for the case that
     * the {@code path} parameter is an {@link Path#isAbsolute absolute} path.
     * When the parameter is a relative path then the directory to open is
     * relative to this open directory. The {@link
     * LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} option may be used to
     * ensure that this method fails if the file is a symbolic link.
     *
     * <p> The new directory stream, once created, is not dependent upon the
     * directory stream used to create it. Closing this directory stream has no
     * effect upon newly created directory stream.
     *
     * @param   path
     *          the path to the directory to open
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  a new and open {@code SecureDirectoryStream} object
     *
     * @throws  ClosedDirectoryStreamException
     *          if the directory stream is closed
     * @throws  NotDirectoryException
     *          if the file could not otherwise be opened because it is not
     *          a directory <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the directory.
     *          打开给定路径所标识的目录，返回一个SecureDirectoryStream来遍历目录中的条目。
    对于路径参数是绝对路径的情况，此方法的工作方式与newDirectoryStream方法指定的方式完全相同。当参数是相对路径时，打开的目录相对于这个打开的目录。如果文件是一个符号链接，可以使用NOFOLLOW_LINKS选项来确保该方法失败。
    新目录流一旦创建，并不依赖于用于创建它的目录流。关闭这个目录流对新创建的目录流没有影响。
     */
    SecureDirectoryStream<T> newDirectoryStream(T path, LinkOption... options)
        throws IOException;

    /**
     * Opens or creates a file in this directory, returning a seekable byte
     * channel to access the file.
     *
     * <p> This method works in exactly the manner specified by the {@link
     * Files#newByteChannel Files.newByteChannel} method for the
     * case that the {@code path} parameter is an {@link Path#isAbsolute absolute}
     * path. When the parameter is a relative path then the file to open or
     * create is relative to this open directory. In addition to the options
     * defined by the {@code Files.newByteChannel} method, the {@link
     * LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} option may be used to
     * ensure that this method fails if the file is a symbolic link.
     *
     * <p> The channel, once created, is not dependent upon the directory stream
     * used to create it. Closing this directory stream has no effect upon the
     * channel.
     *
     * @param   path
     *          the path of the file to open open or create
     * @param   options
     *          options specifying how the file is opened
     * @param   attrs
     *          an optional list of attributes to set atomically when creating
     *          the file
     *
     * @return  the seekable byte channel
     *
     * @throws  ClosedDirectoryStreamException
     *          if the directory stream is closed
     * @throws  IllegalArgumentException
     *          if the set contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          if an unsupported open option is specified or the array contains
     *          attributes that cannot be set atomically when creating the file
     * @throws  FileAlreadyExistsException
     *          if a file of that name already exists and the {@link
     *          StandardOpenOption#CREATE_NEW CREATE_NEW} option is specified
     *          <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the path if the file
     *          is opened for reading. The {@link SecurityManager#checkWrite(String)
     *          checkWrite} method is invoked to check write access to the path
     *          if the file is opened for writing.
     *          在此目录中打开或创建一个文件，返回一个可查找的字节通道以访问该文件。
    这个方法按照文件指定的方式工作。newByteChannel方法用于路径参数为绝对路径的情况。当参数是相对路径时，要打开或创建的文件相对于这个打开的目录。除了文件定义的选项。newByteChannel方法，如果文件是一个符号链接，可以使用NOFOLLOW_LINKS选项来确保该方法失败。
    通道一旦创建，就不依赖用于创建它的目录流。关闭这个目录流对通道没有影响。
     */
    SeekableByteChannel newByteChannel(T path,
                                       Set<? extends OpenOption> options,
                                       FileAttribute<?>... attrs)
        throws IOException;

    /**
     * Deletes a file.
     *
     * <p> Unlike the {@link Files#delete delete()} method, this method does
     * not first examine the file to determine if the file is a directory.
     * Whether a directory is deleted by this method is system dependent and
     * therefore not specified. If the file is a symbolic link, then the link
     * itself, not the final target of the link, is deleted. When the
     * parameter is a relative path then the file to delete is relative to
     * this open directory.
     *
     * @param   path
     *          the path of the file to delete
     *
     * @throws  ClosedDirectoryStreamException
     *          if the directory stream is closed
     * @throws  NoSuchFileException
     *          if the file does not exist <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkDelete(String) checkDelete}
     *          method is invoked to check delete access to the file
     *          删除一个文件。
    与delete()方法不同，该方法首先检查文件以确定文件是否是目录。该方法是否删除一个目录取决于系统，因此没有指定。如果文件是一个符号链接，那么链接本身就会被删除，而不是链接的最终目标。当参数是相对路径时，要删除的文件相对于这个打开的目录。
     */
    void deleteFile(T path) throws IOException;

    /**
     * Deletes a directory.
     *
     * <p> Unlike the {@link Files#delete delete()} method, this method
     * does not first examine the file to determine if the file is a directory.
     * Whether non-directories are deleted by this method is system dependent and
     * therefore not specified. When the parameter is a relative path then the
     * directory to delete is relative to this open directory.
     *
     * @param   path
     *          the path of the directory to delete
     *
     * @throws  ClosedDirectoryStreamException
     *          if the directory stream is closed
     * @throws  NoSuchFileException
     *          if the directory does not exist <i>(optional specific exception)</i>
     * @throws  DirectoryNotEmptyException
     *          if the directory could not otherwise be deleted because it is
     *          not empty <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkDelete(String) checkDelete}
     *          method is invoked to check delete access to the directory
     *          删除一个目录。
    与delete()方法不同，该方法首先检查文件以确定文件是否是目录。此方法是否删除非目录取决于系统，因此没有指定。当参数是相对路径时，删除的目录相对于这个打开的目录。
     */
    void deleteDirectory(T path) throws IOException;

    /**
     * Move a file from this directory to another directory.
     *
     * <p> This method works in a similar manner to {@link Files#move move}
     * method when the {@link StandardCopyOption#ATOMIC_MOVE ATOMIC_MOVE} option
     * is specified. That is, this method moves a file as an atomic file system
     * operation. If the {@code srcpath} parameter is an {@link Path#isAbsolute
     * absolute} path then it locates the source file. If the parameter is a
     * relative path then it is located relative to this open directory. If
     * the {@code targetpath} parameter is absolute then it locates the target
     * file (the {@code targetdir} parameter is ignored). If the parameter is
     * a relative path it is located relative to the open directory identified
     * by the {@code targetdir} parameter. In all cases, if the target file
     * exists then it is implementation specific if it is replaced or this
     * method fails.
     *
     * @param   srcpath
     *          the name of the file to move
     * @param   targetdir
     *          the destination directory
     * @param   targetpath
     *          the name to give the file in the destination directory
     *
     * @throws  ClosedDirectoryStreamException
     *          if this or the target directory stream is closed
     * @throws  FileAlreadyExistsException
     *          if the file already exists in the target directory and cannot
     *          be replaced <i>(optional specific exception)</i>
     * @throws  AtomicMoveNotSupportedException
     *          if the file cannot be moved as an atomic file system operation
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to both the source and
     *          target file.
     *          将文件从这个目录移动到另一个目录。
    此方法与指定ATOMIC_MOVE选项时的move方法类似。也就是说，该方法将文件移动为一个原子文件系统操作。如果srcpath参数是绝对路径，那么它将定位源文件。如果参数是一个相对路径，那么它相对于这个打开的目录。如果targetpath参数是绝对的，那么它将定位目标文件(忽略targetdir参数)。如果参数是一个相对路径，则它相对于targetdir参数标识的打开目录。在所有情况下，如果目标文件存在，那么如果它被替换，或者这个方法失败，那么它就是实现特定的。
     */
    void move(T srcpath, SecureDirectoryStream<T> targetdir, T targetpath)
        throws IOException;

    /**
     * Returns a new file attribute view to access the file attributes of this
     * directory.
     *
     * <p> The resulting file attribute view can be used to read or update the
     * attributes of this (open) directory. The {@code type} parameter specifies
     * the type of the attribute view and the method returns an instance of that
     * type if supported. Invoking this method to obtain a {@link
     * BasicFileAttributeView} always returns an instance of that class that is
     * bound to this open directory.
     *
     * <p> The state of resulting file attribute view is intimately connected
     * to this directory stream. Once the directory stream is {@link #close closed},
     * then all methods to read or update attributes will throw {@link
     * ClosedDirectoryStreamException ClosedDirectoryStreamException}.
     *
     * @param   <V>
     *          The {@code FileAttributeView} type
     * @param   type
     *          the {@code Class} object corresponding to the file attribute view
     *
     * @return  a new file attribute view of the specified type bound to
     *          this directory stream, or {@code null} if the attribute view
     *          type is not available
     *          返回一个新的文件属性视图来访问这个目录的文件属性。
    生成的文件属性视图可用于读取或更新这个(open)目录的属性。类型参数指定属性视图的类型，如果支持，方法返回该类型的实例。调用此方法获取BasicFileAttributeView总是返回绑定到这个打开目录的类的实例。
    结果文件属性视图的状态与这个目录流密切相关。关闭目录流之后，所有读取或更新属性的方法都会抛出ClosedDirectoryStreamException异常。
     */
    <V extends FileAttributeView> V getFileAttributeView(Class<V> type);

    /**
     * Returns a new file attribute view to access the file attributes of a file
     * in this directory.
     *
     * <p> The resulting file attribute view can be used to read or update the
     * attributes of file in this directory. The {@code type} parameter specifies
     * the type of the attribute view and the method returns an instance of that
     * type if supported. Invoking this method to obtain a {@link
     * BasicFileAttributeView} always returns an instance of that class that is
     * bound to the file in the directory.
     *
     * <p> The state of resulting file attribute view is intimately connected
     * to this directory stream. Once the directory stream {@link #close closed},
     * then all methods to read or update attributes will throw {@link
     * ClosedDirectoryStreamException ClosedDirectoryStreamException}. The
     * file is not required to exist at the time that the file attribute view
     * is created but methods to read or update attributes of the file will
     * fail when invoked and the file does not exist.
     *
     * @param   <V>
     *          The {@code FileAttributeView} type
     * @param   path
     *          the path of the file
     * @param   type
     *          the {@code Class} object corresponding to the file attribute view
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  a new file attribute view of the specified type bound to a
     *          this directory stream, or {@code null} if the attribute view
     *          type is not available
     *          返回一个新的文件属性视图，以访问该目录中的文件的文件属性。
    生成的文件属性视图可用于读取或更新该目录中的文件属性。类型参数指定属性视图的类型，如果支持，方法返回该类型的实例。调用此方法获取BasicFileAttributeView始终返回绑定到目录中的文件的该类的实例。
    结果文件属性视图的状态与这个目录流密切相关。一旦目录流关闭，所有读取或更新属性的方法都会抛出ClosedDirectoryStreamException异常。在创建file attribute视图时，并不需要该文件存在，但是在调用该文件时读取或更新该文件属性的方法将失败，并且该文件不存在。
     *
     */
    <V extends FileAttributeView> V getFileAttributeView(T path,
                                                         Class<V> type,
                                                         LinkOption... options);
}
