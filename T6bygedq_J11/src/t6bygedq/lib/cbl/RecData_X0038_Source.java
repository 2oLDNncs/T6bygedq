package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0038_Source extends RecData {
	
	public final LongVar   vLineNumber              = this.newLongVar(LINE_NUMBER);
	public final LongVar   vInputRecordNumber       = this.newLongVar(INPUT_RECORD_NUMBER);
	public final IntVar    vPrimaryFileNumber       = this.newIntVar(PRIMARY_FILE_NUMBER);
	public final IntVar    vLibraryFileNumber       = this.newIntVar(LIBRARY_FILE_NUMBER);
	public final LongVar   vParentRecordNumber      = this.newLongVar(PARENT_RECORD_NUMBER);
	public final IntVar    vParentPrimaryFileNumber = this.newIntVar(PARENT_PRIMARY_FILE_NUMBER);
	public final IntVar    vParentLibraryFileNumber = this.newIntVar(PARENT_LIBRARY_FILE_NUMBER);
	public final StringVar vSourceRecord            = this.newStringVarV(SOURCE_RECORD_LENGTH);
	
	public RecData_X0038_Source(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0038_Source.class);
	
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