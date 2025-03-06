package t6bygedq.lib;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author 2oLDNncs 20241228
 */
public final class Helpers {
	
	@SuppressWarnings("unchecked")
	public static final <T> T cast(final Object object) {
		return (T) object;
	}
	
	public static final <E> E last(final E[] elements) {
		return elements[elements.length - 1];
	}
	
	public static final <E> E last(final List<E> elements) {
		return elements.get(elements.size() - 1);
	}
	
	public static final String substring(final String s, final int start) {
		return s.substring(start < 0 ? (s.length() + start) : start);
	}
	
	public static final String replaceCharAt(final String s, final int i, final char c) {
		return String.format("%s%s%s", s.substring(0, i), c, s.substring(i + 1));
	}
	
	public static final String getMethodName(final Class<?> cls, final Class<? extends Annotation> annotation) {
		return Arrays.stream(cls.getDeclaredMethods())
				.filter(m -> m.isAnnotationPresent(annotation))
				.findAny().get().getName();
	}
	
	public static final String removeExt(final String fileName) {
		return fileName.replaceFirst("\\.[^.]*$", "");
	}
	
	public static final void ignore(final Object object) {
		//pass
	}
	
	public static final void setNum(final byte[] bytes, final int offset, final int length, final boolean littleEndian, final long value) {
		if (littleEndian) {
			for (var i = 0; i < length; i += 1) {
				bytes[offset + i] = (byte) (value >> (8 * i));
			}
		} else {
			for (var i = 0; i < length; i += 1) {
				bytes[offset + i] = (byte) (value >> (8 * (length - 1 - i)));
			}
		}
	}
	
	public static final long getNum(final byte[] bytes, final int offset, final int length, final boolean littleEndian) {
		Objects.checkFromIndexSize(offset, length, bytes.length);
		
		if (0 == length) {
			new Exception(String.format("Warning: 0-length range starting at %s of %s", offset, bytes.length)).printStackTrace();
		}
		
		var result = 0L;
		
		if (littleEndian) {
			for (var i = offset + length - 1; offset <= i; i -= 1) {
				result = (result * 256L) + Byte.toUnsignedInt(bytes[i]);
			}
		} else {
			for (var i = offset; i < offset + length; i += 1) {
				result = (result * 256L) + Byte.toUnsignedInt(bytes[i]);
			}
		}
		
		return result;
	}
	
	public static final boolean testBit(final int flags, final int bitIndex) {
		return testMask(flags, bitMask(bitIndex));
	}
	
	public static final byte setBit(final int flags, final int bitIndex, final boolean value) {
		final var mask = bitMask(bitIndex);
		
		if (value) {
			return (byte) (flags | mask);
		}
		
		return (byte) (flags & (~mask));
	}
	
	private static final int bitMask(final int bitIndex) {
		return 0b10000000 >> bitIndex;
	}
	
	public static final boolean testMask(final int flags, final int mask) {
		return 0 != (flags & mask);
	}
	
	public static final String dformat(final String format, final Object... args) {
		final var stackTrace = Thread.currentThread().getStackTrace();
		final var callerSte = stackTrace[3];
		
		return String.format("(%s:%s) %s",
				callerSte.getFileName(),
				callerSte.getLineNumber(),
				String.format(format, args));
	}
	
	public static final void dprintlnf(final String format, final Object... args) {
		final var stackTrace = Thread.currentThread().getStackTrace();
		
		for (final var ste : stackTrace) {
			final var steClassName = ste.getClassName();
			
			try {
				final var steClass = Class.forName(steClassName);
				final var debug = steClass.getAnnotation(Debug.class);
				
				if (null != debug) {
					if (debug.value()) {
						System.out.println(dformat(format, args));
					}
					
					break;
				}
			} catch (final ClassNotFoundException e) {
				//pass
			}
		}
	}
	
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Debug {
		
		public boolean value() default true;
		
	}
	
}
