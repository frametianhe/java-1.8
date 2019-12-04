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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;   // javadoc
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.spi.FileSystemProvider;
import java.nio.file.spi.FileTypeDetector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class consists exclusively of static methods that operate on files,
 * directories, or other types of files.
 *
 * <p> In most cases, the methods defined here will delegate to the associated
 * file system provider to perform the file operations.
 *
 * @since 1.7
 * 这个类只包含对文件、目录或其他类型的文件进行操作的静态方法。
在大多数情况下，这里定义的方法将委托给相关的文件系统提供程序来执行文件操作。
 */

public final class Files {
    private Files() { }

    /**
     * Returns the {@code FileSystemProvider} to delegate to.
     */
    private static FileSystemProvider provider(Path path) {
        return path.getFileSystem().provider();
    }

    /**
     * Convert a Closeable to a Runnable by converting checked IOException
     * to UncheckedIOException
     * 通过将选中的IOException转换为UncheckedIOException，将一个可运行的闭包转换为可运行的。
     */
    private static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    // -- File contents --

    /**
     * Opens a file, returning an input stream to read from the file. The stream
     * will not be buffered, and is not required to support the {@link
     * InputStream#mark mark} or {@link InputStream#reset reset} methods. The
     * stream will be safe for access by multiple concurrent threads. Reading
     * commences at the beginning of the file. Whether the returned stream is
     * <i>asynchronously closeable</i> and/or <i>interruptible</i> is highly
     * file system provider specific and therefore not specified.
     *
     * <p> The {@code options} parameter determines how the file is opened.
     * If no options are present then it is equivalent to opening the file with
     * the {@link StandardOpenOption#READ READ} option. In addition to the {@code
     * READ} option, an implementation may also support additional implementation
     * specific options.
     *
     * @param   path
     *          the path to the file to open
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new input stream
     *
     * @throws  IllegalArgumentException
     *          if an invalid combination of options is specified
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *          打开一个文件，返回从文件中读取的输入流。流将不会被缓冲，也不需要支持标记或重置方法。对于多个并发线程来说，流是安全的。阅读从文件的开头开始。返回的流是否异步关闭和/或可中断是与文件系统提供程序相关的，因此没有指定。
    options参数确定如何打开文件。如果没有选项，那么就相当于用READ选项打开文件。除了READ选项之外，实现还可以支持其他实现特定的选项。
     */
    public static InputStream newInputStream(Path path, OpenOption... options)
        throws IOException
    {
        return provider(path).newInputStream(path, options);
    }

    /**
     * Opens or creates a file, returning an output stream that may be used to
     * write bytes to the file. The resulting stream will not be buffered. The
     * stream will be safe for access by multiple concurrent threads. Whether
     * the returned stream is <i>asynchronously closeable</i> and/or
     * <i>interruptible</i> is highly file system provider specific and
     * therefore not specified.
     *
     * <p> This method opens or creates a file in exactly the manner specified
     * by the {@link #newByteChannel(Path,Set,FileAttribute[]) newByteChannel}
     * method with the exception that the {@link StandardOpenOption#READ READ}
     * option may not be present in the array of options. If no options are
     * present then this method works as if the {@link StandardOpenOption#CREATE
     * CREATE}, {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING},
     * and {@link StandardOpenOption#WRITE WRITE} options are present. In other
     * words, it opens the file for writing, creating the file if it doesn't
     * exist, or initially truncating an existing {@link #isRegularFile
     * regular-file} to a size of {@code 0} if it exists.
     *
     * <p> <b>Usage Examples:</b>
     * <pre>
     *     Path path = ...
     *
     *     // truncate and overwrite an existing file, or create the file if
     *     // it doesn't initially exist
     *     OutputStream out = Files.newOutputStream(path);
     *
     *     // append to an existing file, fail if the file does not exist
     *     out = Files.newOutputStream(path, APPEND);
     *
     *     // append to an existing file, create file if it doesn't initially exist
     *     out = Files.newOutputStream(path, CREATE, APPEND);
     *
     *     // always create new file, failing if it already exists
     *     out = Files.newOutputStream(path, CREATE_NEW);
     * </pre>
     *
     * @param   path
     *          the path to the file to open or create
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new output stream
     *
     * @throws  IllegalArgumentException
     *          if {@code options} contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file. The {@link
     *          SecurityManager#checkDelete(String) checkDelete} method is
     *          invoked to check delete access if the file is opened with the
     *          {@code DELETE_ON_CLOSE} option.
     *          打开或创建一个文件，返回可用于将字节写入文件的输出流。产生的流将不会被缓冲。对于多个并发线程来说，流是安全的。返回的流是否异步关闭和/或可中断是与文件系统提供程序相关的，因此没有指定。
    此方法以newByteChannel方法指定的方式打开或创建文件，但是读取选项可能不在选项数组中。如果不存在选项，那么该方法就会像存在CREATE、TRUNCATE_EXISTING和WRITE选项一样工作。换句话说，它打开文件进行写入，创建不存在的文件，或者在现有的常规文件存在时首先将其截断为0。
     */
    public static OutputStream newOutputStream(Path path, OpenOption... options)
        throws IOException
    {
        return provider(path).newOutputStream(path, options);
    }

    /**
     * Opens or creates a file, returning a seekable byte channel to access the
     * file.
     *
     * <p> The {@code options} parameter determines how the file is opened.
     * The {@link StandardOpenOption#READ READ} and {@link
     * StandardOpenOption#WRITE WRITE} options determine if the file should be
     * opened for reading and/or writing. If neither option (or the {@link
     * StandardOpenOption#APPEND APPEND} option) is present then the file is
     * opened for reading. By default reading or writing commence at the
     * beginning of the file.
     *
     * <p> In the addition to {@code READ} and {@code WRITE}, the following
     * options may be present:
     *
     * <table border=1 cellpadding=5 summary="Options">
     * <tr> <th>Option</th> <th>Description</th> </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#APPEND APPEND} </td>
     *   <td> If this option is present then the file is opened for writing and
     *     each invocation of the channel's {@code write} method first advances
     *     the position to the end of the file and then writes the requested
     *     data. Whether the advancement of the position and the writing of the
     *     data are done in a single atomic operation is system-dependent and
     *     therefore unspecified. This option may not be used in conjunction
     *     with the {@code READ} or {@code TRUNCATE_EXISTING} options. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} </td>
     *   <td> If this option is present then the existing file is truncated to
     *   a size of 0 bytes. This option is ignored when the file is opened only
     *   for reading. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#CREATE_NEW CREATE_NEW} </td>
     *   <td> If this option is present then a new file is created, failing if
     *   the file already exists or is a symbolic link. When creating a file the
     *   check for the existence of the file and the creation of the file if it
     *   does not exist is atomic with respect to other file system operations.
     *   This option is ignored when the file is opened only for reading. </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#CREATE CREATE} </td>
     *   <td> If this option is present then an existing file is opened if it
     *   exists, otherwise a new file is created. This option is ignored if the
     *   {@code CREATE_NEW} option is also present or the file is opened only
     *   for reading. </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} </td>
     *   <td> When this option is present then the implementation makes a
     *   <em>best effort</em> attempt to delete the file when closed by the
     *   {@link SeekableByteChannel#close close} method. If the {@code close}
     *   method is not invoked then a <em>best effort</em> attempt is made to
     *   delete the file when the Java virtual machine terminates. </td>
     * </tr>
     * <tr>
     *   <td>{@link StandardOpenOption#SPARSE SPARSE} </td>
     *   <td> When creating a new file this option is a <em>hint</em> that the
     *   new file will be sparse. This option is ignored when not creating
     *   a new file. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#SYNC SYNC} </td>
     *   <td> Requires that every update to the file's content or metadata be
     *   written synchronously to the underlying storage device. (see <a
     *   href="package-summary.html#integrity"> Synchronized I/O file
     *   integrity</a>). </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#DSYNC DSYNC} </td>
     *   <td> Requires that every update to the file's content be written
     *   synchronously to the underlying storage device. (see <a
     *   href="package-summary.html#integrity"> Synchronized I/O file
     *   integrity</a>). </td>
     * </tr>
     * </table>
     *
     * <p> An implementation may also support additional implementation specific
     * options.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * file-attributes} to set atomically when a new file is created.
     *
     * <p> In the case of the default provider, the returned seekable byte channel
     * is a {@link java.nio.channels.FileChannel}.
     *
     * <p> <b>Usage Examples:</b>
     * <pre>
     *     Path path = ...
     *
     *     // open file for reading
     *     ReadableByteChannel rbc = Files.newByteChannel(path, EnumSet.of(READ)));
     *
     *     // open file for writing to the end of an existing file, creating
     *     // the file if it doesn't already exist
     *     WritableByteChannel wbc = Files.newByteChannel(path, EnumSet.of(CREATE,APPEND));
     *
     *     // create file with initial permissions, opening it for both reading and writing
     *     {@code FileAttribute<Set<PosixFilePermission>> perms = ...}
     *     SeekableByteChannel sbc = Files.newByteChannel(path, EnumSet.of(CREATE_NEW,READ,WRITE), perms);
     * </pre>
     *
     * @param   path
     *          the path to the file to open or create
     * @param   options
     *          options specifying how the file is opened
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  a new seekable byte channel
     *
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
     *          method is invoked to check read access to the path if the file is
     *          opened for reading. The {@link SecurityManager#checkWrite(String)
     *          checkWrite} method is invoked to check write access to the path
     *          if the file is opened for writing. The {@link
     *          SecurityManager#checkDelete(String) checkDelete} method is
     *          invoked to check delete access if the file is opened with the
     *          {@code DELETE_ON_CLOSE} option.
     *
     * @see java.nio.channels.FileChannel#open(Path,Set,FileAttribute[])
     * 打开或创建一个文件，返回一个可查找的字节通道以访问该文件。
    options参数确定如何打开文件。读和写选项决定是否应该为读取和/或写入打开文件。如果没有选项(或APPEND选项)，则打开文件进行读取。默认情况下，从文件的开头开始读或写。
     */
    public static SeekableByteChannel newByteChannel(Path path,
                                                     Set<? extends OpenOption> options,
                                                     FileAttribute<?>... attrs)
        throws IOException
    {
        return provider(path).newByteChannel(path, options, attrs);
    }

    /**
     * Opens or creates a file, returning a seekable byte channel to access the
     * file.
     *
     * <p> This method opens or creates a file in exactly the manner specified
     * by the {@link #newByteChannel(Path,Set,FileAttribute[]) newByteChannel}
     * method.
     *
     * @param   path
     *          the path to the file to open or create
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new seekable byte channel
     *
     * @throws  IllegalArgumentException
     *          if the set contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          if an unsupported open option is specified
     * @throws  FileAlreadyExistsException
     *          if a file of that name already exists and the {@link
     *          StandardOpenOption#CREATE_NEW CREATE_NEW} option is specified
     *          <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the path if the file is
     *          opened for reading. The {@link SecurityManager#checkWrite(String)
     *          checkWrite} method is invoked to check write access to the path
     *          if the file is opened for writing. The {@link
     *          SecurityManager#checkDelete(String) checkDelete} method is
     *          invoked to check delete access if the file is opened with the
     *          {@code DELETE_ON_CLOSE} option.
     *
     * @see java.nio.channels.FileChannel#open(Path,OpenOption[])
     *
    打开或创建一个文件，返回一个可查找的字节通道以访问该文件。
    此方法以newByteChannel方法指定的方式打开或创建文件。
     */
    public static SeekableByteChannel newByteChannel(Path path, OpenOption... options)
        throws IOException
    {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return newByteChannel(path, set);
    }

    // -- Directories --

    private static class AcceptAllFilter
        implements DirectoryStream.Filter<Path>
    {
        private AcceptAllFilter() { }

        @Override
        public boolean accept(Path entry) { return true; }

        static final AcceptAllFilter FILTER = new AcceptAllFilter();
    }

    /**
     * Opens a directory, returning a {@link DirectoryStream} to iterate over
     * all entries in the directory. The elements returned by the directory
     * stream's {@link DirectoryStream#iterator iterator} are of type {@code
     * Path}, each one representing an entry in the directory. The {@code Path}
     * objects are obtained as if by {@link Path#resolve(Path) resolving} the
     * name of the directory entry against {@code dir}.
     *
     * <p> When not using the try-with-resources construct, then directory
     * stream's {@code close} method should be invoked after iteration is
     * completed so as to free any resources held for the open directory.
     *
     * <p> When an implementation supports operations on entries in the
     * directory that execute in a race-free manner then the returned directory
     * stream is a {@link SecureDirectoryStream}.
     *
     * @param   dir
     *          the path to the directory
     *
     * @return  a new and open {@code DirectoryStream} object
     *
     * @throws  NotDirectoryException
     *          if the file could not otherwise be opened because it is not
     *          a directory <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the directory.
     *          打开一个目录，返回一个DirectoryStream来迭代目录中的所有条目。目录流的迭代器返回的元素是类型路径，每一个都表示目录中的一个条目。路径对象是通过解析目录条目的名称来获得的。
    当不使用带有资源的try构造时，应该在迭代完成后调用目录流的close方法，以便释放为open目录保存的任何资源。
    当实现支持以无竞争方式执行目录中的条目的操作时，返回的目录流就是SecureDirectoryStream。
     */
    public static DirectoryStream<Path> newDirectoryStream(Path dir)
        throws IOException
    {
        return provider(dir).newDirectoryStream(dir, AcceptAllFilter.FILTER);
    }

