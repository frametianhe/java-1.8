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

package java.util.concurrent;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.concurrent.Executor;

/**
 * A stage of a possibly asynchronous computation, that performs an
 * action or computes a value when another CompletionStage completes.
 * A stage completes upon termination of its computation, but this may
 * in turn trigger other dependent stages.  The functionality defined
 * in this interface takes only a few basic forms, which expand out to
 * a larger set of methods to capture a range of usage styles: <ul>
 *
 * <li>The computation performed by a stage may be expressed as a
 * Function, Consumer, or Runnable (using methods with names including
 * <em>apply</em>, <em>accept</em>, or <em>run</em>, respectively)
 * depending on whether it requires arguments and/or produces results.
 * For example, {@code stage.thenApply(x -> square(x)).thenAccept(x ->
 * System.out.print(x)).thenRun(() -> System.out.println())}. An
 * additional form (<em>compose</em>) applies functions of stages
 * themselves, rather than their results. </li>
 *
 * <li> One stage's execution may be triggered by completion of a
 * single stage, or both of two stages, or either of two stages.
 * Dependencies on a single stage are arranged using methods with
 * prefix <em>then</em>. Those triggered by completion of
 * <em>both</em> of two stages may <em>combine</em> their results or
 * effects, using correspondingly named methods. Those triggered by
 * <em>either</em> of two stages make no guarantees about which of the
 * results or effects are used for the dependent stage's
 * computation.</li>
 *
 * <li> Dependencies among stages control the triggering of
 * computations, but do not otherwise guarantee any particular
 * ordering. Additionally, execution of a new stage's computations may
 * be arranged in any of three ways: default execution, default
 * asynchronous execution (using methods with suffix <em>async</em>
 * that employ the stage's default asynchronous execution facility),
 * or custom (via a supplied {@link Executor}).  The execution
 * properties of default and async modes are specified by
 * CompletionStage implementations, not this interface. Methods with
 * explicit Executor arguments may have arbitrary execution
 * properties, and might not even support concurrent execution, but
 * are arranged for processing in a way that accommodates asynchrony.
 *
 * <li> Two method forms support processing whether the triggering
 * stage completed normally or exceptionally: Method {@link
 * #whenComplete whenComplete} allows injection of an action
 * regardless of outcome, otherwise preserving the outcome in its
 * completion. Method {@link #handle handle} additionally allows the
 * stage to compute a replacement result that may enable further
 * processing by other dependent stages.  In all other cases, if a
 * stage's computation terminates abruptly with an (unchecked)
 * exception or error, then all dependent stages requiring its
 * completion complete exceptionally as well, with a {@link
 * CompletionException} holding the exception as its cause.  If a
 * stage is dependent on <em>both</em> of two stages, and both
 * complete exceptionally, then the CompletionException may correspond
 * to either one of these exceptions.  If a stage is dependent on
 * <em>either</em> of two others, and only one of them completes
 * exceptionally, no guarantees are made about whether the dependent
 * stage completes normally or exceptionally. In the case of method
 * {@code whenComplete}, when the supplied action itself encounters an
 * exception, then the stage exceptionally completes with this
 * exception if not already completed exceptionally.</li>
 *
 * </ul>
 *
 * <p>All methods adhere to the above triggering, execution, and
 * exceptional completion specifications (which are not repeated in
 * individual method specifications). Additionally, while arguments
 * used to pass a completion result (that is, for parameters of type
 * {@code T}) for methods accepting them may be null, passing a null
 * value for any other parameter will result in a {@link
 * NullPointerException} being thrown.
 *
 * <p>This interface does not define methods for initially creating,
 * forcibly completing normally or exceptionally, probing completion
 * status or results, or awaiting completion of a stage.
 * Implementations of CompletionStage may provide means of achieving
 * such effects, as appropriate.  Method {@link #toCompletableFuture}
 * enables interoperability among different implementations of this
 * interface by providing a common conversion type.
 *
 * @author Doug Lea
 * @since 1.8
 * 一种可能的异步计算阶段，当另一个完成阶段时执行一个操作或计算一个值。一个阶段在它的计算结束时完成，但是这可能反过来触发其他相关阶段。在这个界面中定义的功能只需要一些基本的表单，扩展到一组更大的方法来捕获一系列的使用样式:
由阶段执行的计算可以表示为函数、使用者或可运行的(使用名称分别包括apply、accept或run的方法)，这取决于它是否需要参数和/或生成结果。例如,舞台。thenApply(x - >平方(x))。然后接受(x -> System.out.print(x) . thenrun () -> system . outprintln())。附加的表单(组合)应用阶段本身的功能，而不是它们的结果。
一个阶段的执行可以由单个阶段的完成触发，也可以是两个阶段的同时完成，也可以是两个阶段中的任何一个。使用带前缀的方法来安排对单个阶段的依赖。这两个阶段的完成所触发的，可以结合它们的结果或效果，使用相应的命名方法。由这两个阶段中的任何一个触发的，不能保证哪个结果或效果用于相关阶段的计算。
阶段之间的依赖关系控制了计算的触发，但是不保证任何特定的顺序。此外，新阶段计算的执行可以通过以下三种方式进行:默认执行、默认异步执行(使用带有使用该阶段默认异步执行功能的后缀异步的方法)或自定义(通过提供的执行程序)。默认和异步模式的执行属性是由CompletionStage实现指定的，而不是这个接口。具有显式执行器参数的方法可能具有任意的执行属性，甚至可能不支持并发执行，但以适合异步的方式安排处理。
两种方法表单支持处理触发阶段是否正常完成或异常完成:方法完成时允许不考虑结果而注入操作，否则在完成时保留结果。方法句柄还允许阶段计算替换结果，从而允许其他相关阶段进行进一步处理。在所有其他情况下，如果一个阶段的计算突然终止(未检查的)异常或错误，那么所有依赖的阶段都需要完成异常的完成，而一个CompletionException将异常作为其原因。如果一个阶段同时依赖于两个阶段，并且两个阶段都是完全异常的，那么CompletionException可能对应于其中一个异常。如果一个阶段依赖于另外两个阶段中的任何一个，并且其中只有一个完成异常，那么就不能保证相关阶段是正常完成还是异常完成。在方法完成时的情况下，当提供的操作本身遇到异常时，如果没有异常完成，则阶段异常完成。
所有方法都遵循上述触发、执行和特殊的完成规范(在单独的方法规范中不重复)。此外，虽然用于传递完成结果的参数(即T类型的参数)可能是null，但传递任何其他参数的空值将导致抛出NullPointerException。
此接口不定义用于初始创建、强制正常完成或异常完成、探测完成状态或结果或等待阶段完成的方法。完成阶段的实现可以在适当的情况下提供实现这些效果的手段。方法toCompletableFuture通过提供一个通用的转换类型来实现这个接口的不同实现之间的互操作性。
 */
