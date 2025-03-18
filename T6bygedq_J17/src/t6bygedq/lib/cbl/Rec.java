package t6bygedq.lib.cbl;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 2oLDNncs 20250202
 */
public final class Rec {
	
	private final RecHeader recHeader;
	
	private RecData recData;
	
	public Rec(final RecHeader recHeader) {
		this.recHeader = recHeader;
	}
	
	public final RecHeader getRecHeader() {
		return this.recHeader;
	}
	
	public final <R extends RecData> R setAndGetRecData(final Class<R> cls) {
		this.getRecHeader().setRecordTypeFromDataClass(cls);
		
		return cls.cast(this.getRecData());
	}
	
	public final RecData getRecData() {
		if (!this.getRecHeader().getRecordType().isInstance(this.recData)) {
			if (null != this.recData) {
				this.getRecHeader().vDataLength.set(0);
			}
			
			this.recData = this.getRecHeader().newRecData();
			this.getRecHeader().vDataLength.set(this.recData.getLength());
		}
		
		return this.recData;
	}
	
	public final void write(final OutputStream out) throws IOException {
		this.getRecHeader().vDataLength.set(this.getRecData().getLength());
		this.getRecHeader().write(out);
		this.getRecData().write(out);
	}
	
	public static final Rec read_4_2(final ReadingContext rc) throws IOException {
		return read(new RecHeader_4_2(), rc);
	}
	
	public static final Rec read_6_1(final ReadingContext rc) throws IOException {
		return read(new RecHeader_6_1(), rc);
	}
	
	public static final Rec read(final RecHeader recHeader, final ReadingContext rc) throws IOException {
		final var result = new Rec(recHeader);
		
		rc.incrLineNumber();
		result.getRecHeader().read(rc);
		result.getRecData().read(rc);
		
		return result;
	}
	
}