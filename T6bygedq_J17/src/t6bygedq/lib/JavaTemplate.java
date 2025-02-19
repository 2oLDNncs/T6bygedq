package t6bygedq.lib;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author 2oLDNncs 20241229
 */
public abstract class JavaTemplate extends TextTemplate {
	
	protected String author = this.getClass().getName();
	
	protected String generatedClassName = "MyClass";
	
	protected Visibility visibility = Visibility.PUBLIC;
	
	protected Class<?> superClass = null;
	
	public JavaTemplate() {
		super();
	}
	
	public JavaTemplate(final TextTemplatePrinter printer) {
		super(printer);
	}
	
	@Override
	protected void doRun() {
		this.println("");
		this.printImports();
		this.println("");
		
		this.println("/**");
		this.printlnf(" * @author %s %s",
				this.author,
				DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()));
		this.println(" */");
		
		this.printf("%s final class %s", this.visibility.getString(), this.generatedClassName);
		
		if (null != this.superClass) {
			this.printf(" extends %s", this.superClass.getSimpleName());
		}
		
		this.printBlock(this::printClassContents);
	}
	
	protected void indent(final Runnable block) {
		((TextTemplateIndenter) this.getPrinter()).indent(block);
	}
	
	protected void indent() {
		((TextTemplateIndenter) this.getPrinter()).indent();
	}
	
	protected void outdent() {
		((TextTemplateIndenter) this.getPrinter()).outdent();
	}
	
	protected void printBlock(final Runnable block) {
		this.println(" {");
		this.indent(block);
		this.println("}");
		this.println("");
	}
	
	protected void printImports() {
		if (null != this.superClass) {
			this.printlnf("import %s;", this.superClass.getName());
		}
	}
	
	protected void printClassContents() {
		this.println("");
		this.printMemberAttributes();
		this.printConstructors();
		this.printMemberMethods();
		this.printClassAttributes();
		this.printClassMethods();
	}
	
	protected void printMemberAttributes() {
		//pass
	}
	
	protected void printConstructors() {
		//pass
	}
	
	protected void printMemberMethods() {
		//pass
	}
	
	protected void printClassAttributes() {
		//pass
	}
	
	protected void printClassMethods() {
		//pass
	}
	
	/**
	 * @author 2oLDNncs 20241231
	 */
	public static enum Visibility {
		
		PUBLIC, PACKAGE(""), PROTECTED, PRIVATE;
		
		private final String string;
		
		private Visibility() {
			this(null);
		}
		
		private Visibility(final String string) {
			this.string = null == string ? (this.toString().toLowerCase() + " ") : string;
		}
		
		public final String getString() {
			return this.string;
		}
		
	}
	
}