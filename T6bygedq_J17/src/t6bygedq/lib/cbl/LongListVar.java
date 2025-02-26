package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250210
 */
public abstract interface LongListVar extends ListVar {
	
	public abstract void add(long value);
	
	public abstract void rem(int index);
	
	public abstract void set(int index, long value);
	
	public abstract long get(int index);
	
}