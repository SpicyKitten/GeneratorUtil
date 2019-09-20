package example;

import generator.LazyGenerator;

public class GeneratorUse
{
	public static void main(String[] args) throws InterruptedException
	{
		LazyGenerator<Integer> gen = new LazyGenerator<>()
		{
			@Override
			protected void get()
			{
				int n = 0;
				while(n < 601)
				{
					yield(n);
					n++;
				}
				System.out.println("Executed");
			}
		};
		var g = gen.iterator();
		Thread cons1 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for(int j = 0; j < 300; j++)
				{
					System.out.println("1: "+g.next());
				}
			}
		});
		Thread cons2 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for(int j = 0; j < 300; j++)
				{
					System.out.println("2: "+g.next());
				}
//				while(g.hasNext())
//				{
//					System.out.println("2: "+g.nextIfPresent().getAndClear());
//				}
			}
		});
		cons1.start();
		cons2.start();
		System.out.println("GEN~: "+gen.iterator().next());
		cons1.join();
		cons2.join();
		System.out.println(gen.iterator().hasNext());
	}
}
