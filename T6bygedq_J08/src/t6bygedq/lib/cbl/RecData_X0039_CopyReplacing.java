package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0039_CopyReplacing extends RecData {
	
	public final LongVar vStartingLineNumberOfReplacedString   = this.newLongVar(STARTING_LINE_NUMBER_OF_REPLACED_STRING);
	public final LongVar vStartingColumnNumberOfReplacedString = this.newLongVar(STARTING_COLUMN_NUMBER_OF_REPLACED_STRING);
	public final LongVar vEndingLineNumberOfReplacedString     = this.newLongVar(ENDING_LINE_NUMBER_OF_REPLACED_STRING);
	public final LongVar vEndingColumnNumberOfReplacedString   = this.newLongVar(ENDING_COLUMN_NUMBER_OF_REPLACED_STRING);
	public final LongVar vStartingLineNumberOfOriginalString   = this.newLongVar(STARTING_LINE_NUMBER_OF_ORIGINAL_STRING);
	public final LongVar vStartingColumnNumberOfOriginalString = this.newLongVar(STARTING_COLUMN_NUMBER_OF_ORIGINAL_STRING);
	public final LongVar vEndingLineNumberOfOriginalString     = this.newLongVar(ENDING_LINE_NUMBER_OF_ORIGINAL_STRING);
	public final LongVar vEndingColumnNumberOfOriginalString   = this.newLongVar(ENDING_COLUMN_NUMBER_OF_ORIGINAL_STRING);
	
	public RecData_X0039_CopyReplacing(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0039_CopyReplacing.class);
	
	private static final Buffer.Region STARTING_LINE_NUMBER_OF_REPLACED_STRING   = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region STARTING_COLUMN_NUMBER_OF_REPLACED_STRING = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region ENDING_LINE_NUMBER_OF_REPLACED_STRING     = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region ENDING_COLUMN_NUMBER_OF_REPLACED_STRING   = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region STARTING_LINE_NUMBER_OF_ORIGINAL_STRING   = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region STARTING_COLUMN_NUMBER_OF_ORIGINAL_STRING = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region ENDING_LINE_NUMBER_OF_ORIGINAL_STRING     = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region ENDING_COLUMN_NUMBER_OF_ORIGINAL_STRING   = staticRegionGenerator.newFixedLength(4);
	
}