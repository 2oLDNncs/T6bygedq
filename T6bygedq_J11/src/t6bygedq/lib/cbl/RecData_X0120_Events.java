package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0120_Events extends RecData {
	
	private Event event;
	
	public RecData_X0120_Events(final Buffer buffer) {
		super(buffer);
	}
	
	public final <E extends Event> E getEvent() {
		return Helpers.cast(this.event);
	}
	
	public final Timestamp setEventTimestamp() {
		this.event = this.new Timestamp();
		
		return this.getEvent();
	}
	
	public final Processor setEventProcessor() {
		this.event = this.new Processor();
		
		return this.getEvent();
	}
	
	public final FileEnd setEventFileEnd() {
		this.event = this.new FileEnd();
		
		return this.getEvent();
	}
	
	public final Program setEventProgram() {
		this.event = this.new Program();
		
		return this.getEvent();
	}
	
	public final FileId setEventFileId() {
		this.event = this.new FileId();
		
		return this.getEvent();
	}
	
	public final Error setEventError() {
		this.event = this.new Error();
		
		return this.getEvent();
	}
	
	@Override
	protected final void afterRead() {
		super.afterRead();
		
		final var vRectypePrefix = this.newStringVarF(RECTYPE_PREFIX);
		
		switch (vRectypePrefix.get()) {
		case "TIMES":
			this.setEventTimestamp().afterRead();
			break;
		case "PROCE":
			this.setEventProcessor().afterRead();
			break;
		case "FILEE":
			this.setEventFileEnd().afterRead();
			break;
		case "PROGR":
			this.setEventProgram().afterRead();
			break;
		case "FILEI":
			this.setEventFileId().afterRead();
			break;
		case "ERROR":
			this.setEventError().afterRead();
			break;
		default:
			throw new IllegalStateException(String.format("Unknown Event Record Type Prefix: %s", vRectypePrefix.get()));
		}
	}
	
	static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0120_Events.class);
	
	private static final Buffer.Region LENGTH_OF_FOLLOWING = staticRegionGenerator.newFixedLength(2);
	
	private static final Buffer.Region.Generator staticRegionGenerator2 = staticRegionGenerator.clone();
	
	private static final Buffer.Region RECTYPE_PREFIX = staticRegionGenerator2.newFixedLength(5);
	
	static {
		Helpers.ignore(LENGTH_OF_FOLLOWING);
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	public abstract class Event extends RecPart {
		
		public final StringVar  vRecordType;
		private final StringVar vBlank01;
		public final IntVar     vRevisionLevel;
		private final StringVar vBlank02;
		
		protected Event(final Buffer.Region recordType, final Buffer.Region blank01,
				final Buffer.Region revisionLevel, final Buffer.Region blank02) {
			super(RecData_X0120_Events.this.buffer);
			this.vRecordType    = newStringVarF(recordType);
			this.vBlank01       = newStringVarF(blank01);
			this.vRevisionLevel = newIntVar(revisionLevel);
			this.vBlank02       = newStringVarF(blank02);
		}
		
		@Override
		protected void beforeWrite() {
			this.vBlank01.set(" ");
			this.vBlank02.set(" ");
		}
		
		protected final void checkRecordType(final String expectedRecordType) {
			if (!expectedRecordType.equals(this.vRecordType.get())) {
				throw new IllegalStateException(String.format("%s != %s", expectedRecordType, this.vRecordType.get()));
			}
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250212
	 */
	public final class Timestamp extends Event {
		
		public final LongVar vDate    = this.newLongVar(Timestamp_Static.DATE);
		public final IntVar  vHour    = this.newIntVar(Timestamp_Static.HOUR);
		public final IntVar  vMinutes = this.newIntVar(Timestamp_Static.MINUTES);
		public final IntVar  vSecond  = this.newIntVar(Timestamp_Static.SECONDS);
		
		Timestamp() {
			super(Timestamp_Static.RECORD_TYPE, Timestamp_Static.BLANK_01, Timestamp_Static.REVISION_LEVEL, Timestamp_Static.BLANK_02);
		}
		
		@Override
		protected final void afterRead() {
			this.checkRecordType(S_RECORD_TYPE);
			super.afterRead();
		}
		
		@Override
		protected void beforeWrite() {
			super.beforeWrite();
			this.vRecordType.set(S_RECORD_TYPE);
		}
		
		public static final String S_RECORD_TYPE = "TIMESTAMP";
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	static final class Timestamp_Static {
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_X0120_Events.Timestamp.class, RecData_X0120_Events.staticRegionGenerator);
		
		static final Buffer.Region RECORD_TYPE    = staticRegionGenerator.newFixedLength(Timestamp.S_RECORD_TYPE.length());
		static final Buffer.Region BLANK_01       = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REVISION_LEVEL = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_02       = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region DATE           = staticRegionGenerator.newFixedLength(8).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region HOUR           = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region MINUTES        = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region SECONDS        = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	public final class Processor extends Event {
		
		public final IntVar     vOutputFileId       = this.newIntVar(Event_Static.OUTPUT_FILE_ID);
		private final StringVar vBlank03            = this.newStringVarF(Event_Static.BLANK_03);
		public final IntVar     vLineClassIndicator = this.newIntVar(Event_Static.LINE_CLASS_INDICATOR);
		
		Processor() {
			super(Event_Static.RECORD_TYPE, Event_Static.BLANK_01, Event_Static.REVISION_LEVEL, Event_Static.BLANK_02);
		}
		
		@Override
		protected final void afterRead() {
			this.checkRecordType(S_RECORD_TYPE);
			super.afterRead();
		}
		
		@Override
		protected final void beforeWrite() {
			super.beforeWrite();
			this.vRecordType.set(S_RECORD_TYPE);
			this.vBlank03.set(" ");
		}
		
		public static final String S_RECORD_TYPE = "PROCESSOR";
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	static final class Event_Static {
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_X0120_Events.Processor.class, RecData_X0120_Events.staticRegionGenerator);
		
		static final Buffer.Region RECORD_TYPE          = staticRegionGenerator.newFixedLength(Processor.S_RECORD_TYPE.length());
		static final Buffer.Region BLANK_01             = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REVISION_LEVEL       = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_02             = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region OUTPUT_FILE_ID       = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_03             = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region LINE_CLASS_INDICATOR = staticRegionGenerator.newFixedLength(1);
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	public final class FileEnd extends Event {
		
		public final IntVar     vInputFileId        = this.newIntVar(FileEnd_Static.INPUT_FILE_ID);
		private final StringVar vBlank03            = this.newStringVarF(FileEnd_Static.BLANK_03);
		public final IntVar     vExpansionIndicator = this.newIntVar(FileEnd_Static.EXPANSION_INDICATOR);
		
		FileEnd() {
			super(FileEnd_Static.RECORD_TYPE, FileEnd_Static.BLANK_01, FileEnd_Static.REVISION_LEVEL, FileEnd_Static.BLANK_02);
		}
		
		@Override
		protected final void afterRead() {
			this.checkRecordType(S_RECORD_TYPE);
			super.afterRead();
		}
		
		@Override
		protected final void beforeWrite() {
			super.beforeWrite();
			this.vRecordType.set(S_RECORD_TYPE);
			this.vBlank03.set(" ");
		}
		
		public static final String S_RECORD_TYPE = "FILEEND";
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	static final class FileEnd_Static {
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_X0120_Events.FileEnd.class, RecData_X0120_Events.staticRegionGenerator);
		
		static final Buffer.Region RECORD_TYPE         = staticRegionGenerator.newFixedLength(FileEnd.S_RECORD_TYPE.length());
		static final Buffer.Region BLANK_01            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REVISION_LEVEL      = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_02            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region INPUT_FILE_ID       = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_03            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region EXPANSION_INDICATOR = staticRegionGenerator.newFixedLength(1);
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	public final class Program extends Event {
		
		public final IntVar     vOutputFileId             = this.newIntVar(Program_Static.OUTPUT_FILE_ID);

		// Optional?
//		private final StringVar vBlank03                  = this.newStringVarF(Program_Static.BLANK_03);
//		public final IntVar     vProgramInputRecordNumber = this.newIntVar(Program_Static.PROGRAM_INPUT_RECORD_NUMBER);
		
		Program() {
			super(Program_Static.RECORD_TYPE, Program_Static.BLANK_01, Program_Static.REVISION_LEVEL, Program_Static.BLANK_02);
		}
		
		@Override
		protected final void afterRead() {
			this.checkRecordType(S_RECORD_TYPE);
			super.afterRead();
		}
		
		@Override
		protected final void beforeWrite() {
			super.beforeWrite();
			this.vRecordType.set(S_RECORD_TYPE);
//			this.vBlank03.set(" ");
		}
		
		public static final String S_RECORD_TYPE = "PROGRAM";
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	static final class Program_Static {
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_X0120_Events.Program.class, RecData_X0120_Events.staticRegionGenerator);
		
		static final Buffer.Region RECORD_TYPE                 = staticRegionGenerator.newFixedLength(Program.S_RECORD_TYPE.length());
		static final Buffer.Region BLANK_01                    = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REVISION_LEVEL              = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_02                    = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region OUTPUT_FILE_ID              = staticRegionGenerator.newFixedLength(1);
		
		// Optional?
//		static final Buffer.Region BLANK_03                    = staticRegionGenerator.newFixedLength(1);
//		static final Buffer.Region PROGRAM_INPUT_RECORD_NUMBER = staticRegionGenerator.newFixedLength(1);
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	public final class FileId extends Event {
		
		public final IntVar     vInputSourceFileId        = this.newIntVar(FileId_Static.INPUT_SOURCE_FILE_ID);
		private final StringVar vBlank03                  = this.newStringVarF(FileId_Static.BLANK_03);
		public final IntVar     vReferenceIndicator       = this.newIntVar(FileId_Static.REFERENCE_INDICATOR);
		private final StringVar vBlank04                  = this.newStringVarF(FileId_Static.BLANK_04);
		
		public final StringVar  vSourceFileName           = this.newStringVarV(FileId_Static.SOURCE_FILE_NAME_LENGTH);
		
		FileId() {
			super(FileId_Static.RECORD_TYPE, FileId_Static.BLANK_01, FileId_Static.REVISION_LEVEL, FileId_Static.BLANK_02);
		}
		
		@Override
		protected final void afterRead() {
			this.checkRecordType(S_RECORD_TYPE);
			super.afterRead();
		}
		
		@Override
		protected final void beforeWrite() {
			super.beforeWrite();
			this.vRecordType.set(S_RECORD_TYPE);
			this.vBlank03.set(" ");
			this.vBlank04.set(" ");
		}
		
		public static final String S_RECORD_TYPE = "FILEID";
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	static final class FileId_Static {
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_X0120_Events.FileId.class, RecData_X0120_Events.staticRegionGenerator);
		
		static final Buffer.Region RECORD_TYPE                 = staticRegionGenerator.newFixedLength(FileId.S_RECORD_TYPE.length());
		static final Buffer.Region BLANK_01                    = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REVISION_LEVEL              = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_02                    = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region INPUT_SOURCE_FILE_ID        = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_03                    = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REFERENCE_INDICATOR         = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_04                    = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region SOURCE_FILE_NAME_LENGTH     = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	public final class Error extends Event {
		
		public final IntVar     vInputSourceFileId               = this.newIntVar(Error_Static.INPUT_SOURCE_FILE_ID);
		private final StringVar vBlank03                         = this.newStringVarF(Error_Static.BLANK_03);
		public final IntVar     vReferenceIndicator              = this.newIntVar(Error_Static.ANNOT_CLASS);
		private final StringVar vBlank04                         = this.newStringVarF(Error_Static.BLANK_04);
		public final IntVar     vErrorInputRecordNumber          = this.newIntVar(Error_Static.ERROR_INPUT_RECORD_NUMBER);
		private final StringVar vBlank05                         = this.newStringVarF(Error_Static.BLANK_05);
		public final IntVar     vErrorStartLineNumber            = this.newIntVar(Error_Static.ERROR_START_LINE_NUMBER);
		private final StringVar vBlank06                         = this.newStringVarF(Error_Static.BLANK_06);
		public final IntVar     vErrorTokenStartNumber           = this.newIntVar(Error_Static.ERROR_TOKEN_START_NUMBER);
		private final StringVar vBlank07                         = this.newStringVarF(Error_Static.BLANK_07);
		public final IntVar     vErrorEndLineNumber              = this.newIntVar(Error_Static.ERROR_END_LINE_NUMBER);
		private final StringVar vBlank08                         = this.newStringVarF(Error_Static.BLANK_08);
		public final IntVar     vErrorTokenEndNumber             = this.newIntVar(Error_Static.ERROR_TOKEN_END_NUMBER);
		private final StringVar vBlank09                         = this.newStringVarF(Error_Static.BLANK_09);
		public final StringVar  vErrorMessageIdNumber            = this.newStringVarF(Error_Static.ERROR_MESSAGE_ID_NUMBER);
		private final StringVar vBlank10                         = this.newStringVarF(Error_Static.BLANK_10);
		public final StringVar  vErrorMessageSeverityCode        = this.newStringVarF(Error_Static.ERROR_MESSAGE_SEVERITY_CODE);
		private final StringVar vBlank11                         = this.newStringVarF(Error_Static.BLANK_11);
		public final IntVar     vErrorMessageSeverityLevelNumber = this.newIntVar(Error_Static.ERROR_MESSAGE_SEVERITY_LEVEL_NUMBER);
		private final StringVar vBlank12                         = this.newStringVarF(Error_Static.BLANK_12);
		private final StringVar vBlank13                         = this.newStringVarF(Error_Static.BLANK_13);
		
		public final StringVar  vErrorMessage                    = this.newStringVarV(Error_Static.ERROR_MESSAGE_LENGTH);
		
		Error() {
			super(Error_Static.RECORD_TYPE, Error_Static.BLANK_01, Error_Static.REVISION_LEVEL, Error_Static.BLANK_02);
		}
		
		@Override
		protected final void afterRead() {
			this.checkRecordType(S_RECORD_TYPE);
			super.afterRead();
		}
		
		@Override
		protected final void beforeWrite() {
			super.beforeWrite();
			this.vRecordType.set(S_RECORD_TYPE);
			this.vBlank03.set(" ");
			this.vBlank04.set(" ");
			this.vBlank05.set(" ");
			this.vBlank06.set(" ");
			this.vBlank07.set(" ");
			this.vBlank08.set(" ");
			this.vBlank09.set(" ");
			this.vBlank10.set(" ");
			this.vBlank11.set(" ");
			this.vBlank12.set(" ");
			this.vBlank13.set(" ");
		}
		
		public static final String S_RECORD_TYPE = "ERROR";
		
	}
	
	/**
	 * @author 2oLDNncs 20250213
	 */
	static final class Error_Static {
		
		private static final Buffer.Region.Generator staticRegionGenerator =
				getStaticRegionGenerator(RecData_X0120_Events.Error.class, RecData_X0120_Events.staticRegionGenerator);
		
		static final Buffer.Region RECORD_TYPE                         = staticRegionGenerator.newFixedLength(Error.S_RECORD_TYPE.length());
		static final Buffer.Region BLANK_01                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region REVISION_LEVEL                      = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_02                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region INPUT_SOURCE_FILE_ID                = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_03                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ANNOT_CLASS                         = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_04                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_INPUT_RECORD_NUMBER           = staticRegionGenerator.newFixedLength(10).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_05                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_START_LINE_NUMBER             = staticRegionGenerator.newFixedLength(10).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_06                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_TOKEN_START_NUMBER            = staticRegionGenerator.newFixedLength(1).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_07                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_END_LINE_NUMBER               = staticRegionGenerator.newFixedLength(10).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_08                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_TOKEN_END_NUMBER              = staticRegionGenerator.newFixedLength(1).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_09                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_MESSAGE_ID_NUMBER             = staticRegionGenerator.newFixedLength(9);
		static final Buffer.Region BLANK_10                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_MESSAGE_SEVERITY_CODE         = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region BLANK_11                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_MESSAGE_SEVERITY_LEVEL_NUMBER = staticRegionGenerator.newFixedLength(2).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_12                            = staticRegionGenerator.newFixedLength(1);
		static final Buffer.Region ERROR_MESSAGE_LENGTH                = staticRegionGenerator.newFixedLength(3).setNumberFormat(Buffer.Region.NumberFormat.TEXT_DECIMAL);
		static final Buffer.Region BLANK_13                            = staticRegionGenerator.newFixedLength(1);
		
	}
	
}