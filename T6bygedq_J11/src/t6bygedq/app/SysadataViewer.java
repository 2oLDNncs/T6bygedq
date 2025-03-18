package t6bygedq.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import t6bygedq.app.ParseSysadata.RecReader;
import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.SwingHelpers;
import t6bygedq.lib.cbl.LongVar;
import t6bygedq.lib.cbl.Rec;
import t6bygedq.lib.cbl.RecData_X0020_ExternalSymbol;
import t6bygedq.lib.cbl.RecData_X0024_ParseTree;
import t6bygedq.lib.cbl.RecData_X0042_Symbol;

/**
 * @author 2oLDNncs 20250313
 */
public final class SysadataViewer extends JPanel {
	
	private final JList<RecItem> recList = new JList<>(new DefaultListModel<>());
	
	private final JTree parseTreeView = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(), false));
	
	private final JTextPane contentView = new JTextPane();
	
	private final JTabbedPane explorerView = new JTabbedPane();
	
	private final Map<Long, Rec> symbols = new HashMap<>();
	
	private final Map<Long, MutableTreeNode> nodes = new HashMap<>();
	
	public SysadataViewer() {
		super(new BorderLayout());
		
		this.explorerView.addTab("List", SwingHelpers.scrollable(this.recList));
		this.explorerView.addTab("Tree", SwingHelpers.scrollable(this.parseTreeView));
		
		this.recList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.recList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public final void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateContentView(recList.getSelectedValue());
				}
			}
			
		});
		
		this.parseTreeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.parseTreeView.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public final void valueChanged(final TreeSelectionEvent e) {
				final var selected = parseTreeView.getSelectionPath();
				
				if (null != selected) {
					updateContentView((RecItem) ((DefaultMutableTreeNode) selected.getLastPathComponent()).getUserObject());
				}
			}
			
		});
		
		this.nodes.put(0L, (MutableTreeNode) this.parseTreeView.getModel().getRoot());
		
		this.contentView.setContentType("text/html");
		this.contentView.setEditable(false);
		
		this.setPreferredSize(new Dimension(800, 580));
		
		this.add(SwingHelpers.horizontalSplit(
				this.explorerView,
				SwingHelpers.scrollable(this.contentView)),
				BorderLayout.CENTER);
	}
	
	private final void updateContentView(final RecItem selectedRecItem) {
		if (null != selectedRecItem) {
			final var selectedRec = selectedRecItem.rec();
			
			final var text = new StringBuilder();
			
			text.append("<html><body>");
			text.append("<a name=\"top\"></a>");
			
			if (selectedRec.getRecData() instanceof RecData_X0024_ParseTree) {
				text.append("<h1>Node</h1>");
			}
			
			printHeaderAndData(selectedRec, text);
			
			if (selectedRec.getRecData() instanceof RecData_X0024_ParseTree) {
				text.append("<hr/>");
				text.append("<h1>Symbol</h1>");
				
				final var rpt = (RecData_X0024_ParseTree) selectedRec.getRecData();
				final var symbolRec = this.symbols.get(((LongVar) rpt.getProperties().get("SymbolId")).get());
				
				if (null != symbolRec) {
					printHeaderAndData(symbolRec, text);
				}
			}
			
			text.append("</body></html>");
			
			this.contentView.setText(text.toString());
			this.contentView.scrollToReference("top");
		} else {
			this.contentView.setText("");
		}
	}
	
	private static final void printHeaderAndData(final Rec rec, final StringBuilder out) {
		out.append("<h2>Header</h2>");

		out.append("<div>");
		out.append(toHTMLTable(rec.getRecHeader().getProperties()));
		out.append("</div>");
		
		out.append("<h2>Data</h2>");
		
		out.append("<div>");
		out.append(toHTMLTable(rec.getRecData().getProperties()));
		out.append("</div>");
	}
	
	private static final String toHTMLTable(final Map<?, ?> map) {
		final var result = new StringBuilder();
		
		result.append("<table>");
		
		map.forEach((k, v) -> {
			result.append("<tr>");
			
			result.append("<td>");
			result.append(k);
			result.append("</td>");
			
			result.append("<td>");
			try {
				result.append(v);
			} catch (final Exception e) {
				new Exception(String.format("Error retrieving %s", k), e).printStackTrace();
				
				result.append("<span style='color: red'>?</span>");
			}
			result.append("</td>");
			
			result.append("</tr>");
		});
		
		result.append("</table>");
		
		return result.toString();
	}
	
	public final void addRec(final Rec rec) {
		final var recItem = new RecItem(this.recList.getModel().getSize(), rec);
		
		((DefaultListModel<RecItem>) this.recList.getModel()).addElement(recItem);
		
		if (rec.getRecData() instanceof RecData_X0020_ExternalSymbol
				|| rec.getRecData() instanceof RecData_X0042_Symbol) {
			final var props = rec.getRecData().getProperties();
			this.symbols.put(((LongVar) props.get("SymbolId")).get(), rec);
		} else if (rec.getRecData() instanceof RecData_X0024_ParseTree) {
			final var tm = this.getParseTreeModel();
			
			final var props = rec.getRecData().getProperties();
			
			final var nodeNumber = ((LongVar) props.get("NodeNumber")).get();
			final var node = this.getParseTreeNode(nodeNumber);
			
			final var parentNodeNumber = ((LongVar) props.get("ParentNodeNumber")).get();
			final var parentNode = this.getParseTreeNode(parentNodeNumber);
			
			final var leftSiblingNodeNumber = ((LongVar) props.get("LeftSiblingNodeNumber")).get();
			
			if (0 < leftSiblingNodeNumber) {
				final var leftSiblingNode = this.getParseTreeNode(leftSiblingNodeNumber);
				
				if (leftSiblingNode.getParent() != parentNode) {
					if (null != leftSiblingNode.getParent()) {
						tm.removeNodeFromParent(leftSiblingNode);
					}
					
					tm.insertNodeInto(leftSiblingNode, parentNode, parentNode.getChildCount());
				}
			}
			
			if (null != node.getParent()) {
				tm.removeNodeFromParent(node);
			}
			
			tm.insertNodeInto(node, parentNode, parentNode.getChildCount());
			
			tm.valueForPathChanged(new TreePath(tm.getPathToRoot(node)), recItem);
		}
	}
	
	private final MutableTreeNode getParseTreeNode(final long nodeNumber) {
		return this.nodes.computeIfAbsent(nodeNumber, __ -> {
			final var result = new DefaultMutableTreeNode(null);
			
			this.getParseTreeModel().insertNodeInto(result, nodes.get(0L), nodes.get(0L).getChildCount());
			
			return result;
		});
	}
	
	private final DefaultTreeModel getParseTreeModel() {
		return (DefaultTreeModel) this.parseTreeView.getModel();
	}
	
	static final Preferences prefs = Preferences.userNodeForPackage(SysadataViewer.class);
	
	static final String K_JFC_CURRENT_DIRECTORY_PATH = "jfc.currentDirectoryPath";
	
	public static final String K_VERSION = "-Version";
	
	public static final void main(final String... args) throws IOException {
		final var ap = new ArgsParser(args);
		
		ap.setDefault(K_VERSION, "6.1");
		
		final RecReader rr;
		
		switch (ap.getString(K_VERSION)) {
		case "4.2":
			rr = Rec::read_4_2;
			break;
		case "6.1":
			rr = Rec::read_6_1;
			break;
		default:
			throw new IllegalArgumentException(String.format("Unknown version: %s", ap.getString(K_VERSION)));
		}
		
		SwingHelpers.useSystemLookAndFeel();
		
		final var sv = new SysadataViewer();
		final var w = SwingHelpers.show(sv, "...");
		final var jfc = new JFileChooser(prefs.get(K_JFC_CURRENT_DIRECTORY_PATH, ""));
		
		if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(w)) {
			prefs.put(K_JFC_CURRENT_DIRECTORY_PATH, jfc.getCurrentDirectory().getPath());
			
			SwingUtilities.invokeLater(() -> {
				((JFrame) w).setTitle(jfc.getSelectedFile().getName());
			});
			
			if (!loadFromFile(jfc.getSelectedFile(), rr, rec -> {
				SwingUtilities.invokeLater(() -> {
					sv.addRec(rec);
				});
			})) {
				SwingUtilities.invokeLater(() -> {
					w.dispose();
				});
			}
		}
	}
	
	public static final boolean loadFromFile(final File file, final RecReader recReader, final Consumer<Rec> action) throws IOException {
		try (final var it = new ParseSysadata.ProgressiveRecReader(file, recReader)) {
			final var progressBar = new JProgressBar(0, (int) it.getTotalBytes());
			
			progressBar.setStringPainted(true);
			progressBar.setPreferredSize(new Dimension(400, 20));
			
			final var w = SwingHelpers.show(progressBar, "Loading...");
			final var canceled = new AtomicBoolean(false);
			
			while (it.hasNext() && !canceled.get()) {
				final var progress = (int) it.getBytesRead();
				
				SwingUtilities.invokeLater(() -> {
					progressBar.setValue(progress);
					
					if (!w.isDisplayable()) {
						canceled.set(true);
					}
				});
				
				action.accept(it.next());
			}
			
			SwingUtilities.invokeLater(() -> {
				w.dispose();
			});
			
			if (canceled.get()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @author 2oLDNncs 20250316
	 */
	private static final class RecItem {
		
		private final int index;
		
		private final Rec rec;
		
		RecItem(final int index, final Rec rec) {
			this.index = index;
			this.rec = rec;
		}
		
		public final int index() {
			return this.index;
		}
		
		public final Rec rec() {
			return this.rec;
		}
		
		@Override
		public final String toString() {
			final var result = new StringBuilder();
			
			result.append(index);
			result.append(": ");
			result.append(rec.getRecHeader().getRecordType());
			
			return result.toString();
		}
		
	}
	
}
