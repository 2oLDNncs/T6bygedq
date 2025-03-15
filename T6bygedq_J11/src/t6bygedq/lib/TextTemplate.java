package t6bygedq.lib;

import static t6bygedq.lib.Helpers.getMethodName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author 2oLDNncs 20241224
 */
public abstract class TextTemplate implements Runnable {
	
	private final TextTemplate parent;
	
	private final TextTemplatePrinter printer;
	
	private String name;
	
	private final Map<String, Object> data = new HashMap<>();
	
	protected TextTemplate() {
		this(stack.isEmpty() ? null : stack.peek());
	}
	
	protected TextTemplate(final TextTemplatePrinter printer) {
		this(null, printer);
	}
	
	protected TextTemplate(final TextTemplate parent) {
		this(parent, null == parent ? new TextTemplateIndenter() : parent.getPrinter());
	}
	
	private TextTemplate(final TextTemplate parent, final TextTemplatePrinter printer) {
		this.parent = parent;
		this.printer = printer;
		this.setName(UUID.randomUUID().toString());
	}
	
	protected final TextTemplatePrinter getPrinter() {
		return this.printer;
	}
	
	protected final void set(final String key, final Object val) {
		this.data.put(key, val);
	}
	
	protected final Object get(final String key) {
		return this.find(key).get();
	}
	
	protected final Object get(final String key, final Function<String, Object> create) {
		return this.data.computeIfAbsent(key, create);
	}
	
	protected final Optional<Object> find(final String key) {
		final var ancestor = this.findAncestor(t -> t.data.containsKey(key));
		
		if (null != ancestor) {
			return Optional.of(ancestor.data.get(key));
		}
		
		return Optional.empty();
	}
	
	protected final String getName() {
		return this.name;
	}
	
	protected final void setName(final String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	@Method_run
	@Override
	public final void run() {
		stack.push(this);
		
		try {
			this.doRun();
		} finally {
			stack.pop(this);
		}
	}
	
	@Override
	public final String toString() {
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.getName());
	}
	
	@Method_doRun
	protected void doRun() {
		//pass
	}
	
	@Method_print
	public final void print(final Object x) {
		this.getPrinter().print(x);
	}
	
	@Method_println
	public final void println(final Object x) {
		this.getPrinter().println(x);
	}
	
	@Method_printf
	public final void printf(final String format, final Object... args) {
		this.print(String.format(format, args));
	}
	
	@Method_printlnf
	public final void printlnf(final String format, final Object... args) {
		this.println(String.format(format, args));
	}
	
	protected final boolean isRoot() {
		return null == this.parent;
	}
	
	protected final TextTemplate getRoot() {
		return this.findAncestor(TextTemplate::isRoot);
	}
	
	protected final TextTemplate getAncestor(final int upCount) {
		return Objects.requireNonNull(this.findAncestor(upCount));
	}
	
	protected final TextTemplate getAncestor(final String ancestorName) {
		return Objects.requireNonNull(this.findAncestor(ancestorName));
	}
	
	protected final <T extends TextTemplate> T getAncestor(final Class<T> ancestorClass) {
		return Objects.requireNonNull(this.findAncestor(ancestorClass));
	}
	
	protected final TextTemplate findAncestor(final int upCount) {
		return this.findAncestor(new Predicate<>() {
			
			private int i = 0;
			
			@Override
			public final boolean test(final TextTemplate t) {
				this.i += 1;
				
				return upCount < i;
			}
			
		});
	}
	
	protected final TextTemplate findAncestor(final String ancestorName) {
		return this.findAncestor(t -> t.getName().equals(ancestorName));
	}
	
	protected final <T extends TextTemplate> T findAncestor(final Class<T> ancestorClass) {
		return ancestorClass.cast(this.findAncestor(ancestorClass::isInstance));
	}
	
	protected final TextTemplate findAncestor(final Predicate<TextTemplate> p) {
		var result = this;
		
		while (null != result && !p.test(result)) {
			result = result.parent;
		}
		
		return result;
		
	}
	
	public static final String M_run = getMethodName(TextTemplate.class, Method_run.class);
	public static final String M_doRun = getMethodName(TextTemplate.class, Method_doRun.class);
	public static final String M_println = getMethodName(TextTemplate.class, Method_println.class);
	
	private static final MTStack<TextTemplate> stack = new MTStack<>();
	
	/**
	 * @author 2oLDNncs 20241225
	 *
	 * @param <T>
	 */
	private static final class MTStack<T> {
		
		private final Map<Thread, Stack<T>> stacks = new WeakHashMap<>();
		
		public final boolean isEmpty() {
			return this.getStack().isEmpty();
		}
		
		public final void push(final T item) {
			this.getStack().push(item);
		}
		
		public final T pop() {
			return this.getStack().pop();
		}
		
		public final void pop(final T item) {
			if (item != this.pop()) {
				throw new IllegalStateException();
			}
		}
		
		public final T peek() {
			return this.getStack().peek();
		}
		
		public final Stack<T> getStack() {
			return this.stacks.computeIfAbsent(Thread.currentThread(), __ -> new Stack<>());
		}
		
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_run {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_doRun {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_print {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_println {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printf {
		//pass
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Method_printlnf {
		//pass
	}
	
}