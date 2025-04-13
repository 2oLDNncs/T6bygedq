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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import t6bygedq.lib.Helpers;
import t6bygedq.lib.Log;
import t6bygedq.lib.LogLevel;

/**
 * @author 2oLDNncs 20250401
 */
@LogLevel(3)
public final class GraphLayout {
	
	private static final String K_DEBUG = "Debug";
	
	public static final void main(final String[] args) throws IOException, XMLStreamException {
		final var g = new Graph();
		
		{
			Log.begin(1, "Creating graph");
			
			final var nbNodes = 40;
			final var nbEdges = nbNodes * nbNodes * 128 / 2048;
			final var rand = new Random(nbNodes);
			
			IntStream.range(0, nbNodes).forEach(__ -> g.addNode());
			
			{
				final var allPossibleEdges = IntStream.range(0, nbNodes * nbNodes)
						.mapToObj(Integer::valueOf)
						.collect(Collectors.toCollection(ArrayList::new));
				
				Collections.shuffle(allPossibleEdges, rand);
				
				allPossibleEdges.subList(0, nbEdges).forEach(e -> g.getNode(e / nbNodes).edgeTo(g.getNode(e % nbNodes)));
			}
			
			Log.done();
		}
		
		applyHierarchicalGridLayout(g);
		
		writeSvg(g, "data/graph.svg");
	}
	
