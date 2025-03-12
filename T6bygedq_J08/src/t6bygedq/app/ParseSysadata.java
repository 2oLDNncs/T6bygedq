package t6bygedq.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;
import t6bygedq.lib.cbl.ReadingContext;
import t6bygedq.lib.cbl.Rec;
import t6bygedq.lib.cbl.RecData_X0001_AdataIdentification;
import t6bygedq.lib.cbl.RecData_X0002_CompilationUnitDelimiter;
import t6bygedq.lib.cbl.RecData_X0039_CopyReplacing;
import t6bygedq.lib.cbl.RecData_X0020_ExternalSymbol;
import t6bygedq.lib.cbl.RecData_X0000_JobIdentification;
import t6bygedq.lib.cbl.RecData_X0060_Library;
import t6bygedq.lib.cbl.RecData_X0046_NestedProgram;
import t6bygedq.lib.cbl.RecData_X0010_Options_6_1;
import t6bygedq.lib.cbl.RecData_X0024_ParseTree;
import t6bygedq.lib.cbl.RecData_X0038_Source;
import t6bygedq.lib.cbl.RecData_X0032_SourceError;
import t6bygedq.lib.cbl.RecData_X0090_Statistics;
import t6bygedq.lib.cbl.RecData_X0042_Symbol_6_1;
import t6bygedq.lib.cbl.RecData_X0044_SymbolCrossReference;
import t6bygedq.lib.cbl.RecData_X0030_Token;
import t6bygedq.lib.cbl.RecHeader_6_1;

/**
 * @author 2oLDNncs 20250202
 */
@Debug(true)
public final class ParseSysadata {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_VERSION = "-Version";
	public static final String ARG_TEST = "-Test";
	
	public static final void main(final String... args) throws IOException {
		final ArgsParser ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_sysadata");
		ap.setDefault(ARG_VERSION, "6.1");
		ap.setDefault(ARG_TEST, true);
		
		final String cblVersion = ap.getString(ARG_VERSION);
		
		if (ap.getBoolean(ARG_TEST)) {
			test_6_1(new File("data/test_sysadata"));
		} else {
			final File file = ap.getFile(ARG_IN);
			
			switch (cblVersion) {
			case "4.2":
				read_4_2(file);
				break;
			case "6.1":
				read_6_1(file);
				break;
			default:
				throw new IllegalArgumentException(String.format("Invalid version: %s", cblVersion));
			}
		}
	}
	
	private static void read_4_2(final File file) throws IOException {
		read_x_y(file, Rec::read_4_2);
	}
	
	private static void read_6_1(final File file) throws IOException {
		read_x_y(file, Rec::read_6_1);
	}
	
	private static void read_x_y(final File file, final Reader read_x_y) throws IOException {
		try (final InputStream in = new FileInputStream(file)) {
			final ReadingContext rc = new ReadingContext(in);
			
			while (rc.isInputAvailable()) {
				rc.incrLineNumber();
				
				try {
					read_x_y.read(rc);
				} catch (final RuntimeException e) {
					System.err.println(String.format("Read error at Line %s Column %s", rc.getLineNumber(), rc.getColumnNumber()));
					throw e;
				}
			}
		}
	}
	