    /**
     * Opens a directory, returning a {@link DirectoryStream} to iterate over
     * the entries in the directory. The elements returned by the directory
     * stream's {@link DirectoryStream#iterator iterator} are of type {@code
     * Path}, each one representing an entry in the directory. The {@code Path}
     * objects are obtained as if by {@link Path#resolve(Path) resolving} the
     * name of the directory entry against {@code dir}. The entries returned by
     * the iterator are filtered by matching the {@code String} representation
     * of their file names against the given <em>globbing</em> pattern.
     *
     * <p> For example, suppose we want to iterate over the files ending with
     * ".java" in a directory:
     * <pre>
     *     Path dir = ...
     *     try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir, "*.java")) {
     *         :
     *     }
     * </pre>
     *
     * <p> The globbing pattern is specified by the {@link
     * FileSystem#getPathMatcher getPathMatcher} method.
     *
     * <p> When not using the try-with-resources construct, then directory
     * stream's {@code close} method should be invoked after iteration is
     * completed so as to free any resources held for the open directory.
     *
     * <p> When an implementation supports operations on entries in the
     * directory that execute in a race-free manner then the returned directory
     * stream is a {@link SecureDirectoryStream}.
     *
     * @param   dir
     *          the path to the directory
     * @param   glob
     *          the glob pattern
     *
     * @return  a new and open {@code DirectoryStream} object
     *
     * @throws  java.util.regex.PatternSyntaxException
     *          if the pattern is invalid
     * @throws  NotDirectoryException
     *          if the file could not otherwise be opened because it is not
     *          a directory <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the directory.
     *          打开一个目录，返回一个DirectoryStream来迭代目录中的条目。目录流的迭代器返回的元素是类型路径，每一个都表示目录中的一个条目。路径对象是通过解析针对dir的目录条目的名称来获得的。迭代器返回的条目通过将其文件名的字符串表示与给定的globbing模式匹配来进行过滤。
     */
    public static DirectoryStream<Path> newDirectoryStream(Path dir, String glob)
        throws IOException
    {
        // avoid creating a matcher if all entries are required.
        if (glob.equals("*"))
            return newDirectoryStream(dir);

        // create a matcher and return a filter that uses it.
        FileSystem fs = dir.getFileSystem();
        final PathMatcher matcher = fs.getPathMatcher("glob:" + glob);
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry)  {
                return matcher.matches(entry.getFileName());
            }
        };
        return fs.provider().newDirectoryStream(dir, filter);
    }

    /**
     * Opens a directory, returning a {@link DirectoryStream} to iterate over
     * the entries in the directory. The elements returned by the directory
     * stream's {@link DirectoryStream#iterator iterator} are of type {@code
     * Path}, each one representing an entry in the directory. The {@code Path}
     * objects are obtained as if by {@link Path#resolve(Path) resolving} the
     * name of the directory entry against {@code dir}. The entries returned by
     * the iterator are filtered by the given {@link DirectoryStream.Filter
     * filter}.
     *
     * <p> When not using the try-with-resources construct, then directory
     * stream's {@code close} method should be invoked after iteration is
     * completed so as to free any resources held for the open directory.
     *
     * <p> Where the filter terminates due to an uncaught error or runtime
     * exception then it is propagated to the {@link Iterator#hasNext()
     * hasNext} or {@link Iterator#next() next} method. Where an {@code
     * IOException} is thrown, it results in the {@code hasNext} or {@code
     * next} method throwing a {@link DirectoryIteratorException} with the
     * {@code IOException} as the cause.
     *
     * <p> When an implementation supports operations on entries in the
     * directory that execute in a race-free manner then the returned directory
     * stream is a {@link SecureDirectoryStream}.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to iterate over the files in a directory that are
     * larger than 8K.
     * <pre>
     *     DirectoryStream.Filter&lt;Path&gt; filter = new DirectoryStream.Filter&lt;Path&gt;() {
     *         public boolean accept(Path file) throws IOException {
     *             return (Files.size(file) &gt; 8192L);
     *         }
     *     };
     *     Path dir = ...
     *     try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir, filter)) {
     *         :
     *     }
     * </pre>
     *
     * @param   dir
     *          the path to the directory
     * @param   filter
     *          the directory stream filter
     *
     * @return  a new and open {@code DirectoryStream} object
     *
     * @throws  NotDirectoryException
     *          if the file could not otherwise be opened because it is not
     *          a directory <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the directory.
     *          打开一个目录，返回一个DirectoryStream来迭代目录中的条目。目录流的迭代器返回的元素是类型路径，每一个都表示目录中的一个条目。路径对象是通过解析针对dir的目录条目的名称来获得的。迭代器返回的条目由给定的过滤器过滤。
    当不使用带有资源的try构造时，应该在迭代完成后调用目录流的close方法，以便释放为open目录保存的任何资源。
    如果过滤器因未捕获的错误或运行时异常而终止，那么它将传播到hasNext或next方法。如果抛出IOException，则会导致hasNext或next方法抛出一个DirectoryIteratorException，并将IOException作为原因。
    当实现支持以无竞争方式执行目录中的条目的操作时，返回的目录流就是SecureDirectoryStream。
     */
    public static DirectoryStream<Path> newDirectoryStream(Path dir,
                                                           DirectoryStream.Filter<? super Path> filter)
        throws IOException
    {
        return provider(dir).newDirectoryStream(dir, filter);
    }

    // -- Creation and deletion --

    /**
     * Creates a new and empty file, failing if the file already exists. The
     * check for the existence of the file and the creation of the new file if
     * it does not exist are a single operation that is atomic with respect to
     * all other filesystem activities that might affect the directory.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * file-attributes} to set atomically when creating the file. Each attribute
     * is identified by its {@link FileAttribute#name name}. If more than one
     * attribute of the same name is included in the array then all but the last
     * occurrence is ignored.
     *
     * @param   path
     *          the path to the file to create
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  the file
     *
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the file
     * @throws  FileAlreadyExistsException
     *          if a file of that name already exists
     *          <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs or the parent directory does not exist
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the new file.
     *          创建一个新的空文件，如果该文件已经存在，则失败。检查文件是否存在以及是否创建新文件(如果不存在的话)是对可能影响目录的所有其他文件系统活动进行原子化的单个操作。
    attrs参数是可选的文件属性，可在创建文件时自动设置。每个属性都由其名称标识。如果在数组中包含多个相同名称的属性，则除最后一个以外的所有属性都将被忽略。
     */
    public static Path createFile(Path path, FileAttribute<?>... attrs)
        throws IOException
    {
        EnumSet<StandardOpenOption> options =
            EnumSet.<StandardOpenOption>of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        newByteChannel(path, options, attrs).close();
        return path;
    }

    /**
     * Creates a new directory. The check for the existence of the file and the
     * creation of the directory if it does not exist are a single operation
     * that is atomic with respect to all other filesystem activities that might
     * affect the directory. The {@link #createDirectories createDirectories}
     * method should be used where it is required to create all nonexistent
     * parent directories first.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * file-attributes} to set atomically when creating the directory. Each
     * attribute is identified by its {@link FileAttribute#name name}. If more
     * than one attribute of the same name is included in the array then all but
     * the last occurrence is ignored.
     *
     * @param   dir
     *          the directory to create
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the directory
     *
     * @return  the directory
     *
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the directory
     * @throws  FileAlreadyExistsException
     *          if a directory could not otherwise be created because a file of
     *          that name already exists <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs or the parent directory does not exist
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the new directory.
     *          创建一个新的目录。检查文件是否存在以及是否创建目录(如果不存在的话)是对所有可能影响目录的其他文件系统活动进行原子化的单个操作。在需要首先创建所有不存在的父目录时，应该使用createdirectory方法。
    attrs参数是可选的文件属性，可以在创建目录时自动设置。每个属性都由其名称标识。如果在数组中包含多个相同名称的属性，则除最后一个以外的所有属性都将被忽略。
     */
    public static Path createDirectory(Path dir, FileAttribute<?>... attrs)
        throws IOException
    {
        provider(dir).createDirectory(dir, attrs);
        return dir;
    }

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     * Unlike the {@link #createDirectory createDirectory} method, an exception
     * is not thrown if the directory could not be created because it already
     * exists.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * file-attributes} to set atomically when creating the nonexistent
     * directories. Each file attribute is identified by its {@link
     * FileAttribute#name name}. If more than one attribute of the same name is
     * included in the array then all but the last occurrence is ignored.
     *
     * <p> If this method fails, then it may do so after creating some, but not
     * all, of the parent directories.
     *
     * @param   dir
     *          the directory to create
     *
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the directory
     *
     * @return  the directory
     *
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the directory
     * @throws  FileAlreadyExistsException
     *          if {@code dir} exists but is not a directory <i>(optional specific
     *          exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          in the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked prior to attempting to create a directory and
     *          its {@link SecurityManager#checkRead(String) checkRead} is
     *          invoked for each parent directory that is checked. If {@code
     *          dir} is not an absolute path then its {@link Path#toAbsolutePath
     *          toAbsolutePath} may need to be invoked to get its absolute path.
     *          This may invoke the security manager's {@link
     *          SecurityManager#checkPropertyAccess(String) checkPropertyAccess}
     *          method to check access to the system property {@code user.dir}
     *          通过首先创建所有不存在的父目录来创建一个目录。与createDirectory方法不同，如果无法创建目录，则不会抛出异常，因为该目录已经存在。
    attrs参数是可选的文件属性，可在创建不存在的目录时自动设置。每个文件属性都由其名称标识。如果在数组中包含多个相同名称的属性，则除最后一个以外的所有属性都将被忽略。
    如果该方法失败，那么它可能在创建一些父目录(但不是全部)之后这样做。
     */
    public static Path createDirectories(Path dir, FileAttribute<?>... attrs)
        throws IOException
    {
        // attempt to create the directory
        try {
            createAndCheckIsDirectory(dir, attrs);
            return dir;
        } catch (FileAlreadyExistsException x) {
            // file exists and is not a directory
            throw x;
        } catch (IOException x) {
            // parent may not exist or other reason
        }
        SecurityException se = null;
        try {
            dir = dir.toAbsolutePath();
        } catch (SecurityException x) {
            // don't have permission to get absolute path
            se = x;
        }
        // find a decendent that exists
        Path parent = dir.getParent();
        while (parent != null) {
            try {
                provider(parent).checkAccess(parent);
                break;
            } catch (NoSuchFileException x) {
                // does not exist
            }
            parent = parent.getParent();
        }
        if (parent == null) {
            // unable to find existing parent
            if (se == null) {
                throw new FileSystemException(dir.toString(), null,
                    "Unable to determine if root directory exists");
            } else {
                throw se;
            }
        }

        // create directories
        Path child = parent;
        for (Path name: parent.relativize(dir)) {
            child = child.resolve(name);
            createAndCheckIsDirectory(child, attrs);
        }
        return dir;
    }

    /**
     * Used by createDirectories to attempt to create a directory. A no-op
     * if the directory already exists.用于创建目录以尝试创建目录。如果目录已经存在，则为no-op。
     */
    private static void createAndCheckIsDirectory(Path dir,
                                                  FileAttribute<?>... attrs)
        throws IOException
    {
        try {
            createDirectory(dir, attrs);
        } catch (FileAlreadyExistsException x) {
            if (!isDirectory(dir, LinkOption.NOFOLLOW_LINKS))
                throw x;
        }
    }

    /**
     * Creates a new empty file in the specified directory, using the given
     * prefix and suffix strings to generate its name. The resulting
     * {@code Path} is associated with the same {@code FileSystem} as the given
     * directory.
     *
     * <p> The details as to how the name of the file is constructed is
     * implementation dependent and therefore not specified. Where possible
     * the {@code prefix} and {@code suffix} are used to construct candidate
     * names in the same manner as the {@link
     * java.io.File#createTempFile(String,String,File)} method.
     *
     * <p> As with the {@code File.createTempFile} methods, this method is only
     * part of a temporary-file facility. Where used as a <em>work files</em>,
     * the resulting file may be opened using the {@link
     * StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} option so that the
     * file is deleted when the appropriate {@code close} method is invoked.
     * Alternatively, a {@link Runtime#addShutdownHook shutdown-hook}, or the
     * {@link java.io.File#deleteOnExit} mechanism may be used to delete the
     * file automatically.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * file-attributes} to set atomically when creating the file. Each attribute
     * is identified by its {@link FileAttribute#name name}. If more than one
     * attribute of the same name is included in the array then all but the last
     * occurrence is ignored. When no file attributes are specified, then the
     * resulting file may have more restrictive access permissions to files
     * created by the {@link java.io.File#createTempFile(String,String,File)}
     * method.
     *
     * @param   dir
     *          the path to directory in which to create the file
     * @param   prefix
     *          the prefix string to be used in generating the file's name;
     *          may be {@code null}
     * @param   suffix
     *          the suffix string to be used in generating the file's name;
     *          may be {@code null}, in which case "{@code .tmp}" is used
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  the path to the newly created file that did not exist before
     *          this method was invoked
     *
     * @throws  IllegalArgumentException
     *          if the prefix or suffix parameters cannot be used to generate
     *          a candidate file name
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the directory
     * @throws  IOException
     *          if an I/O error occurs or {@code dir} does not exist
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *          在指定的目录中创建一个新的空文件，使用给定的前缀和后缀字符串来生成它的名称。结果路径与给定目录的相同文件系统相关联。
    关于如何构造文件名称的细节与实现有关，因此没有指定。在可能的情况下，前缀和后缀用于以与文件相同的方式构造候选名称。createTempFile(字符串,字符串、文件)方法。
    与文件。createTempFile方法，这个方法只是临时文件工具的一部分。在用作工作文件的地方，可以使用DELETE_ON_CLOSE选项打开结果文件，以便在调用适当的close方法时删除该文件。或者，可以使用shutdownhook或File.deleteOnExit机制来自动删除文件。
    attrs参数是可选的文件属性，可在创建文件时自动设置。每个属性都由其名称标识。如果在数组中包含了相同名称的多个属性，那么除了最后一个事件之外，所有属性都将被忽略。当没有指定文件属性时，结果文件可能对文件创建的文件具有更严格的访问权限。createTempFile(字符串,字符串、文件)方法。
     */
    public static Path createTempFile(Path dir,
                                      String prefix,
                                      String suffix,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempFile(Objects.requireNonNull(dir),
                                             prefix, suffix, attrs);
    }

    /**
     * Creates an empty file in the default temporary-file directory, using
     * the given prefix and suffix to generate its name. The resulting {@code
     * Path} is associated with the default {@code FileSystem}.
     *
     * <p> This method works in exactly the manner specified by the
     * {@link #createTempFile(Path,String,String,FileAttribute[])} method for
     * the case that the {@code dir} parameter is the temporary-file directory.
     *
     * @param   prefix
     *          the prefix string to be used in generating the file's name;
     *          may be {@code null}
     * @param   suffix
     *          the suffix string to be used in generating the file's name;
     *          may be {@code null}, in which case "{@code .tmp}" is used
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  the path to the newly created file that did not exist before
     *          this method was invoked
     *
     * @throws  IllegalArgumentException
     *          if the prefix or suffix parameters cannot be used to generate
     *          a candidate file name
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the directory
     * @throws  IOException
     *          if an I/O error occurs or the temporary-file directory does not
     *          exist
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *          在默认的临时文件目录中创建一个空文件，使用给定的前缀和后缀生成它的名称。生成的路径与默认文件系统相关联。
    此方法的工作方式与createTempFile(路径、字符串、字符串、FileAttribute[])方法指定的方式完全相同，如果dir参数是临时文件目录。
     */
    public static Path createTempFile(String prefix,
                                      String suffix,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempFile(null, prefix, suffix, attrs);
    }

    /**
     * Creates a new directory in the specified directory, using the given
     * prefix to generate its name.  The resulting {@code Path} is associated
     * with the same {@code FileSystem} as the given directory.
     *
     * <p> The details as to how the name of the directory is constructed is
     * implementation dependent and therefore not specified. Where possible
     * the {@code prefix} is used to construct candidate names.
     *
     * <p> As with the {@code createTempFile} methods, this method is only
     * part of a temporary-file facility. A {@link Runtime#addShutdownHook
     * shutdown-hook}, or the {@link java.io.File#deleteOnExit} mechanism may be
     * used to delete the directory automatically.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * file-attributes} to set atomically when creating the directory. Each
     * attribute is identified by its {@link FileAttribute#name name}. If more
     * than one attribute of the same name is included in the array then all but
     * the last occurrence is ignored.
     *
     * @param   dir
     *          the path to directory in which to create the directory
     * @param   prefix
     *          the prefix string to be used in generating the directory's name;
     *          may be {@code null}
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the directory
     *
     * @return  the path to the newly created directory that did not exist before
     *          this method was invoked
     *
     * @throws  IllegalArgumentException
     *          if the prefix cannot be used to generate a candidate directory name
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the directory
     * @throws  IOException
     *          if an I/O error occurs or {@code dir} does not exist
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access when creating the
     *          directory.
     *          使用给定的前缀生成指定目录中的新目录名。结果路径与给定目录的相同文件系统相关联。
    关于如何构造目录的名称的细节与实现有关，因此没有指定。在可能的情况下，前缀用于构造候选名称。
    与createTempFile方法一样，该方法只是临时文件工具的一部分。可以使用shutdown-hook或File.deleteOnExit机制自动删除目录。
    attrs参数是可选的文件属性，可以在创建目录时自动设置。每个属性都由其名称标识。如果在数组中包含多个相同名称的属性，则除最后一个以外的所有属性都将被忽略。
     */
    public static Path createTempDirectory(Path dir,
                                           String prefix,
                                           FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempDirectory(Objects.requireNonNull(dir),
                                                  prefix, attrs);
    }

    /**
     * Creates a new directory in the default temporary-file directory, using
     * the given prefix to generate its name. The resulting {@code Path} is
     * associated with the default {@code FileSystem}.
     *
     * <p> This method works in exactly the manner specified by {@link
     * #createTempDirectory(Path,String,FileAttribute[])} method for the case
     * that the {@code dir} parameter is the temporary-file directory.
     *
     * @param   prefix
     *          the prefix string to be used in generating the directory's name;
     *          may be {@code null}
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the directory
     *
     * @return  the path to the newly created directory that did not exist before
     *          this method was invoked
     *
     * @throws  IllegalArgumentException
     *          if the prefix cannot be used to generate a candidate directory name
     * @throws  UnsupportedOperationException
     *          if the array contains an attribute that cannot be set atomically
     *          when creating the directory
     * @throws  IOException
     *          if an I/O error occurs or the temporary-file directory does not
     *          exist
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access when creating the
     *          directory.
     *          在默认的临时文件目录中创建一个新目录，使用给定的前缀生成它的名称。生成的路径与默认文件系统相关联。
    此方法的工作方式与createTempDirectory(路径、字符串、FileAttribute[])方法指定的方式一致，该方法适用于dir参数是临时文件目录的情况。
     */
    public static Path createTempDirectory(String prefix,
                                           FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempDirectory(null, prefix, attrs);
    }

    /**
     * Creates a symbolic link to a target <i>(optional operation)</i>.
     *
     * <p> The {@code target} parameter is the target of the link. It may be an
     * {@link Path#isAbsolute absolute} or relative path and may not exist. When
     * the target is a relative path then file system operations on the resulting
     * link are relative to the path of the link.
     *
     * <p> The {@code attrs} parameter is optional {@link FileAttribute
     * attributes} to set atomically when creating the link. Each attribute is
     * identified by its {@link FileAttribute#name name}. If more than one attribute
     * of the same name is included in the array then all but the last occurrence
     * is ignored.
     *
     * <p> Where symbolic links are supported, but the underlying {@link FileStore}
     * does not support symbolic links, then this may fail with an {@link
     * IOException}. Additionally, some operating systems may require that the
     * Java virtual machine be started with implementation specific privileges to
     * create symbolic links, in which case this method may throw {@code IOException}.
     *
     * @param   link
     *          the path of the symbolic link to create
     * @param   target
     *          the target of the symbolic link
     * @param   attrs
     *          the array of attributes to set atomically when creating the
     *          symbolic link
     *
     * @return  the path to the symbolic link
     *
     * @throws  UnsupportedOperationException
     *          if the implementation does not support symbolic links or the
     *          array contains an attribute that cannot be set atomically when
     *          creating the symbolic link
     * @throws  FileAlreadyExistsException
     *          if a file with the name already exists <i>(optional specific
     *          exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager
     *          is installed, it denies {@link LinkPermission}<tt>("symbolic")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the path of the symbolic link.
     *          创建到目标的符号链接(可选操作)。
    目标参数是链接的目标。它可能是绝对的或相对的路径，也可能不存在。当目标是相对路径时，结果链接上的文件系统操作相对于链接的路径。
    attrs参数是可选属性，可在创建链接时自动设置。每个属性都由其名称标识。如果在数组中包含多个相同名称的属性，则除最后一个以外的所有属性都将被忽略。
    如果支持符号链接，但底层文件存储不支持符号链接，那么可能会在IOException中失败。此外，一些操作系统可能需要使用实现特定的特权启动Java虚拟机来创建符号链接，在这种情况下，该方法可能抛出IOException。
     */
    public static Path createSymbolicLink(Path link, Path target,
                                          FileAttribute<?>... attrs)
        throws IOException
    {
        provider(link).createSymbolicLink(link, target, attrs);
        return link;
    }

    /**
     * Creates a new link (directory entry) for an existing file <i>(optional
     * operation)</i>.
     *
     * <p> The {@code link} parameter locates the directory entry to create.
     * The {@code existing} parameter is the path to an existing file. This
     * method creates a new directory entry for the file so that it can be
     * accessed using {@code link} as the path. On some file systems this is
     * known as creating a "hard link". Whether the file attributes are
     * maintained for the file or for each directory entry is file system
     * specific and therefore not specified. Typically, a file system requires
     * that all links (directory entries) for a file be on the same file system.
     * Furthermore, on some platforms, the Java virtual machine may require to
     * be started with implementation specific privileges to create hard links
     * or to create links to directories.
     *
     * @param   link
     *          the link (directory entry) to create
     * @param   existing
     *          a path to an existing file
     *
     * @return  the path to the link (directory entry)
     *
     * @throws  UnsupportedOperationException
     *          if the implementation does not support adding an existing file
     *          to a directory
     * @throws  FileAlreadyExistsException
     *          if the entry could not otherwise be created because a file of
     *          that name already exists <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager
     *          is installed, it denies {@link LinkPermission}<tt>("hard")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to either the link or the
     *          existing file.
     *          为现有文件创建一个新的链接(目录项)(可选操作)。
    链接参数定位要创建的目录项。现有参数是现有文件的路径。此方法为文件创建一个新的目录条目，以便可以使用链接作为路径访问它。在某些文件系统中，这被称为创建“硬链接”。文件属性是否为文件维护，或者每个目录条目是文件系统特定的，因此没有指定。通常，文件系统要求文件的所有链接(目录项)都位于同一个文件系统上。此外，在某些平台上，Java虚拟机可能需要使用实现特定的特权来创建硬链接或创建到目录的链接。
     */
    public static Path createLink(Path link, Path existing) throws IOException {
        provider(link).createLink(link, existing);
        return link;
    }

    /**
     * Deletes a file.
     *
     * <p> An implementation may require to examine the file to determine if the
     * file is a directory. Consequently this method may not be atomic with respect
     * to other file system operations.  If the file is a symbolic link then the
     * symbolic link itself, not the final target of the link, is deleted.
     *
     * <p> If the file is a directory then the directory must be empty. In some
     * implementations a directory has entries for special files or links that
     * are created when the directory is created. In such implementations a
     * directory is considered empty when only the special entries exist.
     * This method can be used with the {@link #walkFileTree walkFileTree}
     * method to delete a directory and all entries in the directory, or an
     * entire <i>file-tree</i> where required.
     *
     * <p> On some operating systems it may not be possible to remove a file when
     * it is open and in use by this Java virtual machine or other programs.
     *
     * @param   path
     *          the path to the file to delete
     *
     * @throws  NoSuchFileException
     *          if the file does not exist <i>(optional specific exception)</i>
     * @throws  DirectoryNotEmptyException
     *          if the file is a directory and could not otherwise be deleted
     *          because the directory is not empty <i>(optional specific
     *          exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkDelete(String)} method
     *          is invoked to check delete access to the file
     *          删除一个文件。
    实现可能需要检查文件以确定该文件是否是目录。因此，对于其他文件系统操作，此方法可能不具有原子性。如果文件是一个符号链接，则删除符号链接本身，而不是链接的最终目标。
    如果该文件是一个目录，那么该目录必须为空。在某些实现中，目录包含在创建目录时创建的特殊文件或链接的条目。在这种实现中，当只有特殊条目存在时，目录被认为是空的。这个方法可以使用walkFileTree方法来删除目录和目录中的所有条目，或者在需要的地方删除整个文件树。
    在某些操作系统中，当文件打开并被此Java虚拟机或其他程序使用时，可能无法删除它。
     */
    public static void delete(Path path) throws IOException {
        provider(path).delete(path);
    }

    /**
     * Deletes a file if it exists.
     *
     * <p> As with the {@link #delete(Path) delete(Path)} method, an
     * implementation may need to examine the file to determine if the file is a
     * directory. Consequently this method may not be atomic with respect to
     * other file system operations.  If the file is a symbolic link, then the
     * symbolic link itself, not the final target of the link, is deleted.
     *
     * <p> If the file is a directory then the directory must be empty. In some
     * implementations a directory has entries for special files or links that
     * are created when the directory is created. In such implementations a
     * directory is considered empty when only the special entries exist.
     *
     * <p> On some operating systems it may not be possible to remove a file when
     * it is open and in use by this Java virtual machine or other programs.
     *
     * @param   path
     *          the path to the file to delete
     *
     * @return  {@code true} if the file was deleted by this method; {@code
     *          false} if the file could not be deleted because it did not
     *          exist
     *
     * @throws  DirectoryNotEmptyException
     *          if the file is a directory and could not otherwise be deleted
     *          because the directory is not empty <i>(optional specific
     *          exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkDelete(String)} method
     *          is invoked to check delete access to the file.
     *          如果文件存在，则删除它。
    与delete(Path)方法一样，实现可能需要检查文件以确定文件是否是目录。因此，对于其他文件系统操作，此方法可能不具有原子性。如果文件是一个符号链接，则删除符号链接本身，而不是链接的最终目标。
    如果该文件是一个目录，那么该目录必须为空。在某些实现中，目录包含在创建目录时创建的特殊文件或链接的条目。在这种实现中，只有特殊条目存在时，目录被认为是空的。
    在某些操作系统中，当文件打开并被此Java虚拟机或其他程序使用时，可能无法删除它。
     */
    public static boolean deleteIfExists(Path path) throws IOException {
        return provider(path).deleteIfExists(path);
    }

    // -- Copying and moving files --

    /**
     * Copy a file to a target file.
     *
     * <p> This method copies a file to the target file with the {@code
     * options} parameter specifying how the copy is performed. By default, the
     * copy fails if the target file already exists or is a symbolic link,
     * except if the source and target are the {@link #isSameFile same} file, in
     * which case the method completes without copying the file. File attributes
     * are not required to be copied to the target file. If symbolic links are
     * supported, and the file is a symbolic link, then the final target of the
     * link is copied. If the file is a directory then it creates an empty
     * directory in the target location (entries in the directory are not
     * copied). This method can be used with the {@link #walkFileTree
     * walkFileTree} method to copy a directory and all entries in the directory,
     * or an entire <i>file-tree</i> where required.
     *
     * <p> The {@code options} parameter may include any of the following:
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>Option</th> <th>Description</th> </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} </td>
     *   <td> If the target file exists, then the target file is replaced if it
     *     is not a non-empty directory. If the target file exists and is a
     *     symbolic link, then the symbolic link itself, not the target of
     *     the link, is replaced. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#COPY_ATTRIBUTES COPY_ATTRIBUTES} </td>
     *   <td> Attempts to copy the file attributes associated with this file to
     *     the target file. The exact file attributes that are copied is platform
     *     and file system dependent and therefore unspecified. Minimally, the
     *     {@link BasicFileAttributes#lastModifiedTime last-modified-time} is
     *     copied to the target file if supported by both the source and target
     *     file stores. Copying of file timestamps may result in precision
     *     loss. </td>
     * </tr>
     * <tr>
     *   <td> {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} </td>
     *   <td> Symbolic links are not followed. If the file is a symbolic link,
     *     then the symbolic link itself, not the target of the link, is copied.
     *     It is implementation specific if file attributes can be copied to the
     *     new link. In other words, the {@code COPY_ATTRIBUTES} option may be
     *     ignored when copying a symbolic link. </td>
     * </tr>
     * </table>
     *
     * <p> An implementation of this interface may support additional
     * implementation specific options.
     *
     * <p> Copying a file is not an atomic operation. If an {@link IOException}
     * is thrown, then it is possible that the target file is incomplete or some
     * of its file attributes have not been copied from the source file. When
     * the {@code REPLACE_EXISTING} option is specified and the target file
     * exists, then the target file is replaced. The check for the existence of
     * the file and the creation of the new file may not be atomic with respect
     * to other file system activities.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to copy a file into a directory, giving it the same file
     * name as the source file:
     * <pre>
     *     Path source = ...
     *     Path newdir = ...
     *     Files.copy(source, newdir.resolve(source.getFileName());
     * </pre>
     *
     * @param   source
     *          the path to the file to copy
     * @param   target
     *          the path to the target file (may be associated with a different
     *          provider to the source path)
     * @param   options
     *          options specifying how the copy should be done
     *
     * @return  the path to the target file
     *
     * @throws  UnsupportedOperationException
     *          if the array contains a copy option that is not supported
     * @throws  FileAlreadyExistsException
     *          if the target file exists but cannot be replaced because the
     *          {@code REPLACE_EXISTING} option is not specified <i>(optional
     *          specific exception)</i>
     * @throws  DirectoryNotEmptyException
     *          the {@code REPLACE_EXISTING} option is specified but the file
     *          cannot be replaced because it is a non-empty directory
     *          <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the source file, the
     *          {@link SecurityManager#checkWrite(String) checkWrite} is invoked
     *          to check write access to the target file. If a symbolic link is
     *          copied the security manager is invoked to check {@link
     *          LinkPermission}{@code ("symbolic")}.
     *          将文件复制到目标文件。
    此方法将文件复制到目标文件，并使用options参数指定如何执行复制。默认情况下，如果目标文件已经存在或者是一个符号链接，复制将失败，除非源文件和目标文件是相同的文件，在这种情况下，方法在不复制文件的情况下完成复制。不需要将文件属性复制到目标文件。如果支持符号链接，并且文件是符号链接，那么将复制链接的最终目标。如果文件是一个目录，那么它会在目标位置创建一个空目录(目录中的条目不会被复制)。此方法可与walkFileTree方法一起使用，以复制目录中的目录和所有条目，或在需要时复制整个文件树。
     */
    public static Path copy(Path source, Path target, CopyOption... options)
        throws IOException
    {
        FileSystemProvider provider = provider(source);
        if (provider(target) == provider) {
            // same provider
            provider.copy(source, target, options);
        } else {
            // different providers
            CopyMoveHelper.copyToForeignTarget(source, target, options);
        }
        return target;
    }

    /**
     * Move or rename a file to a target file.
     *
     * <p> By default, this method attempts to move the file to the target
     * file, failing if the target file exists except if the source and
     * target are the {@link #isSameFile same} file, in which case this method
     * has no effect. If the file is a symbolic link then the symbolic link
     * itself, not the target of the link, is moved. This method may be
     * invoked to move an empty directory. In some implementations a directory
     * has entries for special files or links that are created when the
     * directory is created. In such implementations a directory is considered
     * empty when only the special entries exist. When invoked to move a
     * directory that is not empty then the directory is moved if it does not
     * require moving the entries in the directory.  For example, renaming a
     * directory on the same {@link FileStore} will usually not require moving
     * the entries in the directory. When moving a directory requires that its
     * entries be moved then this method fails (by throwing an {@code
     * IOException}). To move a <i>file tree</i> may involve copying rather
     * than moving directories and this can be done using the {@link
     * #copy copy} method in conjunction with the {@link
     * #walkFileTree Files.walkFileTree} utility method.
     *
     * <p> The {@code options} parameter may include any of the following:
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>Option</th> <th>Description</th> </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} </td>
     *   <td> If the target file exists, then the target file is replaced if it
     *     is not a non-empty directory. If the target file exists and is a
     *     symbolic link, then the symbolic link itself, not the target of
     *     the link, is replaced. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#ATOMIC_MOVE ATOMIC_MOVE} </td>
     *   <td> The move is performed as an atomic file system operation and all
     *     other options are ignored. If the target file exists then it is
     *     implementation specific if the existing file is replaced or this method
     *     fails by throwing an {@link IOException}. If the move cannot be
     *     performed as an atomic file system operation then {@link
     *     AtomicMoveNotSupportedException} is thrown. This can arise, for
     *     example, when the target location is on a different {@code FileStore}
     *     and would require that the file be copied, or target location is
     *     associated with a different provider to this object. </td>
     * </table>
     *
     * <p> An implementation of this interface may support additional
     * implementation specific options.
     *
     * <p> Moving a file will copy the {@link
     * BasicFileAttributes#lastModifiedTime last-modified-time} to the target
     * file if supported by both source and target file stores. Copying of file
     * timestamps may result in precision loss. An implementation may also
     * attempt to copy other file attributes but is not required to fail if the
     * file attributes cannot be copied. When the move is performed as
     * a non-atomic operation, and an {@code IOException} is thrown, then the
     * state of the files is not defined. The original file and the target file
     * may both exist, the target file may be incomplete or some of its file
     * attributes may not been copied from the original file.
     *
     * <p> <b>Usage Examples:</b>
     * Suppose we want to rename a file to "newname", keeping the file in the
     * same directory:
     * <pre>
     *     Path source = ...
     *     Files.move(source, source.resolveSibling("newname"));
     * </pre>
     * Alternatively, suppose we want to move a file to new directory, keeping
     * the same file name, and replacing any existing file of that name in the
     * directory:
     * <pre>
     *     Path source = ...
     *     Path newdir = ...
     *     Files.move(source, newdir.resolve(source.getFileName()), REPLACE_EXISTING);
     * </pre>
     *
     * @param   source
     *          the path to the file to move
     * @param   target
     *          the path to the target file (may be associated with a different
     *          provider to the source path)
     * @param   options
     *          options specifying how the move should be done
     *
     * @return  the path to the target file
     *
     * @throws  UnsupportedOperationException
     *          if the array contains a copy option that is not supported
     * @throws  FileAlreadyExistsException
     *          if the target file exists but cannot be replaced because the
     *          {@code REPLACE_EXISTING} option is not specified <i>(optional
     *          specific exception)</i>
     * @throws  DirectoryNotEmptyException
     *          the {@code REPLACE_EXISTING} option is specified but the file
     *          cannot be replaced because it is a non-empty directory
     *          <i>(optional specific exception)</i>
     * @throws  AtomicMoveNotSupportedException
     *          if the options array contains the {@code ATOMIC_MOVE} option but
     *          the file cannot be moved as an atomic file system operation.
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to both the source and
     *          target file.
     *          将文件移动或重命名为目标文件。
    默认情况下，此方法尝试将文件移动到目标文件，如果目标文件存在，则失败，除非源文件和目标文件是相同的文件，在这种情况下，此方法无效。如果文件是一个符号链接，则移动符号链接本身，而不是链接的目标。可以调用此方法来移动一个空目录。在某些实现中，目录包含在创建目录时创建的特殊文件或链接的条目。在这种实现中，当只有特殊条目存在时，目录被认为是空的。当调用该目录来移动一个非空的目录时，如果不需要移动目录中的条目，那么该目录将被移动。例如，重命名相同文件存储上的目录通常不需要移动目录中的条目。当移动一个目录需要移动它的条目时，这个方法就失败了(通过抛出一个IOException)。要移动文件树可能需要复制而不是移动目录，这可以使用copy方法与文件一起完成。walkFileTree实用方法。
     */
    public static Path move(Path source, Path target, CopyOption... options)
        throws IOException
    {
        FileSystemProvider provider = provider(source);
        if (provider(target) == provider) {
            // same provider
            provider.move(source, target, options);
        } else {
            // different providers
            CopyMoveHelper.moveToForeignTarget(source, target, options);
        }
        return target;
    }

    // -- Miscellenous --

    /**
     * Reads the target of a symbolic link <i>(optional operation)</i>.
     *
     * <p> If the file system supports <a href="package-summary.html#links">symbolic
     * links</a> then this method is used to read the target of the link, failing
     * if the file is not a symbolic link. The target of the link need not exist.
     * The returned {@code Path} object will be associated with the same file
     * system as {@code link}.
     *
     * @param   link
     *          the path to the symbolic link
     *
     * @return  a {@code Path} object representing the target of the link
     *
     * @throws  UnsupportedOperationException
     *          if the implementation does not support symbolic links
     * @throws  NotLinkException
     *          if the target could otherwise not be read because the file
     *          is not a symbolic link <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager
     *          is installed, it checks that {@code FilePermission} has been
     *          granted with the "{@code readlink}" action to read the link.
     *          读取符号链接的目标(可选操作)。
    如果文件系统支持符号链接，则使用此方法读取链接的目标，如果文件不是符号链接，则失败。链接的目标不需要存在。返回的路径对象将与链接的文件系统相关联。
     */
    public static Path readSymbolicLink(Path link) throws IOException {
        return provider(link).readSymbolicLink(link);
    }

    /**
     * Returns the {@link FileStore} representing the file store where a file
     * is located.
     *
     * <p> Once a reference to the {@code FileStore} is obtained it is
     * implementation specific if operations on the returned {@code FileStore},
     * or {@link FileStoreAttributeView} objects obtained from it, continue
     * to depend on the existence of the file. In particular the behavior is not
     * defined for the case that the file is deleted or moved to a different
     * file store.
     *
     * @param   path
     *          the path to the file
     *
     * @return  the file store where the file is stored
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file, and in
     *          addition it checks {@link RuntimePermission}<tt>
     *          ("getFileStoreAttributes")</tt>
     *          返回文件存储区，该文件存储区表示文件所在的文件存储区。
    一旦获得了对FileStore的引用，就会在返回的FileStore上执行特定的操作，或者从它获得的FileStoreAttributeView对象，继续依赖于文件的存在。特别是当文件被删除或移动到不同的文件存储时，没有定义该行为。
     */
    public static FileStore getFileStore(Path path) throws IOException {
        return provider(path).getFileStore(path);
    }

    /**
     * Tests if two paths locate the same file.
     *
     * <p> If both {@code Path} objects are {@link Path#equals(Object) equal}
     * then this method returns {@code true} without checking if the file exists.
     * If the two {@code Path} objects are associated with different providers
     * then this method returns {@code false}. Otherwise, this method checks if
     * both {@code Path} objects locate the same file, and depending on the
     * implementation, may require to open or access both files.
     *
     * <p> If the file system and files remain static, then this method implements
     * an equivalence relation for non-null {@code Paths}.
     * <ul>
     * <li>It is <i>reflexive</i>: for {@code Path} {@code f},
     *     {@code isSameFile(f,f)} should return {@code true}.
     * <li>It is <i>symmetric</i>: for two {@code Paths} {@code f} and {@code g},
     *     {@code isSameFile(f,g)} will equal {@code isSameFile(g,f)}.
     * <li>It is <i>transitive</i>: for three {@code Paths}
     *     {@code f}, {@code g}, and {@code h}, if {@code isSameFile(f,g)} returns
     *     {@code true} and {@code isSameFile(g,h)} returns {@code true}, then
     *     {@code isSameFile(f,h)} will return return {@code true}.
     * </ul>
     *
     * @param   path
     *          one path to the file
     * @param   path2
     *          the other path
     *
     * @return  {@code true} if, and only if, the two paths locate the same file
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to both files.
     *
     * @see java.nio.file.attribute.BasicFileAttributes#fileKey
     * 测试两个路径是否定位同一文件。
    如果两个路径对象都相等，那么该方法返回true，而不检查文件是否存在。如果两个路径对象与不同的提供程序相关联，则此方法返回false。否则，此方法将检查两个路径对象是否位于同一个文件，并且根据实现的不同，可能需要打开或访问两个文件。
    如果文件系统和文件保持静态，那么该方法对非空路径实现等价关系。
     */
    public static boolean isSameFile(Path path, Path path2) throws IOException {
        return provider(path).isSameFile(path, path2);
    }

    /**
     * Tells whether or not a file is considered <em>hidden</em>. The exact
     * definition of hidden is platform or provider dependent. On UNIX for
     * example a file is considered to be hidden if its name begins with a
     * period character ('.'). On Windows a file is considered hidden if it
     * isn't a directory and the DOS {@link DosFileAttributes#isHidden hidden}
     * attribute is set.
     *
     * <p> Depending on the implementation this method may require to access
     * the file system to determine if the file is considered hidden.
     *
     * @param   path
     *          the path to the file to test
     *
     * @return  {@code true} if the file is considered hidden
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *          告诉文件是否被认为是隐藏的。隐藏的确切定义依赖于平台或提供者。例如，在UNIX上，如果一个文件的名称以一个句号字符('.')开头，那么它就被认为是隐藏的。在Windows上，如果文件不是目录，并且设置了DOS隐藏属性，则认为它是隐藏的。
    根据实现的不同，此方法可能需要访问文件系统以确定文件是否被认为是隐藏的。
     */
    public static boolean isHidden(Path path) throws IOException {
        return provider(path).isHidden(path);
    }

    // lazy loading of default and installed file type detectors
    private static class FileTypeDetectors{
        static final FileTypeDetector defaultFileTypeDetector =
            createDefaultFileTypeDetector();
        static final List<FileTypeDetector> installeDetectors =
            loadInstalledDetectors();

        // creates the default file type detector
        private static FileTypeDetector createDefaultFileTypeDetector() {
            return AccessController
                .doPrivileged(new PrivilegedAction<FileTypeDetector>() {
                    @Override public FileTypeDetector run() {
                        return sun.nio.fs.DefaultFileTypeDetector.create();
                }});
        }

        // loads all installed file type detectors
        private static List<FileTypeDetector> loadInstalledDetectors() {
            return AccessController
                .doPrivileged(new PrivilegedAction<List<FileTypeDetector>>() {
                    @Override public List<FileTypeDetector> run() {
                        List<FileTypeDetector> list = new ArrayList<>();
                        ServiceLoader<FileTypeDetector> loader = ServiceLoader
                            .load(FileTypeDetector.class, ClassLoader.getSystemClassLoader());
                        for (FileTypeDetector detector: loader) {
                            list.add(detector);
                        }
                        return list;
                }});
        }
    }

    /**
     * Probes the content type of a file.
     *
     * <p> This method uses the installed {@link FileTypeDetector} implementations
     * to probe the given file to determine its content type. Each file type
     * detector's {@link FileTypeDetector#probeContentType probeContentType} is
     * invoked, in turn, to probe the file type. If the file is recognized then
     * the content type is returned. If the file is not recognized by any of the
     * installed file type detectors then a system-default file type detector is
     * invoked to guess the content type.
     *
     * <p> A given invocation of the Java virtual machine maintains a system-wide
     * list of file type detectors. Installed file type detectors are loaded
     * using the service-provider loading facility defined by the {@link ServiceLoader}
     * class. Installed file type detectors are loaded using the system class
     * loader. If the system class loader cannot be found then the extension class
     * loader is used; If the extension class loader cannot be found then the
     * bootstrap class loader is used. File type detectors are typically installed
     * by placing them in a JAR file on the application class path or in the
     * extension directory, the JAR file contains a provider-configuration file
     * named {@code java.nio.file.spi.FileTypeDetector} in the resource directory
     * {@code META-INF/services}, and the file lists one or more fully-qualified
     * names of concrete subclass of {@code FileTypeDetector } that have a zero
     * argument constructor. If the process of locating or instantiating the
     * installed file type detectors fails then an unspecified error is thrown.
     * The ordering that installed providers are located is implementation
     * specific.
     *
     * <p> The return value of this method is the string form of the value of a
     * Multipurpose Internet Mail Extension (MIME) content type as
     * defined by <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045:
     * Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet
     * Message Bodies</i></a>. The string is guaranteed to be parsable according
     * to the grammar in the RFC.
     *
     * @param   path
     *          the path to the file to probe
     *
     * @return  The content type of the file, or {@code null} if the content
     *          type cannot be determined
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is installed and it denies an unspecified
     *          permission required by a file type detector implementation.
     *          探测文件的内容类型。
    此方法使用已安装的FileTypeDetector实现探测给定文件，以确定其内容类型。依次调用每个文件类型检测器的probeContentType来探测文件类型。如果识别文件，则返回内容类型。如果安装的任何文件类型检测器都无法识别该文件，则将调用系统默认文件类型检测器来猜测内容类型。
    Java虚拟机的给定调用维护系统范围内的文件类型检测器列表。安装的文件类型检测器使用ServiceLoader类定义的服务提供程序装载工具装载。使用系统类装入器加载已安装的文件类型检测器。如果找不到系统类装入器，则使用扩展类装入器;如果无法找到扩展类装入器，则使用bootstrap类装入器。文件类型检测器通常是通过将它们放置在应用程序类路径的JAR文件中或扩展目录中，JAR文件包含一个名为java.nio.file.spi的提供程序配置文件。资源目录META-INF/services中的FileTypeDetector文件列出一个或多个具有零参数构造函数的具体FileTypeDetector子类的完全限定名。如果定位或实例化已安装文件类型检测器的过程失败，则抛出未指定的错误。安装提供程序所在的顺序是特定于实现的。
    此方法的返回值是RFC 2045: Multipurpose Internet Mail Extensions (MIME)第一部分:Internet消息体的格式。根据RFC中的语法，保证字符串可以被解析。
     */
    public static String probeContentType(Path path)
        throws IOException
    {
        // try installed file type detectors
        for (FileTypeDetector detector: FileTypeDetectors.installeDetectors) {
            String result = detector.probeContentType(path);
            if (result != null)
                return result;
        }

        // fallback to default
        return FileTypeDetectors.defaultFileTypeDetector.probeContentType(path);
    }

    // -- File Attributes --

    /**
     * Returns a file attribute view of a given type.
     *
     * <p> A file attribute view provides a read-only or updatable view of a
     * set of file attributes. This method is intended to be used where the file
     * attribute view defines type-safe methods to read or update the file
     * attributes. The {@code type} parameter is the type of the attribute view
     * required and the method returns an instance of that type if supported.
     * The {@link BasicFileAttributeView} type supports access to the basic
     * attributes of a file. Invoking this method to select a file attribute
     * view of that type will always return an instance of that class.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled by the resulting file attribute view for the case that the
     * file is a symbolic link. By default, symbolic links are followed. If the
     * option {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} is present then
     * symbolic links are not followed. This option is ignored by implementations
     * that do not support symbolic links.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want read or set a file's ACL, if supported:
     * <pre>
     *     Path path = ...
     *     AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
     *     if (view != null) {
     *         List&lt;AclEntry&gt; acl = view.getAcl();
     *         :
     *     }
     * </pre>
     *
     * @param   <V>
     *          The {@code FileAttributeView} type
     * @param   path
     *          the path to the file
     * @param   type
     *          the {@code Class} object corresponding to the file attribute view
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  a file attribute view of the specified type, or {@code null} if
     *          the attribute view type is not available
     *          返回给定类型的文件属性视图。
    文件属性视图提供一组文件属性的只读或可更新视图。这个方法的目的是在文件属性视图定义类型安全的方法来读取或更新文件属性的地方使用。类型参数是所需的属性视图的类型，如果支持，该方法将返回该类型的实例。BasicFileAttributeView类型支持对文件的基本属性的访问。调用此方法来选择该类型的file属性视图将始终返回该类的实例。
    对于文件是符号链接的情况，可以使用options数组来指示结果文件属性视图如何处理符号链接。默认情况下，遵循符号链接。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。不支持符号链接的实现会忽略此选项。
     */
    public static <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                       Class<V> type,
                                                                       LinkOption... options)
    {
        return provider(path).getFileAttributeView(path, type, options);
    }

    /**
     * Reads a file's attributes as a bulk operation.
     *
     * <p> The {@code type} parameter is the type of the attributes required
     * and this method returns an instance of that type if supported. All
     * implementations support a basic set of file attributes and so invoking
     * this method with a  {@code type} parameter of {@code
     * BasicFileAttributes.class} will not throw {@code
     * UnsupportedOperationException}.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> It is implementation specific if all file attributes are read as an
     * atomic operation with respect to other file system operations.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to read a file's attributes in bulk:
     * <pre>
     *    Path path = ...
     *    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
     * </pre>
     * Alternatively, suppose we want to read file's POSIX attributes without
     * following symbolic links:
     * <pre>
     *    PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class, NOFOLLOW_LINKS);
     * </pre>
     *
     * @param   <A>
     *          The {@code BasicFileAttributes} type
     * @param   path
     *          the path to the file
     * @param   type
     *          the {@code Class} of the file attributes required
     *          to read
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  the file attributes
     *
     * @throws  UnsupportedOperationException
     *          if an attributes of the given type are not supported
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file. If this
     *          method is invoked to read security sensitive attributes then the
     *          security manager may be invoke to check for additional permissions.
     *          读取文件的属性作为批量操作。
    类型参数是所需属性的类型，如果支持，此方法将返回该类型的实例。所有实现都支持一组基本的文件属性，因此使用BasicFileAttributes类型参数调用此方法。类不会抛出UnsupportedOperationException。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
    如果所有文件属性都被作为与其他文件系统操作相关的原子操作读取，则它是特定于实现的。
     */
    public static <A extends BasicFileAttributes> A readAttributes(Path path,
                                                                   Class<A> type,
                                                                   LinkOption... options)
        throws IOException
    {
        return provider(path).readAttributes(path, type, options);
    }

    /**
     * Sets the value of a file attribute.
     *
     * <p> The {@code attribute} parameter identifies the attribute to be set
     * and takes the form:
     * <blockquote>
     * [<i>view-name</i><b>:</b>]<i>attribute-name</i>
     * </blockquote>
     * where square brackets [...] delineate an optional component and the
     * character {@code ':'} stands for itself.
     *
     * <p> <i>view-name</i> is the {@link FileAttributeView#name name} of a {@link
     * FileAttributeView} that identifies a set of file attributes. If not
     * specified then it defaults to {@code "basic"}, the name of the file
     * attribute view that identifies the basic set of file attributes common to
     * many file systems. <i>attribute-name</i> is the name of the attribute
     * within the set.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is set. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to set the DOS "hidden" attribute:
     * <pre>
     *    Path path = ...
     *    Files.setAttribute(path, "dos:hidden", true);
     * </pre>
     *
     * @param   path
     *          the path to the file
     * @param   attribute
     *          the attribute to set
     * @param   value
     *          the attribute value
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  the {@code path} parameter
     *
     * @throws  UnsupportedOperationException
     *          if the attribute view is not available
     * @throws  IllegalArgumentException
     *          if the attribute name is not specified, or is not recognized, or
     *          the attribute value is of the correct type but has an
     *          inappropriate value
     * @throws  ClassCastException
     *          if the attribute value is not of the expected type or is a
     *          collection containing elements that are not of the expected
     *          type
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file. If this method is invoked
     *          to set security sensitive attributes then the security manager
     *          may be invoked to check for additional permissions.
     *          设置文件属性的值。
    属性参数标识要设置的属性，并采用以下形式:
    视图名称:属性名称
    在方括号[…描述一个可选的部分，字符':'代表它自己。
    视图名是标识一组文件属性的FileAttributeView的名称。如果没有指定，那么它将默认为“basic”，即文件属性视图的名称，它标识了许多文件系统共同的基本文件属性集。属性名是集合中属性的名称。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并设置链接最终目标的文件属性。
     */
    public static Path setAttribute(Path path, String attribute, Object value,
                                    LinkOption... options)
        throws IOException
    {
        provider(path).setAttribute(path, attribute, value, options);
        return path;
    }

    /**
     * Reads the value of a file attribute.
     *
     * <p> The {@code attribute} parameter identifies the attribute to be read
     * and takes the form:
     * <blockquote>
     * [<i>view-name</i><b>:</b>]<i>attribute-name</i>
     * </blockquote>
     * where square brackets [...] delineate an optional component and the
     * character {@code ':'} stands for itself.
     *
     * <p> <i>view-name</i> is the {@link FileAttributeView#name name} of a {@link
     * FileAttributeView} that identifies a set of file attributes. If not
     * specified then it defaults to {@code "basic"}, the name of the file
     * attribute view that identifies the basic set of file attributes common to
     * many file systems. <i>attribute-name</i> is the name of the attribute.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we require the user ID of the file owner on a system that
     * supports a "{@code unix}" view:
     * <pre>
     *    Path path = ...
     *    int uid = (Integer)Files.getAttribute(path, "unix:uid");
     * </pre>
     *
     * @param   path
     *          the path to the file
     * @param   attribute
     *          the attribute to read
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  the attribute value
     *
     * @throws  UnsupportedOperationException
     *          if the attribute view is not available
     * @throws  IllegalArgumentException
     *          if the attribute name is not specified or is not recognized
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file. If this method is invoked
     *          to read security sensitive attributes then the security manager
     *          may be invoked to check for additional permissions.
    读取文件属性的值。
    属性参数标识要读取的属性，并采用表单:
    视图名称:属性名称
    在方括号[…描述一个可选的部分，字符':'代表它自己。
    视图名是标识一组文件属性的FileAttributeView的名称。如果没有指定，那么它将默认为“basic”，即文件属性视图的名称，它标识了许多文件系统共同的基本文件属性集。属性名是属性的名称。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
     *
     */
    public static Object getAttribute(Path path, String attribute,
                                      LinkOption... options)
        throws IOException
    {
        // only one attribute should be read
        if (attribute.indexOf('*') >= 0 || attribute.indexOf(',') >= 0)
            throw new IllegalArgumentException(attribute);
        Map<String,Object> map = readAttributes(path, attribute, options);
        assert map.size() == 1;
        String name;
        int pos = attribute.indexOf(':');
        if (pos == -1) {
            name = attribute;
        } else {
            name = (pos == attribute.length()) ? "" : attribute.substring(pos+1);
        }
        return map.get(name);
    }

    /**
     * Reads a set of file attributes as a bulk operation.
     *
     * <p> The {@code attributes} parameter identifies the attributes to be read
     * and takes the form:
     * <blockquote>
     * [<i>view-name</i><b>:</b>]<i>attribute-list</i>
     * </blockquote>
     * where square brackets [...] delineate an optional component and the
     * character {@code ':'} stands for itself.
     *
     * <p> <i>view-name</i> is the {@link FileAttributeView#name name} of a {@link
     * FileAttributeView} that identifies a set of file attributes. If not
     * specified then it defaults to {@code "basic"}, the name of the file
     * attribute view that identifies the basic set of file attributes common to
     * many file systems.
     *
     * <p> The <i>attribute-list</i> component is a comma separated list of
     * zero or more names of attributes to read. If the list contains the value
     * {@code "*"} then all attributes are read. Attributes that are not supported
     * are ignored and will not be present in the returned map. It is
     * implementation specific if all attributes are read as an atomic operation
     * with respect to other file system operations.
     *
     * <p> The following examples demonstrate possible values for the {@code
     * attributes} parameter:
     *
     * <blockquote>
     * <table border="0" summary="Possible values">
     * <tr>
     *   <td> {@code "*"} </td>
     *   <td> Read all {@link BasicFileAttributes basic-file-attributes}. </td>
     * </tr>
     * <tr>
     *   <td> {@code "size,lastModifiedTime,lastAccessTime"} </td>
     *   <td> Reads the file size, last modified time, and last access time
     *     attributes. </td>
     * </tr>
     * <tr>
     *   <td> {@code "posix:*"} </td>
     *   <td> Read all {@link PosixFileAttributes POSIX-file-attributes}. </td>
     * </tr>
     * <tr>
     *   <td> {@code "posix:permissions,owner,size"} </td>
     *   <td> Reads the POSX file permissions, owner, and file size. </td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * @param   path
     *          the path to the file
     * @param   attributes
     *          the attributes to read
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  a map of the attributes returned; The map's keys are the
     *          attribute names, its values are the attribute values
     *
     * @throws  UnsupportedOperationException
     *          if the attribute view is not available
     * @throws  IllegalArgumentException
     *          if no attributes are specified or an unrecognized attributes is
     *          specified
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file. If this method is invoked
     *          to read security sensitive attributes then the security manager
     *          may be invoke to check for additional permissions.
     *          读取一组文件属性作为批量操作。
    属性参数标识要读取的属性并采用以下形式:
    视图名称:属性列表
    在方括号[…描述一个可选的部分，字符':'代表它自己。
    视图名是标识一组文件属性的FileAttributeView的名称。如果没有指定，则默认为“basic”，即文件属性视图的名称，该视图标识许多文件系统常见的文件属性的基本集合。
    属性列表组件是一个由0或多个要读取的属性名组成的逗号分隔的列表。如果列表包含值“*”，则读取所有属性。不支持的属性将被忽略，并且不会出现在返回的映射中。如果对其他文件系统操作将所有属性作为原子操作读取，则它是特定于实现的。
    下面的示例演示了属性参数的可能值:
    “*”
    阅读所有basic-file-attributes。
    “大小、lastModifiedTime lastAccessTime”
    读取文件大小、最后修改时间和最后访问时间属性。
    “posix:*”
    阅读所有POSIX-file-attributes。
    “posix:权限、所有者、大小”
    读取POSX文件权限、所有者和文件大小。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
     */
    public static Map<String,Object> readAttributes(Path path, String attributes,
                                                    LinkOption... options)
        throws IOException
    {
        return provider(path).readAttributes(path, attributes, options);
    }

    /**
     * Returns a file's POSIX file permissions.
     *
     * <p> The {@code path} parameter is associated with a {@code FileSystem}
     * that supports the {@link PosixFileAttributeView}. This attribute view
     * provides access to file attributes commonly associated with files on file
     * systems used by operating systems that implement the Portable Operating
     * System Interface (POSIX) family of standards.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * @param   path
     *          the path to the file
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  the file permissions
     *
     * @throws  UnsupportedOperationException
     *          if the associated file system does not support the {@code
     *          PosixFileAttributeView}
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, and it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkRead(String) checkRead} method
     *          denies read access to the file.
     *          返回文件的POSIX文件权限。
    路径参数与支持PosixFileAttributeView的文件系统相关联。此属性视图提供对文件属性的访问，这些文件属性通常与实现可移植操作系统接口(POSIX)系列标准的操作系统使用的文件系统上的文件相关。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
     */
    public static Set<PosixFilePermission> getPosixFilePermissions(Path path,
                                                                   LinkOption... options)
        throws IOException
    {
        return readAttributes(path, PosixFileAttributes.class, options).permissions();
    }

    /**
     * Sets a file's POSIX permissions.
     *
     * <p> The {@code path} parameter is associated with a {@code FileSystem}
     * that supports the {@link PosixFileAttributeView}. This attribute view
     * provides access to file attributes commonly associated with files on file
     * systems used by operating systems that implement the Portable Operating
     * System Interface (POSIX) family of standards.
     *
     * @param   path
     *          The path to the file
     * @param   perms
     *          The new set of permissions
     *
     * @return  The path
     *
     * @throws  UnsupportedOperationException
     *          if the associated file system does not support the {@code
     *          PosixFileAttributeView}
     * @throws  ClassCastException
     *          if the sets contains elements that are not of type {@code
     *          PosixFilePermission}
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file.
     *          设置文件的POSIX权限。
    路径参数与支持PosixFileAttributeView的文件系统相关联。此属性视图提供对文件属性的访问，这些文件属性通常与实现可移植操作系统接口(POSIX)系列标准的操作系统使用的文件系统上的文件相关。
     */
    public static Path setPosixFilePermissions(Path path,
                                               Set<PosixFilePermission> perms)
        throws IOException
    {
        PosixFileAttributeView view =
            getFileAttributeView(path, PosixFileAttributeView.class);
        if (view == null)
            throw new UnsupportedOperationException();
        view.setPermissions(perms);
        return path;
    }

    /**
     * Returns the owner of a file.
     *
     * <p> The {@code path} parameter is associated with a file system that
     * supports {@link FileOwnerAttributeView}. This file attribute view provides
     * access to a file attribute that is the owner of the file.
     *
     * @param   path
     *          The path to the file
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  A user principal representing the owner of the file
     *
     * @throws  UnsupportedOperationException
     *          if the associated file system does not support the {@code
     *          FileOwnerAttributeView}
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkRead(String) checkRead} method
     *          denies read access to the file.
     *          返回文件的所有者。
    路径参数与支持FileOwnerAttributeView的文件系统相关联。此文件属性视图提供对文件属性的访问，该属性是文件的所有者。
     */
    public static UserPrincipal getOwner(Path path, LinkOption... options) throws IOException {
        FileOwnerAttributeView view =
            getFileAttributeView(path, FileOwnerAttributeView.class, options);
        if (view == null)
            throw new UnsupportedOperationException();
        return view.getOwner();
    }

    /**
     * Updates the file owner.
     *
     * <p> The {@code path} parameter is associated with a file system that
     * supports {@link FileOwnerAttributeView}. This file attribute view provides
     * access to a file attribute that is the owner of the file.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to make "joe" the owner of a file:
     * <pre>
     *     Path path = ...
     *     UserPrincipalLookupService lookupService =
     *         provider(path).getUserPrincipalLookupService();
     *     UserPrincipal joe = lookupService.lookupPrincipalByName("joe");
     *     Files.setOwner(path, joe);
     * </pre>
     *
     * @param   path
     *          The path to the file
     * @param   owner
     *          The new file owner
     *
     * @return  The path
     *
     * @throws  UnsupportedOperationException
     *          if the associated file system does not support the {@code
     *          FileOwnerAttributeView}
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file.
     *
     * @see FileSystem#getUserPrincipalLookupService
     * @see java.nio.file.attribute.UserPrincipalLookupService
     * 更新文件所有者。
    路径参数与支持FileOwnerAttributeView的文件系统相关联。此文件属性视图提供对文件属性的访问，该属性是文件的所有者。
     */
    public static Path setOwner(Path path, UserPrincipal owner)
        throws IOException
    {
        FileOwnerAttributeView view =
            getFileAttributeView(path, FileOwnerAttributeView.class);
        if (view == null)
            throw new UnsupportedOperationException();
        view.setOwner(owner);
        return path;
    }

    /**
     * Tests whether a file is a symbolic link.
     *
     * <p> Where it is required to distinguish an I/O exception from the case
     * that the file is not a symbolic link then the file attributes can be
     * read with the {@link #readAttributes(Path,Class,LinkOption[])
     * readAttributes} method and the file type tested with the {@link
     * BasicFileAttributes#isSymbolicLink} method.
     *
     * @param   path  The path to the file
     *
     * @return  {@code true} if the file is a symbolic link; {@code false} if
     *          the file does not exist, is not a symbolic link, or it cannot
     *          be determined if the file is a symbolic link or not.
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file.
     *          测试文件是否为符号链接。
    如果需要将I/O异常与文件不是符号链接区分开来，则可以使用readAttributes方法读取文件属性，并使用BasicFileAttributes属性测试文件类型。isSymbolicLink方法。
     */
    public static boolean isSymbolicLink(Path path) {
        try {
            return readAttributes(path,
                                  BasicFileAttributes.class,
                                  LinkOption.NOFOLLOW_LINKS).isSymbolicLink();
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Tests whether a file is a directory.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> Where it is required to distinguish an I/O exception from the case
     * that the file is not a directory then the file attributes can be
     * read with the {@link #readAttributes(Path,Class,LinkOption[])
     * readAttributes} method and the file type tested with the {@link
     * BasicFileAttributes#isDirectory} method.
     *
     * @param   path
     *          the path to the file to test
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  {@code true} if the file is a directory; {@code false} if
     *          the file does not exist, is not a directory, or it cannot
     *          be determined if the file is a directory or not.
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file.
     *          测试文件是否为目录。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
    如果需要将I/O异常与文件不是目录区分开来，则可以使用readAttributes方法读取文件属性，并使用BasicFileAttributes属性测试文件类型。isDirectory方法。
     */
    public static boolean isDirectory(Path path, LinkOption... options) {
        try {
            return readAttributes(path, BasicFileAttributes.class, options).isDirectory();
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Tests whether a file is a regular file with opaque content.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> Where it is required to distinguish an I/O exception from the case
     * that the file is not a regular file then the file attributes can be
     * read with the {@link #readAttributes(Path,Class,LinkOption[])
     * readAttributes} method and the file type tested with the {@link
     * BasicFileAttributes#isRegularFile} method.
     *
     * @param   path
     *          the path to the file
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  {@code true} if the file is a regular file; {@code false} if
     *          the file does not exist, is not a regular file, or it
     *          cannot be determined if the file is a regular file or not.
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file.
     *          测试文件是否是含有不透明内容的常规文件。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
    如果需要将I/O异常与文件不是常规文件区分开来，则可以使用readAttributes方法读取文件属性，并使用BasicFileAttributes属性测试文件类型。isRegularFile方法。
     */
    public static boolean isRegularFile(Path path, LinkOption... options) {
        try {
            return readAttributes(path, BasicFileAttributes.class, options).isRegularFile();
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Returns a file's last modified time.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed and the file attribute of the final target
     * of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * @param   path
     *          the path to the file
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  a {@code FileTime} representing the time the file was last
     *          modified, or an implementation specific default when a time
     *          stamp to indicate the time of last modification is not supported
     *          by the file system
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file.
     *
     * @see BasicFileAttributes#lastModifiedTime
     * 返回文件的最后修改时间。
    对于文件是符号链接的情况，可以使用options数组来指示如何处理符号链接。默认情况下，遵循符号链接，并读取链接最终目标的文件属性。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
     */
    public static FileTime getLastModifiedTime(Path path, LinkOption... options)
        throws IOException
    {
        return readAttributes(path, BasicFileAttributes.class, options).lastModifiedTime();
    }

    /**
     * Updates a file's last modified time attribute. The file time is converted
     * to the epoch and precision supported by the file system. Converting from
     * finer to coarser granularities result in precision loss. The behavior of
     * this method when attempting to set the last modified time when it is not
     * supported by the file system or is outside the range supported by the
     * underlying file store is not defined. It may or not fail by throwing an
     * {@code IOException}.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to set the last modified time to the current time:
     * <pre>
     *    Path path = ...
     *    FileTime now = FileTime.fromMillis(System.currentTimeMillis());
     *    Files.setLastModifiedTime(path, now);
     * </pre>
     *
     * @param   path
     *          the path to the file
     * @param   time
     *          the new last modified time
     *
     * @return  the path
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, the security manager's {@link
     *          SecurityManager#checkWrite(String) checkWrite} method is invoked
     *          to check write access to file
     *
     * @see BasicFileAttributeView#setTimes
     * 更新文件的最后修改时间属性。文件时间转换为文件系统支持的历元和精度。从细粒度到粗粒度的粒度转换会导致精度损失。当尝试在文件系统不支持或超出底层文件存储支持范围时设置最后修改时间时，此方法的行为没有定义。它可能会抛出IOException，也可能不会失败。
     */
    public static Path setLastModifiedTime(Path path, FileTime time)
        throws IOException
    {
        getFileAttributeView(path, BasicFileAttributeView.class)
            .setTimes(time, null, null);
        return path;
    }

    /**
     * Returns the size of a file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons. The size of files that are not {@link
     * #isRegularFile regular} files is implementation specific and
     * therefore unspecified.
     *
     * @param   path
     *          the path to the file
     *
     * @return  the file size, in bytes
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file.
     *
     * @see BasicFileAttributes#size
     * 返回文件的大小(以字节为单位)。由于压缩、对稀疏文件的支持或其他原因，大小可能与文件系统上的实际大小不同。不是常规文件的文件的大小是特定于实现的，因此不指定。
     */
    public static long size(Path path) throws IOException {
        return readAttributes(path, BasicFileAttributes.class).size();
    }

    // -- Accessibility --

    /**
     * Returns {@code false} if NOFOLLOW_LINKS is present.
     */
    private static boolean followLinks(LinkOption... options) {
        boolean followLinks = true;
        for (LinkOption opt: options) {
            if (opt == LinkOption.NOFOLLOW_LINKS) {
                followLinks = false;
                continue;
            }
            if (opt == null)
                throw new NullPointerException();
            throw new AssertionError("Should not get here");
        }
        return followLinks;
    }

    /**
     * Tests whether a file exists.
     *
     * <p> The {@code options} parameter may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> Note that the result of this method is immediately outdated. If this
     * method indicates the file exists then there is no guarantee that a
     * subsequence access will succeed. Care should be taken when using this
     * method in security sensitive applications.
     *
     * @param   path
     *          the path to the file to test
     * @param   options
     *          options indicating how symbolic links are handled
     * .
     * @return  {@code true} if the file exists; {@code false} if the file does
     *          not exist or its existence cannot be determined.
     *
     * @throws  SecurityException
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String)} is invoked to check
     *          read access to the file.
     *
     * @see #notExists
     * 测试文件是否存在。
    对于文件是符号链接的情况，可以使用options参数来指示如何处理符号链接。默认情况下，遵循符号链接。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
    注意，此方法的结果立即过时。如果此方法指示文件存在，则不能保证子序列访问将成功。在安全敏感的应用程序中使用此方法时应小心。
     */
    public static boolean exists(Path path, LinkOption... options) {
        try {
            if (followLinks(options)) {
                provider(path).checkAccess(path);
            } else {
                // attempt to read attributes without following links
                readAttributes(path, BasicFileAttributes.class,
                               LinkOption.NOFOLLOW_LINKS);
            }
            // file exists
            return true;
        } catch (IOException x) {
            // does not exist or unable to determine if file exists
            return false;
        }

    }

    /**
     * Tests whether the file located by this path does not exist. This method
     * is intended for cases where it is required to take action when it can be
     * confirmed that a file does not exist.
     *
     * <p> The {@code options} parameter may be used to indicate how symbolic links
     * are handled for the case that the file is a symbolic link. By default,
     * symbolic links are followed. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * <p> Note that this method is not the complement of the {@link #exists
     * exists} method. Where it is not possible to determine if a file exists
     * or not then both methods return {@code false}. As with the {@code exists}
     * method, the result of this method is immediately outdated. If this
     * method indicates the file does exist then there is no guarantee that a
     * subsequence attempt to create the file will succeed. Care should be taken
     * when using this method in security sensitive applications.
     *
     * @param   path
     *          the path to the file to test
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  {@code true} if the file does not exist; {@code false} if the
     *          file exists or its existence cannot be determined
     *
     * @throws  SecurityException
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String)} is invoked to check
     *          read access to the file.
     *          测试由此路径定位的文件是否不存在。这种方法适用于在确认文件不存在时需要采取行动的情况。
    对于文件是符号链接的情况，可以使用options参数来指示如何处理符号链接。默认情况下，遵循符号链接。如果出现选项NOFOLLOW_LINKS，则不遵循符号链接。
    注意，此方法不是现有方法的补充。如果无法确定文件是否存在，那么两个方法都返回false。与现有方法一样，该方法的结果立即就过时了。如果该方法表明该文件确实存在，则不能保证创建该文件的后续操作将成功。在安全敏感的应用程序中使用此方法时应小心。
     */
    public static boolean notExists(Path path, LinkOption... options) {
        try {
            if (followLinks(options)) {
                provider(path).checkAccess(path);
            } else {
                // attempt to read attributes without following links
                readAttributes(path, BasicFileAttributes.class,
                               LinkOption.NOFOLLOW_LINKS);
            }
            // file exists
            return false;
        } catch (NoSuchFileException x) {
            // file confirmed not to exist
            return true;
        } catch (IOException x) {
            return false;
        }
    }

    /**
     * Used by isReadbale, isWritable, isExecutable to test access to a file.用于isReadbale, isWritable, isExecutable用于测试对文件的访问。
     */
    private static boolean isAccessible(Path path, AccessMode... modes) {
        try {
            provider(path).checkAccess(path, modes);
            return true;
        } catch (IOException x) {
            return false;
        }
    }

    /**
     * Tests whether a file is readable. This method checks that a file exists
     * and that this Java virtual machine has appropriate privileges that would
     * allow it open the file for reading. Depending on the implementation, this
     * method may require to read file permissions, access control lists, or
     * other file attributes in order to check the effective access to the file.
     * Consequently, this method may not be atomic with respect to other file
     * system operations.
     *
     * <p> Note that the result of this method is immediately outdated, there is
     * no guarantee that a subsequent attempt to open the file for reading will
     * succeed (or even that it will access the same file). Care should be taken
     * when using this method in security sensitive applications.
     *
     * @param   path
     *          the path to the file to check
     *
     * @return  {@code true} if the file exists and is readable; {@code false}
     *          if the file does not exist, read access would be denied because
     *          the Java virtual machine has insufficient privileges, or access
     *          cannot be determined
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          is invoked to check read access to the file.
     *          测试文件是否可读。此方法检查文件是否存在，以及此Java虚拟机是否具有允许打开文件进行读取的适当权限。根据实现的不同，此方法可能需要读取文件权限、访问控制列表或其他文件属性，以检查对文件的有效访问。因此，对于其他文件系统操作，此方法可能不是原子的。
    注意，此方法的结果立即过时，不能保证后续尝试打开文件进行读取将成功(甚至不能保证它将访问相同的文件)。在安全敏感的应用程序中使用此方法时应小心。
     */
    public static boolean isReadable(Path path) {
        return isAccessible(path, AccessMode.READ);
    }

    /**
     * Tests whether a file is writable. This method checks that a file exists
     * and that this Java virtual machine has appropriate privileges that would
     * allow it open the file for writing. Depending on the implementation, this
     * method may require to read file permissions, access control lists, or
     * other file attributes in order to check the effective access to the file.
     * Consequently, this method may not be atomic with respect to other file
     * system operations.
     *
     * <p> Note that result of this method is immediately outdated, there is no
     * guarantee that a subsequent attempt to open the file for writing will
     * succeed (or even that it will access the same file). Care should be taken
     * when using this method in security sensitive applications.
     *
     * @param   path
     *          the path to the file to check
     *
     * @return  {@code true} if the file exists and is writable; {@code false}
     *          if the file does not exist, write access would be denied because
     *          the Java virtual machine has insufficient privileges, or access
     *          cannot be determined
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          is invoked to check write access to the file.
     *          测试文件是否可写。此方法检查文件是否存在，以及此Java虚拟机是否具有允许它打开文件进行写入的适当权限。根据实现的不同，此方法可能需要读取文件权限、访问控制列表或其他文件属性，以检查对文件的有效访问。因此，对于其他文件系统操作，此方法可能不是原子的。
    注意，此方法的结果马上就过时了，不能保证打开文件进行写入操作的后续尝试会成功(甚至不能保证它将访问相同的文件)。在安全敏感的应用程序中使用此方法时应小心。
     */
    public static boolean isWritable(Path path) {
        return isAccessible(path, AccessMode.WRITE);
    }

    /**
     * Tests whether a file is executable. This method checks that a file exists
     * and that this Java virtual machine has appropriate privileges to {@link
     * Runtime#exec execute} the file. The semantics may differ when checking
     * access to a directory. For example, on UNIX systems, checking for
     * execute access checks that the Java virtual machine has permission to
     * search the directory in order to access file or subdirectories.
     *
     * <p> Depending on the implementation, this method may require to read file
     * permissions, access control lists, or other file attributes in order to
     * check the effective access to the file. Consequently, this method may not
     * be atomic with respect to other file system operations.
     *
     * <p> Note that the result of this method is immediately outdated, there is
     * no guarantee that a subsequent attempt to execute the file will succeed
     * (or even that it will access the same file). Care should be taken when
     * using this method in security sensitive applications.
     *
     * @param   path
     *          the path to the file to check
     *
     * @return  {@code true} if the file exists and is executable; {@code false}
     *          if the file does not exist, execute access would be denied because
     *          the Java virtual machine has insufficient privileges, or access
     *          cannot be determined
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkExec(String)
     *          checkExec} is invoked to check execute access to the file.
     *          测试文件是否可执行。此方法检查文件是否存在，以及此Java虚拟机具有执行文件的适当权限。在检查对目录的访问时，语义可能有所不同。例如，在UNIX系统上，检查执行访问检查Java虚拟机是否具有搜索目录以访问文件或子目录的权限。
    根据实现的不同，此方法可能需要读取文件权限、访问控制列表或其他文件属性，以检查对文件的有效访问。因此，对于其他文件系统操作，此方法可能不是原子的。
    注意，此方法的结果立即过时，不能保证后续执行文件的尝试将成功(甚至不能保证它将访问相同的文件)。在安全敏感的应用程序中使用此方法时应小心。
     */
    public static boolean isExecutable(Path path) {
        return isAccessible(path, AccessMode.EXECUTE);
    }

    // -- Recursive operations --

    /**
     * Walks a file tree.
     *
     * <p> This method walks a file tree rooted at a given starting file. The
     * file tree traversal is <em>depth-first</em> with the given {@link
     * FileVisitor} invoked for each file encountered. File tree traversal
     * completes when all accessible files in the tree have been visited, or a
     * visit method returns a result of {@link FileVisitResult#TERMINATE
     * TERMINATE}. Where a visit method terminates due an {@code IOException},
     * an uncaught error, or runtime exception, then the traversal is terminated
     * and the error or exception is propagated to the caller of this method.
     *
     * <p> For each file encountered this method attempts to read its {@link
     * java.nio.file.attribute.BasicFileAttributes}. If the file is not a
     * directory then the {@link FileVisitor#visitFile visitFile} method is
     * invoked with the file attributes. If the file attributes cannot be read,
     * due to an I/O exception, then the {@link FileVisitor#visitFileFailed
     * visitFileFailed} method is invoked with the I/O exception.
     *
     * <p> Where the file is a directory, and the directory could not be opened,
     * then the {@code visitFileFailed} method is invoked with the I/O exception,
     * after which, the file tree walk continues, by default, at the next
     * <em>sibling</em> of the directory.
     *
     * <p> Where the directory is opened successfully, then the entries in the
     * directory, and their <em>descendants</em> are visited. When all entries
     * have been visited, or an I/O error occurs during iteration of the
     * directory, then the directory is closed and the visitor's {@link
     * FileVisitor#postVisitDirectory postVisitDirectory} method is invoked.
     * The file tree walk then continues, by default, at the next <em>sibling</em>
     * of the directory.
     *
     * <p> By default, symbolic links are not automatically followed by this
     * method. If the {@code options} parameter contains the {@link
     * FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} option then symbolic links are
     * followed. When following links, and the attributes of the target cannot
     * be read, then this method attempts to get the {@code BasicFileAttributes}
     * of the link. If they can be read then the {@code visitFile} method is
     * invoked with the attributes of the link (otherwise the {@code visitFileFailed}
     * method is invoked as specified above).
     *
     * <p> If the {@code options} parameter contains the {@link
     * FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} option then this method keeps
     * track of directories visited so that cycles can be detected. A cycle
     * arises when there is an entry in a directory that is an ancestor of the
     * directory. Cycle detection is done by recording the {@link
     * java.nio.file.attribute.BasicFileAttributes#fileKey file-key} of directories,
     * or if file keys are not available, by invoking the {@link #isSameFile
     * isSameFile} method to test if a directory is the same file as an
     * ancestor. When a cycle is detected it is treated as an I/O error, and the
     * {@link FileVisitor#visitFileFailed visitFileFailed} method is invoked with
     * an instance of {@link FileSystemLoopException}.
     *
     * <p> The {@code maxDepth} parameter is the maximum number of levels of
     * directories to visit. A value of {@code 0} means that only the starting
     * file is visited, unless denied by the security manager. A value of
     * {@link Integer#MAX_VALUE MAX_VALUE} may be used to indicate that all
     * levels should be visited. The {@code visitFile} method is invoked for all
     * files, including directories, encountered at {@code maxDepth}, unless the
     * basic file attributes cannot be read, in which case the {@code
     * visitFileFailed} method is invoked.
     *
     * <p> If a visitor returns a result of {@code null} then {@code
     * NullPointerException} is thrown.
     *
     * <p> When a security manager is installed and it denies access to a file
     * (or directory), then it is ignored and the visitor is not invoked for
     * that file (or directory).
     *
     * @param   start
     *          the starting file
     * @param   options
     *          options to configure the traversal
     * @param   maxDepth
     *          the maximum number of directory levels to visit
     * @param   visitor
     *          the file visitor to invoke for each file
     *
     * @return  the starting file
     *
     * @throws  IllegalArgumentException
     *          if the {@code maxDepth} parameter is negative
     * @throws  SecurityException
     *          If the security manager denies access to the starting file.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String) checkRead} method is invoked
     *          to check read access to the directory.
     * @throws  IOException
     *          if an I/O error is thrown by a visitor method
     *          走一个文件树。
    此方法遍历根于给定启动文件的文件树。对遇到的每个文件调用的给定文件访问器来说，文件树遍历是深度优先的。当访问了树中的所有可访问文件，或者访问方法返回终止结果时，文件树遍历就完成了。如果访问方法由于IOException、未捕获错误或运行时异常而终止，那么遍历将终止，并将错误或异常传播给此方法的调用者。
    对于遇到的每个文件，此方法都试图读取基本文件属性。如果文件不是目录，则使用文件属性调用visitFile方法。如果由于I/O异常而无法读取文件属性，则使用I/O异常调用visitFileFailed方法。
    如果该文件是一个目录，并且无法打开该目录，则使用I/O异常调用visitFileFailed方法，在此之后，默认情况下，文件树遍历将在该目录的下一个分支上继续。
    在成功打开目录的地方，将访问目录中的条目及其后代。当访问了所有条目，或者在目录的迭代过程中发生I/O错误时，就会关闭目录，并调用访问者的postVisitDirectory方法。然后，默认情况下，文件树遍历在目录的下一个分支上继续。
    默认情况下，符号链接不会自动跟随此方法。如果选项参数包含FOLLOW_LINKS选项，则遵循符号链接。当跟踪链接时，不能读取目标的属性，然后该方法尝试获取链接的BasicFileAttributes。如果可以读取它们，则使用链接的属性调用visitFile方法(否则按照上面的指定调用visitFileFailed方法)。
    如果options参数包含FOLLOW_LINKS选项，那么该方法将跟踪所访问的目录，以便检测循环。当目录中有一个条目是目录的祖先时，就会出现一个循环。循环检测是通过记录目录的文件键，或者如果文件键不可用，通过调用isSameFile方法来测试目录是否与祖先相同的文件。当检测到一个循环时，它被当作一个I/O错误，而visitFileFailed方法会被一个文件系统循环异常的实例调用。
    maxDepth参数是访问目录的最大级别。值为0意味着只访问起始文件，除非安全管理器拒绝访问。MAX_VALUE的值可以用来指示应该访问所有级别。对在maxDepth中遇到的所有文件(包括目录)调用visitFile方法，除非无法读取基本的文件属性，在这种情况下调用visitFileFailed方法。
    如果访问者返回null，则抛出NullPointerException。
    当安装了安全管理器并拒绝访问文件(或目录)时，就会忽略它，不会为该文件(或目录)调用访问器。
     */
    public static Path walkFileTree(Path start,
                                    Set<FileVisitOption> options,
                                    int maxDepth,
                                    FileVisitor<? super Path> visitor)
        throws IOException
    {
        /**
         * Create a FileTreeWalker to walk the file tree, invoking the visitor 创建FileTreeWalker以遍历文件树，并调用访问者
         * for each event.对于每个事件。
         */
        try (FileTreeWalker walker = new FileTreeWalker(options, maxDepth)) {
            FileTreeWalker.Event ev = walker.walk(start);
            do {
                FileVisitResult result;
                switch (ev.type()) {
                    case ENTRY :
                        IOException ioe = ev.ioeException();
                        if (ioe == null) {
                            assert ev.attributes() != null;
                            result = visitor.visitFile(ev.file(), ev.attributes());
                        } else {
                            result = visitor.visitFileFailed(ev.file(), ioe);
                        }
                        break;

                    case START_DIRECTORY :
                        result = visitor.preVisitDirectory(ev.file(), ev.attributes());

                        // if SKIP_SIBLINGS and SKIP_SUBTREE is returned then
                        // there shouldn't be any more events for the current
                        // directory.
                        if (result == FileVisitResult.SKIP_SUBTREE ||
                            result == FileVisitResult.SKIP_SIBLINGS)
                            walker.pop();
                        break;

                    case END_DIRECTORY :
                        result = visitor.postVisitDirectory(ev.file(), ev.ioeException());

                        // SKIP_SIBLINGS is a no-op for postVisitDirectory
                        if (result == FileVisitResult.SKIP_SIBLINGS)
                            result = FileVisitResult.CONTINUE;
                        break;

                    default :
                        throw new AssertionError("Should not get here");
                }

                if (Objects.requireNonNull(result) != FileVisitResult.CONTINUE) {
                    if (result == FileVisitResult.TERMINATE) {
                        break;
                    } else if (result == FileVisitResult.SKIP_SIBLINGS) {
                        walker.skipRemainingSiblings();
                    }
                }
                ev = walker.next();
            } while (ev != null);
        }

        return start;
    }

    /**
     * Walks a file tree.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <blockquote><pre>
     * walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor)
     * </pre></blockquote>
     * In other words, it does not follow symbolic links, and visits all levels
     * of the file tree.
     *
     * @param   start
     *          the starting file
     * @param   visitor
     *          the file visitor to invoke for each file
     *
     * @return  the starting file
     *
     * @throws  SecurityException
     *          If the security manager denies access to the starting file.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String) checkRead} method is invoked
     *          to check read access to the directory.
     * @throws  IOException
     *          if an I/O error is thrown by a visitor method
     *          走一个文件树。
    这个方法的工作原理就好像调用它就等于计算表达式:
    walkFileTree(开始,EnumSet.noneOf(FileVisitOption.class)整数。MAX_VALUE访客)

    换句话说，它不遵循符号链接，而是访问文件树的所有级别。
     */
    public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor)
        throws IOException
    {
        return walkFileTree(start,
                            EnumSet.noneOf(FileVisitOption.class),
                            Integer.MAX_VALUE,
                            visitor);
    }


    // -- Utility methods for simple usages --

    // buffer size used for reading and writing
    private static final int BUFFER_SIZE = 8192;

    /**
     * Opens a file for reading, returning a {@code BufferedReader} that may be
     * used to read text from the file in an efficient manner. Bytes from the
     * file are decoded into characters using the specified charset. Reading
     * commences at the beginning of the file.
     *
     * <p> The {@code Reader} methods that read from the file throw {@code
     * IOException} if a malformed or unmappable byte sequence is read.
     *
     * @param   path
     *          the path to the file
     * @param   cs
     *          the charset to use for decoding
     *
     * @return  a new buffered reader, with default buffer size, to read text
     *          from the file
     *
     * @throws  IOException
     *          if an I/O error occurs opening the file
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @see #readAllLines
     * 打开一个用于读取的文件，返回一个BufferedReader，该阅读器可用于高效地从文件中读取文本。使用指定的字符集将文件中的字节解码为字符。阅读从文件的开头开始。
    读取文件的读取器方法，如果读取了格式错误或不可映射的字节序列，则抛出IOException。
     */
    public static BufferedReader newBufferedReader(Path path, Charset cs)
        throws IOException
    {
        CharsetDecoder decoder = cs.newDecoder();
        Reader reader = new InputStreamReader(newInputStream(path), decoder);
        return new BufferedReader(reader);
    }

    /**
     * Opens a file for reading, returning a {@code BufferedReader} to read text
     * from the file in an efficient manner. Bytes from the file are decoded into
     * characters using the {@link StandardCharsets#UTF_8 UTF-8} {@link Charset
     * charset}.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <pre>{@code
     * Files.newBufferedReader(path, StandardCharsets.UTF_8)
     * }</pre>
     *
     * @param   path
     *          the path to the file
     *
     * @return  a new buffered reader, with default buffer size, to read text
     *          from the file
     *
     * @throws  IOException
     *          if an I/O error occurs opening the file
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @since 1.8
     * 打开一个用于读取的文件，返回BufferedReader，以便以一种有效的方式从文件中读取文本。使用UTF-8字符集将文件中的字节解码为字符。
    这个方法的工作原理就好像调用它就等于计算表达式:
     */
    public static BufferedReader newBufferedReader(Path path) throws IOException {
        return newBufferedReader(path, StandardCharsets.UTF_8);
    }

    /**
     * Opens or creates a file for writing, returning a {@code BufferedWriter}
     * that may be used to write text to the file in an efficient manner.
     * The {@code options} parameter specifies how the the file is created or
     * opened. If no options are present then this method works as if the {@link
     * StandardOpenOption#CREATE CREATE}, {@link
     * StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and {@link
     * StandardOpenOption#WRITE WRITE} options are present. In other words, it
     * opens the file for writing, creating the file if it doesn't exist, or
     * initially truncating an existing {@link #isRegularFile regular-file} to
     * a size of {@code 0} if it exists.
     *
     * <p> The {@code Writer} methods to write text throw {@code IOException}
     * if the text cannot be encoded using the specified charset.
     *
     * @param   path
     *          the path to the file
     * @param   cs
     *          the charset to use for encoding
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new buffered writer, with default buffer size, to write text
     *          to the file
     *
     * @throws  IOException
     *          if an I/O error occurs opening or creating the file
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *
     * @see #write(Path,Iterable,Charset,OpenOption[])
     * 打开或创建一个用于写入的文件，返回一个可以有效地将文本写入文件的BufferedWriter。options参数指定如何创建或打开文件。如果不存在选项，那么该方法就会像存在CREATE、TRUNCATE_EXISTING和WRITE选项一样工作。换句话说，它打开文件进行写入，创建不存在的文件，或者在现有的常规文件存在时首先将其截断为0。
    如果不能使用指定的字符集编码文本，则写入器方法写入文本抛出IOException。
     */
    public static BufferedWriter newBufferedWriter(Path path, Charset cs,
                                                   OpenOption... options)
        throws IOException
    {
        CharsetEncoder encoder = cs.newEncoder();
        Writer writer = new OutputStreamWriter(newOutputStream(path, options), encoder);
        return new BufferedWriter(writer);
    }

    /**
     * Opens or creates a file for writing, returning a {@code BufferedWriter}
     * to write text to the file in an efficient manner. The text is encoded
     * into bytes for writing using the {@link StandardCharsets#UTF_8 UTF-8}
     * {@link Charset charset}.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <pre>{@code
     * Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)
     * }</pre>
     *
     * @param   path
     *          the path to the file
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new buffered writer, with default buffer size, to write text
     *          to the file
     *
     * @throws  IOException
     *          if an I/O error occurs opening or creating the file
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *
     * @since 1.8
     * 打开或创建一个用于写入的文件，返回BufferedWriter，以便以一种有效的方式将文本写入文件。文本被编码成字节，以便使用UTF-8字符集进行写入。
    这个方法的工作原理就好像调用它就等于计算表达式:
     */
    public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException {
        return newBufferedWriter(path, StandardCharsets.UTF_8, options);
    }

    /**
     * Reads all bytes from an input stream and writes them to an output stream.从输入流读取所有字节并将它们写入输出流。
     */
    private static long copy(InputStream source, OutputStream sink)
        throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    /**
     * Copies all bytes from an input stream to a file. On return, the input
     * stream will be at end of stream.
     *
     * <p> By default, the copy fails if the target file already exists or is a
     * symbolic link. If the {@link StandardCopyOption#REPLACE_EXISTING
     * REPLACE_EXISTING} option is specified, and the target file already exists,
     * then it is replaced if it is not a non-empty directory. If the target
     * file exists and is a symbolic link, then the symbolic link is replaced.
     * In this release, the {@code REPLACE_EXISTING} option is the only option
     * required to be supported by this method. Additional options may be
     * supported in future releases.
     *
     * <p>  If an I/O error occurs reading from the input stream or writing to
     * the file, then it may do so after the target file has been created and
     * after some bytes have been read or written. Consequently the input
     * stream may not be at end of stream and may be in an inconsistent state.
     * It is strongly recommended that the input stream be promptly closed if an
     * I/O error occurs.
     *
     * <p> This method may block indefinitely reading from the input stream (or
     * writing to the file). The behavior for the case that the input stream is
     * <i>asynchronously closed</i> or the thread interrupted during the copy is
     * highly input stream and file system provider specific and therefore not
     * specified.
     *
     * <p> <b>Usage example</b>: Suppose we want to capture a web page and save
     * it to a file:
     * <pre>
     *     Path path = ...
     *     URI u = URI.create("http://java.sun.com/");
     *     try (InputStream in = u.toURL().openStream()) {
     *         Files.copy(in, path);
     *     }
     * </pre>
     *
     * @param   in
     *          the input stream to read from
     * @param   target
     *          the path to the file
     * @param   options
     *          options specifying how the copy should be done
     *
     * @return  the number of bytes read or written
     *
     * @throws  IOException
     *          if an I/O error occurs when reading or writing
     * @throws  FileAlreadyExistsException
     *          if the target file exists but cannot be replaced because the
     *          {@code REPLACE_EXISTING} option is not specified <i>(optional
     *          specific exception)</i>
     * @throws  DirectoryNotEmptyException
     *          the {@code REPLACE_EXISTING} option is specified but the file
     *          cannot be replaced because it is a non-empty directory
     *          <i>(optional specific exception)</i>     *
     * @throws  UnsupportedOperationException
     *          if {@code options} contains a copy option that is not supported
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file. Where the
     *          {@code REPLACE_EXISTING} option is specified, the security
     *          manager's {@link SecurityManager#checkDelete(String) checkDelete}
     *          method is invoked to check that an existing file can be deleted.
     *          将输入流中的所有字节复制到文件中。返回时，输入流将位于流的末尾。
    默认情况下，如果目标文件已经存在或者是一个符号链接，复制将失败。如果指定了REPLACE_EXISTING选项，并且目标文件已经存在，那么如果它不是非空目录，则替换它。如果目标文件存在并且是一个符号链接，那么符号链接将被替换。在这个版本中，REPLACE_EXISTING选项是该方法需要支持的惟一选项。在将来的版本中可能支持其他选项。
    如果在从输入流读取或写入文件时发生I/O错误，则可以在创建目标文件并读取或写入某些字节之后执行。因此，输入流可能不在流的末端，可能处于不一致的状态。强烈建议在发生I/O错误时立即关闭输入流。
    此方法可以无限期地阻止从输入流(或写入文件)读取数据。对于输入流被异步关闭或线程在复制过程中被中断的情况，其行为是与输入流和文件系统提供程序相关的，因此没有指定。
     */
    public static long copy(InputStream in, Path target, CopyOption... options)
        throws IOException
    {
        // ensure not null before opening file
        Objects.requireNonNull(in);

        // check for REPLACE_EXISTING
        boolean replaceExisting = false;
        for (CopyOption opt: options) {
            if (opt == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
            } else {
                if (opt == null) {
                    throw new NullPointerException("options contains 'null'");
                }  else {
                    throw new UnsupportedOperationException(opt + " not supported");
                }
            }
        }

        // attempt to delete an existing file
        SecurityException se = null;
        if (replaceExisting) {
            try {
                deleteIfExists(target);
            } catch (SecurityException x) {
                se = x;
            }
        }

        // attempt to create target file. If it fails with
        // FileAlreadyExistsException then it may be because the security
        // manager prevented us from deleting the file, in which case we just
        // throw the SecurityException.
        OutputStream ostream;
        try {
            ostream = newOutputStream(target, StandardOpenOption.CREATE_NEW,
                                              StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException x) {
            if (se != null)
                throw se;
            // someone else won the race and created the file
            throw x;
        }

        // do the copy
        try (OutputStream out = ostream) {
            return copy(in, out);
        }
    }

    /**
     * Copies all bytes from a file to an output stream.
     *
     * <p> If an I/O error occurs reading from the file or writing to the output
     * stream, then it may do so after some bytes have been read or written.
     * Consequently the output stream may be in an inconsistent state. It is
     * strongly recommended that the output stream be promptly closed if an I/O
     * error occurs.
     *
     * <p> This method may block indefinitely writing to the output stream (or
     * reading from the file). The behavior for the case that the output stream
     * is <i>asynchronously closed</i> or the thread interrupted during the copy
     * is highly output stream and file system provider specific and therefore
     * not specified.
     *
     * <p> Note that if the given output stream is {@link java.io.Flushable}
     * then its {@link java.io.Flushable#flush flush} method may need to invoked
     * after this method completes so as to flush any buffered output.
     *
     * @param   source
     *          the  path to the file
     * @param   out
     *          the output stream to write to
     *
     * @return  the number of bytes read or written
     *
     * @throws  IOException
     *          if an I/O error occurs when reading or writing
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
    将文件中的所有字节复制到输出流。
    如果从文件中读取或写入到输出流时发生I/O错误，那么在读取或写入某些字节之后，它可以这样做。因此，输出流可能处于不一致的状态。强烈建议在发生I/O错误时立即关闭输出流。
    此方法可以无限期地阻塞对输出流的写入(或从文件中读取)。对于输出流被异步关闭或在复制过程中线程被中断的情况，输出流和文件系统提供程序是特定的，因此没有指定。
    注意，如果给定的输出流是java.io。然后，在此方法完成之后，它的刷新方法可能需要调用，以便刷新任何缓冲输出。
     */
    public static long copy(Path source, OutputStream out) throws IOException {
        // ensure not null before opening file
        Objects.requireNonNull(out);

        try (InputStream in = newInputStream(source)) {
            return copy(in, out);
        }
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     * 要分配的数组的最大大小。有些vm在数组中保留一些头信息。试图分配较大的数组可能会导致OutOfMemoryError:请求的数组大小超过VM限制
     */
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reads all the bytes from an input stream. Uses {@code initialSize} as a hint
     * about how many bytes the stream will have.
     *
     * @param   source
     *          the input stream to read from
     * @param   initialSize
     *          the initial size of the byte array to allocate
     *
     * @return  a byte array containing the bytes read from the file
     *
     * @throws  IOException
     *          if an I/O error occurs reading from the stream
     * @throws  OutOfMemoryError
     *          if an array of the required size cannot be allocated
     *          从输入流中读取所有字节。使用initialSize作为流将有多少字节的提示。
     */
    private static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (;;) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = source.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte)n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    /**
     * Reads all the bytes from a file. The method ensures that the file is
     * closed when all bytes have been read or an I/O error, or other runtime
     * exception, is thrown.
     *
     * <p> Note that this method is intended for simple cases where it is
     * convenient to read all bytes into a byte array. It is not intended for
     * reading in large files.
     *
     * @param   path
     *          the path to the file
     *
     * @return  a byte array containing the bytes read from the file
     *
     * @throws  IOException
     *          if an I/O error occurs reading from the stream
     * @throws  OutOfMemoryError
     *          if an array of the required size cannot be allocated, for
     *          example the file is larger that {@code 2GB}
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *          从文件中读取所有字节。该方法确保在读取所有字节或抛出I/O错误或其他运行时异常时关闭文件。
    注意，此方法适用于简单的情况，在这种情况下，可以方便地将所有字节读入字节数组。它不适合在大文件中读取。
     */
    public static byte[] readAllBytes(Path path) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(path);
             InputStream in = Channels.newInputStream(sbc)) {
            long size = sbc.size();
            if (size > (long)MAX_BUFFER_SIZE)
                throw new OutOfMemoryError("Required array size too large");

            return read(in, (int)size);
        }
    }

    /**
     * Read all lines from a file. This method ensures that the file is
     * closed when all bytes have been read or an I/O error, or other runtime
     * exception, is thrown. Bytes from the file are decoded into characters
     * using the specified charset.
     *
     * <p> This method recognizes the following as line terminators:
     * <ul>
     *   <li> <code>&#92;u000D</code> followed by <code>&#92;u000A</code>,
     *     CARRIAGE RETURN followed by LINE FEED </li>
     *   <li> <code>&#92;u000A</code>, LINE FEED </li>
     *   <li> <code>&#92;u000D</code>, CARRIAGE RETURN </li>
     * </ul>
     * <p> Additional Unicode line terminators may be recognized in future
     * releases.
     *
     * <p> Note that this method is intended for simple cases where it is
     * convenient to read all lines in a single operation. It is not intended
     * for reading in large files.
     *
     * @param   path
     *          the path to the file
     * @param   cs
     *          the charset to use for decoding
     *
     * @return  the lines from the file as a {@code List}; whether the {@code
     *          List} is modifiable or not is implementation dependent and
     *          therefore not specified
     *
     * @throws  IOException
     *          if an I/O error occurs reading from the file or a malformed or
     *          unmappable byte sequence is read
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @see #newBufferedReader
     * 从文件中读取所有行。此方法确保在读取所有字节或抛出I/O错误或其他运行时异常时关闭文件。使用指定的字符集将文件中的字节解码为字符。
    这种方法可以识别以下的线路终端:
    在将来的版本中可能会识别其他的Unicode行终止符。
    注意，此方法适用于简单的情况，在这种情况下，可以方便地在单个操作中读取所有行。它不适合在大文件中读取。
     */
    public static List<String> readAllLines(Path path, Charset cs) throws IOException {
        try (BufferedReader reader = newBufferedReader(path, cs)) {
            List<String> result = new ArrayList<>();
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            return result;
        }
    }

    /**
     * Read all lines from a file. Bytes from the file are decoded into characters
     * using the {@link StandardCharsets#UTF_8 UTF-8} {@link Charset charset}.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <pre>{@code
     * Files.readAllLines(path, StandardCharsets.UTF_8)
     * }</pre>
     *
     * @param   path
     *          the path to the file
     *
     * @return  the lines from the file as a {@code List}; whether the {@code
     *          List} is modifiable or not is implementation dependent and
     *          therefore not specified
     *
     * @throws  IOException
     *          if an I/O error occurs reading from the file or a malformed or
     *          unmappable byte sequence is read
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @since 1.8
     * 从文件中读取所有行。使用UTF-8字符集将文件中的字节解码为字符。
    这个方法的工作原理就好像调用它就等于计算表达式:
     */
    public static List<String> readAllLines(Path path) throws IOException {
        return readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Writes bytes to a file. The {@code options} parameter specifies how the
     * the file is created or opened. If no options are present then this method
     * works as if the {@link StandardOpenOption#CREATE CREATE}, {@link
     * StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and {@link
     * StandardOpenOption#WRITE WRITE} options are present. In other words, it
     * opens the file for writing, creating the file if it doesn't exist, or
     * initially truncating an existing {@link #isRegularFile regular-file} to
     * a size of {@code 0}. All bytes in the byte array are written to the file.
     * The method ensures that the file is closed when all bytes have been
     * written (or an I/O error or other runtime exception is thrown). If an I/O
     * error occurs then it may do so after the file has created or truncated,
     * or after some bytes have been written to the file.
     *
     * <p> <b>Usage example</b>: By default the method creates a new file or
     * overwrites an existing file. Suppose you instead want to append bytes
     * to an existing file:
     * <pre>
     *     Path path = ...
     *     byte[] bytes = ...
     *     Files.write(path, bytes, StandardOpenOption.APPEND);
     * </pre>
     *
     * @param   path
     *          the path to the file
     * @param   bytes
     *          the byte array with the bytes to write
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  the path
     *
     * @throws  IOException
     *          if an I/O error occurs writing to or creating the file
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *          将字节写入文件。options参数指定如何创建或打开文件。如果不存在选项，那么该方法就会像存在CREATE、TRUNCATE_EXISTING和WRITE选项一样工作。换句话说，它打开文件进行写入，创建不存在的文件，或者最初将现有的常规文件截断为0。字节数组中的所有字节都被写入文件。该方法确保在写入所有字节(或抛出I/O错误或其他运行时异常)时关闭文件。如果发生I/O错误，那么它可以在文件创建或被截断之后，或者在一些字节被写入文件之后这样做。
    用法示例:默认情况下，该方法创建一个新文件或覆盖现有文件。假设您希望将字节附加到现有文件:
     */
    public static Path write(Path path, byte[] bytes, OpenOption... options)
        throws IOException
    {
        // ensure bytes is not null before opening file
        Objects.requireNonNull(bytes);

        try (OutputStream out = Files.newOutputStream(path, options)) {
            int len = bytes.length;
            int rem = len;
            while (rem > 0) {
                int n = Math.min(rem, BUFFER_SIZE);
                out.write(bytes, (len-rem), n);
                rem -= n;
            }
        }
        return path;
    }

    /**
     * Write lines of text to a file. Each line is a char sequence and is
     * written to the file in sequence with each line terminated by the
     * platform's line separator, as defined by the system property {@code
     * line.separator}. Characters are encoded into bytes using the specified
     * charset.
     *
     * <p> The {@code options} parameter specifies how the the file is created
     * or opened. If no options are present then this method works as if the
     * {@link StandardOpenOption#CREATE CREATE}, {@link
     * StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and {@link
     * StandardOpenOption#WRITE WRITE} options are present. In other words, it
     * opens the file for writing, creating the file if it doesn't exist, or
     * initially truncating an existing {@link #isRegularFile regular-file} to
     * a size of {@code 0}. The method ensures that the file is closed when all
     * lines have been written (or an I/O error or other runtime exception is
     * thrown). If an I/O error occurs then it may do so after the file has
     * created or truncated, or after some bytes have been written to the file.
     *
     * @param   path
     *          the path to the file
     * @param   lines
     *          an object to iterate over the char sequences
     * @param   cs
     *          the charset to use for encoding
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  the path
     *
     * @throws  IOException
     *          if an I/O error occurs writing to or creating the file, or the
     *          text cannot be encoded using the specified charset
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *          将文本行写入文件。每一行都是一个字符序列，并按顺序写入文件，每一行由平台的行分隔符终止，系统属性line.separator定义。字符使用指定的字符集编码成字节。
    options参数指定如何创建或打开文件。如果不存在选项，那么该方法就会像存在CREATE、TRUNCATE_EXISTING和WRITE选项一样工作。换句话说，它打开文件进行写入，创建不存在的文件，或者最初将现有的常规文件截断为0。该方法确保在写入所有行(或抛出I/O错误或其他运行时异常时)关闭文件。如果发生I/O错误，那么它可以在文件创建或被截断之后，或者在一些字节被写入文件之后这样做。
     */
    public static Path write(Path path, Iterable<? extends CharSequence> lines,
                             Charset cs, OpenOption... options)
        throws IOException
    {
        // ensure lines is not null before opening file
        Objects.requireNonNull(lines);
        CharsetEncoder encoder = cs.newEncoder();
        OutputStream out = newOutputStream(path, options);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder))) {
            for (CharSequence line: lines) {
                writer.append(line);
                writer.newLine();
            }
        }
        return path;
    }

    /**
     * Write lines of text to a file. Characters are encoded into bytes using
     * the {@link StandardCharsets#UTF_8 UTF-8} {@link Charset charset}.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <pre>{@code
     * Files.write(path, lines, StandardCharsets.UTF_8, options);
     * }</pre>
     *
     * @param   path
     *          the path to the file
     * @param   lines
     *          an object to iterate over the char sequences
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  the path
     *
     * @throws  IOException
     *          if an I/O error occurs writing to or creating the file, or the
     *          text cannot be encoded as {@code UTF-8}
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file.
     *
     * @since 1.8
     * 将文本行写入文件。字符使用UTF-8字符集编码成字节。
    这个方法的工作原理就好像调用它就等于计算表达式:
     */
    public static Path write(Path path,
                             Iterable<? extends CharSequence> lines,
                             OpenOption... options)
        throws IOException
    {
        return write(path, lines, StandardCharsets.UTF_8, options);
    }

    // -- Stream APIs --

    /**
     * Return a lazily populated {@code Stream}, the elements of
     * which are the entries in the directory.  The listing is not recursive.
     *
     * <p> The elements of the stream are {@link Path} objects that are
     * obtained as if by {@link Path#resolve(Path) resolving} the name of the
     * directory entry against {@code dir}. Some file systems maintain special
     * links to the directory itself and the directory's parent directory.
     * Entries representing these links are not included.
     *
     * <p> The stream is <i>weakly consistent</i>. It is thread safe but does
     * not freeze the directory while iterating, so it may (or may not)
     * reflect updates to the directory that occur after returning from this
     * method.
     *
     * <p> The returned stream encapsulates a {@link DirectoryStream}.
     * If timely disposal of file system resources is required, the
     * {@code try}-with-resources construct should be used to ensure that the
     * stream's {@link Stream#close close} method is invoked after the stream
     * operations are completed.
     *
     * <p> Operating on a closed stream behaves as if the end of stream
     * has been reached. Due to read-ahead, one or more elements may be
     * returned after the stream has been closed.
     *
     * <p> If an {@link IOException} is thrown when accessing the directory
     * after this method has returned, it is wrapped in an {@link
     * UncheckedIOException} which will be thrown from the method that caused
     * the access to take place.
     *
     * @param   dir  The path to the directory
     *
     * @return  The {@code Stream} describing the content of the
     *          directory
     *
     * @throws  NotDirectoryException
     *          if the file could not otherwise be opened because it is not
     *          a directory <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs when opening the directory
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the directory.
     *
     * @see     #newDirectoryStream(Path)
     * @since   1.8
     * 返回一个延迟填充的流，其元素是目录中的条目。清单不是递归的。
    流的元素是路径对象，它们通过解析针对dir的目录条目的名称而获得。有些文件系统维护到目录本身和目录的父目录的特殊链接。不包括表示这些链接的条目。
    这条小溪有微弱的一致性。它是线程安全的，但在迭代时不冻结目录，因此它可能(也可能不)反映从该方法返回后对目录的更新。
    返回的流封装了一个DirectoryStream。如果需要及时地处理文件系统资源，应该使用带有资源的try构造来确保在流操作完成之后调用流的close方法。
    在封闭流上操作，就好像到达了流的末端一样。由于提前读，一个或多个元素可能在流被关闭后返回。
    如果在此方法返回后访问目录时抛出IOException，则将其包装在UncheckedIOException中，该异常将从导致访问发生的方法中抛出。
     */
    public static Stream<Path> list(Path dir) throws IOException {
        DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
        try {
            final Iterator<Path> delegate = ds.iterator();

            // Re-wrap DirectoryIteratorException to UncheckedIOException
            Iterator<Path> it = new Iterator<Path>() {
                @Override
                public boolean hasNext() {
                    try {
                        return delegate.hasNext();
                    } catch (DirectoryIteratorException e) {
                        throw new UncheckedIOException(e.getCause());
                    }
                }
                @Override
                public Path next() {
                    try {
                        return delegate.next();
                    } catch (DirectoryIteratorException e) {
                        throw new UncheckedIOException(e.getCause());
                    }
                }
            };

            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.DISTINCT), false)
                                .onClose(asUncheckedRunnable(ds));
        } catch (Error|RuntimeException e) {
            try {
                ds.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {}
            }
            throw e;
        }
    }

    /**
     * Return a {@code Stream} that is lazily populated with {@code
     * Path} by walking the file tree rooted at a given starting file.  The
     * file tree is traversed <em>depth-first</em>, the elements in the stream
     * are {@link Path} objects that are obtained as if by {@link
     * Path#resolve(Path) resolving} the relative path against {@code start}.
     *
     * <p> The {@code stream} walks the file tree as elements are consumed.
     * The {@code Stream} returned is guaranteed to have at least one
     * element, the starting file itself. For each file visited, the stream
     * attempts to read its {@link BasicFileAttributes}. If the file is a
     * directory and can be opened successfully, entries in the directory, and
     * their <em>descendants</em> will follow the directory in the stream as
     * they are encountered. When all entries have been visited, then the
     * directory is closed. The file tree walk then continues at the next
     * <em>sibling</em> of the directory.
     *
     * <p> The stream is <i>weakly consistent</i>. It does not freeze the
     * file tree while iterating, so it may (or may not) reflect updates to
     * the file tree that occur after returned from this method.
     *
     * <p> By default, symbolic links are not automatically followed by this
     * method. If the {@code options} parameter contains the {@link
     * FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} option then symbolic links are
     * followed. When following links, and the attributes of the target cannot
     * be read, then this method attempts to get the {@code BasicFileAttributes}
     * of the link.
     *
     * <p> If the {@code options} parameter contains the {@link
     * FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} option then the stream keeps
     * track of directories visited so that cycles can be detected. A cycle
     * arises when there is an entry in a directory that is an ancestor of the
     * directory. Cycle detection is done by recording the {@link
     * java.nio.file.attribute.BasicFileAttributes#fileKey file-key} of directories,
     * or if file keys are not available, by invoking the {@link #isSameFile
     * isSameFile} method to test if a directory is the same file as an
     * ancestor. When a cycle is detected it is treated as an I/O error with
     * an instance of {@link FileSystemLoopException}.
     *
     * <p> The {@code maxDepth} parameter is the maximum number of levels of
     * directories to visit. A value of {@code 0} means that only the starting
     * file is visited, unless denied by the security manager. A value of
     * {@link Integer#MAX_VALUE MAX_VALUE} may be used to indicate that all
     * levels should be visited.
     *
     * <p> When a security manager is installed and it denies access to a file
     * (or directory), then it is ignored and not included in the stream.
     *
     * <p> The returned stream encapsulates one or more {@link DirectoryStream}s.
     * If timely disposal of file system resources is required, the
     * {@code try}-with-resources construct should be used to ensure that the
     * stream's {@link Stream#close close} method is invoked after the stream
     * operations are completed.  Operating on a closed stream will result in an
     * {@link java.lang.IllegalStateException}.
     *
     * <p> If an {@link IOException} is thrown when accessing the directory
     * after this method has returned, it is wrapped in an {@link
     * UncheckedIOException} which will be thrown from the method that caused
     * the access to take place.
     *
     * @param   start
     *          the starting file
     * @param   maxDepth
     *          the maximum number of directory levels to visit
     * @param   options
     *          options to configure the traversal
     *
     * @return  the {@link Stream} of {@link Path}
     *
     * @throws  IllegalArgumentException
     *          if the {@code maxDepth} parameter is negative
     * @throws  SecurityException
     *          If the security manager denies access to the starting file.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String) checkRead} method is invoked
     *          to check read access to the directory.
     * @throws  IOException
     *          if an I/O error is thrown when accessing the starting file.
     * @since   1.8
     * 返回一个延迟填充路径的流，方法是遍历位于给定起始文件的文件树。文件树是遍历深度优先级的，流中的元素是获得的路径对象，就像通过解析相对路径开始时获得的一样。
    当使用元素时，流将遍历文件树。返回的流保证至少有一个元素，即开始文件本身。对于访问的每个文件，流尝试读取它的BasicFileAttributes。如果该文件是一个目录，并且可以成功打开，那么目录中的条目以及它们的子代将在遇到流时跟踪流中的目录。当所有条目都已被访问时，目录将被关闭。然后，文件树遍历将继续位于目录的下一个兄弟节点。
    这条小溪有微弱的一致性。它在迭代时不会冻结文件树，因此它可能(也可能不会)反映从该方法返回后对文件树的更新。
    默认情况下，符号链接不会自动跟随此方法。如果选项参数包含FOLLOW_LINKS选项，则遵循符号链接。当跟踪链接时，不能读取目标的属性，然后该方法尝试获取链接的BasicFileAttributes。
    如果options参数包含FOLLOW_LINKS选项，则流将跟踪所访问的目录，以便可以检测循环。当目录中有一个条目是目录的祖先时，就会出现一个循环。循环检测是通过记录目录的文件键，或者如果文件键不可用，通过调用isSameFile方法来测试目录是否与祖先相同的文件。当检测到一个循环时，它将被视为一个I/O错误，并带有FileSystemLoopException的实例。
    maxDepth参数是访问目录的最大级别。值为0意味着只访问起始文件，除非安全管理器拒绝访问。MAX_VALUE的值可以用来表示应该访问所有级别。
    当安装了安全管理器并拒绝对文件(或目录)的访问时，它将被忽略，不包含在流中。
    返回的流封装了一个或多个DirectoryStreams。如果需要及时地处理文件系统资源，应该使用带有资源的try构造来确保在流操作完成之后调用流的close方法。在封闭的流上操作将导致非法的stateexception异常。
    如果在此方法返回后访问目录时抛出IOException，则将其包装在UncheckedIOException中，该异常将从导致访问发生的方法中抛出。
     */
    public static Stream<Path> walk(Path start,
                                    int maxDepth,
                                    FileVisitOption... options)
        throws IOException
    {
        FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
        try {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.DISTINCT), false)
                                .onClose(iterator::close)
                                .map(entry -> entry.file());
        } catch (Error|RuntimeException e) {
            iterator.close();
            throw e;
        }
    }

    /**
     * Return a {@code Stream} that is lazily populated with {@code
     * Path} by walking the file tree rooted at a given starting file.  The
     * file tree is traversed <em>depth-first</em>, the elements in the stream
     * are {@link Path} objects that are obtained as if by {@link
     * Path#resolve(Path) resolving} the relative path against {@code start}.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <blockquote><pre>
     * walk(start, Integer.MAX_VALUE, options)
     * </pre></blockquote>
     * In other words, it visits all levels of the file tree.
     *
     * <p> The returned stream encapsulates one or more {@link DirectoryStream}s.
     * If timely disposal of file system resources is required, the
     * {@code try}-with-resources construct should be used to ensure that the
     * stream's {@link Stream#close close} method is invoked after the stream
     * operations are completed.  Operating on a closed stream will result in an
     * {@link java.lang.IllegalStateException}.
     *
     * @param   start
     *          the starting file
     * @param   options
     *          options to configure the traversal
     *
     * @return  the {@link Stream} of {@link Path}
     *
     * @throws  SecurityException
     *          If the security manager denies access to the starting file.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String) checkRead} method is invoked
     *          to check read access to the directory.
     * @throws  IOException
     *          if an I/O error is thrown when accessing the starting file.
     *
     * @see     #walk(Path, int, FileVisitOption...)
     * @since   1.8
     * 返回一个延迟填充路径的流，方法是遍历位于给定起始文件的文件树。文件树是遍历深度优先级的，流中的元素是获得的路径对象，就像通过解析相对路径开始时获得的一样。
    这个方法的工作原理就好像调用它就等于计算表达式:
    换句话说，它访问文件树的所有级别。
    返回的流封装了一个或多个DirectoryStreams。如果需要及时地处理文件系统资源，应该使用带有资源的try构造来确保在流操作完成之后调用流的close方法。在封闭的流上操作将导致非法的stateexception异常。
     */
    public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
        return walk(start, Integer.MAX_VALUE, options);
    }

    /**
     * Return a {@code Stream} that is lazily populated with {@code
     * Path} by searching for files in a file tree rooted at a given starting
     * file.
     *
     * <p> This method walks the file tree in exactly the manner specified by
     * the {@link #walk walk} method. For each file encountered, the given
     * {@link BiPredicate} is invoked with its {@link Path} and {@link
     * BasicFileAttributes}. The {@code Path} object is obtained as if by
     * {@link Path#resolve(Path) resolving} the relative path against {@code
     * start} and is only included in the returned {@link Stream} if
     * the {@code BiPredicate} returns true. Compare to calling {@link
     * java.util.stream.Stream#filter filter} on the {@code Stream}
     * returned by {@code walk} method, this method may be more efficient by
     * avoiding redundant retrieval of the {@code BasicFileAttributes}.
     *
     * <p> The returned stream encapsulates one or more {@link DirectoryStream}s.
     * If timely disposal of file system resources is required, the
     * {@code try}-with-resources construct should be used to ensure that the
     * stream's {@link Stream#close close} method is invoked after the stream
     * operations are completed.  Operating on a closed stream will result in an
     * {@link java.lang.IllegalStateException}.
     *
     * <p> If an {@link IOException} is thrown when accessing the directory
     * after returned from this method, it is wrapped in an {@link
     * UncheckedIOException} which will be thrown from the method that caused
     * the access to take place.
     *
     * @param   start
     *          the starting file
     * @param   maxDepth
     *          the maximum number of directory levels to search
     * @param   matcher
     *          the function used to decide whether a file should be included
     *          in the returned stream
     * @param   options
     *          options to configure the traversal
     *
     * @return  the {@link Stream} of {@link Path}
     *
     * @throws  IllegalArgumentException
     *          if the {@code maxDepth} parameter is negative
     * @throws  SecurityException
     *          If the security manager denies access to the starting file.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String) checkRead} method is invoked
     *          to check read access to the directory.
     * @throws  IOException
     *          if an I/O error is thrown when accessing the starting file.
     *
     * @see     #walk(Path, int, FileVisitOption...)
     * @since   1.8
     * 返回一个流，该流通过搜索位于给定起始文件的文件树中的文件来惰性地填充路径。
    该方法以遍历方法指定的方式遍历文件树。对于所遇到的每个文件，都使用它的路径和BasicFileAttributes来调用给定的双谓词。路径对象通过解析相对路径来获得，并且只有在双谓词返回true时才包含在返回的流中。与在walk方法返回的流上调用filter相比，该方法可以避免对BasicFileAttributes进行冗余检索，从而提高效率。
    返回的流封装了一个或多个DirectoryStreams。如果需要及时地处理文件系统资源，应该使用带有资源的try构造来确保在流操作完成之后调用流的close方法。在封闭的流上操作将导致非法的stateexception异常。
    如果在从该方法返回后访问目录时抛出IOException，则将其包装在UncheckedIOException中，该异常将从导致访问发生的方法中抛出。
     */
    public static Stream<Path> find(Path start,
                                    int maxDepth,
                                    BiPredicate<Path, BasicFileAttributes> matcher,
                                    FileVisitOption... options)
        throws IOException
    {
        FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
        try {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.DISTINCT), false)
                                .onClose(iterator::close)
                                .filter(entry -> matcher.test(entry.file(), entry.attributes()))
                                .map(entry -> entry.file());
        } catch (Error|RuntimeException e) {
            iterator.close();
            throw e;
        }
    }

    /**
     * Read all lines from a file as a {@code Stream}. Unlike {@link
     * #readAllLines(Path, Charset) readAllLines}, this method does not read
     * all lines into a {@code List}, but instead populates lazily as the stream
     * is consumed.
     *
     * <p> Bytes from the file are decoded into characters using the specified
     * charset and the same line terminators as specified by {@code
     * readAllLines} are supported.
     *
     * <p> After this method returns, then any subsequent I/O exception that
     * occurs while reading from the file or when a malformed or unmappable byte
     * sequence is read, is wrapped in an {@link UncheckedIOException} that will
     * be thrown from the
     * {@link java.util.stream.Stream} method that caused the read to take
     * place. In case an {@code IOException} is thrown when closing the file,
     * it is also wrapped as an {@code UncheckedIOException}.
     *
     * <p> The returned stream encapsulates a {@link Reader}.  If timely
     * disposal of file system resources is required, the try-with-resources
     * construct should be used to ensure that the stream's
     * {@link Stream#close close} method is invoked after the stream operations
     * are completed.
     *
     *
     * @param   path
     *          the path to the file
     * @param   cs
     *          the charset to use for decoding
     *
     * @return  the lines from the file as a {@code Stream}
     *
     * @throws  IOException
     *          if an I/O error occurs opening the file
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @see     #readAllLines(Path, Charset)
     * @see     #newBufferedReader(Path, Charset)
     * @see     java.io.BufferedReader#lines()
     * @since   1.8
     *
    将文件中的所有行作为流进行读取。与readAllLines不同，此方法不将所有行读入列表，而是在流被消费时缓慢地填充。
    使用指定的字符集将文件中的字节解码为字符，并支持readAllLines指定的相同行终止符。
    在此方法返回之后，在读取文件时发生的任何后续的I/O异常，或者读取错误的或不可映射的字节序列时，都将被包装在一个UncheckedIOException中，该异常将从导致该读取发生的流方法中抛出。如果在关闭文件时抛出IOException，它也被包装为UncheckedIOException。
    返回的流封装了一个阅读器。如果需要及时地处理文件系统资源，应该使用带有资源的try构造来确保在流操作完成之后调用流的close方法。
     */
    public static Stream<String> lines(Path path, Charset cs) throws IOException {
        BufferedReader br = Files.newBufferedReader(path, cs);
        try {
            return br.lines().onClose(asUncheckedRunnable(br));
        } catch (Error|RuntimeException e) {
            try {
                br.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {}
            }
            throw e;
        }
    }

    /**
     * Read all lines from a file as a {@code Stream}. Bytes from the file are
     * decoded into characters using the {@link StandardCharsets#UTF_8 UTF-8}
     * {@link Charset charset}.
     *
     * <p> This method works as if invoking it were equivalent to evaluating the
     * expression:
     * <pre>{@code
     * Files.lines(path, StandardCharsets.UTF_8)
     * }</pre>
     *
     * @param   path
     *          the path to the file
     *
     * @return  the lines from the file as a {@code Stream}
     *
     * @throws  IOException
     *          if an I/O error occurs opening the file
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @since 1.8
     * 将文件中的所有行作为流进行读取。使用UTF-8字符集将文件中的字节解码为字符。
    这个方法的工作原理就好像调用它就等于计算表达式:
    文件。行(路径,StandardCharsets.UTF_8)
     */
    public static Stream<String> lines(Path path) throws IOException {
        return lines(path, StandardCharsets.UTF_8);
    }
}
