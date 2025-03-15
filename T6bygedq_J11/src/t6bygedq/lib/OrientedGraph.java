package t6bygedq.lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author 2oLDNncs 20240608
 *
 * @param <V>
 */
public final class OrientedGraph<V> implements Serializable {
	
	private final Map<V, Collection<V>> tailHeads = newMap();
	
	private final Map<V, Collection<V>> headTails = newMap();
	
	private final boolean transitive;
	
	private CycleHandling cycleHandling;
	
	public OrientedGraph(final boolean transitive) {
		this.transitive = transitive;
		
		this.setCycleHandling(CycleHandling.THROW_EXCEPTION);
	}
	
	public final boolean isTransitive() {
		return this.transitive;
	}
	
	public final CycleHandling getCycleHandling() {
		return this.cycleHandling;
	}
	
	public final void setCycleHandling(final CycleHandling cycleHandling) {
		this.cycleHandling = Objects.requireNonNull(cycleHandling);
	}
	
	public final void connect(final V tail, final V head) {
		this.addArc(tail, head);
		
		if (this.isTransitive()) {
			this.applyTransitivity(tail, head);
		}
	}
	
	public final Collection<V> getHeads(final V tail) {
		return this.tailHeads.getOrDefault(tail, Collections.emptySet());
	}
	
	public final Collection<V> getTails(final V head) {
		return this.headTails.getOrDefault(head, Collections.emptySet());
	}
	
	public final boolean isArc(final V tail, final V head) {
		return this.getHeads(tail).contains(head);
	}
	
	public final Collection<V> getAllTails() {
		return this.tailHeads.keySet();
	}
	
	public final Collection<V> getAllHeads() {
		return this.headTails.keySet();
	}
	
	public final Collection<V> getVertices() {
		return plus(this.getAllTails(), this.getAllHeads());
	}
	
	public final Collection<V> getRoots() {
		return minus(this.getAllTails(), this.getAllHeads());
	}
	
	public final Collection<V> getLeaves() {
		return minus(this.getAllHeads(), this.getAllTails());
	}
	
	public final void forEachArc(final BiConsumer<V, V> action) {
		newMap(this.tailHeads).forEach((tail, heads) -> {
			newList(heads).forEach(head -> {
				action.accept(tail, head);
			});
		});
	}
	
	public final void forEachMaxArc(final BiConsumer<V, V> action) {
		final var leaves = this.getLeaves();
		
		this.getRoots().forEach(root -> {
			this.getHeads(root).stream()
			.filter(leaves::contains)
			.forEach(leaf -> {
				action.accept(root, leaf);
			});
		});
	}
	
	public final void forEachMaxPath(final Consumer<List<V>> action) {
		this.getRoots().forEach(root -> {
			this.forEachPathFrom(root, action);
		});
	}
	
	public final void forEachMaxPathArc(final BiConsumer<V, V> action) {
		this.forEachMaxPath(path -> {
			forEachPathArc(path, action);
		});
	}
	
	public final void forEachPathFrom(final V head, final Consumer<List<V>> action) {
		this.new OutgoingPathFinder().forEachPath(head, action);
	}
	
	public final void forEachPathArcFrom(final V head, final BiConsumer<V, V> action) {
		this.forEachPathFrom(head, path -> {
			forEachPathArc(path, action);
		});
	}
	
	public final void forEachPathTo(final V tail, final Consumer<List<V>> action) {
		this.new IncomingPathFinder().forEachPath(tail, action);
	}
	
	public final void forEachPathArcTo(final V tail, final BiConsumer<V, V> action) {
		this.forEachPathTo(tail, path -> {
			forEachPathArc(path, action);
		});
	}
	
	public final Collection<V> getConnectedComponent(final V vertex) {
		final var result = new LinkedHashSet<V>();
		
		this.forEachPathTo(vertex, result::addAll);
		this.forEachPathFrom(vertex, result::addAll);
		
		return result;
	}
	
	private final void addArc(final V tail, final V head) {
		add(this.tailHeads, tail, head);
		add(this.headTails, head, tail);
	}
	
	private final void applyTransitivity(final V tail, final V head) {
		final var predecessors = newList(this.getTails(tail));
		final var successors = newList(this.getHeads(head));
		
		predecessors.forEach(p -> {
			this.addArc(p, head);
		});
		
		successors.forEach(s -> {
			this.addArc(tail, s);
		});
		
		predecessors.forEach(p -> {
			successors.forEach(s -> {
				this.addArc(p, s);
			});
		});
	}
	
	/**
	 * @author 2oLDNncs 20240621
	 */
	private final class OutgoingPathFinder extends PathFinder<V> {
		
		public OutgoingPathFinder() {
			super(OrientedGraph.this.getLeaves(), OrientedGraph.this.getCycleHandling());
		}
		
		@Override
		protected final Object[] getCycleExceptionMessageArgs(final Object path, final Object vertex) {
			return new Object[] { path, vertex };
		}
		
		@Override
		protected final Collection<V> getNextVertices(V origin) {
			return OrientedGraph.this.getHeads(origin);
		}
		
		@Override
		protected final int getInsertionIndex(final int pathSize) {
			return pathSize;
		}
		
