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

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.spi.CharsetProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;
import java.util.SortedMap;
import java.util.TreeMap;
import sun.misc.ASCIICaseInsensitiveComparator;
import sun.nio.cs.StandardCharsets;
import sun.nio.cs.ThreadLocalCoders;
import sun.security.action.GetPropertyAction;


/**
 * A named mapping between sequences of sixteen-bit Unicode <a
 * href="../../lang/Character.html#unicode">code units</a> and sequences of
 * bytes.  This class defines methods for creating decoders and encoders and
 * for retrieving the various names associated with a charset.  Instances of
 * this class are immutable.
 *
 * <p> This class also defines static methods for testing whether a particular
 * charset is supported, for locating charset instances by name, and for
 * constructing a map that contains every charset for which support is
 * available in the current Java virtual machine.  Support for new charsets can
 * be added via the service-provider interface defined in the {@link
 * java.nio.charset.spi.CharsetProvider} class.
 *
 * <p> All of the methods defined in this class are safe for use by multiple
 * concurrent threads.
 *
 *
 * <a name="names"></a><a name="charenc"></a>
 * <h2>Charset names</h2>
 *
 * <p> Charsets are named by strings composed of the following characters:
 *
 * <ul>
 *
 *   <li> The uppercase letters <tt>'A'</tt> through <tt>'Z'</tt>
 *        (<tt>'&#92;u0041'</tt>&nbsp;through&nbsp;<tt>'&#92;u005a'</tt>),
 *
 *   <li> The lowercase letters <tt>'a'</tt> through <tt>'z'</tt>
 *        (<tt>'&#92;u0061'</tt>&nbsp;through&nbsp;<tt>'&#92;u007a'</tt>),
 *
 *   <li> The digits <tt>'0'</tt> through <tt>'9'</tt>
 *        (<tt>'&#92;u0030'</tt>&nbsp;through&nbsp;<tt>'&#92;u0039'</tt>),
 *
 *   <li> The dash character <tt>'-'</tt>
 *        (<tt>'&#92;u002d'</tt>,&nbsp;<small>HYPHEN-MINUS</small>),
 *
 *   <li> The plus character <tt>'+'</tt>
 *        (<tt>'&#92;u002b'</tt>,&nbsp;<small>PLUS SIGN</small>),
 *
 *   <li> The period character <tt>'.'</tt>
 *        (<tt>'&#92;u002e'</tt>,&nbsp;<small>FULL STOP</small>),
 *
 *   <li> The colon character <tt>':'</tt>
 *        (<tt>'&#92;u003a'</tt>,&nbsp;<small>COLON</small>), and
 *
 *   <li> The underscore character <tt>'_'</tt>
 *        (<tt>'&#92;u005f'</tt>,&nbsp;<small>LOW&nbsp;LINE</small>).
 *
 * </ul>
 *
 * A charset name must begin with either a letter or a digit.  The empty string
 * is not a legal charset name.  Charset names are not case-sensitive; that is,
 * case is always ignored when comparing charset names.  Charset names
 * generally follow the conventions documented in <a
 * href="http://www.ietf.org/rfc/rfc2278.txt"><i>RFC&nbsp;2278:&nbsp;IANA Charset
 * Registration Procedures</i></a>.
 *
 * <p> Every charset has a <i>canonical name</i> and may also have one or more
 * <i>aliases</i>.  The canonical name is returned by the {@link #name() name} method
 * of this class.  Canonical names are, by convention, usually in upper case.
 * The aliases of a charset are returned by the {@link #aliases() aliases}
 * method.
 *
 * <p><a name="hn">Some charsets have an <i>historical name</i> that is defined for
 * compatibility with previous versions of the Java platform.</a>  A charset's
 * historical name is either its canonical name or one of its aliases.  The
 * historical name is returned by the <tt>getEncoding()</tt> methods of the
 * {@link java.io.InputStreamReader#getEncoding InputStreamReader} and {@link
 * java.io.OutputStreamWriter#getEncoding OutputStreamWriter} classes.
 *
 * <p><a name="iana"> </a>If a charset listed in the <a
 * href="http://www.iana.org/assignments/character-sets"><i>IANA Charset
 * Registry</i></a> is supported by an implementation of the Java platform then
 * its canonical name must be the name listed in the registry. Many charsets
 * are given more than one name in the registry, in which case the registry
 * identifies one of the names as <i>MIME-preferred</i>.  If a charset has more
 * than one registry name then its canonical name must be the MIME-preferred
 * name and the other names in the registry must be valid aliases.  If a
 * supported charset is not listed in the IANA registry then its canonical name
 * must begin with one of the strings <tt>"X-"</tt> or <tt>"x-"</tt>.
 *
 * <p> The IANA charset registry does change over time, and so the canonical
 * name and the aliases of a particular charset may also change over time.  To
 * ensure compatibility it is recommended that no alias ever be removed from a
 * charset, and that if the canonical name of a charset is changed then its
 * previous canonical name be made into an alias.
 *
 *
 * <h2>Standard charsets</h2>
 *
 *
 *
 * <p><a name="standard">Every implementation of the Java platform is required to support the
 * following standard charsets.</a>  Consult the release documentation for your
 * implementation to see if any other charsets are supported.  The behavior
 * of such optional charsets may differ between implementations.
 *
 * <blockquote><table width="80%" summary="Description of standard charsets">
 * <tr><th align="left">Charset</th><th align="left">Description</th></tr>
 * <tr><td valign=top><tt>US-ASCII</tt></td>
 *     <td>Seven-bit ASCII, a.k.a. <tt>ISO646-US</tt>,
 *         a.k.a. the Basic Latin block of the Unicode character set</td></tr>
 * <tr><td valign=top><tt>ISO-8859-1&nbsp;&nbsp;</tt></td>
 *     <td>ISO Latin Alphabet No. 1, a.k.a. <tt>ISO-LATIN-1</tt></td></tr>
 * <tr><td valign=top><tt>UTF-8</tt></td>
 *     <td>Eight-bit UCS Transformation Format</td></tr>
 * <tr><td valign=top><tt>UTF-16BE</tt></td>
 *     <td>Sixteen-bit UCS Transformation Format,
 *         big-endian byte&nbsp;order</td></tr>
 * <tr><td valign=top><tt>UTF-16LE</tt></td>
 *     <td>Sixteen-bit UCS Transformation Format,
 *         little-endian byte&nbsp;order</td></tr>
 * <tr><td valign=top><tt>UTF-16</tt></td>
 *     <td>Sixteen-bit UCS Transformation Format,
 *         byte&nbsp;order identified by an optional byte-order mark</td></tr>
 * </table></blockquote>
 *
 * <p> The <tt>UTF-8</tt> charset is specified by <a
 * href="http://www.ietf.org/rfc/rfc2279.txt"><i>RFC&nbsp;2279</i></a>; the
 * transformation format upon which it is based is specified in
 * Amendment&nbsp;2 of ISO&nbsp;10646-1 and is also described in the <a
 * href="http://www.unicode.org/unicode/standard/standard.html"><i>Unicode
 * Standard</i></a>.
 *
 * <p> The <tt>UTF-16</tt> charsets are specified by <a
 * href="http://www.ietf.org/rfc/rfc2781.txt"><i>RFC&nbsp;2781</i></a>; the
 * transformation formats upon which they are based are specified in
 * Amendment&nbsp;1 of ISO&nbsp;10646-1 and are also described in the <a
 * href="http://www.unicode.org/unicode/standard/standard.html"><i>Unicode
 * Standard</i></a>.
 *
 * <p> The <tt>UTF-16</tt> charsets use sixteen-bit quantities and are
 * therefore sensitive to byte order.  In these encodings the byte order of a
 * stream may be indicated by an initial <i>byte-order mark</i> represented by
 * the Unicode character <tt>'&#92;uFEFF'</tt>.  Byte-order marks are handled
 * as follows:
 *
 * <ul>
 *
 *   <li><p> When decoding, the <tt>UTF-16BE</tt> and <tt>UTF-16LE</tt>
 *   charsets interpret the initial byte-order marks as a <small>ZERO-WIDTH
 *   NON-BREAKING SPACE</small>; when encoding, they do not write
 *   byte-order marks. </p></li>

 *
 *   <li><p> When decoding, the <tt>UTF-16</tt> charset interprets the
 *   byte-order mark at the beginning of the input stream to indicate the
 *   byte-order of the stream but defaults to big-endian if there is no
 *   byte-order mark; when encoding, it uses big-endian byte order and writes
 *   a big-endian byte-order mark. </p></li>
 *
 * </ul>
 *
 * In any case, byte order marks occurring after the first element of an
 * input sequence are not omitted since the same code is used to represent
 * <small>ZERO-WIDTH NON-BREAKING SPACE</small>.
 *
 * <p> Every instance of the Java virtual machine has a default charset, which
 * may or may not be one of the standard charsets.  The default charset is
 * determined during virtual-machine startup and typically depends upon the
 * locale and charset being used by the underlying operating system. </p>
 *
 * <p>The {@link StandardCharsets} class defines constants for each of the
 * standard charsets.
 *
 * <h2>Terminology</h2>
 *
 * <p> The name of this class is taken from the terms used in
 * <a href="http://www.ietf.org/rfc/rfc2278.txt"><i>RFC&nbsp;2278</i></a>.
 * In that document a <i>charset</i> is defined as the combination of
 * one or more coded character sets and a character-encoding scheme.
 * (This definition is confusing; some other software systems define
 * <i>charset</i> as a synonym for <i>coded character set</i>.)
 *
 * <p> A <i>coded character set</i> is a mapping between a set of abstract
 * characters and a set of integers.  US-ASCII, ISO&nbsp;8859-1,
 * JIS&nbsp;X&nbsp;0201, and Unicode are examples of coded character sets.
 *
 * <p> Some standards have defined a <i>character set</i> to be simply a
 * set of abstract characters without an associated assigned numbering.
 * An alphabet is an example of such a character set.  However, the subtle
 * distinction between <i>character set</i> and <i>coded character set</i>
 * is rarely used in practice; the former has become a short form for the
 * latter, including in the Java API specification.
 *
 * <p> A <i>character-encoding scheme</i> is a mapping between one or more
 * coded character sets and a set of octet (eight-bit byte) sequences.
 * UTF-8, UTF-16, ISO&nbsp;2022, and EUC are examples of
 * character-encoding schemes.  Encoding schemes are often associated with
 * a particular coded character set; UTF-8, for example, is used only to
 * encode Unicode.  Some schemes, however, are associated with multiple
 * coded character sets; EUC, for example, can be used to encode
 * characters in a variety of Asian coded character sets.
 *
 * <p> When a coded character set is used exclusively with a single
 * character-encoding scheme then the corresponding charset is usually
 * named for the coded character set; otherwise a charset is usually named
 * for the encoding scheme and, possibly, the locale of the coded
 * character sets that it supports.  Hence <tt>US-ASCII</tt> is both the
 * name of a coded character set and of the charset that encodes it, while
 * <tt>EUC-JP</tt> is the name of the charset that encodes the
 * JIS&nbsp;X&nbsp;0201, JIS&nbsp;X&nbsp;0208, and JIS&nbsp;X&nbsp;0212
 * coded character sets for the Japanese language.
 *
 * <p> The native character encoding of the Java programming language is
 * UTF-16.  A charset in the Java platform therefore defines a mapping
 * between sequences of sixteen-bit UTF-16 code units (that is, sequences
 * of chars) and sequences of bytes. </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see CharsetDecoder
 * @see CharsetEncoder
 * @see java.nio.charset.spi.CharsetProvider
 * @see java.lang.Character
 * 16位Unicode编码单元序列和字节序列之间的命名映射。这个类定义了创建解码器和编码器以及检索与字符集相关的各种名称的方法。该类的实例是不可变的。
该类还定义了静态方法，用于测试是否支持特定的字符集、按名称定位字符集实例，以及构造包含当前Java虚拟机中可用的每个字符集的映射。可以通过在CharsetProvider类中定义的服务提供者接口添加对新字符集的支持。
该类中定义的所有方法对于多个并发线程都是安全的。
 */

