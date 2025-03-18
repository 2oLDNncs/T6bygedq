package t6bygedq.lib.cbl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250208
 */
public abstract class RecPart {
	
	protected final Buffer buffer;
	
	private Buffer.Region.Generator dynamicRegionGenerator;
	
	protected RecPart(final Buffer buffer) {
		this.buffer = buffer;
	}
	
	public final Map<String, Object> getProperties() {
		final var result = new LinkedHashMap<String, Object>();
		
		this.getProperties(result);
		
		return result;
	}
	
	protected void getProperties(final Map<String, Object> properties) {
		for (final var field : this.getClass().getFields()) {
			if (field.getName().startsWith("v")) {
				try {
					properties.put(field.getName().substring(1), field.get(this));
				} catch (final Exception e) {
					System.err.println(String.format("Field: %s", field));
					e.printStackTrace();
				}
			}
		}
	}
	
	public final int getLength() {
		return this.protectedGetLength();
	}
	
	protected int protectedGetLength() {
		if (this.isStaticRegionGenerator()) {
			return getStaticLength(this.getClass());
		}
		
		return this.getDynamicRegionGenerator().getTotalLength();
	}
	
	public final void read(final ReadingContext rc) throws IOException {
		this.buffer.read(rc);
		this.afterRead();
	}
	
	protected void afterRead() {
		Helpers.dprintlnf(this.getClass().getSimpleName());
		
		for (final var field : this.getClass().getFields()) {
			if (field.getName().startsWith("v")) {
				try {
					Helpers.dprintlnf(" %s<%s>", field.getName().substring(1), field.get(this));
				} catch (final Exception e) {
					System.err.println(String.format("Field: %s", field));
					e.printStackTrace();
				}
			}
		}
	}
	
	public final void write(final OutputStream out) throws IOException {
		this.beforeWrite();
		this.buffer.write(out);
	}
	
	protected void beforeWrite() {
		//pass
	}
	
	private final boolean isStaticRegionGenerator() {
		return null == this.dynamicRegionGenerator;
	}
	
	private final Buffer.Region.Generator getDynamicRegionGenerator() {
		if (this.isStaticRegionGenerator()) {
			this.setDynamicRegionGenerator(getStaticRegionGenerator(this.getClass()).clone());
		}
		
		return this.dynamicRegionGenerator;
	}
	
	protected final void setDynamicRegionGenerator(final Buffer.Region.Generator dynamicRegionGenerator) {
		this.dynamicRegionGenerator = dynamicRegionGenerator;
	}
	
	protected final Buffer.Region.Generator cloneDynamicRegionGenerator() {
		return this.getDynamicRegionGenerator().clone();
	}
	
	protected final Buffer.Region newDynamicFixedLengthRegion(final int length) {
		if (this.buffer.getLength() < this.getDynamicRegionGenerator().getTotalLength() + length) {
			this.buffer.insertBytes(this.buffer.getLength(), length);
		}
		
		return this.getDynamicRegionGenerator().newFixedLength(length);
	}
	
	protected final Buffer.Region newDynamicVariableLengthRegion(final Buffer.Region length) {
		return this.getDynamicRegionGenerator().newVariableLength(length, this.buffer);
	}
	
	protected final <E extends Enum<E>> EnumVar<E> newEnumVar(final int length, final Decoder<Integer, E> decoder) {
		return this.newEnumVar(this.newDynamicFixedLengthRegion(length), decoder);
	}
	
	protected final <E extends Enum<E>> EnumVar<E> newEnumVar(final Buffer.Region region, final Decoder<Integer, E> decoder) {
		return new EnumVar<>() {
			
			@Override
			public final void set(final E value) {
				buffer.setNum(region, decoder.getKey(value));
			}
			
			@Override
			public final E get() {
				return decoder.get(buffer.getInt(region));
			}
			
			@Override
			public final String toString() {
				return Objects.toString(this.get());
			}
			
		};
	}
	
	protected final BooleanVar newBooleanVar(final Buffer.Region region, final int bitIndex) {
		return new BooleanVar() {
			
			@Override
			public final void set(final boolean value) {
				buffer.setBit(region, bitIndex, value);
			}
			
			@Override
			public final boolean get() {
				return buffer.testBit(region, bitIndex);
			}
			
			@Override
			public final String toString() {
				return Objects.toString(this.get());
			}
			
		};
	}
	
	protected final IntVar newIntVar(final int length) {
		return this.newIntVar(this.newDynamicFixedLengthRegion(length));
	}
	
	protected final IntVar newIntVar(final Buffer.Region region) {
		return new IntVar() {
			
			@Override
			public final void set(final int value) {
				buffer.setNum(region, value);
			}
			
			@Override
			public final int get() {
				return buffer.getInt(region);
			}
			
			@Override
			public final String toString() {
				return Objects.toString(this.get());
			}
			
		};
	}
	
	protected final LongVar newLongVar(final int length) {
		return this.newLongVar(this.newDynamicFixedLengthRegion(length));
	}
	
	protected final LongVar newLongVar(final Buffer.Region region) {
		return new LongVar() {
			
			@Override
			public final void set(final long value) {
				buffer.setNum(region, value);
			}
			
			@Override
			public final long get() {
				return buffer.getInt(region);
			}
			
			@Override
			public final String toString() {
				return Objects.toString(this.get());
			}
			
		};
	}
	
