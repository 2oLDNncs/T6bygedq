package t6bygedq.lib.cbl;

import static t6bygedq.lib.Helpers.array;
import static t6bygedq.lib.Helpers.dprintlnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import t6bygedq.lib.Helpers;
import t6bygedq.lib.Rgx;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20250106
 */
@Debug(true)
public final class CblXrefParser {
	
	private final String moduleName;
	
	private final Map<Integer, Def> dataItems = new LinkedHashMap<>();
	private final Map<Integer, Def> procedures = new LinkedHashMap<>();
	private final Map<Integer, Def> programs = new LinkedHashMap<>();
	private final Map<Integer, Def> verbs = new LinkedHashMap<>();
	private Def currentDef = null;
	private String refParseMode = null;
	
	private final LineMatcher lmMapTrigger = new LineMatcher(P_MAP_TRIGGER);
	private final LineMatcher lmMap = new LineMatcher(P_MAP);
	private final LineMatcher lmRefTrigger = new LineMatcher(P_REF_TRIGGER);
	private final LineMatcher lmRefs = new LineMatcher(P_REFS);
	private final LineMatcher lmProcedure = new LineMatcher(P_PROCEDURE);
	private final LineMatcher lmEndOfPage = new LineMatcher(P_END_OF_PAGE);
	private boolean mapMode = false;
	
	public CblXrefParser(final String moduleName) {
		this.moduleName = moduleName;
	}
	
	public final String getModuleName() {
		return this.moduleName;
	}
	
	public final void parse(final String line) {
		dprintlnf("%s", line);
		
		if (this.lmMapTrigger.matches(line)) {
			this.mapMode = true;
			dprintlnf("--> MAP_TRIGGER (%s)", line);
		} else if (this.mapMode && this.lmMap.matches(line)) {
			final var lineId = Integer.parseInt(this.lmMap.group(G_MAP_LINE_ID));
			final var level = Integer.parseInt(this.lmMap.group(G_MAP_LEVEL));
			final var name = this.lmMap.group(G_MAP_NAME);
			
			dprintlnf("--> MAP (%s %s %s)",
					lineId,
					level,
					name);
			
			final var def = this.getDef(this.dataItems, lineId, name);
			
			def.setProp(G_MAP_LEVEL, level);
		} else if (this.lmRefTrigger.matches(line)) {
			this.refParseMode = this.lmRefTrigger.group(G_PARSE_MODE);
			dprintlnf("--> REF_TRIGGER (%s)", this.refParseMode);
		} else if (null != this.refParseMode && this.lmRefs.matches(line)) {
			if (null != this.lmRefs.group(G_DEF_LINE_ID)) {
				this.addNewDef(this.lmRefs::group);
			}
			
			if (null != this.currentDef) {
				parseRefs(this.lmRefs.group(G_REFS), this.currentDef);
				
				dprintlnf("--> REFS (%s) (%s) %s",
						this.currentDef.getId(),
						this.currentDef.getName(),
						this.currentDef.getRefs());
			}
		} else if (this.lmProcedure.matches(line)) {
			// TODO Determine lineId
//			this.addNewDef(this.procedures, sLineId, CblConstants.KW_PROCEDURE);
		} else if (this.lmEndOfPage.matches(line)) {
			dprintlnf("--> END OF PAGE");
			
			this.mapMode = false;
			this.refParseMode = null;
		}
	}
	
	private final void addNewDef(final UnaryOperator<String> matcherGroup) {
		final Map<Integer, Def> target = this.selectParseTarget();
		
		if (null != target) {
			final var sLineId = matcherGroup.apply(G_DEF_LINE_ID);
			final var name = matcherGroup.apply(G_DEF_NAME);
			
			this.addNewDef(target, sLineId, name);
		}
	}
	
	private final void addNewDef(final Map<Integer, Def> target, final String sLineId, final String name) {
		final int lineId;
		
		if (C_EXTERNAL.equals(sLineId) || PM_VERBS.equals(this.refParseMode)) {
			lineId = -target.size() - 1;
		} else {
			lineId = Integer.parseInt(sLineId);
		}
		
		this.currentDef = this.getDef(target, lineId, name);
		
		target.put(this.currentDef.getId(), this.currentDef);
	}
	
	private final Def getDef(final Map<Integer, Def> target, final Integer lineId, final String name) {
		final var nonemptyName = "".equals(name) || CblConstants.KW_FILLER.equalsIgnoreCase(name) ?
				(this.getModuleName() + "@" + lineId) : name;
		
		return target.computeIfAbsent(lineId, k -> new Def(k, nonemptyName));
	}
	
