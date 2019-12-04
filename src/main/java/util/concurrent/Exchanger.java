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
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * A synchronization point at which threads can pair and swap elements
 * within pairs.  Each thread presents some object on entry to the
 * {@link #exchange exchange} method, matches with a partner thread,
 * and receives its partner's object on return.  An Exchanger may be
 * viewed as a bidirectional form of a {@link SynchronousQueue}.
 * Exchangers may be useful in applications such as genetic algorithms
 * and pipeline designs.
 *
 * <p><b>Sample Usage:</b>
 * Here are the highlights of a class that uses an {@code Exchanger}
 * to swap buffers between threads so that the thread filling the
 * buffer gets a freshly emptied one when it needs it, handing off the
 * filled one to the thread emptying the buffer.
 *  <pre> {@code
 * class FillAndEmpty {
 *   Exchanger<DataBuffer> exchanger = new Exchanger<DataBuffer>();
 *   DataBuffer initialEmptyBuffer = ... a made-up type
 *   DataBuffer initialFullBuffer = ...
 *
 *   class FillingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialEmptyBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           addToBuffer(currentBuffer);
 *           if (currentBuffer.isFull())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ... }
 *     }
 *   }
 *
 *   class EmptyingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialFullBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           takeFromBuffer(currentBuffer);
 *           if (currentBuffer.isEmpty())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ...}
 *     }
 *   }
 *
 *   void start() {
 *     new Thread(new FillingLoop()).start();
 *     new Thread(new EmptyingLoop()).start();
 *   }
 * }}</pre>
 *
 * <p>Memory consistency effects: For each pair of threads that
 * successfully exchange objects via an {@code Exchanger}, actions
 * prior to the {@code exchange()} in each thread
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * those subsequent to a return from the corresponding {@code exchange()}
 * in the other thread.
 *
 * @since 1.5
 * @author Doug Lea and Bill Scherer and Michael Scott
 * @param <V> The type of objects that may be exchanged
 *           线程可以在对中对元素进行配对和交换的同步点。每个线程在交换方法的条目上显示一些对象，与伙伴线程匹配，并在返回时接收伙伴的对象。交换器可以看作是同步队列的双向形式。交换器可能在遗传算法和管道设计等应用中很有用。
示例用法:这里是一个类的突出部分，它使用交换器在线程之间交换缓冲区，以便在需要时，填充缓冲区的线程获得一个新清空的线程，并将填充的线程分发到线程清空缓冲区。
内存一致性影响:对于每一对通过交换器成功交换对象的线程，在每个线程中，在交换()之前，在另一个线程中对应的交换()返回之前的操作。
 */
//Exchanger实现线程间数据交换，提供一个同步点，两个线程同事到达开始交换数据
public class Exchanger<V> {

