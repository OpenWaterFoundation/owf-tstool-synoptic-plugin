// SynopticDataStore - class that implements the SynopticDataStore plugin datastore

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

package org.openwaterfoundation.tstool.plugin.synoptic.datastore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openwaterfoundation.tstool.plugin.synoptic.PluginMeta;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.MetadataStation;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.Network;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.NwsCwa;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.State;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.StationObservations;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.Summary;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.Units;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.Variable;
import org.openwaterfoundation.tstool.plugin.synoptic.dto.JacksonToolkit;
import org.openwaterfoundation.tstool.plugin.synoptic.ui.Synoptic_TimeSeries_CellRenderer;
import org.openwaterfoundation.tstool.plugin.synoptic.ui.Synoptic_TimeSeries_InputFilter_JPanel;
import org.openwaterfoundation.tstool.plugin.synoptic.ui.Synoptic_TimeSeries_TableModel;
import org.openwaterfoundation.tstool.plugin.synoptic.util.WebUtil;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequirementCheck;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import riverside.datastore.AbstractWebServiceDataStore;
import riverside.datastore.DataStoreRequirementChecker;
import riverside.datastore.PluginDataStore;

public class SynopticDataStore extends AbstractWebServiceDataStore implements DataStoreRequirementChecker, PluginDataStore {

	/**
	 * Resource path to find supporting files.
	 */
	public final String RESOURCE_PATH = "/org/openwaterfoundation/tstool/plugin/synoptic/resources";

	/**
	 * API token required by the API, set in the datastore configuration 'ApiToken' proeprty.
	 */
	private String apiToken = "";

	/**
	 * Properties for the plugin, used to help with application integration.
	 */
	private Map<String,Object> pluginProperties = new LinkedHashMap<>();

	/**
	 * Global time series catalog, used to streamline creating lists for UI choices.
	 */
	private List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

	/**
	 * Global state (i.e, US states) list.
	 */
	private List<Network> networkList = new ArrayList<>();

	/**
	 * Global NWS CWA list.
	 */
	private List<NwsCwa> nwsCwaList = new ArrayList<>();

	/**
	 * Global state list.
	 */
	private List<State> stateList = new ArrayList<>();

	/**
	 * Global variable list.
	 */
	private List<Variable> variableList = new ArrayList<>();

	/**
	 * Global debug option for datastore, used for development and troubleshooting.
	 */
	private boolean debug = false;

	/**
	Constructor for web service.
	@param name identifier for the data store
	@param description name for the data store
	@param serviceRootURI the root of the URL for the API, for example 'https://api.synopticdata.com/v2'.
	@param props properties from the datastore configuration file, which must contain the 'ApiToken' property.
	*/
	public SynopticDataStore ( String name, String description, URI serviceRootURI, PropList props ) {
		String routine = getClass().getSimpleName() + ".SynopticDataStore";

		String prop = props.getValue("Debug");
		if ( (prop != null) && prop.equalsIgnoreCase("true") ) {
			Message.printStatus(2, routine, "Datastore \"" + name + "\" - detected Debug=true");
			this.debug = true;
		}
	    setName ( name );
	    setDescription ( description );
	    setServiceRootURI ( serviceRootURI );
	    setProperties ( props );

	    // The API token is used for all requests so set as datastore data.
	    this.apiToken = props.getValue("ApiToken");
	    if ( this.apiToken == null ) {
	    	// Don't warn here.  Use the messages from the service.
	    	this.apiToken = "";
	    }

	    // Set standard plugin properties:
        // - plugin properties can be listed in the main TSTool interface
        // - version is used to create a versioned installer and documentation.
        this.pluginProperties.put("Name", "Open Water Foundation Synoptic data web services plugin");
        this.pluginProperties.put("Description", "Plugin to integrate TSTool with Synoptic web resources.");
        this.pluginProperties.put("Author", "Open Water Foundation, https://openwaterfoundation.org");
        this.pluginProperties.put("Version", PluginMeta.VERSION);

	    // Read global data used throughout the session:
	    // - in particular a cache of the TimeSeriesCatalog used for further queries

	    readGlobalData();
	}

	/**
	* THIS IS PLACEHOLDER CODE - NEED TO IMPLEMENT.
	*
 	* Check the database requirement for DataStoreRequirementChecker interface, for example one of:
 	* <pre>
 	* @require datastore kiwis-northern version >= 1.5.5
 	* @require datastore kiwis-northern ?configproperty propname? == Something
 	* @require datastore kiwis-northern configuration system_id == CO-District-MHFD
 	*
 	* @enabledif datastore nsdataws-mhfd version >= 1.5.5
 	* </pre>
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		String routine = getClass().getSimpleName() + ".checkRequirement";
		// Parse the string into parts:
		// - calling code has already interpreted the first 3 parts to be able to do this call
		String requirement = check.getRequirementText();
		Message.printStatus(2, routine, "Checking requirement: " + requirement);
		// Get the annotation that is being checked, so messages are appropriate.
		String annotation = check.getAnnotation();
		String [] requireParts = requirement.split(" ");
		// Datastore name may be an original name but a substitute is used, via TSTool command line.
		String dsName = requireParts[2];
		String dsNameNote = ""; // Note to add on messages to help confirm how substitutions are being handled.
		String checkerName = "SynopticDataStore";
		if ( !dsName.equals(this.getName())) {
			// A substitute datastore name is being used, such as in testing.
			dsNameNote = "\nCommand file datastore name '" + dsName + "' substitute that is actually used is '" + this.getName() + "'";
		}
		if ( requireParts.length < 4 ) {
			check.setIsRequirementMet(checkerName, false, "Requirement does not contain check type as one of: version, configuration, "
				+ "for example: " + annotation + " datastore nsdataws-mhfd version...");
			return check.isRequirementMet();
		}
		String checkType = requireParts[3];
		if ( checkType.equalsIgnoreCase("configuration") ) {
			// Checking requirement of form:
			// 0        1         2             3             4         5  6
			// @require datastore nws-afos configuration system_id == CO-District-MHFD
			String propertyName = requireParts[4];
			String operator = requireParts[5];
			String checkValue = requireParts[6];
			// Get the configuration table property of interest:
			// - currently only support checking system_id
			if ( propertyName.equals("system_id") ) {
				// Know how to handle "system_id" property.
				if ( (checkValue == null) || checkValue.isEmpty() ) {
					// Unable to do check.
					check.setIsRequirementMet ( checkerName, false, "'system_id' value to check is not specified in the requirement." + dsNameNote );
					return check.isRequirementMet();
				}
				else {
					// TODO smalers 2023-01-03 need to evaluate whether KiWIS has configuration properties.
					//String propertyValue = readConfigurationProperty(propertyName);
					String propertyValue = "";
					if ( (propertyValue == null) || propertyValue.isEmpty() ) {
						// Unable to do check.
						check.setIsRequirementMet ( checkerName, false, "KiWIS configuration 'system_id' value is not defined in the database." + dsNameNote );
						return check.isRequirementMet();
					}
					else {
						if ( StringUtil.compareUsingOperator(propertyValue, operator, checkValue) ) {
							check.setIsRequirementMet ( checkerName, true, "KiWIS configuration property '" + propertyName + "' value (" + propertyValue +
								") does meet the requirement: " + operator + " " + checkValue + dsNameNote );
						}
						else {
							check.setIsRequirementMet ( checkerName, false, "KiWIS configuration property '" + propertyName + "' value (" + propertyValue +
								") does not meet the requirement:" + operator + " " + checkValue + dsNameNote );
						}
						return check.isRequirementMet();
					}
				}
			}
			else {
				// Other properties may not be easy to compare.  Probably need to use "contains" and other operators.
				check.setIsRequirementMet ( checkerName, false, "Check type '" + checkType + "' configuration property '" + propertyName + "' is not supported.");
				return check.isRequirementMet();
			}
		}
		/* TODO smalers 2021-07-29 need to implement, maybe need to define the system ID in the configuration file as a cross check for testing.
		else if ( checkType.equalsIgnoreCase("configproperty") ) {
			if ( parts.length < 7 ) {
				// 'property' requires 7 parts
				throw new RuntimeException( "'configproperty' requirement does not contain at least 7 parts for: " + requirement);
			}
		}
		*/
		else if ( checkType.equalsIgnoreCase("version") ) {
			// Checking requirement of form:
			// 0        1         2             3       4  5
			// @require datastore nws-afos version >= 1.5.5
			Message.printStatus(2, routine, "Checking web service version.");
			// Do a web service round trip to check version since it may change with software updates.
			String wsVersion = readVersion();
			if ( (wsVersion == null) || wsVersion.isEmpty() ) {
				// Unable to do check.
				check.setIsRequirementMet ( checkerName, false, "Web service version is unknown (services are down or software problem).");
				return check.isRequirementMet();
			}
			else {
				// Web service versions are strings of format A.B.C.D so can do semantic version comparison:
				// - only compare the first 3 parts
				//Message.printStatus(2, "checkRequirement", "Comparing " + wsVersion + " " + operator + " " + checkValue);
				String operator = requireParts[4];
				String checkValue = requireParts[5];
				boolean verCheck = StringUtil.compareSemanticVersions(wsVersion, operator, checkValue, 3);
				String message = "";
				if ( !verCheck ) {
					message = annotation + " web service version (" + wsVersion + ") does not meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				else {
					message = annotation + " web service version (" + wsVersion + ") does meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				return check.isRequirementMet();
			}
		}
		else {
			// Unknown check type.
			check.setIsRequirementMet ( checkerName, false, "Requirement check type '" + checkType + "' is unknown.");
			return check.isRequirementMet();
		}

	}

	/**
	 * Create a time series input filter, used to initialize user interfaces.
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel () {
		Synoptic_TimeSeries_InputFilter_JPanel ifp = new Synoptic_TimeSeries_InputFilter_JPanel(this, 4);
		return ifp;
	}

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp ) {
		// First query the database for the specified input.
		List<TimeSeriesCatalog> tsmetaList = readTimeSeriesMeta ( dataType, timeStep, ifp );
		return getTimeSeriesListTableModel(tsmetaList);
	}

	/**
	 * Get the list of location identifier (station_no) strings used in the UI.
	 * The list is determined from the cached list of time series catalog.
	 * @param dataType to match, or * or null to return all, should be a value of stationparameter_no
	 * @return a unique sorted list of the location identifiers (station_no)
	 */
	public List<String> getStationIdStrings ( String dataType ) {
		List<String> locIdList = new ArrayList<>();
		/*
		if ( (dataType == null) || dataType.isEmpty() || dataType.equals("*") ) {
			// Return the cached list of all locations.
			return this.locIdList;
		}
		else {
			// Get the list of locations from the cached list of time series catalog
			List<String> locIdList = new ArrayList<>();
			String stationNo = null;
			String stationParameterNo = null;
			boolean found = false;
			for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
				stationNo = tscatalog.getStationNo();
				stationParameterNo = tscatalog.getStationParameterNo();

				if ( !stationParameterNo.equals(dataType) ) {
					// Requested data type does not match.
					continue;
				}

				found = false;
				for ( String locId2 : locIdList ) {
					if ( locId2.equals(stationNo) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					locIdList.add(stationNo);
				}
			}
			Collections.sort(locIdList, String.CASE_INSENSITIVE_ORDER);
			return locIdList;
		}
		*/
		return locIdList;
	}

