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

package java.nio.charset;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.lang.ref.WeakReference;
import java.nio.charset.CoderMalfunctionError;                  // javadoc
import java.util.Arrays;


/**
 * An engine that can transform a sequence of sixteen-bit Unicode characters into a sequence of
 * bytes in a specific charset.
 *
 * <a name="steps"></a>
 *
 * <p> The input character sequence is provided in a character buffer or a series
 * of such buffers.  The output byte sequence is written to a byte buffer
 * or a series of such buffers.  An encoder should always be used by making
 * the following sequence of method invocations, hereinafter referred to as an
 * <i>encoding operation</i>:
 *
 * <ol>
 *
 *   <li><p> Reset the encoder via the {@link #reset reset} method, unless it
 *   has not been used before; </p></li>
 *
 *   <li><p> Invoke the {@link #encode encode} method zero or more times, as
 *   long as additional input may be available, passing <tt>false</tt> for the
 *   <tt>endOfInput</tt> argument and filling the input buffer and flushing the
 *   output buffer between invocations; </p></li>
 *
 *   <li><p> Invoke the {@link #encode encode} method one final time, passing
 *   <tt>true</tt> for the <tt>endOfInput</tt> argument; and then </p></li>
 *
 *   <li><p> Invoke the {@link #flush flush} method so that the encoder can
 *   flush any internal state to the output buffer. </p></li>
 *
 * </ol>
 *
 * Each invocation of the {@link #encode encode} method will encode as many
 * characters as possible from the input buffer, writing the resulting bytes
 * to the output buffer.  The {@link #encode encode} method returns when more
 * input is required, when there is not enough room in the output buffer, or
 * when an encoding error has occurred.  In each case a {@link CoderResult}
 * object is returned to describe the reason for termination.  An invoker can
 * examine this object and fill the input buffer, flush the output buffer, or
 * attempt to recover from an encoding error, as appropriate, and try again.
 *
 * <a name="ce"></a>
 *
 * <p> There are two general types of encoding errors.  If the input character
 * sequence is not a legal sixteen-bit Unicode sequence then the input is considered <i>malformed</i>.  If
 * the input character sequence is legal but cannot be mapped to a valid
 * byte sequence in the given charset then an <i>unmappable character</i> has been encountered.
 *
 * <a name="cae"></a>
 *
 * <p> How an encoding error is handled depends upon the action requested for
 * that type of error, which is described by an instance of the {@link
 * CodingErrorAction} class.  The possible error actions are to {@linkplain
 * CodingErrorAction#IGNORE ignore} the erroneous input, {@linkplain
 * CodingErrorAction#REPORT report} the error to the invoker via
 * the returned {@link CoderResult} object, or {@linkplain CodingErrorAction#REPLACE
 * replace} the erroneous input with the current value of the
 * replacement byte array.  The replacement
 *

 * is initially set to the encoder's default replacement, which often
 * (but not always) has the initial value&nbsp;<tt>{</tt>&nbsp;<tt>(byte)'?'</tt>&nbsp;<tt>}</tt>;




 *
 * its value may be changed via the {@link #replaceWith(byte[])
 * replaceWith} method.
 *
 * <p> The default action for malformed-input and unmappable-character errors
 * is to {@linkplain CodingErrorAction#REPORT report} them.  The
 * malformed-input error action may be changed via the {@link
 * #onMalformedInput(CodingErrorAction) onMalformedInput} method; the
 * unmappable-character action may be changed via the {@link
 * #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter} method.
 *
 * <p> This class is designed to handle many of the details of the encoding
 * process, including the implementation of error actions.  An encoder for a
 * specific charset, which is a concrete subclass of this class, need only
 * implement the abstract {@link #encodeLoop encodeLoop} method, which
 * encapsulates the basic encoding loop.  A subclass that maintains internal
 * state should, additionally, override the {@link #implFlush implFlush} and
 * {@link #implReset implReset} methods.
 *
 * <p> Instances of this class are not safe for use by multiple concurrent
 * threads.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see ByteBuffer
 * @see CharBuffer
 * @see Charset
 * @see CharsetDecoder
 * 可以将16位Unicode字符序列转换为特定字符集中的字节序列的引擎。
输入字符序列在字符缓冲区或一系列此类缓冲区中提供。输出字节序列被写入一个字节缓冲区或一系列这样的缓冲区。编码器应始终使用以下方法调用序列，以下简称编码操作:
通过复位方法复位解码器，除非以前从未使用过;
只要有额外的输入可用，为endOfInput参数传递false，并在调用之间填充输入缓冲区并刷新输出缓冲区，就可以将解码方法调用零次或多次;
最后一次调用decode方法，为endOfInput参数传递true;然后
调用flush方法，以便解码器可以将任何内部状态刷新到输出缓冲区。
解码方法的每次调用将从输入缓冲区解码尽可能多的字节，并将结果字符写入输出缓冲区。解码方法在需要更多输入时返回，当输出缓冲区中没有足够的空间时，或者当解码错误发生时返回。在每种情况下，返回一个CoderResult对象来描述终止的原因。一个调用程序可以检查这个对象并填充输入缓冲区，刷新输出缓冲区，或者根据需要尝试从解码错误中恢复，然后再次尝试。
解码错误通常有两种类型。如果输入字节序列对这个字符集不合法，那么输入被认为是畸形的。如果输入字节序列是合法的，但不能映射到有效的Unicode字符，则会遇到不可映射字符。
如何处理解码错误取决于该类型错误所请求的操作，该操作由CodingErrorAction类的实例描述。可能的错误操作是忽略错误输入，通过返回的CoderResult对象向调用者报告错误，或者用替换字符串的当前值替换错误输入。更换有初始值“\uFFFD”;它的值可以通过replaceWith方法更改。
malford -input和unmappable-character错误的默认操作是报告它们。通过onMalformedInput方法可以更改malford -input错误操作;可以通过onUnmappableCharacter方法更改unmappable字符操作。
这个类用于处理解码过程的许多细节，包括错误操作的实现。一个特定字符集的解码器(它是这个类的一个具体子类)只需要实现抽象的decodeLoop方法，它封装了基本的解码循环。维护内部状态的子类还应该重写implFlush和implReset方法。
这个类的实例不安全，可以由多个并发线程使用。
 */

