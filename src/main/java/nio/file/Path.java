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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

/**
 * An object that may be used to locate a file in a file system. It will
 * typically represent a system dependent file path.
 *
 * <p> A {@code Path} represents a path that is hierarchical and composed of a
 * sequence of directory and file name elements separated by a special separator
 * or delimiter. A <em>root component</em>, that identifies a file system
 * hierarchy, may also be present. The name element that is <em>farthest</em>
 * from the root of the directory hierarchy is the name of a file or directory.
 * The other name elements are directory names. A {@code Path} can represent a
 * root, a root and a sequence of names, or simply one or more name elements.
 * A {@code Path} is considered to be an <i>empty path</i> if it consists
 * solely of one name element that is empty. Accessing a file using an
 * <i>empty path</i> is equivalent to accessing the default directory of the
 * file system. {@code Path} defines the {@link #getFileName() getFileName},
 * {@link #getParent getParent}, {@link #getRoot getRoot}, and {@link #subpath
 * subpath} methods to access the path components or a subsequence of its name
 * elements.
 *
 * <p> In addition to accessing the components of a path, a {@code Path} also
 * defines the {@link #resolve(Path) resolve} and {@link #resolveSibling(Path)
 * resolveSibling} methods to combine paths. The {@link #relativize relativize}
 * method that can be used to construct a relative path between two paths.
 * Paths can be {@link #compareTo compared}, and tested against each other using
 * the {@link #startsWith startsWith} and {@link #endsWith endsWith} methods.
 *
 * <p> This interface extends {@link Watchable} interface so that a directory
 * located by a path can be {@link #register registered} with a {@link
 * WatchService} and entries in the directory watched. </p>
 *
 * <p> <b>WARNING:</b> This interface is only intended to be implemented by
 * those developing custom file system implementations. Methods may be added to
 * this interface in future releases. </p>
 *
 * <h2>Accessing Files</h2>
 * <p> Paths may be used with the {@link Files} class to operate on files,
 * directories, and other types of files. For example, suppose we want a {@link
 * java.io.BufferedReader} to read text from a file "{@code access.log}". The
 * file is located in a directory "{@code logs}" relative to the current working
 * directory and is UTF-8 encoded.
 * <pre>
 *     Path path = FileSystems.getDefault().getPath("logs", "access.log");
 *     BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
 * </pre>
 *
 * <a name="interop"></a><h2>Interoperability</h2>
 * <p> Paths associated with the default {@link
 * java.nio.file.spi.FileSystemProvider provider} are generally interoperable
 * with the {@link java.io.File java.io.File} class. Paths created by other
 * providers are unlikely to be interoperable with the abstract path names
 * represented by {@code java.io.File}. The {@link java.io.File#toPath toPath}
 * method may be used to obtain a {@code Path} from the abstract path name
 * represented by a {@code java.io.File} object. The resulting {@code Path} can
 * be used to operate on the same file as the {@code java.io.File} object. In
 * addition, the {@link #toFile toFile} method is useful to construct a {@code
 * File} from the {@code String} representation of a {@code Path}.
 *
 * <h2>Concurrency</h2>
 * <p> Implementations of this interface are immutable and safe for use by
 * multiple concurrent threads.
 *
 * @since 1.7
 * @see Paths
 * 可以用来在文件系统中定位文件的对象。它通常表示与系统相关的文件路径。
路径表示层次结构的路径，由一系列目录和文件名元素组成，这些元素由特殊的分隔符或分隔符分隔。标识文件系统层次结构的根组件也可能存在。远离目录层次结构根的name元素是文件或目录的名称。其他名称元素是目录名。路径可以表示一个根、一个根和一个序列的名称，或者只是一个或多个名称元素。如果路径仅由一个空的name元素组成，则将其视为空路径。使用空路径访问文件等同于访问文件系统的默认目录。Path定义了getFileName、getParent、getRoot和子路径方法，以访问路径组件或其名称元素的子序列。
除了访问路径的组件之外，路径还定义了组合路径的解析和可分解方法。相对论方法，可用于在两条路径之间构造相对路径。可以使用startsWith和endsWith方法对路径进行比较和相互测试。
此接口扩展了可监视接口，以便可以向WatchService和监视目录中的条目注册路径所在的目录。
警告:此接口仅用于由开发自定义文件系统实现的人员实现。方法可以在以后的版本中添加到这个接口。

访问文件
路径可以与Files类一起使用，以对文件、目录和其他类型的文件进行操作。例如，假设我们需要一个java.io。BufferedReader从“access.log”文件中读取文本。该文件位于相对于当前工作目录的目录“logs”中，并且是UTF-8编码的。

互操作性
与默认提供程序关联的路径通常与java.io互操作。文件类。其他提供者创建的路径不太可能与java.io.File表示的抽象路径名互操作。toPath方法可用于从java.io表示的抽象路径名中获取路径。文件对象。生成的路径可以用于与java.io相同的文件操作。文件对象。此外，toFile方法对于从路径的字符串表示构造文件很有用。

并发性
这个接口的实现是不可变的，并且可以安全地用于多个并发线程。
 */

