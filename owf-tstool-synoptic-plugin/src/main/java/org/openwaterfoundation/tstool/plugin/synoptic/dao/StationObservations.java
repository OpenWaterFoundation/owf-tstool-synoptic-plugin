// StationObservations - class containing STATION.OBSERVATIONS

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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Variable for Synoptic STATION.OBSERVATIONS.
 * See: https://developers.synopticdata.com/mesonet/v2/stations/timeseries/
 *
 * Each station has a list of observations
 *
 *  "STATION": [
 *     {
 *        "OBSERVATIONS": {
 *          "date_time": [
 *            "2015-01-03T00:00:00Z",
 *            "2015-01-03T00:05:00Z",
 *            "2015-01-03T00:10:00Z",
 *            "2015-01-03T00:15:00Z",
 *            "2015-01-03T00:20:00Z"
 *          ],
 *          "air_temp_set_1": [-5.6, -5.6, -6.1, -6.1, -6.7]
 *        }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StationObservations {

	/**
	 * 'date_time' array.
	 */
	@JsonProperty("date_time")
	private String [] dateTimeArray = null;

	/**
	 * Sensor variable values corresponding to the dateTimeArray:
	 * - these are filled in later
	 */
	private Double [] valueArray = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public StationObservations() {
	}

	/**
	 * Get the dateTimeArray.
	 */
	public String [] getDateTimeArray() {
		return this.dateTimeArray;
	}

	/**
	 * Get the variable array.
	 */
	public Double [] getValueArray() {
		return this.valueArray;
	}

}