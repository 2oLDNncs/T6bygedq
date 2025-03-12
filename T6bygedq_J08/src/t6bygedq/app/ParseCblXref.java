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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
		final ArgsParser ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_xref.txt");
		ap.setDefault(ARG_OPS, "data/xref_ops.txt");
		ap.setDefault(ARG_FLOWS, "data/xref_flows.txt");
		ap.setDefault(ARG_MORE_FLOWS, "data/xref_more_flows.txt");
		ap.setDefault(ARG_FLOWS_GV, "data/xref_flows.gv");
		ap.setDefault(ARG_FILTER, "");
		
		final List<List<Object>> flows = new ArrayList<>();
		
		if (!ap.isBlank(ARG_IN)) {
			final List<List<Object>> ops = new ArrayList<>();
			
			try (final PrintStream opsOut = new PrintStream(ap.getFile(ARG_OPS))) {
				opsOut.println("Module\tLine\tProc\tVerb\tObj\tOp");
				
				try (final PrintStream flowsOut = new PrintStream(ap.getFile(ARG_FLOWS))) {
					flowsOut.println("Module\tProc\tVerb\tSrc\tDst");
					
					final Path inPath = ap.getPath(ARG_IN);
					final File[] files = inPath.toFile().listFiles();
					
					if (null != files) {
						for (final File file : files) {
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
				final List<Object> flow = Arrays.asList((Object[]) line.split("\t"));
				
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
				final List<Object> flow = Arrays.asList((Object[]) line.split("\t"));
				
				if (flow.size() < 5) {
					throw new IllegalStateException(String.format("Invalid additional flow: %s", flow));
				}
				
				flows.add(flow);
			});
		}
		
		if (!ap.isBlank(ARG_FLOWS_GV)) {
			if (!ap.isBlank(ARG_FILTER)) {
				final Collection<Object> filteredNodes = new HashSet<>();
				
				{
					final Collection<Collection<String>> comps = getComps(computeNodeComps(flows));
					final Pattern filter = Pattern.compile(ap.getString(ARG_FILTER), Pattern.CASE_INSENSITIVE);
					
					comps.forEach(comp -> {
						for (final String node : comp) {
							if (filter.matcher(node).find()) {
								filteredNodes.addAll(comp);
								break;
							}
						}
					});
				}
				
				{
					final List<List<Object>> filteredFlows = new ArrayList<>();
					
					flows.forEach(flow -> {
						final String src = Objects.toString(flow.get(3));
						final String dst = Objects.toString(flow.get(4));
						
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
		final Map<Collection<N>, Object> comps = new IdentityHashMap<>();
		
		nodeComps.values().forEach(c -> comps.put(c, c));
		
		return comps.keySet();
	}
	
	private static final Map<String, Collection<String>> computeNodeComps(final List<List<Object>> flows) {
		final Map<String, Collection<String>> result = new HashMap<>();
		
		flows.forEach(flow -> {
			final String src = Objects.toString(flow.get(3));
			final String dst = Objects.toString(flow.get(4));
			
			connectComps(src, dst, result);
		});
		
		return result;
	}
	
	private static final <N> void connectComps(final N src, final N dst,
			final Map<N, Collection<N>> nodeComps) {
		final Collection<N> existingSrcComp = nodeComps.get(src);
		final Collection<N> existingDstComp = nodeComps.get(dst);
		
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
				final Collection<N> comp = new HashSet<>();
				comp.add(src);
				comp.add(dst);
				nodeComps.put(src, comp);
				nodeComps.put(dst, comp);
			}
		}
	}
	
	public static final void flows2gv(final List<List<Object>> flows, final File gvFile) throws IOException {
		dprintlnf("Generating %s...", gvFile);
		
		try (final PrintStream gvOut = new PrintStream(gvFile)) {
			final GraphvizPrinter gvp = new GraphvizPrinter(gvOut);
			final Map<List<String>, List<String>> arcToolTips = new LinkedHashMap<>();
			
			gvp.begin();
			
			flows.forEach(row -> {
				final String module = Objects.toString(row.get(0));
				final String proc = Objects.toString(row.get(1));
				final String verb = Objects.toString(row.get(2));
				final String src = Objects.toString(row.get(3));
				final String dst = Objects.toString(row.get(4));
				
				arcToolTips.computeIfAbsent(Arrays.asList(src, dst), __ -> new ArrayList<>())
				.add(String.format("%s§%s.%s", module, proc, verb));
			});
			
			arcToolTips.forEach((k, v) -> {
				final String src = k.get(0);
				final String dst = k.get(1);
				
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
		
		final List<List<Object>> newOps = new ArrayList<>();
		final List<List<Object>> newFlows = new ArrayList<>();
		
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
					.collect(Collectors.toList())));
		});
	}
	
	public static final void processModule(final Path modulePath,
			final List<List<Object>> ops, final List<List<Object>> flows) throws IOException {
		final String moduleName = Helpers.removeExt(modulePath.getFileName().toString());
		final CblXrefParser parser = new CblXrefParser();
		
		Files.lines(modulePath)
		.forEach(parser::parse);
		
		final List<List<Object>> moduleOps = new ArrayList<>();
		
		parser.generateOps(moduleOps);
		
		moduleOps.stream()
		.map(row -> {
			return makeList(moduleName, row);
		})
		.forEach(ops::add);
		
		final List<List<Object>> moduleFlows = new ArrayList<>();
		
		CblXrefParser.generateFlows(moduleOps, moduleFlows);
		
		moduleFlows.stream()
		.map(row -> {
			return makeList(moduleName, row);
		})
		.forEach(flows::add);
	}
	
	private static final List<Object> makeList(final Object head, final List<Object> tail) {
		final List<Object> result = new ArrayList<>(1 + tail.size());
		
		result.add(head);
		result.addAll(tail);
		
		return result;
	}
	
}
