package t6bygedq.lib.cbl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 2oLDNncs 20250223
 */
public final class RecHeader_4_2 extends RecHeader {
	
	public final EnumVar<RecordType_4_2> vRecordType = this.newEnumVar(RECORD_TYPE, RecordType_4_2.decoder);
	
	@Override
	public final RecordType getRecordType() {
		return this.vRecordType.get();
	}
	
	@Override
	public final <R extends RecData> void setRecordTypeFromDataClass(final Class<R> cls) {
		this.vRecordType.set(RecHeader_4_2.RecordType_4_2.fromRecDataClass(cls));
	}
	
	/**
	 * @author 2oLDNncs 20250223
	 */
	public static enum RecordType_4_2 implements RecordType {
		
		JOB_IDENTIFICATION        (RecData_X0000_JobIdentification.class),
		ADATA_IDENTIFICATION      (RecData_X0001_AdataIdentification.class),
		COMPILATION_UNIT_DELIMITER(RecData_X0002_CompilationUnitDelimiter.class),
		OPTIONS                   (RecData_X0010_Options_4_2.class),
		EXTERNAL_SYMBOL           (RecData_X0020_ExternalSymbol.class),
		PARSE_TREE                (RecData_X0024_ParseTree.class),
		TOKEN                     (RecData_X0030_Token.class),
		SOURCE                    (RecData_X0038_Source.class),
		SOURCE_ERROR              (RecData_X0032_SourceError.class),
		COPY_REPLACING            (RecData_X0039_CopyReplacing.class),
		SYMBOL                    (RecData_X0042_Symbol_4_2.class),
		SYMBOL_CROSS_REFERENCE    (RecData_X0044_SymbolCrossReference.class),
		NESTED_PROGRAM            (RecData_X0046_NestedProgram.class),
		LIBRARY                   (RecData_X0060_Library.class),
		STATISTICS                (RecData_X0090_Statistics.class),
		EVENTS                    (RecData_X0120_Events.class),
		;
		
		private final Class<? extends RecData> recDataClass;
		
		private RecordType_4_2(final Class<? extends RecData> recDataClass) {
			this.recDataClass = recDataClass;
		}
		
		@Override
		public final Class<? extends RecData> getRecDataClass() {
			return this.recDataClass;
		}
		
		static final Decoder<Integer, RecordType_4_2> decoder =
				new ReversibleMap<Integer, RecordType_4_2>()
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
		
		private static final Map<Class<? extends RecData>, RecordType_4_2> fromClass = new HashMap<>();
		
		static {
			for (final var rt : values()) {
				fromClass.put(rt.recDataClass, rt);
			}
		}
		
		public static final RecordType_4_2 fromRecDataClass(final Class<? extends RecData> cls) {
			return Objects.requireNonNull(fromClass.get(cls));
		}
		
	}
	
}