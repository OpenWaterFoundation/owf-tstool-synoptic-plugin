// State - class containing Synoptic state list 

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

/**
 * State abbreviations and names.
 * Could generalize and reuse throughout TSTool but keep separate for now
 */
public class State {
	
	/**
	 * State abbreviation.
	 */
	private String abbreviation = "";

	/**
	 * State name.
	 */
	private String name = "";

	/**
	 * Constructor.
	 */
	public State ( String abbreviation, String name ) {
		this.abbreviation = abbreviation;
		this.name = name;
	}

	/**
	 * Get the state name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the state abbreviation.
	 */
	public String getAbbreviation() {
		return this.abbreviation;
	}

}