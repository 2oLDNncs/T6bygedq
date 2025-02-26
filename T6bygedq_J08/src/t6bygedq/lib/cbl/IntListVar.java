package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250210
 */
public abstract interface IntListVar extends ListVar {
	
	public abstract void add(int value);
	
	public abstract void rem(int index);
	
	public abstract void set(int index, int value);
	
	public abstract int get(int index);
	
}