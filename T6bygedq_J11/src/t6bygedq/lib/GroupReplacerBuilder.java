package t6bygedq.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 2oLDNncs 20241030
 */
public final class GroupReplacerBuilder {
	
	private final List<String> groups = new ArrayList<>();
	
	private final List<String> replacements = new ArrayList<>();
	
	public final void addReplacement(final String key, final String replacement) {
		final var group = String.format("(%s)", Pattern.quote(key));
		
		if (this.groups.contains(group)) {
			throw new IllegalArgumentException(String.format("Duplicate replacement key: %s", key));
		}
		
		groups.add(group);
		replacements.add(replacement);
	}
	
	public final GroupReplacer build() {
		return new GroupReplacer(
				Pattern.compile(String.join("|", this.groups)),
				i -> this.replacements.get(i - 1));
	}
	
}