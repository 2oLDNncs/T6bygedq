package t6bygedq.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author 2oLDNncs 20250414
 */
public final class Chrono {
	
	private static final Stack<Long> tics = new Stack<>();
	
	private static final Map<String, Stats> times = new HashMap<>();
	
	public static final void tic() {
		tics.push(System.currentTimeMillis());
	}
	
	public static final void toc(final String key) {
		times.computeIfAbsent(key, __ -> new Stats()).addValue(System.currentTimeMillis() - tics.pop());
	}
	
	public static final Map<String, Stats> getTimes() {
		return times;
	}
	
}