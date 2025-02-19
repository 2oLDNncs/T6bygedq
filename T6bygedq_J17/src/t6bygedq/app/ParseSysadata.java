package t6bygedq.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20250202
 */
@Debug(false)
public final class ParseSysadata {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_TEST = "-Test";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_sysadata");
		ap.setDefault(ARG_TEST, true);
		
		if (ap.getBoolean(ARG_TEST)) {
			test(new File("data/test_sysadata"));
		} else {
			read(ap.getFile(ARG_IN));
		}
	}
	
	private static void read(final File file) throws IOException, FileNotFoundException {
		try (final var in = new FileInputStream(file)) {
			final var rc = new ReadingContext(in);
			
			while (0 < rc.input.available()) {
				rc.lineNumber += 1;
				rc.columnNumber = 0;
				
				try {
					Rec.read(rc);
				} catch (final RuntimeException e) {
					System.err.println(String.format("Read error at Line %s Column %s", rc.lineNumber, rc.columnNumber));
					throw e;
				}
			}
		}
	}
	
	public static final void test(final File file) throws IOException {
		try (final var out = new FileOutputStream(file)) {
			Helpers.dprintlnf("");
			final var rec = new Rec();
			
			rec.getRecHeader().vLanguageCode.set(RecHeader.LanguageCode.COBOL);
			rec.getRecHeader().vLittleEndian.set(false);
			
			{
				final var rd = rec.setAndGetRecData(RecData_JobIdentification.class);
				
				rd.vDate.set("20250208");
				rd.vTime.set("0823");
				rd.vProductNumber.set("1");
				rd.vProductVersion.set("1");
				rd.vBuildLevel.set("1");
				rd.vSystemId.set("TheSystem");
				rd.vJobName.set("TheJob");
				rd.vStepName.set("TheStep");
				rd.vProcStep.set("TheThing");
				
				{
					final var f = rd.addInputFile();
					
					f.vName.set("TheFileName");
				}
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_AdataIdentification.class);
				
				rd.vTime.set(1234567890L);
				rd.vCharacterSet.set(RecData_AdataIdentification.CharacterSet.EBCDIC);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_CompilationUnitDelimiter.class);
				
				rd.vType.set(RecData_CompilationUnitDelimiter.CompilationUnitDelimiterType.END);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_Options.class);
				
				rd.vAdata.set(true);
				rd.vAdexitName.set("TheAdexit");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_ExternalSymbol.class);
				
				rd.vSectionType.set(RecData_ExternalSymbol.SectionType.EXTERNAL_REFERENCE);
				rd.vExternalName.set("TheExternalName");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_ParseTree.class);
				
				rd.vNodeNumber.set(123L);
				rd.vNodeType.set(RecData_ParseTree.NodeType.ELSE_PHRASE);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_ParseTree.class);
				
				rd.vNodeNumber.set(456L);
				rd.vNodeType.set(RecData_ParseTree.NodeType.INITIALIZE_LITERAL_NO_TOKENS);
				rd.vNodeSubtype.set(RecData_ParseTree.NodeSubtype.ALPHABETIC_13);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_Token.class);
				
				rd.vTokenCode.set(RecData_Token.TokenCode.ACCEPT);
				rd.vTokenText.set("Accept");
				rd.vFlags.set(RecData_Token.Flags.LAST_PIECE);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_SourceError.class);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_Source.class);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_CopyReplacing.class);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_Symbol.class);
				
				rd.vSymbolType.set(RecData_Symbol.SymbolType.DATA_NAME);
				rd.vSymbolAttribute.set(RecData_Symbol.SymbolAttribute.NUMERIC);
