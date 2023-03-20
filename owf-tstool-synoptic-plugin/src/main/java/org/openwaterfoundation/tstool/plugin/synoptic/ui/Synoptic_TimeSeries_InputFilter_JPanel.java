// Synoptic_TimeSeries_InputFilter_JPanel - panel to filter time series queries

/* NoticeStart

OWF TSTool Synoptic Plugin
Copyright (C) 2023 Open Water Foundation

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

package org.openwaterfoundation.tstool.plugin.synoptic.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openwaterfoundation.tstool.plugin.synoptic.dao.Network;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.NwsCwa;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.State;
import org.openwaterfoundation.tstool.plugin.synoptic.datastore.SynopticDataStore;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is an input filter for querying Synoptic web services.
*/
@SuppressWarnings("serial")
public class Synoptic_TimeSeries_InputFilter_JPanel extends InputFilter_JPanel {

	/**
	Test datastore, for connection.
	*/
	private SynopticDataStore datastore = null;

	/**
	Constructor for case when no datastore is configured - default panel.
	@param label label for the panel
	*/
	public Synoptic_TimeSeries_InputFilter_JPanel( String label ) {
		super(label);
	}

	/**
	Constructor.
	@param dataStore the data store to use to connect to the test database.  Cannot be null.
	@param numFilterGroups the number of filter groups to display
	*/
	public Synoptic_TimeSeries_InputFilter_JPanel( SynopticDataStore dataStore, int numFilterGroups ) {
	    super();
	    this.datastore = dataStore;
	    if ( this.datastore != null ) {
	        setFilters ( numFilterGroups );
	    }
	}

	/**
 	* Check the input filter for appropriate combination of choices.
 	* These checks can be performed in the ReadSynoptic command and the main TSTool UI,
 	* both of which use this class.
 	* @param displayWarning If true, display a warning dialog if there are errors in the input.
 	* If false, do not display a warning, in which case the calling code should generally display a warning and optionally
 	* also perform other checks by overriding this method.
 	* @return null if no issues or a string that indicates issues,
 	* can use \n for line breaks and put at the front of the string.
 	*/
	@Override
	public String checkInputFilters ( boolean displayWarning ) {
		// Use the parent class method to check basic input types based on data types:
		// - will return empty string if no issues
		String warning = super.checkInputFilters(displayWarning);
		// Perform specific checks.
		String warning2 = "";
		int coordCount = 0;
		/*
		String Latitude = getInputValue("Latitude", false);
		String Longitude = getInputValue("Longitude", false);
		String LatLongRadius = getInputValue("LatLongRadius", false);
		String LatLongRadiusUnits = getInputValue("LatLongRadiusUnits", false);
		if ( (Latitude != null) && !Latitude.isEmpty() ) {
			++coordCount;
		}
		if ( (Longitude != null) && !Longitude.isEmpty() ) {
			++coordCount;
		}
		if ( (LatLongRadius != null) && !LatLongRadius.isEmpty() ) {
			++coordCount;
		}
		if ( (LatLongRadiusUnits != null) && !LatLongRadiusUnits.isEmpty() ) {
			++coordCount;
		}
		if ( (coordCount > 0) && (coordCount != 4) ) {
			warning2 += "\nSpecifying latitude and longitude requires specifying latitude, longitude, radius, and units.";
		}
		*/
		// Check that at least one of state specified.
		int necInputFilters = 0;
		String state = getInputValue("State", false);
		if( (state != null) && !state.isEmpty()){
			++necInputFilters;
		}
		String cwa = getInputValue("NWS CWA", false);
		if( (cwa != null) && !cwa.isEmpty()){
			++necInputFilters;
		}
		if( necInputFilters == 0 ) {
			warning2 += "\nState or NWS CWA is required to limit the query.";
		}
		if ( !warning2.isEmpty() ) {
			// Have non-empty specific warnings so append specific warnings.
			warning += warning2;
		}
		// Return the general warnings or the appended results.
		return warning;
	}

