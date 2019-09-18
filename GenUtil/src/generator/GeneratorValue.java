package generator;

/**
 * Not an Optional - Optionals only know if they may contain a null.
 * GeneratorValues may contain a null, yet still be present.
 */
public class GeneratorValue<T>
{
	private Presence presence = Presence.PRESENT;
	private T value = null;
	
	private GeneratorValue(Presence presence)
	{
		this.presence = presence;
	}
	
	private GeneratorValue(T value)
	{
		this.value = value;
	}
	
	public static <T> GeneratorValue<T> of()
	{
		return new GeneratorValue<>(Presence.MISSING);
	}
	
	public static <T> GeneratorValue<T> of(T value)
	{
		return new GeneratorValue<T>(value);
	}
	
	public boolean present()
	{
		return this.presence == Presence.PRESENT;
	}
	
	public boolean missing()
	{
		return this.presence == Presence.MISSING;
	}
	
	public T value()
	{
		return this.value;
	}
	
	public T getAndClear()
	{
		this.presence = Presence.MISSING;
		return value;
	}
	
	public String toString()
	{
		return String.format("GeneratorValue[%s]",
			missing() ? "MISSING" : value == null ? "null" : value.toString());
	}
	
	private enum Presence
	{
		PRESENT, MISSING;
	}
}
