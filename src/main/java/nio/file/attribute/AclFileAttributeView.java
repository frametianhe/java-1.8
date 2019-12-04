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
import java.util.List;
import java.io.IOException;

/**
 * A file attribute view that supports reading or updating a file's Access
 * Control Lists (ACL) or file owner attributes.
 *
 * <p> ACLs are used to specify access rights to file system objects. An ACL is
 * an ordered list of {@link AclEntry access-control-entries}, each specifying a
 * {@link UserPrincipal} and the level of access for that user principal. This
 * file attribute view defines the {@link #getAcl() getAcl}, and {@link
 * #setAcl(List) setAcl} methods to read and write ACLs based on the ACL
 * model specified in <a href="http://www.ietf.org/rfc/rfc3530.txt"><i>RFC&nbsp;3530:
 * Network File System (NFS) version 4 Protocol</i></a>. This file attribute view
 * is intended for file system implementations that support the NFSv4 ACL model
 * or have a <em>well-defined</em> mapping between the NFSv4 ACL model and the ACL
 * model used by the file system. The details of such mapping are implementation
 * dependent and are therefore unspecified.
 *
 * <p> This class also extends {@code FileOwnerAttributeView} so as to define
 * methods to get and set the file owner.
 *
 * <p> When a file system provides access to a set of {@link FileStore
 * file-systems} that are not homogeneous then only some of the file systems may
 * support ACLs. The {@link FileStore#supportsFileAttributeView
 * supportsFileAttributeView} method can be used to test if a file system
 * supports ACLs.
 *
 * <h2>Interoperability</h2>
 *
 * RFC&nbsp;3530 allows for special user identities to be used on platforms that
 * support the POSIX defined access permissions. The special user identities
 * are "{@code OWNER@}", "{@code GROUP@}", and "{@code EVERYONE@}". When both
 * the {@code AclFileAttributeView} and the {@link PosixFileAttributeView}
 * are supported then these special user identities may be included in ACL {@link
 * AclEntry entries} that are read or written. The file system's {@link
 * UserPrincipalLookupService} may be used to obtain a {@link UserPrincipal}
 * to represent these special identities by invoking the {@link
 * UserPrincipalLookupService#lookupPrincipalByName lookupPrincipalByName}
 * method.
 *
 * <p> <b>Usage Example:</b>
 * Suppose we wish to add an entry to an existing ACL to grant "joe" access:
 * <pre>
 *     // lookup "joe"
 *     UserPrincipal joe = file.getFileSystem().getUserPrincipalLookupService()
 *         .lookupPrincipalByName("joe");
 *
 *     // get view
 *     AclFileAttributeView view = Files.getFileAttributeView(file, AclFileAttributeView.class);
 *
 *     // create ACE to give "joe" read access
 *     AclEntry entry = AclEntry.newBuilder()
 *         .setType(AclEntryType.ALLOW)
 *         .setPrincipal(joe)
 *         .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES)
 *         .build();
 *
 *     // read ACL, insert ACE, re-write ACL
 *     List&lt;AclEntry&gt; acl = view.getAcl();
 *     acl.add(0, entry);   // insert before any DENY entries
 *     view.setAcl(acl);
 * </pre>
 *
 * <h2> Dynamic Access </h2>
 * <p> Where dynamic access to file attributes is required, the attributes
 * supported by this attribute view are as follows:
 * <blockquote>
 * <table border="1" cellpadding="8" summary="Supported attributes">
 *   <tr>
 *     <th> Name </th>
 *     <th> Type </th>
 *   </tr>
 *   <tr>
 *     <td> "acl" </td>
 *     <td> {@link List}&lt;{@link AclEntry}&gt; </td>
 *   </tr>
 *   <tr>
 *     <td> "owner" </td>
 *     <td> {@link UserPrincipal} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> The {@link Files#getAttribute getAttribute} method may be used to read
 * the ACL or owner attributes as if by invoking the {@link #getAcl getAcl} or
 * {@link #getOwner getOwner} methods.
 *
 * <p> The {@link Files#setAttribute setAttribute} method may be used to
 * update the ACL or owner attributes as if by invoking the {@link #setAcl setAcl}
 * or {@link #setOwner setOwner} methods.
 *
 * <h2> Setting the ACL when creating a file </h2>
 *
 * <p> Implementations supporting this attribute view may also support setting
 * the initial ACL when creating a file or directory. The initial ACL
 * may be provided to methods such as {@link Files#createFile createFile} or {@link
 * Files#createDirectory createDirectory} as an {@link FileAttribute} with {@link
 * FileAttribute#name name} {@code "acl:acl"} and a {@link FileAttribute#value
 * value} that is the list of {@code AclEntry} objects.
 *
 * <p> Where an implementation supports an ACL model that differs from the NFSv4
 * defined ACL model then setting the initial ACL when creating the file must
 * translate the ACL to the model supported by the file system. Methods that
 * create a file should reject (by throwing {@link IOException IOException})
 * any attempt to create a file that would be less secure as a result of the
 * translation.
 *
 * @since 1.7
 * 支持读取或更新文件的访问控制列表(ACL)或文件所有者属性的文件属性视图。
acl用于指定文件系统对象的访问权限。ACL是访问控制项的有序列表，每个条目指定一个用户主体和该用户主体的访问级别。这个文件属性视图定义了getAcl和setAcl方法，它们基于RFC 3530: Network file System (NFS) version 4协议中指定的ACL模型来读写ACL。这个文件属性视图用于支持NFSv4 ACL模型的文件系统实现，或者在NFSv4 ACL模型和文件系统使用的ACL模型之间具有定义良好的映射。这种映射的细节是依赖于实现的，因此是未指定的。
这个类还扩展了FileOwnerAttributeView，以便定义获取和设置文件所有者的方法。
当文件系统提供对一组非同构文件系统的访问时，那么只有一些文件系统可能支持acl。可以使用supportsFileAttributeView方法测试文件系统是否支持acl。
互操作性
RFC 3530允许在支持POSIX定义的访问权限的平台上使用特殊的用户标识。特殊的用户标识是“OWNER@”、“GROUP@”和“EVERYONE@”。当同时支持AclFileAttributeView和PosixFileAttributeView时，这些特殊的用户标识可以包含在读取或写入的ACL条目中。文件系统的UserPrincipalLookupService可以通过调用lookupPrincipalByName方法来获取用户主体来表示这些特殊标识。
动态访问
当需要动态访问文件属性时，此属性视图支持的属性如下:
getAttribute方法可以使用getAcl或getOwner方法来读取ACL或所有者属性。
setAttribute方法可以通过调用setAcl或setOwner方法来更新ACL或owner属性。
创建文件时设置ACL
支持此属性视图的实现还可以支持在创建文件或目录时设置初始ACL。初始ACL可以提供给createFile或createDirectory等方法，作为名为“ACL: ACL”的file属性，以及AclEntry对象列表的值。
如果实现支持与NFSv4定义的ACL模型不同的ACL模型，那么在创建文件时，必须将ACL转换为文件系统支持的模型。创建文件的方法应该拒绝(通过抛出IOException)创建由于转换而不那么安全的文件的任何尝试。
 */

