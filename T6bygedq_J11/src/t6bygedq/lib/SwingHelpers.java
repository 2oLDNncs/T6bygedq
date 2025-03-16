package t6bygedq.lib;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * @author 2oLDNncs 20250316
 *
 */
public final class SwingHelpers {
	
	public static final Window show(final Component component, final String title) {
		return show(component, title, false);
	}
	
	public static final Window show(final Component component, final String title, final boolean modal) {
		final Window[] result = { null };
		
		try {
			final Runnable task = new Runnable() {
				
				@Override
				public final void run() {
					if (modal) {
						result[0] = new JDialog((JFrame) null, title, modal);
						((JDialog) result[0]).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					} else {
						result[0] = new JFrame(title);
						((JFrame) result[0]).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					}
					
					result[0].add(component);
					
					packAndCenter(result[0]).setVisible(true);
				}
				
			};
			
			if (SwingUtilities.isEventDispatchThread()) {
				task.run();
			} else {
				SwingUtilities.invokeAndWait(task);
			}
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
		
		return result[0];
	}
	
	public static final <W extends Window> W packAndCenter(final W window) {
		checkAWT();

		window.pack();

		return center(window);
	}
	
	public static final <W extends Window> W center(final W window) {
		checkAWT();

		window.setLocationRelativeTo(null);

		return window;
	}
	
	public static final JScrollPane scrollable(final Component component) {
		return new JScrollPane(component);
	}
	
	public static final JSplitPane horizontalSplit(final Component leftComponent, final Component rightComponent) {
		return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);
	}
	
	public static final JSplitPane verticalSplit(final Component topComponent, final Component bottomComponent) {
		return new JSplitPane(JSplitPane.VERTICAL_SPLIT, topComponent, bottomComponent);
	}
	
	public static final void useSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}
	
	public static final void checkAWT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException();
		}
	}
	
}
