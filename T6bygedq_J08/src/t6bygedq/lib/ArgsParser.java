package t6bygedq.lib;

import static t6bygedq.lib.Helpers.cast;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 2oLDNncs 20241028
 */
public final class ArgsParser {
	
	private String[] args;
	
	private final Map<String, Object> map = new HashMap<>();
	
	public ArgsParser(final String... args) {
		this.args = args;
	}
	
	public final void setDefault(final String key, final Object value) {
		if (null == this.map.get(key)) {
			this.set(key, value);
		}
	}
	
	private final void set(final String key, final Object value) {
		this.map.put(key, Objects.requireNonNull(value));
	}
	
	public final boolean getBoolean(final String key) {
		return this.get(key);
	}
	
	public final byte getByte(final String key) {
		return this.get(key);
	}
	
	public final char getChar(final String key) {
		return this.get(key);
	}
	
	public final short getShort(final String key) {
		return this.get(key);
	}
	
	public final int getInt(final String key) {
		return this.get(key);
	}
	
	public final long getLong(final String key) {
		return this.get(key);
	}
	
	public final double getDouble(final String key) {
		return this.get(key);
	}
	
	public final String getString(final String key) {
		return this.get(key);
	}
	
	public final File getFile(final String key) {
		return new File(this.getString(key));
	}
	
	public final Path getPath(final String key) {
		return this.getFile(key).toPath();
	}
	
	public final <E extends Enum<?>> E getEnum(final String key) {
		return this.get(key);
	}
	
	public final boolean isBlank(final String key) {
		return this.getString(key).trim().isEmpty();
	}
	
	private final void parse() {
		if (null == this.args) {
			return;
		}
		
		final String[] args = this.args;
		this.args = null;
		
		int keyIndex = 0;
		int keyStep;
		
		while (keyIndex < args.length) {
			final String key = args[keyIndex];
			final Object defaultValue = this.get(key);
			Object value = "";
			
			if (defaultValue instanceof Boolean) {
				value = !(Boolean) defaultValue;
				keyStep = 1;
			} else {
				final String valueString = args[keyIndex + 1];
				
				if (defaultValue instanceof Byte) {
					value = Byte.parseByte(valueString);
				} else if (defaultValue instanceof Character) {
					value = Character.valueOf(valueString.charAt(0));
				} else if (defaultValue instanceof Short) {
					value = Short.parseShort(valueString);
				} else if (defaultValue instanceof Integer) {
					value = Integer.parseInt(valueString);
				} else if (defaultValue instanceof Long) {
					value = Long.parseLong(valueString);
				} else if (defaultValue instanceof Float) {
					value = Float.parseFloat(valueString);
				} else if (defaultValue instanceof Double) {
					value = Double.parseDouble(valueString);
				} else if (defaultValue instanceof Enum<?>) {
					value = parseEnum(cast(defaultValue.getClass()), valueString);
				} else {
					value = valueString;
				}
				
				keyStep = 2;
			}
			
			this.set(key, value);
			
			keyIndex += keyStep;
		}
	}
	
	private final <T> T get(final String key) {
		this.parse();
		
		return cast(this.map.get(key));
	}
	
	@SuppressWarnings("unchecked")
	public static final <E extends Enum<?>> E parseEnum(final Class<E> enumClass, final String prefix) {
		try {
			for (final E enumValue : (E[]) enumClass.getMethod("values").invoke(null)) {
				if (enumValue.toString().startsWith(prefix)) {
					return enumValue;
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		
		throw new IllegalArgumentException(String.format("Failed to parse %s as %s", prefix, enumClass));
	}
	
}