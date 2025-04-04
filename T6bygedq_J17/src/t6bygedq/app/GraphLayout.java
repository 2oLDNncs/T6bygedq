package t6bygedq.app;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250401
 */
public final class GraphLayout {
	
	public static final void main(final String[] args) throws IOException, XMLStreamException {
		final var g = new Graph();
		
		{
			System.out.println(Helpers.dformat("Creating graph..."));
			
			final var nbNodes = 4000;
			final var nbEdges = nbNodes * nbNodes * 1 / 1024;
			final var rand = new Random(nbNodes);
			
			IntStream.range(0, nbNodes).forEach(__ -> g.addNode());
			
			{
				final var allPossibleEdges = IntStream.range(0, nbNodes * nbNodes)
						.mapToObj(Integer::valueOf)
						.collect(Collectors.toCollection(ArrayList::new));
				
				Collections.shuffle(allPossibleEdges, rand);
				
				allPossibleEdges.subList(0, nbEdges).forEach(e -> g.getNode(e / nbNodes).edgeTo(g.getNode(e % nbNodes)));
			}
			
			System.out.println(Helpers.dformat("Creating graph... Done"));
		}
		
		applyHierarchicalGridLayout(g);
		
		writeSvg(g, "data/graph.svg");
	}
	
	public static final void writeSvg(final Graph g, final String filePath)
			throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
		System.out.println(Helpers.dformat("Writing svg %s...", filePath));
		
		try (final var svgOut = new PrintStream(filePath)) {
			final var svg = XMLOutputFactory.newFactory().createXMLStreamWriter(svgOut);
			final var cellWidth = 40;
			final var cellHeight = 20;
			final var gridOffsetX = cellWidth / 2;
			final var gridOffsetY = cellHeight / 2;
			final var nodeWidth = cellHeight / 2;
			final var nodeHeight = nodeWidth;
			final var viewBox = new Rectangle();
			
			for (final var node : g.nodes) {
				final var nodeX = getGeomPos(node, K_X, K_COL, gridOffsetX, cellWidth);
				final var nodeY = getGeomPos(node, K_Y, K_ROW, gridOffsetY, cellHeight);
				
				viewBox.add(nodeX - gridOffsetX, nodeY - gridOffsetY);
				viewBox.add(nodeX + gridOffsetX, nodeY + gridOffsetY);
			}
			
			{
				svg.writeStartDocument();
				
				xmlElement(svg, "svg", () -> {
					svg.writeAttribute("xmlns", "http://www.w3.org/2000/svg");
					svg.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
					svg.writeAttribute("viewBox", String.format("%s %s %s %s",
							viewBox.x, viewBox.y, viewBox.width, viewBox.height));
					
					xmlElement(svg, "defs", () -> {
						xmlElement(svg, "marker", () -> {
							svg.writeAttribute("id", "head");
							svg.writeAttribute("orient", "auto");
							svg.writeAttribute("markerWidth", "3");
							svg.writeAttribute("markerHeight", "4");
							svg.writeAttribute("refX", "1.6");
							svg.writeAttribute("refY", "2");
							
							xmlElement(svg, "path", () -> {
								svg.writeAttribute("d", "M0,0 V4 L2,2 Z");
								svg.writeAttribute("fill", "black");
							});
						});
					});
					
					for (final var node : g.nodes) {
						final var nodeX = (int) node.props.get(K_X);
						final var nodeY = (int) node.props.get(K_Y);
						
						xmlElement(svg, "ellipse", () -> {
							svg.writeAttribute("fill", "none");
							svg.writeAttribute("stroke", "black");
							svg.writeAttribute("cx", Integer.toString(nodeX));
							svg.writeAttribute("cy", Integer.toString(nodeY));
							svg.writeAttribute("rx", "" + nodeWidth / 2);
							svg.writeAttribute("ry", "" + nodeHeight / 2);
							svg.writeAttribute("style", "");
						});
						
						final var a1 = getOutline(node, nodeWidth, nodeHeight);
						
						for (final var edge : node.outgoingEdges) {
							final var a2 = new Area(a1);
							final var a3 = new Area(getOutline(edge.getEndNode(), nodeWidth, nodeHeight));
							final var a4 = getOutline(edge);
							
							a2.intersect(a4);
							a3.intersect(a4);
							
							xmlElement(svg, "path", () -> {
								svg.writeAttribute("fill", "none");
								svg.writeAttribute("stroke", "black");
								svg.writeAttribute("d", String.format("M%s,%sL%s,%s",
										a2.getBounds2D().getCenterX(),
										a2.getBounds2D().getCenterY(),
										a3.getBounds2D().getCenterX(),
										a3.getBounds2D().getCenterY()));
								svg.writeAttribute("marker-end", "url(#head)");
							});
						}
					}
				});
				
				svg.writeEndDocument();
			}
		}
		
