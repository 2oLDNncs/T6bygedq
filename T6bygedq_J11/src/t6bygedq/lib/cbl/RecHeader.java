package t6bygedq.lib.cbl;

import java.util.Arrays;
import java.util.Map;

import t6bygedq.lib.Helpers;

public abstract class RecHeader extends RecPart {

	public final EnumVar<LanguageCode> vLanguageCode                = this.newEnumVar(LANGUAGE_CODE, LanguageCode.decoder);
	public final IntVar                vDataArchitectureLevel       = this.newIntVar(DATA_ARCH_LEVEL);
	public final BooleanVar            vLittleEndian                = this.newBooleanVar(FLAG, 6);
	public final BooleanVar            vContinuedInNextRec          = this.newBooleanVar(FLAG, 7);
	public final IntVar                vDataRecordEditionLevel      = this.newIntVar(DATA_REC_EDIT_LEVEL);
	public final IntVar                vDataLength                  = this.newIntVar(DATA_LENGTH);
	
	private int expectedRecDataLength = -1;
	
	protected RecHeader() {
		super(new Buffer(staticRegionGenerator.getTotalLength(), false));
	}
	
	@Override
	protected final void getProperties(final Map<String, Object> properties) {
		super.getProperties(properties);
		properties.put("RecordType", this.getRecordType());
	}
	
	@Override
	protected final void afterRead() {
		super.afterRead();
		
		this.expectedRecDataLength = this.vDataLength.get();
		final var recordType = this.getRecordType();
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
	
	public abstract <R extends RecData> void setRecordTypeFromDataClass(final Class<R> cls);
	
	public abstract RecordType getRecordType();
	
	public final RecData newRecData() {
		final var recordType = this.getRecordType();
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
	
	protected static final Buffer.Region.Generator staticRegionGenerator = getStaticRegionGenerator(RecHeader.class);
	
	private static final Buffer.Region LANGUAGE_CODE = staticRegionGenerator.newFixedLength(1);
	protected static final Buffer.Region RECORD_TYPE = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region DATA_ARCH_LEVEL = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region FLAG = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DATA_REC_EDIT_LEVEL = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01 = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region DATA_LENGTH = staticRegionGenerator.newFixedLength(2);
	
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
	 * @author 2oLDNncs 20250223
	 */
	public static abstract interface RecordType {
		
		public abstract Class<? extends RecData> getRecDataClass();
		
		public default boolean isInstance(final RecData rd) {
			return this.getRecDataClass().isInstance(rd);
		}
		
		public default int getRecDataMinimumLength() {
			return RecPart.getStaticLength(this.getRecDataClass());
		}
		
		public default RecData newRecData(final Buffer buffer) {
			try {
				return this.getRecDataClass().getConstructor(Buffer.class).newInstance(buffer);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
}