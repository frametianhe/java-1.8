/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.*;
import java.util.Map;
import java.util.HashMap;


/**
 * A description of the result state of a coder.
 *
 * <p> A charset coder, that is, either a decoder or an encoder, consumes bytes
 * (or characters) from an input buffer, translates them, and writes the
 * resulting characters (or bytes) to an output buffer.  A coding process
 * terminates for one of four categories of reasons, which are described by
 * instances of this class:
 *
 * <ul>
 *
 *   <li><p> <i>Underflow</i> is reported when there is no more input to be
 *   processed, or there is insufficient input and additional input is
 *   required.  This condition is represented by the unique result object
 *   {@link #UNDERFLOW}, whose {@link #isUnderflow() isUnderflow} method
 *   returns <tt>true</tt>.  </p></li>
 *
 *   <li><p> <i>Overflow</i> is reported when there is insufficient room
 *   remaining in the output buffer.  This condition is represented by the
 *   unique result object {@link #OVERFLOW}, whose {@link #isOverflow()
 *   isOverflow} method returns <tt>true</tt>.  </p></li>
 *
 *   <li><p> A <i>malformed-input error</i> is reported when a sequence of
 *   input units is not well-formed.  Such errors are described by instances of
 *   this class whose {@link #isMalformed() isMalformed} method returns
 *   <tt>true</tt> and whose {@link #length() length} method returns the length
 *   of the malformed sequence.  There is one unique instance of this class for
 *   all malformed-input errors of a given length.  </p></li>
 *
 *   <li><p> An <i>unmappable-character error</i> is reported when a sequence
 *   of input units denotes a character that cannot be represented in the
 *   output charset.  Such errors are described by instances of this class
 *   whose {@link #isUnmappable() isUnmappable} method returns <tt>true</tt> and
 *   whose {@link #length() length} method returns the length of the input
 *   sequence denoting the unmappable character.  There is one unique instance
 *   of this class for all unmappable-character errors of a given length.
 *   </p></li>
 *
 * </ul>
 *
 * <p> For convenience, the {@link #isError() isError} method returns <tt>true</tt>
 * for result objects that describe malformed-input and unmappable-character
 * errors but <tt>false</tt> for those that describe underflow or overflow
 * conditions.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
对编码器的结果状态的描述。
一个charset编码器，即解码器或编码器，从输入缓冲区中消耗字节(或字符)，转换它们，并将生成的字符(或字节)写入输出缓冲区。一个编码过程终止了四类原因之一，这类原因由这个类的实例描述:
当没有更多的输入需要处理，或者输入不足和需要额外的输入时，会报告欠流。该条件由唯一的结果对象下流表示，其isUnderflow方法返回true。
当输出缓冲区中剩余空间不足时报告溢出。此条件由惟一的result对象OVERFLOW表示，其isOverflow方法返回true。
当输入单元的序列不是很好的时候，就会出现一个malformed输入错误。此类错误由该类的实例描述，该类的isMalformed方法返回true，而length方法返回畸形序列的长度。对于给定长度的所有格式输入错误，这个类有一个惟一的实例。
当输入单元序列表示不能在输出字符集中表示的字符时，将报告一个不可映射字符错误。此类错误由该类的实例描述，该类的isUnmappable方法返回true，而length方法返回表示不可mappable字符的输入序列的长度。对于给定长度的所有不可映射字符错误，这个类有一个惟一的实例。
为了方便起见，isError方法对于描述malford -input和unmable -character错误的结果对象返回true，但对于描述欠流或溢出条件的结果对象返回false。
 */

public class CoderResult {

    private static final int CR_UNDERFLOW  = 0;
    private static final int CR_OVERFLOW   = 1;
    private static final int CR_ERROR_MIN  = 2;
    private static final int CR_MALFORMED  = 2;
    private static final int CR_UNMAPPABLE = 3;

    private static final String[] names
        = { "UNDERFLOW", "OVERFLOW", "MALFORMED", "UNMAPPABLE" };

    private final int type;
    private final int length;

    private CoderResult(int type, int length) {
        this.type = type;
        this.length = length;
    }

    /**
     * Returns a string describing this coder result.
     *
     * @return  A descriptive string
     */
    public String toString() {
        String nm = names[type];
        return isError() ? nm + "[" + length + "]" : nm;
    }

    /**
     * Tells whether or not this object describes an underflow condition.告诉此对象是否描述了一个欠流条件。
     *
     * @return  <tt>true</tt> if, and only if, this object denotes underflow
     */
    public boolean isUnderflow() {
        return (type == CR_UNDERFLOW);
    }

    /**
     * Tells whether or not this object describes an overflow condition.告诉此对象是否描述了溢出条件。
     *
     * @return  <tt>true</tt> if, and only if, this object denotes overflow
     */
    public boolean isOverflow() {
        return (type == CR_OVERFLOW);
    }

