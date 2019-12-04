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

/**
 * A {@code UserPrincipal} representing a <em>group identity</em>, used to
 * determine access rights to objects in a file system. The exact definition of
 * a group is implementation specific, but typically, it represents an identity
 * created for administrative purposes so as to determine the access rights for
 * the members of the group. Whether an entity can be a member of multiple
 * groups, and whether groups can be nested, are implementation specified and
 * therefore not specified.
 *
 * @since 1.7
 *
 * @see UserPrincipalLookupService#lookupPrincipalByGroupName
 * 表示组标识的用户主体，用于确定文件系统中对象的访问权限。组的确切定义是特定于实现的，但通常它表示为管理目的创建的标识，以便确定组的成员的访问权限。实体是否可以是多个组的成员，以及组是否可以嵌套，都是指定的实现，因此没有指定。
 */

public interface GroupPrincipal extends UserPrincipal { }
