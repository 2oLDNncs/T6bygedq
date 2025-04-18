package t6bygedq.lib;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author 2oLDNncs 20241228
 */
public final class Helpers {
	
	public static final <E> Iterable<E> in(final Stream<E> stream) {
		return new Iterable<>() {
			
			@Override
			public final Iterator<E> iterator() {
				return stream.iterator();
			}
			
		};
	}
	
	public static <T> Collector<T, ?, List<T>> toList() {
		return Collectors.toCollection(ArrayList::new);
	}
	
	public static final <E> E[] array(@SuppressWarnings("unchecked") final E... elements) {
		return elements;
	}
	
	public static final <E> E[] concat(final E[] array, @SuppressWarnings("unchecked") final E... elements) {
		final var result = Arrays.copyOf(array, array.length + elements.length);
		
		System.arraycopy(elements, 0, result, array.length, elements.length);
		
		return result;
	}
	
	public static String join(final String delimiter, final Object... args) {
		return join(delimiter, Arrays.stream(args));
	}
	
	public static String join(final String delimiter, final Iterable<?> args) {
		return join(delimiter, StreamSupport.stream(args.spliterator(), false));
	}
	
	public static String join(final String delimiter, final Stream<?> args) {
		return String.join(delimiter,
				args
				.map(Objects::toString)
				.toArray(String[]::new));
	}
	
	public static final boolean inRange(final int end, final int index) {
		return inRange(0, end, index);
	}
	
	public static final boolean inRange(final int start, final int end, final int index) {
		return start <= index && index < end;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T cast(final Object object) {
		return (T) object;
	}
	
	public static final <T> T castOrNull(final Class<T> cls, final Object object) {
		if (cls.isInstance(object)) {
			return cast(object);
		}
		
		return null;
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
		checkFromIndexSize(offset, length, bytes.length);
		
		if (0 == length) {
			new Exception(String.format("Warning: empty range starting at %s of %s", offset, bytes.length)).printStackTrace();
		}
		
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
	
    public static final int checkFromIndexSize(final int fromIndex, final int size, final int length) {
        if ((length | fromIndex | size) < 0 || length - fromIndex < size) {
        	throw new IndexOutOfBoundsException(String.format("Invalid range (%s:%s) for length %s", fromIndex, size, length));
        }
        
        return fromIndex;
	}
	
	public static final long getNum(final byte[] bytes, final int offset, final int length, final boolean littleEndian) {
		checkFromIndexSize(offset, length, bytes.length);
		
		if (0 == length) {
			new Exception(String.format("Warning: empty range starting at %s of %s", offset, bytes.length)).printStackTrace();
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
		return dformat(3, format, args);
	}
	
	public static final String dformat(final int stackLevel, final String format, final Object... args) {
		final var stackTrace = Thread.currentThread().getStackTrace();
		final var callerSte = stackTrace[stackLevel];
		
		return String.format("(%s:%s) %s",
				callerSte.getFileName(),
				callerSte.getLineNumber(),
				String.format(format, args));
	}
	
	private static PrintStream debugOut = System.out;
	
	public static final void setDebugOut(final PrintStream debugOut) {
		Helpers.debugOut = Objects.requireNonNull(debugOut);
	}
	
	public static final void setDebugOut(final String outFilePath) {
		try {
			setDebugOut(new PrintStream(outFilePath));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
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
						debugOut.println(dformat(3, format, args));
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