	protected final <E> ListVar_<E> newListVarV2(final Buffer.Region length, final Function<Buffer.Region.Generator, E> newElement) {
		System.out.println(Helpers.dformat("length.debugStructure"));
		length.debugStructure(System.out, "");
		
		final var rg = this.getDynamicRegionGenerator().clone();
		
		return new ListVar_<>() {
			
			private final IntVar vLength = newIntVar(length);
			
			private int currentLength = 0;
			
			@Override
			protected final boolean newElementNeeded() {
				System.out.println(Helpers.dformat("%s %s", this.currentLength, this.vLength.get()));
				return this.currentLength < this.vLength.get();
			}
			
			@Override
			protected final E newElement() {
				final var rgLength = rg.getTotalLength();
				final var result = newElement.apply(rg);
				this.currentLength += rg.getTotalLength() - rgLength;
				
				return result;
			}
			
		};
	}
	
	protected final <E> ListVar_<E> newListVarV(final Buffer.Region count, final Supplier<E> newElement) {
		return new ListVar2<>(this.newIntVar(count)) {
			
			@Override
			protected final E newElement() {
				return newElement.get();
			}
			
		};
	}
	
	protected final IntListVar newIntListVarV(final Buffer.Region count) {
		final var elementSize = 4;
		final var region = this.getDynamicRegionGenerator().newVariableLength(count, elementSize, this.buffer);
		
		return new IntListVar() {
			
			@Override
			public final int count() {
				return buffer.getInt(count);
			}
			
			@Override
			public final void set(final int index, final int value) {
				buffer.setNum(region.getOffset() + index * elementSize, elementSize, value);
			}
			
			@Override
			public final void rem(final int index) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public final int get(final int index) {
				return (int) buffer.getNum(region.getOffset() + index * elementSize, elementSize);
			}
			
			@Override
			public final void add(final int value) {
				final var newIndex = this.count();
				buffer.insertBytes(region.getNextOffset(), elementSize);
				buffer.setNum(count, newIndex + 1);
				this.set(newIndex, value);
			}
			
			@Override
			public final String toString() {
				final var elements = new ArrayList<>();
				final var n = this.count();
				
				for (var i = 0; i < n; i += 1) {
					elements.add(this.get(i));
				}
				
				return elements.toString();
			}
			
		};
	}
	
	protected final LongListVar newLongListVarV(final Buffer.Region count) {
		final var elementSize = 8;
		final var region = this.getDynamicRegionGenerator().newVariableLength(count, elementSize, this.buffer);
		
		return new LongListVar() {
			
			@Override
			public final int count() {
				return buffer.getInt(count);
			}
			
			@Override
			public final void set(final int index, final long value) {
				buffer.setNum(region.getOffset() + index * elementSize, elementSize, value);
			}
			
			@Override
			public final void rem(final int index) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public final long get(final int index) {
				return buffer.getNum(region.getOffset() + index * elementSize, elementSize);
			}
			
			@Override
			public final void add(final long value) {
				final var newIndex = this.count();
				buffer.insertBytes(region.getNextOffset(), elementSize);
				buffer.setNum(count, newIndex + 1);
				this.set(newIndex, value);
			}
			
			@Override
			public final String toString() {
				final var elements = new ArrayList<>();
				final var n = this.count();
				
				for (var i = 0; i < n; i += 1) {
					elements.add(this.get(i));
				}
				
				return elements.toString();
			}
			
		};
	}
	
	protected final StringVar newStringVarV(final Buffer.Region length) {
		return this.newStringVarF(this.getDynamicRegionGenerator().newVariableLength(length, this.buffer));
	}
	
	protected final StringVar newStringVarF(final int length) {
		return this.newStringVarF(this.newDynamicFixedLengthRegion(length));
	}
	
	protected final StringVar newStringVarF(final Buffer.Region region) {
		if (Buffer.DEBUG) {
			System.out.println(Helpers.dformat("region.debugStructure"));
			region.debugStructure(System.out, "");
		}
		
		return new StringVar() {
			
			@Override
			public final void set(final String value) {
				buffer.setStr(region, value);
			}
			
			@Override
			public final String get() {
				return buffer.getStr(region);
			}
			
			@Override
			public final String toString() {
				return Objects.toString(this.get());
			}
			
		};
	}
	
	private static final Map<Class<?>, Buffer.Region.Generator> staticRegionGenerators = new HashMap<>();
	
	protected static final Buffer.Region.Generator getStaticRegionGenerator(final Class<?> cls) {
		return getStaticRegionGenerator(cls, null);
	}
	
	protected static final Buffer.Region.Generator getStaticRegionGenerator(final Class<?> cls, final Buffer.Region.Generator base) {
		try {
			// Ensure static initialization
			Class.forName(cls.getName(), true, cls.getClassLoader());
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		return staticRegionGenerators.computeIfAbsent(cls, __ -> null == base ? new Buffer.Region.Generator() : base.clone());
	}
	
	static final int getStaticLength(final Class<? extends RecPart> cls) {
		return getStaticRegionGenerator(cls).getTotalLength();
	}
	
}