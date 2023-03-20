// ReadSynoptic_JDialog - editor for the ReadSynoptic() command.

/* NoticeStart

OWF TSTool Synoptic Plugin
Copyright (C) 2022-2023 Open Water Foundation

OWF TSTool Synoptic Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OWF TSTool Synoptic Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OWF TSTool Synoptic Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.synoptic.commands;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openwaterfoundation.tstool.plugin.synoptic.datastore.SynopticDataStore;
import org.openwaterfoundation.tstool.plugin.synoptic.ui.Synoptic_TimeSeries_InputFilter_JPanel;

import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessor;
import RTi.TS.TSFormatSpecifiersJPanel;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.TimeInterval;

/**
Editor for the ReadSynoptic() command.
*/
@SuppressWarnings("serial")
public class ReadSynoptic_JDialog extends JDialog
implements ActionListener, DocumentListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton dataStoreDocumentation_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private ReadSynoptic_Command __command = null;
private SimpleJComboBox __DataStore_JComboBox = null;
private SimpleJComboBox __DataType_JComboBox;
private SimpleJComboBox __Interval_JComboBox;
private TSFormatSpecifiersJPanel __Alias_JTextField = null;
private JTabbedPane __tsInfo_JTabbedPane = null;
private JPanel __multipleTS_JPanel = null;
private JTextField __StationId_JTextField = null;
//private SimpleJComboBox __StationId_JComboBox = null;
private JTextField __DataSource_JTextField;
private SimpleJComboBox __IrregularInterval_JComboBox = null;
//private SimpleJComboBox __Read24HourAsDay_JComboBox = null;
//private SimpleJComboBox __ReadDayAs24Hour_JComboBox = null;
private JTextField __TSID_JTextField;
private JTextField __InputStart_JTextField;
private JTextField __InputEnd_JTextField;
//private JTextField __Timezone_JTextField;
private SimpleJComboBox	__Debug_JComboBox;

private JTextArea __command_JTextArea = null;
// Contains all input filter panels.  Use the SynopticDataStore name/description and data type for each to
// figure out which panel is active at any time.
// Using the general panel and casting later causes a ClassCastException since classes are loaded in different ClassLoader.
// private List<InputFilter_JPanel> __inputFilterJPanelList = new ArrayList<>();
private List<Synoptic_TimeSeries_InputFilter_JPanel> __inputFilterJPanelList = new ArrayList<>();
private SynopticDataStore __dataStore = null; // Selected SynopticDataStore.
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Was OK pressed when closing the dialog?
private boolean __ignoreEvents = false; // Used to ignore cascading events when initializing the components.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadSynoptic_JDialog ( JFrame parent, ReadSynoptic_Command command ) {
	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event ) {
	if ( __ignoreEvents ) {
        return; // Startup.
    }
    Object o = event.getSource();

    if ( o == __cancel_JButton ) {
        response ( false );
    }
    else if ( o == dataStoreDocumentation_JButton ) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse ( new URI(dataStoreDocumentation_JButton.getActionCommand()) );
        }
        catch ( Exception e ) {
            Message.printWarning(1, null, "Unable to display Synoptic web services documentation using \"" +
                dataStoreDocumentation_JButton.getActionCommand() + "\"" );
        }
    }
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "ReadSynoptic",
			"https://software.openwaterfoundation.org/tstool-kiwis-plugin/latest/doc-user");
	}
    else if ( o == __ok_JButton ) {
        refresh ();
        checkInput ();
        if ( !__error_wait ) {
            response ( true );
        }
    }
    else {
        // ComboBoxes.
        refresh();
    }
}

