package t6bygedq.lib;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

/**
 * @author 2oLDNncs 20241108
 */
public final class Reshaper {
	
	private final int inputPageWidth;
	
	private final ReshaperLayout inputLayout;
	
	private final List<List<String>> data = new ArrayList<>();
	
	private final Index inputLineIndexInPage;
	
	private final Index inputPageVerticalIndex;
	
	private final Index inputPageHorizontalIndex;
	
	public Reshaper(final int inputPageWidth, final int inputPageHeight,
			final ReshaperLayout inputLayout, final int inputLayoutStride) {
		this.inputPageWidth = inputPageWidth;
		this.inputLineIndexInPage = new Index(inputPageHeight);
		this.inputLayout = inputLayout;
		this.inputPageVerticalIndex = new Index(inputLayoutStride);
		this.inputPageHorizontalIndex = new Index(inputLayoutStride);
	}
	
	private final String formatDataRowElement(final String element) {
		return String.format("%-" + this.inputPageWidth + "." + this.inputPageWidth + "s", element);
	}
	
	public static final int to1D(final Index i0, final Index i1) {
		return i1.getValue() + i0.getValue() * i1.getEnd();
	}
	
	public final void addInputLine(final String inputLine) {
		final var r = to1D(this.inputPageVerticalIndex, this.inputLineIndexInPage);
		final var c = this.inputPageHorizontalIndex.getValue();
		
		growToIndex(this.data, r, __ -> new ArrayList<>());
		growToIndex(this.data.get(r), c, __ -> this.formatDataRowElement(""));
		
		this.data.get(r).set(c, this.formatDataRowElement(inputLine));
		
		if (!this.inputLineIndexInPage.next()) {
			this.inputLayout.next(this.inputPageVerticalIndex, this.inputPageHorizontalIndex);
		}
	}
	
	public static final <E> void growToIndex(final List<E> list,
			final int targetIndex, final IntFunction<E> elementSupplier) {
		growToSize(list, targetIndex + 1, elementSupplier);
	}
	
	public static final <E> void growToSize(final Collection<E> collection,
			final int targetSize, final IntFunction<E> elementSupplier) {
		var n = collection.size();
		
		while (n < targetSize) {
			collection.add(elementSupplier.apply(n));
			n = collection.size();
		}
	}
	
	public final void printTo(final PrintStream out) {
		this.checkData();
		
		this.data.stream()
		.map(row -> String.join("", row))
		.forEach(out::println);
	}
	
	public final void printTo(final PrintStream out,
			final int outputPageWidth, final int outputPageHeight, final ReshaperLayout outputLayout) {
		this.checkData();
		
		final var page = this.new Page(outputPageWidth, outputPageHeight, outputLayout);
		
		while (page.isValid()) {
			page.printTo(out);
			page.next();
		}
	}
	
	final int getDataWidth() {
		return addLengths(this.data.get(0));
	}
	
	private static final int addLengths(final Collection<String> strings) {
		return strings.stream()
				.mapToInt(String::length)
				.sum();
	}
	
	final int getDataHeight() {
		return this.data.size();
	}
	
	final String getData(final int offsetV, final int offsetH, final int width) {
		return String.join("", this.data.get(offsetV)).substring(offsetH, offsetH + width);
	}
	
	private final void checkData() {
		if (!this.data.isEmpty()) {
			final var dataWidth = this.getDataWidth();
			
			for (final var row : this.data) {
				final var rowLength = addLengths(row);
				
				if (dataWidth != rowLength) {
					throw new IllegalStateException(String.format("dataWidth: %s rowLength: %s", dataWidth, rowLength));
				}
			}
		}
	}
	
	/**
	 * @author 2oLDNncs 20241108
	 */
	private final class Page {
		
		private final Index vIndex;
		
		private final Index hIndex;
		
		private final ReshaperLayout layout;
		
		Page(final int width, final int height, final ReshaperLayout layout) {
			this.vIndex = new Index(height, getDataHeight());
			this.hIndex = new Index(width, getDataWidth());
			this.layout = layout;
		}
		
		final boolean isValid() {
			return this.vIndex.isValid() && this.hIndex.isValid();
		}
		
		final void next() {
			this.layout.next(this.vIndex, this.hIndex);
		}
		
		final void printTo(final PrintStream out) {
			for (var k = 0; k < this.vIndex.getStep(); k += 1) {
				out.println(getData(this.vIndex.getValue() + k, this.hIndex.getValue(), this.hIndex.getStep()));
			}
		}
		
	}
	
}