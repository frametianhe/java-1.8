/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.spi.FileSystemProvider;
import java.net.URI;

/**
 * This class consists exclusively of static methods that return a {@link Path}
 * by converting a path string or {@link URI}.这个类只包含通过转换路径字符串或URI返回路径的静态方法。
 *
 * @since 1.7
 */

public final class Paths {
    private Paths() { }

    /**
     * Converts a path string, or a sequence of strings that when joined form
     * a path string, to a {@code Path}. If {@code more} does not specify any
     * elements then the value of the {@code first} parameter is the path string
     * to convert. If {@code more} specifies one or more elements then each
     * non-empty string, including {@code first}, is considered to be a sequence
     * of name elements (see {@link Path}) and is joined to form a path string.
     * The details as to how the Strings are joined is provider specific but
     * typically they will be joined using the {@link FileSystem#getSeparator
     * name-separator} as the separator. For example, if the name separator is
     * "{@code /}" and {@code getPath("/foo","bar","gus")} is invoked, then the
     * path string {@code "/foo/bar/gus"} is converted to a {@code Path}.
     * A {@code Path} representing an empty path is returned if {@code first}
     * is the empty string and {@code more} does not contain any non-empty
     * strings.
     *
     * <p> The {@code Path} is obtained by invoking the {@link FileSystem#getPath
     * getPath} method of the {@link FileSystems#getDefault default} {@link
     * FileSystem}.
     *
     * <p> Note that while this method is very convenient, using it will imply
     * an assumed reference to the default {@code FileSystem} and limit the
     * utility of the calling code. Hence it should not be used in library code
     * intended for flexible reuse. A more flexible alternative is to use an
     * existing {@code Path} instance as an anchor, such as:
     * <pre>
     *     Path dir = ...
     *     Path path = dir.resolve("file");
     * </pre>
     *
     * @param   first
     *          the path string or initial part of the path string
     * @param   more
     *          additional strings to be joined to form the path string
     *
     * @return  the resulting {@code Path}
     *
     * @throws  InvalidPathException
     *          if the path string cannot be converted to a {@code Path}
     *
     * @see FileSystem#getPath
     * 将路径字符串或连接形成路径字符串的字符串序列转换为路径。如果more没有指定任何元素，那么第一个参数的值就是要转换的路径字符串。如果更多指定一个或多个元素，那么每个非空字符串(包括first)都被视为名称元素的序列(请参见路径)，并被连接起来形成一个路径字符串。关于如何连接字符串的细节是特定于提供者的，但是通常使用名称分隔符作为分隔符来连接它们。例如，如果名称分隔符是“/”和getPath(“/foo”、“bar”、“gus”)，那么路径字符串“/foo/bar/gus”将转换为路径。如果第一个是空字符串，而更多的不包含任何非空字符串，则返回表示空路径的路径。
    通过调用默认文件系统的getPath方法获得路径。
    注意，虽然这个方法非常方便，但是使用它将意味着对默认文件系统的假设引用，并限制调用代码的效用。因此，它不应该用于用于灵活重用的库代码中。更灵活的选择是使用现有的路径实例作为锚点，例如:
     */
    public static Path get(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }

    /**
     * Converts the given URI to a {@link Path} object.
     *
     * <p> This method iterates over the {@link FileSystemProvider#installedProviders()
     * installed} providers to locate the provider that is identified by the
     * URI {@link URI#getScheme scheme} of the given URI. URI schemes are
     * compared without regard to case. If the provider is found then its {@link
     * FileSystemProvider#getPath getPath} method is invoked to convert the
     * URI.
     *
     * <p> In the case of the default provider, identified by the URI scheme
     * "file", the given URI has a non-empty path component, and undefined query
     * and fragment components. Whether the authority component may be present
     * is platform specific. The returned {@code Path} is associated with the
     * {@link FileSystems#getDefault default} file system.
     *
     * <p> The default provider provides a similar <em>round-trip</em> guarantee
     * to the {@link java.io.File} class. For a given {@code Path} <i>p</i> it
     * is guaranteed that
     * <blockquote><tt>
     * Paths.get(</tt><i>p</i><tt>.{@link Path#toUri() toUri}()).equals(</tt>
     * <i>p</i><tt>.{@link Path#toAbsolutePath() toAbsolutePath}())</tt>
     * </blockquote>
     * so long as the original {@code Path}, the {@code URI}, and the new {@code
     * Path} are all created in (possibly different invocations of) the same
     * Java virtual machine. Whether other providers make any guarantees is
     * provider specific and therefore unspecified.
     *
     * @param   uri
     *          the URI to convert
     *
     * @return  the resulting {@code Path}
     *
     * @throws  IllegalArgumentException
     *          if preconditions on the {@code uri} parameter do not hold. The
     *          format of the URI is provider specific.
     * @throws  FileSystemNotFoundException
     *          The file system, identified by the URI, does not exist and
     *          cannot be created automatically, or the provider identified by
     *          the URI's scheme component is not installed
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission to access the file system
     *          将给定的URI转换为路径对象。
    此方法遍历已安装的提供程序，以定位由给定URI的URI方案标识的提供程序。URI方案不考虑具体情况进行比较。如果找到提供者，则调用其getPath方法来转换URI。
    在默认的提供者(由URI scheme“文件”标识)中，给定的URI有一个非空的路径组件，以及未定义的查询和片段组件。权威组件是否存在是平台特有的。返回的路径与默认文件系统相关联。
    默认提供程序为java.io提供了类似的往返保证。文件类。对于给定路径p，它保证
    Paths.get(p.toUri())。equals(p.toAbsolutePath())
    只要原始路径、URI和新路径都是在相同的Java虚拟机(可能不同的调用)中创建的。其他提供者是否提供任何保证是提供者特定的，因此没有说明。
     */
    public static Path get(URI uri) {
        String scheme =  uri.getScheme();
        if (scheme == null)
            throw new IllegalArgumentException("Missing scheme");

        // check for default provider to avoid loading of installed providers
        if (scheme.equalsIgnoreCase("file"))
            return FileSystems.getDefault().provider().getPath(uri);

        // try to find provider
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (provider.getScheme().equalsIgnoreCase(scheme)) {
                return provider.getPath(uri);
            }
        }

        throw new FileSystemNotFoundException("Provider \"" + scheme + "\" not installed");
    }
}
