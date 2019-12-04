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

package java.nio.file;

import java.util.Iterator;
import java.io.Closeable;
import java.io.IOException;

/**
 * An object to iterate over the entries in a directory. A directory stream
 * allows for the convenient use of the for-each construct to iterate over a
 * directory.
 *
 * <p> <b> While {@code DirectoryStream} extends {@code Iterable}, it is not a
 * general-purpose {@code Iterable} as it supports only a single {@code
 * Iterator}; invoking the {@link #iterator iterator} method to obtain a second
 * or subsequent iterator throws {@code IllegalStateException}. </b>
 *
 * <p> An important property of the directory stream's {@code Iterator} is that
 * its {@link Iterator#hasNext() hasNext} method is guaranteed to read-ahead by
 * at least one element. If {@code hasNext} method returns {@code true}, and is
 * followed by a call to the {@code next} method, it is guaranteed that the
 * {@code next} method will not throw an exception due to an I/O error, or
 * because the stream has been {@link #close closed}. The {@code Iterator} does
 * not support the {@link Iterator#remove remove} operation.
 *
 * <p> A {@code DirectoryStream} is opened upon creation and is closed by
 * invoking the {@code close} method. Closing a directory stream releases any
 * resources associated with the stream. Failure to close the stream may result
 * in a resource leak. The try-with-resources statement provides a useful
 * construct to ensure that the stream is closed:
 * <pre>
 *   Path dir = ...
 *   try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir)) {
 *       for (Path entry: stream) {
 *           ...
 *       }
 *   }
 * </pre>
 *
 * <p> Once a directory stream is closed, then further access to the directory,
 * using the {@code Iterator}, behaves as if the end of stream has been reached.
 * Due to read-ahead, the {@code Iterator} may return one or more elements
 * after the directory stream has been closed. Once these buffered elements
 * have been read, then subsequent calls to the {@code hasNext} method returns
 * {@code false}, and subsequent calls to the {@code next} method will throw
 * {@code NoSuchElementException}.
 *
 * <p> A directory stream is not required to be <i>asynchronously closeable</i>.
 * If a thread is blocked on the directory stream's iterator reading from the
 * directory, and another thread invokes the {@code close} method, then the
 * second thread may block until the read operation is complete.
 *
 * <p> If an I/O error is encountered when accessing the directory then it
 * causes the {@code Iterator}'s {@code hasNext} or {@code next} methods to
 * throw {@link DirectoryIteratorException} with the {@link IOException} as the
 * cause. As stated above, the {@code hasNext} method is guaranteed to
 * read-ahead by at least one element. This means that if {@code hasNext} method
 * returns {@code true}, and is followed by a call to the {@code next} method,
 * then it is guaranteed that the {@code next} method will not fail with a
 * {@code DirectoryIteratorException}.
 *
 * <p> The elements returned by the iterator are in no specific order. Some file
 * systems maintain special links to the directory itself and the directory's
 * parent directory. Entries representing these links are not returned by the
 * iterator.
 *
 * <p> The iterator is <i>weakly consistent</i>. It is thread safe but does not
 * freeze the directory while iterating, so it may (or may not) reflect updates
 * to the directory that occur after the {@code DirectoryStream} is created.
 *
 * <p> <b>Usage Examples:</b>
 * Suppose we want a list of the source files in a directory. This example uses
 * both the for-each and try-with-resources constructs.
 * <pre>
 *   List&lt;Path&gt; listSourceFiles(Path dir) throws IOException {
 *       List&lt;Path&gt; result = new ArrayList&lt;&gt;();
 *       try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir, "*.{c,h,cpp,hpp,java}")) {
 *           for (Path entry: stream) {
 *               result.add(entry);
 *           }
 *       } catch (DirectoryIteratorException ex) {
 *           // I/O error encounted during the iteration, the cause is an IOException
 *           throw ex.getCause();
 *       }
 *       return result;
 *   }
 * </pre>
 * @param   <T>     The type of element returned by the iterator
 *
 * @since 1.7
 *
 * @see Files#newDirectoryStream(Path)
 * 在目录中遍历条目的对象。目录流允许方便地使用for-each结构在目录上迭代。
虽然DirectoryStream扩展了Iterable，但它不是通用的Iterable，因为它只支持一个迭代器;调用迭代器方法获得第二个或后续迭代器抛出IllegalStateException。
目录流的迭代器的一个重要属性是，它的hasNext方法保证至少有一个元素可以提前读取。如果hasNext方法返回true，并随后调用下一个方法，则保证下一个方法不会由于I/O错误或流已经关闭而抛出异常。迭代器不支持删除操作。
创建时将打开DirectoryStream，并通过调用close方法关闭它。关闭目录流将释放与流相关的任何资源。未能关闭流可能导致资源泄漏。try-with-resources语句提供了一个有用的构造，以确保流是关闭的:
一旦关闭了目录流，然后使用迭代器进一步访问该目录，其行为就好像到达了流的末尾。由于提前读，迭代器可能在关闭目录流之后返回一个或多个元素。读取这些缓冲元素之后，对hasNext方法的后续调用将返回false，对next方法的后续调用将抛出NoSuchElementException。
不需要异步关闭目录流。如果目录流的迭代器从目录中读取一个线程被阻塞，而另一个线程调用close方法，那么第二个线程可能会阻塞，直到读取操作完成。
如果在访问目录时遇到I/O错误，则会导致迭代器的hasNext或next方法抛出DirectoryIteratorException，并将IOException作为原因。如上所述，hasNext方法保证至少有一个元素进行提前读取。这意味着如果hasNext方法返回true，并随后调用下一个方法，那么可以保证下一个方法不会在DirectoryIteratorException中失败。
迭代器返回的元素没有特定的顺序。有些文件系统维护到目录本身和目录的父目录的特殊链接。表示这些链接的条目不会被迭代器返回。
迭代器是弱一致的。它是线程安全的，但在迭代时不会冻结目录，因此它可能(也可能不会)反映在创建DirectoryStream之后发生的目录的更新。
 */

public interface DirectoryStream<T>
    extends Closeable, Iterable<T> {
    /**
     * An interface that is implemented by objects that decide if a directory
     * entry should be accepted or filtered. A {@code Filter} is passed as the
     * parameter to the {@link Files#newDirectoryStream(Path,DirectoryStream.Filter)}
     * method when opening a directory to iterate over the entries in the
     * directory.
     *
     * @param   <T>     the type of the directory entry
     *
     * @since 1.7
     * 由对象实现的接口，该对象决定是否应该接受或过滤目录项。过滤器作为参数传递给文件。newDirectoryStream(Path, DirectoryStream.Filter)方法，在打开一个目录时遍历目录中的条目。
     */
    @FunctionalInterface
    public static interface Filter<T> {
        /**
         * Decides if the given directory entry should be accepted or filtered.
         *
         * @param   entry
         *          the directory entry to be tested
         *
         * @return  {@code true} if the directory entry should be accepted
         *
         * @throws  IOException
         *          If an I/O error occurs
         *          决定是否应该接受或过滤给定的目录项。
         */
        boolean accept(T entry) throws IOException;
    }

    /**
     * Returns the iterator associated with this {@code DirectoryStream}.返回与此DirectoryStream关联的迭代器。
     *
     * @return  the iterator associated with this {@code DirectoryStream}
     *
     * @throws  IllegalStateException
     *          if this directory stream is closed or the iterator has already
     *          been returned
     */
    @Override
    Iterator<T> iterator();
}
