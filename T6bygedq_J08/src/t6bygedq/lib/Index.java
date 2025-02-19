package t6bygedq.lib;

/**
 * @author 2oLDNncs 20241108
 */
public final class Index {
	
	private int value;
	
	private final int step;
	
	private final int end;
	
	public Index(final int end) {
		this(1, end);
	}
	
	public Index(final int step, final int end) {
		this.step = step;
		this.end = end;
	}
	
	public final int getValue() {
		return this.value;
	}
	
	public final void setValue(final int value) {
		if (value < 0) {
			throw new IllegalArgumentException();
		}
		
		this.value = value;
	}
	
	public final int getStep() {
		return this.step;
	}
	
	public final int getEnd() {
		return this.end;
	}
	
	public final boolean isValid() {
		return this.getValue() < this.getEnd();
	}
	
	public final void incr() {
		this.incr(this.getStep());
	}
	
	public final void incr(final int delta) {
		this.setValue(this.getValue() + delta);
	}
	
	public final boolean next() {
		return this.next(this.getStep());
	}
	
	public final boolean next(final int delta) {
		this.incr(delta);
		
		if (this.getValue() < this.getEnd()) {
			return true;
		}
		
		this.setValue(0);
		
		return false;
	}
	
	public final void next(final Index iNext) {
		if (!this.next()) {
			iNext.incr();
		}
	}
	
}