public interface AclFileAttributeView
    extends FileOwnerAttributeView
{
    /**
     * Returns the name of the attribute view. Attribute views of this type
     * have the name {@code "acl"}.
     * 返回属性视图的名称。这种类型的属性视图的名称为“acl”。
     */
    @Override
    String name();

    /**
     * Reads the access control list.
     *
     * <p> When the file system uses an ACL model that differs from the NFSv4
     * defined ACL model, then this method returns an ACL that is the translation
     * of the ACL to the NFSv4 ACL model.
     *
     * <p> The returned list is modifiable so as to facilitate changes to the
     * existing ACL. The {@link #setAcl setAcl} method is used to update
     * the file's ACL attribute.
     *
     * @return  an ordered list of {@link AclEntry entries} representing the
     *          ACL
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, and it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkRead(String) checkRead} method
     *          denies read access to the file.
     *          读取访问控制列表。
    当文件系统使用与NFSv4定义的ACL模型不同的ACL模型时，该方法将返回一个ACL，该ACL将ACL转换为NFSv4 ACL模型。
    返回的列表可以修改，以方便对现有ACL的更改。setAcl方法用于更新文件的ACL属性。
     */
    List<AclEntry> getAcl() throws IOException;

    /**
     * Updates (replace) the access control list.
     *
     * <p> Where the file system supports Access Control Lists, and it uses an
     * ACL model that differs from the NFSv4 defined ACL model, then this method
     * must translate the ACL to the model supported by the file system. This
     * method should reject (by throwing {@link IOException IOException}) any
     * attempt to write an ACL that would appear to make the file more secure
     * than would be the case if the ACL were updated. Where an implementation
     * does not support a mapping of {@link AclEntryType#AUDIT} or {@link
     * AclEntryType#ALARM} entries, then this method ignores these entries when
     * writing the ACL.
     *
     * <p> If an ACL entry contains a {@link AclEntry#principal user-principal}
     * that is not associated with the same provider as this attribute view then
     * {@link ProviderMismatchException} is thrown. Additional validation, if
     * any, is implementation dependent.
     *
     * <p> If the file system supports other security related file attributes
     * (such as a file {@link PosixFileAttributes#permissions
     * access-permissions} for example), the updating the access control list
     * may also cause these security related attributes to be updated.
     *
     * @param   acl
     *          the new access control list
     *
     * @throws  IOException
     *          if an I/O error occurs or the ACL is invalid
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, it denies {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          or its {@link SecurityManager#checkWrite(String) checkWrite}
     *          method denies write access to the file.
     *          更新(替换)访问控制列表。
    在文件系统支持访问控制列表的地方，它使用与NFSv4定义的ACL模型不同的ACL模型，然后该方法必须将ACL转换为文件系统支持的模型。这个方法应该拒绝(通过抛出IOException)编写ACL的任何尝试，因为如果ACL被更新，那么这种尝试看起来会使文件更安全。实现不支持AclEntryType的映射。审计或AclEntryType。警告条目，然后该方法在编写ACL时忽略这些条目。
    如果ACL条目包含一个用户主体，而该用户主体与此属性视图没有关联，则会抛出ProviderMismatchException。如果有其他验证，则依赖于实现。
    如果文件系统支持其他与安全性相关的文件属性(例如文件访问权限)，则更新访问控制列表也可能导致更新这些与安全性相关的属性。
     */
    void setAcl(List<AclEntry> acl) throws IOException;
}