//				rd.vMnemonicNameSymbolClauses.set(RecData_Symbol.MnemonicNameSymbolClauses.C01);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_SymbolCrossReference.class);
				
				rd.vCrossReferenceType.set(RecData_SymbolCrossReference.CrossReferenceType.SYMBOL_OR_DATA_NAME);
				rd.vSymbolName.set("TheSymbolName");
				
				try {
					rd.addStmt();
					throw new RuntimeException();
				} catch (final IllegalStateException e) {
					Helpers.ignore(e);
				}
				
				{
					final var fs = rd.addFlagAndStmt();
					
					fs.vReferenceFlag.set(RecData_SymbolCrossReference.ReferenceFlag.MODIFICATION);
				}
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_NestedProgram.class);
				
				rd.vProgramName.set("TheNestedProgramName");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_Library.class);
				
				{
					final var m = rd.addMember();
					
					m.vFileId.set(123);
					m.vName.set("TheMemberName");
				}
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_Statistics.class);
				
				rd.vSourceRecords.set(123);
				rd.vDataDivisionStatements.set(456);
				rd.vProcedureDivisionStatements.set(789);
				rd.vProgramName.set("TheProgramName");
				
				rec.write(out);
			}
		}
		
		read(file);
	}
	
	public static final long num(final byte[] bytes, final int offset, final int length, final boolean littleEndian) {
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
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static final class Rec {
		
		private final RecHeader recHeader = new RecHeader();
		
		private RecData recData;
		
		public final RecHeader getRecHeader() {
			return this.recHeader;
		}
		
		public final <R extends RecData> R setAndGetRecData(final Class<R> cls) {
			this.getRecHeader().vRecordType.set(RecHeader.RecordType.fromRecDataClass(cls));
			
			return cls.cast(this.getRecData());
		}
		
		public final RecData getRecData() {
			if (!this.getRecHeader().vRecordType.get().isInstance(this.recData)) {
				if (null != this.recData) {
					this.getRecHeader().vDataLength.set(0);
				}
				
				this.recData = this.getRecHeader().newRecData();
				this.getRecHeader().vDataLength.set(this.recData.getLength());
			}
			
			return this.recData;
		}
		
		public final void write(final OutputStream out) throws IOException {
			this.getRecHeader().vDataLength.set(this.getRecData().getLength());
			this.getRecHeader().write(out);
			this.getRecData().write(out);
		}
		
		public static final Rec read(final ReadingContext rc) throws IOException {
			final var result = new Rec();
			
			result.getRecHeader().read(rc);
			
			Helpers.dprintlnf("Header(Lang(%s) RecType(%s) LittleEndian(%s) Continued(%s) Arch(%s) Edit(%s) DataLen(%s))",
					result.getRecHeader().vLanguageCode,
					result.getRecHeader().vRecordType,
					result.getRecHeader().vLittleEndian,
					result.getRecHeader().vContinuedInNextRec,
					result.getRecHeader().vDataArchitectureLevel,
					result.getRecHeader().vDataRecordEditionLevel,
					result.getRecHeader().vDataLength);
			
			final var hBytes = result.getRecHeader().buffer.cloneBytes();
			
			result.getRecData().read(rc);
			
			final var dBytes = result.getRecData().buffer.cloneBytes();
			
			System.out.println("(Rec.read)" + Arrays.toString(hBytes) + dBytes.length + Arrays.toString(dBytes));
			
			return result;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static abstract interface Var {
		//pass
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static abstract interface IntVar extends Var {
		
		public abstract void set(int value);
		
		public abstract int get();
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 *
	 * @param <E>
	 */
	public static abstract interface EnumVar<E extends Enum<E>> extends Var {
		
		public abstract void set(E value);
		
		public abstract E get();
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static abstract interface BooleanVar extends Var {
		
		public abstract void set(boolean value);
		
		public abstract boolean get();
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static abstract interface LongVar extends Var {
		
		public abstract void set(long value);
		
		public abstract long get();
		
	}
	
	/**
	 * @author 2oLDNncs 20250210
	 */
	public static abstract interface ListVar extends Var {
		
		public abstract int count();
		
	}
	
	/**
	 * @author 2oLDNncs 20250214
	 *
	 * @param <E>
	 */
	public static abstract class ListVar_<E> extends AbstractList<E> implements Var {
		
		protected final List<E> elements = new ArrayList<>();
		
		public final void afterRead() {
			while (this.newElementNeeded()) {
				this.elements.add(this.newElement());
			}
		}
		
		protected abstract boolean newElementNeeded();
		
		protected abstract E newElement();
		
		@Override
		public final E get(final int index) {
			return this.elements.get(index);
		}
		
		@Override
		public final int size() {
			return this.elements.size();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250214
	 *
	 * @param <E>
	 */
	public static abstract class ListVar2<E> extends ListVar_<E> {
		
		private final IntVar vCount;
		
		protected ListVar2(final IntVar vCount) {
			this.vCount = vCount;
		}
		
		@Override
		protected boolean newElementNeeded() {
			return this.elements.size() < this.vCount.get();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250210
	 */
	public static abstract interface LongListVar extends ListVar {
		
		public abstract void add(long value);
		
		public abstract void rem(int index);
		
		public abstract void set(int index, long value);
		
		public abstract long get(int index);
		
	}
	
	/**
	 * @author 2oLDNncs 20250210
	 */
	public static abstract interface IntListVar extends ListVar {
		
		public abstract void add(int value);
		
		public abstract void rem(int index);
		
		public abstract void set(int index, int value);
		
		public abstract int get(int index);
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static abstract interface StringVar extends Var {
		
		public abstract void set(String value);
		
		public abstract String get();
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static abstract class RecPart {
		
		protected final Buffer buffer;
		
		private Buffer.Region.Generator dynamicRegionGenerator;
		
		protected RecPart(final Buffer buffer) {
			this.buffer = buffer;
		}
		
		public final int getLength() {
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
				this.dynamicRegionGenerator = getStaticRegionGenerator(this.getClass()).clone();
			}
			
			return this.dynamicRegionGenerator;
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
		
		protected final <E> ListVar_<E> newListVarV2(final Buffer.Region length, final Supplier<E> newElement) {
			final var rg = this.getDynamicRegionGenerator().clone();
			
			return new ListVar_<>() {
				
				private final IntVar vLength = newIntVar(length);
				
				private int currentLength;
				
				@Override
				protected final boolean newElementNeeded() {
					return this.currentLength < this.vLength.get();
				}
				
				@Override
				protected final E newElement() {
					final var rgLength = rg.getTotalLength();
					final var result = newElement.get();
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
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static abstract class RecData extends RecPart {
		
		protected RecData(final Buffer buffer) {
			super(buffer);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static abstract interface Decoder<K, V> {
		
		public abstract V get(K key);
		
		public abstract K getKey(V value);
		
	}
	
	/**
	 * @author 2oLDNncs 20250207
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static final class ReversibleMap<K, V> extends AbstractMap<K, V> implements Decoder<K, V>{
		
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
	
	/**
	 * @author 2oLDNncs 20250217
	 */
	public static final class ModClock {
		
		private final long[] sharedClock;
		
		private long localClock = -1;
		
		private ModClock(final long[] sharedClock) {
			this.sharedClock = sharedClock;
		}
		
		public ModClock() {
			this(new long[1]);
		}
		
		public final void incrShared() {
			++this.sharedClock[0];
		}
		
		public final boolean syncLocal() {
			final var c = this.sharedClock[0];
			
			if (this.localClock < c) {
				this.localClock = c;
				
				return true;
			}
			
			return false;
		}
		
		public final ModClock spawn() {
			return new ModClock(this.sharedClock);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250206
	 */
	public static final class Buffer {
		
		private final ModClock modClock = new ModClock();
		
		private byte[] bytes;
		
		private final boolean littleEndian;
		
		private int lengthLimit = Integer.MAX_VALUE;
		
		public Buffer(final int length, final boolean littleEndian) {
			this.bytes = new byte[length];
			this.littleEndian = littleEndian;
		}
		
		public final ModClock spawnModClock() {
			return this.modClock.spawn();
		}
		
		public final boolean isLittleEndian() {
			return this.littleEndian;
		}
		
		public final byte[] cloneBytes() {
			return this.bytes.clone();
		}
		
		public final int getLength() {
			return this.bytes.length;
		}
		
		public final int getLengthLimit() {
			return this.lengthLimit;
		}
		
		public final void setLengthLimit(final int lengthLimit) {
			this.lengthLimit = lengthLimit;
		}
		
		public final void setStr(final Region region, final String value) {
			final var valueBytes = value.getBytes(EBCDIC);
			final var regionOffset = region.getOffset();
			final var regionLength = region.getLength();
			
			if (region.isFixedLength()) {
				final var n = Math.min(valueBytes.length, regionLength);
				
				System.arraycopy(valueBytes, 0, this.bytes, regionOffset, n);
				Arrays.fill(this.bytes, regionOffset + n, regionOffset + regionLength, SPACE);
			} else {
				final var newLength = valueBytes.length;
				
				region.setLength(newLength);
				
				if (newLength < regionLength) {
					this.removeBytes(regionOffset + newLength, regionLength - newLength);
				} else if (regionLength < newLength) {
					this.insertBytes(regionOffset + regionLength, newLength - regionLength);
				}
				
				System.arraycopy(valueBytes, 0, this.bytes, regionOffset, newLength);
			}
			
			this.incrModClock();
		}
		
		private final void incrModClock() {
			this.modClock.incrShared();
		}
		
		private final void removeBytes(final int offset, final int length) {
			final var newBytes = new byte[this.bytes.length - length];
			
			System.arraycopy(this.bytes, 0, newBytes, 0, offset);
			System.arraycopy(this.bytes, offset + length, newBytes, offset, newBytes.length - offset);
			
			this.bytes = newBytes;
			this.incrModClock();
		}
		
		private final void insertBytes(final int offset, final int length) {
			if ((long) this.getLengthLimit() < (long) offset + length) {
				throw new IllegalStateException();
			}
			
			final var newBytes = new byte[this.bytes.length + length];
			
			System.arraycopy(this.bytes, 0, newBytes, 0, offset);
			System.arraycopy(this.bytes, offset, newBytes, offset + length, bytes.length - offset);
			
			this.bytes = newBytes;
			this.incrModClock();
		}
		
		public final String getStr(final int offset, final int length) {
			return new String(this.bytes, offset, length, EBCDIC);
		}
		
		public final String getStr(final Region region) {
			return this.getStr(region.getOffset(), region.getLength());
		}
		
		public final boolean testBit(final Region region, final int bitIndex) {
			return ParseSysadata.testBit((int) this.getNum(region), bitIndex);
		}
		
		public final void setBit(final Region region, final int bitIndex, final boolean value) {
			this.setNum(region, ParseSysadata.setBit((int) this.getNum(region), bitIndex, value));
		}
		
		public final void setNum(final Region region, final long value) {
			this.setNum(region.getOffset(), region.getLength(), value, region.isTextDecimal());
		}
		
		public final void setNum(final int offset, final int length, final long value) {
			this.setNum(offset, length, value, false);
		}
		
		public final void setNum(final int offset, final int length, final long value, final boolean textDecimal) {
			if (textDecimal) {
				throw new UnsupportedOperationException("TODO"); // TODO
			}
			
			if (this.isLittleEndian()) {
				for (var i = 0; i < length; i += 1) {
					this.bytes[offset + i] = (byte) (value >> (8 * i));
				}
			} else {
				for (var i = 0; i < length; i += 1) {
					this.bytes[offset + i] = (byte) (value >> (8 * (length - 1 - i)));
				}
			}
			
			this.incrModClock();
		}
		
		public final long getNum(final int offset, final int length, final boolean textDecimal) {
			if (textDecimal) {
				return Long.parseLong(this.getStr(offset, length));
			}
			
			return num(this.bytes, offset, length, this.isLittleEndian());
		}
		
		public final long getNum(final int offset, final int length) {
			return this.getNum(offset, length, false);
		}
		
		public final long getNum(final Region region) {
			return this.getNum(region.getOffset(), region.getLength(), region.isTextDecimal());
		}
		
		public final int getInt(final Region region) {
			return (int) this.getNum(region);
		}
		
		public final byte getByte(final Region region) {
			return (byte) this.getNum(region);
		}
		
		public final void read(final ReadingContext rc) throws IOException {
			Helpers.dprintlnf("Reading %s bytes", this.bytes.length);
			rc.read(this.bytes);
			this.incrModClock();
		}
		
		public final void write(final OutputStream out) throws IOException {
			Helpers.dprintlnf("Writing %s bytes", this.bytes.length);
			out.write(this.bytes);
		}
		
		public static final Charset EBCDIC = Charset.forName("Cp500");
		
		public static final byte SPACE = EBCDIC.encode(" ").get();
		
		/**
		 * @author 2oLDNncs 20250206
		 */
		public static final class Region {
			
			private final Offset offset;
			
			private final Length length;
			
			private NumberFormat numberFormat = NumberFormat.BINARY;
			
			Region(final Offset offset, final Length length) {
				this.offset = offset;
				this.length = length;
			}
			public final ModClock spawnModClock() {
				if (this.offset instanceof RelativeOffset) {
					return ((RelativeOffset) this.offset).spawnModClock();
				}
				
				return ((VariableLength) this.length).spawnModClock();
			}
			
			public final boolean isAbsoluteOffset() {
				return this.offset instanceof AbsoluteOffset;
			}
			
			public final boolean isFixedLength() {
				return this.length instanceof FixedLength;
			}
			
			public final void setLength(final int length) {
				((VariableLength) this.length).setValue(length);
			}
			
			public final int getOffset() {
				return this.offset.getAsInt();
			}
			
			public final int getLength() {
				return this.length.getAsInt();
			}
			
			public final int getNextOffset() {
				return this.getOffset() + this.getLength();
			}
			
			public final NumberFormat getNumberFormat() {
				return this.numberFormat;
			}
			
			public final Region setNumberFormat(final NumberFormat numberFormat) {
				this.numberFormat = numberFormat;
				
				return this;
			}
			
			public final boolean isTextDecimal() {
				return NumberFormat.TEXT_DECIMAL.equals(this.getNumberFormat());
			}
			
			/**
			 * @author 2oLDNncs 20250213
			 */
			public static enum NumberFormat {
				
				BINARY, TEXT_DECIMAL;
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			public static final class Generator {
				
				private Region last;
				
				public final int getTotalLength() {
					return getNextOffset(this.last);
				}
				
				public final Buffer.Region newFixedLength(final int length) {
					return this.newRegion(this.newOffset(), new FixedLength(length));
				}
				
				public final Buffer.Region newVariableLength(final Buffer.Region length, final Buffer buffer) {
					return this.newVariableLength(length, 1, buffer);
				}
				
				public final Buffer.Region newVariableLength(final Buffer.Region elementCount, final int elementLength, final Buffer buffer) {
					return this.newRegion(this.newOffset(), new VariableLength(elementCount, elementLength, buffer));
				}
				
				private final Offset newOffset() {
					if (null == this.last) {
						return new AbsoluteOffset(0);
					} else if (this.last.isAbsoluteOffset() && this.last.isFixedLength()) {
						return new AbsoluteOffset(this.last.getNextOffset());
					}
					
					return new RelativeOffset(this.last);
				}
				
				@Override
				public final Generator clone() {
					final var result = new Generator();
					
					result.last = this.last;
					
					return result;
				}
				
				private final Region newRegion(final Offset offset, final Length length) {
					this.last = new Region(offset, length);
					
					return this.last;
				}
				
				private static final int getNextOffset(final Region region) {
					return null == region ? 0 : region.getNextOffset();
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static abstract class Value implements IntSupplier {
				
				private final int value;
				
				protected Value(final int value) {
					this.value = value;
				}
				
				@Override
				public final int getAsInt() {
					return this.value;
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static abstract class Reference {
				
				protected final Region target;
				
				protected Reference(final Region target) {
					this.target = target;
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static abstract interface Offset extends IntSupplier {
				//pass
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static final class AbsoluteOffset extends Value implements Offset {
				
				AbsoluteOffset(final int value) {
					super(value);
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static final class RelativeOffset extends Reference implements Offset {
				
				private final ModClock modClock;
				
				private int cachedValue;
				
				RelativeOffset(final Region previous) {
					super(previous);
					this.modClock = previous.spawnModClock();
				}
				
				public final ModClock spawnModClock() {
					return this.modClock.spawn();
				}
				
				@Override
				public final int getAsInt() {
					if (this.modClock.syncLocal()) {
						this.cachedValue = this.target.getNextOffset();
					}
					
					return this.cachedValue;
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static abstract interface Length extends IntSupplier {
				//pass
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static final class FixedLength extends Value implements Length {
				
				FixedLength(final int value) {
					super(value);
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250206
			 */
			private static final class VariableLength extends Reference implements Length {
				
				private final Buffer buffer;
				
				private final int elementLength;
				
				private final ModClock modClock;
				
				private int cachedValue;
				
				VariableLength(final Region elementCount, final int elementLength, final Buffer buffer) {
					super(elementCount);
					this.buffer = buffer;
					this.elementLength = elementLength;
					this.modClock = buffer.spawnModClock();
				}
				
				public final ModClock spawnModClock() {
					return this.modClock.spawn();
				}
				
				public final void setValue(final int elementCount) {
					this.buffer.setNum(this.target, elementCount);
				}
				
				@Override
				public final int getAsInt() {
					if (this.modClock.syncLocal()) {
						this.cachedValue = this.buffer.getInt(this.target) * this.elementLength;
					}
					
					return this.cachedValue;
				}
				
			}
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static final class RecData_JobIdentification extends RecData {
		
		public final StringVar vDate = this.newStringVarF(DATE);
		public final StringVar vTime = this.newStringVarF(TIME);
		public final StringVar vProductNumber = this.newStringVarF(PRODUCT_NUMBER);
		public final StringVar vProductVersion = this.newStringVarF(PRODUCT_VERSION);
		public final StringVar vBuildLevel = this.newStringVarF(BUILD_LEVEL);
		public final StringVar vSystemId = this.newStringVarF(SYSTEM_ID);
		public final StringVar vJobName = this.newStringVarF(JOB_NAME);
		public final StringVar vStepName = this.newStringVarF(STEP_NAME);
		public final StringVar vProcStep = this.newStringVarF(PROC_STEP);
		public final IntVar vInputFileCount = this.newIntVar(INPUT_FILE_COUNT);
		
		private final List<InputFile> inputFileList = new ArrayList<>();
		
		public RecData_JobIdentification(final Buffer buffer) {
			super(buffer);
		}
		
		private final InputFile newInputFile() {
			final var result = this.new InputFile();
			
			this.inputFileList.add(result);
			
			return result;
		}
		
		public final InputFile addInputFile() {
			final var result = this.newInputFile();
			final var n = this.inputFileList.size();
			
			this.vInputFileCount.set(n);
			result.vNumber.set(n);
			
			return result;
		}
		
		@Override
		protected final void afterRead() {
			super.afterRead();
			
			final var inputFileCount = this.vInputFileCount.get();
			
			if (0 < inputFileCount) {
				for (var i = 0; i < inputFileCount; i += 1) {
					final var fd = this.newInputFile();
					
					Helpers.dprintlnf(" InputFiles(%s)", i);
					Helpers.dprintlnf("  Number<%s>", fd.vNumber);
					Helpers.dprintlnf("  Name<%s>", fd.vName);
					Helpers.dprintlnf("  VolumeSerialNumber<%s>", fd.vVolumeSerialNumber);
					Helpers.dprintlnf("  MemberName<%s>", fd.vMemberName);
				}
			}
		}
		
		@Override
		protected final void beforeWrite() {
			if (this.vInputFileCount.get() != this.inputFileList.size()) {
				throw new IllegalStateException();
			}
		}
		
		public final InputFile getInputFile(final int index) {
			return this.inputFileList.get(index);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_JobIdentification.class);
		
		private static final Buffer.Region DATE             = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region TIME             = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PRODUCT_NUMBER   = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region PRODUCT_VERSION  = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region BUILD_LEVEL      = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region SYSTEM_ID        = staticRegionGenerator.newFixedLength(24);
		private static final Buffer.Region JOB_NAME         = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region STEP_NAME        = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region PROC_STEP        = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region INPUT_FILE_COUNT = staticRegionGenerator.newFixedLength(2);
		
		/**
		 * @author 2oLDNncs 20250204
		 */
		public final class InputFile {
			
			public final IntVar vNumber = newIntVar(2);
			private final Buffer.Region rNameLength = newDynamicFixedLengthRegion(2);
			private final Buffer.Region rVolSerialNumLength = newDynamicFixedLengthRegion(2);
			private final Buffer.Region rMemberNameLength = newDynamicFixedLengthRegion(2);
			public final StringVar vName = newStringVarV(this.rNameLength);
			public final StringVar vVolumeSerialNumber = newStringVarV(this.rVolSerialNumLength);
			public final StringVar vMemberName = newStringVarV(this.rMemberNameLength);
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static final class RecData_AdataIdentification extends RecData {
		
		public final LongVar               vTime         = this.newLongVar(TIME);
		public final IntVar                vCcsid        = this.newIntVar(CCSID);
		public final EnumVar<CharacterSet> vCharacterSet = this.newEnumVar(CHARACTER_SET_FLAGS, CharacterSet.decoder);
		public final StringVar             vCodePageName = this.newStringVarV(CODE_PAGE_NAME_LENGTH);
		
		public RecData_AdataIdentification(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_AdataIdentification.class);
		
		private static final Buffer.Region TIME                  = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region CCSID                 = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region CHARACTER_SET_FLAGS   = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CODE_PAGE_NAME_LENGTH = staticRegionGenerator.newFixedLength(2);
		
		/**
		 * @author 2oLDNncs 20250208
		 */
		public static enum CharacterSet {
			
			EBCDIC, ASCII;
			
			static final Decoder<Integer, CharacterSet> decoder =
					new ReversibleMap<Integer, CharacterSet>()
					.set(0x80, EBCDIC)
					.set(0x40, ASCII)
					;
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static final class RecData_CompilationUnitDelimiter extends RecData {
		
		public final EnumVar<CompilationUnitDelimiterType> vType = this.newEnumVar(TYPE, CompilationUnitDelimiterType.decoder);
		
		public RecData_CompilationUnitDelimiter(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_CompilationUnitDelimiter.class);
		
		private static final Buffer.Region TYPE        = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_01 = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_02 = staticRegionGenerator.newFixedLength(4);
		
		static {
			Helpers.ignore(RESERVED_01);
			Helpers.ignore(RESERVED_02);
		}
		
		/**
		 * @author 2oLDNncs 20250204
		 */
		public static enum CompilationUnitDelimiterType {
			
			START, END;
			
			static final Decoder<Integer, CompilationUnitDelimiterType> decoder =
					new ReversibleMap<Integer, CompilationUnitDelimiterType>()
					.set(0x0000, START)
					.set(0x0001, END)
					;
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static final class RecData_Options extends RecData {
		
		public final BooleanVar vDeck                                 = this.newBooleanVar(BYTE_1, 0);
		public final BooleanVar vAdata                                = this.newBooleanVar(BYTE_1, 1);
		public final BooleanVar vCollseqEbcdic                        = this.newBooleanVar(BYTE_1, 2);
		public final BooleanVar vSepobj                               = this.newBooleanVar(BYTE_1, 3);
		public final BooleanVar vName                                 = this.newBooleanVar(BYTE_1, 4);
		public final BooleanVar vObject                               = this.newBooleanVar(BYTE_1, 5);
		public final BooleanVar vSql                                  = this.newBooleanVar(BYTE_1, 6);
		public final BooleanVar vCics                                 = this.newBooleanVar(BYTE_1, 7);
		public final BooleanVar vOffset                               = this.newBooleanVar(BYTE_2, 0);
		public final BooleanVar vMap                                  = this.newBooleanVar(BYTE_2, 1);
		public final BooleanVar vList                                 = this.newBooleanVar(BYTE_2, 2);
		public final BooleanVar vDbcsxref                             = this.newBooleanVar(BYTE_2, 3);
		public final BooleanVar vXrefShort                            = this.newBooleanVar(BYTE_2, 4);
		public final BooleanVar vSource                               = this.newBooleanVar(BYTE_2, 5);
		public final BooleanVar vVbref                                = this.newBooleanVar(BYTE_2, 6);
		public final BooleanVar vXref                                 = this.newBooleanVar(BYTE_2, 7);
		public final BooleanVar vFlagImbeddedDiagnosticLevelSpecified = this.newBooleanVar(BYTE_3, 0);
		public final BooleanVar vFlagstd                              = this.newBooleanVar(BYTE_3, 1);
		public final BooleanVar vNum                                  = this.newBooleanVar(BYTE_3, 2);
		public final BooleanVar vSequence                             = this.newBooleanVar(BYTE_3, 3);
		public final BooleanVar vSosi                                 = this.newBooleanVar(BYTE_3, 4);
		public final BooleanVar vNsymbolNational                      = this.newBooleanVar(BYTE_3, 5);
		public final BooleanVar vProfile                              = this.newBooleanVar(BYTE_3, 6);
		public final BooleanVar vWord                                 = this.newBooleanVar(BYTE_3, 7);
		public final BooleanVar vAdv                                  = this.newBooleanVar(BYTE_4, 0);
		public final BooleanVar vApost                                = this.newBooleanVar(BYTE_4, 1);
		public final BooleanVar vDynam                                = this.newBooleanVar(BYTE_4, 2);
		public final BooleanVar vAwo                                  = this.newBooleanVar(BYTE_4, 3);
		public final BooleanVar vRmodeSpecified                       = this.newBooleanVar(BYTE_4, 4);
		public final BooleanVar vRent                                 = this.newBooleanVar(BYTE_4, 5);
		public final BooleanVar vRes                                  = this.newBooleanVar(BYTE_4, 6);
		public final BooleanVar vRmode24                              = this.newBooleanVar(BYTE_4, 7);
		public final BooleanVar vSqlccsid                             = this.newBooleanVar(BYTE_5, 0);
		public final BooleanVar vOpt12                                = this.newBooleanVar(BYTE_5, 1);
		public final BooleanVar vSqlims                               = this.newBooleanVar(BYTE_5, 2);
		public final BooleanVar vDbcs                                 = this.newBooleanVar(BYTE_5, 3);
		public final BooleanVar vAfpVolatile                          = this.newBooleanVar(BYTE_5, 4);
		public final BooleanVar vSsrange                              = this.newBooleanVar(BYTE_5, 5);
		public final BooleanVar vTest                                 = this.newBooleanVar(BYTE_5, 6);
		public final BooleanVar vProbe                                = this.newBooleanVar(BYTE_5, 7);
		public final BooleanVar vSrcformatExtend                      = this.newBooleanVar(BYTE_6, 0);
		public final BooleanVar vNumprocPfd                           = this.newBooleanVar(BYTE_6, 2);
		public final BooleanVar vNumclsAlt                            = this.newBooleanVar(BYTE_6, 3);
		public final BooleanVar vBinaryS390                           = this.newBooleanVar(BYTE_6, 5);
		public final BooleanVar vTruncStd                             = this.newBooleanVar(BYTE_6, 6);
		public final BooleanVar vZwb                                  = this.newBooleanVar(BYTE_6, 7);
		public final BooleanVar vAlowcbl                              = this.newBooleanVar(BYTE_7, 0);
		public final BooleanVar vTerm                                 = this.newBooleanVar(BYTE_7, 1);
		public final BooleanVar vDump                                 = this.newBooleanVar(BYTE_7, 2);
		public final BooleanVar vCurrency                             = this.newBooleanVar(BYTE_7, 6);
		public final BooleanVar vRules                                = this.newBooleanVar(BYTE_8, 0);
		public final BooleanVar vOptfile                              = this.newBooleanVar(BYTE_8, 1);
		public final BooleanVar vAddr64                               = this.newBooleanVar(BYTE_8, 2);
		public final BooleanVar vBlock0                               = this.newBooleanVar(BYTE_8, 4);
		public final BooleanVar vDispsignSep                          = this.newBooleanVar(BYTE_8, 6);
		public final BooleanVar vStgopt                               = this.newBooleanVar(BYTE_8, 7);
		public final BooleanVar vData24                               = this.newBooleanVar(BYTE_9, 0);
		public final BooleanVar vFastsrt                              = this.newBooleanVar(BYTE_9, 1);
		public final BooleanVar vThread                               = this.newBooleanVar(BYTE_9, 5);
		public final BooleanVar vHgprPreserve                         = this.newBooleanVar(BYTE_A, 0);
		public final BooleanVar vXmlparse                             = this.newBooleanVar(BYTE_A, 1);
		public final BooleanVar vMapDec                               = this.newBooleanVar(BYTE_A, 2);
		public final BooleanVar vSuppress                             = this.newBooleanVar(BYTE_A, 4);
		public final BooleanVar vVsamopenfsSucc                       = this.newBooleanVar(BYTE_A, 5);
		public final BooleanVar vNcollseqLocale                       = this.newBooleanVar(BYTE_C, 0);
		public final BooleanVar vIntdateLilian                        = this.newBooleanVar(BYTE_C, 2);
		public final BooleanVar vNcollseqBinary                       = this.newBooleanVar(BYTE_C, 3);
		public final BooleanVar vCharEbcdic                           = this.newBooleanVar(BYTE_C, 4);
		public final BooleanVar vFloatHex                             = this.newBooleanVar(BYTE_C, 5);
		public final BooleanVar vCollseqBinary                        = this.newBooleanVar(BYTE_C, 6);
		public final BooleanVar vCollseqLocale                        = this.newBooleanVar(BYTE_C, 7);
		public final BooleanVar vDll                                  = this.newBooleanVar(BYTE_D, 0);
		public final BooleanVar vExportall                            = this.newBooleanVar(BYTE_D, 1);
		public final BooleanVar vCodepage                             = this.newBooleanVar(BYTE_D, 2);
		public final BooleanVar vSourceformatExtend                   = this.newBooleanVar(BYTE_D, 3);
		public final BooleanVar vWsclear                              = this.newBooleanVar(BYTE_D, 6);
		public final BooleanVar vBeopt                                = this.newBooleanVar(BYTE_D, 7);
		public final BooleanVar vVlrCompat                            = this.newBooleanVar(BYTE_E, 0);
		public final BooleanVar vDiagtrunc                            = this.newBooleanVar(BYTE_E, 1);
		public final BooleanVar vLstfileUtf8                          = this.newBooleanVar(BYTE_E, 5);
		public final BooleanVar vMdec                                 = this.newBooleanVar(BYTE_E, 6);
		public final BooleanVar vMdeckNocompile                       = this.newBooleanVar(BYTE_E, 7);
		public final BooleanVar vDivideS390                           = this.newBooleanVar(BYTE_F, 0);
		public final BooleanVar vCopyright                            = this.newBooleanVar(BYTE_F, 1);
		public final BooleanVar vQualifyExtend                        = this.newBooleanVar(BYTE_F, 2);
		public final BooleanVar vService                              = this.newBooleanVar(BYTE_F, 3);
		public final BooleanVar vZonedataMig                          = this.newBooleanVar(BYTE_F, 4);
		public final BooleanVar vZonedataNopfd                        = this.newBooleanVar(BYTE_F, 5);
		public final BooleanVar vNumcheck                             = this.newBooleanVar(BYTE_F, 6);
		public final BooleanVar vParmcheck                            = this.newBooleanVar(BYTE_F, 7);
		public final BooleanVar vNumcheckZon                          = this.newBooleanVar(BYTE_G, 0);
		public final BooleanVar vNumcheckPac                          = this.newBooleanVar(BYTE_G, 1);
		public final BooleanVar vNumcheckBin                          = this.newBooleanVar(BYTE_G, 2);
		public final BooleanVar vNumcheckMsg                          = this.newBooleanVar(BYTE_G, 3);
		public final BooleanVar vNumcheckZonNoalphnum                 = this.newBooleanVar(BYTE_G, 4);
		public final BooleanVar vParmcheckAbd                         = this.newBooleanVar(BYTE_H, 0);
		public final BooleanVar vMinimum                              = this.newBooleanVar(FIPS_FLAGSTD, 0);
		public final BooleanVar vIntermediate                         = this.newBooleanVar(FIPS_FLAGSTD, 1);
		public final BooleanVar vHigh                                 = this.newBooleanVar(FIPS_FLAGSTD, 2);
		public final BooleanVar vIbmExtension                         = this.newBooleanVar(FIPS_FLAGSTD, 3);
		public final BooleanVar vLevel1Segmentation                   = this.newBooleanVar(FIPS_FLAGSTD, 4);
		public final BooleanVar vLevel2Segmentation                   = this.newBooleanVar(FIPS_FLAGSTD, 5);
		public final BooleanVar vDebugging                            = this.newBooleanVar(FIPS_FLAGSTD, 6);
		public final BooleanVar vObsolete                             = this.newBooleanVar(FIPS_FLAGSTD, 7);
		public final BooleanVar vNameAlias                            = this.newBooleanVar(TERN_OPTS_DATA, 0);
		public final BooleanVar vTruncBin                             = this.newBooleanVar(TERN_OPTS_DATA, 2);
		public final BooleanVar vParmcheckAbd2                        = this.newBooleanVar(TERN_OPTS_DATA, 3);
		public final BooleanVar vInitcheckStrict                      = this.newBooleanVar(TERN_OPTS_DATA, 4);
		public final BooleanVar vTestEjpd                             = this.newBooleanVar(TEST_SUBOPTS, 2);
		public final BooleanVar vTestSource                           = this.newBooleanVar(TEST_SUBOPTS, 3);
		public final BooleanVar vPgmnameCompat                        = this.newBooleanVar(PGMNAME_SUBOPTS, 0);
		public final BooleanVar vPgmnameLongupper                     = this.newBooleanVar(PGMNAME_SUBOPTS, 1);
		public final BooleanVar vPgmnameLongmixed                     = this.newBooleanVar(PGMNAME_SUBOPTS, 2);
		public final BooleanVar vEntryInterfaceSystem                 = this.newBooleanVar(ENTRY_INTERFACE_SUBOPTS, 0);
		public final BooleanVar vEntryInterfaceOptLink                = this.newBooleanVar(ENTRY_INTERFACE_SUBOPTS, 1);
		public final BooleanVar vCallinterfaceDll                     = this.newBooleanVar(CALLINTERFACE_SUBOPTS, 0);
		public final BooleanVar vCallinterfaceDynamic                 = this.newBooleanVar(CALLINTERFACE_SUBOPTS, 1);
		public final BooleanVar vArithCompat                          = this.newBooleanVar(ARITH_SUBOPTS, 0);
		public final BooleanVar vArithExtend                          = this.newBooleanVar(ARITH_SUBOPTS, 1);
		public final BooleanVar vIgyclibrInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 0);
		public final BooleanVar vIgycscanInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 1);
		public final BooleanVar vIgycdscnInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 2);
		public final BooleanVar vIgycgrouInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 3);
		public final BooleanVar vIgycpscnInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 4);
		public final BooleanVar vIgycpanaInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 5);
		public final BooleanVar vIgycfgenInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 6);
		public final BooleanVar vIgycpgenInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 7);
		public final BooleanVar vIgyclstrInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 1);
		public final BooleanVar vIgycxrefInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 2);
		public final BooleanVar vIgycdmapInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 3);
		public final BooleanVar vIgycdiagInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 6);
		public final BooleanVar vIgycdgenInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 7);
		
		public final EnumVar<FlagLevel>    vFlagLevel                 = this.newEnumVar(FLAG_LEVEL,     FlagLevel.decoder);
		public final EnumVar<FlagLevel>    vImbeddedDiagnosticLevel   = this.newEnumVar(IMB_DIAG_LEVEL, FlagLevel.decoder);
		public final EnumVar<CompilerMode> vCompilerMode              = this.newEnumVar(COMPILER_MODE,  CompilerMode.decoder);
		
		public final StringVar  vBuildLevel                           = this.newStringVarF(BUILD_LEVEL);
		public final LongVar    vDbcsReq                              = this.newLongVar(DBCS_REQ);
		public final StringVar  vDbcsOrdType                          = this.newStringVarF(DBCS_ORD_TYPE);
		public final IntVar     vOptimizationLevel                    = this.newIntVar(OPTIMIZATION_LEVEL);
		public final StringVar  vConvertedSo                          = this.newStringVarF(CONVERTED_SO);
		public final StringVar  vConvertedSi                          = this.newStringVarF(CONVERTED_SI);
		public final StringVar  vLanguageId                           = this.newStringVarF(LANGUAGE_ID);
		public final StringVar  vCurropt                              = this.newStringVarF(CURROPT);
		public final IntVar     vArch                                 = this.newIntVar(ARCH);
		public final IntVar     vCodepageValue                        = this.newIntVar(CODEPAGE);
		public final IntVar     vLinecnt                              = this.newIntVar(LINECNT);
		public final LongVar    vBufsize                              = this.newLongVar(BUFSIZE);
		
		public final StringVar  vOutddName                            = this.newStringVarV(OUTDD_NAME_LENGTH);
		public final StringVar  vRwtId                                = this.newStringVarV(RWT_ID_LENGTH);
		public final StringVar  vDbcsOrdpgm                           = this.newStringVarV(DBCS_ORDPGM_LENGTH);
		public final StringVar  vDbcsEnctbl                           = this.newStringVarV(DBCS_ENCTBL_LENGTH);
		public final StringVar  vInexitName                           = this.newStringVarV(INEXIT_NAME_LENGTH);
		public final StringVar  vPrtexitName                          = this.newStringVarV(PRTEXIT_NAME_LENGTH);
		public final StringVar  vLibexitName                          = this.newStringVarV(LIBEXIT_NAME_LENGTH);
		public final StringVar  vAdexitName                           = this.newStringVarV(ADEXIT_NAME_LENGTH);
		
		public RecData_Options(final Buffer buffer) {
			super(buffer);
			this.buffer.setNum(SPACE_01, Buffer.SPACE);
		}
		
		public final boolean isXrefFull() {
			return !this.vXrefShort.get() && this.vXref.get();
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Options.class);
		
		private static final Buffer.Region BYTE_0                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_1                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_2                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_3                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_4                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_5                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_6                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_7                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_8                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_9                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_A                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_B                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_C                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_D                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_E                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_F                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_G                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BYTE_H                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region FLAG_LEVEL              = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region IMB_DIAG_LEVEL          = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region FIPS_FLAGSTD            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01             = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region COMPILER_MODE           = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region SPACE_01                = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region TERN_OPTS_DATA          = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region TEST_SUBOPTS            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region OUTDD_NAME_LENGTH       = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RWT_ID_LENGTH           = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region BUILD_LEVEL             = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region PGMNAME_SUBOPTS         = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region ENTRY_INTERFACE_SUBOPTS = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CALLINTERFACE_SUBOPTS   = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region ARITH_SUBOPTS           = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DBCS_REQ                = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region DBCS_ORDPGM_LENGTH      = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region DBCS_ENCTBL_LENGTH      = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region DBCS_ORD_TYPE           = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_02             = staticRegionGenerator.newFixedLength(5);
		private static final Buffer.Region OPTIMIZATION_LEVEL      = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CONVERTED_SO            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CONVERTED_SI            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region LANGUAGE_ID             = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_03             = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region INEXIT_NAME_LENGTH      = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region PRTEXIT_NAME_LENGTH     = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region LIBEXIT_NAME_LENGTH     = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region ADEXIT_NAME_LENGTH      = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region CURROPT                 = staticRegionGenerator.newFixedLength(5);
		private static final Buffer.Region ARCH                    = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_04             = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region CODEPAGE                = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_05             = staticRegionGenerator.newFixedLength(50);
		private static final Buffer.Region LINECNT                 = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_06             = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region BUFSIZE                 = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region RESERVED_07             = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PHASE_RESIDENCE_BYTE_1  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PHASE_RESIDENCE_BYTE_2  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PHASE_RESIDENCE_BYTE_3  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PHASE_RESIDENCE_BYTE_4  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_08             = staticRegionGenerator.newFixedLength(8);
		
		static {
			Helpers.ignore(BYTE_0);
			Helpers.ignore(BYTE_B);
			Helpers.ignore(RESERVED_01);
			Helpers.ignore(RESERVED_02);
			Helpers.ignore(RESERVED_03);
			Helpers.ignore(RESERVED_04);
			Helpers.ignore(RESERVED_05);
			Helpers.ignore(RESERVED_06);
			Helpers.ignore(RESERVED_07);
			Helpers.ignore(PHASE_RESIDENCE_BYTE_3);
			Helpers.ignore(PHASE_RESIDENCE_BYTE_4);
			Helpers.ignore(RESERVED_08);
		}
		
		/**
		 * @author 2oLDNncs 20250208
		 */
		public static enum FlagLevel {
			
			FLAG_I, FLAG_W, FLAG_E, FLAG_S, FLAG_U, NOFLAG;
			
			static final Decoder<Integer, FlagLevel> decoder =
					new ReversibleMap<Integer, FlagLevel>()
					.set(0x00, FLAG_I)
					.set(0x04, FLAG_W)
					.set(0x08, FLAG_E)
					.set(0x0C, FLAG_S)
					.set(0x10, FLAG_U)
					.set(0xFF, NOFLAG)
					;
			
		}
		
		/**
		 * @author 2oLDNncs 20250208
		 */
		public static enum CompilerMode {
			
			NOCOMPILE_I, NOCOMPILE_W, NOCOMPILE_E, NOCOMPILE_S, COMPILE;
			
			static final Decoder<Integer, CompilerMode> decoder =
					new ReversibleMap<Integer, CompilerMode>()
					.set(0x00, NOCOMPILE_I)
					.set(0x04, NOCOMPILE_W)
					.set(0x08, NOCOMPILE_E)
					.set(0x0C, NOCOMPILE_S)
					.set(0xFF, COMPILE)
					;
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_ExternalSymbol extends RecData {
		
		public final EnumVar<SectionType> vSectionType      = this.newEnumVar(SECTION_TYPE, SectionType.decoder);
		public final LongVar              vSymbolId         = this.newLongVar(SYMBOL_ID);
		public final LongVar              vLineNumber       = this.newLongVar(LINE_NUMBER);
		public final StringVar            vExternalName     = this.newStringVarV(EXTERNAL_NAME_LENGTH);
		private final StringVar           vAliasSectionName = this.newStringVarV(ALIAS_SECTION_NAME_LENGTH);
		
		public RecData_ExternalSymbol(final Buffer buffer) {
			super(buffer);
			Helpers.ignore(this.vAliasSectionName);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_ExternalSymbol.class);
		
		private static final Buffer.Region SECTION_TYPE              = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region FLAGS                     = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01               = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region SYMBOL_ID                 = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region LINE_NUMBER               = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region SECTION_LENGTH            = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region LD_ID                     = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region RESERVED_02               = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region EXTERNAL_NAME_LENGTH      = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region ALIAS_SECTION_NAME_LENGTH = staticRegionGenerator.newFixedLength(4);
		
		static {
			Helpers.ignore(FLAGS);
			Helpers.ignore(RESERVED_01);
			Helpers.ignore(SECTION_LENGTH);
			Helpers.ignore(LD_ID);
			Helpers.ignore(RESERVED_02);
		}
		
		/**
		 * @author 2oLDNncs 20250208
		 */
		public static enum SectionType {
			
			PROGRAM_ID, ENTRY, EXTERNAL_REFERENCE,
			X04, X05, X06, X0A,
			INTERNAL_REFERENCE, EXTERNAL_CLASS_NAME, METHOD_ID_NAME, METHOD_REFERENCE,
			XFF;
			
			static final Decoder<Integer, SectionType> decoder =
					new ReversibleMap<Integer, SectionType>()
					.set(0x00, PROGRAM_ID)
					.set(0x01, ENTRY)
					.set(0x02, EXTERNAL_REFERENCE)
					.set(0x04, X04)
					.set(0x05, X05)
					.set(0x06, X06)
					.set(0x0A, X0A)
					.set(0x12, INTERNAL_REFERENCE)
					.set(0xC0, EXTERNAL_CLASS_NAME)
					.set(0xC1, METHOD_ID_NAME)
					.set(0xC6, METHOD_REFERENCE)
					.set(0xFF, XFF)
					;
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static final class SuppliedDecoder<K, V> implements Decoder<K, V> {
		
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
	
	/**
	 * @author 2oLDNncs 20250209
	 *
	 * @param <V>
	 */
	public static final class KeyMaskingDecoder<V> implements Decoder<Integer, V> {
		
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
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_ParseTree extends RecData {
		
		public final LongVar              vNodeNumber            = this.newLongVar(NODE_NUMBER);
		public final EnumVar<NodeType>    vNodeType              = this.newEnumVar(NODE_TYPE, NodeType.decoder);
		public final EnumVar<NodeSubtype> vNodeSubtype           = this.newEnumVar(NODE_SUBTYPE, new SuppliedDecoder<>(() -> this.vNodeType.get().subtypeDecoder));
		public final LongVar              vParentNodeNumber      = this.newLongVar(PARENT_NODE_NUMBER);
		public final LongVar              vLeftSIblingNodeNumber = this.newLongVar(LEFT_SIBLING_NODE_NUMBER);
		public final LongVar              vSymbolId              = this.newLongVar(SYMBOL_ID);
		public final LongVar              vSectionSymbolId       = this.newLongVar(SECTION_SYMBOL_ID);
		public final LongVar              vFirstTokenNumber      = this.newLongVar(FIRST_TOKEN_NUMBER);
		public final LongVar              vLastTokenNumber       = this.newLongVar(LAST_TOKEN_NUMBER);
		public final EnumVar<Flags>       vFlags                 = this.newEnumVar(FLAGS, Flags.decoder);
		
		public RecData_ParseTree(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_ParseTree.class);
		
		private static final Buffer.Region NODE_NUMBER              = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region NODE_TYPE                = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region NODE_SUBTYPE             = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region PARENT_NODE_NUMBER       = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region LEFT_SIBLING_NODE_NUMBER = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region SYMBOL_ID                = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region SECTION_SYMBOL_ID        = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region FIRST_TOKEN_NUMBER       = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region LAST_TOKEN_NUMBER        = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region RESERVED_01              = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region FLAGS                    = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_02              = staticRegionGenerator.newFixedLength(3);
		
		static {
			Helpers.ignore(RESERVED_01);
			Helpers.ignore(RESERVED_02);
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum Flags {
			
			NOFLAGS,
			RESERVED_01,
			GENERATED_NODE_NO_TOKEN,
			;
			
			static final Decoder<Integer, Flags> decoder =
					new ReversibleMap<Integer, Flags>()
					.set(0x00, NOFLAGS)
					.set(0x80, RESERVED_01)
					.set(0x40, GENERATED_NODE_NO_TOKEN)
					;
			
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum NodeType {
			
			PROGRAM,
			CLASS,
			METHOD,
			IDENTIFICATION_DIVISION,
			ENVIRONMENT_DIVISION,
			DATA_DIVISION,
			PROCEDURE_DIVISION,
			END_PROGRAM_METHOD_CLASS,
			DECLARATIVES_BODY,
			NONDECLARATIVES_BODY,
			SECTION(NodeSubtype.decoder1),
			PROCEDURE_SECTION,
			PARAGRAPH(NodeSubtype.decoder2),
			PROCEDURE_PARAGRAPH,
			SENTENCE,
			FILE_DEFINITION,
			SORT_FILE_DEFINITION,
			PROGRAM_NAME,
			PROGRAM_ATTRIBUTE,
			ENVIRONMENT_DIVISION_CLAUSE(NodeSubtype.decoder3),
			CLASS_ATTRIBUTE,
			METHOD_ATTRIBUTE,
			USE_STATEMENT,
			STATEMENT(NodeSubtype.decoder6),
			DATA_DESCRIPTION_CLAUSE(NodeSubtype.decoder4),
			DATA_ENTRY,
			FILE_DESCRIPTION_CLAUSE(NodeSubtype.decoder5),
			DATA_ENTRY_NAME,
			DATA_ENTRY_LEVEL,
			EXEC_ENTRY,
			EVALUATE_SUBJECT_PHRASE,
			EVALUATE_WHEN_PHRASE,
			EVALUATE_WHEN_OTHER_PHRASE,
			SEARCH_WHEN_PHRASE,
			INSPECT_CONVERTING_PHRASE,
			INSPECT_REPLACING_PHRASE,
			INSPECT_TALLYING_PHRASE,
			PERFORM_UNTIL_PHRASE,
			PERFORM_VARYING_PHRASE,
			PERFORM_AFTER_PHRASE,
			STATEMENT_BLOCK,
			SCOPE_TERMINATOR,
			INITIALIZE_REPLACING_PHRASE,
			EXEC_CICS_COMMAND,
			INITIALIZE_WITH_FILLER,
			INITIALIZE_TO_VALUE,
			INITIALIZE_TO_DEFAULT,
			ALLOCATE_INITIALIZED,
			ALLOCATE_LOC,
			DATA_DIVISION_PHRASE,
			PHRASE(NodeSubtype.decoder7),
			ON_PHRASE,
			NOT_PHRASE,
			THEN_PHRASE,
			ELSE_PHRASE,
			CONDITION,
			EXPRESSION,
			RELATIVE_INDEXING,
			EXEC_CICS_OPTION,
			RESERVED_WORD(NodeSubtype.decoder11),
			INITIALIZE_REPLACING_CATEGORY,
			SECTION_OR_PARAGRAPH_NAME,
			IDENTIFIER(NodeSubtype.decoder12),
			ALPHABET_NAME,
			CLASS_NAME,
			CONDITION_NAME(NodeSubtype.decoder12),
			FILE_NAME,
			INDEX_NAME(NodeSubtype.decoder12),
			MNEMONIC_NAME(NodeSubtype.decoder12),
			SYMBOLIC_CHARACTER,
			LITERAL,
			FUNCTION_IDENTIFIER(NodeSubtype.decoder8),
			DATA_NAME(NodeSubtype.decoder12),
			SPECIAL_REGISTER(NodeSubtype.decoder9),
			PROCEDURE_REFERENCE(NodeSubtype.decoder14),
			ARITHMETIC_OPERATOR(NodeSubtype.decoder16),
			ALL_PROCEDURES,
			INITIALIZE_LITERAL_NO_TOKENS(NodeSubtype.decoder13),
			ALL_LITERAL_OR_FIGCON,
			KEYWORD_CLASS_TEST_NAME(NodeSubtype.decoder10),
			RESERVED_WORD_AT_IDENTIFIER_LEVEL(NodeSubtype.decoder15),
			UNARY_OPERATOR,
			RELATIONAL_OPERATOR(NodeSubtype.decoder17),
			SUBSCRIPT,
			REFERENCE_MODIFICATION,
			;
			
			final Decoder<Integer, NodeSubtype> subtypeDecoder;
			
			private NodeType() {
				this(NodeSubtype.decoder0);
			}
			
			private NodeType(final Decoder<Integer, NodeSubtype> subtypeDecoder) {
				this.subtypeDecoder = subtypeDecoder;
			}
			
			static final Decoder<Integer, NodeType> decoder =
					new ReversibleMap<Integer, NodeType>()
					.set(1, PROGRAM)
					.set(2, CLASS)
					.set(3, METHOD)
					.set(101, IDENTIFICATION_DIVISION)
					.set(102, ENVIRONMENT_DIVISION)
					.set(103, DATA_DIVISION)
					.set(104, PROCEDURE_DIVISION)
					.set(105, END_PROGRAM_METHOD_CLASS)
					.set(201, DECLARATIVES_BODY)
					.set(202, NONDECLARATIVES_BODY)
					.set(301, SECTION)
					.set(302, PROCEDURE_SECTION)
					.set(401, PARAGRAPH)
					.set(402, PROCEDURE_PARAGRAPH)
					.set(501, SENTENCE)
					.set(502, FILE_DEFINITION)
					.set(503, SORT_FILE_DEFINITION)
					.set(504, PROGRAM_NAME)
					.set(505, PROGRAM_ATTRIBUTE)
					.set(508, ENVIRONMENT_DIVISION_CLAUSE)
					.set(509, CLASS_ATTRIBUTE)
					.set(510, METHOD_ATTRIBUTE)
					.set(511, USE_STATEMENT)
					.set(601, STATEMENT)
					.set(602, DATA_DESCRIPTION_CLAUSE)
					.set(603, DATA_ENTRY)
					.set(604, FILE_DESCRIPTION_CLAUSE)
					.set(605, DATA_ENTRY_NAME)
					.set(606, DATA_ENTRY_LEVEL)
					.set(607, EXEC_ENTRY)
					.set(701, EVALUATE_SUBJECT_PHRASE)
					.set(702, EVALUATE_WHEN_PHRASE)
					.set(703, EVALUATE_WHEN_OTHER_PHRASE)
					.set(704, SEARCH_WHEN_PHRASE)
					.set(705, INSPECT_CONVERTING_PHRASE)
					.set(706, INSPECT_REPLACING_PHRASE)
					.set(707, INSPECT_TALLYING_PHRASE)
					.set(708, PERFORM_UNTIL_PHRASE)
					.set(709, PERFORM_VARYING_PHRASE)
					.set(710, PERFORM_AFTER_PHRASE)
					.set(711, STATEMENT_BLOCK)
					.set(712, SCOPE_TERMINATOR)
					.set(713, INITIALIZE_REPLACING_PHRASE)
					.set(714, EXEC_CICS_COMMAND)
					.set(715, INITIALIZE_WITH_FILLER)
					.set(716, INITIALIZE_TO_VALUE)
					.set(717, INITIALIZE_TO_DEFAULT)
					.set(718, ALLOCATE_INITIALIZED)
					.set(719, ALLOCATE_LOC)
					.set(720, DATA_DIVISION_PHRASE)
					.set(801, PHRASE)
					.set(802, ON_PHRASE)
					.set(803, NOT_PHRASE)
					.set(804, THEN_PHRASE)
					.set(805, ELSE_PHRASE)
					.set(806, CONDITION)
					.set(807, EXPRESSION)
					.set(808, RELATIVE_INDEXING)
					.set(809, EXEC_CICS_OPTION)
					.set(810, RESERVED_WORD)
					.set(811, INITIALIZE_REPLACING_CATEGORY)
					.set(901, SECTION_OR_PARAGRAPH_NAME)
					.set(902, IDENTIFIER)
					.set(903, ALPHABET_NAME)
					.set(904, CLASS_NAME)
					.set(905, CONDITION_NAME)
					.set(906, FILE_NAME)
					.set(907, INDEX_NAME)
					.set(908, MNEMONIC_NAME)
					.set(910, SYMBOLIC_CHARACTER)
					.set(911, LITERAL)
					.set(912, FUNCTION_IDENTIFIER)
					.set(913, DATA_NAME)
					.set(914, SPECIAL_REGISTER)
					.set(915, PROCEDURE_REFERENCE)
					.set(916, ARITHMETIC_OPERATOR)
					.set(917, ALL_PROCEDURES)
					.set(918, INITIALIZE_LITERAL_NO_TOKENS)
					.set(919, ALL_LITERAL_OR_FIGCON)
					.set(920, KEYWORD_CLASS_TEST_NAME)
					.set(921, RESERVED_WORD_AT_IDENTIFIER_LEVEL)
					.set(922, UNARY_OPERATOR)
					.set(923, RELATIONAL_OPERATOR)
					.set(1001, SUBSCRIPT)
					.set(1002, REFERENCE_MODIFICATION)
					;
			
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum NodeSubtype {
			
			NONE,
			CONFIGURATION_SECTION,
			INPUT_OUTPUT_SECTION,
			FILE_SECTION,
			WORKING_STORAGE_SECTION,
			LINKAGE_SECTION,
			LOCAL_STORAGE_SECTION,
			REPOSITORY_SECTION,
			PROGRAM_ID_PARAGRAPH,
			AUTHOR_PARAGRAPH,
			INSTALLATION_PARAGRAPH,
			DATE_WRITTEN_PARAGRAPH,
			SECURITY_PARAGRAPH,
			SOURCE_COMPUTER_PARAGRAPH,
			OBJECT_COMPUTER_PARAGRAPH,
			SPECIAL_NAMES_PARAGRAPH,
			FILE_CONTROL_PARAGRAPH,
			I_O_CONTROL_PARAGRAPH,
			DATE_COMPILED_PARAGRAPH,
			CLASS_ID_PARAGRAPH,
			METHOD_ID_PARAGRAPH,
			REPOSITORY_PARAGRAPH,
			WITH_DEBUGGING_MODE,
			MEMORY_SIZE,
			SEGMENT_LIMIT,
			CURRENCY_SIGN,
			DECIMAL_POINT,
			PROGRAM_COLLATING_SEQUENCE,
			ALPHABET,
			SYMBOLIC_CHARACTER,
			CLASS,
			ENVIRONMENT_NAME,
			SELECT,
			XML_SCHEMA,
			BLANK_WHEN_ZERO,
			DATA_NAME_OR_FILLER,
			JUSTIFIED,
			OCCURS,
			PICTURE,
			REDEFINES,
			RENAMES,
			SIGN,
			SYNCHRONIZED,
			USAGE,
			VALUE,
			VOLATILE,
			GLOBAL_4,
			EXTERNAL_4,
			FILE_STATUS,
			ORGANIZATION,
			ACCESS_MODE,
			RECORD_KEY,
			ASSIGN,
			RELATIVE_KEY,
			PASSWORD,
			PROCESSING_MODE,
			RECORD_DELIMITER,
			PADDING_CHARACTER,
			BLOCK_CONTAINS,
			RECORD_CONTAINS,
			LABEL_RECORDS,
			VALUE_OF,
			DATA_RECORDS,
			LINAGE,
			ALTERNATE_KEY,
			LINES_AT_TOP_5,
			LINES_AT_BOTTOM_5,
			CODE_SET,
			RECORDING_MODE,
			RESERVE,
			GLOBAL_5,
			EXTERNAL_5,
			LOCK,
			NEXT_SENTENCE,
			ACCEPT,
			ADD,
			ALTER,
			CALL,
			CANCEL,
			CLOSE,
			COMPUTE,
			CONTINUE,
			DELETE,
			DISPLAY,
			DIVIDE_INTO,
			DIVIDE_BY,
			ENTER,
			ENTRY,
			EVALUATE,
			EXIT,
			GO,
			GOBACK,
			IF,
			INITIALIZE,
			INSPECT,
			INVOKE,
			MERGE,
			MOVE,
			MULTIPLY,
			OPEN,
			PERFORM,
			READ,
			READY,
			RELEASE,
			RESET,
			RETURN,
			REWRITE,
			SEARCH,
			SERVICE,
			SET,
			SORT,
			START,
			STOP,
			STRING,
			SUBTRACT,
			UNSTRING,
			EXEC_SQL,
			EXEC_CICS,
			WRITE,
			XML,
			ALLOCATE,
			FREE,
			JSON,
			INTO,
			DELIMITED,
			INITIALIZE_REPLACING,
			INSPECT_ALL,
			INSPECT_LEADING,
			SET_TO,
			SET_UP,
			SET_DOWN,
			PERFORM_TIMES,
			DIVIDE_REMAINDER_7,
			INSPECT_FIRST,
			SEARCH_VARYING,
			MORE_LABELS,
			SEARCH_ALL,
			SEARCH_AT_END,
			SEARCH_TEST_INDEX,
			GLOBAL_7,
			LABEL,
			DEBUGGING,
			SEQUENCE,
			RESERVED_01,
			RESERVED_02,
			RESERVED_03,
			TALLYING,
			RESERVED_04,
			ON_SIZE_ERROR,
			ON_OVERFLOW,
			ON_ERROR,
			AT_END,
			INVALID_KEY,
			END_OF_PAGE,
			USING,
			BEFORE,
			AFTER,
			EXCEPTION,
			CORRESPONDING,
			RESERVED_05,
			RETURNING,
			GIVING,
			THROUGH,
			KEY,
			DELIMITER,
			POINTER,
			COUNT,
			METHOD,
			PROGRAM,
			INPUT,
			OUTPUT,
			I_O,
			EXTEND,
			RELOAD,
			ASCENDING,
			DESCENDING,
			DUPLICATES,
			NATIVE_USAGE,
			INDEXED,
			FROM,
			FOOTING,
			LINES_AT_BOTTOM_7,
			LINES_AT_TOP_7,
			XML_ENCODING,
			XML_GENERATE_XML_DECLARATION,
			XML_GENERATE_ATTRIBUTES,
			XML_GENERATE_NAMESPACE,
			XML_PARSE_PROCESSING,
			XML_PARSE_VALIDATING,
			XML_GENERATE_NAME,
			XML_GENERATE_TYPE,
			XML_GENERATE_SUPPRESS,
			COS,
			LOG,
			MAX,
			MIN,
			MOD,
			ORD,
			REM,
			SIN,
			SUM,
			TAN,
			ACOS,
			ASIN,
			ATAN,
			CHAR,
			MEAN,
			SQRT,
			LOG10,
			RANGE,
			LENGTH,
			MEDIAN,
			NUMVAL,
			RANDOM,
			ANNUITY,
			INTEGER,
			ORD_MAX,
			ORD_MIN,
			REVERSE,
			MIDRANGE,
			NUMVAL_C,
			VARIANCE,
			FACTORIAL,
			LOWER_CASE,
			UPPER_CASE,
			CURRENT_DATE,
			INTEGER_PART,
			PRESENT_VALUE,
			WHEN_COMPILED_8,
			DAY_OF_INTEGER,
			INTEGER_OF_DAY,
			DATE_OF_INTEGER,
			INTEGER_OF_DATE,
			STANDARD_DEVIATION,
			YEAR_TO_YYYY,
			DAY_TO_YYYYDDD,
			DATE_TO_YYYYMMDD,
			DISPLAY_OF,
			NATIONAL_OF,
			UPOS,
			UVALID,
			UWIDTH,
			ULENGTH,
			USUBSTR,
			USUPPLEMENTARY,
			ADDRESS_OF,
			LENGTH_OF,
			ALPHABETIC_10,
			ALPHABETIC_LOWER,
			ALPHABETIC_UPPER,
			DBCS,
			KANJI,
			NUMERIC,
			NEGATIVE,
			POSITIVE,
			ZERO,
			TRUE_11,
			FALSE,
			ANY,
			THRU,
			REFERENCED,
			CHANGED,
			REFERENCED_CHANGED,
			ALPHABETIC_13,
			ALPHANUMERIC,
			NUMERIC_13,
			ALPHANUMERIC_EDITED,
			NUMERIC_EDITED,
			DBCS_EGCS,
			NATIONAL,
			NATIONAL_EDITED,
			SECTION,
			PARAGRAPH,
			ROUNDED,
			TRUE_15,
			ON,
			OFF,
			SIZE,
			DATE,
			DAY,
			DAY_OF_WEEK,
			TIME,
			WHEN_COMPILED_15,
			PAGE,
			DATE_YYYYMMDD,
			DAY_YYYYDDD,
			ATTRIBUTE,
			ELEMENT,
			CONTENT,
			NUMERIC_15,
			NONNUMERIC,
			EVERY,
			WHEN,
			PLUS,
			MINUS,
			TIMES,
			DIVIDE,
			DIVIDE_REMAINDER_16,
			EXPONENTIATE,
			NEGATE,
			LESS,
			LESS_OR_EQUAL,
			EQUAL,
			NOT_EQUAL,
			GREATER,
			GREATER_OR_EQUAL,
			AND,
			OR,
			CLASS_CONDITION,
			NOT_CLASS_CONDITION,
			;
			
			static final Decoder<Integer, NodeSubtype> decoder0 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(0, NONE)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder1 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, CONFIGURATION_SECTION)
					.set(2, INPUT_OUTPUT_SECTION)
					.set(3, FILE_SECTION)
					.set(4, WORKING_STORAGE_SECTION)
					.set(5, LINKAGE_SECTION)
					.set(6, LOCAL_STORAGE_SECTION)
					.set(7, REPOSITORY_SECTION)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder2 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, PROGRAM_ID_PARAGRAPH)
					.set(2, AUTHOR_PARAGRAPH)
					.set(3, INSTALLATION_PARAGRAPH)
					.set(4, DATE_WRITTEN_PARAGRAPH)
					.set(5, SECURITY_PARAGRAPH)
					.set(6, SOURCE_COMPUTER_PARAGRAPH)
					.set(7, OBJECT_COMPUTER_PARAGRAPH)
					.set(8, SPECIAL_NAMES_PARAGRAPH)
					.set(9, FILE_CONTROL_PARAGRAPH)
					.set(10, I_O_CONTROL_PARAGRAPH)
					.set(11, DATE_COMPILED_PARAGRAPH)
					.set(12, CLASS_ID_PARAGRAPH)
					.set(13, METHOD_ID_PARAGRAPH)
					.set(14, REPOSITORY_PARAGRAPH)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder3 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, WITH_DEBUGGING_MODE)
					.set(2, MEMORY_SIZE)
					.set(3, SEGMENT_LIMIT)
					.set(4, CURRENCY_SIGN)
					.set(5, DECIMAL_POINT)
					.set(6, PROGRAM_COLLATING_SEQUENCE)
					.set(7, ALPHABET)
					.set(8, SYMBOLIC_CHARACTER)
					.set(9, CLASS)
					.set(10, ENVIRONMENT_NAME)
					.set(11, SELECT)
					.set(12, XML_SCHEMA)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder4 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, BLANK_WHEN_ZERO)
					.set(2, DATA_NAME_OR_FILLER)
					.set(3, JUSTIFIED)
					.set(4, OCCURS)
					.set(5, PICTURE)
					.set(6, REDEFINES)
					.set(7, RENAMES)
					.set(8, SIGN)
					.set(9, SYNCHRONIZED)
					.set(10, USAGE)
					.set(11, VALUE)
					.set(12, VOLATILE)
					.set(23, GLOBAL_4)
					.set(24, EXTERNAL_4)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder5 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, FILE_STATUS)
					.set(2, ORGANIZATION)
					.set(3, ACCESS_MODE)
					.set(4, RECORD_KEY)
					.set(5, ASSIGN)
					.set(6, RELATIVE_KEY)
					.set(7, PASSWORD)
					.set(8, PROCESSING_MODE)
					.set(9, RECORD_DELIMITER)
					.set(10, PADDING_CHARACTER)
					.set(11, BLOCK_CONTAINS)
					.set(12, RECORD_CONTAINS)
					.set(13, LABEL_RECORDS)
					.set(14, VALUE_OF)
					.set(15, DATA_RECORDS)
					.set(16, LINAGE)
					.set(17, ALTERNATE_KEY)
					.set(18, LINES_AT_TOP_5)
					.set(19, LINES_AT_BOTTOM_5)
					.set(20, CODE_SET)
					.set(21, RECORDING_MODE)
					.set(22, RESERVE)
					.set(23, GLOBAL_5)
					.set(24, EXTERNAL_5)
					.set(25, LOCK)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder6 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(2, NEXT_SENTENCE)
					.set(3, ACCEPT)
					.set(4, ADD)
					.set(5, ALTER)
					.set(6, CALL)
					.set(7, CANCEL)
					.set(8, CLOSE)
					.set(9, COMPUTE)
					.set(10, CONTINUE)
					.set(11, DELETE)
					.set(12, DISPLAY)
					.set(13, DIVIDE_INTO)
					.set(113, DIVIDE_BY)
					.set(14, ENTER)
					.set(15, ENTRY)
					.set(16, EVALUATE)
					.set(17, EXIT)
					.set(18, GO)
					.set(19, GOBACK)
					.set(20, IF)
					.set(21, INITIALIZE)
					.set(22, INSPECT)
					.set(23, INVOKE)
					.set(24, MERGE)
					.set(25, MOVE)
					.set(26, MULTIPLY)
					.set(27, OPEN)
					.set(28, PERFORM)
					.set(29, READ)
					.set(30, READY)
					.set(31, RELEASE)
					.set(32, RESET)
					.set(33, RETURN)
					.set(34, REWRITE)
					.set(35, SEARCH)
					.set(36, SERVICE)
					.set(37, SET)
					.set(38, SORT)
					.set(39, START)
					.set(40, STOP)
					.set(41, STRING)
					.set(42, SUBTRACT)
					.set(43, UNSTRING)
					.set(44, EXEC_SQL)
					.set(144, EXEC_CICS)
					.set(45, WRITE)
					.set(46, XML)
					.set(47, ALLOCATE)
					.set(48, FREE)
					.set(49, JSON)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder7 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, INTO)
					.set(2, DELIMITED)
					.set(3, INITIALIZE_REPLACING)
					.set(4, INSPECT_ALL)
					.set(5, INSPECT_LEADING)
					.set(6, SET_TO)
					.set(7, SET_UP)
					.set(8, SET_DOWN)
					.set(9, PERFORM_TIMES)
					.set(10, DIVIDE_REMAINDER_7)
					.set(11, INSPECT_FIRST)
					.set(12, SEARCH_VARYING)
					.set(13, MORE_LABELS)
					.set(14, SEARCH_ALL)
					.set(15, SEARCH_AT_END)
					.set(16, SEARCH_TEST_INDEX)
					.set(17, GLOBAL_7)
					.set(18, LABEL)
					.set(19, DEBUGGING)
					.set(20, SEQUENCE)
					.set(21, RESERVED_01)
					.set(22, RESERVED_02)
					.set(23, RESERVED_03)
					.set(24, TALLYING)
					.set(25, RESERVED_04)
					.set(26, ON_SIZE_ERROR)
					.set(27, ON_OVERFLOW)
					.set(28, ON_ERROR)
					.set(29, AT_END)
					.set(30, INVALID_KEY)
					.set(31, END_OF_PAGE)
					.set(32, USING)
					.set(33, BEFORE)
					.set(34, AFTER)
					.set(35, EXCEPTION)
					.set(36, CORRESPONDING)
					.set(37, RESERVED_05)
					.set(38, RETURNING)
					.set(39, GIVING)
					.set(40, THROUGH)
					.set(41, KEY)
					.set(42, DELIMITER)
					.set(43, POINTER)
					.set(44, COUNT)
					.set(45, METHOD)
					.set(46, PROGRAM)
					.set(47, INPUT)
					.set(48, OUTPUT)
					.set(49, I_O)
					.set(50, EXTEND)
					.set(51, RELOAD)
					.set(52, ASCENDING)
					.set(53, DESCENDING)
					.set(54, DUPLICATES)
					.set(55, NATIVE_USAGE)
					.set(56, INDEXED)
					.set(57, FROM)
					.set(58, FOOTING)
					.set(59, LINES_AT_BOTTOM_7)
					.set(60, LINES_AT_TOP_7)
					.set(61, XML_ENCODING)
					.set(62, XML_GENERATE_XML_DECLARATION)
					.set(63, XML_GENERATE_ATTRIBUTES)
					.set(64, XML_GENERATE_NAMESPACE)
					.set(65, XML_PARSE_PROCESSING)
					.set(66, XML_PARSE_VALIDATING)
					.set(67, XML_GENERATE_NAME)
					.set(68, XML_GENERATE_TYPE)
					.set(69, XML_GENERATE_SUPPRESS)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder8 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, COS)
					.set(2, LOG)
					.set(3, MAX)
					.set(4, MIN)
					.set(5, MOD)
					.set(6, ORD)
					.set(7, REM)
					.set(8, SIN)
					.set(9, SUM)
					.set(10, TAN)
					.set(11, ACOS)
					.set(12, ASIN)
					.set(13, ATAN)
					.set(14, CHAR)
					.set(15, MEAN)
					.set(16, SQRT)
					.set(17, LOG10)
					.set(18, RANGE)
					.set(19, LENGTH)
					.set(20, MEDIAN)
					.set(21, NUMVAL)
					.set(22, RANDOM)
					.set(23, ANNUITY)
					.set(24, INTEGER)
					.set(25, ORD_MAX)
					.set(26, ORD_MIN)
					.set(27, REVERSE)
					.set(28, MIDRANGE)
					.set(29, NUMVAL_C)
					.set(30, VARIANCE)
					.set(31, FACTORIAL)
					.set(32, LOWER_CASE)
					.set(33, UPPER_CASE)
					.set(34, CURRENT_DATE)
					.set(35, INTEGER_PART)
					.set(36, PRESENT_VALUE)
					.set(37, WHEN_COMPILED_8)
					.set(38, DAY_OF_INTEGER)
					.set(39, INTEGER_OF_DAY)
					.set(40, DATE_OF_INTEGER)
					.set(41, INTEGER_OF_DATE)
					.set(42, STANDARD_DEVIATION)
					.set(43, YEAR_TO_YYYY)
					.set(44, DAY_TO_YYYYDDD)
					.set(45, DATE_TO_YYYYMMDD)
					.set(49, DISPLAY_OF)
					.set(50, NATIONAL_OF)
					.set(51, UPOS)
					.set(52, UVALID)
					.set(53, UWIDTH)
					.set(54, ULENGTH)
					.set(55, USUBSTR)
					.set(56, USUPPLEMENTARY)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder9 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, ADDRESS_OF)
					.set(2, LENGTH_OF)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder10 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, ALPHABETIC_10)
					.set(2, ALPHABETIC_LOWER)
					.set(3, ALPHABETIC_UPPER)
					.set(4, DBCS)
					.set(5, KANJI)
					.set(6, NUMERIC)
					.set(7, NEGATIVE)
					.set(8, POSITIVE)
					.set(9, ZERO)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder11 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, TRUE_11)
					.set(2, FALSE)
					.set(3, ANY)
					.set(4, THRU)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder12 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, REFERENCED)
					.set(2, CHANGED)
					.set(3, REFERENCED_CHANGED)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder13 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, ALPHABETIC_13)
					.set(2, ALPHANUMERIC)
					.set(3, NUMERIC_13)
					.set(4, ALPHANUMERIC_EDITED)
					.set(5, NUMERIC_EDITED)
					.set(6, DBCS_EGCS)
					.set(7, NATIONAL)
					.set(8, NATIONAL_EDITED)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder14 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, SECTION)
					.set(2, PARAGRAPH)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder15 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, ROUNDED)
					.set(2, TRUE_15)
					.set(3, ON)
					.set(4, OFF)
					.set(5, SIZE)
					.set(6, DATE)
					.set(7, DAY)
					.set(8, DAY_OF_WEEK)
					.set(9, TIME)
					.set(10, WHEN_COMPILED_15)
					.set(11, PAGE)
					.set(12, DATE_YYYYMMDD)
					.set(13, DAY_YYYYDDD)
					.set(14, ATTRIBUTE)
					.set(15, ELEMENT)
					.set(16, CONTENT)
					.set(17, NUMERIC_15)
					.set(18, NONNUMERIC)
					.set(19, EVERY)
					.set(20, WHEN)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder16 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(1, PLUS)
					.set(2, MINUS)
					.set(3, TIMES)
					.set(4, DIVIDE)
					.set(5, DIVIDE_REMAINDER_16)
					.set(6, EXPONENTIATE)
					.set(7, NEGATE)
					;
			
			static final Decoder<Integer, NodeSubtype> decoder17 =
					new ReversibleMap<Integer, NodeSubtype>()
					.set(8, LESS)
					.set(9, LESS_OR_EQUAL)
					.set(10, EQUAL)
					.set(11, NOT_EQUAL)
					.set(12, GREATER)
					.set(13, GREATER_OR_EQUAL)
					.set(14, AND)
					.set(15, OR)
					.set(16, CLASS_CONDITION)
					.set(17, NOT_CLASS_CONDITION)
					;
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_Token extends RecData {
		
		public final LongVar            vTokenNumber = this.newLongVar(TOKEN_NUMBER);
		public final EnumVar<TokenCode> vTokenCode   = this.newEnumVar(TOKEN_CODE, TokenCode.decoder);
		public final LongVar            vTokenColumn = this.newLongVar(TOKEN_COLUMN);
		public final LongVar            vTokenLine   = this.newLongVar(TOKEN_LINE);
		public final EnumVar<Flags>     vFlags       = this.newEnumVar(FLAGS, Flags.decoder);
		public final StringVar          vTokenText   = this.newStringVarV(TOKEN_LENGTH);
		
		public RecData_Token(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Token.class);
		
		private static final Buffer.Region TOKEN_NUMBER = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region TOKEN_CODE   = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region TOKEN_LENGTH = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region TOKEN_COLUMN = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region TOKEN_LINE   = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region FLAGS        = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01  = staticRegionGenerator.newFixedLength(7);
		
		static {
			Helpers.ignore(RESERVED_01);
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum Flags {
			
			NOFLAGS,
			CONTINUED,
			LAST_PIECE,
			;
			
			static final Decoder<Integer, Flags> decoder =
					new ReversibleMap<Integer, Flags>()
					.set(0x00, NOFLAGS)
					.set(0x80, CONTINUED)
					.set(0x40, LAST_PIECE)
					;
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum TokenCode {
			
			PICTURE_STRING,
			TOKEN_PIECE,
			ACCEPT,
			ADD,
			ALTER,
			CALL,
			CANCEL,
			CLOSE,
			COMPUTE,
			DELETE,
			DISPLAY,
			DIVIDE,
			READY,
			END_PERFORM,
			ENTER,
			ENTRY,
			EXIT,
			EXEC_or_EXECUTE,
			GO,
			IF,
			INITIALIZE,
			INVOKE,
			INSPECT,
			MERGE,
			MOVE,
			MULTIPLY,
			OPEN,
			PERFORM,
			READ,
			RELEASE,
			RETURN,
			REWRITE,
			SEARCH,
			SET,
			SORT,
			START,
			STOP,
			STRING,
			SUBTRACT,
			UNSTRING,
			USE,
			WRITE,
			CONTINUE,
			END_ADD,
			END_CALL,
			END_COMPUTE,
			END_DELETE,
			END_DIVIDE,
			END_EVALUATE,
			END_IF,
			END_MULTIPLY,
			END_READ,
			END_RETURN,
			END_REWRITE,
			END_SEARCH,
			END_START,
			END_STRING,
			END_SUBTRACT,
			END_UNSTRING,
			END_WRITE,
			GOBACK,
			EVALUATE,
			RESET,
			SERVICE,
			END_INVOKE,
			END_EXEC,
			XML,
			END_XML,
			ALLOCATE,
			FREE,
			JSON,
			END_JSON,
			FOREIGN_VERB,
			DATA_NAME,
			DASHED_NUM,
			DECIMAL,
			DIV_SIGN,
			EQ,
			EXPONENTIATION,
			GT,
			INTEGER,
			LT,
			LPAREN,
			MINUS_SIGN,
			MULT_SIGN,
			NONUMLIT,
			PERIOD,
			PLUS_SIGN,
			RPAREN,
			SIGNED_INTEGER,
			QUID,
			COLON,
			IEOF,
			EGCS_LIT,
			COMMA_SPACE,
			SEMICOLON_SPACE,
			PROCEDURE_NAME,
			FLT_POINT_LIT,
			LANGUAGE_ENVIRONMENT,
			GE,
			IDREF,
			EXPREF,
			CICS,
			NEW,
			NATIONAL_LIT,
			ADDRESS,
			ADVANCING,
			AFTER,
			ALL,
			ALPHABETIC,
			ALPHANUMERIC,
			ANY,
			AND,
			ALPHANUMERIC_EDITED,
			BEFORE,
			BEGINNING,
			FUNCTION,
			CONTENT,
			CORR_or_CORRESPONDING,
			DAY,
			DATE,
			DEBUG_CONTENTS,
			DEBUG_ITEM,
			DEBUG_LINE,
			DEBUG_NAME,
			DEBUG_SUB_1,
			DEBUG_SUB_2,
			DEBUG_SUB_3,
			DELIMITED,
			DELIMITER,
			DOWN,
			NUMERIC_EDITED,
			XML_EVENT,
			END_OF_PAGE_or_EOP,
			EQUAL,
			ERROR,
			XML_NTEXT,
			EXCEPTION,
			EXTEND,
			FIRST,
			FROM,
			GIVING,
			GREATER,
			I_O,
			IN,
			INITIAL,
			INTO,
			INVALID,
			SQL,
			LESS,
			LINAGE_COUNTER,
			XML_TEXT,
			LOCK,
			GENERATE,
			NEGATIVE,
			NEXT,
			NO,
			NOT,
			NUMERIC,
			KANJI,
			OR,
			OTHER,
			OVERFLOW,
			PAGE,
			CONVERTING,
			POINTER,
			POSITIVE,
			DBCS,
			PROCEDURES,
			PROCEED,
			REFERENCES,
			DAY_OF_WEEK,
			REMAINDER,
			REMOVAL,
			REPLACING,
			REVERSED,
			REWIND,
			ROUNDED,
			RUN,
			SENTENCE,
			STANDARD,
			RETURN_CODE_etc, // Also: SORT-CORE-SIZE, SORT-FILE-SIZE, SORT-MESSAGE, SORT-MODE-SIZE, SORT-RETURN, TALLY, XML-CODE
			TALLYING,
			SUM,
			TEST,
			THAN,
			UNTIL,
			UP,
			UPON,
			VARYING,
			RELOAD,
			TRUE,
			THEN,
			RETURNING,
			ELSE,
			SELF,
			SUPER,
			WHEN_COMPILED,
			ENDING,
			FALSE,
			REFERENCE,
			NATIONAL_EDITED,
			COM_REG,
			ALPHABETIC_LOWER,
			ALPHABETIC_UPPER,
			REDEFINES,
			OCCURS,
			SYNC_or_SYNCHRONIZED,
			MORE_LABELS,
			JUST_or_JUSTIFIED,
			SHIFT_IN,
			BLANK,
			VALUE,
			COMP_or_COMPUTATIONAL,
			COMP_1_or_COMPUTATIONAL_1,
			COMP_3_or_COMPUTATIONAL_3,
			COMP_2_or_COMPUTATIONAL_2,
			COMP_4_or_COMPUTATIONAL_4,
			DISPLAY_1,
			SHIFT_OUT,
			INDEX,
			USAGE,
			SIGN,
			LEADING,
			SEPARATE,
			INDEXED,
			LEFT,
			RIGHT,
			PIC_or_PICTURE,
			VALUES,
			GLOBAL,
			EXTERNAL,
			BINARY,
			PACKED_DECIMAL,
			EGCS,
			PROCEDURE_POINTER,
			COMP_5_or_COMPUTATIONAL_5,
			FUNCTION_POINTER,
			TYPE,
			JNIENVPTR,
			NATIONAL,
			GROUP_USAGE,
			VOLATILE,
			HIGH_VALUE_or_HIGH_VALUES,
			LOW_VALUE_or_LOW_VALUES,
			QUOTE_or_QUOTES,
			SPACE_or_SPACES,
			ZERO,
			ZEROES_or_ZEROS,
			NULL_or_NULLS,
			BLOCK,
			BOTTOM,
			CHARACTER,
			CODE,
			CODE_SET,
			FILLER,
			FOOTING,
			LABEL,
			LENGTH,
			LINAGE,
			OMITTED,
			RENAMES,
			TOP,
			TRAILING,
			RECORDING,
			INHERITS,
			RECURSIVE,
			ACCESS,
			ALSO,
			ALTERNATE,
			AREA_or_AREAS,
			ASSIGN,
			COLLATING,
			COMMA,
			CURRENCY,
			CLASS,
			DECIMAL_POINT,
			DUPLICATES,
			DYNAMIC,
			EVERY,
			MEMORY,
			MODE,
			MODULES,
			MULTIPLE,
			NATIVE,
			OFF,
			OPTIONAL,
			ORGANIZATION,
			POSITION,
			PROGRAM,
			RANDOM,
			RELATIVE,
			RERUN,
			RESERVE,
			SAME,
			SEGMENT_LIMIT,
			SELECT,
			SEQUENCE,
			SEQUENTIAL,
			SORT_MERGE,
			STANDARD_1,
			TAPE,
			WORDS,
			PROCESSING,
			APPLY,
			WRITE_ONLY,
			COMMON,
			ALPHABET,
			PADDING,
			SYMBOLIC,
			STANDARD_2,
			OVERRIDE,
			PASSWORD,
			XML_SCHEMA,
			ARE_or_IS,
			ASCENDING,
			AT,
			BY,
			CHARACTERS,
			CONTAINS,
			COUNT,
			DEBUGGING,
			DEPENDING,
			DESCENDING,
			DIVISION,
			FOR,
			ORDER,
			INPUT,
			REPLACE,
			KEY,
			LINE_or_LINES,
			XML_INFORMATION,
			OF,
			ON,
			OUTPUT,
			RECORD,
			RECORDS,
			REEL,
			SECTION,
			SIZE,
			STATUS,
			THROUGH_or_THRU,
			TIME,
			TIMES,
			TO,
			UNIT,
			USING,
			WHEN,
			WITH,
			SQLIMS,
			DEFAULT,
			PROCEDURE,
			DECLARATIVES,
			END,
			DATA,
			FILE,
			FD,
			SD,
			WORKING_STORAGE,
			LOCAL_STORAGE,
			LINKAGE,
			ENVIRONMENT,
			CONFIGURATION,
			SOURCE_COMPUTER,
			OBJECT_COMPUTER,
			SPECIAL_NAMES,
			REPOSITORY,
			INPUT_OUTPUT,
			FILE_CONTROL,
			I_O_CONTROL,
			ID_or_IDENTIFICATION,
			PROGRAM_ID,
			AUTHOR,
			INSTALLATION,
			DATE_WRITTEN,
			DATE_COMPILED,
			SECURITY,
			CLASS_ID,
			METHOD_ID,
			METHOD,
			FACTORY,
			OBJECT,
			TRACE,
			SUPPRESS,
			DATADEF,
			F_NAME,
			UPSI_SWITCH,
			CONDNAME,
			CONDVAR,
			BLOB,
			CLOB,
			DBCLOB,
			BLOB_LOCATOR,
			CLOB_LOCATOR,
			DBCLOB_LOCATOR,
			BLOB_FILE,
			CLOB_FILE,
			DBCLOB_FILE,
			DFHRESP,
			PARSE,
			AUTOMATIC,
			PREVIOUS,
			ENCODING,
			NAMESPACE,
			NAMESPACE_PREFIX,
			XML_DECLARATION,
			ATTRIBUTES,
			VALIDATING,
			UNBOUNDED,
			ATTRIBUTE,
			ELEMENT,
			NONNUMERIC,
			NAME,
			CYCLE,
			PARAGRAPH,
			AS,
			INITIALIZED,
			COBOL,
			;
			
			static final Decoder<Integer, TokenCode> decoder =
					new ReversibleMap<Integer, TokenCode>()
					.set(0, PICTURE_STRING)
					.set(3333, TOKEN_PIECE)
					.set(1, ACCEPT)
					.set(2, ADD)
					.set(3, ALTER)
					.set(4, CALL)
					.set(5, CANCEL)
					.set(7, CLOSE)
					.set(9, COMPUTE)
					.set(11, DELETE)
					.set(13, DISPLAY)
					.set(14, DIVIDE)
					.set(17, READY)
					.set(18, END_PERFORM)
					.set(19, ENTER)
					.set(20, ENTRY)
					.set(21, EXIT)
					.set(22, EXEC_or_EXECUTE)
					.set(23, GO)
					.set(24, IF)
					.set(25, INITIALIZE)
					.set(26, INVOKE)
					.set(27, INSPECT)
					.set(28, MERGE)
					.set(29, MOVE)
					.set(30, MULTIPLY)
					.set(31, OPEN)
					.set(32, PERFORM)
					.set(33, READ)
					.set(35, RELEASE)
					.set(36, RETURN)
					.set(37, REWRITE)
					.set(38, SEARCH)
					.set(40, SET)
					.set(41, SORT)
					.set(42, START)
					.set(43, STOP)
					.set(44, STRING)
					.set(45, SUBTRACT)
					.set(48, UNSTRING)
					.set(49, USE)
					.set(50, WRITE)
					.set(51, CONTINUE)
					.set(52, END_ADD)
					.set(53, END_CALL)
					.set(54, END_COMPUTE)
					.set(55, END_DELETE)
					.set(56, END_DIVIDE)
					.set(57, END_EVALUATE)
					.set(58, END_IF)
					.set(59, END_MULTIPLY)
					.set(60, END_READ)
					.set(61, END_RETURN)
					.set(62, END_REWRITE)
					.set(63, END_SEARCH)
					.set(64, END_START)
					.set(65, END_STRING)
					.set(66, END_SUBTRACT)
					.set(67, END_UNSTRING)
					.set(68, END_WRITE)
					.set(69, GOBACK)
					.set(70, EVALUATE)
					.set(71, RESET)
					.set(72, SERVICE)
					.set(73, END_INVOKE)
					.set(74, END_EXEC)
					.set(75, XML)
					.set(76, END_XML)
					.set(77, ALLOCATE)
					.set(78, FREE)
					.set(79, JSON)
					.set(80, END_JSON)
					.set(99, FOREIGN_VERB)
					.set(101, DATA_NAME)
					.set(105, DASHED_NUM)
					.set(106, DECIMAL)
					.set(107, DIV_SIGN)
					.set(108, EQ)
					.set(109, EXPONENTIATION)
					.set(110, GT)
					.set(111, INTEGER)
					.set(112, LT)
					.set(113, LPAREN)
					.set(114, MINUS_SIGN)
					.set(115, MULT_SIGN)
					.set(116, NONUMLIT)
					.set(117, PERIOD)
					.set(118, PLUS_SIGN)
					.set(121, RPAREN)
					.set(122, SIGNED_INTEGER)
					.set(123, QUID)
					.set(124, COLON)
					.set(125, IEOF)
					.set(126, EGCS_LIT)
					.set(127, COMMA_SPACE)
					.set(128, SEMICOLON_SPACE)
					.set(129, PROCEDURE_NAME)
					.set(130, FLT_POINT_LIT)
					.set(131, LANGUAGE_ENVIRONMENT)
					.set(132, GE)
					.set(133, IDREF)
					.set(134, EXPREF)
					.set(136, CICS)
					.set(137, NEW)
					.set(138, NATIONAL_LIT)
					.set(200, ADDRESS)
					.set(201, ADVANCING)
					.set(202, AFTER)
					.set(203, ALL)
					.set(204, ALPHABETIC)
					.set(205, ALPHANUMERIC)
					.set(206, ANY)
					.set(207, AND)
					.set(208, ALPHANUMERIC_EDITED)
					.set(209, BEFORE)
					.set(210, BEGINNING)
					.set(211, FUNCTION)
					.set(212, CONTENT)
					.set(213, CORR_or_CORRESPONDING)
					.set(214, DAY)
					.set(215, DATE)
					.set(216, DEBUG_CONTENTS)
					.set(217, DEBUG_ITEM)
					.set(218, DEBUG_LINE)
					.set(219, DEBUG_NAME)
					.set(220, DEBUG_SUB_1)
					.set(221, DEBUG_SUB_2)
					.set(222, DEBUG_SUB_3)
					.set(223, DELIMITED)
					.set(224, DELIMITER)
					.set(225, DOWN)
					.set(226, NUMERIC_EDITED)
					.set(227, XML_EVENT)
					.set(228, END_OF_PAGE_or_EOP)
					.set(229, EQUAL)
					.set(230, ERROR)
					.set(231, XML_NTEXT)
					.set(232, EXCEPTION)
					.set(233, EXTEND)
					.set(234, FIRST)
					.set(235, FROM)
					.set(236, GIVING)
					.set(237, GREATER)
					.set(238, I_O)
					.set(239, IN)
					.set(240, INITIAL)
					.set(241, INTO)
					.set(242, INVALID)
					.set(243, SQL)
					.set(244, LESS)
					.set(245, LINAGE_COUNTER)
					.set(246, XML_TEXT)
					.set(247, LOCK)
					.set(248, GENERATE)
					.set(249, NEGATIVE)
					.set(250, NEXT)
					.set(251, NO)
					.set(252, NOT)
					.set(253, NUMERIC)
					.set(254, KANJI)
					.set(255, OR)
					.set(256, OTHER)
					.set(257, OVERFLOW)
					.set(258, PAGE)
					.set(259, CONVERTING)
					.set(260, POINTER)
					.set(261, POSITIVE)
					.set(262, DBCS)
					.set(263, PROCEDURES)
					.set(264, PROCEED)
					.set(265, REFERENCES)
					.set(266, DAY_OF_WEEK)
					.set(267, REMAINDER)
					.set(268, REMOVAL)
					.set(269, REPLACING)
					.set(270, REVERSED)
					.set(271, REWIND)
					.set(272, ROUNDED)
					.set(273, RUN)
					.set(274, SENTENCE)
					.set(275, STANDARD)
					.set(276, RETURN_CODE_etc)
					.set(277, TALLYING)
					.set(278, SUM)
					.set(279, TEST)
					.set(280, THAN)
					.set(281, UNTIL)
					.set(282, UP)
					.set(283, UPON)
					.set(284, VARYING)
					.set(285, RELOAD)
					.set(286, TRUE)
					.set(287, THEN)
					.set(288, RETURNING)
					.set(289, ELSE)
					.set(290, SELF)
					.set(291, SUPER)
					.set(292, WHEN_COMPILED)
					.set(293, ENDING)
					.set(294, FALSE)
					.set(295, REFERENCE)
					.set(296, NATIONAL_EDITED)
					.set(297, COM_REG)
					.set(298, ALPHABETIC_LOWER)
					.set(299, ALPHABETIC_UPPER)
					.set(301, REDEFINES)
					.set(302, OCCURS)
					.set(303, SYNC_or_SYNCHRONIZED)
					.set(304, MORE_LABELS)
					.set(305, JUST_or_JUSTIFIED)
					.set(306, SHIFT_IN)
					.set(307, BLANK)
					.set(308, VALUE)
					.set(309, COMP_or_COMPUTATIONAL)
					.set(310, COMP_1_or_COMPUTATIONAL_1)
					.set(311, COMP_3_or_COMPUTATIONAL_3)
					.set(312, COMP_2_or_COMPUTATIONAL_2)
					.set(313, COMP_4_or_COMPUTATIONAL_4)
					.set(314, DISPLAY_1)
					.set(315, SHIFT_OUT)
					.set(316, INDEX)
					.set(317, USAGE)
					.set(318, SIGN)
					.set(319, LEADING)
					.set(320, SEPARATE)
					.set(321, INDEXED)
					.set(322, LEFT)
					.set(323, RIGHT)
					.set(324, PIC_or_PICTURE)
					.set(325, VALUES)
					.set(326, GLOBAL)
					.set(327, EXTERNAL)
					.set(328, BINARY)
					.set(329, PACKED_DECIMAL)
					.set(330, EGCS)
					.set(331, PROCEDURE_POINTER)
					.set(332, COMP_5_or_COMPUTATIONAL_5)
					.set(333, FUNCTION_POINTER)
					.set(334, TYPE)
					.set(335, JNIENVPTR)
					.set(336, NATIONAL)
					.set(337, GROUP_USAGE)
					.set(342, VOLATILE)
					.set(401, HIGH_VALUE_or_HIGH_VALUES)
					.set(402, LOW_VALUE_or_LOW_VALUES)
					.set(403, QUOTE_or_QUOTES)
					.set(404, SPACE_or_SPACES)
					.set(405, ZERO)
					.set(406, ZEROES_or_ZEROS)
					.set(407, NULL_or_NULLS)
					.set(501, BLOCK)
					.set(502, BOTTOM)
					.set(505, CHARACTER)
					.set(506, CODE)
					.set(507, CODE_SET)
					.set(514, FILLER)
					.set(516, FOOTING)
					.set(520, LABEL)
					.set(521, LENGTH)
					.set(524, LINAGE)
					.set(526, OMITTED)
					.set(531, RENAMES)
					.set(543, TOP)
					.set(545, TRAILING)
					.set(549, RECORDING)
					.set(601, INHERITS)
					.set(603, RECURSIVE)
					.set(701, ACCESS)
					.set(702, ALSO)
					.set(703, ALTERNATE)
					.set(704, AREA_or_AREAS)
					.set(705, ASSIGN)
					.set(707, COLLATING)
					.set(708, COMMA)
					.set(709, CURRENCY)
					.set(710, CLASS)
					.set(711, DECIMAL_POINT)
					.set(712, DUPLICATES)
					.set(713, DYNAMIC)
					.set(714, EVERY)
					.set(716, MEMORY)
					.set(717, MODE)
					.set(718, MODULES)
					.set(719, MULTIPLE)
					.set(720, NATIVE)
					.set(721, OFF)
					.set(722, OPTIONAL)
					.set(723, ORGANIZATION)
					.set(724, POSITION)
					.set(725, PROGRAM)
					.set(726, RANDOM)
					.set(727, RELATIVE)
					.set(728, RERUN)
					.set(729, RESERVE)
					.set(730, SAME)
					.set(731, SEGMENT_LIMIT)
					.set(732, SELECT)
					.set(733, SEQUENCE)
					.set(734, SEQUENTIAL)
					.set(736, SORT_MERGE)
					.set(737, STANDARD_1)
					.set(738, TAPE)
					.set(739, WORDS)
					.set(740, PROCESSING)
					.set(741, APPLY)
					.set(742, WRITE_ONLY)
					.set(743, COMMON)
					.set(744, ALPHABET)
					.set(745, PADDING)
					.set(746, SYMBOLIC)
					.set(747, STANDARD_2)
					.set(748, OVERRIDE)
					.set(750, PASSWORD)
					.set(751, XML_SCHEMA)
					.set(801, ARE_or_IS)
					.set(802, ASCENDING)
					.set(803, AT)
					.set(804, BY)
					.set(805, CHARACTERS)
					.set(806, CONTAINS)
					.set(808, COUNT)
					.set(809, DEBUGGING)
					.set(810, DEPENDING)
					.set(811, DESCENDING)
					.set(812, DIVISION)
					.set(814, FOR)
					.set(815, ORDER)
					.set(816, INPUT)
					.set(817, REPLACE)
					.set(818, KEY)
					.set(819, LINE_or_LINES)
					.set(820, XML_INFORMATION)
					.set(821, OF)
					.set(822, ON)
					.set(823, OUTPUT)
					.set(825, RECORD)
					.set(826, RECORDS)
					.set(827, REEL)
					.set(828, SECTION)
					.set(829, SIZE)
					.set(830, STATUS)
					.set(831, THROUGH_or_THRU)
					.set(832, TIME)
					.set(833, TIMES)
					.set(834, TO)
					.set(836, UNIT)
					.set(837, USING)
					.set(838, WHEN)
					.set(839, WITH)
					.set(840, SQLIMS)
					.set(841, DEFAULT)
					.set(901, PROCEDURE)
					.set(902, DECLARATIVES)
					.set(903, END)
					.set(1001, DATA)
					.set(1002, FILE)
					.set(1003, FD)
					.set(1004, SD)
					.set(1005, WORKING_STORAGE)
					.set(1006, LOCAL_STORAGE)
					.set(1007, LINKAGE)
					.set(1101, ENVIRONMENT)
					.set(1102, CONFIGURATION)
					.set(1103, SOURCE_COMPUTER)
					.set(1104, OBJECT_COMPUTER)
					.set(1105, SPECIAL_NAMES)
					.set(1106, REPOSITORY)
					.set(1107, INPUT_OUTPUT)
					.set(1108, FILE_CONTROL)
					.set(1109, I_O_CONTROL)
					.set(1201, ID_or_IDENTIFICATION)
					.set(1202, PROGRAM_ID)
					.set(1203, AUTHOR)
					.set(1204, INSTALLATION)
					.set(1205, DATE_WRITTEN)
					.set(1206, DATE_COMPILED)
					.set(1207, SECURITY)
					.set(1208, CLASS_ID)
					.set(1209, METHOD_ID)
					.set(1210, METHOD)
					.set(1211, FACTORY)
					.set(1212, OBJECT)
					.set(2020, TRACE)
					.set(2046, SUPPRESS)
					.set(3000, DATADEF)
					.set(3001, F_NAME)
					.set(3002, UPSI_SWITCH)
					.set(3003, CONDNAME)
					.set(3004, CONDVAR)
					.set(3005, BLOB)
					.set(3006, CLOB)
					.set(3007, DBCLOB)
					.set(3008, BLOB_LOCATOR)
					.set(3009, CLOB_LOCATOR)
					.set(3010, DBCLOB_LOCATOR)
					.set(3011, BLOB_FILE)
					.set(3012, CLOB_FILE)
					.set(3013, DBCLOB_FILE)
					.set(3014, DFHRESP)
					.set(5001, PARSE)
					.set(5002, AUTOMATIC)
					.set(5003, PREVIOUS)
					.set(5004, ENCODING)
					.set(5005, NAMESPACE)
					.set(5006, NAMESPACE_PREFIX)
					.set(5007, XML_DECLARATION)
					.set(5008, ATTRIBUTES)
					.set(5009, VALIDATING)
					.set(5010, UNBOUNDED)
					.set(5011, ATTRIBUTE)
					.set(5012, ELEMENT)
					.set(5013, NONNUMERIC)
					.set(5014, NAME)
					.set(5015, CYCLE)
					.set(5016, PARAGRAPH)
					.set(5020, AS)
					.set(5021, INITIALIZED)
					.set(9999, COBOL)
					;
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_SourceError extends RecData {
		
		public final LongVar   vStatementNumber = this.newLongVar(STATEMENT_NUMBER);
		public final StringVar vErrorIdentifier = this.newStringVarF(ERROR_IDENTIFER);
		public final IntVar    vErrorSeverity   = this.newIntVar(ERROR_SEVERITY);
		public final IntVar    vLinePosition    = this.newIntVar(LINE_POSITION);
		public final StringVar vErrorMessage    = this.newStringVarV(ERROR_MESSAGE_LENGTH);
		
		public RecData_SourceError(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_SourceError.class);
		
		private static final Buffer.Region STATEMENT_NUMBER     = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region ERROR_IDENTIFER      = staticRegionGenerator.newFixedLength(16);
		private static final Buffer.Region ERROR_SEVERITY       = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region ERROR_MESSAGE_LENGTH = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region LINE_POSITION        = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01          = staticRegionGenerator.newFixedLength(7);
		
		static {
			Helpers.ignore(RESERVED_01);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_Source extends RecData {
		
		public final LongVar   vLineNumber              = this.newLongVar(LINE_NUMBER);
		public final LongVar   vInputRecordNumber       = this.newLongVar(INPUT_RECORD_NUMBER);
		public final IntVar    vPrimaryFileNumber       = this.newIntVar(PRIMARY_FILE_NUMBER);
		public final IntVar    vLibraryFileNumber       = this.newIntVar(LIBRARY_FILE_NUMBER);
		public final LongVar   vParentRecordNumber      = this.newLongVar(PARENT_RECORD_NUMBER);
		public final IntVar    vParentPrimaryFileNumber = this.newIntVar(PARENT_PRIMARY_FILE_NUMBER);
		public final IntVar    vParentLibraryFileNumber = this.newIntVar(PARENT_LIBRARY_FILE_NUMBER);
		public final StringVar vSourceRecord            = this.newStringVarV(SOURCE_RECORD_LENGTH);
		
		public RecData_Source(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Source.class);
		
		private static final Buffer.Region LINE_NUMBER                = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region INPUT_RECORD_NUMBER        = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PRIMARY_FILE_NUMBER        = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region LIBRARY_FILE_NUMBER        = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_01                = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region PARENT_RECORD_NUMBER       = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PARENT_PRIMARY_FILE_NUMBER = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region PARENT_LIBRARY_FILE_NUMBER = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_02                = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region SOURCE_RECORD_LENGTH       = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_03                = staticRegionGenerator.newFixedLength(10);
		
		static {
			Helpers.ignore(RESERVED_01);
			Helpers.ignore(RESERVED_02);
			Helpers.ignore(RESERVED_03);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_CopyReplacing extends RecData {
		
		public final LongVar vStartingLineNumberOfReplacedString   = this.newLongVar(STARTING_LINE_NUMBER_OF_REPLACED_STRING);
		public final LongVar vStartingColumnNumberOfReplacedString = this.newLongVar(STARTING_COLUMN_NUMBER_OF_REPLACED_STRING);
		public final LongVar vEndingLineNumberOfReplacedString     = this.newLongVar(ENDING_LINE_NUMBER_OF_REPLACED_STRING);
		public final LongVar vEndingColumnNumberOfReplacedString   = this.newLongVar(ENDING_COLUMN_NUMBER_OF_REPLACED_STRING);
		public final LongVar vStartingLineNumberOfOriginalString   = this.newLongVar(STARTING_LINE_NUMBER_OF_ORIGINAL_STRING);
		public final LongVar vStartingColumnNumberOfOriginalString = this.newLongVar(STARTING_COLUMN_NUMBER_OF_ORIGINAL_STRING);
		public final LongVar vEndingLineNumberOfOriginalString     = this.newLongVar(ENDING_LINE_NUMBER_OF_ORIGINAL_STRING);
		public final LongVar vEndingColumnNumberOfOriginalString   = this.newLongVar(ENDING_COLUMN_NUMBER_OF_ORIGINAL_STRING);
		
		public RecData_CopyReplacing(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_CopyReplacing.class);
		
		private static final Buffer.Region STARTING_LINE_NUMBER_OF_REPLACED_STRING   = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region STARTING_COLUMN_NUMBER_OF_REPLACED_STRING = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region ENDING_LINE_NUMBER_OF_REPLACED_STRING     = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region ENDING_COLUMN_NUMBER_OF_REPLACED_STRING   = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region STARTING_LINE_NUMBER_OF_ORIGINAL_STRING   = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region STARTING_COLUMN_NUMBER_OF_ORIGINAL_STRING = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region ENDING_LINE_NUMBER_OF_ORIGINAL_STRING     = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region ENDING_COLUMN_NUMBER_OF_ORIGINAL_STRING   = staticRegionGenerator.newFixedLength(4);
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_Symbol extends RecData {
		
		public final LongVar                            vSymbolId                  = this.newLongVar(SYMBOL_ID);
		public final LongVar                            vLineNumber                = this.newLongVar(LINE_NUMBER);
		public final IntVar                             vLevel                     = this.newIntVar(LEVEL);
		public final EnumVar<QualificationIndicator>    vQualificationIndicator    = this.newEnumVar(QUALIFICATION_INDICATOR, QualificationIndicator.decoder);
		public final EnumVar<SymbolType>                vSymbolType                = this.newEnumVar(SYMBOL_TYPE, SymbolType.decoder);
		public final BooleanVar                         vExternal                  = this.newBooleanVar(SYMBOL_TYPE, 5);
		public final BooleanVar                         vGlobal                    = this.newBooleanVar(SYMBOL_TYPE, 6);
		public final EnumVar<SymbolAttribute>           vSymbolAttribute           = this.newEnumVar(SYMBOL_ATTRIBUTE, SymbolAttribute.decoder);
		public final BooleanVar                         vValue                     = this.newBooleanVar(CLAUSES, 0);
		public final BooleanVar                         vIndexed                   = this.newBooleanVar(CLAUSES, 1);
		public final BooleanVar                         vRedefines                 = this.newBooleanVar(CLAUSES, 2);
		public final BooleanVar                         vRenames                   = this.newBooleanVar(CLAUSES, 3);
		public final BooleanVar                         vOccurs                    = this.newBooleanVar(CLAUSES, 4);
		public final BooleanVar                         vHasOccursKeys             = this.newBooleanVar(CLAUSES, 5);
		public final BooleanVar                         vOccursDependingOn         = this.newBooleanVar(CLAUSES, 6);
		public final BooleanVar                         vOccursInParent            = this.newBooleanVar(CLAUSES, 7);
		public final BooleanVar                         vSelect                    = this.newBooleanVar(CLAUSES, 0);
		public final BooleanVar                         vAssign                    = this.newBooleanVar(CLAUSES, 1);
		public final BooleanVar                         vRerun                     = this.newBooleanVar(CLAUSES, 2);
		public final BooleanVar                         vSameArea                  = this.newBooleanVar(CLAUSES, 3);
		public final BooleanVar                         vSameRecodArea             = this.newBooleanVar(CLAUSES, 4);
		public final BooleanVar                         vRecordingMode             = this.newBooleanVar(CLAUSES, 5);
		public final BooleanVar                         vRecord                    = this.newBooleanVar(CLAUSES, 7);
		public final EnumVar<MnemonicNameSymbolClauses> vMnemonicNameSymbolClauses = this.newEnumVar(CLAUSES, MnemonicNameSymbolClauses.decoder);
		public final BooleanVar                         vRedefined                 = this.newBooleanVar(DATA_FLAGS_1, 0);
		public final BooleanVar                         vRenamed                   = this.newBooleanVar(DATA_FLAGS_1, 1);
		public final BooleanVar                         vSynchronized              = this.newBooleanVar(DATA_FLAGS_1, 2);
		public final BooleanVar                         vImplicitlyRedefined       = this.newBooleanVar(DATA_FLAGS_1, 3);
		public final BooleanVar                         vVolatile                  = this.newBooleanVar(DATA_FLAGS_1, 4);
		public final BooleanVar                         vImplicitlyRedefines       = this.newBooleanVar(DATA_FLAGS_1, 5);
		public final BooleanVar                         vFiller                    = this.newBooleanVar(DATA_FLAGS_1, 6);
		public final BooleanVar                         vLevel77                   = this.newBooleanVar(DATA_FLAGS_1, 7);
		public final BooleanVar                         vBinary                    = this.newBooleanVar(DATA_FLAGS_2, 0);
		public final BooleanVar                         vExternalFloatingPoint     = this.newBooleanVar(DATA_FLAGS_2, 1);
		public final BooleanVar                         vInternalFloatingPoint     = this.newBooleanVar(DATA_FLAGS_2, 2);
		public final BooleanVar                         vPacked                    = this.newBooleanVar(DATA_FLAGS_2, 3);
		public final BooleanVar                         vExternalDecimal           = this.newBooleanVar(DATA_FLAGS_2, 4);
		public final BooleanVar                         vScaledNegative            = this.newBooleanVar(DATA_FLAGS_2, 5);
		public final BooleanVar                         vNumericEdited             = this.newBooleanVar(DATA_FLAGS_2, 6);
		public final BooleanVar                         vAlphabetic                = this.newBooleanVar(DATA_FLAGS_2, 0);
		public final BooleanVar                         vAlphanumeric              = this.newBooleanVar(DATA_FLAGS_2, 1);
		public final BooleanVar                         vAlphanumericEdited        = this.newBooleanVar(DATA_FLAGS_2, 2);
		public final BooleanVar                         vGroupContainsItsOwnOdoObj = this.newBooleanVar(DATA_FLAGS_2, 3);
		public final BooleanVar                         vDbcsItem                  = this.newBooleanVar(DATA_FLAGS_2, 4);
		public final BooleanVar                         vGroupVariableLength       = this.newBooleanVar(DATA_FLAGS_2, 5);
		public final BooleanVar                         vEgcsItem                  = this.newBooleanVar(DATA_FLAGS_2, 6);
		public final BooleanVar                         vEgcsEdited                = this.newBooleanVar(DATA_FLAGS_2, 7);
		public final BooleanVar                         vObjecOfOdoInRecord        = this.newBooleanVar(DATA_FLAGS_2, 0);
		public final BooleanVar                         vSubjecOfOdoInRecord       = this.newBooleanVar(DATA_FLAGS_2, 1);
		public final BooleanVar                         vSequentialAccess          = this.newBooleanVar(DATA_FLAGS_2, 2);
		public final BooleanVar                         vRandomAccess              = this.newBooleanVar(DATA_FLAGS_2, 3);
		public final BooleanVar                         vDynamicAccess             = this.newBooleanVar(DATA_FLAGS_2, 4);
		public final BooleanVar                         vLocateMode                = this.newBooleanVar(DATA_FLAGS_2, 5);
		public final BooleanVar                         vRecordArea                = this.newBooleanVar(DATA_FLAGS_2, 6);
		public final BooleanVar                         vAllRecordsAreSameLength   = this.newBooleanVar(DATA_FLAGS_3, 0);
		public final BooleanVar                         vFixedLength               = this.newBooleanVar(DATA_FLAGS_3, 1);
		public final BooleanVar                         vVariableLength            = this.newBooleanVar(DATA_FLAGS_3, 2);
		public final BooleanVar                         vUndefined                 = this.newBooleanVar(DATA_FLAGS_3, 3);
		public final BooleanVar                         vSpanned                   = this.newBooleanVar(DATA_FLAGS_3, 4);
		public final BooleanVar                         vBlocked                   = this.newBooleanVar(DATA_FLAGS_3, 5);
		public final BooleanVar                         vApplyWriteOnly            = this.newBooleanVar(DATA_FLAGS_3, 6);
		public final BooleanVar                         vSameSortMergeArea         = this.newBooleanVar(DATA_FLAGS_3, 7);
		public final BooleanVar                         vPhysicalSequential        = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 0);
		public final BooleanVar                         vAscii                     = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 1);
		public final BooleanVar                         vStandardLabel             = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 2);
		public final BooleanVar                         vUserLabel                 = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 3);
		public final BooleanVar                         vSequentialOrganization    = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 4);
		public final BooleanVar                         vIndexedOrganization       = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 5);
		public final BooleanVar                         vRelativeOrganization      = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 6);
		public final BooleanVar                         vLineSequential            = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 7);
		public final EnumVar<UsageClause>               vUsageClause               = this.newEnumVar(USAGE_CLAUSE, UsageClause.decoder);
		public final EnumVar<SignClause>                vSignClause                = this.newEnumVar(SIGN_CLAUSE, SignClause.decoder);
		public final BooleanVar                         vJustified                 = this.newBooleanVar(INDICATORS, 7);
		public final BooleanVar                         vBlankWhenZero             = this.newBooleanVar(INDICATORS, 6);
		public final LongVar                            vSize                      = this.newLongVar(SIZE);
		public final IntVar                             vPrecision                 = this.newIntVar(PRECISION);
		public final IntVar                             vScale                     = this.newIntVar(SCALE);
		public final EnumVar<StorageType>               vStorageType               = this.newEnumVar(STORAGE_TYPE, StorageType.decoder);
		public final BooleanVar                         vNumericNational           = this.newBooleanVar(DATA_FLAGS_4, 0);
		public final BooleanVar                         vNational                  = this.newBooleanVar(DATA_FLAGS_4, 0);
		public final BooleanVar                         vNationalEdited            = this.newBooleanVar(DATA_FLAGS_4, 1);
		public final BooleanVar                         vGroupUsageNational        = this.newBooleanVar(DATA_FLAGS_4, 0);
		public final BooleanVar                         vUnboundedLengthGroup      = this.newBooleanVar(DATA_FLAGS_4, 1);
		public final BooleanVar                         vOccursUnbounded           = this.newBooleanVar(DATA_FLAGS_5, 0);
		public final IntVar                             vBaseLocatorCell           = this.newIntVar(BASE_LOCATOR_CELL);
		public final LongVar                            vSymbolIdentifier          = this.newLongVar(SYMBOL_IDENTIFIER);
		public final LongVar                            vStructureDisplacement     = this.newLongVar(STRUCTURE_DISPLACEMENT);
		public final LongVar                            vParentDisplacement        = this.newLongVar(PARENT_DISPLACEMENT);
		public final LongVar                            vParentId                  = this.newLongVar(PARENT_ID);
		public final LongVar                            vRedefinedId               = this.newLongVar(REDEFINED_ID);
		public final LongVar                            vStartRenamedId            = this.newLongVar(START_RENAMED_ID);
		public final LongVar                            vEndRenamedId              = this.newLongVar(END_RENAMED_ID);
		public final LongVar                            vProgramNameSymbolId       = this.newLongVar(PROGRAM_NAME_SYMBOL_ID);
		public final LongVar                            vOccursMinimum             = this.newLongVar(OCCURS_MINIMUM_or_PARAGRAPH_ID);
		public final LongVar                            vParagraphId               = this.newLongVar(OCCURS_MINIMUM_or_PARAGRAPH_ID);
		public final LongVar                            vOccursMaximum             = this.newLongVar(OCCURS_MAXIMUM_or_SECTION_ID);
		public final LongVar                            vSectionId                 = this.newLongVar(OCCURS_MAXIMUM_or_SECTION_ID);
		public final LongVar                            vDimensions                = this.newLongVar(DIMENSIONS);
		public final BooleanVar                         vSymbolNameLowercaseChar01 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 0);
		public final BooleanVar                         vSymbolNameLowercaseChar02 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 1);
		public final BooleanVar                         vSymbolNameLowercaseChar03 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 2);
		public final BooleanVar                         vSymbolNameLowercaseChar04 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 3);
		public final BooleanVar                         vSymbolNameLowercaseChar05 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 4);
		public final BooleanVar                         vSymbolNameLowercaseChar06 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 5);
		public final BooleanVar                         vSymbolNameLowercaseChar07 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 6);
		public final BooleanVar                         vSymbolNameLowercaseChar08 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 7);
		public final BooleanVar                         vSymbolNameLowercaseChar09 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 8);
		public final BooleanVar                         vSymbolNameLowercaseChar10 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 9);
		public final BooleanVar                         vSymbolNameLowercaseChar11 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 10);
		public final BooleanVar                         vSymbolNameLowercaseChar12 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 11);
		public final BooleanVar                         vSymbolNameLowercaseChar13 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 12);
		public final BooleanVar                         vSymbolNameLowercaseChar14 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 13);
		public final BooleanVar                         vSymbolNameLowercaseChar15 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 14);
		public final BooleanVar                         vSymbolNameLowercaseChar16 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 15);
		public final BooleanVar                         vSymbolNameLowercaseChar17 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 16);
		public final BooleanVar                         vSymbolNameLowercaseChar18 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 17);
		public final BooleanVar                         vSymbolNameLowercaseChar19 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 18);
		public final BooleanVar                         vSymbolNameLowercaseChar20 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 19);
		public final BooleanVar                         vSymbolNameLowercaseChar21 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 20);
		public final BooleanVar                         vSymbolNameLowercaseChar22 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 21);
		public final BooleanVar                         vSymbolNameLowercaseChar23 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 22);
		public final BooleanVar                         vSymbolNameLowercaseChar24 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 23);
		public final BooleanVar                         vSymbolNameLowercaseChar25 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 24);
		public final BooleanVar                         vSymbolNameLowercaseChar26 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 25);
		public final BooleanVar                         vSymbolNameLowercaseChar27 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 26);
		public final BooleanVar                         vSymbolNameLowercaseChar28 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 27);
		public final BooleanVar                         vSymbolNameLowercaseChar29 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 28);
		public final BooleanVar                         vSymbolNameLowercaseChar30 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 29);
		public final BooleanVar                         vSymbolNameLowercaseChar31 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 30);
		public final BooleanVar                         vSymbolNameLowercaseChar32 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 31);
		public final LongVar                            vOdoSymbolNameId           = this.newLongVar(ODO_SYMBOL_NAME_ID_or_ASSIGN_DATA_NAME_ID);
		public final LongVar                            vAssignDataNameId          = this.vOdoSymbolNameId;
		
		public final StringVar                          vSymbolName                = this.newStringVarV(SYMBOL_NAME_LENGTH);
		public final StringVar                          vPictureDataString         = this.newStringVarV(PICTURE_DATA_LENGTH_or_ASSIGNMENT_NAME_LENGTH);
		public final StringVar                          vAssignmentName            = this.vPictureDataString;
		public final IntListVar                         vIndexIdList               = this.newIntListVarV(INDEX_COUNT);
