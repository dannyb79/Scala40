package com.plmtmobile.scala40se;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by daniele on 20/10/13.
 */
public class Graphics {
    // definizioni pubbliche
    public static final int    EMPTY_TABLE_CARD     = -1;


    // definizioni private
    private static final int    MAX_TABLE_ROWS      = 10;
    private static final int    MAX_ROW_CARDS       = 14;
    private static final int    HALF_ROW_CARDS      = 7;

    public Activity             activity;
    public Context              mContext;
    private LinearLayout        playerCardsLayout;
    private LinearLayout        deviceCardsLayout;
    private LinearLayout        EndGameLayout;
    private LinearLayout        scrollViewLayout;
    private LinearLayout        TableRowLayouts[]   = new LinearLayout[ MAX_TABLE_ROWS ];
    private ImageView           TableImageViews[][] = new ImageView[ MAX_TABLE_ROWS ][ MAX_ROW_CARDS ];
    private int []              cardsimages;
    private int []              bluecardsimages;
    private ImageView[]         iv_playercards;
    private ImageView[]         iv_devicecards;
    private ImageView           iv_lastdiscardedcard;
    private TextView            tv_devicecards;
    private AnimationDrawable   gearsAnimation;
    private AnimationDrawable   danceAnimation;
    private long[]              longClickTimer = new long[ PlayerClass.MAX_PLAYER_CARDS ];
    private ViewGroup           droplayout;
    private ImageView           iv_EndGameAnimation;
    private ImageView           iv_EndGameText;
    private int                 selectortype;

