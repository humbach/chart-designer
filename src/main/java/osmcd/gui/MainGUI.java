/*******************************************************************************
 * Copyright (c) OSMCB developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package osmcd.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import osmcd.externaltools.ExternalToolDef;
import osmcd.externaltools.ExternalToolsLoader;
import osmcd.gui.actions.AddGpxTrackAreaPolygonMap;
import osmcd.gui.actions.AddGpxTrackPolygonMap;
import osmcd.gui.actions.AddMapLayer;
import osmcd.gui.actions.AtlasConvert;
import osmcd.gui.actions.AtlasNew;
import osmcd.gui.actions.BookmarkAdd;
import osmcd.gui.actions.BookmarkManage;
import osmcd.gui.actions.DebugSetLogLevel;
import osmcd.gui.actions.DebugShowLogFile;
import osmcd.gui.actions.DebugShowMapSourceNames;
import osmcd.gui.actions.DebugShowMapTileGrid;
import osmcd.gui.actions.DebugShowReport;
import osmcd.gui.actions.HelpLicenses;
import osmcd.gui.actions.PanelShowHide;
import osmcd.gui.actions.RefreshCustomMapsources;
import osmcd.gui.actions.SelectionModeCircle;
import osmcd.gui.actions.SelectionModePolygon;
import osmcd.gui.actions.SelectionModeRectangle;
import osmcd.gui.actions.ShowAboutDialog;
import osmcd.gui.actions.ShowHelpAction;
import osmcd.gui.actions.ShowReadme;
import osmcd.gui.atlastree.JAtlasTree;
import osmcd.gui.components.FilledLayeredPane;
import osmcd.gui.components.JAtlasNameField;
import osmcd.gui.components.JBookmarkMenuItem;
import osmcd.gui.components.JCollapsiblePanel;
import osmcd.gui.components.JMenuItem2;
import osmcd.gui.components.JZoomCheckBox;
import osmcd.gui.listeners.AtlasModelListener;
import osmcd.gui.mapview.GridZoom;
import osmcd.gui.mapview.JMapViewer;
import osmcd.gui.mapview.PreviewMap;
import osmcd.gui.mapview.WgsGrid.WgsDensity;
import osmcd.gui.mapview.controller.JMapController;
import osmcd.gui.mapview.controller.PolygonCircleSelectionMapController;
import osmcd.gui.mapview.controller.PolygonSelectionMapController;
import osmcd.gui.mapview.controller.RectangleSelectionMapController;
import osmcd.gui.mapview.interfaces.MapEventListener;
import osmcd.gui.panels.JCoordinatesPanel;
import osmcd.gui.panels.JGpxPanel;
import osmcd.gui.panels.JProfilesPanel;
import osmcd.gui.panels.JTileImageParametersPanel;
import osmcd.gui.panels.JTileStoreCoveragePanel;
import osmcd.gui.settings.SettingsGUI;
import osmcd.mapsources.MapSourcesManager;
import osmcd.program.ProgramInfo;
import osmcd.program.interfaces.BundleInterface;
import osmcd.program.interfaces.InitializableMapSource;
import osmcd.program.interfaces.MapSource;
import osmcd.program.model.Bookmark;
import osmcd.program.model.MapSelection;
import osmcd.program.model.MercatorPixelCoordinate;
import osmcd.program.model.Profile;
import osmcd.program.model.SelectedZoomLevels;
import osmcd.program.model.Settings;
import osmcd.program.model.SettingsWgsGrid;
import osmcd.program.model.TileImageParameters;
import osmcd.utilities.GBC;
import osmcd.utilities.GUIExceptionHandler;
import osmcd.utilities.I18nUtils;
import osmcd.utilities.Utilities;

public class MainGUI extends JFrame implements MapEventListener
{
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(MainGUI.class);

	private static Color labelBackgroundColor = new Color(0, 0, 0, 127);
	private static Color checkboxBackgroundColor = new Color(0, 0, 0, 40);
	private static Color labelForegroundColor = Color.WHITE;

	private static MainGUI mainGUI = null;
	public static final ArrayList<Image> OSMCD_ICONS = new ArrayList<Image>(3);

	static
	{
		OSMCD_ICONS.add(Utilities.loadResourceImageIcon("osmcd48.png").getImage());
		OSMCD_ICONS.add(Utilities.loadResourceImageIcon("osmcd32.png").getImage());
		OSMCD_ICONS.add(Utilities.loadResourceImageIcon("osmcd16.png").getImage());
	}

	protected JMenuBar menuBar;
	protected JMenu toolsMenu = null;

	private JMenu bookmarkMenu = null;

	public final PreviewMap previewMap = new PreviewMap();
	public final JAtlasTree jAtlasTree = new JAtlasTree(previewMap);

	private JCheckBox wgsGridCheckBox;
	private JComboBox wgsGridCombo;

	private JLabel zoomLevelText;
	private JComboBox gridZoomCombo;
	private JSlider zoomSlider;
	private JComboBox mapSourceCombo;
	private JButton settingsButton;
	private JAtlasNameField atlasNameTextField;
	// private JButton createAtlasButton;
	private JPanel zoomLevelPanel;
	private JZoomCheckBox[] cbZoom = new JZoomCheckBox[0];
	private JLabel amountOfTilesLabel;

	private JCoordinatesPanel coordinatesPanel;
	private JProfilesPanel profilesPanel;
	public JTileImageParametersPanel tileImageParametersPanel;
	private JTileStoreCoveragePanel tileStoreCoveragePanel;
	public JGpxPanel gpxPanel;

	private JPanel mapControlPanel = new JPanel(new BorderLayout());
	private JPanel leftPanel = new JPanel(new GridBagLayout());
	private JPanel leftPanelContent = null;
	private JPanel rightPanel = new JPanel(new GridBagLayout());

	public JMenu logLevelMenu;
	private JMenuItem smRectangle;
	private JMenuItem smPolygon;
	private JMenuItem smCircle;

	private MercatorPixelCoordinate mapSelectionMax = null;
	private MercatorPixelCoordinate mapSelectionMin = null;

	public static void createMainGui()
	{
		if (mainGUI != null)
			return;

		mainGUI = new MainGUI();
		mainGUI.setVisible(true);
		log.trace("MainGUI now visible");
	}

	public static MainGUI getMainGUI()
	{
		return mainGUI;
	}

	// MP: get custom font
	static Font sCustomFont = null;

	public static Font customFont()
	{
		if (sCustomFont == null)
		{
			// force to use Chinese font
			sCustomFont = new Font("宋体", 9, 13);
		}
		return sCustomFont;
	}

	// MP: update all UI components' default font to custom font
	public static void setDefaultFontOfAllUIComponents(Font defaultFont)
	{
		if (defaultFont != null)
		{
			// register custom font to application，system font will return false
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(defaultFont);

			// update all UI's font settings
			javax.swing.plaf.FontUIResource fontRes = new javax.swing.plaf.FontUIResource(defaultFont);
			Enumeration<Object> keys = UIManager.getDefaults().keys();
			while (keys.hasMoreElements())
			{
				Object key = keys.nextElement();
				Object value = UIManager.get(key);
				if (value instanceof javax.swing.plaf.FontUIResource)
				{
					UIManager.put(key, fontRes);
				}
			}
		}
	}

	private MainGUI() {
		super();
		mainGUI = this;
		setIconImages(OSMCD_ICONS);

		GUIExceptionHandler.registerForCurrentThread();
		setTitle(ProgramInfo.getCompleteTitle());

		log.trace("Creating main dialog - " + getTitle());
		setResizable(true);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setMinimumSize(new Dimension(Math.min(800, dScreen.width), Math.min(590, dScreen.height)));
		setSize(getMinimumSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowDestroyer());
		addComponentListener(new MainWindowListener());

		previewMap.addMapEventListener(this);

		createControls();
		calculateNrOfTilesToDownload();
		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
		JLayeredPane layeredPane = new FilledLayeredPane();
		layeredPane.add(previewMap, Integer.valueOf(0));
		layeredPane.add(mapControlPanel, Integer.valueOf(1));
		add(layeredPane, BorderLayout.CENTER);

		updateMapControlsPanel();
		updateLeftPanel();
		updateRightPanel();
		updateZoomLevelCheckBoxes();
		calculateNrOfTilesToDownload();

		menuBar = new JMenuBar();
		prepareMenuBar();
		setJMenuBar(menuBar);

		loadSettings();
		profilesPanel.initialize();
		mapSourceChanged(previewMap.getMapSource());
		updateZoomLevelCheckBoxes();
		updateGridSizeCombo();
		tileImageParametersPanel.updateControlsState();
		zoomChanged(previewMap.getZoom());
		gridZoomChanged(previewMap.getGridZoom());
		previewMap.updateMapSelection();
		previewMap.grabFocus();
	}

	private void createControls()
	{

		// zoom slider
		zoomSlider = new JSlider(JMapViewer.MIN_ZOOM, previewMap.getMapSource().getMaxZoom());
		zoomSlider.setOrientation(JSlider.HORIZONTAL);
		zoomSlider.setMinimumSize(new Dimension(50, 10));
		zoomSlider.setSize(50, zoomSlider.getPreferredSize().height);
		zoomSlider.addChangeListener(new ZoomSliderListener());
		zoomSlider.setOpaque(false);

		// zoom level text
		zoomLevelText = new JLabel(" 00 ");
		zoomLevelText.setOpaque(true);
		zoomLevelText.setBackground(labelBackgroundColor);
		zoomLevelText.setForeground(labelForegroundColor);
		zoomLevelText.setToolTipText(I18nUtils.localizedStringForKey("map_ctrl_zoom_level_title_tips"));

		// grid zoom combo
		gridZoomCombo = new JComboBox();
		gridZoomCombo.setEditable(false);
		gridZoomCombo.addActionListener(new GridZoomComboListener());
		gridZoomCombo.setToolTipText(I18nUtils.localizedStringForKey("map_ctrl_zoom_grid_tips"));

		SettingsWgsGrid s = Settings.getInstance().wgsGrid;

		// WGS Grid label
		wgsGridCheckBox = new JCheckBox(I18nUtils.localizedStringForKey("map_ctrl_wgs_grid_title"), s.enabled);
		// wgsGridCheckBox.setOpaque(true);
		wgsGridCheckBox.setOpaque(true);
		wgsGridCheckBox.setBackground(checkboxBackgroundColor);
		wgsGridCheckBox.setForeground(labelForegroundColor);
		wgsGridCheckBox.setToolTipText(I18nUtils.localizedStringForKey("map_ctrl_wgs_grid_tips"));
		wgsGridCheckBox.setMargin(new Insets(0, 0, 0, 0));
		wgsGridCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean enabled = wgsGridCheckBox.isSelected();
				Settings.getInstance().wgsGrid.enabled = enabled;
				wgsGridCombo.setVisible(enabled);
				previewMap.repaint();
			}
		});

		// WGS Grid combo
		wgsGridCombo = new JComboBox(WgsDensity.values());
		wgsGridCombo.setMaximumRowCount(WgsDensity.values().length);
		wgsGridCombo.setVisible(s.enabled);
		wgsGridCombo.setSelectedItem(s.density);
		wgsGridCombo.setToolTipText(I18nUtils.localizedStringForKey("map_ctrl_wgs_grid_density_tips"));
		wgsGridCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				WgsDensity d = (WgsDensity) wgsGridCombo.getSelectedItem();
				Settings.getInstance().wgsGrid.density = d;
				previewMap.repaint();
			}
		});

		// map source combo
		mapSourceCombo = new JComboBox(MapSourcesManager.getInstance().getEnabledOrderedMapSources());
		mapSourceCombo.setMaximumRowCount(20);
		mapSourceCombo.addActionListener(new MapSourceComboListener());
		mapSourceCombo.setToolTipText(I18nUtils.localizedStringForKey("lp_map_source_combo_tips"));

		// settings button
		settingsButton = new JButton(I18nUtils.localizedStringForKey("lp_main_setting_button_title"));
		settingsButton.addActionListener(new SettingsButtonListener());
		settingsButton.setToolTipText(I18nUtils.localizedStringForKey("lp_main_setting_button_tips"));

		// atlas name text field
		atlasNameTextField = new JAtlasNameField();
		atlasNameTextField.setColumns(12);
		atlasNameTextField.setActionCommand("atlasNameTextField");
		atlasNameTextField.setToolTipText(I18nUtils.localizedStringForKey("lp_atlas_name_field_tips"));

		// // main button
		// createAtlasButton = new JButton(I18nUtils.localizedStringForKey("lp_mian_create_btn_title"));
		// createAtlasButton.addActionListener(atlasCreateAction);
		// createAtlasButton.setToolTipText(I18nUtils.localizedStringForKey("lp_main_create_btn_tips"));

		// zoom level check boxes
		zoomLevelPanel = new JPanel();
		zoomLevelPanel.setBorder(BorderFactory.createEmptyBorder());
		zoomLevelPanel.setOpaque(false);

		// amount of tiles to download
		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setToolTipText(I18nUtils.localizedStringForKey("lp_zoom_total_tile_count_tips"));
		amountOfTilesLabel.setOpaque(true);
		amountOfTilesLabel.setBackground(labelBackgroundColor);
		amountOfTilesLabel.setForeground(labelForegroundColor);

		coordinatesPanel = new JCoordinatesPanel();
		tileImageParametersPanel = new JTileImageParametersPanel();
		profilesPanel = new JProfilesPanel(jAtlasTree);
		profilesPanel.getLoadButton().addActionListener(new LoadProfileListener());
		tileStoreCoveragePanel = new JTileStoreCoveragePanel(previewMap);
	}

	private void prepareMenuBar()
	{
		// Bundle menu
		JMenu atlasMenu = new JMenu(I18nUtils.localizedStringForKey("menu_atlas"));
		atlasMenu.setMnemonic(KeyEvent.VK_A);

		JMenuItem newAtlas = new JMenuItem(I18nUtils.localizedStringForKey("menu_atlas_new"));
		newAtlas.setMnemonic(KeyEvent.VK_N);
		newAtlas.addActionListener(new AtlasNew());
		atlasMenu.add(newAtlas);

		JMenuItem convertAtlas = new JMenuItem(I18nUtils.localizedStringForKey("menu_atlas_convert_format"));
		convertAtlas.setMnemonic(KeyEvent.VK_V);
		convertAtlas.addActionListener(new AtlasConvert());
		atlasMenu.add(convertAtlas);
		atlasMenu.addSeparator();

		// Maps menu
		JMenu mapsMenu = new JMenu(I18nUtils.localizedStringForKey("menu_maps"));
		mapsMenu.setMnemonic(KeyEvent.VK_M);
		JMenu selectionModeMenu = new JMenu(I18nUtils.localizedStringForKey("menu_maps_selection"));
		selectionModeMenu.setMnemonic(KeyEvent.VK_M);
		mapsMenu.add(selectionModeMenu);

		smRectangle = new JRadioButtonMenuItem(I18nUtils.localizedStringForKey("menu_maps_selection_rect"));
		smRectangle.addActionListener(new SelectionModeRectangle());
		smRectangle.setSelected(true);
		selectionModeMenu.add(smRectangle);

		smPolygon = new JRadioButtonMenuItem(I18nUtils.localizedStringForKey("menu_maps_selection_polygon"));
		smPolygon.addActionListener(new SelectionModePolygon());
		selectionModeMenu.add(smPolygon);

		smCircle = new JRadioButtonMenuItem(I18nUtils.localizedStringForKey("menu_maps_selection_circle"));
		smCircle.addActionListener(new SelectionModeCircle());
		selectionModeMenu.add(smCircle);

		JMenuItem addSelection = new JMenuItem(I18nUtils.localizedStringForKey("menu_maps_selection_add"));
		addSelection.addActionListener(AddMapLayer.INSTANCE);
		addSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		addSelection.setMnemonic(KeyEvent.VK_A);
		mapsMenu.add(addSelection);

		JMenuItem addGpxTrackSelection = new JMenuItem2(I18nUtils.localizedStringForKey("menu_maps_selection_add_around_gpx"), AddGpxTrackPolygonMap.class);
		mapsMenu.add(addGpxTrackSelection);

		JMenuItem addGpxTrackAreaSelection = new JMenuItem2(I18nUtils.localizedStringForKey("menu_maps_selection_add_by_gpx"), AddGpxTrackAreaPolygonMap.class);
		mapsMenu.add(addGpxTrackAreaSelection);

		// Bookmarks menu
		bookmarkMenu = new JMenu(I18nUtils.localizedStringForKey("menu_bookmark"));
		bookmarkMenu.setMnemonic(KeyEvent.VK_B);
		JMenuItem addBookmark = new JMenuItem(I18nUtils.localizedStringForKey("menu_bookmark_save"));
		addBookmark.setMnemonic(KeyEvent.VK_S);
		addBookmark.addActionListener(new BookmarkAdd(previewMap));
		bookmarkMenu.add(addBookmark);
		JMenuItem manageBookmarks = new JMenuItem2(I18nUtils.localizedStringForKey("menu_bookmark_manage"), BookmarkManage.class);
		manageBookmarks.setMnemonic(KeyEvent.VK_S);
		bookmarkMenu.add(addBookmark);
		bookmarkMenu.add(manageBookmarks);
		bookmarkMenu.addSeparator();

		// Panels menu
		JMenu panelsMenu = new JMenu(I18nUtils.localizedStringForKey("menu_panels"));
		panelsMenu.setMnemonic(KeyEvent.VK_P);
		JMenuItem showLeftPanel = new JMenuItem(I18nUtils.localizedStringForKey("menu_show_hide_left_panel"));
		showLeftPanel.addActionListener(new PanelShowHide(leftPanel));
		JMenuItem showRightPanel = new JMenuItem(I18nUtils.localizedStringForKey("menu_show_hide_gpx_panel"));
		showRightPanel.addActionListener(new PanelShowHide(rightPanel));
		panelsMenu.add(showLeftPanel);
		panelsMenu.add(showRightPanel);

		menuBar.add(atlasMenu);
		menuBar.add(mapsMenu);
		menuBar.add(bookmarkMenu);
		menuBar.add(panelsMenu);

		loadToolsMenu();

		menuBar.add(Box.createHorizontalGlue());

		// Debug menu
		JMenu debugMenu = new JMenu(I18nUtils.localizedStringForKey("menu_debug"));
		JMenuItem mapGrid = new JCheckBoxMenuItem(I18nUtils.localizedStringForKey("menu_debug_show_hide_tile_border"), false);
		mapGrid.addActionListener(new DebugShowMapTileGrid());
		debugMenu.add(mapGrid);
		debugMenu.addSeparator();

		debugMenu.setMnemonic(KeyEvent.VK_D);
		JMenuItem mapSourceNames = new JMenuItem2(I18nUtils.localizedStringForKey("menu_debug_show_all_map_source"), DebugShowMapSourceNames.class);
		mapSourceNames.setMnemonic(KeyEvent.VK_N);
		debugMenu.add(mapSourceNames);
		debugMenu.addSeparator();

		JMenuItem refreshCustomMapSources = new JMenuItem2(I18nUtils.localizedStringForKey("menu_debug_refresh_map_source"), RefreshCustomMapsources.class);
		debugMenu.add(refreshCustomMapSources);
		debugMenu.addSeparator();
		JMenuItem showLog = new JMenuItem2(I18nUtils.localizedStringForKey("menu_debug_show_log_file"), DebugShowLogFile.class);
		showLog.setMnemonic(KeyEvent.VK_S);
		debugMenu.add(showLog);

		logLevelMenu = new JMenu(I18nUtils.localizedStringForKey("menu_debug_log_level"));
		logLevelMenu.setMnemonic(KeyEvent.VK_L);
		Level[] list = new Level[] {Level.TRACE, Level.DEBUG, Level.INFO, Level.ERROR, Level.FATAL, Level.OFF};
		ActionListener al = new DebugSetLogLevel();
		Level rootLogLevel = Logger.getRootLogger().getLevel();
		for (Level level: list)
		{
			String name = level.toString();
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(name, (rootLogLevel.toString().equals(name)));
			item.setName(name);
			item.addActionListener(al);
			logLevelMenu.add(item);
		}
		debugMenu.add(logLevelMenu);
		debugMenu.addSeparator();
		JMenuItem report = new JMenuItem2(I18nUtils.localizedStringForKey("menu_debug_system_report"), DebugShowReport.class);
		report.setMnemonic(KeyEvent.VK_R);
		debugMenu.add(report);
		menuBar.add(debugMenu);

		// Help menu
		JMenu help = new JMenu(I18nUtils.localizedStringForKey("menu_help"));
		JMenuItem readme = new JMenuItem(I18nUtils.localizedStringForKey("menu_help_readme"));
		JMenuItem howToMap = new JMenuItem(I18nUtils.localizedStringForKey("menu_help_how_to_preview"));
		JMenuItem licenses = new JMenuItem(I18nUtils.localizedStringForKey("menu_help_licenses"));
		JMenuItem about = new JMenuItem(I18nUtils.localizedStringForKey("menu_help_about"));
		readme.addActionListener(new ShowReadme());
		about.addActionListener(new ShowAboutDialog());
		howToMap.addActionListener(new ShowHelpAction());
		licenses.addActionListener(new HelpLicenses());
		help.add(readme);
		help.add(howToMap);
		help.addSeparator();
		help.add(licenses);
		help.addSeparator();
		help.add(about);

		menuBar.add(help);
	}

	public void loadToolsMenu()
	{
		if (ExternalToolsLoader.load())
		{
			if (toolsMenu == null)
			{
				toolsMenu = new JMenu(I18nUtils.localizedStringForKey("menu_tool"));
				toolsMenu.addMenuListener(new MenuListener()
				{

					public void menuSelected(MenuEvent e)
					{
						loadToolsMenu();
						log.debug("Tools menu Loaded");
					}

					public void menuDeselected(MenuEvent e)
					{
					}

					public void menuCanceled(MenuEvent e)
					{
					}
				});
				menuBar.add(toolsMenu);
			}
			toolsMenu.removeAll();
			for (ExternalToolDef t: ExternalToolsLoader.tools)
			{
				JMenuItem m = new JMenuItem(t.name);
				m.addActionListener(t);
				toolsMenu.add(m);
			}
		}
	}

	/**
	 * 20140228 AH the sizing of the left side panel is done here
	 */
	private void updateLeftPanel()
	{
		leftPanel.removeAll();

		coordinatesPanel.addButtonActionListener(new ApplySelectionButtonListener());

		JCollapsiblePanel mapSourcePanel = new JCollapsiblePanel(I18nUtils.localizedStringForKey("lp_map_source_title"), new GridBagLayout());
		mapSourcePanel.addContent(mapSourceCombo, GBC.std().insets(2, 2, 2, 2).fill());

		JCollapsiblePanel zoomLevelsPanel = new JCollapsiblePanel(I18nUtils.localizedStringForKey("lp_zoom_title"), new GridBagLayout());
		zoomLevelsPanel.addContent(zoomLevelPanel, GBC.eol().insets(2, 4, 2, 0));
		zoomLevelsPanel.addContent(amountOfTilesLabel, GBC.std().anchor(GBC.WEST).insets(0, 5, 0, 2));

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);

		JCollapsiblePanel atlasContentPanel = new JCollapsiblePanel(I18nUtils.localizedStringForKey("lp_atlas_title"), new GridBagLayout());
		JScrollPane treeScrollPane = new JScrollPane(jAtlasTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jAtlasTree.getTreeModel().addTreeModelListener(new AtlasModelListener(jAtlasTree, profilesPanel));

		treeScrollPane.setMinimumSize(new Dimension(100, 150));
		treeScrollPane.setPreferredSize(new Dimension(100, 200));
		treeScrollPane.setAutoscrolls(true);
		atlasContentPanel.addContent(treeScrollPane, GBC.eol().fill().insets(0, 1, 0, 0));
		JButton clearAtlas = new JButton(I18nUtils.localizedStringForKey("lp_atlas_new_btn_title"));
		atlasContentPanel.addContent(clearAtlas, GBC.std());
		clearAtlas.addActionListener(new AtlasNew());
		JButton addLayers = new JButton(I18nUtils.localizedStringForKey("lp_atlas_add_selection_btn_title"));
		atlasContentPanel.addContent(addLayers, GBC.eol());
		addLayers.addActionListener(AddMapLayer.INSTANCE);
		atlasContentPanel.addContent(new JLabel(I18nUtils.localizedStringForKey("lp_atlas_name_label_title")), gbc_std);
		atlasContentPanel.addContent(atlasNameTextField, gbc_eol.fill(GBC.HORIZONTAL));

		gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill(GBC.HORIZONTAL);

		leftPanelContent = new JPanel(new GridBagLayout());
		leftPanelContent.add(coordinatesPanel, gbc_eol);
		leftPanelContent.add(mapSourcePanel, gbc_eol);
		leftPanelContent.add(zoomLevelsPanel, gbc_eol);
		leftPanelContent.add(tileImageParametersPanel, gbc_eol);
		leftPanelContent.add(atlasContentPanel, gbc_eol);

		leftPanelContent.add(profilesPanel, gbc_eol);
		// leftPanelContent.add(createAtlasButton, gbc_eol);
		leftPanelContent.add(settingsButton, gbc_eol);
		leftPanelContent.add(tileStoreCoveragePanel, gbc_eol);
		leftPanelContent.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

		JScrollPane scrollPane = new JScrollPane(leftPanelContent);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		// Set the scroll pane width large enough so that the
		// scroll bar has enough space to appear right to it
		Dimension d = scrollPane.getPreferredSize();
		d.width += 5 + scrollPane.getVerticalScrollBar().getWidth();
		// scrollPane.setPreferredSize(d);
		scrollPane.setMinimumSize(d);
		leftPanel.add(scrollPane, GBC.std().fill());
		// leftPanel.add(leftPanelContent, GBC.std().fill());

	}

	private void updateRightPanel()
	{
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill();
		gpxPanel = new JGpxPanel(previewMap);
		rightPanel.add(gpxPanel, gbc_eol);
	}

	private JPanel updateMapControlsPanel()
	{
		mapControlPanel.removeAll();
		mapControlPanel.setOpaque(false);

		// zoom label
		JLabel zoomLabel = new JLabel(I18nUtils.localizedStringForKey("map_ctrl_zoom_level_title"));
		zoomLabel.setOpaque(true);
		zoomLabel.setBackground(labelBackgroundColor);
		zoomLabel.setForeground(labelForegroundColor);

		// top panel
		JPanel topControls = new JPanel(new GridBagLayout());
		topControls.setOpaque(false);
		topControls.add(zoomLabel, GBC.std().insets(5, 5, 0, 0));
		topControls.add(zoomSlider, GBC.std().insets(0, 5, 0, 0));
		topControls.add(zoomLevelText, GBC.std().insets(0, 5, 0, 0));
		topControls.add(gridZoomCombo, GBC.std().insets(10, 5, 0, 0));
		topControls.add(wgsGridCheckBox, GBC.std().insets(10, 5, 0, 0));
		topControls.add(wgsGridCombo, GBC.std().insets(5, 5, 0, 0));
		topControls.add(Box.createHorizontalGlue(), GBC.std().fillH());
		mapControlPanel.add(topControls, BorderLayout.NORTH);

		// bottom panel
		// JPanel bottomControls = new JPanel(new GridBagLayout());
		// bottomControls.setOpaque(false);
		// bottomControls.add(Box.createHorizontalGlue(),
		// GBC.std().fill(GBC.HORIZONTAL));
		// mapControlPanel.add(bottomControls, BorderLayout.SOUTH);

		return mapControlPanel;
	}

	public void updateMapSourcesList()
	{
		MapSource ms = (MapSource) mapSourceCombo.getSelectedItem();
		mapSourceCombo.setModel(new DefaultComboBoxModel(MapSourcesManager.getInstance().getEnabledOrderedMapSources()));
		mapSourceCombo.setSelectedItem(ms);
		MapSource ms2 = (MapSource) mapSourceCombo.getSelectedItem();
		if (!ms.equals(ms2))
			previewMap.setMapSource(ms2);
	}

	public void updateBookmarksMenu()
	{
		LinkedList<JMenuItem> items = new LinkedList<JMenuItem>();
		for (int i = 0; i < bookmarkMenu.getMenuComponentCount(); i++)
		{
			JMenuItem item = bookmarkMenu.getItem(i);
			if (!(item instanceof JBookmarkMenuItem))
				items.add(item);
		}
		bookmarkMenu.removeAll();
		for (JMenuItem item: items)
		{
			if (item != null)
				bookmarkMenu.add(item);
			else
				bookmarkMenu.addSeparator();
		}
		for (Bookmark b: Settings.getInstance().placeBookmarks)
		{
			bookmarkMenu.add(new JBookmarkMenuItem(b));
		}
	}

	private void loadSettings()
	{
		if (Profile.DEFAULT.exists())
		{
			try
			{
				jAtlasTree.load(Profile.DEFAULT);
			}
			catch (Exception e)
			{
				log.error("Failed to load atlas", e);
				GUIExceptionHandler.processException(e);
				new AtlasNew().actionPerformed(null);
			}
		}
		else
			new AtlasNew().actionPerformed(null);

		Settings settings = Settings.getInstance();
		atlasNameTextField.setText(settings.elementName);
		previewMap.settingsLoad();
		int nextZoom = 0;
		List<Integer> zoomList = settings.selectedZoomLevels;
		if (zoomList != null)
		{
			for (JZoomCheckBox currentZoomCb: cbZoom)
			{
				for (int i = nextZoom; i < zoomList.size(); i++)
				{
					int currentListZoom = zoomList.get(i);
					if (currentZoomCb.getZoomLevel() == currentListZoom)
					{
						currentZoomCb.setSelected(true);
						nextZoom = 1;
						break;
					}
				}
			}
		}
		coordinatesPanel.setNumberFormat(settings.coordinateNumberFormat);

		tileImageParametersPanel.loadSettings();
		tileImageParametersPanel.atlasFormatChanged(jAtlasTree.getAtlas().getOutputFormat());
		// mapSourceCombo
		// .setSelectedItem(MapSourcesManager.getSourceByName(settings.
		// mapviewMapSource));

		setSize(settings.mainWindow.size);
		Point windowLocation = settings.mainWindow.position;
		if (windowLocation.x == -1 && windowLocation.y == -1)
		{
			setLocationRelativeTo(null);
		}
		else
		{
			setLocation(windowLocation);
		}
		if (settings.mainWindow.maximized)
			setExtendedState(Frame.MAXIMIZED_BOTH);

		leftPanel.setVisible(settings.mainWindow.leftPanelVisible);
		rightPanel.setVisible(settings.mainWindow.rightPanelVisible);

		if (leftPanelContent != null)
		{
			for (Component c: leftPanelContent.getComponents())
			{
				if (c instanceof JCollapsiblePanel)
				{
					JCollapsiblePanel cp = (JCollapsiblePanel) c;
					String name = cp.getName();
					if (name != null && settings.mainWindow.collapsedPanels.contains(name))
						cp.setCollapsed(true);
				}
			}
		}

		updateBookmarksMenu();
	}

	private void saveSettings()
	{
		try
		{
			jAtlasTree.save(Profile.DEFAULT);

			Settings s = Settings.getInstance();
			previewMap.settingsSave();
			s.mapviewMapSource = previewMap.getMapSource().getName();
			s.selectedZoomLevels = new SelectedZoomLevels(cbZoom).getZoomLevelList();

			s.elementName = atlasNameTextField.getText();
			s.coordinateNumberFormat = coordinatesPanel.getNumberFormat();

			tileImageParametersPanel.saveSettings();
			boolean maximized = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
			s.mainWindow.maximized = maximized;
			if (!maximized)
			{
				s.mainWindow.size = getSize();
				s.mainWindow.position = getLocation();
			}
			s.mainWindow.collapsedPanels.clear();
			if (leftPanelContent != null)
			{
				for (Component c: leftPanelContent.getComponents())
				{
					if (c instanceof JCollapsiblePanel)
					{
						JCollapsiblePanel cp = (JCollapsiblePanel) c;
						if (cp.isCollapsed())
							s.mainWindow.collapsedPanels.add(cp.getName());
					}
				}
			}
			s.mainWindow.leftPanelVisible = leftPanel.isVisible();
			s.mainWindow.rightPanelVisible = rightPanel.isVisible();
			checkAndSaveSettings();
		}
		catch (Exception e)
		{
			GUIExceptionHandler.showExceptionDialog(e);
			JOptionPane.showMessageDialog(null, I18nUtils.localizedStringForKey("msg_settings_write_error"), I18nUtils.localizedStringForKey("Error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void checkAndSaveSettings() throws JAXBException
	{
		if (Settings.checkSettingsFileModified())
		{
			int x = JOptionPane.showConfirmDialog(this, I18nUtils.localizedStringForKey("msg_setting_file_is_changed_by_other"),
					I18nUtils.localizedStringForKey("msg_setting_file_is_changed_by_other_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (x != JOptionPane.YES_OPTION)
				return;
		}
		Settings.save();

	}

	public JTileImageParametersPanel getParametersPanel()
	{
		return tileImageParametersPanel;
	}

	public String getUserText()
	{
		return atlasNameTextField.getText();
	}

	public void refreshPreviewMap()
	{
		previewMap.refreshMap();
	}

	private class ZoomSliderListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			previewMap.setZoom(zoomSlider.getValue());
		}
	}

	private class GridZoomComboListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!gridZoomCombo.isEnabled())
				return;
			GridZoom g = (GridZoom) gridZoomCombo.getSelectedItem();
			if (g == null)
				return;
			log.debug("Selected grid zoom combo box item has changed: " + g.getZoom());
			previewMap.setGridZoom(g.getZoom());
			repaint();
			previewMap.updateMapSelection();
		}
	}

	private void updateGridSizeCombo()
	{
		int maxZoom = previewMap.getMapSource().getMaxZoom();
		int minZoom = previewMap.getMapSource().getMinZoom();
		GridZoom lastGridZoom = (GridZoom) gridZoomCombo.getSelectedItem();
		gridZoomCombo.setEnabled(false);
		gridZoomCombo.removeAllItems();
		gridZoomCombo.setMaximumRowCount(maxZoom - minZoom + 2);
		gridZoomCombo.addItem(new GridZoom(-1)
		{

			@Override
			public String toString()
			{
				return I18nUtils.localizedStringForKey("map_ctrl_zoom_grid_disable");
			}

		});
		for (int i = maxZoom; i >= minZoom; i--)
		{
			gridZoomCombo.addItem(new GridZoom(i));
		}
		if (lastGridZoom != null)
			gridZoomCombo.setSelectedItem(lastGridZoom);
		gridZoomCombo.setEnabled(true);
	}

	private class ApplySelectionButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			setSelectionByEnteredCoordinates();
		}
	}

	private class MapSourceComboListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			MapSource mapSource = (MapSource) mapSourceCombo.getSelectedItem();
			if (mapSource == null)
			{
				mapSourceCombo.setSelectedIndex(0);
				mapSource = (MapSource) mapSourceCombo.getSelectedItem();
			}
			if (mapSource instanceof InitializableMapSource)
				// initialize the map source e.g. detect available zoom levels
				((InitializableMapSource) mapSource).initialize();
			previewMap.setMapSource(mapSource);
			zoomSlider.setMinimum(previewMap.getMapSource().getMinZoom());
			zoomSlider.setMaximum(previewMap.getMapSource().getMaxZoom());
			updateGridSizeCombo();
			updateZoomLevelCheckBoxes();
			calculateNrOfTilesToDownload();
		}
	}

	private class LoadProfileListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Profile profile = profilesPanel.getSelectedProfile();
			profilesPanel.getDeleteButton().setEnabled(profile != null);
			if (profile == null)
				return;

			jAtlasTree.load(profile);
			previewMap.repaint();
			tileImageParametersPanel.atlasFormatChanged(jAtlasTree.getAtlas().getOutputFormat());
		}
	}

	private class SettingsButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			SettingsGUI.showSettingsDialog(MainGUI.this);
		}
	}

	private void updateZoomLevelCheckBoxes()
	{
		MapSource tileSource = previewMap.getMapSource();
		int zoomLevels = tileSource.getMaxZoom() - tileSource.getMinZoom() + 1;
		zoomLevels = Math.max(zoomLevels, 0);
		JCheckBox oldZoomLevelCheckBoxes[] = cbZoom;
		int oldMinZoom = 0;
		if (cbZoom.length > 0)
			oldMinZoom = cbZoom[0].getZoomLevel();
		cbZoom = new JZoomCheckBox[zoomLevels];
		zoomLevelPanel.removeAll();

		zoomLevelPanel.setLayout(new GridLayout(0, 10, 1, 2));
		ZoomLevelCheckBoxListener cbl = new ZoomLevelCheckBoxListener();

		for (int i = cbZoom.length - 1; i >= 0; i--)
		{
			int cbz = i + tileSource.getMinZoom();
			JZoomCheckBox cb = new JZoomCheckBox(cbz);
			cb.setPreferredSize(new Dimension(22, 11));
			cb.setMinimumSize(cb.getPreferredSize());
			cb.setOpaque(false);
			cb.setFocusable(false);
			cb.setName(Integer.toString(cbz));
			int oldCbIndex = cbz - oldMinZoom;
			if (oldCbIndex >= 0 && oldCbIndex < (oldZoomLevelCheckBoxes.length))
				cb.setSelected(oldZoomLevelCheckBoxes[oldCbIndex].isSelected());
			cb.addActionListener(cbl);
			// cb.setToolTipText("Select zoom level " + cbz + " for atlas");
			zoomLevelPanel.add(cb);
			cbZoom[i] = cb;

			JLabel l = new JLabel(Integer.toString(cbz));
			zoomLevelPanel.add(l);
		}
		amountOfTilesLabel.setOpaque(false);
		amountOfTilesLabel.setForeground(Color.black);
	}

	private class ZoomLevelCheckBoxListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			calculateNrOfTilesToDownload();
		}
	}

	public void selectionChanged(MercatorPixelCoordinate max, MercatorPixelCoordinate min)
	{
		mapSelectionMax = max;
		mapSelectionMin = min;
		coordinatesPanel.setSelection(max, min);
		calculateNrOfTilesToDownload();
	}

	public void zoomChanged(int zoomLevel)
	{
		zoomLevelText.setText(" " + zoomLevel + " ");
		zoomSlider.setValue(zoomLevel);
	}

	public void gridZoomChanged(int newGridZoomLevel)
	{
		gridZoomCombo.setSelectedItem(new GridZoom(newGridZoomLevel));
	}

	public MapSource getSelectedMapSource()
	{
		return (MapSource) mapSourceCombo.getSelectedItem();
	}

	public SelectedZoomLevels getSelectedZoomLevels()
	{
		return new SelectedZoomLevels(cbZoom);
	}

	public void selectNextMapSource()
	{
		if (mapSourceCombo.getSelectedIndex() == mapSourceCombo.getItemCount() - 1)
		{
			Toolkit.getDefaultToolkit().beep();
		}
		else
		{
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() + 1);
		}
	}

	public void selectPreviousMapSource()
	{
		if (mapSourceCombo.getSelectedIndex() == 0)
		{
			Toolkit.getDefaultToolkit().beep();
		}
		else
		{
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() - 1);
		}
	}

	public void mapSourceChanged(MapSource newMapSource)
	{
		// TODO update selected area if new map source has different projectionCategory
		calculateNrOfTilesToDownload();
		// if (newMapSource != null && newMapSource.equals(mapSourceCombo.getSelectedItem()))
		// return;
		mapSourceCombo.setSelectedItem(newMapSource);
	}

	public void mapSelectionControllerChanged(JMapController newMapController)
	{
		smPolygon.setSelected(false);
		smCircle.setSelected(false);
		smRectangle.setSelected(false);
		if (newMapController instanceof PolygonSelectionMapController)
			smPolygon.setSelected(true);
		else if (newMapController instanceof PolygonCircleSelectionMapController)
			smCircle.setSelected(true);
		else if (newMapController instanceof RectangleSelectionMapController)
			smRectangle.setSelected(true);
	}

	private void setSelectionByEnteredCoordinates()
	{
		coordinatesPanel.correctMinMax();
		MapSelection ms = coordinatesPanel.getMapSelection(previewMap.getMapSource());
		mapSelectionMax = ms.getBottomRightPixelCoordinate();
		mapSelectionMin = ms.getTopLeftPixelCoordinate();
		previewMap.setSelectionAndZoomTo(ms, false);
	}

	public MapSelection getMapSelectionCoordinates()
	{
		if (mapSelectionMax == null || mapSelectionMin == null)
			return null;
		return new MapSelection(previewMap.getMapSource(), mapSelectionMax, mapSelectionMin);
	}

	public TileImageParameters getSelectedTileImageParameters()
	{
		return tileImageParametersPanel.getSelectedTileImageParameters();
	}

	private void calculateNrOfTilesToDownload()
	{
		MapSelection ms = getMapSelectionCoordinates();
		String baseText;
		baseText = I18nUtils.localizedStringForKey("lp_zoom_total_tile_title");
		if (ms == null || !ms.isAreaSelected())
		{
			amountOfTilesLabel.setText(String.format(baseText, "0"));
			amountOfTilesLabel.setToolTipText("");
		}
		else
		{
			try
			{
				SelectedZoomLevels sZL = new SelectedZoomLevels(cbZoom);

				int[] zoomLevels = sZL.getZoomLevels();

				long totalNrOfTiles = 0;

				StringBuilder hint = new StringBuilder(1024);
				hint.append(I18nUtils.localizedStringForKey("lp_zoom_total_tile_hint_head"));
				for (int i = 0; i < zoomLevels.length; i++)
				{
					int zoom = zoomLevels[i];
					long[] info = ms.calculateNrOfTilesEx(zoom);
					totalNrOfTiles += info[0];
					hint.append(String.format(I18nUtils.localizedStringForKey("lp_zoom_total_tile_hint_row"), zoomLevels[i], info[0], info[1], info[2]));
					// hint.append("<br>Level " + zoomLevels[i] + ": " + info[0] + " (" + info[1] + "*" + info[2] +
					// ")");
				}
				String hintText = "<html>" + hint.toString() + "</html>";
				amountOfTilesLabel.setText(String.format(baseText, Long.toString(totalNrOfTiles)));
				amountOfTilesLabel.setToolTipText(hintText);
			}
			catch (Exception e)
			{
				amountOfTilesLabel.setText(String.format(baseText, "?"));
				log.error("", e);
			}
		}
	}

	public BundleInterface getAtlas()
	{
		return jAtlasTree.getAtlas();
	}

	private class WindowDestroyer extends WindowAdapter
	{

		@Override
		public void windowOpened(WindowEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					previewMap.setEnabled(true);
				}
			});
		}

		public void windowClosing(WindowEvent event)
		{
			saveSettings();
		}
	}

	/**
	 * Saves the window position and size when window is moved or resized. This is necessary because of the maximized state. If a window is maximized it is
	 * impossible to retrieve the window size & position of the non-maximized window - therefore we have to collect the information every time they change.
	 */
	private class MainWindowListener extends ComponentAdapter
	{
		public void componentResized(ComponentEvent event)
		{
			// log.debug(event.paramString());
			updateValues();
		}

		public void componentMoved(ComponentEvent event)
		{
			// log.debug(event.paramString());
			updateValues();
		}

		private void updateValues()
		{
			// only update old values while window is in NORMAL state
			// Note(Java bug): Sometimes getExtendedState() says the window is
			// not maximized but maximizing is already in progress and therefore
			// the window bounds are already changed.
			if ((getExtendedState() & MAXIMIZED_BOTH) != 0)
				return;
			Settings s = Settings.getInstance();
			s.mainWindow.size = getSize();
			s.mainWindow.position = getLocation();
		}
	}

}
