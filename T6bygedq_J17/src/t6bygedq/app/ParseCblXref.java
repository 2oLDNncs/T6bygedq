package t6bygedq.app;

import static t6bygedq.lib.Helpers.dprintlnf;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.CblXrefParser;
import t6bygedq.lib.GraphvizPrinter;
import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20250104
 */
@Debug
public class ParseCblXref {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_OPS = "-Ops";
	public static final String ARG_FLOWS = "-Flows";
	public static final String ARG_MORE_FLOWS = "-MoreFlows";
	public static final String ARG_FLOWS_GV = "-FlowsGV";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/test_xref.txt");
		ap.setDefault(ARG_OPS, "data/xref_ops.txt");
		ap.setDefault(ARG_FLOWS, "data/xref_flows.txt");
		ap.setDefault(ARG_MORE_FLOWS, "");
		ap.setDefault(ARG_FLOWS_GV, "data/xref_flows.gv");
		
		final List<List<Object>> ops = new ArrayList<>();
		final List<List<Object>> flows = new ArrayList<>();
		
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
		
		if (!ap.getString(ARG_MORE_FLOWS).isBlank()) {
			Files.lines(ap.getPath(ARG_MORE_FLOWS))
			.forEach(line -> {
				final var flow = Arrays.asList((Object[]) line.split("\t"));
				
				if (flow.size() < 5) {
					throw new IllegalStateException(String.format("Invalid additional flow: %s", flow));
				}
				
				flows.add(flow);
			});
		}
		
		if (!ap.getString(ARG_FLOWS_GV).isBlank()) {
			final var gvFile = ap.getFile(ARG_FLOWS_GV);
			
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
	}
	
	private static final void process(final Path filePath, final List<List<Object>> ops, final List<List<Object>> flows,
			final PrintStream opsOut, final PrintStream flowsOut) throws IOException {
		dprintlnf("Processing %s...", filePath);
		
		processModule(filePath, ops, flows);
		
		printTabbedData(ops, opsOut);
		
		printTabbedData(flows, flowsOut);
		
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
