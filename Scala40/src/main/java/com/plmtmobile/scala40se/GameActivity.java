package com.plmtmobile.scala40se;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.plmtmobile.scala40se.GroupClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 *  Scala 40
 */
public class GameActivity extends Activity {

    /*
        definizioni stati di gioco
    */
    public static final int         GS_GAME_OVER                = 0;
    public static final int         GS_PLAYER_TURN              = 1;
    public static final int         GS_DEVICE_TURN              = 2;
    public static final int         GS_PLAYER_WINS              = 3;
    public static final int         GS_DEVICE_WINS              = 4;
    public static final int         GS_WAIT_GAME_SYNC           = 5;
    public static final int         GS_SEND_GAME_SYNC           = 6;
    public static final int         GS_SEND_END_TURN            = 7;
    public static final int         GS_WAIT_TURN                = 8;
    public static final int         GS_OPPONENT_WINS            = 9;

    /*
        definizioni azioni temporizzate
    */
    public static final int         TIMEDACTION_NULL            = 0;
    public static final int         TIMEDACTION_DEVICE_TURN     = 1;
    public static final int         TIMEDACTION_ENDGAME_LAYOUT  = 2;
    public static final int         TIMEDACTION_DEVICE_WINS_TOURNAMENT  = 3;
    public static final int         TIMEDACTION_BT_OPPONENT_WINS  = 4;

    /*
        TODO TOGLIERE autoplay!!!
    */
    private boolean                 autoplay        = false;
    public static final int         TIMEDACTION_AUTOPLAY_PLAYER_TURN  = 5;
    public static final int         TIMEDACTION_AUTOPLAY_RESTART_GAME  = 6;
    public int                      autoplay_total_games = 0;
    public int                      autoplay_game_errors = 0;
    private Debug                   debug;
    private boolean                 verbose_debug   = false;


    /*
        variabili di gioco
    */
    private RelativeLayout          relativeLayout;
    ArrayList<GroupClass>           groups = new ArrayList<GroupClass>();
    private PlayerClass             player;
    public  PlayerClass             device;
    private DeckClass               deck;
    private Strategy                strategy;
    private Graphics                g;
    private Context                 mContext;
    public  int                     game_status                 = GS_GAME_OVER;
    private android.os.Handler      TimedActionHandler          = new android.os.Handler();
    private int                     TimedActionId               = TIMEDACTION_NULL;
    private boolean                 endgame_result              = false;

    /*
        modalita' di gioco (giocatore imbroglione e classifica)
    */
    public  boolean                 easy_game                   = false;
    public  boolean                 rank_mode                   = false;

    /*
        audio
    */
    private SoundPool               soundPool;
    private int                     winsound;
    private int                     turnsound;
    private int                     btwinsound;
    private int                     waitturnsound;
    private int                     btstartgamesound;

    private int                     cardpicksound;
    private int                     cardselectionsound;
    private int                     message1sound;
    private int                     buttonsound;
    private int                     losematchsound;



    private boolean                 soundfxenabled = true;

    /*
        salvataggio dati
    */
    private PermanentData           permanentData;

    /*
        bluetooth
    */
    private BluetoothCom            bluetoothCom;
    private BluetoothAdapter        bluetoothAdapter;
    private boolean                 bluetooth_enabled;
    private boolean                 btactiveside;
    private String                  device_address;
    private int                     auto_reconnect = 0;

    /*
        selettore carte
    */
    private int                     card_selector = 0;
    private int                     straight_sort_mode = 0;
    private boolean                 straight_sort_alternate = false;
    private int                     values_sort_mode = 0;
    private boolean                 values_sort_alternate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // rimuove la title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // rimuove la notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // imposta il layout da utilizzare
        setContentView(R.layout.activity_game);
        // istanza activity per visibilita' negli handler
        mContext = this;

        // estrazione parametri in ingresso:
        // - modalita' di gioco normale (giocatore vs dispositivo) oppure bluetooth (giocatore vs giocatore)
        // - parte attiva (client) o passiva (server) se in modalita' bluetooth
        // - inidirizzo (MAC address) dell'altro dispositivo se in modalita' bluetooth
        Intent intent   = getIntent();
        bluetooth_enabled   = intent.getBooleanExtra( BluetoothService.INTENTEXTRA_BT_ENABLED,  false );
        btactiveside        = intent.getBooleanExtra( BluetoothService.INTENTEXTRA_BT_ACTIVE,   false );
        device_address      = intent.getStringExtra( BluetoothService.INTENTEXTRA_BT_DEVICEADDRESS );

