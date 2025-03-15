package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0032_SourceError extends RecData {
	
	public final LongVar   vStatementNumber = this.newLongVar(STATEMENT_NUMBER);
	public final StringVar vErrorIdentifier = this.newStringVarF(ERROR_IDENTIFER);
	public final IntVar    vErrorSeverity   = this.newIntVar(ERROR_SEVERITY);
	public final IntVar    vLinePosition    = this.newIntVar(LINE_POSITION);
	public final StringVar vErrorMessage    = this.newStringVarV(ERROR_MESSAGE_LENGTH);
	
	public RecData_X0032_SourceError(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0032_SourceError.class);
	
	private static final Buffer.Region STATEMENT_NUMBER     = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region ERROR_IDENTIFER      = staticRegionGenerator.newFixedLength(16);
	private static final Buffer.Region ERROR_SEVERITY       = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region ERROR_MESSAGE_LENGTH = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region LINE_POSITION        = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01          = staticRegionGenerator.newFixedLength(7);
	
	static {
		Helpers.ignore(RESERVED_01);
	}
	
}