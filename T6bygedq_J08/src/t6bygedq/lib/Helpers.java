package t6bygedq.lib;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

/**
 * @author 2oLDNncs 20241228
 */
public final class Helpers {
	
	@SuppressWarnings("unchecked")
	public static final <T> T cast(final Object object) {
		return (T) object;
	}
	
	public static final <E> E last(final E[] elements) {
		return elements[elements.length - 1];
	}
	
	public static final <E> E last(final List<E> elements) {
		return elements.get(elements.size() - 1);
	}
	
	public static final String substring(final String s, final int start) {
		return s.substring(start < 0 ? (s.length() + start) : start);
	}
	
	public static final String replaceCharAt(final String s, final int i, final char c) {
		return String.format("%s%s%s", s.substring(0, i), c, s.substring(i + 1));
	}
	
	public static final String getMethodName(final Class<?> cls, final Class<? extends Annotation> annotation) {
		return Arrays.stream(cls.getDeclaredMethods())
				.filter(m -> m.isAnnotationPresent(annotation))
				.findAny().get().getName();
	}
	
	public static final String removeExt(final String fileName) {
		return fileName.replaceFirst("\\.[^.]*$", "");
	}
	
	public static final void ignore(final Object object) {
		//pass
	}
	
	public static final String dformat(final String format, final Object... args) {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		final StackTraceElement callerSte = stackTrace[3];
		
		return String.format("(%s:%s) %s",
				callerSte.getFileName(),
				callerSte.getLineNumber(),
				String.format(format, args));
	}
	
	public static final void dprintlnf(final String format, final Object... args) {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for (final StackTraceElement ste : stackTrace) {
			final String steClassName = ste.getClassName();
			
			try {
				final Class<?> steClass = Class.forName(steClassName);
				final Debug debug = steClass.getAnnotation(Debug.class);
				
				if (null != debug) {
					if (debug.value()) {
						System.out.println(dformat(format, args));
					}
					
					break;
				}
			} catch (final ClassNotFoundException e) {
				//pass
			}
		}
	}
	
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public static abstract @interface Debug {
		
		public boolean value() default true;
		
	}
	
}
