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

package java.nio.file;

/**
 * An object that configures how to open or create a file.
 *
 * <p> Objects of this type are used by methods such as {@link
 * Files#newOutputStream(Path,OpenOption[]) newOutputStream}, {@link
 * Files#newByteChannel newByteChannel}, {@link
 * java.nio.channels.FileChannel#open FileChannel.open}, and {@link
 * java.nio.channels.AsynchronousFileChannel#open AsynchronousFileChannel.open}
 * when opening or creating a file.
 *
 * <p> The {@link StandardOpenOption} enumeration type defines the
 * <i>standard</i> options.
 *
 * @since 1.7
 * 配置如何打开或创建文件的对象。
这种类型的对象被诸如newOutputStream、newByteChannel、FileChannel等方法使用。开放,AsynchronousFileChannel。打开或创建文件时打开。
StandardOpenOption枚举类型定义了标准选项。
 */

public interface OpenOption {
}
