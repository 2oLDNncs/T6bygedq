package t6bygedq.app;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.cbl.Buffer;

/**
 * @author 2oLDNncs 20250325
 */
public final class SortEBCDIC {
	
	public static final String ARG_IN = "-In";
	
	public static void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(ARG_IN, "data/list.txt");
		
		Files.lines(ap.getPath(ARG_IN))
		.map(Buffer.EBCDIC::encode)
		.map(bb -> {
			final var result = new int[bb.limit()];
			
			while (bb.hasRemaining()) {
				final var i = bb.position();
				result[i] = bb.get() & 0xFF;
			}
			
			return result;
		})
		.sorted(Arrays::compare)
		.map(SortEBCDIC::toBytes)
		.map(bytes -> new String(bytes, Buffer.EBCDIC))
		.forEach(System.out::println);
	}
	
	public static final byte[] toBytes(final int[] ints) {
		final var result = new byte[ints.length];
		
		for (var i = 0; i < ints.length; i += 1) {
			result[i] = (byte) ints[i];
		}
		
		return result;
	}
	
}
