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

import java.nio.file.spi.FileSystemProvider;
import java.net.URI;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.lang.reflect.Constructor;

/**
 * Factory methods for file systems. This class defines the {@link #getDefault
 * getDefault} method to get the default file system and factory methods to
 * construct other types of file systems.
 *
 * <p> The first invocation of any of the methods defined by this class causes
 * the default {@link FileSystemProvider provider} to be loaded. The default
 * provider, identified by the URI scheme "file", creates the {@link FileSystem}
 * that provides access to the file systems accessible to the Java virtual
 * machine. If the process of loading or initializing the default provider fails
 * then an unspecified error is thrown.
 *
 * <p> The first invocation of the {@link FileSystemProvider#installedProviders
 * installedProviders} method, by way of invoking any of the {@code
 * newFileSystem} methods defined by this class, locates and loads all
 * installed file system providers. Installed providers are loaded using the
 * service-provider loading facility defined by the {@link ServiceLoader} class.
 * Installed providers are loaded using the system class loader. If the
 * system class loader cannot be found then the extension class loader is used;
 * if there is no extension class loader then the bootstrap class loader is used.
 * Providers are typically installed by placing them in a JAR file on the
 * application class path or in the extension directory, the JAR file contains a
 * provider-configuration file named {@code java.nio.file.spi.FileSystemProvider}
 * in the resource directory {@code META-INF/services}, and the file lists one or
 * more fully-qualified names of concrete subclass of {@link FileSystemProvider}
 * that have a zero argument constructor.
 * The ordering that installed providers are located is implementation specific.
 * If a provider is instantiated and its {@link FileSystemProvider#getScheme()
 * getScheme} returns the same URI scheme of a provider that was previously
 * instantiated then the most recently instantiated duplicate is discarded. URI
 * schemes are compared without regard to case. During construction a provider
 * may safely access files associated with the default provider but care needs
 * to be taken to avoid circular loading of other installed providers. If
 * circular loading of installed providers is detected then an unspecified error
 * is thrown.
 *
 * <p> This class also defines factory methods that allow a {@link ClassLoader}
 * to be specified when locating a provider. As with installed providers, the
 * provider classes are identified by placing the provider configuration file
 * in the resource directory {@code META-INF/services}.
 *
 * <p> If a thread initiates the loading of the installed file system providers
 * and another thread invokes a method that also attempts to load the providers
 * then the method will block until the loading completes.
 *
 * @since 1.7
 * 文件系统的工厂方法。这个类定义getDefault方法，以获得默认的文件系统和工厂方法来构造其他类型的文件系统。
这个类定义的任何方法的第一次调用都会导致加载默认的提供者。由URI方案“file”标识的默认提供程序创建文件系统，该文件系统提供对Java虚拟机可访问的文件系统的访问。如果加载或初始化默认提供程序的过程失败，则抛出一个未指定的错误。
通过调用该类定义的任何新文件系统方法，首次调用installedprovider方法，定位并加载所有已安装的文件系统提供程序。使用ServiceLoader类定义的服务提供者加载工具加载已安装的提供者。使用系统类加载器加载已安装的提供程序。如果找不到系统类装入器，则使用扩展类装入器;如果没有扩展类装入器，则使用引导类装入器。提供程序通常是通过将它们放在应用程序类路径的JAR文件中或扩展目录中安装的，JAR文件包含一个名为java.nio.file.spi的提供程序配置文件。在资源目录META-INF/services中，文件列出一个或多个具有零参数构造函数的文件系统提供程序的具体子类的完全限定名。安装提供程序所在的顺序是特定于实现的。如果一个提供者被实例化，并且它的getScheme返回先前实例化的提供者的相同的URI方案，那么最近实例化的副本将被丢弃。在不考虑情况的情况下，对URI方案进行比较。在构建过程中，提供程序可以安全地访问与默认提供程序关联的文件，但是需要注意避免循环加载其他已安装的提供程序。如果检测到安装提供程序的循环加载，则抛出一个未指定的错误。
这个类还定义了工厂方法，允许在定位提供者时指定类加载器。与已安装的提供者一样，提供者类通过将提供者配置文件放在资源目录META-INF/services中来标识。
如果一个线程启动已安装文件系统提供程序的加载，而另一个线程调用同样试图加载提供程序的方法，那么该方法将被阻塞，直到加载完成。
 */