    /*
     * Overview: The core algorithm is, for an exchange "slot",
     * and a participant (caller) with an item:
     *
     * for (;;) {
     *   if (slot is empty) {                       // offer
     *     place item in a Node;
     *     if (can CAS slot from empty to node) {
     *       wait for release;
     *       return matching item in node;
     *     }
     *   }
     *   else if (can CAS slot from node to empty) { // release
     *     get the item in node;
     *     set matching item in node;
     *     release waiting thread;
     *   }
     *   // else retry on CAS failure
     * }
     *
     * This is among the simplest forms of a "dual data structure" --
     * see Scott and Scherer's DISC 04 paper and
     * http://www.cs.rochester.edu/research/synchronization/pseudocode/duals.html
     *
     * This works great in principle. But in practice, like many
     * algorithms centered on atomic updates to a single location, it
     * scales horribly when there are more than a few participants
     * using the same Exchanger. So the implementation instead uses a
     * form of elimination arena, that spreads out this contention by
     * arranging that some threads typically use different slots,
     * while still ensuring that eventually, any two parties will be
     * able to exchange items. That is, we cannot completely partition
     * across threads, but instead give threads arena indices that
     * will on average grow under contention and shrink under lack of
     * contention. We approach this by defining the Nodes that we need
     * anyway as ThreadLocals, and include in them per-thread index
     * and related bookkeeping state. (We can safely reuse per-thread
     * nodes rather than creating them fresh each time because slots
     * alternate between pointing to a node vs null, so cannot
     * encounter ABA problems. However, we do need some care in
     * resetting them between uses.)这在原则上很有效。但实际上，就像许多人一样
算法集中在单个位置的原子更新上
*当参与者人数超过几个时，就会出现可怕的规模
*使用相同的交换器。所以实现用a
*淘汰竞技场的形式，即通过
*安排一些线程通常使用不同的插槽，
*在确保最终达成协议的同时，任何一方都可以
*能够交换物品。也就是说，我们不能完全分割
*跨线程，而是为线程提供竞技场索引
*在竞争中平均会增长，在缺乏竞争时则会收缩
*争用。我们通过定义所需的节点来实现这一点
*无论如何作为threadlocal，并在其中包含每个线程的索引
*及相关簿记状况。(我们可以安全地重用每个线程
*节点而不是每次都创建它们，因为插槽
*在指向节点和指向null之间切换，所以不能
遇到ABA问题。然而，我们确实需要一些照顾
*在使用之间重置它们。)
     *
     * Implementing an effective arena requires allocating a bunch of
     * space, so we only do so upon detecting contention (except on
     * uniprocessors, where they wouldn't help, so aren't used).
     * Otherwise, exchanges use the single-slot slotExchange method.
     * On contention, not only must the slots be in different
     * locations, but the locations must not encounter memory
     * contention due to being on the same cache line (or more
     * generally, the same coherence unit).  Because, as of this
     * writing, there is no way to determine cacheline size, we define
     * a value that is enough for common platforms.  Additionally,
     * extra care elsewhere is taken to avoid other false/unintended
     * sharing and to enhance locality, including adding padding (via
     * sun.misc.Contended) to Nodes, embedding "bound" as an Exchanger
     * field, and reworking some park/unpark mechanics compared to
     * LockSupport versions.实现一个有效的竞技场需要分配一组
*空间，所以我们只在检测到争用时才这样做(on除外)
*单处理器，它们不会有帮助，所以不被使用)。
*否则，交换器使用单槽slotExchange方法。
*在争用方面，插槽不仅必须不同
*位置，但位置不能遇到内存
*由于在同一高速缓存线上(或更多)而引起的争用
*一般来说，相同的相干单位)。因为，因为这个
*写，没有办法确定cacheline大小，我们定义
*对于普通平台来说，这个值就足够了。此外,
*在其他地方要格外小心，避免出现其他错误/意外
*共享和增强局部性，包括添加填充(通过
将“绑定”作为交换器嵌入节点
* field，并对一些park/unpark机制进行返工
* LockSupport版本。
     *
     * The arena starts out with only one used slot. We expand the
     * effective arena size by tracking collisions; i.e., failed CASes
     * while trying to exchange. By nature of the above algorithm, the
     * only kinds of collision that reliably indicate contention are
     * when two attempted releases collide -- one of two attempted
     * offers can legitimately fail to CAS without indicating
     * contention by more than one other thread. (Note: it is possible
     * but not worthwhile to more precisely detect contention by
     * reading slot values after CAS failures.)  When a thread has
     * collided at each slot within the current arena bound, it tries
     * to expand the arena size by one. We track collisions within
     * bounds by using a version (sequence) number on the "bound"
     * field, and conservatively reset collision counts when a
     * participant notices that bound has been updated (in either
     * direction).*竞技场一开始只有一个使用过的插槽。我们扩大
*通过跟踪碰撞有效的竞技场大小;即。失败的情况下
*尝试交换时。根据上述算法的性质
*只有可靠地指示争用的冲突类型是
*当两个试图释放的版本发生冲突时——两个尝试中的一个
*要约可以在没有指明的情况下合法地未能通过核证机关
*由多个其他线程争用。(注:这是可能的
*但不值得更精确地检测争用
*在CAS失败后读取插槽值。)当一根线
*在当前竞技场范围内的每个位置碰撞，它会尝试
*将竞技场规模扩大一倍。我们跟踪内部的碰撞
*在“绑定”上使用版本(序列)号进行绑定
时，保守地重置冲突计数
*参与者注意到绑定已被更新(分别在
*方向)。
     *
     * The effective arena size is reduced (when there is more than
     * one slot) by giving up on waiting after a while and trying to
     * decrement the arena size on expiration. The value of "a while"
     * is an empirical matter.  We implement by piggybacking on the
     * use of spin->yield->block that is essential for reasonable
     * waiting performance anyway -- in a busy exchanger, offers are
     * usually almost immediately released, in which case context
     * switching on multiprocessors is extremely slow/wasteful.  Arena
     * waits just omit the blocking part, and instead cancel. The spin
     * count is empirically chosen to be a value that avoids blocking
     * 99% of the time under maximum sustained exchange rates on a
     * range of test machines. Spins and yields entail some limited
     * randomness (using a cheap xorshift) to avoid regular patterns
     * that can induce unproductive grow/shrink cycles. (Using a
     * pseudorandom also helps regularize spin cycle duration by
     * making branches unpredictable.)  Also, during an offer, a
     * waiter can "know" that it will be released when its slot has
     * changed, but cannot yet proceed until match is set.  In the
     * mean time it cannot cancel the offer, so instead spins/yields.
     * Note: It is possible to avoid this secondary check by changing
     * the linearization point to be a CAS of the match field (as done
     * in one case in the Scott & Scherer DISC paper), which also
     * increases asynchrony a bit, at the expense of poorer collision
     * detection and inability to always reuse per-thread nodes. So
     * the current scheme is typically a better tradeoff.有效竞技场的大小被减少(当超过时)
一段时间后放弃等待并尝试着去做
*在过期时减小竞技场大小。“一段时间”的价值
*是一个经验问题。我们通过使用
*合理使用自旋->产率->块，这是必不可少的
*等待性能——在一个繁忙的交换器中，报价是
*通常几乎立即释放，在这种情况下上下文
打开多处理器非常慢/浪费。竞技场
只需要省略阻塞部分，然后取消。自旋
* count根据经验被选择为一个避免阻塞的值
* 99%的时间在a的最大持续汇率下
*测试机器的范围。自旋和产量都有一定的限制
*避免规则模式的随机性(使用廉价的xorshift)
*这会导致非生产性的增长/收缩周期。(使用
*伪随机还帮助正则化自旋周期持续时间
*使分支不可预测。)同样，在报价时，a
*服务员可以“知道”当它的插槽被打开时，它就会被释放
*已更改，但在设置匹配之前无法继续
*表示时间无法取消报价，因此只能旋转/收益率。
*注意:可以通过更改来避免这种二次检查
*线性化点为匹配字段的ca(如前所述)
*在Scott & Scherer DISC论文中的一个案例中)
增加了一点异步性，代价是减少了碰撞
检测并不能总是重用每个线程节点。所以
当前的方案通常是一个更好的折衷方案。
     *
     * On collisions, indices traverse the arena cyclically in reverse
     * order, restarting at the maximum index (which will tend to be
     * sparsest) when bounds change. (On expirations, indices instead
     * are halved until reaching 0.) It is possible (and has been
     * tried) to use randomized, prime-value-stepped, or double-hash
     * style traversal instead of simple cyclic traversal to reduce
     * bunching.  But empirically, whatever benefits these may have
     * don't overcome their added overhead: We are managing operations
     * that occur very quickly unless there is sustained contention,
     * so simpler/faster control policies work better than more
     * accurate but slower ones.
     *
     * Because we use expiration for arena size control, we cannot
     * throw TimeoutExceptions in the timed version of the public
     * exchange method until the arena size has shrunken to zero (or
     * the arena isn't enabled). This may delay response to timeout
     * but is still within spec.
     *
     * Essentially all of the implementation is in methods
     * slotExchange and arenaExchange. These have similar overall
     * structure, but differ in too many details to combine. The
     * slotExchange method uses the single Exchanger field "slot"
     * rather than arena array elements. However, it still needs
     * minimal collision detection to trigger arena construction.
     * (The messiest part is making sure interrupt status and
     * InterruptedExceptions come out right during transitions when
     * both methods may be called. This is done by using null return
     * as a sentinel to recheck interrupt status.)在碰撞中，指数以相反的周期穿过竞技场
*顺序，在最大索引处重新启动
*稀疏)当边界改变时。(到期时改为指数
*在到达0之前减半。)这是可能的(而且一直是可能的)
尝试)使用随机化、质数步进或双哈希
*减少样式遍历而不是简单的循环遍历
*聚束。但从经验上看，无论这些可能带来什么好处
*不要克服他们额外的开销:我们在管理运营
除非有持续的争论，否则这种情况发生得很快。
因此，更简单/更快的控制策略比更多更好地工作
*准确但速度较慢。
*
*因为我们使用过期来控制竞技场大小，所以不能
*在公共的定时版本中抛出timeoutexception
*交换方法，直到竞技场缩小到0(或)
*竞技场未启用)。这可能会延迟对超时的响应
*但仍在规格范围内。
*
*基本上所有的实现都是用方法实现的
slotExchange和arenaExchange。总体上是相似的
*结构，但差异太多的细节，无法结合。的
*开槽交换方法采用单换热器场“开槽”
而不是竞技场数组元素。然而，它仍然需要
最小碰撞检测触发竞技场建造。
*(最混乱的部分是确保中断状态和
* interruptedexception在转换期间出现
*两个方法都可以调用。这是通过使用null返回来完成的
*用作重新检查中断状态的前哨。)
     *
     * As is too common in this sort of code, methods are monolithic
     * because most of the logic relies on reads of fields that are
     * maintained as local variables so can't be nicely factored --
     * mainly, here, bulky spin->yield->block/cancel code), and
     * heavily dependent on intrinsics (Unsafe) to use inlined
     * embedded CAS and related memory access operations (that tend
     * not to be as readily inlined by dynamic compilers when they are
     * hidden behind other methods that would more nicely name and
     * encapsulate the intended effects). This includes the use of
     * putOrderedX to clear fields of the per-thread Nodes between
     * uses. Note that field Node.item is not declared as volatile
     * even though it is read by releasing threads, because they only
     * do so after CAS operations that must precede access, and all
     * uses by the owning thread are otherwise acceptably ordered by
     * other operations. (Because the actual points of atomicity are
     * slot CASes, it would also be legal for the write to Node.match
     * in a release to be weaker than a full volatile write. However,
     * this is not done because it could allow further postponement of
     * the write, delaying progress.)由于在这类代码中太常见，方法是单块的
*因为大多数逻辑依赖于字段的读取
*维护为局部变量，因此不能很好地分解——
*这里主要是体积较大的spin->yield->block/cancel代码)，以及
严重依赖内部物理(不安全)来使用内联
*嵌入式CAS和相关的内存访问操作(往往
*当动态编译器内联时，不会那么容易
*隐藏在其他方法后面，可以更好地命名和
*封装预期的效果)。这包括使用
* putOrderedX清除之间的每个线程节点的字段
*使用。请注意字段节点。项不声明为易失性
*即使它是通过释放线程读取的，因为它们只是
*在必须先于访问的CAS操作之后执行
*拥有线程的使用可以接受按
*其他操作。(因为原子性的实际点是
*插槽情况下，写入Node.match也是合法的
*在发布版中要弱于完整的易失性写。然而,
*没有这样做，因为这样可以进一步推迟
(写作，拖延进度。)
     */

