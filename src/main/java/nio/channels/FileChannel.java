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

package java.nio.channels;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * A channel for reading, writing, mapping, and manipulating a file.
 *
 * <p> A file channel is a {@link SeekableByteChannel} that is connected to
 * a file. It has a current <i>position</i> within its file which can
 * be both {@link #position() <i>queried</i>} and {@link #position(long)
 * <i>modified</i>}.  The file itself contains a variable-length sequence
 * of bytes that can be read and written and whose current {@link #size
 * <i>size</i>} can be queried.  The size of the file increases
 * when bytes are written beyond its current size; the size of the file
 * decreases when it is {@link #truncate <i>truncated</i>}.  The
 * file may also have some associated <i>metadata</i> such as access
 * permissions, content type, and last-modification time; this class does not
 * define methods for metadata access.
 *
 * <p> In addition to the familiar read, write, and close operations of byte
 * channels, this class defines the following file-specific operations: </p>
 *
 * <ul>
 *
 *   <li><p> Bytes may be {@link #read(ByteBuffer, long) read} or
 *   {@link #write(ByteBuffer, long) <i>written</i>} at an absolute
 *   position in a file in a way that does not affect the channel's current
 *   position.  </p></li>
 *
 *   <li><p> A region of a file may be {@link #map <i>mapped</i>}
 *   directly into memory; for large files this is often much more efficient
 *   than invoking the usual <tt>read</tt> or <tt>write</tt> methods.
 *   </p></li>
 *
 *   <li><p> Updates made to a file may be {@link #force <i>forced
 *   out</i>} to the underlying storage device, ensuring that data are not
 *   lost in the event of a system crash.  </p></li>
 *
 *   <li><p> Bytes can be transferred from a file {@link #transferTo <i>to
 *   some other channel</i>}, and {@link #transferFrom <i>vice
 *   versa</i>}, in a way that can be optimized by many operating systems
 *   into a very fast transfer directly to or from the filesystem cache.
 *   </p></li>
 *
 *   <li><p> A region of a file may be {@link FileLock <i>locked</i>}
 *   against access by other programs.  </p></li>
 *
 * </ul>
 *
 * <p> File channels are safe for use by multiple concurrent threads.  The
 * {@link Channel#close close} method may be invoked at any time, as specified
 * by the {@link Channel} interface.  Only one operation that involves the
 * channel's position or can change its file's size may be in progress at any
 * given time; attempts to initiate a second such operation while the first is
 * still in progress will block until the first operation completes.  Other
 * operations, in particular those that take an explicit position, may proceed
 * concurrently; whether they in fact do so is dependent upon the underlying
 * implementation and is therefore unspecified.
 *
 * <p> The view of a file provided by an instance of this class is guaranteed
 * to be consistent with other views of the same file provided by other
 * instances in the same program.  The view provided by an instance of this
 * class may or may not, however, be consistent with the views seen by other
 * concurrently-running programs due to caching performed by the underlying
 * operating system and delays induced by network-filesystem protocols.  This
 * is true regardless of the language in which these other programs are
 * written, and whether they are running on the same machine or on some other
 * machine.  The exact nature of any such inconsistencies are system-dependent
 * and are therefore unspecified.
 *
 * <p> A file channel is created by invoking one of the {@link #open open}
 * methods defined by this class. A file channel can also be obtained from an
 * existing {@link java.io.FileInputStream#getChannel FileInputStream}, {@link
 * java.io.FileOutputStream#getChannel FileOutputStream}, or {@link
 * java.io.RandomAccessFile#getChannel RandomAccessFile} object by invoking
 * that object's <tt>getChannel</tt> method, which returns a file channel that
 * is connected to the same underlying file. Where the file channel is obtained
 * from an existing stream or random access file then the state of the file
 * channel is intimately connected to that of the object whose <tt>getChannel</tt>
 * method returned the channel.  Changing the channel's position, whether
 * explicitly or by reading or writing bytes, will change the file position of
 * the originating object, and vice versa. Changing the file's length via the
 * file channel will change the length seen via the originating object, and vice
 * versa.  Changing the file's content by writing bytes will change the content
 * seen by the originating object, and vice versa.
 *
 * <a name="open-mode"></a> <p> At various points this class specifies that an
 * instance that is "open for reading," "open for writing," or "open for
 * reading and writing" is required.  A channel obtained via the {@link
 * java.io.FileInputStream#getChannel getChannel} method of a {@link
 * java.io.FileInputStream} instance will be open for reading.  A channel
 * obtained via the {@link java.io.FileOutputStream#getChannel getChannel}
 * method of a {@link java.io.FileOutputStream} instance will be open for
 * writing.  Finally, a channel obtained via the {@link
 * java.io.RandomAccessFile#getChannel getChannel} method of a {@link
 * java.io.RandomAccessFile} instance will be open for reading if the instance
 * was created with mode <tt>"r"</tt> and will be open for reading and writing
 * if the instance was created with mode <tt>"rw"</tt>.
 *
 * <a name="append-mode"></a><p> A file channel that is open for writing may be in
 * <i>append mode</i>, for example if it was obtained from a file-output stream
 * that was created by invoking the {@link
 * java.io.FileOutputStream#FileOutputStream(java.io.File,boolean)
 * FileOutputStream(File,boolean)} constructor and passing <tt>true</tt> for
 * the second parameter.  In this mode each invocation of a relative write
 * operation first advances the position to the end of the file and then writes
 * the requested data.  Whether the advancement of the position and the writing
 * of the data are done in a single atomic operation is system-dependent and
 * therefore unspecified.
 *
 * @see java.io.FileInputStream#getChannel()
 * @see java.io.FileOutputStream#getChannel()
 * @see java.io.RandomAccessFile#getChannel()
 *
 * @author Mark Reinhold
 * @author Mike McCloskey
 * @author JSR-51 Expert Group
 * @since 1.4
 * 用于读取、写入、映射和操作文件的通道。
文件通道是连接到文件的SeekableByteChannel。它的文件中有一个当前位置，可以进行查询和修改。该文件本身包含一个可变长度的字节序列，可以读取和写入，并且可以查询当前大小。当字节写入超过当前大小时，文件的大小会增加;当文件被截断时，它的大小会减少。该文件还可能具有一些相关的元数据，如访问权限、内容类型和最后修改时间;这个类不定义元数据访问的方法。
除了熟悉的字节通道的读、写和关闭操作之外，这个类还定义了以下特定于文件的操作:
字节可以以不影响通道当前位置的方式在文件中的绝对位置读取或写入。
文件的一个区域可以直接映射到内存中;对于大型文件，这通常比调用通常的读或写方法要高效得多。
对文件进行的更新可能会被强制到底层存储设备，以确保在系统崩溃时数据不会丢失。
字节可以从文件传输到另一个通道，反之亦然，许多操作系统可以通过这种方式将字节优化为直接传输到文件系统缓存或从文件系统缓存快速传输。
文件的一个区域可以被锁定以防止其他程序访问。
文件通道对于多个并发线程来说是安全的。根据通道接口的指定，可以在任何时候调用close方法。在任何给定的时间，只有一个操作涉及通道的位置或可以更改其文件的大小;当第一个操作仍在进行时，尝试启动第二个操作将被阻塞，直到第一个操作完成。其他行动，特别是采取明确立场的行动，可以同时进行;实际上，它们是否这样做取决于底层实现，因此不确定。
该类实例提供的文件的视图保证与同一程序中其他实例提供的相同文件的其他视图一致。但是，这个类的实例提供的视图可能与其他同步运行的程序看到的视图一致，也可能不一致，这是由于底层操作系统执行的缓存和网络文件系统协议引起的延迟。不管其他程序是用什么语言编写的，也不管它们是在同一台机器上运行还是在其他机器上运行，都是如此。任何这种不一致的确切性质是与系统有关的，因此是不明确的。
通过调用这个类定义的开放方法之一创建文件通道。通过调用该对象的getChannel方法，也可以从现有的FileInputStream、FileOutputStream或RandomAccessFile对象中获取文件通道，该方法返回连接到同一底层文件的文件通道。如果文件通道是从一个现有的流或随机访问文件中获取的，那么文件通道的状态就与返回通道的getChannel方法的对象的状态密切相关。改变通道的位置，无论是显式的还是读取或写入字节，都会改变原始对象的文件位置，反之亦然。通过文件通道更改文件的长度将更改通过原始对象看到的长度，反之亦然。通过写入字节来更改文件的内容将更改原始对象看到的内容，反之亦然。
在不同的点上，该类指定一个实例是“为读打开”、“为写打开”或“为读和写打开”。通过FileInputStream实例的getChannel方法获得的通道将打开以进行读取。通过FileOutputStream实例的getChannel方法获得的通道将打开以供编写。最后，如果用“r”模式创建实例，则通过RandomAccessFile实例的getChannel方法获得的通道将打开以进行读取，如果用“rw”模式创建实例，则打开通道进行读取和写入。
一个用于写入的文件通道可能在append模式中，例如，如果它是通过调用FileOutputStream(文件，布尔)构造函数而创建的文件输出流，并将true传递给第二个参数。在这种模式下，每个相对写入操作的调用首先将位置移动到文件的末尾，然后写入所请求的数据。位置的提升和数据的写入是否在单个原子操作中完成，这与系统相关，因此不确定。
 */
//用于读取、写入、映射和操作文件的通道，文件通道对于多个并发线程来说是安全的，通过调用该对象的getChannel方法，也可以从现有的FileInputStream、FileOutputStream或RandomAccessFile对象中获取文件通道
public abstract class FileChannel
    extends AbstractInterruptibleChannel
    implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel
{
    /**
     * Initializes a new instance of this class.
     */
    protected FileChannel() { }

    /**
     * Opens or creates a file, returning a file channel to access the file.
     *
     * <p> The {@code options} parameter determines how the file is opened.
     * The {@link StandardOpenOption#READ READ} and {@link StandardOpenOption#WRITE
     * WRITE} options determine if the file should be opened for reading and/or
     * writing. If neither option (or the {@link StandardOpenOption#APPEND APPEND}
     * option) is contained in the array then the file is opened for reading.
     * By default reading or writing commences at the beginning of the file.
     *
     * <p> In the addition to {@code READ} and {@code WRITE}, the following
     * options may be present:
     *
     * <table border=1 cellpadding=5 summary="">
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
     *   the file already exists. When creating a file the check for the
     *   existence of the file and the creation of the file if it does not exist
     *   is atomic with respect to other file system operations. This option is
     *   ignored when the file is opened only for reading. </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#CREATE CREATE} </td>
     *   <td> If this option is present then an existing file is opened if it
     *   exists, otherwise a new file is created. When creating a file the check
     *   for the existence of the file and the creation of the file if it does
     *   not exist is atomic with respect to other file system operations. This
     *   option is ignored if the {@code CREATE_NEW} option is also present or
     *   the file is opened only for reading. </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} </td>
     *   <td> When this option is present then the implementation makes a
     *   <em>best effort</em> attempt to delete the file when closed by the
     *   the {@link #close close} method. If the {@code close} method is not
     *   invoked then a <em>best effort</em> attempt is made to delete the file
     *   when the Java virtual machine terminates. </td>
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
     *   href="../file/package-summary.html#integrity"> Synchronized I/O file
     *   integrity</a>). </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#DSYNC DSYNC} </td>
     *   <td> Requires that every update to the file's content be written
     *   synchronously to the underlying storage device. (see <a
     *   href="../file/package-summary.html#integrity"> Synchronized I/O file
     *   integrity</a>). </td>
     * </tr>
     * </table>
     *
     * <p> An implementation may also support additional options.
     *
     * <p> The {@code attrs} parameter is an optional array of file {@link
     * FileAttribute file-attributes} to set atomically when creating the file.
     *
     * <p> The new channel is created by invoking the {@link
     * FileSystemProvider#newFileChannel newFileChannel} method on the
     * provider that created the {@code Path}.
     *
     * @param   path
     *          The path of the file to open or create
     * @param   options
     *          Options specifying how the file is opened
     * @param   attrs
     *          An optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  A new file channel
     *
     * @throws  IllegalArgumentException
     *          If the set contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          If the {@code path} is associated with a provider that does not
     *          support creating file channels, or an unsupported open option is
     *          specified, or the array contains an attribute that cannot be set
     *          atomically when creating the file
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is installed and it denies an
     *          unspecified permission required by the implementation.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String)} method is invoked to check
     *          read access if the file is opened for reading. The {@link
     *          SecurityManager#checkWrite(String)} method is invoked to check
     *          write access if the file is opened for writing
     *
     * @since   1.7
     * 打开或创建一个文件，返回一个文件通道以访问该文件。
    options参数确定如何打开文件。读和写选项决定是否应该为读取和/或写入打开文件。如果数组中不包含任何选项(或APPEND选项)，则打开文件进行读取。默认情况下，读或写从文件的开头开始。
    除了读和写之外，还可以提供以下选项:
    选项
    描述
    附加
    如果存在此选项，则打开文件以进行写入，通道的写方法的每次调用首先将位置推进到文件的末尾，然后写入所请求的数据。位置的提升和数据的写入是否在单个原子操作中完成，这与系统相关，因此不确定。此选项不能与READ或TRUNCATE_EXISTING选项一起使用。
    TRUNCATE_EXISTING
    如果此选项存在，那么现有文件将被截断为0字节。当文件仅为读取而打开时，将忽略此选项。
    CREATE_NEW
    如果此选项出现，则会创建一个新文件，如果文件已经存在，则会失败。当创建文件时，检查文件是否存在，如果文件不存在，则创建文件，这是相对于其他文件系统操作的原子。当文件仅为读取而打开时，将忽略此选项。
    创建
    如果这个选项存在，那么如果存在一个现有的文件，那么就会创建一个新的文件。在创建文件时，检查文件是否存在，如果文件不存在，则对其他文件系统操作进行原子检查。如果CREATE_NEW选项也存在，或者文件只打开用于读取，则此选项将被忽略。
    DELETE_ON_CLOSE
    当此选项出现时，实现将尽力尝试在关闭时删除该文件。如果不调用close方法，那么在Java虚拟机终止时，将尽力删除文件。
    稀疏的
    在创建新文件时，此选项提示新文件将是稀疏的。此选项在不创建新文件时被忽略。
    同步
    要求对文件内容或元数据的每次更新都要同步地写入底层存储设备。(参见同步I/O文件完整性)。
    DSYNC
    要求对文件内容的每次更新都同步写入底层存储设备。(参见同步I/O文件完整性)。
    实现还可以支持其他选项。
    attrs参数是一个可选的文件文件属性数组，可以在创建文件时自动设置。
    通过在创建路径的提供程序上调用newFileChannel方法创建新通道。
     */
    public static FileChannel open(Path path,
                                   Set<? extends OpenOption> options,
                                   FileAttribute<?>... attrs)
        throws IOException
    {
        FileSystemProvider provider = path.getFileSystem().provider();
        return provider.newFileChannel(path, options, attrs);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // generic array construction
    private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

    /**
     * Opens or creates a file, returning a file channel to access the file.
     *
     * <p> An invocation of this method behaves in exactly the same way as the
     * invocation
     * <pre>
     *     fc.{@link #open(Path,Set,FileAttribute[]) open}(file, opts, new FileAttribute&lt;?&gt;[0]);
     * </pre>
     * where {@code opts} is a set of the options specified in the {@code
     * options} array.
     *
     * @param   path
     *          The path of the file to open or create
     * @param   options
     *          Options specifying how the file is opened
     *
     * @return  A new file channel
     *
     * @throws  IllegalArgumentException
     *          If the set contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          If the {@code path} is associated with a provider that does not
     *          support creating file channels, or an unsupported open option is
     *          specified
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is installed and it denies an
     *          unspecified permission required by the implementation.
     *          In the case of the default provider, the {@link
     *          SecurityManager#checkRead(String)} method is invoked to check
     *          read access if the file is opened for reading. The {@link
     *          SecurityManager#checkWrite(String)} method is invoked to check
     *          write access if the file is opened for writing
     *
     * @since   1.7
     * 打开或创建一个文件，返回一个文件通道以访问该文件。
    此方法的调用行为与调用完全相同
    fc。打开(文件、选择、新FileAttribute < ? >[0]);

    其中，opts是选项数组中指定的一组选项。
     */
    public static FileChannel open(Path path, OpenOption... options)
        throws IOException
    {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return open(path, set, NO_ATTRIBUTES);
    }

    // -- Channel operations --

    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> Bytes are read starting at this channel's current file position, and
     * then the file position is updated with the number of bytes actually
     * read.  Otherwise this method behaves exactly as specified in the {@link
     * ReadableByteChannel} interface. </p>
     * 将该通道中的字节序列读入给定的缓冲区。
     从通道当前文件位置开始读取字节，然后用实际读取的字节数更新文件位置。否则，此方法的行为与ReadableByteChannel接口中指定的完全一样。
     */
//    从通道当前文件位置开始读取字节，然后用实际读取的字节数更新文件位置
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * Reads a sequence of bytes from this channel into a subsequence of the
     * given buffers.
     *
     * <p> Bytes are read starting at this channel's current file position, and
     * then the file position is updated with the number of bytes actually
     * read.  Otherwise this method behaves exactly as specified in the {@link
     * ScatteringByteChannel} interface.  </p>
     * 将该通道中的字节序列读入给定缓冲区的子序列。
     从通道当前文件位置开始读取字节，然后用实际读取的字节数更新文件位置。否则，此方法的行为与散在bytechannel接口中指定的完全相同。
     */
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * Reads a sequence of bytes from this channel into the given buffers.
     *
     * <p> Bytes are read starting at this channel's current file position, and
     * then the file position is updated with the number of bytes actually
     * read.  Otherwise this method behaves exactly as specified in the {@link
     * ScatteringByteChannel} interface.  </p>
     * 将该通道中的字节序列读入给定的缓冲区。
     从通道当前文件位置开始读取字节，然后用实际读取的字节数更新文件位置。否则，此方法的行为与散在bytechannel接口中指定的完全相同。
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> Bytes are written starting at this channel's current file position
     * unless the channel is in append mode, in which case the position is
     * first advanced to the end of the file.  The file is grown, if necessary,
     * to accommodate the written bytes, and then the file position is updated
     * with the number of bytes actually written.  Otherwise this method
     * behaves exactly as specified by the {@link WritableByteChannel}
     * interface. </p>
     * 从给定的缓冲区向该通道写入字节序列。
     在这个通道的当前文件位置上写入字节，除非通道处于追加模式中，在这种情况下，该位置首先被推进到文件的末尾。如果需要的话，文件将被扩展，以适应写入的字节，然后文件位置将根据实际写入的字节数进行更新。否则，该方法的行为完全由WritableByteChannel接口指定。
     */
//    在这个通道的当前文件位置上写入字节，除非通道处于追加模式中
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * Writes a sequence of bytes to this channel from a subsequence of the
     * given buffers.
     *
     * <p> Bytes are written starting at this channel's current file position
     * unless the channel is in append mode, in which case the position is
     * first advanced to the end of the file.  The file is grown, if necessary,
     * to accommodate the written bytes, and then the file position is updated
     * with the number of bytes actually written.  Otherwise this method
     * behaves exactly as specified in the {@link GatheringByteChannel}
     * interface.  </p>
     * 从给定缓冲区的子序列向该通道写入字节序列。
     在这个通道的当前文件位置上写入字节，除非通道处于追加模式中，在这种情况下，该位置首先被推进到文件的末尾。如果需要的话，文件将被扩展，以适应写入的字节，然后文件位置将根据实际写入的字节数进行更新。否则，此方法的行为与在collect bytechannel接口中指定的完全一样。
     */
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

    /**
     * Writes a sequence of bytes to this channel from the given buffers.
     *
     * <p> Bytes are written starting at this channel's current file position
     * unless the channel is in append mode, in which case the position is
     * first advanced to the end of the file.  The file is grown, if necessary,
     * to accommodate the written bytes, and then the file position is updated
     * with the number of bytes actually written.  Otherwise this method
     * behaves exactly as specified in the {@link GatheringByteChannel}
     * interface.  </p>
     * 从给定的缓冲区向该通道写入字节序列。
     在这个通道的当前文件位置上写入字节，除非通道处于追加模式中，在这种情况下，该位置首先被推进到文件的末尾。如果需要的话，文件将被扩展，以适应写入的字节，然后文件位置将根据实际写入的字节数进行更新。否则，此方法的行为与在collect bytechannel接口中指定的完全一样。
     */
    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }


    // -- Other operations --

    /**
     * Returns this channel's file position.返回该通道的文件位置。
     *
     * @return  This channel's file position,
     *          a non-negative integer counting the number of bytes
     *          from the beginning of the file to the current position
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  IOException
     *          If some other I/O error occurs
     */
    public abstract long position() throws IOException;

    /**
     * Sets this channel's file position.
     *
     * <p> Setting the position to a value that is greater than the file's
     * current size is legal but does not change the size of the file.  A later
     * attempt to read bytes at such a position will immediately return an
     * end-of-file indication.  A later attempt to write bytes at such a
     * position will cause the file to be grown to accommodate the new bytes;
     * the values of any bytes between the previous end-of-file and the
     * newly-written bytes are unspecified.  </p>
     *
     * @param  newPosition
     *         The new position, a non-negative integer counting
     *         the number of bytes from the beginning of the file
     *
     * @return  This file channel
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  IllegalArgumentException
     *          If the new position is negative
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          设置该通道的文件位置。
    将位置设置为大于文件当前大小的值是合法的，但不会更改文件的大小。稍后在这种位置读取字节的尝试将立即返回文件结束指示。稍后尝试在这样的位置上写入字节，将导致该文件被扩展以容纳新的字节;前一个文件末端和新写入的字节之间的任何字节的值都是不指定的。
     */
    public abstract FileChannel position(long newPosition) throws IOException;

    /**
     * Returns the current size of this channel's file.
     *
     * @return  The current size of this channel's file,
     *          measured in bytes
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          返回该通道文件的当前大小。
     */
    public abstract long size() throws IOException;

    /**
     * Truncates this channel's file to the given size.
     *
     * <p> If the given size is less than the file's current size then the file
     * is truncated, discarding any bytes beyond the new end of the file.  If
     * the given size is greater than or equal to the file's current size then
     * the file is not modified.  In either case, if this channel's file
     * position is greater than the given size then it is set to that size.
     * </p>
     *
     * @param  size
     *         The new size, a non-negative byte count
     *
     * @return  This file channel
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  IllegalArgumentException
     *          If the new size is negative
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          将该通道的文件截断为给定的大小。
    如果给定的大小小于文件的当前大小，那么文件将被截断，丢弃文件新端之外的任何字节。如果给定的大小大于或等于文件的当前大小，那么文件就不会被修改。无论哪种情况，如果该通道的文件位置大于给定的大小，则将其设置为该大小。
     */
//    将该通道的文件截断为给定的大小
    public abstract FileChannel truncate(long size) throws IOException;

    /**
     * Forces any updates to this channel's file to be written to the storage
     * device that contains it.
     *
     * <p> If this channel's file resides on a local storage device then when
     * this method returns it is guaranteed that all changes made to the file
     * since this channel was created, or since this method was last invoked,
     * will have been written to that device.  This is useful for ensuring that
     * critical information is not lost in the event of a system crash.
     *
     * <p> If the file does not reside on a local device then no such guarantee
     * is made.
     *
     * <p> The <tt>metaData</tt> parameter can be used to limit the number of
     * I/O operations that this method is required to perform.  Passing
     * <tt>false</tt> for this parameter indicates that only updates to the
     * file's content need be written to storage; passing <tt>true</tt>
     * indicates that updates to both the file's content and metadata must be
     * written, which generally requires at least one more I/O operation.
     * Whether this parameter actually has any effect is dependent upon the
     * underlying operating system and is therefore unspecified.
     *
     * <p> Invoking this method may cause an I/O operation to occur even if the
     * channel was only opened for reading.  Some operating systems, for
     * example, maintain a last-access time as part of a file's metadata, and
     * this time is updated whenever the file is read.  Whether or not this is
     * actually done is system-dependent and is therefore unspecified.
     *
     * <p> This method is only guaranteed to force changes that were made to
     * this channel's file via the methods defined in this class.  It may or
     * may not force changes that were made by modifying the content of a
     * {@link MappedByteBuffer <i>mapped byte buffer</i>} obtained by
     * invoking the {@link #map map} method.  Invoking the {@link
     * MappedByteBuffer#force force} method of the mapped byte buffer will
     * force changes made to the buffer's content to be written.  </p>
     *
     * @param   metaData
     *          If <tt>true</tt> then this method is required to force changes
     *          to both the file's content and metadata to be written to
     *          storage; otherwise, it need only force content changes to be
     *          written
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          强制将对该通道文件的任何更新写入包含该文件的存储设备。
    如果该通道的文件驻留在本地存储设备上，那么当该方法返回时，就可以保证在创建该通道或上次调用该方法后对该文件所做的所有更改都将写入该设备。这对于确保在系统崩溃时不会丢失关键信息非常有用。
    如果文件不驻留在本地设备上，则不进行此类保证。
    元数据参数可用于限制该方法执行的I/O操作的数量。传递false表示只需要将文件内容的更新写到存储中;传递true意味着必须编写对文件内容和元数据的更新，这通常至少需要一个I/O操作。这个参数是否真的有任何作用取决于底层操作系统，因此没有说明。
    调用此方法可能会导致I/O操作发生，即使通道仅为读取而打开。例如，有些操作系统将最后访问时间作为文件元数据的一部分，每次读取文件时都会更新这个时间。这是否真正实现是与系统相关的，因此是不确定的。
    此方法仅保证强制通过该类中定义的方法对该通道的文件进行更改。它可以或不可以强制通过修改通过调用映射方法获得的映射字节缓冲区的内容所进行的更改。调用映射字节缓冲区的force方法将强制编写对缓冲区内容所做的更改。
     */
//    强制将对该通道文件的任何更新写入包含该文件的存储设备，这对于确保在系统崩溃时不会丢失关键信息非常有用
    public abstract void force(boolean metaData) throws IOException;

    /**
     * Transfers bytes from this channel's file to the given writable byte
     * channel.
     *
     * <p> An attempt is made to read up to <tt>count</tt> bytes starting at
     * the given <tt>position</tt> in this channel's file and write them to the
     * target channel.  An invocation of this method may or may not transfer
     * all of the requested bytes; whether or not it does so depends upon the
     * natures and states of the channels.  Fewer than the requested number of
     * bytes are transferred if this channel's file contains fewer than
     * <tt>count</tt> bytes starting at the given <tt>position</tt>, or if the
     * target channel is non-blocking and it has fewer than <tt>count</tt>
     * bytes free in its output buffer.
     *
     * <p> This method does not modify this channel's position.  If the given
     * position is greater than the file's current size then no bytes are
     * transferred.  If the target channel has a position then bytes are
     * written starting at that position and then the position is incremented
     * by the number of bytes written.
     *
     * <p> This method is potentially much more efficient than a simple loop
     * that reads from this channel and writes to the target channel.  Many
     * operating systems can transfer bytes directly from the filesystem cache
     * to the target channel without actually copying them.  </p>
     *
     * @param  position
     *         The position within the file at which the transfer is to begin;
     *         must be non-negative
     *
     * @param  count
     *         The maximum number of bytes to be transferred; must be
     *         non-negative
     *
     * @param  target
     *         The target channel
     *
     * @return  The number of bytes, possibly zero,
     *          that were actually transferred
     *
     * @throws IllegalArgumentException
     *         If the preconditions on the parameters do not hold
     *
     * @throws  NonReadableChannelException
     *          If this channel was not opened for reading
     *
     * @throws  NonWritableChannelException
     *          If the target channel was not opened for writing
     *
     * @throws  ClosedChannelException
     *          If either this channel or the target channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes either channel
     *          while the transfer is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread while the
     *          transfer is in progress, thereby closing both channels and
     *          setting the current thread's interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          将该通道的文件中的字节传输到给定的可写字节通道。
    尝试从该通道文件的给定位置读取字节数并将其写入目标通道。此方法的调用可以或不可以传输所请求的所有字节;它是否这样做取决于渠道的性质和状态。如果该通道的文件包含的字节数少于从给定位置开始的计数字节数，或者如果目标通道是非阻塞的，并且输出缓冲区中空闲的字节数少于计数字节数，则传输的字节数将少于所请求的字节数。
    此方法不修改该通道的位置。如果给定的位置大于文件的当前大小，则不会传输任何字节。如果目标通道有一个位置，那么字节就从这个位置开始写入，然后位置就会被写入的字节数增加。
    此方法可能比从该通道读取并写入目标通道的简单循环要高效得多。许多操作系统可以直接将字节从文件系统缓存传输到目标通道，而无需实际复制它们。
     */
//    将该通道的文件中的字节传输到给定的可写字节通道，尝试从该通道文件的给定位置读取字节数并将其写入目标通道
    public abstract long transferTo(long position, long count,
                                    WritableByteChannel target)
        throws IOException;

    /**
     * Transfers bytes into this channel's file from the given readable byte
     * channel.
     *
     * <p> An attempt is made to read up to <tt>count</tt> bytes from the
     * source channel and write them to this channel's file starting at the
     * given <tt>position</tt>.  An invocation of this method may or may not
     * transfer all of the requested bytes; whether or not it does so depends
     * upon the natures and states of the channels.  Fewer than the requested
     * number of bytes will be transferred if the source channel has fewer than
     * <tt>count</tt> bytes remaining, or if the source channel is non-blocking
     * and has fewer than <tt>count</tt> bytes immediately available in its
     * input buffer.
     *
     * <p> This method does not modify this channel's position.  If the given
     * position is greater than the file's current size then no bytes are
     * transferred.  If the source channel has a position then bytes are read
     * starting at that position and then the position is incremented by the
     * number of bytes read.
     *
     * <p> This method is potentially much more efficient than a simple loop
     * that reads from the source channel and writes to this channel.  Many
     * operating systems can transfer bytes directly from the source channel
     * into the filesystem cache without actually copying them.  </p>
     *
     * @param  src
     *         The source channel
     *
     * @param  position
     *         The position within the file at which the transfer is to begin;
     *         must be non-negative
     *
     * @param  count
     *         The maximum number of bytes to be transferred; must be
     *         non-negative
     *
     * @return  The number of bytes, possibly zero,
     *          that were actually transferred
     *
     * @throws IllegalArgumentException
     *         If the preconditions on the parameters do not hold
     *
     * @throws  NonReadableChannelException
     *          If the source channel was not opened for reading
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  ClosedChannelException
     *          If either this channel or the source channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes either channel
     *          while the transfer is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread while the
     *          transfer is in progress, thereby closing both channels and
     *          setting the current thread's interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          将字节从给定的可读字节通道传输到该通道的文件中。
    尝试从源通道读取字节数，并将其写入从给定位置开始的通道文件。此方法的调用可以或不可以传输所请求的所有字节;它是否这样做取决于渠道的性质和状态。如果源通道的剩余字节数少于计数字节，或者如果源通道是非阻塞的，并且在其输入缓冲区中立即可用的字节数少于计数字节，那么传输的字节数将少于所请求的字节数。
    此方法不修改该通道的位置。如果给定的位置大于文件的当前大小，则不会传输任何字节。如果源通道有一个位置，则从该位置开始读取字节，然后以读取的字节数递增该位置。
    这个方法可能比从源通道读取并写入该通道的简单循环要高效得多。许多操作系统可以直接将字节从源通道传输到文件系统缓存中，而无需实际复制它们。
     */
//    将字节从给定的可读字节通道传输到该通道的文件中，尝试从源通道读取字节数，并将其写入从给定位置开始的通道文件
    public abstract long transferFrom(ReadableByteChannel src,
                                      long position, long count)
        throws IOException;

    /**
     * Reads a sequence of bytes from this channel into the given buffer,
     * starting at the given file position.
     *
     * <p> This method works in the same manner as the {@link
     * #read(ByteBuffer)} method, except that bytes are read starting at the
     * given file position rather than at the channel's current position.  This
     * method does not modify this channel's position.  If the given position
     * is greater than the file's current size then no bytes are read.  </p>
     *
     * @param  dst
     *         The buffer into which bytes are to be transferred
     *
     * @param  position
     *         The file position at which the transfer is to begin;
     *         must be non-negative
     *
     * @return  The number of bytes read, possibly zero, or <tt>-1</tt> if the
     *          given position is greater than or equal to the file's current
     *          size
     *
     * @throws  IllegalArgumentException
     *          If the position is negative
     *
     * @throws  NonReadableChannelException
     *          If this channel was not opened for reading
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the read operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the read operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          从这个通道读取字节序列到给定的缓冲区，从给定的文件位置开始。
    这个方法的工作方式与read(ByteBuffer)方法相同，只是从给定的文件位置开始读取字节，而不是在通道的当前位置。此方法不修改该通道的位置。如果给定的位置大于文件的当前大小，则不会读取任何字节。
     */
//    从这个通道读取字节序列到给定的缓冲区，从给定的文件位置开始
    public abstract int read(ByteBuffer dst, long position) throws IOException;

    /**
     * Writes a sequence of bytes to this channel from the given buffer,
     * starting at the given file position.
     *
     * <p> This method works in the same manner as the {@link
     * #write(ByteBuffer)} method, except that bytes are written starting at
     * the given file position rather than at the channel's current position.
     * This method does not modify this channel's position.  If the given
     * position is greater than the file's current size then the file will be
     * grown to accommodate the new bytes; the values of any bytes between the
     * previous end-of-file and the newly-written bytes are unspecified.  </p>
     *
     * @param  src
     *         The buffer from which bytes are to be transferred
     *
     * @param  position
     *         The file position at which the transfer is to begin;
     *         must be non-negative
     *
     * @return  The number of bytes written, possibly zero
     *
     * @throws  IllegalArgumentException
     *          If the position is negative
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel
     *          while the write operation is in progress
     *
     * @throws  ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the write operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *          从给定的缓冲区开始，从给定的文件位置向该通道写入字节序列。
    这个方法的工作方式与write(ByteBuffer)方法相同，只是字节是从给定的文件位置开始写入的，而不是在通道的当前位置。此方法不修改该通道的位置。如果给定的位置大于文件的当前大小，那么文件将被扩展以适应新的字节;前一个文件末端和新写入的字节之间的任何字节的值都是不指定的。
     */
//    从给定的缓冲区开始，从给定的文件位置向该通道写入字节序列
    public abstract int write(ByteBuffer src, long position) throws IOException;


    // -- Memory-mapped buffers --

    /**
     * A typesafe enumeration for file-mapping modes.文件映射模式的类型枚举。
     *
     * @since 1.4
     *
     * @see java.nio.channels.FileChannel#map
     */
    public static class MapMode {

        /**
         * Mode for a read-only mapping.只读映射的模式。
         */
        public static final MapMode READ_ONLY
            = new MapMode("READ_ONLY");

        /**
         * Mode for a read/write mapping.读写映射的模式。
         */
        public static final MapMode READ_WRITE
            = new MapMode("READ_WRITE");

        /**
         * Mode for a private (copy-on-write) mapping.用于私有(写时复制)映射的模式。
         */
        public static final MapMode PRIVATE
            = new MapMode("PRIVATE");

        private final String name;

        private MapMode(String name) {
            this.name = name;
        }

        /**
         * Returns a string describing this file-mapping mode.
         *
         * @return  A descriptive string
         */
        public String toString() {
            return name;
        }

    }

    /**
     * Maps a region of this channel's file directly into memory.
     *
     * <p> A region of a file may be mapped into memory in one of three modes:
     * </p>
     *
     * <ul>
     *
     *   <li><p> <i>Read-only:</i> Any attempt to modify the resulting buffer
     *   will cause a {@link java.nio.ReadOnlyBufferException} to be thrown.
     *   ({@link MapMode#READ_ONLY MapMode.READ_ONLY}) </p></li>
     *
     *   <li><p> <i>Read/write:</i> Changes made to the resulting buffer will
     *   eventually be propagated to the file; they may or may not be made
     *   visible to other programs that have mapped the same file.  ({@link
     *   MapMode#READ_WRITE MapMode.READ_WRITE}) </p></li>
     *
     *   <li><p> <i>Private:</i> Changes made to the resulting buffer will not
     *   be propagated to the file and will not be visible to other programs
     *   that have mapped the same file; instead, they will cause private
     *   copies of the modified portions of the buffer to be created.  ({@link
     *   MapMode#PRIVATE MapMode.PRIVATE}) </p></li>
     *
     * </ul>
     *
     * <p> For a read-only mapping, this channel must have been opened for
     * reading; for a read/write or private mapping, this channel must have
     * been opened for both reading and writing.
     *
     * <p> The {@link MappedByteBuffer <i>mapped byte buffer</i>}
     * returned by this method will have a position of zero and a limit and
     * capacity of <tt>size</tt>; its mark will be undefined.  The buffer and
     * the mapping that it represents will remain valid until the buffer itself
     * is garbage-collected.
     *
     * <p> A mapping, once established, is not dependent upon the file channel
     * that was used to create it.  Closing the channel, in particular, has no
     * effect upon the validity of the mapping.
     *
     * <p> Many of the details of memory-mapped files are inherently dependent
     * upon the underlying operating system and are therefore unspecified.  The
     * behavior of this method when the requested region is not completely
     * contained within this channel's file is unspecified.  Whether changes
     * made to the content or size of the underlying file, by this program or
     * another, are propagated to the buffer is unspecified.  The rate at which
     * changes to the buffer are propagated to the file is unspecified.
     *
     * <p> For most operating systems, mapping a file into memory is more
     * expensive than reading or writing a few tens of kilobytes of data via
     * the usual {@link #read read} and {@link #write write} methods.  From the
     * standpoint of performance it is generally only worth mapping relatively
     * large files into memory.  </p>
     *
     * @param  mode
     *         One of the constants {@link MapMode#READ_ONLY READ_ONLY}, {@link
     *         MapMode#READ_WRITE READ_WRITE}, or {@link MapMode#PRIVATE
     *         PRIVATE} defined in the {@link MapMode} class, according to
     *         whether the file is to be mapped read-only, read/write, or
     *         privately (copy-on-write), respectively
     *
     * @param  position
     *         The position within the file at which the mapped region
     *         is to start; must be non-negative
     *
     * @param  size
     *         The size of the region to be mapped; must be non-negative and
     *         no greater than {@link java.lang.Integer#MAX_VALUE}
     *
     * @return  The mapped byte buffer
     *
     * @throws NonReadableChannelException
     *         If the <tt>mode</tt> is {@link MapMode#READ_ONLY READ_ONLY} but
     *         this channel was not opened for reading
     *
     * @throws NonWritableChannelException
     *         If the <tt>mode</tt> is {@link MapMode#READ_WRITE READ_WRITE} or
     *         {@link MapMode#PRIVATE PRIVATE} but this channel was not opened
     *         for both reading and writing
     *
     * @throws IllegalArgumentException
     *         If the preconditions on the parameters do not hold
     *
     * @throws IOException
     *         If some other I/O error occurs
     *
     * @see java.nio.channels.FileChannel.MapMode
     * @see java.nio.MappedByteBuffer
     * 将该通道文件的区域直接映射到内存中。
    文件的一个区域可以以以下三种方式之一映射到内存:
    只读:任何修改结果缓冲区的尝试都会导致java.nio。ReadOnlyBufferException抛出。(MapMode.READ_ONLY)
    读/写:对生成的缓冲区所做的更改最终将传播到文件中;它们可能对映射相同文件的其他程序可见，也可能不可见。(MapMode.READ_WRITE)
    Private:对结果缓冲区所做的更改将不会传播到文件，也不会对映射相同文件的其他程序可见;相反，它们将创建修改后的缓冲区的私有副本。(MapMode.PRIVATE)
    对于只读映射，必须打开该通道进行读取;对于读/写或私有映射，该通道必须同时为读/写打开。
    该方法返回的映射字节缓冲区的位置为0，并且具有大小的限制和容量;它的标记没有定义。缓冲区及其表示的映射将保持有效，直到缓冲区本身被垃圾收集。
    一个映射，一旦建立，就不依赖于用来创建它的文件通道。特别是关闭通道，对映射的有效性没有影响。
    内存映射文件的许多细节本质上依赖于底层操作系统，因此没有指定。当请求的区域未完全包含在该通道的文件中时，此方法的行为是未指定的。对底层文件的内容或大小所做的更改，是由这个程序或另一个程序传播到缓冲区还是未知的。对缓冲区的更改传播到文件的速率未指定。
    对于大多数操作系统，将文件映射到内存比通过通常的读写方法读取或写入几十千字节的数据要昂贵得多。从性能的角度来看，通常只需要将较大的文件映射到内存中即可。
     */
//    将该通道文件的区域直接映射到内存中，从性能的角度来看，通常只需要将较大的文件映射到内存中即可
    public abstract MappedByteBuffer map(MapMode mode,
                                         long position, long size)
        throws IOException;


    // -- Locks --

    /**
     * Acquires a lock on the given region of this channel's file.
     *
     * <p> An invocation of this method will block until the region can be
     * locked, this channel is closed, or the invoking thread is interrupted,
     * whichever comes first.
     *
     * <p> If this channel is closed by another thread during an invocation of
     * this method then an {@link AsynchronousCloseException} will be thrown.
     *
     * <p> If the invoking thread is interrupted while waiting to acquire the
     * lock then its interrupt status will be set and a {@link
     * FileLockInterruptionException} will be thrown.  If the invoker's
     * interrupt status is set when this method is invoked then that exception
     * will be thrown immediately; the thread's interrupt status will not be
     * changed.
     *
     * <p> The region specified by the <tt>position</tt> and <tt>size</tt>
     * parameters need not be contained within, or even overlap, the actual
     * underlying file.  Lock regions are fixed in size; if a locked region
     * initially contains the end of the file and the file grows beyond the
     * region then the new portion of the file will not be covered by the lock.
     * If a file is expected to grow in size and a lock on the entire file is
     * required then a region starting at zero, and no smaller than the
     * expected maximum size of the file, should be locked.  The zero-argument
     * {@link #lock()} method simply locks a region of size {@link
     * Long#MAX_VALUE}.
     *
     * <p> Some operating systems do not support shared locks, in which case a
     * request for a shared lock is automatically converted into a request for
     * an exclusive lock.  Whether the newly-acquired lock is shared or
     * exclusive may be tested by invoking the resulting lock object's {@link
     * FileLock#isShared() isShared} method.
     *
     * <p> File locks are held on behalf of the entire Java virtual machine.
     * They are not suitable for controlling access to a file by multiple
     * threads within the same virtual machine.  </p>
     *
     * @param  position
     *         The position at which the locked region is to start; must be
     *         non-negative
     *
     * @param  size
     *         The size of the locked region; must be non-negative, and the sum
     *         <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> must be non-negative
     *
     * @param  shared
     *         <tt>true</tt> to request a shared lock, in which case this
     *         channel must be open for reading (and possibly writing);
     *         <tt>false</tt> to request an exclusive lock, in which case this
     *         channel must be open for writing (and possibly reading)
     *
     * @return  A lock object representing the newly-acquired lock
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel while the invoking
     *          thread is blocked in this method
     *
     * @throws  FileLockInterruptionException
     *          If the invoking thread is interrupted while blocked in this
     *          method
     *
     * @throws  OverlappingFileLockException
     *          If a lock that overlaps the requested region is already held by
     *          this Java virtual machine, or if another thread is already
     *          blocked in this method and is attempting to lock an overlapping
     *          region
     *
     * @throws  NonReadableChannelException
     *          If <tt>shared</tt> is <tt>true</tt> this channel was not
     *          opened for reading
     *
     * @throws  NonWritableChannelException
     *          If <tt>shared</tt> is <tt>false</tt> but this channel was not
     *          opened for writing
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *
     * @see     #lock()
     * @see     #tryLock()
     * @see     #tryLock(long,long,boolean)
     * 获取该通道文件的给定区域的锁。
    该方法的调用将被阻塞，直到该区域可以被锁定、该通道被关闭或调用线程被中断，无论哪个是第一个。
    如果该通道在调用该方法期间被另一个线程关闭，那么将抛出一个AsynchronousCloseException。
    如果调用线程在等待获取锁时被中断，则将设置其中断状态，并抛出FileLockInterruptionException异常。如果调用方在调用该方法时设置了中断状态，那么该异常将立即抛出;线程的中断状态将不会更改。
    位置和大小参数指定的区域不需要包含在实际底层文件中，甚至不需要重叠。锁定区域的大小是固定的;如果一个被锁定的区域最初包含文件的末尾，并且文件在该区域之外增长，那么这个锁将不会覆盖文件的新部分。如果预期文件的大小会增加，并且需要对整个文件进行锁定，那么应该锁定一个从0开始的区域，且不小于文件的预期最大大小。zero参数lock()方法简单地锁定了一个大小为Long.MAX_VALUE的区域。
    有些操作系统不支持共享锁，在这种情况下，对共享锁的请求会自动转换为对独占锁的请求。无论新获得的锁是共享的还是专有的，都可以通过调用产生的锁对象的isShared方法来测试。
    文件锁代表整个Java虚拟机。它们不适用于控制同一虚拟机中的多个线程对文件的访问。
     */
    public abstract FileLock lock(long position, long size, boolean shared)
        throws IOException;

    /**
     * Acquires an exclusive lock on this channel's file.
     *
     * <p> An invocation of this method of the form <tt>fc.lock()</tt> behaves
     * in exactly the same way as the invocation
     *
     * <pre>
     *     fc.{@link #lock(long,long,boolean) lock}(0L, Long.MAX_VALUE, false) </pre>
     *
     * @return  A lock object representing the newly-acquired lock
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  AsynchronousCloseException
     *          If another thread closes this channel while the invoking
     *          thread is blocked in this method
     *
     * @throws  FileLockInterruptionException
     *          If the invoking thread is interrupted while blocked in this
     *          method
     *
     * @throws  OverlappingFileLockException
     *          If a lock that overlaps the requested region is already held by
     *          this Java virtual machine, or if another thread is already
     *          blocked in this method and is attempting to lock an overlapping
     *          region of the same file
     *
     * @throws  NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *
     * @see     #lock(long,long,boolean)
     * @see     #tryLock()
     * @see     #tryLock(long,long,boolean)
     * 获取该通道文件上的独占锁。
    窗体fc.lock()的此方法的调用与调用的行为完全相同
    fc。锁(0 l,长。MAX_VALUE假)
     */
    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }

    /**
     * Attempts to acquire a lock on the given region of this channel's file.
     *
     * <p> This method does not block.  An invocation always returns
     * immediately, either having acquired a lock on the requested region or
     * having failed to do so.  If it fails to acquire a lock because an
     * overlapping lock is held by another program then it returns
     * <tt>null</tt>.  If it fails to acquire a lock for any other reason then
     * an appropriate exception is thrown.
     *
     * <p> The region specified by the <tt>position</tt> and <tt>size</tt>
     * parameters need not be contained within, or even overlap, the actual
     * underlying file.  Lock regions are fixed in size; if a locked region
     * initially contains the end of the file and the file grows beyond the
     * region then the new portion of the file will not be covered by the lock.
     * If a file is expected to grow in size and a lock on the entire file is
     * required then a region starting at zero, and no smaller than the
     * expected maximum size of the file, should be locked.  The zero-argument
     * {@link #tryLock()} method simply locks a region of size {@link
     * Long#MAX_VALUE}.
     *
     * <p> Some operating systems do not support shared locks, in which case a
     * request for a shared lock is automatically converted into a request for
     * an exclusive lock.  Whether the newly-acquired lock is shared or
     * exclusive may be tested by invoking the resulting lock object's {@link
     * FileLock#isShared() isShared} method.
     *
     * <p> File locks are held on behalf of the entire Java virtual machine.
     * They are not suitable for controlling access to a file by multiple
     * threads within the same virtual machine.  </p>
     *
     * @param  position
     *         The position at which the locked region is to start; must be
     *         non-negative
     *
     * @param  size
     *         The size of the locked region; must be non-negative, and the sum
     *         <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> must be non-negative
     *
     * @param  shared
     *         <tt>true</tt> to request a shared lock,
     *         <tt>false</tt> to request an exclusive lock
     *
     * @return  A lock object representing the newly-acquired lock,
     *          or <tt>null</tt> if the lock could not be acquired
     *          because another program holds an overlapping lock
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  OverlappingFileLockException
     *          If a lock that overlaps the requested region is already held by
     *          this Java virtual machine, or if another thread is already
     *          blocked in this method and is attempting to lock an overlapping
     *          region of the same file
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *
     * @see     #lock()
     * @see     #lock(long,long,boolean)
     * @see     #tryLock()
     * 尝试获取该通道文件的给定区域的锁。
    此方法不阻塞。调用总是立即返回，要么获得请求区域的锁，要么失败。如果由于另一个程序持有重叠锁而未能获得锁，则返回null。如果由于其他原因未能获得锁，则抛出适当的异常。
    位置和大小参数指定的区域不需要包含在实际底层文件中，甚至不需要重叠。锁定区域的大小是固定的;如果一个被锁定的区域最初包含文件的末尾，并且文件在该区域之外增长，那么这个锁将不会覆盖文件的新部分。如果预期文件的大小会增加，并且需要对整个文件进行锁定，那么应该锁定一个从0开始的区域，且不小于文件的预期最大大小。零参数tryLock()方法只是锁定一个长. max_value大小的区域。
    有些操作系统不支持共享锁，在这种情况下，对共享锁的请求会自动转换为对独占锁的请求。无论新获得的锁是共享的还是专有的，都可以通过调用产生的锁对象的isShared方法来测试。
    文件锁代表整个Java虚拟机。它们不适用于控制同一虚拟机中的多个线程对文件的访问。
     */
    public abstract FileLock tryLock(long position, long size, boolean shared)
        throws IOException;

    /**
     * Attempts to acquire an exclusive lock on this channel's file.
     *
     * <p> An invocation of this method of the form <tt>fc.tryLock()</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     fc.{@link #tryLock(long,long,boolean) tryLock}(0L, Long.MAX_VALUE, false) </pre>
     *
     * @return  A lock object representing the newly-acquired lock,
     *          or <tt>null</tt> if the lock could not be acquired
     *          because another program holds an overlapping lock
     *
     * @throws  ClosedChannelException
     *          If this channel is closed
     *
     * @throws  OverlappingFileLockException
     *          If a lock that overlaps the requested region is already held by
     *          this Java virtual machine, or if another thread is already
     *          blocked in this method and is attempting to lock an overlapping
     *          region
     *
     * @throws  IOException
     *          If some other I/O error occurs
     *
     * @see     #lock()
     * @see     #lock(long,long,boolean)
     * @see     #tryLock(long,long,boolean)
     * 试图获取该通道文件上的独占锁。
    表单fc.tryLock()的此方法的调用与调用的行为完全相同
    fc。tryLock(0 l,长。MAX_VALUE假)
     */
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

}
