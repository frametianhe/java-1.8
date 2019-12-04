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
import java.io.Serializable;

/**
 * One or more variables that together maintain an initially zero
 * {@code double} sum.  When updates (method {@link #add}) are
 * contended across threads, the set of variables may grow dynamically
 * to reduce contention.  Method {@link #sum} (or, equivalently {@link
 * #doubleValue}) returns the current total combined across the
 * variables maintaining the sum. The order of accumulation within or
 * across threads is not guaranteed. Thus, this class may not be
 * applicable if numerical stability is required, especially when
 * combining values of substantially different orders of magnitude.
 *
 * <p>This class is usually preferable to alternatives when multiple
 * threads update a common value that is used for purposes such as
 * summary statistics that are frequently updated but less frequently
 * read.
 *
 * <p>This class extends {@link Number}, but does <em>not</em> define
 * methods such as {@code equals}, {@code hashCode} and {@code
 * compareTo} because instances are expected to be mutated, and so are
 * not useful as collection keys.
 *
 * @since 1.8
 * @author Doug Lea
 * 一个或多个变量一起保持初始的0倍和。当更新(方法添加)在线程之间进行竞争时，变量集可以动态增长以减少争用。方法sum(或等效的doubleValue)返回当前的总数，该总数包含所有保持该和的变量。在线程内部或跨线程中积累的顺序没有保证。因此，如果需要数值稳定性，这个类可能不适用，尤其是当组合具有不同数量级的值时。
当多个线程更新用于经常更新但不太频繁读取的汇总统计信息等目的的公共值时，这个类通常比其他方法更可取。
该类扩展了Number，但不定义equals、hashCode和compareTo等方法，因为实例预期会发生突变，因此作为集合键没有用处。
 */
public class DoubleAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /*
     * Note that we must use "long" for underlying representations,
     * because there is no compareAndSet for double, due to the fact
     * that the bitwise equals used in any CAS implementation is not
     * the same as double-precision equals.  However, we use CAS only
     * to detect and alleviate contention, for which bitwise equals
     * works best anyway. In principle, the long/double conversions
     * used here should be essentially free on most platforms since
     * they just re-interpret bits.
     */

    /**
     * Creates a new adder with initial sum of zero.创建一个初始和为0的加法器。
     */
    public DoubleAdder() {
    }

    /**
     * Adds the given value.
     *
     * @param x the value to add
     */
    public void add(double x) {
        Cell[] as; long b, v; int m; Cell a;
        if ((as = cells) != null ||
            !casBase(b = base,
                     Double.doubleToRawLongBits
                     (Double.longBitsToDouble(b) + x))) {
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended = a.cas(v = a.value,
                                      Double.doubleToRawLongBits
                                      (Double.longBitsToDouble(v) + x))))
                doubleAccumulate(x, null, uncontended);
        }
    }

    /**
     * Returns the current sum.  The returned value is <em>NOT</em> an
     * atomic snapshot; invocation in the absence of concurrent
     * updates returns an accurate result, but concurrent updates that
     * occur while the sum is being calculated might not be
     * incorporated.  Also, because floating-point arithmetic is not
     * strictly associative, the returned result need not be identical
     * to the value that would be obtained in a sequential series of
     * updates to a single variable.
     *
     * @return the sum
     * 返回当前的总和。返回的值不是原子快照;在没有并发更新的情况下，调用会返回一个准确的结果，但是在计算和计算时发生的并发更新可能不会被合并。同样，由于浮点算法不是严格的关联，返回的结果不一定与在对单个变量的一系列连续更新中获得的值相同。
     */
    public double sum() {
        Cell[] as = cells; Cell a;
        double sum = Double.longBitsToDouble(base);
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += Double.longBitsToDouble(a.value);
            }
        }
        return sum;
    }

    /**
     * Resets variables maintaining the sum to zero.  This method may
     * be a useful alternative to creating a new adder, but is only
     * effective if there are no concurrent updates.  Because this
     * method is intrinsically racy, it should only be used when it is
     * known that no threads are concurrently updating.
     * 重置保持和为零的变量。这种方法可能是创建新的adder的有用的替代方法，但是只有在没有并发更新的情况下才有效。因为这个方法本质上是活泼的，所以只有当知道没有线程同时更新时才应该使用它。
     */
    public void reset() {
        Cell[] as = cells; Cell a;
        base = 0L; // relies on fact that double 0 must have same rep as long
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = 0L;
            }
        }
    }

    /**
     * Equivalent in effect to {@link #sum} followed by {@link
     * #reset}. This method may apply for example during quiescent
     * points between multithreaded computations.  If there are
     * updates concurrent with this method, the returned value is
     * <em>not</em> guaranteed to be the final value occurring before
     * the reset.
     *
     * @return the sum
     * 等效于和之后的复位。这种方法可以应用于多线程计算之间的静止点。如果有与此方法并发的更新，则不能保证返回值是重置之前发生的最终值。
     */
    public double sumThenReset() {
        Cell[] as = cells; Cell a;
        double sum = Double.longBitsToDouble(base);
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    long v = a.value;
                    a.value = 0L;
                    sum += Double.longBitsToDouble(v);
                }
            }
        }
        return sum;
    }

    /**
     * Returns the String representation of the {@link #sum}.
     * @return the String representation of the {@link #sum}
     */
    public String toString() {
        return Double.toString(sum());
    }

    /**
     * Equivalent to {@link #sum}.
     *
     * @return the sum
     */
    public double doubleValue() {
        return sum();
    }

    /**
     * Returns the {@link #sum} as a {@code long} after a
     * narrowing primitive conversion.返回在窄化原语转换之后的长时间的总和。
     */
    public long longValue() {
        return (long)sum();
    }

    /**
     * Returns the {@link #sum} as an {@code int} after a
     * narrowing primitive conversion.返回在窄化原语转换之后的整数。
     */
    public int intValue() {
        return (int)sum();
    }

    /**
     * Returns the {@link #sum} as a {@code float}
     * after a narrowing primitive conversion.在缩小原始转换后，将sum返回为浮点数。
     */
    public float floatValue() {
        return (float)sum();
    }

    /**
     * Serialization proxy, used to avoid reference to the non-public
     * Striped64 superclass in serialized forms.序列化代理，用于避免在序列化形式中引用非公共的Striped64超类。
     * @serial include
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * The current value returned by sum().以sum()返回的当前值。
         * @serial
         */
        private final double value;

        SerializationProxy(DoubleAdder a) {
            value = a.sum();
        }

        /**
         * Returns a {@code DoubleAdder} object with initial state
         * held by this proxy.返回具有此代理所持初始状态的怀疑对象。
         *
         * @return a {@code DoubleAdder} object with initial state
         * held by this proxy.
         */
        private Object readResolve() {
            DoubleAdder a = new DoubleAdder();
            a.base = Double.doubleToRawLongBits(value);
            return a;
        }
    }

    /**
     * Returns a
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.DoubleAdder.SerializationProxy">
     * SerializationProxy</a>
     * representing the state of this instance.
     *
     * @return a {@link SerializationProxy}
     * representing the state of this instance
     * 返回表示该实例状态的SerializationProxy。
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * @param s the stream
     * @throws java.io.InvalidObjectException always
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
