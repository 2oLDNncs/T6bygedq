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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author 2oLDNncs 20241228
 */
public final class Helpers {
	
	public static final boolean[] EMPTY_BOOLEANS = new boolean[0];
	public static final byte[] EMPTY_BYTES = new byte[0];
	public static final short[] EMPTY_SHORTS = new short[0];
	public static final char[] EMPTY_CHARS = new char[0];
	public static final int[] EMPTY_INTS = new int[0];
	public static final float[] EMPTY_FLOATS = new float[0];
	public static final long[] EMPTY_LONGS = new long[0];
	public static final double[] EMPTY_DOUBLES = new double[0];
	
	public static final <E> Iterable<E> in(final Stream<E> stream) {
		return stream::iterator;
	}
	
	public static <T> Collector<T, ?, List<T>> toList() {
		return Collectors.toCollection(ArrayList::new);
	}
	
	@SafeVarargs
	public static final <E> List<E> newList(final E... elements) {
		return new ArrayList<>(Arrays.asList(elements));
	}
	
	public static final <E> E[] array(@SuppressWarnings("unchecked") final E... elements) {
		return elements;
	}
	
	public static final <E> E[] concat(final E[] array, @SuppressWarnings("unchecked") final E... elements) {
		final var result = Arrays.copyOf(array, array.length + elements.length);
		
		System.arraycopy(elements, 0, result, array.length, elements.length);
		
		return result;
	}
	
