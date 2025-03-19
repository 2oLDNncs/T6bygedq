package t6bygedq.app;

import static t6bygedq.lib.Helpers.dprintlnf;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;
import t6bygedq.lib.cbl.CblXrefParser;

/**
 * @author 2oLDNncs 20250104
 */
@Debug
public final class ParseCblXref {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_OPS = "-Ops";
	public static final String ARG_FLOWS = "-Flows";
	public static final String ARG_MORE_FLOWS = "-MoreFlows";
	public static final String ARG_FILTER = "-Filter";
	public static final String ARG_FLOWS_GV = "-FlowsGV";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_xref.txt");
		ap.setDefault(ARG_OPS, "data/xref_ops.txt");
		ap.setDefault(ARG_FLOWS, "data/xref_flows.txt");
		ap.setDefault(ARG_MORE_FLOWS, "data/xref_more_flows.txt");
		ap.setDefault(ARG_FLOWS_GV, "data/xref_flows.gv");
		ap.setDefault(ARG_FILTER, "");
		
		final List<List<Object>> flows = new ArrayList<>();
		
		if (!ap.isBlank(ARG_IN)) {
			final List<List<Object>> ops = new ArrayList<>();
			
			try (final var opsOut = new PrintStream(ap.getFile(ARG_OPS))) {
				opsOut.println("Module\tLine\tProc\tVerb\tObj\tOp");
				
				try (final var flowsOut = new PrintStream(ap.getFile(ARG_FLOWS))) {
					flowsOut.println("Module\tProc\tVerb\tSrc\tDst");
					
					final var inPath = ap.getPath(ARG_IN);
					final var files = inPath.toFile().listFiles();
					
					if (null != files) {
						for (final var file : files) {
							process(file.toPath(), ops, flows, opsOut, flowsOut);
						}
					} else {
						process(inPath, ops, flows, opsOut, flowsOut);
					}
				}
			}
		} else {
			Files.lines(ap.getPath(ARG_FLOWS))
			.skip(1L)
			.forEach(line -> {
				final var flow = Arrays.asList((Object[]) line.split("\t"));
				
				if (flow.size() < 5) {
					throw new IllegalStateException(String.format("Invalid flow: %s", flow));
				}
				
				flows.add(flow);
			});
		}
		
		if (!ap.isBlank(ARG_MORE_FLOWS)) {
			Files.lines(ap.getPath(ARG_MORE_FLOWS))
			.skip(1L)
			.forEach(line -> {
				final var flow = Arrays.asList((Object[]) line.split("\t"));
				
				if (flow.size() < 5) {
					throw new IllegalStateException(String.format("Invalid additional flow: %s", flow));
				}
				
				flows.add(flow);
			});
		}
		
