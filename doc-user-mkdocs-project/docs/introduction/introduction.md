# TSTool Synoptic Plugin / Introduction #

*   [Introduction](#introduction)
*   [TSTool use with Synoptic Web Services](#tstool-use-with-synoptic-web-services)

----------------------

## Introduction ##

TSTool is a powerful software tool that automates time series processing and product generation.
It was originally developed for the State of Colorado to process data for basin modeling and has since
been enhanced to work with many data sources including:

*   United States Geological Survey (USGS) web service and file formats
*   Natural Resources Conservation Service (NRCS) web services
*   Regional Climate Center (RCC) Applied Climate Information Service (ACIS) web services
*   US Army Corps of Engineers DSS data files
*   others

TSTool is maintained by the Open Water Foundation,
which also enhances the software based on project needs.

*   See the latest [TSTool Documentation](https://opencdss.state.co.us/tstool/latest/doc-user/) to learn about core TSTool features.
*   See the [TSTool Download website](https://opencdss.state.co.us/tstool/) for the most recent software versions and documentation.
*   See the [Synoptic Plugin download page](https://software.openwaterfoundation.org/tstool-synoptic-plugin/).

## TSTool use with Synoptic Web Services ##

Synoptic Web Services provide access to data that are maintained in the
[Synoptic Data](https://synopticdata.com/) system.
See the following resources:

*   [Synoptic Web Service Documentation](https://synopticdata.com/mesonet-api)

The [Synoptic datastore documentation](../datastore-ref/Synoptic/Synoptic.md) describes how TSTool integrates with Synoptic.

The [`ReadSynoptic`](../command-ref/ReadSynoptic/ReadSynoptic.md) command can be used to read time series,
in addition to time series identifiers that are generated from the main TSTool interface.
