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

/**
 * Basic attributes associated with a file in a file system.
 *
 * <p> Basic file attributes are attributes that are common to many file systems
 * and consist of mandatory and optional file attributes as defined by this
 * interface.
 *
 * <p> <b>Usage Example:</b>
 * <pre>
 *    Path file = ...
 *    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
 * </pre>
 *
 * @since 1.7
 *
 * @see BasicFileAttributeView
 * 与文件系统中的文件相关联的基本属性。
基本文件属性是许多文件系统常见的属性，由这个接口定义的强制和可选文件属性组成。
 */

public interface BasicFileAttributes {

    /**
     * Returns the time of last modification.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time of last modification then this method returns an
     * implementation specific default value, typically a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return  a {@code FileTime} representing the time the file was last
     *          modified
     *          返回上次修改的时间。
    如果文件系统实现不支持时间戳来指示最后修改的时间，那么该方法将返回一个实现特定的默认值，通常是一个表示历元(1970-01-01t00:00 z)的文件时间。
     */
    FileTime lastModifiedTime();

    /**
     * Returns the time of last access.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time of last access then this method returns
     * an implementation specific default value, typically the {@link
     * #lastModifiedTime() last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return  a {@code FileTime} representing the time of last access
     * 返回最后访问的时间。
    如果文件系统实现不支持时间戳来指示最后访问的时间，那么该方法将返回一个实现特定的默认值，通常是最后修改时间或表示历元(1970-01-01t00:00 z)的文件时间。
     */
    FileTime lastAccessTime();

    /**
     * Returns the creation time. The creation time is the time that the file
     * was created.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time when the file was created then this method returns
     * an implementation specific default value, typically the {@link
     * #lastModifiedTime() last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return   a {@code FileTime} representing the time the file was created
     * 返回创建时间。创建时间是文件被创建的时间。
    如果文件系统实现不支持时间戳来指示创建文件的时间，那么该方法将返回特定于实现的默认值，通常是表示历元(1970-01-01T00:00)的最后修改时间或文件时间
     */
    FileTime creationTime();

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return {@code true} if the file is a regular file with opaque content
     * 告诉该文件是否是含有不透明内容的常规文件。
     */
    boolean isRegularFile();

    /**
     * Tells whether the file is a directory.
     *
     * @return {@code true} if the file is a directory
     * 告诉文件是否为目录。
     */
    boolean isDirectory();

    /**
     * Tells whether the file is a symbolic link.
     *
     * @return {@code true} if the file is a symbolic link
     * 告诉文件是否是一个符号链接。
     */
    boolean isSymbolicLink();

    /**
     * Tells whether the file is something other than a regular file, directory,
     * or symbolic link.
     *
     * @return {@code true} if the file something other than a regular file,
     *         directory or symbolic link
     *         说明文件是否与普通文件、目录或符号链接无关。
     */
    boolean isOther();

    /**
     * Returns the size of the file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons. The size of files that are not {@link
     * #isRegularFile regular} files is implementation specific and
     * therefore unspecified.
     *
     * @return  the file size, in bytes
     * 返回文件的大小(以字节为单位)。由于压缩、对稀疏文件的支持或其他原因，大小可能与文件系统上的实际大小不同。不是常规文件的文件的大小是特定于实现的，因此不指定。
     */
    long size();

    /**
     * Returns an object that uniquely identifies the given file, or {@code
     * null} if a file key is not available. On some platforms or file systems
     * it is possible to use an identifier, or a combination of identifiers to
     * uniquely identify a file. Such identifiers are important for operations
     * such as file tree traversal in file systems that support <a
     * href="../package-summary.html#links">symbolic links</a> or file systems
     * that allow a file to be an entry in more than one directory. On UNIX file
     * systems, for example, the <em>device ID</em> and <em>inode</em> are
     * commonly used for such purposes.
     *
     * <p> The file key returned by this method can only be guaranteed to be
     * unique if the file system and files remain static. Whether a file system
     * re-uses identifiers after a file is deleted is implementation dependent and
     * therefore unspecified.
     *
     * <p> File keys returned by this method can be compared for equality and are
     * suitable for use in collections. If the file system and files remain static,
     * and two files are the {@link java.nio.file.Files#isSameFile same} with
     * non-{@code null} file keys, then their file keys are equal.
     *
     * @return an object that uniquely identifies the given file, or {@code null}
     *
     * @see java.nio.file.Files#walkFileTree
     * 返回唯一标识给定文件的对象，如果文件键不可用，则返回null。在某些平台或文件系统中，可以使用标识符或标识符组合来惟一地标识文件。此类标识符对于文件系统中的文件树遍历等操作非常重要，这些文件系统支持符号链接或文件系统，允许文件成为多个目录中的一个条目。例如，在UNIX文件系统中，设备ID和inode通常用于此类目的。
    该方法返回的文件密钥只有在文件系统和文件保持静态时才能保证是唯一的。文件系统在删除文件后是否重用标识符取决于实现，因此未指定。
    该方法返回的文件键可以进行相等性比较，并且适合在集合中使用。如果文件系统和文件保持静态，并且两个文件与非空文件键相同，那么它们的文件键是相等的。
     */
    Object fileKey();
}