		if (!ap.isBlank(ARG_FLOWS_GV)) {
			if (!ap.isBlank(ARG_FILTER)) {
				final var filter = Pattern.compile(ap.getString(ARG_FILTER), Pattern.CASE_INSENSITIVE);
				final var filteredNodes = computeFilteredNodes(flows, filter);
//				final var filteredNodes = new HashSet<>();
//				
//				{
//					final var comps = getComps(computeNodeComps(flows));
//					final var filter = Pattern.compile(ap.getString(ARG_FILTER), Pattern.CASE_INSENSITIVE);
//					
//					comps.forEach(comp -> {
//						for (final var node : comp) {
//							if (filter.matcher(node).find()) {
//								filteredNodes.addAll(comp);
//								break;
//							}
//						}
//					});
//				}
				
				{
					final List<List<Object>> filteredFlows = new ArrayList<>();
					
					flows.forEach(flow -> {
						final var src = Objects.toString(flow.get(3));
						final var dst = Objects.toString(flow.get(4));
						
						if (filteredNodes.contains(src) || filteredNodes.contains(dst)) {
							filteredFlows.add(flow);
						}
					});
					
					flows2gv(filteredFlows, ap.getFile(ARG_FLOWS_GV));
				}
			} else {
				flows2gv(flows, ap.getFile(ARG_FLOWS_GV));
			}
		}
	}
	
	private static final <N> Collection<Collection<N>> getComps(final Map<N, Collection<N>> nodeComps) {
		final var comps = new IdentityHashMap<Collection<N>, Object>();
		
		nodeComps.values().forEach(c -> comps.put(c, c));
		
		return comps.keySet();
	}
	
	private static final Collection<String> computeFilteredNodes(final List<List<Object>> flows, final Pattern filter) {
		final var nodes = new HashSet<String>();
		final var backward = new HashMap<String, Collection<String>>();
		final var forward = new HashMap<String, Collection<String>>();
		
		flows.forEach(flow -> {
			final var src = Objects.toString(flow.get(3));
			final var dst = Objects.toString(flow.get(4));
			
			nodes.add(src);
			nodes.add(dst);
			
			backward.computeIfAbsent(dst, __ -> new HashSet<>()).add(src);
			forward.computeIfAbsent(src, __ -> new HashSet<>()).add(dst);
		});
		
		final var result = new HashSet<String>();
		
		{
			final var seeds = new HashSet<String>();
			
			for (final var node : nodes) {
				if (filter.matcher(node).find()) {
					seeds.add(node);
				}
			}
			
			result.addAll(collectConnected(seeds, backward));
			result.addAll(collectConnected(seeds, forward));
		}
		
		return result;
	}
	
	private static final <T> Collection<T> collectConnected(final Collection<T> seeds, final Map<T, Collection<T>> next) {
		final var result = new HashSet<T>();
		final var todo = new ArrayList<>(seeds);
		
		for (var i = 0; i < todo.size(); i += 1) {
			final var node = todo.get(i);
			
			if (result.add(node)) {
				todo.addAll(next.getOrDefault(node, Collections.emptySet()));
			}
		}
		
		return result;
	}
	
	private static final Map<String, Collection<String>> computeNodeComps(final List<List<Object>> flows) {
		final var result = new HashMap<String, Collection<String>>();
		
		flows.forEach(flow -> {
			final var src = Objects.toString(flow.get(3));
			final var dst = Objects.toString(flow.get(4));
			
			connectComps(src, dst, result);
		});
		
		return result;
	}
	
	private static final <N> void connectComps(final N src, final N dst,
			final Map<N, Collection<N>> nodeComps) {
		final var existingSrcComp = nodeComps.get(src);
		final var existingDstComp = nodeComps.get(dst);
		
		if (null != existingSrcComp) {
			if (null != existingDstComp) {
				existingDstComp.forEach(node -> nodeComps.put(node, existingSrcComp));
				existingSrcComp.addAll(existingDstComp);
			} else {
				nodeComps.put(dst, existingSrcComp);
				existingSrcComp.add(dst);
			}
		} else {
			if (null != existingDstComp) {
				nodeComps.put(src, existingDstComp);
				existingDstComp.add(src);
			} else {
				final var comp = new HashSet<N>();
				comp.add(src);
				comp.add(dst);
				nodeComps.put(src, comp);
				nodeComps.put(dst, comp);
			}
		}
	}
	
	public static final void flows2gv(final List<List<Object>> flows, final File gvFile) throws IOException {
		dprintlnf("Generating %s...", gvFile);
		
		try (final var gvOut = new PrintStream(gvFile)) {
			final var gvp = new GraphvizPrinter(gvOut);
			final var arcToolTips = new LinkedHashMap<List<String>, List<String>>();
			
			gvp.begin();
			
			flows.forEach(row -> {
				final var module = Objects.toString(row.get(0));
				final var proc = Objects.toString(row.get(1));
				final var verb = Objects.toString(row.get(2));
				final var src = Objects.toString(row.get(3));
				final var dst = Objects.toString(row.get(4));
				
				arcToolTips.computeIfAbsent(Arrays.asList(src, dst), __ -> new ArrayList<>())
				.add(String.format("%s§%s.%s", module, proc, verb));
			});
			
			arcToolTips.forEach((k, v) -> {
				final var src = k.get(0);
				final var dst = k.get(1);
				
				gvp.processArc(src, dst);
				gvp.processArcProp(src, dst, "tooltip", String.join("\\n", v));
			});
			
			gvp.end();
		}
		
		dprintlnf("Generating %s... Done", gvFile);
	}
	
	private static final void process(final Path filePath, final List<List<Object>> ops, final List<List<Object>> flows,
			final PrintStream opsOut, final PrintStream flowsOut) throws IOException {
		dprintlnf("Processing %s...", filePath);
		
		final var newOps = new ArrayList<List<Object>>();
		final var newFlows = new ArrayList<List<Object>>();
		
		processModule(filePath, newOps, newFlows);
		
		ops.addAll(newOps);
		flows.addAll(newFlows);
		
		printTabbedData(newOps, opsOut);
		printTabbedData(newFlows, flowsOut);
		
		dprintlnf("Processing %s... Done", filePath);
	}
	
	public static final void printTabbedData(final List<? extends List<?>> rows, final PrintStream out) {
		rows.forEach(row -> {
			out.println(String.join("\t", row.stream()
					.map(Objects::toString)
					.toList()));
		});
	}
	
	public static final void processModule(final Path modulePath,
			final List<List<Object>> ops, final List<List<Object>> flows) throws IOException {
		final var moduleName = Helpers.removeExt(modulePath.getFileName().toString());
		final var parser = new CblXrefParser();
		
		Files.lines(modulePath)
		.forEach(parser::parse);
		
		final List<List<Object>> moduleOps = new ArrayList<>();
		
		parser.generateOps(moduleOps);
		
		moduleOps.stream()
		.map(row -> {
			return makeList(moduleName, row);
		})
		.forEach(ops::add);
		
		final var moduleFlows = new ArrayList<List<Object>>();
		
		CblXrefParser.generateFlows(moduleOps, moduleFlows);
		
		moduleFlows.stream()
		.map(row -> {
			return makeList(moduleName, row);
		})
		.forEach(flows::add);
	}
	
	private static final List<Object> makeList(final Object head, final List<Object> tail) {
		final var result = new ArrayList<>(1 + tail.size());
		
		result.add(head);
		result.addAll(tail);
		
		return result;
	}
	
}