	/**
	 * Fix a request involving 'network':
	 * - the documentation says that the short name can be specified but it seems that only network ID can be used
	 * - rather than switching to opaque network ID in commands, swap '&network=cwa' with '&network=63'.
	 * @param requestUrl the full request URL
	 * @return the updated URL with network number instead of ID
	 */
	private String fixNetworkRequest ( String requestUrl ) {
		// Could probably do this with a regular expression but don't have time to fully confirm so do brute force.
		int pos = requestUrl.indexOf("&network=");
		if ( pos < 0 ) {
			// Don't have a string that needs to be fixed.
			return requestUrl;
		}
		else {
			// Fix the URL:
			// - get the following &
			// - if not found then network is at the end of the URL
			int pos2 = requestUrl.indexOf("&", pos + 1);
			int posEqual = requestUrl.indexOf("=", pos + 1);
			if ( pos2 < 0 ) {
				// 'network' is at the end of the URL.
				String network = requestUrl.substring(posEqual+1);
				if ( StringUtil.isInteger(network) ) {
					// Network is an integer so leave as is.
					return requestUrl;
				}
				else {
					// Network is not an integer so replace the short name with the ID.
					String requestUrl2 = requestUrl.substring(0,pos) + "&network="
						+ Network.lookupNetworkFromShortName(this.networkList, network).getId();
					return requestUrl2;
				}
			}
			else {
				// 'network' is followed by other query parameters.
				String network = requestUrl.substring(posEqual+1,pos2);
				if ( StringUtil.isInteger(network) ) {
					// Network is an integer so leave as is.
					return requestUrl;
				}
				else {
					// Network is not an integer so replace the short name with the ID.
					String requestUrl2 = requestUrl.substring(0,pos) + "&network="
						+ Network.lookupNetworkFromShortName(this.networkList, network).getId()
						+ requestUrl.substring(pos2);
					return requestUrl2;
				}
			}
		}
	}

	/**
	 * Format a UTC time series from a DateTime in local time.
	 * The output will be consistent with what is needed for the 'timeseries' start and end.
	 * @param dt DateTime to format, such as from the PERIOD_OF_RECORD in UTC with Z at the end
	 * @param stationTimeZone the station's local time zone, for example "America/Denver"
	 * @return a formatted string in UTC
	 */
	private String formatUtcTimeFromLocal ( DateTime dt, String stationTimeZone ) {
		String routine = getClass().getSimpleName() + ".formatUtcTimeFromLocal";
		Message.printStatus(2,routine, "Converting DateTime " + dt + " to UTC time zone." );
		// Create a ZonedDateTime from the DateTime:
		// - use the station local time zone, NOT the time zone in 'dt'
		// - therefore the time will be in the station local time regardless of the computer's local time
		ZoneId zoneId = ZoneId.of(stationTimeZone);
		ZonedDateTime zdt = ZonedDateTime.of(
			dt.getYear(),
			dt.getMonth(),
			dt.getDay(),
			dt.getHour(),
			dt.getMinute(),
			dt.getSecond(),
			dt.getNanoSecond(),
			zoneId );
		// Convert to UTC.
		ZoneId utcZoneId = ZoneId.of("UTC");
		ZonedDateTime dtUtc = zdt.withZoneSameInstant(utcZoneId);
		// Synoptic API only wants precision to minute.
		String utcTime = String.format("%04d%02d%02d%02d%02d",
			dtUtc.getYear(),
			dtUtc.getMonthValue(),
			dtUtc.getDayOfMonth(),
			dtUtc.getHour(),
			dtUtc.getMinute());
		Message.printStatus(2,routine, "  UTC time = " + utcTime );
		return utcTime;
	}

	/**
	 * Return the API token.
	 */
	public String getApiToken() {
		return this.apiToken;
	}

	/**
	 * Return the API token query parameter (token=API_TOKEN).
	 */
	public String getApiTokenParameter() {
		return "token=" + this.apiToken;
	}

	/**
 	* Get the properties for the plugin.
 	* A copy of the properties map is returned so that calling code cannot change the properties for the plugin.
 	* @return plugin properties map.
 	*/
	public Map<String,Object> getPluginProperties () {
		Map<String,Object> pluginProperties = new LinkedHashMap<>();
		// For now the properties are all strings so it is easy to copy.
    	for (Map.Entry<String, Object> entry : this.pluginProperties.entrySet()) {
        	pluginProperties.put(entry.getKey(),
                    	entry.getValue());
    	}
		return pluginProperties;
	}

	/**
	 * Return the list of networks.
	 * @param readData if false, return the global cached data, if true read the data and reset in he cache
	 */
	public List<Network> getNetworks(boolean readData) throws IOException {
		if ( readData ) {
			this.networkList = readNetworkList();
		}
		return this.networkList;
	}

	/**
	 * Return the list of NWS CWA.
	 * @param readData if false, return the global cached data, if true read the data and reset in he cache
	 */
	public List<NwsCwa> getNwsCwaList(boolean readData) throws IOException {
		if ( readData ) {
			this.nwsCwaList = readNwsCwaList();
		}
		return this.nwsCwaList;
	}

	/**
	 * Return the list of states.
	 * @param readData if false, return the global cached data, if true read the data and reset in he cache
	 */
	public List<State> getStates(boolean readData) {
		if ( readData ) {
			this.stateList = readStateList();
		}
		return this.stateList;
	}

	/**
	 * Deserialize the SUMMARY object from a response.
	 * @param rootNode the root node that contains 'SUMMARY'
	 * @return the Summary object for the the response
	 */
	private Summary getSummary ( JsonNode rootNode ) {
		JsonNode summaryNode = rootNode.get("SUMMARY");
		if ( summaryNode != null ) {
			return (Summary)JacksonToolkit.getInstance().treeToValue(summaryNode, Summary.class);
		}
		else {
			return null;
		}
	}

	/**
	 * Return the list of time series catalog.
	 * @param readData if false, return the global cached data, if true read the data and reset in he cache
	 */
	public List<TimeSeriesCatalog> getTimeSeriesCatalog(boolean readData) {
		if ( readData ) {
			String tsid = null;
			String dataTypeReq = null;
			String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
			this.tscatalogList = readTimeSeriesCatalog(tsid, dataTypeReq, dataIntervalReq, ifp );
		}
		return this.tscatalogList;
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType) {
		boolean includeWildcards = true;
		return getTimeSeriesDataIntervalStrings(dataType, includeWildcards);
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 * @param includeWildcards if true, include "*" wildcard.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType, boolean includeWildcards ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesDataIntervalStrings";
		List<String> dataIntervals = new ArrayList<>();

		// Currently only IrregSecond since not sure how timesteps align and use * wildcards.
		dataIntervals.add("IrregSecond");

		/*
		Message.printStatus(2, routine, "Getting interval strings for data type \"" + dataType + "\"");

		// Only check datatype if not a wildcard.
		boolean doCheckDataType = false;
		if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
			doCheckDataType = true;
		}

		// Use the cached time series catalog read at startup.
		List<TimeSeriesCatalog> tscatalogList = getTimeSeriesCatalog(false);
		Message.printStatus(2, routine, "  Have " + tscatalogList.size() + " cached time series from the catalog.");
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			if ( doCheckDataType ) {
				// Only check the first part of the data type, which is the 'stationparameter_no'.
				if ( !dataType.equals(tscatalog.getStationParameterNo())) {
					// Data type does not match 'stationparameter_no'.
					continue;
				}
			}
			// Only add the interval if not already in the list.
			if ( !StringUtil.isInList(dataIntervals, tscatalog.getDataInterval())) {
				dataIntervals.add(tscatalog.getDataInterval());
			}
		}
		*/

		// Sort the intervals:
		// - TODO smalers need to sort by time
		Collections.sort(dataIntervals,String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Always allow querying list of time series for all intervals:
			// - always add so that people can get a full list
			// - adding at top makes it easy to explore data without having to scroll to the end

			dataIntervals.add("*");
			if ( dataIntervals.size() > 1 ) {
				// Also add at the beginning to simplify selections:
				// - could check for a small number like 5 but there should always be a few
				dataIntervals.add(0,"*");
			}
		}

		return dataIntervals;
	}

	/**
	 * Return the list of time series data type strings.
	 * This is the version that is required by TSTool UI.
	 * These strings are the same as the dataTypes.name properties from the stationSummaries web service request.
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types (treat as if "*").
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval) {
		boolean includeWildcards = true;
		return getTimeSeriesDataTypeStrings(dataInterval, includeWildcards );
	}

	/**
	 * Return the list of time series data type strings.
	 * These strings are the same as the parameter type list 'parametertype_name'.
	 * @param dataInterval the data interval to filter data types
	 * @param includeWildcards whether "*" should be included at the start and end of the list
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval, boolean includeWildcards ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesDataTypeStrings";

		// Currently the data types are a static list, not determined from an API call.
		List<String> dataTypes = new ArrayList<>();

		for ( Variable variable : this.variableList ) {
			dataTypes.add( variable.getName() );
		}

		// Sort the names.
		Collections.sort(dataTypes, String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Add wildcard at the front and end - allows querying all data types for the location:
			// - always add so that people can get a full list
			// - adding at the top makes it easy to explore data without having to scroll to the end

			dataTypes.add("*");
			if ( dataTypes.size() > 1 ) {
				// Also add at the beginning to simplify selections:
				// - could check for a small number like 5 but there should always be a few
				dataTypes.add(0,"*");
			}
		}

		return dataTypes;
	}

	/**
 	* Return the identifier for a time series in the table model.
 	* The TSIdent parts will be uses as TSID commands.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	*/
	public TSIdent getTimeSeriesIdentifierFromTableModel( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel,
		int row ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesIdentifierFromTableModel";
    	Synoptic_TimeSeries_TableModel tm = (Synoptic_TimeSeries_TableModel)tableModel;
    	// Should not have any nulls.
    	//String locId = (String)tableModel.getValueAt(row,tm.COL_LOCATION_ID);
    	//String source = "Synoptic";
    	String source = (String)tableModel.getValueAt(row,tm.COL_STATION_MNET);
    	String dataType = (String)tableModel.getValueAt(row,tm.COL_DATA_TYPE);
    	String interval = (String)tableModel.getValueAt(row,tm.COL_DATA_INTERVAL);
    	String scenario = "";
    	String inputName = ""; // Only used for files.
    	TSIdent tsid = null;
		String datastoreName = this.getName();
   		String locId = (String)tableModel.getValueAt(row,tm.COL_STATION_ID);
    	try {
    		tsid = new TSIdent(locId, source, dataType, interval, scenario, datastoreName, inputName );
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	return tsid;
	}

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel) {
    	return new Synoptic_TimeSeries_CellRenderer ((Synoptic_TimeSeries_TableModel)tableModel);
    }

