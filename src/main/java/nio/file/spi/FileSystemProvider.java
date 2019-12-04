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

package java.nio.file.spi;

import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.channels.*;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Service-provider class for file systems. The methods defined by the {@link
 * java.nio.file.Files} class will typically delegate to an instance of this
 * class.
 *
 * <p> A file system provider is a concrete implementation of this class that
 * implements the abstract methods defined by this class. A provider is
 * identified by a {@code URI} {@link #getScheme() scheme}. The default provider
 * is identified by the URI scheme "file". It creates the {@link FileSystem} that
 * provides access to the file systems accessible to the Java virtual machine.
 * The {@link FileSystems} class defines how file system providers are located
 * and loaded. The default provider is typically a system-default provider but
 * may be overridden if the system property {@code
 * java.nio.file.spi.DefaultFileSystemProvider} is set. In that case, the
 * provider has a one argument constructor whose formal parameter type is {@code
 * FileSystemProvider}. All other providers have a zero argument constructor
 * that initializes the provider.
 *
 * <p> A provider is a factory for one or more {@link FileSystem} instances. Each
 * file system is identified by a {@code URI} where the URI's scheme matches
 * the provider's {@link #getScheme scheme}. The default file system, for example,
 * is identified by the URI {@code "file:///"}. A memory-based file system,
 * for example, may be identified by a URI such as {@code "memory:///?name=logfs"}.
 * The {@link #newFileSystem newFileSystem} method may be used to create a file
 * system, and the {@link #getFileSystem getFileSystem} method may be used to
 * obtain a reference to an existing file system created by the provider. Where
 * a provider is the factory for a single file system then it is provider dependent
 * if the file system is created when the provider is initialized, or later when
 * the {@code newFileSystem} method is invoked. In the case of the default
 * provider, the {@code FileSystem} is created when the provider is initialized.
 *
 * <p> All of the methods in this class are safe for use by multiple concurrent
 * threads.
 *
 * @since 1.7
 * 文件系统的服务提供者类。由Files类定义的方法通常会委托给该类的一个实例。
文件系统提供程序是这个类的具体实现，它实现这个类定义的抽象方法。提供程序由URI方案标识。默认提供程序由URI方案“文件”标识。它创建文件系统，该文件系统提供对Java虚拟机可访问的文件系统的访问。文件系统类定义文件系统提供程序的定位和加载方式。默认提供程序通常是系统默认提供程序，但如果系统属性java.nio.file.spi，则可能会被覆盖。设置了DefaultFileSystemProvider，在这种情况下，提供者有一个参数构造函数，其形式参数类型为FileSystemProvider。所有其他提供程序都有一个zero参数构造函数来初始化提供程序。
提供者是一个或多个文件系统实例的工厂。每个文件系统由URI标识，其中URI的方案与提供者的方案相匹配。例如，默认文件系统由URI“file:///”标识。例如，基于内存的文件系统可以通过URI(如“memory:///?name=logfs”)进行标识。可以使用新文件系统方法创建文件系统，可以使用getFileSystem方法获取对提供者创建的现有文件系统的引用。如果提供者是单个文件系统的工厂，那么如果在初始化提供者时创建文件系统，或者稍后调用newFileSystem method时创建文件系统，那么它就是提供者依赖的。对于默认提供程序，在初始化提供程序时创建文件系统。
该类中的所有方法都可以安全地用于多个并发线程。
 */

public abstract class FileSystemProvider {
    // lock using when loading providers加载提供程序时使用的锁定
    private static final Object lock = new Object();

    // installed providers
    private static volatile List<FileSystemProvider> installedProviders;

    // used to avoid recursive loading of instaled providers用于避免对已安装的提供程序进行递归加载
    private static boolean loadingProviders  = false;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("fileSystemProvider"));
        return null;
    }
    private FileSystemProvider(Void ignore) { }

    /**
     * Initializes a new instance of this class.
     *
     * <p> During construction a provider may safely access files associated
     * with the default provider but care needs to be taken to avoid circular
     * loading of other installed providers. If circular loading of installed
     * providers is detected then an unspecified error is thrown.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("fileSystemProvider")</tt>
     *          初始化该类的一个新实例。
    在构建过程中，提供程序可以安全地访问与默认提供程序关联的文件，但是需要注意避免循环加载其他已安装的提供程序。如果检测到安装提供程序的循环加载，则抛出一个未指定的错误。
     */
    protected FileSystemProvider() {
        this(checkPermission());
    }

    // loads all installed providers
    private static List<FileSystemProvider> loadInstalledProviders() {
        List<FileSystemProvider> list = new ArrayList<FileSystemProvider>();

        ServiceLoader<FileSystemProvider> sl = ServiceLoader
            .load(FileSystemProvider.class, ClassLoader.getSystemClassLoader());

        // ServiceConfigurationError may be throw here
        for (FileSystemProvider provider: sl) {
            String scheme = provider.getScheme();

            // add to list if the provider is not "file" and isn't a duplicate
            if (!scheme.equalsIgnoreCase("file")) {
                boolean found = false;
                for (FileSystemProvider p: list) {
                    if (p.getScheme().equalsIgnoreCase(scheme)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.add(provider);
                }
            }
        }
        return list;
    }

    /**
     * Returns a list of the installed file system providers.
     *
     * <p> The first invocation of this method causes the default provider to be
     * initialized (if not already initialized) and loads any other installed
     * providers as described by the {@link FileSystems} class.
     *
     * @return  An unmodifiable list of the installed file system providers. The
     *          list contains at least one element, that is the default file
     *          system provider
     *
     * @throws  ServiceConfigurationError
     *          When an error occurs while loading a service provider
     *          返回已安装文件系统提供程序的列表。
    该方法的第一次调用将导致对默认提供程序进行初始化(如果还没有初始化)，并按照文件系统类的描述加载任何其他已安装的提供程序。
     */
    public static List<FileSystemProvider> installedProviders() {
        if (installedProviders == null) {
            // ensure default provider is initialized
            FileSystemProvider defaultProvider = FileSystems.getDefault().provider();

            synchronized (lock) {
                if (installedProviders == null) {
                    if (loadingProviders) {
                        throw new Error("Circular loading of installed providers detected");
                    }
                    loadingProviders = true;

                    List<FileSystemProvider> list = AccessController
                        .doPrivileged(new PrivilegedAction<List<FileSystemProvider>>() {
                            @Override
                            public List<FileSystemProvider> run() {
                                return loadInstalledProviders();
                        }});

                    // insert the default provider at the start of the list
                    list.add(0, defaultProvider);

                    installedProviders = Collections.unmodifiableList(list);
                }
            }
        }
        return installedProviders;
    }

    /**
     * Returns the URI scheme that identifies this provider.
     *
     * @return  The URI scheme
     */
    public abstract String getScheme();

    /**
     * Constructs a new {@code FileSystem} object identified by a URI. This
     * method is invoked by the {@link FileSystems#newFileSystem(URI,Map)}
     * method to open a new file system identified by a URI.
     *
     * <p> The {@code uri} parameter is an absolute, hierarchical URI, with a
     * scheme equal (without regard to case) to the scheme supported by this
     * provider. The exact form of the URI is highly provider dependent. The
     * {@code env} parameter is a map of provider specific properties to configure
     * the file system.
     *
     * <p> This method throws {@link FileSystemAlreadyExistsException} if the
     * file system already exists because it was previously created by an
     * invocation of this method. Once a file system is {@link
     * java.nio.file.FileSystem#close closed} it is provider-dependent if the
     * provider allows a new file system to be created with the same URI as a
     * file system it previously created.
     *
     * @param   uri
     *          URI reference
     * @param   env
     *          A map of provider specific properties to configure the file system;
     *          may be empty
     *
     * @return  A new file system
     *
     * @throws  IllegalArgumentException
     *          If the pre-conditions for the {@code uri} parameter aren't met,
     *          or the {@code env} parameter does not contain properties required
     *          by the provider, or a property value is invalid
     * @throws  IOException
     *          An I/O error occurs creating the file system
     * @throws  SecurityException
     *          If a security manager is installed and it denies an unspecified
     *          permission required by the file system provider implementation
     * @throws  FileSystemAlreadyExistsException
     *          If the file system has already been created
     *          构造一个由URI标识的新文件系统对象。文件系统调用此方法。用于打开由URI标识的新文件系统的新文件系统的方法。
    uri参数是一个绝对的、分层的uri，其方案与该提供者所支持的方案相同(不考虑案例)。URI的确切形式高度依赖于提供者。env参数是配置文件系统的提供者特定属性的映射。
    如果文件系统已经存在，则该方法抛出FileSystemAlreadyExistsException，因为它之前是由该方法的调用创建的。一旦一个文件系统被关闭，如果提供者允许用它先前创建的文件系统的相同的URI创建一个新的文件系统，那么它就是与提供者相关的。
     */
    public abstract FileSystem newFileSystem(URI uri, Map<String,?> env)
        throws IOException;

    /**
     * Returns an existing {@code FileSystem} created by this provider.
     *
     * <p> This method returns a reference to a {@code FileSystem} that was
     * created by invoking the {@link #newFileSystem(URI,Map) newFileSystem(URI,Map)}
     * method. File systems created the {@link #newFileSystem(Path,Map)
     * newFileSystem(Path,Map)} method are not returned by this method.
     * The file system is identified by its {@code URI}. Its exact form
     * is highly provider dependent. In the case of the default provider the URI's
     * path component is {@code "/"} and the authority, query and fragment components
     * are undefined (Undefined components are represented by {@code null}).
     *
     * <p> Once a file system created by this provider is {@link
     * java.nio.file.FileSystem#close closed} it is provider-dependent if this
     * method returns a reference to the closed file system or throws {@link
     * FileSystemNotFoundException}. If the provider allows a new file system to
     * be created with the same URI as a file system it previously created then
     * this method throws the exception if invoked after the file system is
     * closed (and before a new instance is created by the {@link #newFileSystem
     * newFileSystem} method).
     *
     * <p> If a security manager is installed then a provider implementation
     * may require to check a permission before returning a reference to an
     * existing file system. In the case of the {@link FileSystems#getDefault
     * default} file system, no permission check is required.
     *
     * @param   uri
     *          URI reference
     *
     * @return  The file system
     *
     * @throws  IllegalArgumentException
     *          If the pre-conditions for the {@code uri} parameter aren't met
     * @throws  FileSystemNotFoundException
     *          If the file system does not exist
     * @throws  SecurityException
     *          If a security manager is installed and it denies an unspecified
     *          permission.
     *          返回由此提供程序创建的现有文件系统。
    该方法返回一个引用文件系统，该文件系统是通过调用newFileSystem(URI,Map)方法创建的。创建新的文件系统(Path,Map)方法的文件系统不会被这个方法返回。文件系统由其URI标识。它的确切形式高度依赖于提供者。在默认提供程序的情况下，URI的路径组件是“/”，而权限、查询和片段组件是未定义的(未定义的组件由null表示)。
    一旦这个提供程序创建的文件系统被关闭，如果这个方法返回一个对关闭文件系统的引用，或者抛出FileSystemNotFoundException，那么它就与提供程序相关。如果提供程序允许创建一个新的文件系统，该文件系统与之前创建的文件系统具有相同的URI，那么如果在文件系统关闭之后(以及在新文件系统方法创建新实例之前)调用该方法，则会抛出异常。
    如果安装了安全管理器，那么提供者实现可能需要在返回对现有文件系统的引用之前检查权限。对于默认文件系统，不需要进行权限检查。
     */
    public abstract FileSystem getFileSystem(URI uri);

    /**
     * Return a {@code Path} object by converting the given {@link URI}. The
     * resulting {@code Path} is associated with a {@link FileSystem} that
     * already exists or is constructed automatically.
     *
     * <p> The exact form of the URI is file system provider dependent. In the
     * case of the default provider, the URI scheme is {@code "file"} and the
     * given URI has a non-empty path component, and undefined query, and
     * fragment components. The resulting {@code Path} is associated with the
     * default {@link FileSystems#getDefault default} {@code FileSystem}.
     *
     * <p> If a security manager is installed then a provider implementation
     * may require to check a permission. In the case of the {@link
     * FileSystems#getDefault default} file system, no permission check is
     * required.通过转换给定的URI返回一个Path对象。结果路径与已经存在或自动构造的文件系统相关联。
     URI的确切形式依赖于文件系统提供程序。在默认提供程序的情况下，URI模式是“file”，给定的URI有一个非空路径组件、未定义的查询和片段组件。结果路径与默认的默认文件系统相关联。
     如果安装了安全管理器，则提供程序实现可能需要检查权限。对于默认文件系统，不需要进行权限检查。
     *
     * @param   uri
     *          The URI to convert
     *
     * @return  The resulting {@code Path}
     *
     * @throws  IllegalArgumentException
     *          If the URI scheme does not identify this provider or other
     *          preconditions on the uri parameter do not hold
     * @throws  FileSystemNotFoundException
     *          The file system, identified by the URI, does not exist and
     *          cannot be created automatically
     * @throws  SecurityException
     *          If a security manager is installed and it denies an unspecified
     *          permission.
     */
    public abstract Path getPath(URI uri);

    /**
     * Constructs a new {@code FileSystem} to access the contents of a file as a
     * file system.
     *
     * <p> This method is intended for specialized providers of pseudo file
     * systems where the contents of one or more files is treated as a file
     * system. The {@code env} parameter is a map of provider specific properties
     * to configure the file system.
     *
     * <p> If this provider does not support the creation of such file systems
     * or if the provider does not recognize the file type of the given file then
     * it throws {@code UnsupportedOperationException}. The default implementation
     * of this method throws {@code UnsupportedOperationException}.
     *
     * @param   path
     *          The path to the file
     * @param   env
     *          A map of provider specific properties to configure the file system;
     *          may be empty
     *
     * @return  A new file system
     *
     * @throws  UnsupportedOperationException
     *          If this provider does not support access to the contents as a
     *          file system or it does not recognize the file type of the
     *          given file
     * @throws  IllegalArgumentException
     *          If the {@code env} parameter does not contain properties required
     *          by the provider, or a property value is invalid
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is installed and it denies an unspecified
     *          permission.
     *          构造一个新的文件系统来作为文件系统访问文件的内容。
    此方法适用于伪文件系统的专门提供者，其中一个或多个文件的内容被视为文件系统。env参数是配置文件系统的提供者特定属性的映射。
    如果该提供者不支持创建此类文件系统，或者提供者不识别给定文件的文件类型，则会抛出UnsupportedOperationException。该方法的默认实现抛出UnsupportedOperationException。
     */
    public FileSystem newFileSystem(Path path, Map<String,?> env)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Opens a file, returning an input stream to read from the file. This
     * method works in exactly the manner specified by the {@link
     * Files#newInputStream} method.
     *
     * <p> The default implementation of this method opens a channel to the file
     * as if by invoking the {@link #newByteChannel} method and constructs a
     * stream that reads bytes from the channel. This method should be overridden
     * where appropriate.
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
     *          打开一个文件，返回要从文件中读取的输入流。这个方法按照文件指定的方式工作。newInputStream方法。
    该方法的默认实现打开了一个文件通道，就像调用newByteChannel方法并构造了一条从通道读取字节的流。应该在适当的地方重写此方法。
     */
    public InputStream newInputStream(Path path, OpenOption... options)
        throws IOException
    {
        if (options.length > 0) {
            for (OpenOption opt: options) {
                // All OpenOption values except for APPEND and WRITE are allowed
                if (opt == StandardOpenOption.APPEND ||
                    opt == StandardOpenOption.WRITE)
                    throw new UnsupportedOperationException("'" + opt + "' not allowed");
            }
        }
        return Channels.newInputStream(Files.newByteChannel(path, options));
    }

    /**
     * Opens or creates a file, returning an output stream that may be used to
     * write bytes to the file. This method works in exactly the manner
     * specified by the {@link Files#newOutputStream} method.
     *
     * <p> The default implementation of this method opens a channel to the file
     * as if by invoking the {@link #newByteChannel} method and constructs a
     * stream that writes bytes to the channel. This method should be overridden
     * where appropriate.
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
     *          打开或创建一个文件，返回可用于将字节写入文件的输出流。这个方法按照文件指定的方式工作。newOutputStream方法。
    该方法的默认实现打开文件的通道，就像调用newByteChannel方法一样，并构造将字节写入通道的流。应该在适当的地方重写此方法。
     */
    public OutputStream newOutputStream(Path path, OpenOption... options)
        throws IOException
    {
        int len = options.length;
        Set<OpenOption> opts = new HashSet<OpenOption>(len + 3);
        if (len == 0) {
            opts.add(StandardOpenOption.CREATE);
            opts.add(StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            for (OpenOption opt: options) {
                if (opt == StandardOpenOption.READ)
                    throw new IllegalArgumentException("READ not allowed");
                opts.add(opt);
            }
        }
        opts.add(StandardOpenOption.WRITE);
        return Channels.newOutputStream(newByteChannel(path, opts));
    }

    /**
     * Opens or creates a file for reading and/or writing, returning a file
     * channel to access the file. This method works in exactly the manner
     * specified by the {@link FileChannel#open(Path,Set,FileAttribute[])
     * FileChannel.open} method. A provider that does not support all the
     * features required to construct a file channel throws {@code
     * UnsupportedOperationException}. The default provider is required to
     * support the creation of file channels. When not overridden, the default
     * implementation throws {@code UnsupportedOperationException}.
     *
     * @param   path
     *          the path of the file to open or create
     * @param   options
     *          options specifying how the file is opened
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  a new file channel
     *
     * @throws  IllegalArgumentException
     *          If the set contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          If this provider that does not support creating file channels,
     *          or an unsupported open option or file attribute is specified
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default file system, the {@link
     *          SecurityManager#checkRead(String)} method is invoked to check
     *          read access if the file is opened for reading. The {@link
     *          SecurityManager#checkWrite(String)} method is invoked to check
     *          write access if the file is opened for writing
     *          打开或创建一个用于读写的文件，返回一个文件通道以访问该文件。此方法的工作方式与FileChannel指定的完全相同。开放的方法。不支持构建文件通道所需的所有特性的提供者抛出UnsupportedOperationException。需要默认提供程序来支持创建文件通道。当不重写时，默认实现抛出UnsupportedOperationException。
     */
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Opens or creates a file for reading and/or writing, returning an
     * asynchronous file channel to access the file. This method works in
     * exactly the manner specified by the {@link
     * AsynchronousFileChannel#open(Path,Set,ExecutorService,FileAttribute[])
     * AsynchronousFileChannel.open} method.
     * A provider that does not support all the features required to construct
     * an asynchronous file channel throws {@code UnsupportedOperationException}.
     * The default provider is required to support the creation of asynchronous
     * file channels. When not overridden, the default implementation of this
     * method throws {@code UnsupportedOperationException}.
     *
     * @param   path
     *          the path of the file to open or create
     * @param   options
     *          options specifying how the file is opened
     * @param   executor
     *          the thread pool or {@code null} to associate the channel with
     *          the default thread pool
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the file
     *
     * @return  a new asynchronous file channel
     *
     * @throws  IllegalArgumentException
     *          If the set contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          If this provider that does not support creating asynchronous file
     *          channels, or an unsupported open option or file attribute is
     *          specified
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default file system, the {@link
     *          SecurityManager#checkRead(String)} method is invoked to check
     *          read access if the file is opened for reading. The {@link
     *          SecurityManager#checkWrite(String)} method is invoked to check
     *          write access if the file is opened for writing
     *          打开或创建一个用于读写的文件，返回一个异步文件通道以访问该文件。此方法的工作方式与AsynchronousFileChannel指定的方式完全相同。开放的方法。不支持构建异步文件通道所需的所有特性的提供者抛出UnsupportedOperationException。需要默认提供程序来支持创建异步文件通道。当不重写时，该方法的默认实现抛出UnsupportedOperationException。
     */
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path,
                                                              Set<? extends OpenOption> options,
                                                              ExecutorService executor,
                                                              FileAttribute<?>... attrs)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Opens or creates a file, returning a seekable byte channel to access the
     * file. This method works in exactly the manner specified by the {@link
     * Files#newByteChannel(Path,Set,FileAttribute[])} method.
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
     *          打开或创建一个文件，返回一个可查找的字节通道以访问该文件。这个方法按照文件指定的方式工作。newByteChannel(路径集,FileAttribute[])方法。
     */
    public abstract SeekableByteChannel newByteChannel(Path path,
        Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;

    /**
     * Opens a directory, returning a {@code DirectoryStream} to iterate over
     * the entries in the directory. This method works in exactly the manner
     * specified by the {@link
     * Files#newDirectoryStream(java.nio.file.Path, java.nio.file.DirectoryStream.Filter)}
     * method.
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
     *          打开一个目录，返回一个DirectoryStream来遍历目录中的条目。这个方法按照文件指定的方式工作。newDirectoryStream(路径,DirectoryStream.Filter)方法。
     */
    public abstract DirectoryStream<Path> newDirectoryStream(Path dir,
         DirectoryStream.Filter<? super Path> filter) throws IOException;

    /**
     * Creates a new directory. This method works in exactly the manner
     * specified by the {@link Files#createDirectory} method.
     *
     * @param   dir
     *          the directory to create
     * @param   attrs
     *          an optional list of file attributes to set atomically when
     *          creating the directory
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
     *          创建一个新的目录。这个方法按照文件指定的方式工作。createDirectory方法。
     */
    public abstract void createDirectory(Path dir, FileAttribute<?>... attrs)
        throws IOException;

    /**
     * Creates a symbolic link to a target. This method works in exactly the
     * manner specified by the {@link Files#createSymbolicLink} method.
     *
     * <p> The default implementation of this method throws {@code
     * UnsupportedOperationException}.
     *
     * @param   link
     *          the path of the symbolic link to create
     * @param   target
     *          the target of the symbolic link
     * @param   attrs
     *          the array of attributes to set atomically when creating the
     *          symbolic link
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
     *          创建到目标的符号链接。这个方法按照文件指定的方式工作。createSymbolicLink方法。
    该方法的默认实现抛出UnsupportedOperationException。
     */
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new link (directory entry) for an existing file. This method
     * works in exactly the manner specified by the {@link Files#createLink}
     * method.
     *
     * <p> The default implementation of this method throws {@code
     * UnsupportedOperationException}.
     *
     * @param   link
     *          the link (directory entry) to create
     * @param   existing
     *          a path to an existing file
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
     *          method denies write access to either the  link or the
     *          existing file.
     *          为现有文件创建一个新的链接(目录项)。这个方法按照文件指定的方式工作。创建方法。
    该方法的默认实现抛出UnsupportedOperationException。
     */
    public void createLink(Path link, Path existing) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes a file. This method works in exactly the  manner specified by the
     * {@link Files#delete} method.
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
     *          删除一个文件。该方法的工作方式完全符合fils .delete方法的指定方式。
     */
    public abstract void delete(Path path) throws IOException;

    /**
     * Deletes a file if it exists. This method works in exactly the manner
     * specified by the {@link Files#deleteIfExists} method.
     *
     * <p> The default implementation of this method simply invokes {@link
     * #delete} ignoring the {@code NoSuchFileException} when the file does not
     * exist. It may be overridden where appropriate.
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
     *          is invoked to check delete access to the file
     *          如果文件存在，则删除它。该方法的工作方式与Files.deleteIfExists方法指定的完全一样。
    该方法的默认实现只是在文件不存在时调用delete，而忽略NoSuchFileException。它可能在适当的时候被覆盖。
     */
    public boolean deleteIfExists(Path path) throws IOException {
        try {
            delete(path);
            return true;
        } catch (NoSuchFileException ignore) {
            return false;
        }
    }

    /**
     * Reads the target of a symbolic link. This method works in exactly the
     * manner specified by the {@link Files#readSymbolicLink} method.
     *
     * <p> The default implementation of this method throws {@code
     * UnsupportedOperationException}.
     *
     * @param   link
     *          the path to the symbolic link
     *
     * @return  The target of the symbolic link
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
     *          读取符号链接的目标。这个方法按照文件指定的方式工作。readSymbolicLink方法。
    该方法的默认实现抛出UnsupportedOperationException。
     */
    public Path readSymbolicLink(Path link) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Copy a file to a target file. This method works in exactly the manner
     * specified by the {@link Files#copy(Path,Path,CopyOption[])} method
     * except that both the source and target paths must be associated with
     * this provider.
     *
     * @param   source
     *          the path to the file to copy
     * @param   target
     *          the path to the target file
     * @param   options
     *          options specifying how the copy should be done
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
     *          将文件复制到目标文件。这个方法按照文件指定的方式工作。复制(路径、路径、CopyOption[])方法，除了源路径和目标路径必须与此提供程序关联。
     */
    public abstract void copy(Path source, Path target, CopyOption... options)
        throws IOException;

    /**
     * Move or rename a file to a target file. This method works in exactly the
     * manner specified by the {@link Files#move} method except that both the
     * source and target paths must be associated with this provider.
     *
     * @param   source
     *          the path to the file to move
     * @param   target
     *          the path to the target file
     * @param   options
     *          options specifying how the move should be done
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
     *          将文件移动或重命名为目标文件。这个方法按照文件指定的方式工作。移动方法，除非源路径和目标路径都必须与此提供程序关联。
     */
    public abstract void move(Path source, Path target, CopyOption... options)
        throws IOException;

    /**
     * Tests if two paths locate the same file. This method works in exactly the
     * manner specified by the {@link Files#isSameFile} method.
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
     *          测试两个路径是否定位同一文件。这个方法按照文件指定的方式工作。isSameFile方法。
     */
    public abstract boolean isSameFile(Path path, Path path2)
        throws IOException;

    /**
     * Tells whether or not a file is considered <em>hidden</em>. This method
     * works in exactly the manner specified by the {@link Files#isHidden}
     * method.
     *
     * <p> This method is invoked by the {@link Files#isHidden isHidden} method.
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
     *          说明文件是否被认为是隐藏的。这个方法按照文件指定的方式工作。isHidden方法。
    这种方法是由石登方法调用的。
     */
    public abstract boolean isHidden(Path path) throws IOException;

    /**
     * Returns the {@link FileStore} representing the file store where a file
     * is located. This method works in exactly the manner specified by the
     * {@link Files#getFileStore} method.
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
     *          返回表示文件所在文件存储位置的文件存储。这个方法按照文件指定的方式工作。getFileStore方法。
     */
    public abstract FileStore getFileStore(Path path) throws IOException;

    /**
     * Checks the existence, and optionally the accessibility, of a file.
     *
     * <p> This method may be used by the {@link Files#isReadable isReadable},
     * {@link Files#isWritable isWritable} and {@link Files#isExecutable
     * isExecutable} methods to check the accessibility of a file.
     *
     * <p> This method checks the existence of a file and that this Java virtual
     * machine has appropriate privileges that would allow it access the file
     * according to all of access modes specified in the {@code modes} parameter
     * as follows:
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>Value</th> <th>Description</th> </tr>
     * <tr>
     *   <td> {@link AccessMode#READ READ} </td>
     *   <td> Checks that the file exists and that the Java virtual machine has
     *     permission to read the file. </td>
     * </tr>
     * <tr>
     *   <td> {@link AccessMode#WRITE WRITE} </td>
     *   <td> Checks that the file exists and that the Java virtual machine has
     *     permission to write to the file, </td>
     * </tr>
     * <tr>
     *   <td> {@link AccessMode#EXECUTE EXECUTE} </td>
     *   <td> Checks that the file exists and that the Java virtual machine has
     *     permission to {@link Runtime#exec execute} the file. The semantics
     *     may differ when checking access to a directory. For example, on UNIX
     *     systems, checking for {@code EXECUTE} access checks that the Java
     *     virtual machine has permission to search the directory in order to
     *     access file or subdirectories. </td>
     * </tr>
     * </table>
     *
     * <p> If the {@code modes} parameter is of length zero, then the existence
     * of the file is checked.
     *
     * <p> This method follows symbolic links if the file referenced by this
     * object is a symbolic link. Depending on the implementation, this method
     * may require to read file permissions, access control lists, or other
     * file attributes in order to check the effective access to the file. To
     * determine the effective access to a file may require access to several
     * attributes and so in some implementations this method may not be atomic
     * with respect to other file system operations.
     *
     * @param   path
     *          the path to the file to check
     * @param   modes
     *          The access modes to check; may have zero elements
     *
     * @throws  UnsupportedOperationException
     *          an implementation is required to support checking for
     *          {@code READ}, {@code WRITE}, and {@code EXECUTE} access. This
     *          exception is specified to allow for the {@code Access} enum to
     *          be extended in future releases.
     * @throws  NoSuchFileException
     *          if a file does not exist <i>(optional specific exception)</i>
     * @throws  AccessDeniedException
     *          the requested access would be denied or the access cannot be
     *          determined because the Java virtual machine has insufficient
     *          privileges or other reasons. <i>(optional specific exception)</i>
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          is invoked when checking read access to the file or only the
     *          existence of the file, the {@link SecurityManager#checkWrite(String)
     *          checkWrite} is invoked when checking write access to the file,
     *          and {@link SecurityManager#checkExec(String) checkExec} is invoked
     *          when checking execute access.
     *          检查文件的存在性和可访问性。
    这个方法可以被isread、isWritable和isExecutable方法用于检查文件的可访问性。
    该方法检查文件是否存在，该Java虚拟机具有适当的权限，允许它根据模式参数中指定的所有访问模式访问文件，如下所示:
    价值
    描述
    读
    检查文件是否存在，以及Java虚拟机是否具有读取文件的权限。
    写
    检查文件是否存在，以及Java虚拟机是否允许写入文件，
    执行
    检查文件是否存在，Java虚拟机是否具有执行文件的权限。在检查对目录的访问时，语义可能有所不同。例如，在UNIX系统上，检查执行访问检查Java虚拟机是否具有搜索目录以访问文件或子目录的权限。
    如果模式参数的长度为0，则检查文件是否存在。
    如果该对象引用的文件是符号链接，则此方法遵循符号链接。根据实现的不同，此方法可能需要读取文件权限、访问控制列表或其他文件属性，以检查对文件的有效访问。要确定对文件的有效访问可能需要访问几个属性，因此在某些实现中，此方法可能与其他文件系统操作不具有原子性。
     */
    public abstract void checkAccess(Path path, AccessMode... modes)
        throws IOException;

    /**
     * Returns a file attribute view of a given type. This method works in
     * exactly the manner specified by the {@link Files#getFileAttributeView}
     * method.
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
     *          返回给定类型的文件属性视图。这个方法按照文件指定的方式工作。getFileAttributeView方法。
     */
    public abstract <V extends FileAttributeView> V
        getFileAttributeView(Path path, Class<V> type, LinkOption... options);

    /**
     * Reads a file's attributes as a bulk operation. This method works in
     * exactly the manner specified by the {@link
     * Files#readAttributes(Path,Class,LinkOption[])} method.
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
     *          method is invoked to check read access to the file
     *          读取文件的属性作为批量操作。这个方法按照文件指定的方式工作。readAttributes(道路、类LinkOption[])方法。
     */
    public abstract <A extends BasicFileAttributes> A
        readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException;

    /**
     * Reads a set of file attributes as a bulk operation. This method works in
     * exactly the manner specified by the {@link
     * Files#readAttributes(Path,String,LinkOption[])} method.
     *
     * @param   path
     *          the path to the file
     * @param   attributes
     *          the attributes to read
     * @param   options
     *          options indicating how symbolic links are handled
     *
     * @return  a map of the attributes returned; may be empty. The map's keys
     *          are the attribute names, its values are the attribute values
     *
     * @throws  UnsupportedOperationException
     *          if the attribute view is not available
     * @throws  IllegalArgumentException
     *          if no attributes are specified or an unrecognized attributes is
     *          specified
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkRead(String) checkRead}
     *          method denies read access to the file. If this method is invoked
     *          to read security sensitive attributes then the security manager
     *          may be invoke to check for additional permissions.
     *          读取一组文件属性作为批量操作。这个方法按照文件指定的方式工作。readAttributes(路径字符串,LinkOption[])方法。
     */
    public abstract Map<String,Object> readAttributes(Path path, String attributes,
                                                      LinkOption... options)
        throws IOException;

    /**
     * Sets the value of a file attribute. This method works in exactly the
     * manner specified by the {@link Files#setAttribute} method.
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
     * @throws  UnsupportedOperationException
     *          if the attribute view is not available
     * @throws  IllegalArgumentException
     *          if the attribute name is not specified, or is not recognized, or
     *          the attribute value is of the correct type but has an
     *          inappropriate value
     * @throws  ClassCastException
     *          If the attribute value is not of the expected type or is a
     *          collection containing elements that are not of the expected
     *          type
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file. If this method is invoked
     *          to set security sensitive attributes then the security manager
     *          may be invoked to check for additional permissions.
     *          设置文件属性的值。这个方法按照文件指定的方式工作。setAttribute方法。
     */
    public abstract void setAttribute(Path path, String attribute,
                                      Object value, LinkOption... options)
        throws IOException;
}
