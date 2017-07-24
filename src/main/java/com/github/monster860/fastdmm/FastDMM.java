package com.github.monster860.fastdmm;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.monster860.fastdmm.dmirender.DMI;
import com.github.monster860.fastdmm.dmirender.IconState;
import com.github.monster860.fastdmm.dmirender.IconSubstate;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.DMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.editing.*;
import com.github.monster860.fastdmm.editing.placement.DefaultPlacementMode;
import com.github.monster860.fastdmm.editing.placement.DeletePlacementMode;
import com.github.monster860.fastdmm.editing.placement.PlacementHandler;
import com.github.monster860.fastdmm.editing.placement.PlacementMode;
import com.github.monster860.fastdmm.editing.placement.SelectPlacementMode;
import com.github.monster860.fastdmm.editing.ui.EditorTabComponent;
import com.github.monster860.fastdmm.editing.ui.EmptyTabPanel;
import com.github.monster860.fastdmm.editing.ui.NoDmeTreeModel;
import com.github.monster860.fastdmm.editing.ui.ObjectTreeRenderer;
import com.github.monster860.fastdmm.objtree.InstancesRenderer;
import com.github.monster860.fastdmm.objtree.ModifiedType;
import com.github.monster860.fastdmm.objtree.ObjInstance;
import com.github.monster860.fastdmm.objtree.ObjectTree;
import com.github.monster860.fastdmm.objtree.ObjectTreeParser;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import org.json.*;

import static org.lwjgl.opengl.GL11.*;

