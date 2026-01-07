package t6bygedq.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.CaseInsensitiveCharSequence;
import t6bygedq.lib.CompNode;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Log;

/**
 * @author 2oLDNncs 20250929
 */
public class DsvToGraphviz {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_SHEET = "-Sheet";
	public static final String ARG_DELIMITER1 = "-Delimiter1";
	public static final String ARG_DELIMITER2 = "-Delimiter2";
	public static final String ARG_STRICT = "-Strict";
	public static final String ARG_LAYOUT = "-Layout";
	public static final String ARG_COMPOUND = "-Compound";
	public static final String ARG_RANKDIR = "-Rankdir";
	public static final String ARG_RANKSEP = "-Ranksep";
	public static final String ARG_REORG = "-Reorg";
	public static final String ARG_CASE_SENSITIVE = "-CaseSensitive";
	public static final String ARG_COLUMNS = "-Columns";
	public static final String ARG_INVERT = "-Invert";
	public static final String ARG_DIR_BACK_HANDLING = "-DirBackHandling";
	public static final String ARG_OUT = "-Out";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_gv_dsv.txt");
		ap.setDefault(ARG_SHEET, "");
		ap.setDefault(ARG_DELIMITER1, "\t");
		ap.setDefault(ARG_DELIMITER2, "¤");
		ap.setDefault(ARG_STRICT, true);
		ap.setDefault(ARG_LAYOUT, "dot");
		ap.setDefault(ARG_COMPOUND, true);
		ap.setDefault(ARG_RANKDIR, "TB");
		ap.setDefault(ARG_RANKSEP, 0.75);
		ap.setDefault(ARG_REORG, GvReorg.OFF);
		ap.setDefault(ARG_CASE_SENSITIVE, true);
		ap.setDefault(ARG_COLUMNS, "");
//		ap.setDefault(ARG_COLUMNS, "10,-10,1,-1,2:,:2,-2:,:-2,1:2,2:1,-1:1,1:-1");
		ap.setDefault(ARG_INVERT, false);
		ap.setDefault(ARG_DIR_BACK_HANDLING, GvDirBackHandling.AUTO);
		ap.setDefault(ARG_OUT, "data/test_gv_dsv.gv");
		
		Log.outf(0, "%s.main", DsvToGraphviz.class.getSimpleName());
		Log.outf(0, " %s<%s>", ARG_IN, ap.getString(ARG_IN));
		Log.outf(0, " %s<%s>", ARG_SHEET, ap.getString(ARG_SHEET));
		Log.outf(0, " %s<%s>", ARG_DELIMITER1, ap.getString(ARG_DELIMITER1));
		Log.outf(0, " %s<%s>", ARG_DELIMITER2, ap.getString(ARG_DELIMITER2));
		Log.outf(0, " %s<%s>", ARG_STRICT, ap.getBoolean(ARG_STRICT));
		Log.outf(0, " %s<%s>", ARG_LAYOUT, ap.getString(ARG_LAYOUT));
		Log.outf(0, " %s<%s>", ARG_COMPOUND, ap.getBoolean(ARG_COMPOUND));
		Log.outf(0, " %s<%s>", ARG_RANKDIR, ap.getString(ARG_RANKDIR));
		Log.outf(0, " %s<%s>", ARG_RANKSEP, ap.getDouble(ARG_RANKSEP));
		Log.outf(0, " %s<%s>", ARG_REORG, ap.getEnum(ARG_REORG));
		Log.outf(0, " %s<%s>", ARG_CASE_SENSITIVE, ap.getBoolean(ARG_CASE_SENSITIVE));
		Log.outf(0, " %s<%s>", ARG_COLUMNS, ap.getString(ARG_COLUMNS));
		Log.outf(0, " %s<%s>", ARG_INVERT, ap.getBoolean(ARG_INVERT));
		Log.outf(0, " %s<%s>", ARG_DIR_BACK_HANDLING, ap.getEnum(ARG_DIR_BACK_HANDLING));
		Log.outf(0, " %s<%s>", ARG_OUT, ap.getString(ARG_OUT));
		
