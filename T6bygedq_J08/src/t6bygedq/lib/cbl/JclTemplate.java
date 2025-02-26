package t6bygedq.lib.cbl;

import java.util.ArrayList;
import java.util.List;

import t6bygedq.lib.TextTemplate;

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
		final StringBuilder line = new StringBuilder();
		
		line.append(String.format("//%-8s %s ", name, type));
		
		final int alignment = line.length();
		final List<String> params = params(paramKVs);
		
		for (int i = 0; i < params.size(); i += 1) {
			int newLineLength = line.length() + params.get(i).length();
			final boolean commaNeeded = i + 1 < params.size();
			
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
		final List<String> result = new ArrayList<>();
		final StringBuilder entry = new StringBuilder();
		
		for (int i = 0; i < kvs.length; i += 2) {
			final Object k = kvs[i];
			final Object v = kvs[i + 1];
			
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
