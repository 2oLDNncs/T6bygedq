package t6bygedq.lib;

import java.util.stream.IntStream;

/**
 * @author 2oLDNncs 20251022
 */
public final class CaseInsensitiveCharSequence implements CharSequence {
	
	private final String string;
	private final String lowerCase;
	private final int hashCode;
	
	public CaseInsensitiveCharSequence(final String string) {
		this.string = string;
		this.lowerCase = this.string.toLowerCase();
		this.hashCode = this.lowerCase.hashCode();
	}
	
	@Override
	public final boolean isEmpty() {
		return this.string.isEmpty();
	}
	
	@Override
	public final IntStream chars() {
		return this.string.chars();
	}
	
	@Override
	public final IntStream codePoints() {
		return this.string.codePoints();
	}
	
	@Override
	public final int hashCode() {
		return this.hashCode;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		
		final var that = CaseInsensitiveCharSequence.class.cast(obj);
		
		return null != that && this.lowerCase.equals(that.lowerCase);
	}
	
	@Override
	public final String toString() {
		return this.string;
	}
	
	@Override
	public final CharSequence subSequence(final int start, final int end) {
		return this.string.subSequence(start, end);
	}
	
	@Override
	public final int length() {
		return this.string.length();
	}
	
	@Override
	public final char charAt(final int index) {
		return this.string.charAt(index);
	}
	
}