    /**
     * The byte distance (as a shift value) between any two used slots
     * in the arena.  1 << ASHIFT should be at least cacheline size.竞技场中使用的任何两个槽之间的字节距离(作为移位值)。1 < ASHIFT至少要有cacheline尺寸。
     */
    private static final int ASHIFT = 7;

    /**
     * The maximum supported arena index. The maximum allocatable
     * arena size is MMASK + 1. Must be a power of two minus one, less
     * than (1<<(31-ASHIFT)). The cap of 255 (0xff) more than suffices
     * for the expected scaling limits of the main algorithms.
     * 最大支持的竞技场指数。最大分配竞技场大小是MMASK + 1。必须是2 - 1的幂，小于(1< (31-ASHIFT))。255的上限(0xff)超过了主要算法预期的扩展限制。
     */
    private static final int MMASK = 0xff;

    /**
     * Unit for sequence/version bits of bound field. Each successful
     * change to the bound also adds SEQ.
     * 绑定字段的序列/版本位的单元。对边界的每一个成功的更改都添加了SEQ。
     */
    private static final int SEQ = MMASK + 1;

    /** The number of CPUs, for sizing and spin control 用于大小和自旋控制的cpu数量*/
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The maximum slot index of the arena: The number of slots that
     * can in principle hold all threads without contention, or at
     * most the maximum indexable value.
     * 竞技场的最大槽位索引:原则上可以容纳所有线程而不产生争用的槽位数目，或者最多是可索引的最大值。
     */
    static final int FULL = (NCPU >= (MMASK << 1)) ? MMASK : NCPU >>> 1;

