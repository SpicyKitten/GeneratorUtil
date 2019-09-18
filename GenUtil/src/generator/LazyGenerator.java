package generator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LazyGenerator<T> implements Iterable<T>
{
	private final Generator<T> generator;
	private final AtomicBoolean hasNext = new AtomicBoolean(true);
	
	public LazyGenerator()
	{
		this.generator = new Generator<T>()
		{
			@Override
			protected T get()
			{
				LazyGenerator.this.get();
				hasNext.set(false);
				return null;
			}
			
			@Override
			public boolean hasNext()
			{
				return hasNext.get() && super.hasNext();
			}
		};
	}
	
	protected void done()
	{
		generator.done();
	}
	
	protected void yield(T t)
	{
		generator.yield(t);
	}
	
	protected abstract void get();

	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			GeneratorValue<T> value = GeneratorValue.of();
			
			@Override
			public synchronized boolean hasNext()
			{
				var trying = generator.hasNext();
				if(!trying)
					return false;
				if(value.missing())
					value = generator.nextIfPresent();
				return hasNext.get() && value.present();
			}

			@Override
			public synchronized T next()
			{
				if(!hasNext())
					throw new NoSuchElementException();
				return value.getAndClear();
			}
		};
	}
}
