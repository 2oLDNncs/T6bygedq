package t6bygedq.app;

import static t6bygedq.lib.Helpers.in;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import t6bygedq.app.GraphLayout.LayoutGrid.Cell;
import t6bygedq.app.GraphLayout.LayoutGrid.Cell.Side;
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
	
	private static final String K_DEBUG = "Debug";
	
	public static final void main(final String[] args) throws IOException, XMLStreamException {
		final var g = new Graph();
		
		if (true) {
			Log.begin(1, "Creating graph");
			
			final var nbNodes = 40;
			final var nbEdges = nbNodes * nbNodes * 128 / 2048;
			final var rand = new Random(nbNodes);
			
			IntStream.range(0, nbNodes).forEach(__ -> g.addNode());
			
			{
				final var allPossibleEdges = IntStream.range(0, nbNodes * nbNodes)
						.mapToObj(Integer::valueOf)
						.collect(Helpers.toList());
				
				Collections.shuffle(allPossibleEdges, rand);
				
				allPossibleEdges.subList(0, nbEdges).forEach(e -> {
					final var src = g.getNode(e / nbNodes);
					final var dst = g.getNode(e % nbNodes);
					src.edgeTo(dst);
				});
			}
			
			Log.done();
		} else {
			final var ap = new ArgsParser(args);
			
			ap.setDefault(ARG_IN, "data/test_arcs.txt");
			
			Log.beginf(1, "Loading %s", ap.getPath(ARG_IN));
			
			final var nodeMap = new HashMap<String, Graph.Node>();
			final Function<? super String, ? extends Graph.Node> makeNode = lbl -> {
				final var result = g.addNode();
				
				result.getProps().put(Graph.K_LABEL, lbl);
				
				return result;
			};
			
			Files.lines(ap.getPath(ARG_IN)).forEach(line -> {
				final var elements = line.split("\t");
				final var start = nodeMap.computeIfAbsent(elements[0], makeNode);
				final var end = nodeMap.computeIfAbsent(elements[1], makeNode);
				start.edgeTo(end);
			});
			
			Log.done();
		}
		
		final var lg = applyHierarchicalGridLayout(g);
		
//		writeSvg(g, "data/graph.svg");
		writeSvg(lg, "data/graph.svg");
	}
	
	public static final LayoutGrid applyHierarchicalGridLayout(final Graph g) {
		Log.begin(1, "Appying Hierarchical Grid Layout");
		Log.outf(2, "Nodes: %s Edges: %s", g.countNodes(), g.countEdges());
		
		final var roots = g.nodes.stream().filter(Graph.Node::isRoot).toList();
		
		Log.outf(2, "Roots: %s", roots.size());
		
//		final var grid = new TreeMap<Integer, List<Graph.Node>>();
		final var grid = new ArrayList<List<Graph.Node>>();
		final IntFunction<List<Graph.Node>> getRow = rowIndex -> computeIfAbsent(grid, rowIndex);
		
		computeNodeDepth(roots, getRow);
		
		g.forEach(node -> computeNodeDepth(Arrays.asList(node), getRow));
		
		reorderNodesInEachRow(grid);
		
		final var compWidths = computeCompWidths(grid);
		
		Log.out(1, compWidths);
		
		final var compOffsets = computeCompOffsets(compWidths);
		
		Log.out(1, compOffsets);
		
		final var compSubOffsets = computeCompSubOffsets(grid, compOffsets);
		
		Log.out(1, compSubOffsets);
		
		
		if (Log.isEnabled(4)) {
			g.forEach(node -> {
				Log.outf(0, "%s %s", node.getIndex(), node.getProps());
			});
		}
		
		if (Log.isEnabled(3)) {
			IntStream.range(0, grid.size()).forEach(k -> {
				Log.outf(0, "%s%s", k, grid.get(k));
			});
		}
		
		final var lg = new LayoutGrid();
		
		{
			g.forEach(node -> {
				final var compId = node.getCompId();
				final var propRow = (int) node.getProps().get(K_ROW);
				final var propCol = (int) node.getProps().get(K_COL) + compSubOffsets.get(compId).get(propRow);
				
				node.getProps().put(K_COL, propCol);
				
				final var rowIdx = 1 + 2 * propRow;
				final var colIdx = 1 + 2 * propCol;
				
				lg.cell(rowIdx, colIdx).setNode(node);
				lg.cell(rowIdx + 1, colIdx + 1); // east and south borders (north and west already taken care of by rowIdx and colIdx)
			});
			
			g.forEach(node -> {
				lg.forEach(LayoutGrid.Cell::reset);
				
				Log.out(1, node, node.outgoingEdges);
				
				final var targets = node.streamOutgoingEdges()
						.map(Graph.Edge::getEndNode)
						.collect(Collectors.toCollection(HashSet::new));
				final var rowIdx = 1 + 2 * (int) node.getProps().get(K_ROW);
				final var colIdx = 1 + 2 * (int) node.getProps().get(K_COL);
				final var nodeCell = lg.cell(rowIdx, colIdx);
				
				nodeCell.setDistance(0);
				
				if (targets.remove(node)) {
					final var path = new LayoutGrid.Path(nodeCell, nodeCell);
					var pathCell = nodeCell;
					
					for (final var pathSide : Helpers.array(LayoutGrid.Cell.Side.NORTH,
							LayoutGrid.Cell.Side.EAST, LayoutGrid.Cell.Side.SOUTH, LayoutGrid.Cell.Side.WEST)) {
						path.prependWaypoint(pathCell, pathSide);
						pathCell = lg.neighbor(pathCell, pathSide);
					}
				}
				
				final var todo = new ArrayList<LayoutGrid.Cell>();
				
				todo.add(nodeCell);
				
				while (!todo.isEmpty() && !targets.isEmpty()) {
					final var cell = todo.remove(0);
					final var nextDistance = cell.getDistance() + 1;
					
					for (final var side : LayoutGrid.Cell.Side.values()) {
						final var neighbor = lg.neighbor(cell, side);
						
						if (null != neighbor) {
							if (targets.remove(neighbor.getNode())) {
								buildPath(lg, nodeCell, neighbor);
							} else if (null == neighbor.getNode() && nextDistance < neighbor.getDistance()) {
								neighbor.setDistance(nextDistance);
								todo.add(neighbor);
							}
						}
					}
				}
				
				if (!targets.isEmpty()) {
					Log.err(1, node, node.outgoingEdges, targets);
				}
			});
		}
		
		if (false) {
			final var gridBounds = new Rectangle();
			
			g.forEach(node -> {
				gridBounds.add((int) node.getProps().get(K_COL) + 1, (int) node.getProps().get(K_ROW) + 1);
			});
			
			Log.out(1, gridBounds);
			
			final var gridWalls = new HashMap<Collection<Point2D>, GridWall>();
			final var count = new AtomicLong();
			
			g.forEach(srcNode -> {
				final var srcRow = (int) srcNode.getProps().get(K_ROW);
				final var srcCol = (int) srcNode.getProps().get(K_COL);
				
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
					
					final var dstRow = (int) dstNode.getProps().get(K_ROW);
					final var dstCol = (int) dstNode.getProps().get(K_COL);
					
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
//							edge.getProps().put(K_DEBUG, count.getAndIncrement() == 0L);
						}
					}
					
					addToPath.accept(new Point2D.Double(dstCol, dstRow));
					
//					Log.outf(1, "%s", path);
					
					edge.getProps().put(K_GRID_PATH, path);
					
					final var segments = new ArrayList<List<Point2D>>(path.size());
					final var walls = new ArrayList<GridWall>(path.size());
					
					for (var i = 0; i + 1 < path.size(); i += 1) {
						final var segment = Arrays.asList((Point2D) path.get(i).clone(), (Point2D) path.get(i + 1).clone());
						segments.add(segment);
						final var wall = gridWalls.computeIfAbsent(Set.of(path.get(i), path.get(i + 1)), GridWall::new);
						walls.add(wall);
					}
					
					for (var i = 0; i < segments.size(); i += 1) {
						walls.get(i).add(edge, segments, i);
					}
					
					edge.getProps().put(K_GRID_SEGMENTS, segments);
					edge.getProps().put(K_GRID_WALLS, walls);
					
					if ("29".equals(srcNode.getProps().get(Graph.K_LABEL)) || "29".equals(dstNode.getProps().get(Graph.K_LABEL))) {
						edge.getProps().put(K_DEBUG, true);
					}
				});
			});
			
			g.forEach(node -> {
				node.forEachOutgoing(edge -> {
					final List<List<Point2D>> segments = Helpers.cast(edge.getProps().get(K_GRID_SEGMENTS));
					final List<GridWall> walls = Helpers.cast(edge.getProps().get(K_GRID_WALLS));
					
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
		
		return lg;
	}
	
	private static final NavigableMap<Integer, Map<Integer, Integer>> computeCompSubOffsets(final Iterable<List<Graph.Node>> grid,
			final Map<Integer, Integer> compOffsets) {
		final var compSubOffsets = new TreeMap<Integer, Map<Integer, Integer>>();
		
		grid.forEach(row -> {
			row.forEach(node -> {
				final var compId = node.getCompId();
				final var props = node.getProps();
				compSubOffsets.computeIfAbsent(compId, __ -> new TreeMap<>())
				.computeIfAbsent((Integer) props.get(K_ROW), __ -> compOffsets.get(compId) - (int) props.get(K_COL));
			});
		});
		return compSubOffsets;
	}
	
	private static final void reorderNodesInEachRow(final Iterable<List<Graph.Node>> grid) {
		for (final var j : IntStream.range(0, 10).toArray()) {
			grid.forEach(row -> {
				row.sort((node1, node2) -> {
					final var node1CompId = node1.getCompId();
					final var node2CompId = node2.getCompId();
					
					final var cmpCompId = Integer.compare(node1CompId, node2CompId);
					
					if (0 != cmpCompId) {
						return cmpCompId;
					}
					
					final var node1TargetCol = Stream.concat(
							node1.streamOutgoingEdges().map(Graph.Edge::getEndNode),
							node1.streamIncomingEdges().map(Graph.Edge::getStartNode))
							.map(Graph.Node::getProps)
							.mapToDouble(props -> (int) props.get(K_COL))
							.average().orElse((int) node1.getProps().get(K_COL));
					final var node2TargetCol = Stream.concat(
							node2.streamOutgoingEdges().map(Graph.Edge::getEndNode),
							node2.streamIncomingEdges().map(Graph.Edge::getStartNode))
							.map(Graph.Node::getProps)
							.mapToDouble(props -> (int) props.get(K_COL))
							.average().orElse((int) node2.getProps().get(K_COL));
					
					return Double.compare(node1TargetCol, node2TargetCol);
				});
				
				for (var i = 0; i < row.size(); i += 1) {
					final var before = (int) row.get(i).getProps().get(K_COL);
					
					if (i != before) {
						row.get(i).getProps().put(K_COL, i);
						Log.out(1, j, row.get(i).getProps());
					}
				}
			});
		}
	}
	
	private static final NavigableMap<Integer, Integer> computeCompOffsets(final Map<Integer, Integer> compWidths) {
		final var result = new TreeMap<Integer, Integer>();
		
		compWidths.keySet().forEach(compId -> {
			if (result.isEmpty()) {
				result.put(compId, 0);
			} else {
				final var lastEntry = result.lastEntry();
				
				result.put(compId, compWidths.get(lastEntry.getKey()) + lastEntry.getValue());
			}
		});
		
		return result;
	}
	
	private static final NavigableMap<Integer, Integer> computeCompWidths(final Iterable<List<Graph.Node>> grid) {
		final var result = new TreeMap<Integer, Integer>();
		
		grid.forEach(row -> {
			final var compBounds = new HashMap<Integer, Rectangle>();
			
			for (var i = 0; i < row.size(); i += 1) {
				final var node = row.get(i);
				
				node.getProps().put(K_COL, i);
				compBounds.computeIfAbsent(node.getCompId(), __ -> new Rectangle(-1, -1)).add(i, 0);
			}
			
			compBounds.forEach((compId, bounds) -> {
				result.compute(compId, (__, old) -> Math.max(1 + bounds.width, null == old ? 0 : old));
			});
		});
		
		return result;
	}
	
	private static final void buildPath(final LayoutGrid lg, final Cell fromCell, final Cell toCell) {
		final var path = new LayoutGrid.Path(fromCell, toCell);
		var pathCell = toCell;
		
		while (0 < pathCell.getDistance()) {
			for (final var pathSide : LayoutGrid.Cell.Side.values()) {
				final var pathCellNeighbor = lg.neighbor(pathCell, pathSide);
				
				if (null != pathCellNeighbor && pathCellNeighbor.getDistance() < pathCell.getDistance()) {
					path.prependWaypoint(pathCell, pathSide);
					
					pathCell = pathCellNeighbor;
					
					break;
				}
			}
		}
		
		Log.outf(6, " %s %s", path.getOri(), path.getWaypoints());
	}
	
	/**
	 * @author 2oLDNncs 20250412
	 */
	public static final class GridWall {
		
		private final List<Point2D> keyPoints;
		
		private final Point2D keyDir = new Point2D.Double(0.0, 1.0);
		private final Point2D wallDir = new Point2D.Double(1.0, 0.0);
		
		private final List<Graph.Edge> edges = new ArrayList<>();
		private final Map<Graph.Edge, Double> edgeRanks = new HashMap<>();
		
		private final Map<Graph.Node, Set<Graph.Edge>> groupBySrc1 = new LinkedHashMap<>();
		private final Map<Graph.Node, Set<Graph.Edge>> groupByDst1 = new LinkedHashMap<>();
		private final Map<Graph.Node, Set<Graph.Edge>> groupBySrc2 = new LinkedHashMap<>();
		private final Map<Graph.Node, Set<Graph.Edge>> groupByDst2 = new LinkedHashMap<>();
		
		public GridWall(final Collection<Point2D> key) {
			this.keyPoints = new ArrayList<>(key);
			
			if (this.keyPoints.get(0).getX() != this.keyPoints.get(1).getX()) {
				this.keyDir.setLocation(1.0, 0.0);
				this.wallDir.setLocation(0.0, 1.0);
			}
			
			Log.out(4, key, this.wallDir);
		}
		
		public final void add(final Graph.Edge edge, final List<List<Point2D>> segments, final int segmentIndex) {
			this.edges.add(edge);
			
			final var segment = segments.get(segmentIndex);
			
			final var dot = this.computeSegmentDot(segment);
			
			var rank = Math.signum(this.computeSegmentDot2(segment));
			
			if (0.0 != rank) {
				throw new IllegalStateException();
			}
			
			if (0.0 < dot) {
				computeIfAbsentHS(this.groupBySrc1, edge.getStartNode()).add(edge);
				computeIfAbsentHS(this.groupByDst1, edge.getEndNode()).add(edge);
				rank += 1.0 / 1024.0;
			} else {
				computeIfAbsentHS(this.groupBySrc2, edge.getStartNode()).add(edge);
				computeIfAbsentHS(this.groupByDst2, edge.getEndNode()).add(edge);
			}
			
			if (0 < segmentIndex) {
				rank -= Math.signum(this.computeSegmentDot2(segments.get(segmentIndex - 1)));
			}
			
			if (segmentIndex + 1 < segments.size()) {
				rank += Math.signum(this.computeSegmentDot2(segments.get(segmentIndex + 1)));
			}
			
			this.edgeRanks.put(edge, rank);
		}
		
		private final double computeSegmentDot(final List<Point2D> segment) {
			final var segmentDir = new Point2D.Double(
					segment.get(1).getX() - segment.get(0).getX(),
					segment.get(1).getY() - segment.get(0).getY());
			
			return this.keyDir.getX() * segmentDir.getX() + this.keyDir.getY() * segmentDir.getY();
		}
		
		private final double computeSegmentDot2(final List<Point2D> segment) {
			final var segmentDir = new Point2D.Double(
					segment.get(1).getX() - segment.get(0).getX(),
					segment.get(1).getY() - segment.get(0).getY());
			
			return this.wallDir.getX() * segmentDir.getX() + this.wallDir.getY() * segmentDir.getY();
		}
		
		public final Point2D getSegmentOffset(final Graph.Edge edge, final List<Point2D> segment) {
			this.edges.sort((e1, e2) -> Double.compare(this.edgeRanks.get(e1), this.edgeRanks.get(e2)));
			
			var i = this.edges.indexOf(edge);
			var n = this.edges.size();
			
			final var lst_ = new ArrayList<Collection<Graph.Edge>>();
			final var individuals = new ArrayList<Graph.Edge>();
			
			for (final var e : this.edges) {
				final var ns1_ = this.groupBySrc1.getOrDefault(e.getStartNode(), Collections.emptySet()).size();
				final var nd1_ = this.groupByDst1.getOrDefault(e.getEndNode(), Collections.emptySet()).size();
				if (1 < ns1_ && 1 < nd1_ || 1 == ns1_ && 1 == nd1_) {
					individuals.add(e);
				} else {
					final var ns2_ = this.groupBySrc2.getOrDefault(e.getStartNode(), Collections.emptySet()).size();
					final var nd2_ = this.groupByDst2.getOrDefault(e.getEndNode(), Collections.emptySet()).size();
					if (1 < ns2_ && 1 < nd2_ || 1 == ns2_ && 1 == nd2_) {
						individuals.add(e);
					}
				}
			}
			
			final Consumer<Collection<Graph.Edge>> extractFromGroup = group -> {
				if (1 < group.size()) {
					final var tmp = new HashSet<>(group);
					tmp.removeAll(individuals);
					if (!tmp.isEmpty()) {
						lst_.add(tmp);
					}
				}
			};
			
			this.groupBySrc1.values().forEach(extractFromGroup);
			this.groupByDst1.values().forEach(extractFromGroup);
			this.groupBySrc2.values().forEach(extractFromGroup);
			this.groupByDst2.values().forEach(extractFromGroup);
			
			for (final var individual : individuals) {
				lst_.add(Set.of(individual));
			}
			
//			if (this.groupByDst2.keySet().stream().anyMatch(node -> 29 == node.getIndex())
//					&& this.groupBySrc2.keySet().stream().anyMatch(node -> 17 == node.getIndex())
//					&& this.groupBySrc2.keySet().stream().anyMatch(node -> 20 == node.getIndex())) {
//				Log.out(1, lst_);
//				Log.out(1, individuals);
//			}
//			Log.out(1, this.edges);
//			Log.out(1, this.groupBySrc1);
//			Log.out(1, this.groupByDst1);
//			Log.out(1, this.groupBySrc2);
//			Log.out(1, this.groupByDst2);
//			Log.out(1, lst_);
//			Log.out(1, individuals);
			
			lst_.sort((l1, l2) -> {
				final var r1 = l1.stream().mapToDouble(this.edgeRanks::get).sum();
				final var r2 = l2.stream().mapToDouble(this.edgeRanks::get).sum();
				
				return Double.compare(r1, r2);
			});
			
			n = lst_.size();
			
			i = -1;
			
			for (var j = 0; j < n; j += 1) {
				if (lst_.get(j).contains(edge)) {
					i = j;
					break;
				}
			}
			
			Log.out(6, this.edgeRanks);
			Log.out(6, lst_);
			Log.out(6, "", edge, i);
			
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
							
							xmlElement(svg, "rect", () -> {
								svg.writeAttribute("fill", "none");
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
								svg.writeAttribute("font-size", "" + cellHeight * 6.0 / 20.0);
								svg.writeAttribute("x", "" + centerX);
								svg.writeAttribute("y", "" + centerY);
								svg.writeCharacters(node.toString() + ":" + node.getProps().getOrDefault(Graph.K_LABEL, "").toString());
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
	
	public static final void writeSvg(final Graph g, final String filePath)
			throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
		Log.beginf(1, "Writing svg %s", filePath);
		
		try (final var svgOut = new PrintStream(filePath)) {
			final var svg = new MyXMLStreamWriter(svgOut);
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
						final var nodeX = (int) node.getProps().get(K_X);
						final var nodeY = (int) node.getProps().get(K_Y);
						
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
							svg.writeCharacters(node.getProps().getOrDefault("Label", "").toString());
						});
						
						final var a1 = getOutline(node, nodeWidth, nodeHeight);
						
						for (final var edge : node.outgoingEdges) {
							final List<Point2D> gridPath = Helpers.cast(edge.getProps().get(K_GRID_PATH));
							final List<List<Point2D>> gridSegments = Helpers.cast(edge.getProps().get(K_GRID_SEGMENTS));
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
									if (Boolean.TRUE.equals(edge.getProps().get(K_DEBUG)) || true) {
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
									if (edge.getProps().containsKey(K_DEBUG) || true) {
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
	
	private static final int getGeomPos(final Graph.Node node, final String geomKey, final String gridKey, final int geomOffset, final int geomSide) {
		return (int) node.getProps().computeIfAbsent(geomKey, __ -> {
			return geomOffset + (int) node.getProps().get(gridKey) * geomSide;
		});
	}
	
	private static final Area getOutline(final Graph.Node node, final int nodeWidth, final int nodeHeight) {
		return (Area) node.getProps().computeIfAbsent(K_OUTLINE, __ -> {
			final var x = (int) node.getProps().get(K_X);
			final var y = (int) node.getProps().get(K_Y);
			final var result = new Area(new Ellipse2D.Double(x - nodeWidth / 2, y - nodeHeight / 2, nodeWidth, nodeHeight));
			
			result.subtract(new Area(new Ellipse2D.Double(x - nodeWidth / 2 + 0.125, y - nodeHeight / 2 + 0.125, nodeWidth - 0.25, nodeHeight - 0.25)));
			
			return result;
		});
	}
	
	private static final Area getOutline(final Graph.Edge edge) {
		return (Area) edge.getProps().computeIfAbsent(K_OUTLINE, __ -> {
			final int nodeX = (int) edge.getStartNode().getProps().get(K_X);
			final int nodeY = (int) edge.getStartNode().getProps().get(K_Y);
			final int endNodeX = (int) edge.getEndNode().getProps().get(K_X);
			final int endNodeY = (int) edge.getEndNode().getProps().get(K_Y);
			final var result = new Area(new Rectangle2D.Double(nodeX, nodeY,
					Point2D.distance(nodeX, nodeY, endNodeX, endNodeY),
					0.25));
			
			result.transform(AffineTransform.getRotateInstance(endNodeX - nodeX, endNodeY - nodeY, nodeX, nodeY));
			
			return result;
		});
	}
	
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
	
	public static final String K_ROW = "Row";
	public static final String K_COL = "Col";
	public static final String K_GRID_PATH = "GridPath";
	public static final String K_GRID_SEGMENTS = "GridSegments";
	public static final String K_GRID_WALLS = "GridWalls";
	
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
	
	public static final void computeNodeDepth(final Collection<Graph.Node> nodes, final IntFunction<List<Graph.Node>> getRow) {
		final var todo = new ArrayList<Graph.Node>();
		
		final Function<Integer, Consumer<Graph.Node>> applyDepth = d -> node -> {
			final var row = getRow.apply(d);
			node.getProps().put(K_ROW, d);
			node.getProps().put(K_COL, row.size());
			row.add(node);
			todo.add(node);
		};
		
		final var applyDepth0 = applyDepth.apply(0);
		
		for (final var node : nodes) {
			if (!node.getProps().containsKey(K_ROW)) {
				applyDepth0.accept(node);
			}
		}
		
		while (!todo.isEmpty()) {
			final var n0 = todo.remove(todo.size() - 1);
			final var d0 = (int) n0.getProps().get(K_ROW);
			
			n0.streamOutgoingEdges()
			.map(Graph.Edge::getEndNode)
			.filter(n -> !n.getProps().containsKey(K_ROW))
			.forEach(applyDepth.apply(d0 + 1));
		}
		
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
			
			result.getProps().put(K_LABEL, "" + this.nodes.size());
			
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
		
		public static final String K_LABEL = "Label";
		
		/**
		 * @author 2oLDNncs 20250402
		 */
		public static final class Node extends Obj {
			
			private final int index;
			
			private final List<Edge> incomingEdges = new ArrayList<>();
			
			private final List<Edge> outgoingEdges = new ArrayList<>();
			
			private Node parent;
			
			private final List<Node> children = new ArrayList<>();
			
			private final CompId compId = new CompId();
			
			public Node(final int index) {
				this.index = index;
			}
			
			public final int getIndex() {
				return this.index;
			}
			
			public final int getCompId() {
				return this.compId.getVal();
			}
			
			public final Node getParent() {
				return this.parent;
			}
			
			public final void setParent(final Node parent) {
				if (null != this.getParent()) {
					this.getParent().children.remove(this);
				}
				
				this.parent = parent;
				
				if (null != this.getParent()) {
					this.getParent().children.add(this);
				}
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
			
			public final boolean isRoot() {
				return this.incomingEdges.isEmpty();
			}
			
			public final boolean isLeaf() {
				return this.outgoingEdges.isEmpty();
			}
			
			public final Edge edgeTo(final Node edgeEndNode) {
				edgeEndNode.compId.topWith(this.compId);
				
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
//				return String.format("%s%s", this.getIndex(), this.getProps());
//				return String.format("%s", this.getIndex());
				return String.format("%s.%s", this.getCompId(), this.getIndex());
//				return String.format("%s.%s%s", this.getCompId(), this.getIndex(), this.getProps());
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
			 * @author 2oLDNncs 20250418
			 */
			private static final class CompId {
				
				private final int val = ++lastVal;
				
				private CompId top;
				
				public final int getVal() {
					return this.top().val;
				}
				
				public final void topWith(final CompId repl) {
					if (this.top() != repl.top()) {
						Log.out(6, this.stack(), repl.stack());
						this.top().top = repl;
						Log.out(6, this.stack());
					}
				}
				
				private final CompId top() {
					var result = this;
					
					while (null != result.top) {
						result = result.top;
					}
					
					return result;
				}
				
				private final boolean contains(final CompId compId) {
					var tmp = this;
					
					while (null != tmp && tmp != compId) {
						tmp = tmp.top;
					}
					
					return tmp == compId;
				}
				
				@Override
				public final String toString() {
					return "" + this.val;
				}
				
				private final Collection<CompId> stack() {
					final var result = new ArrayList<CompId>();
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
				
				private static int lastVal;
				
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
//				return String.format("%s->%s%s", this.getStartNodeIndex(), this.getEndNodeIndex(), this.getProps());
				return String.format("%s->%s", this.getStartNodeIndex(), this.getEndNodeIndex());
			}
			
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250415
	 */
	public static final class LayoutGrid {
		
		private final List<List<Cell>> rows = new ArrayList<>();
		
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
		
		public final Cell neighbor(final Cell cell, final Cell.Side side) {
			switch (side) {
			case EAST:
				if (this.countCols() <= cell.getColIdx() + 1) {
					return null;
				}
				
				return this.cell(cell.getRowIdx(), cell.getColIdx() + 1);
			case NORTH:
				if (cell.getRowIdx() <= 0) {
					return null;
				}
				
				return this.cell(cell.getRowIdx() - 1, cell.getColIdx());
			case SOUTH:
				if (this.countRows() <= cell.getRowIdx() + 1) {
					return null;
				}
				
				return this.cell(cell.getRowIdx() + 1, cell.getColIdx());
			case WEST:
				if (cell.getColIdx() <= 0) {
					return null;
				}
				
				return this.cell(cell.getRowIdx(), cell.getColIdx() - 1);
			default:
				throw new IllegalStateException(String.format("Invalid side %s", side));
			}
		}
		
		public final Cell findCell(final int rowIdx, final int colIdx) {
			if (Helpers.inRange(this.countRows(), rowIdx) && Helpers.inRange(this.countCols(), colIdx)) {
				return this.getRowElement(rowIdx, colIdx);
			}
			
			return null;
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
		
		/**
		 * @author 2oLDNncs 20250415
		 */
		public static final class Cell {
			
			private final int rowIdx;
			
			private final int colIdx;
			
			private final Map<Side, List<Waypoint>> walls = new EnumMap<>(Side.class);
			
			private Graph.Node node;
			
			private int distance;
			
			private int turns;
			
			private final Map<Cell, Waypoint> groupByOri = new HashMap<>();
			private final Map<Side, Map<Cell, Waypoint>> groupByDst = new HashMap<>();
			
			public Cell(final int rowIdx, final int colIdx) {
				this.rowIdx = rowIdx;
				this.colIdx = colIdx;
				
				this.reset();
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
			
			public final Waypoint newWaypoint(final Side side, final Path path) {
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
				
				result = new LayoutGrid.Waypoint(this, side);
				
				this.getWall(side).add(result);
				
				sameDst.put(path.getDst(), result);
				sameOri.put(path.getOri(), result);
				
				return result;
			}
			
			public final Graph.Node getNode() {
				return this.node;
			}
			
			public final void setNode(Graph.Node node) {
				this.node = node;
			}
			
			public final void reset() {
				this.setDistance(Integer.MAX_VALUE);
				this.setTurns(0);
			}
			
			public final int getDistance() {
				return this.distance;
			}
			
			public final void setDistance(final int distance) {
				this.distance = distance;
			}
			
			public final int getTurns() {
				return this.turns;
			}
			
			public final void setTurns(final int turns) {
				this.turns = turns;
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
				
				NORTH {
					
					@Override
					public final Side flip() {
						return SOUTH;
					}
					
					@Override
					public final int getRelation(final Side other) {
						switch (other) {
						case EAST:
							return 1;
						case SOUTH:
							return 0;
						case WEST:
							return -1;
						default:
							throw new IllegalArgumentException(String.format("%s", other));
						}
					}
					
					@Override
					public final Point2D getGeomWallOri() {
//						return PZ;
						return ZZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
//						return NZ;
						return PZ;
					}
					
				}, WEST {
					
					@Override
					public final Side flip() {
						return EAST;
					}
					
					@Override
					public final int getRelation(final Side other) {
						switch (other) {
						case SOUTH:
							return 1;
						case EAST:
							return 0;
						case NORTH:
							return -1;
						default:
							throw new IllegalArgumentException(String.format("%s", other));
						}
					}
					
					@Override
					public final Point2D getGeomWallOri() {
						return ZZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
						return ZP;
					}
					
				}, SOUTH {
					
					@Override
					public final Side flip() {
						return NORTH;
					}
					
					@Override
					public final int getRelation(final Side other) {
						switch (other) {
						case WEST:
							return 1;
						case NORTH:
							return 0;
						case EAST:
							return -1;
						default:
							throw new IllegalArgumentException(String.format("%s", other));
						}
					}
					
					@Override
					public final Point2D getGeomWallOri() {
						return ZP;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
						return PZ;
					}
					
				}, EAST {
					
					@Override
					public final Side flip() {
						return WEST;
					}
					
					@Override
					public final int getRelation(final Side other) {
						switch (other) {
						case NORTH:
							return 1;
						case WEST:
							return 0;
						case SOUTH:
							return -1;
						default:
							throw new IllegalArgumentException(String.format("%s", other));
						}
					}
					
					@Override
					public final Point2D getGeomWallOri() {
//						return PP;
						return PZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
//						return ZN;
						return ZP;
					}
					
				};
				
				public abstract Side flip();
				
				public abstract int getRelation(Side other);
				
				public abstract Point2D getGeomWallOri();
				
				public abstract Point2D getGeomWallDir();
				
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
			
			public Waypoint(final Cell cell, final Side side) {
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
				final var wp = cell.newWaypoint(side, this);
				
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