//		public final LongListVar                        vKeys                      = this.newLongListVarV(KEYS_COUNT);
		public final ListVar_<Key>                      vKeys                      = this.newListVarV(KEYS_COUNT, Key::new);
		
//		public final StringVar                          vInitialValueData          = this.newStringVarV(INITIAL_VALUE_LENGTH);
		public final ListVar_<Pair>                     vPairs                     = newListVarV2(INITIAL_VALUE_LENGTH, Pair::new);
		
		public final StringVar                          vExternalClassName         = this.newStringVarV(EXTERNAL_CLASS_NAME_LENGTH);;
		
		public RecData_Symbol(final Buffer buffer) {
			super(buffer);
		}
		
		@Override
		protected final void afterRead() {
			this.vKeys.afterRead();
			this.vPairs.afterRead();
			super.afterRead();
		}
		
		/**
		 * @author 2oLDNncs 20250214
		 */
		public final class Key {
			
			public final EnumVar<KeySequence>           vKeySequence               = newEnumVar(1, KeySequence.decoder);
			private final StringVar                     vReserved02                = newStringVarF(3);
			public final LongVar                        vKeyId                     = newLongVar(4);
			
			{
				Helpers.ignore(this.vReserved02);
			}
			
		}
		
		/**
		 * @author 2oLDNncs 20250214
		 */
		public final class Pair {
			
			public final StringVar firstValue  = newStringVarV(newDynamicFixedLengthRegion(2));
			public final StringVar secondValue = newStringVarV(newDynamicFixedLengthRegion(2));
			
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Symbol.class);
		
		private static final Buffer.Region SYMBOL_ID                                          = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region LINE_NUMBER                                        = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region LEVEL                                              = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region QUALIFICATION_INDICATOR                            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region SYMBOL_TYPE                                        = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region SYMBOL_ATTRIBUTE                                   = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CLAUSES                                            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATA_FLAGS_1                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATA_FLAGS_2                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATA_FLAGS_3                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region FILE_ORGANIZATION_AND_ATTRIBUTES                   = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region USAGE_CLAUSE                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region SIGN_CLAUSE                                        = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region INDICATORS                                         = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region SIZE                                               = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PRECISION                                          = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region SCALE                                              = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region STORAGE_TYPE                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATE_FORMAT                                        = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATA_FLAGS_4                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATA_FLAGS_5                                       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region BASE_LOCATOR_CELL                                  = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region SYMBOL_IDENTIFIER                                  = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region STRUCTURE_DISPLACEMENT                             = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PARENT_DISPLACEMENT                                = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PARENT_ID                                          = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region REDEFINED_ID                                       = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region START_RENAMED_ID                                   = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region END_RENAMED_ID                                     = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PROGRAM_NAME_SYMBOL_ID                             = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region OCCURS_MINIMUM_or_PARAGRAPH_ID                     = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region OCCURS_MAXIMUM_or_SECTION_ID                       = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region DIMENSIONS                                         = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region CASE_BIT_VECTOR_BYTE_1                             = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CASE_BIT_VECTOR_BYTE_2                             = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CASE_BIT_VECTOR_BYTE_3                             = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region CASE_BIT_VECTOR_BYTE_4                             = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01                                        = staticRegionGenerator.newFixedLength(8);
		private static final Buffer.Region VALUE_PAIRS_COUNT                                  = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region SYMBOL_NAME_LENGTH                                 = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region PICTURE_DATA_LENGTH_or_ASSIGNMENT_NAME_LENGTH      = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region INITIAL_VALUE_LENGTH                               = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region EXTERNAL_CLASS_NAME_LENGTH                         = INITIAL_VALUE_LENGTH;
		private static final Buffer.Region ODO_SYMBOL_NAME_ID_or_ASSIGN_DATA_NAME_ID          = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region KEYS_COUNT                                         = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region INDEX_COUNT                                        = staticRegionGenerator.newFixedLength(2);
		
		static {
			Helpers.ignore(DATE_FORMAT);
			Helpers.ignore(RESERVED_01);
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum QualificationIndicator {
			
			NO_QUALIFICATION_NEEDED,
			QUALIFICATION_NEEDED,
			;
			
			static final Decoder<Integer, QualificationIndicator> decoder =
					new ReversibleMap<Integer, QualificationIndicator>()
					.set(0x00, NO_QUALIFICATION_NEEDED)
					.set(0x01, QUALIFICATION_NEEDED)
					;
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum SymbolType {
			
			CLASS_NAME,
			METHOD_NAME,
			DATA_NAME,
			PROCEDURE_NAME,
			MNEMONIC_NAME,
			PROGRAM_NAME,
			RESERVED_01,
			;
			
			static final Decoder<Integer, SymbolType> decoder =
					new KeyMaskingDecoder<>(new ReversibleMap<Integer, SymbolType>()
							.set(0x68, CLASS_NAME)
							.set(0x58, METHOD_NAME)
							.set(0x40, DATA_NAME)
							.set(0x20, PROCEDURE_NAME)
							.set(0x10, MNEMONIC_NAME)
							.set(0x08, PROGRAM_NAME)
							.set(0x81, RESERVED_01)
							, 0b11111001);
			
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum SymbolAttribute {
			
			NUMERIC,
			ELEMENTARY_CHARACTER,
			GROUP,
			POINTER,
			INDEX_DATA_ITEM,
			INDEX_NAME,
			CONDITION,
			FILE,
			SORT_FILE,
			CLASS_NAME,
			OBJECT_REFERENCE,
			CURRENCY_SIGN,
			XML_SCHEMA_NAME,
			;
			
			static final Decoder<Integer, SymbolAttribute> decoder =
					new ReversibleMap<Integer, SymbolAttribute>()
					.set(0x01, NUMERIC)
					.set(0x02, ELEMENTARY_CHARACTER)
					.set(0x03, GROUP)
					.set(0x04, POINTER)
					.set(0x05, INDEX_DATA_ITEM)
					.set(0x06, INDEX_NAME)
					.set(0x07, CONDITION)
					.set(0x0F, FILE)
					.set(0x10, SORT_FILE)
					.set(0x17, CLASS_NAME)
					.set(0x18, OBJECT_REFERENCE)
					.set(0x19, CURRENCY_SIGN)
					.set(0x1A, XML_SCHEMA_NAME)
					;
			
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum MnemonicNameSymbolClauses {
			
			CSP,
			C01,
			C02,
			C03,
			C04,
			C05,
			C06,
			C07,
			C08,
			C09,
			C10,
			C11,
			C12,
			S01,
			S02,
			S03,
			S04,
			S05,
			CONSOLE,
			SYSIN_SYSIPT,
			SYSOUT_SYSLST_SYSLIST,
			SYSPUNCH_SYSPCH,
			UPSI_0,
			UPSI_1,
			UPSI_2,
			UPSI_3,
			UPSI_4,
			UPSI_5,
			UPSI_6,
			UPSI_7,
			AFP_5A,
			;
			
			static final Decoder<Integer, MnemonicNameSymbolClauses> decoder =
					new ReversibleMap<Integer, MnemonicNameSymbolClauses>()
					.set(1, CSP)
					.set(2, C01)
					.set(3, C02)
					.set(4, C03)
					.set(5, C04)
					.set(6, C05)
					.set(7, C06)
					.set(8, C07)
					.set(9, C08)
					.set(10, C09)
					.set(11, C10)
					.set(12, C11)
					.set(13, C12)
					.set(14, S01)
					.set(15, S02)
					.set(16, S03)
					.set(17, S04)
					.set(18, S05)
					.set(19, CONSOLE)
					.set(20, SYSIN_SYSIPT)
					.set(22, SYSOUT_SYSLST_SYSLIST)
					.set(24, SYSPUNCH_SYSPCH)
					.set(26, UPSI_0)
					.set(27, UPSI_1)
					.set(28, UPSI_2)
					.set(29, UPSI_3)
					.set(30, UPSI_4)
					.set(31, UPSI_5)
					.set(32, UPSI_6)
					.set(33, UPSI_7)
					.set(34, AFP_5A)
					;
			
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum UsageClause {
			
			USAGE_IS_DISPLAY,
			USAGE_IS_COMP_1,
			USAGE_IS_COMP_2,
			USAGE_IS_PACKED_DECIMAL_or_COMP_3,
			USAGE_IS_BINARY_etc, // COMP, COMP-4, COMP-5
			USAGE_IS_DISPLAY_1,
			USAGE_IS_POINTER,
			USAGE_IS_INDEX,
			USAGE_IS_PROCEDURE_POINTER,
			USAGE_IS_OBJECT_REFERENCE,
			FUNCTION_POINTER,
			NATIONAL,
			;
			
			static final Decoder<Integer, UsageClause> decoder =
					new ReversibleMap<Integer, UsageClause>()
					.set(0x00, USAGE_IS_DISPLAY)
					.set(0x01, USAGE_IS_COMP_1)
					.set(0x02, USAGE_IS_COMP_2)
					.set(0x03, USAGE_IS_PACKED_DECIMAL_or_COMP_3)
					.set(0x04, USAGE_IS_BINARY_etc)
					.set(0x05, USAGE_IS_DISPLAY_1)
					.set(0x06, USAGE_IS_POINTER)
					.set(0x07, USAGE_IS_INDEX)
					.set(0x08, USAGE_IS_PROCEDURE_POINTER)
					.set(0x09, USAGE_IS_OBJECT_REFERENCE)
					.set(0x0A, FUNCTION_POINTER)
					.set(0x0B, NATIONAL)
					;
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum SignClause {
			
			NO_SIGN,
			SIGN_IS_LEADING,
			SIGN_IS_LEADING_SEPARATE_CHARACTER,
			SIGN_IS_TRAILING,
			SIGN_IS_TRAILING_SEPARATE_CHARACTER,
			;
			
			static final Decoder<Integer, SignClause> decoder =
					new ReversibleMap<Integer, SignClause>()
					.set(0x00, NO_SIGN)
					.set(0x01, SIGN_IS_LEADING)
					.set(0x02, SIGN_IS_LEADING_SEPARATE_CHARACTER)
					.set(0x03, SIGN_IS_TRAILING)
					.set(0x04, SIGN_IS_TRAILING_SEPARATE_CHARACTER)
					;
		}
		
		/**
		 * @author 2oLDNncs 20250209
		 */
		public static enum StorageType {
			
			NOT_APPLICABLE,
			FILES,
			WORKING_STORAGE,
			LINKAGE_SECTION,
			SPECIAL_REGISTERS,
			INDEXED_BY_VARIABLE,
			UPSI_SWITCH,
			VARIABLY_LOCATED_ITEMS,
			EXTERNAL_DATA,
			ALPHANUMERIC_FUNC,
			ALPHANUMERIC_EVAL,
			OBJECT_DATA,
			LOCAL_STORAGE,
			FACTORY_DATA,
			XML_TEXT_and_XML_NTEXT,
			;
			
			static final Decoder<Integer, StorageType> decoder =
					new ReversibleMap<Integer, StorageType>()
					.set(0, NOT_APPLICABLE)
					.set(1, FILES)
					.set(2, WORKING_STORAGE)
					.set(3, LINKAGE_SECTION)
					.set(5, SPECIAL_REGISTERS)
					.set(7, INDEXED_BY_VARIABLE)
					.set(10, UPSI_SWITCH)
					.set(13, VARIABLY_LOCATED_ITEMS)
					.set(14, EXTERNAL_DATA)
					.set(15, ALPHANUMERIC_FUNC)
					.set(16, ALPHANUMERIC_EVAL)
					.set(17, OBJECT_DATA)
					.set(19, LOCAL_STORAGE)
					.set(20, FACTORY_DATA)
					.set(21, XML_TEXT_and_XML_NTEXT)
					;
		}
		
		/**
		 * @author 2oLDNncs 20250211
		 */
		public static enum KeySequence {
			
			ASCENDING,
			DESCENDING,
			;
			
			static final Decoder<Integer, KeySequence> decoder =
					new ReversibleMap<Integer, KeySequence>()
					.set(0x00, ASCENDING)
					.set(0x01, DESCENDING)
					;
		}

	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_SymbolCrossReference extends RecData {
		
		public final LongVar                     vStatementNumber     = this.newLongVar(STATEMENT_NUMBER_or_STATEMENT_COUNT);
		public final LongVar                     vStatementCount      = this.vStatementNumber;
		public final IntVar                      vNumberOfReferences  = this.newIntVar(NUMBER_OF_REFERENCES);
		public final EnumVar<CrossReferenceType> vCrossReferenceType  = this.newEnumVar(CROSS_REFERENCE_TYPE, CrossReferenceType.decoder);
		
		public final StringVar                   vSymbolName          = this.newStringVarV(SYMBOL_LENGTH);
		
		private final List<FlagAndStmt> flagAndStmtList = new ArrayList<>();
		private final List<Stmt>        stmtList        = new ArrayList<>();
		
		public RecData_SymbolCrossReference(final Buffer buffer) {
			super(buffer);
		}
		
		public final FlagAndStmt getFlagAndStmt(final int index) {
			return this.flagAndStmtList.get(index);
		}
		
		public final Stmt getStmt(final int index) {
			return this.stmtList.get(index);
		}
		
		public final Stmt newStmt() {
			if (!CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
				throw new IllegalStateException();
			}
			
			if (!this.flagAndStmtList.isEmpty()) {
				throw new IllegalStateException();
			}
			
			final var result = this.new Stmt();
			
			this.stmtList.add(result);
			
			return result;
		}
		
		public final Stmt addStmt() {
			final var result = this.newStmt();
			
			this.vNumberOfReferences.set(this.vNumberOfReferences.get() + 1);
			
			return result;
		}
		
		public final FlagAndStmt newFlagAndStmt() {
			if (CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
				throw new IllegalStateException();
			}
			
			if (!this.stmtList.isEmpty()) {
				throw new IllegalStateException();
			}
			
			final var result = this.new FlagAndStmt();
			
			this.flagAndStmtList.add(result);
			
			return result;
		}
		
		public final FlagAndStmt addFlagAndStmt() {
			final var result = this.newFlagAndStmt();
			
			this.vNumberOfReferences.set(this.vNumberOfReferences.get() + 1);
			
			return result;
		}
		
		@Override
		protected final void afterRead() {
			super.afterRead();
			
			final var n = this.vNumberOfReferences.get();
			
			if (CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
				for (var i = 0; i < n; i += 1) {
					final var s = this.newStmt();
					Helpers.dprintlnf(" Stmt(%s)", i);
					Helpers.dprintlnf("  StatementNumber<%s>", s.vStatementNumber);
				}
			} else {
				for (var i = 0; i < n; i += 1) {
					final var fs = this.newFlagAndStmt();
					Helpers.dprintlnf(" FlagAndStmt(%s)", i);
					Helpers.dprintlnf("  ReferenceFlag<%s>", fs.vReferenceFlag);
					Helpers.dprintlnf("  StatementNumber<%s>", fs.vStatementNumber);
				}
			}
		}
		
		@Override
		protected final void beforeWrite() {
			if (CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
				if (!this.flagAndStmtList.isEmpty()) {
					throw new IllegalStateException();
				}
				
				if (this.vNumberOfReferences.get() != this.stmtList.size()) {
					throw new IllegalStateException();
				}
			} else {
				if (!this.stmtList.isEmpty()) {
					throw new IllegalStateException();
				}
				
				if (this.vNumberOfReferences.get() != this.flagAndStmtList.size()) {
					throw new IllegalStateException();
				}
			}
		}
		
		/**
		 * @author 2oLDNncs 20250211
		 */
		public final class FlagAndStmt {
			
			public final EnumVar<ReferenceFlag> vReferenceFlag   = newEnumVar(1, ReferenceFlag.decoder);
			public final LongVar                vStatementNumber = newLongVar(4);
			
		}
		
		/**
		 * @author 2oLDNncs 20250211
		 */
		public final class Stmt {
			
			public final LongVar                vStatementNumber = newLongVar(4);
			
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_SymbolCrossReference.class);
		
		private static final Buffer.Region SYMBOL_LENGTH                       = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region STATEMENT_NUMBER_or_STATEMENT_COUNT = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region NUMBER_OF_REFERENCES                = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region CROSS_REFERENCE_TYPE                = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01                         = staticRegionGenerator.newFixedLength(7);
		
		static {
			Helpers.ignore(RESERVED_01);
		}
		
		/**
		 * @author 2oLDNncs 20250211
		 */
		public static enum CrossReferenceType {
			
			PROGRAM,
			PROCEDURE,
			STATEMENT,
			SYMBOL_OR_DATA_NAME,
			METHOD,
			CLASS,
			;
			
			static final Decoder<Integer, CrossReferenceType> decoder =
					new ReversibleMap<Integer, CrossReferenceType>()
					.set(1, PROGRAM)
					.set(2, PROCEDURE)
					.set(3, STATEMENT)
					.set(4, SYMBOL_OR_DATA_NAME)
					.set(5, METHOD)
					.set(6, CLASS)
					;
		}
		
		/**
		 * @author 2oLDNncs 20250211
		 */
		public static enum ReferenceFlag {
			
			REFERENCE_ONLY,
			MODIFICATION,
			ALTER,
			GO_TO_DEPENDING_ON,
			END_OF_RANGE_OF_PERFORM_THROUGH,
			GO_TO,
			PERFORM,
			ALTER_TO_PROCEED_TO,
			USE_FOR_DEBUGGING,
			;
			
			static final Decoder<Integer, ReferenceFlag> decoder =
					new ReversibleMap<Integer, ReferenceFlag>()
					.set(0xFF & Buffer.EBCDIC.encode(" ").get(), REFERENCE_ONLY)
					.set(0xFF & Buffer.EBCDIC.encode("M").get(), MODIFICATION)
					.set(0xFF & Buffer.EBCDIC.encode("A").get(), ALTER)
					.set(0xFF & Buffer.EBCDIC.encode("D").get(), GO_TO_DEPENDING_ON)
					.set(0xFF & Buffer.EBCDIC.encode("E").get(), END_OF_RANGE_OF_PERFORM_THROUGH)
					.set(0xFF & Buffer.EBCDIC.encode("G").get(), GO_TO)
					.set(0xFF & Buffer.EBCDIC.encode("P").get(), PERFORM)
					.set(0xFF & Buffer.EBCDIC.encode("T").get(), ALTER_TO_PROCEED_TO)
					.set(0xFF & Buffer.EBCDIC.encode("U").get(), USE_FOR_DEBUGGING)
					;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_NestedProgram extends RecData {
		
		public final LongVar    vStatementDefinition    = this.newLongVar(STATEMENT_DEFINITION);
		public final IntVar     vProgramNestingLevel    = this.newIntVar(PROGRAM_NESTING_LEVEL);
		public final BooleanVar vInital                 = this.newBooleanVar(PROGRAM_ATTRIBUTES, 0);
		public final BooleanVar vCommon                 = this.newBooleanVar(PROGRAM_ATTRIBUTES, 1);
		public final BooleanVar vProcedureDivisionUsing = this.newBooleanVar(PROGRAM_ATTRIBUTES, 2);
		
		public final StringVar  vProgramName            = this.newStringVarV(PROGRAM_NAME_LENGTH);
		
		public RecData_NestedProgram(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_NestedProgram.class);
		
		private static final Buffer.Region STATEMENT_DEFINITION                   = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PROGRAM_NESTING_LEVEL                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PROGRAM_ATTRIBUTES                     = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01                            = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PROGRAM_NAME_LENGTH                    = staticRegionGenerator.newFixedLength(1);
		
		static {
			Helpers.ignore(RESERVED_01);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_Library extends RecData {
		
		public final IntVar    vNumberOfMembers     = this.newIntVar(NUMBER_OF_MEMBERS);
		public final IntVar    vConcatenationNumber = this.newIntVar(CONCATENATION_NUMBER);
		
		public final StringVar vLibraryName         = this.newStringVarV(LIBRARY_NAME_LENGTH);
		public final StringVar vLibraryVolume       = this.newStringVarV(LIBRARY_VOLUME_LENGTH);
		public final StringVar vLibraryDdname       = this.newStringVarV(LIBRARY_DDNAME_LENGTH);
		
		private final List<Member> memberList = new ArrayList<>();
		
		public final Member newMember() {
			final var result = this.new Member();
			
			memberList.add(result);
			
			return result;
		}
		
		public final Member addMember() {
			final var result = this.newMember();
			
			this.vNumberOfMembers.set(this.vNumberOfMembers.get() + 1);
			
			return result;
		}
		
		public RecData_Library(final Buffer buffer) {
			super(buffer);
		}
		
		@Override
		protected final void afterRead() {
			super.afterRead();
			
			final var n = this.vNumberOfMembers.get();
			
			for (var i = 0; i < n; i += 1) {
				final var m = this.newMember();
				
				Helpers.dprintlnf(" Member(%s)", i);
				Helpers.dprintlnf("  FileId<%s>", m.vFileId.get());
				Helpers.dprintlnf("  Name<%s>", m.vName.get());
			}
		}
		
		@Override
		protected final void beforeWrite() {
			if (this.vNumberOfMembers.get() != this.memberList.size()) {
				throw new IllegalStateException();
			}
		}
		
		/**
		 * @author 2oLDNncs 20250212
		 */
		public final class Member {
			
			public final IntVar vFileId = newIntVar(2);
			private final Buffer.Region nameLength = newDynamicFixedLengthRegion(2);
			public final StringVar vName = newStringVarV(this.nameLength);
			
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Library.class);
		
		private static final Buffer.Region NUMBER_OF_MEMBERS     = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region LIBRARY_NAME_LENGTH   = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region LIBRARY_VOLUME_LENGTH = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region CONCATENATION_NUMBER  = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region LIBRARY_DDNAME_LENGTH = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region RESERVED_01           = staticRegionGenerator.newFixedLength(4);
		
		static {
			Helpers.ignore(RESERVED_01);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_Statistics extends RecData {
		
		public final LongVar    vSourceRecords               = this.newLongVar(SOURCE_RECORDS);
		public final LongVar    vDataDivisionStatements      = this.newLongVar(DATA_DIVISION_STATEMENTS);
		public final LongVar    vProcedureDivisionStatements = this.newLongVar(PROCEDURE_DIVISION_STATEMENTS);
		public final IntVar     vCompilationNumber           = this.newIntVar(COMPILATION_NUMBER);
		public final IntVar     vErrorSeverity               = this.newIntVar(ERROR_SEVERITY);
		public final BooleanVar vEndOfJob                    = this.newBooleanVar(FLAGS, 0);
		public final BooleanVar vClassDefinition             = this.newBooleanVar(FLAGS, 1);
		public final IntVar     vEojSeverity                 = this.newIntVar(EOJ_SEVERITY);
		
		public final StringVar  vProgramName                 = this.newStringVarV(PROGRAM_NAME_LENGTH);
		
		public RecData_Statistics(final Buffer buffer) {
			super(buffer);
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Statistics.class);
		
		private static final Buffer.Region SOURCE_RECORDS                = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region DATA_DIVISION_STATEMENTS      = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region PROCEDURE_DIVISION_STATEMENTS = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region COMPILATION_NUMBER            = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region ERROR_SEVERITY                = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region FLAGS                         = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region EOJ_SEVERITY                  = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region PROGRAM_NAME_LENGTH           = staticRegionGenerator.newFixedLength(1);
		
	}
	
	/**
	 * @author 2oLDNncs 20250203
	 */
	public static final class RecData_Events extends RecData {
		
		private Event event;
		
		public RecData_Events(final Buffer buffer) {
			super(buffer);
		}
		
		public final <E extends Event> E getEvent() {
			return Helpers.cast(this.event);
		}
		
		public final Timestamp setEventTimestamp() {
			this.event = this.new Timestamp();
			
			return this.getEvent();
		}
		
		public final Timestamp setEventProcessor() {
			this.event = this.new Processor();
			
			return this.getEvent();
		}
		
		public final Timestamp setEventFileEnd() {
			this.event = this.new FileEnd();
			
			return this.getEvent();
		}
		
		public final Timestamp setEventProgram() {
			this.event = this.new Program();
			
			return this.getEvent();
		}
		
		public final Timestamp setEventFileId() {
			this.event = this.new FileId();
			
			return this.getEvent();
		}
		
		public final Timestamp setEventError() {
			this.event = this.new Error();
			
			return this.getEvent();
		}
		
		@Override
		protected final void afterRead() {
			super.afterRead();
			
			final var vRectypePrefix = this.newStringVarF(RECTYPE_PREFIX);
			
			if (false) {
				switch (vRectypePrefix.get()) {
				case "TIMES":
					this.setEventTimestamp().afterRead();
					break;
				case "PROCE":
					this.setEventProcessor().afterRead();
					break;
				case "FILEE":
					this.setEventFileEnd().afterRead();
					break;
				case "PROGR":
					this.setEventProgram().afterRead();
					break;
				case "FILEI":
					this.setEventFileId().afterRead();
					break;
				case "ERROR":
					this.setEventError().afterRead();
					break;
				default:
					throw new IllegalStateException(String.format("Unknown Event Record Type Prefix: %s", vRectypePrefix.get()));
				}
			}
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_Events.class);
		
		private static final Buffer.Region LENGTH_OF_FOLLOWING = staticRegionGenerator.newFixedLength(2);
		
		private static final Buffer.Region.Generator staticRegionGenerator2 = staticRegionGenerator.clone();
		
		private static final Buffer.Region RECTYPE_PREFIX = staticRegionGenerator2.newFixedLength(5);
		
		static {
			Helpers.ignore(LENGTH_OF_FOLLOWING);
		}
		
		/**
		 * @author 2oLDNncs 20250213
		 */
		public abstract class Event extends RecPart {
			
			public final StringVar  vRecordType;
			private final StringVar vBlank01;
			public final IntVar     vRevisionLevel;
			private final StringVar vBlank02;
			
			protected Event(final Buffer.Region recordType, final Buffer.Region blank01,
					final Buffer.Region revisionLevel, final Buffer.Region blank02) {
				super(RecData_Events.this.buffer);
				this.vRecordType    = newStringVarF(recordType);
				this.vBlank01       = newStringVarF(blank01);
				this.vRevisionLevel = newIntVar(revisionLevel);
				this.vBlank02       = newStringVarF(blank02);
			}
			
			@Override
			protected void beforeWrite() {
				this.vBlank01.set(" ");
				this.vBlank02.set(" ");
			}
			
			protected final void checkRecordType(final String expectedRecordType) {
				if (!expectedRecordType.equals(this.vRecordType.get())) {
					throw new IllegalStateException();
				}
			}
			
		}
		
		/**
		 * @author 2oLDNncs 20250212
		 */
		public final class Timestamp extends Event {
			
			public final LongVar vDate    = this.newLongVar(DATE);
			public final IntVar  vHour    = this.newIntVar(HOUR);
			public final IntVar  vMinutes = this.newIntVar(MINUTES);
			public final IntVar  vSecond  = this.newIntVar(SECONDS);
			
			Timestamp() {
				super(RECORD_TYPE, BLANK_01, REVISION_LEVEL, BLANK_02);
			}
			
			@Override
			protected final void afterRead() {
				this.checkRecordType(S_RECORD_TYPE);
			}
			
			@Override
			protected void beforeWrite() {
				super.beforeWrite();
				this.vRecordType.set(S_RECORD_TYPE);
			}
			
			private static final Buffer.Region.Generator staticRegionGenerator =
					getStaticRegionGenerator(RecData_Events.Timestamp.class, RecData_Events.staticRegionGenerator);
			
			private static final Buffer.Region RECORD_TYPE    = staticRegionGenerator.newFixedLength(12);
			private static final Buffer.Region BLANK_01       = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REVISION_LEVEL = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_02       = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region DATE           = staticRegionGenerator.newFixedLength(8).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region HOUR           = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region MINUTES        = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region SECONDS        = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			
			public static final String S_RECORD_TYPE = "PROCESSOR";
			
		}
		
		/**
		 * @author 2oLDNncs 20250213
		 */
		public final class Processor extends Event {
			
			public final IntVar     vOutputFileId       = this.newIntVar(OUTPUT_FILE_ID);
			private final StringVar vBlank03            = this.newStringVarF(BLANK_03);
			public final IntVar     vLineClassIndicator = this.newIntVar(LINE_CLASS_INDICATOR);
			
			Processor() {
				super(RECORD_TYPE, BLANK_01, REVISION_LEVEL, BLANK_02);
			}
			
			@Override
			protected final void afterRead() {
				this.checkRecordType(S_RECORD_TYPE);
			}
			
			@Override
			protected final void beforeWrite() {
				super.beforeWrite();
				this.vRecordType.set(S_RECORD_TYPE);
				this.vBlank03.set(" ");
			}
			
			private static final Buffer.Region.Generator staticRegionGenerator =
					getStaticRegionGenerator(RecData_Events.Timestamp.class, RecData_Events.staticRegionGenerator);
			
			private static final Buffer.Region RECORD_TYPE          = staticRegionGenerator.newFixedLength(9);
			private static final Buffer.Region BLANK_01             = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REVISION_LEVEL       = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_02             = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region OUTPUT_FILE_ID       = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_03             = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region LINE_CLASS_INDICATOR = staticRegionGenerator.newFixedLength(1);
			
			public static final String S_RECORD_TYPE = "PROCESSOR";
			
		}
		
		/**
		 * @author 2oLDNncs 20250213
		 */
		public final class FileEnd extends Event {
			
			public final IntVar     vInputFileId        = this.newIntVar(INPUT_FILE_ID);
			private final StringVar vBlank03            = this.newStringVarF(BLANK_03);
			public final IntVar     vExpansionIndicator = this.newIntVar(EXPANSION_INDICATOR);
			
			FileEnd() {
				super(RECORD_TYPE, BLANK_01, REVISION_LEVEL, BLANK_02);
			}
			
			@Override
			protected final void afterRead() {
				this.checkRecordType(S_RECORD_TYPE);
			}
			
			@Override
			protected final void beforeWrite() {
				super.beforeWrite();
				this.vRecordType.set(S_RECORD_TYPE);
				this.vBlank03.set(" ");
			}
			
			private static final Buffer.Region.Generator staticRegionGenerator =
					getStaticRegionGenerator(RecData_Events.Timestamp.class, RecData_Events.staticRegionGenerator);
			
			private static final Buffer.Region RECORD_TYPE         = staticRegionGenerator.newFixedLength(9);
			private static final Buffer.Region BLANK_01            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REVISION_LEVEL      = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_02            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region INPUT_FILE_ID       = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_03            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region EXPANSION_INDICATOR = staticRegionGenerator.newFixedLength(1);
			
			public static final String S_RECORD_TYPE = "FILEEND";
			
		}
		
		/**
		 * @author 2oLDNncs 20250213
		 */
		public final class Program extends Event {
			
			public final IntVar     vOutputFileId             = this.newIntVar(OUTPUT_FILE_ID);
			private final StringVar vBlank03                  = this.newStringVarF(BLANK_03);
			public final IntVar     vProgramInputRecordNumber = this.newIntVar(PROGRAM_INPUT_RECORD_NUMBER);
			
			Program() {
				super(RECORD_TYPE, BLANK_01, REVISION_LEVEL, BLANK_02);
			}
			
			@Override
			protected final void afterRead() {
				this.checkRecordType(S_RECORD_TYPE);
			}
			
			@Override
			protected final void beforeWrite() {
				super.beforeWrite();
				this.vRecordType.set(S_RECORD_TYPE);
				this.vBlank03.set(" ");
			}
			
			private static final Buffer.Region.Generator staticRegionGenerator =
					getStaticRegionGenerator(RecData_Events.Timestamp.class, RecData_Events.staticRegionGenerator);
			
			private static final Buffer.Region RECORD_TYPE                 = staticRegionGenerator.newFixedLength(9);
			private static final Buffer.Region BLANK_01                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REVISION_LEVEL              = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_02                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region OUTPUT_FILE_ID              = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_03                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region PROGRAM_INPUT_RECORD_NUMBER = staticRegionGenerator.newFixedLength(1);
			
			public static final String S_RECORD_TYPE = "PROGRAM";
			
		}
		
		/**
		 * @author 2oLDNncs 20250213
		 */
		public final class FileId extends Event {
			
			public final IntVar     vInputSourceFileId        = this.newIntVar(INPUT_SOURCE_FILE_ID);
			private final StringVar vBlank03                  = this.newStringVarF(BLANK_03);
			public final IntVar     vReferenceIndicator       = this.newIntVar(REFERENCE_INDICATOR);
			private final StringVar vBlank04                  = this.newStringVarF(BLANK_04);
			
			public final StringVar  vSourceFileName           = this.newStringVarV(SOURCE_FILE_NAME_LENGTH);
			
			FileId() {
				super(RECORD_TYPE, BLANK_01, REVISION_LEVEL, BLANK_02);
			}
			
			@Override
			protected final void afterRead() {
				this.checkRecordType(S_RECORD_TYPE);
			}
			
			@Override
			protected final void beforeWrite() {
				super.beforeWrite();
				this.vRecordType.set(S_RECORD_TYPE);
				this.vBlank03.set(" ");
				this.vBlank04.set(" ");
			}
			
			private static final Buffer.Region.Generator staticRegionGenerator =
					getStaticRegionGenerator(RecData_Events.Timestamp.class, RecData_Events.staticRegionGenerator);
			
			private static final Buffer.Region RECORD_TYPE                 = staticRegionGenerator.newFixedLength(9);
			private static final Buffer.Region BLANK_01                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REVISION_LEVEL              = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_02                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region INPUT_SOURCE_FILE_ID        = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_03                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REFERENCE_INDICATOR         = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_04                    = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region SOURCE_FILE_NAME_LENGTH     = staticRegionGenerator.newFixedLength(2);
			
			public static final String S_RECORD_TYPE = "FILEID";
			
		}
		
		/**
		 * @author 2oLDNncs 20250213
		 */
		public final class Error extends Event {
			
			public final IntVar     vInputSourceFileId               = this.newIntVar(INPUT_SOURCE_FILE_ID);
			private final StringVar vBlank03                         = this.newStringVarF(BLANK_03);
			public final IntVar     vReferenceIndicator              = this.newIntVar(ANNOT_CLASS);
			private final StringVar vBlank04                         = this.newStringVarF(BLANK_04);
			public final IntVar     vErrorInputRecordNumber          = this.newIntVar(ERROR_INPUT_RECORD_NUMBER);
			private final StringVar vBlank05                         = this.newStringVarF(BLANK_05);
			public final IntVar     vErrorStartLineNumber            = this.newIntVar(ERROR_START_LINE_NUMBER);
			private final StringVar vBlank06                         = this.newStringVarF(BLANK_06);
			public final IntVar     vErrorEndLineNumber              = this.newIntVar(ERROR_END_LINE_NUMBER);
			private final StringVar vBlank07                         = this.newStringVarF(BLANK_07);
			public final IntVar     vErrorTokenEndNumber             = this.newIntVar(ERROR_TOKEN_END_NUMBER);
			private final StringVar vBlank08                         = this.newStringVarF(BLANK_08);
			public final IntVar     vErrorMessageIdNumber            = this.newIntVar(ERROR_MESSAGE_ID_NUMBER);
			private final StringVar vBlank09                         = this.newStringVarF(BLANK_09);
			public final IntVar     vErrorMessageSeverityCode        = this.newIntVar(ERROR_MESSAGE_SEVERITY_CODE);
			private final StringVar vBlank10                         = this.newStringVarF(BLANK_10);
			public final IntVar     vErrorMessageSeverityLevelNumber = this.newIntVar(ERROR_MESSAGE_SEVERITY_LEVEL_NUMBER);
			private final StringVar vBlank11                         = this.newStringVarF(BLANK_11);
			private final StringVar vBlank12                         = this.newStringVarF(BLANK_12);
			
			public final StringVar  vErrorMessage                    = this.newStringVarV(ERROR_MESSAGE_LENGTH);
			
			Error() {
				super(RECORD_TYPE, BLANK_01, REVISION_LEVEL, BLANK_02);
			}
			
			@Override
			protected final void afterRead() {
				this.checkRecordType(S_RECORD_TYPE);
			}
			
			@Override
			protected final void beforeWrite() {
				super.beforeWrite();
				this.vRecordType.set(S_RECORD_TYPE);
				this.vBlank03.set(" ");
				this.vBlank04.set(" ");
				this.vBlank05.set(" ");
				this.vBlank06.set(" ");
				this.vBlank07.set(" ");
				this.vBlank08.set(" ");
				this.vBlank09.set(" ");
				this.vBlank10.set(" ");
				this.vBlank11.set(" ");
				this.vBlank12.set(" ");
			}
			
			private static final Buffer.Region.Generator staticRegionGenerator =
					getStaticRegionGenerator(RecData_Events.Timestamp.class, RecData_Events.staticRegionGenerator);
			
			private static final Buffer.Region RECORD_TYPE                         = staticRegionGenerator.newFixedLength(9);
			private static final Buffer.Region BLANK_01                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region REVISION_LEVEL                      = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_02                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region INPUT_SOURCE_FILE_ID                = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_03                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ANNOT_CLASS                         = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_04                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_INPUT_RECORD_NUMBER           = staticRegionGenerator.newFixedLength(10).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region BLANK_05                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_START_LINE_NUMBER             = staticRegionGenerator.newFixedLength(10).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region BLANK_06                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_END_LINE_NUMBER               = staticRegionGenerator.newFixedLength(10).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region BLANK_07                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_TOKEN_END_NUMBER              = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_08                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_MESSAGE_ID_NUMBER             = staticRegionGenerator.newFixedLength(9).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);;
			private static final Buffer.Region BLANK_09                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_MESSAGE_SEVERITY_CODE         = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region BLANK_10                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_MESSAGE_SEVERITY_LEVEL_NUMBER = staticRegionGenerator.newFixedLength(2);
			private static final Buffer.Region BLANK_11                            = staticRegionGenerator.newFixedLength(1);
			private static final Buffer.Region ERROR_MESSAGE_LENGTH                = staticRegionGenerator.newFixedLength(3).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
			private static final Buffer.Region BLANK_12                            = staticRegionGenerator.newFixedLength(1);
			
			public static final String S_RECORD_TYPE = "ERROR";
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	public static final class RecHeader extends RecPart {
		
		public final EnumVar<LanguageCode> vLanguageCode           = this.newEnumVar(LANGUAGE_CODE, LanguageCode.decoder);
		public final EnumVar<RecordType>   vRecordType             = this.newEnumVar(RECORD_TYPE, RecordType.decoder);
		public final IntVar                vDataArchitectureLevel  = this.newIntVar(DATA_ARCH_LEVEL);
		public final BooleanVar            vLittleEndian           = this.newBooleanVar(FLAG, 6);
		public final BooleanVar            vContinuedInNextRec     = this.newBooleanVar(FLAG, 7);
		public final IntVar                vDataRecordEditionLevel = this.newIntVar(DATA_REC_EDIT_LEVEL);
		public final IntVar                vDataLength             = this.newIntVar(DATA_LENGTH);
		
		private int expectedRecDataLength = -1;
		
		public RecHeader() {
			super(new Buffer(staticRegionGenerator.getTotalLength(), false));
		}
		
		@Override
		protected final void afterRead() {
			this.expectedRecDataLength = this.vDataLength.get();
			final var recordType = this.vRecordType.get();
			final var recDataMinimumLength = recordType.getRecDataMinimumLength();
			
			if (this.expectedRecDataLength < recDataMinimumLength) {
				System.err.println(Arrays.toString(this.buffer.cloneBytes()));
				throw new IllegalStateException(String.format("DataLength(%s) < RecordType(%s).MinimumLength(%s)",
						this.expectedRecDataLength, recordType, recDataMinimumLength));
			}
		}
		
		@Override
		protected final void beforeWrite() {
			this.vDataArchitectureLevel.set(3);
		}
		
		public final RecData newRecData() {
			final var recordType = this.vRecordType.get();
			final var bufferLength = Math.max(recordType.getRecDataMinimumLength(), this.vDataLength.get());
			final var buffer = new Buffer(bufferLength, this.vLittleEndian.get());
			
			if (0 <= this.expectedRecDataLength) {
				buffer.setLengthLimit(expectedRecDataLength);
			}
			
			final var result = recordType.newRecData(buffer);
			
			if (0 <= this.expectedRecDataLength && this.expectedRecDataLength != buffer.getLength()) {
				throw new IllegalStateException(String.format("Expected data length: %s Actual: %s",
						this.expectedRecDataLength, buffer.getLength()));
			}
			
			buffer.setLengthLimit(Integer.MAX_VALUE);
			
			return result;
		}
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecHeader.class);
		
		private static final Buffer.Region LANGUAGE_CODE       = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RECORD_TYPE         = staticRegionGenerator.newFixedLength(2);
		private static final Buffer.Region DATA_ARCH_LEVEL     = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region FLAG                = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region DATA_REC_EDIT_LEVEL = staticRegionGenerator.newFixedLength(1);
		private static final Buffer.Region RESERVED_01         = staticRegionGenerator.newFixedLength(4);
		private static final Buffer.Region DATA_LENGTH         = staticRegionGenerator.newFixedLength(2);
		
		static {
			Helpers.ignore(RESERVED_01);
		}
		
		/**
		 * @author 2oLDNncs 20250203
		 */
		public static enum LanguageCode {
			
			HLASM, COBOL, PL1;
			
			static final Decoder<Integer, LanguageCode> decoder =
					new ReversibleMap<Integer, LanguageCode>()
					.set(16, HLASM)
					.set(17, COBOL)
					.set(40, PL1)
					;
			
		}
		
		/**
		 * @author 2oLDNncs 20250204
		 */
		public static enum RecordType {
			
			JOB_IDENTIFICATION        (RecData_JobIdentification.class),
			ADATA_IDENTIFICATION      (RecData_AdataIdentification.class),
			COMPILATION_UNIT_DELIMITER(RecData_CompilationUnitDelimiter.class),
			OPTIONS                   (RecData_Options.class),
			EXTERNAL_SYMBOL           (RecData_ExternalSymbol.class),
			PARSE_TREE                (RecData_ParseTree.class),
			TOKEN                     (RecData_Token.class),
			SOURCE                    (RecData_Source.class),
			SOURCE_ERROR              (RecData_SourceError.class),
			COPY_REPLACING            (RecData_CopyReplacing.class),
			SYMBOL                    (RecData_Symbol.class),
			SYMBOL_CROSS_REFERENCE    (RecData_SymbolCrossReference.class),
			NESTED_PROGRAM            (RecData_NestedProgram.class),
			LIBRARY                   (RecData_Library.class),
			STATISTICS                (RecData_Statistics.class),
			EVENTS                    (RecData_Events.class),
			;
			
			private final Class<? extends RecData> recDataClass;
			
			private RecordType(final Class<? extends RecData> recDataClass) {
				this.recDataClass = recDataClass;
			}
			
			public final boolean isInstance(final RecData rd) {
				return this.recDataClass.isInstance(rd);
			}
			
			public final int getRecDataMinimumLength() {
				return RecPart.getStaticLength(this.recDataClass);
			}
			
			public final RecData newRecData(final Buffer buffer) {
				try {
					return this.recDataClass.getConstructor(Buffer.class).newInstance(buffer);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			static final Decoder<Integer, RecordType> decoder =
					new ReversibleMap<Integer, RecordType>()
					.set(0x0000, JOB_IDENTIFICATION)
					.set(0x0001, ADATA_IDENTIFICATION)
					.set(0x0002, COMPILATION_UNIT_DELIMITER)
					.set(0x0010, OPTIONS)
					.set(0x0020, EXTERNAL_SYMBOL)
					.set(0x0024, PARSE_TREE)
					.set(0x0030, TOKEN)
					.set(0x0032, SOURCE_ERROR)
					.set(0x0038, SOURCE)
					.set(0x0039, COPY_REPLACING)
					.set(0x0042, SYMBOL)
					.set(0x0044, SYMBOL_CROSS_REFERENCE)
					.set(0x0046, NESTED_PROGRAM)
					.set(0x0060, LIBRARY)
					.set(0x0090, STATISTICS)
					.set(0x0120, EVENTS)
					;
			
			private static final Map<Class<? extends RecData>, RecordType> fromClass = new HashMap<>();
			
			static {
				for (final var rt : values()) {
					fromClass.put(rt.recDataClass, rt);
				}
			}
			
			public static final RecordType fromRecDataClass(final Class<? extends RecData> cls) {
				return Objects.requireNonNull(fromClass.get(cls));
			}
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250202
	 */
	static final class ReadingContext {
		
		final InputStream input;
		
		int lineNumber;
		
		int columnNumber;
		
		ReadingContext(final InputStream input) {
			this.input = input;
		}
		
		public final void read(final byte[] bytes) throws IOException {
			final var n = this.input.read(bytes);
			
			if (bytes.length != n) {
				throw new IllegalStateException(String.format("Read error at (%s:%s): Expected %s bytes, Actual %s bytes",
						this.lineNumber, this.columnNumber, bytes.length, n));
			}
		}
		
	}
	
}
