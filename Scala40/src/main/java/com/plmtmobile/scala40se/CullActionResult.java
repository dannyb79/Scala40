package com.plmtmobile.scala40se;

/**
 * Created by daniele on 22/10/13.
 */
public class CullActionResult {
    public  boolean     update_screen;
    public  boolean     discarded_card;
    public  boolean     open_failure;
    public  boolean     cull_pick_failure;

    CullActionResult( boolean update_screen, boolean discarded_card, boolean open_failure, boolean cull_pick_failure ) {
        this.update_screen      = update_screen;
        this.discarded_card     = discarded_card;
        this.open_failure       = open_failure;
        this.cull_pick_failure  = cull_pick_failure;
    }
}
