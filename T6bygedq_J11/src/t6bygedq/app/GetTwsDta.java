package t6bygedq.app;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.XSSFWorkbookUpdater;

/**
 * @author 2oLDNncs 20250405
 */
public final class GetTwsDta {
	
	public static final String ARG_URI_FMT = "-UriFmt";
	public static final String ARG_URI_ARGS = "-UriArgs";
	public static final String ARG_DELAY_MIN = "-DelayMin";
	public static final String ARG_DELAY_MAX = "-DelayMax";
	public static final String ARG_WORKBOOK = "-Workbook";
	public static final String ARG_SHEET = "-Sheet";
	
	public static final String LOCAL_COL_SEP = "\t";
	public static final String REMOTE_COL_SEP = ";";
	public static final String REMOTE_ROW_SEP = Pattern.quote("||");
	
	static long delayMin = 0_500L;
	static long delayMax = 1_500L;
	
	public static final void main(final String... args)
			throws IOException, ParserConfigurationException, SAXException, InvalidFormatException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_URI_FMT, "data/twsdta_%s_%s.xml");
		ap.setDefault(ARG_URI_ARGS, "data/twsurlargs.txt");
		ap.setDefault(ARG_DELAY_MIN, delayMin);
		ap.setDefault(ARG_DELAY_MAX, delayMax);
		ap.setDefault(ARG_WORKBOOK, "data/twsdta.xlsx");
		ap.setDefault(ARG_SHEET, "Data");
		
		delayMin = ap.getLong(ARG_DELAY_MIN);
		delayMax = ap.getLong(ARG_DELAY_MAX);
		
		process(
				ap.getString(ARG_URI_FMT),
				Files.lines(ap.getPath(ARG_URI_ARGS)),
				ap.getFile(ARG_WORKBOOK),
				ap.getString(ARG_SHEET),
				WorkbookUpdater::newDefaultInstance);
	}
	
	/**
	 * @author 2oLDNncs 20250408
	 */
	public static abstract interface WuAction {
		
		public abstract void process(XSSFWorkbookUpdater wu);
		
	}
	
	private static final void updateWorkbook(final File workbookFile, final String sheetName,
			final WorkbookUpdater.Factory wuFactory, final Consumer<WorkbookUpdater> wuAction)
					throws InvalidFormatException, IOException {
		try (final var wu = wuFactory.newInstance(workbookFile, sheetName)) {
			wuAction.accept(wu);
		}
	}
	
	public static final void process(final String uriFmt, final Stream<String> uriArgsStream,
			final File workbookFile, final String sheetName, final WorkbookUpdater.Factory wuFactory)
					throws InvalidFormatException, IOException, ParserConfigurationException, SAXException {
		updateWorkbook(workbookFile, sheetName, wuFactory, wu -> {
			try {
				process(uriFmt, uriArgsStream, wu);
			} catch (final ParserConfigurationException | SAXException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	public static final void process(final String uriFmt, final Stream<String> uriArgsStream, final RowHandler rowHandler)
			throws ParserConfigurationException, SAXException {
		final var xmlParser = SAXParserFactory.newInstance().newSAXParser();
		final var rand = ThreadLocalRandom.current();
		
		process(uriFmt, uriArgsStream, (uriStr, uriArgs) -> {
			try {
				rowHandler.setUriArgs(uriArgs);
				
				xmlParser.parse(uriStr, rowHandler);
				
				Thread.sleep(rand.nextLong(delayMin, delayMax));
			} catch (final Exception e) {
				System.err.println(Helpers.dformat("uriStr<%s> uriArgs<%s>", uriStr, Arrays.toString(uriArgs)));
				e.printStackTrace();
			}
		});
	}
	
	public static final void process(final String uriFmt, final Stream<String> uriArgsStream,
			final BiConsumer<String, String[]> uriArgsAction) {
		uriArgsStream
		.map(line -> line.split(LOCAL_COL_SEP))
		.forEach(uriArgs -> {
			final var uriStr = String.format(uriFmt, (Object[]) uriArgs);
			
			System.out.println(Helpers.dformat("%s", uriStr));
			
			uriArgsAction.accept(uriStr, uriArgs);
		});
	}
	
	/**
	 * @author 2oLDNncs 20250405
	 */
	public static abstract class RowHandler extends DefaultHandler {
		
		private String[] uriArgs;
		private final Stack<String> path = new Stack<>();
		private final StringBuilder buffer = new StringBuilder();
		
		protected final List<String> getPath() {
			return this.path;
		}
		
		public final String[] getUriArgs() {
			return this.uriArgs;
		}
		
		public final void setUriArgs(final String[] uriArgs) {
			this.uriArgs = uriArgs;
			this.onSetUriArgs();
		}
		
		protected void onSetUriArgs() {
			//pass
		}
		
		private final void flush() {
			final var data = this.buffer.toString();
			
			if (data.contains(REMOTE_COL_SEP)) {
				final var rows = data.split(REMOTE_ROW_SEP);
				
				for (final var row : rows) {
					this.row(row.split(REMOTE_COL_SEP));
				}
			}
			
			this.buffer.setLength(0);
		}
		
		protected void row(final String[] rowData) {
			System.out.println(Helpers.dformat("%s %s	%s",
					this.getPath(),
					String.join(LOCAL_COL_SEP, this.getUriArgs()),
					String.join(LOCAL_COL_SEP, rowData)));
		}
		
		@Override
		public final void startElement(final String uri,
				final String localName, final String qName, final Attributes attributes)
				throws SAXException {
			this.flush();
			this.path.push(qName);
		}
		
		@Override
		public final void characters(final char[] ch, final int start, final int length) throws SAXException {
			this.buffer.append(ch, start, length);
		}
		
		@Override
		public final void endElement(final String uri, final String localName, final String qName) throws SAXException {
			this.flush();
			this.path.pop();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250408
	 */
	public static abstract class WorkbookUpdater extends RowHandler implements Closeable {
		
		private final XSSFWorkbookUpdater wu;
		
		public WorkbookUpdater(final File workbookFile, final String sheetName) throws InvalidFormatException, IOException {
			this.wu = new XSSFWorkbookUpdater(workbookFile, sheetName);
		}
		
		@Override
		protected final void onSetUriArgs() {
			this.wu.setUpdateKey(this.getUriArgs());
		}
		
		@Override
		protected void row(final String[] rowData) {
			super.row(rowData);
			
			this.wu.row(Helpers.concat(
					Helpers.array(Helpers.last(this.getPath())),
					rowData));
		}
		
		@Override
		public final void close() throws IOException {
			this.wu.close();
		}
		
		public static final WorkbookUpdater newDefaultInstance(final File workbookFile, final String sheetName)
				throws InvalidFormatException, IOException {
			return new WorkbookUpdater(workbookFile, sheetName) {};
		}
		
		/**
		 * @author 2oLDNncs 20250405
		 */
		public static abstract interface Factory {
			
			public abstract WorkbookUpdater newInstance(File workbookFile, String sheetName)
					throws InvalidFormatException, IOException;
			
		}
		
	}
	
}
