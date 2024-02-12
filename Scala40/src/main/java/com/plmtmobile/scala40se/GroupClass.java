package com.plmtmobile.scala40se;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by daniele on 20/10/13.
 */
public class GroupClass implements Serializable {

    static final long serialVersionUID = -3026980480194610254L;

    // definizioni pubbliche
    public static final int     GROUP_TYPE_UNSET        = 0;
    public static final int     GROUP_TYPE_STRAIGHT     = 1;
    public static final int     GROUP_TYPE_COMBINATION  = 2;


    public static final int     GROUP_MAX_CARDS         = 14;

    public int                  total_cards;
    public int []               cards;
    public int                  type;
    public int                  owner;
    public int                  score;
    public int                  joker_value;

    GroupClass( int owner, int total_cards, int cards[], int type, int joker_value, int score ) {
        this.owner          = owner;
        this.total_cards    = total_cards;
        this.type           = type;
        this.joker_value    = joker_value;
        this.score          = score;
        this.cards          = new int[ GROUP_MAX_CARDS ];
        for( int i = 0; i < total_cards; i++ ) {
            this.cards[ i ] = cards[ i ];
        }
    }

    public void modify( int total_cards, int cards[], int type, int joker_value, int score ) {
        this.total_cards    = total_cards;
        this.type           = type;
        this.joker_value    = joker_value;
        this.score          = score;
        for( int i = 0; i < total_cards; i++ ) {
            this.cards[ i ] = cards[ i ];
        }
    }
}