	private final Map<Integer, Def> selectParseTarget() {
		switch (this.refParseMode) {
		case PM_DATA_NAMES:
			return this.dataItems;
		case PM_PROCEDURES:
			return this.procedures;
		case PM_PROGRAMS:
			return this.programs;
		case PM_VERBS:
			return this.verbs;
		}
		
		return null;
	}
	
	public final void generateOps(final List<Op> ops) {
		this.verbs.values().forEach(def -> {
			def.getRefs().forEach(ref -> {
				ops.add(new Op(this.getModuleName(), ref.getId(), null, def.getName(), "", ref.getUsage()));
			});
		});
		
		this.procedures.values().forEach(def -> {
			ops.add(new Op(this.getModuleName(), def.getId(), def.getName(), "", "#proc", ""));
			def.getRefs().forEach(ref -> {
				ops.add(new Op(this.getModuleName(), ref.getId(), null, null, def.getName(), ref.getUsage()));
			});
		});
		
		this.dataItems.values().forEach(def -> {
			ops.add(new Op(this.getModuleName(), def.getId(), null, "#data", def.getName(),
					"" + def.getProp(G_MAP_LEVEL, 0)));
			def.getRefs().forEach(ref -> {
				ops.add(new Op(this.getModuleName(), ref.getId(), null, null, def.getName(),
						U_MODIFY.equals(ref.getUsage()) ? U_MODIFY : U_READ));
			});
		});
		
		ops.sort((r1, r2) -> r1.getLineId().compareTo(r2.getLineId()));
		
		for (var i = 1; i < ops.size(); i += 1) {
			final var previousRow = ops.get(i - 1);
			final var currentRow = ops.get(i);
			
			if (null == currentRow.getProc()) {
				currentRow.setProc(previousRow.getProc());
			}
			
			if (null == currentRow.getVerb()) {
				currentRow.setVerb(previousRow.getVerb());
			}
		}
	}
	
	public static final String U_PERFORM = "P";
	public static final String U_READ = "R";
	public static final String U_MODIFY = "M";
	
	public static final String PM_DATA_NAMES = "data names";
	public static final String PM_PROCEDURES = "procedures";
	public static final String PM_PROGRAMS = "programs";
	public static final String PM_VERBS = "verbs";
	
	public static final String G_PARSE_MODE = "ParseMode";
	public static final String G_REF = "Ref";
	public static final String G_REF_USAGE = "RefUsage";
	public static final String G_REF_LINE_ID = "RefLineId";
	public static final String G_DEF_LINE_ID = "DefLineId";
	public static final String G_DEF_NAME = "DefName";
	public static final String G_REFS = "Refs";
	
	public static final String C_EXTERNAL = "EXTERNAL";
	
	public static final String G_MAP_LINE_ID = "MapLineId";
	public static final String G_MAP_LEVEL = "Level";
	public static final String G_MAP_NAME = "Name";
	
