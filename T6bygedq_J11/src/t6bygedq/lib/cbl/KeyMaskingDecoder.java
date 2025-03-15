package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250209
 *
 * @param <V>
 */
public final class KeyMaskingDecoder<V> implements Decoder<Integer, V> {
	
	private final Decoder<Integer, V> decoder;
	
	private final int keyMask;
	
	public KeyMaskingDecoder(final Decoder<Integer, V> decoder, final int keyMask) {
		this.decoder = decoder;
		this.keyMask = keyMask;
	}
	
	@Override
	public final V get(Integer key) {
		return this.decoder.get(key & this.keyMask);
	}
	
	@Override
	public final Integer getKey(final V value) {
		return this.decoder.getKey(value);
	}
	
}