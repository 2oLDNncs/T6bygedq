package t6bygedq.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import t6bygedq.app.ParseSysadata.RecReader;
import t6bygedq.lib.ArgsParser;
import t6bygedq.lib.Helpers;
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
	
	private final JTable recTable = new JTable(new DefaultTableModel(new Object[] {"Rec"}, 0));
	
	private final JTree parseTreeView = new JTree(new DefaultTreeModel(new SwingNode(), false));
	
	private final JTextPane contentView = new JTextPane();
	
	private final JTabbedPane explorerView = new JTabbedPane();
	
	private final Map<Long, Rec> symbols = new HashMap<>();
	
	private final Map<Long, SwingNode> swingNodes = new HashMap<>();
	
	private Pattern filterPattern = Pattern.compile("");
	
	public SysadataViewer() {
		super(new BorderLayout());
		
		final var listPanel = new JPanel(new BorderLayout());
		
		final var filter = new JTextField();
		
		listPanel.add(filter, BorderLayout.NORTH);
		listPanel.add(SwingHelpers.scrollable(this.recTable), BorderLayout.CENTER);
		
		this.explorerView.addTab("List", listPanel);
		this.explorerView.addTab("Tree", SwingHelpers.scrollable(this.parseTreeView));
		
		recTable.setAutoCreateRowSorter(true);
		
		((TableRowSorter<DefaultTableModel>) this.recTable.getRowSorter()).setRowFilter(new RowFilter<>() {
			
			@Override
			public final boolean include(final Entry<? extends DefaultTableModel, ? extends Integer> entry) {
				return filterPattern.matcher(entry.getStringValue(0)).find();
			}
			
		});
		
		filter.addCaretListener(new CaretListener() {
			
			private final Timer timer = new Timer(150, this::update);
			
			@Override
			public final void caretUpdate(final CaretEvent e) {
				this.timer.restart();
			}
			
			private final void update(final ActionEvent e) {
				try {
					filterPattern = Pattern.compile(filter.getText(), Pattern.CASE_INSENSITIVE);
					recTable.getRowSorter().allRowsChanged();
					filter.setForeground(null);
				} catch (final Exception e1) {
					Helpers.ignore(e1);
					filter.setForeground(Color.RED);
				}
			}
			
		});
		
		this.recTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.recTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public final void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					final var selectedRow = recTable.getSelectedRow();
					
					if (0 <= selectedRow) {
						updateContentView((RecItem) recTable.getValueAt(selectedRow, 0));
					}
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
		
		this.swingNodes.put(0L, (SwingNode) this.parseTreeView.getModel().getRoot());
		
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
	
	private final DefaultTableModel getRecTableModel() {
		return (DefaultTableModel) this.recTable.getModel();
	}
	
	public final void addRec(final Rec rec) {
		final var recItem = new RecItem(this.getRecTableModel().getRowCount(), rec);
		
		this.getRecTableModel().addRow(new Object[] { recItem });
		
		if (rec.getRecData() instanceof RecData_X0020_ExternalSymbol
				|| rec.getRecData() instanceof RecData_X0042_Symbol) {
			final var props = rec.getRecData().getProperties();
			this.symbols.put(((LongVar) props.get("SymbolId")).get(), rec);
		} else if (rec.getRecData() instanceof RecData_X0024_ParseTree) {
			final var tm = this.getParseTreeModel();
			final var props = rec.getRecData().getProperties();
			final var nodeNumber = ((LongVar) props.get("NodeNumber")).get();
			final var swingNode = this.getSwingNode(nodeNumber);
			final var parentNodeNumber = ((LongVar) props.get("ParentNodeNumber")).get();
			final var parentSwingNode = this.getSwingNode(parentNodeNumber);
			final var leftSiblingNodeNumber = ((LongVar) props.get("LeftSiblingNodeNumber")).get();
			
			if (0 < leftSiblingNodeNumber) {
				final var leftSiblingSwingNode = this.getSwingNode(leftSiblingNodeNumber);
				
				this.reparent(leftSiblingSwingNode, parentSwingNode);
			}
			
			this.reparent(swingNode, parentSwingNode);
			
			tm.valueForPathChanged(new TreePath(tm.getPathToRoot(swingNode)), recItem);
			
			this.sortChildren(parentSwingNode);
		}
	}
	
	private final void reparent(final SwingNode node, final SwingNode newParent) {
		final var oldParent = (SwingNode) node.getParent();
		
		if (newParent != oldParent) {
			this.getParseTreeModel().insertNodeInto(node, newParent, newParent.getChildCount());
			this.sortChildren(oldParent);
		}
	}
	
	private final void sortChildren(final SwingNode parent) {
		final var tm = this.getParseTreeModel();
		final var n = parent.getChildCount();
		
		for (var i = 0; i < n; i += 1) {
			final var child = (SwingNode) parent.getChildAt(i);
			final var childIndex = child.getIndex();
			
			if (0 <= childIndex && childIndex < n && i != childIndex) {
				final var otherChild = (SwingNode) parent.getChildAt(childIndex);
				
				if (childIndex != otherChild.getIndex()) {
					tm.insertNodeInto(child, parent, childIndex);
					
					i -= 1;
				}
			}
		}
	}
	
	private final SwingNode getSwingNode(final long nodeNumber) {
		return this.swingNodes.computeIfAbsent(nodeNumber, __ -> {
			final var result = new SwingNode();
			
			this.getParseTreeModel().insertNodeInto(result, swingNodes.get(0L), swingNodes.get(0L).getChildCount());
			
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
	 * @author 2oLDNncs 20250318
	 */
	private final class SwingNode extends DefaultMutableTreeNode {
		
		private long leftSiblingNodeNumber = -1;
		
		private int index = -1;
		
		public final int getIndex() {
			if (this.index < 0) {
				final var left = this.getLeftSiblingNodeNumber();
				
				if (0 == left) {
					this.index = 0;
				} else {
					final var leftSwingNode = swingNodes.get(left);
					
					if (null != leftSwingNode) {
						final var leftIndex = leftSwingNode.getIndex();
						
						if (0 <= leftIndex) {
							this.index = 1 + leftIndex;
						}
					}
				}
			}
			
			return this.index;
		}
		
		private final long getLeftSiblingNodeNumber() {
			if (this.leftSiblingNodeNumber < 0L) {
				final var recItem = (RecItem) this.getUserObject();
				
				if (null != recItem) {
					final var properties = recItem.rec().getRecData().getProperties();
					
					this.leftSiblingNodeNumber = ((LongVar) properties.get("LeftSiblingNodeNumber")).get();
				}
			}
			
			return this.leftSiblingNodeNumber;
		}
		
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
			
			result.append(this.index);
			result.append(": ");
			result.append(this.rec.getRecHeader().getRecordType());
			
			final var properties = this.rec.getRecData().getProperties();
			
			appendProp(properties, "JobName", result);
			appendProp(properties, "StepName", result);
			appendProp(properties, "NodeNumber", result);
			appendProp(properties, "SymbolId", result);
			appendProp(properties, "SymbolName", result);
			appendProp(properties, "ExternalName", result);
			appendProp(properties, "TokenNumber", result);
			appendProp(properties, "TokenText", result);
			appendProp(properties, "ProgramName", result);
			appendProp(properties, "LibraryName", result);
			appendProp(properties, "LibraryDdname", result);
			appendProp(properties, "FirstTokenNumber", result);
			appendProp(properties, "LastTokenNumber", result);
			appendProp(properties, "RecordType", result);
			
			return result.toString();
		}
		
		private static final void appendProp(final Map<String, Object> properties, final String key, final StringBuilder sb) {
			final var vProp = properties.get(key);
			
			if (null != vProp) {
				sb.append("(");
				sb.append(key);
				sb.append(":");
				try {
					sb.append(vProp.toString());
				} catch (final Exception e) {
					e.printStackTrace();
					sb.append("<span style=\"color:red\">?</span>");
				}
				sb.append(")");
			}
		}
		
	}
	
}
