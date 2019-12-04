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


/**
 * A typesafe enumeration for coding-error actions.
 *
 * <p> Instances of this class are used to specify how malformed-input and
 * unmappable-character errors are to be handled by charset <a
 * href="CharsetDecoder.html#cae">decoders</a> and <a
 * href="CharsetEncoder.html#cae">encoders</a>.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 * 用于编码错误操作的类型安全枚举。
该类的实例用于指定如何由charset解码器和编码器处理malformed-input和unmappable-character错误。
 */

public class CodingErrorAction {

    private String name;

    private CodingErrorAction(String name) {
        this.name = name;
    }

    /**
     * Action indicating that a coding error is to be handled by dropping the
     * erroneous input and resuming the coding operation.指示错误输入和恢复编码操作以处理编码错误的动作。
     */
    public static final CodingErrorAction IGNORE
        = new CodingErrorAction("IGNORE");

    /**
     * Action indicating that a coding error is to be handled by dropping the
     * erroneous input, appending the coder's replacement value to the output
     * buffer, and resuming the coding operation.指示编码错误的操作是通过删除错误输入、将编码者的替换值附加到输出缓冲区并恢复编码操作来处理。
     */
    public static final CodingErrorAction REPLACE
        = new CodingErrorAction("REPLACE");

    /**
     * Action indicating that a coding error is to be reported, either by
     * returning a {@link CoderResult} object or by throwing a {@link
     * CharacterCodingException}, whichever is appropriate for the method
     * implementing the coding process.指示要报告编码错误的操作，可以返回一个CoderResult对象，也可以抛出一个CharacterCodingException，以适合实现编码过程的方法。
     */
    public static final CodingErrorAction REPORT
        = new CodingErrorAction("REPORT");

    /**
     * Returns a string describing this action.
     *
     * @return  A descriptive string
     */
    public String toString() {
        return name;
    }

}
