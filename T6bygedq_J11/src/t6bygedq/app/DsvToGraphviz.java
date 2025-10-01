package t6bygedq.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Consumer;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250929
 */
public class DsvToGraphviz {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_SHEET = "-Sheet";
	public static final String ARG_DELIMITER1 = "-Delimiter1";
	public static final String ARG_DELIMITER2 = "-Delimiter2";
	public static final String ARG_STRICT = "-Strict";
	public static final String ARG_LAYOUT = "-Layout";
	public static final String ARG_COMPOUND = "-Compound";
	public static final String ARG_RANKDIR = "-Rankdir";
	public static final String ARG_OUT = "-Out";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_gv_dsv.txt");
		ap.setDefault(ARG_SHEET, "");
		ap.setDefault(ARG_DELIMITER1, "\t");
		ap.setDefault(ARG_DELIMITER2, "¤");
		ap.setDefault(ARG_STRICT, true);
		ap.setDefault(ARG_LAYOUT, "dot");
		ap.setDefault(ARG_COMPOUND, true);
		ap.setDefault(ARG_RANKDIR, "TB");
		ap.setDefault(ARG_OUT, "data/test_gv_dsv.gv");
		
		try (final var out = getPrintStream(ap, ARG_OUT)) {
			final var propDelimiter = ap.getString(ARG_DELIMITER2);
			final var gvp = new GraphvizPrinter(out);
			
			gvp.begin(ap.getBoolean(ARG_STRICT), ap.getString(ARG_LAYOUT), ap.getBoolean(ARG_COMPOUND), ap.getString(ARG_RANKDIR));
			
			try {
				if (!ap.getString(ARG_SHEET).isBlank()) {
					XSSFWorkbookToGraphviz.forEachRowInWorkbookSheet(ap, ARG_IN, ARG_SHEET, row -> {
						processRow(row, propDelimiter, gvp);
					});
				} else {
					forEachRowInDsv(ap, ARG_IN, ARG_DELIMITER1, row -> {
						processRow(row, propDelimiter, gvp);
					});
				}
			} finally {
				gvp.end();
			}
		}
	}
	
	public static final void forEachRowInDsv(final ArgsParser ap, final String dsvFileKey, final String delimiterKey,
			final Consumer<String[]> action) throws IOException {
		final var delimiter = ap.getString(delimiterKey);
		
		Files.lines(ap.getPath(dsvFileKey)).forEach(line -> {
			action.accept(line.split(delimiter, -1));
		});
	}
	
	private static final void processRow(final String[] row, final String propDelimiter, final GraphvizPrinter gvp) {
		final var nodeSize = row.length / 2;
		final var isArc = !String.join("", Arrays.copyOfRange(row, nodeSize, 2 * nodeSize)).isEmpty();
		final var hasProps = 1 == (row.length & 1);
		final var arcElements = hasProps ? Arrays.copyOf(row, row.length - 1) : row;
		
		if (isArc) {
			gvp.processArc(arcElements);
		} else if (!hasProps) {
			gvp.processNodeProp(Helpers.concat(Arrays.copyOf(row, nodeSize), "label", row[0]));
		}
		
		if (hasProps) {
			Arrays.stream(row[row.length - 1].split(propDelimiter))
			.filter(prop -> !prop.isEmpty())
			.map(prop -> prop.split("=", 2))
			.filter(propEntry -> {
				if (2 == propEntry.length) {
					return true;
				}
				
				System.err.println(String.format("Error parsing row: %s", Arrays.toString(row)));
				System.err.println(String.format(" Error parsing propEntry: %s", Arrays.toString(propEntry)));
				
				return false;
			})
			.forEach(propEntry -> {
				if (isArc) {
					gvp.processArcProp(Helpers.concat(arcElements, propEntry));
				} else {
					gvp.processNodeProp(Helpers.concat(Arrays.copyOf(row, nodeSize), propEntry));
				}
			});
		}
	}
	
	public static final PrintStream getPrintStream(final ArgsParser ap, final String outKey) throws FileNotFoundException {
		if (!ap.getString(outKey).isBlank()) {
			return new PrintStream(ap.getFile(outKey));
		}
		
		return System.out;
	}
	
}
