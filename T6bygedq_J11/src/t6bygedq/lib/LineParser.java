package t6bygedq.lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * @author 2oLDNncs 20250406
 */
public abstract class LineParser {
	
	private URI source;
	
	public final URI getSource() {
		return this.source;
	}
	
	public final void setSource(final URI source) {
		this.source = source;
	}
	
	protected void parseLine(final String line) {
		//pass
	}
	
	protected void parseEnd() {
		//pass
	}
	
	public static final void parse(final Path path, final LineParser parser) throws IOException {
		parser.setSource(path.toUri());
		
		Files.lines(path).forEach(parser::parseLine);
		
		parser.parseEnd();
	}
	
	public static final void parse(final URI source, final LineParser parser) throws IOException {
		parser.setSource(source);
		
		try (final var scanner = new Scanner(new BufferedInputStream(source.toURL().openStream()))) {
			while (scanner.hasNext()) {
				parser.parseLine(scanner.nextLine());
			}
		}
		
		parser.parseEnd();
	}
	
}