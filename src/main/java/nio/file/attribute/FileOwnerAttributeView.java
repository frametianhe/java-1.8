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

package java.nio.file.attribute;

import java.io.IOException;

/**
 * A file attribute view that supports reading or updating the owner of a file.
 * This file attribute view is intended for file system implementations that
 * support a file attribute that represents an identity that is the owner of
 * the file. Often the owner of a file is the identity of the entity that
 * created the file.
 *
 * <p> The {@link #getOwner getOwner} or {@link #setOwner setOwner} methods may
 * be used to read or update the owner of the file.
 *
 * <p> The {@link java.nio.file.Files#getAttribute getAttribute} and
 * {@link java.nio.file.Files#setAttribute setAttribute} methods may also be
 * used to read or update the owner. In that case, the owner attribute is
 * identified by the name {@code "owner"}, and the value of the attribute is
 * a {@link UserPrincipal}.
 *
 * @since 1.7
 * 支持读取或更新文件所有者的文件属性视图。此文件属性视图用于支持文件属性的文件系统实现，该文件属性表示文件所有者的标识。文件的所有者通常是创建文件的实体的标识。
可以使用getOwner或setOwner方法读取或更新文件的所有者。
getAttribute和setAttribute方法也可以用于读取或更新所有者。在这种情况下，owner属性由name“owner”标识，属性的值是UserPrincipal。
 */

public interface FileOwnerAttributeView
    extends FileAttributeView
{
    /**
     * Returns the name of the attribute view. Attribute views of this type
     * have the name {@code "owner"}.
     * 返回属性视图的名称。此类型的属性视图的名称为“owner”。
     */
    @Override
    String name();

    /**
     * Read the file owner.
     *
     * <p> It it implementation specific if the file owner can be a {@link
     * GroupPrincipal group}.
     *
     * @return  the file owner
     *
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, and it denies {@link
     *          RuntimePermission}<tt>("accessUserInformation")</tt> or its
     *          {@link SecurityManager#checkRead(String) checkRead} method
     *          denies read access to the file.
     *          读取文件所有者。
    如果文件所有者可以是一个组，它将实现特定的实现。
     */
    UserPrincipal getOwner() throws IOException;

    /**
     * Updates the file owner.
     *
     * <p> It it implementation specific if the file owner can be a {@link
     * GroupPrincipal group}. To ensure consistent and correct behavior
     * across platforms it is recommended that this method should only be used
     * to set the file owner to a user principal that is not a group.
     *
     * @param   owner
     *          the new file owner
     *
     * @throws  IOException
     *          if an I/O error occurs, or the {@code owner} parameter is a
     *          group and this implementation does not support setting the owner
     *          to a group
     * @throws  SecurityException
     *          In the case of the default provider, a security manager is
     *          installed, and it denies {@link
     *          RuntimePermission}<tt>("accessUserInformation")</tt> or its
     *          {@link SecurityManager#checkWrite(String) checkWrite} method
     *          denies write access to the file.
     *          更新文件所有者。
    如果文件所有者可以是一个组，它将实现特定的实现。为了确保跨平台的一致和正确的行为，建议只使用此方法将文件所有者设置为不属于组的用户主体。
     */
    void setOwner(UserPrincipal owner) throws IOException;
}
