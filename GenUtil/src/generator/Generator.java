package generator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import throwing.Throwing;
import throwing.ThrowingRunnable;

/**
 * A generator of elements of type {@link T} akin to a Python generator
 * 
 * @author ratha
 *
 * @param <T>
 *            The type of elements produced by this Generator
 */
public abstract class Generator<T> implements Iterator<T>
{
	/**
	 * The currently yielded value
	 */
	private T curr;
	/**
	 * The thread storing the state of this generator
	 */
	private Thread execution;
	/**
	 * Whether {@link #execution execution} has begun
	 */
	private AtomicBoolean executing = new AtomicBoolean(false);
	/**
	 * Marks the beginning of generator execution (until {@link #yield})
	 */
	private CyclicBarrier begin = new CyclicBarrier(2);
	/**
	 * Marks the end of generator execution (at {@link #yield})
	 */
	private CyclicBarrier end = new CyclicBarrier(2);
	/**
	 * An {@link Optional} representing the action taken when the generator
	 * stops producing elements
	 */
	private Optional<Runnable> onTermination = Optional.empty();
	/**
	 * An action to be applied to each remaining action of the generator
	 */
	private Consumer<? super T> forEachAction = null;
	
	public Generator()
	{
		execution = new Thread(() ->
		{
			try
			{
				begin.await();
				T t = get();
				onTermination = Optional.of(ThrowingRunnable.of(execution::join));
				yield(t);
			}
			catch(Exception unexpected)
			{
				onTermination = Optional.of(ThrowingRunnable.of(() ->
				{
					Exception e = new NoSuchElementException("Failed to retrieve element!");
					e.initCause(unexpected);
					throw e;
				}));
			}
			finally
			{
				Throwing.run(end::await);
			}
		});
		execution.setDaemon(true);
	}
	
	/**
	 * Returns {@code true} if the generator has more elements. (In other words,
	 * returns {@code true} if {@link #next} would return an element rather than
	 * throwing an exception.)
	 *
	 * @return {@code true} if the generator has more elements
	 */
	@Override
	public final boolean hasNext()
	{
		return onTermination.isEmpty();
	}
	
	/**
	 * Returns an {@link Optional} representing the next element in the
	 * generator, if present
	 *
	 * @return an {@link Optional} representing the next element in the
	 *         generator if present, else {@link Optional#empty}
	 */
	public synchronized final Optional<T> nextIfPresent()
	{
		return hasNext() ? Optional.ofNullable(next()) : Optional.empty();
	}
	
	/**
	 * Returns the next element in the generator.
	 *
	 * @return the next element in the generator
	 * @throws NoSuchElementException
	 *             if the generator has no more elements
	 */
	@Override
	public synchronized final T next()
	{
		if(!hasNext())
			throw new NoSuchElementException(
				"There are no more elements to be generated by this Generator!");
		if(executing.compareAndSet(false, true))
			execution.start();
		Throwing.run(begin::await, end::await);
		onTermination.ifPresent(Runnable::run);
		return curr;
	}
	
	/**
	 * Yield an element from this generator
	 * 
	 * @param elem
	 *            The element yielded from this generator
	 */
	protected final void yield(T elem)
	{
		if(forEachAction != null)
		{
			forEachAction.accept(elem);
			return;
		}
		curr = elem;
		Throwing.run(end::await, begin::await);
	}
	
	/**
	 * Consumes all remaining elements possible. Obviously, don't use on
	 * infinite Generators.
	 * 
	 * @param action
	 *            The action to perform on all remaining elements
	 * @throws NoSuchElementException
	 *             if an exception is thrown within {@link #get} or
	 *             {@link action}
	 */
	@Override
	public synchronized void forEachRemaining(Consumer<? super T> action)
	{
		Objects.requireNonNull(action);
		if(!hasNext())
			throw new IllegalStateException("Exhausted elements before calling forEach!");
		forEachAction = action;
		Throwing.run(begin::await, end::await);
		onTermination.ifPresent(Runnable::run);
	}
	
	/**
	 * The method encoding the logic of this generator. Calls
	 * {@link #yield(Object)} as necessary and uses return appropriately.
	 * 
	 * @return the last element in the generator
	 */
	protected abstract T get();
}