	public static final Pattern P_END_OF_PAGE = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep1X("."),
			String.join(Rgx.rep1X("."), " Date ", " Time ", " Page ", Rgx.rep1X("\\d")),
			Rgx.rep0X("."))));
	
	public static final Pattern P_MAP_TRIGGER = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep0X(" "),
			String.join(Rgx.rep1X(" "), "LineID", "Data Name", "Locator", "Structure", "Definition", "Data Type", "Attributes"),
			Rgx.rep0X(" "))));
	
	public static Pattern P_MAP = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep0X(" "),
			String.join(Rgx.rep1X(" "),
					Rgx.grp(G_MAP_LINE_ID, Rgx.rep1X("\\d")),
					Rgx.grp(G_MAP_LEVEL, Rgx.rep1X("\\d")),
					Rgx.grp(G_MAP_NAME, Rgx.rep0X("[^ .]"))),
			Rgx.rep0X("."))));
	
	public static final Pattern P_REF_TRIGGER = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep0X(" "),
			Rgx.or("Defined", "Count"),
			Rgx.rep1X(" "),
			"Cross-reference of ",
			Rgx.grp(G_PARSE_MODE, Rgx.or(PM_DATA_NAMES, PM_PROCEDURES, PM_PROGRAMS, PM_VERBS)),
			Rgx.rep1X(" "),
			"References",
			Rgx.rep0X(" "))));
	private static final String R_REF = Rgx.grp(G_REF,Rgx.seq(
			Rgx.grp(G_REF_USAGE, Rgx.rep01("[A-Z]")),
			Rgx.grp(G_REF_LINE_ID, Rgx.rep1X("\\d"))));
	public static final Pattern P_REF = Pattern.compile(R_REF, Pattern.CASE_INSENSITIVE);
	public static final Pattern P_REFS = Pattern.compile(
			Rgx.line(Rgx.seq( // 2 cases: Definition with LineId, Name and references; Continuation with references only
					Rgx.or(
							Rgx.seq( // Definition
									Rgx.rep(0, 9, " "),
									Rgx.grp(G_DEF_LINE_ID, Rgx.or(
											C_EXTERNAL,
											Rgx.rep1X("\\d"))),
									Rgx.rep1X(" "),
									Rgx.grp(G_DEF_NAME, Rgx.rep1X("[^ .]")),
									Rgx.rep1X("[ .]")),
							Rgx.rep(40, 43, " ")), // No definition: continuation of previous line
					Rgx.grp(G_REFS, Rgx.rep0X(Rgx.seq(
							" ",
							R_REF))))), // References
			Pattern.CASE_INSENSITIVE);
	public static final Pattern P_PROCEDURE = Pattern.compile(
			Rgx.start(Rgx.seq("  ", Rgx.rep(21, 21, "."), Rgx.repNX(1, " "), CblConstants.KW_PROCEDURE)),
			Pattern.CASE_INSENSITIVE);
	
	private static int lastId = 0;
	
	private static final int newId() {
		return ++lastId;
	}
	
	public static final void generateFlows(final List<Op> ops, final List<Flow> flows) {
		final var context = new Op[1];
		final var srcs = new LinkedHashSet<>();
		final var dsts = new LinkedHashSet<>();
		final var dataParents = new HashMap<>();
		final var dataDefinitionStack = new Stack<Object[]>();
		
		dataDefinitionStack.push(array("", 0));
		
		ops.forEach(op -> {
			final var module = op.getModule();
			
			if (null == context[0]) {
				context[0] = new Op(module, null, null, null, null, null);
			}
			
			final var lineId = op.getLineId();
			final var proc = op.getProc();
			final var verb = op.getVerb();
			final var obj = op.getObj();
			final var usage = op.getUsage();
			
			if (null == proc && "#data".equals(verb) && usage.matches("\\d+")) {
				dprintlnf("DataItem: %s %s", obj, usage);
				
				final var dataName = obj;
				final var dataLevel = Integer.parseInt(usage);
				
				if ((int) dataDefinitionStack.peek()[1] < dataLevel) {
					if (88 == dataLevel) {
						dataParents.put(dataName, dataDefinitionStack.peek()[0]);
					} else {
						dataDefinitionStack.push(array(dataName, dataLevel));
					}
				} else {
					while (dataLevel <= (int) dataDefinitionStack.peek()[1]) {
						dataDefinitionStack.pop();
					}
					
					dataDefinitionStack.push(array(dataName, dataLevel));
				}
			}
			
			if (!lineId.equals(context[0].getLineId()) && "".equals(obj)) {
				updateFlows(dataParents, context[0], srcs, dsts, flows);
				
				dsts.clear();
				srcs.clear();
				context[0] = op;
			}
			
			if (U_READ.equals(usage)) {
				srcs.add(obj);
			} else if (U_MODIFY.equals(usage)) {
				dsts.add(obj);
			}
		});
		
		if (null == context[0]) {
			System.err.println(Helpers.dformat("Warning: no data"));
		} else {
			updateFlows(dataParents, context[0], srcs, dsts, flows);
		}
	}
	
	private static final void updateFlows(final Map<Object, Object> dataParents, final Op context, final Collection<Object> srcs,
			final Collection<Object> dsts, final List<Flow> flows) {
		final var verb = context.getVerb();
		final var verbIsValid = null != verb && !"".equals(verb);
		
		if (verbIsValid) {
			if (srcs.isEmpty()) {
				if (CblConstants.VB_SET.equalsIgnoreCase(verb) && 1 == dsts.size()) {
					final var parent = dataParents.get(dsts.iterator().next());
					
					if (null != parent) {
						srcs.addAll(dsts);
						dsts.clear();
						dsts.add(parent);
					}
				}
				
				if (srcs.isEmpty()) {
					srcs.add("?" + newId());
				}
			} else if (dsts.isEmpty()) {
				switch (verb.toUpperCase(Locale.ENGLISH)) {
				case CblConstants.VB_CALL:
				case CblConstants.VB_DISPLAY:
				case CblConstants.VB_EVALUATE:
				case CblConstants.VB_IF:
					break;
				default:
					dsts.add("?" + newId());
					break;
				}
				
//				new ArrayList<>(srcs).stream()
//				.map(dataParents::get)
//				.filter(Objects::nonNull)
//				.forEach(srcs::add);
				
				for (final var src : srcs) {
					final var parent = dataParents.get(src);
					
					if (null != parent) {
						flows.add(new Flow(context.getModule(), context.getProc(), "#88", src.toString(), parent.toString()));
					}
				}
			}
			
			srcs.forEach(src -> {
				dsts.forEach(dst -> {
					flows.add(new Flow(context.getModule(), context.getProc(), context.getVerb(), src.toString(), dst.toString()));
				});
			});
		}
	}
	
	private static final void parseRefs(final String refs, final Def def) {
		final var refStrings = refs.split(" +");
		
		Arrays.stream(refStrings)
				.filter(refString -> !refString.isEmpty())
				.map(refString -> {
					final var m = P_REF.matcher(refString);
					
					if (!m.matches()) {
						throw new IllegalStateException(String.format("Invalid ref: %s", refString));
					}
					
					return new Ref(Integer.parseInt(m.group(G_REF_LINE_ID)), m.group(G_REF_USAGE));
				})
				.forEach(def.getRefs()::add);
	}
	
	/**
	 * @author 2oLDNncs 20250104
	 */
	public static abstract class Obj {
		
		private final int id;
		
		protected Obj(final int id) {
			this.id = id;
		}
		
		public final int getId() {
			return this.id;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250104
	 */
	public static final class Def extends Obj {
		
		private final String name;
		
		private final Collection<Ref> refs = new ArrayList<>();
		
		private final Map<String, Object> props = new HashMap<>();
		
		public Def(final int id, final String name) {
			super(id);
			this.name = name;
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final void setProp(final String key, final Object value) {
			this.props.put(key, value);
		}
		
		public final Object getProp(final String key) {
			return this.props.get(key);
		}
		
		public final Object getProp(final String key, final Object defaultValue) {
			return this.props.getOrDefault(key, defaultValue);
		}
		
		public final Collection<Ref> getRefs() {
			return this.refs;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250104
	 */
	public static final class Ref extends Obj {
		
		private final String usage;
		
		public Ref(final int lineId, final String usage) {
			super(lineId);
			this.usage = usage;
		}
		
		public final String getUsage() {
			return this.usage;
		}
		
		@Override
		public final String toString() {
			return String.format("%s%s", this.getUsage(), this.getId());
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250321
	 */
	public static final class LineMatcher {
		
		private final Pattern pattern;
		
		private Matcher matcher;
		
		public LineMatcher(final Pattern pattern) {
			this.pattern = pattern;
		}
		
		public final boolean matches(final CharSequence line) {
			this.matcher = this.pattern.matcher(line);
			
			return this.matcher.matches();
		}
		
		public final String group(final String name) {
			return this.matcher.group(name);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250324
	 */
	public static final class Op {
		
		private final String module;
		private final Integer lineId;
		private String proc;
		private String verb;
		private String obj;
		private String usage;
		
		public Op(final String module, final Integer lineId,
				final String proc, final String verb, final String obj, final String usage) {
			this.module = module;
			this.lineId = lineId;
			this.proc = proc;
			this.verb = verb;
			this.obj = obj;
			this.usage = usage;
		}
		
		public final String getModule() {
			return this.module;
		}
		
		public final String getProc() {
			return this.proc;
		}
		
		public final void setProc(final String proc) {
			this.proc = proc;
		}
		
		public final String getVerb() {
			return this.verb;
		}
		
		public final void setVerb(final String verb) {
			this.verb = verb;
		}
		
		public final String getObj() {
			return this.obj;
		}
		
		public final void setObj(final String obj) {
			this.obj = obj;
		}
		
		public final String getUsage() {
			return this.usage;
		}
		
		public final void setUsage(final String usage) {
			this.usage = usage;
		}
		
		public final Integer getLineId() {
			return this.lineId;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250324
	 */
	public static final class Flow {
		
		private final String module;
		private final String proc;
		private final String verb;
		private final String src;
		private final String dst;
		
		public Flow(final String module, final String proc, final String verb, final String src, final String dst) {
			this.module = module;
			this.proc = proc;
			this.verb = verb;
			this.src = src;
			this.dst = dst;
		}
		
		public final String getModule() {
			return this.module;
		}
		
		public final String getProc() {
			return this.proc;
		}
		
		public final String getVerb() {
			return this.verb;
		}
		
		public final String getSrc() {
			return this.src;
		}
		
		public final String getDst() {
			return this.dst;
		}
		
	}
	
}