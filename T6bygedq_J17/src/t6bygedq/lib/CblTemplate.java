package t6bygedq.lib;

import static t6bygedq.lib.CblConstants.*;
import static t6bygedq.lib.Helpers.cast;
import static t6bygedq.lib.Helpers.getMethodName;
import static t6bygedq.lib.Helpers.replaceCharAt;

import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author 2oLDNncs 20241228
 */
public abstract class CblTemplate extends TextTemplate {
	
	protected final TextTemplateIndenter indt;
	
	protected String programId = "HELLO001";
	
	protected boolean debuggingMode = false;
	
	protected boolean hasEnvironmentDivision = false;
	
	protected boolean hasConfigurationSection = false;
	
	protected boolean hasDataDivision = false;
	
	protected boolean hasWorkingStorageSection = false;
	
	protected boolean hasLocalStorageSection = false;
	
	protected boolean hasLinkageSection = false;
	
	private final Map<String, CblXrefParser.Def> verbs = new TreeMap<>();
	private final Map<String, CblXrefParser.Def> items = new LinkedHashMap<>();
	private final Map<String, CblXrefParser.Def> procs = new LinkedHashMap<>();
	
	protected CblTemplate() {
		super();
		this.indt = cast(this.getPrinter());
		this.setup();
	}
	
	protected CblTemplate(final PrintStream out) {
		super(new TextTemplateIndenter(out, "       ", "    "));
		this.indt = cast(this.getPrinter());
		this.setup();
	}
	
	protected void setup() {
		final var declaredMethodNames = Arrays.stream(this.getClass().getDeclaredMethods())
				.map(Method::getName)
				.collect(Collectors.toSet());
		
		this.hasConfigurationSection = declaredMethodNames.contains(M_printConfigurationSection);
		this.hasEnvironmentDivision = declaredMethodNames.contains(M_printEnvirontmentDivision)
				|| this.hasConfigurationSection;
		
		this.hasWorkingStorageSection = declaredMethodNames.contains(M_printWorkingStorageSection);
		this.hasLocalStorageSection = declaredMethodNames.contains(M_printLocalStorageSection);
		this.hasLinkageSection = declaredMethodNames.contains(M_printLinkageSection);
		this.hasDataDivision = declaredMethodNames.contains(M_printDataDivision)
				|| this.hasWorkingStorageSection|| this.hasLocalStorageSection || this.hasLinkageSection;
	}
	
	protected final void setIndicator(final char indicator) {
		this.indt.setCurrent(replaceCharAt(this.indt.getCurrent(), 6, indicator));
	}
	
	protected final void setIndicator(final char indicator, final Runnable block) {
		final var saved = this.indt.getCurrent().charAt(6);
		
		this.setIndicator(indicator);
		
		try {
			block.run();
		} finally {
			this.setIndicator(saved);
		}
	}
	
	protected void indent(final Runnable block) {
		((TextTemplateIndenter) this.getPrinter()).indent(block);
	}
	
	@Method_printComment
	protected void printComment(final String line) {
		this.setIndicator(INDICATOR_COMMENT, () -> this.println(line));
	}
	
	@Method_printDebug
	protected void printDebug(final String line) {
		this.setIndicator(INDICATOR_DEBUG, () -> this.println(line));
	}
	
	@Method_printContinuation
	protected void printContinuation(final String line) {
		this.setIndicator(INDICATOR_CONTINUATION, () -> this.println(line));
	}
	
	@Override
	protected final void doRun() {
		this.printIdentificationDivision();
		
		if (this.indt.getCurrent().isEmpty()) {
			throw new IllegalStateException();
		}
		
		if (this.hasEnvironmentDivision) {
			this.printEnvironmentDivision();
		}
		
		if (this.hasDataDivision) {
			this.printDataDivision();
		}
		
		this.printProcedureDivision();
		
		this.printEndProgram();
		
		this.printNextPrograms();
	}
	
	@Method_printIdentificationDivision
	protected void printIdentificationDivision() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_IDENTIFICATION, KW_DIVISION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
		
