package com.plmtmobile.scala40se;

/**
 * Created by daniele on 21/10/13.
 */
public class EvaluationResult {

    public boolean      is_valid;
    public int          type;
    public int          total_cards;
    public int[]        cards;
    public int          score;
    public int          joker_value;

    EvaluationResult( boolean is_valid, int type, int total_cards, int score, int joker_value, int cards[] ) {
        this.is_valid           = is_valid;
        this.type               = type;
        this.total_cards        = total_cards;
        this.score              = score;
        this.joker_value        = joker_value;
        this.cards              = new int[ PlayerClass.MAX_PLAYER_CARDS * 2 ];
        if( cards != null ) {
            for( int i = 0; i < total_cards; i++ ) {
                this.cards[ i ] = cards[ i ];
            }
        }
    }
}
