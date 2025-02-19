package t6bygedq.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 2oLDNncs 20250114
 */
public abstract class JclTemplate extends TextTemplate {
	
	protected String jobName = "MYJOB001";
	
	public JclTemplate() {
		
	}
	
	@Override
	protected void doRun() {
		this.printCard(this.jobName, "JOB", "ABC", null);
	}
	
	protected final void printCard(final String name, final String type, final Object... paramKVs) {
		final var line = new StringBuilder();
		
		line.append(String.format("//%-8s %s ", name, type));
		
		final var alignment = line.length();
		final var params = params(paramKVs);
		
		for (var i = 0; i < params.size(); i += 1) {
			var newLineLength = line.length() + params.get(i).length();
			final var commaNeeded = i + 1 < params.size();
			
			if (commaNeeded) {
				newLineLength += 1;
			}
			
			if (71 < newLineLength) {
				this.println(line);
				line.setLength(0);
				line.append(String.format("//%" + (alignment - 2) + "s", ""));
			}
			
			line.append(params.get(i));
			
			if (commaNeeded) {
				line.append(",");
			}
		}
		
		this.println(line);
	}
	
	private static final List<String> params(final Object... kvs) {
		final var result = new ArrayList<String>();
		final var entry = new StringBuilder();
		
		for (var i = 0; i < kvs.length; i += 2) {
			final var k = kvs[i];
			final var v = kvs[i + 1];
			
			entry.setLength(0);
			entry.append(k);
			
			if (null != v) {
				entry.append('=');
				entry.append(v);
			}
			
			result.add(entry.toString());
		}
		
		return result;
	}
	
}
