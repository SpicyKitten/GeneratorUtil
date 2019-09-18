package generator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class EagerGenerator<T> implements Iterable<T>
{
	private final Generator<T> generator;
	private boolean hasNext = true;
	
	public EagerGenerator()
	{
		this.generator = new Generator<T>()
		{
			@Override
			protected T get()
			{
				EagerGenerator.this.get();
				hasNext = false;
				return null;
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
			GeneratorValue<T> next = generator.nextIfPresent();
			
			@Override
			public synchronized boolean hasNext()
			{
				return hasNext && next.present();
			}
			
			@Override
			public synchronized T next()
			{
				if(!hasNext())
					throw new NoSuchElementException();
				var curr = next;
				next = generator.nextIfPresent();
				return curr.value();
			}
		};
	}
	
}