    /**
     * The bound for spins while waiting for a match. The actual
     * number of iterations will on average be about twice this value
     * due to randomization. Note: Spinning is disabled when NCPU==1.
     * 在等待比赛时，这条线旋转。由于随机化，实际的迭代次数平均将是这个值的两倍。注意:当NCPU==1时，旋转被禁用。
     */
    private static final int SPINS = 1 << 10;

    /**
     * Value representing null arguments/returns from public
     * methods. Needed because the API originally didn't disallow null
     * arguments, which it should have.
     * 表示公共方法的空参数/返回值。需要，因为API最初不允许空参数，它应该有空参数。
     */
    private static final Object NULL_ITEM = new Object();

    /**
     * Sentinel value returned by internal exchange methods upon
     * timeout, to avoid need for separate timed versions of these
     * methods.内部交换方法在超时时返回的前哨值，以避免对这些方法的单独定时版本的需要。
     */
    private static final Object TIMED_OUT = new Object();

    /**
     * Nodes hold partially exchanged data, plus other per-thread
     * bookkeeping. Padded via @sun.misc.Contended to reduce memory
     * contention.节点保存部分交换的数据，以及其他每个线程的簿记。垫通过@sun.misc。主张减少内存争用。
     */
    @sun.misc.Contended static final class Node {
        int index;              // Arena index竞技场指数
        int bound;              // Last recorded value of Exchanger.bound最后记录的交换器的值
        int collides;           // Number of CAS failures at current bound当前界的CAS故障数量
        int hash;               // Pseudo-random for spins伪随机的旋转
        Object item;            // This thread's current item这个线程的当前项目
        volatile Object match;  // Item provided by releasing thread释放线程提供的项
        volatile Thread parked; // Set to this thread when parked, else null当停靠时设置此线程，否则为空。
    }

