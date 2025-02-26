package t6bygedq.lib.cbl;

import static t6bygedq.lib.cbl.CblTemplate.M_printDataDivision;
import static t6bygedq.lib.cbl.CblTemplate.M_printEndProgram;
import static t6bygedq.lib.cbl.CblTemplate.M_printEnvirontmentDivision;
import static t6bygedq.lib.cbl.CblTemplate.M_printFullLine;
import static t6bygedq.lib.cbl.CblTemplate.M_printIdentificationDivision;
import static t6bygedq.lib.cbl.CblTemplate.M_printProcedureDivision;

import java.io.PrintStream;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import t6bygedq.lib.JavaTemplate;

/**
 * @author 2oLDNncs 20241230
 */
public final class CblTemplateMaker extends JavaTemplate {
	
	private final ListIterator<String> inputLines;
	
	private String currentMethod = M_printIdentificationDivision;
	
	private int subpgmNum = 0;
	
	public CblTemplateMaker(final String generatedClassName,
			final ListIterator<String> inputLines) {
		this.inputLines = inputLines;
		this.generatedClassName = generatedClassName;
		this.superClass = CblTemplate.class;
	}
	
	@Override
	protected final void printImports() {
		if (this.isRoot()) {
			super.printImports();
			this.printlnf("import %s;", PrintStream.class.getName());
		}
	}
	
	@Override
	protected final void printConstructors() {
		this.printf("public %s()", this.generatedClassName);
		this.printBlock(() -> {
			this.println("super();");
		});
		
		this.printf("public %s(final %s out)",
				this.generatedClassName, PrintStream.class.getSimpleName());
		this.printBlock(() -> {
			this.println("super(out);");
		});
	}
	
	@Override
	protected final void printMemberMethods() {
		this.println("@Override");
		this.printlnf("protected final void %s() {", this.currentMethod);
		this.indent();
		
		while (this.inputLines.hasNext()) {
			final String line = this.inputLines.next();
			Matcher m = P_IDENTIFICATION_DIVISION.matcher(line);
			
			if (m.matches()) {
				if (!M_printIdentificationDivision.equals(this.currentMethod)) {
					this.subpgmNum += 1;
					final String subpgmClassName = String.format("%s_%s", this.generatedClassName, this.subpgmNum);
					
					this.inputLines.previous();
					final CblTemplateMaker subpgmMaker = new CblTemplateMaker(subpgmClassName, this.inputLines);
					
					subpgmMaker.visibility = Visibility.PACKAGE;
					subpgmMaker.run();
					
					this.printlnf("new %s().run();", subpgmMaker.generatedClassName);
					
					continue;
				}
				
				this.println("//IDENTIFICATION DIVISION");
			} else {
				m = null;
			}
			
			if (null == m) {
				m = P_PROGRAM_ID.matcher(line);
				
				if (m.matches()) {
					this.printlnf("//PROGRAM-ID %s", m.group(1));
					this.printlnf("this.programId = \"%s\";", m.group(1));
				} else {
					m = null;
				}
			}
			
			if (null == m) {
				m = P_ENVIRONMENT_DIVISION.matcher(line);
				
				if (m.matches()) {
					this.betweenMethods(M_printEnvirontmentDivision);
				} else {
					m = null;
				}
			}
			
			if (null == m) {
				m = P_DATA_DIVISION.matcher(line);
				
				if (m.matches()) {
					this.betweenMethods(M_printDataDivision);
				} else {
					m = null;
				}
			}
			
			if (null == m) {
				m = P_PROCEDURE_DIVISION.matcher(line);
				
				if (m.matches()) {
					this.betweenMethods(M_printProcedureDivision);
				} else {
					m = null;
				}
			}
			
			if (null == m) {
				m = P_END_PROGRAM.matcher(line);
				
				if (m.matches()) {
					this.betweenMethods(M_printEndProgram);
					this.println("//END PROGRAM " + m.group(1));
				} else {
					m = null;
				}
			}
			
			this.printlnf("this.%s(\"%s\");", M_printFullLine, line.replace("\"", "\\\""));
			
			if (M_printEndProgram.equals(this.currentMethod) && !this.isRoot()) {
				break;
			}
		}
		
		this.outdent();
		this.println("}");
		this.println("");
	}
	
	private final void betweenMethods(final String nextMethod) {
		this.outdent();
		this.println("}");
		this.println("");
		this.currentMethod = nextMethod;
		this.println("@Override");
		this.printlnf("protected final void %s() {", this.currentMethod);
		this.indent();
	}
	
	@Override
	protected final void printClassMethods() {
		if (this.isRoot()) {
			this.print("public static final void main(final String... args)");
			this.printBlock(() -> {
				this.printlnf("new %s(System.out).%s();", generatedClassName, M_run);
			});
		}
	}
	
	private static final String REGEX_NORMAL_LINE_PREFIX = "^.{6} +";
	
	private static final Pattern P_IDENTIFICATION_DIVISION = Pattern.compile(REGEX_NORMAL_LINE_PREFIX +
			"IDENTIFICATION +DIVISION.*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PROGRAM_ID = Pattern.compile(REGEX_NORMAL_LINE_PREFIX +
			"PROGRAM-ID *\\. *([^ .]+).*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern P_ENVIRONMENT_DIVISION = Pattern.compile(REGEX_NORMAL_LINE_PREFIX +
			"ENVIRONMENT +DIVISION.*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern P_DATA_DIVISION = Pattern.compile(REGEX_NORMAL_LINE_PREFIX +
			"DATA +DIVISION.*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PROCEDURE_DIVISION = Pattern.compile(REGEX_NORMAL_LINE_PREFIX +
			"PROCEDURE +DIVISION.*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern P_END_PROGRAM = Pattern.compile(REGEX_NORMAL_LINE_PREFIX +
			"END +PROGRAM +([^ .]+).*$", Pattern.CASE_INSENSITIVE);
	
}