		@Override
		protected final int getRemovalIndex(final int pathSize) {
			return pathSize - 1;
		}
		
		private static final long serialVersionUID = -2004507300852730885L;
		
	}
	
	/**
	 * @author 2oLDNncs 20240621
	 */
	private final class IncomingPathFinder extends PathFinder<V> {
		
		public IncomingPathFinder() {
			super(OrientedGraph.this.getRoots(), OrientedGraph.this.getCycleHandling());
		}
		
		@Override
		protected final Object[] getCycleExceptionMessageArgs(final Object path, final Object vertex) {
			return new Object[] { vertex, path };
		}
		
		@Override
		protected final Collection<V> getNextVertices(V origin) {
			return OrientedGraph.this.getTails(origin);
		}
		
		@Override
		protected final int getInsertionIndex(final int pathSize) {
			return 0;
		}
		
		@Override
		protected final int getRemovalIndex(final int pathSize) {
			return 0;
		}
		
		private static final long serialVersionUID = -1342815930328295026L;
		
	}
	
	private static final long serialVersionUID = 7797977345376468869L;
	
	private static final <K, V> boolean add(final Map<K, Collection<V>> arcs, final K tail, final V head) {
		return arcs.computeIfAbsent(tail, __ -> newSet()).add(head);
	}
	
	private static final <E> Collection<E> binop(final BiConsumer<Collection<E>, Collection<E>> op,
			final Collection<E> a, final Collection<E> b) {
		final var result = newSet(a);
		
		op.accept(result, b);
		
		return result;
	}
	
	private static final <E> Collection<E> plus(final Collection<E> a, final Collection<E> b) {
		return binop(Collection::addAll, a, b);
	}
	
	private static final <E> Collection<E> minus(final Collection<E> a, final Collection<E> b) {
		return binop(Collection::removeAll, a, b);
	}
	
	private static final <E> List<E> newList() {
		return new ArrayList<>();
	}
	
	private static final <E> List<E> newList(final Collection<E> source) {
		return new ArrayList<>(source);
	}
	
	private static final <K, V> Map<K, V> newMap() {
		return new HashMap<>();
	}
	
	private static final <K, V> Map<K, V> newMap(final Map<K, V> source) {
		return new HashMap<>(source);
	}
	
	private static final <E> Collection<E> newSet() {
		return new HashSet<>();
	}
	
	private static final <E> Collection<E> newSet(final Collection<E> source) {
		return new HashSet<>(source);
	}
	
	public static final <V> void forEachPathArc(final List<V> path, final BiConsumer<V, V> action) {
		for (var i = 0; i + 1 < path.size(); i += 1) {
			action.accept(path.get(i), path.get(i + 1));
		}
	}
	
	/**
	 * @author 2oLDNncs 20240621
	 *
	 * @param <V>
	 */
	private static abstract class PathFinder<V> implements Serializable {
		
		private final List<V> path = newList();
		
		private final Collection<V> ends;
		
		private final CycleHandling cycleHandling;
		
		protected PathFinder(final Collection<V> ends, final CycleHandling cycleHandling) {
			this.ends = ends;
			this.cycleHandling = cycleHandling;
		}
		
		public final void forEachPath(final V anchor, final Consumer<List<V>> action) {
			if (this.begin(anchor)) {
				this.getNextVertices(anchor).forEach(v -> {
					this.forEachPath(v, action);
				});
				
				this.end(action);
			}
		}
		
		protected abstract Object[] getCycleExceptionMessageArgs(Object path, Object vertex);
		
		protected abstract Collection<V> getNextVertices(V origin);
		
		protected abstract int getInsertionIndex(int pathSize);
		
		protected abstract int getRemovalIndex(int pathSize);
		
		private final boolean begin(final V vertex) {
			if (this.path.contains(vertex)) {
				this.cycleDetected(this.path, vertex);
				
				return false;
			}
			
			this.path.add(this.getInsertionIndex(this.path.size()), vertex);
			
			return true;
		}
		
		protected void cycleDetected(final Object path, final Object vertex) {
			if (CycleHandling.IGNORE.equals(this.cycleHandling)) {
				return;
			}
			
			final var message = String.format("Cycle detected: %s -> %s",
					this.getCycleExceptionMessageArgs(this.path, vertex));
			
			switch (this.cycleHandling) {
			case THROW_EXCEPTION:
				throw new IllegalStateException(message);
			case PRINT_WARNING:
				System.err.println(message);
				break;
			default:
				throw new UnsupportedOperationException(
						String.format("%s: %s", this.cycleHandling, message));
			}
		}
		
		private final void end(final Consumer<List<V>> action) {
			final var i = this.getRemovalIndex(this.path.size());
			
			if (0 <= i) {
				final var e = this.path.get(i);
				
				if (this.ends.contains(e)) {
					action.accept(this.path);
				}
				
				this.path.remove(i);
			}
		}
		
		private static final long serialVersionUID = -2445477282075393002L;
		
	}
	
	/**
	 * @author 2oLDNncs 20250117
	 */
	public static enum CycleHandling {
		
		THROW_EXCEPTION, PRINT_WARNING, IGNORE;
		
	}
	
}