public final class FileSystems {
    private FileSystems() {
    }

    // lazy initialization of default file system
    private static class DefaultFileSystemHolder {
        static final FileSystem defaultFileSystem = defaultFileSystem();

        // returns default file system
        private static FileSystem defaultFileSystem() {
            // load default provider
            FileSystemProvider provider = AccessController
                .doPrivileged(new PrivilegedAction<FileSystemProvider>() {
                    public FileSystemProvider run() {
                        return getDefaultProvider();
                    }
                });

            // return file system
            return provider.getFileSystem(URI.create("file:///"));
        }

        // returns default provider
        private static FileSystemProvider getDefaultProvider() {
            FileSystemProvider provider = sun.nio.fs.DefaultFileSystemProvider.create();

            // if the property java.nio.file.spi.DefaultFileSystemProvider is
            // set then its value is the name of the default provider (or a list)
            String propValue = System
                .getProperty("java.nio.file.spi.DefaultFileSystemProvider");
            if (propValue != null) {
                for (String cn: propValue.split(",")) {
                    try {
                        Class<?> c = Class
                            .forName(cn, true, ClassLoader.getSystemClassLoader());
                        Constructor<?> ctor = c
                            .getDeclaredConstructor(FileSystemProvider.class);
                        provider = (FileSystemProvider)ctor.newInstance(provider);

                        // must be "file"
                        if (!provider.getScheme().equals("file"))
                            throw new Error("Default provider must use scheme 'file'");

                    } catch (Exception x) {
                        throw new Error(x);
                    }
                }
            }
            return provider;
        }
    }

    /**
     * Returns the default {@code FileSystem}. The default file system creates
     * objects that provide access to the file systems accessible to the Java
     * virtual machine. The <em>working directory</em> of the file system is
     * the current user directory, named by the system property {@code user.dir}.
     * This allows for interoperability with the {@link java.io.File java.io.File}
     * class.
     *
     * <p> The first invocation of any of the methods defined by this class
     * locates the default {@link FileSystemProvider provider} object. Where the
     * system property {@code java.nio.file.spi.DefaultFileSystemProvider} is
     * not defined then the default provider is a system-default provider that
     * is invoked to create the default file system.
     *
     * <p> If the system property {@code java.nio.file.spi.DefaultFileSystemProvider}
     * is defined then it is taken to be a list of one or more fully-qualified
     * names of concrete provider classes identified by the URI scheme
     * {@code "file"}. Where the property is a list of more than one name then
     * the names are separated by a comma. Each class is loaded, using the system
     * class loader, and instantiated by invoking a one argument constructor
     * whose formal parameter type is {@code FileSystemProvider}. The providers
     * are loaded and instantiated in the order they are listed in the property.
     * If this process fails or a provider's scheme is not equal to {@code "file"}
     * then an unspecified error is thrown. URI schemes are normally compared
     * without regard to case but for the default provider, the scheme is
     * required to be {@code "file"}. The first provider class is instantiated
     * by invoking it with a reference to the system-default provider.
     * The second provider class is instantiated by invoking it with a reference
     * to the first provider instance. The third provider class is instantiated
     * by invoking it with a reference to the second instance, and so on. The
     * last provider to be instantiated becomes the default provider; its {@code
     * getFileSystem} method is invoked with the URI {@code "file:///"} to
     * get a reference to the default file system.
     *
     * <p> Subsequent invocations of this method return the file system that was
     * returned by the first invocation.
     *
     * @return  the default file system
     * 返回默认的文件系统。默认的文件系统创建对象，这些对象提供对Java虚拟机访问的文件系统的访问。文件系统的工作目录是当前用户目录，由系统属性user.dir命名。这允许与java.io进行互操作性。文件类。
    这个类定义的任何方法的第一次调用都会定位默认的provider对象。其中系统属性为java.nio.file.spi。没有定义DefaultFileSystemProvider，那么默认提供程序是一个系统默认提供程序，它被调用来创建默认文件系统。
    如果系统属性是java.nio.file.spi。定义了DefaultFileSystemProvider，然后将其作为URI方案“文件”标识的具体提供者类的一个或多个完全限定名称的列表。其中，属性是多个名称的列表，然后用逗号分隔名称。使用系统类装入器装载每个类，并通过调用一个参数构造函数来实例化，该参数构造函数的形式参数类型是FileSystemProvider。按属性中列出的顺序加载和实例化提供程序。如果此进程失败或提供者的方案不等于“file”，则抛出未指定的错误。URI方案通常不考虑具体情况进行比较，但对于默认提供程序，则要求该方案是“file”。通过调用第一个提供程序类并引用系统默认提供程序来实例化它。第二个提供程序类通过调用第一个提供程序实例的引用来实例化。通过使用对第二个实例的引用来调用第三个提供程序类，等等。最后一个被实例化的提供者成为默认的提供者;它的getFileSystem方法使用URI“file:///”来调用，以获得对默认文件系统的引用。
    该方法的后续调用返回第一次调用返回的文件系统。
     */
    public static FileSystem getDefault() {
        return DefaultFileSystemHolder.defaultFileSystem;
    }