public interface Path
    extends Comparable<Path>, Iterable<Path>, Watchable
{
    /**
     * Returns the file system that created this object.返回创建此对象的文件系统。
     *
     * @return  the file system that created this object
     */
    FileSystem getFileSystem();

    /**
     * Tells whether or not this path is absolute.
     *
     * <p> An absolute path is complete in that it doesn't need to be combined
     * with other path information in order to locate a file.
     *
     * @return  {@code true} if, and only if, this path is absolute
     * 告诉该路径是否为绝对路径。
    绝对路径是完整的，因为它不需要与其他路径信息相结合来定位文件。
     */
    boolean isAbsolute();

    /**
     * Returns the root component of this path as a {@code Path} object,
     * or {@code null} if this path does not have a root component.
     *
     * @return  a path representing the root component of this path,
     *          or {@code null}
     *          将该路径的根组件作为路径对象返回，如果该路径没有根组件，则返回null。
     */
    Path getRoot();

    /**
     * Returns the name of the file or directory denoted by this path as a
     * {@code Path} object. The file name is the <em>farthest</em> element from
     * the root in the directory hierarchy.
     *
     * @return  a path representing the name of the file or directory, or
     *          {@code null} if this path has zero elements
     *          返回该路径作为路径对象表示的文件或目录的名称。文件名是目录层次结构中离根最远的元素。
     */
    Path getFileName();

    /**
     * Returns the <em>parent path</em>, or {@code null} if this path does not
     * have a parent.
     *
     * <p> The parent of this path object consists of this path's root
     * component, if any, and each element in the path except for the
     * <em>farthest</em> from the root in the directory hierarchy. This method
     * does not access the file system; the path or its parent may not exist.
     * Furthermore, this method does not eliminate special names such as "."
     * and ".." that may be used in some implementations. On UNIX for example,
     * the parent of "{@code /a/b/c}" is "{@code /a/b}", and the parent of
     * {@code "x/y/.}" is "{@code x/y}". This method may be used with the {@link
     * #normalize normalize} method, to eliminate redundant names, for cases where
     * <em>shell-like</em> navigation is required.
     *
     * <p> If this path has one or more elements, and no root component, then
     * this method is equivalent to evaluating the expression:
     * <blockquote><pre>
     * subpath(0,&nbsp;getNameCount()-1);
     * </pre></blockquote>
     *
     * @return  a path representing the path's parent
     * 返回父路径，如果该路径没有父路径，则返回null。
    该路径对象的父对象由该路径的根组件(如果有的话)和路径中的每个元素组成，除了目录层次结构中离根最远的元素。此方法不访问文件系统;路径或它的父路径可能不存在。此外，该方法不删除特殊名称，如“.”和“..”，这些名称可能用于某些实现。例如在UNIX上，“/a/b/c”的父节点是“/a/b”，“x/y/.”的父节点是“x/y”。这种方法可以与规范化方法一起使用，以消除冗余的名称，对于需要shell类导航的情况。
    如果该路径有一个或多个元素，且没有根元素，则该方法等价于计算表达式:
    子路径(0,getNameCount()1);
     */
    Path getParent();

    /**
     * Returns the number of name elements in the path.返回路径中名称元素的数量。
     *
     * @return  the number of elements in the path, or {@code 0} if this path
     *          only represents a root component
     */
    int getNameCount();

    /**
     * Returns a name element of this path as a {@code Path} object.
     *
     * <p> The {@code index} parameter is the index of the name element to return.
     * The element that is <em>closest</em> to the root in the directory hierarchy
     * has index {@code 0}. The element that is <em>farthest</em> from the root
     * has index {@link #getNameCount count}{@code -1}.
     *
     * @param   index
     *          the index of the element
     *
     * @return  the name element
     *
     * @throws  IllegalArgumentException
     *          if {@code index} is negative, {@code index} is greater than or
     *          equal to the number of elements, or this path has zero name
     *          elements
     *          返回此路径的名称元素作为路径对象。
    index参数是要返回的name元素的索引。最接近目录层次结构中的根的元素的索引为0。离根最远的元素有指数可数-1。
     */
    Path getName(int index);

    /**
     * Returns a relative {@code Path} that is a subsequence of the name
     * elements of this path.
     *
     * <p> The {@code beginIndex} and {@code endIndex} parameters specify the
     * subsequence of name elements. The name that is <em>closest</em> to the root
     * in the directory hierarchy has index {@code 0}. The name that is
     * <em>farthest</em> from the root has index {@link #getNameCount
     * count}{@code -1}. The returned {@code Path} object has the name elements
     * that begin at {@code beginIndex} and extend to the element at index {@code
     * endIndex-1}.
     *
     * @param   beginIndex
     *          the index of the first element, inclusive
     * @param   endIndex
     *          the index of the last element, exclusive
     *
     * @return  a new {@code Path} object that is a subsequence of the name
     *          elements in this {@code Path}
     *
     * @throws  IllegalArgumentException
     *          if {@code beginIndex} is negative, or greater than or equal to
     *          the number of elements. If {@code endIndex} is less than or
     *          equal to {@code beginIndex}, or larger than the number of elements.
     *          返回相对路径，该路径是该路径的名称元素的子序列。
    开始索引和结束索引参数指定名称元素的子序列。在目录层次结构中最接近根的名称有索引0。离根最远的名称有索引可数-1。返回的路径对象的名称元素从beginIndex开始，并扩展到index endIndex-1的元素。
     */
    Path subpath(int beginIndex, int endIndex);

    /**
     * Tests if this path starts with the given path.
     *
     * <p> This path <em>starts</em> with the given path if this path's root
     * component <em>starts</em> with the root component of the given path,
     * and this path starts with the same name elements as the given path.
     * If the given path has more name elements than this path then {@code false}
     * is returned.
     *
     * <p> Whether or not the root component of this path starts with the root
     * component of the given path is file system specific. If this path does
     * not have a root component and the given path has a root component then
     * this path does not start with the given path.
     *
     * <p> If the given path is associated with a different {@code FileSystem}
     * to this path then {@code false} is returned.
     *
     * @param   other
     *          the given path
     *
     * @return  {@code true} if this path starts with the given path; otherwise
     *          {@code false}
     *          测试此路径是否从给定路径开始。
    如果该路径的根组件从给定路径的根组件开始，则此路径以给定路径开始，而此路径以给定路径的相同名称元素开始。如果给定路径的名称元素多于此路径，则返回false。
    该路径的根组件是否从给定路径的根组件开始，它是特定于文件系统的。如果此路径没有根组件，且给定路径有根组件，则此路径不从给定路径开始。
    如果给定路径与此路径的不同文件系统相关联，则返回false。
     */
    boolean startsWith(Path other);

    /**
     * Tests if this path starts with a {@code Path}, constructed by converting
     * the given path string, in exactly the manner specified by the {@link
     * #startsWith(Path) startsWith(Path)} method. On UNIX for example, the path
     * "{@code foo/bar}" starts with "{@code foo}" and "{@code foo/bar}". It
     * does not start with "{@code f}" or "{@code fo}".
     *
     * @param   other
     *          the given path string
     *
     * @return  {@code true} if this path starts with the given path; otherwise
     *          {@code false}
     *
     * @throws  InvalidPathException
     *          If the path string cannot be converted to a Path.
     *          测试此路径是否从路径开始，路径是通过转换给定的路径字符串构造的，其方式与startsWith(path)方法指定的方式完全相同。例如，在UNIX上，路径“foo/bar”以“foo”和“foo/bar”开头。它不是以“f”或“fo”开头的。
     */
    boolean startsWith(String other);

    /**
     * Tests if this path ends with the given path.
     *
     * <p> If the given path has <em>N</em> elements, and no root component,
     * and this path has <em>N</em> or more elements, then this path ends with
     * the given path if the last <em>N</em> elements of each path, starting at
     * the element farthest from the root, are equal.
     *
     * <p> If the given path has a root component then this path ends with the
     * given path if the root component of this path <em>ends with</em> the root
     * component of the given path, and the corresponding elements of both paths
     * are equal. Whether or not the root component of this path ends with the
     * root component of the given path is file system specific. If this path
     * does not have a root component and the given path has a root component
     * then this path does not end with the given path.
     *
     * <p> If the given path is associated with a different {@code FileSystem}
     * to this path then {@code false} is returned.
     *
     * @param   other
     *          the given path
     *
     * @return  {@code true} if this path ends with the given path; otherwise
     *          {@code false}
     *
    测试此路径是否以给定路径结束。
    如果给定的路径有N个元素，没有根元素，而这条路径有N个或更多的元素，那么如果每条路径的最后N个元素(从离根最远的元素开始)相等，那么这条路径以给定的路径结束。
    如果给定路径有根组件，那么该路径以给定路径结束，如果该路径的根组件以给定路径的根组件结束，且这两条路径的相应元素相等。该路径的根组件是否以给定路径的根组件结束，这是特定于文件系统的。如果此路径没有根组件，且给定路径有根组件，则此路径不会以给定路径结束。
    如果给定路径与此路径的不同文件系统相关联，则返回false。
     */
    boolean endsWith(Path other);

    /**
     * Tests if this path ends with a {@code Path}, constructed by converting
     * the given path string, in exactly the manner specified by the {@link
     * #endsWith(Path) endsWith(Path)} method. On UNIX for example, the path
     * "{@code foo/bar}" ends with "{@code foo/bar}" and "{@code bar}". It does
     * not end with "{@code r}" or "{@code /bar}". Note that trailing separators
     * are not taken into account, and so invoking this method on the {@code
     * Path}"{@code foo/bar}" with the {@code String} "{@code bar/}" returns
     * {@code true}.
     *
     * @param   other
     *          the given path string
     *
     * @return  {@code true} if this path ends with the given path; otherwise
     *          {@code false}
     *
     * @throws  InvalidPathException
     *          If the path string cannot be converted to a Path.
     *          测试此路径是否以路径结束，路径是通过转换给定的路径字符串构造的，其方式与endsWith(path)方法指定的方式完全相同。例如，在UNIX上，路径“foo/bar”以“foo/bar”和“bar”结尾。它不会以“r”或“/bar”结尾。注意，不考虑跟踪分隔符，因此在路径“foo/bar”上调用这个方法，字符串“bar/”返回true。
     */
    boolean endsWith(String other);

    /**
     * Returns a path that is this path with redundant name elements eliminated.
     *
     * <p> The precise definition of this method is implementation dependent but
     * in general it derives from this path, a path that does not contain
     * <em>redundant</em> name elements. In many file systems, the "{@code .}"
     * and "{@code ..}" are special names used to indicate the current directory
     * and parent directory. In such file systems all occurrences of "{@code .}"
     * are considered redundant. If a "{@code ..}" is preceded by a
     * non-"{@code ..}" name then both names are considered redundant (the
     * process to identify such names is repeated until it is no longer
     * applicable).
     *
     * <p> This method does not access the file system; the path may not locate
     * a file that exists. Eliminating "{@code ..}" and a preceding name from a
     * path may result in the path that locates a different file than the original
     * path. This can arise when the preceding name is a symbolic link.
     *
     * @return  the resulting path or this path if it does not contain
     *          redundant name elements; an empty path is returned if this path
     *          does have a root component and all name elements are redundant
     *
     * @see #getParent
     * @see #toRealPath
     * 返回该路径的路径，其中删除了冗余名称元素。
    此方法的确切定义依赖于实现，但通常它派生自此路径，该路径不包含冗余名称元素。在许多文件系统中，"."和".."是用来表示当前目录和父目录的特殊名称。在这样的文件系统中，“.”的所有出现都被认为是多余的。如果“.. .”之前有一个非“.. .”名，那么这两个名称都被认为是多余的(识别这些名称的过程被重复，直到不再适用)。
    此方法不访问文件系统;路径可能无法定位存在的文件。从路径中删除“..”和前面的名称可能会导致找到与原始路径不同的文件的路径。当前面的名字是一个符号链接时，就会出现这种情况。
     */
    Path normalize();

    // -- resolution and relativization --

    /**
     * Resolve the given path against this path.
     *
     * <p> If the {@code other} parameter is an {@link #isAbsolute() absolute}
     * path then this method trivially returns {@code other}. If {@code other}
     * is an <i>empty path</i> then this method trivially returns this path.
     * Otherwise this method considers this path to be a directory and resolves
     * the given path against this path. In the simplest case, the given path
     * does not have a {@link #getRoot root} component, in which case this method
     * <em>joins</em> the given path to this path and returns a resulting path
     * that {@link #endsWith ends} with the given path. Where the given path has
     * a root component then resolution is highly implementation dependent and
     * therefore unspecified.
     *
     * @param   other
     *          the path to resolve against this path
     *
     * @return  the resulting path
     *
     * @see #relativize
     * 针对此路径解析给定的路径。
    如果另一个参数是绝对路径，则该方法将返回其他参数。如果other是一个空路径，那么该方法将返回该路径。否则，该方法将此路径视为一个目录，并在此路径上解析给定的路径。在最简单的情况下，给定路径没有根组件，在这种情况下，该方法将给定路径连接到该路径，并返回以给定路径结束的结果路径。当给定路径有根组件时，解析高度依赖于实现，因此未指定。
     */
    Path resolve(Path other);

    /**
     * Converts a given path string to a {@code Path} and resolves it against
     * this {@code Path} in exactly the manner specified by the {@link
     * #resolve(Path) resolve} method. For example, suppose that the name
     * separator is "{@code /}" and a path represents "{@code foo/bar}", then
     * invoking this method with the path string "{@code gus}" will result in
     * the {@code Path} "{@code foo/bar/gus}".
     *
     * @param   other
     *          the path string to resolve against this path
     *
     * @return  the resulting path
     *
     * @throws  InvalidPathException
     *          if the path string cannot be converted to a Path.
     *
     * @see FileSystem#getPath
     * 将给定的路径字符串转换为路径，并按照解析方法指定的方式对其进行解析。例如，假设名称分隔符是“/”，一个路径表示“foo/bar”，然后使用路径字符串“gus”调用这个方法，将导致路径“foo/bar/gus”。
     */
    Path resolve(String other);

    /**
     * Resolves the given path against this path's {@link #getParent parent}
     * path. This is useful where a file name needs to be <i>replaced</i> with
     * another file name. For example, suppose that the name separator is
     * "{@code /}" and a path represents "{@code dir1/dir2/foo}", then invoking
     * this method with the {@code Path} "{@code bar}" will result in the {@code
     * Path} "{@code dir1/dir2/bar}". If this path does not have a parent path,
     * or {@code other} is {@link #isAbsolute() absolute}, then this method
     * returns {@code other}. If {@code other} is an empty path then this method
     * returns this path's parent, or where this path doesn't have a parent, the
     * empty path.
     *
     * @param   other
     *          the path to resolve against this path's parent
     *
     * @return  the resulting path
     *
     * @see #resolve(Path)
     * 针对该路径的父路径解析给定路径。当需要用另一个文件名替换文件名时，这是非常有用的。例如，假设名称分隔符是“/”，而路径表示“dir1/dir2/foo”，那么使用路径“bar”调用此方法将导致路径“dir1/dir2/bar”。如果这个路径没有父路径，或者其他是绝对路径，那么这个方法将返回其他路径。如果另一个路径是空路径，那么该方法将返回该路径的父路径，或者该路径没有父路径的空路径。
     */
    Path resolveSibling(Path other);

    /**
     * Converts a given path string to a {@code Path} and resolves it against
     * this path's {@link #getParent parent} path in exactly the manner
     * specified by the {@link #resolveSibling(Path) resolveSibling} method.
     *
     * @param   other
     *          the path string to resolve against this path's parent
     *
     * @return  the resulting path
     *
     * @throws  InvalidPathException
     *          if the path string cannot be converted to a Path.
     *
     * @see FileSystem#getPath
     * 将给定的路径字符串转换为路径，并按照解析方法指定的方式根据路径的父路径解析它。
     */
    Path resolveSibling(String other);

    /**
     * Constructs a relative path between this path and a given path.
     *
     * <p> Relativization is the inverse of {@link #resolve(Path) resolution}.
     * This method attempts to construct a {@link #isAbsolute relative} path
     * that when {@link #resolve(Path) resolved} against this path, yields a
     * path that locates the same file as the given path. For example, on UNIX,
     * if this path is {@code "/a/b"} and the given path is {@code "/a/b/c/d"}
     * then the resulting relative path would be {@code "c/d"}. Where this
     * path and the given path do not have a {@link #getRoot root} component,
     * then a relative path can be constructed. A relative path cannot be
     * constructed if only one of the paths have a root component. Where both
     * paths have a root component then it is implementation dependent if a
     * relative path can be constructed. If this path and the given path are
     * {@link #equals equal} then an <i>empty path</i> is returned.
     *
     * <p> For any two {@link #normalize normalized} paths <i>p</i> and
     * <i>q</i>, where <i>q</i> does not have a root component,
     * <blockquote>
     *   <i>p</i><tt>.relativize(</tt><i>p</i><tt>.resolve(</tt><i>q</i><tt>)).equals(</tt><i>q</i><tt>)</tt>
     * </blockquote>
     *
     * <p> When symbolic links are supported, then whether the resulting path,
     * when resolved against this path, yields a path that can be used to locate
     * the {@link Files#isSameFile same} file as {@code other} is implementation
     * dependent. For example, if this path is  {@code "/a/b"} and the given
     * path is {@code "/a/x"} then the resulting relative path may be {@code
     * "../x"}. If {@code "b"} is a symbolic link then is implementation
     * dependent if {@code "a/b/../x"} would locate the same file as {@code "/a/x"}.
     *
     * @param   other
     *          the path to relativize against this path
     *
     * @return  the resulting relative path, or an empty path if both paths are
     *          equal
     *
     * @throws  IllegalArgumentException
     *          if {@code other} is not a {@code Path} that can be relativized
     *          against this path
     *          构造此路径与给定路径之间的相对路径。
    相对论是分辨率的倒数。此方法尝试构造一个相对路径，当针对此路径解析时，会产生一个路径，该路径与给定路径定位相同的文件。例如，在UNIX上，如果这个路径是“/a/b”，而给定的路径是“/a/b/c/d”，那么得到的相对路径将是“c/d”。如果此路径和给定路径没有根组件，则可以构造相对路径。如果只有一个路径有根组件，则不能构造相对路径。如果两个路径都有根组件，那么如果可以构造一个相对路径，那么它就是依赖于实现的。如果这条路径和给定的路径相等，则返回一个空路径。
    对于任意两个归一化的路径p和q，其中q没有根元素，
    p.relativize(p.resolve(q).equals(q)
    当支持符号链接时，当解决此路径时，所产生的路径是否会产生一个路径，该路径可以用来定位与其他的实现依赖的相同的文件。例如，如果这个路径是“/a/b”，而给定的路径是“/a/x”，那么得到的相对路径可能是“../x”。如果“b”是一个符号链接，那么实现依赖于“a/b/..”。/x"将定位与"/a/x"相同的文件。
     */
    Path relativize(Path other);

    /**
     * Returns a URI to represent this path.
     *
     * <p> This method constructs an absolute {@link URI} with a {@link
     * URI#getScheme() scheme} equal to the URI scheme that identifies the
     * provider. The exact form of the scheme specific part is highly provider
     * dependent.
     *
     * <p> In the case of the default provider, the URI is hierarchical with
     * a {@link URI#getPath() path} component that is absolute. The query and
     * fragment components are undefined. Whether the authority component is
     * defined or not is implementation dependent. There is no guarantee that
     * the {@code URI} may be used to construct a {@link java.io.File java.io.File}.
     * In particular, if this path represents a Universal Naming Convention (UNC)
     * path, then the UNC server name may be encoded in the authority component
     * of the resulting URI. In the case of the default provider, and the file
     * exists, and it can be determined that the file is a directory, then the
     * resulting {@code URI} will end with a slash.
     *
     * <p> The default provider provides a similar <em>round-trip</em> guarantee
     * to the {@link java.io.File} class. For a given {@code Path} <i>p</i> it
     * is guaranteed that
     * <blockquote><tt>
     * {@link Paths#get(URI) Paths.get}(</tt><i>p</i><tt>.toUri()).equals(</tt><i>p</i>
     * <tt>.{@link #toAbsolutePath() toAbsolutePath}())</tt>
     * </blockquote>
     * so long as the original {@code Path}, the {@code URI}, and the new {@code
     * Path} are all created in (possibly different invocations of) the same
     * Java virtual machine. Whether other providers make any guarantees is
     * provider specific and therefore unspecified.
     *
     * <p> When a file system is constructed to access the contents of a file
     * as a file system then it is highly implementation specific if the returned
     * URI represents the given path in the file system or it represents a
     * <em>compound</em> URI that encodes the URI of the enclosing file system.
     * A format for compound URIs is not defined in this release; such a scheme
     * may be added in a future release.
     *
     * @return  the URI representing this path
     *
     * @throws  java.io.IOError
     *          if an I/O error occurs obtaining the absolute path, or where a
     *          file system is constructed to access the contents of a file as
     *          a file system, and the URI of the enclosing file system cannot be
     *          obtained
     *
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager
     *          is installed, the {@link #toAbsolutePath toAbsolutePath} method
     *          throws a security exception.
     *          返回表示此路径的URI。
    该方法使用与标识提供者的URI方案相同的方案构造一个绝对URI。方案特定部分的确切形式高度依赖于提供者。
    对于默认提供程序，URI具有绝对的路径组件的层次结构。查询和片段组件没有定义。是否定义了authority组件取决于实现。不能保证URI可以用于构造java.io.File。特别是，如果该路径表示通用命名约定(UNC)路径，那么UNC服务器名可能被编码在结果URI的权限组件中。在默认提供程序和文件存在的情况下，可以确定该文件是一个目录，然后产生的URI将以斜杠结束。
    默认提供程序为File类提供类似的往返保证。对于给定路径p，它保证
    Paths.get(p.toUri())。=(p .toAbsolutePath())
    只要原始路径、URI和新路径都是在同一个Java虚拟机(可能是不同的调用)中创建的。其他提供者是否作出任何保证是特定于提供者的，因此是不确定的。
    当构建一个文件系统以作为文件系统访问文件的内容时，如果返回的URI表示文件系统中的给定路径，或者它表示对封装文件系统的URI进行编码的复合URI，那么它是高度实现特定的。这个版本没有定义复合uri的格式;这样的方案可以在以后的版本中添加。
     */
    URI toUri();

    /**
     * Returns a {@code Path} object representing the absolute path of this
     * path.
     *
     * <p> If this path is already {@link Path#isAbsolute absolute} then this
     * method simply returns this path. Otherwise, this method resolves the path
     * in an implementation dependent manner, typically by resolving the path
     * against a file system default directory. Depending on the implementation,
     * this method may throw an I/O error if the file system is not accessible.
     *
     * @return  a {@code Path} object representing the absolute path
     *
     * @throws  java.io.IOError
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager
     *          is installed, and this path is not absolute, then the security
     *          manager's {@link SecurityManager#checkPropertyAccess(String)
     *          checkPropertyAccess} method is invoked to check access to the
     *          system property {@code user.dir}
     *          返回表示此路径的绝对路径的路径对象。
    如果这个路径已经是绝对的，那么这个方法只返回这个路径。否则，该方法将以依赖于实现的方式解决路径，通常是通过解决针对文件系统默认目录的路径。根据实现，如果文件系统无法访问，此方法可能会抛出I/O错误。
     */
    Path toAbsolutePath();

    /**
     * Returns the <em>real</em> path of an existing file.
     *
     * <p> The precise definition of this method is implementation dependent but
     * in general it derives from this path, an {@link #isAbsolute absolute}
     * path that locates the {@link Files#isSameFile same} file as this path, but
     * with name elements that represent the actual name of the directories
     * and the file. For example, where filename comparisons on a file system
     * are case insensitive then the name elements represent the names in their
     * actual case. Additionally, the resulting path has redundant name
     * elements removed.
     *
     * <p> If this path is relative then its absolute path is first obtained,
     * as if by invoking the {@link #toAbsolutePath toAbsolutePath} method.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled. By default, symbolic links are resolved to their final
     * target. If the option {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} is
     * present then this method does not resolve symbolic links.
     *
     * Some implementations allow special names such as "{@code ..}" to refer to
     * the parent directory. When deriving the <em>real path</em>, and a
     * "{@code ..}" (or equivalent) is preceded by a non-"{@code ..}" name then
     * an implementation will typically cause both names to be removed. When
     * not resolving symbolic links and the preceding name is a symbolic link
     * then the names are only removed if it guaranteed that the resulting path
     * will locate the same file as this path.
     *
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  an absolute path represent the <em>real</em> path of the file
     *          located by this object
     *
     * @throws  IOException
     *          if the file does not exist or an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager
     *          is installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file, and where
     *          this path is not absolute, its {@link SecurityManager#checkPropertyAccess(String)
     *          checkPropertyAccess} method is invoked to check access to the
     *          system property {@code user.dir}
     *          返回现有文件的实际路径。
    此方法的确切定义依赖于实现，但通常它派生自此路径，这是一个位于与此路径相同的文件的绝对路径，但其名称元素表示目录和文件的实际名称。例如，如果文件系统上的文件名比较不区分大小写，那么name元素表示实际大小写中的名称。此外，结果路径中删除了冗余的名称元素。
    如果这个路径是相对的，那么它的绝对路径首先被获取，就像调用toAbsolutePath方法一样。
    选项数组可用于指示如何处理符号链接。默认情况下，符号链接解析为它们的最终目标。如果出现了选项NOFOLLOW_LINKS，那么该方法不会解析符号链接。有些实现允许“..”这样的特殊名称引用父目录。当得到真正的路径，和一个“。”(或等效的)前面有一个非“.. .”名，然后实现通常会导致删除两个名。当不解析符号链接和前面的名称是符号链接时，只有在保证结果路径将定位到与此路径相同的文件时，才删除这些名称。
     */
    Path toRealPath(LinkOption... options) throws IOException;

    /**
     * Returns a {@link File} object representing this path. Where this {@code
     * Path} is associated with the default provider, then this method is
     * equivalent to returning a {@code File} object constructed with the
     * {@code String} representation of this path.
     *
     * <p> If this path was created by invoking the {@code File} {@link
     * File#toPath toPath} method then there is no guarantee that the {@code
     * File} object returned by this method is {@link #equals equal} to the
     * original {@code File}.
     *
     * @return  a {@code File} object representing this path
     *
     * @throws  UnsupportedOperationException
     *          if this {@code Path} is not associated with the default provider
     *          返回表示此路径的文件对象。如果该路径与默认提供程序相关联，那么该方法等价于返回用该路径的字符串表示构造的文件对象。
    如果该路径是通过调用文件toPath方法创建的，那么不能保证该方法返回的文件对象与原始文件相同。
     */
    File toFile();

    // -- watchable --

    /**
     * Registers the file located by this path with a watch service.
     *
     * <p> In this release, this path locates a directory that exists. The
     * directory is registered with the watch service so that entries in the
     * directory can be watched. The {@code events} parameter is the events to
     * register and may contain the following events:
     * <ul>
     *   <li>{@link StandardWatchEventKinds#ENTRY_CREATE ENTRY_CREATE} -
     *       entry created or moved into the directory</li>
     *   <li>{@link StandardWatchEventKinds#ENTRY_DELETE ENTRY_DELETE} -
     *        entry deleted or moved out of the directory</li>
     *   <li>{@link StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} -
     *        entry in directory was modified</li>
     * </ul>
     *
     * <p> The {@link WatchEvent#context context} for these events is the
     * relative path between the directory located by this path, and the path
     * that locates the directory entry that is created, deleted, or modified.
     *
     * <p> The set of events may include additional implementation specific
     * event that are not defined by the enum {@link StandardWatchEventKinds}
     *
     * <p> The {@code modifiers} parameter specifies <em>modifiers</em> that
     * qualify how the directory is registered. This release does not define any
     * <em>standard</em> modifiers. It may contain implementation specific
     * modifiers.
     *
     * <p> Where a file is registered with a watch service by means of a symbolic
     * link then it is implementation specific if the watch continues to depend
     * on the existence of the symbolic link after it is registered.
     *
     * @param   watcher
     *          the watch service to which this object is to be registered
     * @param   events
     *          the events for which this object should be registered
     * @param   modifiers
     *          the modifiers, if any, that modify how the object is registered
     *
     * @return  a key representing the registration of this object with the
     *          given watch service
     *
     * @throws  UnsupportedOperationException
     *          if unsupported events or modifiers are specified
     * @throws  IllegalArgumentException
     *          if an invalid combination of events or modifiers is specified
     * @throws  ClosedWatchServiceException
     *          if the watch service is closed
     * @throws  NotDirectoryException
     *          if the file is registered to watch the entries in a directory
     *          and the file is not a directory  <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *          用表服务注册位于此路径的文件。
    在这个版本中，该路径定位一个存在的目录。该目录在监视服务中注册，以便可以监视目录中的条目。事件参数是要注册的事件，可以包含以下事件:
    ENTRY_CREATE——创建或移动到目录中的条目
    条目删除-删除或移出目录
    ENTRY_MODIFY——修改目录中的条目
    这些事件的上下文是该路径所在目录之间的相对路径，以及定位创建、删除或修改的目录条目的路径。
    事件集可以包含附加的实现特定的事件，这些事件不是由enum watcheventkind定义的
    修饰符参数指定修饰符，限定如何注册目录。此版本不定义任何标准修饰符。它可能包含实现特定的修饰符。
    如果一个文件通过符号链接在监视服务中注册，那么如果该监视在注册后继续依赖符号链接的存在，那么它就是实现特定的。
     */
    @Override
    WatchKey register(WatchService watcher,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
        throws IOException;

    /**
     * Registers the file located by this path with a watch service.
     *
     * <p> An invocation of this method behaves in exactly the same way as the
     * invocation
     * <pre>
     *     watchable.{@link #register(WatchService,WatchEvent.Kind[],WatchEvent.Modifier[]) register}(watcher, events, new WatchEvent.Modifier[0]);
     * </pre>
     *
     * <p> <b>Usage Example:</b>
     * Suppose we wish to register a directory for entry create, delete, and modify
     * events:
     * <pre>
     *     Path dir = ...
     *     WatchService watcher = ...
     *
     *     WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
     * </pre>
     * @param   watcher
     *          The watch service to which this object is to be registered
     * @param   events
     *          The events for which this object should be registered
     *
     * @return  A key representing the registration of this object with the
     *          given watch service
     *
     * @throws  UnsupportedOperationException
     *          If unsupported events are specified
     * @throws  IllegalArgumentException
     *          If an invalid combination of events is specified
     * @throws  ClosedWatchServiceException
     *          If the watch service is closed
     * @throws  NotDirectoryException
     *          If the file is registered to watch the entries in a directory
     *          and the file is not a directory  <i>(optional specific exception)</i>
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *          用表服务注册位于此路径的文件。
    此方法的调用行为与调用完全相同
     */
    @Override
    WatchKey register(WatchService watcher,
                      WatchEvent.Kind<?>... events)
        throws IOException;

    // -- Iterable --

    /**
     * Returns an iterator over the name elements of this path.
     *
     * <p> The first element returned by the iterator represents the name
     * element that is closest to the root in the directory hierarchy, the
     * second element is the next closest, and so on. The last element returned
     * is the name of the file or directory denoted by this path. The {@link
     * #getRoot root} component, if present, is not returned by the iterator.
     *
     * @return  an iterator over the name elements of this path.
     * 返回该路径名元素的迭代器。
    迭代器返回的第一个元素表示目录层次结构中最接近根的name元素，第二个元素是最近的，依此类推。返回的最后一个元素是该路径表示的文件或目录的名称。如果存在根组件，则不会由迭代器返回。
     */
    @Override
    Iterator<Path> iterator();

    // -- compareTo/equals/hashCode --

    /**
     * Compares two abstract paths lexicographically. The ordering defined by
     * this method is provider specific, and in the case of the default
     * provider, platform specific. This method does not access the file system
     * and neither file is required to exist.
     *
     * <p> This method may not be used to compare paths that are associated
     * with different file system providers.
     *
     * @param   other  the path compared to this path.
     *
     * @return  zero if the argument is {@link #equals equal} to this path, a
     *          value less than zero if this path is lexicographically less than
     *          the argument, or a value greater than zero if this path is
     *          lexicographically greater than the argument
     *
     * @throws  ClassCastException
     *          if the paths are associated with different providers
     *          在字典上比较两个抽象路径。此方法定义的排序是特定于提供者的，在默认提供者的情况下，是特定于平台的。此方法不访问文件系统，也不需要存在任何文件。
    此方法不能用于比较与不同文件系统提供程序关联的路径。
     */
    @Override
    int compareTo(Path other);

    /**
     * Tests this path for equality with the given object.
     *
     * <p> If the given object is not a Path, or is a Path associated with a
     * different {@code FileSystem}, then this method returns {@code false}.
     *
     * <p> Whether or not two path are equal depends on the file system
     * implementation. In some cases the paths are compared without regard
     * to case, and others are case sensitive. This method does not access the
     * file system and the file is not required to exist. Where required, the
     * {@link Files#isSameFile isSameFile} method may be used to check if two
     * paths locate the same file.
     *
     * <p> This method satisfies the general contract of the {@link
     * java.lang.Object#equals(Object) Object.equals} method. </p>
     *
     * @param   other
     *          the object to which this object is to be compared
     *
     * @return  {@code true} if, and only if, the given object is a {@code Path}
     *          that is identical to this {@code Path}
     *          测试此路径与给定对象的相等。
    如果给定的对象不是路径，或者是与不同文件系统相关联的路径，则此方法返回false。
    两个路径是否相等取决于文件系统实现。在某些情况下，对路径进行比较而不考虑大小写，而其他情况则是大小写敏感的。此方法不访问文件系统，也不需要文件存在。如果需要，可以使用isSameFile方法检查两个路径是否定位同一文件。
    该方法满足对象的一般契约。=方法。
     */
    boolean equals(Object other);

    /**
     * Computes a hash code for this path.
     *
     * <p> The hash code is based upon the components of the path, and
     * satisfies the general contract of the {@link Object#hashCode
     * Object.hashCode} method.
     *
     * @return  the hash-code value for this path
     * 计算此路径的哈希代码。
    哈希代码基于路径的组件，满足对象的一般约定。hashCode方法
     */
    int hashCode();

    /**
     * Returns the string representation of this path.
     *
     * <p> If this path was created by converting a path string using the
     * {@link FileSystem#getPath getPath} method then the path string returned
     * by this method may differ from the original String used to create the path.
     *
     * <p> The returned path string uses the default name {@link
     * FileSystem#getSeparator separator} to separate names in the path.
     *
     * @return  the string representation of this path
     * 返回此路径的字符串表示形式。
    如果通过使用getPath方法转换路径字符串来创建此路径，那么该方法返回的路径字符串可能与用于创建路径的原始字符串不同。
    返回的路径字符串使用默认的名称分隔符来分隔路径中的名称。
     */
    String toString();
}