    /*
        istanza oggetto grafica
    */
    Graphics ( Activity _activity, Context context, int selectortype, int backgroundimage ) {
        mContext            = context;
        this.activity       = _activity;
        this.selectortype   = selectortype;

        // array contenente gli indici (integer) delle immagini delle carte
        cardsimages = new int[ 54 ];
        cardsimages[ 0 ] = R.drawable.hearts_1;
        cardsimages[ 1 ] = R.drawable.hearts_2;
        cardsimages[ 2 ] = R.drawable.hearts_3;
        cardsimages[ 3 ] = R.drawable.hearts_4;
        cardsimages[ 4 ] = R.drawable.hearts_5;
        cardsimages[ 5 ] = R.drawable.hearts_6;
        cardsimages[ 6 ] = R.drawable.hearts_7;
        cardsimages[ 7 ] = R.drawable.hearts_8;
        cardsimages[ 8 ] = R.drawable.hearts_9;
        cardsimages[ 9 ] = R.drawable.hearts_10;
        cardsimages[ 10 ] = R.drawable.hearts_j;
        cardsimages[ 11 ] = R.drawable.hearts_q;
        cardsimages[ 12 ] = R.drawable.hearts_k;
        cardsimages[ 13 ] = R.drawable.diamonds_1;
        cardsimages[ 14 ] = R.drawable.diamonds_2;
        cardsimages[ 15 ] = R.drawable.diamonds_3;
        cardsimages[ 16 ] = R.drawable.diamonds_4;
        cardsimages[ 17 ] = R.drawable.diamonds_5;
        cardsimages[ 18 ] = R.drawable.diamonds_6;
        cardsimages[ 19 ] = R.drawable.diamonds_7;
        cardsimages[ 20 ] = R.drawable.diamonds_8;
        cardsimages[ 21 ] = R.drawable.diamonds_9;
        cardsimages[ 22 ] = R.drawable.diamonds_10;
        cardsimages[ 23 ] = R.drawable.diamonds_j;
        cardsimages[ 24 ] = R.drawable.diamonds_q;
        cardsimages[ 25 ] = R.drawable.diamonds_k;
        cardsimages[ 26 ] = R.drawable.spades_1;
        cardsimages[ 27 ] = R.drawable.spades_2;
        cardsimages[ 28 ] = R.drawable.spades_3;
        cardsimages[ 29 ] = R.drawable.spades_4;
        cardsimages[ 30 ] = R.drawable.spades_5;
        cardsimages[ 31 ] = R.drawable.spades_6;
        cardsimages[ 32 ] = R.drawable.spades_7;
        cardsimages[ 33 ] = R.drawable.spades_8;
        cardsimages[ 34 ] = R.drawable.spades_9;
        cardsimages[ 35 ] = R.drawable.spades_10;
        cardsimages[ 36 ] = R.drawable.spades_j;
        cardsimages[ 37 ] = R.drawable.spades_q;
        cardsimages[ 38 ] = R.drawable.spades_k;
        cardsimages[ 39 ] = R.drawable.clubs_1;
        cardsimages[ 40 ] = R.drawable.clubs_2;
        cardsimages[ 41 ] = R.drawable.clubs_3;
        cardsimages[ 42 ] = R.drawable.clubs_4;
        cardsimages[ 43 ] = R.drawable.clubs_5;
        cardsimages[ 44 ] = R.drawable.clubs_6;
        cardsimages[ 45 ] = R.drawable.clubs_7;
        cardsimages[ 46 ] = R.drawable.clubs_8;
        cardsimages[ 47 ] = R.drawable.clubs_9;
        cardsimages[ 48 ] = R.drawable.clubs_10;
        cardsimages[ 49 ] = R.drawable.clubs_j;
        cardsimages[ 50 ] = R.drawable.clubs_q;
        cardsimages[ 51 ] = R.drawable.clubs_k;
        cardsimages[ 52 ] = R.drawable.black_joker;
        cardsimages[ 53 ] = R.drawable.red_joker;

        // array contenente gli indici (integer) delle immagini delle carte
        bluecardsimages = new int[ 54 ];
        bluecardsimages[ 0 ] = R.drawable.bluhearts_1;
        bluecardsimages[ 1 ] = R.drawable.bluhearts_2;
        bluecardsimages[ 2 ] = R.drawable.bluhearts_3;
        bluecardsimages[ 3 ] = R.drawable.bluhearts_4;
        bluecardsimages[ 4 ] = R.drawable.bluhearts_5;
        bluecardsimages[ 5 ] = R.drawable.bluhearts_6;
        bluecardsimages[ 6 ] = R.drawable.bluhearts_7;
        bluecardsimages[ 7 ] = R.drawable.bluhearts_8;
        bluecardsimages[ 8 ] = R.drawable.bluhearts_9;
        bluecardsimages[ 9 ] = R.drawable.bluhearts_10;
        bluecardsimages[ 10 ] = R.drawable.bluhearts_j;
        bluecardsimages[ 11 ] = R.drawable.bluhearts_q;
        bluecardsimages[ 12 ] = R.drawable.bluhearts_k;
        bluecardsimages[ 13 ] = R.drawable.bludiamonds_1;
        bluecardsimages[ 14 ] = R.drawable.bludiamonds_2;
        bluecardsimages[ 15 ] = R.drawable.bludiamonds_3;
        bluecardsimages[ 16 ] = R.drawable.bludiamonds_4;
        bluecardsimages[ 17 ] = R.drawable.bludiamonds_5;
        bluecardsimages[ 18 ] = R.drawable.bludiamonds_6;
        bluecardsimages[ 19 ] = R.drawable.bludiamonds_7;
        bluecardsimages[ 20 ] = R.drawable.bludiamonds_8;
        bluecardsimages[ 21 ] = R.drawable.bludiamonds_9;
        bluecardsimages[ 22 ] = R.drawable.bludiamonds_10;
        bluecardsimages[ 23 ] = R.drawable.bludiamonds_j;
        bluecardsimages[ 24 ] = R.drawable.bludiamonds_q;
        bluecardsimages[ 25 ] = R.drawable.bludiamonds_k;
        bluecardsimages[ 26 ] = R.drawable.bluspades_1;
        bluecardsimages[ 27 ] = R.drawable.bluspades_2;
        bluecardsimages[ 28 ] = R.drawable.bluspades_3;
        bluecardsimages[ 29 ] = R.drawable.bluspades_4;
        bluecardsimages[ 30 ] = R.drawable.bluspades_5;
        bluecardsimages[ 31 ] = R.drawable.bluspades_6;
        bluecardsimages[ 32 ] = R.drawable.bluspades_7;
        bluecardsimages[ 33 ] = R.drawable.bluspades_8;
        bluecardsimages[ 34 ] = R.drawable.bluspades_9;
        bluecardsimages[ 35 ] = R.drawable.bluspades_10;
        bluecardsimages[ 36 ] = R.drawable.bluspades_j;
        bluecardsimages[ 37 ] = R.drawable.bluspades_q;
        bluecardsimages[ 38 ] = R.drawable.bluspades_k;
        bluecardsimages[ 39 ] = R.drawable.bluclubs_1;
        bluecardsimages[ 40 ] = R.drawable.bluclubs_2;
        bluecardsimages[ 41 ] = R.drawable.bluclubs_3;
        bluecardsimages[ 42 ] = R.drawable.bluclubs_4;
        bluecardsimages[ 43 ] = R.drawable.bluclubs_5;
        bluecardsimages[ 44 ] = R.drawable.bluclubs_6;
        bluecardsimages[ 45 ] = R.drawable.bluclubs_7;
        bluecardsimages[ 46 ] = R.drawable.bluclubs_8;
        bluecardsimages[ 47 ] = R.drawable.bluclubs_9;
        bluecardsimages[ 48 ] = R.drawable.bluclubs_10;
        bluecardsimages[ 49 ] = R.drawable.bluclubs_j;
        bluecardsimages[ 50 ] = R.drawable.bluclubs_q;
        bluecardsimages[ 51 ] = R.drawable.bluclubs_k;
        bluecardsimages[ 52 ] = R.drawable.blublack_joker;
        bluecardsimages[ 53 ] = R.drawable.blured_joker;



        // imageview per le carte del giocatore
        iv_playercards = new ImageView[ PlayerClass.MAX_PLAYER_CARDS ];
        iv_playercards[ 0 ] = (ImageView)this.activity.findViewById(R.id.iv_pc1);
        iv_playercards[ 1 ] = (ImageView)this.activity.findViewById(R.id.iv_pc2);
        iv_playercards[ 2 ] = (ImageView)this.activity.findViewById(R.id.iv_pc3);
        iv_playercards[ 3 ] = (ImageView)this.activity.findViewById(R.id.iv_pc4);
        iv_playercards[ 4 ] = (ImageView)this.activity.findViewById(R.id.iv_pc5);
        iv_playercards[ 5 ] = (ImageView)this.activity.findViewById(R.id.iv_pc6);
        iv_playercards[ 6 ] = (ImageView)this.activity.findViewById(R.id.iv_pc7);
        iv_playercards[ 7 ] = (ImageView)this.activity.findViewById(R.id.iv_pc8);
        iv_playercards[ 8 ] = (ImageView)this.activity.findViewById(R.id.iv_pc9);
        iv_playercards[ 9 ] = (ImageView)this.activity.findViewById(R.id.iv_pc10);
        iv_playercards[ 10 ] = (ImageView)this.activity.findViewById(R.id.iv_pc11);
        iv_playercards[ 11 ] = (ImageView)this.activity.findViewById(R.id.iv_pc12);
        iv_playercards[ 12 ] = (ImageView)this.activity.findViewById(R.id.iv_pc13);
        iv_playercards[ 13 ] = (ImageView)this.activity.findViewById(R.id.iv_pc14);

        // imposta il listener sulle carte del giocatore per il drag 'n' drop
        for( int i = 0; i < PlayerClass.MAX_PLAYER_CARDS; i++ ) {
            iv_playercards[ i ].setTag(i);
            iv_playercards[ i ].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return onTouchPlayerCardEvent(v, event);
                }
            });
        }
        // imposta il layout contenente le view di destinazione del drag and drop
        droplayout          = (LinearLayout) this.activity.findViewById(R.id.PlayerCardsLayout);
        deviceCardsLayout   = (LinearLayout) this.activity.findViewById(R.id.DeviceCardsLayout);

        // carte dell'avversario
        iv_devicecards = new ImageView[ PlayerClass.MAX_PLAYER_CARDS ];
        iv_devicecards[ 0 ] = (ImageView)this.activity.findViewById(R.id.iv_dc1);
        iv_devicecards[ 1 ] = (ImageView)this.activity.findViewById(R.id.iv_dc2);
        iv_devicecards[ 2 ] = (ImageView)this.activity.findViewById(R.id.iv_dc3);
        iv_devicecards[ 3 ] = (ImageView)this.activity.findViewById(R.id.iv_dc4);
        iv_devicecards[ 4 ] = (ImageView)this.activity.findViewById(R.id.iv_dc5);
        iv_devicecards[ 5 ] = (ImageView)this.activity.findViewById(R.id.iv_dc6);
        iv_devicecards[ 6 ] = (ImageView)this.activity.findViewById(R.id.iv_dc7);
        iv_devicecards[ 7 ] = (ImageView)this.activity.findViewById(R.id.iv_dc8);
        iv_devicecards[ 8 ] = (ImageView)this.activity.findViewById(R.id.iv_dc9);
        iv_devicecards[ 9 ] = (ImageView)this.activity.findViewById(R.id.iv_dc10);
        iv_devicecards[ 10 ] = (ImageView)this.activity.findViewById(R.id.iv_dc11);
        iv_devicecards[ 11 ] = (ImageView)this.activity.findViewById(R.id.iv_dc12);
        iv_devicecards[ 12 ] = (ImageView)this.activity.findViewById(R.id.iv_dc13);
        iv_devicecards[ 13 ] = (ImageView)this.activity.findViewById(R.id.iv_dc14);

        // imageview del pozzo
        iv_lastdiscardedcard    = (ImageView)this.activity.findViewById( R.id.iv_lastdiscardedcard );
        // textview che indica il numero di carte dell'avversario
        tv_devicecards          = (TextView)this.activity.findViewById( R.id.tv_devicecards );
        tv_devicecards.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onTouchDeviceCardsEvent(v,event);
            }
        });

        // layout di fine gioco
        EndGameLayout           = (LinearLayout) this.activity.findViewById(R.id.EndGameLayout);
        // imageview per animazione fine partita
        iv_EndGameAnimation     = (ImageView)this.activity.findViewById( R.id.iv_EndGameAnimation );
        iv_EndGameText          = (ImageView)this.activity.findViewById( R.id.iv_EndGameText );

        // carica immagine di sfondo selezionata dal giocatore
        switch( backgroundimage ) {
            case 1:
                ((RelativeLayout)this.activity.findViewById( R.id.MainBackground )).setBackgroundResource( R.drawable.background1 );
                droplayout.setBackgroundResource( 0 );
            break;
            case 2:
                ((RelativeLayout)this.activity.findViewById( R.id.MainBackground )).setBackgroundResource( R.drawable.background2 );
                droplayout.setBackgroundResource( 0 );
                break;
            case 3:
                ((RelativeLayout)this.activity.findViewById( R.id.MainBackground )).setBackgroundResource( R.drawable.background3 );
                droplayout.setBackgroundResource( 0 );
                break;
            case 4:
                ((RelativeLayout)this.activity.findViewById( R.id.MainBackground )).setBackgroundResource( R.drawable.background4 );
                droplayout.setBackgroundResource( 0 );
                break;
            default:
                // lascia tutto com'e'
                break;
        }

        // annulla il suono di default per alcune ImageView
        ((ImageView)this.activity.findViewById( R.id.iv_unplayedcards )).setSoundEffectsEnabled( false );
        ((ImageView)this.activity.findViewById( R.id.iv_lastdiscardedcard )).setSoundEffectsEnabled( false );
        ((ImageView)this.activity.findViewById( R.id.btn_sortstraights )).setSoundEffectsEnabled( false );
        ((ImageView)this.activity.findViewById( R.id.btn_sortvalues )).setSoundEffectsEnabled( false );

    }

    private boolean onTouchDeviceCardsEvent( View v, MotionEvent event ) {
        switch( event.getActionMasked() )
        {
            // inizio pressione sul touchscreen
            case MotionEvent.ACTION_DOWN:
                // stampa e visualizza le carte dell'avversario
                if( ((GameActivity)mContext).easy_game ) {
                    if( ((GameActivity)mContext).game_status == GameActivity.GS_PLAYER_TURN ) {
                        for( int i = 0; i < PlayerClass.MAX_PLAYER_CARDS; i++ ) {
                            if( i < ((GameActivity)mContext).device.cards.size() ) {
                                iv_devicecards[ i ].setImageResource( cardsimages[ ((GameActivity)mContext).device.cards.get( i ).value ] );
                                iv_devicecards[ i ].setVisibility( View.VISIBLE );
                            } else {
                                iv_devicecards[ i ].setImageResource( 0 );
                                iv_devicecards[ i ].setVisibility( View.INVISIBLE );
                            }
                        }
                        deviceCardsLayout.setVisibility( View.VISIBLE );
                    }
                }
            break;

            // pressione continua sul touchscreen
            case MotionEvent.ACTION_MOVE:
                break;

            // rilascio touchscreen
            case MotionEvent.ACTION_UP:
                // nasconde le carte dell'avversario
                deviceCardsLayout.setVisibility( View.INVISIBLE );
                break;
        }

        return true;
    }

    /*
    la funzione onTouchCustomEvent deve essere invocata dal listener degli eventi del touch;
    ogni view interessata alle operazioni di drag'n'drop deve istanziare un listener e invocare
    la funzione onTouchCustomEvent
    */
    private boolean onTouchPlayerCardEvent( View v, MotionEvent event ) {
        float rawx = event.getRawX();
        float rawy = event.getRawY();

        switch( event.getActionMasked() )
        {
            // inizio pressione sul touchscreen
            case MotionEvent.ACTION_DOWN:
                // imposta il timer per discriminare il tocco rapido da quello prolungato
                longClickTimer[ Integer.parseInt( v.getTag().toString() ) ] = System.currentTimeMillis();

             break;

            // pressione continua sul touchscreen
            case MotionEvent.ACTION_MOVE:

            break;

            // rilascio touchscreen
            case MotionEvent.ACTION_UP:

                if( ( System.currentTimeMillis() - longClickTimer[ Integer.parseInt( v.getTag().toString() ) ] ) < 500 ) {
                    ((GameActivity)mContext).onPlayerCardClick( v );
                }
                onDropDetection(v, rawx, rawy);

            break;
        }

        return true;
    }

    /*
    la funzione onDropDetection ricerca nel viewgroup contenente le views di destinazione
    la view corrispondente alle coordinate x e y; se viene trovata una view alle coordinate indicate
    da x e y viene invocata la funzione doDropAction inviando come parametri la view di partenza
    e la view di destinazione, La funzione doDropAction dovrebbe contenere le operazioni da
    intraprendere secondo la logica di gioco.

    ATTENZIONE!!! il viewgroup (variabile droplayout) deve essere precedentemente impostata
    con il layout contenente le possibili destinazioni dell'operazione di drag'n'drop
*/
    private void onDropDetection( View startview, float x, float y )
    {
        View    childview;
        int[]   viewcoord = new int[2];
        // analisi di tutte le view child del viewgroup
         for( int i = 0; i < droplayout.getChildCount(); i++ ) {
            // analisi dell'n-esima view child
            childview = droplayout.getChildAt(i);
            // recupero coordinate assolute della view child
            childview.getLocationOnScreen( viewcoord );
            // se le coordinate si trovano all'interno dell'area occupata dalla view...
            if( ( x >= viewcoord[0] ) && ( x <= ( viewcoord[0] + childview.getWidth() ) ) && ( y >= viewcoord[1] ) && ( y <= ( viewcoord[1] + childview.getHeight() ) ) ) {
                // ...viene invocata la funzione doDropAction indicando la view di partenza e quella di destinazione
                ((GameActivity)mContext).onDropCard( startview, childview );
                break;
            }
        }
    }

    /*
        avvia animazione durante il turno dell'avversario
    */
    public void start_gears_animation() {
        tv_devicecards.setTextColor( Color.TRANSPARENT );
        tv_devicecards.setBackgroundResource( R.drawable.gearsanim );
        gearsAnimation = (AnimationDrawable) tv_devicecards.getBackground();
        gearsAnimation.start();
    }

    /*
        ferma animazione durante il turno dell'avversario
    */
    public void stop_gears_animation() {
        if( gearsAnimation != null ) {
            gearsAnimation.stop();
        }
        tv_devicecards.setBackgroundResource( R.drawable.androidrobot_shadow );
        tv_devicecards.setTextColor(Color.BLACK);
    }


    /*
        aggiornamento numero di carte del dispositivo
    */
    public void draw_total_device_cards( int total_cards, boolean bt_enabled ) {
        if( bt_enabled ) {
            tv_devicecards.setText( "" );
            switch ( total_cards ) {
                case 0: tv_devicecards.setBackgroundResource( R.drawable.humanrival_0 );        break;
                case 1: tv_devicecards.setBackgroundResource( R.drawable.humanrival_1 );        break;
                case 2: tv_devicecards.setBackgroundResource( R.drawable.humanrival_2 );        break;
                case 3: tv_devicecards.setBackgroundResource( R.drawable.humanrival_3 );        break;
                case 4: tv_devicecards.setBackgroundResource( R.drawable.humanrival_4 );        break;
                case 5: tv_devicecards.setBackgroundResource( R.drawable.humanrival_5 );        break;
                case 6: tv_devicecards.setBackgroundResource( R.drawable.humanrival_6 );        break;
                case 7: tv_devicecards.setBackgroundResource( R.drawable.humanrival_7 );        break;
                case 8: tv_devicecards.setBackgroundResource( R.drawable.humanrival_8 );        break;
                case 9: tv_devicecards.setBackgroundResource( R.drawable.humanrival_9 );        break;
                case 10: tv_devicecards.setBackgroundResource( R.drawable.humanrival_10 );      break;
                case 11: tv_devicecards.setBackgroundResource( R.drawable.humanrival_11 );      break;
                case 12: tv_devicecards.setBackgroundResource( R.drawable.humanrival_12 );      break;
                case 13: tv_devicecards.setBackgroundResource( R.drawable.humanrival_13 );      break;
                default: tv_devicecards.setBackgroundResource( R.drawable.humanrival_unset );   break;
            }
        } else {
            tv_devicecards.setText( "" );
            switch ( total_cards ) {
                case 0: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_0 );        break;
                case 1: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_1 );        break;
                case 2: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_2 );        break;
                case 3: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_3 );        break;
                case 4: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_4 );        break;
                case 5: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_5 );        break;
                case 6: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_6 );        break;
                case 7: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_7 );        break;
                case 8: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_8 );        break;
                case 9: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_9 );        break;
                case 10: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_10 );      break;
                case 11: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_11 );      break;
                case 12: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_12 );      break;
                case 13: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_13 );      break;
                default: tv_devicecards.setBackgroundResource( R.drawable.androidrobot_unset );   break;
            }
        }
    }

    /*
        impostazioni grafiche postume alla creazione del layout
    */
    public void post_creation() {
        int i, j;

        // calcola l'altezza di una carta nel layout carte giocatore
        playerCardsLayout   = (LinearLayout) this.activity.findViewById(R.id.PlayerCardsLayout);
        int cardsheight     = playerCardsLayout.getHeight();
        // puntatore al vertical layout scorrevole
        scrollViewLayout    = (LinearLayout) this.activity.findViewById(R.id.scrollViewLayout);
        // calcola la larghezza di una carta nel layout carte giocatore
        ImageView iv_pc1    = (ImageView) this.activity.findViewById(R.id.iv_pc1);
        int cardswidth      = iv_pc1.getWidth();

        for( i = 0; i < MAX_TABLE_ROWS; i++ ) {

            // crea un layout orizzontale in cui inserire le carte del tavolo
            TableRowLayouts[ i ] = new LinearLayout( mContext );
            TableRowLayouts[ i ].setOrientation( LinearLayout.HORIZONTAL );
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cardsheight );
            TableRowLayouts[ i ].setLayoutParams( lparams );
            // per ogni riga crea un set di 14 carte poiche' la sequenza massima componibile e' A-2-3-4-5-6-7-8-9-10-J-Q-K-Jolly
            for( j = 0; j < PlayerClass.MAX_PLAYER_CARDS; j++ ) {

                TableImageViews[ i ][ j ] = new ImageView( mContext );
                LinearLayout.LayoutParams iparams = new LinearLayout.LayoutParams( cardswidth, ViewGroup.LayoutParams.MATCH_PARENT );
                TableImageViews[ i ][ j ].setLayoutParams( iparams );
                TableImageViews[ i ][ j ].setTag( EMPTY_TABLE_CARD );
                TableImageViews[ i ][ j ].setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        onTClick( v );
                    }
                });
                TableRowLayouts[ i ].addView( TableImageViews[ i ][ j ] );
            }
            scrollViewLayout.addView( TableRowLayouts[ i ] );
        }
    }

    public void onTClick( View v ) {
        ((GameActivity)mContext).onTableClick( v );
    }

    /*
        ristampa tutte le carte del giocatore
    */
    public void draw_player_cards( PlayerClass player ) {
        for( int i = 0; i < PlayerClass.MAX_PLAYER_CARDS; i++ ) {
            if( i < player.cards.size() ) {
                //iv_playercards[ i ].setImageResource( cardsimages[ player.cards.get( i ).value ] );
                if( player.cards.get( i ).selected ) {
                    // visualizza il selettore
                    if( selectortype == 0 ) {
                        // visaulizza la carta (sfondo bianco)
                        iv_playercards[ i ].setImageResource( cardsimages[ player.cards.get( i ).value ] );
                        // visualizza le freccie sopra e sotto la carta
                        iv_playercards[ i ].setBackgroundResource( R.drawable.selector );
                    } else {
                        // visaulizza la carta (sfondo blue)
                        iv_playercards[ i ].setImageResource( bluecardsimages[ player.cards.get( i ).value ] );
                    }
                } else {
                    // se la carta non e' selezionata
                    if( selectortype == 0 ) {
                        // visaulizza la carta (sfondo bianco)
                        iv_playercards[ i ].setImageResource( cardsimages[ player.cards.get( i ).value ] );
                        // nasconde le frecce
                        iv_playercards[ i ].setBackgroundColor( Color.TRANSPARENT );
                    } else {
                        // visaulizza la carta (sfondo bianco)
                        iv_playercards[ i ].setImageResource( cardsimages[ player.cards.get( i ).value ] );
                    }
                }
                iv_playercards[ i ].setVisibility( View.VISIBLE );
            } else {
                iv_playercards[ i ].setBackgroundColor( Color.TRANSPARENT );
                iv_playercards[ i ].setVisibility( View.INVISIBLE );
            }
        }
    }

    public void draw_selected_card( View v, int position, int cardvalue, boolean status ) {
        if( status ) {
            if( selectortype == 0 ) {
                // visualizza il selettore
                v.setBackgroundResource(R.drawable.selector);
            } else {
                // visualizza la carta blu
                // visaulizza la carta (sfondo blue)
                iv_playercards[ position ].setImageResource( bluecardsimages[ cardvalue ] );
            }
        } else {
            if( selectortype == 0 ) {
                // cancella il selettore
                v.setBackgroundColor(Color.TRANSPARENT);
            } else {
                iv_playercards[ position ].setImageResource( cardsimages[ cardvalue ] );
            }
        }
    }

    /*
    ristampa tutte le carte del giocatore con la carta ?
    */
    public void draw_unknown_player_cards( PlayerClass player ) {
        for( int i = 0; i < PlayerClass.MAX_PLAYER_CARDS; i++ ) {
            if( i < player.cards.size() ) {
                iv_playercards[ i ].setImageResource( R.drawable.unsetcard );
                iv_playercards[ i ].setVisibility( View.VISIBLE );
            } else {
                iv_playercards[ i ].setBackgroundColor( Color.TRANSPARENT );
                iv_playercards[ i ].setVisibility( View.INVISIBLE );
            }
        }
    }

    /*
        stampa la carta del pozzo con il ?
    */
    public void draw_unknown_last_discarded_card() {
        iv_lastdiscardedcard.setImageResource( R.drawable.unsetcard );
    }

    /*
        ristampa la carta del pozzo
    */
    public void draw_last_discarded_card( DeckClass deck ) {

        // in teoria il numero di carte nel pozzo non dovrebbe mai essere 0 ma e' sempre meglio controllare
        if( deck.cull_size > 0 ) {
            int card = deck.cull_cards [ deck.cull_size - 1 ];
            iv_lastdiscardedcard.setImageResource( cardsimages[ card ] );
            //iv_lastdiscardedcard.setVisibility( View.VISIBLE );
        } else {
            iv_lastdiscardedcard.setImageResource( R.drawable.emptycard );
            //iv_lastdiscardedcard.setVisibility( View.INVISIBLE );
        }
    }

    /*
        ristampa il tavolo
    */
    public void draw_table( ArrayList<GroupClass> groups, boolean clean ) {
        int     table_data[][]      = new int[ MAX_TABLE_ROWS ][ 3 ];
        int     table_rows          = 0;
        boolean pos_found           = false;
        int     group_id            = 0;
        int     i, j, k;

        for( i = 0; i < MAX_TABLE_ROWS; i++ )
            for( j = 0; j < 3; j++ )
                table_data[ i ][ j ] = -1;

        for( i = 0; i < groups.size(); i++ ) {
            if( groups.get( i ).type == GroupClass.GROUP_TYPE_STRAIGHT ) {
                table_data[ table_rows ][ 0 ] = GroupClass.GROUP_TYPE_STRAIGHT;
                table_data[ table_rows ][ 1 ] = i;
                table_rows += 1;
            } else if( groups.get( i ).type == GroupClass.GROUP_TYPE_COMBINATION ) {
                pos_found = false;
                for( j = 0; j < table_rows; j++ ) {
                    if( ( table_data[ j ][ 0 ] == GroupClass.GROUP_TYPE_COMBINATION ) && ( table_data[ j ][ 2 ] == -1 ) ) {
                        table_data[ j ][ 2 ] = i;
                        pos_found = true;
                        break;
                    }
                }
                if( pos_found == false ) {
                    table_data[ table_rows ][ 0 ] = GroupClass.GROUP_TYPE_COMBINATION;
                    table_data[ table_rows ][ 1 ] = i;
                    table_rows += 1;
                }
            }
        }

        if( clean ) {
            for( i = 0; i < MAX_TABLE_ROWS; i++ ) {
                for( j = 0; j < MAX_ROW_CARDS; j++ ) {
                    TableImageViews[ i ][ j ].setImageResource( 0 );
                    TableImageViews[ i ][ j ].setTag( EMPTY_TABLE_CARD );
                }
            }
        }


        for( i = 0; i < MAX_TABLE_ROWS; i++ ) {
            if( table_data[ i ][ 0 ] == GroupClass.GROUP_TYPE_STRAIGHT ) {
                group_id = table_data[ i ][ 1 ];
                for( j = 0; j < MAX_ROW_CARDS; j++ ) {
                    if( j < groups.get( group_id ).total_cards ) {
                        TableImageViews[ i ][ j ].setImageResource( cardsimages[ groups.get( group_id ).cards[ j ] ] );
                        TableImageViews[ i ][ j ].setTag( group_id );
                    } else {
                        if( clean ) {
                            TableImageViews[ i ][ j ].setImageResource( 0 );
                            TableImageViews[ i ][ j ].setTag( EMPTY_TABLE_CARD );
                        }
                    }
                }
            } else if( table_data[ i ][ 0 ] == GroupClass.GROUP_TYPE_COMBINATION ) {
                group_id = table_data[ i ][ 1 ];
                for( j = 0; j < HALF_ROW_CARDS; j++ ) {
                    if( j < groups.get( group_id ).total_cards ) {
                        TableImageViews[ i ][ j ].setImageResource( cardsimages[ groups.get( group_id ).cards[ j ] ] );
                        TableImageViews[ i ][ j ].setTag( group_id );
                    } else {
                        if( clean ) {
                            TableImageViews[ i ][ j ].setImageResource( 0 );
                            TableImageViews[ i ][ j ].setTag( EMPTY_TABLE_CARD );
                        }
                    }
                }
                group_id = table_data[ i ][ 2 ];
                if( group_id >= 0 ) {
                    for( j = 0; j < HALF_ROW_CARDS; j++ ) {
                        if( j < groups.get( group_id ).total_cards ) {
                            TableImageViews[ i ][ j + HALF_ROW_CARDS ].setImageResource( cardsimages[ groups.get( group_id ).cards[ j ] ] );
                            TableImageViews[ i ][ j + HALF_ROW_CARDS ].setTag( group_id );
                        } else {
                            if( clean ) {
                                TableImageViews[ i ][ j + HALF_ROW_CARDS ].setImageResource( 0 );
                                TableImageViews[ i ][ j + HALF_ROW_CARDS ].setTag( EMPTY_TABLE_CARD );
                            }
                        }
                    }
                }
            } else {
                /*
                if( clean ) {
                    for( j = 0; j < MAX_ROW_CARDS; j++ ) {
                        TableImageViews[ i ][ j ].setImageResource( 0 );
                        TableImageViews[ i ][ j ].setTag( EMPTY_TABLE_CARD );
                    }
                }
                */
            }
        }
    }


    public void show_endgame_layout( boolean playerwins ) {
        // avvio animazione secondo playerwins
        if( playerwins ) {
            iv_EndGameAnimation.setImageResource( R.drawable.cryanim );
            danceAnimation = (AnimationDrawable) iv_EndGameAnimation.getDrawable();
            danceAnimation.start();
            iv_EndGameText.setImageResource( R.drawable.haivintotu );
        } else {
            iv_EndGameAnimation.setImageResource( R.drawable.danceanim );
            danceAnimation = (AnimationDrawable) iv_EndGameAnimation.getDrawable();
            danceAnimation.start();
            iv_EndGameText.setImageResource( R.drawable.hovintoio );
        }

        // visualizza il layout di fine partita
        EndGameLayout.setVisibility( View.VISIBLE );
    }

    public boolean is_endgame_layout_visible() {
        return EndGameLayout.getVisibility() == View.VISIBLE;
    }


    public void hide_endgame_layout() {
        // ferma l'animazione
        danceAnimation.stop();

        // nasconde il layout di fine partita
        EndGameLayout.setVisibility( View.INVISIBLE );
    }



    private ProgressDialog      btProgressDialog;

    public static final int    BTPD_STARTGAMESYNC   = 1;
    public static final int    BTPD_ENDOFTURNSYNC   = 2;
    public static final int    BTPD_RECONNECTING    = 3;
    public static final int    BTPD_WAITTURN        = 4;

    public synchronized void show_bt_wait_dialog( int type ) {
        if( btProgressDialog != null ) {
            btProgressDialog.dismiss();
            btProgressDialog = null;
        }

        btProgressDialog = new ProgressDialog( mContext );
        btProgressDialog.setCancelable( false );
        btProgressDialog.setIndeterminate( true );

        btProgressDialog.setButton( DialogInterface.BUTTON_POSITIVE, "Annulla partita", new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                ((GameActivity)mContext).onBackToMenuClick( null );
            }
        }
        );
        switch( type ) {
            case BTPD_STARTGAMESYNC:
                btProgressDialog.setMessage( "Sincronizzazione inizio partita" );
                btProgressDialog.setIndeterminateDrawable( mContext.getResources().getDrawable(R.drawable.syncanim ) );
                break;
            case BTPD_ENDOFTURNSYNC:
                btProgressDialog.setMessage( "Sincronizzazione dati partita" );
                btProgressDialog.setIndeterminateDrawable( mContext.getResources().getDrawable(R.drawable.syncanim ) );
                break;
            case BTPD_RECONNECTING:
                btProgressDialog.setMessage( "Connessione in corso..." );
                btProgressDialog.setIndeterminateDrawable( mContext.getResources().getDrawable(R.drawable.syncanim ) );
                break;
            case BTPD_WAITTURN:
                btProgressDialog.setMessage( "Attendi il tuo turno..." );
                btProgressDialog.setIndeterminateDrawable( mContext.getResources().getDrawable(R.drawable.waitturnanim ) );
                break;
        }
        btProgressDialog.show();
    }

    public synchronized void hide_bt_wait_dialog() {
        if( btProgressDialog != null ) {
            btProgressDialog.dismiss();
            btProgressDialog = null;
        }
    }

    public static final int    BTED_ACTIVESIDE_CONNECTION_LOST      = 1;
    public static final int    BTED_PASSIVESIDE_CONNECTION_ERROR    = 2;
    public static final int    BTED_ACTIVESIDE_CONNECTION_FAILED    = 3;
    public static final int    BTED_ACTIVESIDE_COMMUNICATION_ERROR  = 4;
    private AlertDialog alerterror;

    public synchronized void show_bt_error_dialog( int type ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( mContext );
        builder.setCancelable( false );
        builder.setIcon( mContext.getResources().getDrawable( R.drawable.disconnectionicon ) );
        switch( type ) {
            case BTED_ACTIVESIDE_COMMUNICATION_ERROR:
                builder.setTitle( "Errore di comunicazione");
                builder.setMessage( "Vuoi tentare la riconnessione con l'altro dispositivo o annullare la partita?" );
                builder.setPositiveButton( "Riconnetti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity)mContext).bluetooth_reconnect();
                    }
                });
                builder.setNegativeButton( "Annulla partita", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity)mContext).onBackToMenuClick( null );
                    }
                });
                break;
            case BTED_ACTIVESIDE_CONNECTION_LOST:
                builder.setTitle( "Connessione persa");
                builder.setMessage( "Vuoi tentare la riconnessione con l'altro dispositivo o annullare la partita?" );
                builder.setPositiveButton( "Riconnetti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity)mContext).bluetooth_reconnect();
                    }
                });
                builder.setNegativeButton( "Annulla partita", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity)mContext).onBackToMenuClick( null );
                    }
                });
                break;
            case BTED_ACTIVESIDE_CONNECTION_FAILED:
                builder.setTitle( "Connessione fallita");
                builder.setMessage("Vuoi tentare la riconnessione con l'altro dispositivo o annullare la partita?");
                builder.setPositiveButton("Riconnetti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity) mContext).bluetooth_reconnect();
                    }
                });
                builder.setNegativeButton("Annulla partita", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity) mContext).onBackToMenuClick(null);
                    }
                });
                break;
            case BTED_PASSIVESIDE_CONNECTION_ERROR:
                builder.setTitle( "Connessione persa o errore di comunicazione");
                builder.setMessage("Vuoi attendere la riconnessione con l'altro dispositivo o annullare la partita?");
                builder.setNegativeButton("Annulla partita", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity) mContext).onBackToMenuClick(null);
                    }
                });
        }
        alerterror = builder.create();
        alerterror.show();
    }

    public synchronized void hide_bt_error_dialog() {
        if( alerterror != null ) {
            alerterror.dismiss();
            alerterror = null;
        }
    }


    public static final int    BTFD_WIN_ACTIVE      = 1;
    public static final int    BTFD_WIN_PASSIVE     = 2;
    public static final int    BTFD_LOSE_ACTIVE     = 3;
    public static final int    BTFD_LOSE_PASSIVE    = 4;
    private AlertDialog alertfinal;

    public synchronized void show_bt_final_dialog( int type ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( mContext );

        builder.setCancelable( false );
        switch( type ) {
            case BTFD_WIN_ACTIVE:
            case BTFD_WIN_PASSIVE:
                builder.setTitle( "HAI VINTO");
                builder.setIcon( mContext.getResources().getDrawable( R.drawable.trophyicon ) );
                break;
            case BTFD_LOSE_ACTIVE:
            case BTFD_LOSE_PASSIVE:
                builder.setTitle( "HAI PERSO");
                // TODO impostare icona
                //dialog.setIcon( mContext.getResources().getDrawable( R.drawable.goldencup ) );
                break;
        }
        if( type == BTFD_WIN_ACTIVE || type == BTFD_LOSE_ACTIVE ) {
            builder.setMessage( "Vuoi fare un'altra partita ? " );
            // il tasto "Gioca ancora" e' visibile solo alla parte attiva
            builder.setPositiveButton( "Gioca ancora", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((GameActivity)mContext).bluetooth_start_new_game();
                }
            });
            builder.setNegativeButton( "Menu", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((GameActivity)mContext).onBackToMenuClick( null );
                }
            });

        } else {
            builder.setMessage( "Attendi l'invito per un'altra partita o esci dal gioco" );
            builder.setNegativeButton( "Menu", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((GameActivity)mContext).onBackToMenuClick( null );
                }
            });

        }
        alertfinal = builder.create();
        alertfinal.show();
    }

    public synchronized void hide_bt_final_dialog() {
        if( alertfinal != null ) {
            alertfinal.dismiss();
            alertfinal = null;
        }
    }






    private AlertDialog alerttrnm;

    public synchronized void show_end_of_turnament_dialog( boolean result, int player_score, int device_score ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( mContext );

        builder.setCancelable( false );
        if( result ) {
            builder.setTitle( "HAI VINTO LA GARA!");
            builder.setIcon( mContext.getResources().getDrawable( R.drawable.trophyicon ) );
        } else {
            builder.setTitle( "L'AVVERSARIO VINCE LA GARA");
        }
        String string = String.format( "Giocatore : %d pt.\n", player_score );
        string += String.format( "Avversario: %d pt.\n", device_score );
        string += "Vuoi continuare a giocare?";
        builder.setMessage( string );
        // il tasto "Gioca ancora" e' visibile solo alla parte attiva
        builder.setPositiveButton( "Gioca", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((GameActivity)mContext).onPlayAgainClick( null );
                //hide_end_of_turnament_dialog();
            }
        });
        builder.setNegativeButton( "Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((GameActivity)mContext).onBackToMenuClick( null );
                //hide_end_of_turnament_dialog();
            }
        });

        alertfinal = builder.create();
        alertfinal.show();
    }

    public synchronized void hide_end_of_turnament_dialog() {
        if( alertfinal != null ) {
            alertfinal.dismiss();
            alertfinal = null;
        }
    }

}