public abstract class CharsetEncoder {

    private final Charset charset;
    private final float averageBytesPerChar;
    private final float maxBytesPerChar;

    private byte[] replacement;
    private CodingErrorAction malformedInputAction
        = CodingErrorAction.REPORT;
    private CodingErrorAction unmappableCharacterAction
        = CodingErrorAction.REPORT;

    // Internal states
    //
    private static final int ST_RESET   = 0;
    private static final int ST_CODING  = 1;
    private static final int ST_END     = 2;
    private static final int ST_FLUSHED = 3;

    private int state = ST_RESET;

    private static String stateNames[]
        = { "RESET", "CODING", "CODING_END", "FLUSHED" };


    /**
     * Initializes a new encoder.  The new encoder will have the given
     * bytes-per-char and replacement values.
     *
     * @param  cs
     *         The charset that created this encoder
     *
     * @param  averageBytesPerChar
     *         A positive float value indicating the expected number of
     *         bytes that will be produced for each input character
     *
     * @param  maxBytesPerChar
     *         A positive float value indicating the maximum number of
     *         bytes that will be produced for each input character
     *
     * @param  replacement
     *         The initial replacement; must not be <tt>null</tt>, must have
     *         non-zero length, must not be longer than maxBytesPerChar,
     *         and must be {@linkplain #isLegalReplacement legal}
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          初始化一个新的编码器。新的编码器将具有给定的字节数和替换值。
     */
    protected
    CharsetEncoder(Charset cs,
                   float averageBytesPerChar,
                   float maxBytesPerChar,
                   byte[] replacement)
    {
        this.charset = cs;
        if (averageBytesPerChar <= 0.0f)
            throw new IllegalArgumentException("Non-positive "
                                               + "averageBytesPerChar");
        if (maxBytesPerChar <= 0.0f)
            throw new IllegalArgumentException("Non-positive "
                                               + "maxBytesPerChar");
        if (!Charset.atBugLevel("1.4")) {
            if (averageBytesPerChar > maxBytesPerChar)
                throw new IllegalArgumentException("averageBytesPerChar"
                                                   + " exceeds "
                                                   + "maxBytesPerChar");
        }
        this.replacement = replacement;
        this.averageBytesPerChar = averageBytesPerChar;
        this.maxBytesPerChar = maxBytesPerChar;
        replaceWith(replacement);
    }

    /**
     * Initializes a new encoder.  The new encoder will have the given
     * bytes-per-char values and its replacement will be the
     * byte array <tt>{</tt>&nbsp;<tt>(byte)'?'</tt>&nbsp;<tt>}</tt>.
     *
     * @param  cs
     *         The charset that created this encoder
     *
     * @param  averageBytesPerChar
     *         A positive float value indicating the expected number of
     *         bytes that will be produced for each input character
     *
     * @param  maxBytesPerChar
     *         A positive float value indicating the maximum number of
     *         bytes that will be produced for each input character
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          初始化一个新的编码器。新的编码器将具有给定的字节/字符值，它的替换将是字节数组{(字节)'?”}。
     */
    protected CharsetEncoder(Charset cs,
                             float averageBytesPerChar,
                             float maxBytesPerChar)
    {
        this(cs,
             averageBytesPerChar, maxBytesPerChar,
             new byte[] { (byte)'?' });
    }

