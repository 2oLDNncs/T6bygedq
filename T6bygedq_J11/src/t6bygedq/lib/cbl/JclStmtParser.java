package t6bygedq.lib.cbl;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
		
		{
			final var tokens = new ArrayList<Parm>();
			
			try {
				generateTokens(newTokenizer(parms), tokens);
				
				Helpers.dprintlnf("<%s", tokens);
				
				parseGrps(tokens);
				
				Helpers.dprintlnf(">%s", tokens);
			} catch (final IOException e) {
				e.printStackTrace();
			}
			
			this.currentStmt.getParms().addAll(tokens);
		}
	}
	
	private static final void generateTokens(final StreamTokenizer st, final List<Parm> tokens) throws IOException {
		while (StreamTokenizer.TT_EOF != st.nextToken()) {
			switch (st.ttype) {
			case StreamTokenizer.TT_WORD:
				tokens.add(new Parm_Id(st.sval));
				break;
			case '\'':
				final var previousToken = tokens.isEmpty() ? null : Helpers.castOrNull(Parm_Str.class, Helpers.last(tokens));
				
				if (null != previousToken) {
					tokens.set(tokens.size() - 1, new Parm_Str(String.format("%s'%s",
							previousToken.getVal(), st.sval)));
				} else {
					tokens.add(new Parm_Str(st.sval));
				}
				break;
			case StreamTokenizer.TT_NUMBER:
				throw new IllegalStateException();
			default:
				switch (st.ttype) {
				case ' ':
					tokens.add(Parm_Id.T_SPAC);
					break;
				case ',':
					tokens.add(Parm_Id.T_DELI);
					break;
				case '(':
					tokens.add(Parm_Id.T_LPAR);
					break;
				case ')':
					tokens.add(Parm_Id.T_RPAR);
					break;
				case '=':
					tokens.add(Parm_Id.T_EQUA);
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
		
		result.quoteChar('\'');
		result.slashSlashComments(false);
		result.slashStarComments(false);
		result.commentChar(' ');
		result.eolIsSignificant(false);
		result.lowerCaseMode(false);
		
		return result;
	}
	
	private static final void parseGrps(final List<Parm> tokens) {
		final var grouping = new Stack<Integer>();
		
		for (var j = 0; j < tokens.size(); j += 1) {
			final var token = tokens.get(j);
			
			if (Parm_Id.T_LPAR == token) {
				grouping.push(j);
			} else if (Parm_Id.T_RPAR == token) {
				final var i = grouping.pop();
				final var group = new ArrayList<>(tokens.subList(i + 1, j));
				
				parseElts(group);
				
				final var grp = newGrp(group);
				
				replaceSubList(tokens, i, j + 1, grp);
				
				j = i;
			}
		}
		
		parseElts(tokens);
	}
	
	private static final void parseElts(final List<Parm> tokens) {
		for (int i = 0, j = 0; j < tokens.size(); j += 1) {
			final var token = tokens.get(j);
			
			if (Parm_Id.T_DELI  == token) {
				final var group = new ArrayList<>(tokens.subList(i, j));
				
				parseDfns(group);
				
				final var p = 1 == group.size() ? group.get(0) : newGrp(group);
				
				replaceSubList(tokens, i, j + 1, p);
				
				j = i;
				i += 1;
			}
		}
		
		parseDfns(tokens);
	}
	
	private static final void parseDfns(final List<Parm> tokens) {
		for (var i = tokens.size() - 1; 0 <= i; i -= 1) {
			final var token = tokens.get(i);
			
			if (Parm_Id.T_EQUA == token) {
				final var keyToken = tokens.get(i - 1);
				final var valToken = tokens.get(i + 1);
				final var dfn = new Parm_Dfn(((Parm_Id) keyToken).getVal(), valToken);
				
				replaceSubList(tokens, i - 1, i + 2, dfn);
				
				i -= 1;
			}
		}
	}
	
	private static final Parm_Grp newGrp(final List<Parm> elts) {
		final var result = new Parm_Grp();
		
		result.getElts().addAll(elts);
		
		return result;
	}
	
	public static final <E> void replaceSubList(final List<E> list, final int start, final int end, final E element) {
		list.subList(start, end).clear();
		list.add(start, element);
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static abstract class Parm {
		//pass
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Parm_Grp extends Parm {
		
		private final List<Parm> elts = new ArrayList<>();
		
		public final List<Parm> getElts() {
			return this.elts;
		}
		
		@Override
		public final String toString() {
			return "¤" + this.getElts().toString();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Parm_Dfn extends Parm {
		
		private final String key;
		
		private final Parm val;
		
		public Parm_Dfn(final String key, final Parm val) {
			this.key = key;
			this.val = val;
		}
		
		public final String getKey() {
			return this.key;
		}
		
		public final Parm getVal() {
			return this.val;
		}
		
		@Override
		public final String toString() {
			return "¤" + String.format("%s=%s", this.getKey(), this.getVal());
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static abstract class Parm_Atom extends Parm {
		
		private final String val;
		
		protected Parm_Atom(final String val) {
			this.val = val;
		}
		
		public final String getVal() {
			return this.val;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Parm_Id extends Parm_Atom {
		
		public Parm_Id(final String val) {
			super(val);
		}
		
		@Override
		public final String toString() {
			return "¤" + this.getVal();
		}
		
		public static final Parm_Id T_SPAC = new Parm_Id(" ");
		public static final Parm_Id T_DELI = new Parm_Id(",");
		public static final Parm_Id T_LPAR = new Parm_Id("(");
		public static final Parm_Id T_RPAR = new Parm_Id(")");
		public static final Parm_Id T_EQUA = new Parm_Id("=");
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Parm_Str extends Parm_Atom {
		
		public Parm_Str(final String val) {
			super(val);
		}
		
		@Override
		public final String toString() {
			return "¤" + String.format("'%s'", this.getVal());
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
		
		private final List<Parm> parms = new ArrayList<>();
		
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
		
		public final List<Parm> getParms() {
			return this.parms;
		}
		
		public final List<String> getInput() {
			return this.input;
		}
		
		public final Parm findParmVal(final String parmKey) {
			final var itStack = new Stack<Iterator<Parm>>();
			
			itStack.push(this.getParms().iterator());
			
			while (!itStack.isEmpty()) {
				final var it = itStack.peek();
				
				if (it.hasNext()) {
					final var parm = it.next();
					
					if (parm instanceof Parm_Grp) {
						itStack.pop();
						itStack.push(((Parm_Grp) parm).getElts().iterator());
					} else if (parm instanceof Parm_Dfn) {
						final var dfn = (Parm_Dfn) parm;
						final var dfnKey = dfn.getKey();
						final var dfnValue = dfn.getVal();
						
						if (Objects.equals(parmKey, dfnKey)) {
							return dfnValue;
						}
						
						if (dfnValue instanceof Parm_Grp) {
							itStack.push(((Parm_Grp) dfnValue).getElts().iterator());
						}
					}
				} else {
					itStack.pop();
				}
			}
			
			return null;
		}
		
		@Override
		public final String toString() {
			return String.format("%s:%s%s%s", this.getName(), this.getType(), this.getParms(), this.getInput());
		}
		
	}
	
}