package t6bygedq.lib.cbl;

import static t6bygedq.lib.Helpers.dprintlnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import t6bygedq.lib.Rgx;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20250106
 */
@Debug(false)
public final class CblXrefParser {
	
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
	private int mapMode = 0;
	
	public final void parse(final String line) {
		dprintlnf("%s", line);
		
		if (0 < this.mapMode) {
			this.mapMode -= 1;
		}
		
		if (this.lmMapTrigger.matches(line)) {
			this.mapMode = 2;
			dprintlnf("--> MAP_TRIGGER (%s)", line);
		} else if (0 < this.mapMode && this.lmMap.matches(line)) {
			this.mapMode = 2;
			dprintlnf("--> MAP (%s %s %s)",
					this.lmMap.group(G_MAP_LINE_ID),
					this.lmMap.group(G_MAP_HIERARCHY),
					this.lmMap.group(G_MAP_NAME));
			// TODO Add def
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
		
		this.currentDef = new Def(lineId, this.refParseMode, name);
		
		target.put(this.currentDef.getId(), this.currentDef);
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
	
	public final void generateOps(final List<List<Object>> ops) {
		this.verbs.values().forEach(def -> {
			def.getRefs().forEach(ref -> {
				ops.add(Arrays.asList(ref.getId(), null, def.getName(), "", ref.getUsage()));
			});
		});
		
		this.procedures.values().forEach(def -> {
			ops.add(Arrays.asList(def.getId(), def.getName(), "", "", "!"));
			def.getRefs().forEach(ref -> {
				ops.add(Arrays.asList(ref.getId(), null, null, def.getName(), ref.getUsage()));
			});
		});
		
		this.dataItems.values().forEach(def -> {
			ops.add(Arrays.asList(def.getId(), null, null, def.getName(), "!"));
			def.getRefs().forEach(ref -> {
				ops.add(Arrays.asList(ref.getId(), null, null, def.getName(),
						U_MODIFY.equals(ref.getUsage()) ? U_MODIFY : U_READ));
			});
		});
		
		ops.sort((r1, r2) -> Integer.compare((int) r1.get(0), (int) r2.get(0)));
		
		for (var i = 1; i < ops.size(); i += 1) {
			final var previousRow = ops.get(i - 1);
			final var currentRow = ops.get(i);
			
			for (var j = 1; j < currentRow.size(); j += 1) {
				if (null == currentRow.get(j)) {
					currentRow.set(j, previousRow.get(j));
				}
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
	public static final String G_MAP_HIERARCHY = "Hierarchy";
	public static final String G_MAP_NAME = "Name";
	
	public static final Pattern P_MAP_TRIGGER = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep0(" "),
			String.join(Rgx.rep1(" "), "LineID", "Data Name", "Locator", "Structure", "Definition", "Data Type", "Attributes"),
			Rgx.rep0(" "))));
	
	public static Pattern P_MAP = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep0(" "),
			String.join(Rgx.rep1(" "),
					Rgx.grp(G_MAP_LINE_ID, Rgx.rep1("\\d")),
					Rgx.grp(G_MAP_HIERARCHY, Rgx.rep1("\\d")),
					Rgx.grp(G_MAP_NAME, Rgx.rep1("[^ .]")),
					Rgx.rep0(".")),
			Rgx.rep0(" "))));
	
	public static final Pattern P_REF_TRIGGER = Pattern.compile(Rgx.line(Rgx.seq(
			Rgx.rep0(" "),
			Rgx.or("Defined", "Count"),
			Rgx.rep1(" "),
			"Cross-reference of ",
			Rgx.grp(G_PARSE_MODE, Rgx.or(PM_DATA_NAMES, PM_PROCEDURES, PM_PROGRAMS, PM_VERBS)),
			Rgx.rep1(" "),
			"References",
			Rgx.rep0(" "))));
	private static final String R_REF = Rgx.grp(G_REF,Rgx.seq(
			Rgx.grp(G_REF_USAGE, Rgx.rep01("[A-Z]")),
			Rgx.grp(G_REF_LINE_ID, Rgx.rep1("\\d"))));
	public static final Pattern P_REF = Pattern.compile(R_REF, Pattern.CASE_INSENSITIVE);
	public static final Pattern P_REFS = Pattern.compile(
			Rgx.line(Rgx.seq( // 2 cases: Definition with LineId, Name and references; Continuation with references only
					Rgx.or(
							Rgx.seq( // Definition
									Rgx.rep(0, 9, " "),
									Rgx.grp(G_DEF_LINE_ID, Rgx.or(
											C_EXTERNAL,
											Rgx.rep1("\\d"))),
									Rgx.rep1(" "),
									Rgx.grp(G_DEF_NAME, Rgx.rep1("[^ .]")),
									Rgx.rep1("[ .]")),
							Rgx.rep(40, 42, " ")), // No definition: continuation of previous line
					Rgx.grp(G_REFS, Rgx.rep0(Rgx.seq(
							" ",
							R_REF))))), // References
			Pattern.CASE_INSENSITIVE);
	public static final Pattern P_PROCEDURE = Pattern.compile(
			Rgx.start(Rgx.seq("  ", Rgx.rep(21, 21, "."), Rgx.repN(1, " "), CblConstants.KW_PROCEDURE)),
			Pattern.CASE_INSENSITIVE);
	
	private static final List<Object> emptyContext = Arrays.asList(null, null, null);
	
	public static final void generateFlows(final List<List<Object>> ops, final List<List<Object>> flows) {
		final var context = new ArrayList<>(emptyContext);
		final var srcs = new LinkedHashSet<>();
		final var dsts = new LinkedHashSet<>();
		
		ops.forEach(row -> {
			final var lineId = row.get(0);
			final var obj = row.get(3);
			
			if (!lineId.equals(context.get(0)) && "".equals(obj)) {
				updateFlows(context, srcs, dsts, flows);
				
				dsts.clear();
				srcs.clear();
				context.clear();
				context.addAll(row.subList(0, 3));
			}
			
			final var op = row.get(4);
			
			if (U_READ.equals(op)) {
				srcs.add(obj);
			} else if (U_MODIFY.equals(op)) {
				dsts.add(obj);
			}
		});
		
		updateFlows(context, srcs, dsts, flows);
	}
	
	private static final void updateFlows(final List<Object> context, final Iterable<Object> srcs,
			final Iterable<Object> dsts, final List<List<Object>> flows) {
		final var verb = context.get(2);
		final var verbIsValid = null != verb && !"".equals(verb);
		
		if (verbIsValid) {
			srcs.forEach(src -> {
				dsts.forEach(dst -> {
					final var move = new ArrayList<>(context.subList(1, 3));
					move.add(src);
					move.add(dst);
					flows.add(move);
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
		
		private final String type;
		
		private final String name;
		
		private final Collection<Ref> refs = new ArrayList<>();
		
		public Def(final int id, final String type, final String name) {
			super(id);
			this.type = type;
			this.name = name;
		}
		
		public final String getType() {
			return this.type;
		}
		
		public final String getName() {
			return this.name;
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
	
}