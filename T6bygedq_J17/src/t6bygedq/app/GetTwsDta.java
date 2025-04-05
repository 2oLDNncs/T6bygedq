package t6bygedq.app;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250405
 */
public final class GetTwsDta {
	
	public static final String ARG_URI_FMT = "-UriFmt";
	public static final String ARG_URI_ARGS = "-UriArgs";
	public static final String ARG_WORKBOOK = "-Workbook";
	public static final String ARG_SHEET = "-Sheet";
	
	public static final String LOCAL_COL_SEP = "\t";
	public static final String REMOTE_COL_SEP = ";";
	public static final String REMOTE_ROW_SEP = Pattern.quote("||");
	
	public static final void main(final String... args)
			throws IOException, ParserConfigurationException, SAXException, InvalidFormatException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_URI_FMT, "data/twsdta_%s_%s.xml");
		ap.setDefault(ARG_URI_ARGS, "data/twsurlargs.txt");
		ap.setDefault(ARG_WORKBOOK, "data/twsdta.xlsx");
		ap.setDefault(ARG_SHEET, "Data");
		
		process(
				ap.getString(ARG_URI_FMT),
				Files.lines(ap.getPath(ARG_URI_ARGS)),
				ap.getFile(ARG_WORKBOOK),
				ap.getString(ARG_SHEET),
				XSSFWorkbookUpdater::newDefaultInstance);
	}
	
	public static final void process(final String uriFmt, final Stream<String> uriArgsStream,
			final File workbookFile, final String sheetName, final XSSFWorkbookUpdater.Factory wuFactory)
					throws InvalidFormatException, IOException, ParserConfigurationException, SAXException {
		try (final var wu = wuFactory.newInstance(workbookFile, sheetName)) {
			process(uriFmt, uriArgsStream, wu);
		}
	}
	
	public static final void process(final String uriFmt, final Stream<String> uriArgsStream, final RowHandler rowHandler)
			throws ParserConfigurationException, SAXException {
		final var xmlParser = SAXParserFactory.newInstance().newSAXParser();
		final var rand = new Random();
		
		uriArgsStream
		.map(line -> line.split(LOCAL_COL_SEP))
		.forEach(uriArgs -> {
			try {
				final var uriStr = String.format(uriFmt, (Object[]) uriArgs);
				
				System.out.println(Helpers.dformat("%s", uriStr));
				
				rowHandler.setUriArgs(uriArgs);
				xmlParser.parse(uriStr, rowHandler);
				
				Thread.sleep(rand.nextLong(1_000L, 2_000L));
			} catch (final IOException | SAXException | InterruptedException e) {
				e.printStackTrace();
			}
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
	 * @author 2oLDNncs 20250405
	 */
	public static abstract class XSSFWorkbookUpdater extends RowHandler implements Closeable {
		
		private final File workbookFile;
		
		private final XSSFWorkbook workbook;
		
		private final XSSFSheet sheet;
		
		private String[] rangeKey;
		
		private final String timestamp = new Date().toInstant().toString();
		
		private final Queue<Integer> availableRowNums = new PriorityQueue<>();
		
		protected XSSFWorkbookUpdater(final File workbookFile, final String sheetName) throws InvalidFormatException, IOException {
			this.workbookFile = workbookFile;
			this.workbook = openOrCreate(workbookFile);
			
			var sheet = this.workbook.getSheet(sheetName);
			
			if (null == sheet) {
				sheet = this.workbook.createSheet(sheetName);
				System.out.println(Helpers.dformat("%s", sheet));
			}
			
			this.sheet = sheet;
		}
		
		@Override
		public final void close() throws IOException {
			save(this.workbook, this.workbookFile);
			this.workbook.close();
		}
		
		private final void setupRange() {
			if (null == this.rangeKey || !Arrays.equals(this.rangeKey, this.getUriArgs())) {
				this.rangeKey = this.getUriArgs().clone();
				
				final var toRemove = new ArrayList<Row>();
				
				for (final var row : this.sheet) {
					if (startsWith(row, this.rangeKey)) {
						toRemove.add(row);
					}
				}
				
				toRemove.forEach(this.sheet::removeRow);
				
				this.packRows();
			}
		}
		
		private final void packRows() {
			for (var i = 0; i < this.sheet.getLastRowNum(); i += 1) {
				final var row = this.sheet.getRow(i);
				
				if (null == row) {
					this.availableRowNums.add(i);
				} else if (!this.availableRowNums.isEmpty()) {
					final var availableRowNum = this.availableRowNums.poll();
					final var rowNum = row.getRowNum();
					
					this.sheet.copyRows(rowNum, rowNum, availableRowNum, new CellCopyPolicy());
					this.sheet.removeRow(row);
					
					this.availableRowNums.add(rowNum);
				}
			}
		}
		
		private final int getAvailableRowNum() {
			if (this.availableRowNums.isEmpty()) {
				return this.sheet.getLastRowNum() + 1;
			}
			
			return this.availableRowNums.poll();
		}
		
		@Override
		protected void row(final String[] rowData) {
			super.row(rowData);
			
			this.setupRange();
			
			final var row = this.sheet.createRow(this.getAvailableRowNum());
			var colNum = 0;
			colNum = addCells(row, colNum, this.rangeKey);
			colNum = addCells(row, colNum,
					this.timestamp,
					Helpers.last(this.getPath()));
			colNum = addCells(row, colNum, rowData);
		}
		
		public static final boolean startsWith(final Row row, final String[] values) {
			for (var i = 0; i < values.length; i += 1) {
				final var cell = row.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);
				
				if (!values[i].equals(cell.getStringCellValue())) {
					return false;
				}
			}
			
			return true;
		}
		
		public static final int addCells(final Row row, final int availableColNum, final String... values) {
			var colNum = availableColNum;
			
			for (var i = 0; i < values.length; i += 1) {
				final var cell = row.createCell(colNum++, CellType.STRING);
				
				cell.setCellValue(values[i]);
			}
			
			return colNum;
		}
		
		public static final XSSFWorkbook openOrCreate(final File file) throws IOException {
			if (!file.exists()) {
				try (final var w = new XSSFWorkbook()) {
					save(w, file);
				}
			}
			
			try (final var fis = new FileInputStream(file)) {
				return new XSSFWorkbook(fis, false);
			}
		}
		
		public static final void save(final XSSFWorkbook workbook, final File file) throws IOException {
			try (final var fos = new FileOutputStream(file)) {
				workbook.write(fos);
			}
		}
		
		public static final XSSFWorkbookUpdater newDefaultInstance(final File workbookFile, final String sheetName)
				throws InvalidFormatException, IOException {
			return new XSSFWorkbookUpdater(workbookFile, sheetName) {};
		}
		
		/**
		 * @author 2oLDNncs 20250405
		 */
		public static abstract interface Factory {
			
			public abstract XSSFWorkbookUpdater newInstance(File workbookFile, String sheetName)
					throws InvalidFormatException, IOException;
			
		}
		
	}
	
}
