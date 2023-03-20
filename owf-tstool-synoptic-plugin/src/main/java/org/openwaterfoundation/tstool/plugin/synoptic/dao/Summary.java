// Summary - class containing response SUMMARY, used with multiple services

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
 * Variable for Synoptic response SUMMARY.
 * See: https://developers.synopticdata.com/mesonet/v2/stations/timeseries/
 *
 * The response includes a top-level summary:
 *
 *  "SUMMARY": {
 *    "RESPONSE_CODE": 1,
 *    "RESPONSE_MESSAGE": "OK",
 *    "TOTAL_DATA_TIME": "12.0379924774 ms",
 *    "VERSION" : "v2.17.0",
 *    "NUMBER_OF_OBJECTS": 1
 *  }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Summary {

	/**
	 * 'DATA_PARSING_TIME' time to query (e.g., "1.23. ms").
	 */
	@JsonProperty("DATA_PARSING_TIME")
	private String dataParsingTime = null;

	/**
	 * 'DATA_QUERY_TIME' time to query data (e.g., "1.23. ms").
	 */
	@JsonProperty("DATA_QUERY_TIME")
	private String dataQueryTime = null;

	/**
	 * 'FUNCTION_USED'.
	 */
	@JsonProperty("FUNCTION_USED")
	private String functionUsed = null;

	/**
	 * 'METADATA_RESPONSE_TIME' time to query metadata (e.g., "1.23. ms").
	 */
	@JsonProperty("METADATA_RESPONSE_TIME")
	private String metadataResponseTime = null;

	/**
	 * 'NUMBER_OF_OBJECTS' number of objects returned.
	 */
	@JsonProperty("NUMBER_OF_OBJECTS")
	private Integer numberOfObjects = null;

	/**
	 * 'RESPONSE_CODE' (1=OK, 2=zero results, 200=Authentication failure, 400=violates a rule of the API).
	 */
	@JsonProperty("RESPONSE_CODE")
	private Integer responseCode = null;

	/**
	 * 'RESPONSE_MESSAGE' message for the response status.
	 */
	@JsonProperty("RESPONSE_MESSAGE")
	private String responseMessage = "";

	/**
	 * 'TOTAL_DATA_TIME' total time to query data (e.g., "1.23 ms").
	 */
	@JsonProperty("TOTAL_DATA_TIME")
	private String totalDataTime = null;

	/**
	 * 'VERSION' API version.
	 */
	@JsonProperty("VERSION")
	private String version = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public Summary() {
	}

	/**
	 * Get the 'NUMBER_OF_OBJECTS'.
	 */
	public Integer getNumberOfObjects() {
		return this.numberOfObjects;
	}

	/**
	 * Get the 'RESPONSE_CODE'.
	 */
	public Integer getResponseCode() {
		return this.responseCode;
	}

	/**
	 * Get the 'RESPONSE_MESSAGE'.
	 */
	public String getResponseMessage() {
		return this.responseMessage;
	}

	/**
	 * Get the 'TOTAL_DATA_TIME'.
	 */
	public String getTotalDataTime() {
		return this.totalDataTime;
	}
	
	/**
	 * Get the 'VERSION'.
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Indicate whether the response is OK, meaning the response code is 1.
	 */
	public boolean isOk ( ) {
		if ( this.responseCode == 1 ) {
			return true;
		}
		else {
			return false;
		}
	}

}