/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;
import java.util.function.LongUnaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

/**
 * A {@code long} array in which elements may be updated atomically.
 * See the {@link java.util.concurrent.atomic} package specification
 * for description of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 * 元素可以自动更新的长数组。看到java . util . concurrent。描述原子变量属性的原子包规范。
 */
public class AtomicLongArray implements java.io.Serializable {
    private static final long serialVersionUID = -2308431214976778248L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(long[].class);
    private static final int shift;
    private final long[] array;

    static {
        int scale = unsafe.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }

    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    /**
     * Creates a new AtomicLongArray of the given length, with all
     * elements initially zero.创建给定长度的新AtomicLongArray，所有元素最初为零。
     *
     * @param length the length of the array
     */
    public AtomicLongArray(int length) {
        array = new long[length];
    }

    /**
     * Creates a new AtomicLongArray with the same length as, and
     * all elements copied from, the given array.创建一个新的AtomicLongArray，长度与给定数组相同，所有元素都是从该数组复制的。
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicLongArray(long[] array) {
        // Visibility guaranteed by final field guarantees
        this.array = array.clone();
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public final int length() {
        return array.length;
    }

    /**
     * Gets the current value at position {@code i}.
     *
     * @param i the index
     * @return the current value
     */
    public final long get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private long getRaw(long offset) {
        return unsafe.getLongVolatile(array, offset);
    }

    /**
     * Sets the element at position {@code i} to the given value.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final void set(int i, long newValue) {
        unsafe.putLongVolatile(array, checkedByteOffset(i), newValue);
    }

    /**
     * Eventually sets the element at position {@code i} to the given value.最终将元素设置为给定的值。
     *
     * @param i the index
     * @param newValue the new value
     * @since 1.6
     */
    public final void lazySet(int i, long newValue) {
        unsafe.putOrderedLong(array, checkedByteOffset(i), newValue);
    }

    /**
     * Atomically sets the element at position {@code i} to the given value
     * and returns the old value.原子化地将元素设置为给定值的位置i，并返回旧值。
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final long getAndSet(int i, long newValue) {
        return unsafe.getAndSetLong(array, checkedByteOffset(i), newValue);
    }

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value if the current value {@code ==} the expected value.如果当前值=期望值，则原子化地将元素设置为给定的更新值。
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(int i, long expect, long update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, long expect, long update) {
        return unsafe.compareAndSwapLong(array, offset, expect, update);
    }

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value if the current value {@code ==} the expected value.
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     * 如果当前值=期望值，则原子化地将元素设置为给定的更新值。
     可能会虚假地失败，并且不提供订单保证，因此很少是compareAndSet的合适替代方案。
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public final boolean weakCompareAndSet(int i, long expect, long update) {
        return compareAndSet(i, expect, update);
    }

    /**
     * Atomically increments by one the element at index {@code i}.原子地将给定的值添加到索引i的元素中。
     *
     * @param i the index
     * @return the previous value
     */
    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    /**
     * Atomically decrements by one the element at index {@code i}.原子化地减去索引i的元素。
     *
     * @param i the index
     * @return the previous value
     */
    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    /**
     * Atomically adds the given value to the element at index {@code i}.原子地将给定的值添加到索引i的元素中。
     *
     * @param i the index
     * @param delta the value to add
     * @return the previous value
     */
    public final long getAndAdd(int i, long delta) {
        return unsafe.getAndAddLong(array, checkedByteOffset(i), delta);
    }

    /**
     * Atomically increments by one the element at index {@code i}.原子化地增加索引i的一个元素。
     *
     * @param i the index
     * @return the updated value
     */
    public final long incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    /**
     * Atomically decrements by one the element at index {@code i}.原子化地减去索引i的元素。
     *
     * @param i the index
     * @return the updated value
     */
    public final long decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    /**
     * Atomically adds the given value to the element at index {@code i}.原子地将给定的值添加到索引i的元素中。
     *
     * @param i the index
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGet(int i, long delta) {
        return getAndAdd(i, delta) + delta;
    }

    /**
     * Atomically updates the element at index {@code i} with the results
     * of applying the given function, returning the previous value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     * 将索引i中的元素原子更新为应用给定函数的结果，返回之前的值。该函数应该是无副作用的，因为当尝试的更新由于线程之间的争用而失败时，可以重新应用它。
     *
     * @param i the index
     * @param updateFunction a side-effect-free function
     * @return the previous value
     * @since 1.8
     */
    public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * Atomically updates the element at index {@code i} with the results
     * of applying the given function, returning the updated value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param i the index
     * @param updateFunction a side-effect-free function
     * @return the updated value
     * @since 1.8
     * 原子地使用应用给定函数的结果更新索引i上的元素，返回更新后的值。该函数应该是无副作用的，因为当尝试的更新由于线程之间的争用而失败时，可以重新应用它。
     */
    public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * Atomically updates the element at index {@code i} with the
     * results of applying the given function to the current and
     * given values, returning the previous value. The function should
     * be side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value at index {@code i} as its first
     * argument, and the given update as the second argument.
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the previous value
     * @since 1.8
     * 原子性地更新索引i上的元素，其结果是将给定的函数应用到当前和给定的值，返回先前的值。该函数应该是无副作用的，因为当尝试的更新由于线程之间的争用而失败时，可以重新应用它。函数的第一个参数是索引i处的当前值，第二个参数是给定的更新。
     */
    public final long getAndAccumulate(int i, long x,
                                      LongBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * Atomically updates the element at index {@code i} with the
     * results of applying the given function to the current and
     * given values, returning the updated value. The function should
     * be side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value at index {@code i} as its first
     * argument, and the given update as the second argument.
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the updated value
     * @since 1.8
     * 原子地更新索引i上的元素，其结果是将给定的函数应用到当前和给定的值，返回更新的值。该函数应该是无副作用的，因为当尝试的更新由于线程之间的争用而失败时，可以重新应用它。函数的第一个参数是索引i处的当前值，第二个参数是给定的更新。
     */
    public final long accumulateAndGet(int i, long x,
                                      LongBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * Returns the String representation of the current values of array.
     * @return the String representation of the current values of array
     */
    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }

}
