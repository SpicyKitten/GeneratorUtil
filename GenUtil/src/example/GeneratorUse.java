package example;

import generator.Generator;

public class GeneratorUse
{
	public static void main(String[] args) throws InterruptedException
	{
		Generator<Integer> g = new Generator<>()
		{
			@Override
			protected Integer get()
			{
				int n = 0;
				while(true)
				{
					yield(n);
					n++;
				}
			}
		};
		Thread cons1 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for(int j = 0; j < 20; j++)
				{
					g.nextIfPresent().ifPresent(i -> System.out.println("Consumer 1: " + i));
				}
			}
		}, "Consumer 1");
		Thread cons2 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for(int j = 0; j < 20; j++)
				{
					g.nextIfPresent().ifPresent(i -> System.out.println("Consumer 2: " + i));
				}
				g.forEachRemaining(i ->
				{
					System.out.println("Consumer 2: " + i);
					if(i >= 60)
						throw new IllegalStateException("termination time!");
				});
			}
		}, "Consumer 2");
		cons1.start();
		cons2.start();
		cons1.join();
		cons2.join();
		
		g.nextIfPresent().ifPresentOrElse(System.out::println,
			() -> System.out.println("No elements left!"));
	}
}
