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

import java.io.IOException;

/**
 * An object to lookup user and group principals by name. A {@link UserPrincipal}
 * represents an identity that may be used to determine access rights to objects
 * in a file system. A {@link GroupPrincipal} represents a <em>group identity</em>.
 * A {@code UserPrincipalLookupService} defines methods to lookup identities by
 * name or group name (which are typically user or account names). Whether names
 * and group names are case sensitive or not depends on the implementation.
 * The exact definition of a group is implementation specific but typically a
 * group represents an identity created for administrative purposes so as to
 * determine the access rights for the members of the group. In particular it is
 * implementation specific if the <em>namespace</em> for names and groups is the
 * same or is distinct. To ensure consistent and correct behavior across
 * platforms it is recommended that this API be used as if the namespaces are
 * distinct. In other words, the {@link #lookupPrincipalByName
 * lookupPrincipalByName} should be used to lookup users, and {@link
 * #lookupPrincipalByGroupName lookupPrincipalByGroupName} should be used to
 * lookup groups.
 *
 * @since 1.7
 *
 * @see java.nio.file.FileSystem#getUserPrincipalLookupService
 * 按名称查找用户和组主体的对象。UserPrincipal表示可以用于确定文件系统中的对象访问权限的标识。GroupPrincipal表示一个组标识。UserPrincipalLookupService定义按名称或组名(通常是用户或帐户名)查找标识的方法。名称和组名称是否区分大小写取决于实现。组的确切定义是特定于实现的，但通常情况下，组代表为管理目的创建的标识，以便确定组的成员的访问权限。特别是当名称和组的名称空间相同或不同时，它是特定于实现的。为了确保跨平台的一致和正确的行为，建议将该API当作名称空间不同使用。换句话说，lookupPrincipalByName应该用于查找用户，lookupPrincipalByGroupName应该用于查找组。
 */

public abstract class UserPrincipalLookupService {

    /**
     * Initializes a new instance of this class.
     */
    protected UserPrincipalLookupService() {
    }

    /**
     * Lookup a user principal by name.
     *
     * @param   name
     *          the string representation of the user principal to lookup
     *
     * @return  a user principal
     *
     * @throws  UserPrincipalNotFoundException
     *          the principal does not exist
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, it checks {@link RuntimePermission}<tt>("lookupUserInformation")</tt>
     */
    public abstract UserPrincipal lookupPrincipalByName(String name)
        throws IOException;

    /**
     * Lookup a group principal by group name.
     *
     * <p> Where an implementation does not support any notion of group then
     * this method always throws {@link UserPrincipalNotFoundException}. Where
     * the namespace for user accounts and groups is the same, then this method
     * is identical to invoking {@link #lookupPrincipalByName
     * lookupPrincipalByName}.
     *
     * @param   group
     *          the string representation of the group to lookup
     *
     * @return  a group principal
     *
     * @throws  UserPrincipalNotFoundException
     *          the principal does not exist or is not a group
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, it checks {@link RuntimePermission}<tt>("lookupUserInformation")</tt>
     *          按组名查找组主体。
    如果一个实现不支持任何组的概念，那么这个方法总是抛出UserPrincipalNotFoundException。如果用户帐户和组的名称空间相同，则此方法与调用lookupPrincipalByName相同。
     */
    public abstract GroupPrincipal lookupPrincipalByGroupName(String group)
        throws IOException;
}
