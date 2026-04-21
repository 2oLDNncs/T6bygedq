package t6bygedq.lib;

import java.io.PrintStream;
import java.util.Objects;

import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20241228
 */
@Debug(false)
public abstract class TextTemplatePrinter {
	
	private final PrintStream out;
	
	private String lineSeparator = System.lineSeparator();
	
	private boolean linePrinted = true;
	
	private int currentLineNumber = 1;
	
	public TextTemplatePrinter(final PrintStream out) {
		this.out = out;
	}
	
	public final String getLineSeparator() {
		return this.lineSeparator;
	}
	
	public final void setLineSeparator(final String lineSeparator) {
		this.lineSeparator = Objects.requireNonNull(lineSeparator);
	}
	
	public final int getCurrentLineNumber() {
		return this.currentLineNumber;
	}
	
	public final void print(final Object x) {
		this.beforePrint();
		this.out.print(x);
	}
	
	public final void println(final Object x) {
		this.beforePrint();
		this.out.print(x);
		this.out.print(this.getLineSeparator());
		this.afterPrintln();
	}
	
	protected void beforeLine() {
		//pass
	}
	
	protected void afterLine() {
		//pass
	}
	
	private final void beforePrint() {
		if (this.linePrinted) {
			this.linePrinted = false;
			this.beforeLine();
		}
	}
	
	private final void afterPrintln() {
		this.linePrinted = true;
		this.currentLineNumber += 1;
		this.afterLine();
	}
	
}