package t6bygedq.lib.cbl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.IntSupplier;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250206
 */
public final class Buffer {
	
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
	
	final void insertBytes(final int offset, final int length) {
		if ((long) this.getLengthLimit() < (long) offset + length) {
			throw new IllegalStateException(String.format("%s < %s + %s", this.getLengthLimit(), offset, length));
		}
		
		final var newBytes = new byte[this.bytes.length + length];
		
		System.arraycopy(this.bytes, 0, newBytes, 0, offset);
		System.arraycopy(this.bytes, offset, newBytes, offset + length, bytes.length - offset);
		
		this.bytes = newBytes;
		this.incrModClock();
	}
	
	public final String getStr(final int offset, final int length) {
		Helpers.checkFromIndexSize(offset, length, this.bytes.length);
		
		return new String(this.bytes, offset, length, EBCDIC);
	}
	
	public final String getStr(final Region region) {
		return this.getStr(region.getOffset(), region.getLength());
	}
	
	public final boolean testBit(final Region region, final int bitIndex) {
		return Helpers.testBit((int) this.getNum(region), bitIndex);
	}
	
	public final void setBit(final Region region, final int bitIndex, final boolean value) {
		this.setNum(region, Helpers.setBit((int) this.getNum(region), bitIndex, value));
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
		
		Helpers.setNum(bytes, offset, length, textDecimal, value);
		
		this.incrModClock();
	}
	
	public final long getNum(final int offset, final int length, final boolean textDecimal) {
		if (textDecimal) {
			return Long.parseLong(this.getStr(offset, length));
		}
		
		return Helpers.getNum(this.bytes, offset, length, this.isLittleEndian());
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
	
	public static boolean DEBUG = false;
	
	public static final Charset EBCDIC = Charset.forName("Cp1047");
	
	public static final byte SPACE = EBCDIC.encode(" ").get();
	
	/**
	 * @author 2oLDNncs 20250312
	 */
	public static abstract interface DebugStructure {
		
		public default void debugStructure(final PrintStream out, final String indent) {
			out.println(String.format("%s%s %X",
					indent, this.getClass().getSimpleName(), this.hashCode()));
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250206
	 */
	public static final class Region implements DebugStructure {
		
		private final Offset offset;
		
		private final Length length;
		
		private NumberFormat numberFormat = NumberFormat.BINARY;
		
		Region(final Offset offset, final Length length) {
			this.offset = offset;
			this.length = length;
		}
		
		@Override
		public final void debugStructure(final PrintStream out, final String indent) {
			DebugStructure.super.debugStructure(out, indent);
			this.offset.debugStructure(out, indent + "\t");
			this.length.debugStructure(out, indent + "\t");
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
				if (DEBUG) {
					System.out.println(Helpers.dformat("this.last.debugStructure"));
					this.last.debugStructure(System.out, "");
				}
				
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
		private static abstract interface Offset extends IntSupplier, DebugStructure {
			//pass
		}
		
		/**
		 * @author 2oLDNncs 20250206
		 */
		private static final class AbsoluteOffset extends Value implements Offset {
			
			AbsoluteOffset(final int value) {
				super(value);
			}
			
			@Override
			public final void debugStructure(final PrintStream out, final String indent) {
				out.println(String.format("%s%s(%s) %X",
						indent, this.getClass().getSimpleName(), this.getAsInt(), this.hashCode()));
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
			
			@Override
			public final void debugStructure(final PrintStream out, final String indent) {
				Offset.super.debugStructure(out, indent);
				this.target.debugStructure(out, indent + "\t");
			}
			
		}
		
		/**
		 * @author 2oLDNncs 20250206
		 */
		private static abstract interface Length extends IntSupplier, DebugStructure {
			//pass
		}
		
		/**
		 * @author 2oLDNncs 20250206
		 */
		private static final class FixedLength extends Value implements Length {
			
			FixedLength(final int value) {
				super(value);
			}
			
			@Override
			public final void debugStructure(final PrintStream out, final String indent) {
				out.println(String.format("%s%s(%s) %X",
						indent, this.getClass().getSimpleName(), this.getAsInt(), this.hashCode()));
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
			
			@Override
			public final void debugStructure(final PrintStream out, final String indent) {
				out.println(String.format("%s%s(. * %s) %X",
						indent, this.getClass().getSimpleName(), this.elementLength, this.hashCode()));
				this.target.debugStructure(out, indent + "\t");
			}
			
		}
		
	}
	
}