package t6bygedq.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250929
 */
public class DsvToGraphviz {
	
	public static final String ARG_IN = "-In";
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
		ap.setDefault(ARG_DELIMITER1, "\t");
		ap.setDefault(ARG_DELIMITER2, "¤");
		ap.setDefault(ARG_STRICT, true);
		ap.setDefault(ARG_LAYOUT, "dot");
		ap.setDefault(ARG_COMPOUND, true);
		ap.setDefault(ARG_RANKDIR, "TB");
		ap.setDefault(ARG_OUT, "data/test_gv_dsv.gv");
		
		try (final var out = getPrintStream(ap, ARG_OUT)) {
			final var delimiter = ap.getString(ARG_DELIMITER1);
			final var gvp = new GraphvizPrinter(out);
			
			gvp.begin(ap.getBoolean(ARG_STRICT), ap.getString(ARG_LAYOUT), ap.getBoolean(ARG_COMPOUND), ap.getString(ARG_RANKDIR));
			
			try {
				Files.lines(ap.getPath(ARG_IN)).forEach(line -> {
					final var elements = line.split(delimiter, -1);
					final var isArc = !String.join("", Arrays.copyOfRange(elements, elements.length / 2, 2 * (elements.length / 2))).isEmpty();
					final var hasProps = 1 == (elements.length & 1);
					final var arcElements = hasProps ? Arrays.copyOf(elements, elements.length - 1) : elements;
					
					if (isArc) {
						gvp.processArc(arcElements);
					} else if (!hasProps) {
						gvp.processNodeProp(Helpers.concat(Arrays.copyOf(elements, elements.length / 2), "label", elements[0]));
					}
					
					if (hasProps) {
						Arrays.stream(elements[elements.length - 1].split(ap.getString(ARG_DELIMITER2)))
						.map(prop -> prop.split("=", 2))
						.forEach(propEntry -> {
							if (isArc) {
								gvp.processArcProp(Helpers.concat(arcElements, propEntry));
							} else {
								gvp.processNodeProp(Helpers.concat(Arrays.copyOf(elements, elements.length / 2), propEntry));
							}
						});
					}
				});
			} finally {
				gvp.end();
			}
		}
	}
	
	private static final PrintStream getPrintStream(final ArgsParser ap, final String apKey) throws FileNotFoundException {
		if (!ap.getString(apKey).isBlank()) {
			return new PrintStream(ap.getFile(apKey));
		}
		
		return System.out;
	}
	
}
