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
import java.util.function.Consumer;
import java.util.function.Function;

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
		Log.outf(0, " %s<%s>", ARG_OUT, ap.getString(ARG_OUT));
		
		Log.beginf(0, "Processing");
		try (final var out = getPrintStream(ap, ARG_OUT)) {
			final var gvp = new GvPrinter(out);
			final var propDelimiter = ap.getString(ARG_DELIMITER2);
			final var reorg = ap.<GvReorg>getEnum(ARG_REORG);
			final var caseSensitive = ap.getBoolean(ARG_CASE_SENSITIVE);
			
			gvp.begin(ap.getBoolean(ARG_STRICT));
			gvp.graphPropLayout(ap.getString(ARG_LAYOUT));
			gvp.graphPropCompound(ap.getBoolean(ARG_COMPOUND));
			gvp.graphPropRankdir(ap.getString(ARG_RANKDIR));
			gvp.graphPropRanksep(ap.getDouble(ARG_RANKSEP));
			
			try {
				final var graph = new GvGraph();
				final var linkParser = new GvLink.GvLinkParser(graph, propDelimiter, caseSensitive);
				
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
	
	/**
	 * @author 2oLDNncs 20251010
	 */
	public static enum GvReorg {
		
		OFF, FOLD;
		
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
		
		public GvPath(final List<GvCluster> nest, final int id) {
			this.nest = nest;
			this.id = id;
			final var lastCluster = Helpers.last(nest);
			this.lastName = lastCluster.getKey().getName();
			this.compNode = lastCluster.getComponent();
			
//			Log.outf(0, "%s %s", this, nest.stream().map(GvCluster::getComponent).collect(Collectors.toSet()));
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
		
		private final CompNode mainCompNode = new CompNode() {};
		
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
			this.links.add(link);
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
		
		public final Map<String, String> findPathProps(final GvPath path) {
			final var result = new LinkedHashMap<String, String>();
			final var nest = path.getNest();
			
			for (var j = nest.size(); 0 < j; j -= 1) {
				for (var i = j - 1; 0 <= i; i -= 1) {
					final var p = this.findPath(nest.subList(i, j));
					
					if (null != p) {
						p.getProps().forEach((k, v) -> result.computeIfAbsent(k, __ -> v));
					}
				}
			}
			
			return result;
		}
		
		private final void prepareClusterStack(final List<GvCluster> clusterStack) {
			final var top = clusterStack.get(0);
			
			for (final var link : top.links) {
				if (!link.dstNest.isEmpty() && !top.equals(link.dstNest.get(0))) {
					clusterStack.add(0, link.dstNest.get(0));
					this.prepareClusterStack(clusterStack);
					clusterStack.remove(0);
				}
				
				final var implicitLink = link.dup();
				
				for (final var c : clusterStack) {
					if (!implicitLink.srcNest.contains(c)) {
						implicitLink.srcNest.add(0, c);
					}
					
					if (!implicitLink.dstNest.contains(c)) {
						implicitLink.dstNest.add(0, c);
					}
				}
				
				this.prepareLink(implicitLink);
			}
		}
		
		public final void prepareLink(final GvLink link) {
			if (link.included) {
				link.connectTo(this.getMainCompNode());
			}
			
			if (link.hasDst()) {
				this.addPath(link.dstNest);
				
				if (link.hasSrc()) {
					this.addPath(link.srcNest);
					
					this.gvLinks
					.computeIfAbsent(this.getPath(link.srcNest), __ -> new LinkedHashMap<>())
					.computeIfAbsent(this.getPath(link.dstNest), __ -> new LinkedHashMap<>())
					.putAll(link.props);
				}
			}
		}
		
		private final void addPath(final List<GvCluster> path) {
			Map<GvPath, Object> t = this.gvTrees;
			final var p = new ArrayList<GvCluster>();
			
			for (final var current : path) {
				p.add(current);
				t = Helpers.cast(t.computeIfAbsent(this.getPath(new ArrayList<>(p)), __ -> new LinkedHashMap<>()));
			}
		}
		
		public static final String PROP_KEY_DEFCLASS = "$defclass";
		public static final String PROP_KEY_CLASS = "$class";
		
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
			trees.forEach((path, content) -> {
				if (!path.getCompNode().isConnectedTo(graph.getMainCompNode())) {
					return;
				}
				
				final var subtrees = Helpers.<Map<GvPath, Object>>cast(content);
				final var pathId = path.getId();
				final var pathProps = graph.findPathProps(path);
				final var indent = String.join("", Collections.nCopies(path.getNest().size(), "\t"));
				
				graph.evalProps(pathProps);
				pathProps.computeIfAbsent("label", __ -> path.getLastName().toString());
				
				if (!subtrees.isEmpty()) {
					this.out.println(String.format("%ssubgraph cluster_%s {", indent, pathId));
					pathProps.forEach((propKey, propValue) -> {
						this.out.println(String.format("%s\t%s=%s", indent, propKey, GraphvizPrinter.formatPropValue(propValue)));
					});
					this.out.println(String.format("%s\t%s [label=\"\",shape=point,width=0,height=0]", indent, pathId));
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
		private final CompNode component = new CompNode() {};
		
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
		
		public final void connectTo(final CompNode component) {
			component.connectTo(this.component);
		}
		
		public final CompNode getComponent() {
			return this.component.findRoot();
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
			this.srcNest.addAll(that.srcNest);
			this.dstNest.addAll(that.dstNest);
			this.props.putAll(that.props);
			this.included = that.included;
		}
		
		public final void tryParentSrcToDst() {
			if (this.hasSrc() && this.hasDst()) {
				if (!this.srcNest.get(0).equals(this.dstNest.get(0))) {
					this.srcNest.get(0).addChild(this.dstNest.get(0));
				}
			}
		}
		
		public final boolean hasSrc() {
			return !this.srcNest.isEmpty();
		}
		
		public final boolean hasDst() {
			return !this.dstNest.isEmpty();
		}
		
		public final GvLink dup() {
			return new GvLink(this);
		}
		
		public final void reorgDirBack() {
			for (final var propIt = this.props.entrySet().iterator(); propIt.hasNext();) {
				final var prop = propIt.next();
				
				if ("dir".equals(prop.getKey()) && "back".equals(prop.getValue())) {
					var tmp = this.srcNest;
					this.srcNest = this.dstNest;
					this.dstNest = tmp;
					propIt.remove();
					break;
				}
			}
		}
		
		public final void connectTo(final CompNode compNode) {
			connectTo(compNode, this.srcNest);
			connectTo(compNode, this.dstNest);
		}
		
		@Override
		public final String toString() {
			return String.format("%s -> %s %s", this.srcNest, this.dstNest, this.props);
		}
		
		private static final void connectTo(final CompNode compNode, final Iterable<GvCluster> nest) {
			for (final var cluster : nest) {
				cluster.connectTo(compNode);
			}
		}
		
		/**
		 * @author 2oLDNncs 20251022
		 */
		public static final class GvLinkParser {
			
			private final GvGraph graph;
			
			private final String propDelimiter;
			
			private final Function<String, CharSequence> makeName;
			
			private final List<GvSource> sources = new ArrayList<>();
			
			public GvLinkParser(final GvGraph graph, final String propDelimiter, final boolean caseSensitive) {
				this.graph = graph;
				this.propDelimiter = propDelimiter;
				this.makeName = caseSensitive ? String.class::cast : CaseInsensitiveCharSequence::new;
			}
			
			public final GvLink parse(final String[] row, final boolean included) {
				final var result = new GvLink(included);
				final var n = row.length / 2;
				
				final var srcNode = Arrays.asList(Arrays.copyOfRange(row, 0, n));
				final var dstNode = Arrays.asList(Arrays.copyOfRange(row, n, 2 * n));
				
				for (var i = 0; i < n; i += 1) {
					this.updatePath(result.srcNest, srcNode, i);
					this.updatePath(result.dstNest, dstNode, i);
					
					CompNode component = null;
					
					for (final var cluster : result.srcNest) {
						if (null == component) {
							component = cluster.getComponent();
						} else {
							cluster.connectTo(component);
						}
					}
					
					for (final var cluster : result.dstNest) {
						if (null == component) {
							component = cluster.getComponent();
						} else {
							cluster.connectTo(component);
						}
					}
				}
				
				final var hasProps = ((row.length & 1) != 0) && !Helpers.last(row).isEmpty();
				
				if (hasProps) {
					this.parseProps(row, result);
				}
				
				if (result.hasSrc()) {
					if (result.hasDst()) {
						result.srcNest.get(0).links.add(result);
					} else {
						this.graph.getPath(result.srcNest).getProps().putAll(result.props);
					}
				} else if (result.hasDst()) {
					this.graph.getPath(result.dstNest).getProps().putAll(result.props);
				}
				
				return result;
			}
			
			private final void parseProps(final String[] row, final GvLink result) {
				for (final var prop : Helpers.last(row).split(this.propDelimiter)) {
					final var kv = prop.split("=", 2);
					
					if (2 != kv.length) {
						throw new IllegalArgumentException(String.format("Invalid prop: %s", prop));
					}
					
					result.props.put(kv[0], kv[1]);
				}
				
				this.graph.parseProps(result.props);
				
				this.evalIncludes(result);
			}
			
			private final void evalIncludes(final GvLink result) {
				final var includeFile = result.props.getOrDefault(PROP_KEY_INCLUDE_FILE, "");
				final var includeSheet = result.props.getOrDefault(PROP_KEY_INCLUDE_SHEET, "");
				
				if (includeFile.isBlank() && includeSheet.isBlank()) {
					return;
				}
				
				final var includeAll = parseBoolean(result.props.getOrDefault(PROP_KEY_INCLUDE_ALL, Boolean.FALSE.toString()));
				
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
					
					try {
						processRows(source.getFile(), source.getSheetName(), "\t", incRow -> {
							this.graph.addLink(this.parse(incRow, source.isIncludingAll()));
						});
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			private final void updatePath(final List<GvCluster> path, final List<String> node, final int i) {
				final var name = node.get(i);
				
				if (!name.isEmpty()) {
					path.add(this.graph.getCluster(node.size() - 1 - i, this.makeName.apply(name)));
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
