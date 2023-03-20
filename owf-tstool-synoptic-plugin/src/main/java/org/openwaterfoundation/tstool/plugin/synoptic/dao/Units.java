// LatestUnits - class containing units from the 'latest' service

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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Synoptic 'latest' request units, which match a sensor variable for the key.
 * See: https://developers.synopticdata.com/mesonet/v2/variables/
 *
 * {
 *    "UNITS": {
 *      "soil_temp": "Celsius",
 *      "cloud_layer_1": "Meters",
 *      "cloud_layer_3": "Meters",
 *      "cloud_layer_2": "Meters",
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Units {
	
	/**
	 * Variable name associated with units.
	 */
	private String variableName = "";

	/**
	 * Units associated with a variable.
	 */
	private String units = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public Units() {
	}

	/**
	 * Constructor to set the variable and units
	 * @param variableName the variable name
	 * @param units the units for the variable
	 */
	public Units ( String variableName, String units ) {
		this.variableName = variableName;
		this.units = units;
	}

	/**
	 * Get the variable name.
	 */
	public String getVariableName() {
		return this.variableName;
	}
	
	/**
	 * Get the units.
	 */
	public String getUnits() {
		return this.units;
	}

	/**
	 * Lookup the units for a variable name.
	 * @param latestUnitsList list of LatestUnits to search
	 * @param variableName variable name to look up
	 * @return matching LatestUnits or null if not found
	 */
	public static Units lookupLatestUnitsFromVariable ( List<Units> latestUnitsList, String variableName ) {
		if ( (latestUnitsList == null) || (variableName == null) ) {
			return null;
		}
		for ( Units latestUnits : latestUnitsList ) {
			if ( latestUnits.variableName != null ) {
				if ( latestUnits.variableName.equals(variableName) ) {
					return latestUnits;
				}
			}
		}
		return null;
	}

	/**
	 * Set the variable name.
	 * @param name variable name.
	 */
	public void setVariableName ( String variableName ) {
		this.variableName = variableName;
	}

	/**
	 * Set the units.
	 * @param units units.
	 */
	public void setUnits ( String units ) {
		this.units = units;
	}
}