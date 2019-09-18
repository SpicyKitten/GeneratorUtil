package example;

import generator.EagerGenerator;

public class GeneratorUse
{
	public static void main(String[] args) throws InterruptedException
	{
		EagerGenerator<Integer> g = new EagerGenerator<>()
		{
			@Override
			protected void get()
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
					System.out.println("1: "+g.iterator().next());
				}
			}
		});
		Thread cons2 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for(int j = 0; j < 20; j++)
				{
					System.out.println("2: "+g.iterator().next());
				}
				g.forEach(i ->
				{
					System.out.println("2 to 60: " + i);
					if(i >= 600)
						throw new IllegalStateException("termination time!");
				});
			}
		});
		cons1.start();
		cons2.start();
		cons1.join();
		cons2.join();
		
	}
}
