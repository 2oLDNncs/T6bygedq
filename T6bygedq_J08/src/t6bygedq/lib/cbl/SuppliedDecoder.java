package t6bygedq.lib.cbl;

import java.util.function.Supplier;

/**
 * @author 2oLDNncs 20250209
 *
 * @param <K>
 * @param <V>
 */
public final class SuppliedDecoder<K, V> implements Decoder<K, V> {
	
	private final Supplier<Decoder<K, V>> decoderSupplier;
	
	public SuppliedDecoder(final Supplier<Decoder<K, V>> decoderSupplier) {
		this.decoderSupplier = decoderSupplier;
	}
	
	@Override
	public final V get(final K key) {
		return this.decoderSupplier.get().get(key);
	}
	
	@Override
	public final K getKey(final V value) {
		return this.decoderSupplier.get().getKey(value);
	}
	
}