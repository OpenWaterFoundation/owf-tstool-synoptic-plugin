// PeriodOfRecord - class containing period of record for network, station, etc.

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
 * Period of record, for example for networks:
 * 
 * "PERIOD_OF_RECORD": {
 *      "start": "1997-01-01T00:00:00Z",
 *       "end": "2021-07-08T18:00:00Z"
 *  },
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeriodOfRecord {
	
	/**
	 * 'start'.
	 */
	private String start = "";

	/**
	 * 'end'.
	 */
	private String end = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public PeriodOfRecord() {
	}

	/**
	 * Get the end.
	 */
	public String getEnd() {
		return this.end;
	}

	/**
	 * Get the start.
	 */
	public String getStart() {
		return this.start;
	}
}