        // se la partita deve essere giocata in modalita' bluetooth
        if( bluetooth_enabled ) {
            // e il dispositivo ha il supporto bluetooth (controllo inutile)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if( bluetoothAdapter != null ) {
                // avvio la parte di comunicazione con il servizio bluetooth
                bluetoothCom  = new BluetoothCom( bluetoothGameHandler );
            }
            // azzera il numero di riconnessioni automatiche
            auto_reconnect = 0;
        }

        // instanzia gli oggetti:
        // stato di gioco, mazzo, giocatore, avversario, gruppi di carte, ecc.
        game_status  = GS_GAME_OVER;
        strategy    = new Strategy();
        deck        = new DeckClass();
        debug       = new Debug();

        if( bluetooth_enabled ) {
            if( btactiveside ) {
                player      = new PlayerClass( PlayerClass.MASTERPLAYER_ID );
            } else {
                player      = new PlayerClass( PlayerClass.RIVALPLAYER_ID );
            }
        } else {
            player      = new PlayerClass( PlayerClass.PLAYER_ID );
        }
        device      = new PlayerClass( PlayerClass.DEVICE_ID );


        // istanza per salvataggio dati
        permanentData = new PermanentData( this );

        // modalita' di ordinamento delle scale e dei valori
        straight_sort_mode = permanentData.get_straight_sort_mode();
        values_sort_mode = permanentData.get_values_sort_mode();

        // tipo di selettore delle carte
        card_selector   = permanentData.get_card_selector();
        g           = new Graphics( this, this, card_selector, permanentData.get_background_image() );
        groups.clear();

        // in modalita' bluetooth le modalita' "giocatore imbroglione" e "classifica" sono SEMPRE disabilitate
        if( bluetooth_enabled ) {
            easy_game = false;
            rank_mode = false;
        } else {
            easy_game = permanentData.get_fraud_option();
            rank_mode = permanentData.get_rank_option();
        }

        // audio
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool   = new SoundPool( 10, AudioManager.STREAM_MUSIC, 0 );
        winsound    = soundPool.load( this, R.raw.win_match, 1 );

        turnsound           = soundPool.load( this, R.raw.turnalert, 1 );
        btwinsound          = soundPool.load( this, R.raw.btwin, 1 );
        waitturnsound       = soundPool.load( this, R.raw.waitturn, 1 );
        btstartgamesound    = soundPool.load( this, R.raw.startgamesound, 1 );
        cardpicksound       = soundPool.load( this, R.raw.card_pick, 1 );
        cardselectionsound  = soundPool.load( this, R.raw.tap, 1 );
        message1sound       = soundPool.load( this, R.raw.message1, 1 );
        buttonsound         = soundPool.load( this, R.raw.button, 1 );
        losematchsound      = soundPool.load( this, R.raw.lose_match, 1 );

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // creazione e ridimensionamento tavolo da gioco
        relativeLayout  = (RelativeLayout) findViewById( R.id.MainBackground );
        relativeLayout.post( new Runnable() {
            @Override
            public void run() {
                g.post_creation();
                // avvio nuova partita
                start_new_game();
            }
        });

    }

    @Override
    protected void onDestroy() {
        // se bluetooth attivo elimina i messaggi nella coda dell'handler
        if( bluetooth_enabled == true && bluetoothCom != null ) {
            bluetoothCom.clearHandler();
            BluetoothService.getInstance().start();
        }
        super.onDestroy();
        TimedActionHandler.removeCallbacksAndMessages( null );
    }

    /*
        visualizzazione info breve
    */
    private void show_info( String text, int duration ) {
        if( !autoplay ) {
            Toast toast = Toast.makeText( this, text, duration );
            toast.show();
        }
    }


    /*
        avvio di una nuova partita
    */
    private void start_new_game() {

        if( autoplay ) {
            autoplay_total_games += 1;
            Log.d( debug.debugTag, "Partita n. " + autoplay_total_games + " N. errori " + autoplay_game_errors );
        }

        // durante le operazioni di partenza lo stato di gioco e' GAME_OVER
        game_status  = GS_GAME_OVER;

        // toglie tutte le carte dal tavolo
        groups.clear();
        // inizializza e mescola il mazzo
        deck.reset();
        // inizializza il giocatore e distribuisce 13 carte
        player.reset( deck );
        // inizializza l'avversario  e distribuisce 13 carte
        device.reset( deck );
        // 1 carta va presa dal tallone e aggiunta al pozzo
        deck.add_cull( deck.pick_card() );

        // ristampa le carte sul tavolo
        g.draw_table( groups, true );

        // se il gioco e' in modalita' bluetooth
        if( bluetooth_enabled ) {
            // i 2 dispositivi devono sincronizzare i dati di inizio partita
            g.show_bt_wait_dialog( Graphics.BTPD_STARTGAMESYNC );
            // la parte attiva...
            if( btactiveside ) {

                // ristampa tutte le carte del giocatore
                g.draw_player_cards( player );
                // ristampa la carta del pozzo
                g.draw_last_discarded_card( deck );
                // ristampa il numero di carte in mano all'avversario
                g.draw_total_device_cards( device.cards.size(), bluetooth_enabled );

                // ..invia i dati relativi alle carte che deve avere l'avversario e alle carte sul tavolo
                game_status = GS_SEND_GAME_SYNC;
                bluetoothCom.sendStartGameMsg( device, deck );
            } else {
                // stampa la carta del pozzo con ?
                g.draw_unknown_last_discarded_card();
                // stampa tutte le carte del giocatore con il ?
                g.draw_unknown_player_cards( player );
                // ristampa il numero di carte in mano all'avversario
                g.draw_total_device_cards( device.cards.size(), bluetooth_enabled );

                // ..la parte passiva li attende
                game_status = GS_WAIT_GAME_SYNC;
            }
        } else {
            // ristampa tutte le carte del giocatore
            g.draw_player_cards( player );
            // ristampa la carta del pozzo
            g.draw_last_discarded_card( deck );
            // ristampa il numero di carte in mano all'avversario
            g.draw_total_device_cards( device.cards.size(), bluetooth_enabled );


            // stato di gioco: turno del giocatore
            game_status  = GS_PLAYER_TURN;

            if( permanentData.get_tournament_enabled() ) {
                // modalita' gara a punti
                String string = "GARA A " + permanentData.get_tournament_limit() + " PUNTI\n";
                string += String.format( "Inizio partita n. %d\n", ( permanentData.get_tournament_games() + 1 ) );
                string += String.format( "Giocatore : %d pt.\n", permanentData.get_tournament_player_score() );
                string += String.format( "Avversario: %d pt.", permanentData.get_tournament_device_score() );
                show_info( string, Toast.LENGTH_LONG );
            } else {
                // partita singola
                show_info("Inizio partita", Toast.LENGTH_SHORT);
            }
        }

        // inizio turno giocatore
        player.start_turn();


        /*
            TODO togliere autoplay!!!!
        */
        if( autoplay ) {
            TimedActionId = TIMEDACTION_AUTOPLAY_PLAYER_TURN;
            TimedActionHandler.postDelayed( TimedActionThread, 500 );
        }
    }

    /*
        selezione tallone da parte del giocatore
    */
    public void onPoolClick( View v ) {
        // se e' il turno del giocatore e il giocatore non ha ancora pescato...
        if( ( game_status == GS_PLAYER_TURN ) && ( player.has_picked == false ) ) {
            if( soundfxenabled ) {
                soundPool.play( cardpicksound, 1.0f, 1.0f, 0, 0, 1.0f );
            }
            // pesca una carta dal tallone
            player.pick_from_pool( deck );
            // ristampa le carte del giocatore
            g.draw_player_cards(player);
        }
    }

    private void checkBTCardsCount() {

        final int total_cards = 54;

        int cardcount[] = new int[total_cards];
        // azzera il totalizzatore delle carte
        for (int i = 0; i < total_cards; i++) {
            cardcount[i] = 0;
        }
        // conta le carte del mazzo
        for (int i = 0; i < deck.pool_size; i++) {
            cardcount[deck.pool_cards[i]] += 1;
        }
        // conta le carte del pozzo
        for (int i = 0; i < deck.cull_size; i++) {
            cardcount[deck.cull_cards[i]] += 1;
        }
        // conta le carte del giocatore
        for (int i = 0; i < player.cards.size(); i++) {
            cardcount[player.cards.get(i).value] += 1;
        }
        // conta le carte dell'avversario
        for (int i = 0; i < device.cards.size(); i++) {
            cardcount[device.cards.get(i).value] += 1;
        }
        // conta le carte sul tavolo
        for (int i = 0; i < groups.size(); i++) {
            for (int j = 0; j < groups.get(i).total_cards; j++) {
                cardcount[groups.get(i).cards[j]] += 1;
            }
        }
        String tmpstr = "";
        for( int i = 0; i < total_cards; i++ ) {
            tmpstr += Integer.toString( cardcount[ i ] );
            if( cardcount[ i ] > 2 ) {
                Log.d( debug.debugTag, "Errore carta " + debug.LogCardToString( i ) + " > 2 " );
            } else if( cardcount[ i ] < 2 ) {
                Log.d( debug.debugTag, "Errore carta " + debug.LogCardToString( i ) + " < 2 " );
            }
        }
        Log.d( debug.debugTag, tmpstr );
    }

    /*
		Questa funzione controlla la consistenza di tutte le carte in gioco e andrebbe
		chiamata quando il giocatore esegue lo scarto; la funzione tenta di
		ripristinare in qualche modo le carte in caso di errore aggiungendo quelle mancanti
		o togliendo quelle di troppo.

        Ritorna
        -1  tutto ok
        0   situazione corrota, impossibile ripristinare (la partita andrebbe chiusa)
        1   situazione corrota, ripristino eseguito con successo
    */
    private int cardsRecovery() {

        int         result      = -1;
        final int   total_cards = 54;
        int         wrong_cards = 0;
        int         cardcount[] = new int[ total_cards ];
        // azzera il totalizzatore delle carte
        for( int i = 0; i < total_cards; i++ ) {
            cardcount[ i ] = 0;
        }
        // conta le carte del mazzo
        for( int i = 0; i < deck.pool_size; i++ ) {
            cardcount[ deck.pool_cards[ i ] ] += 1;
        }
        // conta le carte del pozzo
        for( int i = 0; i < deck.cull_size; i++ ) {
            cardcount[ deck.cull_cards[ i ] ] += 1;
        }
        // conta le carte del giocatore
        for( int i = 0; i < player.cards.size(); i++ ) {
            cardcount[ player.cards.get( i ).value ] += 1;
        }
        // conta le carte dell'avversario
        for( int i = 0; i < device.cards.size(); i++ ) {
            cardcount[ device.cards.get( i ).value ] += 1;
        }
        // conta le carte sul tavolo
        for( int i = 0; i < groups.size(); i++ ) {
            for( int j = 0; j < groups.get( i ).total_cards; j++ ) {
                cardcount[ groups.get( i ).cards[ j ] ] += 1;
            }
        }

        // log conteggio di tutte le carte in caso di errore
/*
        String tmpstr = "";
        for( int i = 0; i < total_cards; i++ ) {
            tmpstr += Integer.toString( cardcount[ i ] );
            if( cardcount[ i ] > 2 ) {
                Log.d( debug.debugTag, "Errore carta " + debug.LogCardToString( i ) + " > 2 " );
            } else if( cardcount[ i ] < 2 ) {
                Log.d( debug.debugTag, "Errore carta " + debug.LogCardToString( i ) + " < 2 " );
            }
        }
        Log.d( debug.debugTag, tmpstr );
*/

        // azzera il numero totale di carte errate
        wrong_cards = 0;
        // controllo totale carte per ogni tipo, essendo 2 mazzi ogni carta deve comparire 2 volte
        for( int i = 0; i < total_cards; i++ ) {
            // se di una carta esiste una sola occorrenza...
            if( cardcount[ i ] < 2 ) {
                // incrementa il numero di carte corrotte
                wrong_cards += 1;

                int missing_cards = 2 - cardcount[ i ];
                int inserted_cards = 0;
                for( int k = 0; k < missing_cards; k++ ) {
                    // ...se il mazzo non e' pieno, la carta viene aggiunta al mazzo
                    if( deck.pool_size < ( DeckClass.DECK_TOTAL_SIZE - 1 ) ) {
                        // aggiunge la carta in cima al mazzo
                        deck.pool_cards[ deck.pool_size ] = i;
                        deck.pool_size += 1;
                        // numero carte inserito
                        inserted_cards += 1;
                    }
                }
                if( missing_cards == inserted_cards ) {
                    // ripristino eseguito
                    result = 1;
                } else {
                    // ripristino fallito
                    result = 0;
                    // esce dal loop di controllo di tutte le carte
                    break;
                }
            }
            // se di una carta esistono piu' di 2 occorrenze...
            if( cardcount[ i ] > 2 ) {
                // incrementa il numero di carte corrotte
                wrong_cards += 1;

                // calcola il numero di carte in eccedenza
                int exceeding_cards = cardcount[ i ] - 2;
                // come default imposto il ripristino come riuscito
                result = 1;
                do {
                    // il primo posto in cui fare operazioni non visibili sono il mazzo e il pozzo!

                    // se almeno una delle carte in eccesso si trova tra le carte del mazzo...
                    for( int k = 0; k < deck.pool_size; k++ ) {
                        if( deck.pool_cards[ k ] == i ) {
                            // ...viene rimossa dal mazzo (copia tutte le carte seguenti a partire dalla posizione k)
                            for( int j = k; j < ( deck.pool_size - 1 ); j++ ) {
                                deck.pool_cards[ j ] = deck.pool_cards[ j + 1 ];
                            }
                            deck.pool_size -= 1;
                            // aggiorna il numero di carte in eccedenza, ed esce (non piu' di una carta dal mazzo)
                            exceeding_cards -= 1;
                            break;
                        }
                    }
                    // se il numero di carte in eccedenza e' zero, esco dalla ricerca nelle carte dell'avversario
                    if( exceeding_cards == 0 ) {
                        break;
                    }
                    // ...se la carta si trova tra le carte del pozzo...
                    for( int k = 0; k < deck.cull_size; k++ ) {
                        if( deck.cull_cards[ k ] == i ) {
                            // ...viene rimossa dal pozzo (copia tutte le carte seguenti a partire dalla posizione k)
                            for( int j = k; j < ( deck.cull_size - 1 ); j++ ) {
                                deck.cull_cards[ j ] = deck.cull_cards[ j + 1 ];
                            }
                            deck.cull_size -= 1;
                            // aggiorna il numero di carte in eccedenza, ed esce (non piu' di una carta dal pozzo)
                            exceeding_cards -= 1;
                            break;
                        }
                    }
                    // se il numero di carte in eccedenza e' zero, esco dalla ricerca nelle carte dell'avversario
                    if( exceeding_cards == 0 ) {
                        break;
                    }
                    // se le carte in eccesso si trovano tra le carte dell'avversario...
                    for( int k = 0; k < device.cards.size(); k++ ) {
                        if( device.cards.get( k ).value == i ) {
                            // ...viene rimossa
                            device.cards.remove( k );
                            // aggiorna il numero di carte in eccedenza
                            exceeding_cards -= 1;
                            // 1 sola carta puo' essere rimossa altrimenti serve l'iteratore
                            break;
                        }
                    }
                    // se il numero di carte in eccedenza e' zero, esco dalla ricerca nelle carte dell'avversario
                    if( exceeding_cards == 0 ) {
                        break;
                    }
                    // se le carte in eccesso si trovano tra le carte del giocatore...
                    for( int k = 0; k < player.cards.size(); k++ ) {
                        if( player.cards.get( k ).value == i ) {
                            // ...viene rimossa
                            player.cards.remove( k );
                            // aggiorna il numero di carte in eccedenza
                            exceeding_cards -= 1;
                            // 1 sola carta puo' essere rimossa altrimenti serve l'iteratore
                            break;
                        }
                    }
                    // se il numero di carte in eccedenza e' zero, esco dalla ricerca nelle carte dell'avversario
                    if( exceeding_cards == 0 ) {
                        break;
                    }

                    // se arriva qui significa che non e' stato possibile un ripristino
                    result = 0;

                } while( false );
                // se non e' stato possibile ripristinare una situazione esco dal loop di ricerca
                if( result == 0 ) {
                    break;
                }
            }
        }
        // questa funzione serve a ripristinare situazione di piccolissime corruzioni, se ci sono
        // piu' di 5 carte corrotte la situazione e' grave!
        if( wrong_cards > 5 ) {
            result = 0;
        }
        // ritorna esito: -1 tutto ok, 1 ripristino eseguito, 0 ripristino fallito
        return result;
    }

    /*
        selezione pozzo da parte del giocatore
    */
    public void onCullClick( View v ) {
        // se e' il turno del giocatore
        if( game_status == GS_PLAYER_TURN ) {
            // il giocatore ha scelto il pozzo
            CullActionResult result = player.cull_action( deck, groups );

            // se il giocatore ha scartato controllo immediatamente la consistenza delle carte
            if( result.discarded_card && bluetooth_enabled == false ) {
                switch( cardsRecovery() ) {
                    // tutto OK!
                    case -1:
                        break;
                    // situazione ripristinato, non visualizza nulla al giocatore
                    case 1:
                    break;
                    // errore grave non ripristinabile,
                    case 0:
                        // TODO annullamento partita?
                    break;
                }
            }

            // segnalazioni
            if( result.update_screen ) {
                g.draw_player_cards( player );
                g.draw_last_discarded_card( deck );
                if( soundfxenabled ) {
                    soundPool.play( cardpicksound, 1.0f, 1.0f, 0, 0, 1.0f );
                }
            }
            if( result.open_failure ) {
                show_info( "Almeno 40 punti per aprire", Toast.LENGTH_SHORT );
                g.draw_table( groups, true );
            } if( result.cull_pick_failure ) {
                show_info( "Obbligatorio aprire con la carta pescata dal pozzo", Toast.LENGTH_LONG );
                g.draw_table( groups, true );
            } else if( result.discarded_card ) {
                // se in modalita' bluetooth..
                if( bluetooth_enabled ) {
                    // devo inviare SEMPRE i dati di fine turno
                    if( player.cards.size() == 0 ) {

                        // se pero' ho vinto visualizzo la dialog finale con la win celebration
                        game_status = GS_PLAYER_WINS;

                        if( btactiveside ) {
                            g.show_bt_final_dialog( Graphics.BTFD_WIN_ACTIVE );
                        } else {
                            g.show_bt_final_dialog( Graphics.BTFD_WIN_PASSIVE );
                        }
                        soundPool.play( btwinsound, 1.0f, 1.0f, 0, 0, 1.0f );

                    } else {
                        game_status = GS_SEND_END_TURN;

                        soundPool.play( waitturnsound, 1.0f, 1.0f, 0, 0, 1.0f );
                        // NOTE la visualizzazione di questa dialog e' fastidiosa!
                        //g.show_bt_wait_dialog( Graphics.BTPD_ENDOFTURNSYNC );
                    }
                    // invio dati relativi al numero di carte del giocatore e alle carte sul tavolo
                    bluetoothCom.sendEndTurnMsg( player, deck, groups );

                } else {
                    // se il giocatore ha eliminato tutte le carte..
                    if( player.cards.size() == 0 ) {
                        // ..ha vinto!
                        game_status = GS_PLAYER_WINS;
                        int losing_score = device.calculate_losing_score();

                        // aggiorna le statistiche: n. partite, n. partite vinte, punti, ecc.
                        permanentData.endofgame_stats( true, 0, losing_score );
                        // aggiorna le statistiche per la classifica
                        if( rank_mode ) {
                            permanentData.endofgame_rankstats(true, 0, losing_score);
                            // connessione al server per aggiornare i dati del giocatore in modalita'  silenziosa ovvero
                            // senza visualizzazione operazione in corso ed esito aggiornamento
                            new Uploader( mContext, false ).execute( permanentData );
                        }

                        if( permanentData.get_tournament_enabled() ) {
                            // modalita' gara a punti

                            // aggiornare il punteggio dell'avversario
                            int trnm_device_score = permanentData.get_tournament_device_score();
                            trnm_device_score += losing_score;
                            permanentData.set_tournament_device_score( trnm_device_score );
                            int tournament_limit = permanentData.get_tournament_limit();
                            // - se il punteggio dell'avversario oltrepassa il limite il giocatore vince la gara
                            if( trnm_device_score >= tournament_limit ) {

                                // aggiorna il numero totale di gare vinte dal giocatore
                                if( tournament_limit == 101 ) {
                                    permanentData.inc_won_101_tournaments();
                                } else {
                                    permanentData.inc_won_201_tournaments();
                                }
                                // aggiorna il numero totale di gare effettuate
                                permanentData.inc_total_tournaments();

                                // IL GIOCATORE VINCE LA GARA

                                g.show_end_of_turnament_dialog( true, permanentData.get_tournament_player_score(), permanentData.get_tournament_device_score() );
                                soundPool.play( btwinsound, 1.0f, 1.0f, 0, 0, 1.0f );

                                // reset variabili di gara
                                permanentData.reset_tournament_data();

                            } else {

                                // IL GIOCATORE VINCE LA PARTITA DELLA GARA

                                // aggiornamento numero di partite della gara
                                int trnm_games = permanentData.get_tournament_games();
                                trnm_games += 1;
                                permanentData.set_tournament_games( trnm_games );

                                // modalita' gara a punti
                                String string = "GARA A " + permanentData.get_tournament_limit() + " PUNTI\n";
                                string += "HAI VINTO LA PARTITA!\n";
                                string += String.format( "Giocatore : %d pt.\n", permanentData.get_tournament_player_score() );
                                string += String.format( "Avversario: %d pt.", permanentData.get_tournament_device_score() );
                                show_info( string, Toast.LENGTH_LONG );
                                soundPool.play( winsound, 1.0f, 1.0f, 0, 0, 1.0f );

                                // dopo 5 secondi visualizza il layout di fine partita
                                endgame_result = true;  // ha vinto il giocatore
                                TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                                TimedActionHandler.postDelayed( TimedActionThread, 5000 );
                            }
                        } else {
                            // partita singola
                            show_info( "Hai vinto.\nL'avversario perde con " + String.format( "%d punti in mano", losing_score ), Toast.LENGTH_LONG );
                            soundPool.play(winsound, 1.0f, 1.0f, 0, 0, 1.0f);

                            // dopo 5 secondi visualizza il layout di fine partita
                            endgame_result = true;  // ha vinto il giocatore
                            TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                            TimedActionHandler.postDelayed( TimedActionThread, 5000 );
                        }
/*
                        // dopo 5 secondi visualizza il layout di fine partita
                        endgame_result = true;  // ha vinto il giocatore
                        TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                        TimedActionHandler.postDelayed( TimedActionThread, 5000 );
*/
                    } else {
                        // tocca all'avversario
                        game_status = GS_DEVICE_TURN;
                        g.start_gears_animation();

                        TimedActionId = TIMEDACTION_DEVICE_TURN;
                        TimedActionHandler.postDelayed( TimedActionThread, 1000 );
                    }

                }

            }
        }
    }

    /*
        selezione una carta del giocatore
    */
    public void onPlayerCardClick( View v ) {
        int position = 0;
        // se e' il turno del giocatore e il giocatore ha gia' pescato
        if( ( game_status == GS_PLAYER_TURN ) && ( player.has_picked ) ) {
            if( soundfxenabled ) {
                soundPool.play( cardselectionsound, 1.0f, 1.0f, 0, 0, 1.0f );
            }
            // recupero l'id (0-13) della carta selezionata contenuto nel Tag
            position = Integer.parseInt( v.getTag().toString() );
            // seleziona la carta del giocatore e in base al nuovo sata aggiorna la grafica
            if( player.select_card( position ) ) {
                g.draw_selected_card( v, position, player.cards.get( position ).value, true );
            } else {
                g.draw_selected_card( v, position, player.cards.get( position ).value, false );
            }
        }
    }

    /*
        spostamento di una carta tramite drag and drop
    */
    public void onDropCard( View start, View end ) {
        int     moving_card = 0;
        int     start_pos   = 0;
        int     end_pos     = 0;

        // controllo dati in ingresso
        if( ( start == null ) || ( end == null ) ) {
            return;
        }
        // recupero id carta da spostare
        if( start.getTag() != null ) {
            start_pos = Integer.parseInt( start.getTag().toString() );
        }
        // recupero id posizione finale
        if( end.getTag() != null ) {
            end_pos = Integer.parseInt( end.getTag().toString() );
        }
        // controllo che la posizione di partenza sia valida
        if( ( start_pos < 0 ) || ( start_pos >= player.cards.size() ) ) {
            return;
        }
        // controllo che la destinazione sia valida
        if( ( end_pos < 0 ) || ( end_pos >= player.cards.size() ) ) {
            return;
        }
        // se partenza e destinazione coincidono esco
        if( start_pos == end_pos ) {
            return;
        }
        // spostamento verso destra
        if( start_pos < end_pos ) {
            moving_card = player.cards.get( start_pos ).value;
            for( int i = start_pos; i < end_pos; i++ ) {
                player.cards.get( i ).value = player.cards.get( i + 1 ).value;
            }
            player.cards.get( end_pos ).value = moving_card;
            player.reset_cards_selection();
            player.selectedCards.clear();
            g.draw_player_cards( player );
        }
        // spostamento verso sinistra
        if( start_pos > end_pos ) {
            moving_card = player.cards.get( start_pos ).value;
            for( int i = ( start_pos - 1 ); i >= end_pos; i-- ) {
                player.cards.get( i + 1 ).value = player.cards.get( i ).value;
            }
            player.cards.get( end_pos ).value = moving_card;
            player.reset_cards_selection();
            player.selectedCards.clear();
            g.draw_player_cards( player );
        }
    }

    /*
        selezione una carta sul tavolo
    */
    public void onTableClick( View v ) {
        int card_tag = 0;
        if( game_status == GS_PLAYER_TURN ) {
            // recupero l'indice della carta o del gruppo di carte selezionato
            card_tag = Integer.parseInt( v.getTag().toString() );
            // azione del giocatore sul tavolo
            TableActionResult result = player.table_action( card_tag, groups );
            // aggiornamento a video carte giocatore e carte sul tavolo
            if( result.update_screen ) {
                g.draw_table( groups, false );
                g.draw_player_cards( player );
            }
            // segnalazioni
            if( result.opening ) {
                show_info(String.format("Apertura con %d punti", player.turn_score), Toast.LENGTH_SHORT);
                if( soundfxenabled ) {
                    soundPool.play( message1sound, 1.0f, 1.0f, 0, 0, 1.0f );
                }
            } else if ( result.score_update ) {
                show_info(String.format("%d Punti", player.turn_score), Toast.LENGTH_SHORT );
            } else if ( result.cannot_discard ) {
                show_info( "Scarto non possibile!", Toast.LENGTH_SHORT );
            }
        }
    }

    /*
        ordinamento carte giocatore in ordine crescente divise nelle seguenti sezioni
        [cuori][quadri][picche][fiori][jokers]
    */
    public void onSortStraightsButtonClick( View v ) {
        if( game_status == GS_PLAYER_TURN ) {
            if( soundfxenabled ) {
                soundPool.play( buttonsound, 1.0f, 1.0f, 0, 0, 1.0f );
            }
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.buttonclick));
            player.reset_cards_selection();
            player.selectedCards.clear();
            switch( straight_sort_mode ) {
                case 0:
                    player.sort_cards_straights();
                    break;
                case 1:
                    player.sort_cards_straights_reverse();
                    break;
                default:
                    straight_sort_alternate = !straight_sort_alternate;
                    if( straight_sort_alternate ) {
                        player.sort_cards_straights();
                    } else {
                        player.sort_cards_straights_reverse();
                    }
                    break;
            }
            g.draw_player_cards(player);
        }
    }

    /*
        ordinamento carte giocatore in ordine crescente divise per valori
        [Assi][2][3][4]...[K][jokers]
    */
    public void onSortValuesButtonClick( View v ) {
        if( game_status == GS_PLAYER_TURN ) {
            if( soundfxenabled ) {
                soundPool.play( buttonsound, 1.0f, 1.0f, 0, 0, 1.0f );
            }
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.buttonclick));
            player.reset_cards_selection();
            player.selectedCards.clear();
            switch ( values_sort_mode ) {
                case 0:
                    player.sort_cards_values();
                    break;
                case 1:
                    player.sort_cards_values_reverse();
                    break;
                default:
                    values_sort_alternate = !values_sort_alternate;
                    if( values_sort_alternate ) {
                        player.sort_cards_values();
                    } else {
                        player.sort_cards_values_reverse();
                    }
                break;
            }
            g.draw_player_cards(player);
        }
    }

    /*
        pressione del bottone gioca ancora
    */
    public void onPlayAgainClick( View v ) {
        // reinizializza le variabili di gioco
        start_new_game();
        // nasconde il layout di fine partita
        if( g.is_endgame_layout_visible() ) {
            g.hide_endgame_layout();
        }
    }

    /*
        pressione del bottone "menu" nel layout di fine partita
    */
    public void onBackToMenuClick( View v ) {
        this.finish();
    }



    /*
        azioni temporizzate
    */
    private Runnable TimedActionThread = new Runnable() {
        public void run() {

            if( TimedActionId == TIMEDACTION_DEVICE_TURN ) {

                // azione giocatore
                device.start_turn();
                StrategyResult result = strategy.play( device, deck, groups );

                g.stop_gears_animation();
                g.draw_total_device_cards( device.cards.size(), bluetooth_enabled );
                g.draw_last_discarded_card( deck );

                // info operazioni del device
                if( result.str_device_opened ) {
                    g.draw_table( groups, false );
                    show_info( String.format( "Apertura con %d punti", result.str_opening_score ), Toast.LENGTH_SHORT );
                    if( soundfxenabled ) {
                        soundPool.play( message1sound, 1.0f, 1.0f, 0, 0, 1.0f );
                    }
                }
                if( result.str_device_attach ) {
                    g.draw_table( groups, false );
                    show_info( "Attacco", Toast.LENGTH_SHORT );
                }
                if( result.str_device_newgroup ) {
                    g.draw_table( groups, false );
                    show_info( "Nuovo gruppo", Toast.LENGTH_SHORT );
                }
                // TODO togliere ?
                if( result.error > 0 ) {
                    show_info( String.format( "Errore %d", result.error ), Toast.LENGTH_SHORT );
                }


                if( device.cards.size() == 0 ) {

                    // vince l'avversario
                    game_status = GS_DEVICE_WINS;

                    int losing_score = player.calculate_losing_score();



                    // aggiorna le statistiche: n. partite, n. partite vinte, punti, ecc.
                    permanentData.endofgame_stats( false, losing_score, 0 );
                    // aggiorna le statistiche per la classifica
                    if( rank_mode ) {
                        permanentData.endofgame_rankstats( false, losing_score, 0 );
                        // connessione al server per aggiornare i dati del giocatore in modalita'  silenziosa ovvero
                        // senza visualizzazione operazione in corso ed esito aggiornamento
                        new Uploader( mContext, false ).execute( permanentData );
                    }

                    if( permanentData.get_tournament_enabled() ) {
                        // modalita' gara a punti

                        // aggiornare il punteggio dell'avversario
                        int trnm_player_score = permanentData.get_tournament_player_score();
                        trnm_player_score += losing_score;
                        permanentData.set_tournament_player_score( trnm_player_score );
                        // - se il punteggio dell'avversario oltrepassa il limite il giocatore vince la gara
                        if( trnm_player_score >= permanentData.get_tournament_limit() ) {

                            // aggiorna il numero totale di gare effettuate
                            permanentData.inc_total_tournaments();

                            // L'AVVERSARIO VINCE LA GARA

                            TimedActionId = TIMEDACTION_DEVICE_WINS_TOURNAMENT;
                            TimedActionHandler.postDelayed( this, 2000 );

                        } else {
                            // L'AVVERSARIO VINCE LA PARTITA DELLA GARA

                            // aggiornamento numero di partite della gara
                            int trnm_games = permanentData.get_tournament_games();
                            trnm_games += 1;
                            permanentData.set_tournament_games( trnm_games );

                            // modalita' gara a punti
                            String string = "GARA A " + permanentData.get_tournament_limit() + " PUNTI\n";
                            string += "L'AVVERSARIO VINCE LA PARTITA\n";
                            string += String.format( "Giocatore : %d pt.\n", permanentData.get_tournament_player_score() );
                            string += String.format( "Avversario: %d pt.", permanentData.get_tournament_device_score() );
                            show_info( string, Toast.LENGTH_LONG );
                            soundPool.play( winsound, 1.0f, 1.0f, 0, 0, 1.0f );

                            // dopo 5 secondi visualizza il layout di fine partita
                            endgame_result = false;  // il giocatore ha perso
                            TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                            TimedActionHandler.postDelayed( this, 5000 );
                        }
                    } else {
                        // partita singola
                        show_info("Hai perso con " + String.format("%d punti in mano", losing_score), Toast.LENGTH_LONG);
                        if( soundfxenabled ) {
                            soundPool.play( losematchsound, 1.0f, 1.0f, 0, 0, 1.0f );
                        }

                        // dopo 5 secondi visualizza il layout di fine partita
                        endgame_result = false;  // il giocatore ha perso
                        TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                        TimedActionHandler.postDelayed( this, 5000 );
                    }

/*
                    // dopo 5 secondi visualizza il layout di fine partita
                    endgame_result = false;  // il giocatore ha perso
                    TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                    TimedActionHandler.postDelayed( this, 5000 );
*/
                } else {
                    // e' il turno del giocatore
                    player.start_turn();
                    game_status = GS_PLAYER_TURN;

                    /*
                        TODO togliere autoplay!!!
                    */
                    if( autoplay ) {
                        TimedActionId = TIMEDACTION_AUTOPLAY_PLAYER_TURN;
                        TimedActionHandler.postDelayed( TimedActionThread, 500 );
                    } else {
                        TimedActionId = TIMEDACTION_NULL;
                    }
                }
            } else if( TimedActionId == TIMEDACTION_ENDGAME_LAYOUT ) {

                // visualizza il layout di fine partita
                g.show_endgame_layout( endgame_result );

                if( autoplay ) {
                    TimedActionId = TIMEDACTION_AUTOPLAY_RESTART_GAME;
                    TimedActionHandler.postDelayed( TimedActionThread, 500 );
                } else {
                    TimedActionId = TIMEDACTION_NULL;
                }

            } else if( TimedActionId == TIMEDACTION_AUTOPLAY_RESTART_GAME ) {
                TimedActionId = TIMEDACTION_NULL;
                onPlayAgainClick(null);
            } else if( TimedActionId == TIMEDACTION_DEVICE_WINS_TOURNAMENT ) {

                // l'avversario vince la gara
                g.show_end_of_turnament_dialog( false, permanentData.get_tournament_player_score(), permanentData.get_tournament_device_score() );
                soundPool.play( losematchsound, 1.0f, 1.0f, 0, 0, 1.0f );

                // reset variabili di gara
                permanentData.reset_tournament_data();

                TimedActionId = TIMEDACTION_NULL;

            } else if( TimedActionId == TIMEDACTION_BT_OPPONENT_WINS ) {
                // visualizza "HAI PERSO" in gara via bluetooth
                TimedActionId = TIMEDACTION_NULL;
                if( btactiveside ) {
                    g.show_bt_final_dialog( Graphics.BTFD_LOSE_ACTIVE );
                } else {
                    g.show_bt_final_dialog( Graphics.BTFD_LOSE_PASSIVE );
                }
            } else if( TimedActionId == TIMEDACTION_AUTOPLAY_PLAYER_TURN ) {

                StrategyResult result = strategy.play( player, deck, groups );


                g.draw_player_cards( player );
                g.draw_last_discarded_card( deck );

                    // info operazioni del device
                    if( result.str_device_opened ) {
                        g.draw_table( groups, false );
                        show_info( String.format( "Apertura con %d punti", result.str_opening_score ), Toast.LENGTH_SHORT );
                    }
                    if( result.str_device_attach ) {
                        g.draw_table( groups, false );
                        show_info( "Attacco", Toast.LENGTH_SHORT );
                    }
                    if( result.str_device_newgroup ) {
                        g.draw_table( groups, false );
                        show_info( "Nuovo gruppo", Toast.LENGTH_SHORT );
                    }
                    // TODO togliere ?
                    if( result.error > 0 ) {
                        show_info( String.format( "Errore %d", result.error ), Toast.LENGTH_SHORT );
                    }

                    if( result.str_discard_failed ) {
                        show_info( String.format( "ERRORE!!! SCARTO NON ESEGUITO" ), Toast.LENGTH_LONG );
                    }

                // se il giocatore ha eliminato tutte le carte..
                if( player.cards.size() == 0 ) {
                    // ..ha vinto!
                    game_status = GS_PLAYER_WINS;
                    int losing_score = device.calculate_losing_score();

                    // aggiorna le statistiche: n. partite, n. partite vinte, punti, ecc.
                    permanentData.endofgame_stats( true, 0, losing_score );
                    // aggiorna le statistiche per la classifica
                    if( rank_mode ) {
                        permanentData.endofgame_rankstats(true, 0, losing_score);
                        // connessione al server per aggiornare i dati del giocatore in modalita'  silenziosa ovvero
                        // senza visualizzazione operazione in corso ed esito aggiornamento
                        new Uploader( mContext, false ).execute( permanentData );
                    }


                    // partita singola
                    show_info( "Hai vinto.\nL'avversario perde con " + String.format( "%d punti in mano", losing_score ), Toast.LENGTH_LONG );
                    soundPool.play(winsound, 1.0f, 1.0f, 0, 0, 1.0f);

                    // dopo 5 secondi visualizza il layout di fine partita
                    endgame_result = true;  // ha vinto il giocatore
                    TimedActionId = TIMEDACTION_ENDGAME_LAYOUT;
                    TimedActionHandler.postDelayed( TimedActionThread, 5000 );

                } else {
                    // tocca all'avversario
                    game_status = GS_DEVICE_TURN;

                    TimedActionId = TIMEDACTION_DEVICE_TURN;
                    TimedActionHandler.postDelayed( TimedActionThread, 1000 );
                }


            }
        }
    };

    /*
        riconnessione al dispositivo
    */
    public void bluetooth_reconnect() {
        // riavvio del servizio bluetooth (si mette in ascolto)
        BluetoothService.getInstance().start();
        // connessione al (MAC address del) dispositivo selezionato
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice( device_address );
        BluetoothService.getInstance().connect( device );
        // visualizza l'intederminate progress dialog
        g.show_bt_wait_dialog( Graphics.BTPD_RECONNECTING );
    }

    /*
        nuova partita
    */
    public void bluetooth_start_new_game() {
        // nasconde la dialog di fine partita
        g.hide_bt_final_dialog();
        // essendo la parte attiva invia il messaggio di sync inizio partita
        start_new_game();
    }

    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

    /*
        l'handler di gioco che riceve informazioni dal servizio bluetooth
    */
    private final Handler bluetoothGameHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                // ricezione dati di sync inizio partita
                case BluetoothCom.MSG_BTCOM_RECEIVED_STARTDATA:

                    // ricezione dei dati di inizio partita come richiesta nuova partita da parte del client
                    if( ( game_status == GS_PLAYER_WINS ) || ( game_status == GS_OPPONENT_WINS ) ) {
                        g.hide_bt_final_dialog();
                        // inizia una nuova partita come all'avvio dell'activity
                        start_new_game();
                        // e entra nel blocco successivo i dati come se fosse in attesa
                        game_status = GS_WAIT_GAME_SYNC;
                    }

                    // ricezione dati di inizio partita in attesa dati di inizio partita
                    if( game_status == GS_WAIT_GAME_SYNC ) {

                        // la parte passiva deve ricevere tutte le informazioni necessarie
                        // affinche' la partita cominci senza disallineamenti. I dati ricevuti sono:
                        // - le carte con cui il giocatore (parte passiva) deve cominciare (13 bytes)
                        // - la carta (e anche l'unica) del pozzo (1 byte)
                        // - tutte le carte del tallone (81 bytes)
                        // il pacchetto e' complessivamente formato da 13 + 1 + 81 = 95 bytes

                        // solo la parte passiva riceve i dati di inizo partita, lo step successivo e' l'attesa turno
                        game_status = GS_WAIT_TURN;

                        //Debug.LogBytesArray( "MSG_BTCOM_RECEIVED_STARTDATA ", (( byte[] ) msg.obj) );

                        for( int i = 0; i < 13; i++ ) {
                            player.cards.get( i ).value = (( byte[] ) msg.obj)[ i + 1 ];
                        }
                        deck.cull_cards[ deck.cull_size - 1 ] = (( byte[] ) msg.obj)[ 14 ];

                        // !!!!ATTENZIONE!!!!!
                        // Fino alla versione 2.1 inclusa questo pacchetto e' di 19 bytes
                        // 1 (comando)
                        // 17 18 18 19 20 21 28 31 34 43 44 49 50 (carte)
                        // 9 (pozzo)
                        // -109  1 48 10 (adler32)
                        // Dalla versione 2.2 questo pacchetto e' di 95 bytes
                        // La mancata comunicazione alla parte passiva di tutte le carte del tallone causa
                        // il disallineamento del numero di carte e quindi la presenza di carte multiple (>2)

                        // Questo controllo serve a mantenere la retrocompatibilita' tra versioni non aggiornate
                        if( (( byte[] ) msg.obj).length > 19 ) {
                            for( int i = 0; i < 81; i++ ) {
                                deck.pool_cards[ i ] = (( byte[] ) msg.obj)[ 14 + i ];
                            }
                        }

                        // conteggio carte
                        //checkBTCardsCount();

                        g.draw_player_cards( player );
                        g.draw_last_discarded_card( deck );

                        g.hide_bt_wait_dialog();
                        g.show_bt_wait_dialog( Graphics.BTPD_WAITTURN );
                    }

                    break;

                // ricezione dati di fine turno
                case BluetoothCom.MSG_BTCOM_RECEIVED_ENDTURNDATA:

                    if( game_status == GS_WAIT_TURN ) {
                        // n. di carte dell'avversario
                        int pos = 0;
                        int total_rival_cards   = (( byte[] ) msg.obj)[ pos ];
                        pos += 1;
                        device.cards.clear();
                        for( int i = 0; i < total_rival_cards; i++ ) {
                            device.cards.add( new PlayerClass.PlayerCard( (( byte[] ) msg.obj)[ pos ], false ) );
                            pos += 1;
                        }

                        // n. di carte del tallone
                        int total_pool_cards    = (( byte[] ) msg.obj)[ pos ];
                        pos += 1;
                        // carte del tallone
                        deck.pool_size = total_pool_cards;
                        for( int i = 0; i < total_pool_cards; i++ ) {
                            deck.pool_cards[ i ] = (( byte[] ) msg.obj)[ pos ];
                            pos += 1;
                        }
                        // n. di carte del pozzo
                        int total_cull_cards    = (( byte[] ) msg.obj)[ pos ];
                        pos += 1;
                        deck.cull_size = total_cull_cards;
                        // carte del pozzo
                        for( int i = 0; i < total_cull_cards; i++ ) {
                            deck.cull_cards[ i ] = (( byte[] ) msg.obj)[ pos ];
                            pos += 1;
                        }

                        // subarray contiene i dati serializzati dell'array di oggetti di tipo GroupClass
                        byte[] subarray = copyOfRange( (( byte[] ) msg.obj), pos, (( byte[] ) msg.obj).length );
                        try {
                            // riconversione dei dati serializzati in oggetto di tipo array di oggetti GroupClass
                            ArrayList<GroupClass> newgroups = (ArrayList<GroupClass>)bluetoothCom.toObject( subarray );
                            // se non ci sono exception ricrea l'array dei gruppi sul tavolo
                            groups.clear();
                            for( int i = 0; i < newgroups.size(); i++ ) {
                                GroupClass newgroup = new GroupClass(
                                        newgroups.get( i ).owner,
                                        newgroups.get( i ).total_cards,
                                        newgroups.get( i ).cards,
                                        newgroups.get( i ).type,
                                        newgroups.get( i ).joker_value,
                                        newgroups.get( i ).score );
                                groups.add( newgroup );
                            }
                            // aggiorna a video il tavolo
                            g.draw_table( groups, false );

                        } catch ( ClassNotFoundException e ) {
                            show_info( e.getMessage(), Toast.LENGTH_SHORT );
                        } catch ( IOException e ) {
                            show_info( e.getMessage(), Toast.LENGTH_SHORT );
                        }

                        // conteggio carte
                        //checkBTCardsCount();

                        // aggiorna il video
                        g.draw_total_device_cards( total_rival_cards, bluetooth_enabled );
                        g.draw_last_discarded_card( deck );

                        // verifica se l'avversario ha vinto (n.carte in mano = 0)
                        if( total_rival_cards == 0 ) {

                            // aggiorna SOLO le statistiche personali: n. partite, n. partite vinte, punti, ecc.
                            int losing_score = player.calculate_losing_score();
                            permanentData.endofgame_stats( false, losing_score, 0 );

                            // invia il punteggio delle carte in mano al giocatore per l'aggiornamento delle statistiche
                            bluetoothCom.sendScoreValue( losing_score );

                            game_status = GS_OPPONENT_WINS;
                            g.hide_bt_wait_dialog();

                            TimedActionId = TIMEDACTION_BT_OPPONENT_WINS;
                            TimedActionHandler.postDelayed( TimedActionThread, 3500 );
                            soundPool.play( losematchsound, 1.0f, 1.0f, 0, 0, 1.0f );

                            // la finestra di notifica della perdita viene visualizzata dal TimedActionThread

                        } else {
                            // se l'avversario non ha vinto attende il turno
                            g.hide_bt_wait_dialog();
                            player.start_turn();
                            game_status = GS_PLAYER_TURN;
                            soundPool.play( turnsound, 1.0f, 1.0f, 0, 0, 1.0f );
                        }
                    }

                    break;

                // nuovo tentativo di invio messaggio
                case BluetoothCom.MSG_BTCOM_SEND_ATTEMPT:
                    // Toast.makeText( mContext, "Tentativo di invio n. " + Integer.toString( msg.arg1 ), Toast.LENGTH_SHORT ).show();
                    break;

                // messaggio ricevuto correttamente dall'altro dispositivo
                case BluetoothCom.MSG_BTCOM_DELIVERY_SUCCESSFULL:

                    // la notifica di questo evento avviene alla ricezione di un ACK ovvero quando l'altro dispositivo
                    // ha ricevuto correttamente il messaggio inviato da questo dispositivo

                    // un ACK ricevuto durante l'invio dei dati di sync inizio partita porta questo lato del gioco
                    // al vero e proprio turno di gioco (inizia sempre la parte attiva ovvero il client)
                    if( game_status == GS_SEND_GAME_SYNC ) {
                        player.start_turn();
                        game_status = GS_PLAYER_TURN;
                        g.hide_bt_wait_dialog();

                        soundPool.play( btstartgamesound, 1.0f, 1.0f, 0, 0, 1.0f );
                        show_info( "Inizio partita", Toast.LENGTH_SHORT );
                    }
                    // un ACK ricevuto durante l'invio dei dati di fine turno porta questo lato del gioco all'attesa
                    // del proprio turno di gioco
                    if( game_status == GS_SEND_END_TURN ) {
                        game_status = GS_WAIT_TURN;
                        g.hide_bt_wait_dialog();
                        g.show_bt_wait_dialog( Graphics.BTPD_WAITTURN );
                    }

                    break;

                case BluetoothCom.MSG_BTCOM_RECEIVED_SCOREVALUE:

                    // recupero punti in mano all'avversario
                    int losing_score = msg.arg1;
                    // aggiorna SOLO le statistiche personali: n. partite, n. partite vinte, punti, ecc.
                    permanentData.endofgame_stats( true, 0, losing_score );

                    break;

                // ricezione comando non valido
                case BluetoothCom.MSG_BTCOM_INVALID_MESSAGE:
                    // e' stato ricevuto un comando non corretto, l'errore puo' essere ignorato poiche' essendoci
                    // i tentativi di invio la cosa puo' risolversi da sola
                    break;

                // l'altro dispositivo non risponde
                case BluetoothCom.MSG_BTCOM_NO_RESPONSE_FROM_DEVICE:

                    // segnalazione tramite dialog
                    g.hide_bt_wait_dialog();
                    if( btactiveside ) {
                        g.show_bt_error_dialog( Graphics.BTED_ACTIVESIDE_COMMUNICATION_ERROR );
                    } else {
                        g.show_bt_error_dialog( Graphics.BTED_PASSIVESIDE_CONNECTION_ERROR );
                    }
                    break;

                // connessione o riconnessione al dispositivo avvenuta correttamente
                case BluetoothService.MSG_BT_CONNECTED:

                    // elimina la dialog "connessione in corso..."
                    g.hide_bt_wait_dialog();
                    g.hide_bt_error_dialog();

                    //Toast.makeText( mContext, "Connesso", Toast.LENGTH_SHORT ).show();

                    // in caso di riconnessione reintraprendo le azione legate allo stato di gioco:
                    if( game_status == GS_SEND_GAME_SYNC ) {
                        // solo la parte attiva puo' andare in GS_SEND_GAME_SYNC quindi reinvia i dati
                        bluetoothCom.sendStartGameMsg( device, deck );
                        g.show_bt_wait_dialog( Graphics.BTPD_STARTGAMESYNC );
                    } else if( game_status == GS_SEND_END_TURN ) {
                        // solo la parte attiva puo' andare in GS_SEND_END_TURN quindi reinvia i dati
                        bluetoothCom.sendEndTurnMsg( player, deck, groups );
                        g.show_bt_wait_dialog( Graphics.BTPD_ENDOFTURNSYNC );
                    } else if ( game_status == GS_WAIT_TURN ) {
                        // attesa del proprio turno
                        g.show_bt_wait_dialog( Graphics.BTPD_WAITTURN );
                    }

                    break;

                // connessione al dispositivo in corso
                case BluetoothService.MSG_BT_CONNECTING:
                    break;

                // tentativo di connessione fallita
                case BluetoothService.MSG_BT_CONNECTION_FAILED:

                    // solo il dispositivo parte attiva puo' tentare di ristabilire la connessione con l'altro disposibito
                    // in caso di fallimento rivisualizza la possibilita' di riconnettersi o di annullare la partita
                    g.hide_bt_wait_dialog();
                    g.show_bt_error_dialog( Graphics.BTED_ACTIVESIDE_CONNECTION_FAILED );
                    break;

                // connessione al dispositivo persa
                case BluetoothService.MSG_BT_CONNECTION_LOST:

                    /*
                        in caso di perdita di connessione la parte attiva (conoscendo l'indirizzo della parte
                         server) tenta automaticamente la riconnessione; questo workaround e' dovuto al fatto
                         che al cambio di activity e quindi all'ingresso in gioco viene perso il socket
                         precedentemente aperto nel menu'!!!!
                    */
                    if( btactiveside ) {
                        if( auto_reconnect < 1 ) {
                            bluetooth_reconnect();
                            auto_reconnect += 1;
                        } else {
                            g.show_bt_error_dialog( Graphics.BTED_ACTIVESIDE_CONNECTION_LOST );
                        }
                    } else {
                        if( auto_reconnect < 1 ) {
                            auto_reconnect += 1;
                        } else {
                            g.show_bt_error_dialog( Graphics.BTED_PASSIVESIDE_CONNECTION_ERROR );
                        }
                    }

                    // se il dispositivo e' parte attiva visualizza la possibilita' di annullare o di riconnettersi
                    // se il dispositivo e' parte passiva visualizza la possibilita' attendere o di annullare
/*
                    if( btactiveside ) {
                        g.show_bt_error_dialog( Graphics.BTED_ACTIVESIDE_CONNECTION_LOST );
                    } else {
                        g.show_bt_error_dialog( Graphics.BTED_PASSIVESIDE_CONNECTION_ERROR );
                    }
*/
                    break;

                // servizio bluetooth in ascolto (???) -> Errore!!!
                case BluetoothService.MSG_BT_LISTENING:
                    // se la connessione cade la parte attiva tenta automaticamente la riconnessione
                    break;

                // servizio bluetooth fermo (???) -> Errore!!!
                case BluetoothService.MSG_BT_STOPPED:
                    // se la connessione cade la parte attiva tenta automaticamente la riconnessione
                    break;

                // exception
                case BluetoothService.MSG_BT_EXCEPTION:
                    // TODO segnalare le exception ??? variano da dispositivo a dispositivo!!!!
                    String text = (String)msg.obj;
                    if( ( text != null ) && ( text.length() > 0 ) ) {
                        Toast.makeText( mContext, text, Toast.LENGTH_SHORT ).show();
                    } else {
                        Toast.makeText( mContext, "Errore generico", Toast.LENGTH_SHORT ).show();
                    }
                    break;
            }
        }
    };


}