    /**
     * Returns a reference to an existing {@code FileSystem}.
     *
     * <p> This method iterates over the {@link FileSystemProvider#installedProviders()
     * installed} providers to locate the provider that is identified by the URI
     * {@link URI#getScheme scheme} of the given URI. URI schemes are compared
     * without regard to case. The exact form of the URI is highly provider
     * dependent. If found, the provider's {@link FileSystemProvider#getFileSystem
     * getFileSystem} method is invoked to obtain a reference to the {@code
     * FileSystem}.
     *
     * <p> Once a file system created by this provider is {@link FileSystem#close
     * closed} it is provider-dependent if this method returns a reference to
     * the closed file system or throws {@link FileSystemNotFoundException}.
     * If the provider allows a new file system to be created with the same URI
     * as a file system it previously created then this method throws the
     * exception if invoked after the file system is closed (and before a new
     * instance is created by the {@link #newFileSystem newFileSystem} method).
     *
     * <p> If a security manager is installed then a provider implementation
     * may require to check a permission before returning a reference to an
     * existing file system. In the case of the {@link FileSystems#getDefault
     * default} file system, no permission check is required.
     *
     * @param   uri  the URI to locate the file system
     *
     * @return  the reference to the file system
     *
     * @throws  IllegalArgumentException
     *          if the pre-conditions for the {@code uri} parameter are not met
     * @throws  FileSystemNotFoundException
     *          if the file system, identified by the URI, does not exist
     * @throws  ProviderNotFoundException
     *          if a provider supporting the URI scheme is not installed
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission
     *          返回对现有文件系统的引用。
    此方法遍历已安装的提供程序，以定位由给定URI的URI方案标识的提供程序。在不考虑情况的情况下，对URI方案进行比较。URI的确切形式高度依赖于提供者。如果找到，将调用提供者的getFileSystem method来获取对文件系统的引用。
    一旦这个提供程序创建的文件系统被关闭，如果这个方法返回一个对关闭文件系统的引用，或者抛出FileSystemNotFoundException，那么它就与提供程序相关。如果提供程序允许创建一个新的文件系统，该文件系统与之前创建的文件系统具有相同的URI，那么如果在文件系统关闭之后(以及在新文件系统方法创建新实例之前)调用该方法，则会抛出异常。
    如果安装了安全管理器，那么提供者实现可能需要在返回对现有文件系统的引用之前检查权限。对于默认文件系统，不需要进行权限检查。
     */
    public static FileSystem getFileSystem(URI uri) {
        String scheme = uri.getScheme();
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider.getFileSystem(uri);
            }
        }
        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    /**
     * Constructs a new file system that is identified by a {@link URI}
     *
     * <p> This method iterates over the {@link FileSystemProvider#installedProviders()
     * installed} providers to locate the provider that is identified by the URI
     * {@link URI#getScheme scheme} of the given URI. URI schemes are compared
     * without regard to case. The exact form of the URI is highly provider
     * dependent. If found, the provider's {@link FileSystemProvider#newFileSystem(URI,Map)
     * newFileSystem(URI,Map)} method is invoked to construct the new file system.
     *
     * <p> Once a file system is {@link FileSystem#close closed} it is
     * provider-dependent if the provider allows a new file system to be created
     * with the same URI as a file system it previously created.
     *
     * <p> <b>Usage Example:</b>
     * Suppose there is a provider identified by the scheme {@code "memory"}
     * installed:
     * <pre>
     *   Map&lt;String,String&gt; env = new HashMap&lt;&gt;();
     *   env.put("capacity", "16G");
     *   env.put("blockSize", "4k");
     *   FileSystem fs = FileSystems.newFileSystem(URI.create("memory:///?name=logfs"), env);
     * </pre>
     *
     * @param   uri
     *          the URI identifying the file system
     * @param   env
     *          a map of provider specific properties to configure the file system;
     *          may be empty
     *
     * @return  a new file system
     *
     * @throws  IllegalArgumentException
     *          if the pre-conditions for the {@code uri} parameter are not met,
     *          or the {@code env} parameter does not contain properties required
     *          by the provider, or a property value is invalid
     * @throws  FileSystemAlreadyExistsException
     *          if the file system has already been created
     * @throws  ProviderNotFoundException
     *          if a provider supporting the URI scheme is not installed
     * @throws  IOException
     *          if an I/O error occurs creating the file system
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission required by the file system provider implementation
     *          构造一个由URI标识的新文件系统
    此方法遍历已安装的提供程序，以定位由给定URI的URI方案标识的提供程序。在不考虑情况的情况下，对URI方案进行比较。URI的确切形式高度依赖于提供者。如果找到，将调用提供者的newFileSystem(URI,Map)方法来构建新的文件系统。
    一旦一个文件系统被关闭，如果提供者允许用它先前创建的文件系统的相同的URI创建一个新的文件系统，那么它就是与提供者相关的。
     */
    public static FileSystem newFileSystem(URI uri, Map<String,?> env)
        throws IOException
    {
        return newFileSystem(uri, env, null);
    }

    /**
     * Constructs a new file system that is identified by a {@link URI}
     *
     * <p> This method first attempts to locate an installed provider in exactly
     * the same manner as the {@link #newFileSystem(URI,Map) newFileSystem(URI,Map)}
     * method. If none of the installed providers support the URI scheme then an
     * attempt is made to locate the provider using the given class loader. If a
     * provider supporting the URI scheme is located then its {@link
     * FileSystemProvider#newFileSystem(URI,Map) newFileSystem(URI,Map)} is
     * invoked to construct the new file system.
     *
     * @param   uri
     *          the URI identifying the file system
     * @param   env
     *          a map of provider specific properties to configure the file system;
     *          may be empty
     * @param   loader
     *          the class loader to locate the provider or {@code null} to only
     *          attempt to locate an installed provider
     *
     * @return  a new file system
     *
     * @throws  IllegalArgumentException
     *          if the pre-conditions for the {@code uri} parameter are not met,
     *          or the {@code env} parameter does not contain properties required
     *          by the provider, or a property value is invalid
     * @throws  FileSystemAlreadyExistsException
     *          if the URI scheme identifies an installed provider and the file
     *          system has already been created
     * @throws  ProviderNotFoundException
     *          if a provider supporting the URI scheme is not found
     * @throws  ServiceConfigurationError
     *          when an error occurs while loading a service provider
     * @throws  IOException
     *          an I/O error occurs creating the file system
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission required by the file system provider implementation
     *          构造一个由URI标识的新文件系统
    此方法首先尝试以与newFileSystem(URI,Map)方法完全相同的方式定位已安装的提供者。如果没有安装的提供程序支持URI方案，则尝试使用给定的类加载程序定位提供程序。如果找到了支持URI方案的提供者，那么将调用它的新文件系统(URI,Map)来构建新的文件系统。
     */
    public static FileSystem newFileSystem(URI uri, Map<String,?> env, ClassLoader loader)
        throws IOException
    {
        String scheme = uri.getScheme();

        // check installed providers
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider.newFileSystem(uri, env);
            }
        }

        // if not found, use service-provider loading facility
        if (loader != null) {
            ServiceLoader<FileSystemProvider> sl = ServiceLoader
                .load(FileSystemProvider.class, loader);
            for (FileSystemProvider provider: sl) {
                if (scheme.equalsIgnoreCase(provider.getScheme())) {
                    return provider.newFileSystem(uri, env);
                }
            }
        }

        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    /**
     * Constructs a new {@code FileSystem} to access the contents of a file as a
     * file system.
     *
     * <p> This method makes use of specialized providers that create pseudo file
     * systems where the contents of one or more files is treated as a file
     * system.
     *
     * <p> This method iterates over the {@link FileSystemProvider#installedProviders()
     * installed} providers. It invokes, in turn, each provider's {@link
     * FileSystemProvider#newFileSystem(Path,Map) newFileSystem(Path,Map)} method
     * with an empty map. If a provider returns a file system then the iteration
     * terminates and the file system is returned. If none of the installed
     * providers return a {@code FileSystem} then an attempt is made to locate
     * the provider using the given class loader. If a provider returns a file
     * system then the lookup terminates and the file system is returned.
     *
     * @param   path
     *          the path to the file
     * @param   loader
     *          the class loader to locate the provider or {@code null} to only
     *          attempt to locate an installed provider
     *
     * @return  a new file system
     *
     * @throws  ProviderNotFoundException
     *          if a provider supporting this file type cannot be located
     * @throws  ServiceConfigurationError
     *          when an error occurs while loading a service provider
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          if a security manager is installed and it denies an unspecified
     *          permission
     *          构造一个新的文件系统来作为文件系统访问文件的内容。
    此方法使用专门的提供程序创建伪文件系统，其中一个或多个文件的内容被视为文件系统。
    此方法遍历已安装的提供程序。它反过来调用每个提供者的新文件系统(路径，映射)方法，并使用一个空映射。如果提供者返回文件系统，则迭代终止，并返回文件系统。如果没有一个已安装的提供程序返回文件系统，则尝试使用给定的类加载程序定位提供程序。如果提供者返回文件系统，那么查找将终止，并返回文件系统。
     */
    public static FileSystem newFileSystem(Path path,
                                           ClassLoader loader)
        throws IOException
    {
        if (path == null)
            throw new NullPointerException();
        Map<String,?> env = Collections.emptyMap();

        // check installed providers
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            try {
                return provider.newFileSystem(path, env);
            } catch (UnsupportedOperationException uoe) {
            }
        }

        // if not found, use service-provider loading facility
        if (loader != null) {
            ServiceLoader<FileSystemProvider> sl = ServiceLoader
                .load(FileSystemProvider.class, loader);
            for (FileSystemProvider provider: sl) {
                try {
                    return provider.newFileSystem(path, env);
                } catch (UnsupportedOperationException uoe) {
                }
            }
        }

        throw new ProviderNotFoundException("Provider not found");
    }
}
