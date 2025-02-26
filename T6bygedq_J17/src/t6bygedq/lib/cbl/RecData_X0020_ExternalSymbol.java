package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0020_ExternalSymbol extends RecData {
	
	public final EnumVar<SectionType> vSectionType      = this.newEnumVar(SECTION_TYPE, SectionType.decoder);
	public final LongVar              vSymbolId         = this.newLongVar(SYMBOL_ID);
	public final LongVar              vLineNumber       = this.newLongVar(LINE_NUMBER);
	public final StringVar            vExternalName     = this.newStringVarV(EXTERNAL_NAME_LENGTH);
	private final StringVar           vAliasSectionName = this.newStringVarV(ALIAS_SECTION_NAME_LENGTH);
	
	public RecData_X0020_ExternalSymbol(final Buffer buffer) {
		super(buffer);
		Helpers.ignore(this.vAliasSectionName);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0020_ExternalSymbol.class);
	
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