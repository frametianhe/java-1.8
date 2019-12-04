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

package java.nio.file.attribute;

import java.nio.file.*;
import java.util.Set;
import java.io.IOException;

/**
 * A file attribute view that provides a view of the file attributes commonly
 * associated with files on file systems used by operating systems that implement
 * the Portable Operating System Interface (POSIX) family of standards.
 *
 * <p> Operating systems that implement the <a href="http://www.opengroup.org">
 * POSIX</a> family of standards commonly use file systems that have a
 * file <em>owner</em>, <em>group-owner</em>, and related <em>access
 * permissions</em>. This file attribute view provides read and write access
 * to these attributes.
 *
 * <p> The {@link #readAttributes() readAttributes} method is used to read the
 * file's attributes. The file {@link PosixFileAttributes#owner() owner} is
 * represented by a {@link UserPrincipal} that is the identity of the file owner
 * for the purposes of access control. The {@link PosixFileAttributes#group()
 * group-owner}, represented by a {@link GroupPrincipal}, is the identity of the
 * group owner, where a group is an identity created for administrative purposes
 * so as to determine the access rights for the members of the group.
 *
 * <p> The {@link PosixFileAttributes#permissions() permissions} attribute is a
 * set of access permissions. This file attribute view provides access to the nine
 * permission defined by the {@link PosixFilePermission} class.
 * These nine permission bits determine the <em>read</em>, <em>write</em>, and
 * <em>execute</em> access for the file owner, group, and others (others
 * meaning identities other than the owner and members of the group). Some
 * operating systems and file systems may provide additional permission bits
 * but access to these other bits is not defined by this class in this release.
 *
 * <p> <b>Usage Example:</b>
 * Suppose we need to print out the owner and access permissions of a file:
 * <pre>
 *     Path file = ...
 *     PosixFileAttributes attrs = Files.getFileAttributeView(file, PosixFileAttributeView.class)
 *         .readAttributes();
 *     System.out.format("%s %s%n",
 *         attrs.owner().getName(),
 *         PosixFilePermissions.toString(attrs.permissions()));
 * </pre>
 *
 * <h2> Dynamic Access </h2>
 * <p> Where dynamic access to file attributes is required, the attributes
 * supported by this attribute view are as defined by {@link
 * BasicFileAttributeView} and {@link FileOwnerAttributeView}, and in addition,
 * the following attributes are supported:
 * <blockquote>
 * <table border="1" cellpadding="8" summary="Supported attributes">
 *   <tr>
 *     <th> Name </th>
 *     <th> Type </th>
 *   </tr>
 *  <tr>
 *     <td> "permissions" </td>
 *     <td> {@link Set}&lt;{@link PosixFilePermission}&gt; </td>
 *   </tr>
 *   <tr>
 *     <td> "group" </td>
 *     <td> {@link GroupPrincipal} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> The {@link Files#getAttribute getAttribute} method may be used to read
 * any of these attributes, or any of the attributes defined by {@link
 * BasicFileAttributeView} as if by invoking the {@link #readAttributes
 * readAttributes()} method.
 *
 * <p> The {@link Files#setAttribute setAttribute} method may be used to update
 * the file's last modified time, last access time or create time attributes as
 * defined by {@link BasicFileAttributeView}. It may also be used to update
 * the permissions, owner, or group-owner as if by invoking the {@link
 * #setPermissions setPermissions}, {@link #setOwner setOwner}, and {@link
 * #setGroup setGroup} methods respectively.
 *
 * <h2> Setting Initial Permissions </h2>
 * <p> Implementations supporting this attribute view may also support setting
 * the initial permissions when creating a file or directory. The
 * initial permissions are provided to the {@link Files#createFile createFile}
 * or {@link Files#createDirectory createDirectory} methods as a {@link
 * FileAttribute} with {@link FileAttribute#name name} {@code "posix:permissions"}
 * and a {@link FileAttribute#value value} that is the set of permissions. The
 * following example uses the {@link PosixFilePermissions#asFileAttribute
 * asFileAttribute} method to construct a {@code FileAttribute} when creating a
 * file:
 *
 * <pre>
 *     Path path = ...
 *     Set&lt;PosixFilePermission&gt; perms =
 *         EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ);
 *     Files.createFile(path, PosixFilePermissions.asFileAttribute(perms));
 * </pre>
 *
 * <p> When the access permissions are set at file creation time then the actual
 * value of the permissions may differ that the value of the attribute object.
 * The reasons for this are implementation specific. On UNIX systems, for
 * example, a process has a <em>umask</em> that impacts the permission bits
 * of newly created files. Where an implementation supports the setting of
 * the access permissions, and the underlying file system supports access
 * permissions, then it is required that the value of the actual access
 * permissions will be equal or less than the value of the attribute
 * provided to the {@link Files#createFile createFile} or {@link
 * Files#createDirectory createDirectory} methods. In other words, the file may
 * be more secure than requested.
 *
 * @since 1.7
 * 一个文件属性视图，它提供了一个文件属性的视图，该视图通常与操作系统所使用的文件系统的文件相关联，这些文件系统实现了可移植操作系统接口(POSIX)系列标准。
实现POSIX系列标准的操作系统通常使用具有文件所有者、组所有者和相关访问权限的文件系统。这个文件属性视图提供对这些属性的读写访问。
readAttributes方法用于读取文件的属性。文件所有者由UserPrincipal表示，UserPrincipal是用于访问控制的文件所有者的标识。group-owner(由GroupPrincipal表示)是group owner的标识，其中group是为管理目的创建的标识，用于确定组的成员的访问权限。
权限属性是一组访问权限。这个文件属性视图提供了对PosixFilePermission类定义的9个权限的访问。这9个权限位决定了文件所有者、组和其他的读、写和执行访问(其他的意思是除了所有者和组成员之外的其他身份)。一些操作系统和文件系统可能提供额外的权限位，但是在这个版本中，这个类没有定义对这些其他位的访问。
 */

public interface PosixFileAttributeView
    extends BasicFileAttributeView, FileOwnerAttributeView
{
    /**
     * Returns the name of the attribute view. Attribute views of this type
     * have the name {@code "posix"}.返回属性视图的名称。这种类型的属性视图的名称为“posix”。
     */
    @Override
    String name();

    /**
     * @throws  IOException                {@inheritDoc}
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, and it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkRead(String) checkRead} method
     *          denies read access to the file.
     *          读取基本文件属性作为批量操作。
    如果所有文件属性都被作为与其他文件系统操作相关的原子操作读取，则它是特定于实现的。
     */
    @Override
    PosixFileAttributes readAttributes() throws IOException;

    /**
     * Updates the file permissions.
     *
     * @param   perms
     *          the new set of permissions
     *
     * @throws  ClassCastException
     *          if the sets contains elements that are not of type {@code
     *          PosixFilePermission}
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, and it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file.
     */
    void setPermissions(Set<PosixFilePermission> perms) throws IOException;

    /**
     * Updates the file group-owner.
     *
     * @param   group
     *          the new file group-owner
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file.
     */
    void setGroup(GroupPrincipal group) throws IOException;
}
