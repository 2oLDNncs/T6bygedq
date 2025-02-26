package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250209
 *
 * @param <K>
 * @param <V>
 */
public abstract interface Decoder<K, V> {
	
	public abstract V get(K key);
	
	public abstract K getKey(V value);
	
}