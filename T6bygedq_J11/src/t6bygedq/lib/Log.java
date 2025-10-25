package t6bygedq.lib;

import java.io.PrintStream;
import java.util.Stack;

/**
 * @author 2oLDNncs 20250412
 */
public final class Log {
	
	private static PrintStream out = System.out;
	
	private static PrintStream err = System.err;
	
	private static final Stack<TimingInfo> t0 = new Stack<>();
	
	public static final void setOut(final PrintStream logOut) {
		Log.out = logOut;
	}
	
	public static final void setErr(final PrintStream logErr) {
		Log.err = logErr;
	}
	
	private static final void tic(final int logLevel, final String text) {
		t0.push(new TimingInfo(logLevel, text, System.currentTimeMillis()));
		
		if (logLevel <= 0 || isEnabled(logLevel)) {
			out.println(format5(text));
		}
	}
	
	private static final TimingInfo toc() {
		return t0.pop();
	}
	
	public static final void beginf(final int logLevel, final String format, final Object... args) {
		tic(logLevel, String.format(format, args) + "...");
	}
	
	public static final void begin(final int logLevel, final Object... args) {
		tic(logLevel, Helpers.join(" ", args) + "...");
	}
	
	public static final long done() {
		final var info = toc();
		final var logLevel = info.logLevel();
		
		if (logLevel <= 0 || isEnabled(logLevel)) {
			out.println(format(info));
		}
		
		return info.millis();
	}
	
	public static final void outf(final int logLevel, final String format, final Object... args) {
		if (logLevel <= 0 || isEnabled(logLevel)) {
			out.println(formatf(format, args));
		}
	}
	
	public static final void out(final int logLevel, final Object... args) {
		if (logLevel <= 0 || isEnabled(logLevel)) {
			out.println(format(args));
		}
	}
	
	public static final void errf(final int logLevel, final String format, final Object... args) {
		if (logLevel <= 0 || isEnabled(logLevel)) {
			err.println(formatf(format, args));
		}
	}
	
	public static final void err(final int logLevel, final Object... args) {
		if (logLevel <= 0 || isEnabled(logLevel)) {
			err.println(format(args));
		}
	}
	
	private static final String format5(final Object... args) {
		return Helpers.dformat(5, "%s", Helpers.join(" ", args));
	}
	
	private static final String format(final Object... args) {
		return Helpers.dformat(4, "%s", Helpers.join(" ", args));
	}
	
	private static final String formatf(final String format, final Object... args) {
		return Helpers.dformat(4, format, args);
	}
	
	public static final boolean isEnabled(final int logLevel) {
		final var stackTrace = Thread.currentThread().getStackTrace();
		
		for (final var ste : stackTrace) {
			final var steClassName = ste.getClassName();
			
			try {
				final var steClass = Class.forName(steClassName);
				final var classLogLevel = steClass.getAnnotation(LogLevel.class);
				
				if (null != classLogLevel) {
					if (logLevel <= classLogLevel.value()) {
						return true;
					}
					
					return false;
				}
			} catch (final ClassNotFoundException e) {
				//pass
			}
		}
		
		return true;
	}
	
	/**
	 * @author 2oLDNncs 20250412
	 */
	private static final class TimingInfo {
		
		private final int logLevel;
		
		private final String text;
		
		private final long t0;
		
		TimingInfo(final int logLevel, final String text, final long t0) {
			this.logLevel = logLevel;
			this.text = text;
			this.t0 = t0;
		}
		
		public final int logLevel() {
			return this.logLevel;
		}
		
		public final String text() {
			return this.text;
		}
		
		public final long t0() {
			return this.t0;
		}
		
		public final long millis() {
			return System.currentTimeMillis() - this.t0();
		}
		
		@Override
		public final String toString() {
			return String.format("%s Done (%s ms)", this.text(), this.millis());
		}
		
	}
	
}