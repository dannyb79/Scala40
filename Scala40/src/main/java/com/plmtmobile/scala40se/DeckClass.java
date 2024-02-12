package com.plmtmobile.scala40se;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by daniele on 20/10/13.
 */
public class DeckClass implements Serializable {
    /*
        definizioni pubbliche
    */
    public static final int     DECK_TOTAL_SIZE     = 108;  // n. totale di carte dei 2 singoli mazzi
    public static final int     BLACK_JOKER         = 52;   // id joker nero
    public static final int     RED_JOKER           = 53;   // id joker rosso
    public static final int     ACE_CARD            = 0;    // asso

    public int[]                pool_cards          = new int [ DECK_TOTAL_SIZE ];
    public int[]                cull_cards          = new int [ DECK_TOTAL_SIZE ];
    public int                  pool_size;
    public int                  cull_size;

    DeckClass() {
        pool_size   = 0;
        cull_size   = 0;
        Arrays.fill( pool_cards, 0 );
        Arrays.fill( cull_cards, 0 );
    }

    private void shuffle() {
        boolean out[] = new boolean[ DECK_TOTAL_SIZE ];
        int i, rnd;

        // inserisce in pool le carte mescolate
        Arrays.fill( out, false );
        for( i = 0; i < DECK_TOTAL_SIZE; i++ ) {
            do {
                rnd = (int)( Math.random() * DECK_TOTAL_SIZE );
            } while( out[ rnd ] );
            if( rnd >= 54 )
                pool_cards[ i ]    = rnd - 54;
            else
                pool_cards[ i ]    = rnd;
            out[ rnd ]  = true;
        }
    }

    public void reset() {
        pool_size   = DECK_TOTAL_SIZE;
        cull_size   = 0;
        Arrays.fill( pool_cards, 0 );
        Arrays.fill( cull_cards, 0 );
        shuffle();
    }

    public int pick_card() {
        int card = 0;
        if( pool_size > 0 ) {
            card = pool_cards[ pool_size - 1 ];
            pool_size -= 1;
            // se finiscono le carte del tallone...
            if( pool_size == 0 ) {
                // tute le carte del pozzo vengono usate per creare un nuovo tallone
                for( int i = 0; i < cull_size; i++ ) {
                    pool_cards[ i ] = cull_cards[ i ];
                }
                pool_size   = cull_size;
                cull_size   = 0;
                // la prima carta del nuovo tallone viene messa nel pozzo
                cull_cards[ 0 ] = pool_cards[ pool_size - 1 ];
                cull_size   += 1;
                pool_size   -= 1;
            }
        }
        return card;
    }

    public void add_cull( int card ) {
        cull_cards[ cull_size ] = card;
        cull_size += 1;
    }

    public int pick_from_cull() {
        int card = 0;
        if( cull_size > 0 ) {
            card = cull_cards[ cull_size - 1 ];
            cull_size -= 1;
        } else {
            // possono finire le carte del pozzo?
        }
        return card;
    }

}
