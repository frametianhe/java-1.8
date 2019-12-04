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

package java.nio.charset.spi;

import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * Charset service-provider class.
 *
 * <p> A charset provider is a concrete subclass of this class that has a
 * zero-argument constructor and some number of associated charset
 * implementation classes.  Charset providers may be installed in an instance
 * of the Java platform as extensions, that is, jar files placed into any of
 * the usual extension directories.  Providers may also be made available by
 * adding them to the applet or application class path or by some other
 * platform-specific means.  Charset providers are looked up via the current
 * thread's {@link java.lang.Thread#getContextClassLoader() context class
 * loader}.
 *
 * <p> A charset provider identifies itself with a provider-configuration file
 * named <tt>java.nio.charset.spi.CharsetProvider</tt> in the resource
 * directory <tt>META-INF/services</tt>.  The file should contain a list of
 * fully-qualified concrete charset-provider class names, one per line.  A line
 * is terminated by any one of a line feed (<tt>'\n'</tt>), a carriage return
 * (<tt>'\r'</tt>), or a carriage return followed immediately by a line feed.
 * Space and tab characters surrounding each name, as well as blank lines, are
 * ignored.  The comment character is <tt>'#'</tt> (<tt>'&#92;u0023'</tt>); on
 * each line all characters following the first comment character are ignored.
 * The file must be encoded in UTF-8.
 *
 * <p> If a particular concrete charset provider class is named in more than
 * one configuration file, or is named in the same configuration file more than
 * once, then the duplicates will be ignored.  The configuration file naming a
 * particular provider need not be in the same jar file or other distribution
 * unit as the provider itself.  The provider must be accessible from the same
 * class loader that was initially queried to locate the configuration file;
 * this is not necessarily the class loader that loaded the file. </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see java.nio.charset.Charset
 * 字符集服务提供者类。
charset提供程序是该类的一个具体子类，它具有零参数构造函数和一些相关的charset实现类。Charset提供程序可以作为扩展安装在Java平台的实例中，也就是说，将jar文件放置在任何通常的扩展目录中。还可以通过将提供程序添加到applet或应用程序类路径或其他特定于平台的方法来提供。通过当前线程的上下文类装入器查找Charset提供程序。
charset提供程序用一个名为java.nio.charset.spi的提供程序配置文件标识自己。资源目录META-INF/services中的CharsetProvider。该文件应该包含一个全限定的具体charset-provider类名称列表，每行一个。行以行提要('\n')、节回车('\r')或节回车(后跟行提要)中的任何一个作为终止。每个名称周围的空格和制表符以及空行都被忽略。注释字符为'#' ('\u0023');在每一行中，跟随第一个注释字符的所有字符都被忽略。文件必须用UTF-8编码。
如果一个特定的charset provider类在多个配置文件中命名，或者在同一个配置文件中多次命名，那么重复的内容将被忽略。命名特定提供者的配置文件不需要与提供者本身位于相同的jar文件或其他分发单元中。提供者必须可以从最初查询的类装入器中访问，以定位配置文件;这并不一定是加载该文件的类装入器。
 */

public abstract class CharsetProvider {

    /**
     * Initializes a new charset provider.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("charsetProvider")</tt>
     */
    protected CharsetProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("charsetProvider"));
    }

    /**
     * Creates an iterator that iterates over the charsets supported by this
     * provider.  This method is used in the implementation of the {@link
     * java.nio.charset.Charset#availableCharsets Charset.availableCharsets}
     * method.
     * 创建一个迭代器，该迭代器遍历此提供程序支持的字符集。此方法用于实现Charset。availableCharsets方法。
     *
     * @return  The new iterator
     */
    public abstract Iterator<Charset> charsets();

    /**
     * Retrieves a charset for the given charset name.检索给定字符集名称的字符集。
     *
     * @param  charsetName
     *         The name of the requested charset; may be either
     *         a canonical name or an alias
     *
     * @return  A charset object for the named charset,
     *          or <tt>null</tt> if the named charset
     *          is not supported by this provider
     */
    public abstract Charset charsetForName(String charsetName);

}
