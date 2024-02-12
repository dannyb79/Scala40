package com.plmtmobile.scala40se;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MPActivity extends BaseGameActivity implements RoomStatusUpdateListener, RoomUpdateListener, RealTimeMessageReceivedListener, RealTimeMultiplayer.ReliableMessageSentCallback, OnInvitationReceivedListener {

    private final static String LEADERBOARD_ID = "CgkIt9Pxh48XEAIQDw";     // ID classifica multiplayer online

    final static int            RC_SELECT_PLAYERS           = 10000;     // activity selezione avversario
    final static int            RC_INVITATION_INBOX         = 10001;     // activity inviti ricevuti
    final static int            RC_WAITING_ROOM             = 10002;     // activity waiting room
    final static int            REQUEST_LEADERBOARD         = 10003;     // activity classifica
    final static int            REQUEST_ACHIEVEMENTS        = 10004;    // activity obiettivi

    private MPCOM               mpcom;                                  // modulo per comunicazione con l'avversario
    private String              mRoomId                     = null;     // codice della stanza di gioco
    private String              rivalParticipantID          = null;     // codice (ID) avversario

    //  definizione stati di gioco
    private final static int    GS_GAME_OVER                = 0;            // fine gioco
    private final static int    GS_SENDING_LOCAL_INFO       = 1;            // invio dati all'avversario
    private final static int    GS_PARSING_RIVAL_INFO       = 2;            // utilizzo dai dell'avversario
    private final static int    GS_READY_TO_START           = 3;            // pronti al gioco
    private final static int    GS_PLAYER_TURN              = 4;            // turno del giocatore corrente
    private final static int    GS_RIVAL_TURN               = 5;            // turno dell'avversario
    private final static int    GS_PLAYER_WINS              = 6;            // vince il giocatore
    private final static int    GS_RIVAL_WINS               = 7;            // vince l'avversario (visualizzazione perdita)
    private final static int    GS_RIVAL_WINS_WAIT_ACTION   = 8;            // vince l'avversario (visualizzazione perdita)
    private final static int    GS_WAITING_REPLAY_CONFIRM   = 9;            // in attesa di conferma nuova partita

    // nomi degli stati di gioco (x debug)
    private String[] game_status_name = {
            "GS_GAME_OVER",
            "GS_SENDING_LOCAL_INFO",
            "GS_PARSING_RIVAL_INFO",
            "GS_READY_TO_START",
            "GS_PLAYER_TURN",
            "GS_RIVAL_TURN",
            "GS_PLAYER_WINS",
            "GS_RIVAL_WINS",
            "GS_RIVAL_WINS_WAIT_ACTION",
            "GS_WAITING_REPLAY_CONFIRM"
    };



    private int                     game_status             = 0;        // stato di gioco corrente
    private int                     mLocalRandomValue       = 0;        // valore random locale
    private int                     mRivalRandomValue       = 0;        // valore random dell'avversario
    private String                  mRivalName              = null;     // nickname dell'avversario
    private boolean                 mActiveSide             = false;    // flag parte attiva o passiva
    private boolean                 mRivalReady             = false;    // flag avversario pronto a cominciare
    private boolean                 replayRequestReceived   = false;    // flag ricezione richiesta replay

    private PermanentData           permanentData;                          // modulo dati permanenti

    ArrayList<GroupClass>           groups = new ArrayList<GroupClass>();   // gruppi di carte sul tavolo
    private PlayerClass             player;                                 // dati giocatore
    public  PlayerClass             rival;                                  // dati avversario
    private DeckClass               deck;                                   // tallone + pozzo
    private MPGraphics              mpGraphics;                             // modulo grafica multiplayer
    // ATTENZIONE!!!! fino alla versione 2.1
    //private byte[]                  startGameData           = new byte[ 14 ];
    // dalla versione 2.2 la dimensione varia da 14 a 95
    private byte[]                  startGameData           = new byte[ 95 ];
    private boolean                 fullStartGameData       = false;
    private ViewFlipper             vf_MPMain;
    private ViewFlipper             vf_MPMenu;
    private int                     last_rival_total_cards  = 0;
    private boolean                 rival_has_left          = false;

    private android.os.Handler      TimedActionHandler          = new android.os.Handler();

    private int                     straight_sort_mode          = 0;
    private boolean                 straight_sort_alternate     = false;
    private int                     values_sort_mode            = 0;
    private boolean                 values_sort_alternate       = false;

    // audio
    private SoundPool               soundPool;
    private int                     turnsound;
    private int                     btwinsound;
    private int                     waitturnsound;
    private int                     btstartgamesound;
    private int                     losesound;
    private int                     replaysound;
    private int                     exitsound;

    private int                     cardpicksound;
    private int                     cardselectionsound;
    private int                     message1sound;
    private int                     buttonsound;

    private boolean                 soundfxenabled = true;

    // sblocco achievements
    private int                     start_turn_total_cards      = 0;
    private int                     consecutive_5_won_games     = 0;
    private int                     consecutive_10_won_games    = 0;
    private int                     consecutive_20_won_games    = 0;
    private int                     consecutive_deals           = 0;

    // punteggio corrente giocatore
    long                            currentPlayerScore          = 0;
    String                          currentPlayerID             = null;

    // mini chat e numero di messaggi
    private static final int        MAX_MESSAGES_PER_GAME       = 20;
    long                            totalSentMessages           = 0;
    public boolean                  chat_message_enable         = true;

    private Context                 mContext;

    // visualizzazione numero di giocatori online
    private static final int        CONTINUOUS_ACTIONS_DELAY    = 15000;    // 15 secondi
    private boolean                 onlinePlayersRequest        = false;
    private boolean                 allGraphicsConnected        = false;


    // activity lifecycle: create, resume, pause, stop, destroy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // rimuove la title bar e la notification bar
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature( Window.FEATURE_NO_TITLE );
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // imposta il layout
        setContentView(R.layout.activity_mp);

        mContext = this;
        // imposta il listener per il bottone di sign in
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugLog("Click su Accedi G+");
                // start the asynchronous sign in flow
                beginUserInitiatedSignIn();
            }
        });

        // view flipper handler
        vf_MPMain = (ViewFlipper) findViewById(R.id.vf_MPMain);
        vf_MPMenu = (ViewFlipper) findViewById(R.id.vf_MPMenu);

        // creazione e inizializzazione modulo per preparazione e decodifica dati p2p
        mpcom = new MPCOM();
        // istanza per recupero/salvataggio dati
        permanentData   = new PermanentData( this );
        // tipo di selezione/ordinamento delle carte
        int card_selector = permanentData.get_card_selector();
        straight_sort_mode  = permanentData.get_straight_sort_mode();
        values_sort_mode    = permanentData.get_values_sort_mode();

        // imposta un valore per lo stato di gioco
        set_game_status( GS_GAME_OVER );
        // instanzia gli oggetti di gioco: mazzo, giocatore, avversario
        // i giocatori vengono creati con id nulli poiche' gli id vengono aggiornati a ogni inizo partita
        deck        = new DeckClass();
        player      = new PlayerClass( PlayerClass.UNDEFINEDPLAYER_ID );
        rival       = new PlayerClass( PlayerClass.UNDEFINEDPLAYER_ID );
        // elimina tutte le carte dal tavolo
        groups.clear();

        // inizializza il modulo di grafica
        mpGraphics  = new MPGraphics( this, this, card_selector, permanentData.get_background_image() );

        // audio
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool           = new SoundPool( 10, AudioManager.STREAM_MUSIC, 0 );
        turnsound           = soundPool.load( this, R.raw.turnalert, 1 );
        btwinsound          = soundPool.load( this, R.raw.btwin, 1 );
        waitturnsound       = soundPool.load( this, R.raw.waitturn, 1 );
        btstartgamesound    = soundPool.load( this, R.raw.startgamesound, 1 );
        losesound           = soundPool.load( this, R.raw.lose_match, 1 );
        replaysound         = soundPool.load( this, R.raw.replay, 1 );
        exitsound           = soundPool.load( this, R.raw.exit, 1 );

        cardpicksound       = soundPool.load( this, R.raw.card_pick, 1 );
        cardselectionsound  = soundPool.load( this, R.raw.tap, 1 );
        message1sound       = soundPool.load( this, R.raw.message1, 1 );
        buttonsound         = soundPool.load( this, R.raw.button, 1 );

        // creazione e ridimensionamento tavolo da gioco
        LinearLayout MPGameLayout = (LinearLayout) findViewById(R.id.MPGameLayout);
        MPGameLayout.post(new Runnable() {
            @Override
            public void run() {
                mpGraphics.post_creation();
                allGraphicsConnected = true;
            }
        });

        // azzera variabili x sblocco achievement
        consecutive_5_won_games     = 0;
        consecutive_10_won_games    = 0;
        consecutive_20_won_games    = 0;
        consecutive_deals           = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        debugLog( "onResume" );
        onlinePlayersRequest = true;
        continuousActions.run();
    }

    @Override
    public void onPause() {
        super.onPause();
        debugLog( "onPause" );
        onlinePlayersRequest = false;
        TimedActionHandler.removeCallbacksAndMessages( null );
    }

    @Override
    public void onStop() {
        super.onStop();
        debugLog( "onStop" );
        onlinePlayersRequest = false;
        TimedActionHandler.removeCallbacksAndMessages( null );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        debugLog( "onDestroy" );
        onlinePlayersRequest = false;
        TimedActionHandler.removeCallbacksAndMessages( null );
    }

    // sign in flow

    @Override
    public void onSignInSucceeded() {
        debugLog("onSignInSucceeded()");
        // registrazione ricezione inviti
        Games.Invitations.registerInvitationListener(getApiClient(),this);
        // abbandona la schermata di login e visualizza il menu
        vf_MPMenu.setDisplayedChild(1);
        // ottiene l'ID corrente del giocatore
        currentPlayerID = Games.Players.getCurrentPlayerId( getApiClient() );
        debugLog( "currentPlayerID " + currentPlayerID );

        // cerca il punteggio corrente nella classifica social
        PendingResult resultSocial = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
                getApiClient(),
                LEADERBOARD_ID,
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_SOCIAL );
        resultSocial.setResultCallback( new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                if(     ( loadPlayerScoreResult != null ) &&
                        ( loadPlayerScoreResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK ) &&
                        ( loadPlayerScoreResult.getScore() != null ) ) {
                            debugLog( "Score (Social) " + Long.toString( loadPlayerScoreResult.getScore().getRawScore() ) );
                            currentPlayerScore = loadPlayerScoreResult.getScore().getRawScore();
                            debugLog( "currentPlayerScore " + currentPlayerScore );
                            show_info( "Punteggio attuale " + currentPlayerScore, Toast.LENGTH_SHORT );
                        } else {
                            debugLog( "Punteggio giocatore non presente in classifica Social. Cerca in Public..." );
                            // cerca nella classifica public
                            PendingResult resultPublic = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
                                    getApiClient(),
                                    LEADERBOARD_ID,
                                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                                    LeaderboardVariant.COLLECTION_PUBLIC );
                            resultPublic.setResultCallback( new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                                @Override
                                public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                                    if(     ( loadPlayerScoreResult != null ) &&
                                            ( loadPlayerScoreResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK ) &&
                                            ( loadPlayerScoreResult.getScore() != null ) ) {
                                                debugLog( "Score (Public) " + Long.toString( loadPlayerScoreResult.getScore().getRawScore() ) );
                                                currentPlayerScore = loadPlayerScoreResult.getScore().getRawScore();
                                                debugLog( "currentPlayerScore " + currentPlayerScore );
                                                show_info( "Punteggio attuale " + currentPlayerScore, Toast.LENGTH_SHORT );
                                            } else {
                                                // punteggio non trovato
                                                debugLog( "Punteggio giocatore non presente in nessuna classifica" );
                                                show_info("Punteggio giocatore non presente in classifica", Toast.LENGTH_SHORT);
                                            }
                                    }
                            });
                        }
                }
        });
    }

    @Override
    public void onSignInFailed() {
        debugLog("onSignInFailed() " + getGameHelper().getSignInError().toString());
    }

    // disconnessione
    public void signout(View v) {
        // deregistrazione ricezione inviti
        Games.Invitations.unregisterInvitationListener(getApiClient());
        // signout
        signOut();
        // chiusura activity
        this.finish();
    }

    // callbacks inviti
    public String invitationId = null;

    @Override
    public void onInvitationReceived(Invitation invitation) {
        if( invitation != null ) {
            debugLog("onInvitationReceived " + invitation.toString() );
            String rivalName = invitation.getInviter().getDisplayName();
            invitationId = invitation.getInvitationId();
            // altrimenti visualizza la finestra di accettazione
            mpGraphics.showDialog( MPGraphics.MPWAITDLG_INVITATION_RECEIVED, rivalName, 0, false );

        } else {
            debugLog("onInvitationReceived" );
        }
    }

    @Override
    public void onInvitationRemoved(String s) {
        debugLog("onInvitationRemoved " + s );

    }

    public void onAcceptInvitation() {
        debugLog("onAcceptInvitation " + invitationId );
        if( invitationId != null ) {
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept( invitationId );
            Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());
            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void onRefuseInvitation() {
        debugLog("onRefuseInvitation" );
    }

    // buttons

    // esce dall'activity MP e torna all'activity principale
    public void onBackToMainMenu(View v) {
        debugLog("Uscita e ritorno al menu principale");
        this.finish();
    }

    // visualizza la classifica
    public void onLeaderboardButtonClick(View v) {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), LEADERBOARD_ID), REQUEST_LEADERBOARD);
    }

    // visualizza obiettivi raggiunti
    public void onAchievementsButtonClick(View v) {
        startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
    }

    // invita altri giocatori
    public void show_player_selection(View v) {
        startActivityForResult(Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 1), RC_SELECT_PLAYERS);
    }

    // visualizza gli inviti da parte di altri giocatori
    public void show_invitation_box(View v) {
        startActivityForResult(Games.Invitations.getInvitationInboxIntent(getApiClient()), RC_INVITATION_INBOX);
    }

    // il giocatore vuole abbandorare il match e tornare al menu
    public void onGiveUpMatchClick( View v ) {
        // invio messaggio abbandono del gioco
        sendLeaveGame();
        // player wants to leave the room.
        Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
        // imposta lo stato di gioco a game over
        set_game_status( GS_GAME_OVER );
        // torna al menu
        exit_game_screen();
    }

    // intercettazione del tasto "back"
    @Override
    public void onBackPressed() {
        if( isPlaying() ) {
            new AlertDialog.Builder(this)
                    .setMessage("Vuoi davvero abbandonare la partita?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            onGiveUpMatchClick( null );
                        }
                    }).create().show();
        } else {
            MPActivity.super.onBackPressed();
        }
    }

    // risultato activity selezione giocatore, stanza di attesa, inviti...

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        if (request == RC_SELECT_PLAYERS) {
            if( response != Activity.RESULT_OK ) {
                // il giocatore ha annullato
                return;
            }
            // get the invitee list
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            // get auto-match criteria
            Bundle autoMatchCriteria;
            int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }
            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(getApiClient(), roomConfig);
            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (request == RC_WAITING_ROOM) {
            if (response == Activity.RESULT_OK) {
                // (start game)
                enter_game_screen();
            }
            else if (response == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.

                // in this example, we take the simple approach and just leave the room:
                Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        if (request == RC_INVITATION_INBOX) {
            if (response != Activity.RESULT_OK) {
                // canceled
                return;
            }

            // get the selected invitation
            Bundle extras = data.getExtras();
            Invitation invitation = extras.getParcelable(Multiplayer.EXTRA_INVITATION);

            // accept it!
            RoomConfig roomConfig = makeBasicRoomConfigBuilder()
                    .setInvitationIdToAccept(invitation.getInvitationId())
                    .build();
            Games.RealTimeMultiplayer.join(getApiClient(), roomConfig);
            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // go to game screen
        }


    }

    // callbacks creazione stanza di gioco

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        debugLog("onRoomCreated() " + Integer.toString(statusCode) + " " + room.toString() );
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // rilascia il flag di schermo sempre attivo
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // visualizza messaggio di errore.
            show_info("Errore creazione stanza di gioco", Toast.LENGTH_LONG);
            return;
        }
        // imposta la room ID
        mRoomId = room.getRoomId();
        // stanza di attesa
        startActivityForResult( Games.RealTimeMultiplayer.getWaitingRoomIntent( getApiClient(), room, Integer.MAX_VALUE ), RC_WAITING_ROOM );
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if( room != null ) {
            debugLog("onJoinedRoom() " + Integer.toString(statusCode) + " " + room.toString() );
        }
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // rilascia il flag di schermo sempre attivo
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // visualizza messaggio di errore.
            show_info("Errore creazione stanza di gioco", Toast.LENGTH_LONG);
            return;
        }
        // imposta la room ID
        mRoomId = room.getRoomId();
        // stanza di attesa
        startActivityForResult( Games.RealTimeMultiplayer.getWaitingRoomIntent( getApiClient(), room, Integer.MAX_VALUE ), RC_WAITING_ROOM );
    }

    @Override
    public void onLeftRoom(int statusCode, String s) {
        debugLog("onLeftRoom() " + Integer.toString(statusCode) + " " + s );
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // rilascia il flag di schermo sempre attivo
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // visualizza messaggio di errore.
            show_info("Errore creazione stanza di gioco", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        debugLog("onRoomConnected() " + Integer.toString(statusCode) + " " + room.toString() );
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // rilascia il flag di schermo sempre attivo
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // visualizza messaggio di errore.
            show_info("Errore creazione stanza di gioco", Toast.LENGTH_LONG);
            return;
        }
        // imposta la room ID
        mRoomId = room.getRoomId();

        ArrayList<Participant> list = room.getParticipants();


        Uri rivalIconUri;
        String player0Id = null;
        String player1Id = null;
        String player0ParticipantId = null;
        String player1ParticipantId = null;
        Uri player0IconUri = null;
        Uri player1IconUri = null;

        if( list != null ) {
            if( list.size() > 0 ) {
                Participant participant_0 = list.get( 0 );
                if( participant_0 != null ) {
                    Player player_0 = participant_0.getPlayer();
                    if( player_0 != null ) {
                        player0Id = player_0.getPlayerId();
                    }
                    player0IconUri = participant_0.getIconImageUri();
                    player0ParticipantId = participant_0.getParticipantId();
                }
            }
            if( list.size() > 1 ) {
                Participant participant_1 = list.get( 1 );
                if( participant_1 != null ) {
                    Player player_1 = participant_1.getPlayer();
                    if( player_1 != null ) {
                        player1Id = player_1.getPlayerId();
                    }
                    player1IconUri = participant_1.getIconImageUri();
                    player1ParticipantId = participant_1.getParticipantId();
                }
            }
        }

        debugLog("currentPlayerID " + currentPlayerID );
        debugLog("player0Id " + player0Id );
        debugLog("player1Id " + player1Id );
        debugLog("player0ParticipantId " + player0ParticipantId );
        debugLog("player1ParticipantId " + player1ParticipantId );
        if( player0IconUri != null ) {
            debugLog("player0IconUri " + player0IconUri.toString() );
        }
        if( player1IconUri != null ) {
            debugLog("player1IconUri " + player1IconUri.toString() );
        }

        // i dati disponibili della stanza (oggetto room) sono differenti a seconda del tipo di connessione:
        // 1) se la connessione tra 2 giocatori avviene tramite invito sono instanziati gli oggetti Player dei 2
        // oggetti Participant della stanza percui sono disponibili entrambi i PlayerId; pertanto e' possibile
        // confrontare l'Id del giocatore (contenuto nella variabile currentPlayerID) con i PlayerId dei Participants
        // per capire chi e' l'avversario e ottenere l'Uri dell'icona
        // 2) se la connessione tra 2 giocatori avviene per auto-match nella lista dei Partecipants l'oggetto Player
        // dell'avversario non e' istanziato per cui si puo' capire immediatamente quale dei 2 Partecipant e' l'avversario
        // e ottenere cosi' l'Uri dell'icona

        // se il Player 0 non e' istanziato significa che e' l'avversario
        if( player0Id == null ) {
            // estraggo dal Participant 0 l'Uri dell'icona
            rivalIconUri = player0IconUri;
        } else if( player1Id == null ) {
            // se il Player 1 non e' istanziato significa che e' l'avversario,
            // estraggo dal Participant 1 l'Uri dell'icona
            rivalIconUri = player1IconUri;
        } else {
            // se Player 0 e Player 1 sono istanziati devo stabilire chi e' l'avversario
            if( player0Id.compareTo( currentPlayerID ) == 0 ) {
                rivalIconUri = player1IconUri;
                rivalParticipantID = player1ParticipantId;
            } else {
                rivalIconUri = player0IconUri;
                rivalParticipantID = player0ParticipantId;
            }
        }
        debugLog( "rivalParticipantID " + rivalParticipantID );

        ImageView iv_RivalIcon = (ImageView)findViewById(R.id.iv_RivalIcon);
        if( rivalIconUri != null ) {
            ImageManager imageManager = ImageManager.create(this);
            imageManager.loadImage( iv_RivalIcon, rivalIconUri, R.drawable.defaultusericon );
        } else {
            iv_RivalIcon.setImageDrawable( getResources().getDrawable( R.drawable.defaultusericon ) );
        }

        // avvia procedura di sincronizzazione dei dispositivi
        start_sync_devices();
    }

    // callbacks connessione dei giocatori alla stanza di gioco

    @Override
    public void onRoomConnecting(Room room) {
        debugLog("onRoomConnecting() " + room.toString() );
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        debugLog("onRoomAutoMatching() " + room.toString() );
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {
        debugLog("onPeerInvitedToRoom() " + room.toString() );
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {
        debugLog("onPeerDeclined() " );
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {
        debugLog("onPeerJoined()");
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
        debugLog("onPeerLeft()");
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onConnectedToRoom(Room room) {
        debugLog("onConnectedToRoom()");
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        debugLog("onDisconnectedFromRoom()");
        // imposta la room ID
        mRoomId = room.getRoomId();
        // se ci si trova in gioco si ritorna al menu' principale
        if( isPlaying() ) {
            exit_game_screen();
            if(!rival_has_left) {
                show_info( "Giocatore disconnesso", Toast.LENGTH_SHORT );
            }
            rival_has_left = false;
        }
    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {
        debugLog("onPeersConnected()");
        // imposta la room ID
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {
        debugLog("onPeersDisconnected()");
        // se ci si trova in gioco si ritorna al menu' principale
        if( isPlaying() ) {
            exit_game_screen();
            if(!rival_has_left) {
                show_info( "Giocatore disconnesso", Toast.LENGTH_SHORT );
            }
            rival_has_left = false;
        }
    }

    @Override
    public void onP2PConnected(String s) {
        debugLog("onP2PConnected()");
    }

    @Override
    public void onP2PDisconnected(String s) {
        debugLog("onP2PDisconnected()");
    }

    // callback messaggio ricevuto dall'altro giocatore

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        messageReceivedSelectAction(realTimeMessage.getMessageData());
    }

    @Override
    public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId ) {
        debugLog( "onRealTimeMessageSent " + statusCode );
        if( statusCode == GamesStatusCodes.STATUS_OK ) {
            debugLog( "Messaggio token " + tokenId + " inviato a " + recipientParticipantId );
        } else if( statusCode == GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED ) {
            debugLog( "Messaggio token " + tokenId + " invio fallito" );
        } else if( statusCode == GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED ) {
            debugLog( "Messaggio token " + tokenId + " stanza non connessa" );
        }
    }

    // invio dati all'avversario
    private void sendDataToRival( byte[] data ) {
        Games.RealTimeMultiplayer.sendReliableMessage(
                getApiClient(),
                this,
                data,
                mRoomId,
                rivalParticipantID );
    }

    // funzioni di gioco

    // ritorna true se la schermata visualizzata e' quella di gioco
    private boolean isPlaying() {
        return ( vf_MPMain.getDisplayedChild() == 1 );
    }

    // visualizza la schermata di gioco (seconda pagina del view flipper)
    public void enter_game_screen() {
        vf_MPMain.setDisplayedChild( 1 );
    }

    // esce dalla schermata di gioco e torna al menu multiplayer
    public void exit_game_screen() {
        // svuota la coda dell'handler
        TimedActionHandler.removeCallbacksAndMessages( null );
        continuousActions.run();
        // elimina qualunque dialog
        mpGraphics.hideDialog();
        // visualizza la prima pagina del viewFlipper principale (menu)
        vf_MPMain.setDisplayedChild( 0 );
        // visualizza la seconda pagina del viewFlipper del menu (menu multiplayer)
        vf_MPMenu.setDisplayedChild( 1 );
    }

    // impostazione stati di gioco
    private void set_game_status( int new_status ) {
        game_status = new_status;

        debugLog( "Game status : " + game_status_name[ new_status ] );
    }




    // invio messaggi

    // calcolo e invio nuovo numero random
    private void sendNewLocalRandomValue() {

        // estrazione numero random locale...
        mLocalRandomValue = mpcom.getMPCOMRandomValue();
        // ... impacchettamento e invio
        byte[] msgValue = mpcom.wrapLocalValue(mLocalRandomValue);
        sendDataToRival(msgValue);

        debugLog("Invio valore random " + Integer.toString(mLocalRandomValue));
    }

    // invio nickname del giocatore
    private void sendPlayerNickName() {
        String username;
        // nickname o nome dell'account ?
        if( permanentData.get_online_name() == PermanentData.ONLINESETTINGS_ACCOUNT_NAME) {
            // nome associato all'account
            username = Games.Players.getCurrentPlayer(getApiClient()).getDisplayName();
            debugLog( "Invio nick giocatore (Classifica Multiplayer) " + username );
        } else {
            // nickname utilizzato anche nella classifica generale
            username = permanentData.get_user_name();
            debugLog( "Invio nick giocatore (Classifica CPU)" + username );
        }
        // ... impacchettamento e invio
        byte[] msgName = mpcom.wrapUserName( username );
        sendDataToRival(msgName);
    }

    // invio dati partita
    private void sendStartGameData() {
        // impacchettamento e invio
        byte[] msgGame = mpcom.wrapStartGameData( rival, deck );
        sendDataToRival(msgGame);
        debugLog("Invio dati inizio partita");
    }

    // invio messaggio pronti a cominciare
    private void sendReadyToStart() {
        // impacchettamento ...
        byte[] msgReady = mpcom.wrapReadyToStart();
        // ... e invio
        sendDataToRival(msgReady);
        debugLog("Invio READY TO START");
    }

    // invio dati fine turno
    private void sendEndTurnData() {
        // impacchettamento...
        byte[] msgEOT = mpcom.wrapEndTurnData( player, deck, groups );
        // verifica validita' e dimensione pacchetto dati
        if( msgEOT == null ) {
            debugLog("ERRORE! Impossibile inviare dati fine turno (msgEOT null)");
        } else if( msgEOT.length > Multiplayer.MAX_RELIABLE_MESSAGE_LEN ) {
            debugLog("ERRORE! Pacchetto dati fine turno troppo grande " + Integer.toString(msgEOT.length) + " bytes");
        } else {
            // ok! invio
            sendDataToRival(msgEOT);
            debugLog( "Invio dati fine turno" + Integer.toString( msgEOT.length ) + " bytes" );
        }
    }

    // invio punteggio per aggiornamento classifica
    private void sendEndOfTurnScore( int score ) {
        // ... impacchettamento e invio
        byte[] msgScore = mpcom.wrapEndOfTurnScore(score);
        sendDataToRival(msgScore);
        debugLog("Invio punti fine partita " + Integer.toString(score));
    }

    // invio messaggio voglio rigiocare
    private void sendWantReplay() {
        byte[] msgReplay = mpcom.wrapWantReplay();
        sendDataToRival(msgReplay);
        debugLog( "Invio richiesta REPLAY" );
    }

    // invio messaggio ciao me ne vado
    private void sendLeaveGame() {
        byte[] msgLeave = mpcom.wrapLeaveGame();
        sendDataToRival(msgLeave);

        debugLog( "Invio messaggio abbandono del gioco" );
    }

    // invio messaggio all'altro giocatore
    private void sendUserMessage( String message ) {
        byte[] msgMessage = mpcom.wrapUserMessage(message);
        sendDataToRival( msgMessage );
        // incrementa il numero massimo di messaggi inviati in questa partita
        totalSentMessages += 1;
        debugLog( "Invio Messaggio " + message );
    }

    // visualizzazione info generiche (toast)
    public void show_info( String text, int duration ) {
        Toast toast = Toast.makeText( this, text, duration );
        toast.show();
    }

    // debug a video e a console
    private void debugLog(String msg) {
        // TODO togliere log
        String debugTag = "MPCOM";
        Log.v(debugTag, msg);
    }

    // inizio nuova partita
    private void start_new_mpgame() {
        debugLog("Inizio partita");
        // azzera il flag di ricezione richiesta di replay
        replayRequestReceived = false;
        // azzera il numero di mani consecutive (x sblocco obiettivo)
        consecutive_deals = 0;
        // azzera il numero di messaggi inviati
        totalSentMessages = 0;
        // azzera il flag il giocatore ha abbandonato il gioco
        rival_has_left = false;
        // inizia la partita sempre la parte attiva (casuale)
        if( mActiveSide ) {
            // la parte attiva ha l'ID pari a MASTERPLAYER_ID
            player.set_player_id( PlayerClass.MASTERPLAYER_ID );
            // stato di gioco -> gioco turno
            set_game_status(GS_PLAYER_TURN);

            // aggiorna il numero di mani consecutive (x sblocco obiettivo)
            consecutive_deals += 1;

        } else {

            // la parte passiva ha l'ID pari a RIVALPLAYER_ID
            player.set_player_id( PlayerClass.RIVALPLAYER_ID );
            // stato di gioco -> attesa turno
            set_game_status( GS_RIVAL_TURN );
            // le carte del giocatore e del pozzo devono essere impostate con quelle
            // contenute nell'array startGameData
            for( int i = 0; i < 13; i++ ) {
                player.cards.get( i ).value = startGameData[ i ];
            }
            deck.cull_cards[ deck.cull_size - 1 ] = startGameData[ 13 ];
            // questo controllo e' per la retrocompatibilita' giocando con versioni non ancora aggiornate
            if(fullStartGameData) {
                for( int i = 0; i < 81; i++ ) {
                    deck.pool_cards[ i ] = startGameData[ 14 + i ];
                }
            }
        }

        // pulizia del tavolo
        groups.clear();
        mpGraphics.draw_table( groups, true );

        // imposta i dati giocatore di inizio turno
        player.start_turn();

        // scopre le carte in mano al giocatore
        mpGraphics.draw_player_cards( player );
        // scopre la carta del pozzo
        mpGraphics.draw_last_discarded_card(deck);
        // audio inizio partita
        soundPool.play( btstartgamesound, 1.0f, 1.0f, 0, 0, 1.0f );
        // cancella dialog "sincronizzazione partite"
        mpGraphics.hideDialog();

        // info di inizio partita
        if( mRivalName != null ) {
            show_info("Inizio partita contro " + mRivalName, Toast.LENGTH_SHORT);
        } else {
            show_info( "Inizio partita", Toast.LENGTH_SHORT );
        }
        if( mActiveSide ) {
            show_info( "E' il tuo turno", Toast.LENGTH_SHORT );
            // numero di carte iniziale dell'avversario, a inzio partita e' sempre 13
            // senza lampadina perche' e' il turno del giocatore
            last_rival_total_cards = 13;
            mpGraphics.draw_total_rival_cards( last_rival_total_cards, false);

        } else {
            show_info( "Inizia il tuo avversario", Toast.LENGTH_SHORT );
            // numero di carte iniziale dell'avversario, a inzio partita e' sempre 13
            // con lampadina perche' e' il turno dell'avversario
            last_rival_total_cards = 13;
            mpGraphics.draw_total_rival_cards( last_rival_total_cards, true );
        }
        // imposta il n. di carte a inzio partita (x sblocco achvm. "Chiusura in mano")
        start_turn_total_cards = 13;
    }

    // selezione tallone da parte del giocatore
    public void onPoolClick( View v ) {
        // se e' il turno del giocatore e il giocatore non ha ancora pescato...
        if( ( game_status == GS_PLAYER_TURN ) && (!player.has_picked) ) {
            if( soundfxenabled ) {
                soundPool.play( cardpicksound, 1.0f, 1.0f, 0, 0, 1.0f );
            }
            // pesca una carta dal tallone
            player.pick_from_pool( deck );
            // ristampa le carte del giocatore
            mpGraphics.draw_player_cards(player);
        }
    }

    // selezione pozzo
    public void onCullClick( View v ) {
        // se e' il turno del giocatore
        if( game_status == GS_PLAYER_TURN ) {
            // il giocatore ha scelto il pozzo
            CullActionResult result = player.cull_action( deck, groups );
            // segnalazioni
            if( result.update_screen ) {
                mpGraphics.draw_player_cards(player);
                mpGraphics.draw_last_discarded_card(deck);
                if( soundfxenabled ) {
                    soundPool.play( cardpicksound, 1.0f, 1.0f, 0, 0, 1.0f );
                }
            }
            if( result.open_failure ) {
                show_info( "Almeno 40 punti per aprire", Toast.LENGTH_SHORT );
                mpGraphics.draw_table(groups, true);
            } if( result.cull_pick_failure ) {
                show_info( "Obbligatorio aprire con la carta pescata dal pozzo", Toast.LENGTH_LONG );
                mpGraphics.draw_table(groups, true);
            } else if( result.discarded_card ) {

                // se ho vinto...
                if( player.cards.size() == 0 ) {
                    // ..imposto lo stato di gioco
                    set_game_status( GS_PLAYER_WINS );
                    // audio vincita
                    soundPool.play( btwinsound, 1.0f, 1.0f, 0, 0, 1.0f );
                    // sblocca l'obiettivo "chiusura in mano" se a inizio mano avevo 13 carte
                    if( start_turn_total_cards == 13 ) {
                        Games.Achievements.unlock(getApiClient(), "CgkIt9Pxh48XEAIQCA");
                    }
                    consecutive_5_won_games += 1;
                    if( consecutive_5_won_games >= 5 ) {
                        // sblocca l'obiettivo "Vinci 5 partite consecutive"
                        Games.Achievements.unlock(getApiClient(), "CgkIt9Pxh48XEAIQCg");
                        consecutive_5_won_games = 0;
                    }
                    consecutive_10_won_games += 1;
                    if( consecutive_10_won_games >= 10 ) {
                        // sblocca l'obiettivo "Vinci 10 partite consecutive"
                        Games.Achievements.unlock(getApiClient(), "CgkIt9Pxh48XEAIQCQ");
                        consecutive_10_won_games = 0;
                    }
                    consecutive_20_won_games += 1;
                    if( consecutive_20_won_games >= 10 ) {
                        // sblocca l'obiettivo "Vinci 20 partite consecutive"
                        Games.Achievements.unlock(getApiClient(), "CgkIt9Pxh48XEAIQCw");
                        consecutive_20_won_games = 0;
                    }

                    if( consecutive_deals <= 5 ) {
                        // sblocca l'obiettivo "Vinci con meno di 5 mani"
                        Games.Achievements.unlock(getApiClient(), "CgkIt9Pxh48XEAIQDA");
                        consecutive_deals = 0;
                    }

                    // NOTE la dialog che informa della vittoria viene visualizzata al ricevimento
                    // del messaggio contenente i punti dell'avversario

                } else {

                    // e' il turno dell'avversario
                    set_game_status( GS_RIVAL_TURN );
                    // audio attesa turno
                    soundPool.play( waitturnsound, 1.0f, 1.0f, 0, 0, 1.0f );
                    // aggiorna il numero di carte dell'avversario con lampadina perche' e' il suo turno
                    mpGraphics.draw_total_rival_cards( last_rival_total_cards, true );
                }
                // invio dati relativi al numero di carte del giocatore e alle carte sul tavolo
                sendEndTurnData();
            }
        }
    }

    // selezione di una carta
    public void onPlayerCardClick( View v ) {
        int position;
        // se e' il turno del giocatore e il giocatore ha gia' pescato
        if( ( game_status == GS_PLAYER_TURN ) && ( player.has_picked ) ) {
            if( soundfxenabled ) {
                soundPool.play( cardselectionsound, 1.0f, 1.0f, 0, 0, 1.0f );
            }
            // recupero l'id (0-13) della carta selezionata contenuto nel Tag
            position = Integer.parseInt( v.getTag().toString() );
            // seleziona la carta del giocatore e in base al nuovo sata aggiorna la grafica
            if( player.select_card( position ) ) {
                mpGraphics.draw_selected_card(v, position, player.cards.get(position).value, true);
            } else {
                mpGraphics.draw_selected_card(v, position, player.cards.get(position).value, false);
            }
        }
    }

    // trascinamento di una carta
    public void onDropCard( View start, View end ) {
        int     moving_card;
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
            mpGraphics.draw_player_cards(player);
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
            mpGraphics.draw_player_cards(player);
        }
    }

    // selezione di una carta sul tavolo
    public void onTableClick( View v ) {
        int card_tag;
        if( game_status == GS_PLAYER_TURN ) {
            // recupero l'indice della carta o del gruppo di carte selezionato
            card_tag = Integer.parseInt( v.getTag().toString() );
            // azione del giocatore sul tavolo
            TableActionResult result = player.table_action( card_tag, groups );
            // aggiornamento a video carte giocatore e carte sul tavolo
            if( result.update_screen ) {
                mpGraphics.draw_table(groups, false);
                mpGraphics.draw_player_cards(player);
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
        if( ( game_status == GS_PLAYER_TURN ) || ( game_status == GS_RIVAL_TURN ) ) {
            if( soundfxenabled ) {
                soundPool.play( buttonsound, 1.0f, 1.0f, 0, 0, 1.0f );
            }

            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.buttonclick) );
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
            mpGraphics.draw_player_cards(player);
        }
    }

    /*
        ordinamento carte giocatore in ordine crescente divise per valori
        [Assi][2][3][4]...[K][jokers]
    */
    public void onSortValuesButtonClick( View v ) {
        if( ( game_status == GS_PLAYER_TURN ) || ( game_status == GS_RIVAL_TURN ) ) {
            if( soundfxenabled ) {
                soundPool.play( buttonsound, 1.0f, 1.0f, 0, 0, 1.0f );
            }

            v.startAnimation( AnimationUtils.loadAnimation( this, R.anim.buttonclick ) );
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
            mpGraphics.draw_player_cards(player);
        }
    }

    // il giocatore invita l'avversario a rigiocare
    public void onWantReplayClick( View v ) {
        // gioco in stato di attesa conferma rinvincita
        set_game_status( GS_WAITING_REPLAY_CONFIRM );
        // invio messaggio "voglio la rivincita"
        sendWantReplay();
        // visualizza la dialog box di attesa
        mpGraphics.showDialog( MPGraphics.MPWAITDLG_WAIT_REPLAY_CONFIRM, null, 0, false );
    }

    // il giocatore accetta di rigiocare
    public void onAcceptReplayClick( View v ) {
        // cancella la dialog box
        mpGraphics.hideDialog();
        // invia il messaggio di rinvicta
        sendWantReplay();
        // inizia una nuova partita
        start_sync_devices();
    }

    // avvio del flusso di messaggi da parte dei dispositivi per scambiare i dati delle carte,
    // decidere chi inizia la partita e scambiare le informazioni circa i nomi dei due giocatori
    private void start_sync_devices() {

        mpGraphics.showDialog( MPGraphics.MPWAITDLG_SYNC_DEVICES, null, 0, true );

        // flag "avversario non pronto a cominciare una partita"
        mRivalReady = false;

        // preparazione del gioco
        groups.clear();                     // elimina tutte le carte dal tavolo
        deck.reset();                       // inizializza e mescola il mazzo
        player.reset( deck );               // inizializza il giocatore e gli da 13 carte
        rival.reset( deck );                // inizializza l'avversario e gli da 13 carte
        deck.add_cull( deck.pick_card() );  // 1 carta va presa dal tallone e aggiunta al pozzo

        // pulisce il tavolo
        mpGraphics.draw_table( groups, true );

        // stato di gioco -> invio informazioni all'altro dispositivo
        set_game_status(GS_SENDING_LOCAL_INFO);

        // ========== STEP 1 - INVIO DATI DI GIOCO =============

        sendStartGameData();

        // ========== STEP 2 - INVIO NICK GIOCATORE ============

        sendPlayerNickName();

        // ========== STEP 3 - INVIO NUMERO RANDOM ============

        sendNewLocalRandomValue();

        // stato di gioco -> pronto a confrontare le informazioni dell'avversario
        set_game_status( GS_PARSING_RIVAL_INFO );
    }

    // click sull'avversario per inviare messaggi
    public void onRivalCardsClick( View v ) {
        // se e' stato raggiunto il numero massimo di messaggi avvisa l'utente ed esce
        if( totalSentMessages >= MAX_MESSAGES_PER_GAME ) {
            show_info( "Max " + Integer.toString( MAX_MESSAGES_PER_GAME ) + " messaggi per partita", Toast.LENGTH_SHORT );
            return;
        }
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.messageprompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this );
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        // set dialog message
        alertDialogBuilder
                .setPositiveButton("Invia",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // il messaggio viene inviato solo se si e' ancora in gioco
                                if (isPlaying()) {
                                    if( userInput.getText().toString().length() < 160 ) {
                                        sendUserMessage(userInput.getText().toString());
                                    } else {
                                        show_info( "Messaggio troppo lungo (max 160 caratteri)", Toast.LENGTH_SHORT );
                                    }
                                }
                            }
                        })
                .setNegativeButton("Annulla",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    // copia di un sottoinsieme di un array
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
         se siamo pronti (game_status = GS_PARSING_RIVAL_INFO) confrontiamo i valori random
         altrimenti posticipiamo l'operazione
     */
    private void compareRandomValues() {
        // per procedere al confronto la parte locale deve essere pronta...
        if( game_status == GS_PARSING_RIVAL_INFO ) {

            if( mLocalRandomValue > mRivalRandomValue ) {
                // io sono la parte attiva :) e l'avversario quella passiva
                mActiveSide = true;
                // sono pronto a cominciare
                set_game_status( GS_READY_TO_START );
                // e lo comunico all'avversario
                sendReadyToStart();
                // se ho gia' ricevuto il messaggio "sono pronto" dall'avversio do inizio alla partita
                if( mRivalReady ) {
                    // inizio nuova partita
                    start_new_mpgame();
                }

                debugLog("Sono attivo!");

            } else if( mLocalRandomValue < mRivalRandomValue ) {

                // io sono la parte passiva O_o' e l'avversario quella attiva
                mActiveSide = false;
                // sono pronto a cominciare
                set_game_status( GS_READY_TO_START );
                // e lo comunico all'avversario
                sendReadyToStart();
                // se ho gia' ricevuto il messaggio "sono pronto" dall'avversio do inizio alla partita
                if( mRivalReady ) {
                    // inizio nuova partita
                    start_new_mpgame();
                }

                debugLog("Sono passivo!");

            } else {
                // nel caso eccezionalissimo che i 2 numeri fossero uguali il numero viene ricalcolato e reinviato
                sendNewLocalRandomValue();

                debugLog( "Reinvio numero random!" );
            }
        } else {
            // ...altrimenti l'operazione di confronto deve essere posticipata
            // impostare l'handler per la chiamata posticipata a questa funzione
            TimedActionHandler.postDelayed( new Runnable() {
                @Override
                public void run() {
                    compareRandomValues();
                }
            }, 1000 );

            debugLog("Posticipo confronto valori random!");
        }
    }

    // visualizza messaggio chat ricevuto da altro giocatore
    private void show_chat_message( String text ) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.chat_layout, (ViewGroup) findViewById( R.id.chat_layout ) );
        TextView txt = (TextView) layout.findViewById( R.id.chat_text );
        txt.setText(text);
        Toast toast = new Toast( getApplicationContext() );
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration( Toast.LENGTH_SHORT );
        toast.setView(layout);
        toast.show();
    }

    // informa il giocatore che l'avversario ha abbandonato il gioco
    private void show_leavegame_message() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.leavegame_layout, ( ViewGroup ) findViewById( R.id.leavegame_layout ) );
        TextView txt = (TextView) layout.findViewById( R.id.leavegame_text );
        txt.setText( mRivalName + " lascia il gioco" );
        Toast toast = new Toast( getApplicationContext() );
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration( Toast.LENGTH_SHORT );
        toast.setView( layout );
        toast.show();
    }

    // questa funzione viene chiamata dopo qualche secondo al termine di una partita perdente
    private void showLosingGameDialog() {
        set_game_status( GS_RIVAL_WINS_WAIT_ACTION );
        // se non ci sono richieste pendenti di replay visualizza normale
        if(!replayRequestReceived) {
            mpGraphics.showDialog( MPGraphics.MPWAITDLG_LOST, null, 0, false );
        } else {
            // altrimenti visualizza la finestra di accettazione
            mpGraphics.showDialog( MPGraphics.MPWAITDLG_ACCEPT_REPLAY_REQUEST, mRivalName, 0, false );
        }
    }

    // interpreta i dati ricevuti dal dispositivo dell'avversario
    private void messageReceivedSelectAction( byte[] msg ) {
        // controllo validita' del pacchetto dati
        if( !mpcom.checkData( msg ) ) {
            return;
        }
        // estrazione comando (primo byte del pacchetto)
        int command = (int) msg[ 0 ];
        // azioni diverse a seconda del pacchetto dati ricevuto
        if( command == MPCOM.MPCOMCMD_RANDOM_VALUE) {

            // RICEZIONE NUMERO RANDOM DELL'AVVERSARIO

            byte[]  temp = copyOfRange( msg, 1, msg.length - 4 );
            // la variabile contenente il numero random dell'avversario e' globale
            mRivalRandomValue = mpcom.convertByteArrayToInteger(temp);

            // l'arrivo nell'esatta sequenza dei messaggi dovrebbe essere garantita dalla
            // sendReliableRealTimeMessage, all'arrivo dell'ultimo messaggio (valore random)
            // si da per scontato che siano gia' arrivati i dati partita e il nick del giocatore;
            // se noi siamo pronti al confronto dei valori random bene, altrimenti l'operazione
            // viene posticipata a 1 secondo
            compareRandomValues();

            debugLog( "Numero ricevuto " + Integer.toString( mRivalRandomValue ) );

        } else if( command == MPCOM.MPCOMCMD_NICKNAME) {

            // RICEZIONE NICKNAME DELL'AVVERSARIO

            // se il pacchetto ricevuto e' di 5 bytes non contiene alcun nome!
            mRivalName = null;
            if( msg.length > 5 ) {

                // estrazione byte per deserializzazione oggetto String
                byte[]  temp = copyOfRange(msg, 1, msg.length - 4);
                // cerca di convertire in stringa i dati ricevuti
                try {
                    mRivalName = (String)mpcom.toObject( temp );
                } catch ( ClassNotFoundException e ) {
                    show_info(e.getMessage(), Toast.LENGTH_SHORT);
                } catch ( IOException e ) {
                    show_info(e.getMessage(), Toast.LENGTH_SHORT);
                }
            } else {
                // se la conversione non riesce assegna un nome di default
                mRivalName = "il tuo avversario";
            }
            // se il nickname ricevuto non e' valido (e' nullo o troppo corto)
            // viene utilizzato "il tuo avversario"
            if( mRivalName == null ) {
                mRivalName = "il tuo avversario";
            } else {
                if( mRivalName.length() <= 1 ) {
                    mRivalName = "il tuo avversario";
                }
            }
            debugLog( "Nickname avversario " + mRivalName );

        } else if( command == MPCOM.MPCOMCMD_READY_TO_START) {

            // RICEZIONE AVVERSARIO PRONTO A COMINCIARE LA PARTITA

            // impostazione flag (variabile globale) "avversario pronto"
            mRivalReady = true;
            // se mi trovo gia' in attesa dell'avversario do inizio alla partita
            if( game_status == GS_READY_TO_START ) {
                // inizio nuova partita
                start_new_mpgame();
            }

            debugLog("Ricezione READY TO START");

        } else if( command == MPCOM.MPCOMCMD_LEAVE_GAME) {
            // flag il giocatore ha abbandonatao il gioco (di sua iniziativa)
            rival_has_left = true;
            // visaulizza il mesaggio di uscita
            show_leavegame_message();
            // audio uscita del giocatore
            soundPool.play( exitsound, 1.0f, 1.0f, 0, 0, 1.0f );
            debugLog("Ricezione Abbandono del gioco");

        } else if( command == mpcom.MPCOMCMD_GAME_DATA ) {

            // RICEZIONE DATI INIZIO PARTITA
            // (da utilizzare solo se viene determinato che questa e' la parte passiva)
            //Debug.LogBytesArray( "MPCOMCMD_GAME_DATA msg: ", msg );
            // i dati partita vengono inseriti in un buffer temporaneo e verranno utilizzati
            // solo se questa parte risulta passiva

            // questo controllo serve per la retrocompatibilita', versioni non aggiornate inviano
            // pacchetti da 14 bytes anziche' da 95 bytes
            // Il valore da verificare e' 19 perche' ai 14 bytes dati bisogna aggiungere il comando(+1)
            // e i 4 bytes del checksum
            if( msg.length == 19 ) {
                fullStartGameData = false;
                System.arraycopy(msg, 1, startGameData, 0, 14);
            } else {
                fullStartGameData = true;
                System.arraycopy(msg, 1, startGameData, 0, 95);
            }


            debugLog( "Ricezione dati inizio partita" );

        } else if( command == MPCOM.MPCOMCMD_END_TURN_SCORE) {

            // RICEZIONE VALORE PUNTI DELL'AVVERSARIO PER AGGIORNAMENTO CLASSIFICA

            byte[]  temp = copyOfRange(msg, 1, msg.length - 4);
            // la variabile contenente il numero random dell'avversario e' globale
            int rivalScore = mpcom.convertByteArrayToInteger( temp );

            currentPlayerScore += rivalScore;
            // invia il punteggio per la classifica
            Games.Leaderboards.submitScore( getApiClient(), LEADERBOARD_ID, currentPlayerScore );
            // visualizza la dialog "HAI VINTO"
            mpGraphics.showDialog(MPGraphics.MPWAITDLG_WINNER, mRivalName, rivalScore, false);

            debugLog("Ricezione punti fine partita " + Integer.toString(rivalScore));

        } else if ( command == MPCOM.MPCOMCMD_WANT_REPLAY) {

            // RICEZIONE RICHIESTA DI REPLAY

            // - se era gia' stata inviata una richiesta di replay cancello la dialog di attesa
            // - se non era stata inviata una richiesta visualizzo la dialog di attesa conferma
            if( game_status == GS_WAITING_REPLAY_CONFIRM ) {
                // - cancella la dialog box di attesa conferma
                mpGraphics.hideDialog();
                // - inizia una nuova partita
                start_sync_devices();
            } else if( ( game_status == GS_PLAYER_WINS ) || ( game_status == GS_RIVAL_WINS_WAIT_ACTION ) ) {
                // visualiza la dialog box per accettare la rivincita
                mpGraphics.showDialog(MPGraphics.MPWAITDLG_ACCEPT_REPLAY_REQUEST, mRivalName, 0, false );
                soundPool.play( replaysound, 1.0f, 1.0f, 0, 0, 1.0f );
            } else if( game_status == GS_RIVAL_WINS ) {
                // memorizza la richiesta di replay, verra' controllata non appena il gioco va in GS_RIVAL_WINS_WAIT_ACTION
                replayRequestReceived = true;
            }

            debugLog( "Ricezione richiesta REPLAY" );

        } else if ( command == MPCOM.MPCOMCMD_USER_MESSAGE) {

            // RICEZIONE MESSAGGIO DA PARTE DELL'AVVERSARIO

            // se il pacchetto ricevuto e' di 5 bytes non contiene alcun nome!
            String message = null;
            if( msg.length > 5 ) {

                // estrazione byte per deserializzazione oggetto String
                byte[]  temp = copyOfRange(msg, 1, msg.length - 4);
                try {
                    message = (String)mpcom.toObject( temp );
                } catch ( ClassNotFoundException e ) {
                    show_info(e.getMessage(), Toast.LENGTH_SHORT);
                } catch ( IOException e ) {
                    show_info(e.getMessage(), Toast.LENGTH_SHORT);
                }

                if( ( message != null ) && ( chat_message_enable ) ) {
                    soundPool.play( replaysound, 1.0f, 1.0f, 0, 0, 1.0f );
                    //show_info( "Messaggio: \n" + message, Toast.LENGTH_LONG );
                    show_chat_message( message );
                    debugLog("Messaggio dall'avversario " + message);
                }
            }

        } else if( command == MPCOM.MPCOMCMD_END_TURN_DATA) {

            // RICEZIONE DATI FINE TURNO
            debugLog( "Ricezione dati fine turno " + Integer.toString( msg.length ) + " bytes" );

            // il gioco e' in attesa dei dati di fine turno dell'avversario
            if( game_status == GS_RIVAL_TURN ) {

                // estrazione dati dal messaggio escludendo il comando e i 4 byte Adler
                byte[]  temp = copyOfRange(msg, 1, msg.length - 4);

                // n. di carte dell'avversario
                int pos = 0;
                last_rival_total_cards  = temp[ pos ];
                pos += 1;
                // n. di carte del tallone
                int total_pool_cards    = temp[ pos ];
                pos += 1;
                // carte del tallone
                deck.pool_size = total_pool_cards;
                for( int i = 0; i < total_pool_cards; i++ ) {
                    deck.pool_cards[ i ] = temp[ pos ];
                    pos += 1;
                }
                // n. di carte del pozzo
                int total_cull_cards    = temp[ pos ];
                pos += 1;
                deck.cull_size = total_cull_cards;
                // carte del pozzo
                for( int i = 0; i < total_cull_cards; i++ ) {
                    deck.cull_cards[ i ] = temp[ pos ];
                    pos += 1;
                }
                // subarray contiene i dati serializzati dell'array di oggetti di tipo GroupClass
                byte[] subarray = copyOfRange(temp, pos, temp.length);
                try {
                    // riconversione dei dati serializzati in oggetto di tipo array di oggetti GroupClass
                    ArrayList<GroupClass> newgroups = (ArrayList<GroupClass>)mpcom.toObject( subarray );
                    // se non ci sono exception ricrea l'array dei gruppi sul tavolo
                    groups.clear();
                    for (GroupClass newgroup1 : newgroups) {
                        GroupClass newgroup = new GroupClass(
                                newgroup1.owner,
                                newgroup1.total_cards,
                                newgroup1.cards,
                                newgroup1.type,
                                newgroup1.joker_value,
                                newgroup1.score);
                        groups.add(newgroup);
                    }
                    // aggiorna a video il tavolo
                    mpGraphics.draw_table( groups, false );

                } catch ( ClassNotFoundException e ) {
                    show_info( e.getMessage(), Toast.LENGTH_SHORT );
                } catch ( IOException e ) {
                    show_info( e.getMessage(), Toast.LENGTH_SHORT );
                }

                // debug conteggio carte
                //checkMPCardsCount();

                // aggiorna il numero totale di carte dell'avversario (senza lampadina)
                mpGraphics.draw_total_rival_cards( last_rival_total_cards, false );
                // aggiorna il pozzo con l'ultima carta scartata dall'avversario
                mpGraphics.draw_last_discarded_card(deck);


                // se l'avversario ha vinto (n.carte in mano = 0)
                if( last_rival_total_cards == 0 ) {

                    // invia all'avversario il punteggio per l'aggiornamento classifica
                    int losing_score = player.calculate_losing_score();
                    sendEndOfTurnScore( losing_score );

                    // stato di visualizzazione vincita dell'avversario
                    set_game_status( GS_RIVAL_WINS );
                    // suono di perdita
                    soundPool.play( losesound, 1.0f, 1.0f, 0, 0, 1.0f );

                    // la visualizzazione della dialog box viene positicipata per permettere
                    // di capire come ha fatto a vincere l'avversario

                    // visualizzazione perdita
                    TimedActionHandler.postDelayed( new Runnable() {
                        @Override
                        public void run() {
                            showLosingGameDialog();
                        }
                    }, 3000 );
                    // la finestra di notifica della perdita viene visualizzata dal TimedActionThread
                } else {

                    // se l'avversario non ha vinto attende il turno
                    // se l'avversario non ha vinto attende il turno
                    player.start_turn();
                    set_game_status( GS_PLAYER_TURN );
                    // memorizza il n. di carte a inizio turno per verifica sblocco achvm. "Chiusura in mano"
                    start_turn_total_cards = player.cards.size();
                    // audio "turno del giocatore"
                    soundPool.play( turnsound, 1.0f, 1.0f, 0, 0, 1.0f );
                    // segnalazione a video turno del giocatore
                    show_info( "E' il tuo turno", Toast.LENGTH_SHORT );
                    // aggiorna il numero di mani fatte dal giocatore (x sblocco obiettivo)
                    consecutive_deals += 1;
                }
            }
        }
    }

    // handlers

    Runnable continuousActions = new Runnable() {
        @Override
        public void run() {
            // debug
            //Log.d( debugTag, "continuousActions" );

            // recupera informazioni sui giocatori online solo quando e' necessario ovvero
            // se non c'e' alcuna  in corso e il menu principale e' visualizzato
            if( (allGraphicsConnected) &&
                (onlinePlayersRequest) &&
                ( !isPlaying() ) ) {
                // debug
                //Log.d( debugTag, "getOnlinePlayers.execute" );
                new GetOnlinePlayers( mContext ).execute();
            }
            // debug
            //Log.d( debugTag, "scheduling continuousActions within 15 seconds..." );
            TimedActionHandler.postDelayed( continuousActions, CONTINUOUS_ACTIONS_DELAY );
        }
    };


}