public abstract class Charset
    implements Comparable<Charset>
{

    /* -- Static methods -- */

    private static volatile String bugLevel = null;

    static boolean atBugLevel(String bl) {              // package-private
        String level = bugLevel;
        if (level == null) {
            if (!sun.misc.VM.isBooted())
                return false;
            bugLevel = level = AccessController.doPrivileged(
                new GetPropertyAction("sun.nio.cs.bugLevel", ""));
        }
        return level.equals(bl);
    }

    /**
     * Checks that the given string is a legal charset name. </p>检查给定字符串是否是合法的字符集名称。
     *
     * @param  s
     *         A purported charset name
     *
     * @throws  IllegalCharsetNameException
     *          If the given name is not a legal charset name
     */
    private static void checkName(String s) {
        int n = s.length();
        if (!atBugLevel("1.4")) {
            if (n == 0)
                throw new IllegalCharsetNameException(s);
        }
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') continue;
            if (c >= 'a' && c <= 'z') continue;
            if (c >= '0' && c <= '9') continue;
            if (c == '-' && i != 0) continue;
            if (c == '+' && i != 0) continue;
            if (c == ':' && i != 0) continue;
            if (c == '_' && i != 0) continue;
            if (c == '.' && i != 0) continue;
            throw new IllegalCharsetNameException(s);
        }
    }

    /* The standard set of charsets */
    private static CharsetProvider standardProvider = new StandardCharsets();

    // Cache of the most-recently-returned charsets,
    // along with the names that were used to find them
    //
    private static volatile Object[] cache1 = null; // "Level 1" cache
    private static volatile Object[] cache2 = null; // "Level 2" cache

    private static void cache(String charsetName, Charset cs) {
        cache2 = cache1;
        cache1 = new Object[] { charsetName, cs };
    }

    // Creates an iterator that walks over the available providers, ignoring 创建一个遍历器，遍历可用的提供程序，忽略
    // those whose lookup or instantiation causes a security exception to be 查找或实例化导致安全异常的那些
    // thrown.  Should be invoked with full privileges. 抛出。应该使用完全特权调用。
    //
    private static Iterator<CharsetProvider> providers() {
        return new Iterator<CharsetProvider>() {

                ClassLoader cl = ClassLoader.getSystemClassLoader();
                ServiceLoader<CharsetProvider> sl =
                    ServiceLoader.load(CharsetProvider.class, cl);
                Iterator<CharsetProvider> i = sl.iterator();

                CharsetProvider next = null;

                private boolean getNext() {
                    while (next == null) {
                        try {
                            if (!i.hasNext())
                                return false;
                            next = i.next();
                        } catch (ServiceConfigurationError sce) {
                            if (sce.getCause() instanceof SecurityException) {
                                // Ignore security exceptions
                                continue;
                            }
                            throw sce;
                        }
                    }
                    return true;
                }

                public boolean hasNext() {
                    return getNext();
                }

                public CharsetProvider next() {
                    if (!getNext())
                        throw new NoSuchElementException();
                    CharsetProvider n = next;
                    next = null;
                    return n;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
    }

    // Thread-local gate to prevent recursive provider lookups 防止递归提供者查找的线程本地门
    private static ThreadLocal<ThreadLocal<?>> gate =
            new ThreadLocal<ThreadLocal<?>>();

    private static Charset lookupViaProviders(final String charsetName) {

        // The runtime startup sequence looks up standard charsets as a 运行时启动序列将标准字符集查找为a
        // consequence of the VM's invocation of System.initializeSystemClass VM调用System.initializeSystemClass的结果
        // in order to, e.g., set system properties and encode filenames.  At 为了，例如，设置系统属性和编码文件名。在
        // that point the application class loader has not been initialized, 此时应用程序类装入器还没有初始化，
        // however, so we can't look for providers because doing so will cause 但是，我们不能寻找提供者，因为这样做会导致
        // that loader to be prematurely initialized with incomplete 加载程序将被初始化为不完整
        // information. 信息。
        //
        if (!sun.misc.VM.isBooted())
            return null;

        if (gate.get() != null)
            // Avoid recursive provider lookups
            return null;
        try {
            gate.set(gate);

            return AccessController.doPrivileged(
                new PrivilegedAction<Charset>() {
                    public Charset run() {
                        for (Iterator<CharsetProvider> i = providers();
                             i.hasNext();) {
                            CharsetProvider cp = i.next();
                            Charset cs = cp.charsetForName(charsetName);
                            if (cs != null)
                                return cs;
                        }
                        return null;
                    }
                });

        } finally {
            gate.set(null);
        }
    }

    /* The extended set of charsets */
    private static class ExtendedProviderHolder {
        static final CharsetProvider extendedProvider = extendedProvider();
        // returns ExtendedProvider, if installed
        private static CharsetProvider extendedProvider() {
            return AccessController.doPrivileged(
                       new PrivilegedAction<CharsetProvider>() {
                           public CharsetProvider run() {
                                try {
                                    Class<?> epc
                                        = Class.forName("sun.nio.cs.ext.ExtendedCharsets");
                                    return (CharsetProvider)epc.newInstance();
                                } catch (ClassNotFoundException x) {
                                    // Extended charsets not available
                                    // (charsets.jar not present)
                                } catch (InstantiationException |
                                         IllegalAccessException x) {
                                  throw new Error(x);
                                }
                                return null;
                            }
                        });
        }
    }

    private static Charset lookupExtendedCharset(String charsetName) {
        CharsetProvider ecp = ExtendedProviderHolder.extendedProvider;
        return (ecp != null) ? ecp.charsetForName(charsetName) : null;
    }

    private static Charset lookup(String charsetName) {
        if (charsetName == null)
            throw new IllegalArgumentException("Null charset name");
        Object[] a;
        if ((a = cache1) != null && charsetName.equals(a[0]))
            return (Charset)a[1];
        // We expect most programs to use one Charset repeatedly.
        // We convey a hint to this effect to the VM by putting the
        // level 1 cache miss code in a separate method.
        return lookup2(charsetName);
    }

    private static Charset lookup2(String charsetName) {
        Object[] a;
        if ((a = cache2) != null && charsetName.equals(a[0])) {
            cache2 = cache1;
            cache1 = a;
            return (Charset)a[1];
        }
        Charset cs;
        if ((cs = standardProvider.charsetForName(charsetName)) != null ||
            (cs = lookupExtendedCharset(charsetName))           != null ||
            (cs = lookupViaProviders(charsetName))              != null)
        {
            cache(charsetName, cs);
            return cs;
        }

        /* Only need to check the name if we didn't find a charset for it 只需要检查名称，如果我们没有找到它的字符集*/
        checkName(charsetName);
        return null;
    }

    /**
     * Tells whether the named charset is supported.
     *
     * @param  charsetName
     *         The name of the requested charset; may be either
     *         a canonical name or an alias
     *
     * @return  <tt>true</tt> if, and only if, support for the named charset
     *          is available in the current Java virtual machine
     *
     * @throws IllegalCharsetNameException
     *         If the given charset name is illegal
     *
     * @throws  IllegalArgumentException
     *          If the given <tt>charsetName</tt> is null
     *          告诉是否支持已命名的字符集。
     */
    public static boolean isSupported(String charsetName) {
        return (lookup(charsetName) != null);
    }

    /**
     * Returns a charset object for the named charset.
     *
     * @param  charsetName
     *         The name of the requested charset; may be either
     *         a canonical name or an alias
     *
     * @return  A charset object for the named charset
     *
     * @throws  IllegalCharsetNameException
     *          If the given charset name is illegal
     *
     * @throws  IllegalArgumentException
     *          If the given <tt>charsetName</tt> is null
     *
     * @throws  UnsupportedCharsetException
     *          If no support for the named charset is available
     *          in this instance of the Java virtual machine
     *          返回名为charset的charset对象。
     */
    public static Charset forName(String charsetName) {
        Charset cs = lookup(charsetName);
        if (cs != null)
            return cs;
        throw new UnsupportedCharsetException(charsetName);
    }

    // Fold charsets from the given iterator into the given map, ignoring 将给定迭代器中的字符集折叠到给定的映射中，忽略
    // charsets whose names already have entries in the map. 名称在映射中已经有条目的字符集。
    //
    private static void put(Iterator<Charset> i, Map<String,Charset> m) {
        while (i.hasNext()) {
            Charset cs = i.next();
            if (!m.containsKey(cs.name()))
                m.put(cs.name(), cs);
        }
    }

    /**
     * Constructs a sorted map from canonical charset names to charset objects.
     *
     * <p> The map returned by this method will have one entry for each charset
     * for which support is available in the current Java virtual machine.  If
     * two or more supported charsets have the same canonical name then the
     * resulting map will contain just one of them; which one it will contain
     * is not specified. </p>
     *
     * <p> The invocation of this method, and the subsequent use of the
     * resulting map, may cause time-consuming disk or network I/O operations
     * to occur.  This method is provided for applications that need to
     * enumerate all of the available charsets, for example to allow user
     * charset selection.  This method is not used by the {@link #forName
     * forName} method, which instead employs an efficient incremental lookup
     * algorithm.
     *
     * <p> This method may return different results at different times if new
     * charset providers are dynamically made available to the current Java
     * virtual machine.  In the absence of such changes, the charsets returned
     * by this method are exactly those that can be retrieved via the {@link
     * #forName forName} method.  </p>
     *
     * @return An immutable, case-insensitive map from canonical charset names
     *         to charset objects
     *         构造从规范字符集名称到字符集对象的排序映射。
    此方法返回的映射将为当前Java虚拟机中提供支持的每个字符集提供一个条目。如果两个或多个受支持的字符集具有相同的规范名称，那么结果映射将只包含其中一个;不指定它将包含哪个。
    此方法的调用以及随后对结果映射的使用可能会导致耗时的磁盘或网络I/O操作。此方法提供给需要枚举所有可用字符集的应用程序，例如，允许用户字符集选择。forName方法不使用此方法，而是使用高效的增量查找算法。
    如果动态地向当前Java虚拟机提供新的字符集提供程序，该方法可能在不同的时间返回不同的结果。在缺少这些更改的情况下，该方法返回的字符集正是通过forName方法检索到的。
     */
    public static SortedMap<String,Charset> availableCharsets() {
        return AccessController.doPrivileged(
            new PrivilegedAction<SortedMap<String,Charset>>() {
                public SortedMap<String,Charset> run() {
                    TreeMap<String,Charset> m =
                        new TreeMap<String,Charset>(
                            ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
                    put(standardProvider.charsets(), m);
                    CharsetProvider ecp = ExtendedProviderHolder.extendedProvider;
                    if (ecp != null)
                        put(ecp.charsets(), m);
                    for (Iterator<CharsetProvider> i = providers(); i.hasNext();) {
                        CharsetProvider cp = i.next();
                        put(cp.charsets(), m);
                    }
                    return Collections.unmodifiableSortedMap(m);
                }
            });
    }

    private static volatile Charset defaultCharset;

    /**
     * Returns the default charset of this Java virtual machine.
     *
     * <p> The default charset is determined during virtual-machine startup and
     * typically depends upon the locale and charset of the underlying
     * operating system.
     *
     * @return  A charset object for the default charset
     *
     * @since 1.5
     * 返回此Java虚拟机的默认字符集。
    默认字符集是在虚拟机启动期间确定的，通常取决于底层操作系统的语言环境和字符集。
     */
    public static Charset defaultCharset() {
        if (defaultCharset == null) {
            synchronized (Charset.class) {
                String csn = AccessController.doPrivileged(
                    new GetPropertyAction("file.encoding"));
                Charset cs = lookup(csn);
                if (cs != null)
                    defaultCharset = cs;
                else
                    defaultCharset = forName("UTF-8");
            }
        }
        return defaultCharset;
    }


    /* -- Instance fields and methods -- */

    private final String name;          // tickles a bug in oldjavac
    private final String[] aliases;     // tickles a bug in oldjavac
    private Set<String> aliasSet = null;

    /**
     * Initializes a new charset with the given canonical name and alias
     * set.
     *
     * @param  canonicalName
     *         The canonical name of this charset
     *
     * @param  aliases
     *         An array of this charset's aliases, or null if it has no aliases
     *
     * @throws IllegalCharsetNameException
     *         If the canonical name or any of the aliases are illegal
     *         初始化一个具有给定的规范名称和别名集的新字符集。
     */
    protected Charset(String canonicalName, String[] aliases) {
        checkName(canonicalName);
        String[] as = (aliases == null) ? new String[0] : aliases;
        for (int i = 0; i < as.length; i++)
            checkName(as[i]);
        this.name = canonicalName;
        this.aliases = as;
    }

    /**
     * Returns this charset's canonical name.返回此字符集的规范名。
     *
     * @return  The canonical name of this charset
     */
    public final String name() {
        return name;
    }

    /**
     * Returns a set containing this charset's aliases.返回包含此字符集别名的集合。
     *
     * @return  An immutable set of this charset's aliases
     */
    public final Set<String> aliases() {
        if (aliasSet != null)
            return aliasSet;
        int n = aliases.length;
        HashSet<String> hs = new HashSet<String>(n);
        for (int i = 0; i < n; i++)
            hs.add(aliases[i]);
        aliasSet = Collections.unmodifiableSet(hs);
        return aliasSet;
    }

    /**
     * Returns this charset's human-readable name for the default locale.
     *
     * <p> The default implementation of this method simply returns this
     * charset's canonical name.  Concrete subclasses of this class may
     * override this method in order to provide a localized display name. </p>
     *
     * @return  The display name of this charset in the default locale
     * 返回此字符集的缺省语言环境的可读名称。
    此方法的默认实现仅返回此字符集的规范名。该类的具体子类可以重写此方法，以便提供本地化的显示名称。
     */
    public String displayName() {
        return name;
    }

    /**
     * Tells whether or not this charset is registered in the <a
     * href="http://www.iana.org/assignments/character-sets">IANA Charset
     * Registry</a>.
     *
     * @return  <tt>true</tt> if, and only if, this charset is known by its
     *          implementor to be registered with the IANA
     *          说明这个字符集是否在IANA charset注册表中注册。
     */
    public final boolean isRegistered() {
        return !name.startsWith("X-") && !name.startsWith("x-");
    }

    /**
     * Returns this charset's human-readable name for the given locale.
     *
     * <p> The default implementation of this method simply returns this
     * charset's canonical name.  Concrete subclasses of this class may
     * override this method in order to provide a localized display name. </p>
     *
     * @param  locale
     *         The locale for which the display name is to be retrieved
     *
     * @return  The display name of this charset in the given locale
     * 返回给定区域设置的人类可读名称。
    此方法的默认实现仅返回此字符集的规范名。该类的具体子类可以重写此方法，以便提供本地化的显示名称
     */
    public String displayName(Locale locale) {
        return name;
    }

    /**
     * Tells whether or not this charset contains the given charset.
     *
     * <p> A charset <i>C</i> is said to <i>contain</i> a charset <i>D</i> if,
     * and only if, every character representable in <i>D</i> is also
     * representable in <i>C</i>.  If this relationship holds then it is
     * guaranteed that every string that can be encoded in <i>D</i> can also be
     * encoded in <i>C</i> without performing any replacements.
     *
     * <p> That <i>C</i> contains <i>D</i> does not imply that each character
     * representable in <i>C</i> by a particular byte sequence is represented
     * in <i>D</i> by the same byte sequence, although sometimes this is the
     * case.
     *
     * <p> Every charset contains itself.
     *
     * <p> This method computes an approximation of the containment relation:
     * If it returns <tt>true</tt> then the given charset is known to be
     * contained by this charset; if it returns <tt>false</tt>, however, then
     * it is not necessarily the case that the given charset is not contained
     * in this charset.
     *
     * @param   cs
     *          The given charset
     *
     * @return  <tt>true</tt> if the given charset is contained in this charset
     * 告诉此字符集是否包含给定的字符集。
    字符集C被认为包含一个字符集D,如果且仅如果在D也可表示的每个字符表示的C .如果这种关系那么可以保证每一个字符串,可以编码在D也可以用C编码没有执行任何替代品。
    C包含D并不意味着在C中由特定字节序列表示的每个字符在D中由相同的字节序列表示，尽管有时确实如此。
    每个字符集都包含本身。
    这个方法计算包含关系的近似:如果它返回true，那么给定的字符集就被这个字符集所包含;如果返回false，那么给定的字符集并不一定不包含在这个字符集中。
     */
    public abstract boolean contains(Charset cs);

    /**
     * Constructs a new decoder for this charset.
     *
     * @return  A new decoder for this charset
     */
    public abstract CharsetDecoder newDecoder();

    /**
     * Constructs a new encoder for this charset.
     *
     * @return  A new encoder for this charset
     *
     * @throws  UnsupportedOperationException
     *          If this charset does not support encoding
     */
    public abstract CharsetEncoder newEncoder();

    /**
     * Tells whether or not this charset supports encoding.
     *
     * <p> Nearly all charsets support encoding.  The primary exceptions are
     * special-purpose <i>auto-detect</i> charsets whose decoders can determine
     * which of several possible encoding schemes is in use by examining the
     * input byte sequence.  Such charsets do not support encoding because
     * there is no way to determine which encoding should be used on output.
     * Implementations of such charsets should override this method to return
     * <tt>false</tt>. </p>
     *
     * @return  <tt>true</tt> if, and only if, this charset supports encoding
     * 告诉此字符集是否支持编码。
    几乎所有字符集都支持编码。主要的例外是特殊用途的自动检测字符集，它的解码器可以通过检查输入字节序列来确定使用了几种可能的编码方案中的哪一种。此类字符集不支持编码，因为无法确定在输出上应该使用哪种编码。此类字符集的实现应该重写此方法以返回false。
     */
    public boolean canEncode() {
        return true;
    }

    /**
     * Convenience method that decodes bytes in this charset into Unicode
     * characters.
     *
     * <p> An invocation of this method upon a charset <tt>cs</tt> returns the
     * same result as the expression
     *
     * <pre>
     *     cs.newDecoder()
     *       .onMalformedInput(CodingErrorAction.REPLACE)
     *       .onUnmappableCharacter(CodingErrorAction.REPLACE)
     *       .decode(bb); </pre>
     *
     * except that it is potentially more efficient because it can cache
     * decoders between successive invocations.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  In order
     * to detect such sequences, use the {@link
     * CharsetDecoder#decode(java.nio.ByteBuffer)} method directly.  </p>
     *
     * @param  bb  The byte buffer to be decoded
     *
     * @return  A char buffer containing the decoded characters
     *
    将这个字符集中的字节解码为Unicode字符的方便方法。
    在字符集cs上调用此方法会返回与表达式相同的结果
     */
    public final CharBuffer decode(ByteBuffer bb) {
        try {
            return ThreadLocalCoders.decoderFor(this)
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .decode(bb);
        } catch (CharacterCodingException x) {
            throw new Error(x);         // Can't happen
        }
    }

    /**
     * Convenience method that encodes Unicode characters into bytes in this
     * charset.
     *
     * <p> An invocation of this method upon a charset <tt>cs</tt> returns the
     * same result as the expression
     *
     * <pre>
     *     cs.newEncoder()
     *       .onMalformedInput(CodingErrorAction.REPLACE)
     *       .onUnmappableCharacter(CodingErrorAction.REPLACE)
     *       .encode(bb); </pre>
     *
     * except that it is potentially more efficient because it can cache
     * encoders between successive invocations.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  In order to
     * detect such sequences, use the {@link
     * CharsetEncoder#encode(java.nio.CharBuffer)} method directly.  </p>
     *
     * @param  cb  The char buffer to be encoded
     *
     * @return  A byte buffer containing the encoded characters
     * 在此字符集中将Unicode字符编码成字节的方便方法。
    在字符集cs上调用此方法会返回与表达式相同的结果
    除了它可能更有效，因为它可以在连续调用之间缓存编码器。
    这个方法总是用这个字符集的默认替换字符串替换malformed-input和unmappable-character序列。为了检测这些序列，直接使用CharsetEncoder.encode(CharBuffer)方法。
     */
    public final ByteBuffer encode(CharBuffer cb) {
        try {
            return ThreadLocalCoders.encoderFor(this)
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .encode(cb);
        } catch (CharacterCodingException x) {
            throw new Error(x);         // Can't happen
        }
    }

    /**
     * Convenience method that encodes a string into bytes in this charset.
     *
     * <p> An invocation of this method upon a charset <tt>cs</tt> returns the
     * same result as the expression
     *
     * <pre>
     *     cs.encode(CharBuffer.wrap(s)); </pre>
     *
     * @param  str  The string to be encoded
     *
     * @return  A byte buffer containing the encoded characters
     * 方便的方法，将字符串编码为这个字符集中的字节。
    在字符集cs上调用此方法会返回与表达式相同的结果
     */
    public final ByteBuffer encode(String str) {
        return encode(CharBuffer.wrap(str));
    }

    /**
     * Compares this charset to another.
     *
     * <p> Charsets are ordered by their canonical names, without regard to
     * case. </p>
     *
     * @param  that
     *         The charset to which this charset is to be compared
     *
     * @return A negative integer, zero, or a positive integer as this charset
     *         is less than, equal to, or greater than the specified charset
     *         将这个字符集与另一个字符集进行比较。
    字符集是按照它们的教名排序的，而不考虑大小写。
     */
    public final int compareTo(Charset that) {
        return (name().compareToIgnoreCase(that.name()));
    }

    /**
     * Computes a hashcode for this charset.
     *
     * @return  An integer hashcode
     */
    public final int hashCode() {
        return name().hashCode();
    }

    /**
     * Tells whether or not this object is equal to another.
     *
     * <p> Two charsets are equal if, and only if, they have the same canonical
     * names.  A charset is never equal to any other type of object.  </p>
     *
     * @return  <tt>true</tt> if, and only if, this charset is equal to the
     *          given object
     */
    public final boolean equals(Object ob) {
        if (!(ob instanceof Charset))
            return false;
        if (this == ob)
            return true;
        return name.equals(((Charset)ob).name());
    }

    /**
     * Returns a string describing this charset.
     *
     * @return  A string describing this charset
     */
    public final String toString() {
        return name();
    }

}
