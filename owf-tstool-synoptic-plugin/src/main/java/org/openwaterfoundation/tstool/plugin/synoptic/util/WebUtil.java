// WebUtil - web utility functions

/* NoticeStart

OWF TSTool MesoWest Plugin
Copyright (C) 2023 Open Water Foundation

OWF TSTool MesoWest Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OWF TSTool MesoWest Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OWF TSTool MesoWest Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.synoptic.util;

import RTi.Util.GUI.InputFilter;
import RTi.Util.Message.Message;

/**
 * Useful web utility methods.
 * @author sam
 *
 */
public class WebUtil {

	/**
	 * Append a URL query parameter to the URL, automatically adding ? and &.
	 * @param urlString URL string builder to update.
	 */
	public static void appendUrlQueryParameter ( StringBuilder urlString, String paramName, String paramValue ) {
		if ( urlString.toString().indexOf("?") > 0 ) {
			// ? was found so need to add & in front of the query parameter
			urlString.append("&");
		}
		else {
			// ? was not found so need to add ? in front of the query parameter
			urlString.append("?");
		}
		urlString.append(paramName);
		urlString.append("=");
		// TODO smalers 2020-01-24 evaluate whether need to escape any characters, etc.
		urlString.append(paramValue);
	}

	/**
	 * Format a URL query parameter clause give an input filter and operator.
	 * Wildcards are generally allowed in KiWIS for string query parameters but not strings that
	 * will be converted to integers, such as internal identifiers.
	 * @param filter the InputFilter that is being processed, to extract a query parameter
	 * @param operator the operator that is selected for the filter
	 * @return a query parameter clause, for example "someParam=someValue",
	 * or null if unable to do so or does not exist in InputFilter.
	 */
	public static String getQueryClauseFromInputFilter ( InputFilter filter, String operator ) {
		String routine = WebUtil.class.getSimpleName() + ".getWhereClauseFromInputFilter";
		boolean upperCase = false; // Keep for now but may not be needed.
		// Get the selected filter for the filter group.
		if ( filter.getWhereLabel().trim().equals("") ) {
			// Blank indicates that the filter should be ignored.
			return null;
		}
		// Get the internal where.
		String whereSubject = filter.getWhereInternal();
		if ( (whereSubject == null) || whereSubject.equals("") ) {
		    return null;
		}
		// Get the user input.
		String input = filter.getInputInternal().trim();
	    if ( upperCase ) {
	        input = input.toUpperCase();
	    }
		Message.printStatus(2,routine,"Internal input is \"" + input + "\"");
		// Now format the where clause.

		String whereClause = null;

		if ( operator.equalsIgnoreCase(InputFilter.INPUT_BETWEEN) ) {
			// TODO - need to enable in InputFilter_JPanel.
		}
		else if ( operator.equalsIgnoreCase( InputFilter.INPUT_CONTAINS) ) {
			// Only applies to strings.
		    whereClause = whereSubject + "=*" + input + "*";
		}
		else if ( operator.equalsIgnoreCase( InputFilter.INPUT_ENDS_WITH) ) {
			whereClause = whereSubject + "=*" + input;
		}
		else if ( operator.equalsIgnoreCase(InputFilter.INPUT_EQUALS) ){
			whereClause = whereSubject + "=" + input;
		}
		else if ( operator.equalsIgnoreCase( InputFilter.INPUT_GREATER_THAN) ) {
			// Only applies to numbers (?).
			whereClause = whereSubject + ">" + input;
		}
		else if ( operator.equalsIgnoreCase(InputFilter.INPUT_GREATER_THAN_OR_EQUAL_TO) ) {
			// Only applies to numbers (?).
			whereClause = whereSubject + ">=" + input;
		}
	    //else if ( operator.equalsIgnoreCase(InputFilter.INPUT_IS_EMPTY)){
	    //    where_clause = whereSubject + "='' or where is null";
	    //}
		else if ( operator.equalsIgnoreCase( InputFilter.INPUT_LESS_THAN) ) {
			// Only applies to numbers (?).
			whereClause = whereSubject + "<" + input;
		}
		else if ( operator.equalsIgnoreCase( InputFilter.INPUT_LESS_THAN_OR_EQUAL_TO) ) {
			// Only applies to numbers (?).
			whereClause = whereSubject + "<=" + input;
		}
		else if ( operator.equalsIgnoreCase(InputFilter.INPUT_MATCHES)){
		    // Only applies to strings.
			whereClause = whereSubject + "=" + input;
		}
		else if ( operator.equalsIgnoreCase(InputFilter.INPUT_ONE_OF) ){
			// TODO - need to enable in InputFilter_JPanel.
		}
		else if ( operator.equalsIgnoreCase( InputFilter.INPUT_STARTS_WITH) ) {
			whereClause = whereSubject + "=" + input + "*";
		}
		else {
			// Unrecognized where.
		    String message = "Unrecognized operator \"" + operator + "\"";
			Message.printWarning ( 2, routine, message );
			throw new IllegalArgumentException(message);
		}
		// TODO - need to handle is null, negative (not), when enabled in InputFilter_JPanel.
		// TODO - need a clean way to enforce upper case input but
		// also perhaps allow a property in the filter to override
		// because a database may have mixed case in only a few tables.
		//if ( dmi.uppercaseStringsPreferred() ) {
			//where_clause = where_clause.toUpperCase();
		//}
		return whereClause;
	}
}