	public static final void test_6_1(final File file) throws IOException {
		try (final OutputStream out = new FileOutputStream(file)) {
			Helpers.dprintlnf("");
			final Rec rec = new Rec(new RecHeader_6_1());
			
			rec.getRecHeader().vLanguageCode.set(RecHeader_6_1.LanguageCode.COBOL);
			rec.getRecHeader().vLittleEndian.set(false);
			
			{
				final RecData_X0000_JobIdentification rd = rec.setAndGetRecData(RecData_X0000_JobIdentification.class);
				
				rd.vDate.set("20250208");
				rd.vTime.set("0823");
				rd.vProductNumber.set("1");
				rd.vProductVersion.set("1");
				rd.vBuildLevel.set("1");
				rd.vSystemId.set("TheSystem");
				rd.vJobName.set("TheJob");
				rd.vStepName.set("TheStep");
				rd.vProcStep.set("TheThing");
				
				{
					final RecData_X0000_JobIdentification.InputFile f = rd.addInputFile();
					
					f.vName.set("TheFileName");
				}
				
				rec.write(out);
			}
			
			{
				final RecData_X0001_AdataIdentification rd = rec.setAndGetRecData(RecData_X0001_AdataIdentification.class);
				
				rd.vTime.set(1234567890L);
				rd.vCharacterSet.set(RecData_X0001_AdataIdentification.CharacterSet.EBCDIC);
				
				rec.write(out);
			}
			
			{
				final RecData_X0002_CompilationUnitDelimiter rd = rec.setAndGetRecData(RecData_X0002_CompilationUnitDelimiter.class);
				
				rd.vType.set(RecData_X0002_CompilationUnitDelimiter.CompilationUnitDelimiterType.END);
				
				rec.write(out);
			}
			
			{
				final RecData_X0010_Options_6_1 rd = rec.setAndGetRecData(RecData_X0010_Options_6_1.class);
				
				rd.vAdata.set(true);
				rd.vAdexitName.set("TheAdexit");
				
				rec.write(out);
			}
			
			{
				final RecData_X0020_ExternalSymbol rd = rec.setAndGetRecData(RecData_X0020_ExternalSymbol.class);
				
				rd.vSectionType.set(RecData_X0020_ExternalSymbol.SectionType.EXTERNAL_REFERENCE);
				rd.vExternalName.set("TheExternalName");
				
				rec.write(out);
			}
			
			{
				final RecData_X0024_ParseTree rd = rec.setAndGetRecData(RecData_X0024_ParseTree.class);
				
				rd.vNodeNumber.set(123L);
				rd.vNodeType.set(RecData_X0024_ParseTree.NodeType.ELSE_PHRASE);
				
				rec.write(out);
			}
			
			{
				final RecData_X0024_ParseTree rd = rec.setAndGetRecData(RecData_X0024_ParseTree.class);
				
				rd.vNodeNumber.set(456L);
				rd.vNodeType.set(RecData_X0024_ParseTree.NodeType.INITIALIZE_LITERAL_NO_TOKENS);
				rd.vNodeSubtype.set(RecData_X0024_ParseTree.NodeSubtype.ALPHABETIC_13);
				
				rec.write(out);
			}
			
			{
				final RecData_X0030_Token rd = rec.setAndGetRecData(RecData_X0030_Token.class);
				
				rd.vTokenCode.set(RecData_X0030_Token.TokenCode.ACCEPT);
				rd.vTokenText.set("Accept");
				rd.vFlags.set(RecData_X0030_Token.Flags.LAST_PIECE);
				
				rec.write(out);
			}
			
			{
				final RecData_X0032_SourceError rd = rec.setAndGetRecData(RecData_X0032_SourceError.class);
				
				rec.write(out);
			}
			
			{
				final RecData_X0038_Source rd = rec.setAndGetRecData(RecData_X0038_Source.class);
				
				rec.write(out);
			}
			
			{
				final RecData_X0039_CopyReplacing rd = rec.setAndGetRecData(RecData_X0039_CopyReplacing.class);
				
				rec.write(out);
			}
			
			{
				final RecData_X0042_Symbol_6_1 rd = rec.setAndGetRecData(RecData_X0042_Symbol_6_1.class);
				
				rd.vSymbolType.set(RecData_X0042_Symbol_6_1.SymbolType.DATA_NAME);
				rd.vSymbolAttribute.set(RecData_X0042_Symbol_6_1.SymbolAttribute.NUMERIC);
//				rd.vMnemonicNameSymbolClauses.set(RecData_Symbol.MnemonicNameSymbolClauses.C01);
				
				rd.vSymbolName.set("Symbol01");
				
				rec.write(out);
			}
			
			{
				final RecData_X0044_SymbolCrossReference rd = rec.setAndGetRecData(RecData_X0044_SymbolCrossReference.class);
				
				rd.vCrossReferenceType.set(RecData_X0044_SymbolCrossReference.CrossReferenceType.SYMBOL_OR_DATA_NAME);
				rd.vSymbolName.set("TheSymbolName");
				
				try {
					rd.addStmt();
					throw new RuntimeException();
				} catch (final IllegalStateException e) {
					Helpers.ignore(e);
				}
				
				{
					final RecData_X0044_SymbolCrossReference.FlagAndStmt fs = rd.addFlagAndStmt();
					
					fs.vReferenceFlag.set(RecData_X0044_SymbolCrossReference.ReferenceFlag.MODIFICATION);
				}
				
				rec.write(out);
			}
			
			{
				final RecData_X0046_NestedProgram rd = rec.setAndGetRecData(RecData_X0046_NestedProgram.class);
				
				rd.vProgramName.set("TheNestedProgramName");
				
				rec.write(out);
			}
			
			{
				final RecData_X0060_Library rd = rec.setAndGetRecData(RecData_X0060_Library.class);
				
				{
					final RecData_X0060_Library.Member m = rd.addMember();
					
					m.vFileId.set(123);
					m.vName.set("TheMemberName");
				}
				
				rec.write(out);
			}
			
			{
				final RecData_X0090_Statistics rd = rec.setAndGetRecData(RecData_X0090_Statistics.class);
				
				rd.vSourceRecords.set(123);
				rd.vDataDivisionStatements.set(456);
				rd.vProcedureDivisionStatements.set(789);
				rd.vProgramName.set("TheProgramName");
				
				rec.write(out);
			}
		}
		
		read_6_1(file);
	}
	
	/**
	 * @author 2oLDNncs 20250223
	 */
	private static abstract interface Reader {
		
		public abstract void read(ReadingContext rc) throws IOException;
		
	}
	
}
