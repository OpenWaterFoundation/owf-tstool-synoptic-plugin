// SynopticDataStoreFactory - class to create a SynopticDataStore instance

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

package org.openwaterfoundation.tstool.plugin.synoptic.datastore;

import java.net.URI;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import riverside.datastore.DataStore;
import riverside.datastore.DataStoreFactory;

public class SynopticDataStoreFactory implements DataStoreFactory {

	/**
	Create a SynopticDataStore instance.
	@param props datastore configuration properties, such as read from the configuration file
	*/
	public DataStore create ( PropList props ) {  
	    String name = props.getValue ( "Name" );
	    String description = props.getValue ( "Description" );
	    if ( description == null ) {
	        description = "";
	    }
	    String serviceRootURI = props.getValue ( "ServiceRootURI" );
	    if ( serviceRootURI == null ) {
	    	System.out.println("Synoptic datastore ServiceRootURI is not defined.");
	    }
	    String apiToken = props.getValue ( "ApiToken" );
	    if ( apiToken == null ) {
	    	System.out.println("Synoptic datastore ApiToken is not defined.");
	    }
	    try {
	        DataStore ds = new SynopticDataStore ( name, description, new URI(serviceRootURI), props );
	        return ds;
	    }
	    catch ( Exception e ) {
	        Message.printWarning(3,"",e);
	        throw new RuntimeException ( e );
	    }
	}
}