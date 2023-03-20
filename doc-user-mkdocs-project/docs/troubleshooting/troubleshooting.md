# TSTool / Troubleshooting #

Troubleshooting TSTool for Synoptic involves confirming that the core product and plugin are performing as expected.
Issues may also be related to Synoptic data.

*   [Troubleshooting Core TSTool Product](#troubleshooting-core-tstool-product)
*   [Troubleshooting Synoptic TSTool Integration](#troubleshooting-synoptic-tstool-integration)
    +   [***Commands(Plugin)*** Menu Contains Duplicate Commands](#commandsplugin-menu-contains-duplicate-commands)
    +   [Web Service Datastore Returns no Data](#web-service-datastore-returns-no-data)

------------------

## Troubleshooting Core TSTool Product ##

See the main [TSTool Troubleshooting documentation](https://opencdss.state.co.us/tstool/latest/doc-user/troubleshooting/troubleshooting/).

## Troubleshooting Synoptic TSTool Integration ##

The following are typical issues that are encountered when using TSTool with Synoptic.
The ***View / Datastores*** menu item will display the status of datastores.
The ***Tools / Diagnostics - View Log File...*** menu item will display the log file.

### ***Commands(Plugin)*** Menu Contains Duplicate Commands ###

If the ***Commands(Plugin)*** menu contains duplicate commands,
TSTool is finding multiple plugin `jar` files.
To fix, check the `plugins` folder and subfolders for the software installation folder
and the user's `.tstool/NN/plugins` folder.
Remove extra jar files, leaving only the version that is desired (typically the most recent version).

### Web Service Datastore Returns no Data ###

If the web service datastore returns no data, check the following:

1.  Confirm that the input parameters are correct:
    1.  Check the ***Status*** property for the time series in the ***Time Series List*** area.
        Confirm that the station is active.
    2.  Confirm that the period of record for the time series in the ***Time Series List*** area overlaps the requested period.
    3.  If no [`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command is specified,
        a default period of the last month is used,
        in order to minimize load on Synoptic web services.
        Some stations may be temporarily offline, for example due to winter conditions or maintenance.
        If necessary, use a longer perod to confirm that some data are available.
    4.  Use the time series table view to view data and confirm that values are not missing
        (e.g., `NaN` or `null` data values will be indicated as missing).
2.  Review the TSTool log file for errors.
    Typically a message will indicate an HTTP error code for the URL that was requested.
3.  Copy and paste the URL into a web browser to confirm the error.
    The browser will typically show a specific web service error message such as a
    missing query parameter or typo.
4.  See the [Synoptic API documentation](https://developers.synopticdata.com/mesonet/)
    to check whether the URL is correct.
5.  Contact Synoptic support for the data publisher to determine whether data limits are in place.

If the issue cannot be resolved, contact the [Open Water Foundation](https://openwaterfoundation.org/about-owf/staff/).