    /** The corresponding thread local class 对应的线程本地类*/
    static final class Participant extends ThreadLocal<Node> {
        public Node initialValue() { return new Node(); }
    }

    /**
     * Per-thread state线程状态
     */
    private final Participant participant;

    /**
     * Elimination array; null until enabled (within slotExchange).
     * Element accesses use emulation of volatile gets and CAS.消除阵列;空直到启用为止(在slotExchange中)。元素访问使用volatile gets和CAS的仿真。
     */
    private volatile Node[] arena;

    /**
     * Slot used until contention detected.直到检测到争用为止。
     */
    private volatile Node slot;

    /**
     * The index of the largest valid arena position, OR'ed with SEQ
     * number in high bits, incremented on each update.  The initial
     * update from 0 to SEQ is used to ensure that the arena array is
     * constructed only once.最大的有效竞技场位置的索引，或以高位的SEQ数字表示的索引，在每次更新时都增加。从0到SEQ的初始更新用于确保只构建了一次竞技场数组。
     */
    private volatile int bound;

    /**
     * Exchange function when arenas enabled. See above for explanation.当竞技场启用时，交换功能。看到上面的解释。
     *
     * @param item the (non-null) item to exchange
     * @param timed true if the wait is timed
     * @param ns if timed, the maximum wait time, else 0L
     * @return the other thread's item; or null if interrupted; or
     * TIMED_OUT if timed and timed out
     */
    private final Object arenaExchange(Object item, boolean timed, long ns) {
        Node[] a = arena;
        Node p = participant.get();
        for (int i = p.index;;) {                      // access slot at i
            int b, m, c; long j;                       // j is raw array offset
            Node q = (Node)U.getObjectVolatile(a, j = (i << ASHIFT) + ABASE);
            if (q != null && U.compareAndSwapObject(a, j, q, null)) {
                Object v = q.item;                     // release
                q.match = item;
                Thread w = q.parked;
                if (w != null)
                    U.unpark(w);
                return v;
            }
            else if (i <= (m = (b = bound) & MMASK) && q == null) {
                p.item = item;                         // offer
                if (U.compareAndSwapObject(a, j, null, p)) {
                    long end = (timed && m == 0) ? System.nanoTime() + ns : 0L;
                    Thread t = Thread.currentThread(); // wait
                    for (int h = p.hash, spins = SPINS;;) {
                        Object v = p.match;
                        if (v != null) {
                            U.putOrderedObject(p, MATCH, null);
                            p.item = null;             // clear for next use
                            p.hash = h;
                            return v;
                        }
                        else if (spins > 0) {
                            h ^= h << 1; h ^= h >>> 3; h ^= h << 10; // xorshift
                            if (h == 0)                // initialize hash
                                h = SPINS | (int)t.getId();
                            else if (h < 0 &&          // approx 50% true
                                     (--spins & ((SPINS >>> 1) - 1)) == 0)
                                Thread.yield();        // two yields per wait
                        }
                        else if (U.getObjectVolatile(a, j) != p)
                            spins = SPINS;       // releaser hasn't set match yet
                        else if (!t.isInterrupted() && m == 0 &&
                                 (!timed ||
                                  (ns = end - System.nanoTime()) > 0L)) {
                            U.putObject(t, BLOCKER, this); // emulate LockSupport
                            p.parked = t;              // minimize window
                            if (U.getObjectVolatile(a, j) == p)
                                U.park(false, ns);
                            p.parked = null;
                            U.putObject(t, BLOCKER, null);
                        }
                        else if (U.getObjectVolatile(a, j) == p &&
                                 U.compareAndSwapObject(a, j, p, null)) {
                            if (m != 0)                // try to shrink
                                U.compareAndSwapInt(this, BOUND, b, b + SEQ - 1);
                            p.item = null;
                            p.hash = h;
                            i = p.index >>>= 1;        // descend
                            if (Thread.interrupted())
                                return null;
                            if (timed && m == 0 && ns <= 0L)
                                return TIMED_OUT;
                            break;                     // expired; restart
                        }
                    }
                }
                else
                    p.item = null;                     // clear offer
            }
            else {
                if (p.bound != b) {                    // stale; reset
                    p.bound = b;
                    p.collides = 0;
                    i = (i != m || m == 0) ? m : m - 1;
                }
                else if ((c = p.collides) < m || m == FULL ||
                         !U.compareAndSwapInt(this, BOUND, b, b + SEQ + 1)) {
                    p.collides = c + 1;
                    i = (i == 0) ? m : i - 1;          // cyclically traverse
                }
                else
                    i = m + 1;                         // grow
                p.index = i;
            }
        }
    }

