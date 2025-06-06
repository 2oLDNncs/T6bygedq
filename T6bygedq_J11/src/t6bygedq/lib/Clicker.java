package t6bygedq.lib;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author 2oLDNncs 20250606
 */
public final class Clicker {
	
	private final Robot robot;
	
	public Clicker() throws AWTException {
		this.robot = new Robot();
	}
	
	public final void moveToImage(final String imagePath) throws IOException {
		final var pattern = ImageIO.read(new File(imagePath));
		final var finder = new ImageFinder(pattern);
		final var screen = this.robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		
		finder.findIn(screen);
		
		Log.out(1, finder.getBestMatchLeft(), finder.getBestMatchTop(), finder.getBestMatchScore());
		
		if (0 == finder.getBestMatchScore()) {
			this.moveTo(finder.getBestMatchLeft() + pattern.getWidth() / 2, finder.getBestMatchTop() + pattern.getHeight() / 2);
		} else {
			throw new IllegalStateException("Pattern not found: " + imagePath);
		}
	}
	
	public final void moveTo(final int x, final int y) {
		this.robot.mouseMove(x, y);
	}
	
	public final void click() {
		this.robot.mousePress(M1);
		this.robot.mouseRelease(M1);
	}
	
	public final void type(final String text) {
		for (final var c : text.toCharArray()) {
			this.type(c);
		}
	}
	
	private final void type(final char c) {
		final var k = KeyEvent.getExtendedKeyCodeForChar(c);
		this.robot.keyPress(k);
		this.robot.keyRelease(k);
	}
	
	public static final int M1 = InputEvent.getMaskForButton(MouseEvent.BUTTON1);
	
	/**
	 * @author 2oLDNncs 20250606
	 */
	public static final class ImageFinder {
		
		private final BufferedImage pattern;
		
		private final int tolerance;
		
		private int bestMatchLeft;
		private int bestMatchTop;
		private int bestMatchScore;
		
		public ImageFinder(final BufferedImage pattern) {
			this(pattern, 0);
		}
		
		public ImageFinder(final BufferedImage pattern, final int tolerance) {
			this.pattern = pattern;
			this.tolerance = tolerance;
		}
		
		public final int getBestMatchLeft() {
			return this.bestMatchLeft;
		}
		
		public final int getBestMatchTop() {
			return this.bestMatchTop;
		}
		
		public final int getBestMatchScore() {
			return this.bestMatchScore;
		}
		
		public final void findIn(final BufferedImage image) {
			this.bestMatchLeft = -1;
			this.bestMatchTop = -1;
			this.bestMatchScore = Integer.MAX_VALUE;
			
			for (var top = 0; top < image.getHeight() - this.pattern.getHeight(); top += 1) {
				Log.out(1, top);
				for (var left = 0; left < image.getWidth() - this.pattern.getWidth(); left += 1) {
					final var score = this.computeScoreAt(left, top, image);
					
					if (score < this.bestMatchScore) {
						this.bestMatchLeft = left;
						this.bestMatchTop = top;
						this.bestMatchScore = score;
					}
				}
			}
		}
		
		private final int computeScoreAt(final int left, final int top, final BufferedImage image) {
			var result = 0;
			
			for (var y = 0; y < this.pattern.getHeight(); y += 1) {
				for (var x = 0; x < this.pattern.getWidth(); x += 1) {
					final var patternRgb = this.pattern.getRGB(x, y);
					final var imageRgb = image.getRGB(left + x, top + y);
					
					result += (patternRgb != imageRgb) ? 1 : 0;
					
					if (this.bestMatchScore < result || this.tolerance < result) {
						break;
					}
				}
			}
			
			return result;
		}
		
	}
	
}
