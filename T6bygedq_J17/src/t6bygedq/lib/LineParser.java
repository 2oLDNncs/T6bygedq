package t6bygedq.lib;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * @author 2oLDNncs 20250406
 */
public abstract class LineParser {
	
	private final Location currentLocation = new Location();
	
	public final URI getSource() {
		return this.currentLocation.getSource();
	}
	
	public final void setSource(final URI source) {
		this.currentLocation.source = source;
		this.currentLocation.lineNumber = 0;
	}
	
	protected final String getCurrentLine() {
		return this.currentLocation.getLine();
	}
	
	protected final int getCurrentLineNumber() {
		return this.currentLocation.getLineNumber();
	}
	
	protected final Location cloneCurrentLocation() {
		return this.currentLocation.clone();
	}
	
	protected final String errorMessage(final String prefixFormat, final Object... prefixArgs) {
		return this.currentLocation.errorMessage(prefixFormat, prefixArgs);
	}
	
	protected void parseLine(final String line) {
		this.currentLocation.lineNumber += 1;
		this.currentLocation.line = line;
	}
	
	protected void parseEnd() {
		//pass
	}
	
	public static final void parse(final Path path, final Charset charset, final LineParser parser) throws IOException {
		parser.setSource(path.toUri());
		
		Files.lines(path, charset).forEach(parser::parseLine);
		
		parser.parseEnd();
	}
	
	public static final void parse(final URI source, final Charset charset, final LineParser parser) throws IOException {
		parser.setSource(source);
		
		try (final var scanner = new Scanner(source.toURL().openStream(), charset)) {
			while (scanner.hasNext()) {
				parser.parseLine(scanner.nextLine());
			}
		}
		
		parser.parseEnd();
	}
	
	/**
	 * @author 2oLDNncs 20250407
	 */
	public static final class Location implements Cloneable {
		
		private URI source;
		
		private int lineNumber;
		
		private String line;
		
		public final URI getSource() {
			return this.source;
		}
		
		public final int getLineNumber() {
			return this.lineNumber;
		}
		
		public final String getLine() {
			return this.line;
		}
		
		@Override
		public final Location clone() {
			try {
				return Helpers.cast(super.clone());
			} catch (final CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
		
		public final String errorMessage(final String prefixFormat, final Object... prefixArgs) {
			return String.format("%s at (%s:%s) <%s>",
					String.format(prefixFormat, prefixArgs),
					this.getSource(), this.getLineNumber(), this.getLine());
		}
		
	}
		
}