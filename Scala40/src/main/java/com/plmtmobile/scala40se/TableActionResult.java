package com.plmtmobile.scala40se;

/**
 * Created by daniele on 22/10/13.
 */
public class TableActionResult {
    public  boolean     update_screen;
    public  boolean     opening;
    public  boolean     score_update;
    public  boolean     cannot_discard;
    public  int         error;

    TableActionResult( boolean update_screen, boolean opening, boolean score_update, boolean cannot_discard, int error ) {
        this.update_screen  = update_screen;
        this.opening        = opening;
        this.score_update   = score_update;
        this.cannot_discard = cannot_discard;
        this.error          = error;
    }
}
