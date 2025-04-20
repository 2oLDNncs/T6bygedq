package t6bygedq.app;

import static t6bygedq.lib.Helpers.in;

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
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
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
	
	public static final void main(final String[] args) throws IOException, XMLStreamException {
		final var g = new Graph();
		
		if (true) {
			Log.begin(1, "Creating graph");
			
			final var nbNodes = 40;
			final var nbEdges = nbNodes * nbNodes * 64 / 2048;
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
			
			grid.forEach(row -> {
				for (var i = row.size() - 1; 0 <= i; i -= 1) {
					final var node = row.get(i);
					final var currentRow = (int) node.getProps().get(K_ROW);
					final var currentCol = (int) node.getProps().get(K_COL);
					final var rowIdx = 1 + 2 * currentRow;
					final var colIdx = 1 + 2 * currentCol;
					final var cell = lg.cell(rowIdx, colIdx);
					var targetCol = (int) computeTargetCol(node);
					var targetColIdx = 1 + 2 * targetCol;
					var targetCell = lg.cell(rowIdx, targetColIdx);
					
					while (currentCol < targetCol && null != targetCell.getNode()) {
						targetCol -= 1;
						targetColIdx = 1 + 2 * targetCol;
						targetCell = lg.cell(rowIdx, targetColIdx);
					}
					
					if (null == targetCell.getNode()) {
						Log.out(2, "Moving", node, "from", cell, "to", targetCell);
						cell.setNode(null);
						targetCell.setNode(node);
						node.getProps().put(K_COL, targetCol);
					}
				}
			});
			
			{
				final var nbRows = lg.countRows();
				final var nbCols = lg.countCols();
				
//				lg.setGpsStations(lg.cell(0, nbCols / 2), lg.cell(nbRows - 1, 0), lg.cell(nbRows - 1, nbCols - 1));
//				lg.setGpsStations(lg.cell(0, 0), lg.cell(0, nbCols - 1), lg.cell(nbRows - 1, 0), lg.cell(nbRows - 1, nbCols - 1));
				lg.setGpsStations(lg.cell(0, 0), lg.cell(0, nbCols - 1), lg.cell(nbRows - 1, 0));
//				lg.setGpsStations(lg.cell(0, 0), lg.cell(0, nbCols - 1));
				
				if (nbRows != lg.countRows()) {
					throw new IllegalStateException(String.format("Unexpected row count: %s != %s", lg.countRows(), nbRows));
				}
				
				if (nbCols != lg.countCols()) {
					throw new IllegalStateException(String.format("Unexpected col count: %s != %s", lg.countCols(), nbCols));
				}
			}
			
			lg.computeGpsCoordsForNodeCells();
			
			g.forEach(node -> {
				Log.out(1, node);
				lg.forEach(LayoutGrid.Cell::reset);
				
				Log.out(6, node, node.outgoingEdges);
				
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
					
					for (final var pathSide : LayoutGrid.Cell.Side.N_E_S_W) {
						path.prependWaypoint(pathCell, pathSide);
						pathCell = lg.neighbor(pathCell, pathSide);
					}
				}
				
				if (true) {
					for (final var target : targets) {
						final var targetRowIdx = 1 + 2 * (int) target.getProps().get(K_ROW);
						final var targetColIdx = 1 + 2 * (int) target.getProps().get(K_COL);
						final var targetCell = lg.cell(targetRowIdx, targetColIdx);
						
						Log.out(6, "node:", node, "target:", target, "gpsDistance:", lg.gpsDistance(nodeCell, targetCell));
						
						final var path = new LayoutGrid.Path(nodeCell, targetCell);
						var pathCell = targetCell;
						
						while (pathCell != nodeCell) {
							LayoutGrid.Cell.Side ps = null;
							LayoutGrid.Cell nextPathCell = null;
							final var dirR = nodeCell.getRowIdx() - pathCell.getRowIdx();
							final var dirC = nodeCell.getColIdx() - pathCell.getColIdx();
							var bestDot = Double.NEGATIVE_INFINITY;
							
							for (final var pathSide : LayoutGrid.Cell.Side.N_W_S_E) {
								final var pathCellNeighbor = lg.neighbor(pathCell, pathSide);
								var dot = Double.NEGATIVE_INFINITY;
								
								if (nodeCell == pathCellNeighbor) {
									dot = Double.POSITIVE_INFINITY;
								} else if (null != pathCellNeighbor && null == pathCellNeighbor.getNode()) {
									dot = dirR * pathSide.dr() + dirC * pathSide.dc();
								}
								
								if (bestDot < dot) {
									bestDot = dot;
									ps = pathSide;
									nextPathCell = pathCellNeighbor;
								}
							}
							
							path.prependWaypoint(pathCell, ps);
							pathCell = nextPathCell;
						}
						
						Log.out(6, path, path.waypoints);
					}
				} else if (true) {
					for (final var target : targets) {
						final var targetRowIdx = 1 + 2 * (int) target.getProps().get(K_ROW);
						final var targetColIdx = 1 + 2 * (int) target.getProps().get(K_COL);
						final var targetCell = lg.cell(targetRowIdx, targetColIdx);
						
						Log.out(1, "node:", node, "target:", target, "gpsDistance:", lg.gpsDistance(nodeCell, targetCell));
						
						final var path = new LayoutGrid.Path(nodeCell, targetCell);
						var pathCell = targetCell;
						var remainingDistance = Double.POSITIVE_INFINITY;
						final var tmp1 = new ArrayList<>();
						final var tmp2 = new ArrayList<>();
						tmp1.add(remainingDistance);
						
						while (0.0 < remainingDistance) {
							LayoutGrid.Cell.Side ps = null;
							LayoutGrid.Cell nextPathCell = null;
							var rd = remainingDistance;
							tmp2.clear();
							
							for (final var pathSide : LayoutGrid.Cell.Side.N_W_S_E) {
								final var pathCellNeighbor = lg.neighbor(pathCell, pathSide);
								
								if (nodeCell == pathCellNeighbor) {
									ps = pathSide;
									nextPathCell = pathCellNeighbor;
									rd = 0.0;
								} else if (null != pathCellNeighbor && null == pathCellNeighbor.getNode()) {
									final double d = lg.gpsDistance(nodeCell, pathCellNeighbor);
									tmp2.add(d);
									
									if (d < rd) {
										ps = pathSide;
										nextPathCell = pathCellNeighbor;
										rd = d;
									}
								}
							}
							
							if (null == ps) {
								Log.err(1, pathCell, tmp1, tmp2);
								Log.err(1, "", path, path.waypoints);
								break;
							}
							Log.out(1, "**", pathCell, tmp1, tmp2);
							Log.out(1, "***", path, path.waypoints);
							
							path.prependWaypoint(pathCell, ps);
							pathCell = nextPathCell;
							remainingDistance = rd;
							tmp1.add(remainingDistance);
						}
						
						Log.out(1, path, path.waypoints);
					}
				} else {
					final var todo = new ArrayList<LayoutGrid.Cell>();
					
					todo.add(nodeCell);
					
					while (!todo.isEmpty() && !targets.isEmpty()) {
						final var cell = todo.remove(0);
						final var nextDistance = cell.getDistance() + 1;
						
						lg.forEachNeighborOf(cell, neighbor -> {
							if (targets.remove(neighbor.getNode())) {
								buildPath(lg, nodeCell, neighbor);
							} else if (null == neighbor.getNode() && nextDistance < neighbor.getDistance()) {
								neighbor.setDistance(nextDistance);
								todo.add(neighbor);
							}
						});
					}
					
					if (!targets.isEmpty()) {
						Log.err(1, node, node.outgoingEdges, targets);
					}
				}
			});
		}
		
		Log.done();
		
		return lg;
	}
	
	private static final double computeTargetCol(final Graph.Node node) {
		return Stream.concat(
				node.streamOutgoingEdges().map(Graph.Edge::getEndNode),
				node.streamIncomingEdges().map(Graph.Edge::getStartNode))
				.map(Graph.Node::getProps)
				.mapToDouble(props -> (int) props.get(K_COL))
				.average().orElse((int) node.getProps().get(K_COL));
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
					
					final var node1TargetCol = computeTargetCol(node1);
					final var node2TargetCol = computeTargetCol(node2);
					
					return Double.compare(node1TargetCol, node2TargetCol);
				});
				
				for (var i = 0; i < row.size(); i += 1) {
					final var before = (int) row.get(i).getProps().get(K_COL);
					
					if (i != before) {
						row.get(i).getProps().put(K_COL, i);
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
	
	private static final void buildPath(final LayoutGrid lg, final LayoutGrid.Cell fromCell, final LayoutGrid.Cell toCell) {
		final var path = new LayoutGrid.Path(fromCell, toCell);
		var pathCell = toCell;
		
		while (0 < pathCell.getDistance()) {
			for (final var pathSide : LayoutGrid.Cell.Side.N_W_S_E) {
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
						if (false) {
							xmlElement(svg, "text", () -> {
								final var cellX = cellWidth * cell.getColIdx();
								final var cellY = cellHeight * cell.getRowIdx();
								svg.writeAttribute("text-anchor", "left");
								svg.writeAttribute("dominant-baseline", "hanging");
								svg.writeAttribute("font-size", "" + cellHeight * 4.0 / 20.0);
								svg.writeAttribute("x", "" + cellX);
								svg.writeAttribute("y", "" + cellY);
								svg.writeAttribute("fill", "red");
								
								xmlElement(svg, "tspan", () -> {
									svg.writeCharacters(Arrays.toString(cell.gpsCoords));
								});
							});
						}
						
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
								svg.writeAttribute("font-size", "" + cellHeight * 5.0 / 20.0);
								svg.writeAttribute("x", "" + centerX);
								svg.writeAttribute("y", "" + centerY);
								
								xmlElement(svg, "tspan", () -> {
									svg.writeAttribute("dy", "-0.6em");
									svg.writeCharacters(node.toString() + " | " + String.format("%.1f", computeTargetCol(node)));
								});
								
								xmlElement(svg, "tspan", () -> {
									svg.writeAttribute("x", "" + centerX);
									svg.writeAttribute("dy", "1.2em");
									svg.writeCharacters(node.getProps().getOrDefault(Graph.K_LABEL, "").toString());
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
//				return String.format("%s.%s", this.getCompId(), this.getIndex());
//				return String.format("%s.%s%s", this.getCompId(), this.getIndex(), this.getProps());
//				return String.format("%s.%s@%s_%s", this.getCompId(), this.getIndex(), this.getProps().get(K_ROW), this.getProps().get(K_COL));
				return String.format("%s.%s@%s_%s", this.getCompId(), this.getIndex(), 1 + 2 * (int) this.getProps().get(K_ROW), 1 + 2 * (int) this.getProps().get(K_COL));
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
					final var thisTop = this.top();
					final var replTop = repl.top();
					
					if (thisTop != replTop) {
						thisTop.top = replTop;
					}
				}
				
				@Override
				public final String toString() {
					return "" + this.val;
				}
				
				private final CompId top() {
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
				
				private final List<CompId> stack() {
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
		
		/**
		 * Grid Positioning System multilateration stations
		 */
		private Cell[] gpsStations = new Cell[0];
		
		public final int countRows() {
			return this.rows.size();
		}
		
		public final int countCols() {
			return this.rows.isEmpty() ? 0 : this.rows.get(0).size();
		}
		
		public final void setGpsStations(final Cell... gpsStations) {
			this.gpsStations = gpsStations;
			
			this.forEach(cell -> {
				cell.resetGpsCoords(gpsStations.length);
			});
			
			for (var gpsIdx = 0; gpsIdx < gpsStations.length; gpsIdx += 1) {
				gpsStations[gpsIdx].setGpsCoord(gpsIdx, 0);
				
				final var todo = new ArrayList<Cell>();
				
				todo.add(gpsStations[gpsIdx]);
				
				while (!todo.isEmpty()) {
					final var cell = todo.remove(0);
					final var cellGpsCoord = cell.getGpsCoord(gpsIdx);
					
					for (final var delta : LayoutGrid.Cell.Side.D4) {
						final var neighbor = this.neighbor(cell, delta);
						
						if (null != neighbor && null == neighbor.getNode()) {
							final var nextCoord = delta.dd + cellGpsCoord;
							
							if (nextCoord < neighbor.getGpsCoord(gpsIdx)) {
								neighbor.setGpsCoord(gpsIdx, nextCoord);
								todo.add(neighbor);
							}
						}
					}
				}
			}
		}
		
		public final double gpsDistance(final Cell cell1, final Cell cell2) {
			var result = 0.0;
			
			for (var i = 0; i < this.gpsStations.length; i += 1) {
				result += Math.abs(cell1.getGpsCoord(i) - cell2.getGpsCoord(i));
			}
			
			return result;
		}
		
		public final void computeGpsCoordsForNodeCells() {
			final var sum = new double[this.gpsStations.length];
			
			this.streamCells()
			.filter(cell -> null != cell.getNode())
			.forEach(cell -> {
				Arrays.fill(sum, 0);
				var n = 0;
				
				for (final var neighbor : this.neighbors(cell)) {
					for (var i = 0; i < sum.length; i += 1) {
						sum[i] += neighbor.getGpsCoord(i);
					}
					
					n += 1;
				}
				
				if (0 < n) {
					for (var i = 0; i < sum.length; i += 1) {
						cell.setGpsCoord(i, sum[i] / n);
					}
				}
			});
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
			
			result.resetGpsCoords(this.gpsStations.length);
			
			this.rows.get(rowIdx).set(colIdx, result);
			
			return result;
		}
		
		/**
		 * @author 2oLDNncs 20250415
		 */
		public static final class Cell extends Obj {
			
			private final int rowIdx;
			
			private final int colIdx;
			
			private final Map<Side, List<Waypoint>> walls = new EnumMap<>(Side.class);
			
			private Graph.Node node;
			
			private double[] gpsCoords = Helpers.EMPTY_DOUBLES;
			
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
			
			public final void resetGpsCoords(final int nbGpsCoords) {
				if (nbGpsCoords != this.gpsCoords.length) {
					this.gpsCoords = new double[nbGpsCoords];
				}
				
				Arrays.fill(this.gpsCoords, Double.POSITIVE_INFINITY);
			}
			
			public final double[] getGpsCoords() {
				return this.gpsCoords;
			}
			
			public final void setGpsCoord(final int idx, final double val) {
				this.getGpsCoords()[idx] = val;
			}
			
			public final double getGpsCoord(final int idx) {
				return this.getGpsCoords()[idx];
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
					public final Point2D getGeomWallOri() {
//						return PZ;
						return ZZ;
					}
					
					@Override
					public final Point2D getGeomWallDir() {
//						return NZ;
						return PZ;
					}
					
					@Override
					public final int dr() {
						return -1;
					}
					
					@Override
					public final int dc() {
						return 0;
					}
					
				}, WEST {
					
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
					
					@Override
					public final int dr() {
						return 0;
					}
					
					@Override
					public final int dc() {
						return -1;
					}
					
				}, SOUTH {
					
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
					
					@Override
					public final int dr() {
						return +1;
					}
					
					@Override
					public final int dc() {
						return 0;
					}
					
				}, EAST {
					
					@Override
					public final Side flip() {
						return WEST;
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
					
					@Override
					public final int dr() {
						return 0;
					}
					
					@Override
					public final int dc() {
						return +1;
					}
					
				};
				
				public abstract Side flip();
				
				public abstract Point2D getGeomWallOri();
				
				public abstract Point2D getGeomWallDir();
				
				public abstract int dr();
				public abstract int dc();
				
				public static final Side[] N_W_S_E = { NORTH, WEST, SOUTH, EAST };
				public static final Side[] N_E_S_W = { NORTH, EAST, SOUTH, WEST };
				public static final Delta[] D4 = {
						delta(NORTH, null),
						delta(WEST, null),
						delta(EAST, null),
						delta(SOUTH, null),
				};
				public static final Delta[] D8 = {
						delta(NORTH, WEST),
						delta(NORTH, null),
						delta(NORTH, EAST),
						delta(WEST, null),
						delta(EAST, null),
						delta(SOUTH, WEST),
						delta(SOUTH, null),
						delta(SOUTH, EAST)
				};
				
				private static final Delta delta(final Side side1, final Side side2) {
					var dr = side1.dr();
					var dc = side1.dc();
					
					if (null != side2) {
						dr += side2.dr();
						dc += side2.dc();
					}
					
					return delta(dr, dc);
				}
				
				private static final Delta delta(final int dr, final int dc) {
					return new Delta(dr, dc);
				}
				
			}
			
			/**
			 * @author 2oLDNncs 20250419
			 */
			public static record Delta(int dr, int dc, double dd) {
				
				public Delta(final int dr, final int dc) {
					this(dr, dc, Point2D.distance(0.0, 0.0, dc, dr));
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
