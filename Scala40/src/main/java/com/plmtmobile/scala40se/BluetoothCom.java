package com.plmtmobile.scala40se;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.zip.Adler32;

/**
 * Created by daniele on 22/11/13.
 */
public class BluetoothCom {

    /*
        definizione messaggi per l' handler di gioco
    */
    public static final int     MSG_BTCOM_DELIVERY_SUCCESSFULL      = 100;
    public static final int     MSG_BTCOM_NO_RESPONSE_FROM_DEVICE   = 101;
    public static final int     MSG_BTCOM_SEND_ATTEMPT              = 102;
    public static final int     MSG_BTCOM_INVALID_MESSAGE           = 103;
    public static final int     MSG_BTCOM_RECEIVED_STARTDATA        = 104;
    public static final int     MSG_BTCOM_RECEIVED_ENDTURNDATA      = 105;
    public static final int     MSG_BTCOM_RECEIVED_SCOREVALUE       = 106;

    /*
        definizione comandi
    */
    public static final int     BTCOMCMD_STARTGAME              = 1;
    public static final int     BTCOMCMD_ENDTURN                = 2;
    public static final int     BTCOMCMD_SCOREVALUE             = 3;
    public static final int     BTCOMCMD_ACK                    = 10;

    /*
        stato comunicazione con dispositivo
    */
    public static final int     BTCOMSTATUS_IDLE                = 0;
    public static final int     BTCOMSTATUS_SENDING             = 1;


    private Handler             gameHandler;
    private int                 status;
    private int                 sendretry;

    /*
        costruttore
    */
    BluetoothCom( Handler gameHandler ) {
        // handler di gioco a cui comunicare i messaggi
        this.gameHandler        = gameHandler;
        // stato di idle
        status                  = BTCOMSTATUS_IDLE;
        // azzera n. di tentativi di trasmissione
        sendretry               = 0;
        // imposta l'handler (di questa classe!) a cui il servizio bluetooth deve comunicare gli eventi
        BluetoothService.getInstance().setHandler( bluetoothComHandler );
    }

