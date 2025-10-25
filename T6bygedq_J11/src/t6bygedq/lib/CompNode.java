package t6bygedq.lib;

/**
 * @author 2oLDNncs 20251024
 */
public abstract class CompNode {
	
	private CompNode parent = this;
	
	public final CompNode getParent() {
		return this.parent;
	}
	
	private final void setParent(final CompNode parent) {
		this.parent = parent;
	}
	
	public final boolean isRoot() {
		return this == this.getParent();
	}
	
	public final CompNode findRoot() {
		var result = this.getAncestor();
		
		while (!result.isRoot()) {
			result.compress();
			result = result.getAncestor();
		}
		
		return result;
	}
	
	private final CompNode getAncestor() {
		return this.getParent().getParent().getParent().getParent();
	}
	
	private final void compress() {
		this.setParent(this.getAncestor());
	}
	
	public final boolean isConnectedTo(final CompNode that) {
		return this.findRoot() == that.findRoot();
	}
	
	public final void connectTo(final CompNode that) {
		final var thisRoot = this.findRoot();
		final var thatRoot = that.findRoot();
		
		if (thisRoot != thatRoot) {
			thatRoot.setParent(thisRoot);
		}
	}
	
}