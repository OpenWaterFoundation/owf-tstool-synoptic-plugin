// Variable - class containing Synoptic variable 

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Variable for Synoptic time series data.
 * See: https://developers.synopticdata.com/mesonet/v2/variables/
 * 
 * Each object has a separate name, which complicates processing:
 * 
 *   "VARIABLES": [
 *     {
 *       "air_temp": {
 *         "long_name": "Temperature",
 *         "unit": "Celsius"
 *       }
 *     },
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {
	
	/**
	 * 'long_name'.
	 */
	private String longName = "";

	/**
	 * 'name'.
	 */
	private String name = "";

	/**
	 * 'unit'.
	 */
	private String unit = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public Variable() {
	}

	/**
	 * Get the variable long name.
	 */
	public String getLongName() {
		return this.longName;
	}
	
	/**
	 * Get the variable name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the variable units.
	 */
	public String getUnit() {
		return this.unit;
	}

	/**
	 * Set the variable name.
	 * @param name variable name.
	 */
	public void setName ( String name ) {
		this.name = name;
	}
}