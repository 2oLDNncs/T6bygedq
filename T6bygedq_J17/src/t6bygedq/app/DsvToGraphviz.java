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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Log;
import t6bygedq.lib.LogLevel;

/**
 * @author 2oLDNncs 20250929
 */
@LogLevel(0)
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
					
//					dprint(graph.allLinks);
					
					final var roots = graph.collectRoots();
					
					Log.out(1, roots.size());
					
					roots.forEach(Cluster::updateLevel);
					
					Log.out(1, graph.allClusters);
					
					{
						final var paddingSize = -roots.stream()
								.mapToInt(Cluster::getLevel)
								.min().getAsInt();
						graph.nodeSize += paddingSize;
						
						// fix negative levels after update
						graph.allClusters.values().forEach(cluster -> cluster.level += paddingSize);
					}
					
					graph.allLinks.forEach(Link::reorgDirBack);
					
					roots.forEach(root -> processClusterStack(newList(root), graph, gvpAction));
					
					graph.allPrefixes.forEach(prefix -> {
						final var prefixProps = graph.findPrefixProps(prefix);
						
						if (null != prefixProps) {
							gvpAction.accept(makeRow(graph, prefix, Collections.emptyList(), prefixProps));
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
		public final Map<List<Cluster>, List<String>> pathProps = new HashMap<>();
		public final Collection<List<Cluster>> allPrefixes = new LinkedHashSet<>();
		public int nodeSize;
		
		public final Collection<Cluster> collectRoots() {
			return this.allClusters.values().stream()
					.filter(Cluster::isRoot)
					.collect(Collectors.toSet());
		}
		
		public final List<String> findPrefixProps(List<Cluster> prefix) {
			List<String> result = null;
			
			for (var j = prefix.size(); 0 < j && null == result; j -= 1) {
				for (var i = j - 1; 0 <= i && null == result; i -= 1) {
					final var props = this.pathProps.get(prefix.subList(i, j));
					
					if (null != props) {
						result = props;
					}
				}
			}
			
			return result;
		}
		
	}
	
	private static final void processClusterStack(final List<Cluster> clusterStack, final Graph graph, final Consumer<String[]> action) {
//		Log.out(1, callStack);
		final var top = clusterStack.get(0);
		
		for (final var link : top.links) {
			if (!link.dstPath.isEmpty() && !top.key.equals(link.dstPath.get(0).key)) {
				clusterStack.add(0, link.dstPath.get(0));
				processClusterStack(clusterStack, graph, action);
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
				addAllPrefixes(tmp.srcPath, graph.allPrefixes);
				addAllPrefixes(tmp.dstPath, graph.allPrefixes);
			}
		}
		
		for (final var dst : top.children) {
			clusterStack.add(0, dst);
			processClusterStack(clusterStack, graph, action);
			clusterStack.remove(0);
		}
	}
	
	private static final <E> void addAllPrefixes(final List<E> list, final Collection<List<E>> target) {
		Log.out(1, list);
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
		
		public int level;
		
		public final String name;
		
		private final Collection<Cluster> parents = new LinkedHashSet<>();
		private final Collection<Cluster> children = new LinkedHashSet<>();
		private final Collection<Link> links = new LinkedHashSet<>();
		private final Collection<String> props = new LinkedHashSet<>();
		
		public Cluster(final Object key) {
			this.key = Helpers.cast(key);
			this.level = this.getKeyLevel();
			this.name = this.getKeyName();
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
			return this.key.hashCode();
		}
		
		@Override
		public final boolean equals(final Object obj) {
			final var that = Cluster.class.cast(obj);
			
			return null != that && this.key.equals(that.key);
		}
		
		@Override
		public final String toString() {
			return Arrays.asList(this.level, this.name).toString();
		}
		
	}
	
//	private static final void dprint(final List<Link> links) {
//		Log.out(0);
//		links.stream().forEach(System.out::println);
//	}
	
	/**
	 * @author 2oLDNncs 20251011
	 */
	private static final class Link {
		
		private final Graph graph;
		
		public final List<String> props;
		
		public final List<Cluster> srcPath = new ArrayList<>();
		public final List<Cluster> dstPath = new ArrayList<>();
		
		public Link(final Graph graph, final String[] row) {
			this.graph = graph;
			final var n = row.length / 2;
			final var hasProps = ((row.length & 1) != 0) && !Helpers.last(row).isEmpty();
			
			final var srcNode = Arrays.asList(Arrays.copyOfRange(row, 0, n));
			final var dstNode = Arrays.asList(Arrays.copyOfRange(row, n, 2 * n));
			this.props = hasProps ? newList(Helpers.last(row).split(graph.propDelimiter)) : Collections.emptyList();
			
			graph.nodeSize = srcNode.size();
			
			for (var i = 0; i < srcNode.size(); i += 1) {
				this.tryAddCluster(this.srcPath, srcNode, i);
				this.tryAddCluster(this.dstPath, dstNode, i);
			}
			
			if (this.dstPath.isEmpty()) {
				graph.pathProps.put(this.srcPath, this.props);
			}
			
			if (!this.srcPath.isEmpty()) {
				if (!this.dstPath.isEmpty()) {
					this.srcPath.get(0).links.add(this);
					
					if (!this.srcPath.get(0).key.equals(this.dstPath.get(0).key)) {
						this.srcPath.get(0).addChild(this.dstPath.get(0));
					}
				} else {
					this.srcPath.get(0).props.addAll(this.props);
				}
			}
		}
		
		private Link(final Graph graph,
				final List<Cluster> srcPath, final List<Cluster> dstPath,
				final List<String> props) {
			this.graph = graph;
			this.srcPath.addAll(srcPath);
			this.dstPath.addAll(dstPath);
			this.props = props;
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
			Log.out(1, Arrays.toString(this.toRow()));
			
			for (final var propIt = this.props.iterator(); propIt.hasNext();) {
				final var prop = propIt.next();
				final var kv = prop.split("=", 2);
				
				if (2 != kv.length) {
					throw new IllegalArgumentException(String.format("Invalid prop: %s", prop));
				}
				
				if ("dir".equals(kv[0]) && "back".equals(kv[1])) {
					Helpers.swap(this.srcPath, this.dstPath);
					propIt.remove();
					break;
				}
			}
		}
		
		public final String[] toRow() {
			return makeRow(this.graph, this.srcPath, this.dstPath, this.props);
		}
		
		@Override
		public final String toString() {
			return Arrays.toString(this.toRow());
		}
		
	}
	
	public static final String[] makeRow(final Graph graph, final List<Cluster> srcPath, final List<Cluster> dstPath, final List<String> props) {
		final var result = new String[2 * graph.nodeSize + 1];
		
		Arrays.fill(result, "");
		
		for (var i = 0; i < srcPath.size(); i += 1) {
			result[srcPath.get(i).getLevel()] = srcPath.get(i).getKeyName();
		}
		
		for (var i = 0; i < dstPath.size(); i += 1) {
			result[graph.nodeSize + dstPath.get(i).getLevel()] = dstPath.get(i).getKeyName();
		}
		
		result[result.length - 1] = String.join(graph.propDelimiter, props);
		
		return result;
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
		Log.outf(1, "%s", Arrays.toString(row));
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
