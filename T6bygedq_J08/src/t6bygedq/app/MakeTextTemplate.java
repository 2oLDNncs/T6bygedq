package t6bygedq.app;

import static t6bygedq.lib.Helpers.removeExt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.JavaTemplate;
import t6bygedq.lib.TextTemplate;
import t6bygedq.lib.TextTemplatePrinter;

/**
 * @author 2oLDNncs 20241228
 */
public final class MakeTextTemplate {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_OUT = "-Out";
	
	public static final void main(final String... args) throws IOException {
		final ArgsParser ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/MYPGM001.cbl");
		ap.setDefault(ARG_OUT, "");
		
		final String generatedClassName_;
		
		{
			String name = removeExt(ap.getFile(ARG_IN).getName());
			
			if (!ap.getString(ARG_OUT).trim().isEmpty()) {
				name = removeExt(ap.getFile(ARG_OUT).getName());
			}
			
			generatedClassName_ = name;
		}
		
		final Class<?> superTemplateClass = TextTemplate.class;
		final Class<?> textTemplatePrinterClass = TextTemplatePrinter.class;
		final String textTemplatePrinterClassName = textTemplatePrinterClass.getSimpleName();
		
		new JavaTemplate() {
			
			{
				this.generatedClassName = generatedClassName_;
				this.superClass = superTemplateClass;
			}
			
			@Override
			protected final void printImports() {
				super.printImports();
				this.printlnf("import %s;", textTemplatePrinterClass.getName());
			}
			
			@Override
			protected final void printConstructors() {
				this.printf("public %s()", this.generatedClassName);
				this.printBlock(() -> {
					this.println("super();");
				});
				
				this.printf("public %s(final %s printer)",
						this.generatedClassName, textTemplatePrinterClassName);
				this.printBlock(() -> {
					this.println("super(printer);");
				});
			}
			
			@Override
			protected final void printMemberMethods() {
				this.println("@Override");
				this.printf("protected final void %s()", M_doRun);
				this.printBlock(() -> {
					try {
						Files.lines(ap.getPath(ARG_IN))
						.forEach(line -> {
							this.printlnf("this.%s(\"%s\");", M_println, line.replace("\"", "\\\""));
						});
					} catch (final IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			
			@Override
			protected final void printClassMethods() {
				this.print("public static final void main(final String... args)");
				this.printBlock(() -> {
					this.printlnf("new %s().%s();", generatedClassName, M_run);
				});
			}
			
		}.run();
	}
	
}