    /**
     * Exchange function used until arenas enabled. See above for explanation.交换函数，直到竞技场启用为止。看到上面的解释。
     *
     * @param item the item to exchange
     * @param timed true if the wait is timed
     * @param ns if timed, the maximum wait time, else 0L
     * @return the other thread's item; or null if either the arena
     * was enabled or the thread was interrupted before completion; or
     * TIMED_OUT if timed and timed out
     */
    private final Object slotExchange(Object item, boolean timed, long ns) {
        Node p = participant.get();
        Thread t = Thread.currentThread();
        if (t.isInterrupted()) // preserve interrupt status so caller can recheck
            return null;

        for (Node q;;) {
            if ((q = slot) != null) {
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    Object v = q.item;
                    q.match = item;
                    Thread w = q.parked;
                    if (w != null)
                        U.unpark(w);
                    return v;
                }
                // create arena on contention, but continue until slot null
                if (NCPU > 1 && bound == 0 &&
                    U.compareAndSwapInt(this, BOUND, 0, SEQ))
                    arena = new Node[(FULL + 2) << ASHIFT];
            }
            else if (arena != null)
                return null; // caller must reroute to arenaExchange
            else {
                p.item = item;
                if (U.compareAndSwapObject(this, SLOT, null, p))
                    break;
                p.item = null;
            }
        }

