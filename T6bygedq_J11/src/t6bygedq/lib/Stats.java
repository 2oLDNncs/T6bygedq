package t6bygedq.lib;

/**
 * @author 2oLDNncs 20250413
 */
public final class Stats {
	
	private long count;
	
	private double sum;
	
	private double min = Double.POSITIVE_INFINITY;
	
	private double max = Double.NEGATIVE_INFINITY;
	
	public final long getCount() {
		return this.count;
	}
	
	public final double getSum() {
		return this.sum;
	}
	
	public final double getMin() {
		return this.min;
	}
	
	public final double getMax() {
		return this.max;
	}
	
	public final double getAverage() {
		return this.getSum() / this.getCount();
	}
	
	public final void addValue(final double value) {
		this.addValue(value, 1L);
	}
	
	public final void addValue(final double value, final long count) {
		if (value < this.min) {
			this.min = value;
		}
		
		if (this.max < value) {
			this.max = value;
		}
		
		this.sum += count * value;
		this.count += count;
	}
	
	@Override
	public final String toString() {
		return String.format("{min:%s max:%s sum:%s nb:%s avg:%s}",
				this.getMin(), this.getMax(), this.getSum(), this.getCount(), this.getAverage());
	}
	
}