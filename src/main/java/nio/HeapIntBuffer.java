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

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;


/**

 * A read/write HeapIntBuffer.






 */

class HeapIntBuffer
    extends IntBuffer
{

    // For speed these fields are actually declared in X-Buffer; 对于速度，这些字段实际上是在X-Buffer中声明的;
    // these declarations are here as documentation 这些声明在这里作为文档
    /*

    protected final int[] hb;
    protected final int offset;

    */

    HeapIntBuffer(int cap, int lim) {            // package-private

        super(-1, 0, lim, cap, new int[cap], 0);
        /*
        hb = new int[cap];
        offset = 0;
        */




    }

    HeapIntBuffer(int[] buf, int off, int len) { // package-private

        super(-1, off, off + len, buf.length, buf, 0);
        /*
        hb = buf;
        offset = 0;
        */




    }

    protected HeapIntBuffer(int[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {

        super(mark, pos, lim, cap, buf, off);
        /*
        hb = buf;
        offset = off;
        */




    }

//    基于剩下的容量构建一个新的缓冲区
    public IntBuffer slice() {
        return new HeapIntBuffer(hb,
                                        -1,
                                        0,
                                        this.remaining(),
                                        this.remaining(),
                                        this.position() + offset);
    }

//    构建一个一样的缓冲区
    public IntBuffer duplicate() {
        return new HeapIntBuffer(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

//    构建一个一样的只读缓冲区
    public IntBuffer asReadOnlyBuffer() {

        return new HeapIntBufferR(hb,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     offset);



    }



    protected int ix(int i) {
        return i + offset;
    }

    public int get() {
        return hb[ix(nextGetIndex())];
    }

    public int get(int i) {
        return hb[ix(checkIndex(i))];
    }







    public IntBuffer get(int[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        System.arraycopy(hb, ix(position()), dst, offset, length);
        position(position() + length);
        return this;
    }

    public boolean isDirect() {
        return false;
    }



    public boolean isReadOnly() {
        return false;
    }

    public IntBuffer put(int x) {

        hb[ix(nextPutIndex())] = x;
        return this;



    }

    public IntBuffer put(int i, int x) {

        hb[ix(checkIndex(i))] = x;
        return this;



    }

    public IntBuffer put(int[] src, int offset, int length) {

        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        System.arraycopy(src, offset, hb, ix(position()), length);
        position(position() + length);
        return this;



    }

    public IntBuffer put(IntBuffer src) {

        if (src instanceof HeapIntBuffer) {
            if (src == this)
                throw new IllegalArgumentException();
            HeapIntBuffer sb = (HeapIntBuffer)src;
            int n = sb.remaining();
            if (n > remaining())
                throw new BufferOverflowException();
            System.arraycopy(sb.hb, sb.ix(sb.position()),
                             hb, ix(position()), n);
            sb.position(sb.position() + n);
            position(position() + n);
        } else if (src.isDirect()) {
            int n = src.remaining();
            if (n > remaining())
                throw new BufferOverflowException();
            src.get(hb, ix(position()), n);
            position(position() + n);
        } else {
            super.put(src);
        }
        return this;



    }

//    一般用来压缩操作
    public IntBuffer compact() {

//        把position好elimit之间的元素复制到buffer起始位置
        System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
//        position = limit - position
        position(remaining());
//        limit = capacity
        limit(capacity());
        discardMark();
        return this;



    }






































































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
