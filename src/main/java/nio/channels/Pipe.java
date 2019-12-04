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

package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.*;


/**
 * A pair of channels that implements a unidirectional pipe.
 *
 * <p> A pipe consists of a pair of channels: A writable {@link
 * Pipe.SinkChannel sink} channel and a readable {@link Pipe.SourceChannel source}
 * channel.  Once some bytes are written to the sink channel they can be read
 * from source channel in exactlyAthe order in which they were written.
 *
 * <p> Whether or not a thread writing bytes to a pipe will block until another
 * thread reads those bytes, or some previously-written bytes, from the pipe is
 * system-dependent and therefore unspecified.  Many pipe implementations will
 * buffer up to a certain number of bytes between the sink and source channels,
 * but such buffering should not be assumed.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 实现单向管道的一对通道。
管道由一对通道组成:一个可写的接收通道和一个可读的源通道。一旦一些字节被写入接收器通道，它们就可以从源通道以它们被写入的顺序读取。
不管一个线程在管道上写入字节是否会阻塞，直到另一个线程读取这些字节，或者一些以前写过的字节，从管道中是系统依赖的，因此不指定。许多管道实现会在汇聚通道和源通道之间缓冲一定数量的字节，但是不应该假定这种缓冲。
 */

public abstract class Pipe {

    /**
     * A channel representing the readable end of a {@link Pipe}.表示管道可读端的通道。
     *
     * @since 1.4
     */
    public static abstract class SourceChannel
        extends AbstractSelectableChannel
        implements ReadableByteChannel, ScatteringByteChannel
    {
        /**
         * Constructs a new instance of this class.
         *
         * @param  provider
         *         The selector provider
         */
        protected SourceChannel(SelectorProvider provider) {
            super(provider);
        }

        /**
         * Returns an operation set identifying this channel's supported
         * operations.
         *
         * <p> Pipe-source channels only support reading, so this method
         * returns {@link SelectionKey#OP_READ}.  </p>
         *
         * @return  The valid-operation set
         * 返回确定该通道支持操作的操作集。
        管道源通道只支持读取，因此该方法返回SelectionKey.OP_READ。
         */
        public final int validOps() {
            return SelectionKey.OP_READ;
        }

    }

    /**
     * A channel representing the writable end of a {@link Pipe}.表示管道可写末端的通道。
     *
     * @since 1.4
     */
    public static abstract class SinkChannel
        extends AbstractSelectableChannel
        implements WritableByteChannel, GatheringByteChannel
    {
        /**
         * Initializes a new instance of this class.
         *
         * @param  provider
         *         The selector provider
         */
        protected SinkChannel(SelectorProvider provider) {
            super(provider);
        }

        /**
         * Returns an operation set identifying this channel's supported
         * operations.
         *
         * <p> Pipe-sink channels only support writing, so this method returns
         * {@link SelectionKey#OP_WRITE}.  </p>
         *
         * @return  The valid-operation set
         * 返回确定该通道支持操作的操作集。
        管道接收通道只支持写操作，因此该方法返回SelectionKey.OP_WRITE。
         */
        public final int validOps() {
            return SelectionKey.OP_WRITE;
        }

    }

    /**
     * Initializes a new instance of this class.
     */
    protected Pipe() { }

    /**
     * Returns this pipe's source channel.
     *
     * @return  This pipe's source channel
     */
    public abstract SourceChannel source();

    /**
     * Returns this pipe's sink channel.
     *
     * @return  This pipe's sink channel
     */
    public abstract SinkChannel sink();

    /**
     * Opens a pipe.
     *
     * <p> The new pipe is created by invoking the {@link
     * java.nio.channels.spi.SelectorProvider#openPipe openPipe} method of the
     * system-wide default {@link java.nio.channels.spi.SelectorProvider}
     * object.  </p>
     *
     * @return  A new pipe
     *
     * @throws  IOException
     *          If an I/O error occurs
     *          打开一个管道。
    通过调用系统范围内默认SelectorProvider对象的openPipe方法创建新的管道。
     */
    public static Pipe open() throws IOException {
        return SelectorProvider.provider().openPipe();
    }

}
