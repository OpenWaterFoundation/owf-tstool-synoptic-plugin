// Synoptic_TimeSeries_TableModel - table model for the time series catalog

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

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import org.openwaterfoundation.tstool.plugin.synoptic.datastore.SynopticDataStore;
import org.openwaterfoundation.tstool.plugin.synoptic.dao.TimeSeriesCatalog;

/**
This class is a table model for time series header information for Synoptic web resource time series.
By default the sheet will contain row and column numbers.
*/
@SuppressWarnings({ "serial", "rawtypes" })
public class Synoptic_TimeSeries_TableModel extends JWorksheet_AbstractRowTableModel {

	/**
	Number of columns in the table model.
	*/
	private final int COLUMNS = 22;

	public final int COL_STATION_ID = 0;
	public final int COL_STATION_NAME = 1;
	public final int COL_DATA_SOURCE = 2;
	public final int COL_DATA_TYPE = 3;
	public final int COL_DATA_INTERVAL = 4;
	public final int COL_DATA_UNITS = 5;
	public final int COL_SENSOR_VARIABLE = 6;
	public final int COL_SENSOR_VARIABLE_OUT = 7;
	public final int COL_SENSOR_START = 8;
	public final int COL_SENSOR_END = 9;
	public final int COL_STATION_TIMEZONE = 10;
	public final int COL_STATION_STATE = 11;
	public final int COL_STATION_STATUS = 12;
	public final int COL_STATION_QC_FLAGGED = 13;
	public final int COL_STATION_MNET = 14;
	public final int COL_STATION_MNET_ID = 15;
	public final int COL_STATION_LONGITUDE = 16;
	public final int COL_STATION_LATITUDE = 17;
	public final int COL_STATION_ELEVATION = 18;
	public final int COL_STATION_ELEV_DEM = 19;
	public final int COL_PROBLEMS = 20;
	public final int COL_DATASTORE = 21;

	/**
	Datastore corresponding to datastore used to retrieve the data.
	*/
	SynopticDataStore datastore = null;

	/**
	Data are a list of TimeSeriesCatalog.
	*/
	private List<TimeSeriesCatalog> timeSeriesCatalogList = null;

	/**
	Constructor.  This builds the model for displaying the given KiWIS time series data.
	@param dataStore the data store for the data
	@param data the list of KiWIS TimeSeriesCatalog that will be displayed in the table.
	@throws Exception if an invalid results passed in.
	*/
	@SuppressWarnings("unchecked")
	public Synoptic_TimeSeries_TableModel ( SynopticDataStore dataStore, List<? extends Object> data ) {
		if ( data == null ) {
			_rows = 0;
		}
		else {
		    _rows = data.size();
		}
	    this.datastore = dataStore;
		_data = data; // Generic
		// TODO SAM 2016-04-17 Need to use instanceof here to check.
		this.timeSeriesCatalogList = (List<TimeSeriesCatalog>)data;
	}

