package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250202
 */
public final class RecData_X0002_CompilationUnitDelimiter extends RecData {
	
	public final EnumVar<CompilationUnitDelimiterType> vType = this.newEnumVar(TYPE, CompilationUnitDelimiterType.decoder);
	
	public RecData_X0002_CompilationUnitDelimiter(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0002_CompilationUnitDelimiter.class);
	
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