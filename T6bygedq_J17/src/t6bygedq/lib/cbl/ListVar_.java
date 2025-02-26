package t6bygedq.lib.cbl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 2oLDNncs 20250214
 *
 * @param <E>
 */
public abstract class ListVar_<E> extends AbstractList<E> implements Var {
	
	protected final List<E> elements = new ArrayList<>();
	
	public final void afterRead() {
		while (this.newElementNeeded()) {
			this.elements.add(this.newElement());
		}
	}
	
	protected abstract boolean newElementNeeded();
	
	protected abstract E newElement();
	
	@Override
	public final E get(final int index) {
		return this.elements.get(index);
	}
	
	@Override
	public final int size() {
		return this.elements.size();
	}
	
}