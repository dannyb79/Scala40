package com.plmtmobile.scala40se;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by daniele on 25/10/13.
 */
public class PermanentData {

    private static final String  PERMDATA_FILENAME          = "preferences";
    private static final String  PERMDATA_TOTALGAMES        = "PERMDATA_TOTALGAMES";
    private static final String  PERMDATA_WONGAMES          = "PERMDATA_WONGAMES";
    private static final String  PERMDATA_PLAYERSCORE       = "PERMDATA_PLAYER_FINALSCORE";
    private static final String  PERMDATA_DEVICESCORE       = "PERMDATA_DEVICE_FINALSCORE";
    private static final String  PERMDATA_FRAUDOPTION       = "PERMDATA_FRAUDOPTION";

    private static final String  PERMDATA_RANKOPTION        = "PERMDATA_RANKOPTION";
    private static final String  PERMDATA_HASHSETUP         = "PERMDATA_HASHSETUP";
    private static final String  PERMDATA_USERHASH          = "PERMDATA_USERHASH";
    private static final String  PERMDATA_USERNAME          = "PERMDATA_USERNAME";
    private static final String  PERMDATA_USERTOTALGAMES    = "PERMDATA_USERTOTALGAMES";
    private static final String  PERMDATA_USERWONGAMES      = "PERMDATA_USERWONGAMES";
    private static final String  PERMDATA_USERSCORE         = "PERMDATA_USERSCORE";

    private static final String  PERMDATA_TOURNAMENTENABLE  = "PERMDATA_TOURNAMENTENABLE";
    private static final String  PERMDATA_TOURNAMENTLIMIT   = "PERMDATA_TOURNAMENTLIMIT";

    private static final String  PERMDATA_CARDSELECTOR      = "PERMDATA_CARDSELECTOR";

    private static final String  PERMDATA_TOURNAMENT_DEVICE_SCORE   = "PERMDATA_TOURNAMENT_DEVICE_SCORE";
    private static final String  PERMDATA_TOURNAMENT_PLAYER_SCORE   = "PERMDATA_TOURNAMENT_PLAYER_SCORE";
    private static final String  PERMDATA_TOURNAMENT_GAMES          = "PERMDATA_TOURNAMENT_GAMES";
    private static final String  PERMDATA_TOTAL_TOURNAMENTS         = "PERMDATA_TOTAL_TOURNAMENTS";
    private static final String  PERMDATA_WON_101_TOURNAMENTS       = "PERMDATA_WON_101_TOURNAMENTS";
    private static final String  PERMDATA_WON_201_TOURNAMENTS       = "PERMDATA_WON_201_TOURNAMENTS";

    private static final String  PERMDATA_STRAIGHT_SORT_MODE        = "PERMDATA_STRAIGHT_SORT_MODE";
    private static final String  PERMDATA_VALUES_SORT_MODE        = "PERMDATA_VALUES_SORT_MODE";

    private static final String  PERMDATA_ONLINE_NAME               = "PERMDATA_ONLINE_NAME";
    public static final int         ONLINESETTINGS_ACCOUNT_NAME     = 0;
    public static final int         ONLINESETTINGS_USER_NICKNAME    = 1;
    private static final String  PERMDATA_BACKGROUND_IMAGE          = "PERMDATA_BACKGROUND_IMAGE";

    private Context mContext;

    PermanentData( Context context ) {
        mContext = context;
    }

