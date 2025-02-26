package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250208
 *
 * @param <E>
 */
public abstract interface EnumVar<E extends Enum<E>> extends Var {
	
	public abstract void set(E value);
	
	public abstract E get();
	
}