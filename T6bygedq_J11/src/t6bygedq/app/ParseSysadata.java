package t6bygedq.app;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;
import t6bygedq.lib.cbl.ReadingContext;
import t6bygedq.lib.cbl.Rec;
import t6bygedq.lib.cbl.RecData_X0000_JobIdentification;
import t6bygedq.lib.cbl.RecData_X0001_AdataIdentification;
import t6bygedq.lib.cbl.RecData_X0002_CompilationUnitDelimiter;
import t6bygedq.lib.cbl.RecData_X0010_Options_6_1;
import t6bygedq.lib.cbl.RecData_X0020_ExternalSymbol;
import t6bygedq.lib.cbl.RecData_X0024_ParseTree;
import t6bygedq.lib.cbl.RecData_X0030_Token;
import t6bygedq.lib.cbl.RecData_X0032_SourceError;
import t6bygedq.lib.cbl.RecData_X0038_Source;
import t6bygedq.lib.cbl.RecData_X0039_CopyReplacing;
import t6bygedq.lib.cbl.RecData_X0042_Symbol_6_1;
import t6bygedq.lib.cbl.RecData_X0044_SymbolCrossReference;
import t6bygedq.lib.cbl.RecData_X0046_NestedProgram;
import t6bygedq.lib.cbl.RecData_X0060_Library;
import t6bygedq.lib.cbl.RecData_X0090_Statistics;
import t6bygedq.lib.cbl.RecData_X0120_Events;
import t6bygedq.lib.cbl.RecHeader_6_1;

/**
 * @author 2oLDNncs 20250202
 */
@Debug(false)
public final class ParseSysadata {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_VERSION = "-Version";
	public static final String ARG_TEST = "-Test";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_sysadata");
		ap.setDefault(ARG_VERSION, "6.1");
		ap.setDefault(ARG_TEST, true);
		
		final var cblVersion = ap.getString(ARG_VERSION);
		
		if (ap.getBoolean(ARG_TEST)) {
			test_6_1(new File("data/test_sysadata"));
		} else {
			final var file = ap.getFile(ARG_IN);
			
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
	
	private static void read_x_y(final File file, final RecReader read_x_y) throws IOException {
		try (final var prr = new ProgressiveRecReader(file, read_x_y)) {
			while (prr.hasNext()) {
				Helpers.dprintlnf("%s%%", prr.getProgress(10L));
				prr.next();
			}
			
			Helpers.dprintlnf("%s%%", prr.getProgress(10L));
		}
	}
	
	public static final void test_6_1(final File file) throws IOException {
		try (final var out = new FileOutputStream(file)) {
			Helpers.dprintlnf("");
			final var rec = new Rec(new RecHeader_6_1());
			
			rec.getRecHeader().vLanguageCode.set(RecHeader_6_1.LanguageCode.COBOL);
			rec.getRecHeader().vLittleEndian.set(false);
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0000_JobIdentification.class);
				
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
					final var f = rd.addInputFile();
					
					f.vName.set("TheFileName");
				}
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0001_AdataIdentification.class);
				