	public static void applyHierarchicalGridLayout(final Graph g) {
		Log.begin(1, "Appying Hierarchical Grid Layout");
		Log.outf(2, "Nodes: %s Edges: %s", g.countNodes(), g.countEdges());
		
		final var roots = g.nodes.stream().filter(Graph.Node::isRoot).toList();
		
		Log.outf(2, "Roots: %s", roots.size());
		
//		final var grid = new TreeMap<Integer, List<Graph.Node>>();
		final var grid = new ArrayList<List<Graph.Node>>();
		final IntFunction<List<Graph.Node>> getRow = rowIndex -> computeIfAbsent(grid, rowIndex);
		
		computeNodeDepth(roots, getRow);
		
		g.forEach(node -> computeNodeDepth(Arrays.asList(node), getRow));
		
		if (Log.isEnabled(4)) {
			g.forEach(node -> {
				Log.outf(0, "%s %s", node.getIndex(), node.props);
			});
		}
		
		if (Log.isEnabled(3)) {
			IntStream.range(0, grid.size()).forEach(k -> {
				Log.outf(0, "%s%s", k, grid.get(k));
			});
		}
		
		{
			final var gridBounds = new Rectangle();
			
			g.forEach(node -> {
				gridBounds.add((int) node.props.get(K_COL) + 1, (int) node.props.get(K_ROW) + 1);
			});
			
			Log.out(1, gridBounds);
			
			final var gridWalls = new HashMap<Collection<Point2D>, GridWall>();
			final var count = new AtomicLong();
			
			g.forEach(srcNode -> {
				final var srcRow = (int) srcNode.props.get(K_ROW);
				final var srcCol = (int) srcNode.props.get(K_COL);
				
				srcNode.forEachOutgoing(edge -> {
					final var dstNode = edge.getEndNode();
					final var path = new ArrayList<Point2D>();
					
					final Consumer<Point2D> addToPath = p -> {
						if (path.isEmpty()) {
							path.add(p);
						} else {
							final var tmp = (Point2D) Helpers.last(path).clone();
							final var dx = Math.signum(p.getX() - tmp.getX()) / 2.0;
							final var dy = Math.signum(p.getY() - tmp.getY()) / 2.0;
							
							if (0.5 <= tmp.distance(p)) {
								tmp.setLocation(tmp.getX() + dx, tmp.getY() + dy);
								
								while (0.5 <= tmp.distance(p)) {
									path.add((Point2D) tmp.clone());
									tmp.setLocation(tmp.getX() + dx, tmp.getY() + dy);
								}
								
								path.add(p);
							}
						}
						if (path.isEmpty() || !Helpers.last(path).equals(p)) {
							path.add(p);
						}
					};
					
					addToPath.accept(new Point2D.Double(srcCol, srcRow));
					
					final var dstRow = (int) dstNode.props.get(K_ROW);
					final var dstCol = (int) dstNode.props.get(K_COL);
					
					if (dstRow < srcRow) { // North
						if (dstCol < srcCol) { // West
							addToPath.accept(new Point2D.Double(srcCol - 0.5, srcRow));
							addToPath.accept(new Point2D.Double(srcCol - 0.5, dstRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol + 0.5, dstRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol + 0.5, dstRow));
						} else if (srcCol == dstCol) {
							if (dstRow + 1 == srcRow) {
								addToPath.accept(new Point2D.Double(srcCol, srcRow - 0.5));
							} else {
								addToPath.accept(new Point2D.Double(srcCol - 0.5, srcRow));
								addToPath.accept(new Point2D.Double(srcCol - 0.5, dstRow));
							}
						} else if (srcCol < dstCol) { // East
							addToPath.accept(new Point2D.Double(srcCol + 0.5, srcRow));
							addToPath.accept(new Point2D.Double(srcCol + 0.5, dstRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol - 0.5, dstRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol - 0.5, dstRow));
						}
					} else if (srcRow == dstRow) {
						if (dstCol < srcCol) { // West
							if (dstCol + 1 == srcCol) {
								addToPath.accept(new Point2D.Double(srcCol - 0.5, srcRow));
							} else {
								addToPath.accept(new Point2D.Double(srcCol - 0.5, srcRow));
								addToPath.accept(new Point2D.Double(srcCol - 0.5, srcRow + 0.5));
								addToPath.accept(new Point2D.Double(dstCol + 0.5, dstRow + 0.5));
								addToPath.accept(new Point2D.Double(dstCol + 0.5, dstRow));
							}
						} else if (srcCol == dstCol) {
							addToPath.accept(new Point2D.Double(srcCol + 0.5, srcRow));
							addToPath.accept(new Point2D.Double(srcCol + 0.5, srcRow - 0.5));
							addToPath.accept(new Point2D.Double(srcCol, srcRow - 0.5));
						} else if (srcCol < dstCol) { // East
							if (srcCol + 1 == dstCol) {
								addToPath.accept(new Point2D.Double(srcCol + 0.5, srcRow));
							} else {
								addToPath.accept(new Point2D.Double(srcCol + 0.5, srcRow));
								addToPath.accept(new Point2D.Double(srcCol + 0.5, srcRow + 0.5));
								addToPath.accept(new Point2D.Double(dstCol - 0.5, dstRow + 0.5));
								addToPath.accept(new Point2D.Double(dstCol - 0.5, dstRow));
							}
						}
					} else if (srcRow < dstRow) { // South
						if (dstCol < srcCol) { // West
							addToPath.accept(new Point2D.Double(srcCol, srcRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol + 0.5, srcRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol + 0.5, dstRow - 0.5));
							addToPath.accept(new Point2D.Double(dstCol, dstRow - 0.5));
						} else if (srcCol == dstCol) {
							if (srcRow + 1 == dstRow) {
								addToPath.accept(new Point2D.Double(srcCol, srcRow + 0.5));
							} else {
								addToPath.accept(new Point2D.Double(srcCol, srcRow + 0.5));
								addToPath.accept(new Point2D.Double(srcCol - 0.5, srcRow + 0.5));
								addToPath.accept(new Point2D.Double(dstCol - 0.5, dstRow - 0.5));
								addToPath.accept(new Point2D.Double(dstCol, dstRow - 0.5));
							}
						} else if (srcCol < dstCol) { // East
							addToPath.accept(new Point2D.Double(srcCol, srcRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol - 0.5, srcRow + 0.5));
							addToPath.accept(new Point2D.Double(dstCol - 0.5, dstRow - 0.5));
							addToPath.accept(new Point2D.Double(dstCol, dstRow - 0.5));
							edge.props.put(K_DEBUG, count.getAndIncrement() == 0L);
						}
					}
					
					addToPath.accept(new Point2D.Double(dstCol, dstRow));
					
//					Log.outf(1, "%s", path);
					
					edge.props.put(K_GRID_PATH, path);
					
					final var segments = new ArrayList<List<Point2D>>(path.size());
					final var walls = new ArrayList<GridWall>(path.size());
					
					for (var i = 0; i + 1 < path.size(); i += 1) {
						final var segment = Arrays.asList((Point2D) path.get(i).clone(), (Point2D) path.get(i + 1).clone());
						segments.add(segment);
						final var wall = gridWalls.computeIfAbsent(Set.of(path.get(i), path.get(i + 1)), GridWall::new);
						walls.add(wall);
						
						wall.add(edge, segment);
					}
					
					edge.props.put(K_GRID_SEGMENTS, segments);
					edge.props.put(K_GRID_WALLS, walls);
				});
			});
			
			g.forEach(node -> {
				node.forEachOutgoing(edge -> {
					final List<List<Point2D>> segments = Helpers.cast(edge.props.get(K_GRID_SEGMENTS));
					final List<GridWall> walls = Helpers.cast(edge.props.get(K_GRID_WALLS));
					
					for (var i = 0; i < segments.size(); i += 1) {
						final var segment = segments.get(i);
						final var offset = walls.get(i).getSegmentOffset(edge, segment);
						
//						Log.out(1, segments.get(i), offset);
						segment.forEach(p -> {
							p.setLocation(p.getX() + offset.getX(), p.getY() + offset.getY());
						});
//						Log.out(1, segments.get(i));
						
					}
				});
			});
		}
		
		Log.done();
	}
	