    /*
        elimina tutti i messaggi dalla coda dell'handler
    */
    public void clearHandler() {
        bluetoothComHandler.removeCallbacksAndMessages( null );
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

    /*
        avvio procedura di invio messaggio di inizio partita
    */
    public void sendStartGameMsg( PlayerClass rival, DeckClass deck ) {

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

        byte[] wrappedData = wrapData( BTCOMCMD_STARTGAME, data );
        startSendingData( wrappedData );
    }

    /*
        - n. carte del giocatore
        - n. carte del tallone
        - carte del tallone
        - n. carte del pozzo
        - carte del pozzo
    */
    public void sendEndTurnMsg( PlayerClass player, DeckClass deck, ArrayList<GroupClass> groups ) {

        int total_pool_cards    = deck.pool_size;
        int total_cull_cards    = deck.cull_size;
        int total_groups_data   = 0;
        int total_player_cards  = 0;
        int pos                 = 0;

        try {
            byte[] groupsdata = toByteArray( groups );
            total_groups_data = groupsdata.length;
            total_player_cards = player.cards.size();
            byte[] data = new byte[ 1 + total_player_cards + 1 + total_pool_cards + 1 + total_cull_cards + total_groups_data ];

            // n. carte del giocatore
            data[ 0 ] = ( byte )( player.cards.size() );
            pos += 1;
            // carte del ciocatore
            for( int i = 0; i < total_player_cards; i++ ) {
                data[ pos ] = ( byte ) player.cards.get( i ).value;
                pos += 1;
            }

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


            byte[] wrappedData = wrapData( BTCOMCMD_ENDTURN, data );
            startSendingData( wrappedData );

        } catch ( IOException e ) {
            e.getMessage();
        }

    }

    /*
        invio punteggio per aggiornamento statistiche
    */
    public void sendScoreValue( int score ) {

        byte[] data = new byte[ 1 ];
        data[ 0 ] = (byte)score;
        // creazione pacchetto dati
        byte[] wrappedData = wrapData( BTCOMCMD_SCOREVALUE, null );
        // invia direttamente il messaggio attraverso il socket
        BluetoothService.getInstance().sendmsg( wrappedData );
    }


    /*
        invio di un ACK al ricevimento di un pacchetto dati formalmente valido
    */
    private void sendAck() {
        // creazione pacchetto dati
        byte[] wrappedData = wrapData( BTCOMCMD_ACK, null );
        // invia direttamente il messaggio attraverso il socket
        BluetoothService.getInstance().sendmsg( wrappedData );
    }

    /*
        preparazione alla sequenza di invii del messaggio
    */
    private void startSendingData( byte[] data ) {

        // se e' gia' in corso l'invio di un pacchetto annulla l'operazione
        if( status == BTCOMSTATUS_SENDING ) {
            return;
        }
        // imposta lo stato corrente in "invio dati" e azzera il n. di retry
        status      = BTCOMSTATUS_SENDING;
        sendretry   = 0;
        // invia messaggio all'handler
        bluetoothComHandler.obtainMessage( MSG_BTCOM_SEND_ATTEMPT, data ).sendToTarget();
    }

    /*
        decodifica il pacchetto dati ricevuto
    */
    private void parseReceivedData( int nbytes, byte[] data ) {

        // il pacchetto dati piu' piccolo e' composto da 5 bytes comando + adler32 (checksum)
        if( ( nbytes >= 5 ) && ( data != null ) ) {

            // verifica l'integrita' del pacchetto ricevuto (verifica dell'adler32)
            if( checkAdler32( data ) == true ) {

                // estrazione comando (data[0])
                switch( data[ 0 ] ) {

                    // ricezione dati di inizio partita
                    case BTCOMCMD_STARTGAME:

                        // comunico i dati all'handler di gioco
                        byte[] gdata = new byte[ nbytes ];
                        for( int i = 0; i < nbytes; i++ ) {
                            gdata[ i ] = data[ i ];
                        }
                        Message m = new Message();
                        m.what  = MSG_BTCOM_RECEIVED_STARTDATA;
                        m.obj   = gdata;
                        gameHandler.sendMessage( m );

                        // invia un ACK all'altro dispositivo per notificare l'avvenuto ricevimento del messaggio
                        sendAck();

                        break;

                    // ricezione dati di fine turno
                    case BTCOMCMD_ENDTURN:

                        // comunico i dati all'handler di gioco
                        byte[] tdata = new byte[ nbytes - 5 ];
                        for( int i = 0; i < ( nbytes - 5 ); i++ ) {
                            tdata[ i ] = data[ 1 + i ];
                        }
                        Message m2 = new Message();
                        m2.what  = MSG_BTCOM_RECEIVED_ENDTURNDATA;
                        m2.obj   = tdata;
                        gameHandler.sendMessage( m2 );

                        // invia un ACK all'altro dispositivo per notificare l'avvenuto ricevimento del messaggio
                        sendAck();

                        break;

                    // ricezione punteggio dell'avversario per aggiornamento statistiche
                    case BTCOMCMD_SCOREVALUE:

                        // comunico i dati all'handler di gioco
                        Message m3 = new Message();
                        m3.what  = MSG_BTCOM_RECEIVED_SCOREVALUE;
                        int score = (int)data[ 1 ];
                        m3.obj   = score;
                        gameHandler.obtainMessage( MSG_BTCOM_RECEIVED_SCOREVALUE, score, -1, null ).sendToTarget();

                        break;

                    // il dispositivo ha risposto con ACK
                    case BTCOMCMD_ACK:

                        if( status == BTCOMSTATUS_SENDING ) {
                            // annulla qualunque nuovo invio
                            bluetoothComHandler.removeMessages( MSG_BTCOM_SEND_ATTEMPT );
                            // azzera lo stato e il numero di retry
                            status      = BTCOMSTATUS_IDLE;
                            sendretry   = 0;
                            // lo comunico all'handler di gioco
                            gameHandler.obtainMessage( MSG_BTCOM_DELIVERY_SUCCESSFULL ).sendToTarget();
                        }

                        break;

                    default:
                        // comando ricevuto non valido, lo comunico all'handler di gioco
                        gameHandler.obtainMessage( MSG_BTCOM_INVALID_MESSAGE ).sendToTarget();

                        break;
                }
            }
        }
    }


    /*
        l'handler di gioco che riceve informazioni dal servizio bluetooth
    */
    private final Handler bluetoothComHandler = new Handler() {
        @Override
        public void handleMessage( Message msg ) {
            switch( msg.what ) {

                case MSG_BTCOM_SEND_ATTEMPT:

                    // invia fisicamente il messaggio attraverso il socket
                    BluetoothService.getInstance().sendmsg((byte[]) msg.obj);
                    // incrementa il numero di tentativi eseguiti
                    sendretry += 1;

                    // togliere!!! solo x debug
                    // gameHandler.obtainMessage( MSG_BTCOM_SEND_ATTEMPT, sendretry, 0 ).sendToTarget();

                    // riprogramma il messaggio nel caso non venga ricevuta risposta
                    if( sendretry < 5 ) {
                        // riprogramma un nuovo invio del messaggio posticipato
                        Message m = new Message();
                        m.what  = MSG_BTCOM_SEND_ATTEMPT;
                        m.obj   = msg.obj;
                        bluetoothComHandler.sendMessageDelayed( m, 2000 );

                    } else {
                        // comunica all'handler di gioco che e' non stata ricevuta alcuna rispota dal dispositivo
                        gameHandler.obtainMessage( MSG_BTCOM_NO_RESPONSE_FROM_DEVICE ).sendToTarget();
                        status      = BTCOMSTATUS_IDLE;
                        sendretry   = 0;
                    }

                    break;

                // e' stato ricevuto un messaggio, avvio decodifica pacchetto
                case BluetoothService.MSG_BT_READ_DATA:
                    // verifica l'integrita' e decodifica il messaggio
                    parseReceivedData( msg.arg1, ( byte[] ) msg.obj );
                    break;

                // tutti questi eventi vengono girati per conoscenza all'handler di gioco
                case BluetoothService.MSG_BT_CONNECTED:
                case BluetoothService.MSG_BT_CONNECTING:
                case BluetoothService.MSG_BT_LISTENING:
                case BluetoothService.MSG_BT_STOPPED:
                case BluetoothService.MSG_BT_CONNECTION_FAILED:
                case BluetoothService.MSG_BT_CONNECTION_LOST:
                    // messaggi che trasportano solo l'indice del messaggio
                    Message m1 = new Message();
                    m1.what = msg.what;
                    gameHandler.sendMessage( m1 );
                    break;
                case BluetoothService.MSG_BT_EXCEPTION:
                    // messaggi trasportano l'indice del messaggio e un oggetto (String per descrizione exception)
                    Message m2 = new Message();
                    m2.what  = msg.what;
                    m2.obj   = msg.obj;
                    gameHandler.sendMessage( m2 );
                    break;
                default:
                    // messaggi non previsti
                    break;
            }
        }
    };

}
