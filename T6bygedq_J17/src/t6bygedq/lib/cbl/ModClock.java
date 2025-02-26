package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250217
 */
public final class ModClock {
	
	private final long[] sharedClock;
	
	private long localClock = -1;
	
	private ModClock(final long[] sharedClock) {
		this.sharedClock = sharedClock;
	}
	
	public ModClock() {
		this(new long[1]);
	}
	
	public final void incrShared() {
		++this.sharedClock[0];
	}
	
	public final boolean syncLocal() {
		final var c = this.sharedClock[0];
		
		if (this.localClock < c) {
			this.localClock = c;
			
			return true;
		}
		
		return false;
	}
	
	public final ModClock spawn() {
		return new ModClock(this.sharedClock);
	}
	
}