		Log.beginf(0, "Processing");
		try (final var out = getPrintStream(ap, ARG_OUT)) {
			final var gvp = new GvPrinter(out);
			final var propDelimiter = ap.getString(ARG_DELIMITER2);
			final var reorg = ap.<GvReorg>getEnum(ARG_REORG);
			final var caseSensitive = ap.getBoolean(ARG_CASE_SENSITIVE);
			final var columns = ap.getString(ARG_COLUMNS);
			final var invert = ap.getBoolean(ARG_INVERT);
			final var dirBackHandling = ap.<GvDirBackHandling>getEnum(ARG_DIR_BACK_HANDLING);
			
			gvp.begin(ap.getBoolean(ARG_STRICT));
			gvp.graphPropLayout(ap.getString(ARG_LAYOUT));
			gvp.graphPropCompound(ap.getBoolean(ARG_COMPOUND));
			gvp.graphPropRankdir(ap.getString(ARG_RANKDIR));
			gvp.graphPropRanksep(ap.getDouble(ARG_RANKSEP));
			
			try {
				final var graph = new GvGraph();
				final var linkParser = new GvLink.GvLinkParser(graph, propDelimiter,
						caseSensitive, columns, invert, dirBackHandling);
				
				linkParser.addSource(ap.getString(ARG_IN), ap.getString(ARG_SHEET), true);
				
				linkParser.processSources();
				
				graph.prepare(reorg);
				
				gvp.printGraph(graph);
			} finally {
				gvp.end();
			}
		}
		Log.done();
	}
	
	private static final CompNode newCompNode() {
		return new CompNode() {
			//pass
		};
	}
	
	/**
	 * @author 2oLDNncs 20251010
	 */
	public static enum GvReorg {
		
		OFF, FOLD;
		
	}
	
	/**
	 * @author 2oLDNncs 20260107
	 */
	public static enum GvDirBackHandling {
		
		AUTO, IGNORE, INVERT, SKIP;
		
	}
	
	/**
	 * @author 2oLDNncs 20251022
	 */
	public static final class GvPath {
		
		private final List<GvCluster> nest;
		
		private final int id;
		
		private final Map<String, String> props = new LinkedHashMap<>();
		
		private final CharSequence lastName;
		
		private final CompNode compNode;
		
		private boolean src;
		
		private boolean dst;
		
		public GvPath(final List<GvCluster> nest, final int id) {
			this.nest = nest;
			this.id = id;
			final var lastCluster = Helpers.last(nest);
			this.lastName = lastCluster.getKey().getName();
			this.compNode = lastCluster.getCompNode();
		}
		
		public final List<GvCluster> getNest() {
			return this.nest;
		}
		
		public final int getId() {
			return this.id;
		}
		
		public final Map<String, String> getProps() {
			return this.props;
		}
		
		public final CharSequence getLastName() {
			return this.lastName;
		}
		
		public final CompNode getCompNode() {
			return this.compNode;
		}
		
		public final boolean isSrc() {
			return this.src;
		}
		
		public final void setSrc(final boolean src) {
			this.src = src;
		}
		
		public final boolean isDst() {
			return this.dst;
		}
		
		public final void setDst(final boolean dst) {
			this.dst = dst;
		}
		
		@Override
		public final boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			
			final var that = GvPath.class.cast(obj);
			
			return null != that && this.getNest().equals(that.getNest());
		}
		
		@Override
		public final int hashCode() {
			return this.getNest().hashCode();
		}
		
		@Override
		public final String toString() {
			return this.getNest().toString();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20251019
	 */
	public static final class GvGraph {
		
		private final List<GvLink> links = new ArrayList<>();
		private final Map<GvClusterKey, GvCluster> clusters = new LinkedHashMap<>();
		private final Map<List<GvCluster>, GvPath> paths = new LinkedHashMap<>();
		
		private final Map<GvPath, Object> gvTrees = new LinkedHashMap<>();
		private final Map<GvPath, Map<GvPath, Map<String, String>>> gvLinks = new LinkedHashMap<>();
		
		private final Map<String, Map<String, String>> propClasses = new HashMap<>();
		
		private final CompNode mainCompNode = newCompNode();
		
		public final CompNode getMainCompNode() {
			return this.mainCompNode;
		}
		
		public final void parseProps(final Map<String, String> props) {
			final var propClassName = props.get(PROP_KEY_DEFCLASS);
			
			if (null != propClassName) {
				final var classProps = new LinkedHashMap<>(props);
				
				classProps.remove(PROP_KEY_DEFCLASS);
				
				this.propClasses.put(propClassName, classProps);
			}
		}
		
		public final void evalProps(final Map<String, String> props) {
			if (!props.containsKey(PROP_KEY_CLASS)) {
				return;
			}
			
			final var done = new HashSet<>();
			final var todo = new ArrayList<>(props.entrySet());
			
			props.clear();
			
			while (!todo.isEmpty()) {
				final var prop = todo.remove(0);
				
				if (PROP_KEY_CLASS.equals(prop.getKey())) {
					for (final var propClassName : prop.getValue().split(" +")) {
						if (done.add(propClassName)) {
							todo.addAll(this.propClasses.getOrDefault(propClassName, Collections.emptyMap()).entrySet());
						}
					}
				} else {
					props.computeIfAbsent(prop.getKey(), __ -> prop.getValue());
				}
			}
		}
		
		public final void addLink(final GvLink link) {
			if (null != link) {
				this.links.add(link);
			}
		}
		
		public final GvCluster getCluster(final int level, final CharSequence name) {
			return this.clusters.computeIfAbsent(new GvClusterKey(level, name), GvCluster::new);
		}
		
		public final GvPath findPath(final List<GvCluster> clusters) {
			return this.paths.get(clusters);
		}
		
		public final GvPath getPath(final List<GvCluster> nest) {
			return this.paths.computeIfAbsent(nest, k -> new GvPath(k, this.paths.size() + 1));
		}
		
		public final void prepare(final GvReorg reorg) {
			if (GvReorg.FOLD == reorg) {
				this.fold();
				this.reorgDirBack();
				this.preparePaths();
			} else {
				this.prepapreLinks();
			}
			
			this.classifyLinksEndpoints();
			this.prepareTrees(this.gvTrees);
		}
		
		private final void fold() {
			this.links.forEach(GvLink::tryParentSrcToDst);
		}
		
		private final void reorgDirBack() {
			this.links.forEach(GvLink::reorgDirBack);
		}
		
		private final void preparePaths() {
			this.clusters.values().stream()
			.filter(GvCluster::isRoot)
			.map(Helpers::newList)
			.forEach(this::prepareClusterStack);
		}
		
		private final void prepapreLinks() {
			this.links.forEach(this::prepareLink);
		}
		
		private final void classifyLinksEndpoints() {
			this.gvLinks.forEach((src, dsts) -> {
				dsts.forEach((dst, props) -> {
					if (GvLink.PROP_VAL_BACK.equals(props.get(GvLink.PROP_KEY_DIR))) {
						src.setDst(true);
						dst.setSrc(true);
					} else {
						src.setSrc(true);
						dst.setDst(true);
					}
				});
			});
		}
		
		private final void prepareTrees(final Map<GvPath, Object> trees) {
			this.forEachConnectedPath(trees, (path, subtrees) -> {
				GvGraph.updatePropClassTopo(path);
				this.prepareTrees(subtrees);
			});
		}
		
		public final void forEachConnectedPath(final Map<GvPath, Object> trees, BiConsumer<GvPath, Map<GvPath, Object>> action) {
			trees.forEach((path, content) -> {
				if (path.getCompNode().isConnectedTo(this.getMainCompNode())) {
					action.accept(path, Helpers.<Map<GvPath, Object>>cast(content));
				}
			});
		}
		
		public final Map<String, String> findPathProps(final GvPath path) {
			final var result = new LinkedHashMap<String, String>();
			final var nest = path.getNest();
			
			for (var j = nest.size(); 0 < j; j -= 1) {
				for (var i = j - 1; 0 <= i; i -= 1) {
					final var p = this.findPath(nest.subList(i, j));
					
					if (null != p) {
						p.getProps().forEach((k, v) -> {
							if (p.equals(path) || !"label".equals(k)) {
								result.computeIfAbsent(k, __ -> v);
							}
						});
					}
				}
			}
			
			return result;
		}
		
		private final void prepareClusterStack(final List<GvCluster> clusterStack) {
			final var top = clusterStack.get(0);
			
			for (final var link : top.links) {
				if (link.hasDst()) {
					final var dst0 = link.getDstNest().get(0);
					
					if (!top.equals(dst0)) {
						if (clusterStack.contains(dst0)) {
							Log.errf(0, "Cycle detected: %s -> %s", dst0, clusterStack);
						} else {
							clusterStack.add(0, dst0);
							this.prepareClusterStack(clusterStack);
							clusterStack.remove(0);
						}
					}
				}
				
				final var implicitLink = link.dup();
				
				for (final var c : clusterStack) {
					pushFrontIfAbsent(implicitLink.getSrcNest(), c);
					pushFrontIfAbsent(implicitLink.getDstNest(), c);
				}
				
				this.prepareLink(implicitLink);
			}
		}
		
		public final void prepareLink(final GvLink link) {
			if (link.isIncluded()) {
				link.connectCompNode(this.getMainCompNode());
			}
			
			if (link.hasDst()) {
				this.addPath(link.getDstNest());
				
				if (link.hasSrc()) {
					this.addPath(link.getSrcNest());
					
					this.gvLinks
					.computeIfAbsent(this.getPath(link.getSrcNest()), GvGraph::newLinkedHashMap)
					.computeIfAbsent(this.getPath(link.getDstNest()), GvGraph::newLinkedHashMap)
					.putAll(link.getProps());
				}
			}
		}
		
		private final void addPath(final List<GvCluster> path) {
			Map<GvPath, Object> t = this.gvTrees;
			final var p = new ArrayList<GvCluster>();
			
			for (final var current : path) {
				p.add(current);
				t = Helpers.cast(t.computeIfAbsent(this.getPath(new ArrayList<>(p)), GvGraph::newLinkedHashMap));
			}
		}
		
		public static final String PROP_KEY_DEFCLASS = "$defclass";
		public static final String PROP_KEY_CLASS = "$class";
		public static final String PROP_VAL_SOURCE = "$source";
		public static final String PROP_VAL_SINK = "$sink";
		public static final String PROP_VAL_ISOLATED = "$isolated";
		public static final String PROP_VAL_BETWEEN = "$between";
		
		private static final Pattern P_PROP_CLASS_TOPO = Pattern.compile(
				"(^| )(" + String.join("|", patternQuote(PROP_VAL_SOURCE, PROP_VAL_SINK, PROP_VAL_ISOLATED, PROP_VAL_BETWEEN)) + ")( |$)");
		
		private static final String[] patternQuote(final String... strings) {
			return Arrays.stream(strings)
					.map(Pattern::quote)
					.toArray(String[]::new);
		}
		
		private static final void updatePropClassTopo(final GvPath path) {
			if (path.isSrc()) {
				if (path.isDst()) {
					addPropClassTopo(path, PROP_VAL_BETWEEN);
				} else {
					addPropClassTopo(path, PROP_VAL_SOURCE);
				}
			} else {
				if (path.isDst()) {
					addPropClassTopo(path, PROP_VAL_SINK);
				} else {
					addPropClassTopo(path, PROP_VAL_ISOLATED);
				}
			}
		}
		
		private static final void addPropClassTopo(final GvPath path, final String propVal) {
			path.getProps().compute(PROP_KEY_CLASS, (__, v) -> null == v ? propVal :
				(P_PROP_CLASS_TOPO.matcher(v).find() ? v : v + " " + propVal));
		}
		
		private static final <E> void pushFrontIfAbsent(final List<E> list, final E element) {
			if (!list.contains(element)) {
				list.add(0, element);
			}
		}
		
		private static final <K1, K2, V2> Map<K2, V2> newLinkedHashMap(final K1 __) {
			return new LinkedHashMap<>();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20251022
	 */
	public static final class GvPrinter {
		
		private final PrintStream out;
		
		private int state = 1;
		
		public GvPrinter(final PrintStream out) {
			this.out = out;
		}
		
		public final void begin(final boolean strict) {
			this.checkState(0b0001);
			this.printlnf("%sdigraph G { // Use strict to merge duplicate edges", strict ? "strict " : "");
			this.nextState();
		}
		
		public final void graphPropLayout(final String layout) {
			this.graphProp("layout", layout, "Use dot for clustering");
		}
		
		public final void graphPropCompound(final boolean compound) {
			this.graphProp("compound", compound, "If true, allow edges between clusters");
		}
		
		public final void graphPropRankdir(final String rankdir) {
			this.graphProp("rankdir", rankdir, "TB (Top-Bottom) or LR (Left-Right)");
		}
		
		public final void graphPropRanksep(final double ranksep) {
			this.graphProp("ranksep", ranksep, "In dot, sets the desired rank separation, in inches");
		}
		
		public final void graphProp(final String key, final Object value, final String comment) {
			this.checkState(0b0010);
			this.printlnf("	%s=%s // %s", key, value, comment);
		}
		
		public final void end() {
			this.checkState(0b0110);
			this.out.println("}");
			this.nextState();
		}
		
		private final void printlnf(final String format, final Object... args) {
			this.out.println(String.format(format, args));
		}
		
		private final void checkState(final int validStates) {
			if (0 == (validStates & this.state)) {
				throw new IllegalStateException(Integer.toBinaryString(this.state));
			}
		}
		
		private final void nextState() {
			this.state <<= 1;
		}
		
		public final void printGraph(final GvGraph graph) {
			this.checkState(0b0010);
			this.printLinks(graph);
			this.printTrees(graph);
			this.nextState();
		}
		
		private final void printLinks(final GvGraph graph) {
			graph.gvLinks.forEach((src, dsts) -> {
				final var srcId = src.getId();
				dsts.forEach((dst, props) -> {
					final var dstId = dst.getId();
					
					if (src.getCompNode().isConnectedTo(graph.getMainCompNode())
							|| dst.getCompNode().isConnectedTo(graph.getMainCompNode())) {
						graph.evalProps(props);
						
						this.out.println(String.format("\t%s -> %s [ltail=cluster_%s,lhead=cluster_%s,%s]",
								srcId, dstId, srcId, dstId,
								GraphvizPrinter.formatProps(props)));
					}
				});
			});
		}
		
		private final void printTrees(final GvGraph graph) {
			this.printTrees(graph, graph.gvTrees);
		}
		
		private final void printTrees(final GvGraph graph, final Map<GvPath, Object> trees) {
			graph.forEachConnectedPath(trees, (path, subtrees) -> {
				final var pathProps = graph.findPathProps(path);
				
				graph.evalProps(pathProps);
				pathProps.computeIfAbsent("label", __ -> path.getLastName().toString());
				
				final var pathId = path.getId();
				final var indent = String.join("", Collections.nCopies(path.getNest().size(), "\t"));
				
				if (!subtrees.isEmpty()) {
					this.out.println(String.format("%ssubgraph cluster_%s {", indent, pathId));
					pathProps.forEach((propKey, propValue) -> {
						this.out.println(String.format("%s\t%s=%s", indent, propKey, GraphvizPrinter.formatPropValue(propValue)));
					});
//					this.out.println(String.format("%s\t%s [label=\"\",shape=point,width=0,height=0]", indent, pathId));
					this.out.println(String.format("%s\t%s [label=\"%s\""
							+ ",fixedsize=true,width=0,height=0"
							+ ",fontsize=0,fontcolor=\"#00000000\""
							+ ",penwidth=0,color=\"#00000000\"]",
							indent, pathId, pathProps.get("label")));
					this.printTrees(graph, subtrees);
					this.out.println(String.format("%s}", indent));
				} else {
					this.out.println(String.format("%s\t%s [%s]", indent, pathId,
							GraphvizPrinter.formatProps(pathProps)));
				}
			});
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20251014
	 */
	private static final class GvCluster {
		
		private final GvClusterKey key;
		private final Collection<GvCluster> parents = new LinkedHashSet<>();
		private final Collection<GvCluster> children = new LinkedHashSet<>();
		private final Collection<GvLink> links = new LinkedHashSet<>();
		private final CompNode compNode = newCompNode();
		
		public GvCluster(final GvClusterKey key) {
			this.key = key;
		}
		
		public final GvClusterKey getKey() {
			return this.key;
		}
		
		public final void addChild(final GvCluster child) {
			this.children.add(child);
			child.parents.add(this);
		}
		
		public final boolean isRoot() {
			return this.parents.isEmpty();
		}
		
		public final void connectCompNode(final GvCluster that) {
			this.getCompNode().connectTo(that.getCompNode());
		}
		
		public final void connectCompNode(final CompNode compNode) {
			compNode.connectTo(this.compNode);
		}
		
		public final CompNode getCompNode() {
			return this.compNode;
		}
		
		@Override
		public final int hashCode() {
			return this.getKey().hashCode();
		}
		
		@Override
		public final boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			
			final var that = GvCluster.class.cast(obj);
			
			return null != that && this.getKey().equals(that.getKey());
		}
		
		@Override
		public final String toString() {
			return this.getKey().toString();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20251022
	 */
	public static final class GvClusterKey {
		
		private final int level;
		
		private final CharSequence name;
		
		private final int hashCode;
		
		public GvClusterKey(final int level, final CharSequence name) {
			this.level = level;
			this.name = Objects.requireNonNull(name);
			this.hashCode = Objects.hash(level, name);
		}
		
		public final int getLevel() {
			return this.level;
		}
		
		public final CharSequence getName() {
			return this.name;
		}
		
		@Override
		public final int hashCode() {
			return this.hashCode;
		}
		
		@Override
		public final boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			final var that = GvClusterKey.class.cast(obj);
			
			return null != that
					&& this.level == that.level
					&& this.getName().equals(that.getName());
		}
		
		@Override
		public final String toString() {
			return String.format("[%s %s]", this.getLevel(), this.getName());
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20251011
	 */
	private static final class GvLink {
		
		private List<GvCluster> srcNest = new ArrayList<>();
		private List<GvCluster> dstNest = new ArrayList<>();
		private final Map<String, String> props = new LinkedHashMap<>();
		private final boolean included;
		
		private GvLink(final boolean included) {
			this.included = included;
		}
		
		private GvLink(final GvLink that) {
			this.getSrcNest().addAll(that.getSrcNest());
			this.getDstNest().addAll(that.getDstNest());
			this.getProps().putAll(that.getProps());
			this.included = that.included;
		}
		
		public final List<GvCluster> getSrcNest() {
			return this.srcNest;
		}
		
		public final List<GvCluster> getDstNest() {
			return this.dstNest;
		}
		
		public final Map<String, String> getProps() {
			return this.props;
		}
		
		public final boolean isIncluded() {
			return this.included;
		}
		
		public final void tryParentSrcToDst() {
			if (this.hasSrc() && this.hasDst()) {
				final var src0 = this.getSrcNest().get(0);
				final var dst0 = this.getDstNest().get(0);
				
				if (!src0.equals(dst0)) {
					src0.addChild(dst0);
				}
			}
		}
		
		public final boolean hasSrc() {
			return !this.getSrcNest().isEmpty();
		}
		
		public final boolean hasDst() {
			return !this.getDstNest().isEmpty();
		}
		
		public final GvLink dup() {
			return new GvLink(this);
		}
		
		public final void reorgDirBack() {
			for (final var propIt = this.getProps().entrySet().iterator(); propIt.hasNext();) {
				final var prop = propIt.next();
				
				if (PROP_KEY_DIR.equals(prop.getKey()) && PROP_VAL_BACK.equals(prop.getValue())) {
					var tmp = this.getSrcNest();
					this.srcNest = this.getDstNest();
					this.dstNest = tmp;
					propIt.remove();
					break;
				}
			}
		}
		
		public final void connectCompNode(final CompNode compNode) {
			connectCompNode(compNode, this.getSrcNest());
			connectCompNode(compNode, this.getDstNest());
		}
		
		@Override
		public final String toString() {
			return String.format("%s -> %s %s", this.getSrcNest(), this.getDstNest(), this.getProps());
		}
		
		private static final void connectCompNode(final CompNode compNode, final List<GvCluster> nest) {
			if (!nest.isEmpty()) {
				nest.get(0).connectCompNode(compNode);
			}
		}
		
		public static final String PROP_KEY_DIR = "dir";
		public static final String PROP_VAL_BACK = "back";
		
		/**
		 * @author 2oLDNncs 20251022
		 */
		public static final class GvLinkParser {
			
			private final GvGraph graph;
			
			private final String propDelimiter;
			
			private final Function<String, CharSequence> makeName;
			
			private final List<Function<Integer, IntStream>> columnsGenerators = new ArrayList<>();
			
			private int nodeMaxSize;
			
			private int[] columns;
			
			private final boolean invertAll;
			
			private boolean invertCurrent;
			
			private final GvDirBackHandling dirBackHandling;
			
			private final List<GvSource> sources = new ArrayList<>();
			
			public GvLinkParser(final GvGraph graph, final String propDelimiter,
					final boolean caseSensitive, final String columns, final boolean invert, final GvDirBackHandling dirBackHandling) {
				this.graph = graph;
				this.propDelimiter = propDelimiter;
				this.makeName = caseSensitive ? String.class::cast : CaseInsensitiveCharSequence::new;
				this.invertAll = invert;
				this.dirBackHandling = dirBackHandling;
				
				this.parseColumns(columns);
			}
			
			private final void parseColumns(final String columns) {
				if (!columns.isEmpty()) {
					for (final var interval : columns.split(",")) {
						final var bounds = interval.split(":", -1);
						
						if (1 == bounds.length) {
							final var b = Integer.parseInt(bounds[0]);
							
							this.columnsGenerators.add(n -> IntStream.of(b < 0 ? b + n : b));
						} else if (2 == bounds.length) {
							if (bounds[0].isEmpty()) {
								final var b1 = Integer.parseInt(bounds[1]);
								
								this.columnsGenerators.add(n -> IntStream.rangeClosed(0, b1 < 0 ? b1 + n : b1));
							} else if (bounds[1].isEmpty()) {
								final var b0 = Integer.parseInt(bounds[0]);
								
								this.columnsGenerators.add(n -> IntStream.range(b0 < 0 ? b0 + n : b0, n));
							} else {
								final var b0 = Integer.parseInt(bounds[0]);
								final var b1 = Integer.parseInt(bounds[1]);
								
								if (b0 < b1) {
									this.columnsGenerators.add(n -> IntStream.rangeClosed(b0, b1)
											.map(b -> b < 0 ? b + n : b));
								} else if (b1 < b0) {
									this.columnsGenerators.add(n -> IntStream.rangeClosed(b1, b0)
											.map(b -> b0 + b1 - b)
											.map(b -> b < 0 ? b + n : b));
								} else {
									this.columnsGenerators.add(n -> IntStream.of(b0 < 0 ? b0 + n : b0));
								}
							}
						} else {
							throw new IllegalArgumentException(String.format("Invalid column interval: %s", interval));
						}
					}
				}
			}
			
			public final GvLink parse(final String[] row, final boolean included) {
				this.invertCurrent = this.invertAll;
				
				final var result = new GvLink(included);
				
				final var hasProps = ((row.length & 1) != 0) && !Helpers.last(row).isEmpty();
				
				if (hasProps) {
					this.parseProps(Helpers.last(row), result);
					
					if (PROP_VAL_BACK.equals(result.getProps().get(PROP_KEY_DIR))) {
						switch (this.dirBackHandling) {
						case AUTO:
							break;
						case IGNORE:
							result.getProps().remove(PROP_KEY_DIR);
							break;
						case INVERT:
							result.getProps().remove(PROP_KEY_DIR);
							this.invertCurrent = !this.invertCurrent;
							break;
						case SKIP:
							return null;
						default:
							throw new IllegalStateException(String.format("dirBackHandling: %s", this.dirBackHandling));
						}
					}
				}
				
				this.initNests(row, result);
				
				if (result.hasSrc() && result.hasDst()) {
					result.getSrcNest().get(0).connectCompNode(result.getDstNest().get(0));
				}
				
				if (result.hasSrc()) {
					if (result.hasDst()) {
						result.getSrcNest().get(0).links.add(result);
					} else {
						this.graph.getPath(result.getSrcNest()).getProps().putAll(result.getProps());
					}
				} else if (result.hasDst()) {
					this.graph.getPath(result.getDstNest()).getProps().putAll(result.getProps());
				}
				
				return result;
			}
			
			private final void initNests(final String[] row, final GvLink link) {
				final var n = row.length / 2;
				final List<String> srcNode;
				final List<String> dstNode;
				
				if (this.columnsGenerators.isEmpty()) {
					srcNode = Arrays.asList(Arrays.copyOfRange(row, 0, n));
					dstNode = Arrays.asList(Arrays.copyOfRange(row, n, n + n));
				} else {
					if (n != this.nodeMaxSize) {
						this.nodeMaxSize = n;
						this.columns = this.columnsGenerators.stream()
								.flatMapToInt(f -> f.apply(n))
								.filter(i -> 0 <= i && i < n)
								.toArray();
						
						Log.out(0, "Columns:", Arrays.toString(this.columns));
					}
					
					srcNode = new ArrayList<>(this.columns.length);
					dstNode = new ArrayList<>(this.columns.length);
					
					for (final var i : this.columns) {
						srcNode.add(row[i]);
						dstNode.add(row[n + i]);
					}
				}
				
				final var nodeSize = srcNode.size();
				final var invertSrcDst = this.invertCurrent
						&& IntStream.range(0, n).anyMatch(i -> !row[i].isEmpty())
						&& IntStream.range(n, n + n).anyMatch(i -> !row[i].isEmpty());
				
				for (var i = 0; i < nodeSize; i += 1) {
					if (invertSrcDst) {
						this.updateNest(link.getSrcNest(), dstNode, i);
						this.updateNest(link.getDstNest(), srcNode, i);
					} else {
						this.updateNest(link.getSrcNest(), srcNode, i);
						this.updateNest(link.getDstNest(), dstNode, i);
					}
				}
			}
			
			private final void parseProps(final String props, final GvLink result) {
				for (final var prop : props.split(this.propDelimiter)) {
					final var kv = prop.split("=", 2);
					
					if (2 != kv.length) {
						throw new IllegalArgumentException(String.format("Invalid prop: %s", prop));
					}
					
					result.getProps().put(kv[0], kv[1]);
				}
				
				this.graph.parseProps(result.getProps());
				
				this.evalIncludes(result);
			}
			
			private final void evalIncludes(final GvLink result) {
				final var props = result.getProps();
				final var includeFile = props.getOrDefault(PROP_KEY_INCLUDE_FILE, "");
				final var includeSheet = props.getOrDefault(PROP_KEY_INCLUDE_SHEET, "");
				
				if (includeFile.isBlank() && includeSheet.isBlank()) {
					return;
				}
				
				final var includeAll = parseBoolean(props.getOrDefault(PROP_KEY_INCLUDE_ALL, Boolean.FALSE.toString()));
				
				this.addSource(includeFile, includeSheet, includeAll);
			}
			
			public final void addSource(final String fileName, final String sheetName, final boolean includeAll) {
				final GvSource source;
				
				if (this.sources.isEmpty()) {
					source = new GvSource(new File(fileName), sheetName, includeAll);
				} else {
					source = this.sources.get(0).newSource(fileName, sheetName, includeAll);
				}
				
				if (!source.isValid()) {
					Log.errf(0, "%s.addSource", this.getClass().getSimpleName());
					Log.errf(0, " %s<%s>", PROP_KEY_INCLUDE_FILE, fileName);
					Log.errf(0, " %s<%s>", PROP_KEY_INCLUDE_SHEET, sheetName);
					Log.errf(0, " %s<%s>", PROP_KEY_INCLUDE_ALL, includeAll);
					Log.errf(0, " -> Invalid %s", source);
					
					return;
				}
				
				if (!this.sources.contains(source)) {
					this.sources.add(source);
					
					if (1 < this.sources.size()) {
						Log.outf(0, "%s.evalIncludes", this.getClass().getSimpleName());
						Log.outf(0, " %s<%s>", PROP_KEY_INCLUDE_FILE, fileName);
						Log.outf(0, " %s<%s>", PROP_KEY_INCLUDE_SHEET, sheetName);
						Log.outf(0, " %s<%s>", PROP_KEY_INCLUDE_ALL, includeAll);
						Log.outf(0, " -> %s", source);
					}
				}
			}
			
			public final void processSources() {
				for (var i = 0; i < this.sources.size(); i += 1) {
					final var source = this.sources.get(i);
					
					Log.out(0, source);
					
					try {
						processRows(source.getFile(), source.getSheetName(), "\t", incRow -> {
							this.graph.addLink(this.parse(incRow, source.isIncludingAll()));
						});
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			private final void updateNest(final List<GvCluster> nest, final List<String> node, final int i) {
				final var name = node.get(i);
				
				if (!name.isEmpty()) {
					final var cluster = this.graph.getCluster(node.size() - 1 - i, this.makeName.apply(name));
					
					if (!nest.isEmpty()) {
						nest.get(0).connectCompNode(cluster);
					}
					
					nest.add(cluster);
				}
			}
			
			public static final String PROP_KEY_INCLUDE_FILE = "$include-file";
			public static final String PROP_KEY_INCLUDE_SHEET = "$include-sheet";
			public static final String PROP_KEY_INCLUDE_ALL = "$include-all";
			
			public static final boolean parseBoolean(final String string) {
				return Objects.requireNonNull(Boolean.parseBoolean(string),
						() -> String.format("Invalid value: %s", string));
			}
			
			/**
			 * @author 2oLDNncs 20251023
			 */
			public static final class GvSource {
				
				private final File file;
				
				private final String sheetName;
				
				private final int hashCode;
				
				private final boolean includingAll;
				
				public GvSource(final File file, final String sheetName, final boolean includingAll) {
					this.file = file;
					this.sheetName = Objects.requireNonNull(sheetName);
					this.hashCode = Objects.hash(
							null == file ? 0 : this.getFile().getAbsolutePath(),
							this.getSheetName());
					this.includingAll = includingAll;
				}
				
				public final File getFile() {
					return this.file;
				}
				
				public final String getSheetName() {
					return this.sheetName;
				}
				
				public final boolean isValid() {
					return null != this.getFile();
				}
				
				public final boolean isIncludingAll() {
					return this.includingAll;
				}
				
				public final GvSource newSource(final String fileName, final String sheetName, final boolean includeAll) {
					File file = null;
					
					if (!fileName.isBlank()) {
						file = new File(this.getFile().getParent(), fileName);
					} else if (!sheetName.isBlank() && !this.getSheetName().isBlank()) {
						file = this.getFile();
					}
					
					return new GvSource(file, sheetName, includeAll);
				}
				
				@Override
				public final int hashCode() {
					return this.hashCode;
				}
				
				@Override
				public final boolean equals(final Object obj) {
					if (this == obj) {
						return true;
					}
					
					final var that = GvSource.class.cast(obj);
					
					return null != that
							&& this.getFile().equals(that.getFile())
							&& this.getSheetName().equals(that.getSheetName());
				}
				
				@Override
				public String toString() {
					return "GvSource [file=" + this.getFile() + ", sheetName=" + this.getSheetName() + "]";
				}
				
			}
			
		}
				
	}
	
	private static final void processRows(final File file, final String sheetName, final String delimiter,
			final Consumer<String[]> action) throws IOException {
		if (!sheetName.isBlank()) {
			XSSFWorkbookToGraphviz.forEachRowInWorkbookSheet(file, sheetName, action);
		} else {
			forEachRowInDsv(file, delimiter, action);
		}
	}
	
	public static final void forEachRowInDsv(final File file, final String delimiter,
			final Consumer<String[]> action) throws IOException {
		Files.lines(file.toPath()).forEach(line -> {
			action.accept(line.split(delimiter, -1));
		});
	}
	
	public static final PrintStream getPrintStream(final ArgsParser ap, final String outKey) throws FileNotFoundException {
		if (!ap.getString(outKey).isBlank()) {
			return new PrintStream(ap.getFile(outKey));
		}
		
		return System.out;
	}
	
}
