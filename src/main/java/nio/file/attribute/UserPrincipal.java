/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.security.Principal;

/**
 * A {@code Principal} representing an identity used to determine access rights
 * to objects in a file system.
 *
 * <p> On many platforms and file systems an entity requires appropriate access
 * rights or permissions in order to access objects in a file system. The
 * access rights are generally performed by checking the identity of the entity.
 * For example, on implementations that use Access Control Lists (ACLs) to
 * enforce privilege separation then a file in the file system may have an
 * associated ACL that determines the access rights of identities specified in
 * the ACL.
 *
 * <p> A {@code UserPrincipal} object is an abstract representation of an
 * identity. It has a {@link #getName() name} that is typically the username or
 * account name that it represents. User principal objects may be obtained using
 * a {@link UserPrincipalLookupService}, or returned by {@link
 * FileAttributeView} implementations that provide access to identity related
 * attributes. For example, the {@link AclFileAttributeView} and {@link
 * PosixFileAttributeView} provide access to a file's {@link
 * PosixFileAttributes#owner owner}.
 * 表示用于确定文件系统中对象访问权限的标识的主体。
 在许多平台和文件系统中，实体需要适当的访问权限才能访问文件系统中的对象。访问权限通常通过检查实体的标识来执行。例如，在使用访问控制列表(ACL)强制特权分离的实现上，文件系统中的文件可能具有一个关联的ACL，该ACL确定ACL中指定的身份的访问权限。
 UserPrincipal对象是标识的抽象表示。它的名称通常是它所代表的用户名或帐户名称。用户主体对象可以使用UserPrincipalLookupService获得，也可以通过提供对标识相关属性访问的FileAttributeView实现返回。例如，AclFileAttributeView和PosixFileAttributeView提供对文件所有者的访问。
 *
 * @since 1.7
 */

public interface UserPrincipal extends Principal { }