	public static final void shuffle(final int[] values, final Random random) {
		for (var i = 0; i < values.length; i += 1) {
			final var k = random.nextInt(values.length);
			final var tmp = values[i];
			values[i] = values[k];
			values[k] = tmp;
		}
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
	
	public static final <E> void swap(final Collection<E> a, final Collection<E> b) {
		if (a.size() <= b.size()) {
			final var tmp = new ArrayList<>(a);
			a.clear();
			a.addAll(b);
			b.clear();
			b.addAll(tmp);
		} else {
			swap(b, a);
		}
	}
	
	public static final <E> Iterable<E> reversed(final List<E> list) {
		return new Iterable<>() {
			
			@Override
			public final Iterator<E> iterator() {
				final var it = list.listIterator(list.size());
				
				return new Iterator<>() {
					
					@Override
					public boolean hasNext() {
						return it.hasPrevious();
					}
					
					@Override
					public final E next() {
						return it.previous();
					}
					
					@Override
					public final void remove() {
						it.remove();
					}
					
				};
			}
			
		};
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

	public static final <E> Iterable<IndexedElement<E>> inIndexed(final Iterable<E> iterable) {
		return new Iterable<>() {
			
			@Override
			public final Iterator<IndexedElement<E>> iterator() {
				return new Iterator<>() {
					
					private final IndexedElement<E> result = new IndexedElement<>();
					
					private final Iterator<E> it = iterable.iterator();
					
					@Override
					public final boolean hasNext() {
						return this.it.hasNext();
					}
					
					@Override
					public final IndexedElement<E> next() {
						this.result.setIndex(this.result.getIndex() + 1);
						this.result.setElement(this.it.next());
						
						return this.result;
					}
					
				};
			}
			
		};
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
	
	public static final <N> void forEachNodeUp(final N start, final Function<N, N> getParent, final Consumer<N> action) {
		var node = start;
		
		while (null != node) {
			action.accept(node);
			node = getParent.apply(node);
		}
	}
	
	public static final <N> void forEachNodeDepthFirst(final N start, final Function<N, Iterable<? extends N>> getChildren,
			final Consumer<N> actionBeforeChildren) {
		forEachNodeDepthFirst(start, getChildren, actionBeforeChildren, null);
	}
	
	public static final <N> void forEachNodeDepthFirst(final N start, final Function<N, Iterable<? extends N>> getChildren,
			final Consumer<N> actionBeforeChildren, final Consumer<N> actionAfterChildren) {
		forEachNodeDepthFirst(start, getChildren, actionBeforeChildren, actionAfterChildren, null);
	}
	
	public static final <N> void forEachNodeDepthFirst(final N start, final Function<N, Iterable<? extends N>> getChildren,
			final Consumer<N> actionBeforeChildren, final Consumer<N> actionAfterChildren,
			final Consumer<N> actionWhenCycleDetected) {
		final var todo = new ArrayList<N>();
		final var done = new HashSet<N>();
		
		todo.add(start);
		
		while (!todo.isEmpty()) {
			final var node = todo.remove(todo.size() - 1);
			
			if (done.add(node)) {
				todo.add(node);
				
				accept(actionBeforeChildren, node);
				
				int i = 0;
				
				for (final var child : getChildren.apply(node)) {
					if (done.contains(child)) {
						accept(actionWhenCycleDetected, child);
					} else {
						todo.add(todo.size() - i, child);
						i += 1;
					}
				}
			} else {
				accept(actionAfterChildren, node);
			}
		}
	}
	
	public static final <N> void forEachNodeBreadthFirst(final N start, final Function<N, Iterable<? extends N>> getChildren,
			final Consumer<N> action) {
		forEachNodeBreadthFirst(start, getChildren, action, null);
	}
	
	public static final <N> void forEachNodeBreadthFirst(final N start, final Function<N, Iterable<? extends N>> getChildren,
			final Consumer<N> action, final Consumer<N> actionWhenCycleDetected) {
		final var todo = new ArrayList<N>();
		final var done = new HashSet<N>();
		
		todo.add(start);
		
		while (!todo.isEmpty()) {
			final var node = todo.remove(0);
			
			if (done.add(node)) {
				accept(action, node);
				
				for (final var child : getChildren.apply(node)) {
					if (done.contains(child)) {
						accept(actionWhenCycleDetected, child);
					} else {
						todo.add(child);
					}
				}
			}
		}
	}
	
	public static final <N> void setNodeParent(
			final N node, final N parent,
			final Function<N, N> getParent, final BiConsumer<N, N> setParent,
			final BiConsumer<N, N> addChild, final BiConsumer<N, N> removeChild) {
		final var oldParent = getParent.apply(node);
		
		if (null != oldParent) {
			removeChild.accept(oldParent, node);
		}
		
		setParent.accept(node, parent);
		
		if (null != parent) {
			addChild.accept(parent, node);
		}
	}
	
	public static final <N> Function<N, Iterable<? extends N>> generator(final ToIntFunction<N> getElementCount,
			final IndexFunction<N, N> getElement) {
		return node -> new Iterable<>() {
			
			@Override
			public final Iterator<N> iterator() {
				return new Iterator<>() {
					
					private int currentIndex = 0;
					
					@Override
					public final boolean hasNext() {
						return this.currentIndex < getElementCount.applyAsInt(node);
					}
					
					@Override
					public final N next() {
						final var result = getElement.apply(node, this.currentIndex);
						
						this.currentIndex += 1;
						
						return result;
					}
					
				};
			}
			
			@Override
			public final Spliterator<N> spliterator() {
				return new Spliterator<>() {
					
					private int currentIndex = 0;
					
					@Override
					public final boolean tryAdvance(final Consumer<? super N> action) {
						if (this.currentIndex < this.estimateSize()) {
							action.accept(getElement.apply(node, this.currentIndex));
							
							return true;
						}
						
						return false;
					}
					
					@Override
					public final Spliterator<N> trySplit() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public final long estimateSize() {
						return getElementCount.applyAsInt(node);
					}
					
					@Override
					public final int characteristics() {
						return Spliterator.SIZED | Spliterator.ORDERED;
					}
					
				};
			}
			
		};
	}
	
	public static final <T> Iterable<T> generator(final ExceptionalSupplier<T> supplier) {
		return generator(supplier, null);
	}
	
	public static final <T> Iterable<T> generator(final ExceptionalSupplier<T> supplier, final Function<Exception, T> actionWhenException) {
		return new Iterable<>() {
			
			@Override
			public final Iterator<T> iterator() {
				return new Iterator<>() {
					
					private T next = this.get();
					
					@Override
					public final boolean hasNext() {
						return null != this.next;
					}
					
					@Override
					public final T next() {
						final var result = this.next;
						
						this.next = this.get();
						
						return result;
					}
					
					private final T get() {
						try {
							return supplier.get();
						} catch (final Exception e) {
							if (null != actionWhenException) {
								return actionWhenException.apply(e);
							}
							
							return null;
						}
					}
					
				};
			}
			
		};
	}
	
	private static <T> void accept(final Consumer<T> action, final T arg) {
		if (null != action) {
			action.accept(arg);
		}
	}
	
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Debug {
		
		public boolean value() default true;
		
	}

	/**
	 * @author 2oLDNncs 20250421
	 */
	public static final class IndexedElement<E> {
		
		private int index = -1;
		
		private E element;
		
		public final int getIndex() {
			return this.index;
		}
		
		final void setIndex(final int index) {
			this.index = index;
		}
		
		public final E getElement() {
			return this.element;
		}
		
		final void setElement(final E element) {
			this.element = element;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20260506
	 *
	 * @param <T>
	 * @param <R>
	 */
	public static abstract interface IndexFunction<T, R> {
		
		public abstract R apply(T t, int index);
		
	}
	
	/**
	 * @author 2oLDNncs 20260507
	 *
	 * @param <R>
	 */
	public static abstract interface ExceptionalSupplier<R> {
		
		public abstract R get() throws Exception;
		
	}
	
}