    /**
     * Returns the charset that created this encoder.返回创建此编码器的字符集。
     *
     * @return  This encoder's charset
     */
    public final Charset charset() {
        return charset;
    }

    /**
     * Returns this encoder's replacement value.返回编码器的替换值。
     *
     * @return  This encoder's current replacement,
     *          which is never <tt>null</tt> and is never empty
     */
    public final byte[] replacement() {




        return Arrays.copyOf(replacement, replacement.length);

    }

    /**
     * Changes this encoder's replacement value.
     *
     * <p> This method invokes the {@link #implReplaceWith implReplaceWith}
     * method, passing the new replacement, after checking that the new
     * replacement is acceptable.  </p>
     *
     * @param  newReplacement  The replacement value
     *





     *         The new replacement; must not be <tt>null</tt>, must have
     *         non-zero length, must not be longer than the value returned by
     *         the {@link #maxBytesPerChar() maxBytesPerChar} method, and
     *         must be {@link #isLegalReplacement legal}

     *
     * @return  This encoder
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameter do not hold
     *          更改编码器的替换值。
    该方法在检查新替换是否可接受后调用implReplaceWith方法，传递新的替换。
     */
    public final CharsetEncoder replaceWith(byte[] newReplacement) {
        if (newReplacement == null)
            throw new IllegalArgumentException("Null replacement");
        int len = newReplacement.length;
        if (len == 0)
            throw new IllegalArgumentException("Empty replacement");
        if (len > maxBytesPerChar)
            throw new IllegalArgumentException("Replacement too long");




        if (!isLegalReplacement(newReplacement))
            throw new IllegalArgumentException("Illegal replacement");
        this.replacement = Arrays.copyOf(newReplacement, newReplacement.length);

        implReplaceWith(this.replacement);
        return this;
    }

    /**
     * Reports a change to this encoder's replacement value.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by encoders that require notification of changes to
     * the replacement.  </p>
     *
     * @param  newReplacement    The replacement value
     *                           报告对该编码器的替换值的更改。
    这个方法的默认实现什么都不做。该方法应该由需要通知替换更改的编码器重写。
     */
    protected void implReplaceWith(byte[] newReplacement) {
    }



    private WeakReference<CharsetDecoder> cachedDecoder = null;

    /**
     * Tells whether or not the given byte array is a legal replacement value
     * for this encoder.
     *
     * <p> A replacement is legal if, and only if, it is a legal sequence of
     * bytes in this encoder's charset; that is, it must be possible to decode
     * the replacement into one or more sixteen-bit Unicode characters.
     *
     * <p> The default implementation of this method is not very efficient; it
     * should generally be overridden to improve performance.  </p>
     *
     * @param  repl  The byte array to be tested
     *
     * @return  <tt>true</tt> if, and only if, the given byte array
     *          is a legal replacement value for this encoder
     *          告诉给定的字节数组是否是该编码器的合法替换值。
    替换是合法的，只要它是这个编码器字符集中合法的字节序列;也就是说，必须有可能将替换转换成一个或多个16位的Unicode字符。
    这种方法的默认实现不是很有效;通常应该重写它以提高性能。
     */
    public boolean isLegalReplacement(byte[] repl) {
        WeakReference<CharsetDecoder> wr = cachedDecoder;
        CharsetDecoder dec = null;
        if ((wr == null) || ((dec = wr.get()) == null)) {
            dec = charset().newDecoder();
            dec.onMalformedInput(CodingErrorAction.REPORT);
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            cachedDecoder = new WeakReference<CharsetDecoder>(dec);
        } else {
            dec.reset();
        }
        ByteBuffer bb = ByteBuffer.wrap(repl);
        CharBuffer cb = CharBuffer.allocate((int)(bb.remaining()
                                                  * dec.maxCharsPerByte()));
        CoderResult cr = dec.decode(bb, cb, true);
        return !cr.isError();
    }



    /**
     * Returns this encoder's current action for malformed-input errors.
     *
     * @return The current malformed-input action, which is never <tt>null</tt>
     * 返回该编码器的当前动作，以防止输入错误。
     */
    public CodingErrorAction malformedInputAction() {
        return malformedInputAction;
    }

