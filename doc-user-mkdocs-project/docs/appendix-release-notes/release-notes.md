# TSTool / Synoptic Data Web Services Plugin / Release Notes #

Release notes are available for the core TSTool product and plugin.
The core software and plugins are maintained separately and may be updated at different times.

*   [TSTool core product release notes](http://opencdss.state.co.us/tstool/latest/doc-user/appendix-release-notes/release-notes/)
*   [TSTool Version Compatibility](#tstool-version-compatibility)
*   [Release Note Details](#release-note-details)
*   [GitHub repository issues](https://github.com/OpenWaterFoundation/owf-tstool-synoptic-plugin/issues) - outstanding issues

----

## TSTool Version Compatibility ##

The following table lists TSTool and plugin software version compatibility.

**<p style="text-align: center;">
TSTool and Plugin Version Compatibility
</p>**

| **Plugin Version** | **Required TSTool Version** | **Comments** |
| -- | -- | -- |
| 2.0.0 | >=  15.0.0 | TSTool and plugin updated to Java 11, new plugin manager. |
| 1.0.0 | >= 14.0.0 | |

## Release Note Details ##

Release notes for specific versions are listed below.

*   [Version 2.0.0](#version-200)
*   [Version 1.0.0](#version-100)

----------

## Version 2.0.0 ##

**Major release to use Java 11.**

*   ![change](change.png) Update the plugin to use Java 11:
    +   The Java version is consistent with TSTool 15.0.0.
    *   The plugin installation now uses a version folder,
        which allows multiple versions of the plugin to be installed at the same time,
        for use with different versions of TSTool.

## Version 1.0.0 ##

**Initial release.**

*   ![new](new.png) [1.0.0] Enable interactive browsing of Synoptic web service time series.
*   ![new](new.png) [1.0.0] Enable [Synoptic `TSID`](../command-ref/TSID/TSID.md) command support
*   ![new](new.png) [1.0.0] Enable the [`ReadSynoptic`](../command-ref/ReadSynoptic/ReadSynoptic.md) plugin command
