# TSTool / Command / TSID for Synoptic #

*   [Overview](#overview)
*   [Command Editor](#command-editor)
*   [Command Syntax](#command-syntax)
*   [Examples](#examples)
*   [Troubleshooting](#troubleshooting)
*   [See Also](#see-also)

-------------------------

## Overview ##

The TSID command for Synoptic causes a single time series to be read from Synoptic web services using default parameters.
A TSID command is created by copying a time series from the ***Time Series List*** in the main TSTool interface
to the ***Commands*** area.
TSID commands can also be created by editing the command file with a text editor.

See the [Synoptic Datastore Appendix](../../datastore-ref/Synoptic/Synoptic.md) for information about TSID syntax.

See also the [`ReadSynoptic`](../ReadSynoptic/ReadSynoptic.md) command,
which reads one or more time series and provides parameters for control over how data are read.

The TSTool Synoptic plugin automatically manipulates time series timestamps to be consistent
with TSTool, as follows:

*   Irregular interval time series:
    +   use timestamps from Synoptic web services without changing
*   Regular interval time series:
    +   currently all Synoptic time series are treated as irregular given that the
        focus is on real-time data and Synoptic timestamps do not seem to 
        require alignment on time boundaries
    +   this approach may change in the future
        
## Command Editor ##

All TSID commands are edited using the general
[`TSID`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/TSID/TSID/)
command editor.

## Command Syntax ##

See the [Synoptic Datastore Appendix](../../datastore-ref/Synoptic/Synoptic.md) for information about TSID syntax.

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-Synoptic-plugin/tree/main/test/commands/TSID/).

## Troubleshooting ##

*   See the [`ReadSynoptic` command troubleshooting](../ReadSynoptic/ReadSynoptic.md#troubleshooting) documentation.

## See Also ##

*   [`ReadSynoptic`](../ReadSynoptic/ReadSynoptic.md) command for full control reading Synoptic time series
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command - provides more flexibility than a TSID
