package t6bygedq.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Log;

/**
 * @author 2oLDNncs 20250929
 */
public class DsvToGraphviz {
	
	/**
	 * @author 2oLDNncs 20251010
	 */
	public static enum Reorg {
		
		OFF, FOLD;
		
	}
	
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
		ap.setDefault(ARG_REORG, Reorg.OFF);
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
			final var propDelimiter = ap.getString(ARG_DELIMITER2);
			final var gvp = new GraphvizPrinter(out);
			final var reorg = ap.<Reorg>getEnum(ARG_REORG);
			
			gvp.begin(ap.getBoolean(ARG_STRICT), ap.getString(ARG_LAYOUT), ap.getBoolean(ARG_COMPOUND), ap.getString(ARG_RANKDIR));
			gvp.graphProp("ranksep", ap.getDouble(ARG_RANKSEP), "In dot, sets the desired rank separation, in inches");
			
			final Consumer<String[]> gvpAction = row -> processRow(row, propDelimiter, gvp);
			
			try {
				if (Reorg.FOLD == reorg) {
					final var graph = new Graph(propDelimiter);
					
					processRows(ap, row -> {
						graph.allLinks.add(new Link(graph, row));
					});
					
//					graph.allLinks.forEach(link -> gvpAction.accept(link.toRow()));
					graph.allLinks.forEach(Link::tryParentSrcToDst);
					graph.collectRoots();
					graph.updateLevels();
					graph.reorgDirBack();
					
					graph.roots.forEach(root -> graph.processClusterStack(newList(root), gvpAction));
					
					graph.allPaths.forEach(prefix -> {
						final var prefixProps = graph.findPathProps(prefix);
						
						if (!prefixProps.isEmpty()) {
							gvpAction.accept(graph.makeRow(prefix, Collections.emptyList(), prefixProps));
						}
					});
				} else {
					processRows(ap, gvpAction);
				}
			} finally {
				gvp.end();
			}
		}
		Log.done();
	}
	
	/**
	 * @author 2oLDNncs 20251019
	 */
	public static final class Graph {
		
		public final String propDelimiter;
		
		public Graph(final String propDelimiter) {
			this.propDelimiter = propDelimiter;
		}
		
		public final List<Link> allLinks = new ArrayList<>();
		public final Map<Object, Cluster> allClusters = new HashMap<>();
		public final Map<List<Cluster>, Map<String, String>> pathProps = new HashMap<>();
		public final Collection<List<Cluster>> allPaths = new LinkedHashSet<>();
		public final Collection<Cluster> roots = new LinkedHashSet<>();
		private int nodeSize = 0;
		
		public final int getNodeSize() {
			return this.nodeSize;
		}
		
		public final void setNodeSize(final int nodeSize) {
			if (0 == this.getNodeSize()) {
				this.nodeSize = nodeSize;
			} else if (this.getNodeSize() != nodeSize) {
				throw new IllegalArgumentException(String.format("Invalid node size: Expected %s Actual %s", this.getNodeSize(), nodeSize));
			}
		}
		
		public final void collectRoots() {
			this.allClusters.values().stream()
					.filter(Cluster::isRoot)
					.forEach(this.roots::add);
		}
		
		public final void updateLevels() {
			this.roots.forEach(Cluster::updateLevel);
			
			final var paddingSize = -this.roots.stream()
					.mapToInt(Cluster::getLevel)
					.min().getAsInt();
			this.nodeSize += paddingSize;
			
			// fix negative levels after update
			this.allClusters.values().forEach(cluster -> cluster.updateLevel(paddingSize));
		}
		
		public final void reorgDirBack() {
			this.allLinks.forEach(Link::reorgDirBack);
		}
		
		public final Map<String, String> findPathProps(final List<Cluster> path) {
			final var result = new LinkedHashMap<String, String>();
			
			for (var j = path.size(); 0 < j; j -= 1) {
				for (var i = j - 1; 0 <= i; i -= 1) {
					final var props = this.pathProps.get(path.subList(i, j));
					
					if (null != props) {
						props.forEach((k, v) -> result.computeIfAbsent(k, __ -> v));
					}
				}
			}
			
			return result;
		}
		
		public final String[] makeRow(final List<Cluster> srcPath, final List<Cluster> dstPath, final Map<String, String> props) {
			final var result = new String[2 * this.nodeSize + 1];
			
			Arrays.fill(result, "");
			
			for (var i = 0; i < srcPath.size(); i += 1) {
				result[srcPath.get(i).getLevel()] = srcPath.get(i).getKeyName();
			}
			
			for (var i = 0; i < dstPath.size(); i += 1) {
				result[this.nodeSize + dstPath.get(i).getLevel()] = dstPath.get(i).getKeyName();
			}
			
			result[result.length - 1] = props.entrySet().stream()
					.map(Object::toString)
					.reduce((e1, e2) -> e1 + this.propDelimiter + e2)
					.orElse("");
			
			return result;
		}
		
		public final void processClusterStack(final List<Cluster> clusterStack, final Consumer<String[]> action) {
			final var top = clusterStack.get(0);
			
			for (final var link : top.links) {
				if (!link.dstPath.isEmpty() && !top.equals(link.dstPath.get(0))) {
					clusterStack.add(0, link.dstPath.get(0));
					this.processClusterStack(clusterStack, action);
					clusterStack.remove(0);
				}
				
				final var tmp = link.dup();
				
				for (final var c : clusterStack) {
					tmp.srcPath.add(0, c);
					
					if (tmp.hasDst()) {
						tmp.dstPath.add(0, c);
					}
				}
				
				action.accept(tmp.toRow());
				
				if (!tmp.srcPath.isEmpty() && !tmp.dstPath.isEmpty()) {
					addAllPrefixes(tmp.srcPath, this.allPaths);
					addAllPrefixes(tmp.dstPath, this.allPaths);
				}
			}
			
			for (final var dst : top.children) {
				clusterStack.add(0, dst);
				this.processClusterStack(clusterStack, action);
				clusterStack.remove(0);
			}
		}
		
	}
	
	private static final <E> void addAllPrefixes(final List<E> list, final Collection<List<E>> target) {
		for (var i = 1; i <= list.size(); i += 1) {
			target.add(list.subList(0, i));
		}
	}
	
	@SafeVarargs
	public static final <E> List<E> newList(final E... elements) {
		return new ArrayList<>(Arrays.asList(elements));
	}
	
	/**
	 * @author 2oLDNncs 20251014
	 */
	private static final class Cluster {
		
		private final List<Object> key;
		private final int hashCode;
		
		private int level;
		
		private final Collection<Cluster> parents = new LinkedHashSet<>();
		private final Collection<Cluster> children = new LinkedHashSet<>();
		private final Collection<Link> links = new LinkedHashSet<>();
		
		public Cluster(final Object key) {
			this.key = Helpers.cast(key);
			this.hashCode = key.hashCode();
			this.level = this.getKeyLevel();
		}
		
		public final Object getKey() {
			return this.key;
		}
		
		public final int getKeyLevel() {
			return Helpers.cast(this.key.get(0));
		}
		
		public final String getKeyName() {
			return Helpers.cast(this.key.get(1));
		}
		
		public final void addChild(final Cluster child) {
			this.children.add(child);
			child.parents.add(this);
		}
		
		public final boolean isRoot() {
			return this.parents.isEmpty();
		}
		
		public final int getLevel() {
			return this.level;
		}
		
		public final void updateLevel(final int offset) {
			this.level += offset;
		}
		
		public final int updateLevel() {
			if (!this.children.isEmpty()) {
				final var childrenMaxLevel = this.children.stream()
						.mapToInt(Cluster::updateLevel)
						.min().getAsInt();
				this.level = Math.min(this.level, childrenMaxLevel - 1);
			}
			
			return this.level;
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
			
			final var that = Cluster.class.cast(obj);
			
			return null != that && this.getKey().equals(that.getKey());
		}
		
		@Override
		public final String toString() {
			return Arrays.asList(this.getLevel(), this.getKeyName()).toString();
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20251011
	 */
	private static final class Link {
		
		private final Graph graph;
		
		public final Map<String, String> props = new LinkedHashMap<>();
		public final List<Cluster> srcPath = new ArrayList<>();
		public final List<Cluster> dstPath = new ArrayList<>();
		
		public Link(final Graph graph, final String[] row) {
			this.graph = graph;
			final var n = row.length / 2;
			
			graph.setNodeSize(n);
			
			final var srcNode = Arrays.asList(Arrays.copyOfRange(row, 0, n));
			final var dstNode = Arrays.asList(Arrays.copyOfRange(row, n, 2 * n));
			
			for (var i = 0; i < n; i += 1) {
				this.tryAddCluster(this.srcPath, srcNode, i);
				this.tryAddCluster(this.dstPath, dstNode, i);
			}
			
			final var hasProps = ((row.length & 1) != 0) && !Helpers.last(row).isEmpty();
			
			if (hasProps) {
				for (final var prop : Helpers.last(row).split(graph.propDelimiter)) {
					final var kv = prop.split("=", 2);
					
					if (2 != kv.length) {
						throw new IllegalArgumentException(String.format("Invalid prop: %s", prop));
					}
					
					this.props.put(kv[0], kv[1]);
				}
			}
			
			if (!this.srcPath.isEmpty()) {
				if (!this.dstPath.isEmpty()) {
					this.srcPath.get(0).links.add(this);
				} else {
					graph.pathProps.put(this.srcPath, this.props);
				}
			}
		}
		
		private Link(final Graph graph,
				final List<Cluster> srcPath, final List<Cluster> dstPath,
				final Map<String, String> props) {
			this.graph = graph;
			this.srcPath.addAll(srcPath);
			this.dstPath.addAll(dstPath);
			this.props.putAll(props);
		}
		
		public final void tryParentSrcToDst() {
			if (!this.srcPath.isEmpty() && !this.dstPath.isEmpty()) {
				if (!this.srcPath.get(0).equals(this.dstPath.get(0))) {
					this.srcPath.get(0).addChild(this.dstPath.get(0));
				}
			}
		}
		
		private final void tryAddCluster(final List<Cluster> path, final List<String> node, final int i) {
			final var name = node.get(i);
			
			if (!name.isEmpty()) {
				path.add(this.graph.allClusters.computeIfAbsent(Arrays.asList(i, name), Cluster::new));
			}
		}
		
		public final boolean hasDst() {
			return !this.dstPath.isEmpty();
		}
		
		public final Link dup() {
			return new Link(this.graph, this.srcPath, this.dstPath, this.props);
		}
		
		public final void reorgDirBack() {
			for (final var propIt = this.props.entrySet().iterator(); propIt.hasNext();) {
				final var prop = propIt.next();
				
				if ("dir".equals(prop.getKey()) && "back".equals(prop.getValue())) {
					Helpers.swap(this.srcPath, this.dstPath);
					propIt.remove();
					break;
				}
			}
		}
		
		public final String[] toRow() {
			return this.graph.makeRow(this.srcPath, this.dstPath, this.props);
		}
		
		@Override
		public final String toString() {
			return Arrays.toString(this.toRow());
		}
		
	}
	
	private static final void processRows(final ArgsParser ap, final Consumer<String[]> action) throws IOException {
		if (!ap.getString(ARG_SHEET).isBlank()) {
			XSSFWorkbookToGraphviz.forEachRowInWorkbookSheet(ap, ARG_IN, ARG_SHEET, action);
		} else {
			forEachRowInDsv(ap, ARG_IN, ARG_DELIMITER1, action);
		}
	}
	
	public static final void forEachRowInDsv(final ArgsParser ap, final String dsvFileKey, final String delimiterKey,
			final Consumer<String[]> action) throws IOException {
		final var delimiter = ap.getString(delimiterKey);
		
		Files.lines(ap.getPath(dsvFileKey)).forEach(line -> {
			action.accept(line.split(delimiter, -1));
		});
	}
	
	private static final void processRow(final String[] row, final String propDelimiter, final GraphvizPrinter gvp) {
		final var nodeSize = row.length / 2;
		final var isArc = !String.join("", Arrays.copyOfRange(row, nodeSize, 2 * nodeSize)).isEmpty();
		final var hasProps = 1 == (row.length & 1);
		final var arcElements = hasProps ? Arrays.copyOf(row, row.length - 1) : row;
		
		if (isArc) {
			gvp.processArc(arcElements);
		} else if (!hasProps) {
			gvp.processNodeProp(Helpers.concat(Arrays.copyOf(row, nodeSize), "label", row[0]));
		}
		
		if (hasProps) {
			Arrays.stream(row[row.length - 1].split(propDelimiter))
			.filter(prop -> !prop.isEmpty())
			.map(prop -> prop.split("=", 2))
			.filter(propEntry -> {
				if (2 == propEntry.length) {
					return true;
				}
				
				System.err.println(String.format("Error parsing row: %s", Arrays.toString(row)));
				System.err.println(String.format(" Error parsing propEntry: %s", Arrays.toString(propEntry)));
				
				return false;
			})
			.forEach(propEntry -> {
				if (isArc) {
					gvp.processArcProp(Helpers.concat(arcElements, propEntry));
				} else {
					gvp.processNodeProp(Helpers.concat(Arrays.copyOf(row, nodeSize), propEntry));
				}
			});
		}
	}
	
	public static final PrintStream getPrintStream(final ArgsParser ap, final String outKey) throws FileNotFoundException {
		if (!ap.getString(outKey).isBlank()) {
			return new PrintStream(ap.getFile(outKey));
		}
		
		return System.out;
	}
	
}