        // await release
        int h = p.hash;
        long end = timed ? System.nanoTime() + ns : 0L;
        int spins = (NCPU > 1) ? SPINS : 1;
        Object v;
        while ((v = p.match) == null) {
            if (spins > 0) {
                h ^= h << 1; h ^= h >>> 3; h ^= h << 10;
                if (h == 0)
                    h = SPINS | (int)t.getId();
                else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0)
                    Thread.yield();
            }
            else if (slot != p)
                spins = SPINS;
            else if (!t.isInterrupted() && arena == null &&
                     (!timed || (ns = end - System.nanoTime()) > 0L)) {
                U.putObject(t, BLOCKER, this);
                p.parked = t;
                if (slot == p)
                    U.park(false, ns);
                p.parked = null;
                U.putObject(t, BLOCKER, null);
            }
            else if (U.compareAndSwapObject(this, SLOT, p, null)) {
                v = timed && ns <= 0L && !t.isInterrupted() ? TIMED_OUT : null;
                break;
            }
        }
        U.putOrderedObject(p, MATCH, null);
        p.item = null;
        p.hash = h;
        return v;
    }

    /**
     * Creates a new Exchanger.
     */
    public Exchanger() {
        participant = new Participant();
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@linkplain Thread#interrupt interrupted}),
     * and then transfers the given object to it, receiving its object
     * in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread.  The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of two things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * for the exchange,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @param x the object to exchange
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting
     *         等待另一个线程到达这个交换点(除非当前线程被中断)，然后将给定的对象传输给它，并接收它的对象作为回报。
    如果另一个线程已经在交换点等待，那么为了线程调度目的，它将被恢复，并接收当前线程传入的对象。当前线程立即返回，接收由另一个线程传递给交换的对象。
    如果没有其他线程已经在交换中等待，则出于线程调度的目的禁用当前线程，并处于休眠状态，直到发生以下两种情况之一:
    其他线程进入交换;或
    其他一些线程中断当前线程。
    如果当前线程:
    在进入此方法时设置其中断状态;或
    在等待交换时被中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。
     */
//    阻塞，等待另一个线程到达这个交换点
    @SuppressWarnings("unchecked")
    public V exchange(V x) throws InterruptedException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x; // translate null args
        if ((arena != null ||
             (v = slotExchange(item, false, 0L)) == null) &&
            ((Thread.interrupted() || // disambiguates null return
              (v = arenaExchange(item, false, 0L)) == null)))
            throw new InterruptedException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@linkplain Thread#interrupt interrupted} or
     * the specified waiting time elapses), and then transfers the given
     * object to it, receiving its object in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread.  The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of three things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * for the exchange,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link
     * TimeoutException} is thrown.  If the time is less than or equal
     * to zero, the method will not wait at all.
     *
     * @param x the object to exchange
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting
     * @throws TimeoutException if the specified waiting time elapses
     *         before another thread enters the exchange
     *         等待另一个线程到达这个交换点(除非当前线程被中断或指定的等待时间流逝)，然后将给定的对象转移到它，接收它的对象作为回报。
    如果另一个线程已经在交换点等待，那么为了线程调度目的，它将被恢复，并接收当前线程传入的对象。当前线程立即返回，接收由另一个线程传递给交换的对象。
    如果没有其他线程已经在交换中等待，那么出于线程调度的目的，当前线程将被禁用，并处于休眠状态，直到发生以下三种情况之一:
    其他线程进入交换;或
    其他一些线程中断当前线程;或
    指定的等待时间运行。
    如果当前线程:
    在进入此方法时设置其中断状态;或
    在等待交换时被中断，
    然后抛出InterruptedException，并清除当前线程的中断状态。
    如果指定的等待时间过去，则抛出TimeoutException。如果时间小于或等于零，方法将不再等待。
     */
    @SuppressWarnings("unchecked")
    public V exchange(V x, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x;
        long ns = unit.toNanos(timeout);
        if ((arena != null ||
             (v = slotExchange(item, true, ns)) == null) &&
            ((Thread.interrupted() ||
              (v = arenaExchange(item, true, ns)) == null)))
            throw new InterruptedException();
        if (v == TIMED_OUT)
            throw new TimeoutException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long BOUND;
    private static final long SLOT;
    private static final long MATCH;
    private static final long BLOCKER;
    private static final int ABASE;
    static {
        int s;
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> ek = Exchanger.class;
            Class<?> nk = Node.class;
            Class<?> ak = Node[].class;
            Class<?> tk = Thread.class;
            BOUND = U.objectFieldOffset
                (ek.getDeclaredField("bound"));
            SLOT = U.objectFieldOffset
                (ek.getDeclaredField("slot"));
            MATCH = U.objectFieldOffset
                (nk.getDeclaredField("match"));
            BLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            s = U.arrayIndexScale(ak);
            // ABASE absorbs padding in front of element 0
            ABASE = U.arrayBaseOffset(ak) + (1 << ASHIFT);

        } catch (Exception e) {
            throw new Error(e);
        }
        if ((s & (s-1)) != 0 || s > (1 << ASHIFT))
            throw new Error("Unsupported array scale");
    }

}