	/**
	Set the filter data.  This method is called at setup and when refreshing the list with a new subject type.
	For all cases, use the InputFilter constructor "whereLabelPersistent" to ensure that the TSTool ReadSynoptic command
	will show nice choices.
	*/
	public void setFilters ( int numFilterGroups ) {
		String routine = getClass().getSimpleName() + ".setFilters";
		
		// Read the data to populate filter choices.

		// Network choices.
		List<String> networkChoices = new ArrayList<>();
		try {
			List<Network> networkList = new ArrayList<>();
			networkList = datastore.getNetworks(false);
			for ( Network network : networkList ) {
				networkChoices.add(network.getShortName());
			}
		}
		catch ( Exception e ) {
			Message.printWarning(2, routine, "Error getting networks for input filter.");
		}
		Collections.sort(networkChoices);

		// NWS CWA choices:
		// - sorted when read
		List<String> nwsCwaChoices = new ArrayList<>();
		List<String> nwsCwaInternalChoices = new ArrayList<>();
		try {
			List<NwsCwa> nwsCwaList = new ArrayList<>();
			nwsCwaList = datastore.getNwsCwaList(false);
			for ( NwsCwa nwsCwa : nwsCwaList ) {
				nwsCwaChoices.add(nwsCwa.getFilterChoice());
				nwsCwaInternalChoices.add(nwsCwa.getCwa());
			}
		}
		catch ( Exception e ) {
			Message.printWarning(2, routine, "Error getting NWS CWA for input filter.");
		}

		// State choices.
		List<String> stateChoices = new ArrayList<>();
		List<String> stateInternalChoices = new ArrayList<>();
		List<State> stateList = datastore.getStates(false);
		for ( State state : stateList ) {
			stateChoices.add(state.getAbbreviation() + " - " + state.getName());
			stateInternalChoices.add(state.getAbbreviation());
		}
		// Do not sort because have two lists and should be sorted already.

		// Status choices.
		List<String> statusChoices = new ArrayList<>();
		statusChoices.add("ACTIVE");
		statusChoices.add("INACTIVE");
		
		// The internal names for filters match the /tscatalog web service query parameters.
		// TODO smalers 2020-01-24 add more filters for points, point type, etc. as long as the web service API supports.

	    List<InputFilter> filters = new ArrayList<>();

	    // Always add blank to top of filter.
	    filters.add(new InputFilter("", "", StringUtil.TYPE_STRING, null, null, false)); // Blank.

	    // Network.
	    if ( networkChoices.size() > 0 ) {
	    	InputFilter filter = new InputFilter("Network",
	        	"network", "network", "network",
	        	StringUtil.TYPE_STRING, networkChoices, networkChoices, false);
	    	filter.removeConstraint(InputFilter.INPUT_CONTAINS);
	    	filter.removeConstraint(InputFilter.INPUT_ENDS_WITH);
	    	filter.removeConstraint(InputFilter.INPUT_STARTS_WITH);
	    	filters.add(filter);
	    }

	    // NWS CWA.
	    if ( nwsCwaChoices.size() > 0 ) {
	    	InputFilter filter = new InputFilter("NWS CWA",
	        	"cwa", "cwa", "cwa",
	        	StringUtil.TYPE_STRING, nwsCwaChoices, nwsCwaInternalChoices, false);
	    	filter.removeConstraint(InputFilter.INPUT_CONTAINS);
	    	filter.removeConstraint(InputFilter.INPUT_ENDS_WITH);
	    	filter.removeConstraint(InputFilter.INPUT_STARTS_WITH);
	    	// Tell the filter that values need to be parsed because of the additional note.
	    	filter.setTokenInfo ( "-", 0, StringUtil.TYPE_STRING );
	    	filters.add(filter);
	    }

	    // State:
	    // - provide an internal list that only has state abbreviation
	    // - TODO smalers 2023-03-16 figure out how to use the token for internal data
	    // State.
	    if ( stateChoices.size() > 0 ) {
	    	InputFilter filter = new InputFilter("State",
	        	"state", "state", "state",
	        	StringUtil.TYPE_STRING, stateChoices, stateInternalChoices, false);
	    	// Tell the filter that values need to be parsed because of the additional note.
	    	filter.setTokenInfo ( "-", 0, StringUtil.TYPE_STRING );
	    	filter.removeConstraint(InputFilter.INPUT_CONTAINS);
	    	filter.removeConstraint(InputFilter.INPUT_ENDS_WITH);
	    	filter.removeConstraint(InputFilter.INPUT_STARTS_WITH);
	    	filters.add(filter);
	    }

	    // Station ID:
	    // - add a text field so that a list of stations can be queried
    	InputFilter filter = new InputFilter("Station - ID",
        	"stid", "stid", "stid",
        	StringUtil.TYPE_STRING, null, null, false);
    	filter.removeConstraint(InputFilter.INPUT_CONTAINS);
    	filter.removeConstraint(InputFilter.INPUT_ENDS_WITH);
    	filter.removeConstraint(InputFilter.INPUT_STARTS_WITH);
    	filters.add(filter);

	    // Status.
	    if ( statusChoices.size() > 0 ) {
	    	InputFilter stidFilter = new InputFilter("Status",
	        	"status", "status", "status",
	        	StringUtil.TYPE_STRING, statusChoices, statusChoices, false);
	    	stidFilter.removeConstraint(InputFilter.INPUT_CONTAINS);
	    	stidFilter.removeConstraint(InputFilter.INPUT_ENDS_WITH);
	    	stidFilter.removeConstraint(InputFilter.INPUT_STARTS_WITH);
	    	filters.add(stidFilter);
	    }

	  	setToolTipText("<html>Specify one or more input filters to limit query, will be ANDed.</html>");
	    
	    int numVisible = 14;
	    setInputFilters(filters, numFilterGroups, numVisible);
	}

	/**
	Return the data store corresponding to this input filter panel.
	@return the data store corresponding to this input filter panel.
	*/
	public SynopticDataStore getDataStore ( ) {
	    return this.datastore;
	}
}
