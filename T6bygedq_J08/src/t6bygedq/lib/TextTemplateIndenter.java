package t6bygedq.lib;

import java.io.PrintStream;

/**
 * @author 2oLDNncs 20241225
 */
public final class TextTemplateIndenter extends TextTemplatePrinter {
	
	private String current;
	
	private String delta;
	
	public TextTemplateIndenter() {
		this(System.out);
	}
	
	public TextTemplateIndenter(final PrintStream out) {
		this(out, "", "\t");
	}
	
	public TextTemplateIndenter(final PrintStream out, final String initial, final String delta) {
		super(out);
		this.current = initial;
		this.delta = delta;
	}
	
	public final String getCurrent() {
		return this.current;
	}
	
	public final void setCurrent(final String current) {
		this.current = current;
	}
	
	public final String getDelta() {
		return this.delta;
	}
	
	public final void setDelta(final String delta) {
		this.delta = delta;
	}
	
	public final void indent(final Runnable action) {
		this.indent();
		
		try {
			action.run();
		} finally {
			this.outdent();
		}
	}
	
	public final void indent() {
		this.setCurrent(this.getCurrent() + this.getDelta());
	}
	
	public final void outdent() {
		this.setCurrent(this.getCurrent().substring(0, this.getCurrent().length() - this.getDelta().length()));
	}
	
	@Override
	protected final void beforeLine() {
		this.print(this.getCurrent());
	}
	
}