				rd.vTime.set(1234567890L);
				rd.vCharacterSet.set(RecData_X0001_AdataIdentification.CharacterSet.EBCDIC);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0002_CompilationUnitDelimiter.class);
				
				rd.vType.set(RecData_X0002_CompilationUnitDelimiter.CompilationUnitDelimiterType.END);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0010_Options_6_1.class);
				
				rd.vAdata.set(true);
				rd.vAdexitName.set("TheAdexit");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0020_ExternalSymbol.class);
				
				rd.vSectionType.set(RecData_X0020_ExternalSymbol.SectionType.EXTERNAL_REFERENCE);
				rd.vExternalName.set("TheExternalName");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0024_ParseTree.class);
				
				rd.vNodeNumber.set(123L);
				rd.vNodeType.set(RecData_X0024_ParseTree.NodeType.ELSE_PHRASE);
				
				rec.write(out);
			}
			
			final var n = 1_000L;
			final var rand = new Random(n);
			final var tree = new LinkedHashMap<Long, List<List<Long>>>();
			
			for (var i = 1L; i <= n; i += 1L) {
				final var parentNodeNumber = rand.nextLong() % i;
				final var siblings = tree.computeIfAbsent(parentNodeNumber, __ -> new ArrayList<>());
				
				if (siblings.isEmpty()) {
					siblings.add(Arrays.asList(0L, i));
				} else {
					siblings.add(Arrays.asList(siblings.get(siblings.size() - 1).get(1), i));
				}
			}
			
			final var parents = new ArrayList<>(tree.keySet());
			
			Collections.shuffle(parents, rand);
			
			parents.forEach(parentNodeNumber -> {
				final var children = tree.get(parentNodeNumber);
				
				Collections.shuffle(children, rand);
				System.out.println(String.format("%s%s", parentNodeNumber, children));
				
				children.forEach(nodeInfo -> {
					final var leftSiblingNodeNumber = nodeInfo.get(0);
					final var nodeNumber = nodeInfo.get(1);
					final var rd = rec.setAndGetRecData(RecData_X0024_ParseTree.class);
					
					rd.vParentNodeNumber.set(parentNodeNumber);
					rd.vLeftSiblingNodeNumber.set(leftSiblingNodeNumber);
					rd.vNodeNumber.set(nodeNumber);
					rd.vSymbolId.set(nodeNumber);
					rd.vNodeType.set(RecData_X0024_ParseTree.NodeType.INITIALIZE_LITERAL_NO_TOKENS);
					rd.vNodeSubtype.set(RecData_X0024_ParseTree.NodeSubtype.ALPHABETIC_13);
					
					try {
						rec.write(out);
					} catch (final IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			});
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0030_Token.class);
				
				rd.vTokenCode.set(RecData_X0030_Token.TokenCode.ACCEPT);
				rd.vTokenText.set("Accept");
				rd.vFlags.set(RecData_X0030_Token.Flags.LAST_PIECE);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0032_SourceError.class);
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0038_Source.class);
				
				rd.vSourceRecord.set("      * TEST");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0039_CopyReplacing.class);
				
				rec.write(out);
			}
			
			for (var i = 1; i < n; i += 1L) {
				final var rd = rec.setAndGetRecData(RecData_X0042_Symbol_6_1.class);
				
				rd.vSymbolId.set(i);
				rd.vSymbolType.set(RecData_X0042_Symbol_6_1.SymbolType.DATA_NAME);
				rd.vSymbolAttribute.set(RecData_X0042_Symbol_6_1.SymbolAttribute.NUMERIC);
//				rd.vMnemonicNameSymbolClauses.set(RecData_Symbol.MnemonicNameSymbolClauses.C01);
				
				rd.vSymbolName.set("Symbol01");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0044_SymbolCrossReference.class);
				
				rd.vCrossReferenceType.set(RecData_X0044_SymbolCrossReference.CrossReferenceType.SYMBOL_OR_DATA_NAME);
				rd.vSymbolName.set("TheSymbolName");
				
				try {
					rd.addStmt();
					throw new RuntimeException();
				} catch (final IllegalStateException e) {
					Helpers.ignore(e);
				}
				
				{
					final var fs = rd.addFlagAndStmt();
					
					fs.vReferenceFlag.set(RecData_X0044_SymbolCrossReference.ReferenceFlag.MODIFICATION);
				}
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0046_NestedProgram.class);
				
				rd.vProgramName.set("TheNestedProgramName");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0060_Library.class);
				
				{
					final var m = rd.addMember();
					
					m.vFileId.set(123);
					m.vName.set("TheMemberName");
				}
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0090_Statistics.class);
				
				rd.vSourceRecords.set(123);
				rd.vDataDivisionStatements.set(456);
				rd.vProcedureDivisionStatements.set(789);
				rd.vProgramName.set("TheProgramName");
				
				rec.write(out);
			}
			
			{
				final var rd = rec.setAndGetRecData(RecData_X0120_Events.class);
				
				rd.setEventTimestamp();
				
				rec.write(out);
			}
		}
		
		read_6_1(file);
	}
	
	/**
	 * @author 2oLDNncs 20250223
	 */
	public static abstract interface RecReader {
		
		public abstract Rec read(ReadingContext rc) throws IOException;
		
	}
	
	/**
	 * @author 2oLDNncs 20250315
	 */
	public static final class ProgressiveRecReader implements Closeable, Iterator<Rec> {
		
		private final long totalBytes;
		
		private final InputStream in;
		
		private final ReadingContext rc;
		
		private final RecReader recReader;
		
		public ProgressiveRecReader(final File file, final RecReader recReader) throws FileNotFoundException {
			this.totalBytes = file.length();
			this.in = new FileInputStream(file);
			this.rc = new ReadingContext(in);
			this.recReader = recReader;
		}
		
		public final long getTotalBytes() {
			return this.totalBytes;
		}
		
		public final long getBytesRead() {
			return this.rc.getTotalBytesRead();
		}
		
		public final double getProgress(final long p) {
			return 100L * p * this.getBytesRead() / this.getTotalBytes() / (double) p;
		}
		
		@Override
		public final boolean hasNext() {
			try {
				return this.rc.isInputAvailable();
			} catch (final IOException e) {
				throw this.newException(e);
			}
		}
		
		@Override
		public final Rec next() {
			try {
				return this.recReader.read(this.rc);
			} catch (final Exception e) {
				throw this.newException(e);
			}
		}
		
		@Override
		public void close() throws IOException {
			this.in.close();
		}
		
		private final RuntimeException newException(final Exception cause) {
			return new RuntimeException(
					String.format("Read error at Line %s Column %s", rc.getLineNumber(), rc.getColumnNumber()),
					cause);
		}
		
	}
	
}
