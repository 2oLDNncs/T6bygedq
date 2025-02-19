package t6bygedq.app;

import static t6bygedq.lib.Helpers.dprintlnf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.OrientedGraph;
import t6bygedq.lib.OrientedGraph.CycleHandling;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20240608
 */
@Debug
public final class GraphOp {
	
	static final String ARGKEY_ARCS = "-Arcs";
	static final String ARGKEY_NODES = "-Nodes";
	static final String ARGKEY_OP = "-Op";
	static final String ARGKEY_SEP = "-Sep";
	static final String ARGKEY_TRANS = "-Trans";
	
	public static final void main(final String[] args) throws IOException {
		final ArgsParser ap = new ArgsParser(args);
		
		ap.setDefault(ARGKEY_ARCS, "data/test_arcs.txt");
		ap.setDefault(ARGKEY_NODES, "data/test_nodes.txt");
		ap.setDefault(ARGKEY_OP, Operation.COMP_ARCS);
		ap.setDefault(ARGKEY_TRANS, false);
		ap.setDefault(ARGKEY_SEP, "\t");
		
		final Operation operation = ap.getEnum(ARGKEY_OP);
		final boolean transitive = ap.getBoolean(ARGKEY_TRANS);
		final String separator = ap.getString(ARGKEY_SEP);
		final Path inputFilePath = ap.getPath(ARGKEY_ARCS);
		final OrientedGraph<Object> og = newOrientedGraph(inputFilePath, transitive, separator);
		
		og.setCycleHandling(CycleHandling.PRINT_WARNING);
		
		dprintlnf("Operation: %s", operation);
		
		switch (operation) {
		case ALL_HEADS:
			og.getAllHeads().forEach(System.out::println);
			break;
		case ALL_TAILS:
			og.getAllTails().forEach(System.out::println);
			break;
		case ROOTS:
			og.getRoots().forEach(System.out::println);
			break;
		case LEAVES:
			og.getLeaves().forEach(System.out::println);
			break;
		case ARCS:
			og.forEachArc(GraphOp::printArc);
			break;
		case MAX_ARCS:
			og.forEachMaxArc(GraphOp::printArc);
			break;
		case MAX_PATHS:
			og.forEachMaxPath(System.out::println);
			break;
		case PATHS_TO:
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				og.forEachPathTo(node, System.out::println);
			});
			break;
		case PATHS_FROM:
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				og.forEachPathFrom(node, System.out::println);
			});
			break;
		case ARCS_FROM:
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				og.forEachPathArcFrom(node, GraphOp::printArc);
			});
			break;
		case ARCS_TO:
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				og.forEachPathArcTo(node, GraphOp::printArc);
			});
			break;
		case COMP_ARCS:
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				og.forEachPathArcTo(node, GraphOp::printArc);
			});
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				og.forEachPathArcFrom(node, GraphOp::printArc);
			});
			break;
		case COMP:
			Files.lines(ap.getPath(ARGKEY_NODES)).forEach(node -> {
				System.out.println(og.getConnectedComponent(node));
			});
			break;
		default:
			throw new IllegalArgumentException(String.format("operation: %s", operation));
		}
	}
	
	public static final OrientedGraph<Object> newOrientedGraph(final Path inputFilePath,
			final boolean transitive, final String separator)
			throws IOException {
		final OrientedGraph<Object> result = new OrientedGraph<>(transitive);
		
		Files.lines(inputFilePath).forEach(line -> {
			final int i = line.indexOf(separator);
			
			result.connect(line.substring(0, i), line.substring(i + 1));
		});
		
		return result;
	}
	
	public static final void printArc(final Object head, final Object tail) {
		System.out.print(head);
		System.out.print('\t');
		System.out.println(tail);
	}
	
	/**
	 * @author 2oLDNncs 20240608
	 */
	public static enum Operation {
		
		ALL_HEADS, ALL_TAILS, ROOTS, LEAVES, ARCS, MAX_ARCS, MAX_PATHS,
		PATHS_TO, PATHS_FROM,
		ARCS_FROM, ARCS_TO, COMP_ARCS,
		COMP;
		
	}
	
}