		System.out.println(Helpers.dformat("Writing svg %s... Done", filePath));
	}
	
	public static void applyHierarchicalGridLayout(final Graph g) {
		System.out.println(Helpers.dformat("Appying Hierarchical Grid Layout..."));
		
		System.out.println(Helpers.dformat("Nodes: %s Edges: %s", g.countNodes(), g.countEdges()));
		
		final var roots = g.nodes.stream().filter(Graph.Node::isRoot).toList();
		
		System.out.println(Helpers.dformat("Roots: %s", roots.size()));
		
		final var grid = new TreeMap<Integer, List<Graph.Node>>();
		
		computeNodeDepth(roots, grid);
		
		g.nodes.forEach(node -> computeNodeDepth(Arrays.asList(node), grid));
		
//		g.nodes.forEach(node -> System.out.println(Helpers.dformat("%s %s", node.getIndex(), node.props)));
		grid.forEach((k, v) -> {
			System.out.println(Helpers.dformat("%s%s", k, v));
		});
		
		System.out.println(Helpers.dformat("Appying Hierarchical Grid Layout... Done"));
	}
	
	public static final String K_X = "X";
	public static final String K_Y = "Y";
	public static final String K_OUTLINE = "Outline";
	
	private static final int getGeomPos(final Graph.Node node, final String geomKey, final String gridKey, final int geomOffset, final int geomSide) {
		return (int) node.props.computeIfAbsent(geomKey, __ -> {
			return geomOffset + (int) node.props.get(gridKey) * geomSide;
		});
	}
	
	private static final Area getOutline(final Graph.Node node, final int nodeWidth, final int nodeHeight) {
		return (Area) node.props.computeIfAbsent(K_OUTLINE, __ -> {
			final var x = (int) node.props.get(K_X);
			final var y = (int) node.props.get(K_Y);
			final var result = new Area(new Ellipse2D.Double(x - nodeWidth / 2, y - nodeHeight / 2, nodeWidth, nodeHeight));
			
			result.subtract(new Area(new Ellipse2D.Double(x - nodeWidth / 2 + 0.125, y - nodeHeight / 2 + 0.125, nodeWidth - 0.25, nodeHeight - 0.25)));
			
			return result;
		});
	}
	
	private static final Area getOutline(final Graph.Edge edge) {
		return (Area) edge.props.computeIfAbsent(K_OUTLINE, __ -> {
			final int nodeX = (int) edge.getStartNode().props.get(K_X);
			final int nodeY = (int) edge.getStartNode().props.get(K_Y);
			final int endNodeX = (int) edge.getEndNode().props.get(K_X);
			final int endNodeY = (int) edge.getEndNode().props.get(K_Y);
			final var result = new Area(new Rectangle2D.Double(nodeX, nodeY,
					Point2D.distance(nodeX, nodeY, endNodeX, endNodeY),
					0.25));
			
			result.transform(AffineTransform.getRotateInstance(endNodeX - nodeX, endNodeY - nodeY, nodeX, nodeY));
			
			return result;
		});
	}
	
	public static final void xmlElement(final XMLStreamWriter writer, final String localName,
			final ElementBuilder builder) throws XMLStreamException {
		writer.writeStartElement(localName);
		builder.build();
		writer.writeEndElement();;
	}
	
	/**
	 * @author 2oLDNncs 20250403
	 */
	public static abstract interface ElementBuilder {
		
		public abstract void build() throws XMLStreamException;
		
	}
	
	public static final String K_ROW = "Row";
	public static final String K_COL = "Col";
	
	public static final void computeNodeDepth(final Collection<Graph.Node> nodes, final Map<Integer, List<Graph.Node>> grid) {
		final var todo = new ArrayList<Graph.Node>();
		
		final Function<Integer, Consumer<Graph.Node>> applyDepth = d -> node -> {
			final var row = grid.computeIfAbsent(d, __ -> new ArrayList<>());
			node.props.put(K_ROW, d);
			node.props.put(K_COL, row.size());
			row.add(node);
			todo.add(node);
		};
		
		final var applyDepth0 = applyDepth.apply(0);
		
		for (final var node : nodes) {
			if (!node.props.containsKey(K_ROW)) {
				applyDepth0.accept(node);
			}
		}
		
		while (!todo.isEmpty()) {
			final var n0 = todo.remove(todo.size() - 1);
			final var d0 = (int) n0.props.get(K_ROW);
			
			n0.streamOutgoingEdges()
			.map(Graph.Edge::getEndNode)
			.filter(n -> !n.props.containsKey(K_ROW))
			.forEach(applyDepth.apply(d0 + 1));
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250401
	 */
	public static final class Graph {
		
		private final List<Node> nodes = new ArrayList<>();
		
		public final Node addNode() {
			final var result = new Node(this.nodes.size());
			
			this.nodes.add(result);
			
			return result;
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
		 * @author 2oLDNncs 20250402
		 */
		public static abstract class Obj {
			
			public final Map<String, Object> props = new HashMap<>();
			
		}
		
		/**
		 * @author 2oLDNncs 20250402
		 */
		public static final class Node extends Obj {
			
			private final int index;
			
			private final List<Edge> incomingEdges = new ArrayList<>();
			
			private final List<Edge> outgoingEdges = new ArrayList<>();
			
			public Node(final int index) {
				this.index = index;
			}
			
			public final int getIndex() {
				return this.index;
			}
			
			public final boolean isRoot() {
				return this.incomingEdges.isEmpty();
			}
			
			public final boolean isLeaf() {
				return this.outgoingEdges.isEmpty();
			}
			
			public final Edge edgeTo(final Node edgeEndNode) {
				return this.edgeTo(edgeEndNode, i -> {
					final var result = new Edge(this, edgeEndNode);
					
					this.outgoingEdges.add(i, result);
					
					edgeEndNode.addIncomingEdge(result);
					
					return result;
				});
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
				return String.format("%s%s", this.getIndex(), this.props);
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
		    
		}
		
		/**
		 * @author 2oLDNncs 20250402
		 */
		public static final class Edge extends Obj {
			
			private final Node startNode;
			
			private final Node endNode;
			
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
			
			public final int getStartNodeIndex() {
				return this.getStartNode().getIndex();
			}
			
			public final int getEndNodeIndex() {
				return this.getEndNode().getIndex();
			}
			
			@Override
			public final String toString() {
				return String.format("%s->%s%s", this.getStartNodeIndex(), this.getEndNodeIndex(), this.props);
			}
			
		}
		
	}

}
