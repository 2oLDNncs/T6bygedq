package t6bygedq.lib.cbl;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 2oLDNncs 20250207
 *
 * @param <K>
 * @param <V>
 */
public final class ReversibleMap<K, V> extends AbstractMap<K, V> implements Decoder<K, V>{
	
	private final Map<K, V> forward = new LinkedHashMap<>();
	
	private final Map<V, K> backward = new LinkedHashMap<>();
	
	private final Set<Entry<K, V>> forwardEntries = Collections.unmodifiableSet(this.forward.entrySet());
	
	public final ReversibleMap<K, V> set(final K key, final V value) {
		this.put(key, value);
		
		return this;
	}
	
	@Override
	public final V put(final K key, final V value) {
		checkAbsent(this.forward, key);
		checkAbsent(this.backward, value);
		
		this.forward.put(key, value);
		this.backward.put(value, key);
		
		return null;
	}
	
	@Override
	public final V get(final Object key) {
		if (!this.containsKey(key)) {
			throw new IllegalArgumentException(String.format("Invalid key: %s", key));
		}
		
		return this.forward.get(key);
	}
	
	@Override
	public final K getKey(final V value) {
		if (!this.backward.containsKey(value)) {
			throw new IllegalArgumentException(String.format("Invalid value: %s", value));
		}
		
		return this.backward.get(value);
	}
	
	@Override
	public final Set<Entry<K, V>> entrySet() {
		return this.forwardEntries;
	}
	
	private static final void checkAbsent(final Map<?, ?> map, final Object key) {
		if (map.containsKey(key)) {
			throw new IllegalStateException(String.format("Key already exists: %s", key));
		}
	}
	
}