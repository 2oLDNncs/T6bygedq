package t6bygedq.lib.cbl;

import static t6bygedq.lib.Helpers.dprintlnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import t6bygedq.lib.Helpers;
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
	private String parseMode = null;
	
	public final void parse(final String line) {
		dprintlnf("%s", line);
		
		Matcher matcher = null;
		
		if (null == matcher) {
			matcher = P_TRIGGER.matcher(line);
			
			if (matcher.matches()) {
				this.parseMode = matcher.group(G_PARSE_MODE);
				dprintlnf("--> TRIGGER (%s)", this.parseMode);
			} else {
				matcher = null;
			}
		}
		
		if (null == matcher && null != this.parseMode) {
			matcher = P_REFS.matcher(line);
			
			if (matcher.matches()) {
				if (null != matcher.group(G_DEF_LINE_ID)) {
					this.addNewDef(matcher);
				}
				
				if (null != this.currentDef) {
					parseRefs(matcher.group(G_REFS), this.currentDef);
					
					dprintlnf("--> REFS (%s) (%s) %s",
							this.currentDef.getId(),
							this.currentDef.getName(),
							this.currentDef.getRefs());
				}
			} else {
				matcher = null;
			}
		}
		
		if (null == matcher) {
			matcher = P_PROCEDURE.matcher(line);
			
			if (matcher.matches()) {
//				TODO Determine lineId
//				this.addNewDef(this.procedures, sLineId, CblConstants.KW_PROCEDURE);
			} else {
				matcher = null;
			}
		}
	}
	
	private final void addNewDef(final Matcher matcher) {
		final Map<Integer, Def> target = this.selectParseTarget();
		
		if (null != target) {
			final String sLineId = matcher.group(G_DEF_LINE_ID);
			final String name = matcher.group(G_DEF_NAME);
			
			this.addNewDef(target, sLineId, name);
		}
	}
	
	private final void addNewDef(final Map<Integer, Def> target, final String sLineId, final String name) {
		final int lineId;
		
		if (C_EXTERNAL.equals(sLineId) || PM_VERBS.equals(this.parseMode)) {
			lineId = -target.size() - 1;
		} else {
			lineId = Integer.parseInt(sLineId);
		}
		
		this.currentDef = new Def(lineId, this.parseMode, name);
		
		target.put(this.currentDef.getId(), this.currentDef);
	}
	
	private final Map<Integer, Def> selectParseTarget() {
		switch (this.parseMode) {
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
		
		for (int i = 1; i < ops.size(); i += 1) {
			final List<Object> previousRow = ops.get(i - 1);
			final List<Object> currentRow = ops.get(i);
			
			for (int j = 1; j < currentRow.size(); j += 1) {
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
	
	public static final Pattern P_TRIGGER = Pattern.compile(rgxLine(rgxSeq(
			rgxRep0(" "),
			rgxOr("Defined", "Count"),
			rgxRep1(" "),
			"Cross-reference of ",
			rgxGrp(G_PARSE_MODE, rgxOr(PM_DATA_NAMES, PM_PROCEDURES, PM_PROGRAMS, PM_VERBS)),
			rgxRep1(" "),
			"References",
			rgxRep0(" "))));
	private static final String R_REF = rgxGrp(G_REF,rgxSeq(
			rgxGrp(G_REF_USAGE, rgxRep01("[A-Z]")),
			rgxGrp(G_REF_LINE_ID, rgxRep1("\\d"))));
	public static final Pattern P_REF = Pattern.compile(R_REF, Pattern.CASE_INSENSITIVE);
	public static final Pattern P_REFS = Pattern.compile(
			rgxLine(rgxSeq( // 2 cases: Definition with LineId, Name and references; Continuation with references only
					rgxOr(
							rgxSeq( // Definition
									rgxRep(0, 9, " "),
									rgxGrp(G_DEF_LINE_ID, rgxOr(
											C_EXTERNAL,
											rgxRep1("\\d"))),
									rgxRep1(" "),
									rgxGrp(G_DEF_NAME, rgxRep1("[^ .]")),
									rgxRep1("[ .]")),
							rgxRep(40, 42, " ")), // No definition: continuation of previous line
					rgxGrp(G_REFS, rgxRep0(rgxSeq(
							" ",
							R_REF))))), // References
			Pattern.CASE_INSENSITIVE);
	public static final Pattern P_PROCEDURE = Pattern.compile(
			rgxStart(rgxSeq("  ", rgxRep(21, 21, "."), rgxRepN(1, " "), CblConstants.KW_PROCEDURE)),
			Pattern.CASE_INSENSITIVE);
	
	private static final String rgxRep01(final String regex) {
		return rgxRep(0, 1, regex);
	}
	
	private static final String rgxRep0(final String regex) {
		return rgxRepN(0, regex);
	}
	
	private static final String rgxRep1(final String regex) {
		return rgxRepN(1, regex);
	}
	
	private static final String rgxRepN(final int min, final String regex) {
		return rgxRep(min, Integer.MAX_VALUE, regex);
	}
	
	private static final String rgxRep(final int min, final int max, final String regex) {
		final String grp = rgxNcgrp(regex);
		
		if (Integer.MAX_VALUE == max) {
			if (0 == min) {
				return String.format("%s*", grp);
			}
			
			if (1 == min) {
				return String.format("%s+", grp);
			}
			
			return String.format("%s{%s,}", grp, min);
		}
		
		if (0 == min && 1 == max) {
			return String.format("%s?", grp);
		}
		
		if (min == max) {
			return String.format("%s{%s}", grp, min);
		}
		
		return String.format("%s{%s,%s}", grp, min, max);
	}
	
	/**
	 * Full line
	 */
	private static final String rgxLine(final String regex) {
		return rgxStart(rgxEnd(regex));
	}
	
	/**
	 * Line start
	 */
	private static final String rgxStart(final String regex) {
		return String.format("^%s", regex);
	}
	
	/**
	 * Line end
	 */
	private static final String rgxEnd(final String regex) {
		return String.format("%s$", regex);
	}
	
	/**
	 * Sequence
	 */
	private static final String rgxSeq(final String... regexes) {
		return String.join("", regexes);
	}
	
	/**
	 * Union
	 */
	private static final String rgxOr(final String... regexes) {
		return rgxNcgrp(String.join("|", Arrays.stream(regexes)
				.map(CblXrefParser::rgxNcgrp)
				.toArray(String[]::new)));
	}
	
	/**
	 * Named capturing group
	 */
	private static final String rgxGrp(final String name, final String regex) {
		return String.format("(?<%s>%s)", name, regex);
	}
	
	/**
	 * Capturing group
	 */
	private static final String rgxGrp(final String regex) {
		return String.format("(%s)", regex);
	}
	
	/**
	 * Non-capturing group
	 */
	private static final String rgxNcgrp(final String regex) {
		return String.format("(?:%s)", regex);
	}
	
	private static final List<Object> emptyContext = Arrays.asList(null, null, null);
	
	public static final void generateFlows(final List<List<Object>> ops, final List<List<Object>> flows) {
		final List<Object> context = new ArrayList<>(emptyContext);
		final Collection<Object> srcs = new LinkedHashSet<>();
		final Collection<Object> dsts = new LinkedHashSet<>();
		
		ops.forEach(row -> {
			final Object lineId = row.get(0);
			final Object obj = row.get(3);
			
			if (!lineId.equals(context.get(0)) && "".equals(obj)) {
				updateFlows(context, srcs, dsts, flows);
				
				dsts.clear();
				srcs.clear();
				context.clear();
				context.addAll(row.subList(0, 3));
			}
			
			final Object op = row.get(4);
			
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
		final Object verb = context.get(2);
		final boolean verbIsValid = null != verb && !"".equals(verb);
		
		if (verbIsValid) {
			srcs.forEach(src -> {
				dsts.forEach(dst -> {
					final List<Object> move = new ArrayList<>(context.subList(1, 3));
					move.add(src);
					move.add(dst);
					flows.add(move);
				});
			});
		}
	}
	
	private static final void parseRefs(final String refs, final Def def) {
		final String[] refStrings = refs.split(" +");
		
		Arrays.stream(refStrings)
				.filter(refString -> !refString.isEmpty())
				.map(refString -> {
					final Matcher m = P_REF.matcher(refString);
					
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
	
}