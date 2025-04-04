package t6bygedq.app;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;
import t6bygedq.lib.Rgx;
import t6bygedq.lib.cbl.CblXrefParser.LineMatcher;

/**
 * @author 2oLDNncs 20250404
 */
@Debug(true)
public final class ParseJcl {
	
	public static final String ARG_IN = "-In";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/JCL/pds.jcl");
		
		final var parser = new JclParser();
		
		Files.lines(ap.getPath(ARG_IN)).forEach(parser::parseLine);
	}
	
	/**
	 * @author 2oLDNncs 20250404
	 */
	public static final class JclParser {
		
		private final LineMatcher card1Matcher = new LineMatcher(P_CARD1);
		private final LineMatcher card2Matcher = new LineMatcher(P_CARD2);
		private final LineMatcher commentMatcher = new LineMatcher(P_COMMENT);
		
		public final void parseLine(final String line) {
			Helpers.dprintlnf("%s", line);
			
			if (this.card1Matcher.matches(line)) {
				final var name = this.card1Matcher.group(G_NAME);
				final var stmtType = Objects.requireNonNullElse(this.card1Matcher.group(G_STMT_TYPE), "").toUpperCase(Locale.ENGLISH);
				final var parms = Objects.requireNonNullElse(this.card1Matcher.group(G_PARMS), "").split(",");
				
				Helpers.dprintlnf("--> %s: %s", G_NAME, name);
				Helpers.dprintlnf("--> %s: %s", G_STMT_TYPE, stmtType);
				Helpers.dprintlnf("--> %s: %s", G_PARMS, Arrays.toString(parms));
				
				switch (stmtType) {
				case K_JOB:
					System.out.println(Helpers.dformat("--> TODO: %s", stmtType));
					break;
				case K_EXEC:
					System.out.println(Helpers.dformat("--> TODO: %s", stmtType));
					break;
				case K_DD:
					System.out.println(Helpers.dformat("--> TODO: %s", stmtType));
					break;
				case K_PROC:
					System.out.println(Helpers.dformat("--> TODO: %s", stmtType));
					break;
				case K_PEND:
					System.out.println(Helpers.dformat("--> TODO: %s", stmtType));
					break;
				case "":
					System.out.println(Helpers.dformat("--> TODO: %s", stmtType));
					break;
				default:
					System.err.println(Helpers.dformat("--> TODO: %s", stmtType));
				}
			} else if (this.card2Matcher.matches(line)) {
				//pass
			} else if (this.commentMatcher.matches(line)) {
				//pass
			} else {
				System.err.println(Helpers.dformat("?%s", line));
			}
		}
		
		private static final String G_NAME = "Name";
		private static final String G_STMT_TYPE = "StmtType";
		private static final String G_PARMS = "Parms";
		
		private static final String K_JOB = "JOB";
		private static final String K_EXEC = "EXEC";
		private static final String K_DD = "DD";
		private static final String K_PROC = "PROC";
		private static final String K_PEND = "PEND";
		
		private static final Pattern P_CARD1 = Pattern.compile(Rgx.seq(
				"//",
				Rgx.grp(G_NAME, Rgx.rep0X("[^ ]")),
				Rgx.rep1X(" "),
				Rgx.rep01(Rgx.grp(G_STMT_TYPE, Rgx.or(K_JOB, K_EXEC, K_DD, K_PROC, K_PEND))),
				Rgx.rep01(Rgx.seq(
					Rgx.rep1X(" "),
					Rgx.grp(G_PARMS, Rgx.rep0X("."))
				))), Pattern.CASE_INSENSITIVE);
		
		private static final Pattern P_CARD2 = Pattern.compile(Rgx.seq(
				"/\\*",
				Rgx.rep0X(".")
				));
		
		private static final Pattern P_COMMENT = Pattern.compile(Rgx.seq(
				"//\\*",
				Rgx.rep0X(".")
				));
		
	}

}