    /**
     * Changes this encoder's action for malformed-input errors.
     *
     * <p> This method invokes the {@link #implOnMalformedInput
     * implOnMalformedInput} method, passing the new action.  </p>
     *
     * @param  newAction  The new action; must not be <tt>null</tt>
     *
     * @return  This encoder
     *
     * @throws IllegalArgumentException
     *         If the precondition on the parameter does not hold
     *         更改此编码器的动作以防止输入错误。
    该方法调用implOnMalformedInput方法，传递新的操作。
     */
    public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        malformedInputAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * Reports a change to this encoder's malformed-input action.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by encoders that require notification of changes to
     * the malformed-input action.  </p>
     *
     * @param  newAction  The new action
     *                    报告对编码器的malformed-input操作的更改。
    这个方法的默认实现什么都不做。该方法应该由需要通知修改malformed-input操作的编码器重写。
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) { }

    /**
     * Returns this encoder's current action for unmappable-character errors.
     *
     * @return The current unmappable-character action, which is never
     *         <tt>null</tt>
     *         返回此编码器的当前动作，以处理unmappable-character错误。
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmappableCharacterAction;
    }

    /**
     * Changes this encoder's action for unmappable-character errors.
     *
     * <p> This method invokes the {@link #implOnUnmappableCharacter
     * implOnUnmappableCharacter} method, passing the new action.  </p>
     *
     * @param  newAction  The new action; must not be <tt>null</tt>
     *
     * @return  This encoder
     *
     * @throws IllegalArgumentException
     *         If the precondition on the parameter does not hold
     *
    更改此编码器的动作，以处理unmappable-character错误。
    该方法调用implOnUnmappableCharacter方法，传递新的操作。
     */
    public final CharsetEncoder onUnmappableCharacter(CodingErrorAction
                                                      newAction)
    {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        unmappableCharacterAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    /**
     * Reports a change to this encoder's unmappable-character action.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by encoders that require notification of changes to
     * the unmappable-character action.  </p>
     *
     * @param  newAction  The new action
     *                    报告对该编码器的unmappable字符操作的更改。
    这个方法的默认实现什么都不做。需要通知unmappable-character操作更改的编码器应该重写此方法。
     */
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) { }

    /**
     * Returns the average number of bytes that will be produced for each
     * character of input.  This heuristic value may be used to estimate the size
     * of the output buffer required for a given input sequence.
     *
     * @return  The average number of bytes produced
     *          per character of input
     *          返回将为每个输入字符生成的平均字节数。这个启发式值可用于估计给定输入序列所需的输出缓冲区的大小。
     */
    public final float averageBytesPerChar() {
        return averageBytesPerChar;
    }

    /**
     * Returns the maximum number of bytes that will be produced for each
     * character of input.  This value may be used to compute the worst-case size
     * of the output buffer required for a given input sequence.
     *
     * @return  The maximum number of bytes that will be produced per
     *          character of input
     *          返回将为每个输入字符生成的最大字节数。此值可用于计算给定输入序列所需的输出缓冲区的最坏情况大小。
     */
    public final float maxBytesPerChar() {
        return maxBytesPerChar;
    }

    /**
     * Encodes as many characters as possible from the given input buffer,
     * writing the results to the given output buffer.
     *
     * <p> The buffers are read from, and written to, starting at their current
     * positions.  At most {@link Buffer#remaining in.remaining()} characters
     * will be read and at most {@link Buffer#remaining out.remaining()}
     * bytes will be written.  The buffers' positions will be advanced to
     * reflect the characters read and the bytes written, but their marks and
     * limits will not be modified.
     *
     * <p> In addition to reading characters from the input buffer and writing
     * bytes to the output buffer, this method returns a {@link CoderResult}
     * object to describe its reason for termination:
     *
     * <ul>
     *
     *   <li><p> {@link CoderResult#UNDERFLOW} indicates that as much of the
     *   input buffer as possible has been encoded.  If there is no further
     *   input then the invoker can proceed to the next step of the
     *   <a href="#steps">encoding operation</a>.  Otherwise this method
     *   should be invoked again with further input.  </p></li>
     *
     *   <li><p> {@link CoderResult#OVERFLOW} indicates that there is
     *   insufficient space in the output buffer to encode any more characters.
     *   This method should be invoked again with an output buffer that has
     *   more {@linkplain Buffer#remaining remaining} bytes. This is
     *   typically done by draining any encoded bytes from the output
     *   buffer.  </p></li>
     *
     *   <li><p> A {@linkplain CoderResult#malformedForLength
     *   malformed-input} result indicates that a malformed-input
     *   error has been detected.  The malformed characters begin at the input
     *   buffer's (possibly incremented) position; the number of malformed
     *   characters may be determined by invoking the result object's {@link
     *   CoderResult#length() length} method.  This case applies only if the
     *   {@linkplain #onMalformedInput malformed action} of this encoder
     *   is {@link CodingErrorAction#REPORT}; otherwise the malformed input
     *   will be ignored or replaced, as requested.  </p></li>
     *
     *   <li><p> An {@linkplain CoderResult#unmappableForLength
     *   unmappable-character} result indicates that an
     *   unmappable-character error has been detected.  The characters that
     *   encode the unmappable character begin at the input buffer's (possibly
     *   incremented) position; the number of such characters may be determined
     *   by invoking the result object's {@link CoderResult#length() length}
     *   method.  This case applies only if the {@linkplain #onUnmappableCharacter
     *   unmappable action} of this encoder is {@link
     *   CodingErrorAction#REPORT}; otherwise the unmappable character will be
     *   ignored or replaced, as requested.  </p></li>
     *
     * </ul>
     *
     * In any case, if this method is to be reinvoked in the same encoding
     * operation then care should be taken to preserve any characters remaining
     * in the input buffer so that they are available to the next invocation.
     *
     * <p> The <tt>endOfInput</tt> parameter advises this method as to whether
     * the invoker can provide further input beyond that contained in the given
     * input buffer.  If there is a possibility of providing additional input
     * then the invoker should pass <tt>false</tt> for this parameter; if there
     * is no possibility of providing further input then the invoker should
     * pass <tt>true</tt>.  It is not erroneous, and in fact it is quite
     * common, to pass <tt>false</tt> in one invocation and later discover that
     * no further input was actually available.  It is critical, however, that
     * the final invocation of this method in a sequence of invocations always
     * pass <tt>true</tt> so that any remaining unencoded input will be treated
     * as being malformed.
     *
     * <p> This method works by invoking the {@link #encodeLoop encodeLoop}
     * method, interpreting its results, handling error conditions, and
     * reinvoking it as necessary.  </p>
     *
     *
     * @param  in
     *         The input character buffer
     *
     * @param  out
     *         The output byte buffer
     *
     * @param  endOfInput
     *         <tt>true</tt> if, and only if, the invoker can provide no
     *         additional input characters beyond those in the given buffer
     *
     * @return  A coder-result object describing the reason for termination
     *
     * @throws  IllegalStateException
     *          If an encoding operation is already in progress and the previous
     *          step was an invocation neither of the {@link #reset reset}
     *          method, nor of this method with a value of <tt>false</tt> for
     *          the <tt>endOfInput</tt> parameter, nor of this method with a
     *          value of <tt>true</tt> for the <tt>endOfInput</tt> parameter
     *          but a return value indicating an incomplete encoding operation
     *
     * @throws  CoderMalfunctionError
     *          If an invocation of the encodeLoop method threw
     *          an unexpected exception
     *          从给定的输入缓冲区编码尽可能多的字符，将结果写入给定的输出缓冲区。
    缓冲区从当前位置开始读取和写入。其余的()字符将被读取，并且在大多数情况下将被写入。缓冲区的位置将被改进，以反映读取的字符和写入的字节，但是不会修改它们的标记和限制。
    除了从输入缓冲区读取字符和向输出缓冲区写入字节外，该方法还返回一个CoderResult对象，以描述其终止的原因:
    CoderResult。欠流表明尽可能多的输入缓冲区已经被编码。如果没有进一步的输入，则调用程序可以继续进行编码操作的下一步。否则，应该使用进一步的输入再次调用此方法。
    CoderResult。溢出表示输出缓冲区中没有足够的空间来编码更多的字符。该方法应该再次使用具有更多剩余字节的输出缓冲区调用。这通常是通过从输出缓冲区中抽取任何编码的字节来完成的。
    malford -input结果表明检测到malford -input错误。畸形字符开始于输入缓冲区的位置(可能增加);可以通过调用结果对象的长度方法来确定畸形字符的数量。本案例仅适用于该编码器的畸形动作为CodingErrorAction.REPORT;否则，根据请求，将忽略或替换错误的输入。
    一个unmappable-character结果表明检测到一个unmappable-character错误。编码不可映射字符的字符从输入缓冲区的(可能是递增的)位置开始;可以通过调用result对象的length方法来确定这些字符的数量。此情况仅适用于此编码器的不可映射操作是CodingErrorAction.REPORT;否则，不可映射字符将按要求被忽略或替换。
    在任何情况下，如果要在相同的编码操作中重新调用此方法，则应该注意保存输入缓冲区中剩余的任何字符，以便下次调用时可以使用它们。
    endOfInput参数建议这种方法，即调用者是否能够提供超出给定输入缓冲区中所包含的进一步输入。如果有可能提供额外的输入，那么调用者应该为这个参数传递false;如果不可能提供进一步的输入，则调用程序应该传递true。在一次调用中传递false并随后发现实际上没有进一步的输入是错误的，实际上这是很常见的。然而，关键的是，在一系列调用中对该方法的最终调用始终传递为true，以便将剩余的未编码输入处理为畸形。
    该方法通过调用encodeLoop方法来工作，解释其结果，处理错误条件，并在必要时重新调用它。
     */
    public final CoderResult encode(CharBuffer in, ByteBuffer out,
                                    boolean endOfInput)
    {
        int newState = endOfInput ? ST_END : ST_CODING;
        if ((state != ST_RESET) && (state != ST_CODING)
            && !(endOfInput && (state == ST_END)))
            throwIllegalStateException(state, newState);
        state = newState;

        for (;;) {

            CoderResult cr;
            try {
                cr = encodeLoop(in, out);
            } catch (BufferUnderflowException x) {
                throw new CoderMalfunctionError(x);
            } catch (BufferOverflowException x) {
                throw new CoderMalfunctionError(x);
            }

            if (cr.isOverflow())
                return cr;

            if (cr.isUnderflow()) {
                if (endOfInput && in.hasRemaining()) {
                    cr = CoderResult.malformedForLength(in.remaining());
                    // Fall through to malformed-input case
                } else {
                    return cr;
                }
            }

            CodingErrorAction action = null;
            if (cr.isMalformed())
                action = malformedInputAction;
            else if (cr.isUnmappable())
                action = unmappableCharacterAction;
            else
                assert false : cr.toString();

            if (action == CodingErrorAction.REPORT)
                return cr;

            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replacement.length)
                    return CoderResult.OVERFLOW;
                out.put(replacement);
            }

            if ((action == CodingErrorAction.IGNORE)
                || (action == CodingErrorAction.REPLACE)) {
                // Skip erroneous input either way
                in.position(in.position() + cr.length());
                continue;
            }

            assert false;
        }

    }

    /**
     * Flushes this encoder.
     *
     * <p> Some encoders maintain internal state and may need to write some
     * final bytes to the output buffer once the overall input sequence has
     * been read.
     *
     * <p> Any additional output is written to the output buffer beginning at
     * its current position.  At most {@link Buffer#remaining out.remaining()}
     * bytes will be written.  The buffer's position will be advanced
     * appropriately, but its mark and limit will not be modified.
     *
     * <p> If this method completes successfully then it returns {@link
     * CoderResult#UNDERFLOW}.  If there is insufficient room in the output
     * buffer then it returns {@link CoderResult#OVERFLOW}.  If this happens
     * then this method must be invoked again, with an output buffer that has
     * more room, in order to complete the current <a href="#steps">encoding
     * operation</a>.
     *
     * <p> If this encoder has already been flushed then invoking this method
     * has no effect.
     *
     * <p> This method invokes the {@link #implFlush implFlush} method to
     * perform the actual flushing operation.  </p>
     *
     * @param  out
     *         The output byte buffer
     *
     * @return  A coder-result object, either {@link CoderResult#UNDERFLOW} or
     *          {@link CoderResult#OVERFLOW}
     *
     * @throws  IllegalStateException
     *          If the previous step of the current encoding operation was an
     *          invocation neither of the {@link #flush flush} method nor of
     *          the three-argument {@link
     *          #encode(CharBuffer,ByteBuffer,boolean) encode} method
     *          with a value of <tt>true</tt> for the <tt>endOfInput</tt>
     *          parameter
     *          经过编码器。
    有些编码器维护内部状态，一旦读取了整个输入序列，可能需要将一些最后的字节写入输出缓冲区。
    任何额外的输出都被写入从当前位置开始的输出缓冲区。剩余的()字节将被写入。缓冲区的位置将被适当地提升，但是它的标记和限制将不会被修改。
    如果该方法成功完成，则返回CoderResult.UNDERFLOW。如果输出缓冲区中空间不足，则返回CoderResult.OVERFLOW。如果发生这种情况，那么必须再次调用此方法，并使用具有更大空间的输出缓冲区，以便完成当前的编码操作。
    如果这个编码器已经被刷新，那么调用这个方法没有任何效果。
    这个方法调用implFlush方法来执行实际的刷新操作。
     */
    public final CoderResult flush(ByteBuffer out) {
        if (state == ST_END) {
            CoderResult cr = implFlush(out);
            if (cr.isUnderflow())
                state = ST_FLUSHED;
            return cr;
        }

        if (state != ST_FLUSHED)
            throwIllegalStateException(state, ST_FLUSHED);

        return CoderResult.UNDERFLOW; // Already flushed
    }

    /**
     * Flushes this encoder.
     *
     * <p> The default implementation of this method does nothing, and always
     * returns {@link CoderResult#UNDERFLOW}.  This method should be overridden
     * by encoders that may need to write final bytes to the output buffer
     * once the entire input sequence has been read. </p>
     *
     * @param  out
     *         The output byte buffer
     *
     * @return  A coder-result object, either {@link CoderResult#UNDERFLOW} or
     *          {@link CoderResult#OVERFLOW}
     *          经过编码器。
    这个方法的默认实现什么都不做，并且总是返回CoderResult.UNDERFLOW。该方法应该被编码器覆盖，一旦读取了整个输入序列，编码器可能需要将最终字节写入输出缓冲区。
     */
    protected CoderResult implFlush(ByteBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * Resets this encoder, clearing any internal state.
     *
     * <p> This method resets charset-independent state and also invokes the
     * {@link #implReset() implReset} method in order to perform any
     * charset-specific reset actions.  </p>
     *
     * @return  This encoder
     * 重置此编码器，清除任何内部状态。
    该方法重置与字符集无关的状态，并调用implReset方法，以便执行任何与字符集相关的重置操作。
     *
     */
    public final CharsetEncoder reset() {
        implReset();
        state = ST_RESET;
        return this;
    }

    /**
     * Resets this encoder, clearing any charset-specific internal state.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by encoders that maintain internal state.  </p>
     * 重置此编码器，清除任何指定字符集的内部状态。
     这个方法的默认实现什么都不做。该方法应该由维护内部状态的编码器重写。
     */
    protected void implReset() { }

    /**
     * Encodes one or more characters into one or more bytes.
     *
     * <p> This method encapsulates the basic encoding loop, encoding as many
     * characters as possible until it either runs out of input, runs out of room
     * in the output buffer, or encounters an encoding error.  This method is
     * invoked by the {@link #encode encode} method, which handles result
     * interpretation and error recovery.
     *
     * <p> The buffers are read from, and written to, starting at their current
     * positions.  At most {@link Buffer#remaining in.remaining()} characters
     * will be read, and at most {@link Buffer#remaining out.remaining()}
     * bytes will be written.  The buffers' positions will be advanced to
     * reflect the characters read and the bytes written, but their marks and
     * limits will not be modified.
     *
     * <p> This method returns a {@link CoderResult} object to describe its
     * reason for termination, in the same manner as the {@link #encode encode}
     * method.  Most implementations of this method will handle encoding errors
     * by returning an appropriate result object for interpretation by the
     * {@link #encode encode} method.  An optimized implementation may instead
     * examine the relevant error action and implement that action itself.
     *
     * <p> An implementation of this method may perform arbitrary lookahead by
     * returning {@link CoderResult#UNDERFLOW} until it receives sufficient
     * input.  </p>
     *
     * @param  in
     *         The input character buffer
     *
     * @param  out
     *         The output byte buffer
     *
     * @return  A coder-result object describing the reason for termination
     * 将一个或多个字符编码为一个或多个字节。
    该方法封装了基本的编码循环，编码尽可能多的字符，直到它耗尽输入，在输出缓冲区中耗尽空间，或者遇到编码错误。该方法由encode方法调用，该方法处理结果解释和错误恢复。
    缓冲区从当前位置开始读取和写入。最多为in.remaining()字符将被读取，最多为out.remaining()字节将被写入。缓冲区的位置将被改进，以反映读取的字符和写入的字节，但是不会修改它们的标记和限制。
    该方法返回一个CoderResult对象，以描述其终止的原因，其方式与encode方法相同。该方法的大多数实现都将通过返回适当的结果对象来通过encode方法进行解释来处理编码错误。优化的实现可以检查相关的错误操作并实现该操作本身。
    该方法的实现可以通过返回CoderResult来执行任意的前视。流下，直到接收到足够的输入。
     */
    protected abstract CoderResult encodeLoop(CharBuffer in,
                                              ByteBuffer out);

    /**
     * Convenience method that encodes the remaining content of a single input
     * character buffer into a newly-allocated byte buffer.
     *
     * <p> This method implements an entire <a href="#steps">encoding
     * operation</a>; that is, it resets this encoder, then it encodes the
     * characters in the given character buffer, and finally it flushes this
     * encoder.  This method should therefore not be invoked if an encoding
     * operation is already in progress.  </p>
     *
     * @param  in
     *         The input character buffer
     *
     * @return A newly-allocated byte buffer containing the result of the
     *         encoding operation.  The buffer's position will be zero and its
     *         limit will follow the last byte written.
     *
     * @throws  IllegalStateException
     *          If an encoding operation is already in progress
     *
     * @throws  MalformedInputException
     *          If the character sequence starting at the input buffer's current
     *          position is not a legal sixteen-bit Unicode sequence and the current malformed-input action
     *          is {@link CodingErrorAction#REPORT}
     *
     * @throws  UnmappableCharacterException
     *          If the character sequence starting at the input buffer's current
     *          position cannot be mapped to an equivalent byte sequence and
     *          the current unmappable-character action is {@link
     *          CodingErrorAction#REPORT}
     *          将单个输入字符缓冲区的其余内容编码到新分配的字节缓冲区的方便方法。
    该方法实现了一个完整的编码操作;也就是说，它重新设置这个编码器，然后对给定的字符缓冲区中的字符进行编码，最后刷新这个编码器。因此，如果已在进行编码操作，则不应调用此方法。
     */
    public final ByteBuffer encode(CharBuffer in)
        throws CharacterCodingException
    {
        int n = (int)(in.remaining() * averageBytesPerChar());
        ByteBuffer out = ByteBuffer.allocate(n);

        if ((n == 0) && (in.remaining() == 0))
            return out;
        reset();
        for (;;) {
            CoderResult cr = in.hasRemaining() ?
                encode(in, out, true) : CoderResult.UNDERFLOW;
            if (cr.isUnderflow())
                cr = flush(out);

            if (cr.isUnderflow())
                break;
            if (cr.isOverflow()) {
                n = 2*n + 1;    // Ensure progress; n might be 0!
                ByteBuffer o = ByteBuffer.allocate(n);
                out.flip();
                o.put(out);
                out = o;
                continue;
            }
            cr.throwException();
        }
        out.flip();
        return out;
    }















































































    private boolean canEncode(CharBuffer cb) {
        if (state == ST_FLUSHED)
            reset();
        else if (state != ST_RESET)
            throwIllegalStateException(state, ST_CODING);
        CodingErrorAction ma = malformedInputAction();
        CodingErrorAction ua = unmappableCharacterAction();
        try {
            onMalformedInput(CodingErrorAction.REPORT);
            onUnmappableCharacter(CodingErrorAction.REPORT);
            encode(cb);
        } catch (CharacterCodingException x) {
            return false;
        } finally {
            onMalformedInput(ma);
            onUnmappableCharacter(ua);
            reset();
        }
        return true;
    }

    /**
     * Tells whether or not this encoder can encode the given character.
     *
     * <p> This method returns <tt>false</tt> if the given character is a
     * surrogate character; such characters can be interpreted only when they
     * are members of a pair consisting of a high surrogate followed by a low
     * surrogate.  The {@link #canEncode(java.lang.CharSequence)
     * canEncode(CharSequence)} method may be used to test whether or not a
     * character sequence can be encoded.
     *
     * <p> This method may modify this encoder's state; it should therefore not
     * be invoked if an <a href="#steps">encoding operation</a> is already in
     * progress.
     *
     * <p> The default implementation of this method is not very efficient; it
     * should generally be overridden to improve performance.  </p>
     *
     * @param   c
     *          The given character
     *
     * @return  <tt>true</tt> if, and only if, this encoder can encode
     *          the given character
     *
     * @throws  IllegalStateException
     *          If an encoding operation is already in progress
     */
    public boolean canEncode(char c) {
        CharBuffer cb = CharBuffer.allocate(1);
        cb.put(c);
        cb.flip();
        return canEncode(cb);
    }

    /**
     * Tells whether or not this encoder can encode the given character
     * sequence.
     *
     * <p> If this method returns <tt>false</tt> for a particular character
     * sequence then more information about why the sequence cannot be encoded
     * may be obtained by performing a full <a href="#steps">encoding
     * operation</a>.
     *
     * <p> This method may modify this encoder's state; it should therefore not
     * be invoked if an encoding operation is already in progress.
     *
     * <p> The default implementation of this method is not very efficient; it
     * should generally be overridden to improve performance.  </p>
     *
     * @param   cs
     *          The given character sequence
     *
     * @return  <tt>true</tt> if, and only if, this encoder can encode
     *          the given character without throwing any exceptions and without
     *          performing any replacements
     *
     * @throws  IllegalStateException
     *          If an encoding operation is already in progress
     */
    public boolean canEncode(CharSequence cs) {
        CharBuffer cb;
        if (cs instanceof CharBuffer)
            cb = ((CharBuffer)cs).duplicate();
        else
            cb = CharBuffer.wrap(cs.toString());
        return canEncode(cb);
    }




    private void throwIllegalStateException(int from, int to) {
        throw new IllegalStateException("Current state = " + stateNames[from]
                                        + ", new state = " + stateNames[to]);
    }

}
