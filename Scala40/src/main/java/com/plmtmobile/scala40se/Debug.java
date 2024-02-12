package com.plmtmobile.scala40se;

import android.util.Log;
import java.util.ArrayList;

/**
 * Created by daniele on 10/08/15.
 */
public class Debug {

    public static final String                  debugTag = "S40AUTO";

    Debug() {
        // costruttore
    }

    static public void LogBytesArray(String prefix,  byte[] data ) {
        String str = new String( prefix );
        for( int i = 0; i < data.length; i++ ) {
            // descrizione carta nel formato [Valore][Seme] + spazio
            str += String.format( "%2d ", data[ i ] );
        }
        Log.d( debugTag, str );
    }

    public void LogIntArray( String tag, int[] data, int length ) {
        String str = new String( tag + " " );
        for( int i = 0; i < length; i++ ) {
            // descrizione carta nel formato [Valore][Seme] + spazio
            str += String.format( "%2d ", data[ i ] );
        }
        Log.d( debugTag, str );
    }


    public String LogCardToString( int card_value ) {
        boolean addcardvalue = false;

        String s = new String( "" );
        if( card_value == DeckClass.BLACK_JOKER ) {
            s += "Wb ";
        } else if( card_value == DeckClass.RED_JOKER ) {
            s += "Wr ";
        } else if( card_value > DeckClass.RED_JOKER || card_value < 0 ) {
            s += "?? ";
        } else {
            switch( card_value % 13 ) {
                case 0: s += "A"; break;
                case 9: s += "T"; break;
                case 10: s += "J"; break;
                case 11: s += "Q"; break;
                case 12: s += "K"; break;
                default: s += String.format( "%d", ( card_value % 13 ) + 1 ); break;
            }
            switch( card_value / 13 ) {
                case 0: s += "c "; break;
                case 1: s += "q "; break;
                case 2: s += "p "; break;
                case 3: s += "f "; break;
                default: s += "? "; break;
            }
        }
        if( addcardvalue ) {
            s += "(";
            s += String.format( "%d", card_value );
            s += ") ";
        }
        return s;
    }

    public void LogCardsArray( int[] cards, int total_cards ) {
        String cardStr = new String( Integer.toString( total_cards ) + " carte : " );
        for( int i = 0; i < total_cards; i++ ) {
            // descrizione carta nel formato [Valore][Seme] + spazio
            cardStr += LogCardToString( cards[ i ] );
        }
        Log.d( debugTag, cardStr );
    }


    public void LogCardsArrayList( ArrayList<PlayerClass.PlayerCard> cards ) {
        String cardStr = new String( Integer.toString( cards.size() ) + " carte : " );
        for( int i = 0; i < cards.size(); i++ ) {
            // descrizione carta nel formato [Valore][Seme] + spazio
            cardStr += LogCardToString( cards.get( i ).value );
        }
        Log.d( debugTag, cardStr );
        /*
        String selectionStr = new String( Integer.toString( cards.size() ) + " selez : " );
        for( int i = 0; i < cards.size(); i++ ) {
            // descrizione carta nel formato [Valore][Seme] + spazio
            selectionStr += ( cards.get( i ).selected ? "1  " : "0  " );
        }
        Log.d( debugTag, selectionStr );
        */
    }

    public void LogSelectedCardsArrayList( ArrayList<PlayerClass.SelectedCard> cards ) {
        String cardStr = new String( Integer.toString( cards.size() ) + " carte selezionate : " );
        for( int i = 0; i < cards.size(); i++ ) {
            // descrizione carta nel formato [Valore][Seme] + spazio
            cardStr += LogCardToString( cards.get( i ).value );
        }
        Log.d( debugTag, cardStr );
    }

    public int checkCardsArray( ArrayList<PlayerClass.PlayerCard> cards ) {
        int         result      = -1;
        final int   total_cards = 54;
        int         cardcount[] = new int[ total_cards ];
        // azzera il totalizzatore delle carte
        for( int i = 0; i < total_cards; i++ ) {
            cardcount[ i ] = 0;
        }
        // debug di ogni singola carta
        /*
        for( int i = 0; i < cards.size(); i++ ) {
            Log.d( debugTag, "Carta " + i + " : " +  cards.get( i ).value );
            cardcount[ cards.get( i ).value ] += 1;
        }
        */
        // ogni carta deve comparire al massimo 2 volte (essendo due mazzi)
        for( int i = 0; i < total_cards; i++ ) {
            if( cardcount[ i ] > 2 ) {
                // ERRORE!!!
                // esito negativo, ritorna il valore della carta errata
                result = i;
                break;
            }
        }

        // debug conteggio per ogni tipo di carta
        /*
        String tmpstr = "";
        for( int i = 0; i < total_cards; i++ ) {
            tmpstr += Integer.toString( cardcount[ i ] );
        }
        Log.d( debugTag, tmpstr );
        */

        return result;
    }

}