    /**
     * Tells whether or not this object describes an error condition.说明该对象是否描述了错误条件。
     *
     * @return  <tt>true</tt> if, and only if, this object denotes either a
     *          malformed-input error or an unmappable-character error
     */
    public boolean isError() {
        return (type >= CR_ERROR_MIN);
    }

    /**
     * Tells whether or not this object describes a malformed-input error.告诉此对象是否描述了malford -input错误。
     *
     * @return  <tt>true</tt> if, and only if, this object denotes a
     *          malformed-input error
     */
    public boolean isMalformed() {
        return (type == CR_MALFORMED);
    }

    /**
     * Tells whether or not this object describes an unmappable-character
     * error.告诉此对象是否描述了不可映射字符错误。
     *
     * @return  <tt>true</tt> if, and only if, this object denotes an
     *          unmappable-character error
     */
    public boolean isUnmappable() {
        return (type == CR_UNMAPPABLE);
    }

    /**
     * Returns the length of the erroneous input described by this
     * object&nbsp;&nbsp;<i>(optional operation)</i>.返回该对象描述的错误输入的长度(可选操作)。
     *
     * @return  The length of the erroneous input, a positive integer
     *
     * @throws  UnsupportedOperationException
     *          If this object does not describe an error condition, that is,
     *          if the {@link #isError() isError} does not return <tt>true</tt>
     */
    public int length() {
        if (!isError())
            throw new UnsupportedOperationException();
        return length;
    }

    /**
     * Result object indicating underflow, meaning that either the input buffer
     * has been completely consumed or, if the input buffer is not yet empty,
     * that additional input is required.
     * 结果对象指示欠流，这意味着要么输入缓冲区已被完全消耗，要么，如果输入缓冲区尚未为空，则需要额外的输入。
     */
    public static final CoderResult UNDERFLOW
        = new CoderResult(CR_UNDERFLOW, 0);

    /**
     * Result object indicating overflow, meaning that there is insufficient
     * room in the output buffer.结果对象指示溢出，意味着输出缓冲区中没有足够的空间。
     */
    public static final CoderResult OVERFLOW
        = new CoderResult(CR_OVERFLOW, 0);

    private static abstract class Cache {

        private Map<Integer,WeakReference<CoderResult>> cache = null;

        protected abstract CoderResult create(int len);

        private synchronized CoderResult get(int len) {
            if (len <= 0)
                throw new IllegalArgumentException("Non-positive length");
            Integer k = new Integer(len);
            WeakReference<CoderResult> w;
            CoderResult e = null;
            if (cache == null) {
                cache = new HashMap<Integer,WeakReference<CoderResult>>();
            } else if ((w = cache.get(k)) != null) {
                e = w.get();
            }
            if (e == null) {
                e = create(len);
                cache.put(k, new WeakReference<CoderResult>(e));
            }
            return e;
        }

    }

    private static Cache malformedCache
        = new Cache() {
                public CoderResult create(int len) {
                    return new CoderResult(CR_MALFORMED, len);
                }};

    /**
     * Static factory method that returns the unique object describing a
     * malformed-input error of the given length.静态工厂方法，返回描述给定长度的malford -input错误的唯一对象。
     *
     * @param   length
     *          The given length
     *
     * @return  The requested coder-result object
     */
    public static CoderResult malformedForLength(int length) {
        return malformedCache.get(length);
    }

    private static Cache unmappableCache
        = new Cache() {
                public CoderResult create(int len) {
                    return new CoderResult(CR_UNMAPPABLE, len);
                }};

    /**
     * Static factory method that returns the unique result object describing
     * an unmappable-character error of the given length.静态工厂方法，返回描述给定长度的不可映射字符错误的唯一结果对象。
     *
     * @param   length
     *          The given length
     *
     * @return  The requested coder-result object
     */
    public static CoderResult unmappableForLength(int length) {
        return unmappableCache.get(length);
    }

    /**
     * Throws an exception appropriate to the result described by this object.
     *
     * @throws  BufferUnderflowException
     *          If this object is {@link #UNDERFLOW}
     *
     * @throws  BufferOverflowException
     *          If this object is {@link #OVERFLOW}
     *
     * @throws  MalformedInputException
     *          If this object represents a malformed-input error; the
     *          exception's length value will be that of this object
     *
     * @throws  UnmappableCharacterException
     *          If this object represents an unmappable-character error; the
     *          exceptions length value will be that of this object
     */
    public void throwException()
        throws CharacterCodingException
    {
        switch (type) {
        case CR_UNDERFLOW:   throw new BufferUnderflowException();
        case CR_OVERFLOW:    throw new BufferOverflowException();
        case CR_MALFORMED:   throw new MalformedInputException(length);
        case CR_UNMAPPABLE:  throw new UnmappableCharacterException(length);
        default:
            assert false;
        }
    }

}
