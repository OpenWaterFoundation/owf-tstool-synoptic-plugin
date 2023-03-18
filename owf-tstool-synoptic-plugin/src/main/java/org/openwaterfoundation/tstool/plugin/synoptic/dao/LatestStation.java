// LatestStation - class containing Synoptic latest value for a station 

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

package org.openwaterfoundation.tstool.plugin.synoptic.dao;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objects corresponding the 'latest' request STATION array items,
 * which are used to get a time series list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LatestStation {
	
	// List alphabetized.

	/**
	 * 'ELEVATION'.
	 */
	@JsonProperty("ELEVATION")
	private String elevation = "";

	/**
	 * 'ELEV_DEM'.
	 */
	@JsonProperty("ELEV_DEM")
	private String elevDem = "";

	/**
	 * 'LATITUDE'.
	 */
	@JsonProperty("LATITUDE")
	private String latitude = "";

	/**
	 * 'LONGITUDE'.
	 */
	@JsonProperty("LONGITUDE")
	private String longitude = "";

	/**
	 * 'MNET_ID'.
	 */
	@JsonProperty("MNET_ID")
	private String mnetId = "";

	/**
	 * 'NAME'.
	 */
	@JsonProperty("NAME")
	private String name = "";

	/**
	 * 'QC_FLAGGED'.
	 */
	@JsonProperty("QC_FLAGGED")
	private Boolean qcFlagged = null;

	/**
	 * 'SENSOR_VARIABLES' map
	 */
	@JsonProperty("SENSOR_VARIABLES")
	private Map<String,?> sensorVariablesMap = null;

	/**
	 * 'STATE'.
	 */
	@JsonProperty("STATE")
	private String state = "";

	/**
	 * 'STATUS'.
	 */
	@JsonProperty("STATUS")
	private String status = "";

	/**
	 * 'STID'.
	 */
	@JsonProperty("STID")
	private String stid = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public LatestStation() {
	}

	/**
	 * Get the ELEVATION.
	 */
	@JsonProperty("ELEVATION")
	public String getElevation() {
		return this.elevation;
	}

	/**
	 * Get the ELEV_DEM.
	 */
	public String getElevDem() {
		return this.elevDem;
	}

	/**
	 * Get the LATITUDE.
	 */
	public String getLatitude() {
		return this.latitude;
	}

	/**
	 * Get the LONGITUDE.
	 */
	public String getLongitude() {
		return this.longitude;
	}

	/**
	 * Get the MNET_ID.
	 */
	public String getMnetId() {
		return this.mnetId;
	}

	/**
	 * Get the NAME.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the SENSOR_VARIABLES as a map:
	 * - must parse with custom code because variable names are the map key
	 */
	public Map<String,?> getSensorVariablesMap() {
		return this.sensorVariablesMap;
	}

	/**
	 * Get the QC_FLAGGED.
	 */
	public Boolean getQcFlagged() {
		return this.qcFlagged;
	}

	/**
	 * Get the STATE.
	 */
	public String getState() {
		return this.state;
	}

	/**
	 * Get the 'STID'.
	 */
	public String getStid() {
		return this.stid;
	}

	/**
	 * Get the STATUS.
	 */
	public String getStatus() {
		return this.status;
	}
}