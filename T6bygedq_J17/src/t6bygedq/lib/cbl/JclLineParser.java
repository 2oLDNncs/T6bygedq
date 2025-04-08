package t6bygedq.lib.cbl;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import t6bygedq.lib.Helpers;
import t6bygedq.lib.LineParser;
import t6bygedq.lib.Rgx;
import t6bygedq.lib.cbl.CblXrefParser.LineMatcher;

/**
 * @author 2oLDNncs 20250406
 */
public abstract class JclLineParser extends LineParser {
	
	private final LineMatcher stmtMatcher = new LineMatcher(P_STMT);
	private final LineMatcher inputEndMatcher = new LineMatcher(P_INPUT_END);
	private final LineMatcher commentMatcher = new LineMatcher(P_COMMENT);
	
	@Override
	protected void parseLine(final String line) {
		super.parseLine(line);
		
		Helpers.dprintlnf("%s", line);
		
		if (this.commentMatcher.matches(line)) {
			this.parseJclLineComment(this.commentMatcher.group(G_COMMENT));
		} else if (this.stmtMatcher.matches(line)) {
			final var name = this.stmtMatcher.group(G_NAME);
			final var type = Objects.requireNonNullElse(this.stmtMatcher.group(G_STMT_TYPE), "").toUpperCase(Locale.ENGLISH);
			final var parms = Objects.requireNonNullElse(this.stmtMatcher.group(G_PARMS), "");
			
			this.parseJclLineStmt(name, type, parms);
		} else if (this.inputEndMatcher.matches(line)) {
			this.parseJclLineInputEnd();
		} else {
			this.parseJclLineInput(line);
		}
	}
	
	protected void parseJclLineComment(final String comment) {
		Helpers.dprintlnf(">%s", comment);
	}
	
	protected void parseJclLineStmt(final String name, final String type, final String parms) {
		Helpers.dprintlnf(">%s %s %s", name, type, parms);
	}
	
	protected void parseJclLineInput(final String line) {
		Helpers.dprintlnf(">%s", line);
	}
	
	protected void parseJclLineInputEnd() {
		Helpers.dprintlnf(">");
	}
	
	private static final String G_COMMENT = "Comment";
	private static final String G_NAME = "Name";
	private static final String G_STMT_TYPE = "StmtType";
	private static final String G_PARMS = "Parms";
	
	protected static final String K_JOB = "JOB";
	protected static final String K_INCLUDE = "INCLUDE";
	protected static final String K_SET = "SET";
	protected static final String K_JCLLIB = "JCLLIB";
	protected static final String K_EXEC = "EXEC";
	protected static final String K_DD = "DD";
	protected static final String K_PROC = "PROC";
	protected static final String K_PEND = "PEND";
	
	private static final Pattern P_COMMENT = Pattern.compile(Rgx.seq(
			Pattern.quote("//*"),
			Rgx.grp(G_COMMENT, Rgx.rep0X("."))
			));
	
	private static final Pattern P_STMT = Pattern.compile(Rgx.seq(
			Pattern.quote("//"),
			Rgx.grp(G_NAME, Rgx.rep0X("[^ ]")),
			Rgx.rep01(Rgx.seq(
					Rgx.rep1X(" "),
					Rgx.rep01(Rgx.grp(G_STMT_TYPE, Rgx.or(K_JOB, K_INCLUDE, K_SET, K_JCLLIB, K_EXEC, K_DD, K_PROC, K_PEND))))),
			Rgx.rep01(Rgx.seq(
				Rgx.rep1X(" "),
				Rgx.grp(G_PARMS, Rgx.rep0X("."))))),
			Pattern.CASE_INSENSITIVE);
	
	private static final Pattern P_INPUT_END = Pattern.compile(Rgx.seq(
			Pattern.quote("/*"),
			Rgx.rep0X(".")
			));
	
}