public class FastDMM extends JFrame implements ActionListener, TreeSelectionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	public File dme;
	public DMM dmm;
	public List<DMM> loadedMaps = new ArrayList<DMM>();
	public Map<String, ModifiedType> modifiedTypes = new HashMap<>();
	
	private Stack<Undoable> undostack = new Stack<Undoable>();
	private Stack<Undoable> redostack = new Stack<Undoable>();

	public float viewportX = 0;
	public float viewportY = 0;
	public int viewportZoom = 32;

	int selX = 0;
	int selY = 0;

	boolean selMode = false;

	public String statusstring = " ";

	private JPanel leftPanel;
	private JPanel objTreePanel;
	private JPanel instancesPanel;
	private JPanel vpData;
	private JLabel coords;
	public JLabel selection;
	private JTabbedPane leftTabs;
	private JPanel editorPanel;
	private JTabbedPane editorTabs;
	private Canvas canvas;

	private JMenuBar menuBar;
	private JMenu menuRecent;
	private JMenu menuRecentMaps;
	private JMenuItem menuItemNew;
	private JMenuItem menuItemOpen;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemExpand;
	private JMenuItem menuItemMapImage;
	private JMenuItem menuItemUndo;
	private JMenuItem menuItemRedo;

	private JPopupMenu currPopup;

	public JTree objTreeVis;
	public JList<ObjInstance> instancesVis;

	SortedSet<String> filters;
	public ObjectTree objTree;

	public ObjectTree.Item selectedObject;
	public ObjInstance selectedInstance;

	private boolean hasLoadedImageThisFrame = false;

	private PlacementHandler currPlacementHandler = null;
	public PlacementMode placementMode = null;

	public boolean isCtrlPressed = false;
	public boolean isShiftPressed = false;
	public boolean isAltPressed = false;

	private boolean areMenusFrozen = false;

	public static final void main(String[] args) throws IOException, LWJGLException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException
				| IllegalAccessException e) {
			e.printStackTrace();
		}

		FastDMM fastdmm = new FastDMM();

		fastdmm.initSwing();
		fastdmm.interface_dmi = new DMI(Util.getFile("interface.dmi"));

		try {
			fastdmm.init();
			fastdmm.loop();
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			JOptionPane.showMessageDialog(fastdmm, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} finally {
			Display.destroy();
		}
	}

	public FastDMM() {
	}

	public void initSwing() {
		SwingUtilities.invokeLater(() -> {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			canvas = new Canvas();

			ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

			leftPanel = new JPanel();
			leftPanel.setLayout(new BorderLayout());
			leftPanel.setSize(350, 1);
			leftPanel.setPreferredSize(leftPanel.getSize());

			vpData = new JPanel();
			vpData.setLayout(new BorderLayout());
			vpData.setSize(350, 25);
			vpData.setPreferredSize(vpData.getSize());
			coords = new JLabel(" No DME loaded.");
			if (currPlacementHandler != null) {
				statusstring = "No tiles selected. ";
			}
			selection = new JLabel(statusstring);
			vpData.add(coords, BorderLayout.WEST);
			vpData.add(selection, BorderLayout.EAST);
			leftPanel.add(vpData, BorderLayout.SOUTH);

			instancesPanel = new JPanel();
			instancesPanel.setLayout(new BorderLayout());

			instancesVis = new JList<>();
			instancesVis.addListSelectionListener(FastDMM.this);
			instancesVis.setLayoutOrientation(JList.VERTICAL);
			instancesVis.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ToolTipManager.sharedInstance().registerComponent(instancesVis);
			instancesVis.setCellRenderer(new InstancesRenderer(FastDMM.this));
			instancesPanel.add(new JScrollPane(instancesVis));

			objTreePanel = new JPanel();
			objTreePanel.setLayout(new BorderLayout());

			objTreeVis = new JTree(new NoDmeTreeModel());
			objTreeVis.addTreeSelectionListener(FastDMM.this);
			ToolTipManager.sharedInstance().registerComponent(objTreeVis);
			objTreeVis.setCellRenderer(new ObjectTreeRenderer(FastDMM.this));
			objTreePanel.add(new JScrollPane(objTreeVis));

			leftTabs = new JTabbedPane();
			leftTabs.addTab("Objects", objTreePanel);
			leftTabs.addTab("Instances", instancesPanel);
			leftPanel.add(leftTabs, BorderLayout.CENTER);
			
			editorPanel = new JPanel();
			editorPanel.setLayout(new BorderLayout());
			editorPanel.add(canvas, BorderLayout.CENTER);
			
			editorTabs = new JTabbedPane();
			editorTabs.addChangeListener(new ChangeListener() {
		        public void stateChanged(ChangeEvent e) {
		        	if(editorTabs.getSelectedIndex() == -1) {
		        		dmm = null;
		        		FastDMM.this.setTitle(dme.getName().replaceFirst("[.][^.]+$", ""));
		        		return;
		        	}
		        	if(loadedMaps.get(editorTabs.getSelectedIndex()) == dmm)
		        		return;
		            synchronized(FastDMM.this) {
		            	if(dmm != null) {
		            		dmm.storedViewportX = viewportX;
		            		dmm.storedViewportY = viewportY;
		            		dmm.storedViewportZoom = viewportZoom;
		            	}
		            	dmm = loadedMaps.get(editorTabs.getSelectedIndex());
		            	viewportX = dmm.storedViewportX;
		            	viewportY = dmm.storedViewportY;
		            	viewportZoom = dmm.storedViewportZoom;
		            	FastDMM.this.setTitle(dme.getName().replaceFirst("[.][^.]+$", "") + ": "
								+ dmm.file.getName().replaceFirst("[.][^.]+$", ""));
		            }
		        }
		    });
			editorPanel.add(editorTabs, BorderLayout.NORTH);

			getContentPane().add(editorPanel, BorderLayout.CENTER);
			getContentPane().add(leftPanel, BorderLayout.WEST);

			setSize(1280, 720);
			setPreferredSize(getSize());
			pack();

			menuBar = new JMenuBar();

			JMenu menu = new JMenu("File");
			menu.setMnemonic(KeyEvent.VK_O);
			menu.getPopupMenu().setLightWeightPopupEnabled(false);
			menuBar.add(menu);

			menuItemNew = new JMenuItem("New");
			menuItemNew.setActionCommand("new");
			menuItemNew.addActionListener(FastDMM.this);
			menuItemNew.setEnabled(false);
			menu.add(menuItemNew);

			menuItemOpen = new JMenuItem("Open");
			menuItemOpen.setActionCommand("open");
			menuItemOpen.addActionListener(FastDMM.this);
			menuItemOpen.setEnabled(false);
			menu.add(menuItemOpen);

			menuRecentMaps = new JMenu("Recent Maps");
			menuRecentMaps.setMnemonic(KeyEvent.VK_O);
			menuRecentMaps.getPopupMenu().setLightWeightPopupEnabled(false);
			menuRecentMaps.setVisible(false);
			menuRecentMaps.setEnabled(false);
			menu.add(menuRecentMaps);

			menuItemSave = new JMenuItem("Save");
			menuItemSave.setActionCommand("save");
			menuItemSave.addActionListener(FastDMM.this);
			menuItemSave.setEnabled(false);
			menu.add(menuItemSave);

			JMenuItem menuItem = new JMenuItem("Open DME");
			menuItem.setActionCommand("open_dme");
			menuItem.addActionListener(FastDMM.this);
			menu.add(menuItem);

			menuRecent = new JMenu("Recent Environments");
			menuRecent.setMnemonic(KeyEvent.VK_O);
			menuRecent.getPopupMenu().setLightWeightPopupEnabled(false);
			menu.add(menuRecent);
			
			menu.addSeparator();
			
			menuItemMapImage = new JMenuItem("Create map image");
			menuItemMapImage.setActionCommand("mapimage");
			menuItemMapImage.addActionListener(FastDMM.this);
			menuItemMapImage.setEnabled(false);
			menu.add(menuItemMapImage);

			initRecent("dme");
			
			menu = new JMenu("Edit");
			menuBar.add(menu);

			menuItemUndo = new JMenuItem("Undo", KeyEvent.VK_U);
			menuItemUndo.setActionCommand("undo");
			menuItemUndo.addActionListener(FastDMM.this);
			menuItemUndo.setEnabled(false);
			menu.add(menuItemUndo);

			menuItemRedo = new JMenuItem("Redo", KeyEvent.VK_R);
			menuItemRedo.setActionCommand("redo");
			menuItemRedo.addActionListener(FastDMM.this);
			menuItemRedo.setEnabled(false);
			menu.add(menuItemRedo);
			
			menu = new JMenu("Options");
			menu.setMnemonic(KeyEvent.VK_O);
			menuBar.add(menu);

			menuItem = new JMenuItem("Change Filters", KeyEvent.VK_F);
			menuItem.setActionCommand("change_filters");
			menuItem.addActionListener(FastDMM.this);
			menu.add(menuItem);

			menuItemExpand = new JMenuItem("Expand Map");
			menuItemExpand.setActionCommand("expand");
			menuItemExpand.addActionListener(FastDMM.this);
			menuItemExpand.setEnabled(false);
			menu.add(menuItemExpand);

			menu.addSeparator();

			ButtonGroup placementGroup = new ButtonGroup();

			menuItem = new JRadioButtonMenuItem("Default Placement", true);
			menuItem.addItemListener(e -> { // I know this is ugly, but what can
											// you do
				statusstring = "Default Placement Mode ";
				if (dme == null || dmm == null) {
					statusstring = " ";
				}
				selection.setText(statusstring);
				selMode = false;
			});
			menuItem.addActionListener(new PlacementModeListener(this, placementMode = new DefaultPlacementMode()));
			placementGroup.add(menuItem);
			menu.add(menuItem);

			menuItem = new JRadioButtonMenuItem("Select", false);
			menuItem.addActionListener(new PlacementModeListener(this, new SelectPlacementMode()));
			menuItem.addItemListener(e -> {
				selMode = true;
			});
			placementGroup.add(menuItem);
			menu.add(menuItem);

			menuItem = new JRadioButtonMenuItem("Delete", false);
			menuItem.addActionListener(new PlacementModeListener(this, new DeletePlacementMode()));
			menuItem.addItemListener(e -> {
				selMode = false;
			});
			placementGroup.add(menuItem);
			menu.add(menuItem);

			setJMenuBar(menuBar);

			filters = new TreeSet<>(new FilterComparator());
			filters.add("/obj");
			filters.add("/turf");
			filters.add("/mob");
			filters.add("/area");

			// Yes, there's a good reason input is being handled in 2 places:
			// For some reason, this doesn't work when the LWJGL Canvas is in
			// focus.
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(e -> {
				isCtrlPressed = e.isControlDown();
				isShiftPressed = e.isShiftDown();
				isAltPressed = e.isAltDown();
				return false;
			});
		});
	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		if (arg0.getPath().getLastPathComponent() instanceof ObjectTree.Item) {
			selectedObject = (ObjectTree.Item) arg0.getPath().getLastPathComponent();
			instancesVis.setModel(selectedObject);
			if(selectedInstance == null || objTree.get(selectedInstance.typeString()) != selectedObject)
				selectedInstance = selectedObject;
			instancesVis.setSelectedValue(selectedInstance, true);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if(instancesVis.getSelectedValue() == null)
			return;
		selectedInstance = instancesVis.getSelectedValue();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (areMenusFrozen)
			return;
		if ("change_filters".equals(e.getActionCommand())) {
			JTextArea ta = new JTextArea(20, 40);
			StringBuilder taText = new StringBuilder();
			for (String filter : filters) {
				taText.append(filter);
				taText.append('\n');
			}
			ta.setText(taText.toString());
			if (JOptionPane.showConfirmDialog(canvas, new JScrollPane(ta), "Input filter",
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
				return;
			synchronized (filters) {
				filters.clear();
				for (String filter : ta.getText().split("(\\r\\n|\\r|\\n)")) {
					if (!filter.trim().isEmpty()) {
						if(filter.startsWith("~"))
							filter = '~' + ObjectTreeParser.cleanPath(filter.substring(1));
						else
							filter = ObjectTreeParser.cleanPath(filter);
						filters.add(filter);
					}
				}
			}
		} else if ("open_dme".equals(e.getActionCommand())) {
			openDME();
		} else if ("open".equals(e.getActionCommand())) {
			openDMM();
		} else if ("save".equals(e.getActionCommand())) {
			try {
				synchronized(this) {
					placementMode.flush(this);
				}
				dmm.save();
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if ("new".equals(e.getActionCommand())) {
			statusstring = " ";
			selection.setText(statusstring);
			String usePath = JOptionPane.showInputDialog(canvas,
					"Please enter the path of the new DMM file relative to your DME: ", "FastDMM",
					JOptionPane.QUESTION_MESSAGE);
			String strMaxX = (String) JOptionPane.showInputDialog(canvas, "Select the X-size of your new map",
					"FastDMM", JOptionPane.QUESTION_MESSAGE, null, null, "255");
			String strMaxY = (String) JOptionPane.showInputDialog(canvas, "Select the Y-size of your new map",
					"FastDMM", JOptionPane.QUESTION_MESSAGE, null, null, "255");
			String strMaxZ = (String) JOptionPane.showInputDialog(canvas,
					"Select the number of Z-levels of your new map", "FastDMM", JOptionPane.QUESTION_MESSAGE, null,
					null, "1");

			if (usePath == null || usePath.isEmpty())
				return;

			int maxX = 0;
			int maxY = 0;
			int maxZ = 0;

			try {
				maxX = Integer.parseInt(strMaxX);
				maxY = Integer.parseInt(strMaxY);
				maxZ = Integer.parseInt(strMaxZ);
			} catch (NumberFormatException ex) {
				return;
			}

			synchronized (this) {
				placementMode.flush(this);
				try {
					dmm = new DMM(new File(dme.getParentFile(), usePath), objTree, this);
					dmm.setSize(1, 1, 1, maxX, maxY, maxZ);
					menuItemExpand.setEnabled(true);
					menuItemMapImage.setEnabled(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (selMode) {
				SelectPlacementMode spm = (SelectPlacementMode) placementMode;
				spm.clearSelection();
			}
		} else if ("expand".equals(e.getActionCommand())) {
			if (dmm == null)
				return;
			String strMaxX = (String) JOptionPane.showInputDialog(canvas, "Select the new X-size", "FastDMM",
					JOptionPane.QUESTION_MESSAGE, null, null, "" + dmm.maxX);
			String strMaxY = (String) JOptionPane.showInputDialog(canvas, "Select the new Y-size", "FastDMM",
					JOptionPane.QUESTION_MESSAGE, null, null, "" + dmm.maxY);
			String strMaxZ = (String) JOptionPane.showInputDialog(canvas, "Select the new number of Z-levels",
					"FastDMM", JOptionPane.QUESTION_MESSAGE, null, null, "" + dmm.maxZ);

			int maxX = 0;
			int maxY = 0;
			int maxZ = 0;

			try {
				maxX = Integer.parseInt(strMaxX);
				maxY = Integer.parseInt(strMaxY);
				maxZ = Integer.parseInt(strMaxZ);
			} catch (NumberFormatException ex) {
				return;
			}

			synchronized (this) {
				dmm.setSize(1, 1, 1, maxX, maxY, maxZ);
			}
		} else if("mapimage".equals(e.getActionCommand())) {
			JFileChooser fc = new JFileChooser();
			if (fc.getChoosableFileFilters().length > 0)
				fc.removeChoosableFileFilter(fc.getChoosableFileFilters()[0]);
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Image (*.png)", "png"));
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				BufferedImage mapImage;
				synchronized(this) {
					mapImage = createMapImage(1);
				}
				try {
					ImageIO.write(mapImage, "png", fc.getSelectedFile());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else if("undo".equals(e.getActionCommand())) {
			undoAction();
		} else if("redo".equals(e.getActionCommand())) {
			redoAction();
		}
	}

	private void openDME(File filetoopen) {
		statusstring = " ";
		selection.setText(statusstring);
		synchronized (this) {
			objTree = null;
			dmm = null;
			if (selMode) {
				SelectPlacementMode spm = (SelectPlacementMode) placementMode;
				spm.clearSelection();
			}
			dme = filetoopen;
		}
		areMenusFrozen = true;
		menuItemOpen.setEnabled(false);
		menuItemSave.setEnabled(false);
		menuItemNew.setEnabled(false);
		menuItemExpand.setEnabled(false);
		menuRecent.setEnabled(false);
		menuRecentMaps.setEnabled(false);
		menuItemMapImage.setEnabled(false);
		menuRecentMaps.setVisible(false);
		modifiedTypes = new HashMap<>();
		while(editorTabs.getTabCount() > 0)
			editorTabs.removeTabAt(0);
		loadedMaps.clear();
		// PARSE TREE
		new Thread() {
			public void run() {
				try {
					ObjectTreeParser parser = new ObjectTreeParser();
					parser.modalParent = FastDMM.this;
					parser.parseDME(dme);
					parser.tree.completeTree();
					objTree = parser.tree;
					objTree.dmePath = dme.getAbsolutePath();
					objTreeVis.setModel(objTree);
					menuItemOpen.setEnabled(true);
					menuItemSave.setEnabled(true);
					menuItemNew.setEnabled(true);
					menuRecent.setEnabled(true);
					menuRecentMaps.setEnabled(true);
					menuRecentMaps.setVisible(true);
					areMenusFrozen = false;
				} catch (Exception ex) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					ex.printStackTrace(pw);
					JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
					dme = null;
					objTree = null;
				} finally {
					areMenusFrozen = false;
					addToRecent(dme, dmm);
					FastDMM.this.setTitle(dme.getName().replaceFirst("[.][^.]+$", ""));
					initRecent("both");
				}
			}
		}.start();
	}

	private void openDME() {
		JFileChooser fc = new JFileChooser();
		if (fc.getChoosableFileFilters().length > 0)
			fc.removeChoosableFileFilter(fc.getChoosableFileFilters()[0]);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("BYOND Environments (*.dme)", "dme"));
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			openDME(fc.getSelectedFile());
		}
	}

	private void openDMM(File filetoopen) {
		synchronized (this) {
			for(DMM map : loadedMaps) {
				try {
					if(map.file.getCanonicalPath().equals(filetoopen.getCanonicalPath())) {
						dmm = map;
						editorTabs.setSelectedIndex(loadedMaps.indexOf(map));
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			int insertIndex = loadedMaps.size();
			if(dmm != null)
				insertIndex = loadedMaps.contains(dmm) ? insertIndex : loadedMaps.indexOf(dmm);
			
			dmm = null;
			areMenusFrozen = true;
			DMM newDmm;
			try {
				newDmm = new DMM(filetoopen, objTree, this);
				dmm = newDmm;
				// Tabs!
				loadedMaps.add(dmm);
				int mapIndex = loadedMaps.indexOf(dmm);;
				editorTabs.insertTab(dmm.relPath, null, new EmptyTabPanel(editorPanel), dmm.relPath, mapIndex);
				editorTabs.setTabComponentAt(mapIndex, new EditorTabComponent(this, dmm));
				editorTabs.setSelectedIndex(mapIndex);
				viewportX = 0;
				viewportY = 0;
				viewportZoom = 32;
				menuItemExpand.setEnabled(true);
				menuItemMapImage.setEnabled(true);
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
				dmm = null;
				menuItemExpand.setEnabled(false);
				menuItemMapImage.setEnabled(false);
			} finally {
				areMenusFrozen = false;
				if (!selMode) {
					statusstring = "Default Placement Mode ";
				}
				selection.setText(statusstring);
				if (selMode) {
					SelectPlacementMode spm = (SelectPlacementMode) placementMode;
					spm.clearSelection();
				}
				addToRecent(dme, dmm);
				this.setTitle(dme.getName().replaceFirst("[.][^.]+$", "") + ": "
						+ dmm.file.getName().replaceFirst("[.][^.]+$", ""));
				initRecent("both");
			}
		}
	}

	private void openDMM() {
		List<File> dmms = getDmmFiles(dme.getParentFile());
		JList<File> dmmList = new JList<>(dmms.toArray(new File[dmms.size()]));

		if (JOptionPane.showConfirmDialog(canvas, new JScrollPane(dmmList), "Select a DMM",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
			return;

		if (dmmList.getSelectedValue() == null)
			return;

		openDMM(dmmList.getSelectedValue());
	}
	
	public void closeTab(DMM map) {
		synchronized(this) {
			int idx = loadedMaps.indexOf(map);
			loadedMaps.remove(idx);
			dmm = null;
			editorTabs.removeTabAt(idx);
		}
	}

	public static List<File> getDmmFiles(File directory) {
		List<File> l = new ArrayList<>();
		for (File f : directory.listFiles()) {
			if (f.getName().endsWith(".dmm") || f.getName().endsWith(".dmp")) {
				l.add(f);
			} else if (!f.getName().equals(".git") && !f.getName().equals("node_modules") && f.isDirectory()) { 
				// .git and node_modules usually contain fucktons of files and no dmm's.
				l.addAll(getDmmFiles(f));
			}
		}
		return l;
	}

	private void init() throws LWJGLException {
		String path = System.getProperty("user.home") + File.separator + ".fastdmm" + File.separator;
		Path AppDataPath = Paths.get(path);
		if (Files.notExists(AppDataPath)) {
			new File(path).mkdirs();
		}
		String dummy = System.getProperty("user.home") + File.separator + ".fastdmm" + File.separator + "dummy";
		File dummyFile = new File(dummy);
		if (!dummyFile.exists()) {
			convertintojson();
		}
		try {
			synchronized (this) {
				while (filters == null) {
					wait(1000);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setVisible(true);
		// Display.setDisplayMode(new DisplayMode(640, 480));
		// Display.setResizable(true);
		Display.setParent(canvas);
		Display.create();
		this.setTitle("FastDMM");

		if (interface_dmi != null) {
			interface_dmi.createGL();
		}
		Thread autosaveThread = new Thread(new AutosaveDaemon(this));
		autosaveThread.setDaemon(true);
		autosaveThread.start();
	}

	private Map<String, DMI> dmis = new HashMap<>();
	public DMI interface_dmi;

	public DMI getDmi(String name, boolean doInitGL) {
		if (dmis.containsKey(name)) {
			DMI dmi = dmis.get(name);
			if (dmi != null && doInitGL && dmi.glID == -1)
				dmi.createGL();
			return dmi;
		} else {
			if (hasLoadedImageThisFrame && doInitGL) {
				return interface_dmi;
			} else {
				hasLoadedImageThisFrame = true;
			}
			DMI dmi = null;
			try {
				if (name != null && name.trim().length() > 0) {
					dmi = new DMI(new File(dme.getParentFile(), objTree.filePath(Util.separatorsToSystem(name))));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dmi != null && doInitGL) {
				dmi.createGL();
			}
			if (dmi == null)
				dmi = interface_dmi;
			dmis.put(name, dmi);
			return dmi;
		}
	}

	private void processInput() {
		float xpos = Mouse.getX();
		float ypos = Display.getHeight() - Mouse.getY();
		double dx = Mouse.getDX();
		double dy = -Mouse.getDY();

		if (dmm == null) {
			viewportX = 0;
			viewportY = 0;
		}

		float xScrOff = (float) Display.getWidth() / viewportZoom / 2;
		float yScrOff = (float) Display.getHeight() / viewportZoom / 2;

		int prevSelX = selX, prevSelY = selY;

		selX = (int) Math.floor(viewportX + (xpos / viewportZoom) - xScrOff);
		selY = (int) Math.floor(viewportY - (ypos / viewportZoom) + yScrOff);

		if ((prevSelX != selX || prevSelY != selY) && currPlacementHandler != null) {
			currPlacementHandler.dragTo(new Location(selX, selY, 1));
		}

		if (dme == null || dmm == null) { // putting this here because it's the
											// only func that updates regularly
											// besides loop()
			statusstring = " ";
		}

		float dwheel = Mouse.getDWheel();
		if (dwheel != 0) {
			if (dwheel > 0)
				viewportZoom *= 2;
			else if (dwheel < 0)
				viewportZoom /= 2;
			if (viewportZoom < 8)
				viewportZoom = 8;
			if (viewportZoom > 128)
				viewportZoom = 128;
		}

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)
					isCtrlPressed = true;
				if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
					isShiftPressed = true;
				if (Keyboard.getEventKey() == Keyboard.KEY_LMENU || Keyboard.getEventKey() == Keyboard.KEY_RMENU)
					isAltPressed = true;
			} else {
				if (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)
					isCtrlPressed = false;
				if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
					isShiftPressed = false;
				if (Keyboard.getEventKey() == Keyboard.KEY_LMENU || Keyboard.getEventKey() == Keyboard.KEY_RMENU)
					isAltPressed = false;
			}
		}

		if (Mouse.isButtonDown(2) || (Mouse.isButtonDown(0) && isAltPressed)) {
			viewportX -= (dx / viewportZoom);
			viewportY += (dy / viewportZoom);
		}

		if (dme != null) {
			if (dmm != null) {
				if (selX >= 1 && selY >= 1 && selX <= dmm.maxX && selY <= dmm.maxY) {
					String tcoord = " " + String.valueOf(selX) + ", " + String.valueOf(selY);
					coords.setText(tcoord);
				} else {
					coords.setText(" Out of bounds.");
				}
			} else {
				coords.setText(" No DMM loaded.");
			}
		} else {
			coords.setText(" No DME loaded.");
		}

		if (dmm == null || dme == null)
			return;

		while (Mouse.next()) {
			if (isAltPressed)
				continue;
			if (Mouse.getEventButtonState()) {
				if (currPopup != null && !currPopup.isVisible())
					currPopup = null;
				if (currPopup != null) {
					currPopup.setVisible(false);
					currPopup = null;
					continue;
				}
				Location l = new Location(selX, selY, 1);
				String key = dmm.map.get(l);
				if (Mouse.getEventButton() == 1) {
					if (key != null) {
						SwingUtilities.invokeLater(() -> {
							TileInstance tInstance = dmm.instances.get(key);
							currPopup = new JPopupMenu();
							currPopup.setLightWeightPopupEnabled(false);
							placementMode.addToTileMenu(this, l, tInstance, currPopup);
							List<ObjInstance> layerSorted = tInstance.getLayerSorted();
							for (int idx = layerSorted.size() - 1; idx >= 0; idx--) {
								ObjInstance i = layerSorted.get(idx);
								if (i == null)
									continue;
								boolean valid = inFilter(i);
	
								JMenu menu = new JMenu(i.getVar("name") + " (" + i.typeString() + ")");
								DMI dmi = getDmi(i.getIcon(), false);
								if (dmi != null) {
									String iconState = i.getIconState();
									IconSubstate substate = dmi.getIconState(iconState).getSubstate(i.getDir());
									menu.setIcon(substate.getScaled());
								}
								if (valid)
									menu.setFont(menu.getFont().deriveFont(Font.BOLD)); // Make it bold if is visible by the filter.
								currPopup.add(menu);
	
								JMenuItem item = new JMenuItem("Make Active Object");
								item.addActionListener(new MakeActiveObjectListener(this, l, i));
								menu.add(item);
	
								item = new JMenuItem("Delete");
								item.addActionListener(new DeleteListener(this, l, i));
								menu.add(item);
	
								item = new JMenuItem("View Variables");
								item.addActionListener(new EditVarsListener(this, l, i));
								menu.add(item);
	
								item = new JMenuItem("Move to Top");
								item.addActionListener(new MoveToTopListener(this, l, i));
								menu.add(item);
	
								item = new JMenuItem("Move to Bottom");
								item.addActionListener(new MoveToBottomListener(this, l, i));
								menu.add(item);
							}
							canvas.getParent().add(currPopup);
							currPopup.show(canvas, Mouse.getX(), Display.getHeight() - Mouse.getY());
						});
					}
				} else if (Mouse.getEventButton() == 0) {
					currPlacementHandler = placementMode.getPlacementHandler(this, selectedInstance, l);
					if (currPlacementHandler != null)
						currPlacementHandler.init(this, selectedInstance, l);
				}
			} else {
				if (Mouse.getEventButton() == 0 && currPlacementHandler != null) {
					synchronized (this) {
						currPlacementHandler.finalizePlacement();
					}
					currPlacementHandler = null;
				}
			}
		}
	}

	private void loop() {

		// Set the clear color
		glClearColor(0.25f, 0.25f, 0.5f, 1.0f);

		int width;
		int height;

		while (!Display.isCloseRequested()) {

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			width = Display.getWidth();
			height = Display.getHeight();
			glViewport(0, 0, width, height);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			float xScrOff = (float) width / viewportZoom / 2;
			float yScrOff = (float) height / viewportZoom / 2;
			glOrtho(-xScrOff, xScrOff, -yScrOff, yScrOff, 100, -100);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glTranslatef(.5f, .5f, 0);
			float roundPlace = (objTree != null ? objTree.icon_size : 32) * viewportZoom / 32f;
			glTranslatef(-Math.round(viewportX*roundPlace)/roundPlace, -Math.round(viewportY*roundPlace)/roundPlace, 0);

			glEnable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);

			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

			hasLoadedImageThisFrame = false;
			Set<RenderInstance> rendInstanceSet = buildViewport(true,
					(int)Math.floor(viewportX - xScrOff - 2),
					(int)Math.ceil(viewportX + xScrOff + 2),
					(int)Math.floor(viewportY - yScrOff - 2),
					(int)Math.ceil(viewportY + yScrOff + 2), 1, true);
					

			for (RenderInstance ri : rendInstanceSet) {
				glColor3f(ri.color.getRed() / 255f, ri.color.getGreen() / 255f, ri.color.getBlue() / 255f);
				glBindTexture(GL_TEXTURE_2D, ri.substate.dmi.glID);

				glPushMatrix();
				glTranslatef(ri.x, ri.y, 0);
				glBegin(GL_QUADS);
				glTexCoord2f(ri.substate.x2, ri.substate.y1);
				glVertex3f(-.5f + (ri.substate.dmi.width / (float) objTree.icon_size),
						-.5f + (ri.substate.dmi.height / (float) objTree.icon_size), 0);
				glTexCoord2f(ri.substate.x1, ri.substate.y1);
				glVertex3f(-.5f, -.5f + (ri.substate.dmi.height / (float) objTree.icon_size), 0);
				glTexCoord2f(ri.substate.x1, ri.substate.y2);
				glVertex3f(-.5f, -.5f, 0);
				glTexCoord2f(ri.substate.x2, ri.substate.y2);
				glVertex3f(-.5f + (ri.substate.dmi.width / (float) objTree.icon_size), -.5f, 0);
				glEnd();
				glPopMatrix();
			}

			glBindTexture(GL_TEXTURE_2D, -1);
			glColor4f(1, 1, 1, .25f);
			glPushMatrix();
			glTranslatef(selX, selY, 1);
			glBegin(GL_QUADS);
			glVertex3f(.5f, .5f, 0);
			glVertex3f(-.5f, .5f, 0);
			glVertex3f(-.5f, -.5f, 0);
			glVertex3f(.5f, -.5f, 0);
			glEnd();
			glPopMatrix();

			Display.update();

			processInput();

			Display.sync(60);
		}
	}
	
	private Set<RenderInstance> buildViewport(boolean editingElements, int minx, int maxx, int miny, int maxy, int zlev, boolean glIcons) {
		int currCreationIndex = 0;
		Set<RenderInstance> rendInstanceSet = new TreeSet<>();
		Location l = new Location(1, 1, zlev);
		if (dme != null && dmm != null) {
			synchronized (this) {
				for (int x = minx; x <= maxx; x++) {
					for (int y = miny; y <= maxy; y++) {
						l.x = x;
						l.y = y;
						String instanceID = dmm.map.get(l);
						if (instanceID == null)
							continue;
						TileInstance instance = dmm.instances.get(instanceID);
						if (instance == null)
							continue;
						for (ObjInstance oInstance : instance.getLayerSorted()) {
							if (oInstance == null)
								continue;
							boolean valid = inFilter(oInstance);
							if (!valid)
								continue;
							DMI dmi = getDmi(oInstance.getIcon(), glIcons);
							if (dmi == null)
								continue;
							String iconState = oInstance.getIconState();
							IconSubstate substate = dmi.getIconState(iconState).getSubstate(oInstance.getDir());

							RenderInstance ri = new RenderInstance(currCreationIndex++);
							ri.layer = oInstance.getLayer();
							ri.plane = oInstance.getPlane();
							ri.x = x + (oInstance.getPixelX() / (float) objTree.icon_size);
							ri.y = y + (oInstance.getPixelY() / (float) objTree.icon_size);
							ri.substate = substate;
							ri.color = oInstance.getColor();

							rendInstanceSet.add(ri);
						}
						if(editingElements) {
							int dirs = 0;
							for (int i = 0; i < 4; i++) {
								int cdir = IconState.indexToDirArray[i];
								Location l2 = l.getStep(cdir);
								String instId = dmm.map.get(l2);
								if (instId == null) {
									dirs |= cdir;
									continue;
								}
								TileInstance instance2 = dmm.instances.get(instId);
								if (instance2 == null) {
									dirs |= cdir;
									continue;
								}
								if (!instance.getArea().equals(instance2.getArea())) {
									dirs |= cdir;
								}
							}
							if (dirs != 0) {
								RenderInstance ri = new RenderInstance(currCreationIndex++);
								ri.plane = 101;
								ri.x = x;
								ri.y = y;
								ri.substate = interface_dmi.getIconState("" + dirs).getSubstate(2);
								ri.color = new Color(200, 200, 200);
								
								rendInstanceSet.add(ri);
							}
						}
					}
				}
			}
		}
		
		if(editingElements) {
			if (currPlacementHandler != null) {
				currCreationIndex = currPlacementHandler.visualize(rendInstanceSet, currCreationIndex);
			}
			
			currCreationIndex = placementMode.visualize(rendInstanceSet, currCreationIndex);
		}
		return rendInstanceSet;
	}

	private void addToRecent(File dme, DMM dmm) {
		String path = System.getProperty("user.home") + File.separator + ".fastdmm" + File.separator + "recent.json";
		File file = new File(path);
		JSONObject main = new JSONObject();

		if (!file.exists()) {
			JSONObject env = new JSONObject();
			JSONArray maplist = new JSONArray();
			env.put("dme", dme.getPath());
			env.put("maplist", maplist);
			main.put("dme1", env);
			try {
				Files.write(Paths.get(path), main.toString().getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		FileReader reader = null;
		try {
			reader = new FileReader(path);
		} catch (FileNotFoundException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		main = new JSONObject(new JSONTokener(Objects.requireNonNull(reader)));

		Iterator<?> keys = main.keys();
		String checkmaps = null;
		String listmap = null;
		boolean createDME = true;

		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (main.get(key) instanceof JSONObject) {
				JSONObject environ = main.getJSONObject(key);
				if (environ.get("dme").equals(dme.getPath())) {
					checkmaps = key;
					createDME = false;
				}
			}
		}

		if (createDME) {
			JSONObject environ = new JSONObject();
			JSONArray listmaps = new JSONArray();
			environ.put("dme", dme.getPath());
			environ.put("maplist", listmaps);
			main.put("dme" + String.valueOf(JSONObject.getNames(main).length + 1), environ);
			checkmaps = "dme" + String.valueOf(JSONObject.getNames(main).length);
		}

		JSONObject tocheck = main.getJSONObject(checkmaps);
		Iterator<?> checkkeys = tocheck.keys();

		if (dmm != null) {
			while (checkkeys.hasNext()) {
				String key = (String) checkkeys.next();
				if (tocheck.get(key) instanceof JSONArray) {
					JSONArray array = tocheck.getJSONArray(key);
					listmap = key;
					for (int i = 0; i < array.length(); i++) {
						String mapcheck = array.getString(i);
						if (mapcheck.equals(dmm.file.getPath())) {
							return;
						}
					}
				}
			}

			JSONObject addmap = main.getJSONObject(checkmaps);
			JSONArray listofmaps = addmap.getJSONArray(listmap);

			if (listofmaps.length() == 0) {
				listofmaps.put(listofmaps.length(), dmm.file.getPath());
			} else {
				listofmaps.put(listofmaps.length() + 1, dmm.file.getPath());
			}
		}

		String toWrite = main.toString();
		toWrite = toWrite.replaceAll(",null", ""); // yes, it's bad but I have
													// no idea why the null
													// thing happens

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(toWrite);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initRecent(String mode) {
		String recentPath = System.getProperty("user.home") + File.separator + ".fastdmm" + File.separator
				+ "recent.json";
		File recent = new File(recentPath);
		if (recent.exists()) {
			JSONObject main;
			FileReader reader = null;
			try {
				reader = new FileReader(recentPath);
			} catch (FileNotFoundException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			main = new JSONObject(new JSONTokener(Objects.requireNonNull(reader)));

			if (mode.equals("dme") || mode.equals("both")) {
				Iterator<?> keys = main.keys();
				menuRecent.removeAll();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (main.get(key) instanceof JSONObject) {
						JSONObject environ = main.getJSONObject(key);
						File f = new File(environ.getString("dme"));
						JMenuItem menuItem = new JMenuItem(environ.getString("dme"));
						menuItem.addActionListener(arg0 -> openDME(f));
						menuItem.setEnabled(true);
						menuRecent.add(menuItem);
					}
				}
			}

			if (mode.equals("dmm") || mode.equals("both")) {
				Iterator<?> keys = main.keys();
				menuRecentMaps.removeAll();
				String checkmaps = null;
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (main.get(key) instanceof JSONObject) {
						JSONObject environ = main.getJSONObject(key);
						if (environ.get("dme").equals(dme.getPath())) {
							checkmaps = key;
						}
					}
				}

				if (checkmaps != null) {
					JSONObject tocheck = main.getJSONObject(checkmaps);
					Iterator<?> checkkeys = tocheck.keys();

					while (checkkeys.hasNext()) {
						String keymap = (String) checkkeys.next();
						if (tocheck.get(keymap) instanceof JSONArray) {
							JSONArray array = tocheck.getJSONArray(keymap);
							for (int i = 0; i < array.length(); i++) {
								String map = array.getString(i);
								File f = new File(map);
								JMenuItem menuItem = new JMenuItem(map);
								menuItem.addActionListener(arg0 -> openDMM(f));
								menuItem.setEnabled(true);
								menuRecentMaps.add(menuItem);
							}
						}
					}
				}
			}
		}
	}

	private void convertintojson() {
		String recentPath = System.getProperty("user.home") + File.separator + ".fastdmm" + File.separator
				+ "recent.txt";
		File recent = new File(recentPath);
		if (recent.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(recentPath))) {
				String line;
				while ((line = br.readLine()) != null) {
					File dmetoadd = new File(line);
					if (dmetoadd.exists()) {
						addToRecent(dmetoadd, null);
					}
				}
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			recent.delete();
			String dummy = System.getProperty("user.home") + File.separator + ".fastdmm" + File.separator + "dummy";
			try {
				Files.write(Paths.get(dummy), "".getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				JOptionPane.showMessageDialog(FastDMM.this, sw.getBuffer(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private BufferedImage createMapImage(int zlev) {
		int imgwidth = (dmm.maxX+1-dmm.minX)*objTree.icon_size;
		int imgheight = (dmm.maxY+1-dmm.minY)*objTree.icon_size;
		
		BufferedImage img = new BufferedImage(imgwidth, imgheight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		
		Set<RenderInstance> rendInstanceSet = buildViewport(false, dmm.minX, dmm.maxX, dmm.minY, dmm.maxY, 1, false);
		
		for (RenderInstance ri : rendInstanceSet) {
			DMI dmi = ri.substate.dmi;
			int px = (int)((ri.x - dmm.minX) * objTree.icon_size);
			int py = (int)((dmm.maxY - ri.y + dmm.minY - 1) * objTree.icon_size) - (dmi.height - objTree.icon_size);
			
			if(ri.color.getRed() == 255 && ri.color.getGreen() == 255 && ri.color.getBlue() == 255) {
				// Easy time!
				g.drawImage(dmi.image, px, py, px+dmi.width, py+dmi.height, ri.substate.i_x1, ri.substate.i_y1, ri.substate.i_x2+1, ri.substate.i_y2+1, null);
			} else {
				float rm = ri.color.getRed() / 255f;
				float gm = ri.color.getGreen() / 255f;
				float bm = ri.color.getBlue() / 255f;
				float am = ri.color.getAlpha() / 255f;
				if(am == 0)
					continue;
				for(int x = 0; x < dmi.width; x++) {
					for(int y = 0; y < dmi.height; y++) {
						int pixel = dmi.image.getRGB(x+ri.substate.i_x1, y+ri.substate.i_y1);
						if(((pixel >> 24) & 0xFF) == 0)
							continue;
						g.setColor(new Color((int)(((pixel >> 16) & 0xFF) * rm),(int)(((pixel >> 8) & 0xFF) * gm),(int)((pixel & 0xFF) * bm),(int)(((pixel >> 24) & 0xFF) * am)));
						g.drawRect(px+x, py+y, 0, 0);
					}
				}
			}
		}
		g.dispose();
		return img;
	}
	
	public boolean inFilter(ObjInstance i) {
		boolean valid = false;
		synchronized (filters) {
			for (String s : filters) {
				boolean newValid = true;
				if(s.startsWith("~")) {
					s = s.substring(1);
					newValid = false; 
				}
					
				if(i.toString().startsWith(s)) {
					valid = newValid;
				}
			}
		}
		return valid;
	}
	
	public void addToUndoStack(Undoable action){
		if(action == null) {
			return;
		}
		undostack.push(action);
		redostack.clear();
		menuItemUndo.setEnabled(true);
		menuItemRedo.setEnabled(false);
	}
	
	public boolean undoAction(){
		if(undostack.empty()) {
			return false;
		}
		Undoable action = undostack.pop();
		redostack.push(action);
		menuItemRedo.setEnabled(true);
		menuItemUndo.setEnabled(!undostack.isEmpty());
		return action.undo();
	}
	
	public boolean redoAction(){
		if(redostack.empty()) {
			return false;
		}
		Undoable action = redostack.pop();
		undostack.push(action);
		menuItemUndo.setEnabled(true);
		menuItemRedo.setEnabled(!redostack.isEmpty());
		return action.redo();
	}
	
}