		this.printlnf("%s. %s.", KW_PROGRAM_ID, this.programId);
		this.println("");
	}
	
	@Method_printEnvironmentDivision
	protected void printEnvironmentDivision() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_ENVIRONMENT, KW_DIVISION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
		
		if (this.hasConfigurationSection) {
			this.printConfigurationSection();
		}
	}
	
	@Method_printConfigurationSection
	protected void printConfigurationSection() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_CONFIGURATION, KW_SECTION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
		this.setIndicator(this.debuggingMode ? INDICATOR_NONE : INDICATOR_COMMENT, () -> {
			this.printlnf("%s. IBM %s %s %s.", KW_SOURCE_COMPUTER, KW_WITH, KW_DEBUGGING, KW_MODE);
		});
		this.println("");
	}
	
	@Method_printDataDivision
	protected void printDataDivision() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_DATA, KW_DIVISION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
		
		if (this.hasWorkingStorageSection) {
			this.printWorkingStorageSection();
		}
		
		if (this.hasLocalStorageSection) {
			this.printLocalStorageSection();
		}
		
		if (this.hasLinkageSection) {
			this.printLinkageSection();
		}
	}
	
	@Method_printWorkingStorageSection
	protected void printWorkingStorageSection() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_WORKING_STORAGE, KW_SECTION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
	}
	
	@Method_printLocalStorageSection
	protected void printLocalStorageSection() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_LOCAL_STORAGE, KW_SECTION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
	}
	
	@Method_printLinkageSection
	protected void printLinkageSection() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_LINKAGE, KW_SECTION);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
	}
	
	@Method_printProcedureDivision
	protected void printProcedureDivision() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("%s %s.", KW_PROCEDURE, KW_DIVISION);
		this.printFullLine(SEPARATOR_LINE);
		
		this.indent(() -> {
			this.printProcedureCode();
			this.println(".");
		});
		
		this.println("");
		
		this.printParagraphs();
		
		this.printNestedPrograms();
	}
	
	protected void printProcedureCode() {
		this.printVerb(VB_DISPLAY);
		this.println(" 'Hello World.'");
//		this.printStmt(VB_DISPLAY, "'Hello World.'");
		this.printStmt(VB_GOBACK);
	}
	
	protected void printParagraphs() {
		//pass
	}
	
	protected void printNestedPrograms() {
		//pass
	}
	
	protected void printParagraph(final String paragraphName, final Runnable block) {
		this.printFullLine(SEPARATOR_LINE);
		this.procs.put(paragraphName, new CblXrefParser.Def(
				this.getPrinter().getCurrentLineNumber(),
				CblXrefParser.PM_PROCEDURES,
				paragraphName));
		this.printlnf("%s.", paragraphName);
		this.printFullLine(SEPARATOR_LINE);
		
		this.indent(() -> {
			block.run();
			this.println(".");
		});
		
		this.println("");
	}
	
	@Method_printEndProgram
	protected void printEndProgram() {
		this.printFullLine(SEPARATOR_LINE);
		this.printlnf("END PROGRAM %s.", this.programId);
		this.printFullLine(SEPARATOR_LINE);
		this.println("");
	}
	
	protected void printNextPrograms() {
		//pass
	}
	
	@Method_printFullLine
	protected void printFullLine(final String line) {
		final var saved = this.indt.getCurrent();
		
		this.indt.setCurrent("");
		
		try {
			this.println(line);
		} finally {
			this.indt.setCurrent(saved);
		}
	}
	
	protected void printStmt(final String verb, final String... args) {
		this.addRefTo(this.getDef(this.verbs, verb), "");
		
		switch (verb) {
		case VB_PERFORM:
			if (1 == args.length) {
				this.addRefTo(this.getDef(this.procs, args[0]), CblXrefParser.U_PERFORM);
			}
			break;
		case VB_DISPLAY:
			for (final var item : args) {
				this.addRefTo(this.getDef(this.items, item), CblXrefParser.U_READ);
			}
			break;
		}
		
		// TODO separate args into multiple lines
		
		this.printlnf("%s %s", verb, String.join(" ", args));
	}
	
	protected void printVerb(final String verb) {
		this.addRefTo(this.getDef(this.verbs, verb), "");
		this.print(verb);
	}
	
	protected void printItem(final String itemName, final String usage) {
		this.addRefTo(this.getDef(this.items, itemName), usage);
		this.print(itemName);
	}
	
	private final CblXrefParser.Def getDef(final Map<String, CblXrefParser.Def> map, final String name) {
		final String type;
		
		if (this.verbs.equals(map)) {
			type = CblXrefParser.PM_VERBS;
		} else if (this.items.equals(map)) {
			type = CblXrefParser.PM_VERBS;
		} else if (this.procs.equals(map)) {
			type = CblXrefParser.PM_PROCEDURES;
		} else {
			throw new IllegalArgumentException("Unhandled map");
		}
		
		return map.computeIfAbsent(name, k -> new CblXrefParser.Def(0, type, k));
	}
	
	private final void addRefTo(final CblXrefParser.Def def, final String usage) {
		def.getRefs().add(new CblXrefParser.Ref(
				this.getPrinter().getCurrentLineNumber(),
				usage));
	}
	
	protected static final String CBL_COLS       = "----+-*--1----+----2----+----3----+----4----+----5----+----6----+----7--";
	protected static final String SEPARATOR_LINE = "      *=================================================================";
	
	
	// Indicators (character at position 7)
	
	protected static final char INDICATOR_NONE         = ' ';
	protected static final char INDICATOR_CONTINUATION = '-';
	protected static final char INDICATOR_COMMENT      = '*';
	protected static final char INDICATOR_DEBUG        = 'D';
	
	public static final String M_printFullLine = getMethodName(
			CblTemplate.class, Method_printFullLine.class);
	public static final String M_printComment = getMethodName(
			CblTemplate.class, Method_printComment.class);
	public static final String M_printDebug = getMethodName(
			CblTemplate.class, Method_printDebug.class);
	public static final String M_printContinuation = getMethodName(
			CblTemplate.class, Method_printContinuation.class);
	public static final String M_printIdentificationDivision = getMethodName(
			CblTemplate.class, Method_printIdentificationDivision.class);
	public static final String M_printEnvirontmentDivision = getMethodName(
			CblTemplate.class, Method_printEnvironmentDivision.class);
	public static final String M_printConfigurationSection = getMethodName(
			CblTemplate.class, Method_printConfigurationSection.class);
	public static final String M_printDataDivision = getMethodName(
			CblTemplate.class, Method_printDataDivision.class);
	public static final String M_printWorkingStorageSection = getMethodName(
			CblTemplate.class, Method_printWorkingStorageSection.class);
	public static final String M_printLocalStorageSection = getMethodName(
			CblTemplate.class, Method_printLocalStorageSection.class);
	public static final String M_printLinkageSection = getMethodName(
			CblTemplate.class, Method_printLinkageSection.class);
	public static final String M_printProcedureDivision = getMethodName(
			CblTemplate.class, Method_printProcedureDivision.class);
	public static final String M_printEndProgram = getMethodName(
			CblTemplate.class, Method_printEndProgram.class);
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printFullLine {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printComment {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printDebug {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printContinuation {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printIdentificationDivision {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printEnvironmentDivision {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printConfigurationSection {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printDataDivision {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printWorkingStorageSection {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printLocalStorageSection {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printLinkageSection {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printProcedureDivision {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printEndProgram {
		//pass
	}
	
}