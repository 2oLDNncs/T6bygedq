package t6bygedq.lib;

import java.io.PrintStream;

import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20241228
 */
@Debug(false)
public abstract class TextTemplatePrinter {
	
	private final PrintStream out;
	
	private boolean newLine = true;
	
	private int currentLineNumber = 1;
	
	public TextTemplatePrinter(final PrintStream out) {
		this.out = out;
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
		this.out.println(x);
		this.afterPrintln();
	}
	
	protected void beforeLine() {
		//pass
	}
	
	protected void afterLine() {
		//pass
	}
	
	private final void beforePrint() {
		if (this.newLine) {
			this.newLine = false;
			this.beforeLine();
		}
	}
	
	private final void afterPrintln() {
		this.newLine = true;
		this.currentLineNumber += 1;
		this.afterLine();
	}
	
}