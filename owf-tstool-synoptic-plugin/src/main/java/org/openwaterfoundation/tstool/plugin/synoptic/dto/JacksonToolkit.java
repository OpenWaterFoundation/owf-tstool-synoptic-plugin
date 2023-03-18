// JacksonToolkit - useful tools to use the Jackson library

/* NoticeStart

OWF TSTool KiWIS Plugin
Copyright (C) 2022-2023 Open Water Foundation

OWF TSTool KiWIS Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OWF TSTool KiWIS Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OWF TSTool KiWIS Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.synoptic.dto;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import RTi.Util.Message.Message;

/**
 * This toolkit facilitates using Jackson package to translate JSON to/from data access objects.
 * @author sam
 *
 */
public class JacksonToolkit {
	
	/**
	 * Global ObjectMapper as part of the Jackson library used
	 * for serializing and deserializing JSON data to a POJO.
	 */
	private ObjectMapper mapper;
	
	/**
	 * Jackson Toolkit used for lazy initialization of a singleton class
	 */
	private static JacksonToolkit instance;
	
	private JacksonToolkit() {
		this.mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	/**
	 * Lazy initialization of a singleton class instance
	 * @return instance of JacksonToolkit class
	 */
	public static JacksonToolkit getInstance() {
		if ( instance == null ) {
			instance = new JacksonToolkit();
		}
		return instance;
	}
	
	/**
	 * Given a url to Web Services this method retrieves the JSON response from
	 * web services and converts that to a JsonNode from the Jackson Library.
	 * @param url web service URL to query
	 * @param element element name corresponding to the JSON node, typically the name of an array of objects
	 * (if null don't position at the array name)
	 * @return JsonNode of returned value from web services request.
	 * @throws JsonParseException if a JSON parse error
	 * @throws JsonMappingException if a JSON mapping error
	 * @throws MalformedURLException if a bad URL
	 * @throws IOException typically a timeout
	 */
	public JsonNode getJsonNodeFromWebServiceUrl(String url, String element)
		throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		String routine = getClass().getSimpleName() + ".getJsonNodeFromWebServicesUrl";
		JsonNode results = null;
		URL request = null;
		
		// TODO smalers 219-09-04 this is in HydroBase REST but results in double query of the web service!
		//if ( !httpResponse200(url) ) {
			//Message.printWarning(2, routine, "Error: " + url + " returned a 404 error");
			//return null;
		//}
		
		//System.out.println(url);
		
		try {
			request = new URL(url);
			JsonNode rootNode = this.mapper.readTree(request);
			results = rootNode;
			// For now return the root node.
			if ( (element != null) && !element.isEmpty() ) {
				// Position the node at the requested name.
				results = rootNode.path(element);
			}
		}
		catch ( JsonParseException e ) {
			Message.printWarning(2, routine, "Error parsing JSON response from \"" + request + "\" (" + e + ").");
			throw e;
		}
		catch ( JsonMappingException e ) {
			Message.printWarning(2, routine, "Error mapping JSON response from \"" + request + "\" (" + e + ").");
			throw e;
		}
		catch ( MalformedURLException e ) {
			Message.printWarning(2, routine, "Malformed URL has occured. URL=\"" + url + "\" (" + e + ").");
			throw e;
		}
		catch ( IOException e ) {
			Message.printWarning(2, routine, "IOException (" + e + ").");
			throw e;
		}
		
		return results;
	}
	
	/**
	 * Return the object mapper used with the toolkit.
	 * The object mapper can be reused.
	 */
	public ObjectMapper getObjectMapper () {
		return this.mapper;
	}

	/**
	 * Checks to see if the request string returns a response 200 or an error 404.
	 * @param urlString String representing the URL request from web services.
	 * @return true if request came back okay, with a response 200, false if response 404.
	 */
	public boolean httpResponse200 ( String urlString ) {
		HttpURLConnection connection;
		try {
			URL url = new URL(urlString);
			connection = (HttpURLConnection)url.openConnection();
			if ( connection.getResponseCode() == 200 ) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (MalformedURLException e) {
			return false;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Deserializes a JsonNode to a POJO class.
	 * This has the advantage of providing control over the process.
	 * null values are handled by setting to null in the POJO object if values are defined
	 * as classes (Integer, Double, etc.), but will be set to 0 if primitives are used.
	 * Therefore, if zeros are being used where not expected, define as an object and not primitive in the class.
	 * @param node - JsonNode to deserialize to POJO.
	 * @param objClass - The class that the JsonNode is to be deserialized to.
	 * @return the POJO that has been initialized via Jackson deserialization from the JsonNode data.
	 */
	public Object treeToValue(JsonNode node, Class<?> objClass) {
		String routine = getClass().getSimpleName() + ".treeToValue";
		try {
			return this.mapper.treeToValue(node, objClass);
		}
		catch (JsonParseException e ) {
			Message.printWarning(3, routine, "Error converting JSON response to class instance (" + e + ").");
			Message.printWarning(3, routine, e);
			return null;
		}
		catch (JsonMappingException e ) {
			Message.printWarning(3, routine, "Error converting JSON response to class instance (" + e + ").");
			Message.printWarning(3, routine, e);
			return null;
		}
		catch (IOException e) {
			Message.printWarning(3, routine, "IOException (" + e + ").");
			Message.printWarning(3, routine, e);
			return null;
		}
	}
}
