package t6bygedq.app;

import static t6bygedq.lib.Helpers.cast;
import static t6bygedq.lib.Helpers.in;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Chrono;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Log;
import t6bygedq.lib.LogLevel;
import t6bygedq.lib.MyXMLStreamWriter;

/**
 * @author 2oLDNncs 20250401
 */
@LogLevel(3)
public final class GraphLayout {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_OUT = "-Out";
	
	public static final void main(final String... args) throws IOException, XMLStreamException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_clusterarcs2.txt");
		ap.setDefault(ARG_OUT, "data/graph.svg");
		
		Log.beginf(1, "Loading %s", ap.getPath(ARG_IN));
		
		final var g = parseArcs(Files.lines(ap.getPath(ARG_IN)));
		
		Log.done();
		
		final var lg = new LayoutGrid(g);
		
		lg.layout();
		
		writeSvg(lg, ap.getString(ARG_OUT));
	}
	
	public static final List<String> blank(final List<String> src, final int i) {
		final var result = new ArrayList<>(src);
		
		Collections.fill(result.subList(i, result.size()), "0");
		
		return result;
	}
	
	public static final Graph parseArcs(final Stream<String> lines) {
		final Graph g = new Graph();
		
		final var nodeMap = new HashMap<List<String>, Graph.Node>();
		final Function<? super List<String>, ? extends Graph.Node> makeNode = lbl -> {
			final var result = g.addNode();
			
			result.setLabel(Helpers.last(lbl));
			
			return result;
		};
		final Consumer<List<String>> buildClusters = nodePath -> {
			for (var i = 2; i <= nodePath.size(); i += 1) {
				final var parent = nodeMap.computeIfAbsent(blank(nodePath, i - 1), makeNode);
				final var child = nodeMap.computeIfAbsent(blank(nodePath, i), makeNode);
				
				if (parent != child) {
					Log.out(4, parent, blank(nodePath, i - 1), "->", child, blank(nodePath, i));
					child.setParent(parent);
				}
			}
		};
		
		lines.forEach(line -> {
			final var elements = line.split("\t", -1);
			final var n = elements.length / 2;
			final var node1Path = Arrays.asList(Arrays.copyOfRange(elements, 0, n));
			final var node2Path = Arrays.asList(Arrays.copyOfRange(elements, n, elements.length));
			
			buildClusters.accept(node1Path);
			final var start = nodeMap.computeIfAbsent(node1Path, makeNode);
			
			if (!String.join("", node2Path).isEmpty()) {
				buildClusters.accept(node2Path);
				final var end = nodeMap.computeIfAbsent(node2Path, makeNode);
				
				Log.out(4, start, node1Path, "->", end, node2Path);
				start.edgeTo(end);
			}
		});
		
		return g;
	}
	
	public static final void writeSvg(final LayoutGrid grid, final String filePath)
			throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
		Log.beginf(1, "Writing svg %s", filePath);
		
		try (final var svgOut = new PrintStream(filePath)) {
			final var svg = new MyXMLStreamWriter(svgOut);
			final var cellWidth = 40;
			final var cellHeight = 20;
			final var minCellDim = Math.min(cellWidth, cellHeight);
			final var viewBox = new Rectangle();
			
			grid.forEach(cell -> {
				viewBox.add(
						cellWidth * (1 + cell.getColIdx()),
						cellHeight * (1 + cell.getRowIdx()));
			});
			
			{
				svg.writeStartDocument();
				
				xmlElement(svg, "svg", () -> {
					svg.writeAttribute("xmlns", "http://www.w3.org/2000/svg");
					svg.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
					svg.writeAttribute("viewBox", String.format("%s %s %s %s",
							viewBox.x, viewBox.y, viewBox.width, viewBox.height));
					svg.writeAttribute("style", String.format("width: %s; height: %s", viewBox.width, viewBox.height));
					
					xmlElement(svg, "defs", () -> {
						xmlElement(svg, "marker", () -> {
							svg.writeAttribute("id", "head");
							svg.writeAttribute("orient", "auto");
							svg.writeAttribute("markerWidth", "12");
							svg.writeAttribute("markerHeight", "8");
							svg.writeAttribute("refX", "12");
							svg.writeAttribute("refY", "4");
							
							xmlElement(svg, "path", () -> {
								svg.writeAttribute("d", "M0,0 V8 L12,4 Z");
								svg.writeAttribute("fill", "context-stroke");
							});
						});
					});
					
					for (final var cell : in(grid.streamCells())) {
						final var node = cell.getNode();
						
						if (null != node) {
							final var cellX = cellWidth * cell.getColIdx();
							final var cellY = cellHeight * cell.getRowIdx();
							final var centerX = cellX + cellWidth / 2;
							final var centerY = cellY + cellHeight / 2;
							
							{
								if (1 < node.getClusterWeight()) {
									final var clusterRect = getRect(node);
									final var cd = node.getClusterDepth();
									final var ch = node.getClusterHeight();
									final var r0 = 16;
									final var r1 = ((r0 - 1.0) / r0) * ch / (ch + cd);
									final var clusterX = cellWidth * (lerp(1.0, 0.5, r1) + 2 * clusterRect.x);
									final var clusterY = cellHeight * (lerp(1.0, 0.5, r1) + 2 * clusterRect.y);
									final var clusterWidth = cellWidth * (2 * clusterRect.width + lerp(-1.0, 0.0, r1));
									final var clusterHeight = cellHeight * (2 * clusterRect.height + lerp(-1.0, 0.0, r1));
									Log.out(1, node, clusterRect);
									
									xmlElement(svg, "rect", () -> {
										svg.writeAttribute("fill", "#00000010");
										svg.writeAttribute("stroke", "black");
										svg.writeAttribute("stroke-width", "0.25");
										svg.writeAttribute("x", "" + clusterX);
										svg.writeAttribute("y", "" + clusterY);
										svg.writeAttribute("width", "" + clusterWidth);
										svg.writeAttribute("height", "" + clusterHeight);
										svg.writeAttribute("rx", "" + minCellDim / 4);
										svg.writeAttribute("ry", "" + minCellDim / 4);
										svg.writeAttribute("style", "");
									});
								}
							}
							
							xmlElement(svg, "rect", () -> {
								svg.writeAttribute("fill", "white");
								svg.writeAttribute("stroke", "black");
								svg.writeAttribute("stroke-width", "0.25");
								svg.writeAttribute("x", "" + cellX);
								svg.writeAttribute("y", "" + cellY);
								svg.writeAttribute("width", "" + cellWidth);
								svg.writeAttribute("height", "" + cellHeight);
								svg.writeAttribute("rx", "" + minCellDim / 4);
								svg.writeAttribute("ry", "" + minCellDim / 4);
								svg.writeAttribute("style", "");
							});
							
							xmlElement(svg, "text", () -> {
								svg.writeAttribute("text-anchor", "middle");
								svg.writeAttribute("dominant-baseline", "central");
								svg.writeAttribute("font-size", "" + cellHeight * 5.0 / 20.0);
								svg.writeAttribute("x", "" + centerX);
								svg.writeAttribute("y", "" + centerY);
								
								xmlElement(svg, "tspan", () -> {
									svg.writeAttribute("dy", "-0.6em");
									svg.writeCharacters(node.toString());
								});
								
								xmlElement(svg, "tspan", () -> {
									svg.writeAttribute("x", "" + centerX);
									svg.writeAttribute("dy", "1.2em");
									svg.writeCharacters(node.getLabel());
								});
							});
							
							for (final var path : in(cell.streamOutgoingPaths())) {
								final var dBuilder = new StringBuilder();
								
								final BiConsumer<Integer, BiConsumer<Double, Double>> f = (i, g) -> {
									final var wi = path.getWaypoints().get(i);
									final var wiCellX = wi.getCell().getColIdx() * cellWidth;
									final var wiCellY = wi.getCell().getRowIdx() * cellHeight;
									final var wiSide = wi.getSide();
									final var wiSideGeomWallOri = wiSide.getGeomWallOri();
									final var wiSideGeomWallDir = wiSide.getGeomWallDir();
									final var wiPosition = wi.getPosition();
									final var wiX = wiCellX + (wiSideGeomWallOri.getX() + wiSideGeomWallDir.getX() * wiPosition) * cellWidth;
									final var wiY = wiCellY + (wiSideGeomWallOri.getY() + wiSideGeomWallDir.getY() * wiPosition) * cellHeight;
									
									g.accept(wiX, wiY);
								};
								
								f.accept(0, (x, y) -> {
									dBuilder.append("M");
									dBuilder.append(x);
									dBuilder.append(",");
									dBuilder.append(y);
								});
								
								for (var i = 1; i < path.getWaypoints().size(); i += 1) {
									f.accept(i, (x, y) -> {
										dBuilder.append("L");
										dBuilder.append(x);
										dBuilder.append(",");
										dBuilder.append(y);
									});
								}
								
								xmlElement(svg, "path", () -> {
									svg.writeAttribute("fill", "none");
									svg.writeAttribute("stroke", path.getOri() == path.getDst() ? "red" : "black");
									svg.writeAttribute("stroke-width", "0.25");
									svg.writeAttribute("d", dBuilder.toString());
									svg.writeAttribute("marker-end", "url(#head)");
								});
							}
						}
					}
				});
				
				svg.writeEndDocument();
			}
		}
		
		Log.done();
		
		Chrono.getTimes().forEach((k, v) -> {
			Log.outf(1, " time(%s)=%s", k, v);
		});
	}
	
	public static final double lerp(final double a, final double b, final double t) {
		return a + t * (b - a);
	}
	
	public static final Point2D lerp(final Point2D a, final Point2D b, final double t) {
		return new Point2D.Double(
				lerp(a.getX(), b.getX(), t),
				lerp(a.getY(), b.getY(), t));
	}
	
	public static final String K_X = "X";
	public static final String K_Y = "Y";
	public static final String K_OUTLINE = "Outline";
	
	public static final void xmlElement(final XMLStreamWriter writer, final String localName,
			final ElementBuilder builder) throws XMLStreamException {
		Chrono.tic();
		writer.writeStartElement(localName);
		builder.build();
		writer.writeEndElement();;
		Chrono.toc(localName);
	}
	
	/**
	 * @author 2oLDNncs 20250403
	 */
	public static abstract interface ElementBuilder {
		
		public abstract void build() throws XMLStreamException;
		
	}
	
	public static final String K_CLUSTER = "Cluster";
	public static final String K_CLUSTER_RECT = "ClusterRect";
	public static final String K_DEPTH = "Depth";
	public static final String K_DEPTH_EXPLICIT = "DepthExplicit";
	public static final String K_GRID_PATH = "GridPath";
	public static final String K_GRID_SEGMENTS = "GridSegments";
	public static final String K_GRID_WALLS = "GridWalls";
	
	public static final Rectangle getRect(final Graph.Node node) {
		return cast(node.getProps().computeIfAbsent(K_CLUSTER_RECT, __ -> new Rectangle()));
	}
	
	public static final <K, E> List<E> computeIfAbsent(final Map<K, List<E>> map, final K key) {
		return map.computeIfAbsent(key, __ -> new ArrayList<>());
	}
	
	public static final <K, E> Set<E> computeIfAbsentHS(final Map<K, Set<E>> map, final K key) {
		return map.computeIfAbsent(key, __ -> new HashSet<>());
	}
	
	public static final <E> List<E> computeIfAbsent(final List<List<E>> map, final int key) {
		while (map.size() <= key) {
			map.add(new ArrayList<>());
		}
		
		return map.get(key);
	}
	
	/**
	 * @author 2oLDNncs 20250402
	 */
	public static abstract class Obj {
		
		private final Map<String, Object> props = new HashMap<>();
		
		public final Map<String, Object> getProps() {
			return this.props;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250401
	 */
	public static final class Graph extends Obj {
		
		private final List<Node> nodes = new ArrayList<>();
		
		public final Node addNode() {
			final var result = new Node(this.nodes.size());
			
			this.nodes.add(result);
			
			return result;
		}
		
		public final void forEach(final Consumer<? super Node> action) {
			this.nodes.forEach(action);
		}
		
		public final int countNodes() {
			return this.nodes.size();
		}
		
		public final Node getNode(final int index) {
			return this.nodes.get(index);
		}
		
		public final int countEdges() {
			return this.nodes.stream().mapToInt(Node::countOutgoingEdges).sum();
		}
		
		/**
		 * @author 2oLDNncs 20250619
		 */
		public static abstract class Cluster extends Obj {
			
			private Cluster parent;
			
			final List<Node> children = new ArrayList<>();
			
			private int clusterDepth = -1;
			
			private int clusterHeight = -1;
			
			private int clusterWeight = -1;
			
			public final Cluster getParent() {
				return this.parent;
			}
			
			public final void setParent(final Cluster parent) {
				if (this == parent) {
					throw new IllegalStateException(String.format("Cannot parent %s to itself", this));
				}
				
				if (this.getParent() != parent) {
					if (null != this.getParent()) {
						this.removeFromParent(this.getParent());
					}
					
					this.parent = parent;
					
					if (null != this.getParent()) {
						this.addToParent(this.getParent());
					}
				}
			}
			
			protected void removeFromParent(final Cluster parent) {
				parent.children.remove(this);
			}
			
			protected void addToParent(final Cluster parent) {
				parent.children.add((Node) this);
			}
			
			public final int countChildren() {
				return this.children.size();
			}
			
			public final Node getChild(final int index) {
				return this.children.get(index);
			}
			
			public final Stream<Node> streamChildren() {
				return this.children.stream();
			}
			
			public final void forEachChild(final Consumer<Node> action) {
				this.children.forEach(action);
			}
			
			public final int getClusterDepth() {
				if (this.clusterDepth < 0) {
					if (this.isClusterRoot()) {
						this.clusterDepth = 0;
					} else {
						this.clusterDepth = 1 + this.getParent().getClusterDepth();
					}
				}
				
				return this.clusterDepth;
			}
			
			public final int getClusterHeight() {
				if (this.clusterHeight < 0) {
					this.clusterHeight = 1 +
							this.streamChildren()
							.mapToInt(Cluster::getClusterHeight)
							.max().orElse(-1);
				}
				
				return this.clusterHeight;
			}
			
			public final int getClusterWeight() {
				if (this.clusterWeight < 0) {
					this.clusterWeight = 1 +
							this.streamChildren()
							.mapToInt(Cluster::getClusterWeight)
							.sum();
				}
				
				return this.clusterWeight;
			}
			
			public final boolean isClusterRoot() {
				return null == this.getParent();
			}
			
			public final boolean isClusterLeaf() {
				return 0 == this.countChildren();
			}
			
		}
		
		/**
		 * @author 2oLDNncs 20250619
		 */
		public static final class Comp extends Cluster {
			
		}
		
		/**
		 * @author 2oLDNncs 20250402
		 */
		public static final class Node extends Cluster {
			
			private final int index;
			
			private final List<Edge> incomingEdges = new ArrayList<>();
			
			private final List<Edge> outgoingEdges = new ArrayList<>();
			
			private final CompId compId = new CompId();
			
			private String label;
			
			public Node(final int index) {
				this.index = index;
				this.setLabel(Integer.toString(index));
			}
			
			public final int getIndex() {
				return this.index;
			}
			
			public final int getCompId() {
				return this.compId.getVal();
			}
			
			public final String getLabel() {
				return this.label;
			}
			
			public final void setLabel(final String label) {
				this.label = label;
			}
			
			public final Node getParentNode() {
				return Helpers.castOrNull(Node.class, this.getParent());
			}
			
			@Override
			protected final void removeFromParent(final Cluster parent) {
				super.removeFromParent(parent);
				
				final var parentNode = this.getParentNode();
				
				if (null != parentNode) {
					final var edge = parentNode.edgeTo(this, false);
					
					if (!edge.isExplicit()) {
						parentNode.outgoingEdges.remove(edge);
						this.incomingEdges.remove(edge);
					}
				}
			}
			
			@Override
			protected final void addToParent(final Cluster parent) {
				super.addToParent(parent);
				
				final var parentNode = this.getParentNode();
				
				if (null != parentNode) {
					parentNode.edgeTo(this, false);
				}
			}
			
			public final boolean isGraphRoot() {
				return this.incomingEdges.isEmpty();
			}
			
			public final boolean isGraphLeaf() {
				return this.outgoingEdges.isEmpty();
			}
			
			public final Edge edgeTo(final Node edgeEndNode) {
				return this.edgeTo(edgeEndNode, true);
			}
			
			public final Edge edgeTo(final Node edgeEndNode, final boolean explicit) {
				edgeEndNode.compId.topWith(this.compId);
				
				final var result = this.edgeTo(edgeEndNode, i -> {
					final var newEdge = new Edge(this, edgeEndNode);
					
					this.outgoingEdges.add(i, newEdge);
					
					edgeEndNode.addIncomingEdge(newEdge);
					
					return newEdge;
				});
				
				if (explicit) {
					result.setExplicit(true);
				}
				
				return result;
			}
			
			public final int countOutgoingEdges() {
				return this.outgoingEdges.size();
			}
			
			public final Edge getOutgoingEdge(final int index) {
				return this.outgoingEdges.get(index);
			}
			
			public final Edge findOutgoingEdge(final Node edgeEndNode) {
				return this.edgeTo(edgeEndNode, __ -> null);
			}
			
			public final Stream<Edge> streamOutgoingEdges() {
				return this.outgoingEdges.stream();
			}
			
			public final void forEachIncoming(final Consumer<Edge> action) {
				this.incomingEdges.forEach(action);
			}
			
			public final void forEachOutgoing(final Consumer<Edge> action) {
				this.outgoingEdges.forEach(action);
			}
			
			public final int countIncomingEdges() {
				return this.incomingEdges.size();
			}
			
			public final Edge getIncomingEdge(final int index) {
				return this.incomingEdges.get(index);
			}
			
			public final Edge findIncomingEdge(final Node edgeStartNode) {
				return this.edgeFrom(edgeStartNode, __ -> null);
			}
			
			public final Stream<Edge> streamIncomingEdges() {
				return this.incomingEdges.stream();
			}
			
			@Override
			public final String toString() {
//				return String.format("%s%s", this.getIndex(), this.getProps());
//				return String.format("%s", this.getIndex());
				return String.format("%s.%s", this.getCompId(), this.getIndex());
//				return String.format("%s.%s%s", this.getCompId(), this.getIndex(), this.getProps());
//				return String.format("%s.%s@%s_%s", this.getCompId(), this.getIndex(), this.getProps().get(K_ROW), this.getProps().get(K_COL));
//				return String.format("%s.%s@%s_%s", this.getCompId(), this.getIndex(), 1 + 2 * (int) this.getProps().get(K_ROW), 1 + 2 * (int) this.getProps().get(K_COL));
			}
			
			private final void addIncomingEdge(final Edge edge) {
				this.edgeFrom(edge.getStartNode(), i -> {
					this.incomingEdges.add(i, edge);
					
					return edge;
				});
			}
			
			private final Edge edgeTo(final Node edgeEndNode, final IntFunction<Edge> ifAbsent) {
				return search(this.outgoingEdges, edgeEndNode, Edge::getEndNodeIndex, ifAbsent);
			}
			
			private final Edge edgeFrom(final Node edgeStartNode, final IntFunction<Edge> ifAbsent) {
				return search(this.incomingEdges, edgeStartNode, Edge::getStartNodeIndex, ifAbsent);
			}
			
			private static final Edge search(final List<Edge> edges, final Node edgeNode,
					final ToIntFunction<Edge> getEdgeNodeIndex, final IntFunction<Edge> ifAbsent) {
				final var i = binarySearch(edges, edgeNode.getIndex(), getEdgeNodeIndex);
				
				if (0 <= i) {
					return edges.get(i);
				}
				
				return ifAbsent.apply(-(i + 1));
			}
			
			public static final <T> int binarySearch(final List<? extends T> lst, final int index,
					final ToIntFunction<T> indexer) {
				var low = 0;
				var high = lst.size() - 1;
				
				while (low <= high) {
					final var mid = (low + high) >>> 1;
					final var midVal = lst.get(mid);
					final var cmp = Integer.compare(indexer.applyAsInt(midVal), index);
					
					if (cmp < 0) {
						low = mid + 1;
					} else if (0 < cmp) {
						high = mid - 1;
					} else {
						return mid; // key found
					}
				}
				
				return -(low + 1); // key not found
			}
			
			/**
			 * @author 2oLDNncs 20250619
			 */
			public static abstract class Aggregator {
				
				private Aggregator top;
				
				public final void topWith(final Aggregator repl) {
					final var thisTop = this.top();
					final var replTop = repl.top();
					
					if (thisTop != replTop) {
						final var oldTop = thisTop.top;
						thisTop.top = replTop;
						replTop.replacing(oldTop);
					}
				}
				
				protected void replacing(final Aggregator oldTop) {
					//pass
				}
				
				protected final Aggregator top() {
					if (null != this.top && null != this.top.top) {
						this.pack();
					}
					
					return Objects.requireNonNullElse(this.top, this);
				}
				
				private final void pack() {
					final var stack = this.stack();
					final var stackTop = Helpers.last(stack);
					
					for (var i = stack.size() - 1; 0 <= i; i -= 1) {
						final var element = stack.get(i);
						element.top = stackTop;
						
						if (element.top == element) {
							element.top = null;
						}
					}
				}
				
				private final List<Aggregator> stack() {
					final var result = new ArrayList<Aggregator>();
					var tmp = this;
					
					while (null != tmp && !result.contains(tmp)) {
						result.add(tmp);
						tmp = tmp.top;
					}
					
					if (null != tmp) {
						result.add(tmp);
					}
					
					return result;
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250418
			 */
			private static final class CompId extends Aggregator {
				
				private final int val = ++lastVal;
				
				public final int getVal() {
					return ((CompId) this.top()).val;
				}
				
				@Override
				public final String toString() {
					return "" + this.val;
				}
				
				private static int lastVal;
				
			}
		    
		}
		
		/**
		 * @author 2oLDNncs 20250402
		 */
		public static final class Edge extends Obj {
			
			private final Node startNode;
			
			private final Node endNode;
			
			private boolean explicit = false;
			
			public Edge(final Node startNode, final Node endNode) {
				this.startNode = startNode;
				this.endNode = endNode;
			}
			
			public final Node getStartNode() {
				return this.startNode;
			}
			
			public final Node getEndNode() {
				return this.endNode;
			}
			
			public final boolean isExplicit() {
				return this.explicit;
			}
			
			public final boolean isNotExplicit() {
				return !this.isExplicit();
			}
			
			public final void setExplicit(final boolean explicit) {
				this.explicit = explicit;
			}
			
			public final int getStartNodeIndex() {
				return this.getStartNode().getIndex();
			}
			
			public final int getEndNodeIndex() {
				return this.getEndNode().getIndex();
			}
			
			@Override
			public final String toString() {
//				return String.format("%s->%s%s", this.getStartNodeIndex(), this.getEndNodeIndex(), this.getProps());
				return String.format("%s->%s", this.getStartNodeIndex(), this.getEndNodeIndex());
			}
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250415
	 */
	public static final class LayoutGrid {
		
		private final Graph graph;
		
		private final List<List<Cell>> rows = new ArrayList<>();
		
		public LayoutGrid(final Graph graph) {
			this.graph = graph;
		}
		
		public final Graph getGraph() {
			return this.graph;
		}
		
		public final int countRows() {
			return this.rows.size();
		}
		
		public final int countCols() {
			return this.rows.isEmpty() ? 0 : this.rows.get(0).size();
		}
		
		public final Stream<Cell> streamCells() {
			return this.rows.stream()
			.flatMap(Collection::stream)
			.filter(Objects::nonNull);
		}
		
		public final void forEach(final Consumer<Cell> action) {
			this.streamCells().forEach(action);
		}
		
		public final Cell cell(final int rowIdx, final int colIdx) {
			while (this.countRows() <= rowIdx) {
				this.rows.add(new ArrayList<>(Collections.nCopies(this.countCols(), null)));
			}
			
			if (this.countCols() <= colIdx) {
				this.rows.forEach(row -> row.addAll(Collections.nCopies(1 + colIdx - row.size(), null)));
			}
			
			var result = this.getRowElement(rowIdx, colIdx);
			
			if (null == result) {
				result = this.setRowElement(rowIdx, colIdx);
			}
			
			return result;
		}
		
		public final Cell neighbor(final Cell cell, final int dr, final int dc) {
			final var neighborRowIdx = cell.getRowIdx() + dr;
			final var neighborColIdx = cell.getColIdx() + dc;
			
			return this.inRange(neighborRowIdx, neighborColIdx) ? this.cell(neighborRowIdx, neighborColIdx) : null;
		}
		
		public final Cell neighbor(final Cell cell, final Cell.Delta delta) {
			return this.neighbor(cell, delta.dr(), delta.dc());
		}
		
		public final Cell neighbor(final Cell cell, final Cell.Side side) {
			return this.neighbor(cell, side.dr(), side.dc());
		}
		
		public final Iterable<Cell> neighbors(final Cell cell) {
			return in(this.streamNeighbors(cell));
		}
		
		public final Stream<Cell> streamNeighbors(final Cell cell) {
			return Arrays.stream(LayoutGrid.Cell.Side.N_W_S_E)
					.map(side -> this.neighbor(cell, side))
					.filter(Objects::nonNull);
		}
		
		public final void forEachNeighborOf(final Cell cell, final Consumer<Cell> action) {
			this.streamNeighbors(cell).forEach(action);
		}
		
		public final Cell findCell(final int rowIdx, final int colIdx) {
			if (this.inRange(rowIdx, colIdx)) {
				return this.getRowElement(rowIdx, colIdx);
			}
			
			return null;
		}
		
		public final boolean inRange(final int rowIdx, final int colIdx) {
			return Helpers.inRange(this.countRows(), rowIdx) && Helpers.inRange(this.countCols(), colIdx);
		}
		
		private final Cell getRowElement(final int rowIdx, final int colIdx) {
			return this.rows.get(rowIdx).get(colIdx);
		}
		
		private final Cell setRowElement(final int rowIdx, final int colIdx) {
			final var result = new Cell(rowIdx, colIdx);
			
			result.setWall(Cell.Side.NORTH, this.findCell(rowIdx - 1, colIdx));
			result.setWall(Cell.Side.WEST, this.findCell(rowIdx, colIdx - 1));
			result.setWall(Cell.Side.EAST, this.findCell(rowIdx, colIdx + 1));
			result.setWall(Cell.Side.SOUTH, this.findCell(rowIdx + 1, colIdx));
			
			this.rows.get(rowIdx).set(colIdx, result);
			
			return result;
		}
		
		public final void layout() {
			Log.begin(1, "Applying Hierarchical Grid Layout");
			Log.outf(2, "Nodes: %s Edges: %s", this.getGraph().countNodes(), this.getGraph().countEdges());
			
			this.computeNodeDepth();
			
			final var compRoots = new TreeMap<Integer, Collection<Graph.Node>>();
			
			this.graph.forEach(node -> {
				if (node.isClusterRoot()) {
					compRoots.computeIfAbsent(node.getCompId(), __ -> new ArrayList<>()).add(node);
				}
			});
			
			Log.out(6, "CompRoots:", compRoots);
			
			// Init node rects
			{
				final var todo = new ArrayList<Graph.Node>();
				final var done = new HashSet<Graph.Node>();
				
				compRoots.values().forEach(todo::addAll);
				
				final BiConsumer<Graph.Node, Point>[] translateRect = cast(new BiConsumer[1]);
				translateRect[0] = (node, delta) -> {
					getRect(node).translate(delta.x, delta.y);
					node.children.forEach(child -> translateRect[0].accept(child, delta));
				};
				
				while (!todo.isEmpty()) {
					final var node = todo.get(0);
					
					if (done.contains(node)) {
						todo.remove(0);
						
						final var rect = getRect(node);
						final var minChildTop = new int[] { Integer.MAX_VALUE };
						final var maxChildBottom = new int[] { Integer.MIN_VALUE };
						
						node.forEachChild(child -> {
							final var childRect = getRect(child);
							
							if (childRect.y <= rect.y) {
								translateRect[0].accept(child, new Point(0, +1));
							}
							
							minChildTop[0] = Math.min(minChildTop[0], childRect.y);
							maxChildBottom[0] = Math.max(maxChildBottom[0], childRect.y + childRect.width);
						});
						
						if (minChildTop[0] < maxChildBottom[0]) {
							rect.y = minChildTop[0] - 1;
							rect.height = maxChildBottom[0] - rect.y;
						}
						
						Log.out(4, node, rect);
					} else {
						final var rect = getRect(node);
						final var row = (Integer) node.getProps().get(K_DEPTH);
						
						rect.setBounds(0, row, 1, 1);
						
						todo.addAll(0, node.children);
						done.add(node);
					}
				}
			}
			
			// Init node locations
			{
				final var rowSize = new HashMap<Integer, Integer>();
				final var todo = new ArrayList<Graph.Node>();
				final var done = new HashSet<Graph.Node>();
				
				compRoots.values().forEach(todo::addAll);
				
				while (!todo.isEmpty()) {
					final var node = todo.get(0);
					
					if (done.contains(node)) {
						todo.remove(0);
						
						final var rect = getRect(node);
						final Consumer<Graph.Node>[] updateRect = cast(new Consumer[1]);
						updateRect[0] = child -> {
							final var childRect = getRect(child);
							rect.add(childRect);
							child.forEachChild(updateRect[0]);
						};
						
						node.forEachChild(updateRect[0]);
						
						{
							final var right = rect.x + rect.width - 1;
							
							for (var y = rect.y; y < rect.y + rect.height; y += 1) {
								rowSize.compute(y, (__, v) -> Math.max(null == v ? 0 : v, right));
							}
						}
						
						Log.out(4, node, rect, rowSize);
					} else {
						final var rect = getRect(node);
						var minX = 0;
						
						for (var y = rect.y; y < rect.y + rect.height; y += 1) {
							minX = Math.max(minX, rowSize.getOrDefault(y, 0));
						}
						
						for (var y = rect.y; y < rect.y + rect.height; y += 1) {
							rowSize.put(y, minX);
						}
						
						rect.x = rowSize.compute(rect.y, (__, v) -> null == v ? 0 : 1 + v);
						
						Log.out(4, node, rect);
						
						todo.addAll(0, node.children);
						done.add(node);
					}
				}
			}
			
			this.initCells();
			
			this.buildPaths();
			
			Log.done();
		}
		
		private final void computeNodeDepth() {
			final var roots = this.getGraph().nodes.stream().filter(Graph.Node::isGraphRoot).toList();
			
			Log.outf(2, "Roots: %s", roots.size());
			
			this.computeNodeDepth(roots);
			
			this.getGraph().forEach(node -> this.computeNodeDepth(Arrays.asList(node)));
		}
		
		private final void computeNodeDepth(final Collection<Graph.Node> nodes) {
			final var todo = new ArrayList<Graph.Node>();
			
			final Function<Integer, Consumer<Graph.Node>> applyDepth = d -> node -> {
				node.getProps().put(K_DEPTH, d);
				todo.add(node);
			};
			
			final var applyDepth0 = applyDepth.apply(0);
			
			for (final var node : nodes) {
				if (!node.getProps().containsKey(K_DEPTH)) {
					applyDepth0.accept(node);
				}
			}
			
			while (!todo.isEmpty()) {
				final var n0 = todo.remove(todo.size() - 1);
				final var d0 = (int) n0.getProps().get(K_DEPTH);
				
				for (final var edge : n0.outgoingEdges) {
					final var explicit = edge.isExplicit();
					final var n1 = edge.getEndNode();
					
					if (n1.getProps().containsKey(K_DEPTH)) {
						if (!(boolean) n1.getProps().getOrDefault(K_DEPTH_EXPLICIT, true)) {
							n1.getProps().put(K_DEPTH, d0 + 1);
							n1.getProps().put(K_DEPTH_EXPLICIT, explicit);
							todo.add(n1);
						}
					} else {
						n1.getProps().put(K_DEPTH, d0 + 1);
						n1.getProps().put(K_DEPTH_EXPLICIT, explicit);
						todo.add(n1);
					}
				}
			}
		}
		
		private final void initCells() {
			this.getGraph().forEach(node -> {
				final var rect = getRect(node);
				
				final var rowIdx = 1 + 2 * rect.y;
				final var colIdx = 1 + 2 * rect.x;
				
				Log.out(6, node, rowIdx, colIdx, node.getCompId(), node.getParent());
				
				if (null != this.cell(rowIdx, colIdx).getNode()) {
					
					throw new IllegalStateException(String.format("Conflict in cell(%s %s): %s vs %s",
							rowIdx, colIdx, this.cell(rowIdx, colIdx).getNode(), node));
				}
				
				this.cell(rowIdx, colIdx).setNode(node);
				this.cell(rowIdx + 1, colIdx + 1); // east and south borders (north and west already taken care of by rowIdx and colIdx)
			});
		}
		
		private final void buildPaths() {
			this.getGraph().forEach(node -> {
				Log.out(1, "Building paths for node:", node);
				
				Log.out(6, node, node.outgoingEdges);
				
				final var targets = node.streamOutgoingEdges()
						.filter(Graph.Edge::isExplicit)
						.map(GraphLayout.Graph.Edge::getEndNode)
						.collect(Collectors.toCollection(HashSet::new));
				final var rect = getRect(node);
				final var rowIdx = 1 + 2 * rect.y;
				final var colIdx = 1 + 2 * rect.x;
				final var nodeCell = this.cell(rowIdx, colIdx);
				
				if (targets.remove(node)) {
					this.buildPath1(nodeCell);
				}
				
				for (final var target : targets) {
					final var targetRect = getRect(target);
					final var targetRowIdx = 1 + 2 * targetRect.y;
					final var targetColIdx = 1 + 2 * targetRect.x;
					final var targetCell = this.cell(targetRowIdx, targetColIdx);
					
					Log.out(6, "node:", node, "target:", target);
					
					this.buildPath2(nodeCell, targetCell);
				}
			});
		}
		
		public void buildPath1(final Cell nodeCell) {
			final var path = new Path(nodeCell, nodeCell);
			var pathCell = nodeCell;
			
			for (final var pathSide : Cell.Side.N_E_S_W) {
				path.prependWaypoint(pathCell, pathSide);
				pathCell = this.neighbor(pathCell, pathSide);
			}
		}
		
		private final void buildPath2(final Cell nodeCell, final Cell targetCell) {
			final var path = new Path(nodeCell, targetCell);
			var pathCell = targetCell;
			
			while (pathCell != nodeCell) {
				Cell.Side pathSide = null;
				Cell nextPathCell = null;
				final var dirR = nodeCell.getRowIdx() - pathCell.getRowIdx();
				final var dirC = nodeCell.getColIdx() - pathCell.getColIdx();
				var bestDot = Double.NEGATIVE_INFINITY;
				
				for (final var candidatePathSide : Cell.Side.N_W_S_E) {
					final var pathCellNeighbor = this.neighbor(pathCell, candidatePathSide);
					var dot = Double.NEGATIVE_INFINITY;
					
					if (nodeCell == pathCellNeighbor) {
						dot = Double.POSITIVE_INFINITY;
					} else if (null != pathCellNeighbor && null == pathCellNeighbor.getNode()) {
						dot = dirR * candidatePathSide.dr() + dirC * candidatePathSide.dc();
					}
					
					if (bestDot < dot) {
						bestDot = dot;
						pathSide = candidatePathSide;
						nextPathCell = pathCellNeighbor;
					}
				}
				
				path.prependWaypoint(pathCell, pathSide);
				pathCell = nextPathCell;
			}
			
			Log.out(6, path, path.waypoints);
		}
		
		/**
		 * @author 2oLDNncs 20250415
		 */
		public static final class Cell extends Obj {
			
			private final int rowIdx;
			
			private final int colIdx;
			
			private final Map<Side, List<Waypoint>> walls = new EnumMap<>(Side.class);
			
			private Graph.Node node;
			
			private final Map<Cell, Waypoint> groupByOri = new HashMap<>();
			private final Map<Side, Map<Cell, Waypoint>> groupByDst = new HashMap<>();
			
			public Cell(final int rowIdx, final int colIdx) {
				this.rowIdx = rowIdx;
				this.colIdx = colIdx;
			}
			
			public final int getRowIdx() {
				return this.rowIdx;
			}
			
			public final int getColIdx() {
				return this.colIdx;
			}
			
			public final List<Waypoint> getWall(final Side side) {
				return this.walls.get(side);
			}
			
			public final void setWall(final Side side, final Cell neighbor) {
				final List<Waypoint> wall;
				
				if (null != neighbor) {
					wall = neighbor.getWall(side.flip());
				} else {
					wall = new ArrayList<>();
				}
				
				this.walls.put(side, Objects.requireNonNull(wall));
			}
			
			public final Stream<Path> streamOutgoingPaths() {
				return this.walls.values().stream()
						.flatMap(Collection::stream)
						.map(Waypoint::getPaths)
						.flatMap(Collection::stream)
						.filter(path -> this == path.getOri());
			}
			
			public static final <K, V> boolean removeValue(final Map<K, V> map, final V value) {
				return map.keySet().removeAll(findKeys(map, value));
			}
			
			public static final <K, V> Collection<K> findKeys(final Map<K, V> map, final V value) {
				return map.entrySet().stream()
						.filter(e -> Objects.equals(e.getValue(), value))
						.map(Map.Entry::getKey)
						.collect(Collectors.toSet());
			}
			
			public final Waypoint waypoint(final Side side, final Path path) {
				final var sameOri = this.groupByOri;
				final var sameDst = this.groupByDst.computeIfAbsent(side, __ -> new HashMap<>());
				var result = sameDst.get(path.getDst());
				
				if (null != result) {
					removeValue(sameOri, result);
					
					return result;
				}
				
				result = sameOri.get(path.getOri());
				
				if (null != result) {
					removeValue(sameDst, result);
					
					return result;
				}
				
				result = new Waypoint(this, side);
				
				this.getWall(side).add(result);
				
				sameDst.put(path.getDst(), result);
				sameOri.put(path.getOri(), result);
				
				return result;
			}
			
			public final Graph.Node getNode() {
				return this.node;
			}
			
			public final void setNode(final Graph.Node node) {
				this.node = node;
			}
			
			@Override
			public final String toString() {
				return String.format("@%s,%s<%s>", this.getRowIdx(), this.getColIdx(), this.getNode());
			}
			
			private static final Point2D ZZ = new Point2D.Double(0.0, 0.0);
			private static final Point2D PZ = new Point2D.Double(+1.0, 0.0);
			private static final Point2D NZ = new Point2D.Double(-1.0, 0.0);
			private static final Point2D ZP = new Point2D.Double(0.0, +1.0);
			private static final Point2D ZN = new Point2D.Double(0.0, -1.0);
			private static final Point2D PP = new Point2D.Double(+1.0, +1.0);
			
			/**
			 * @author 2oLDNncs 20250416
			 */
			public static enum Side {
				
				NORTH(D_N) {
					
					@Override
					public final Side flip() {
						return SOUTH;
					}
					
					@Override
					public final Point2D getGeomWallOri() {
						return ZZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
						return PZ;
					}
					
				}, WEST(D_W) {
					
					@Override
					public final Side flip() {
						return EAST;
					}
					
					@Override
					public final Point2D getGeomWallOri() {
						return ZZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
						return ZP;
					}
					
				}, SOUTH(D_S) {
					
					@Override
					public final Side flip() {
						return NORTH;
					}
					
					@Override
					public final Point2D getGeomWallOri() {
						return ZP;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
						return PZ;
					}
					
				}, EAST(D_E) {
					
					@Override
					public final Side flip() {
						return WEST;
					}
					
					@Override
					public final Point2D getGeomWallOri() {
						return PZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
						return ZP;
					}
					
				};
				
				private final Delta delta;
				
				private Side(final Delta delta) {
					this.delta = delta;
				}
				
				public final Delta getDelta() {
					return this.delta;
				}
				
				public abstract Side flip();
				
				public abstract Point2D getGeomWallOri();
				
				public abstract Point2D getGeomWallDir();

				public final int dr() {
					return this.getDelta().dr();
				}
				
				public final int dc() {
					return this.getDelta().dc();
				}
				
				public static final Side[] N_W_S_E = { NORTH, WEST, SOUTH, EAST };
				public static final Side[] N_E_S_W = { NORTH, EAST, SOUTH, WEST };
				
			}
			
			public static final Delta D_N = delta(-1, 0);
			public static final Delta D_W = delta(0, -1);
			public static final Delta D_E = D_W.flip();
			public static final Delta D_S = D_N.flip();
			public static final Delta D_NW = D_N.plus(D_W);
			public static final Delta D_NE = D_N.plus(D_E);
			public static final Delta D_SW = D_S.plus(D_W);
			public static final Delta D_SE = D_S.plus(D_E);
			
			private static final Delta delta(final int dr, final int dc) {
				return new Delta(dr, dc);
			}
			
			/**
			 * @author 2oLDNncs 20250419
			 */
			public static record Delta(int dr, int dc, double dd) {
				
				public Delta(final int dr, final int dc) {
					this(dr, dc, Point2D.distance(0.0, 0.0, dc, dr));
				}
				
				public final Delta flip() {
					return new Delta(-this.dr(), -this.dc());
				}
				
				public final Delta plus(final Delta delta) {
					return new Delta(this.dr() + delta.dr(), this.dc() + delta.dc());
				}
				
			}
			
		}
		
		/**
		 * @author 2oLDNncs 20250415
		 */
		public static final class Waypoint {
			
			private final Cell cell;
			
			private final Cell.Side side;
			
			private Waypoint previous;
			
			private Waypoint next;
			
			private final List<Path> paths = new ArrayList<>();
			
			public Waypoint(final Cell cell, final Cell.Side side) {
				this.cell = cell;
				this.side = side;
			}
			
			public final Waypoint getPrevious() {
				return this.previous;
			}
			
			public final void setPrevious(final Waypoint previous) {
				this.previous = previous;
			}
			
			public final Waypoint getNext() {
				return this.next;
			}
			
			public final void setNext(final Waypoint next) {
				this.next = next;
			}
			
			public final Cell getCell() {
				return this.cell;
			}
			
			public final Cell.Side getSide() {
				return this.side;
			}
			
			public final List<Path> getPaths() {
				return this.paths;
			}
			
			public final double getPosition() {
				final var wall = this.getCell().getWall(this.getSide());
				
				return (wall.indexOf(this) + 1.0) / (wall.size() + 1.0);
			}
			
			@Override
			public final String toString() {
				return String.format("%s.%s", this.getCell(), this.getSide());
			}
			
		}
		
		/**
		 * @author 2oLDNncs 20250416
		 */
		public static final class Path {
			
			private final Cell ori;
			
			private final Cell dst;
			
			private final List<Waypoint> waypoints = new ArrayList<>();
			
			public Path(final Cell ori, final Cell dst) {
				this.ori = ori;
				this.dst = dst;
			}
			
			public final Cell getOri() {
				return this.ori;
			}
			
			public final Cell getDst() {
				return this.dst;
			}
			
			public final List<Waypoint> getWaypoints() {
				return this.waypoints;
			}
			
			public final void prependWaypoint(final Cell cell, final Cell.Side side) {
				final var wp = cell.waypoint(side, this);
				
				wp.getPaths().add(this);
				
				this.getWaypoints().add(0, wp);
			}
			
			@Override
			public final String toString() {
//				return String.format("%s -> %s %s", this.getOri(), this.getDst(), this.getWaypoints());
				return String.format("%s -> %s", this.getOri(), this.getDst());
			}
			
		}
		
	}
	
}
