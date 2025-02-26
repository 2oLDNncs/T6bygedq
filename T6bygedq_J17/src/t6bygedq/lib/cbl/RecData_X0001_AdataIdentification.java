package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250202
 */
public final class RecData_X0001_AdataIdentification extends RecData {
	
	public final LongVar               vTime         = this.newLongVar(TIME);
	public final IntVar                vCcsid        = this.newIntVar(CCSID);
	public final EnumVar<CharacterSet> vCharacterSet = this.newEnumVar(CHARACTER_SET_FLAGS, CharacterSet.decoder);
	public final StringVar             vCodePageName = this.newStringVarV(CODE_PAGE_NAME_LENGTH);
	
	public RecData_X0001_AdataIdentification(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0001_AdataIdentification.class);
	
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