package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0090_Statistics extends RecData {
	
	public final LongVar    vSourceRecords               = this.newLongVar(SOURCE_RECORDS);
	public final LongVar    vDataDivisionStatements      = this.newLongVar(DATA_DIVISION_STATEMENTS);
	public final LongVar    vProcedureDivisionStatements = this.newLongVar(PROCEDURE_DIVISION_STATEMENTS);
	public final IntVar     vCompilationNumber           = this.newIntVar(COMPILATION_NUMBER);
	public final IntVar     vErrorSeverity               = this.newIntVar(ERROR_SEVERITY);
	public final BooleanVar vEndOfJob                    = this.newBooleanVar(FLAGS, 0);
	public final BooleanVar vClassDefinition             = this.newBooleanVar(FLAGS, 1);
	public final IntVar     vEojSeverity                 = this.newIntVar(EOJ_SEVERITY);
	
	public final StringVar  vProgramName                 = this.newStringVarV(PROGRAM_NAME_LENGTH);
	
	public RecData_X0090_Statistics(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0090_Statistics.class);
	
	private static final Buffer.Region SOURCE_RECORDS                = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region DATA_DIVISION_STATEMENTS      = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PROCEDURE_DIVISION_STATEMENTS = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region COMPILATION_NUMBER            = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region ERROR_SEVERITY                = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region FLAGS                         = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region EOJ_SEVERITY                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region PROGRAM_NAME_LENGTH           = staticRegionGenerator.newFixedLength(1);
	
}