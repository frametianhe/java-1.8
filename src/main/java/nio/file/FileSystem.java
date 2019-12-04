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
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.io.Closeable;
import java.io.IOException;

/**
 * Provides an interface to a file system and is the factory for objects to
 * access files and other objects in the file system.
 *
 * <p> The default file system, obtained by invoking the {@link FileSystems#getDefault
 * FileSystems.getDefault} method, provides access to the file system that is
 * accessible to the Java virtual machine. The {@link FileSystems} class defines
 * methods to create file systems that provide access to other types of (custom)
 * file systems.
 *
 * <p> A file system is the factory for several types of objects:
 *
 * <ul>
 *   <li><p> The {@link #getPath getPath} method converts a system dependent
 *     <em>path string</em>, returning a {@link Path} object that may be used
 *     to locate and access a file. </p></li>
 *   <li><p> The {@link #getPathMatcher  getPathMatcher} method is used
 *     to create a {@link PathMatcher} that performs match operations on
 *     paths. </p></li>
 *   <li><p> The {@link #getFileStores getFileStores} method returns an iterator
 *     over the underlying {@link FileStore file-stores}. </p></li>
 *   <li><p> The {@link #getUserPrincipalLookupService getUserPrincipalLookupService}
 *     method returns the {@link UserPrincipalLookupService} to lookup users or
 *     groups by name. </p></li>
 *   <li><p> The {@link #newWatchService newWatchService} method creates a
 *     {@link WatchService} that may be used to watch objects for changes and
 *     events. </p></li>
 * </ul>
 *
 * <p> File systems vary greatly. In some cases the file system is a single
 * hierarchy of files with one top-level root directory. In other cases it may
 * have several distinct file hierarchies, each with its own top-level root
 * directory. The {@link #getRootDirectories getRootDirectories} method may be
 * used to iterate over the root directories in the file system. A file system
 * is typically composed of one or more underlying {@link FileStore file-stores}
 * that provide the storage for the files. Theses file stores can also vary in
 * the features they support, and the file attributes or <em>meta-data</em> that
 * they associate with files.
 *
 * <p> A file system is open upon creation and can be closed by invoking its
 * {@link #close() close} method. Once closed, any further attempt to access
 * objects in the file system cause {@link ClosedFileSystemException} to be
 * thrown. File systems created by the default {@link FileSystemProvider provider}
 * cannot be closed.
 *
 * <p> A {@code FileSystem} can provide read-only or read-write access to the
 * file system. Whether or not a file system provides read-only access is
 * established when the {@code FileSystem} is created and can be tested by invoking
 * its {@link #isReadOnly() isReadOnly} method. Attempts to write to file stores
 * by means of an object associated with a read-only file system throws {@link
 * ReadOnlyFileSystemException}.
 *
 * <p> File systems are safe for use by multiple concurrent threads. The {@link
 * #close close} method may be invoked at any time to close a file system but
 * whether a file system is <i>asynchronously closeable</i> is provider specific
 * and therefore unspecified. In other words, if a thread is accessing an
 * object in a file system, and another thread invokes the {@code close} method
 * then it may require to block until the first operation is complete. Closing
 * a file system causes all open channels, watch services, and other {@link
 * Closeable closeable} objects associated with the file system to be closed.
 *
 * @since 1.7
 * 为文件系统提供接口，是对象访问文件系统中的文件和其他对象的工厂。
通过调用文件系统获得的默认文件系统。getDefault方法，提供对Java虚拟机可访问的文件系统的访问。FileSystems类定义创建文件系统的方法，以提供对其他类型(自定义)文件系统的访问。
文件系统是若干类型对象的工厂:
getPath方法转换系统相关的路径字符串，返回一个路径对象，该对象可用于定位和访问文件。
getPathMatcher方法用于创建在路径上执行匹配操作的PathMatcher。
getFileStores方法返回底层文件存储上的迭代器。
getUserPrincipalLookupService方法返回UserPrincipalLookupService，以名称查找用户或组。
newWatchService方法创建一个WatchService，可用于监视对象的更改和事件。
文件系统有很大区别。在某些情况下，文件系统是一个具有顶级根目录的文件层次结构。在其他情况下，它可能有几个不同的文件层次结构，每个层次结构都有自己的顶级根目录。getrootdirectory方法可用于遍历文件系统中的根目录。文件系统通常由一个或多个提供文件存储的底层文件存储组成。这些文件存储还可以在它们支持的特性以及与文件相关联的文件属性或元数据方面有所不同。
文件系统在创建时是打开的，可以通过调用它的close方法来关闭。一旦关闭，任何进一步访问文件系统中的对象的尝试都会导致ClosedFileSystemException被抛出。无法关闭默认提供程序创建的文件系统。
文件系统可以为文件系统提供只读或读写访问。文件系统是否提供只读访问是在创建文件系统时确定的，并且可以通过调用其isReadOnly方法进行测试。试图通过与只读文件系统相关联的对象写入文件存储将引发ReadOnlyFileSystemException。
文件系统对于多个并发线程来说是安全的。可以随时调用close方法来关闭文件系统，但是文件系统是否异步关闭是特定于提供者的，因此未指定。换句话说，如果一个线程正在访问一个文件系统中的对象，另一个线程调用close方法，那么它可能需要阻塞直到第一个操作完成。关闭文件系统会导致关闭与文件系统相关的所有打开的通道、监视服务和其他关闭对象。
 */

