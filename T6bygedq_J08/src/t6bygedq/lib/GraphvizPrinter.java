package t6bygedq.lib;

import static t6bygedq.lib.Helpers.cast;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 2oLDNncs 20240804
 */
public final class GraphvizPrinter {
	
	private final PrintStream out;
	
	private final Map<List<String>, Map<String, String>> nodeProps = new LinkedHashMap<>();
	private final Map<List<Integer>, Map<String, String>> arcProps = new LinkedHashMap<>();
	private final Map<String, Map<String, String>> classProps = new LinkedHashMap<>();
	private final Map<List<String>, Integer> nodeIds = new LinkedHashMap<>();
	private final Map<String, Object> clusters = new LinkedHashMap<>();
	private final Collection<List<Integer>> arcs = new ArrayList<>();
	
	private int expectedArcElementsLength = -1;
	private int nodePropLineNumber = 1;
	private int arcPropLineNumber = 1;
	private int arcLineNumber = 1;
	
	public GraphvizPrinter(final PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Call <code>begin(<br>
	 * 	true,  // strict, to merge duplicate edges<br>
	 *  "dot", // dot layout, to allow clustering<br>
	 *  true,  // compound, to allow edges between clusters<br>
	 *  "TB"   // Top Down direction
	 * )</code>
	 */
	public final void begin() {
		this.begin(true, "dot", true, "TB");
	}
	
	/**
	 * @param strict If true, merge duplicate edges
	 * @param layout
	 * @param compound If true, allow edges between clusters
	 * @param rankdir
	 */
	public final void begin(final boolean strict, final String layout, final boolean compound, final String rankdir) {
		this.out.println(String.format("%sdigraph G { // Use strict to merge duplicate edges", strict ? "strict " : ""));
		this.out.println(String.format("	layout=%s // Use dot for clustering", layout));
		this.out.println(String.format("	compound=%s // If true, allow edges between clusters", compound));
		this.out.println(String.format("	rankdir=%s // TB or LR", rankdir));
	}
	
	/**
	 * @param nodePropElements an array of length N+2:
	 * [nodeParts[0], ..., nodeParts[N-1], propKey, propVal]
	 */
	public final void processNodeProp(final String... nodePropElements) {
		final List<String> node = this.toNode(String.format("Node prop line %s", this.nodePropLineNumber),
				Arrays.copyOf(nodePropElements, nodePropElements.length - 2));
		
		this.addNode(node);
		
		final String propKey = nodePropElements[nodePropElements.length - 2];
		final String propVal = nodePropElements[nodePropElements.length - 1];
		
		setObjProp(this.nodeProps, node, propKey, propVal);
		
		this.nodePropLineNumber += 1;
	}
	
	/**
	 * @param classPropElements an array of length 3:
	 * [className, propKey, propVal]
	 */
	public final void processClassProp(final String... classPropElements) {
		final String className = classPropElements[0];
		final String propKey = classPropElements[1];
		final String propVal = classPropElements[2];
		
		setObjProp(this.classProps, className, propKey, propVal);
	}
	
	/**
	 * @param arcPropElements an array of length 2*N+2:
	 * [tailNodeParts[0], ..., tailNodeParts[N-1], headNodeParts[0], ..., headNodeParts[N-1], propKey, propVal]
	 */
	public final void processArcProp(final String... arcPropElements) {
		final List<Integer> arc = this.toArc(String.format("Arc prop line %s", this.arcPropLineNumber),
				Arrays.copyOf(arcPropElements, arcPropElements.length - 2));
		final String propKey = arcPropElements[arcPropElements.length - 2];
		final String propVal = arcPropElements[arcPropElements.length - 1];
		
		setObjProp(this.arcProps, arc, propKey, propVal);
		
		this.arcPropLineNumber += 1;
	}
	
	/**
	 * @param arcElements an array of length 2*N:
	 * [tailNodeParts[0], ..., tailNodeParts[N-1], headNodeParts[0], ..., headNodeParts[N-1]]
	 */
	public final void processArc(final String... arcElements) {
		this.arcs.add(this.toArc(String.format("Arc line %s", this.arcLineNumber), arcElements));
		
		this.arcLineNumber += 1;
	}
	
	public final void end() {
		this.printArcs();
		this.printClusters();
		
		this.out.println("}");
	}
	
	private final List<String> toNode(final String context, final String[] nodeElements) {
		if (this.expectedArcElementsLength < 0) {
			this.expectedArcElementsLength = 2 * nodeElements.length;
		}
		
		if (this.expectedArcElementsLength / 2 != nodeElements.length) {
			throw new IllegalArgumentException(String.format("%s: Wrong number of elements (expected: %s, actual: %s)",
					context,
					this.expectedArcElementsLength / 2,
					nodeElements.length));
		}
		
		return Arrays.asList(nodeElements);
	}
	
	private final List<Integer> toArc(final String context, final String[] arcElements) {
		if (0 != (arcElements.length & 1)) {
			throw new IllegalArgumentException(String.format("%s: Odd number of elements", context));
		}
		
		if (this.expectedArcElementsLength < 0) {
			this.expectedArcElementsLength = arcElements.length;
		}
		
		if (this.expectedArcElementsLength != arcElements.length) {
			throw new IllegalArgumentException(String.format("%s: Wrong number of elements (expected: %s, actual: %s)",
					context, this.expectedArcElementsLength, arcElements.length));
		}
		
		final List<String> tail = Arrays.asList(Arrays.copyOfRange(arcElements, 0, arcElements.length / 2));
		final List<String> head = Arrays.asList(Arrays.copyOfRange(arcElements, arcElements.length / 2, arcElements.length));
		
		this.addNode(tail);
		this.addNode(head);
		
		final Integer tailId = this.nodeIds.get(tail);
		final Integer headId = this.nodeIds.get(head);
		
		return Arrays.asList(tailId, headId);
	}
	
	private void addNode(final List<String> node) {
		this.updateNodeIds(node);
		this.updateClusters(node);
	}
	
	private final void updateNodeIds(final List<String> node) {
		for (int i = 0; i < node.size(); i += 1) {
			this.nodeIds.computeIfAbsent(keepLeft(node, i, FILLER), __ -> 1 + this.nodeIds.size());
		}
	}
	
	private final void printArcs() {
		this.arcs.forEach(arc -> {
			final Integer tailId = arc.get(0);
			final Integer headId = arc.get(1);
			final Map<String, String> props = new LinkedHashMap<>();
			
			this.applyProps(this.arcProps.getOrDefault(arc, Collections.emptyMap()), props);
			
			// It is possible to use ltail and lhead all the time
			// When the node is an actual cluster node, Graphviz will clip the edges appropriately
			// Otherwise, the nodes are connected as usual
			this.out.println(String.format("	%s -> %s [ltail=cluster_%s,lhead=cluster_%s,%s]",
					tailId, headId, tailId, headId, formatProps(props)));
		});
	}
	
	private final void printClusters() {
		final List<String> node = new ArrayList<>(Collections.nCopies(this.expectedArcElementsLength / 2, FILLER));
		
		this.printClusters(this.clusters, node, 0, "\t");
	}
	
	private final void printClusters(final Map<String, Object> clusters,
			final List<String> node, final int i, final String indent) {
		clusters.forEach((k, v) -> {
			node.set(i, k);
			final Integer nodeId = Objects.requireNonNull(this.nodeIds.get(node));
			final Map<String, String> props = new LinkedHashMap<>(mapOf("label", k));
			
			this.applyProps(this.nodeProps.getOrDefault(node, Collections.emptyMap()), props);
			
			if (i + 1 < node.size()) {
				// Graphviz doc: Clusters are encoded as subgraphs whose names have the prefix 'cluster'.
				this.out.println(String.format("%ssubgraph cluster_%s {", indent, nodeId));
				
				props.forEach((propK, propV) -> {
					this.out.println(String.format("%s	%s=%s", indent, propK, formatPropValue(propV)));
				});
				
				// This is a trick to get a node that can be used to connect to/from the whole cluster using ltail and lhead
				// FIXME This invisible node actually gets positioned somewhere next to the visible ones,
				//       so the incoming and outgoing edges aren't aimed at the center of the cluster : how to fix this?
				//       We can add invisible edges from the invisible cluster node to force its rank,
				//       but the result looks weird (hard to describe, the invisible nodes affect the global layout)
				this.out.println(String.format("%s	%s [label=\"\",shape=point,width=0,height=0]", indent, nodeId));
				
				this.printClusters(cast(v), node, i + 1, indent + "\t");
				
				this.out.println(String.format("%s}", indent));
			} else if (!"".equals(k)) { // To avoid duplicates, don't print an additional node if the leaf matches a cluster node
				this.out.println(String.format("%s%s [%s]",
						indent, nodeId, formatProps(props)));
			}
			
			node.set(i, FILLER);
		});
	}
	
	private final void updateClusters(final List<String> node) {
		Map<String, Object> c = this.clusters;
		
		for (int i = 0; i + 1 < node.size(); i += 1) {
			c = cast(c.computeIfAbsent(node.get(i), __ -> new LinkedHashMap<>()));
		}
		
		c.put(node.get(node.size() - 1), null);
	}
	
	private final void applyProps(final Map<String, String> src, final Map<String, String> dst) {
		this.applyProps(src, dst, new HashSet<>());
	}
	
	private final void applyProps(final Map<String, String> src, final Map<String, String> dst, final Collection<String> done) {
		for (final String className : src.getOrDefault(PROPKEY_CLASSES, ",").split(",")) {
			final Map<String, String> classProps = this.classProps.get(className);
			
			if (null != classProps) {
				if (done.add(className)) {
					this.applyProps(classProps, dst, done);
				} else {
					System.err.println(String.format("Warning: Recursive style class: %s", className));
				}
			} else {
				System.err.println(String.format("Warning: Unknown style class: %s", className));
			}
		}
		
		src.forEach((k, v) -> {
			if (!PROPKEY_CLASSES.equals(k)) {
				dst.put(k, v);
			}
		});
	}
	
	public static final String PROPKEY_CLASSES = "classes";
	
	private static final String FILLER = "";
	
	private static final <E> List<E> keepLeft(final List<E> elements, final int n, final E filler) {
		final List<E> result = new ArrayList<>(elements);
		
		Collections.fill(result.subList(n + 1, result.size()), filler);
		
		return result;
	}
	
	private static final <K> void setObjProp(final Map<K, Map<String, String>> props,
			final K obj, final String propKey, final String propVal) {
		props.computeIfAbsent(obj, __ -> new LinkedHashMap<>()).put(propKey, propVal);
	}
	
	private static final String formatProps(final Map<String, String> props) {
		return String.join(",", props.entrySet().stream()
				.map(e -> String.format("%s=%s", e.getKey(), formatPropValue(e.getValue())))
				.toArray(String[]::new));
	}
	
	private static final String formatPropValue(final String value) {
		if (value.startsWith("<") && value.endsWith(">")) {
			return value;
		}
		
		return String.format("\"%s\"", value);
	}
	
	private static final <K, V> Map<K, V> mapOf(final Object... kvPairs) {
		final Map<K, V> result = new HashMap<>();
		
		for (int i = 0; i < kvPairs.length; i += 2) {
			result.put(cast(kvPairs[i]), cast(kvPairs[i + 1]));
		}
		
		return Collections.unmodifiableMap(result);
	}
	
}