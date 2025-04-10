package t6bygedq.lib.cbl;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20250406
 */
@Debug(false)
public abstract class JclStmtParser extends JclLineParser {
	
	private Stmt currentStmt = null;
	
	@Override
	protected void parseJclLineStmt(final String name, final String type, final String parms) {
		super.parseJclLineStmt(name, type, parms);
		
		if (null == this.currentStmt || !"".equals(name)) {
			this.nextStmt(name, type);
		} else {
			switch (type) {
			case "":
				break;
			case K_EXEC:
			case K_PEND:
				this.nextStmt(name, type);
				break;
			case K_DD:
				this.nextStmt(this.currentStmt.getName(), type);
				break;
			default:
				System.err.println(Helpers.dformat(this.errorMessage("Unexpected stmt type <%s>", type)));
				break;
			}
		}
		
		if (true) {
			final var st = newTokenizer(parms);
			
			final var tokens = new ArrayList<ParmsToken>();
			
			try {
				generateTokens(st, tokens);
				
				Helpers.dprintlnf("<%s", tokens);
				
				parseGrps(tokens);
				
				Helpers.dprintlnf(">%s", tokens);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
//			this.currentStmt.getParms().addAll(Arrays.asList(parms.split(",")));
			this.currentStmt.getParms().addAll(ParmsToken.parse(parms));
		}
	}
	
	private static final void generateTokens(final StreamTokenizer st, final ArrayList<ParmsToken> tokens) throws IOException {
		while (StreamTokenizer.TT_EOF != st.nextToken()) {
			switch (st.ttype) {
			case StreamTokenizer.TT_WORD:
				tokens.add(new ParmsToken_Id(st.sval));
				break;
			case '\'':
			case '\"':
				tokens.add(new ParmsToken_String(st.sval));
				break;
			case StreamTokenizer.TT_NUMBER:
				throw new IllegalStateException();
			default:
				switch (st.ttype) {
				case ' ':
					tokens.add(ParmsToken_Id.T_SPAC);
					break;
				case ',':
					tokens.add(ParmsToken_Id.T_DELI);
					break;
				case '(':
					tokens.add(ParmsToken_Id.T_LPAR);
					break;
				case ')':
					tokens.add(ParmsToken_Id.T_RPAR);
					break;
				case '=':
					tokens.add(ParmsToken_Id.T_EQUA);
					break;
				default:
					throw new IllegalStateException(String.format("Syntax error: %s", (char) st.ttype));
				}
			}
		}
	}
	
	private static final StreamTokenizer newTokenizer(final String parms) {
		final var result = new StreamTokenizer(new StringReader(parms));
		
		result.resetSyntax();
		
		for (char i = 0; i < 256; i += 1) {
			switch (i) {
			case '(':
			case ')':
			case '"':
			case '\'':
			case ',':
			case ' ':
			case '=':
				break;
			default:
				result.wordChars(i, i);
				break;
			}
		}
		
		result.quoteChar('"');
		result.quoteChar('\'');
		result.slashSlashComments(false);
		result.slashStarComments(false);
		result.commentChar(' ');
		result.eolIsSignificant(false);
		result.lowerCaseMode(false);
		
		return result;
	}
	
	private static final void parseGrps(final ArrayList<ParmsToken> tokens) {
		final var grouping = new Stack<Integer>();
		
		for (var i = 0; i < tokens.size(); i += 1) {
			final var token = tokens.get(i);
			
			if (ParmsToken_Id.T_LPAR == token) {
				grouping.push(i);
			} else if (ParmsToken_Id.T_RPAR == token) {
				final var j = grouping.pop();
				final var group = new ArrayList<>(tokens.subList(j + 1, i));
				parseElts(group);
				final var subList = tokens.subList(j, i + 1);
				subList.clear();
				final var grp = new ParmsToken_Group();
				group.forEach(grp.getTokens()::add);
				subList.add(grp);
				i = j;
			}
		}
		
		parseElts(tokens);
	}
	
	private static final void parseElts(final List<ParmsToken> tokens) {
		for (int i = 0, j = 0; j < tokens.size(); j += 1) {
			final var token = tokens.get(j);
			
			if (ParmsToken_Id.T_DELI  == token) {
				final var group = new ArrayList<>(tokens.subList(i, j));
				
				parseDfns(group);
				
				final var subList = tokens.subList(i, j + 1);
				subList.clear();
				
				if (1 == group.size()) {
					subList.add(group.get(0));
				} else {
					final var grp = new ParmsToken_Group();
					
					group.stream()
					.map(ParmsToken.class::cast)
					.forEach(grp.getTokens()::add);
					
					subList.add(grp);
				}
				j = i;
				i += 1;
			}
		}
		
		parseDfns(tokens);
	}
	
	private static final void parseDfns(final List<ParmsToken> tokens) {
		for (var i = 0; i < tokens.size(); i += 1) {
			final var token = tokens.get(i);
			
			if (ParmsToken_Id.T_EQUA == token) {
				final var dfn = new ParmsToken_Dfn(tokens.get(i - 1).toString(), tokens.get(i + 1));
				final var subList = tokens.subList(i - 1, i + 2);
				subList.clear();
				subList.add(dfn);
				i -= 1;
			}
		}
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static abstract class ParmsToken {
					
		private static enum ParseMode {
			NORMAL, STRING, GROUP_END;
		}
		
		public static final List<ParmsToken> parse(final String parms) {
			final var result = new ArrayList<ParmsToken>();
			final var buffer = new StringBuilder();
			final var grouping = new Stack<Integer>();
			
			final Runnable detectDfn = () -> {
				final var n = result.size();
				
				if (3 <= n && "=".equals(result.get(n - 2).toString()) &&
						(grouping.isEmpty() || grouping.peek() <= n - 3)) {
					final var dfn = new ParmsToken_Dfn(result.get(n - 3).toString(), result.get(n - 1));
					result.subList(n - 3, n).clear();
					result.add(dfn);
				}
			};
			
			var parseMode = ParseMode.NORMAL;
			final var n = parms.length();
			
			for (var i = 0; i < n; i += 1) {
				final var c = parms.charAt(i);
				
				switch (parseMode) {
				case GROUP_END:
					if (',' == c) {
						detectDfn.run();
						parseMode = ParseMode.NORMAL;
					} else if (')' == c) {
						parseMode = ParseMode.NORMAL;
					} else if (' ' == c) {
						i = n;
						parseMode = ParseMode.NORMAL;
					} else if ('\'' == c) {
						buffer.append(((ParmsToken_String) result.remove(result.size() - 1)).getValue());
						buffer.append(c);
						parseMode = ParseMode.STRING;
					} else {
						System.err.println(String.format("Unexpected char <%s>", c));
						i = n;
						parseMode = ParseMode.NORMAL;
					}
					
					break;
				case NORMAL:
					if (',' == c) {
						result.add(new ParmsToken_Id(buffer.toString()));
						buffer.setLength(0);
						detectDfn.run();
					} else if ('=' == c) {
						result.add(new ParmsToken_Id(buffer.toString()));
						buffer.setLength(0);
						result.add(new ParmsToken_Id("="));
//						System.out.println(Helpers.dformat("%s", result));
					} else if ('\'' == c) {
						parseMode = ParseMode.STRING;
					} else if ('(' == c) {
						grouping.push(result.size());
					} else if (')' == c) {
						result.add(new ParmsToken_Id(buffer.toString()));
						buffer.setLength(0);
						detectDfn.run();

						final var j = grouping.pop();
						final var group = new ParmsToken_Group();
						final var groupTokens = result.subList(j, result.size());
						group.getTokens().addAll(groupTokens);
						
//						System.out.println(Helpers.dformat("%s %s %s", j, result.size(), groupTokens));
						
						groupTokens.clear();
						result.add(group);
						
						detectDfn.run();
						
						parseMode = ParseMode.GROUP_END;
					} else if (' ' == c) {
						i = n;
					} else {
						buffer.append(c);
					}
					
					break;
				case STRING:
					if ('\'' == c) {
						result.add(new ParmsToken_String(buffer.toString()));
						buffer.setLength(0);
						
						parseMode = ParseMode.GROUP_END;
					} else {
						buffer.append(c);
					}
					
					break;
				default:
					break;
				}
			}
			
			if (0 < buffer.length()) {
				result.add(new ParmsToken_Id(buffer.toString()));
			}
			
			detectDfn.run();
			
			return result;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class ParmsToken_Group extends ParmsToken {
		
		private final List<ParmsToken> tokens = new ArrayList<>();
		
		public final List<ParmsToken> getTokens() {
			return this.tokens;
		}
		
		@Override
		public final String toString() {
			return "¤" + this.getTokens().toString();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class ParmsToken_Dfn extends ParmsToken {
		
		private final String key;
		
		private final ParmsToken value;
		
		public ParmsToken_Dfn(String key, ParmsToken value) {
			this.key = key;
			this.value = value;
		}
		
		public final String getKey() {
			return this.key;
		}
		
		public final ParmsToken getValue() {
			return this.value;
		}
		
		@Override
		public final String toString() {
			return "¤" + String.format("%s=%s", this.getKey(), this.getValue());
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static abstract class ParmsToken_Atom extends ParmsToken {
		
		private final String value;
		
		protected ParmsToken_Atom(final String value) {
			this.value = value;
		}
		
		public final String getValue() {
			return this.value;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class ParmsToken_Id extends ParmsToken_Atom {
		
		public ParmsToken_Id(final String value) {
			super(value);
		}
		
		@Override
		public final String toString() {
			return "¤" + this.getValue();
		}
		
		public static final ParmsToken_Id T_SPAC = new ParmsToken_Id(" ");
		public static final ParmsToken_Id T_DELI = new ParmsToken_Id(",");
		public static final ParmsToken_Id T_LPAR = new ParmsToken_Id("(");
		public static final ParmsToken_Id T_RPAR = new ParmsToken_Id(")");
		public static final ParmsToken_Id T_EQUA = new ParmsToken_Id("=");
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class ParmsToken_String extends ParmsToken_Atom {
		
		public ParmsToken_String(final String value) {
			super(value);
		}
		
		@Override
		public final String toString() {
			return "¤" + String.format("'%s'", this.getValue());
		}
		
	}
	
	@Override
	protected void parseJclLineInput(final String line) {
		super.parseJclLineInput(line);
		
		if (null != this.currentStmt) {
			this.currentStmt.getInput().add(line);
		} else {
			System.err.println(Helpers.dformat("Unexpected input line <%s>", line));
		}
	}
	
	@Override
	protected void parseEnd() {
		super.parseEnd();
		this.setCurrentStmt(null);
	}
	
	private final void nextStmt(final String name, final String type) {
		this.setCurrentStmt(new Stmt(name, type, this.cloneCurrentLocation()));
	}
	
	private final void setCurrentStmt(final Stmt currentStmt) {
		if (null != this.currentStmt) {
			this.parseStmt(this.currentStmt);
		}
		
		this.currentStmt = currentStmt;
	}
	
	protected void parseStmt(final Stmt stmt) {
		Helpers.dprintlnf(">>%s", stmt);
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Stmt {
		
		private final String name;
		
		private final String type;
		
		private final List<Object> parms = new ArrayList<>();
		
		private final List<String> input = new ArrayList<>();
		
		private final Location location;
		
		public Stmt(final String name, final String type, final Location location) {
			this.name = name;
			this.type = type;
			this.location = location;
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final String getType() {
			return this.type;
		}
		
		public final URI getSource() {
			return this.location.getSource();
		}
		
		public final int getSourceLineNumber() {
			return this.location.getLineNumber();
		}
		
		public final String getSourceLine() {
			return this.location.getLine();
		}
		
		public final String errorMessage(final String prefixFormat, final Object... prefixArgs) {
			return this.location.errorMessage(prefixFormat, prefixArgs);
		}
		
		public final List<Object> getParms() {
			return this.parms;
		}
		
		public final List<String> getInput() {
			return this.input;
		}
		
		@Override
		public final String toString() {
			return String.format("%s:%s%s%s", this.getName(), this.getType(), this.getParms(), this.getInput());
		}
		
	}
	
}