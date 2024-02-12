package com.plmtmobile.scala40se;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.zip.Adler32;

/**
 * Created by daniele on 13/01/14.
 */
public class MPCOM {

    /*
        definizione comandi
    */
    public static final int     MPCOMCMD_RANDOM_VALUE       = 1;
    public static final int     MPCOMCMD_NICKNAME           = 2;
    public static final int     MPCOMCMD_GAME_DATA          = 3;
    public static final int     MPCOMCMD_READY_TO_START     = 4;
    public static final int     MPCOMCMD_END_TURN_DATA      = 5;
    public static final int     MPCOMCMD_END_TURN_SCORE     = 6;
    public static final int     MPCOMCMD_WANT_REPLAY        = 7;
    public static final int     MPCOMCMD_USER_MESSAGE       = 8;
    public static final int     MPCOMCMD_LEAVE_GAME         = 9;


    /*
        costruttore
    */
    MPCOM() {

    }

    /*
        ritorna un valore casuale di dimensioni sufficienti a non generarne uno identico
    */
    public int getMPCOMRandomValue() {
        return (int)( Math.random()*1000000000 );
    }

    /*
        creazione pacchetto dati da inviare all'avversrio contenente il numero random locale
    */
    public byte[] wrapLocalValue( int local_value ) {

        byte[] temp = new byte[ 4 ];
        temp[ 0 ] = (byte)( ( local_value >> 0  ) & 0xff );
        temp[ 1 ] = (byte)( ( local_value >> 8  ) & 0xff );
        temp[ 2 ] = (byte)( ( local_value >> 16 ) & 0xff );
        temp[ 3 ] = (byte)( ( local_value >> 24 ) & 0xff );

        byte[] wrappedData = wrapData( MPCOMCMD_RANDOM_VALUE, temp );
        return wrappedData;
    }

    /*
        estrazione del numero random dell'avversario dal pacchetto dati ricevuto
    */
    public int convertByteArrayToInteger( byte[] buf ) {
        int value = 0;
        value = ByteBuffer.wrap( buf ).order( ByteOrder.LITTLE_ENDIAN ).getInt();
        return value;
    }

    /*
        creazione pacchetto dati contenente il nickname del giocatore
    */
    public byte[] wrapUserName( String name ) {

        try {
            // serializza il nickname...
            byte[] temp = toByteArray( name );
            // .. e crea il pacchetto da inviare
            byte[] wrappedData = wrapData( MPCOMCMD_NICKNAME, temp );
            return wrappedData;
        } catch ( IOException e ) {
            e.getMessage();
            // nickname, vuoto
            byte[] wrappedData = wrapData( MPCOMCMD_NICKNAME, null );
            return wrappedData;
        }
    }

    /*
        creazione pacchetto dati contenente il messaggio del giocotore
    */
    public byte[] wrapUserMessage( String name ) {

        try {
            // serializza il nickname...
            byte[] temp = toByteArray( name );
            // .. e crea il pacchetto da inviare
            byte[] wrappedData = wrapData( MPCOMCMD_USER_MESSAGE, temp );
            return wrappedData;
        } catch ( IOException e ) {
            e.getMessage();
            // nickname, vuoto
            byte[] wrappedData = wrapData( MPCOMCMD_USER_MESSAGE, null );
            return wrappedData;
        }
    }



