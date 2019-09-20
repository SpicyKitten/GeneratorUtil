package generator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class LazyGenerator<T> implements Iterable<T>
{
	private final Generator<T> generator;
	private final Iterator<T> iterator;
	private volatile boolean hasNext = true;
	
	public LazyGenerator()
	{
		this.generator = new Generator<T>()
		{
			@Override
			protected T get()
			{
				LazyGenerator.this.get();
				hasNext = false;
				return null;
			}
			
			@Override
			public boolean hasNext()
			{
				return hasNext && super.hasNext();
			}
		};
		
		this.iterator = new Iterator<T>()
		{
			private GeneratorValue<T> value = GeneratorValue.empty();
			
			@Override
			public boolean hasNext()
			{
				if(!generator.hasNext())
					return false;
				boolean ret;
				synchronized(this)
				{
					if(value.missing())
						value = generator.nextIfPresent();
					ret = hasNext && value.present();
				}
				return ret;
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
	
	protected void yield(T t)
	{
		generator.yield(t);
	}
	
	protected abstract void get();

	@Override
	public Iterator<T> iterator()
	{
		return iterator;
	}
}