public abstract class FileSystem
    implements Closeable
{
    /**
     * Initializes a new instance of this class.
     */
    protected FileSystem() {
    }

    /**
     * Returns the provider that created this file system.返回创建此文件系统的提供程序。
     *
     * @return  The provider that created this file system.
     */
    public abstract FileSystemProvider provider();

    /**
     * Closes this file system.
     *
     * <p> After a file system is closed then all subsequent access to the file
     * system, either by methods defined by this class or on objects associated
     * with this file system, throw {@link ClosedFileSystemException}. If the
     * file system is already closed then invoking this method has no effect.
     *
     * <p> Closing a file system will close all open {@link
     * java.nio.channels.Channel channels}, {@link DirectoryStream directory-streams},
     * {@link WatchService watch-service}, and other closeable objects associated
     * with this file system. The {@link FileSystems#getDefault default} file
     * system cannot be closed.
     *
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  UnsupportedOperationException
     *          Thrown in the case of the default file system
     *          关闭这个文件系统。
    在关闭文件系统之后，对文件系统的所有后续访问(通过这个类定义的方法或与此文件系统关联的对象)都会抛出ClosedFileSystemException。如果文件系统已经关闭，那么调用此方法没有任何效果。
    关闭文件系统将关闭与此文件系统相关的所有打开的通道、目录流、监视服务和其他关闭的对象。无法关闭默认文件系统。
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Tells whether or not this file system is open.
     *
     * <p> File systems created by the default provider are always open.
     *
     * @return  {@code true} if, and only if, this file system is open
     * 告诉此文件系统是否打开。
    默认提供程序创建的文件系统总是打开的。
     */
    public abstract boolean isOpen();

    /**
     * Tells whether or not this file system allows only read-only access to
     * its file stores.
     *
     * @return  {@code true} if, and only if, this file system provides
     *          read-only access
     *          告诉此文件系统是否只允许对其文件存储进行只读访问。
     */
    public abstract boolean isReadOnly();

    /**
     * Returns the name separator, represented as a string.
     *
     * <p> The name separator is used to separate names in a path string. An
     * implementation may support multiple name separators in which case this
     * method returns an implementation specific <em>default</em> name separator.
     * This separator is used when creating path strings by invoking the {@link
     * Path#toString() toString()} method.
     *
     * <p> In the case of the default provider, this method returns the same
     * separator as {@link java.io.File#separator}.
     *
     * @return  The name separator
     *
    返回名称分隔符，表示为字符串。
    名称分隔符用于在路径字符串中分隔名称。实现可以支持多个名称分隔符，在这种情况下，该方法返回特定于实现的默认名称分隔符。通过调用toString()方法创建路径字符串时使用此分隔符。
    对于默认提供程序，此方法返回与java.io.File.separator相同的分隔符。
     */
    public abstract String getSeparator();

    /**
     * Returns an object to iterate over the paths of the root directories.
     *
     * <p> A file system provides access to a file store that may be composed
     * of a number of distinct file hierarchies, each with its own top-level
     * root directory. Unless denied by the security manager, each element in
     * the returned iterator corresponds to the root directory of a distinct
     * file hierarchy. The order of the elements is not defined. The file
     * hierarchies may change during the lifetime of the Java virtual machine.
     * For example, in some implementations, the insertion of removable media
     * may result in the creation of a new file hierarchy with its own
     * top-level directory.
     *
     * <p> When a security manager is installed, it is invoked to check access
     * to the each root directory. If denied, the root directory is not returned
     * by the iterator. In the case of the default provider, the {@link
     * SecurityManager#checkRead(String)} method is invoked to check read access
     * to each root directory. It is system dependent if the permission checks
     * are done when the iterator is obtained or during iteration.
     *
     * @return  An object to iterate over the root directories
     * 返回要遍历根目录路径的对象。
    文件系统提供对文件存储库的访问，文件存储库可以由许多不同的文件层次结构组成，每个层次结构都有自己的顶级根目录。除非安全管理器拒绝，否则返回迭代器中的每个元素都对应于不同文件层次结构的根目录。元素的顺序没有定义。在Java虚拟机的生命周期中，文件层次结构可能会发生变化。例如，在某些实现中，插入可移动媒体可能导致创建具有自己顶级目录的新文件层次结构。
     */
    public abstract Iterable<Path> getRootDirectories();

    /**
     * Returns an object to iterate over the underlying file stores.
     *
     * <p> The elements of the returned iterator are the {@link
     * FileStore FileStores} for this file system. The order of the elements is
     * not defined and the file stores may change during the lifetime of the
     * Java virtual machine. When an I/O error occurs, perhaps because a file
     * store is not accessible, then it is not returned by the iterator.
     *
     * <p> In the case of the default provider, and a security manager is
     * installed, the security manager is invoked to check {@link
     * RuntimePermission}<tt>("getFileStoreAttributes")</tt>. If denied, then
     * no file stores are returned by the iterator. In addition, the security
     * manager's {@link SecurityManager#checkRead(String)} method is invoked to
     * check read access to the file store's <em>top-most</em> directory. If
     * denied, the file store is not returned by the iterator. It is system
     * dependent if the permission checks are done when the iterator is obtained
     * or during iteration.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to print the space usage for all file stores:
     * <pre>
     *     for (FileStore store: FileSystems.getDefault().getFileStores()) {
     *         long total = store.getTotalSpace() / 1024;
     *         long used = (store.getTotalSpace() - store.getUnallocatedSpace()) / 1024;
     *         long avail = store.getUsableSpace() / 1024;
     *         System.out.format("%-20s %12d %12d %12d%n", store, total, used, avail);
     *     }
     * </pre>
     *
     * @return  An object to iterate over the backing file stores
     *
    返回要在底层文件存储上迭代的对象。
    返回迭代器的元素是这个文件系统的文件存储。元素的顺序没有定义，文件存储在Java虚拟机的生命周期中可能会发生变化。当发生I/O错误时，可能因为无法访问文件存储，迭代器不会返回它。
    在默认提供程序的情况下，安装了安全管理器，调用安全管理器来检查RuntimePermission(“getFileStoreAttributes”)。如果拒绝，则迭代器不会返回任何文件存储。此外，还调用安全管理器的SecurityManager.checkRead(String)方法来检查对文件存储的最顶层目录的读访问。如果被拒绝，则迭代器不会返回文件存储库。如果在获取迭代器时或在迭代期间进行权限检查，则与系统相关。
     */
    public abstract Iterable<FileStore> getFileStores();

    /**
     * Returns the set of the {@link FileAttributeView#name names} of the file
     * attribute views supported by this {@code FileSystem}.
     *
     * <p> The {@link BasicFileAttributeView} is required to be supported and
     * therefore the set contains at least one element, "basic".
     *
     * <p> The {@link FileStore#supportsFileAttributeView(String)
     * supportsFileAttributeView(String)} method may be used to test if an
     * underlying {@link FileStore} supports the file attributes identified by a
     * file attribute view.
     *
     * @return  An unmodifiable set of the names of the supported file attribute
     *          views
     *          返回此文件系统支持的文件属性视图的名称集。
    需要支持BasicFileAttributeView，因此集合至少包含一个元素“basic”。
    supportsFileAttributeView(String)方法可用于测试底层文件存储是否支持由文件属性视图标识的文件属性。
     */
    public abstract Set<String> supportedFileAttributeViews();

    /**
     * Converts a path string, or a sequence of strings that when joined form
     * a path string, to a {@code Path}. If {@code more} does not specify any
     * elements then the value of the {@code first} parameter is the path string
     * to convert. If {@code more} specifies one or more elements then each
     * non-empty string, including {@code first}, is considered to be a sequence
     * of name elements (see {@link Path}) and is joined to form a path string.
     * The details as to how the Strings are joined is provider specific but
     * typically they will be joined using the {@link #getSeparator
     * name-separator} as the separator. For example, if the name separator is
     * "{@code /}" and {@code getPath("/foo","bar","gus")} is invoked, then the
     * path string {@code "/foo/bar/gus"} is converted to a {@code Path}.
     * A {@code Path} representing an empty path is returned if {@code first}
     * is the empty string and {@code more} does not contain any non-empty
     * strings.
     *
     * <p> The parsing and conversion to a path object is inherently
     * implementation dependent. In the simplest case, the path string is rejected,
     * and {@link InvalidPathException} thrown, if the path string contains
     * characters that cannot be converted to characters that are <em>legal</em>
     * to the file store. For example, on UNIX systems, the NUL (&#92;u0000)
     * character is not allowed to be present in a path. An implementation may
     * choose to reject path strings that contain names that are longer than those
     * allowed by any file store, and where an implementation supports a complex
     * path syntax, it may choose to reject path strings that are <em>badly
     * formed</em>.
     *
     * <p> In the case of the default provider, path strings are parsed based
     * on the definition of paths at the platform or virtual file system level.
     * For example, an operating system may not allow specific characters to be
     * present in a file name, but a specific underlying file store may impose
     * different or additional restrictions on the set of legal
     * characters.
     *
     * <p> This method throws {@link InvalidPathException} when the path string
     * cannot be converted to a path. Where possible, and where applicable,
     * the exception is created with an {@link InvalidPathException#getIndex
     * index} value indicating the first position in the {@code path} parameter
     * that caused the path string to be rejected.
     *
     * @param   first
     *          the path string or initial part of the path string
     * @param   more
     *          additional strings to be joined to form the path string
     *
     * @return  the resulting {@code Path}
     *
     * @throws  InvalidPathException
     *          If the path string cannot be converted
     *          将路径字符串或连接形成路径字符串的字符串序列转换为路径。如果more没有指定任何元素，那么第一个参数的值就是要转换的路径字符串。如果更多指定一个或多个元素，那么每个非空字符串(包括first)都被视为名称元素的序列(请参见路径)，并被连接起来形成一个路径字符串。关于如何连接字符串的细节是特定于提供者的，但是通常使用名称分隔符作为分隔符来连接它们。例如，如果名称分隔符是“/”和getPath(“/foo”、“bar”、“gus”)，那么路径字符串“/foo/bar/gus”将转换为路径。如果第一个是空字符串，而更多的不包含任何非空字符串，则返回表示空路径的路径。
    解析和转换到路径对象本身是依赖于实现的。在最简单的情况下，如果路径字符串包含不能转换为文件存储合法字符的字符，那么路径字符串将被拒绝，并将抛出InvalidPathException。例如，在UNIX系统中，不允许在路径中显示NUL (\u0000)字符。实现可以选择拒绝包含比任何文件存储所允许的更长的名称的路径字符串，如果实现支持复杂的路径语法，则可以选择拒绝格式糟糕的路径字符串。
    在缺省提供程序的情况下，基于平台或虚拟文件系统级别的路径定义解析路径字符串。例如，操作系统可能不允许在文件名中显示特定的字符，但是特定的底层文件存储可能对一组合法字符施加不同的或附加的限制。
    当路径字符串不能转换为路径时，此方法抛出InvalidPathException。在可能的情况下(在适用的情况下)创建异常时使用一个索引值，该索引值指示导致路径字符串被拒绝的路径参数中的第一个位置。
     */
    public abstract Path getPath(String first, String... more);

    /**
     * Returns a {@code PathMatcher} that performs match operations on the
     * {@code String} representation of {@link Path} objects by interpreting a
     * given pattern.
     *
     * The {@code syntaxAndPattern} parameter identifies the syntax and the
     * pattern and takes the form:
     * <blockquote><pre>
     * <i>syntax</i><b>:</b><i>pattern</i>
     * </pre></blockquote>
     * where {@code ':'} stands for itself.
     *
     * <p> A {@code FileSystem} implementation supports the "{@code glob}" and
     * "{@code regex}" syntaxes, and may support others. The value of the syntax
     * component is compared without regard to case.
     *
     * <p> When the syntax is "{@code glob}" then the {@code String}
     * representation of the path is matched using a limited pattern language
     * that resembles regular expressions but with a simpler syntax. For example:
     *
     * <blockquote>
     * <table border="0" summary="Pattern Language">
     * <tr>
     *   <td>{@code *.java}</td>
     *   <td>Matches a path that represents a file name ending in {@code .java}</td>
     * </tr>
     * <tr>
     *   <td>{@code *.*}</td>
     *   <td>Matches file names containing a dot</td>
     * </tr>
     * <tr>
     *   <td>{@code *.{java,class}}</td>
     *   <td>Matches file names ending with {@code .java} or {@code .class}</td>
     * </tr>
     * <tr>
     *   <td>{@code foo.?}</td>
     *   <td>Matches file names starting with {@code foo.} and a single
     *   character extension</td>
     * </tr>
     * <tr>
     *   <td><tt>&#47;home&#47;*&#47;*</tt>
     *   <td>Matches <tt>&#47;home&#47;gus&#47;data</tt> on UNIX platforms</td>
     * </tr>
     * <tr>
     *   <td><tt>&#47;home&#47;**</tt>
     *   <td>Matches <tt>&#47;home&#47;gus</tt> and
     *   <tt>&#47;home&#47;gus&#47;data</tt> on UNIX platforms</td>
     * </tr>
     * <tr>
     *   <td><tt>C:&#92;&#92;*</tt>
     *   <td>Matches <tt>C:&#92;foo</tt> and <tt>C:&#92;bar</tt> on the Windows
     *   platform (note that the backslash is escaped; as a string literal in the
     *   Java Language the pattern would be <tt>"C:&#92;&#92;&#92;&#92;*"</tt>) </td>
     * </tr>
     *
     * </table>
     * </blockquote>
     *
     * <p> The following rules are used to interpret glob patterns:
     *
     * <ul>
     *   <li><p> The {@code *} character matches zero or more {@link Character
     *   characters} of a {@link Path#getName(int) name} component without
     *   crossing directory boundaries. </p></li>
     *
     *   <li><p> The {@code **} characters matches zero or more {@link Character
     *   characters} crossing directory boundaries. </p></li>
     *
     *   <li><p> The {@code ?} character matches exactly one character of a
     *   name component.</p></li>
     *
     *   <li><p> The backslash character ({@code \}) is used to escape characters
     *   that would otherwise be interpreted as special characters. The expression
     *   {@code \\} matches a single backslash and "\{" matches a left brace
     *   for example.  </p></li>
     *
     *   <li><p> The {@code [ ]} characters are a <i>bracket expression</i> that
     *   match a single character of a name component out of a set of characters.
     *   For example, {@code [abc]} matches {@code "a"}, {@code "b"}, or {@code "c"}.
     *   The hyphen ({@code -}) may be used to specify a range so {@code [a-z]}
     *   specifies a range that matches from {@code "a"} to {@code "z"} (inclusive).
     *   These forms can be mixed so [abce-g] matches {@code "a"}, {@code "b"},
     *   {@code "c"}, {@code "e"}, {@code "f"} or {@code "g"}. If the character
     *   after the {@code [} is a {@code !} then it is used for negation so {@code
     *   [!a-c]} matches any character except {@code "a"}, {@code "b"}, or {@code
     *   "c"}.
     *   <p> Within a bracket expression the {@code *}, {@code ?} and {@code \}
     *   characters match themselves. The ({@code -}) character matches itself if
     *   it is the first character within the brackets, or the first character
     *   after the {@code !} if negating.</p></li>
     *
     *   <li><p> The {@code { }} characters are a group of subpatterns, where
     *   the group matches if any subpattern in the group matches. The {@code ","}
     *   character is used to separate the subpatterns. Groups cannot be nested.
     *   </p></li>
     *
     *   <li><p> Leading period<tt>&#47;</tt>dot characters in file name are
     *   treated as regular characters in match operations. For example,
     *   the {@code "*"} glob pattern matches file name {@code ".login"}.
     *   The {@link Files#isHidden} method may be used to test whether a file
     *   is considered hidden.
     *   </p></li>
     *
     *   <li><p> All other characters match themselves in an implementation
     *   dependent manner. This includes characters representing any {@link
     *   FileSystem#getSeparator name-separators}. </p></li>
     *
     *   <li><p> The matching of {@link Path#getRoot root} components is highly
     *   implementation-dependent and is not specified. </p></li>
     *
     * </ul>
     *
     * <p> When the syntax is "{@code regex}" then the pattern component is a
     * regular expression as defined by the {@link java.util.regex.Pattern}
     * class.
     *
     * <p>  For both the glob and regex syntaxes, the matching details, such as
     * whether the matching is case sensitive, are implementation-dependent
     * and therefore not specified.
     *
     * @param   syntaxAndPattern
     *          The syntax and pattern
     *
     * @return  A path matcher that may be used to match paths against the pattern
     *
     * @throws  IllegalArgumentException
     *          If the parameter does not take the form: {@code syntax:pattern}
     * @throws  java.util.regex.PatternSyntaxException
     *          If the pattern is invalid
     * @throws  UnsupportedOperationException
     *          If the pattern syntax is not known to the implementation
     *
     * @see Files#newDirectoryStream(Path,String)
     * 返回通过解释给定模式在路径对象的字符串表示上执行匹配操作的路径映射器。syntaxAndPattern参数标识语法和模式，并采用以下形式:
     * 文件系统实现支持“glob”和“regex”语法，并可能支持其他语法。不考虑大小写，对语法组件的值进行比较。
     * 下面的规则用于解释glob模式:
     *字符匹配一个名称组件的零个或多个字符，而不跨越目录边界。
     **字符匹配零或多个跨越目录边界的字符。
    的吗?字符恰好匹配名称组件的一个字符。
    反斜杠字符(\)用于转义字符，否则将被解释为特殊字符。表达式\(\)匹配一个反斜杠和“\{”匹配一个左括号。
    []字符是一种括号表达式，它与一组字符中一个名称组件的单个字符相匹配。例如，[abc]匹配“a”、“b”或“c”。连字符(-)可以用来指定一个范围，所以[a-z]指定从“a”到“z”(包括)的范围。这些形式可以混合，所以[abce-g]匹配“a”、“b”、“c”、“e”、“f”或“g”。如果字符后面的字符是a !那么它就被用来否定so [!匹配除“a”、“b”或“c”之外的任何字符。
    在括号内的表达式*，?和\字符匹配他们自己。如果它是方括号中的第一个字符，或者是后面的第一个字符，那么这个(-)字符将匹配它本身。如果否定。
    {}字符是一组子模式，如果组中的任何子模式匹配，则组匹配。“，”字符用于分离子模式。组织不能嵌套。
    在匹配操作中，文件名中的前导字符/点字符被视为常规字符。例如，“*”glob模式匹配文件名“.login”。这些文件。可以使用石登方法来测试文件是否被认为是隐藏的。
    所有其他字符都以依赖于实现的方式进行匹配。这包括表示任何名称分隔符的字符。
    根组件的匹配高度依赖于实现，没有指定。
    当语法是“regex”时，模式组件就是java.util.regex定义的正则表达式。模式类。
    对于glob和regex语法，匹配的细节，例如是否匹配是区分大小写的，都是依赖于实现的，因此没有指定。
     */
    public abstract PathMatcher getPathMatcher(String syntaxAndPattern);

    /**
     * Returns the {@code UserPrincipalLookupService} for this file system
     * <i>(optional operation)</i>. The resulting lookup service may be used to
     * lookup user or group names.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to make "joe" the owner of a file:
     * <pre>
     *     UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
     *     Files.setOwner(path, lookupService.lookupPrincipalByName("joe"));
     * </pre>
     *
     * @throws  UnsupportedOperationException
     *          If this {@code FileSystem} does not does have a lookup service
     *
     * @return  The {@code UserPrincipalLookupService} for this file system
     * 返回此文件系统的UserPrincipalLookupService(可选操作)。产生的查找服务可用于查找用户或组名。
     */
    public abstract UserPrincipalLookupService getUserPrincipalLookupService();

    /**
     * Constructs a new {@link WatchService} <i>(optional operation)</i>.
     *
     * <p> This method constructs a new watch service that may be used to watch
     * registered objects for changes and events.
     *
     * @return  a new watch service
     *
     * @throws  UnsupportedOperationException
     *          If this {@code FileSystem} does not support watching file system
     *          objects for changes and events. This exception is not thrown
     *          by {@code FileSystems} created by the default provider.
     * @throws  IOException
     *          If an I/O error occurs
     *          构造一个新的WatchService(可选操作)。
    该方法构造了一个新的表服务，可以用来监视已注册对象的更改和事件。
     */
    public abstract WatchService newWatchService() throws IOException;
}
