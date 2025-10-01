package t6bygedq.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.GraphvizPrinter;

/**
 * @author 2oLDNncs 20241214
 */
public class XSSFWorkbookToGraphviz {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_ARCS = "-Arcs";
	public static final String ARG_NODE_PROPS = "-NodeProps";
	public static final String ARG_ARC_PROPS = "-ArcProps";
	public static final String ARG_CLASS_PROPS = "-ClassProps";
	public static final String ARG_RANKDIR = "-Rankdir";
	public static final String ARG_OUT = "-Out";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_gv.xlsx");
		ap.setDefault(ARG_ARCS, "Arcs");
		ap.setDefault(ARG_NODE_PROPS, "Node Props");
		ap.setDefault(ARG_ARC_PROPS, "Arc Props");
		ap.setDefault(ARG_CLASS_PROPS, "Class Props");
		ap.setDefault(ARG_RANKDIR, "TB");
		ap.setDefault(ARG_OUT, "data/test_gv.gv");
		
		try (final var workbook = tryOpenWorkbook(ap.getFile(ARG_IN), PackageAccess.READ)) {
			processWorkbook(workbook, ap);
		}
	}
	
	public static final void forEachRowInWorkbookSheet(final ArgsParser ap, final String workbookFileKey, final String sheetKey,
			final Consumer<String[]> action) throws IOException {
		try (final var workbook = tryOpenWorkbook(ap.getFile(workbookFileKey), PackageAccess.READ)) {
			processSheet(workbook, ap, sheetKey, action);
		}
	}
	
	public static final XSSFWorkbook tryOpenWorkbook(final File file, final PackageAccess packageAccess) {
		try {
			return new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
		} catch (final InvalidFormatException e) {
			e.printStackTrace();
			
			return null;
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static final void processWorkbook(final XSSFWorkbook workbook, final ArgsParser ap)
			throws FileNotFoundException {
		final PrintStream out;
		
		if (!ap.getString(ARG_OUT).isBlank()) {
			out = new PrintStream(ap.getFile(ARG_OUT));
		} else {
			out = System.out;
		}
		
		try {
			final var gvp = new GraphvizPrinter(out);
			
			gvp.begin(true, "dot", true, ap.getString(ARG_RANKDIR));
			
			processSheet(workbook, ap, ARG_ARCS, gvp::processArc);
			processSheet(workbook, ap, ARG_NODE_PROPS, gvp::processNodeProp);
			processSheet(workbook, ap, ARG_ARC_PROPS, gvp::processArcProp);
			processSheet(workbook, ap, ARG_CLASS_PROPS, gvp::processClassProp);
			
			gvp.end();
		} finally {
			if (System.out != out) {
				out.close();
			}
		}
	}
	
	public static final void processSheet(final XSSFWorkbook workbook, final ArgsParser ap, final String apKey,
			final Consumer<String[]> action) {
		final var sheetName = ap.getString(apKey);
		
		if (!sheetName.isEmpty()) {
			final var sheet = workbook.getSheet(sheetName);
			
			if (null == sheet) {
				throw new IllegalStateException(String.format("Sheet not found: %s", sheetName));
			}
			
			forEachRow(sheet, action);
		}
	}
	
	public static final void forEachRow(final XSSFSheet sheet, final Consumer<String[]> action) {
		final var rowRange = new IntRange();
		final var colRange = new IntRange();
		
		if (!sheet.getTables().isEmpty()) {
			final var table = sheet.getTables().get(0);
			rowRange.addRange(table.getStartRowIndex() + table.getHeaderRowCount(), table.getEndRowIndex());
			colRange.addRange(table.getStartColIndex(), table.getEndColIndex());
		} else {
			for (final var row : sheet) {
				final var cellIterator = row.cellIterator();
				
				rowRange.addValue(row.getRowNum());
				
				while (cellIterator.hasNext()) {
					final var cell = cellIterator.next();
					colRange.addValue(cell.getColumnIndex());
				}
			}
		}
		
		if (rowRange.isEmpty() || colRange.isEmpty()) {
			return;
		}
		
		final var lineElements = new String[colRange.getSize()];
		
		for (var i = rowRange.getMin(); i <= rowRange.getMax(); i += 1) {
			final var row = sheet.getRow(i);
			
			Arrays.fill(lineElements, "");
			
			for (var j = colRange.getMin(); j <= colRange.getMax(); j += 1) {
				lineElements[j - colRange.getMin()] = toString(row.getCell(j));
			}
			
			action.accept(lineElements);
		}
	}
	
	public static final String toString(final Cell cell) {
		if (null == cell) {
			return "";
		}
		
		if (CellType.FORMULA.equals(cell.getCellType())) {
			switch (cell.getCachedFormulaResultType()) {
			case NUMERIC:
				return String.format("%s", cell.getNumericCellValue());
			case STRING:
				return String.format("%s", cell.getStringCellValue());
			case BOOLEAN:
				return String.format("%s", cell.getBooleanCellValue());
			case ERROR:
				return String.format("%s", FormulaError.forInt(cell.getErrorCellValue()).getString());
			default:
				break;
			}
		}
		
		return cell.toString();
	}
	
	/**
	 * @author 2oLDNncs 20241215
	 */
	private static final class IntRange {
		
		private int min = Integer.MAX_VALUE;
		
		private int max = Integer.MIN_VALUE;
		
		public final void addValue(final int value) {
			if (value < this.min) {
				this.min = value;
			}
			
			if (this.max < value) {
				this.max = value;
			}
		}
		
		public final void addRange(final int min, final int max) {
			if (min <= max) {
				this.addValue(min);
				this.addValue(max);
			}
		}
		
		public final int getMin() {
			return this.min;
		}
		
		public final int getMax() {
			return this.max;
		}
		
		public final int getSize() {
			final var result = 1L + this.getMax() - this.getMin();
			
			if (result < 0L || Integer.MAX_VALUE < result) {
				throw new ArithmeticException(String.format("Invalid range: %s", this));
			}
			
			return (int) result;
		}
		
		public final boolean isEmpty() {
			return this.getMax() < this.getMin();
		}
		
		@Override
		public final String toString() {
			return "IntRange [min=" + this.min + ", max=" + this.max + "]";
		}
		
	}
	
}
