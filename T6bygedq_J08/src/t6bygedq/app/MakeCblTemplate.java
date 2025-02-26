package t6bygedq.app;

import java.io.IOException;
import java.nio.file.Files;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.cbl.CblTemplateMaker;

/**
 * @author 2oLDNncs 20241228
 */
public final class MakeCblTemplate {
	
	public static final String ARG_IN = "-In";
	public static final String ARG_OUT = "-Out";
	
	public static final void main(final String... args) throws IOException {
		final ArgsParser ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/MYPGM001.cbl");
		ap.setDefault(ARG_OUT, "");
		
		final String generatedClassName;
		
		{
			String name = removeExtension(ap.getFile(ARG_IN).getName());
			
			if (!ap.getString(ARG_OUT).trim().isEmpty()) {
				name = removeExtension(ap.getFile(ARG_OUT).getName());
			}
			
			generatedClassName = name;
		}
		
		new CblTemplateMaker(generatedClassName, Files.readAllLines(ap.getPath(ARG_IN)).listIterator()).run();
	}
	
	public static final String removeExtension(final String fileName) {
		return fileName.replaceFirst("\\.[^.]*$", "");
	}
	
}