	/**
	From AbstractTableModel.  Returns the class of the data stored in a given column.
	@param columnIndex the column for which to return the data class.
	*/
	@SuppressWarnings({ "unchecked" })
	public Class getColumnClass (int columnIndex) {
		switch (columnIndex) {
			// List in the same order as top of the class.
			//case COL_STATION_ID: return Integer.class;
			//case COL_STATION_LATITUDE: return Double.class;
			//case COL_STATION_LONGITUDE: return Double.class;
			//case COL_STATION_ELEV_DEM: return Double.class;
			default: return String.class; // All others.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of columns of data.
	@return the number of columns of data.
	*/
	public int getColumnCount() {
		return this.COLUMNS;
	}

	/**
	From AbstractTableMode.  Returns the name of the column at the given position.
	@return the name of the column at the given position.
	*/
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			//case COL_LOCATION_ID: return "Location ID";
			case COL_STATION_ID: return "Station ID";
			case COL_STATION_NAME: return "Station Name";
			case COL_DATA_SOURCE: return "Data Source";
			case COL_DATA_TYPE: return "Data Type";
			case COL_DATA_INTERVAL: return "Interval";
			case COL_DATA_UNITS: return "Units";
			case COL_SENSOR_VARIABLE: return "Sensor Variable";
			case COL_SENSOR_VARIABLE_OUT: return "Sensor Variable (Out)";
			case COL_SENSOR_START: return "Start (UTC)";
			case COL_SENSOR_END: return "End (UTC)";
			case COL_STATION_TIMEZONE: return "Station Time Zone";
			case COL_STATION_STATE: return "State";
			case COL_STATION_STATUS: return "Status";
			case COL_STATION_QC_FLAGGED: return "QC Flagged";
			case COL_STATION_MNET: return "Network";
			case COL_STATION_MNET_ID: return "Network ID";
			case COL_STATION_LONGITUDE: return "Longitude";
			case COL_STATION_LATITUDE: return "Latitude";
			case COL_STATION_ELEVATION: return "Elevation";
			case COL_STATION_ELEV_DEM: return "Elevation (DEM)";
			case COL_PROBLEMS: return "Problems";
			case COL_DATASTORE: return "Datastore";

			default: return "";
		}
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public String[] getColumnToolTips() {
	    String[] toolTips = new String[this.COLUMNS];
	    toolTips[COL_STATION_ID] = "Station identifier (STID)";
	    toolTips[COL_STATION_NAME] = "Station name (NAME)";
	    toolTips[COL_DATA_SOURCE] = "Data source (NETWORK)";
	    toolTips[COL_DATA_TYPE] = "Time series data type (SENSOR_VARIABLE)";
	    toolTips[COL_DATA_INTERVAL] = "Time series data interval, always IrregSecond";
	    toolTips[COL_DATA_UNITS] = "Time series data value units abbreviation (UNITS)";
	    toolTips[COL_SENSOR_VARIABLE] = "Sensor variable (SENSOR_VARIABLE)";
	    toolTips[COL_SENSOR_VARIABLE_OUT] = "Sensor variable in output, used for uniqueness when multiple same sensor variable";
	    toolTips[COL_SENSOR_START] = "Period of record start for the sensor veriable (UTC).";
	    toolTips[COL_SENSOR_END] = "Period of record end for the sensor variable (UTC).";
	    toolTips[COL_STATION_TIMEZONE] = "Station local time zone.";
	    toolTips[COL_STATION_STATE] = "State (STATE)";
	    toolTips[COL_STATION_STATUS] = "Station status (STATUS)";
	    toolTips[COL_STATION_QC_FLAGGED] = "Has the station been flagged by QC checks (QC_FLAGGED)?";
	    toolTips[COL_STATION_MNET] = "Station measurement network (from MNET_ID)";
	    toolTips[COL_STATION_MNET_ID] = "Station network ID (MNET_ID)";
	    toolTips[COL_STATION_LONGITUDE] = "Station longitude, decimal degrees (LONGITUDE)";
	    toolTips[COL_STATION_LATITUDE] = "Station latitude, decimal degrees (LATITUDE)";
	    toolTips[COL_STATION_ELEVATION] = "Station elevation (ELEVATION)";
	    toolTips[COL_STATION_ELEV_DEM] = "Station elevation from DEM (ELEV_DEM)";
		toolTips[COL_PROBLEMS] = "Problems";
		toolTips[COL_DATASTORE] = "Datastore name";
	    return toolTips;
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public int[] getColumnWidths() {
		int[] widths = new int[this.COLUMNS];
	    widths[COL_STATION_ID] = 11;
	    widths[COL_STATION_NAME] = 20;
	    widths[COL_DATA_SOURCE] = 13;
	    widths[COL_DATA_TYPE] = 25;
	    widths[COL_DATA_INTERVAL] = 8;
	    widths[COL_DATA_UNITS] = 6;
	    widths[COL_SENSOR_VARIABLE] = 15;
	    widths[COL_SENSOR_VARIABLE_OUT] = 15;
	    widths[COL_SENSOR_START] = 13;
	    widths[COL_SENSOR_END] = 13;
	    widths[COL_STATION_TIMEZONE] = 15;
	    widths[COL_STATION_STATE] = 5;
	    widths[COL_STATION_STATUS] = 6;
	    widths[COL_STATION_QC_FLAGGED] = 8;
	    widths[COL_STATION_MNET] = 12;
	    widths[COL_STATION_MNET_ID] = 8;
	    widths[COL_STATION_LONGITUDE] = 9;
	    widths[COL_STATION_LATITUDE] = 9;
	    widths[COL_STATION_ELEVATION] = 9;
	    widths[COL_STATION_ELEV_DEM] = 11;
		widths[COL_PROBLEMS] = 30;
		widths[COL_DATASTORE] = 10;
		return widths;
	}

	/**
	Returns the format to display the specified column.
	@param column column for which to return the format.
	@return the format (as used by StringUtil.formatString()).
	*/
	public String getFormat ( int column ) {
		switch (column) {
			//case COL_STATION_LONGITUDE: return "%.6f";
			//case COL_STATION_LATITUDE: return "%.6f";
			//case COL_STATION_ELEVATION: return "%.2f";
			default: return "%s"; // All else are strings.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of rows of data in the table.
	*/
	public int getRowCount() {
		return _rows;
	}

	/**
	From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
	@param row the row for which to return data.
	@param col the column for which to return data.
	@return the data that should be placed in the JTable at the given row and column.
	*/
	public Object getValueAt(int row, int col) {
		// Make sure the row numbers are never sorted.
		if (_sortOrder != null) {
			row = _sortOrder[row];
		}

		TimeSeriesCatalog timeSeriesCatalog = this.timeSeriesCatalogList.get(row);
		switch (col) {
			// OK to allow null because will be displayed as blank.
			case COL_STATION_ID: return timeSeriesCatalog.getStationId();
			case COL_STATION_NAME: return timeSeriesCatalog.getStationName();
			case COL_DATA_SOURCE: return timeSeriesCatalog.getDataSource();
			case COL_DATA_TYPE: return timeSeriesCatalog.getDataType();
			case COL_DATA_INTERVAL: return timeSeriesCatalog.getDataInterval();
			case COL_DATA_UNITS: return timeSeriesCatalog.getDataUnits();
			case COL_SENSOR_VARIABLE: return timeSeriesCatalog.getSensorVariable();
			case COL_SENSOR_VARIABLE_OUT: return timeSeriesCatalog.getSensorVariableOut();
			case COL_SENSOR_START: return timeSeriesCatalog.getSensorStart();
			case COL_SENSOR_END: return timeSeriesCatalog.getSensorEnd();
			case COL_STATION_TIMEZONE: return timeSeriesCatalog.getStationTimeZone();
			case COL_STATION_STATE: return timeSeriesCatalog.getStationState();
			case COL_STATION_STATUS: return timeSeriesCatalog.getStationStatus();
			case COL_STATION_QC_FLAGGED: return timeSeriesCatalog.getStationQcFlagged();
			case COL_STATION_MNET: return timeSeriesCatalog.getStationMnet();
			case COL_STATION_MNET_ID: return timeSeriesCatalog.getStationMnetId();
			case COL_STATION_LONGITUDE: return timeSeriesCatalog.getStationLongitude();
			case COL_STATION_LATITUDE: return timeSeriesCatalog.getStationLatitude();
			case COL_STATION_ELEVATION: return timeSeriesCatalog.getStationElevation();
			case COL_STATION_ELEV_DEM: return timeSeriesCatalog.getStationElevDem();
			case COL_PROBLEMS: return timeSeriesCatalog.formatProblems();
			case COL_DATASTORE: return this.datastore.getName();
			default: return "";
		}
	}

}