    public boolean reset_statistics() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOTALGAMES,     0 );
        editor.putInt( PERMDATA_WONGAMES,       0 );
        editor.putInt( PERMDATA_PLAYERSCORE,    0 );
        editor.putInt( PERMDATA_DEVICESCORE,    0 );
        return editor.commit();
    }

    public boolean get_fraud_option() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getBoolean( PERMDATA_FRAUDOPTION, false );
    }

    public boolean set_fraud_option( boolean status ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean( PERMDATA_FRAUDOPTION, status );
        // commit nuovi valori
        return editor.commit();
    }

    public int get_total_games() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_TOTALGAMES, 0 );
    }

    public int get_won_games() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_WONGAMES, 0 );
    }

    public int get_player_score() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_PLAYERSCORE, 0 );
    }

    public int get_device_score() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_DEVICESCORE, 0 );
    }

    public boolean endofgame_stats( boolean player_wins, int player_score, int device_score ) {
        int value = 0;

        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();

        // incrementa il numero totale di partite giocate
        value   =   settings.getInt( PERMDATA_TOTALGAMES, 0 );
        value   +=  1;
        editor.putInt( PERMDATA_TOTALGAMES, value );
        // se il giocatore ha vinto
        if( player_wins ) {
            // incrementa il numero totale di partite vinte dal giocatore
            value =     settings.getInt( PERMDATA_WONGAMES, 0 );
            value +=    1;
            editor.putInt( PERMDATA_WONGAMES, value );
        }
        // se il giocatore ha perso aggiurno il totale dei punti
        if( player_score > 0 ) {
            // incrementa il numero totale di partite vinte dal giocatore
            value =     settings.getInt( PERMDATA_PLAYERSCORE, 0 );
            value +=    player_score;
            editor.putInt( PERMDATA_PLAYERSCORE, value );
        }
        // se l'avversario (il dispositivo) ha perso aggiurno il totale dei punti
        if( device_score > 0 ) {
            // incrementa il numero totale di partite vinte dal giocatore
            value =     settings.getInt( PERMDATA_DEVICESCORE, 0 );
            value +=    device_score;
            editor.putInt( PERMDATA_DEVICESCORE, value );
        }
        // commit nuovi valori
        return editor.commit();
    }

    public boolean set_rank_option( boolean status ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean( PERMDATA_RANKOPTION, status );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean get_rank_option() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getBoolean( PERMDATA_RANKOPTION, false );
    }

    public boolean unset_hash() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        // flag hash gia' calcolato
        editor.putBoolean( PERMDATA_HASHSETUP, false );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean is_hash_set() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getBoolean( PERMDATA_HASHSETUP, false );
    }

    private String getRandomChar() {
        switch( ( int )( Math.random() * 20 ) ) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            case 3: return "D";
            case 4: return "E";
            case 5: return "F";
            case 6: return "G";
            case 7: return "H";
            case 8: return "J";
            case 9: return "R";
            case 10: return "0";
            case 11: return "1";
            case 12: return "2";
            case 13: return "3";
            case 14: return "4";
            case 15: return "5";
            case 16: return "6";
            case 17: return "7";
            case 18: return "8";
            case 19: return "9";
        }
        return "Z";
    }

    public boolean calculate_hash() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        // flag hash gia' calcolato
        editor.putBoolean( PERMDATA_HASHSETUP, true );
        // generazione dell'hash
        String hash = "";
        for( int i = 0; i < 20; i++ ) {
            hash += getRandomChar();
        }
        editor.putString( PERMDATA_USERHASH, hash );
        // azzera le statistiche riguardanti la classifica
        editor.putInt( PERMDATA_USERTOTALGAMES,     0 );
        editor.putInt( PERMDATA_USERWONGAMES,       0 );
        editor.putInt( PERMDATA_USERSCORE,          0 );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean save_username( String name ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( PERMDATA_USERNAME, name );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean set_hash( String hash ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( PERMDATA_USERHASH, hash );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean set_user_score( int user_score ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_USERSCORE, user_score );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean set_user_totalgames( int totalgames ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_USERTOTALGAMES, totalgames );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean set_user_wongames( int wongames ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_USERWONGAMES, wongames );
        // commit nuovi valori
        return editor.commit();
    }

    public boolean set_hash_is_set() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        // flag hash gia' calcolato
        editor.putBoolean( PERMDATA_HASHSETUP, true );
        // commit nuovi valori
        return editor.commit();
    }


    public String get_user_name() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getString( PERMDATA_USERNAME, "" );
    }

    public String get_user_hash() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getString( PERMDATA_USERHASH, "" );
    }

    public int get_user_score() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_USERSCORE, 0 );
    }

    public int get_user_totalgames() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_USERTOTALGAMES, 0 );
    }

    public int get_user_wongames() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_USERWONGAMES, 0 );
    }

    public boolean endofgame_rankstats( boolean player_wins, int player_score, int device_score ) {
        int value = 0;

        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();

        // incrementa il numero totale di partite giocate in modalita' classifica
        value   =   settings.getInt( PERMDATA_USERTOTALGAMES, 0 );
        value   +=  1;
        editor.putInt( PERMDATA_USERTOTALGAMES, value );
        // se il giocatore ha vinto
        if( player_wins ) {
            // incrementa il numero totale di partite vinte dal giocatore
            value =     settings.getInt( PERMDATA_USERWONGAMES, 0 );
            value +=    1;
            editor.putInt( PERMDATA_USERWONGAMES, value );
        }
        // se il giocatore ha perso il totale dei punti in mano viene scalatao dal punteggio corrente
        if( player_score > 0 ) {
            value =     settings.getInt( PERMDATA_USERSCORE, 0 );
            value -=    player_score;
            editor.putInt( PERMDATA_USERSCORE, value );
        }
        // se l'avversario (il dispositivo) ha perso il totale dei punti in mano viene aggiunto al punteggio del giocatore
        if( device_score > 0 ) {
            // incrementa il numero totale di partite vinte dal giocatore
            value =     settings.getInt( PERMDATA_USERSCORE, 0 );
            value +=    device_score;
            editor.putInt( PERMDATA_USERSCORE, value );
        }
        // commit nuovi valori
        return editor.commit();
    }





    /*
        ritorna true o false se abilitata la modalità gara a punti
    */
    public int get_card_selector() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_CARDSELECTOR, 0 );
    }

    /*
        imposta la modalita' gara a punti
    */
    public boolean set_card_selector( int new_selector ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_CARDSELECTOR, new_selector );
        // commit nuovi valori
        return editor.commit();
    }



    /*
        ritorna true o false se abilitata la modalità gara a punti
    */
    public boolean get_tournament_enabled() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getBoolean( PERMDATA_TOURNAMENTENABLE, false );
    }

    /*
        imposta la modalita' gara a punti
    */
    public boolean set_tournament_enabled( boolean status ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean( PERMDATA_TOURNAMENTENABLE, status );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il numero di punti impostato come limite gara
    */
    public int get_tournament_limit() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_TOURNAMENTLIMIT, 101 );
    }

    /*
        imposta il numero di punti limite della gara (101 o 201)
    */
    public boolean set_tournament_limit( int limit ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOURNAMENTLIMIT, limit );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il punteggio del giocatore relativo alla gara corrente
    */
    public int get_tournament_player_score() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_TOURNAMENT_PLAYER_SCORE, 0 );
    }

    /*
        imposta il punteggio del giocatore relativo alla gara corrente
    */
    public boolean set_tournament_player_score( int new_score ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOURNAMENT_PLAYER_SCORE, new_score );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il punteggio dell'avversario relativo alla gara corrente
    */
    public int get_tournament_device_score() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_TOURNAMENT_DEVICE_SCORE, 0 );
    }

    /*
        imposta il punteggio dell'avversario relativo alla gara corrente
    */
    public boolean set_tournament_device_score( int new_score ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOURNAMENT_DEVICE_SCORE, new_score );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il numero di partite effettuate nella gara corrente
    */
    public int get_tournament_games() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_TOURNAMENT_GAMES, 0 );
    }

    /*
        imposta il numero di partite effettuate nella gara corrente
    */
    public boolean set_tournament_games( int new_games ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOURNAMENT_GAMES, new_games );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il numero di gare effettuate
    */
    public int get_total_tournaments() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_TOTAL_TOURNAMENTS, 0 );
    }

    /*
        incrementa il numero di gare effettuate
    */
    public boolean inc_total_tournaments() {
        int total_tournaments = 0;
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        total_tournaments = settings.getInt( PERMDATA_TOTAL_TOURNAMENTS, 0 );
        total_tournaments += 1;
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOTAL_TOURNAMENTS, total_tournaments );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il numero di gare a 101 punti vinte
    */
    public int get_won_101_tournaments() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_WON_101_TOURNAMENTS, 0 );
    }

    /*
        incrementa il numero di gare effettuate
    */
    public boolean inc_won_101_tournaments() {
        int won_tournaments = 0;
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        won_tournaments = settings.getInt( PERMDATA_WON_101_TOURNAMENTS, 0 );
        won_tournaments += 1;
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_WON_101_TOURNAMENTS, won_tournaments );
        // commit nuovi valori
        return editor.commit();
    }

    /*
    ritorna il numero di gare a 101 punti vinte
*/
    public int get_won_201_tournaments() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_WON_201_TOURNAMENTS, 0 );
    }

    /*
        incrementa il numero di gare effettuate
    */
    public boolean inc_won_201_tournaments() {
        int won_tournaments = 0;
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        won_tournaments = settings.getInt( PERMDATA_WON_201_TOURNAMENTS, 0 );
        won_tournaments += 1;
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_WON_201_TOURNAMENTS, won_tournaments );
        // commit nuovi valori
        return editor.commit();
    }


    public boolean reset_tournament_data() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_TOURNAMENT_GAMES, 0 );
        editor.putInt( PERMDATA_TOURNAMENT_DEVICE_SCORE, 0 );
        editor.putInt( PERMDATA_TOURNAMENT_PLAYER_SCORE, 0 );
        // commit nuovi valori
        return editor.commit();
    }



    /*
    imposta il numero di partite effettuate nella gara corrente
*/
    public boolean set_straight_sort_mode( int mode ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_STRAIGHT_SORT_MODE, mode );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il numero di gare effettuate
    */
    public int get_straight_sort_mode() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_STRAIGHT_SORT_MODE, 0 );
    }

    /*
imposta il numero di partite effettuate nella gara corrente
*/
    public boolean set_values_sort_mode( int mode ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_VALUES_SORT_MODE, mode );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il numero di gare effettuate
    */
    public int get_values_sort_mode() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_VALUES_SORT_MODE, 0 );
    }

    /*
        imposta il nome da inviare nel gioco online
    */
    public boolean set_online_name( int mode ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_ONLINE_NAME, mode );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il nome inviato nel gioco online
    */
    public int get_online_name() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_ONLINE_NAME, 0 );
    }

    /*
        imposta il tipo di sfondo da visualizzare nel gioco
    */
    public boolean set_background_image( int type ) {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt( PERMDATA_BACKGROUND_IMAGE, type );
        // commit nuovi valori
        return editor.commit();
    }

    /*
        ritorna il nome inviato nel gioco online
    */
    public int get_background_image() {
        SharedPreferences settings = mContext.getSharedPreferences(PERMDATA_FILENAME, mContext.MODE_PRIVATE );
        return settings.getInt( PERMDATA_BACKGROUND_IMAGE, 0 );
    }


}
