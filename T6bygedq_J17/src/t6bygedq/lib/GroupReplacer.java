package t6bygedq.lib;

import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 2oLDNncs 20241030
 */
public final class GroupReplacer implements UnaryOperator<String> {
	
	private final Pattern pattern;
	
	private final IntFunction<String> replacer;
	
	public GroupReplacer(final Pattern pattern, final IntFunction<String> replacer) {
		this.pattern = pattern;
		this.replacer = replacer;
	}
	
	@Override
	public final String apply(final String input) {
		final var matcher = this.pattern.matcher(input);
		final var result = new StringBuilder();
		var i = 0;
		
		while (matcher.find()) {
			result.append(input.substring(i, matcher.start()));
			result.append(this.replacer.apply(groupIndex(matcher)));
			i = matcher.end();
		}
		
		result.append(input.substring(i));
		
		return result.toString();
	}
	
	public static final int groupIndex(final Matcher matcher) {
		for (var i = 1; i <= matcher.groupCount(); i += 1) {
			if (null != matcher.group(i)) {
				return i;
			}
		}
		
		return 0;
	}
	
}