package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0046_NestedProgram extends RecData {
	
	public final LongVar    vStatementDefinition    = this.newLongVar(STATEMENT_DEFINITION);
	public final IntVar     vProgramNestingLevel    = this.newIntVar(PROGRAM_NESTING_LEVEL);
	public final BooleanVar vInital                 = this.newBooleanVar(PROGRAM_ATTRIBUTES, 0);
	public final BooleanVar vCommon                 = this.newBooleanVar(PROGRAM_ATTRIBUTES, 1);
	public final BooleanVar vProcedureDivisionUsing = this.newBooleanVar(PROGRAM_ATTRIBUTES, 2);
	
	public final StringVar  vProgramName            = this.newStringVarV(PROGRAM_NAME_LENGTH);
	
	public RecData_X0046_NestedProgram(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0046_NestedProgram.class);
	
	private static final Buffer.Region STATEMENT_DEFINITION                   = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PROGRAM_NESTING_LEVEL                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region PROGRAM_ATTRIBUTES                     = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01                            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region PROGRAM_NAME_LENGTH                    = staticRegionGenerator.newFixedLength(1);
	
	static {
		Helpers.ignore(RESERVED_01);
	}
	
}