    /*
        creazione pacchetto dati contenente i dati di inizio partita
    */
    public byte[] wrapStartGameData( PlayerClass rival, DeckClass deck ) {

        // la parte attiva deve inviare tutte le informazioni necessarie alla parte passiva
        // affinche' la partita cominci senza disallineamenti. I dati da inviare sono:
        // - le carte con cui il giocatore (parte passiva) deve cominciare (13 bytes)
        // - la carta (e anche l'unica) del pozzo (1 byte)
        // - tutte le carte del tallone (81 bytes)
        // il pacchetto e' complessivamente formato da 13 + 1 + 81 = 95 bytes
        byte[] data = new byte[ 95 ];

        // carte con cui il giocatore (parte passiva) deve cominciare
        for( int i = 0; i < 13; i++ ) {
            data[ i ] = ( byte )( rival.cards.get( i ).value );
        }
        // carta del pozzo
        int cull_card_pos = deck.cull_size - 1;
        if( cull_card_pos >= 0 ) {
            data[ 13 ] = ( byte ) ( deck.cull_cards[ cull_card_pos ] );
        } else {
            data[ 13 ] = 0;
        }
        // carte del tallone
        for( int i = 0; i < 81; i++ ) {
            data[ 14 + i ] = ( byte )( deck.pool_cards[ i ] );
        }
        // incapsulamento
        byte[] wrappedData = wrapData( MPCOMCMD_GAME_DATA, data );
        return wrappedData;

// versioni fino a 2.1
/*
        byte[] data = new byte[ 14 ];   // 13 carte dell'avversario + carta pozzo

        // inserimento carte dell'avversario
        for( int i = 0; i < 13; i++ ) {
            data[ i ] = ( byte )( rival.cards.get( i ).value );
        }
        // inserimento carta del pozzo
        int cull_card_pos = deck.cull_size - 1;
        if( cull_card_pos >= 0 ) {
            data[ 13 ] = ( byte ) ( deck.cull_cards[ cull_card_pos ] );
        } else {
            data[ 13 ] = 0;
        }
        // incapsulamento
        byte[] wrappedData = wrapData( MPCOMCMD_GAME_DATA, data );
        return wrappedData;
*/
    }

    /*
        creazione pacchetto dati contenente il messaggio "pronti a cominciare"
    */
    public byte[] wrapReadyToStart() {
        byte[] wrappedData = wrapData( MPCOMCMD_READY_TO_START, null );
        return wrappedData;
    }


    /*
        creazione pacchetto dati contenente i dati di fine turno
    */
    public byte[] wrapEndTurnData( PlayerClass player, DeckClass deck, ArrayList<GroupClass> groups ) {

        int total_pool_cards    = deck.pool_size;
        int total_cull_cards    = deck.cull_size;
        int total_groups_data   = 0;
        int pos                 = 0;

        try {
            byte[] groupsdata = toByteArray( groups );
            total_groups_data = groupsdata.length;

            byte[] data = new byte[ 1 + 1 + total_pool_cards + 1 + total_cull_cards + total_groups_data ];

            // n. carte del giocatore
            pos = 0;
            data[ pos ] = ( byte )( player.cards.size() );
            pos += 1;
            // n. carte del tallone
            data[ pos ] = ( byte )( total_pool_cards );
            pos += 1;
            // carte del tallone
            for( int i = 0; i < total_pool_cards; i++ ) {
                data[ pos ] = ( byte ) deck.pool_cards[ i ];
                pos += 1;
            }
            // n. carte del pozzo
            data[ pos ] = ( byte )( total_cull_cards );
            pos += 1;
            // carte del pozzo
            for( int i = 0; i < total_cull_cards; i++ ) {
                data[ pos ] = ( byte ) deck.cull_cards[ i ];
                pos += 1;
            }
            // aggiunge i dati del tavolo
            for( int i = 0; i < total_groups_data; i++ ) {
                data[ pos ] = groupsdata[ i ];
                pos += 1;
            }

            byte[] wrappedData = wrapData( MPCOMCMD_END_TURN_DATA, data );
            return wrappedData;

        } catch ( IOException e ) {
            e.getMessage();
        }
        return null;
    }

