// Network - class containing network information

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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Networks:
 * See: https://developers.synopticdata.com/mesonet/v2/networks/
 *
 * {
 *   "MNET": [
 *       {
 *         "CATEGORY": "7",
 *         "REPORTING_STATIONS": 2494,
 *         "TOTAL_RESTRICTED": 0,
 *         "ACTIVE_RESTRICTED": 0,
 *         "LAST_OBSERVATION": "2021-07-08T18:15:00Z",
 *         "URL": null,
 *         "PERCENT_REPORTING": 96.59,
 *         "PERIOD_CHECKED": 120,
 *         "TOTAL_STATIONS": 3478,
 *         "ACTIVE_STATIONS": 2582,
 *         "PERIOD_OF_RECORD": {
 *         "start": "1997-01-01T00:00:00Z",
 *         "end": "2021-07-08T18:00:00Z"
 *         },
 *         "LONGNAME": "National Weather Service/Federal Aviation Administration",
 *         "SHORTNAME": "NWS/FAA",
 *         "PERCENT_ACTIVE": 74.24,
 *         "ID": "1"
 *       },
 *     ],
 *     "SUMMARY": {
 *       "NUMBER_OF_OBJECTS": 123,
 *       "RESPONSE_CODE": 1,
 *       "RESPONSE_MESSAGE": "OK",
 *       "RESPONSE_TIME": "0.164985656738 ms"
 *     }
 *  }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Network {

	/**
	 * 'ACTIVE_RESTRICTED'.
	 */
	@JsonProperty("ACTIVE_RESTRICTED")
	private Integer activeRestricted = null;

	/**
	 * 'ACTIVE_STATIONS'.
	 */
	@JsonProperty("ACTIVE_STATIONS")
	private Integer activeStations = null;

	/**
	 * 'CATEGORY'.
	 */
	@JsonProperty("CATEGORY")
	private String category = "";

	/**
	 * 'ID'.
	 */
	@JsonProperty("ID")
	private Integer id = null;

	/**
	 * 'LAST_OBSERVATION'.
	 */
	@JsonProperty("LAST_OBSERVATION")
	private String lastObservation = "";

	/**
	 * 'LONGNAME'.
	 */
	@JsonProperty("LONGNAME")
	private String longname = "";

	/**
	 * 'PERCENT_ACTIVE'.
	 */
	@JsonProperty("PERCENT_ACTIVE")
	private Double percentActive = null;

	/**
	 * 'PERCENT_OF_RECORD'.
	 */
	@JsonProperty("PERCENT_OF_RECORD")
	private PeriodOfRecord percentOfRecord = null;

	/**
	 * 'PERCENT_REPORTING'.
	 */
	@JsonProperty("PERCENT_REPORTING")
	private Double percentReporting = null;

	/**
	 * 'PERIOD_CHECKED'.
	 */
	@JsonProperty("PERIOD_CHECKED")
	private Integer periodChecked = null;

	/**
	 * 'REPORTING_STATIONS'.
	 */
	@JsonProperty("REPORTING_STATIONS")
	private Integer reportingStations = null;

	/**
	 * 'SHORTNAME'.
	 */
	@JsonProperty("SHORTNAME")
	private String shortname = "";

	/**
	 * 'TOTAL_RESTRICTED'.
	 */
	@JsonProperty("TOTAL_RESTRICTED")
	private Integer totalRestricted = null;

	/**
	 * 'TOTAL_STATIONS'.
	 */
	@JsonProperty("TOTAL_STATIONS")
	private Integer totalStations = null;

	/**
	 * 'URL'.
	 */
	@JsonProperty("URL")
	private String url = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public Network() {
	}

	/**
	 * Get the network 'ID'.
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * Get the network 'SHORTNAME'.
	 */
	public String getShortName() {
		return this.shortname;
	}

	/**
	 * Lookup at network from the ID.
	 * @param networkList list of Network to search
	 * @param id network ID
	 * @return matching Network or null if not found
	 */
	public static Network lookupNetworkFromId ( List<Network> networkList, String id ) {
		if ( (networkList == null) || (id == null) ) {
			return null;
		}
		try {
			Integer idInt = Integer.parseInt(id);
			return lookupNetworkFromId ( networkList, idInt );
		}
		catch ( NumberFormatException e ) {
			return null;
		}
	}

	/**
	 * Lookup at network from the ID.
	 * @param networkList list of Network to search
	 * @param id network ID
	 * @return matching Network or null if not found
	 */
	public static Network lookupNetworkFromId ( List<Network> networkList, Integer id ) {
		if ( (networkList == null) || (id == null) ) {
			return null;
		}
		for ( Network network : networkList ) {
			if ( network.id != null ) {
				if ( network.id.equals(id) ) {
					return network;
				}
			}
		}
		return null;
	}
}