// NwsCwa - class containing Synoptic NWS CWA data

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
 * National Weather Service (NWS) County Warning Areas.
 * See the repository resources/nws-cwa folder.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NwsCwa {

	/**
	 * CWA abbreviation (e.g., "AFC").
	 */
	private String cwa = "";

	/**
	 * Weather Forecast Office abbreviation (e.g., "AFC").
	 */
	private String wfo = "";

	/**
	 * Latitude decimal degrees
	 */
	private Double lat = null;

	/**
	 * Longitude decimal degrees
	 */
	private Double lon = null;

	/**
	 * Region (e.g., AR).
	 */
	private String region = "";

	/**
	 * Full station ID.
	 */
	private String fullStaid = "";

	/**
	 * City and state (e.g., "Anchorage, AK").
	 */
	private String cityState = "";

	/**
	 * City (e.g., "Anchorage").
	 */
	private String city = "";

	/**
	 * State (e.g., "Alaska").
	 */
	private String state = "";

	/**
	 * State abbreviation (e.g., "AK").
	 */
	private String st = "";

	/**
	 * Plugin filter choice (e.g., "AFC - AK, Anchorage").
	 */
	private String filterChoice = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public NwsCwa() {
	}

	/**
	 * Get the CWA.
	 */
	public String getCwa() {
		return this.cwa;
	}

	/**
	 * Get the filter choice.
	 */
	public String getFilterChoice() {
		return this.filterChoice;
	}

	/**
	 * Set the CWA string.
	 */
	public void setCwa ( String cwa ) {
		this.cwa = cwa;
	}

	/**
	 * Set the filter choice string.
	 */
	public void setFilterChoice ( String filterChoice ) {
		this.filterChoice = filterChoice;
	}

}