    /*
        creazione pacchetto dati contenente il valore punti a fine partita
    */
    public byte[] wrapEndOfTurnScore( int score ) {
        byte[] temp = new byte[ 4 ];
        temp[ 0 ] = (byte)( ( score >> 0  ) & 0xff );
        temp[ 1 ] = (byte)( ( score >> 8  ) & 0xff );
        temp[ 2 ] = (byte)( ( score >> 16 ) & 0xff );
        temp[ 3 ] = (byte)( ( score >> 24 ) & 0xff );
        byte[] wrappedData = wrapData( MPCOMCMD_END_TURN_SCORE, temp );
        return wrappedData;
    }

    /*
        creazione pacchetto dati contenente il messaggio "voglio rigiocare"
    */
    public byte[] wrapWantReplay() {
        byte[] wrappedData = wrapData( MPCOMCMD_WANT_REPLAY, null );
        return wrappedData;
    }


    /*
        creazione pacchetto dati contenente il messaggio "abbandono il gioco"
    */
    public byte[] wrapLeaveGame() {
        byte[] wrappedData = wrapData( MPCOMCMD_LEAVE_GAME, null );
        return wrappedData;
    }



    /*
        controllo validita' pacchetto dati (verifica dell'Adler32)
    */
    public boolean checkData( byte[] data ) {

        boolean result = false;

        do {
            if( data == null )
                break;
            if( data.length < 5 )
                break;
            if( !checkAdler32( data ) )
                break;
            // ok!
            result = true;
        } while( false );

        return result;
    }


    /*
        conversione di un oggetto in array di bytes (serializzazione)
    */
    private byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } catch ( Exception e ) {
            e.getMessage();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    /*
        conversione di array di bytes in oggetto (deserializzazione)
    */
    public Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }

    /*
    inclusione dati in un pacchetto nel seguente formato:
    [ comando ][ dati ][ adler32 4 bytes ]
*/
    private byte[] wrapData( int command, byte[] data ) {

        int final_total_data    = 0;
        int total_data          = 0;

        if( data == null ) {
            total_data          = 0;
            final_total_data    = 5;
        } else {
            total_data          = data.length;
            final_total_data    = total_data + 5;
        }

        byte[]      wrappedData     = new byte[ final_total_data ];


        wrappedData[ 0 ] = (byte)command;
        for( int i = 0; i < total_data; i++ )
            wrappedData[ i + 1 ] = data[ i ];
        // il checksum viene calcolato sul pacchetto composto da: comando + dati
        Adler32     adler = new Adler32();
        adler.reset();
        adler.update( wrappedData, 0, total_data + 1 );
        long adlervalue = adler.getValue();
        wrappedData[ total_data + 1 + 0 ] = (byte)( adlervalue & 0xff );
        wrappedData[ total_data + 1 + 1 ] = (byte)( ( adlervalue >> 8  ) & 0xff );
        wrappedData[ total_data + 1 + 2 ] = (byte)( ( adlervalue >> 16 ) & 0xff );
        wrappedData[ total_data + 1 + 3 ] = (byte)( ( adlervalue >> 24 ) & 0xff );

        return wrappedData;
    }

    /*
        verifica che l'adler32 della parte comando+dati coincida
        con l'adler contenuto in fondo al pacchetto dati
        [ comando ][ dati ][ adler32 4 bytes ]
    */
    private boolean checkAdler32( byte[] data ) {
        boolean     result      = false;
        Adler32     adler       = new Adler32();
        long        adlervalue  = 0;
        int         adlerpos    = data.length - 4;

        if( data.length >= 4 ) {
            adler.reset();
            adler.update( data, 0, data.length - 4 );
            adlervalue = adler.getValue();
            do {
                if( (byte)( adlervalue & 0xff ) != data[ adlerpos ] ) break;
                if( (byte)( ( adlervalue >> 8  ) & 0xff ) != data[ adlerpos + 1 ] ) break;
                if( (byte)( ( adlervalue >> 16  ) & 0xff ) != data[ adlerpos + 2 ] ) break;
                if( (byte)( ( adlervalue >> 24  ) & 0xff ) != data[ adlerpos + 3 ] ) break;
                result = true;
            } while( false );
        }
        return result;
    }
}
