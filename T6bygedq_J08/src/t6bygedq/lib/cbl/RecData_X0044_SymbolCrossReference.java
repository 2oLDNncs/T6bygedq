package t6bygedq.lib.cbl;

import java.util.ArrayList;
import java.util.List;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0044_SymbolCrossReference extends RecData {
	
	public final LongVar                     vStatementNumber     = this.newLongVar(STATEMENT_NUMBER_or_STATEMENT_COUNT);
	public final LongVar                     vStatementCount      = this.vStatementNumber;
	public final IntVar                      vNumberOfReferences  = this.newIntVar(NUMBER_OF_REFERENCES);
	public final EnumVar<CrossReferenceType> vCrossReferenceType  = this.newEnumVar(CROSS_REFERENCE_TYPE, CrossReferenceType.decoder);
	
	public final StringVar                   vSymbolName          = this.newStringVarV(SYMBOL_LENGTH);
	
	private final List<FlagAndStmt> flagAndStmtList = new ArrayList<>();
	private final List<Stmt>        stmtList        = new ArrayList<>();
	
	public RecData_X0044_SymbolCrossReference(final Buffer buffer) {
		super(buffer);
	}
	
	public final FlagAndStmt getFlagAndStmt(final int index) {
		return this.flagAndStmtList.get(index);
	}
	
	public final Stmt getStmt(final int index) {
		return this.stmtList.get(index);
	}
	
	public final Stmt newStmt() {
		if (!CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
			throw new IllegalStateException();
		}
		
		if (!this.flagAndStmtList.isEmpty()) {
			throw new IllegalStateException();
		}
		
		final Stmt result = this.new Stmt();
		
		this.stmtList.add(result);
		
		return result;
	}
	
	public final Stmt addStmt() {
		final Stmt result = this.newStmt();
		
		this.vNumberOfReferences.set(this.vNumberOfReferences.get() + 1);
		
		return result;
	}
	
	public final FlagAndStmt newFlagAndStmt() {
		if (CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
			throw new IllegalStateException();
		}
		
		if (!this.stmtList.isEmpty()) {
			throw new IllegalStateException();
		}
		
		final FlagAndStmt result = this.new FlagAndStmt();
		
		this.flagAndStmtList.add(result);
		
		return result;
	}
	
	public final FlagAndStmt addFlagAndStmt() {
		final FlagAndStmt result = this.newFlagAndStmt();
		
		this.vNumberOfReferences.set(this.vNumberOfReferences.get() + 1);
		
		return result;
	}
	
	@Override
	protected final void afterRead() {
		super.afterRead();
		
		final int n = this.vNumberOfReferences.get();
		
		if (CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
			for (int i = 0; i < n; i += 1) {
				final Stmt s = this.newStmt();
				Helpers.dprintlnf(" Stmt(%s)", i);
				Helpers.dprintlnf("  StatementNumber<%s>", s.vStatementNumber);
			}
		} else {
			for (int i = 0; i < n; i += 1) {
				final FlagAndStmt fs = this.newFlagAndStmt();
				Helpers.dprintlnf(" FlagAndStmt(%s)", i);
				Helpers.dprintlnf("  ReferenceFlag<%s>", fs.vReferenceFlag);
				Helpers.dprintlnf("  StatementNumber<%s>", fs.vStatementNumber);
			}
		}
	}
	
	@Override
	protected final void beforeWrite() {
		if (CrossReferenceType.STATEMENT.equals(this.vCrossReferenceType.get())) {
			if (!this.flagAndStmtList.isEmpty()) {
				throw new IllegalStateException();
			}
			
			if (this.vNumberOfReferences.get() != this.stmtList.size()) {
				throw new IllegalStateException();
			}
		} else {
			if (!this.stmtList.isEmpty()) {
				throw new IllegalStateException();
			}
			
			if (this.vNumberOfReferences.get() != this.flagAndStmtList.size()) {
				throw new IllegalStateException();
			}
		}
	}
	
	/**
	 * @author 2oLDNncs 20250211
	 */
	public final class FlagAndStmt {
		
		public final EnumVar<ReferenceFlag> vReferenceFlag   = newEnumVar(1, ReferenceFlag.decoder);
		public final LongVar                vStatementNumber = newLongVar(4);
		
	}
	
	/**
	 * @author 2oLDNncs 20250211
	 */
	public final class Stmt {
		
		public final LongVar                vStatementNumber = newLongVar(4);
		
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0044_SymbolCrossReference.class);
	
	private static final Buffer.Region SYMBOL_LENGTH                       = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region STATEMENT_NUMBER_or_STATEMENT_COUNT = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region NUMBER_OF_REFERENCES                = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region CROSS_REFERENCE_TYPE                = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01                         = staticRegionGenerator.newFixedLength(7);
	
	static {
		Helpers.ignore(RESERVED_01);
	}
	
	/**
	 * @author 2oLDNncs 20250211
	 */
	public static enum CrossReferenceType {
		
		PROGRAM,
		PROCEDURE,
		STATEMENT,
		SYMBOL_OR_DATA_NAME,
		METHOD,
		CLASS,
		;
		
		static final Decoder<Integer, CrossReferenceType> decoder =
				new ReversibleMap<Integer, CrossReferenceType>()
				.set(1, PROGRAM)
				.set(2, PROCEDURE)
				.set(3, STATEMENT)
				.set(4, SYMBOL_OR_DATA_NAME)
				.set(5, METHOD)
				.set(6, CLASS)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250211
	 */
	public static enum ReferenceFlag {
		
		REFERENCE_ONLY,
		MODIFICATION,
		ALTER,
		GO_TO_DEPENDING_ON,
		END_OF_RANGE_OF_PERFORM_THROUGH,
		GO_TO,
		PERFORM,
		ALTER_TO_PROCEED_TO,
		USE_FOR_DEBUGGING,
		;
		
		static final Decoder<Integer, ReferenceFlag> decoder =
				new ReversibleMap<Integer, ReferenceFlag>()
				.set(0xFF & Buffer.EBCDIC.encode(" ").get(), REFERENCE_ONLY)
				.set(0xFF & Buffer.EBCDIC.encode("M").get(), MODIFICATION)
				.set(0xFF & Buffer.EBCDIC.encode("A").get(), ALTER)
				.set(0xFF & Buffer.EBCDIC.encode("D").get(), GO_TO_DEPENDING_ON)
				.set(0xFF & Buffer.EBCDIC.encode("E").get(), END_OF_RANGE_OF_PERFORM_THROUGH)
				.set(0xFF & Buffer.EBCDIC.encode("G").get(), GO_TO)
				.set(0xFF & Buffer.EBCDIC.encode("P").get(), PERFORM)
				.set(0xFF & Buffer.EBCDIC.encode("T").get(), ALTER_TO_PROCEED_TO)
				.set(0xFF & Buffer.EBCDIC.encode("U").get(), USE_FOR_DEBUGGING)
				;
	}
	
}