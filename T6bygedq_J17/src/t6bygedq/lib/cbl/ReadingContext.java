package t6bygedq.lib.cbl;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 2oLDNncs 20250202
 */
public final class ReadingContext {
	
	final InputStream input;
	
	int lineNumber;
	
	int columnNumber;
	
	public ReadingContext(final InputStream input) {
		this.input = input;
	}
	
	public final boolean isInputAvailable() throws IOException {
		return 0 < this.input.available();
	}
	
	public final int getLineNumber() {
		return this.lineNumber;
	}
	
	public final int getColumnNumber() {
		return this.columnNumber;
	}
	
	public final void incrLineNumber() {
		this.lineNumber += 1;
	}
	
	public final void incrColumnNumber() {
		this.columnNumber += 1;
	}
	
	public final void read(final byte[] bytes) throws IOException {
		final var n = this.input.read(bytes);
		
		if (bytes.length != n) {
			throw new IllegalStateException(String.format("Read error at (%s:%s): Expected %s bytes, Actual %s bytes",
					this.lineNumber, this.columnNumber, bytes.length, n));
		}
	}
	
}