	/**
	 * @author 2oLDNncs 20250413
	 */
	public static final class MyXmlStreamWriter implements XMLStreamWriter {
		
		private final PrintStream svgOut;
		
		private final XMLStreamWriter delegate;
		
		public MyXmlStreamWriter(final PrintStream svgOut) throws XMLStreamException, FactoryConfigurationError {
			this.svgOut = svgOut;
			this.delegate = XMLOutputFactory.newFactory().createXMLStreamWriter(this.svgOut);
		}
		
		@Override
		public final void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
			tic();
			this.delegate.writeStartElement(prefix, localName, namespaceURI);
			toc("writeStartElement-1");
		}

		@Override
		public final void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
			tic();
			this.delegate.writeStartElement(namespaceURI, localName);
			toc("writeStartElement-2");
		}

		@Override
		public final void writeStartElement(final String localName) throws XMLStreamException {
			tic();
			this.delegate.writeStartElement(localName);
			toc("writeStartElement-3");
		}

		@Override
		public final void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
			tic();
			this.delegate.writeStartDocument(version);
			toc("writeStartDocument-1");
		}

		@Override
		public final void writeStartDocument(final String version) throws XMLStreamException {
			tic();
			this.delegate.writeStartDocument(version);
			toc("writeStartDocument-2");
		}

		@Override
		public final void writeStartDocument() throws XMLStreamException {
			tic();
			this.delegate.writeStartDocument();
			toc("writeStartDocument-3");
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public final void writeEndElement() throws XMLStreamException {
			tic();
			this.delegate.writeEndElement();
			toc("writeEndElement");
		}

		@Override
		public final void writeEndDocument() throws XMLStreamException {
			tic();
			this.delegate.writeEndDocument();
			toc("writeEndDocument");
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeComment(String data) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public final void writeCharacters(final String text) throws XMLStreamException {
			tic();
			this.delegate.writeCharacters(text);
			toc("writeCharacters");
		}

		@Override
		public void writeCData(String data) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public final void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value)
				throws XMLStreamException {
			tic();
			this.delegate.writeAttribute(prefix, namespaceURI, localName, value);
			toc("writeAttribute-1");
		}

		@Override
		public final void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException {
			tic();
			this.delegate.writeAttribute(namespaceURI, localName, value);
			toc("writeAttribute-2");
		}

		@Override
		public final void writeAttribute(final String localName, final String value) throws XMLStreamException {
			tic();
//			delegate is way too slow for this method (about 700 000 times slower! WTF)
//			this.delegate.writeAttribute(localName, value);
			this.svgOut.print(String.format(" %s=\"%s\"", localName, escape(value)));
			toc("writeAttribute-3");
		}
		
		@Override
		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NamespaceContext getNamespaceContext() {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void flush() throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() throws XMLStreamException {
			Log.out(1, "TODO");
			// TODO Auto-generated method stub
			
		}
		
		public static final String escape(final String string) {
			final var result = new StringBuilder();
			
			for (var i = 0; i < string.length(); i += 1) {
				final var c = string.charAt(i);
				
				switch (c) {
				case '"':
					result.append("&quot;");
					break;
				case '&':
					result.append("&amp;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				default:
					result.append(c);
					break;
				}
			}
			
			return result.toString();
		}
		
	}

	/**
	 * @author 2oLDNncs 20250412
	 */
	public static final class GridWall {
		
		private final List<Point2D> keyPoints;
		
		private final Point2D keyDir = new Point2D.Double(0.0, 1.0);
		private final Point2D wallDir = new Point2D.Double(1.0, 0.0);
		
		private final List<Graph.Edge> edges = new ArrayList<>();
		
		private final Map<Graph.Node, List<Graph.Edge>> groupBySrc1 = new LinkedHashMap<>();
		private final Map<Graph.Node, List<Graph.Edge>> groupByDst1 = new LinkedHashMap<>();
		private final Map<Graph.Node, List<Graph.Edge>> groupBySrc2 = new LinkedHashMap<>();
		private final Map<Graph.Node, List<Graph.Edge>> groupByDst2 = new LinkedHashMap<>();
		
		public GridWall(final Collection<Point2D> key) {
			this.keyPoints = new ArrayList<>(key);
			
			if (this.keyPoints.get(0).getX() != this.keyPoints.get(1).getX()) {
				this.keyDir.setLocation(1.0, 0.0);
				this.wallDir.setLocation(0.0, 1.0);
			}
			
			Log.out(4, key, this.wallDir);
		}
		
		public final void add(final Graph.Edge edge, final List<Point2D> segment) {
			this.edges.add(edge);
			
			final var dot = this.computeSegmentDot(segment);
			
			if (dot > 0.0) {
				computeIfAbsent(this.groupBySrc1, edge.getStartNode()).add(edge);
				computeIfAbsent(this.groupByDst1, edge.getEndNode()).add(edge);
			} else {
				computeIfAbsent(this.groupBySrc2, edge.getStartNode()).add(edge);
				computeIfAbsent(this.groupByDst2, edge.getEndNode()).add(edge);
			}
		}
		
		private final double computeSegmentDot(final List<Point2D> segment) {
			final var segmentDir = new Point2D.Double(
					segment.get(1).getX() - segment.get(0).getX(),
					segment.get(1).getY() - segment.get(0).getY());
			
			return this.keyDir.getX() * segmentDir.getX() + this.keyDir.getY() * segmentDir.getY();
		}
		
		public final Point2D getSegmentOffset(final Graph.Edge edge, final List<Point2D> segment) {
			var i = this.edges.indexOf(edge);
			var n = this.edges.size();
			
			final var ns1 = this.groupBySrc1.size();
			final var nd1 = this.groupByDst1.size();
			final var ns2 = this.groupBySrc2.size();
			final var nd2 = this.groupByDst2.size();
			final var dot = this.computeSegmentDot(segment);
			
			if (ns1 + ns2 < n) {
				i = new ArrayList<>(this.groupBySrc1.keySet()).indexOf(edge.getStartNode());
				
				if (i < 0) {
					i = ns1 + new ArrayList<>(this.groupBySrc2.keySet()).indexOf(edge.getStartNode());
				}
				
				n = ns1 + ns2;
			}
			
			if (ns1 + nd2 < n) {
				i = new ArrayList<>(this.groupBySrc1.keySet()).indexOf(edge.getStartNode());
				
				if (i < 0) {
					i = ns1 + new ArrayList<>(this.groupByDst2.keySet()).indexOf(edge.getEndNode());
				}
				
				n = ns1 + nd2;
			}
			
			if (nd1 + ns2 < n) {
				i = new ArrayList<>(this.groupByDst1.keySet()).indexOf(edge.getEndNode());
				
				if (i < 0) {
					i = nd1 + new ArrayList<>(this.groupBySrc2.keySet()).indexOf(edge.getStartNode());
				}
				
				n = nd1 + ns2;
			}
			
			if (nd1 + nd2 < n) {
				i = new ArrayList<>(this.groupByDst1.keySet()).indexOf(edge.getEndNode());
				
				if (i < 0) {
					i = nd1 + new ArrayList<>(this.groupByDst2.keySet()).indexOf(edge.getEndNode());
				}
				
				n = nd1 + nd2;
			}
			
			if (i < 0) {
				throw new IllegalStateException();
			}
			
			final var t = (i + 1.0) / (n + 1.0);
			final var scale = 0.5;
			
			return new Point2D.Double(
					lerp(-this.wallDir.getX() * scale / 2.0, this.wallDir.getX() * scale / 2.0, t),
					lerp(-this.wallDir.getY() * scale / 2.0, this.wallDir.getY() * scale / 2.0, t));
		}
		
	}
	
	public static final void writeSvg(final Graph g, final String filePath)
			throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
		Log.beginf(1, "Writing svg %s", filePath);
		
		try (final var svgOut = new PrintStream(filePath)) {
			final var svg = new MyXmlStreamWriter(svgOut);
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
					
					
					svg.writeAttribute("test", "<\"&>");
					
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
						
						xmlElement(svg, "text", () -> {
							svg.writeAttribute("text-anchor", "middle");
							svg.writeAttribute("dominant-baseline", "central");
							svg.writeAttribute("font-size", "" + cellHeight * 6.0 / 20.0);
							svg.writeAttribute("x", Integer.toString(nodeX));
							svg.writeAttribute("y", Integer.toString(nodeY));
							svg.writeCharacters(node.props.getOrDefault("Label", "").toString());
						});
						
						final var a1 = getOutline(node, nodeWidth, nodeHeight);
						
						for (final var edge : node.outgoingEdges) {
							final List<Point2D> gridPath = Helpers.cast(edge.props.get(K_GRID_PATH));
							final List<List<Point2D>> gridSegments = Helpers.cast(edge.props.get(K_GRID_SEGMENTS));
							final var dBuilder = new StringBuilder();
							
							final BiConsumer<String, Point2D> appendXY = (t, p) -> {
								dBuilder.append(t);
								dBuilder.append(gridOffsetX + p.getX() * cellWidth);
								dBuilder.append(",");
								dBuilder.append(gridOffsetY + p.getY() * cellHeight);
							};
							
							if (null != gridSegments) {
								for (var i = 0; i + 1 < gridSegments.size(); i += 1) {
									final var si = gridSegments.get(i);
									final var sj = gridSegments.get(i + 1);
									appendXY.accept("M", lerp(si.get(0), si.get(1), 0.5));
									appendXY.accept("C", lerp(si.get(0), si.get(1), 0.75));
									appendXY.accept(" ", lerp(sj.get(0), sj.get(1), 0.25));
									appendXY.accept(" ", lerp(sj.get(0), sj.get(1), 0.5));
								}
								
								xmlElement(svg, "path", () -> {
									svg.writeAttribute("fill", "none");
									if (node == edge.getEndNode()) {
										svg.writeAttribute("stroke", "red");
//										Log.out(1, gridSegments);
									} else {
										svg.writeAttribute("stroke", "black");
									}
									svg.writeAttribute("stroke-width", "0.25");
									if (Boolean.TRUE.equals(edge.props.get(K_DEBUG)) || true) {
										svg.writeAttribute("d", dBuilder.toString());
									}
									svg.writeAttribute("marker-end", "url(#head)");
								});
								
								if (false) {
									final BiConsumer<Point2D, String> makeDot = (p, c) -> {
										try {
											final double rScale;
											
											if ("red".equals(c)) {
												rScale = 0.5;
											} else if ("green".equals(c)) {
												rScale = 0.75;
											} else {
												rScale = 1.0;
											}
											
											xmlElement(svg, "ellipse", () -> {
												svg.writeAttribute("fill", c);
												svg.writeAttribute("stroke", "none");
												svg.writeAttribute("cx", "" + (gridOffsetX + p.getX() * cellWidth));
												svg.writeAttribute("cy", "" + (gridOffsetY + p.getY() * cellHeight));
												svg.writeAttribute("rx", "" + nodeWidth * rScale / 16.0);
												svg.writeAttribute("ry", "" + nodeHeight * rScale / 16.0);
												svg.writeAttribute("style", "");
											});
										} catch (final XMLStreamException e) {
											throw new RuntimeException(e);
										}
									};
									
									for (final var segment : gridSegments) {
										makeDot.accept(segment.get(0), "red");
										makeDot.accept(lerp(segment.get(0), segment.get(1), 0.5), "green");
										makeDot.accept(segment.get(1), "blue");
									}
								}
							} else if (null != gridPath) {
								appendXY.accept("M", lerp(gridPath.get(0), gridPath.get(1), 1.0 / 8.0));
								
								if (2 < gridPath.size()) {
									appendXY.accept("L", gridPath.get(1));
								} else {
									appendXY.accept("L", lerp(gridPath.get(0), gridPath.get(1), 7.0 / 8.0));
								}
								
								for (var i = 2; i < gridPath.size(); i += 1) {
									if (i + 1 < gridPath.size()) {
										appendXY.accept(" ", gridPath.get(i));
									} else {
										appendXY.accept(" ", lerp(gridPath.get(i - 1), gridPath.get(i), 7.0 / 8.0));
									}
								}
								
								xmlElement(svg, "path", () -> {
									svg.writeAttribute("fill", "none");
									svg.writeAttribute("stroke", "black");
									if (edge.props.containsKey(K_DEBUG) || true) {
										svg.writeAttribute("d", dBuilder.toString());
									}
									svg.writeAttribute("marker-end", "url(#head)");
								});
							} else if (node == edge.getEndNode()) {
								xmlElement(svg, "path", () -> {
									svg.writeAttribute("fill", "none");
									svg.writeAttribute("stroke", "black");
									svg.writeAttribute("d", String.format("M%s,%sC%s,%s %s,%s %s,%s",
											nodeX + cellWidth * 1/ 8,
											nodeY - cellHeight * 1/ 8,
											nodeX + cellWidth * 4 / 8,
											nodeY - cellHeight * 5 / 8,
											nodeX - cellWidth * 4 / 8,
											nodeY - cellHeight * 5 / 8,
											nodeX - cellWidth * 1/ 8,
											nodeY - cellHeight * 1/ 8));
									svg.writeAttribute("marker-end", "url(#head)");
								});
							} else {
								final var a2 = new Area(a1);
								final var a3 = new Area(getOutline(edge.getEndNode(), nodeWidth, nodeHeight));
								final var a4 = getOutline(edge);
								
								a2.intersect(a4);
								a3.intersect(a4);
								
								if (a2.isEmpty()) {
									Log.err(1, "Failed to compute geometric origin for edge %s", edge);
								}
								
								if (a3.isEmpty()) {
									Log.err(1, "Failed to compute geometric destination for edge %s", edge);
								}
								
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
					}
				});
				
				svg.writeEndDocument();
			}
		}
		
		Log.done();
		
		times.forEach((k, v) -> {
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
	
	/**
	 * @author 2oLDNncs 20250413
	 */
	public static final class Stats {
		
		private long count;
		
		private double sum;
		
		private double min;
		
		private double max;
		
		public final long getCount() {
			return this.count;
		}
		
		public final double getSum() {
			return this.sum;
		}
		
		public final double getMin() {
			return this.min;
		}
		
		public final double getMax() {
			return this.max;
		}
		
		public final double getAverage() {
			return this.getSum() / this.getCount();
		}
		
		public final void addValue(final double value) {
			this.addValue(value, 1L);
		}
		
		public final void addValue(final double value, final long count) {
			if (value < this.min) {
				this.min = value;
			}
			
			if (this.max < value) {
				this.max = value;
			}
			
			this.sum += count * value;
			this.count += count;
		}
		
		@Override
		public final String toString() {
			return String.format("{min:%s max:%s sum:%s nb:%s avg:%s}",
					this.getMin(), this.getMax(), this.getSum(), this.getCount(), this.getAverage());
		}
		
	}
	
	private static final Stack<Long> tics = new Stack<>();
	private static final Map<String, Stats> times = new HashMap<>();
	
	public static final void tic() {
		tics.push(System.currentTimeMillis());
	}
	
	public static final void toc(final String key) {
		times.computeIfAbsent(key, __ -> new Stats()).addValue(System.currentTimeMillis() - tics.pop());
	}
	
	public static final void xmlElement(final XMLStreamWriter writer, final String localName,
			final ElementBuilder builder) throws XMLStreamException {
		tic();
		writer.writeStartElement(localName);
		builder.build();
		writer.writeEndElement();;
		toc(localName);
	}
	
	/**
	 * @author 2oLDNncs 20250403
	 */
	public static abstract interface ElementBuilder {
		
		public abstract void build() throws XMLStreamException;
		
	}
	
	public static final String K_ROW = "Row";
	public static final String K_COL = "Col";
	public static final String K_GRID_PATH = "GridPath";
	public static final String K_GRID_SEGMENTS = "GridSegments";
	public static final String K_GRID_WALLS = "GridWalls";
	
	public static final <K, E> List<E> computeIfAbsent(final Map<K, List<E>> map, final K key) {
		return map.computeIfAbsent(key, __ -> new ArrayList<>());
	}
	
	public static final <E> List<E> computeIfAbsent(final List<List<E>> map, final int key) {
		while (map.size() <= key) {
			map.add(new ArrayList<>());
		}
		
		return map.get(key);
	}
	
	public static final void computeNodeDepth(final Collection<Graph.Node> nodes, final IntFunction<List<Graph.Node>> getRow) {
		final var todo = new ArrayList<Graph.Node>();
		
		final Function<Integer, Consumer<Graph.Node>> applyDepth = d -> node -> {
			final var row = getRow.apply(d);
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
	 * @author 2oLDNncs 20250402
	 */
	public static abstract class Obj {
		
		public final Map<String, Object> props = new HashMap<>();
		
	}
	
	/**
	 * @author 2oLDNncs 20250401
	 */
	public static final class Graph extends Obj {
		
		private final List<Node> nodes = new ArrayList<>();
		
		public final Node addNode() {
			final var result = new Node(this.nodes.size());
			
			result.props.put("Label", "" + this.nodes.size());
			
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
