package com.plmtmobile.scala40se;

import java.util.Comparator;

/**
 * Created by daniele on 23/10/13.
 */
public class CardsStraightsComparator implements Comparator<PlayerClass.PlayerCard> {
    @Override
    public int compare(PlayerClass.PlayerCard o1, PlayerClass.PlayerCard o2) {
        if( o1.value > o2.value )
            return 1;
        else if ( o1.value < o2.value )
            return -1;
        else
            return 0;
    }
}
