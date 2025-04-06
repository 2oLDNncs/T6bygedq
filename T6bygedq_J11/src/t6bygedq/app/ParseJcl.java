package t6bygedq.app;

import java.io.IOException;
import java.net.URISyntaxException;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers.Debug;
import t6bygedq.lib.cbl.JclJobParser;

/**
 * @author 2oLDNncs 20250404
 */
@Debug(false)
public final class ParseJcl {
	
	public static final String ARG_IN = "-In";
	
	public static final void main(final String... args) throws IOException, URISyntaxException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/JCL/pds.jcl");
		
		JclJobParser.parse(ap.getPath(ARG_IN), new JclJobParser() {});
	}
	
}
