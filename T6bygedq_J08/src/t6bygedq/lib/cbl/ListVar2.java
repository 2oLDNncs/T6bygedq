package t6bygedq.lib.cbl;

/**
 * @author 2oLDNncs 20250214
 *
 * @param <E>
 */
public abstract class ListVar2<E> extends ListVar_<E> {
	
	private final IntVar vCount;
	
	protected ListVar2(final IntVar vCount) {
		this.vCount = vCount;
	}
	
	@Override
	protected boolean newElementNeeded() {
		return this.elements.size() < this.vCount.get();
	}
	
}