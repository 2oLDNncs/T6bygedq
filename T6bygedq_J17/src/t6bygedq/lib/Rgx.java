package t6bygedq.lib;

import java.util.Arrays;

/**
 * @author 2oLDNncs 20250321
 */
public final class Rgx {
	
	private Rgx() {
		throw new IllegalStateException();
	}
	
	public static final String rep01(final String regex) {
		return rep(0, 1, regex);
	}
	
	public static final String rep0X(final String regex) {
		return repNX(0, regex);
	}
	
	public static final String rep1X(final String regex) {
		return repNX(1, regex);
	}
	
	public static final String repNX(final int min, final String regex) {
		return rep(min, Integer.MAX_VALUE, regex);
	}
	
	public static final String repNN(final int n, final String regex) {
		return rep(n, n, regex);
	}
	
	public static final String rep(final int min, final int max, final String regex) {
		final var grp = ncgrp(regex);
		
		if (Integer.MAX_VALUE == max) {
			if (0 == min) {
				return String.format("%s*", grp);
			}
			
			if (1 == min) {
				return String.format("%s+", grp);
			}
			
			return String.format("%s{%s,}", grp, min);
		}
		
		if (0 == min && 1 == max) {
			return String.format("%s?", grp);
		}
		
		if (min == max) {
			return String.format("%s{%s}", grp, min);
		}
		
		return String.format("%s{%s,%s}", grp, min, max);
	}
	
	/**
	 * Full line
	 */
	public static final String line(final String regex) {
		return start(end(regex));
	}
	
	/**
	 * Line start
	 */
	public static final String start(final String regex) {
		return String.format("^%s", regex);
	}
	
	/**
	 * Line end
	 */
	public static final String end(final String regex) {
		return String.format("%s$", regex);
	}
	
	/**
	 * Sequence
	 */
	public static final String seq(final String... regexes) {
		return String.join("", regexes);
	}
	
	/**
	 * Union
	 */
	public static final String or(final String... regexes) {
		return ncgrp(String.join("|", Arrays.stream(regexes)
				.map(Rgx::ncgrp)
				.toArray(String[]::new)));
	}
	
	/**
	 * Named capturing group
	 */
	public static final String grp(final String name, final String regex) {
		return grp(String.format("?<%s>%s", name, regex));
	}
	
	/**
	 * Non-capturing group
	 */
	public static final String ncgrp(final String regex) {
		return grp(String.format("?:%s", regex));
	}
	
	/**
	 * Capturing group
	 */
	public static final String grp(final String regex) {
		return String.format("(%s)", regex);
	}
	
}