/**
Refresh the data type choices in response to the currently selected Synoptic datastore.
@param value if non-null, then the selection is from the command initialization, in which case the
specified data type should be selected
*/
private void actionPerformedDataStoreSelected ( ) {
    if ( __DataStore_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    setDataStoreForSelectedInput();
    //Message.printStatus(2, "", "Selected data store " + __dataStore + " __dmi=" + __dmi );
    // Now populate the data type choices corresponding to the data store
    populateDataTypeChoices ( getSelectedDataStore() );
}

/**
Refresh the query choices for the currently selected Synoptic datastore.
@param value if non-null, then the selection is from the command initialization, in which case the
specified data type should be selected
*/
private void actionPerformedDataTypeSelected ( ) {
    if ( __DataType_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    // Populate the interval choices corresponding to the data type.
    populateIntervalChoices ( getSelectedDataStore() );
    populateStationIdChoices ( getSelectedDataStore() );
}

/**
Set visible the appropriate input filter, based on the interval and other previous selections.
*/
private void actionPerformedIntervalSelected ( ) {
    if ( __Interval_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    // Populate the filters corresponding to the data type and interval.
    selectInputFilter ( getDataStore() );
}

/**
Refresh the query choices for the currently selected Synoptic location identifier.
@param value if non-null, then the selection is from the command initialization,
in which case the specified data type should be selected
*/
private void actionPerformedStationIdSelected ( ) {
	/*
    if ( __StationId_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    */
}


// Start event handlers for DocumentListener...

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void changedUpdate ( DocumentEvent e ) {
	checkGUIState();
    refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void insertUpdate ( DocumentEvent e ) {
	checkGUIState();
    refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void removeUpdate ( DocumentEvent e ) {
	checkGUIState();
    refresh();
}

// ...end event handlers for DocumentListener.

/**
Check the state of the dialog, disabling/enabling components as appropriate.
*/
private void checkGUIState() {
	// If "AllMatchingTSID", enable the list.
	// Otherwise, clear and disable.
	if ( __DataType_JComboBox != null ) {
		String DataType = getSelectedDataType();
		if ( DataType == null ) {
		    // Initialization.
		    DataType = "*";
		}
	}

    // If datastore is selected and has the property for API documentation, enable the documentation buttons.
    SynopticDataStore dataStore = getSelectedDataStore();
    if ( dataStore != null ) {
        String urlString = dataStore.getProperty ( "ServiceAPIDocumentationURI" );
        if ( urlString == null ) {
            this.dataStoreDocumentation_JButton.setEnabled(false);
        }
        else {
            this.dataStoreDocumentation_JButton.setActionCommand(urlString);
            this.dataStoreDocumentation_JButton.setEnabled(true);
        }
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput () {
	if ( __ignoreEvents ) {
        return; // Startup.
    }
    // Put together a list of parameters to check.
	PropList props = new PropList ( "" );
	__error_wait = false;
	// Check parameters for the two command versions.
    String DataStore = __DataStore_JComboBox.getSelected();
    if ( DataStore.length() > 0 ) {
        props.set ( "DataStore", DataStore );
    }
	String Alias = __Alias_JTextField.getText().trim();
	if ( Alias.length() > 0 ) {
		props.set ( "Alias", Alias );
	}
	//String TSID = __TSID_JTextField.getText().trim();
	//if ( TSID.length() > 0 ) {
	//	props.set ( "TSID", TSID );
	//}
    String DataType = getSelectedDataType();
	if ( DataType.length() > 0 ) {
		props.set ( "DataType", DataType );
	}
	String Interval = getSelectedInterval();
	if ( Interval.length() > 0 ) {
		props.set ( "Interval", Interval );
	}
	String StationId = getSelectedStationId();
	if ( StationId.length() > 0 ) {
		props.set ( "StationId", StationId );
	}
	InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	int whereCount = 0; // Number of non-empty Where parameters specified.
	if ( filterPanel != null ) {
    	for ( int i = 1; i <= filterPanel.getNumFilterGroups(); i++ ) {
    	    String where = getWhere ( i - 1 );
    	    // Blank where is something like: ";operator;"
    	    if ( !where.isEmpty() && !where.startsWith(";") && !where.endsWith(";") ) {
    	    	++whereCount;
    	    }
    	    if ( where.length() > 0 ) {
    	        props.set ( "Where" + i, where );
    	    }
        }
	}
	// Both command types use these.
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	if ( InputStart.length() > 0 ) {
		props.set ( "InputStart", InputStart );
	}
	if ( InputEnd.length() > 0 ) {
		props.set ( "InputEnd", InputEnd );
	}
    String IrregularInterval = __IrregularInterval_JComboBox.getSelected();
    if ( IrregularInterval.length() > 0 ) {
        props.set ( "IrregularInterval", IrregularInterval );
    }
    /*
    String Read24HourAsDay = __Read24HourAsDay_JComboBox.getSelected();
    if ( Read24HourAsDay.length() > 0 ) {
        props.set ( "Read24HourAsDay", Read24HourAsDay );
    }
    String ReadDayAs24Hour = __ReadDayAs24Hour_JComboBox.getSelected();
    if ( ReadDayAs24Hour.length() > 0 ) {
        props.set ( "ReadDayAs24Hour", ReadDayAs24Hour );
    }
    */
    if ( whereCount > 0 ) {
        // Input filters are specified so check:
    	// - this is done in the input filter because that code is called from this command and main TSTool UI
        InputFilter_JPanel ifp = getVisibleInputFilterPanel();
        if ( ifp != null ) {
        	// Set a property to pass to the general checkCommandParameters method so that the
        	// results can be combined with the other command parameter checks.
        	props.set("InputFiltersCheck",ifp.checkInputFilters(false));
        }
    }
    /*
	String Timezone = __Timezone_JTextField.getText().trim();
	if ( Timezone.length() > 0 ) {
		props.set ( "Timezone", Timezone );
	}
	*/
	String Debug = __Debug_JComboBox.getSelected();
	if ( Debug.length() > 0 ) {
		props.set ( "Debug", Debug );
	}
	try {
	    // This will warn the user.
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits () {
	String DataStore = __DataStore_JComboBox.getSelected();
    __command.setCommandParameter ( "DataStore", DataStore );
	String Alias = __Alias_JTextField.getText().trim();
	__command.setCommandParameter ( "Alias", Alias );
	//String TSID = __TSID_JTextField.getText().trim();
	//__command.setCommandParameter ( "TSID", TSID );
	String DataType = getSelectedDataType();
	__command.setCommandParameter ( "DataType", DataType );
	String Interval = getSelectedInterval();
	__command.setCommandParameter ( "Interval", Interval );
	// Match 1 time series.
	String StationId = __StationId_JTextField.getText().trim();
	//String StationId = __StationId_JComboBox.getSelected();
	__command.setCommandParameter ( "StationId", StationId );
	// 1+ time series.
	String delim = ";";
	//InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	Synoptic_TimeSeries_InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	for ( int i = 1; i <= filterPanel.getNumFilterGroups(); i++ ) {
	    String where = getWhere ( i - 1 );
	    if ( where.startsWith(delim) ) {
	        where = "";
	    }
	    __command.setCommandParameter ( "Where" + i, where );
	}
	// Both versions of the commands use these.
	String InputStart = __InputStart_JTextField.getText().trim();
	__command.setCommandParameter ( "InputStart", InputStart );
	String InputEnd = __InputEnd_JTextField.getText().trim();
	__command.setCommandParameter ( "InputEnd", InputEnd );
	String IrregularInterval = __IrregularInterval_JComboBox.getSelected();
	__command.setCommandParameter (	"IrregularInterval", IrregularInterval );
	/*
	String Read24HourAsDay = __Read24HourAsDay_JComboBox.getSelected();
	__command.setCommandParameter (	"Read24HourAsDay", Read24HourAsDay );
	String ReadDayAs24Hour = __ReadDayAs24Hour_JComboBox.getSelected();
	__command.setCommandParameter (	"ReadDayAs24Hour", ReadDayAs24Hour );
	*/
	//String Timezone = __Timezone_JTextField.getText().trim();
	//__command.setCommandParameter ( "Timezone", Timezone );
	String Debug = __Debug_JComboBox.getSelected();
	__command.setCommandParameter (	"Debug", Debug );
}

/**
Return the datastore that is in effect.
*/
private SynopticDataStore getDataStore() {
    return __dataStore;
}

/**
Get the input filter list.
*/
//private List<InputFilter_JPanel> getInputFilterJPanelList ()
private List<Synoptic_TimeSeries_InputFilter_JPanel> getInputFilterJPanelList () {
    return __inputFilterJPanelList;
}

/**
Get the input name to use for the TSID.
*/
private String getInputNameForTSID() {
    // Use the data store name if specified.
    String DataStore = __DataStore_JComboBox.getSelected();
    if ( (DataStore != null) && !DataStore.equals("") ) {
        return DataStore;
    }
    else {
        return "Synoptic"; // Default.
    }
}

/**
Get the selected data store from the processor.
*/
private SynopticDataStore getSelectedDataStore () {
	String routine = getClass().getSimpleName() + ".getSelectedDataStore";
    String DataStore = __DataStore_JComboBox.getSelected();
    SynopticDataStore dataStore = (SynopticDataStore)((TSCommandProcessor)
        __command.getCommandProcessor()).getDataStoreForName( DataStore, SynopticDataStore.class );
    if ( dataStore != null ) {
        //Message.printStatus(2, routine, "Selected datastore is \"" + dataStore.getName() + "\"." );
    }
    else {
        Message.printStatus(2, routine, "Cannot get datastore for \"" + DataStore + "\"." );
    }
    return dataStore;
}

/**
Return the selected data type, omitting the trailing SHEF code from "dataType - SHEF PE", should it be present.
However, include the statistic, as in "WaterLevelRiver-Max".
*/
private String getSelectedDataType() {
    if ( __DataType_JComboBox == null ) {
        return null;
    }
    String dataType = __DataType_JComboBox.getSelected();
    if ( dataType == null ) {
    	return dataType;
    }
    // Make sure to use spaces around the dashes because dash without space is used to indicate statistic,
    // and want that included in the data type.
  	int pos = dataType.indexOf(" - ");
    if ( pos > 0 ) {
    	// Return the first item.
        dataType = dataType.substring(0,pos).trim();
    }
    else {
    	// Return the full string.
        dataType = dataType.trim();
    }
    return dataType;
}

/**
Return the selected data interval, omitting the trailing SHEF code from "Interval - SHEF duration", should it be present.
*/
private String getSelectedInterval() {
    if ( __Interval_JComboBox == null ) {
        return null;
    }
    String interval = __Interval_JComboBox.getSelected();
    if ( interval == null ) {
    	return interval;
    }
   	int pos = interval.indexOf(" - ");
    if ( pos > 0 ) {
    	// Return the first item.
        interval = interval.substring(0,pos).trim();
    }
    else {
    	// Return the full string.
        interval = interval.trim();
    }
    return interval;
}

/**
Return the selected location ID (station_no).
*/
private String getSelectedStationId() {
	/*
    if ( __StationId_JComboBox == null ) {
        return null;
    }
    return __StationId_JComboBox.getSelected();
    */
	String StationId = __StationId_JTextField.getText().trim();
	return StationId;
}

/**
Return the visible input filter panel, or null if none visible.
*/
//private InputFilter_JPanel getVisibleInputFilterPanel() {
private Synoptic_TimeSeries_InputFilter_JPanel getVisibleInputFilterPanel() {
    //List<InputFilter_JPanel> panelList = getInputFilterJPanelList();
    List<Synoptic_TimeSeries_InputFilter_JPanel> panelList = getInputFilterJPanelList();
    String panelName;
    //for ( InputFilter_JPanel panel : panelList ) {
    for ( Synoptic_TimeSeries_InputFilter_JPanel panel : panelList ) {
        // Skip default.
        panelName = panel.getName();
        if ( (panelName != null) && panelName.equalsIgnoreCase("Default") ) {
            continue;
        }
        if ( panel.isVisible() ) {
        	if ( Message.isDebugOn ) {
        		Message.printStatus(2,"","Visible filter panel name is \"" + panelName + "\"");
        	}
            return panel;
        }
    }
    return null;
}

/**
Return the "WhereN" parameter for the requested input filter.
@return the "WhereN" parameter for the requested input filter.
@param ifg the Input filter to process (zero index).
*/
private String getWhere ( int ifg ) {
	String delim = ";";	// To separate input filter parts.
	//InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	Synoptic_TimeSeries_InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
    String where = "";
    if ( filterPanel != null ) {
    	// Use the internal value for the where to ensure integration.
        where = filterPanel.toString(ifg,delim,3).trim();
    }
	return where;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, ReadSynoptic_Command command ) {
	//String routine = getClass().getSimpleName() + ".initialize";
	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	addWindowListener( this );
    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Read 1+ time series from a Synoptic web services datastore, using options from the choices below to select time series."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Refer to the Synoptic web services documentation for information about data types (sensor variables) and intervals (always IrregSecond)." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specifying the period will limit data that are available " +
		"for later commands but can increase performance." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Reading time series for a single location takes precedence over reading multiple time series." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Requests may be constrained by the software to prevent unintended large bulk queries." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add buttons for the documentation:
    // - the checkGUIState() method checks for and sets the URL in the button's action

	this.dataStoreDocumentation_JButton = new SimpleJButton("Synoptic Documentation", this);
	this.dataStoreDocumentation_JButton.setToolTipText("View the Synoptic documentation for the datastore in a web browser.");
    JGUIUtil.addComponent(main_JPanel, this.dataStoreDocumentation_JButton,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

   	__ignoreEvents = true; // So that a full pass of initialization can occur.

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Synoptic datastore:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStore_JComboBox = new SimpleJComboBox ( false );
    TSCommandProcessor tsProcessor = (TSCommandProcessor)processor;
    Message.printStatus(2, "ReadSynoptic", "Getting datastores for SynopticDataStore class");
    List<DataStore> dataStoreList = tsProcessor.getDataStoresByType( SynopticDataStore.class );
    // Datastore is required, so no blank
    List<String> datastoreChoices = new ArrayList<>();
    for ( DataStore dataStore: dataStoreList ) {
    	datastoreChoices.add ( dataStore.getName() );
    }
    __DataStore_JComboBox.setData(datastoreChoices);
    if ( datastoreChoices.size() > 0 ) {
    	__DataStore_JComboBox.select ( 0 );
    }
    __DataStore_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __DataStore_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Required - Synoptic datastore."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    //JGUIUtil.addComponent(main_JPanel, inputFilterJPanel,
    //    0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Data type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DataType_JComboBox = new SimpleJComboBox ( false );
	__DataType_JComboBox.setToolTipText("Data types from Synoptic 'stationparameter_no', used in TSID data type.");
	__DataType_JComboBox.setMaximumRowCount(20);
	__DataType_JComboBox.addItemListener ( this );
        JGUIUtil.addComponent(main_JPanel, __DataType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required to match a single location - data type for time series."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Data interval:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Interval_JComboBox = new SimpleJComboBox ();
	__Interval_JComboBox.setToolTipText("Data interval for Synoptic time series, currently always IrregSecond.");
	__Interval_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Interval_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required to match a single location - data interval (time step) for time series."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    __tsInfo_JTabbedPane = new JTabbedPane ();
    __tsInfo_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Indicate how to match time series in Synoptic" ));
    JGUIUtil.addComponent(main_JPanel, __tsInfo_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JPanel singleTS_JPanel = new JPanel();
    singleTS_JPanel.setLayout(new GridBagLayout());
    __tsInfo_JTabbedPane.addTab ( "Match Single Time Series", singleTS_JPanel );

    int ySingle = -1;
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel(
    	"Match a single time series for a location (station_no)."),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel(
    	"A unique TSID is formed from the Synoptic station ID and data type (sensor variable)."),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel(
    	"The data type and interval must be specified above (DO NOT USE * for data type)."),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Station ID:"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __StationId_JTextField = new JTextField ( "", 20 );
    __StationId_JTextField.setToolTipText("Station identifier (stid) to match.");
    __StationId_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(singleTS_JPanel, __StationId_JTextField,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Used in the TSID."),
        3, ySingle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    /*
    __StationId_JComboBox = new SimpleJComboBox ( false );
    __StationId_JComboBox.setToolTipText("Station identifier (stid) to match.");
    __StationId_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(singleTS_JPanel, __StationId_JComboBox,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Required if matching a single location."),
        3, ySingle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    */

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Data source:"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataSource_JTextField = new JTextField ( "", 20 );
    __DataSource_JTextField.setToolTipText("Data source to match, will be determined when time series are read .");
    __DataSource_JTextField.setText("TBD_AT_RUNTIME");
    __DataSource_JTextField.setEditable(false);
    __DataSource_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(singleTS_JPanel, __DataSource_JTextField,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Used in the TSID, will be determined when data are read."),
        3, ySingle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "TSID (full):"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TSID_JTextField = new JTextField ( "" );
    __TSID_JTextField.setToolTipText("The time series identifier that will be used to read the time series.");
    __TSID_JTextField.setEditable ( false );
    __TSID_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(singleTS_JPanel, __TSID_JTextField,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Created from above parameters."),
        3, ySingle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    __multipleTS_JPanel = new JPanel();
    __multipleTS_JPanel.setLayout(new GridBagLayout());
    __tsInfo_JTabbedPane.addTab ( "Match 1+ Time Series", __multipleTS_JPanel );
    // Note to warn about performance.
    int yMult = -1;
    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel("Use filters (\"where\" clauses) to limit result size and " +
        "increase performance.  Filters are AND'ed."),
        0, ++yMult, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel(
    	"The 'metadata' service is called first and then each time series is read using 'timeseries' service."),
        0, ++yMult, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(__multipleTS_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yMult, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    // Initialize all the filters (selection will be based on data store).
    initializeInputFilters ( __multipleTS_JPanel, ++yMult, dataStoreList );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Alias to assign:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Alias_JTextField = new TSFormatSpecifiersJPanel(10);
    __Alias_JTextField.setToolTipText("Use %L for location, %T for data type, %I for interval.");
    __Alias_JTextField.addKeyListener ( this );
    __Alias_JTextField.getDocument().addDocumentListener(this);
    __Alias_JTextField.setToolTipText("%L for location, %T for data type.");
    JGUIUtil.addComponent(main_JPanel, __Alias_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - use %L for location, etc. (default=no alias)."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Input start:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputStart_JTextField = new JTextField (20);
    __InputStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
        1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - overrides the global input start."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input end:"),
        0, ++y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputEnd_JTextField = new JTextField (20);
    __InputEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
        1, y, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - overrides the global input end."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Irregular interval:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> irregInterval_List = new ArrayList<>( 3 );
	irregInterval_List = TimeInterval.getTimeIntervalChoices(TimeInterval.UNKNOWN, TimeInterval.UNKNOWN, false, -1, true);
	irregInterval_List.add(0,"");
	__IrregularInterval_JComboBox = new SimpleJComboBox ( false );
	__IrregularInterval_JComboBox.setToolTipText("Irregular interval to use instead of web service interval");
	__IrregularInterval_JComboBox.setData ( irregInterval_List);
	__IrregularInterval_JComboBox.select ( 0 );
	__IrregularInterval_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IrregularInterval_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - irregular interval for time series (default=IrregSecond)."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Read 24Hour as 1Day:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> read24Hour_List = new ArrayList<>( 3 );
	read24Hour_List.add ( "" );
	read24Hour_List.add ( __command._False );
	read24Hour_List.add ( __command._True );
	__Read24HourAsDay_JComboBox = new SimpleJComboBox ( false );
	__Read24HourAsDay_JComboBox.setToolTipText("Whether to read 24Hour interval time series as 1Day interval");
	__Read24HourAsDay_JComboBox.setData ( read24Hour_List);
	__Read24HourAsDay_JComboBox.select ( 0 );
	__Read24HourAsDay_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Read24HourAsDay_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - read 24Hour as 1Day (default=" + __command._False + ")."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Read day as 24Hour:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> readDay_List = new ArrayList<>( 3 );
	readDay_List.add ( "" );
	readDay_List.add ( __command._False );
	readDay_List.add ( __command._True );
	__ReadDayAs24Hour_JComboBox = new SimpleJComboBox ( false );
	__ReadDayAs24Hour_JComboBox.setToolTipText("Whether to read day interval time series as 24Hour interval");
	__ReadDayAs24Hour_JComboBox.setData ( readDay_List);
	__ReadDayAs24Hour_JComboBox.select ( 0 );
	__ReadDayAs24Hour_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ReadDayAs24Hour_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - read day as 24Hour  (default=" + __command._False + ")."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		*/

    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Timezone:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Timezone_JTextField = new JTextField ( "", 20 );
    __Timezone_JTextField.setToolTipText("Time zone for response, for example America/Denver (default is server time zone).");
    __Timezone_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Timezone_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - to control response time zone."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        */

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Debug:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> Debug_List = new ArrayList<>( 3 );
	Debug_List.add ( "" );
	Debug_List.add ( __command._False );
	Debug_List.add ( __command._True );
	__Debug_JComboBox = new SimpleJComboBox ( false );
	__Debug_JComboBox.setToolTipText("Enable debug for web services, used for troubleshooting).");
	__Debug_JComboBox.setData ( Debug_List);
	__Debug_JComboBox.select ( 0 );
	__Debug_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Debug_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - enable debug for web services (default=" + __command._False + ")."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	// Refresh the contents (still ignoring events).
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel,
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__ok_JButton = new SimpleJButton("OK", this);
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add ( __ok_JButton );
	__cancel_JButton = new SimpleJButton( "Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " Command" );

    // Because it is necessary to select the proper input filter during initialization (to transfer an old command's
    // parameter values), the selected input filter may not be desirable for dialog sizing.  Therefore, manually set
    // all panels to visible and then determine the preferred size as the maximum.  Then reselect the appropriate input
    // filter before continuing.
    setAllFiltersVisible();
    // All filters are visible at this point so pack chooses good sizes.
    pack();
    setPreferredSize(getSize()); // Will reflect all filters being visible
    __multipleTS_JPanel.setPreferredSize(__multipleTS_JPanel.getSize()); // So initial height is maximum height
    selectInputFilter( getDataStore()); // Now go back to the filter for the selected input type and intern
    JGUIUtil.center( this );
    __ignoreEvents = false; // After initialization of components let events happen to dynamically cause cascade.
    // Now refresh once more.
	refresh();
	checkGUIState(); // Do this again because it may not have happened due to the special event handling.
	setResizable ( false );
    super.setVisible( true );
}

/**
Initialize input filters for all of the available Synoptic datastores.
The input filter panels will be layered on top of each other, but only one will be set visible, based on the
other visible selections.
@param parent_JPanel the panel to receive the input filter panels
@param y position in the layout to add the input filter panel
@param dataStoreList the list of available SynopticDataStore
*/
private void initializeInputFilters ( JPanel parent_JPanel, int y, List<DataStore> dataStoreList ) {
	String routine = getClass().getSimpleName() + ".initializeInputFilters";
    // Loop through data stores and add filters for all data groups.
    for ( DataStore ds : dataStoreList ) {
    	Message.printStatus(2,routine,"Initializing data store list for datastore name \"" + ds.getName() +
    		"\" class: " + ds.getClass() );
    	Message.printStatus(2, routine, "Casting to SynopticDataStore class: " + SynopticDataStore.class);
        initializeInputFilters_OneFilter ( parent_JPanel, y, (SynopticDataStore)ds);
    }

    // Blank panel indicating data type was not matched.
    // Add in the same position as the other filter panels.

    int buffer = 3;
    Insets insets = new Insets(1,buffer,1,buffer);
    //List<InputFilter_JPanel> ifPanelList = getInputFilterJPanelList();
    List<Synoptic_TimeSeries_InputFilter_JPanel> ifPanelList = getInputFilterJPanelList();
    //InputFilter_JPanel panel = new InputFilter_JPanel("Data type and interval have no input filters.");
    Synoptic_TimeSeries_InputFilter_JPanel panel =
    	new Synoptic_TimeSeries_InputFilter_JPanel("Data type and interval have no input filters.");
    panel.setName("Default");
    JGUIUtil.addComponent(parent_JPanel, panel,
        0, y, 7, 1, 0.0, 0.0, insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
    ifPanelList.add ( panel );
}

/**
Initialize input filters for one NoavStar web service datastore.
@param parent_JPanel the panel to receive the input filter panels
@param y for layout
@param dataStore datastore to use with the filter
*/
private void initializeInputFilters_OneFilter ( JPanel parent_JPanel, int y, SynopticDataStore dataStore ) {
	String routine = getClass().getSimpleName() + ".initializeInputFilters_OneFilter";
    int buffer = 3;
    Insets insets = new Insets(1,buffer,1,buffer);
    //List<InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();
    List<Synoptic_TimeSeries_InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();

    boolean visibility = true; // Set this so that the layout manager will figure out the size of the dialog at startup.
    int x = 0; // Position in layout manager, same for all since overlap.
    //int numVisibleChoices = -1; // For the combobox choices, -1 means size to data list size.
    try {
        // Time series...
        Synoptic_TimeSeries_InputFilter_JPanel panel = new Synoptic_TimeSeries_InputFilter_JPanel ( dataStore, 5 );
        //panel.setName(dataStore.getName() + ".Station" );
        panel.setName(dataStore.getName() );
        JGUIUtil.addComponent(parent_JPanel, panel,
            x, y, 7, 1, 0.0, 0.0, insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
        inputFilterJPanelList.add ( panel );
        panel.addEventListeners ( this );
        panel.setVisible ( visibility );
    }
    catch ( Exception e ) {
        Message.printWarning ( 2, routine,
        "Unable to initialize input filter for Synoptic time series catalog (" + e + ")." );
        Message.printWarning ( 3, routine, e );
    }
}

/**
Respond to ItemEvents.
*/
public void itemStateChanged ( ItemEvent event ) {
    if ( __ignoreEvents ) {
        return; // Startup.
    }
    if ( (event.getSource() == __DataStore_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected a data store.
        actionPerformedDataStoreSelected ();
    }
    else if ( (event.getSource() == __DataType_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected a data type.
        actionPerformedDataTypeSelected ();
    }
    else if ( (event.getSource() == __Interval_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected an interval.
        actionPerformedIntervalSelected ();
    }
    /*
    else if ( (event.getSource() == __StationId_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected a data type.
        actionPerformedStationIdSelected ();
    }
    */
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event ) {
	refresh();
}

/**
Need this to properly capture key events, especially deletes.
*/
public void keyReleased ( KeyEvent event ) {
	refresh();
}

public void keyTyped ( KeyEvent event ) {
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok () {
	return __ok;
}

/**
Set the data type choices in response to a new datastore being selected.
This should match the main TSTool interface.
@param datastore the datastore to use to determine the data types
*/
private void populateDataTypeChoices ( SynopticDataStore datastore ) {
	if ( datastore == null ) {
		return;
	}
	//boolean includeWildcards = false;
	boolean includeWildcards = true;
	// Don't include the SHEF types since they just complicate things.
    List<String> dataTypes = datastore.getTimeSeriesDataTypeStrings(getSelectedInterval(), includeWildcards);
    __DataType_JComboBox.setData ( dataTypes );
    // Select the default.
    // TODO smalers 2018-06-21 evaluate whether need datastore method for default.
    __DataType_JComboBox.select(0);
}

/**
Populate the data interval choices in response to a new data type being selected.
This code matches the TSTool main interface code.
*/
private void populateIntervalChoices ( SynopticDataStore datastore ) {
	String routine = getClass().getSimpleName() + ".populateIntervalChoices";
	String selectedDataType = getSelectedDataType();
    Message.printStatus ( 2, routine, "Populating intervals for selected data type \"" + selectedDataType + "\"" );
	List<String> dataIntervals = null;
	if ( datastore == null ) {
		dataIntervals = new ArrayList<>();
	}
	else {
		//boolean includeWildcards = false;
		boolean includeWildcards = true;
		dataIntervals = datastore.getTimeSeriesDataIntervalStrings(selectedDataType, includeWildcards);
	}
    __Interval_JComboBox.setData ( dataIntervals );
    // Select the first item.
    try {
        __Interval_JComboBox.select ( null ); // To force event.
        __Interval_JComboBox.select ( 0 );
    }
    catch ( Exception e ) {
        // Cases when for some reason no choice is available.
        __Interval_JComboBox.add ( "" );
        __Interval_JComboBox.select ( 0 );
    }
}

/**
Set the location ID choices in response to a new datastore being selected.
The location choices are also in the where filter (for multiple time series)
but a single location is needed when reading a single time series.
@param datastore the datastore to use to determine the data types
*/
private void populateStationIdChoices ( SynopticDataStore datastore ) {
	// TODO smalers 2023-03-19 not currently used since using a text field.
	/*
	if ( datastore == null ) {
		return;
	}
	//boolean includeWildcards = false;
    List<String> stationIds0 = datastore.getStationIdStrings(getSelectedDataType());
    // Make a copy since adding a blank.
    List<String> stationIds = new ArrayList<>();
    for ( String stationId : stationIds0 ) {
    	stationIds.add(stationId);
    }
    // Add a blank because multiple time series tab might be used.
    stationIds.add(0,"");
    __StationId_JComboBox.setData ( stationIds );
    // Select the default.
    __StationId_JComboBox.select(0);
    */
}

/**
Refresh the command string from the dialog contents.
*/
private void refresh () {
	String routine = getClass().getSimpleName() + ".refresh";
	String Alias = "";
	__error_wait = false;
	String DataStore = "";
	String DataType = "";
	String Interval = "";
	String StationId = "";
	String filterDelim = ";";
	String InputStart = "";
	String InputEnd = "";
	String IrregularInterval = "";
	//String Read24HourAsDay = "";
	//String ReadDayAs24Hour = "";
	//String Timezone = "";
	String Debug = "";
	PropList props = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command.
		props = __command.getCommandParameters();
	    DataStore = props.getValue ( "DataStore" );
	    DataType = props.getValue ( "DataType" );
	    Interval = props.getValue ( "Interval" );
	    StationId = props.getValue ( "StationId" );
		Alias = props.getValue ( "Alias" );
		InputStart = props.getValue ( "InputStart" );
		InputEnd = props.getValue ( "InputEnd" );
		IrregularInterval = props.getValue ( "IrregularInterval" );
		//Read24HourAsDay = props.getValue ( "Read24HourAsDay" );
		//ReadDayAs24Hour = props.getValue ( "ReadDayAs24Hour" );
		//Timezone = props.getValue ( "Timezone" );
		Debug = props.getValue ( "Debug" );
        // The data store list is set up in initialize() but is selected here.
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStore_JComboBox, DataStore, JGUIUtil.NONE, null, null ) ) {
            __DataStore_JComboBox.select ( null ); // To ensure that following causes an event.
            __DataStore_JComboBox.select ( DataStore ); // This will trigger getting the DMI for use in the editor.
        }
        else {
            if ( (DataStore == null) || DataStore.equals("") ) {
                // New command...select the default.
                __DataStore_JComboBox.select ( null ); // To ensure that following causes an event.
                if ( __DataStore_JComboBox.getItemCount() > 0 ) {
                	__DataStore_JComboBox.select ( 0 );
                }
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStore parameter \"" + DataStore + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        //
        // Also need to make sure that the input type and DMI are actually selected.
        // Call manually because events are disabled at startup to allow cascade to work properly.
        setDataStoreForSelectedInput();
        // First populate the data type choices.
        populateDataTypeChoices(getSelectedDataStore() );
        // Then set to the value from the command.
        int [] index = new int[1];
        //Message.printStatus(2,routine,"Checking to see if DataType=\"" + DataType + "\" is a choice.");
        //if ( JGUIUtil.isSimpleJComboBoxItem(__DataType_JComboBox, DataType, JGUIUtil.CHECK_SUBSTRINGS, "-", 0, index, true ) ) {
	    if ( JGUIUtil.isSimpleJComboBoxItem( __DataType_JComboBox, DataType, JGUIUtil.NONE, null, null ) ) {
            // Existing command so select the matching choice.
            //Message.printStatus(2,routine,"DataType=\"" + DataType + "\" was a choice, selecting index " + index[0] + "...");
            //__DataType_JComboBox.select(index[0]);
            __DataType_JComboBox.select(DataType);
        }
        else {
            Message.printStatus(2,routine,"DataType=\"" + DataType + "\" is not a choice.");
            if ( (DataType == null) || DataType.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__DataType_JComboBox.select(0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataType parameter \"" + DataType + "\".  Select a\ndifferent value or Cancel." );
            	__DataType_JComboBox.select(0);
            }
        }
        // Populate the interval choices based on the selected data type.
        populateIntervalChoices(getSelectedDataStore());
        // Now select what the command had previously (if specified).
        //if ( JGUIUtil.isSimpleJComboBoxItem(__Interval_JComboBox, Interval, JGUIUtil.CHECK_SUBSTRINGS, "-", 1, index, true ) ) {
	    if ( JGUIUtil.isSimpleJComboBoxItem( __Interval_JComboBox, Interval, JGUIUtil.NONE, null, null ) ) {
            //__Interval_JComboBox.select (index[0] );
            __Interval_JComboBox.select (Interval);
        }
        else {
            Message.printStatus(2,routine,"Interval=\"" + Interval + "\" is not a choice.");
            if ( (Interval == null) || Interval.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__Interval_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "Interval parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__Interval_JComboBox.select (0);
            }
        }
	    if ( StationId != null ) {
		    __StationId_JTextField.setText ( StationId );
	    }
        // Populate the location ID (station_no) choices based on the time series list.
	    /*
        populateStationIdChoices(getSelectedDataStore());
        // Now select what the command had previously (if specified).
        //if ( JGUIUtil.isSimpleJComboBoxItem(__StationId_JComboBox, StationId, JGUIUtil.CHECK_SUBSTRINGS, "-", 1, index, true ) ) {
	    if ( JGUIUtil.isSimpleJComboBoxItem( __StationId_JComboBox, StationId, JGUIUtil.NONE, null, null ) ) {
            //__StationId_JComboBox.select (index[0] );
            __StationId_JComboBox.select (StationId);
            __tsInfo_JTabbedPane.setSelectedIndex(0);
        }
        else {
            Message.printStatus(2,routine,"StationId=\"" + StationId + "\" is not a choice.");
            if ( (StationId == null) || StationId.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__StationId_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "StationId parameter \"" + StationId + "\".  Select a\ndifferent value or Cancel." );
            	__StationId_JComboBox.select (0);
            }
        }
        */
		// Selecting the data type and interval will result in the corresponding filter group being selected.
		selectInputFilter(getDataStore());
		InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
		if ( filterPanel == null ) {
			Message.printWarning(1, routine, "Trouble finding visible input filter panel for selected Synoptic datastore." );
		}
		else {
    		int nfg = filterPanel.getNumFilterGroups();
    		String where;
    		for ( int ifg = 0; ifg < nfg; ifg++ ) {
    			where = props.getValue ( "Where" + (ifg + 1) );
    			if ( (where != null) && (where.length() > 0) ) {
    				// Set the filter.
    				try {
    				    Message.printStatus(2,routine,"Setting filter Where" + (ifg + 1) + "=\"" + where + "\" from panel " + filterPanel );
    				    filterPanel.setInputFilter (ifg, where, filterDelim );
    				}
    				catch ( Exception e ) {
    					Message.printWarning ( 1, routine,
    					"Error setting where information using \"" + where + "\"" );
    					Message.printWarning ( 3, routine, e );
    				}
    				if ( !where.startsWith(";") ) {
    					// Select the tab.
    					__tsInfo_JTabbedPane.setSelectedIndex(1);
    				}
    			}
    		}
		    // For some reason the values do not always show up so invalidate the component to force redraw.
		    // TODO SAM 2016-08-20 This still does not work.
    		Message.printStatus(2,routine,"Revalidating component to force redraw.");
		    filterPanel.revalidate();
		    //filterPanel.repaint();
		}
	    if ( Alias != null ) {
		    __Alias_JTextField.setText ( Alias );
	    }
		if ( InputStart != null ) {
			__InputStart_JTextField.setText ( InputStart );
		}
		if ( InputEnd != null ) {
			__InputEnd_JTextField.setText ( InputEnd );
		}
	    if ( JGUIUtil.isSimpleJComboBoxItem( __IrregularInterval_JComboBox, IrregularInterval, JGUIUtil.NONE, null, null ) ) {
            //__IrregularInterval_JComboBox.select (index[0] );
            __IrregularInterval_JComboBox.select (IrregularInterval);
        }
        else {
            Message.printStatus(2,routine,"IrregularInterval=\"" + IrregularInterval + "\" is invalid.");
            if ( (IrregularInterval == null) || IrregularInterval.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__IrregularInterval_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "IrregularInterval parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__IrregularInterval_JComboBox.select (0);
            }
        }
	    /*
	    if ( JGUIUtil.isSimpleJComboBoxItem( __Read24HourAsDay_JComboBox, Read24HourAsDay, JGUIUtil.NONE, null, null ) ) {
            //__Read24HourAsDay_JComboBox.select (index[0] );
            __Read24HourAsDay_JComboBox.select (Read24HourAsDay);
        }
        else {
            Message.printStatus(2,routine,"Read24HourAsDay=\"" + Read24HourAsDay + "\" is invalid.");
            if ( (Read24HourAsDay == null) || Read24HourAsDay.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__Read24HourAsDay_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "Read24HourAsDay parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__Read24HourAsDay_JComboBox.select (0);
            }
        }
	    if ( JGUIUtil.isSimpleJComboBoxItem( __ReadDayAs24Hour_JComboBox, ReadDayAs24Hour, JGUIUtil.NONE, null, null ) ) {
            //__ReadDayAs24Hour_JComboBox.select (index[0] );
            __ReadDayAs24Hour_JComboBox.select (ReadDayAs24Hour);
        }
        else {
            Message.printStatus(2,routine,"ReadDayAs24Hour=\"" + ReadDayAs24Hour + "\" is invalid.");
            if ( (ReadDayAs24Hour == null) || ReadDayAs24Hour.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__ReadDayAs24Hour_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "ReadDayAs24Hour parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__ReadDayAs24Hour_JComboBox.select (0);
            }
        }
        */
	    if ( JGUIUtil.isSimpleJComboBoxItem( __Debug_JComboBox, Debug, JGUIUtil.NONE, null, null ) ) {
            //__Debug_JComboBox.select (index[0] );
            __Debug_JComboBox.select (Debug);
        }
        else {
            Message.printStatus(2,routine,"Debug=\"" + Debug + "\" is invalid.");
            if ( (Debug == null) || Debug.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__Debug_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "Debug parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__Debug_JComboBox.select (0);
            }
        }
	}
	// Regardless, reset the command from the fields.
    DataStore = __DataStore_JComboBox.getSelected();
    if ( DataStore == null ) {
        DataStore = "";
    }
	Alias = __Alias_JTextField.getText().trim();
	StationId = __StationId_JTextField.getText().trim();
	//StationId = __StationId_JComboBox.getSelected();
	String DataSource = __DataSource_JTextField.getText().trim();
    DataType = getSelectedDataType();
    Interval = getSelectedInterval();
    // Format a tsid to display in the uneditable text field.
	StringBuffer tsid = new StringBuffer();
	tsid.append ( StationId );
	tsid.append ( "." );
	tsid.append ( DataSource );
	tsid.append ( "." );
	String dataType = DataType;
	if ( (dataType.indexOf("-") >= 0) || (dataType.indexOf(".") >= 0) ) {
		dataType = "'" + dataType + "'";
	}
	tsid.append ( dataType );
	tsid.append ( "." );
	if ( (Interval != null) && !Interval.equals("*") ) {
		tsid.append ( Interval );
	}
	tsid.append ( "~" + getInputNameForTSID() );
	__TSID_JTextField.setText ( tsid.toString() );
	// Regardless, reset the command from the fields.
	props = new PropList ( __command.getCommandName() );
    props.add ( "DataStore=" + DataStore );
	//props.add ( "TSID=" + TSID );
	if ( (StationId != null) && !StationId.isEmpty() ) {
		props.add ( "StationId=" + StationId );
	}
	if ( (DataType != null) && !DataType.isEmpty() ) {
		props.add ( "DataType=" + DataType );
	}
	if ( (Interval != null) && !Interval.isEmpty() ) {
		props.add ( "Interval=" + Interval );
	}
	// Set the where clauses.
	// Since numbers may cause problems, first unset and then set.
	InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	if ( filterPanel != null ) {
    	int nfg = filterPanel.getNumFilterGroups();
        //Message.printStatus(2,routine,"Input filter panel has " + nfg + " filter groups.");
    	String where;
    	for ( int ifg = 0; ifg < nfg; ifg ++ ) {
    		// Use the internal value for the where to ensure integration.
    		where = filterPanel.toString(ifg,filterDelim,3).trim();
    		// Make sure there is a field that is being checked in a where clause:
    		// - otherwise, unset the where if blank
    		props.unSet("Where" + (ifg + 1) );
    		if ( (where.length() > 0) && !where.startsWith(filterDelim) ) {
                // FIXME SAM 2010-11-01 The following discards '=' in the quoted string.
                //props.add ( "Where" + (ifg + 1) + "=" + where );
                props.set ( "Where" + (ifg + 1), where );
                //Message.printStatus(2,routine,"Setting command parameter from visible input filter:  Where" +
                //    (ifg + 1) + "=\"" + where + "\"" );
    		}
    		else {
                //Message.printStatus(2,routine,"Visible input filter:  Where" + (ifg + 1) + " is set to blank, "
               	//	+ "where=" + where + " where.length()=" + where.length() + " filterDelim=" + filterDelim );
    		}
    	}
	}
	else {
		//Message.printStatus(2, routine, "Visible input filter panel is null.");
	}
	props.add ( "Alias=" + Alias );
	InputStart = __InputStart_JTextField.getText().trim();
	props.add ( "InputStart=" + InputStart );
	InputEnd = __InputEnd_JTextField.getText().trim();
	props.add ( "InputEnd=" + InputEnd );
	IrregularInterval = __IrregularInterval_JComboBox.getSelected();
	props.add ( "IrregularInterval=" + IrregularInterval );
	/*
	Read24HourAsDay = __Read24HourAsDay_JComboBox.getSelected();
	props.add ( "Read24HourAsDay=" + Read24HourAsDay );
	ReadDayAs24Hour = __ReadDayAs24Hour_JComboBox.getSelected();
	props.add ( "ReadDayAs24Hour=" + ReadDayAs24Hour );
	*/
	//Timezone = __Timezone_JTextField.getText().trim();
	//props.add ( "Timezone=" + Timezone );
	Debug = __Debug_JComboBox.getSelected();
	props.add ( "Debug=" + Debug );
	__command_JTextArea.setText( __command.toString ( props ).trim() );

	// Check the GUI state to determine whether some controls should be disabled.

	checkGUIState();
}

/**
React to the user response.
@param ok if false, then the edit is canceled.
If true, the edit is committed and the dialog is closed.
*/
private void response ( boolean ok ) {
	__ok = ok;	// Save to be returned by ok().
	if ( ok ) {
		// Commit the changes.
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close.
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

/**
Select (set visible) the appropriate input filter based on the other data choices.
For Synoptic there is currently only one input filter per datastore.
@param dataStore the data store from the DataStore and InputName parameters.
*/
private void selectInputFilter ( SynopticDataStore dataStore ) {
	String routine = getClass().getSimpleName() + ".selectInputFilter";
    // Selected datastore name.
    if ( dataStore == null ) {
        return;
    }
    String dataStoreName = dataStore.getName();
    // Selected data type and interval must be converted to Synoptic internal convention.
    // The following lookups are currently hard coded and not read from Synoptic.
    String selectedDataType = getSelectedDataType();
    String selectedTimeStep = __Interval_JComboBox.getSelected();
    //List<InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();
    List<Synoptic_TimeSeries_InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();
    // Loop through all available input filters and match the data store name, type (whether legacy or new design),
    // and filter for the data type.  If matched, set to visible and otherwise not visible.
    boolean matched;
    int matchCount = 0;
    Message.printStatus(2, routine, "Trying to set visible the input filter given selected datastore name \"" + dataStoreName +
        "\" selectedDataType=\"" + selectedDataType + "\" selectedTimeStep=\"" + selectedTimeStep + "\"" );
    for ( InputFilter_JPanel panel : inputFilterJPanelList ) {
        matched = false; // Does selected datastore name match the filter datastore?
        SynopticDataStore datastore =
            ((Synoptic_TimeSeries_InputFilter_JPanel)panel).getDataStore();
        if ( (datastore != null) && datastore.getName().equalsIgnoreCase(dataStoreName) ) {
            // Have a match in the datastore name so return the panel.
            matched = true;
        }
        // If the panel was matched, set it visible.
        panel.setVisible(matched);
        if ( matched ) {
            ++matchCount;
        }
    }
    // No normal panels were matched enable the generic panel, which will be last panel in list.
    InputFilter_JPanel defaultPanel = inputFilterJPanelList.get(inputFilterJPanelList.size() - 1);
    if ( matchCount == 0 ) {
        defaultPanel.setVisible(true);
        Message.printStatus(2, routine, "Setting default input filter panel visible.");
    }
    else {
        defaultPanel.setVisible(false);
    }
}

/**
Set all the filters visible, necessary to help compute layout dimensions and dialog size.
*/
private void setAllFiltersVisible() {
    //List<InputFilter_JPanel> panelList = getInputFilterJPanelList();
    List<Synoptic_TimeSeries_InputFilter_JPanel> panelList = getInputFilterJPanelList();
    for ( InputFilter_JPanel panel : panelList ) {
        panel.setVisible(true);
    }
}

/**
Set the datastore to use for queries based on the selected data store and input name.
*/
private void setDataStoreForSelectedInput() {
    // Data store will be used if set.  Otherwise input name is used.
    String dataStoreString = __DataStore_JComboBox.getSelected();
    if ( dataStoreString == null ) {
        dataStoreString = "";
    }
    if ( !dataStoreString.equals("") ) {
        // Use the selected datastore.
        __dataStore = getSelectedDataStore();
    }
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event ) {
	response ( false );
}

public void windowActivated( WindowEvent evt ) {
}

public void windowClosed( WindowEvent evt ) {
}

public void windowDeactivated( WindowEvent evt ) {
}

public void windowDeiconified( WindowEvent evt ) {
}

public void windowIconified( WindowEvent evt ) {
}

public void windowOpened( WindowEvent evt ) {
}

}
