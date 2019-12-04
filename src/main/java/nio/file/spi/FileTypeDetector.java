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

package java.nio.file.spi;

import java.nio.file.Path;
import java.io.IOException;

/**
 * A file type detector for probing a file to guess its file type.
 *
 * <p> A file type detector is a concrete implementation of this class, has a
 * zero-argument constructor, and implements the abstract methods specified
 * below.
 *
 * <p> The means by which a file type detector determines the file type is
 * highly implementation specific. A simple implementation might examine the
 * <em>file extension</em> (a convention used in some platforms) and map it to
 * a file type. In other cases, the file type may be stored as a file <a
 * href="../attribute/package-summary.html"> attribute</a> or the bytes in a
 * file may be examined to guess its file type.
 *
 * @see java.nio.file.Files#probeContentType(Path)
 *
 * @since 1.7
 * 用于探测文件以猜测其文件类型的文件类型检测器。
文件类型检测器是该类的具体实现，具有一个零参数构造函数，并实现下面指定的抽象方法。
文件类型检测器用来确定文件类型的方法是高度特定于实现的。一个简单的实现可以检查文件扩展名(在某些平台中使用的约定)并将其映射到文件类型。在其他情况下，文件类型可以存储为文件属性，或者可以检查文件中的字节来猜测其文件类型。
 */

public abstract class FileTypeDetector {

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("fileTypeDetector"));
        return null;
    }
    private FileTypeDetector(Void ignore) { }

    /**
     * Initializes a new instance of this class.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("fileTypeDetector")</tt>
     */
    protected FileTypeDetector() {
        this(checkPermission());
    }

    /**
     * Probes the given file to guess its content type.
     *
     * <p> The means by which this method determines the file type is highly
     * implementation specific. It may simply examine the file name, it may use
     * a file <a href="../attribute/package-summary.html">attribute</a>,
     * or it may examines bytes in the file.
     *
     * <p> The probe result is the string form of the value of a
     * Multipurpose Internet Mail Extension (MIME) content type as
     * defined by <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045:
     * Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet
     * Message Bodies</i></a>. The string must be parsable according to the
     * grammar in the RFC 2045.
     *
     * @param   path
     *          the path to the file to probe
     *
     * @return  The content type or {@code null} if the file type is not
     *          recognized
     *
     * @throws  IOException
     *          An I/O error occurs
     * @throws  SecurityException
     *          If the implementation requires to access the file, and a
     *          security manager is installed, and it denies an unspecified
     *          permission required by a file system provider implementation.
     *          If the file reference is associated with the default file system
     *          provider then the {@link SecurityManager#checkRead(String)} method
     *          is invoked to check read access to the file.
     *
     * @see java.nio.file.Files#probeContentType
     * 探测给定文件以猜测其内容类型。
    此方法确定文件类型的方法是高度特定于实现的。它可以检查文件名，可以使用file属性，也可以检查文件中的字节。
    探测结果是RFC 2045: Multipurpose Internet Mail Extensions (MIME)第一部分:Internet消息体的格式。根据RFC 2045中的语法，字符串必须是可解析的。
     */
    public abstract String probeContentType(Path path)
        throws IOException;
}