public interface CompletionStage<T> {

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed with this stage's result as the argument
     * to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，该stage在此阶段正常完成时以该阶段的结果作为提供函数的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param <U> the function's return type
     * @return the new CompletionStage
     */
    public <U> CompletionStage<U> thenApply(Function<? super T,? extends U> fn);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using this stage's default asynchronous
     * execution facility, with this stage's result as the argument to
     * the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当该阶段正常完成时，该阶段使用该阶段的默认异步执行工具执行，该阶段的结果作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param <U> the function's return type
     * @return the new CompletionStage
     */
    public <U> CompletionStage<U> thenApplyAsync
        (Function<? super T,? extends U> fn);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using the supplied Executor, with this
     * stage's result as the argument to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，该stage在此阶段正常完成时使用提供的Executor执行，该阶段的结果作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @param <U> the function's return type
     * @return the new CompletionStage
     */
    public <U> CompletionStage<U> thenApplyAsync
        (Function<? super T,? extends U> fn,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed with this stage's result as the argument
     * to the supplied action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的完成阶段，当这个阶段正常完成时，该阶段的结果作为提供的操作的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     */
    public CompletionStage<Void> thenAccept(Consumer<? super T> action);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using this stage's default asynchronous
     * execution facility, with this stage's result as the argument to
     * the supplied action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当该阶段正常完成时，该阶段使用该阶段的默认异步执行工具执行，该阶段的结果作为提供操作的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     */
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using the supplied Executor, with this
     * stage's result as the argument to the supplied action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个阶段正常完成时，将使用所提供的Executor执行，并将此阶段的结果作为提供的操作的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,
                                                 Executor executor);
    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, executes the given action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的完成阶段，当这个阶段正常完成时，执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     */
    public CompletionStage<Void> thenRun(Runnable action);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, executes the given action using this stage's default
     * asynchronous execution facility.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个阶段正常完成时，使用这个阶段的默认异步执行工具执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     */
    public CompletionStage<Void> thenRunAsync(Runnable action);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, executes the given action using the supplied Executor.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的完成阶段，当这个阶段正常完成时，使用提供的执行程序执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    public CompletionStage<Void> thenRunAsync(Runnable action,
                                              Executor executor);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage both complete normally, is executed with the two
     * results as arguments to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个阶段和另一个给定阶段都正常完成时，该阶段以两个结果作为所提供函数的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param <U> the type of the other CompletionStage's result
     * @param <V> the function's return type
     * @return the new CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombine
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using this stage's
     * default asynchronous execution facility, with the two results
     * as arguments to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个阶段和另一个给定阶段正常完成时，该阶段使用这个阶段的默认异步执行工具执行，这两个结果作为所提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param <U> the type of the other CompletionStage's result
     * @param <V> the function's return type
     * @return the new CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombineAsync
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using the supplied
     * executor, with the two results as arguments to the supplied
     * function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个阶段和另一个给定阶段正常完成时，使用提供的executor执行，两个结果作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @param <U> the type of the other CompletionStage's result
     * @param <V> the function's return type
     * @return the new CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombineAsync
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage both complete normally, is executed with the two
     * results as arguments to the supplied action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个阶段和另一个给定阶段都正常完成时，该阶段以两个结果作为所提供操作的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param <U> the type of the other CompletionStage's result
     * @return the new CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBoth
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using this stage's
     * default asynchronous execution facility, with the two results
     * as arguments to the supplied action.返回一个新的CompletionStage，当这个阶段和另一个给定阶段正常完成时，该阶段使用这个阶段的默认异步执行工具执行，这两个结果作为所提供操作的参数。
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param <U> the type of the other CompletionStage's result
     * @return the new CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBothAsync
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using the supplied
     * executor, with the two results as arguments to the supplied
     * function.返回一个新的CompletionStage，当这个阶段和另一个给定阶段正常完成时，使用提供的executor执行，两个结果作为提供函数的参数。
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @param <U> the type of the other CompletionStage's result
     * @return the new CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBothAsync
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage both complete normally, executes the given action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的完成阶段，当这个阶段和另一个给定阶段都正常完成时，执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     */
    public CompletionStage<Void> runAfterBoth(CompletionStage<?> other,
                                              Runnable action);
    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, executes the given action using
     * this stage's default asynchronous execution facility.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个和另一个给定的stage正常完成时，该stage使用这个stage的默认异步执行工具执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     */
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,
                                                   Runnable action);

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, executes the given action using
     * the supplied executor.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个和另一个给定阶段通常完成时，使用提供的执行器执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,
                                                   Runnable action,
                                                   Executor executor);
    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed with the
     * corresponding result as argument to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个或另一个给定的stage正常完成时，该stage将以相应的结果作为所提供函数的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param <U> the function's return type
     * @return the new CompletionStage
     */
    public <U> CompletionStage<U> applyToEither
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using this
     * stage's default asynchronous execution facility, with the
     * corresponding result as argument to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，当这个或另一个给定阶段正常完成时，将使用这个阶段的缺省异步执行工具执行，并将相应的结果作为参数提供给所提供的函数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param <U> the function's return type
     * @return the new CompletionStage
     */
    public <U> CompletionStage<U> applyToEitherAsync
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using the
     * supplied executor, with the corresponding result as argument to
     * the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.返回一个新的CompletionStage，该stage在这个或另一个给定的stage正常完成时，使用提供的executor执行，并将相应的结果作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     *
     * @param other the other CompletionStage
     * @param fn the function to use to compute the value of
     * the returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @param <U> the function's return type
     * @return the new CompletionStage
     */
    public <U> CompletionStage<U> applyToEitherAsync
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed with the
     * corresponding result as argument to the supplied action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，当这个或另一个给定的stage正常完成时，该stage将以相应的结果作为所提供操作的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public CompletionStage<Void> acceptEither
        (CompletionStage<? extends T> other,
         Consumer<? super T> action);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using this
     * stage's default asynchronous execution facility, with the
     * corresponding result as argument to the supplied action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，当这个或另一个给定的stage正常完成时，该stage将以相应的结果作为所提供操作的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public CompletionStage<Void> acceptEitherAsync
        (CompletionStage<? extends T> other,
         Consumer<? super T> action);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using the
     * supplied executor, with the corresponding result as argument to
     * the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，该stage在这个或另一个给定的stage正常完成时，使用提供的executor执行，并将相应的结果作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public CompletionStage<Void> acceptEitherAsync
        (CompletionStage<? extends T> other,
         Consumer<? super T> action,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, executes the given action.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     * 返回一个新的完成阶段，当这个或另一个给定阶段正常完成时，执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public CompletionStage<Void> runAfterEither(CompletionStage<?> other,
                                                Runnable action);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, executes the given action
     * using this stage's default asynchronous execution facility.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，当这个或另一个给定的stage正常完成时，该stage使用这个stage的默认异步执行功能执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public CompletionStage<Void> runAfterEitherAsync
        (CompletionStage<?> other,
         Runnable action);

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, executes the given action
     * using the supplied executor.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param action the action to perform before completing the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，该stage在这个或另一个给定阶段正常完成时，使用提供的executor执行给定的操作。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public CompletionStage<Void> runAfterEitherAsync
        (CompletionStage<?> other,
         Runnable action,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed with this stage as the argument
     * to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function returning a new CompletionStage
     * @param <U> the type of the returned CompletionStage's result
     * @return the CompletionStage
     * 返回一个新的CompletionStage，该stage在此阶段正常完成时作为提供函数的参数执行。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public <U> CompletionStage<U> thenCompose
        (Function<? super T, ? extends CompletionStage<U>> fn);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using this stage's default asynchronous
     * execution facility, with this stage as the argument to the
     * supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function returning a new CompletionStage
     * @param <U> the type of the returned CompletionStage's result
     * @return the CompletionStage
     * 返回一个新的CompletionStage，当这个阶段正常完成时，将使用这个阶段的默认异步执行工具执行这个阶段，这个阶段作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public <U> CompletionStage<U> thenComposeAsync
        (Function<? super T, ? extends CompletionStage<U>> fn);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using the supplied Executor, with this
     * stage's result as the argument to the supplied function.
     *
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function returning a new CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @param <U> the type of the returned CompletionStage's result
     * @return the CompletionStage
     * 返回一个新的CompletionStage，该stage在此阶段正常完成时使用提供的Executor执行，该阶段的结果作为提供函数的参数。请参阅完整阶段文档，以了解异常完成的规则。
     */
    public <U> CompletionStage<U> thenComposeAsync
        (Function<? super T, ? extends CompletionStage<U>> fn,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * exceptionally, is executed with this stage's exception as the
     * argument to the supplied function.  Otherwise, if this stage
     * completes normally, then the returned stage also completes
     * normally with the same value.
     *
     * @param fn the function to use to compute the value of the
     * returned CompletionStage if this CompletionStage completed
     * exceptionally
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，该stage在此阶段异常完成时执行，该阶段的异常作为提供函数的参数。否则，如果这个阶段正常完成，那么返回的阶段也会以相同的值正常完成。
     */
    public CompletionStage<T> exceptionally
        (Function<Throwable, ? extends T> fn);

    /**
     * Returns a new CompletionStage with the same result or exception as
     * this stage, that executes the given action when this stage completes.
     *
     * <p>When this stage is complete, the given action is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null}
     * if none) of this stage as arguments.  The returned stage is completed
     * when the action returns.  If the supplied action itself encounters an
     * exception, then the returned stage exceptionally completes with this
     * exception unless this stage also completed exceptionally.
     *
     * @param action the action to perform
     * @return the new CompletionStage
     * 返回与此阶段相同的结果或异常的新完成阶段，该阶段在此阶段完成时执行给定的操作。
    当这个阶段完成时，给定的操作将被调用，该阶段的结果(如果没有)和异常(如果没有)作为参数。返回的阶段在操作返回时完成。如果提供的操作本身遇到一个异常，那么返回的阶段会异常地完成这个异常，除非这个阶段也异常地完成。
     */
    public CompletionStage<T> whenComplete
        (BiConsumer<? super T, ? super Throwable> action);

    /**
     * Returns a new CompletionStage with the same result or exception as
     * this stage, that executes the given action using this stage's
     * default asynchronous execution facility when this stage completes.
     *
     * <p>When this stage is complete, the given action is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null}
     * if none) of this stage as arguments.  The returned stage is completed
     * when the action returns.  If the supplied action itself encounters an
     * exception, then the returned stage exceptionally completes with this
     * exception unless this stage also completed exceptionally.
     *
     * @param action the action to perform
     * @return the new CompletionStage
     * 返回与此阶段相同的结果或异常的新的CompletionStage，该阶段在此阶段完成时使用该阶段的默认异步执行工具执行给定的操作。
    当这个阶段完成时，给定的操作将被调用，该阶段的结果(如果没有)和异常(如果没有)作为参数。返回的阶段在操作返回时完成。如果提供的操作本身遇到一个异常，那么返回的阶段会异常地完成这个异常，除非这个阶段也异常地完成。
     */
    public CompletionStage<T> whenCompleteAsync
        (BiConsumer<? super T, ? super Throwable> action);

    /**
     * Returns a new CompletionStage with the same result or exception as
     * this stage, that executes the given action using the supplied
     * Executor when this stage completes.
     *
     * <p>When this stage is complete, the given action is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null}
     * if none) of this stage as arguments.  The returned stage is completed
     * when the action returns.  If the supplied action itself encounters an
     * exception, then the returned stage exceptionally completes with this
     * exception unless this stage also completed exceptionally.
     *
     * @param action the action to perform
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     * 返回与此阶段相同的结果或异常的新完成阶段，该阶段在此阶段完成时使用提供的执行程序执行给定的操作。
    当这个阶段完成时，给定的操作将被调用，该阶段的结果(如果没有)和异常(如果没有)作为参数。返回的阶段在操作返回时完成。如果提供的操作本身遇到一个异常，那么返回的阶段会异常地完成这个异常，除非这个阶段也异常地完成。
     */
    public CompletionStage<T> whenCompleteAsync
        (BiConsumer<? super T, ? super Throwable> action,
         Executor executor);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * either normally or exceptionally, is executed with this stage's
     * result and exception as arguments to the supplied function.
     *
     * <p>When this stage is complete, the given function is invoked
     * with the result (or {@code null} if none) and the exception (or
     * {@code null} if none) of this stage as arguments, and the
     * function's result is used to complete the returned stage.
     *
     * @param fn the function to use to compute the value of the
     * returned CompletionStage
     * @param <U> the function's return type
     * @return the new CompletionStage
     *
    返回一个新的CompletionStage，该stage在此阶段正常或异常完成时执行，该阶段的结果和异常作为所提供函数的参数。
    当这个阶段完成时，将使用结果调用给定的函数(如果没有的话)和这个阶段的异常(或者如果没有)作为参数，并使用函数的结果来完成返回的阶段。
     */
    public <U> CompletionStage<U> handle
        (BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * either normally or exceptionally, is executed using this stage's
     * default asynchronous execution facility, with this stage's
     * result and exception as arguments to the supplied function.
     *
     * <p>When this stage is complete, the given function is invoked
     * with the result (or {@code null} if none) and the exception (or
     * {@code null} if none) of this stage as arguments, and the
     * function's result is used to complete the returned stage.
     *
     * @param fn the function to use to compute the value of the
     * returned CompletionStage
     * @param <U> the function's return type
     * @return the new CompletionStage
     * 返回一个新的CompletionStage，当这个阶段正常或异常地完成时，将使用这个阶段的默认异步执行工具执行，并将此阶段的结果和异常作为参数提供给提供的函数。
    当这个阶段完成时，将使用结果调用给定的函数(如果没有的话)和这个阶段的异常(或者如果没有)作为参数，并使用函数的结果来完成返回的阶段。
     */
    public <U> CompletionStage<U> handleAsync
        (BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * Returns a new CompletionStage that, when this stage completes
     * either normally or exceptionally, is executed using the
     * supplied executor, with this stage's result and exception as
     * arguments to the supplied function.
     *
     * <p>When this stage is complete, the given function is invoked
     * with the result (or {@code null} if none) and the exception (or
     * {@code null} if none) of this stage as arguments, and the
     * function's result is used to complete the returned stage.
     *
     * @param fn the function to use to compute the value of the
     * returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @param <U> the function's return type
     * @return the new CompletionStage
     *
    返回一个新的CompletionStage，当此阶段完成正常或异常时，将使用提供的executor执行此阶段的结果和异常作为提供函数的参数。
    当这个阶段完成时，将使用结果调用给定的函数(如果没有的话)和这个阶段的异常(或者如果没有)作为参数，并使用函数的结果来完成返回的阶段。
     */
    public <U> CompletionStage<U> handleAsync
        (BiFunction<? super T, Throwable, ? extends U> fn,
         Executor executor);

    /**
     * Returns a {@link CompletableFuture} maintaining the same
     * completion properties as this stage. If this stage is already a
     * CompletableFuture, this method may return this stage itself.
     * Otherwise, invocation of this method may be equivalent in
     * effect to {@code thenApply(x -> x)}, but returning an instance
     * of type {@code CompletableFuture}. A CompletionStage
     * implementation that does not choose to interoperate with others
     * may throw {@code UnsupportedOperationException}.
     *
     * @return the CompletableFuture
     * @throws UnsupportedOperationException if this implementation
     * does not interoperate with CompletableFuture
     * 返回与此阶段保持相同的完成属性的完整未来。如果这个阶段已经是一个完整的未来，这个方法可能会返回这个阶段本身。否则，该方法的调用实际上可能与thenApply(x -> x)等效，但返回类型为CompletableFuture的实例。不选择与他人互操作的完成阶段实现可能会抛出UnsupportedOperationException。
     */
    public CompletableFuture<T> toCompletableFuture();

}