    /**
     * Get the TableModel used for displaying the time series.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data) {
    	return new Synoptic_TimeSeries_TableModel(this,(List<TimeSeriesCatalog>)data);
    }

    /**
     * Get the value array name used in the 'timeseries' service response (e.g., "precip_accum_one_hour_set_1")
     * for the 'metadata' sensor variable (e.g., "precip_accum_one_hour_1")
     * @param sensorVariable sensor variable with the number (e.g., "precip_accum_one_hour_1")
     */
	private String getValueArrayNameForSensorVariable ( String sensorVariable ) {
		// Get the ending of the input, for example "_1".
		int pos = sensorVariable.lastIndexOf("_");
		String number = sensorVariable.substring(pos+1);
		return sensorVariable.substring(0,pos) + "_set_" + number;
	}

	/**
	 * Indicate whether the datastore provides a time series input filter.
	 * This datastore does provide an input filter panel.
	 */
	public boolean providesTimeSeriesListInputFilterPanel () {
		return true;
	}

	/**
	 * Read global data that should be kept in memory to increase performance.
	 * This is called from the constructor.
	 * The following data are read and are available with get() methods:
	 * <ul>
	 * <li>TimeSeriesCatalog - cache used to find time series without re-requesting from the web service</li>
	 * </ul>
	 * If an error is detected, set on the datastore so that TSTool View / Datastores will show the error.
	 * This is usually an issue with a misconfigured datastore.
	 */
	public void readGlobalData () {
		String routine = getClass().getSimpleName() + ".readGlobalData";
		Message.printWarning ( 2, routine, "Reading global data for datastore \"" + getName() + "\"." );

		try {
			this.stateList = readStateList();
			Message.printStatus(2, routine, "Initialized " + this.stateList.size() + " states." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error initializing global state list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		try {
			this.networkList = readNetworkList();
			Message.printStatus(2, routine, "Read " + this.networkList.size() + " networks." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global network list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		try {
			this.nwsCwaList = readNwsCwaList();
			Message.printStatus(2, routine, "Read " + this.nwsCwaList.size() + " NWS CWA." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global network list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		try {
			this.variableList = readVariableList();
			Message.printStatus(2, routine, "Read " + this.variableList.size() + " variables." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global variable list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

	}

	/**
 	* Read the network list objects.
 	*/
	private List<Network> readNetworkList() throws IOException {
		String routine = getClass().getSimpleName() + ".readNetworkList";
		String requestUrl = getServiceRootURI() + "/networks?" + getApiTokenParameter();
		Message.printStatus(2, routine, "Reading network list from: " + requestUrl);
		List<Network> siteList = new ArrayList<>();
		String arrayName = "MNET";
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				siteList.add((Network)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Network.class));
			}
		}
		return siteList;
	}

	/**
 	* Read the NwsCwa objects from the data file.
 	*/
	private List<NwsCwa> readNwsCwaList() throws IOException {
		String routine = getClass().getSimpleName() + ".readNwsCwa";

		// Read the data file.
		DataTable table = null;
		BufferedReader fileReader = null;
		List<NwsCwa> nwsCwaList = new ArrayList<>();
		try {
			try {
				// Get a reader based on class path resource.
				String resourcePath = this.RESOURCE_PATH + "/synoptic-nws-cwa.csv";
				fileReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream( resourcePath)));
				Message.printStatus(2, routine, "Reading NWS CWA list resource file: " + resourcePath );
				// Read the CSV into a data table:
				// - can use the defaults
				PropList props = new PropList("NWS-CWA");
				props.set ( "Delimiter", "," );
				props.set ( "CommentLineIndicator=#" );	// Skip comment lines.
				props.set ( "TrimInput=True" ); // Trim strings before parsing.
				props.set ( "TrimStrings=True" ); // Trim strings after parsing.
				props.setUsingObject("BufferedReader", fileReader);
				table = DataTable.parseFile(resourcePath, props);
				table.setTableID("NWS-CWA");
				// Sort by the "FILTER_CHOICE" columns.
				String [] sortColumns = { "FILTER_CHOICE_TO_SORT" };
				table.sortTable(sortColumns, null);
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading NWS CWA list resource file." );
				Message.printWarning(3, routine, e );
			}

			if ( table != null ) {
				// Get the column number for the FILTER_CHOICE column, which is used for the input filter:
				// - the table has been previously sorted
				int filterChoiceCol = -1;
				int cwaCol = -1;
				try {
					filterChoiceCol = table.getFieldIndex("FILTER_CHOICE");
				}
				catch ( Exception e ) {
					Message.printWarning(3, routine, "NWS CWA file does not have the column FILTER_CHOICE" );
				}
				try {
					cwaCol = table.getFieldIndex("CWA");
				}
				catch ( Exception e ) {
					Message.printWarning(3, routine, "NWS CWA file does not have the column CWA" );
				}
				if ( filterChoiceCol >= 0 ) {
					for ( int irec = 0; irec < table.getNumberOfRecords(); irec++ ) {
						try {
							// Create an object and set the filter choice.
							NwsCwa nwsCwa = new NwsCwa();
							nwsCwa.setFilterChoice((String)table.getFieldValue(irec, filterChoiceCol));
							nwsCwa.setCwa((String)table.getFieldValue(irec, cwaCol));
							nwsCwaList.add(nwsCwa);
						}
						catch ( Exception e ) {
							// For now ignore.
						}
					}
				}
			}
		}
		finally {
			if ( fileReader != null ) {
				fileReader.close();
			}
		}
		return nwsCwaList;
	}

	/**
 	* Read the state list:
 	* @return the list of states.
 	*/
	private List<State> readStateList() {
		List<State> stateList = new ArrayList<>();

		// States are hard-coded:
		// - TODO smalers 2023-03-20 could put this in a file and distribute in the jar
		stateList.add(new State("AZ", "Arizona") );
		stateList.add(new State("AL", "Alabama") );
		stateList.add(new State("AK", "Alaska") );
		stateList.add(new State("AZ", "Arizona") );
		stateList.add(new State("AR", "Arkansas") );
		stateList.add(new State("CA", "California") );
		stateList.add(new State("CO", "Colorado") );
		stateList.add(new State("CT", "Connecticut") );
		stateList.add(new State("DE", "Delaware") );
		stateList.add(new State("DC", "District of Columbia") );
		stateList.add(new State("FL", "Florida") );
		stateList.add(new State("GA", "Georgia") );
		stateList.add(new State("HI", "Hawaii") );
		stateList.add(new State("ID", "Idaho") );
		stateList.add(new State("IL", "Illinois") );
		stateList.add(new State("IN", "Indiana") );
		stateList.add(new State("IA", "Iowa") );
		stateList.add(new State("KS", "Kansas") );
		stateList.add(new State("KY", "Kentucky") );
		stateList.add(new State("LA", "Louisiana") );
		stateList.add(new State("ME", "Maine") );
		stateList.add(new State("MD", "Maryland") );
		stateList.add(new State("MA", "Massachusetts") );
		stateList.add(new State("MI", "Michigan") );
		stateList.add(new State("MN", "Minnesota") );
		stateList.add(new State("MS", "Mississippi") );
		stateList.add(new State("MO", "Missouri") );
		stateList.add(new State("MT", "Montana") );
		stateList.add(new State("NE", "Nebraska") );
		stateList.add(new State("NV", "Nevada") );
		stateList.add(new State("NH", "New Hampshire") );
		stateList.add(new State("NJ", "New Jersey") );
		stateList.add(new State("NM", "New Mexico") );
		stateList.add(new State("NY", "New York") );
		stateList.add(new State("NC", "North Carolina") );
		stateList.add(new State("ND", "North Dakota") );
		stateList.add(new State("OH", "Ohio") );
		stateList.add(new State("OK", "Oklahoma") );
		stateList.add(new State("OR", "Oregon") );
		stateList.add(new State("PA", "Pennsylvania") );
		stateList.add(new State("RI", "Rhode Island") );
		stateList.add(new State("SC", "South Carolina") );
		stateList.add(new State("SD", "South Dakota") );
		stateList.add(new State("TN", "Tennessee") );
		stateList.add(new State("TX", "Texas") );
		stateList.add(new State("UT", "Utah") );
		stateList.add(new State("VT", "Vermont") );
		stateList.add(new State("VA", "Virginia") );
		stateList.add(new State("WA", "Washington") );
		stateList.add(new State("WV", "West Virginia") );
		stateList.add(new State("WI", "Wisconsin") );
		stateList.add(new State("WY", "Wyoming") );
		return stateList;
	}

    /**
     * Read a single time series given its time series identifier using default read properties.
     * This is typically called by TSID command, which uses default properties.
     * @param tsid time series identifier.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd, boolean readData ) {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
    	try {
    		Message.printStatus(2, routine, "Reading time series \"" + tsid + "\".");
    		return readTimeSeries ( tsid, readStart, readEnd, readData, null );
    	}
    	catch ( Exception e ) {
    		// Throw a RuntimeException since the method interface does not include an exception type.
    		Message.printWarning(2, routine, e);
    		throw new RuntimeException ( e );
    	}
    }

    /**
     * Read a single time series given its time series identifier.
     * @param tsidReq requested time series identifier.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @param readProperties additional properties to control the query:
     * <ul>
     * <li> "IrregularInterval" - irregular interval (e.g., "IrregHour" to use instead of TSID interval,
     *      where the TSID intervals corresponds to the web services.</li>
     * <li> "Read24HourAsDay" - string "false" (default) or "true" indicating whether 24Hour interval time series
     *      should be output as 1Day time series.</li>
     * <li> "ReadDayAs24Hour" - string "false" (default) or "true" indicating whether day interval time series
     *      should be output as 24Hour time series.</li>
     * <li> "Debug" - if true, turn on debug for the query</li>
     * </ul>
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsidReq, DateTime readStart, DateTime readEnd,
    	boolean readData, HashMap<String,Object> readProperties ) throws Exception {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
    	boolean debug = false;
    	if ( Message.isDebugOn ) {
    		debug = true;
    	}

    	// The Synoptic API requires that the start and end are set:
    	// - default to one month of data
    	if ( readStart == null ) {
    		readStart = new DateTime(DateTime.DATE_CURRENT);
    		readStart.addMonth(-1);
    	}
    	if ( readEnd == null ) {
    		// Default to current.
    		readEnd = new DateTime(DateTime.DATE_CURRENT);
    	}

    	// Get the properties of interest:
    	// - corresponds to parameters in the ReadSynoptic command
    	// - TSID command uses the defaults and may result in more exceptions because TSID can only handle general behavior
    	if ( readProperties == null ) {
    		// Create an empty hashmap if necessary to avoid checking for null below.
    		readProperties = new HashMap<>();
    	}
    	String IrregularInterval = null;
    	TimeInterval irregularInterval = null;
    	boolean read24HourAsDay = false;
    	boolean readDayAs24Hour = false;
    	Object object = readProperties.get("IrregularInterval");
    	if ( object != null ) {
    		IrregularInterval = (String)object;
    		irregularInterval = TimeInterval.parseInterval(IrregularInterval);
    	}
    	object = readProperties.get("Read24HourAsDay");
    	if ( object != null ) {
    		String Read24HourAsDay = (String)object;
    		if ( Read24HourAsDay.equalsIgnoreCase("true") ) {
    			read24HourAsDay = true;
    		}
    	}
    	object = readProperties.get("ReadDayAs24Hour");
    	if ( object != null ) {
    		String ReadDayAs24Hour = (String)object;
    		if ( ReadDayAs24Hour.equalsIgnoreCase("true") ) {
    			readDayAs24Hour = true;
    		}
    	}

    	TS ts = null;

    	// Create a time series identifier for the requested TSID:
    	// - the actual output may be set to a different identifier based on the above properties
    	// - also save interval base and multiplier for the original request
    	TSIdent tsidentReq = TSIdent.parseIdentifier(tsidReq);
   		int intervalBaseReq = tsidentReq.getIntervalBase();
   		int intervalMultReq = tsidentReq.getIntervalMult();
   		boolean isRegularIntervalReq = TimeInterval.isRegularInterval(intervalBaseReq);

    	// Up front, check for invalid request and throw exceptions:
   		// - some cases are OK as long as IrregularInterval was specified in ReadSynoptic

    	if ( tsidentReq.getInterval().isEmpty() ) {
    		// Version 1.0.0 of the plugin allowed blank interval in TSID but this is no longer accepted.
   			throw new RuntimeException ( "TSID (" + tsidReq + ") has no interval - cannot read time series." );
    	}
    	else if ( (irregularInterval != null) && !TimeInterval.isRegularInterval(intervalBaseReq)) {
   			throw new RuntimeException ( "TSID (" + tsidReq
   				+ ") is an irregular interval ime series - it is redundant to request IrregularInterval." );
    	}
    	else if ( (intervalBaseReq == TimeInterval.DAY) && (intervalMultReq != 1) && (irregularInterval == null) ) {
   			throw new RuntimeException ( "TSID ( " + tsidReq
   				+ ") reading NDay interval is not supported.  Use ReadSynoptic(IrregularInterval=IrregDay) or IrregHour." );
   		}
    	else if ( readDayAs24Hour && !((intervalBaseReq == TimeInterval.DAY) && (intervalMultReq == 1)) ) {
   			throw new RuntimeException ( "TSID (" + tsidReq + ") requesting reading day as 24 hour but input is not 1Day interval." );
    	}
    	else if ( read24HourAsDay && !((intervalBaseReq == TimeInterval.HOUR) && (intervalMultReq == 24)) ) {
   			throw new RuntimeException ( "TSID (" + tsidReq + ") requesting reading 24 hour as day but input is not 24Hour interval." );
    	}
   		else if ( (intervalBaseReq == TimeInterval.MONTH) && (irregularInterval == null) ) {
   			throw new RuntimeException ( "TSID ( " + tsidReq
   				+ ") reading Month interval is not supported.  Use ReadSynoptic(IrregularInterval=IrregMonth)" );
   		}
   		else if ( (intervalBaseReq == TimeInterval.YEAR) && (irregularInterval == null) ) {
   			throw new RuntimeException ( "TSID ( " + tsidReq +
   				") reading Year interval is not supported.  Use ReadSynoptic(IrregularInterval=IrregYear)" );
   		}

    	// Read the time series catalog for the requested TSID:
    	// - TODO smalers 2023-03-18 may be able to avoid this since the 'timeseries' service also returns STATION,
    	//   but for now do it in two steps

    	String dataTypeReq = null;
    	String dataIntervalReq = null;
    	InputFilter_JPanel ifp = null;
 		// All the matching time series catalog (should be 1 if successful).
 		List<TimeSeriesCatalog> tscatalogList = readTimeSeriesCatalog(tsidReq, dataTypeReq, dataIntervalReq, ifp);
 		// The single matching time series catalog.
 		TimeSeriesCatalog tscatalog = null;
 		if ( tscatalogList.size() == 0 ) {
 			// Did not match the requested time series.
    		throw new RuntimeException ( "Did not match TSID \"" + tsidReq + "\" - cannot read the time series." );
 		}
 		else if ( tscatalogList.size() > 1 ) {
 			// Did not match the requested time series.
    		throw new RuntimeException ( "Matched " + tscatalogList.size() + " TSID \"" + tsidReq +
    			"\" but was expecting 1 match - cannot read the time series." );
 		}
 		else {
 			// Match a single requested time series.
    		Message.printStatus (2,routine, "Matched TSID \"" + tsidReq + "\" - reading the time series." );
 		}
 		// For processing below, operate on the single matching time series catalog.
 		tscatalog = tscatalogList.get(0);

    	try {
    		ts = TSUtil.newTimeSeries(tsidReq.toString(), true);
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}

    	// Set the time series properties.
    	int intervalBase = tsidentReq.getIntervalBase();
    	int intervalMult = tsidentReq.getIntervalMult();
    	try {
    		ts.setIdentifier(tsidReq);
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}

    	// Always set the original period to the catalog information:
    	// - if for some reason the metadata period was not provided, set it below
    	ts.setDate1Original(tscatalog.getSensorStartDateTime());
    	ts.setDate2Original(tscatalog.getSensorEndDateTime());

    	// Set the output period to the requested but only if regular interval:
    	// - the period may be reset below depending on time series interval, interval end adjustments, etc.
    	// - TODO smalers 2023-01-17 may need to do more to handle the case of interval data timestamps being adjusted below
    	// - if irregular interval, the query period eill impact what is returned
    	if ( readStart != null ) {
    		if ( TimeInterval.isRegularInterval(intervalBase) ) {
    			ts.setDate1(readStart);
    		}
    	}
    	if ( readEnd != null ) {
    		if ( TimeInterval.isRegularInterval(intervalBase) ) {
    			ts.setDate2(readEnd);
    		}
    	}

    	// Set standard properties:
    	// - use station name for the description
    	// - data units are set below
		ts.setDescription(tscatalog.getStationName());
		ts.setMissing(Double.NaN);

		// Set the time series properties:
		// - additional properties are set below to help understand adjusted timestamps and offset days
		setTimeSeriesProperties ( ts, tscatalog );

    	if ( readData ) {
    		// Request the time series:
    		// - station ID matches the TSID location
    		// - sensor variable matches TSID main data type
    		// - use 'obstimezone=local' so that output does not need to be converted
    		// - units default to 'english'
    		StringBuilder requestUrl = new StringBuilder(
    			getServiceRootURI() + "/stations/timeseries?" + getApiTokenParameter()
    			+ "&stid=" + tsidentReq.getLocation()
    			+ "&vars=" + tscatalog.getSensorVariable()
    			+ "&obtimezone=local"
    			+ "&units=english");

    			// If the read period was specific, add to the URL.
    			if ( readStart != null ) {
    				requestUrl.append( "&start=" + formatUtcTimeFromLocal(readStart, tscatalog.getStationTimeZone() ) );
    			}
    			if ( readEnd != null ) {
    				requestUrl.append( "&end=" + formatUtcTimeFromLocal(readEnd, tscatalog.getStationTimeZone() ) );
    			}

		  		// The data for the time series will have a format similar to the following:
		        // "OBSERVATIONS": {
		  	    //     "date_time": [
		  	    //       "2015-01-03T00:00:00Z",
		  	    //       "2015-01-03T00:05:00Z",
		  	    //       "2015-01-03T00:10:00Z",
		  	    //       "2015-01-03T00:15:00Z",
		  	    //       "2015-01-03T00:20:00Z"
		  	    //     ],
		  	    //     "air_temp_set_1": [-5.6, -5.6, -6.1, -6.1, -6.7]
		  	    //
		  		//
		  		// Use the rootNode to get the observations node and then decode somewhat manually because,
		  		// once again, the key is the sensor variable and not a generic name.
		  		// If here only one station will have returned.

    			// Request the data.
    			JsonNode rootNode = null;
		  		String arrayName = null;
    			Message.printStatus(2, routine, "Reading time series data using: " );
    			Message.printStatus(2, routine, "  " + requestUrl );
		  		try {
		  			rootNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl.toString(), arrayName);
		  		}
		  		catch ( Exception e ) {
			  		Message.printWarning(3,routine,"Error reading 'timeseries' service (" + e + ").");
			  		Message.printWarning(3,routine,e);
			  		// Rethrow the exception.
			  		throw new RuntimeException ( "Error requesting data from the 'timeseries' service.", e );
		  		}

		  		// Process the 'SUMMARY' to check if the request had a problem.
		  		Summary summary = getSummary(rootNode);
		  		if ( summary == null ) {
			  		String message = "Unable to find 'SUMMARY' in response - cannot evaluate success.";
			  		Message.printWarning(3, routine, message );
		  		}
		  		else if ( summary.isOk() ) {
			  		String message = "Request returned RESPONSE_CODE=" + summary.getResponseCode() + " - OK to continue.";
			  		Message.printStatus(2, routine, "  " + message );
		  		}
		  		else {
			  		String message = "Request returned RESPONSE_CODE=" + summary.getResponseCode() + " - cannot continue.";
			  		Message.printWarning(3, routine, "  " + message );
			  		throw new RuntimeException ( message );
		  		}

		  		// Process the "UNITS" map:
		  		// - not in 'metadata' (is in 'latest' and 'timeseries')
		  		List<Units> unitsList = new ArrayList<>();
		  		try {
			  		JsonNode unitsNode = rootNode.get("UNITS");
			  		if ( unitsNode != null ) {
				  		// Iterate through the map.
				  		Iterator<Entry<String, JsonNode>> mapNodes = unitsNode.fields();
				  		while ( mapNodes.hasNext() ) {
					  		Map.Entry<String, JsonNode> unitNode = mapNodes.next();
					  		String variable = unitNode.getKey();
					  		JsonNode value = (JsonNode)unitNode.getValue();
					  		String units = value.asText();
					  		Units latestUnits = new Units(variable,units);
					  		unitsList.add(latestUnits);
				  		}
				  		Message.printStatus(2, routine, "Read " + unitsList.size() + " units from 'timeseries'.");
			  		}
		  		}
		  		catch ( Exception e ) {
			  		Message.printWarning(3,routine,"Error reading 'UNITS' from 'timeseries' results (" + e + ").");
			  		Message.printWarning(3,routine,e);
		  		}
		  		// Set the units based on the sensor variable.
		  		for ( Units units : unitsList ) {
		  			if ( units.getVariableName().equalsIgnoreCase(tscatalog.getSensorVariable()) ) {
		  				ts.setDataUnits(units.getUnits());
		  				ts.setDataUnitsOriginal(units.getUnits());
		  			}
		  		}

		  		// Get data out of the object.

		  		JsonNode stationArrayNode = rootNode.get("STATION");
		  		int dataCount = 0;
		  		if ( stationArrayNode != null ) {
		  			if ( stationArrayNode.size() == 0 ) {
			  			Message.printWarning(3, routine, "  Read 0 items ('STATION' JSON node not read).");
		  			}
		  			else if ( stationArrayNode.size() > 1 ) {
			  			Message.printWarning(3, routine, "  Read " + stationArrayNode.size() + "  items ('STATION' has too many elements).");
		  			}
		  			else {
		  				// Have one station, as expected:
		  				// - one station but iterate through the array
		  				Message.printStatus(2, routine, "  Read 1 STATION from 'metadata' service - continuing to read observations.");
		  				Message.printStatus(2, routine, "  Looping through " + stationArrayNode.size() + " stations in STATION.");
			  			for ( int i = 0; i < stationArrayNode.size(); i++ ) {
				  			JsonNode stationNode = stationArrayNode.get(i);
				  			// Get the "OBSERVATIONS" node.
				  			JsonNode observationsNode = stationNode.get("OBSERVATIONS");
				  			if ( observationsNode != null ) {
				  				JsonNode valuesNode = observationsNode.get(
				  					getValueArrayNameForSensorVariable(tscatalog.getSensorVariableOut()));
				  				if ( valuesNode != null ) {
				  					// Get the object matching the sensor variable, with _1, etc.
				  					// Decode the observations 'date_time' array and corresponding values.
				  					StationObservations stationObservations = (StationObservations)
				  						JacksonToolkit.getInstance().treeToValue(observationsNode, StationObservations.class);
				  					String [] dateTimeArray = stationObservations.getDateTimeArray();
				  					Message.printStatus(2, routine, "  'date_time' array has " + dateTimeArray.length + " items.");
				  					// Decode the values array, for example:
				  					//   "air_temp_set_1": [-5.6, -5.6, -6.1, -6.1, -6.7]
				  					// If have data, loop through and transfer the data using the iterator below.
				  					Iterator<JsonNode> valueNodesIterator = valuesNode.elements();
				  					if ( dateTimeArray != null ) {
				  						String dateTimeString = null;
			  							Double value = null;
			  							DateTime dateTime = null;
				  						for ( int iVal = 0; iVal < dateTimeArray.length; iVal++ ) {
				  							dateTimeString = dateTimeArray[iVal];
				  							// Convert the string to a DateTime.
				  							try {
				  								dateTime = DateTime.parse(dateTimeString);
				  							}
				  							catch ( Exception e ) {
				  								Message.printWarning(3, routine, "Error parsing observation date/tme: \"" + dateTimeString + "\"");
				  								// Skip the value.
				  								continue;
				  							}
				  							if ( valueNodesIterator.hasNext() ) {
				  									JsonNode valueNode = valueNodesIterator.next();
				  								// Value is a Double.
				  								if ( valueNode.isNull() ) {
				  									value = Double.NaN;
				  								}
				  								else {
				  									value = valueNode.asDouble();
				  								}
				  							}
				  							else {
				  								// No more values.
				  								break;
				  							}
				  							// Set the value in the time series:
				  							// - there are no flags
				  							// - TODO smalers 2023-03-19 may need to add logic to covert to daily, etc.
				  							if ( debug ) {
				  								Message.printStatus(2, routine, "  Setting data " + dateTime + " " + value);
				  							}
				  							ts.setDataValue(dateTime, value);
				  							++dataCount;
				  						}
				  						Message.printStatus(2, routine, "  Read " + dataCount + " data values.");
				  					}
				  				}
				  				else {
			  						Message.printStatus(2, routine, "  Did not find " + tscatalog.getSensorVariableOut()
			  							+ " array in STATION.OBSERVATIONS.");
			  					}
				  			}
			  				else {
			  					Message.printStatus(2, routine, "  Did not find 'OBSERVATIONS' array in STATION[" + i + "].");
			  				}
			  			}
		  			}
		  		}

		  		// Loop through the stations.

		  		// TODO smalers 2023-03-20 could enable the following like KiWIS plugin if it seems relevent.

     		/*
    		List<TimeSeriesValue> timeSeriesValueList = readTimeSeriesValues (
    			kiwisTsid, kiwisTsPath, readStart, readEnd, readProperties,
    			valuesUrl );

    		String dataFlag = null;
    		DateTime dateTime = null;
    		double value;
    		int interpolationTypeNum = -1;
    		InterpolationType interpolationType = null;
    		String valueString;
    		int duration = -1;
    		List<QualityCode> qualityCodeList = this.getQualityCodes(false);
    		int badDateTimeCount = 0;
    		int badValueCount = 0;
    		int badInterpolationTypeCount = 0;
    		int notInsertedCount = 0;
    		// Count of how many values are adjusted from beginning to end of interval.
    		int timeAdjustCount = 0;
    		// Count of how many daily values have non-zero hour.
    		int dayNonZeroHourCount = 0;
   			int valueErrorCount = 0;
    		if ( timeSeriesValueList.size() > 0 ) {
    			// Set the period based on data from the first and last values:
    			// - this values may be adjusted below
    			ts.setDate1(DateTime.parse(timeSeriesValueList.get(0).getTimestamp()));
    			ts.setDate2(DateTime.parse(timeSeriesValueList.get(timeSeriesValueList.size() - 1).getTimestamp()));

    			// Check the time series values up front to see if any date/times will be adjusted from
    			// beginning to end of the interval.  TSTool uses interval-ending values.
    			// Must do this up front in order to adjust the time series period for regular interval time series
    			// so that all queried values will be saved in the time series.
    			interpolationType = InterpolationType.UNKNOWN;
    			for ( TimeSeriesValue tsValue : timeSeriesValueList ) {
    				if ( tsValue.getInterpolationType().getTimestampPos() == -1 ) {
    					// Save the interpolation type that triggered the adjustment.
    					interpolationType = tsValue.getInterpolationType();
    					++timeAdjustCount;
    					// Can break since only need to know if one time is adjusted.
    					break;
    				}
    			}

    			// If any times will be adjusted from beginning to end of the interval, also adjust the periods:
    			// - only need to adjust if regular interval time series (KiWIS has non-blank ts_spacing)
    			// - the data value timestamps are shifted similarly before adding data to the time series
    			if ( timeAdjustCount > 0 ) {
    				if ( isRegularIntervalReq ) {
    					// Regular interval time series.
    					DateTime date1 = ts.getDate1();
    					adjustTimeForInterpolationType(intervalBaseReq, intervalMultReq, date1, interpolationType);
    					ts.setDate1(date1);
    					DateTime date2 = ts.getDate2();
    					adjustTimeForInterpolationType(intervalBaseReq, intervalMultReq, date2, interpolationType);
    					ts.setDate2(date2);
    					DateTime date1Original = ts.getDate1Original();
    					if ( date1Original != null ) {
    						adjustTimeForInterpolationType(intervalBaseReq, intervalMultReq, date1Original, interpolationType);
    						ts.setDate1Original(date1Original);
    					}
    					DateTime date2Original = ts.getDate2Original();
    					if ( date2Original != null ) {
    						adjustTimeForInterpolationType(intervalBaseReq, intervalMultReq, date2Original, interpolationType);
    						ts.setDate2Original(date2Original);
    					}
    				}
    			}

    			// The following if blocks match the logic inside the loop where values are transfered.

    			if ( isRegularIntervalReq && (intervalBaseReq == TimeInterval.DAY) && (intervalMultReq == 1) ) {
    				if ( readDayAs24Hour ) {
    					// Since KiWIS timestamp already includes hour, don't need to do anything,
    					// other than the output time series needs to have its interval changed above (above).
    					// Parsed date/time will already include the correct hour so just need to set the precision.
   						DateTime date1 = ts.getDate1();
   						date1.setPrecision(DateTime.PRECISION_HOUR);
    					ts.setDate1(date1);
   						DateTime date2 = ts.getDate2();
   						date2.setPrecision(DateTime.PRECISION_HOUR);
    					ts.setDate2(date2);
    					Message.printStatus(2,routine,"After adjusting period precision for ReadDayAs24Hour, date1="
    						+ ts.getDate1() + " date2=" + ts.getDate2());
    					// Original period is probably null but try to set.
   						DateTime date1Original = ts.getDate1Original();
   						if ( date1Original != null ) {
   							date1Original.setPrecision(DateTime.PRECISION_HOUR);
    						ts.setDate1Original(date1Original);
   						}
   						DateTime date2Original = ts.getDate2Original();
   						if ( date2Original != null ) {
   							date2Original.setPrecision(DateTime.PRECISION_HOUR);
    						ts.setDate2Original(date2Original);
   						}
    				}
    				else {
    					// By default, 1Day time series are shifted.
    					// - KiWIS timestamp is at midnight (hour zero of next day)
    					// - adjust the KiWIS timestamp to previous day (time will be discarded).
    					// - do not do the adjustment if irregular interval other than if IrregDay is requested
    					// - TODO smalers 2023-01-18 will need to handle month and year when enabled
    					if ( (irregularInterval == null) ||
    						((irregularInterval != null) && (irregularInterval.getIrregularIntervalPrecision() == TimeInterval.DAY)) ) {
    						DateTime date1 = ts.getDate1();
   							date1.setPrecision(DateTime.PRECISION_DAY);
    						date1.addDay(-1);
    						date1.setHour(0); // Should not be used.
    						ts.setDate1(date1);
   							DateTime date2 = ts.getDate2();
   							date2.setPrecision(DateTime.PRECISION_DAY);
    						date2.addDay(-1);
    						date2.setHour(0); // Should not be used.
    						ts.setDate2(date2);
    						Message.printStatus(2,routine,"After adjusting period precision for default day handling, date1="
    							+ ts.getDate1() + " date2=" + ts.getDate2());
    						// Original period is probably null but try to set.
   							DateTime date1Original = ts.getDate1Original();
   							if ( date1Original != null ) {
   								date1Original.setPrecision(DateTime.PRECISION_DAY);
   								date1Original.addDay(-1);
    							date1Original.setHour(0); // Should not be used.
    							ts.setDate1Original(date1Original);
   							}
   							DateTime date2Original = ts.getDate2Original();
   							if ( date2Original != null ) {
   								date2Original.setPrecision(DateTime.PRECISION_DAY);
   								date2Original.addDay(-1);
    							date2Original.setHour(0); // Should not be used.
    							ts.setDate2Original(date2Original);
   							}
    					}
    				}
    			}
    			else if ( isRegularIntervalReq && (intervalBaseReq == TimeInterval.HOUR) && (intervalMultReq == 24) && read24HourAsDay ) {
    				// 24Hour in KiWIS but want 1Day output:
    				// - adjust the KiWIS timestamp to previous day (time will be discarded).
   					DateTime date1 = ts.getDate1();
   					date1.setPrecision(DateTime.PRECISION_DAY);
    				date1.addDay(-1);
    				date1.setHour(0); // Should not be used.
    				ts.setDate1(date1);
   					DateTime date2 = ts.getDate2();
   					date2.setPrecision(DateTime.PRECISION_DAY);
    				date2.addDay(-1);
    				date2.setHour(0); // Should not be used.
    				ts.setDate2(date2);
    				Message.printStatus(2,routine,"After adjusting period precision for Read24HourAsDay, date1="
    					+ ts.getDate1() + " date2=" + ts.getDate2());
    				// Original period is probably null but try to set.
   					DateTime date1Original = ts.getDate1Original();
   					date1Original.setPrecision(DateTime.PRECISION_DAY);
   					if ( date1Original != null ) {
   						date1Original.addDay(-1);
    					date1Original.setHour(0); // Should not be used.
    					ts.setDate1Original(date1Original);
   					}
   					DateTime date2Original = ts.getDate2Original();
   					date2Original.setPrecision(DateTime.PRECISION_DAY);
   					if ( date2Original != null ) {
   						date2Original.addDay(-1);
    					date2Original.setHour(0); // Should not be used.
    					ts.setDate2Original(date2Original);
   					}
    			}

    			if ( irregularInterval != null ) {
    				// Adjust the precision on the period date/times:
    				// - copies of the dates are returned so have to reset
   					DateTime date1 = ts.getDate1();
    				date1.setPrecision(irregularInterval.getIrregularIntervalPrecision());
    				ts.setDate1(date1);
   					DateTime date2 = ts.getDate2();
    				date2.setPrecision(irregularInterval.getIrregularIntervalPrecision());
    				ts.setDate2(date2);
    				Message.printStatus(2,routine,"After adjusting period precision for irregular interval ("
    					+ irregularInterval + "), date1=" + ts.getDate1() + " date2=" + ts.getDate2());
    				// Original period is probably null but try to set.
   					DateTime date1Original = ts.getDate1Original();
   					if ( date1Original != null ) {
   						date1Original.setPrecision(irregularInterval.getIrregularIntervalPrecision());
   						ts.setDate1Original(date1Original);
   					}
   					DateTime date2Original = ts.getDate2Original();
   					if ( date2Original != null ) {
   						date2Original.setPrecision(irregularInterval.getIrregularIntervalPrecision());
    					ts.setDate2Original(date2Original);
   					}
    			}

    			// If reading day interval, convert midnight hour 0 of the next day to day precision of the previous day,
    			// for example:
    			//   2023-01-01 00:00:00 -> 2022-12-31
    			// Time series that use timestamps with time intervals can remain as is.

    			// Allocate the time series data array:
    			// - do this after adjusting the period for timestamps
    			// - irregular interval does not allocate an array up front
    			ts.allocateDataSpace();

    			// Transfer the TimeSeriesValue list to the TS data.

    			Message.printStatus(2,routine, "Transferring " + timeSeriesValueList.size() + " time series values.");
    			timeAdjustCount = 0;
    			for ( TimeSeriesValue tsValue : timeSeriesValueList ) {
    				if ( Message.isDebugOn ) {
    					Message.printStatus(2,routine, "  Processing timestamp=" + tsValue.getTimestamp()
    						+ " value=" + tsValue.getValue() + " quality code=" + tsValue.getQualityCode()
    						+ " interpolation type number=" + tsValue.getInterpolationTypeNum()
    						+ " interpolation type=" + tsValue.getInterpolationType() );
    				}
    				try {
    					try {
    						dateTime = DateTime.parse(tsValue.getTimestamp());
    					}
    					catch ( Exception e ) {
    						Message.printWarning(3, routine, "  Error parsing date/time: " + tsValue.getTimestamp());
    						++badDateTimeCount;
    						continue;
    					}
    					valueString = tsValue.getValue();
    					if ( (valueString != null) && !valueString.isEmpty() ) {
    						try {
    							value = Double.parseDouble(tsValue.getValue());
    						}
    						catch ( NumberFormatException e ) {
    							Message.printWarning(3, routine, "  Error parsing " + tsValue.getTimestamp() + " data value: " + tsValue.getValue());
    							++badValueCount;
    							continue;
    						}
    						// Get the interpolation type enumeration.
    						interpolationType = tsValue.getInterpolationType();
    						if ( interpolationType == InterpolationType.UNKNOWN ) {
    							Message.printWarning(3, routine, "  Unknown interpolation type " + interpolationTypeNum
    								+ " at " + tsValue.getTimestamp() + " - skipping value." );
    							++badInterpolationTypeCount;
    							continue;
    						}
    						// Adjust the date/time based on the interpolation type:
    						// - only need to do this for regular interval time series
    						// - only values that have timestamp at the beginning of an interval are adjusted
    						// - keep a count that is added as a time series property
    						if ( isRegularIntervalReq ) {
    							timeAdjustCount += adjustTimeForInterpolationType(intervalBaseReq, intervalMultReq, dateTime, interpolationType);
    						}

    						// Look up the data flag from the quality code integer.
    						dataFlag = lookupQualityCode(qualityCodeList, tsValue.getQualityCode());

    						// Also check daily interval time series:
    						// - if the hour is not zero, count and add as a property later
    						// - time zone is ignored so -0700, -0600, etc. does not come into play
    						// - if the count is non-zero, generate an exception because need to handle as 24Hour or irregular
    						if ( isRegularIntervalReq && (intervalBaseReq == TimeInterval.DAY) && (intervalMultReq == 1) ) {
    							if ( dateTime.getHour() != 0 ) {
    								// Any day interval values with non-zero hour will result in an exception because TSTool does
    								// not have a clean way to handle, for example, 7AM to 7AM time series.
    								// The IrregularInterval=IrregDay parameter should be specified and uses have to deal with the data.
    								++dayNonZeroHourCount;
    							}
    							if ( readDayAs24Hour ) {
    								// Since KiWIS timestamp already includes hour, don't need to do anything,
    								// other than the output time series needs to have its interval changed above (above).
    								dateTime.setPrecision(DateTime.PRECISION_HOUR);
    							}
    							else {
    								// By default, 1Day time series are shifted.
    								// - KiWIS timestamp is at midnight (hour zero of next day)
    								// - adjust the KiWIS timestamp to previous day (time will be discarded).
    								// - do not do the adjustment if irregular interval other than if IrregDay is requested
    								// - TODO smalers 2023-01-18 will need to handle month and year when enabled
    								if ( (irregularInterval == null) ||
    									((irregularInterval != null) && (irregularInterval.getIrregularIntervalPrecision() == TimeInterval.DAY)) ) {
    									dateTime.addDay(-1);
    									dateTime.setHour(0); // Should not be used.
    									dateTime.setPrecision(DateTime.PRECISION_DAY);
    								}
    							}
    						}
    						else if ( isRegularIntervalReq && (intervalBaseReq == TimeInterval.HOUR) && (intervalMultReq == 24) && read24HourAsDay ) {
    							// 24Hour in KiWIS but want 1Day output:
    							// - adjustment will not occur if IrregularInterval was specified
    							// - adjust the KiWIS timestamp to previous day (time will be discarded).
    							dateTime.addDay(-1);
    							dateTime.setHour(0); // Should not be used.
    							dateTime.setPrecision(DateTime.PRECISION_DAY);
    						}

    						if ( irregularInterval != null ) {
    							// Irregular interval output was requested:
    							// - don't need to do adjustments below for day and 24Hour
    							// - set the precision based on what was requested
    							dateTime.setPrecision(irregularInterval.getIrregularIntervalPrecision());
    						}

    						// Set the data value in the time series:
    						// - the date/time will be copied if necessary and the precision set to be consistent with the time series
    						if ( Message.isDebugOn ) {
    							Message.printStatus(2, routine, "  Setting " + dateTime + " value=" + value
    								+ " flag=\"" + dataFlag + "\" for interpolationType=" + interpolationType );
    						}
    						if ( ts.setDataValue(dateTime, value, dataFlag, duration) == 0 ) {
    							// Track points that are not inserted because may be an issue with the period due to
    							// adjusted date/times not aligning with the allocated period.
    							++notInsertedCount;
    						}
    					}
    				}
    				catch ( Throwable e ) {
    					// Catch a Throwable:
    					// - Exception may not be general enough
    					// - if the plugin code and TSTool code are incompatible, may get unexpected errors
						++valueErrorCount;
						Message.printWarning(3, routine, "  Error processing value (" + e + ")." );
						if ( valueErrorCount <= 50 ) {
							Message.printWarning(3, routine, e );
						}
    				}
    			}
    			if ( badDateTimeCount > 0 ) {
    				//problems.add("Time series had " + badDateTimeCount + " bad timestamps.  See the log file.");
    				String message = "  Time series had " + badDateTimeCount + " bad timestamps.  See the log file.";
    				Message.printWarning(3,routine,message);
    				throw new Exception (message);
    			}
    			if ( badValueCount > 0 ) {
    				//problems.add("Time series had " + badValueCount + " bad data values.  See the log file.");
    				String message = "  Time series had " + badValueCount + " bad data values.  See the log file.";
    				Message.printWarning(3,routine,message);
    				throw new Exception(message);
    			}
    			if ( badInterpolationTypeCount > 0 ) {
    				String message = "  Time series had " + badInterpolationTypeCount + " bad interpolation types.  See the log file.";
    				//problems.add("Time series had " + badInterpolationTypeCount + " bad interpolation types.  See the log file.");
    				Message.printWarning(3,routine,message);
    				throw new Exception (message);
    			}
    			if ( valueErrorCount > 0 ) {
    				String message = "  Time series had " + valueErrorCount + " errors setting values.  See the log file.";
    				//problems.add("Time series had " + badDateTimeCount + " bad timestamps.  See the log file.");
    				Message.printWarning(3,routine,message);
    			}
    		}
    		*/

    		// Set additional time series properties to help understand the data.
    		/*
    		ts.setProperty("ts.TimestampsAdjustedToIntervalEndCount", new Integer(timeAdjustCount));
    		ts.setProperty("ts.DayNonZeroHourCount", new Integer(dayNonZeroHourCount));
    		ts.setProperty("ts.NotInsertedCount", new Integer(notInsertedCount));
    		ts.setProperty("ts.GetTimeSeriesValuesUrl", valuesUrl.toString());
    		ts.setProperty("ts.SetDataValueErrorCount", new Integer(valueErrorCount));
    		*/
    	}

    	return ts;
    }

	/**
	 * Read time series catalog, which uses the "/getTimeseriesList" web service query.
	 * @param tsid requested time series identifier, called from readTimeSeries() to get metadata,
	 *        if null use the filters to read 1+ time series catalog
	 * @param dataTypeReq Requested data type (e.g., "DischargeRiver") or "*" to read all data types,
	 *        or null to use default of "*".
	 * @param dataIntervalReq Requested data interval (e.g., "IrregSecond") or "*" to read all intervals,
	 *        or null to use default of "*".
	 * @param ifp input filter panel with "where" conditions
	 */
	public List<TimeSeriesCatalog> readTimeSeriesCatalog ( String tsid, String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
		String routine = getClass().getSimpleName() + ".readTimeSeriesCatalog";

		// Note that when requesting additional fields with 'returnfields', aLL fields to be returned must be specified,
		// not just additional fields above the default.
		StringBuilder requestUrl = null;
		String requestUrlString = null;

		TSIdent tsident = null;
		// The following are checked below to know when the data type contains a _1, etc.
		String tsidDataTypeReq = null;
		String tsidDataSubTypeReq = null;
		Message.printStatus(2,routine,"Reading time series catalog using:" );
		Message.printStatus(2,routine,"  tsid=\"" + tsid + "\"");
		Message.printStatus(2,routine,"  dataTypeReq=\"" + dataTypeReq + "\"" );
		Message.printStatus(2,routine,"  dataIntervalReq=\"" + dataIntervalReq + "\"");
		if ( ifp == null ) {
			Message.printStatus(2,routine,"  ifp=null");
		}
		else {
			Message.printStatus(2,routine,"  ifp=not null");
		}
		if ( (tsid != null) && !tsid.isEmpty() ) {
			// Form the web service request based on the TSID parts.
			try {
				tsident = TSIdent.parseIdentifier(tsid);
			}
			catch ( Exception e ) {
				throw new RuntimeException("Error parsing the requested time series identifier \"" + tsid + "\"");
			}
			requestUrl = new StringBuilder(
				getServiceRootURI() + "/stations/metadata?" + getApiTokenParameter() + "&complete=1&sensorvars=1");
			// Request the specific station.
			requestUrl.append("&stid=" + tsident.getLocation());
			// Request the main variable:
			// - data type may include the main sensor variable and the numbered variable, separated by a dash
			String dataType = tsident.getType();
			if ( dataType.indexOf("-") > 0 ) {
				// Need to use the first part.
				String [] parts = dataType.split("-");
				dataType = parts[0].trim();
				tsidDataTypeReq = parts[0].trim();
				tsidDataSubTypeReq = parts[1].trim();
			}
			else {
				// Only the main data type is requested.
				tsidDataTypeReq = dataType;
				tsidDataSubTypeReq = null;
			}
			requestUrl.append("&var=" + dataType);
			// Workaround to fix the network issue.
			requestUrlString = fixNetworkRequest ( requestUrl.toString() );
			Message.printStatus(2, routine, "Reading 1 station time series metadata using:" );
			Message.printStatus(2, routine, "  " + requestUrlString);
		}
		else {
			// Reading 1+ time series using the provided filter parameters.
			requestUrl = new StringBuilder(
				getServiceRootURI() + "/stations/metadata?" + getApiTokenParameter() + "&complete=1&sensorvars=1");

			// Add filters for the data type and time step.

			if ( (dataTypeReq != null) && !dataTypeReq.isEmpty() && !dataTypeReq.equals("*") ) {
				try {
					//requestUrl.append ( "&var=" + URLEncoder.encode(dataTypeReq,StandardCharsets.UTF_8.toString()) );
					requestUrl.append ( "&var=" + dataTypeReq );
				}
				catch ( Exception e ) {
					// TODO smalers 2023-01-01 should not happen.
				}
			}

			if ( (dataIntervalReq != null) && !dataIntervalReq.isEmpty() && !dataIntervalReq.equals("*") ) {
				// Synoptic always returns 5Minute?
			}

			// Add query parameters based on the input filter:
			// - this includes list type parameters and specific parameters to match database values
			int numFilterWheres = 0; // Number of filter where clauses that are added.
			if ( ifp != null ) {
	        	int nfg = ifp.getNumFilterGroups ();
	        	InputFilter filter;
	        	for ( int ifg = 0; ifg < nfg; ifg++ ) {
	            	filter = ifp.getInputFilter ( ifg );
	            	//Message.printStatus(2, routine, "IFP whereLabel =\"" + whereLabel + "\"");
	            	boolean special = false; // TODO smalers 2022-12-26 might add special filters.
	            	if ( special ) {
	            	}
	            	else {
	            		// Add the query parameter to the URL.
				    	filter = ifp.getInputFilter(ifg);
				    	String queryClause = WebUtil.getQueryClauseFromInputFilter(filter,ifp.getOperator(ifg));
				    	if ( Message.isDebugOn ) {
				    		Message.printStatus(2,routine,"Filter group " + ifg + " where is: \"" + queryClause + "\"");
				    	}
				    	if ( queryClause != null ) {
				    		requestUrl.append("&" + queryClause);
				    		++numFilterWheres;
				    	}
	            	}
	        	}
	        }
			// Workaround to fix the network issue.
			requestUrlString = fixNetworkRequest ( requestUrl.toString() );
			Message.printStatus(2, routine, "Reading 1+ station time series metadata using:" );
			Message.printStatus(2, routine, "  " + requestUrlString);
		}

		List<MetadataStation> metadataStationList = new ArrayList<>();
		JsonNode jsonNode = null;
		JsonNode rootNode = null;

		// Request the data.
		String arrayName = null;
		try {
			rootNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrlString, arrayName);
		}
		catch ( Exception e ) {
			String message = "Error reading 'metadata' service (" + e + ").";
			Message.printWarning(3,routine,"  " + message);
			Message.printWarning(3,routine,e);
			throw new RuntimeException ( message, e);
		}

		// Process the 'SUMMARY' to check if the request had a problem.
		Summary summary = getSummary(rootNode);
		if ( summary == null ) {
			String message = "Unable to find 'SUMMARY' in response - cannot evaluate success.";
			Message.printWarning(3, routine, message );
		}
		else if ( summary.isOk() ) {
	  		String message = "Request returned RESPONSE_CODE=" + summary.getResponseCode() + " - OK to continue.";
	  		Message.printStatus(2, routine, "  " + message );
  		}
  		else {
			String message = "Request returned RESPONSE_CODE=" + summary.getResponseCode() + " - cannot continue.";
			Message.printWarning(3, routine, "  " + message );
			throw new RuntimeException ( message );
		}

		// Process the "UNITS" map:
		// - not in 'metadata' (is in 'latest' and 'timeseries')?
		arrayName = "UNITS";
		List<Units> latestUnitsList = new ArrayList<>();
		try {
			jsonNode = rootNode.get(arrayName);
			if ( jsonNode != null ) {
				// Iterate through the map.
				Iterator<Entry<String, JsonNode>> mapNodes = jsonNode.fields();
				while ( mapNodes.hasNext() ) {
					Map.Entry<String, JsonNode> unitNode = mapNodes.next();
					String variable = unitNode.getKey();
					JsonNode value = (JsonNode)unitNode.getValue();
					String units = value.asText();
					Units latestUnits = new Units(variable,units);
					latestUnitsList.add(latestUnits);
				}
				Message.printStatus(2, routine, "Read " + latestUnitsList.size() + " units from 'metadata'.");
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,"Error reading 'UNITS' from 'latest' results (" + e + ").");
			Message.printWarning(3,routine,e);
		}

		// Process the "STATION" array.
		arrayName = "STATION";
		try {
			// 'get' will return null if not found.
			jsonNode = rootNode.get(arrayName);
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,"Error reading 'metadata' service (" + e + ").");
			Message.printWarning(3,routine,e);
		}
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			Message.printStatus(2, routine, "  Read " + jsonNode.size() + " stations from 'metadata' service.");
			for ( int i = 0; i < jsonNode.size(); i++ ) {
				metadataStationList.add((MetadataStation)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), MetadataStation.class));
			}
			Message.printStatus(2, routine, "  Created " + metadataStationList.size() + " stations from 'metadata' service response.");
		}
		else {
			Message.printStatus(2, routine, "  Read 0 items ('STATION' JSON node not read).");
		}

		// Convert the Synoptic MetadataStation objects to TimeSeriesCatalog:
		// - also filter on the data interval, which is not a web service parameter
		boolean doCheckInterval = false;
		if ( (dataIntervalReq != null) && !dataIntervalReq.isEmpty() && !dataIntervalReq.equals("*") ) {
			doCheckInterval = true;
		}
		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();
		String dataInterval;

		// Loop through the 'MetadataStation' instances and create corresponding TimeSeriesCatalog entries.
		for ( MetadataStation metadataStation : metadataStationList ) {
			// TODO smalers 2023-03-16 need to get the data interval from the variable?
			dataInterval = "IrregSecond";
			if ( doCheckInterval ) {
				if ( !dataIntervalReq.equals(dataInterval) ) {
					continue;
				}
			}

			// Matched the filters so continue adding.

			// Loop through the sensor variables (SENSOR_VARIABLES) for the station.
			//
		    //  "SENSOR_VARIABLES": {
		    //    "wind_speed": {
		    //      "wind_speed_value_1": {
		    //        "period_of_record": {
		    //          "start": "",
		    //          "end": ""
		    //        }
		    //      }
		    //    },
		    //    "air_temp": {
		    //      "air_temp_value_1": {
		    //        "period_of_record": {
		    //          "start": "",
		    //          "end": ""
		    //        }
		    //      }
		    //    },
			//
			// The following map corresponds to the SENSOR_VARIABLES key.
			Map<String, ?> SENSOR_VARIABLES_Map = metadataStation.getSensorVariablesMap();
			// Used to break out of multiple levels of for loops.
			boolean doBreak = false;
			if ( SENSOR_VARIABLES_Map != null ) {
				for ( Map.Entry<String, ?> sensorVariableEntry : metadataStation.getSensorVariablesMap().entrySet() ) {
					// Get the data for the sensor variable:
					// - key is the variable name (e.g., "wind_speed" in the above example)
					// - value is the map under the name
					String sensorVariableName = sensorVariableEntry.getKey();
					@SuppressWarnings("unchecked")
					Map<String,?> sensorVariableOutMap = (Map<String,?>)sensorVariableEntry.getValue();
					if ( sensorVariableOutMap.size() == 0 ) {
						// There are no variables (e.g., "wind_speed_value_1") so don't add 'tscatalog'.
					}
					// Loop through the output variable for the variable (e.g., "wind_speed_value_1").
					for ( Map.Entry<String, ?> sensorVariableOutEntry : sensorVariableOutMap.entrySet() ) {
						// Get the data for the sensor variable (output) dataset:
						// - key is the variable name (e.g., "wind_speed_value_1" in the above example)
						// - a 'tscatalog' object corresponds to this inner sensor
						TimeSeriesCatalog tscatalog = new TimeSeriesCatalog();
						String sensorVariableNameOut = sensorVariableOutEntry.getKey();
						String start = "";
						String end = "";
						@SuppressWarnings("unchecked")
						Map<String,?> sensorVariableOutDetailsMap = (Map<String,?>)sensorVariableOutEntry.getValue();
						for ( Map.Entry<String, ?> sensorVariableOutDetailsEntry : sensorVariableOutDetailsMap.entrySet() ) {
							// Get the detailed data for the sensor variable (output details) dataset:
							// - key is the variable name (e.g., "period_of_record" in the above example)
							String sensorVariableOutDetailKey = sensorVariableOutDetailsEntry.getKey();
							if ( sensorVariableOutDetailKey.equalsIgnoreCase("period_of_record") ) {
								// Read the period start and end.
								@SuppressWarnings("unchecked")
								Map<String,?> periodMap = (Map<String,?>)sensorVariableOutDetailsEntry.getValue();
								for ( Map.Entry<String, ?> periodData : periodMap.entrySet() ) {
									String periodKey = periodData.getKey();
									if ( periodKey.equalsIgnoreCase("start") ) {
										start = (String)periodData.getValue();
									}
									else if ( periodKey.equalsIgnoreCase("end") ) {
										end = (String)periodData.getValue();
									}
								}
							}
						}

						// Standard properties expected by TSTool:
						// - data source is set to network below
						if ( sensorVariableOutMap.size() == 1 ) {
							tscatalog.setDataType(sensorVariableName);
						}
						else {
							// For uniqueness the data type is both parts:
							// - may need to evaluate if this is too verbose but should occur infrequently
							tscatalog.setDataType(sensorVariableName + "-" + sensorVariableNameOut);
						}
						tscatalog.setDataInterval(dataInterval);
						/*
						LatestUnits units = LatestUnits.lookupLatestUnitsFromVariable(latestUnitsList, variableName);
						if ( units == null ) {
							tscatalog.setDataUnits("");
						}
						else {
							tscatalog.setDataUnits(units.getUnits());
						}
						*/

						if ( StringUtil.isDouble(metadataStation.getElevation()) ) {
							Double elevation = Double.parseDouble(metadataStation.getElevation());
							tscatalog.setStationElevation(elevation);
						}
						if ( StringUtil.isDouble(metadataStation.getElevDem()) ) {
							Double elevDem = Double.parseDouble(metadataStation.getElevDem());
							tscatalog.setStationElevDem(elevDem);
						}
						tscatalog.setStationId( metadataStation.getStid());
						if ( StringUtil.isDouble(metadataStation.getLatitude()) ) {
							Double latitude = Double.parseDouble(metadataStation.getLatitude());
							tscatalog.setStationLatitude(latitude);
						}
						if ( StringUtil.isDouble(metadataStation.getLongitude()) ) {
							Double longitude = Double.parseDouble(metadataStation.getLongitude());
							tscatalog.setStationLongitude(longitude);
						}
						tscatalog.setStationMnetId(metadataStation.getMnetId());
						Network network = Network.lookupNetworkFromId(this.networkList, metadataStation.getMnetId());
						if ( network != null ) {
							// Replace space with underscore to avoid issues with TSIDs including whitespace.
							tscatalog.setStationMnet(network.getShortName().replace(" ", "_"));
							tscatalog.setDataSource(network.getShortName().replace(" ", "_"));
						}
						tscatalog.setStationQcFlagged(metadataStation.getQcFlagged());
						tscatalog.setStationName(metadataStation.getName());
						tscatalog.setStationState(metadataStation.getState());
						tscatalog.setStationStatus(metadataStation.getStatus());
						tscatalog.setStationTimeZone(metadataStation.getTimeZone());

						// Sensor variable.
						tscatalog.setSensorVariable(sensorVariableName);
						tscatalog.setSensorVariableOut(sensorVariableNameOut);
						tscatalog.setSensorStart(start);
						tscatalog.setSensorEnd(end);

						// Save the catalog in the list.
						if ( (tsid != null) && !tsid.isEmpty() ) {
							// Reading a single time series:
							// - only save if the request is matched and can then break out of the loop
							if ( tsidDataSubTypeReq == null ) {
								// Check the main sensor variable.
								if ( sensorVariableName.equalsIgnoreCase(tsidDataTypeReq) ) {
									// Save the catalog and return it.
									tscatalogList.add(tscatalog);
									doBreak = true;
									break;
								}
							}
							else {
								// Check the main sensor variable and the numbered variable.
								if ( sensorVariableName.equalsIgnoreCase(tsidDataTypeReq) &&
									sensorVariableNameOut.equalsIgnoreCase(tsidDataSubTypeReq)) {
									// Save the catalog and return it.
									tscatalogList.add(tscatalog);
									doBreak = true;
									break;
								}
							}
						}
						else {
							// Reading 1+ catalogs so always add.
							tscatalogList.add(tscatalog);
						}
					}
					if ( doBreak ) {
						break;
					}
				}
			}
			else {
				// No sensor variables map.
				Message.printStatus(2, routine, "No sensor variables map for station \"" + metadataStation.getStid() + "\"");
			}
		}

		return tscatalogList;
	}

    /**
     * Read time series metadata, which results in a query that joins station, station_type, point, point_class, and point_type.
     */
    List<TimeSeriesCatalog> readTimeSeriesMeta ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
    	// Remove note from data type.
	   	int pos = dataTypeReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataTypeReq = dataTypeReq.substring(0, pos);
	   	}
	   	pos = dataIntervalReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataIntervalReq = dataIntervalReq.substring(0, pos).trim();
	   	}
		String tsid = null;
	   	// By default all time series are included in the catalog:
	   	// - the filter panel options can be used to constrain
	    return readTimeSeriesCatalog ( tsid, dataTypeReq, dataIntervalReq, ifp );
	}

	/**
 	* Read the variable list objects from JSON similar to:
 	*
 	*   "VARIABLES": [
 	*     {
 	*       "air_temp": {
 	*         "long_name": "Temperature",
 	*         "unit": "Celsius"
 	*       }
 	*     },
 	* @return the list of variables.
 	*/
	private List<Variable> readVariableList() throws IOException {
		String routine = getClass().getSimpleName() + ".readVariableList";
		String requestUrl = getServiceRootURI() + "/variables?" + getApiTokenParameter();
		Message.printStatus(2, routine, "Reading variable list from: " + requestUrl);
		List<Variable> variableList = new ArrayList<>();
		/*
		String arrayName = null;
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				siteList.add((Site)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Site.class));
			}
		}
		*/

		URL request = null;
		try {
			request = new URL(requestUrl);
			ObjectMapper mapper = JacksonToolkit.getInstance().getObjectMapper();
			// The following gets the JSON from the URL.
			JsonNode rootNode = mapper.readTree(request);
			String element = "VARIABLES";
			// Position the node at the "VARIABLES" node:
			// - 'get' will return null if not found
			JsonNode jsonNode = rootNode.get(element);
			if ( jsonNode != null ) {
				Message.printStatus(2, routine, "VARIABLES has " + jsonNode.size() + " objects.");
			}
			if ( (jsonNode != null) && (jsonNode.size() > 0) && !jsonNode.isMissingNode() ) {
				// Node (variable) name changes for each object so have to iterate manually:
				// - VARIABLES is an array so use 'elements' below.
				Iterator<JsonNode> nodes = jsonNode.elements();
				while ( nodes.hasNext() ) {
					JsonNode arrayNode = nodes.next();
					// Know that each array item is a map:
					// - key is the variable name
					// - value is another map with the variable long name and units
					Iterator<Entry<String, JsonNode>> mapNodes = arrayNode.fields();
					while ( mapNodes.hasNext() ) {
						Map.Entry<String, JsonNode> entry = mapNodes.next();
						//Message.printStatus(2, routine, "Variable name = " + entry.getKey());
						// Deserialize the variable object.
						Variable variable =	(Variable)JacksonToolkit.getInstance().treeToValue(entry.getValue(), Variable.class);
						// Set the name to the key of the parent map.
						variable.setName(entry.getKey());
						// Add the variable to the variable list.
						variableList.add(variable);
					}
				}
			}
			else {
				Message.printStatus ( 2, routine, "Did not find VARIABLES object in JSON." );
			}
		}
		catch ( JsonParseException e ) {
			Message.printWarning(2, routine, "Error parsing JSON response from \"" + request + "\" (" + e + ").");
			Message.printWarning(2, routine, e );
			throw e;
		}
		catch ( JsonMappingException e ) {
			Message.printWarning(2, routine, "Error mapping JSON response from \"" + request + "\" (" + e + ").");
			Message.printWarning(2, routine, e );
			throw e;
		}
		catch ( MalformedURLException e ) {
			Message.printWarning(2, routine, "Malformed URL has occured. URL=\"" + requestUrl + "\" (" + e + ").");
			Message.printWarning(2, routine, e );
			throw e;
		}
		catch ( IOException e ) {
			Message.printWarning(2, routine, "IOException (" + e + ").");
			Message.printWarning(2, routine, e );
			throw e;
		}
		return variableList;
	}

    /**
     * Read the version from the web service, used when processing #@require commands in TSTool.
     * TODO smalers 2023-01-03 need to figure out if a version is available.
     */
    private String readVersion () {
    	return "";
    }

    /**
     * Set the time series properties from the TimeSeriesCatalog.
     * @param ts time series to update
     * @param tscatalog time series catalog to supply properties
     */
    private void setTimeSeriesProperties ( TS ts, TimeSeriesCatalog tscatalog ) {
    	// Set all the Synoptic properties that are known for the time series.
    	ts.setProperty("station.ELEVATION", tscatalog.getStationElevation());
    	ts.setProperty("station.ELEV_DEM", tscatalog.getStationElevDem());
    	ts.setProperty("station.LATITUDE", tscatalog.getStationLatitude());
    	ts.setProperty("station.LONGITUDE", tscatalog.getStationLongitude());
    	ts.setProperty("station.MNET", tscatalog.getStationMnet());
    	ts.setProperty("station.MNET_ID", tscatalog.getStationMnetId());
    	ts.setProperty("station.NAME", tscatalog.getStationName());
    	ts.setProperty("station.QC_FLAGGED", tscatalog.getStationQcFlagged());
    	ts.setProperty("station.STATE", tscatalog.getStationState());
    	ts.setProperty("station.STATUS", tscatalog.getStationStatus());
    	ts.setProperty("station.STID", tscatalog.getStationId());
    	ts.setProperty("station.TIMEZONE", tscatalog.getStationTimeZone());

    	ts.setProperty("sensor.SENSOR_VARIABLE", tscatalog.getSensorVariable());
    	ts.setProperty("sensor.SENSOR_VARIABLE_OUT", tscatalog.getSensorVariableOut());
    	ts.setProperty("sensor.start", tscatalog.getSensorStart());
    	ts.setProperty("sensor.end", tscatalog.getSensorEnd());
    }

}
