package t6bygedq.lib;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author 2oLDNncs 20250405
 */
public final class XSSFWorkbookUpdater implements Closeable {
	
	private final File workbookFile;
	
	private final XSSFWorkbook workbook;
	
	private final XSSFSheet sheet;
	
	private String[] updateKey;
	
	private String[] rangeKey;
	
	private final String timestamp = new Date().toInstant().toString();
	
	private final Queue<Integer> availableRowNums = new PriorityQueue<>();
	
	public XSSFWorkbookUpdater(final File workbookFile, final String sheetName) throws InvalidFormatException, IOException {
		this.workbookFile = workbookFile;
		this.workbook = openOrCreate(workbookFile);
		
		var sheet = this.workbook.getSheet(sheetName);
		
		if (null == sheet) {
			sheet = this.workbook.createSheet(sheetName);
			System.out.println(Helpers.dformat("%s", sheet));
		}
		
		this.sheet = sheet;
	}
	
	public final String[] getUpdateKey() {
		return this.updateKey;
	}
	
	public final void setUpdateKey(final String... updateKey) {
		this.updateKey = updateKey;
	}
	
	public final String getTimestamp() {
		return this.timestamp;
	}
	
	@Override
	public final void close() throws IOException {
		save(this.workbook, this.workbookFile);
		this.workbook.close();
	}
	
	private final void setupRange() {
		if (null == this.rangeKey || !Arrays.equals(this.rangeKey, this.getUpdateKey())) {
			this.rangeKey = this.getUpdateKey().clone();
			
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
		for (var i = 0; i <= this.sheet.getLastRowNum(); i += 1) {
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
	
	public final void row(final String... rowData) {
		this.setupRange();
		
		final var row = this.sheet.createRow(this.getAvailableRowNum());
		var colNum = 0;
		colNum = addCells(row, colNum, this.getUpdateKey());
		colNum = addCells(row, colNum, this.getTimestamp());
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
	
}