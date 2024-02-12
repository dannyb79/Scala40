package com.plmtmobile.scala40se;

/**
 * Created by daniele on 23/10/13.
 */
public class StrategyResult {
    public boolean  str_device_opened   = false;
    public int      str_opening_score   = 0;
    public boolean  str_device_attach   = false;
    public boolean  str_device_newgroup = false;
    public int      error               = 0;
    public boolean  str_discard_failed  = false;

    StrategyResult( boolean str_device_opened, int str_opening_score, boolean str_device_newgroup, boolean str_device_attach, int error, boolean str_discard_failed ) {
        this.str_device_opened      = str_device_opened;
        this.str_opening_score      = str_opening_score;
        this.str_device_newgroup    = str_device_newgroup;
        this.str_device_attach      = str_device_attach;
        this.error                  = error;
        this.str_discard_failed     = str_discard_failed;
    }
}
