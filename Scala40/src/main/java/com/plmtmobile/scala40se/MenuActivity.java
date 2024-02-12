package com.plmtmobile.scala40se;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.AccountPicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MenuActivity extends Activity {

    private PermanentData   permanentData;
    private TextView        tv_TotalGamesValue;
    private TextView        tv_PlayerScoreValue;
    private TextView        tv_DeviceScoreValue;
    private TextView        tv_RankScoreValue;
    private TextView        tv_RankWonsValue;
    private TextView        tv_TotalTournamentsValue;
    private TextView        tv_WonTournaments101Value;
    private TextView        tv_WonTournaments201Value;
    private EditText        edit_Name;
    private CheckBox        chk_FraudPlayer;
    private CheckBox        chk_RankPlayer;
    private CheckBox        chk_Tournament;
    private RadioButton     rdbtn_101;
    private RadioButton     rdbtn_201;

    private ArrayList<RankListItem> ranklist;
    private CustomAdapter   adapter;
    private LinearLayout    AccountIDSetupLayout;
    //private TextView        tv_AccountID;

    private Button          btn_Play;
    private Button          btn_Stats;
    private Button          btn_Info;
    private Button          btn_Rank;
    private Button          btn_Settings;
    private Button          btn_Bluetooth;
    private Button          btn_Multiplayer;

    private ListView            lv_BTDevices;
    private TextView            tv_BTStatus;
    private Context             mContext;

    private static final int        ACCOUNT_PICKER_INTENT_CODE  = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        RadioButton     rdbtn_Arrows;
        RadioButton     rdbtn_BlueCard;
        RadioButton     rdbtn_StraightsAscending;
        RadioButton     rdbtn_StraightsDescending;
        RadioButton     rdbtn_StraightsAlternate;
        RadioButton     rdbtn_ValuesAscending;
        RadioButton     rdbtn_ValuesDescending;
        RadioButton     rdbtn_ValuesAlternate;
        RadioButton     rdbtn_OnlineNameAccount;
        RadioButton     rdbtn_OnlineNameNickName;

        super.onCreate(savedInstanceState);
        // rimuove la title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // rimuove la notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        mContext = this;
        // instanza per recupero/salvataggio dati permanenti
        permanentData = new PermanentData( this );

        // TODO da togliere!!! solo x debug
        //permanentData.unset_hash();

        AccountIDSetupLayout = (LinearLayout) findViewById( R.id.AccountIDSetup );
        //tv_AccountID = (TextView) findViewById( R.id.tv_AccountID );
        //updateIdAccountTextView();

        btn_Play        = (Button) findViewById( R.id.btn_Play );
        btn_Stats       = (Button) findViewById( R.id.btn_Stats );
        btn_Info        = (Button) findViewById( R.id.btn_Info );
        btn_Rank        = (Button) findViewById( R.id.btn_Rank );
        btn_Settings    = (Button) findViewById( R.id.btn_Settings );
        btn_Bluetooth        = (Button) findViewById( R.id.btn_Bluetooth );
        btn_Multiplayer     = (Button) findViewById( R.id.btn_Multiplayer );

        // se non e' ancora stato creato un hash visualizzo la schermata
        // per generarne uno nuovo o recuperarne uno vecchio
        if( permanentData.is_hash_set() == false ) {

            // disabilita i tasti sottostanti
            btn_Play.setEnabled( false );
            btn_Stats.setEnabled( false );
            btn_Info.setEnabled( false );
            btn_Rank.setEnabled( false );
            btn_Settings.setEnabled( false );
            btn_Bluetooth.setEnabled( false );
            btn_Multiplayer.setEnabled( false );
            // fa comparire la schermata
            AccountIDSetupLayout.setVisibility( View.VISIBLE );
        }

        tv_TotalGamesValue  = (TextView)findViewById( R.id.tv_TotalGamesValue );
        tv_PlayerScoreValue = (TextView)findViewById( R.id.tv_PlayerScoreValue );
        tv_DeviceScoreValue = (TextView)findViewById( R.id.tv_DeviceScoreValue );
        tv_RankScoreValue   = (TextView)findViewById( R.id.tv_RankScoreValue );
        tv_RankWonsValue    = (TextView)findViewById( R.id.tv_RankWonsValue );
        tv_TotalTournamentsValue    = (TextView)findViewById( R.id.tv_TotalTournamentsValue );
        tv_WonTournaments101Value   = (TextView)findViewById( R.id.tv_WonTournaments101Value );
        tv_WonTournaments201Value   = (TextView)findViewById( R.id.tv_WonTournaments201Value);

        chk_FraudPlayer     = (CheckBox)findViewById( R.id.chk_FraudPlayer );
        chk_RankPlayer      = (CheckBox)findViewById( R.id.chk_Rank );
        edit_Name           = (EditText)findViewById( R.id.edit_Name );
        chk_Tournament      = (CheckBox)findViewById( R.id.chk_Tournament );
        rdbtn_101           = (RadioButton)findViewById( R.id.rdbtn_101 );
        rdbtn_201           = (RadioButton)findViewById( R.id.rdbtn_201 );
        rdbtn_Arrows        = (RadioButton)findViewById( R.id.rdbtn_Arrows );
        rdbtn_BlueCard      = (RadioButton)findViewById( R.id.rdbtn_BlueCard );
        rdbtn_StraightsAscending = (RadioButton)findViewById( R.id.rdbtn_StraightsAscending );
        rdbtn_StraightsDescending = (RadioButton)findViewById( R.id.rdbtn_StraightsDescending );
        rdbtn_StraightsAlternate = (RadioButton)findViewById( R.id.rdbtn_StraightsAlternate );
        rdbtn_ValuesAscending = (RadioButton)findViewById( R.id.rdbtn_ValuesAscending );
        rdbtn_ValuesDescending = (RadioButton)findViewById( R.id.rdbtn_ValuesDescending );
        rdbtn_ValuesAlternate = (RadioButton)findViewById( R.id.rdbtn_ValuesAlternate );
        rdbtn_OnlineNameAccount = (RadioButton)findViewById( R.id.rdbtn_AccountName);
        rdbtn_OnlineNameNickName = (RadioButton)findViewById( R.id.rdbtn_NickName );

        edit_Name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSettingSaveNameClick( v );
                }
                return false;
            }
        });

        boolean fraud = permanentData.get_fraud_option();
        chk_FraudPlayer.setChecked( fraud );
        boolean rank = permanentData.get_rank_option();
        chk_RankPlayer.setChecked( rank );
        if( rank ) {
            edit_Name.setEnabled( true );
        } else {
            edit_Name.setEnabled( false );
        }
        edit_Name.setText( permanentData.get_user_name() );
        boolean tournament = permanentData.get_tournament_enabled();
        chk_Tournament.setChecked( tournament );
        if( chk_Tournament.isChecked() ) {
            rdbtn_101.setEnabled( true );
            rdbtn_201.setEnabled( true );
            if( permanentData.get_tournament_limit() == 101 ) {
                rdbtn_101.setChecked( true );
            } else {
                rdbtn_201.setChecked( true );
            }
        } else {
            rdbtn_101.setEnabled( false );
            rdbtn_201.setEnabled( false );
        }
        // selettore carte
        if( permanentData.get_card_selector() == 0 ) {
            rdbtn_Arrows.setChecked( true );
        } else {
            rdbtn_BlueCard.setChecked( true );
        }
        // ordinamento scale
        switch( permanentData.get_straight_sort_mode() ) {
            case 0:
                rdbtn_StraightsAscending.setChecked( true );
                break;
            case 1:
                rdbtn_StraightsDescending.setChecked( true );
                break;
            default:
                rdbtn_StraightsAlternate.setChecked( true );
                break;
        }
        // ordinamento valori
        switch( permanentData.get_values_sort_mode() ) {
            case 0:
                rdbtn_ValuesAscending.setChecked( true );
                break;
            case 1:
                rdbtn_ValuesDescending.setChecked( true );
                break;
            default:
                rdbtn_ValuesAlternate.setChecked( true );
                break;
        }

        if( permanentData.get_online_name() == permanentData.ONLINESETTINGS_ACCOUNT_NAME ) {
            rdbtn_OnlineNameAccount.setChecked( true );
        } else {
            rdbtn_OnlineNameNickName.setChecked( true );
        }

        // ordinamento valori
        switch( permanentData.get_background_image() ) {
            case 0:
                ((RadioButton)findViewById(R.id.rdbtn_BackgroundImageNone)).setChecked( true );
                break;
            case 1:
                ((RadioButton)findViewById(R.id.rdbtn_BackgroundImage1)).setChecked( true );
                break;
            case 2:
                ((RadioButton)findViewById(R.id.rdbtn_BackgroundImage2)).setChecked( true );
                break;
            case 3:
                ((RadioButton)findViewById(R.id.rdbtn_BackgroundImage3)).setChecked( true );
                break;
            case 4:
                ((RadioButton)findViewById(R.id.rdbtn_BackgroundImage4)).setChecked(true);
                break;
            default:
                ((RadioButton)findViewById(R.id.rdbtn_BackgroundImageNone)).setChecked(true);
                break;
        }

         ListView listView = (ListView)findViewById(R.id.lv_Rank);
        listView.setCacheColorHint( Color.TRANSPARENT );
        ranklist = new ArrayList<RankListItem>();
        adapter = new CustomAdapter(this, R.layout.rowcustom, ranklist);
        listView.setAdapter(adapter);

        fillInfoList();

        // supporto bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lv_BTDevices = (ListView) findViewById( R.id.lv_BTDevices );
        lv_BTDevices.setCacheColorHint( Color.TRANSPARENT );
        lv_BTDevices.setOnItemClickListener(DeviceClickListener);
        DevicesArrayAdapter = new ArrayAdapter<String>( this, R.layout.devicelistrow );
        DevicesArrayAdapter.clear();
        lv_BTDevices.setAdapter( DevicesArrayAdapter );

        BluetoothService.getInstance().setHandler( bluetoothHandler );
        if( BluetoothService.getInstance().isCreatedinstance() == false ) {
            BluetoothService.getInstance().create();
        }

        tv_BTStatus = (TextView) findViewById( R.id.tv_BTStatus );

    }

    private void fillInfoList() {
        String versionName = "2.2"; // default
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String[] web = {
                "Numero di carte dell'avversario",
                "Tallone",
                "Ordina le carte in scale",
                "Ordina le carte per valore",
                "Tocco rapido per selezionare la carta",
                "Trascina una carta per spostarla",
                "Numero carte dell'avversario (bluetooth)",
                "Info carte e turno avversario (online)",
                "Invia o ignora messaggi (tocco breve o lungo)",
                "ID account: " + permanentData.get_user_hash(),
                "Versione software "+versionName
        } ;
        Integer[] imageId = {
                R.drawable.androidrobot_13,
                R.drawable.cardback,
                R.drawable.sort_straight,
                R.drawable.sort_values,
                R.drawable.infoselectedcard,
                R.drawable.infodragcard,
                R.drawable.turno_giocatore,
                R.drawable.infompuser,
                R.drawable.invia_messaggio,
                R.drawable.infokey,
                R.drawable.infosoftware
        };

        InfoAdapter adapter = new InfoAdapter( MenuActivity.this, web, imageId );
        ListView lv_Info = (ListView)findViewById(R.id.lv_Info);
        lv_Info.setCacheColorHint( Color.TRANSPARENT );
        lv_Info.setAdapter( adapter );


        // supporto bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lv_BTDevices = (ListView) findViewById( R.id.lv_BTDevices );
        lv_BTDevices.setCacheColorHint( Color.TRANSPARENT );
        lv_BTDevices.setOnItemClickListener(DeviceClickListener);
        DevicesArrayAdapter = new ArrayAdapter<String>( this, R.layout.devicelistrow );
        DevicesArrayAdapter.clear();
        lv_BTDevices.setAdapter( DevicesArrayAdapter );

        BluetoothService.getInstance().setHandler( bluetoothHandler );
        if( BluetoothService.getInstance().isCreatedinstance() == false ) {
            BluetoothService.getInstance().create();
        }

        tv_BTStatus = (TextView) findViewById( R.id.tv_BTStatus );

    }

    private void updateIdAccountTextView() {
        //tv_AccountID.setText("ID ACCOUNT: " + permanentData.get_user_hash());
    }

    public void onGenerateNewAccountIDClick( View v ) {
        // calcola un nuovo hash e lo salva
        permanentData.calculate_hash();
        // riabilita i tasti sotto
        btn_Play.setEnabled( true );
        btn_Stats.setEnabled( true );
        btn_Info.setEnabled( true );
        btn_Rank.setEnabled( true );
        btn_Settings.setEnabled( true );
        btn_Bluetooth.setEnabled( true );
        btn_Multiplayer.setEnabled( true );

        // nasconde la finestra per l'impostazione dell'ID Account
        AccountIDSetupLayout.setVisibility(View.INVISIBLE);
    }

    public void onUseOldAccountIDClick( View v ) {
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.AISviewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
        vf.showNext();
    }

    public void onAbortUseOldAccountIDClick( View v ) {
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.AISviewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
        vf.showPrevious();
    }

    public void onRetrieveAccountIDClick( View v ) {
        EditText edit_AccountID = (EditText)findViewById( R.id.edit_AccountID );
        String account = edit_AccountID.getText().toString();
        if( account.length() < 20 ) {
            Toast toast = Toast.makeText( this, "Il codice deve essere di 20 caratteri", Toast.LENGTH_LONG );
            toast.show();
            return;
        }
        new Retriever( this, account, AccountIDSetupLayout ).execute(permanentData);
    }

    private boolean validateAccountCode( String accountCode ) {
        boolean result = true;

        for( int i = 0; i < accountCode.length(); i++ ) {
            char singlechar = accountCode.charAt( i );
            switch( singlechar ) {
                case 'A': break;
                case 'B': break;
                case 'C': break;
                case 'D': break;
                case 'E': break;
                case 'F': break;
                case 'G': break;
                case 'H': break;
                case 'J': break;
                case 'R': break;
                case 'Z': break;
                case '0': break;
                case '1': break;
                case '2': break;
                case '3': break;
                case '4': break;
                case '5': break;
                case '6': break;
                case '7': break;
                case '8': break;
                case '9': break;
                default:    result = false; break;
            }
            if( result == false ) {
                break;
            }
        }
        return result;
    }


    public void onAccountRecovery( View v ) {
        EditText edit_AccountID = (EditText)findViewById( R.id.edit_OldAccountID );
        String account = edit_AccountID.getText().toString();
        if( account.length() != 20 ) {
            Toast toast = Toast.makeText( this, "Il codice deve essere di 20 caratteri", Toast.LENGTH_LONG );
            toast.show();
            return;
        }
        if( validateAccountCode( account ) == false ) {
            Toast toast = Toast.makeText( this, "Codice non valido", Toast.LENGTH_LONG );
            toast.show();
            return;
        }
        new Retriever( this, account, AccountIDSetupLayout ).execute(permanentData);
    }

    public void RetrieverPositiveResultCallback() {
        // riabilita i tasti sotto
        btn_Play.setEnabled(true);
        btn_Stats.setEnabled( true );
        btn_Info.setEnabled( true );
        btn_Rank.setEnabled( true );
        btn_Settings.setEnabled( true );
        btn_Bluetooth.setEnabled( true );
        btn_Multiplayer.setEnabled( true );
        edit_Name.setText( permanentData.get_user_name() );
        fillInfoList();
    }


    public void changeFraudOptionStatus( View v ) {
        boolean is_checked = ((CheckBox)v).isChecked();
        permanentData.set_fraud_option(is_checked);

        if( is_checked ) {
            if( chk_RankPlayer.isChecked() ) {
                chk_RankPlayer.setChecked(false);
                changeRankOptionStatus(chk_RankPlayer);
            }
            if( chk_Tournament.isChecked() ) {
                chk_Tournament.setChecked( false );
                changeTournamentOptionStatus(chk_Tournament);
            }
        }
    }

    public void changeRankOptionStatus( View v ) {
        boolean is_checked = ((CheckBox)v).isChecked();
        permanentData.set_rank_option(is_checked);

        if( is_checked ) {
            if( chk_FraudPlayer.isChecked() ) {
                chk_FraudPlayer.setChecked( false );
                changeFraudOptionStatus( chk_FraudPlayer );
            }
            edit_Name.setEnabled( true );
        } else {
            edit_Name.setEnabled( false );
        }
    }

    public void changeTournamentOptionStatus( View v ) {
        boolean is_checked = ((CheckBox)v).isChecked();
        permanentData.set_tournament_enabled( is_checked );


        if( is_checked ) {
            // abilita la selezione 101 o 201
            rdbtn_101.setEnabled( true );
            rdbtn_201.setEnabled( true );
            if( permanentData.get_tournament_limit() == 101 ) {
                rdbtn_101.setChecked( true );
            } else {
                rdbtn_201.setChecked( true );
            }
            // disabilita la modalita' "giocatore imbroglione"
            if( chk_FraudPlayer.isChecked() ) {
                chk_FraudPlayer.setChecked( false );
                changeFraudOptionStatus( chk_FraudPlayer );
            }
            // reset contatori di gara
            permanentData.reset_tournament_data();
        } else {
            rdbtn_101.setEnabled( false );
            rdbtn_201.setEnabled( false );
        }
    }

    public void changeTournamentLimitTo101( View v ) {
        permanentData.set_tournament_limit( 101 );
    }

    public void changeTournamentLimitTo201( View v ) {
        permanentData.set_tournament_limit( 201 );
    }

    public void changeCardSelectorToArrow( View v ) {
        permanentData.set_card_selector( 0 );
    }

    public void changeCardSelectorToBlueCard( View v ) {
        permanentData.set_card_selector( 1 );
    }

    public void changeStraightSortToAscending( View v ) {
        permanentData.set_straight_sort_mode( 0 );
    }

    public void changeStraightSortToDescending( View v ) {
        permanentData.set_straight_sort_mode( 1 );
    }

    public void changeStraightSortToAlternate( View v ) {
        permanentData.set_straight_sort_mode( 2 );
    }

    public void changeValuesSortToAscending( View v ) {
        permanentData.set_values_sort_mode( 0 );
    }

    public void changeValuesSortToDescending( View v ) {
        permanentData.set_values_sort_mode( 1 );
    }

    public void changeValuesSortToAlternate( View v ) {
        permanentData.set_values_sort_mode( 2 );
    }

    public void changeOnlineNameToAccountName( View v ) {
        permanentData.set_online_name( permanentData.ONLINESETTINGS_ACCOUNT_NAME );
    }

    public void changeOnlineNameToNickName( View v ) {
        permanentData.set_online_name( permanentData.ONLINESETTINGS_USER_NICKNAME );
    }

    public void changeBackgroundImage( View v ) {
        switch( v.getId() ) {
            case R.id.rdbtn_BackgroundImage1:
                permanentData.set_background_image( 1 );
                break;
            case R.id.rdbtn_BackgroundImage2:
                permanentData.set_background_image( 2 );
                break;
            case R.id.rdbtn_BackgroundImage3:
                permanentData.set_background_image( 3 );
                break;
            case R.id.rdbtn_BackgroundImage4:
                permanentData.set_background_image( 4 );
                break;
            default:
                permanentData.set_background_image( 0 );
                break;
        }
    }


    /*
        pagina principale
    */

    public void onPlayButtonClick( View v ) {
        Intent intent = new Intent( this, GameActivity.class );
        startActivity(intent);
    }

    public void onStatButtonClick( View v ) {
        // aggiorna i valori visualizzati a video
        update_stats_textviews();
        // animazione ingresso layout statistiche
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
        vf.showNext();
    }

    public void onInfoButtonClick( View v ) {
        // aggiorna la textview con l'account id
        //updateIdAccountTextView();


        // animazione ingresso layout statistiche
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
        vf.setDisplayedChild( 3 );
    }

    public void onSettingsButtonClick( View v ) {
        // animazione ingresso layout statistiche
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
        vf.setDisplayedChild(2);
    }

    public void onRankButtonClick( View v ) {
        // animazione ingresso layout statistiche
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
        vf.setDisplayedChild( 4 );

        // caricamento classifica
        ranklist.clear();
        new Downloader( this, ranklist, adapter ).execute(ranklist);
    }

    public void onRankBackMenuClick( View v ) {
        // animazione ingresso layout statistiche
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in));
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
        vf.setDisplayedChild(0);
    }
    /*
        pagina statistiche
    */
    private void update_stats_textviews() {
        tv_TotalGamesValue.setText( String.format( "%d/%d", permanentData.get_won_games(), permanentData.get_total_games() ) );
        tv_PlayerScoreValue.setText(String.format("%d", permanentData.get_player_score()));
        tv_DeviceScoreValue.setText( String.format( "%d", permanentData.get_device_score() ) );

        tv_RankWonsValue.setText(String.format("%d/%d", permanentData.get_user_wongames(), permanentData.get_user_totalgames()));
        tv_RankScoreValue.setText(String.format("%d", permanentData.get_user_score()));

        tv_TotalTournamentsValue.setText(String.format("%d", permanentData.get_total_tournaments()));
        tv_WonTournaments101Value.setText(String.format("%d", permanentData.get_won_101_tournaments()));
        tv_WonTournaments201Value.setText(String.format("%d", permanentData.get_won_201_tournaments()));
    }

    public void onStatsBackButtonClick( View v ) {
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in));
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
        vf.showPrevious();
    }

    public void onStatsResetButtonClick( View v ) {
        // azzera e salva i valori permanentemente
        permanentData.reset_statistics();
        // aggiorna i valori visualizzati a video
        update_stats_textviews();
    }

    /*
        pagina info
    */
    public void onInfoBackButtonClick( View v ) {
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation( this, R.anim.right_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation( this, R.anim.right_out ) );
        vf.setDisplayedChild(0);
    }

    /*
        pagina impostazioni
    */
    public void onSettingsBackButtonClick( View v ) {
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation( this, R.anim.right_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
        vf.setDisplayedChild(0);
    }

    public void onSettingSaveNameClick( View v ) {
        String newname = edit_Name.getText().toString();
        /*
        // TODO abilitare in caso di problemi!!!
        if( !newname.matches( "/^[a-z0-9_-]{3,20}$/" ) ) {
            Toast toast = Toast.makeText( this, "Uno o piu' caratteri non validi.\nPuoi usare lettere numeri e _", Toast.LENGTH_LONG );
            toast.show();
            return;
        }*/
        permanentData.save_username( newname );
        // connessione al server per segnalare il nuovo nome in modalita' "verbose" ovvero
        // con visualizzazione operazione in corso ed esito aggiornamento
        new Uploader( this, true ).execute(permanentData);
    }


    public void onIdAccountEmailBackup( View v ) {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, ACCOUNT_PICKER_INTENT_CODE );

    }


    /*
        TODO aggiungere pulsante uscita
    */
    public void onMainManuExit( View v ) {
        this.finish();
    }

    // ================================== BLUETOOTH ======================================

    // Intent request codes
    private static final int        REQUEST_ENABLE_BT          = 1;
    private static final int        REQUEST_DISCOVERABLE_BT    = 2;

    private BluetoothAdapter        mBluetoothAdapter;
    private ArrayAdapter<String>    DevicesArrayAdapter;
    private ProgressDialog          mProgressDialog;
    private boolean                 registeredReceiver = false;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        int position = vf.getDisplayedChild();
        savedInstanceState.putInt("TAB_NUMBER", position);
    }
    /*
        deregistra il ricevitore di eventi broadcast
    */
    private void unregistrerBCReceiver() {
        // deregistrazione dal broadcast receiver
        if( registeredReceiver == true ) {
            try {
                unregisterReceiver( bcReceiver );
            } catch ( IllegalArgumentException e ) {
                Toast.makeText( mContext, e.getMessage(), Toast.LENGTH_SHORT ).show();
            }
            registeredReceiver = false;
        }
    }

    /*
        l'activity menu torna visibile (all'uscita dal gioco)
    */
    @Override
    public synchronized void onResume() {
        super.onResume();
        // reimposto l'handler (del menu e non del gioco) a cui segnalare eventuali eventi
        BluetoothService.getInstance().setHandler( bluetoothHandler );
    }

    /*
        TODO ricontrollare!
    */
    @Override
    public void onDestroy() {
        // ferma il servizio bluetooth
        BluetoothService.getInstance().delete();
        // deregistra il ricevitore di eventi broadcast
        unregistrerBCReceiver();

        super.onDestroy();
    }

    /*
        impostazione stringa stato connessione
    */
    private void setTvStatus( String text, int color ) {
        tv_BTStatus.setTextColor( color );
        tv_BTStatus.setText( text );
    }

    /*
        click sull'item della lista dei dispositivi e tentativo di connessione
    */
    private AdapterView.OnItemClickListener DeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // connessione al (MAC address del) dispositivo selezionato
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( address );
            BluetoothService.getInstance().connect( device );
        }
    };


    /*
        avvio ricerca di altri dispositivi oltre a quelli gia' associati
    */
    public void startBluetoothDiscovery( View v ) {

        // registrazione al broadcast receiver
        if( registeredReceiver == false ) {
            // Register for broadcasts when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver( bcReceiver, filter);
            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver( bcReceiver, filter);
            // activity registrata
            registeredReceiver = true;
        }

        // riempie la lista dei device solo con quelli associati
        fill_device_list_with_bonded();

        // visualizza progress dialog
        mProgressDialog = new ProgressDialog( this );
        mProgressDialog.setMessage("Ricerca dispositivi...");
        mProgressDialog.setIndeterminate( true );
        mProgressDialog.show();
        // ferma un'evenutale ricerca gia' in corso
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // avvio ricerca
        mBluetoothAdapter.startDiscovery();
    }

    private void fill_device_list_with_bonded() {
        // riempie la lista dei dispositivi con i dispositivi gia' associati
        if( DevicesArrayAdapter != null ) {
            DevicesArrayAdapter.clear();
            if( mBluetoothAdapter != null ) {
                if( BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON ) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if ( ( pairedDevices != null ) && ( pairedDevices.size() > 0 ) ) {
                        for( BluetoothDevice device : pairedDevices ) {
                            DevicesArrayAdapter.add( device.getName() + "\n" + device.getAddress() );
                        }
                        DevicesArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /*
        ingresso nella pagina impostazioni bluetooth
    */
    private void showBluetoothMenu() {

        // avvio o riavvio del servizio bluetooth in modalita' ascolto
        if( BluetoothService.getInstance().start() == true ) {
            //Toast.makeText( mContext, "Spegnere e riaccendere il bluetooth", Toast.LENGTH_SHORT ).show();
            mBluetoothAdapter.disable();
            onBluetoothPageEnterClick( null );
            return;
        }
        // riempie la lista dei device solo con quelli associati
        fill_device_list_with_bonded();
        // animazione ingresso layout bluetooth
        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in) );
        vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
        vf.setDisplayedChild( 5 );
    }

    /*
        richiesta attivazione visibilita' agli altri dispositivi
    */
    private void discoverableRequest() {
        // richiesta di rendere il dispositivo trovabile
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
        startActivityForResult( discoverableIntent, REQUEST_DISCOVERABLE_BT );
    }

    /*
        pressione tasto "bluetooth" nel menu' principale
    */
    public void onBluetoothPageEnterClick( View v ) {

        // se l'adapter e' null il bluetooth non e' supportato
        if( mBluetoothAdapter == null ) {
            Toast.makeText( this, "Bluetooth non supportato", Toast.LENGTH_LONG ).show();
        } else {
            // se non e' attivato il bluetooth viene richiesto di attivarlo
            if( !mBluetoothAdapter.isEnabled() ) {
                Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
            } else {
                // se il bluetooth e' gia' attivo bisogna accertarsi che il dispositivo sia trovabile
                if( mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
                    // richiesta attivazione visibilita'
                    discoverableRequest();
                } else {
                    // ingresso nella pagina impostazioni bluetooth
                    showBluetoothMenu();
                }
            }
        }
    }

    /*
        tasto uscita dalla pagina bluetooth
    */
    public void onBluetoothPageExitClick( View v ) {
        // disabilita il servizio bluetooth
        BluetoothService.getInstance().stop();
        // deregistra (se registrato) il ricevitore di eventi broadcast
        unregistrerBCReceiver();
        // ritorno al menu principale
        onInfoBackButtonClick( btn_Info );
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler bluetoothHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MSG_BT_CONNECTED:

                    setTvStatus( "CONNESSO", Color.GREEN );

                    // recupero dati del dispositivo agganciato (nome, MAC address)
                    BluetoothService.ConnectionData data = (BluetoothService.ConnectionData)msg.obj;

                    // .. e avvio gioco
                    Intent intent = new Intent( mContext, GameActivity.class );
                    intent.putExtra( BluetoothService.INTENTEXTRA_BT_DEVICEADDRESS, data.deviceAddress  );
                    intent.putExtra( BluetoothService.INTENTEXTRA_BT_ENABLED,       true                );
                    intent.putExtra( BluetoothService.INTENTEXTRA_BT_ACTIVE,        data.active         );
                    startActivity( intent );

                    break;
                case BluetoothService.MSG_BT_CONNECTING:
                    setTvStatus( "CONNESSIONE IN CORSO...", Color.YELLOW );
                    break;
                case BluetoothService.MSG_BT_LISTENING:
                    setTvStatus( "IN ATTESA DI CONNESSIONE", Color.YELLOW );
                    break;
                case BluetoothService.MSG_BT_STOPPED:
                    setTvStatus( "INATTIVO", Color.RED );
                    break;

                case BluetoothService.MSG_BT_CONNECTION_FAILED:
                    Toast.makeText( mContext, "Connessione fallita", Toast.LENGTH_SHORT ).show();
                    break;
                case BluetoothService.MSG_BT_CONNECTION_LOST:
                    Toast.makeText( mContext, "Connessione persa", Toast.LENGTH_SHORT ).show();
                    break;
                case BluetoothService.MSG_BT_EXCEPTION:
                    String text = (String)msg.obj;
                    if( ( text != null ) && ( text.length() > 0 ) ) {
                        Toast.makeText( mContext, text, Toast.LENGTH_SHORT ).show();
                    } else {
                        Toast.makeText( mContext, "Errore generico", Toast.LENGTH_SHORT ).show();
                    }
                    break;
                case BluetoothService.MSG_BT_NEW_DEVICE_FOUND:
                    // nuovo dispositivo trovato durante la ricerca
                    String devicedescription = (String)msg.obj;
                    if( devicedescription == null ) {
                        devicedescription = "[Nome mancante]";
                    }
                    DevicesArrayAdapter.add( devicedescription );
                    DevicesArrayAdapter.notifyDataSetChanged();
                    break;
                case BluetoothService.MSG_BT_DISCOVERY_FINISHED:
                    // fine ricerca dispositivi
                    mProgressDialog.dismiss();
                    break;

            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // ritorno dalla richiesta di abilitazione bluetooth
                if (resultCode == Activity.RESULT_OK) {
                    // il bluetooth e' ora abilitato...
                    // se il bluetooth e' gia' attivo bisogna accertarsi che il dispositivo sia trovabile
                    if( mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
                        // richiesta attivazione visibilita'
                        discoverableRequest();
                    } else {
                        // ingresso nella pagina impostazioni bluetooth
                        showBluetoothMenu();
                    }
                } else {
                    // l'utente non ha voluto abilitare il bluetooth
                    Toast.makeText(this, "Bluetooth non attivo", Toast.LENGTH_SHORT ).show();
                }
            break;
            case REQUEST_DISCOVERABLE_BT:
                // ritorno dalla richiesta di abilitazione bluetooth
                if( resultCode == 300 /*Activity.RESULT_OK*/ ) {
                    // l'utente ha accettato di rendere visibile il dispositivo
                    // ingresso nella pagina impostazioni bluetooth
                    showBluetoothMenu();
                } else {
                    // l'utente non ha voluto rendere visibile il disposizibo
                    Toast.makeText(this, "Dispositivo non visibile", Toast.LENGTH_SHORT ).show();
                }
            break;
            case ACCOUNT_PICKER_INTENT_CODE:
                if( resultCode == RESULT_OK ) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if( accountName != null ) {
                        new IDAccountEmailSender( this, accountName, permanentData.get_user_hash() ).execute();
                    } else {
                        Toast.makeText(this, "Indirizzo non valido", Toast.LENGTH_SHORT ).show();
                    }
                }
                break;
        }
    }

    /*
    il broadcast receiver per l'intercettazione degli eventi:
    - trovato dispositivo bluetooth visible
    - ricerca altri dispositivi terminata
*/
    private final BroadcastReceiver bcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if( BluetoothDevice.ACTION_FOUND.equals( action ) ) {

                // BluetoothDevice dall'intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // se e' gia' associato lo salta
                if ( device.getBondState() != BluetoothDevice.BOND_BONDED ) {
                    // segnala all'handler che e' stato trovato un nuovo dispositivo
                    if( bluetoothHandler != null ) {
                        String devicedescription = device.getName() + "\n" + device.getAddress();
                        bluetoothHandler.obtainMessage( BluetoothService.MSG_BT_NEW_DEVICE_FOUND, devicedescription ).sendToTarget();
                    }
                }
                // When discovery is finished, change the Activity title
            } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
                // segnala all'handler che e' terminata la ricerca
                if( bluetoothHandler != null ) {
                    bluetoothHandler.obtainMessage( BluetoothService.MSG_BT_DISCOVERY_FINISHED ).sendToTarget();
                }
            }
        }
    };


    /*
        avvia l'activiy per il multiplayer
    */
    public void onMultiplayerButtonClick( View v ) {
        Intent intent = new Intent( this, MPActivity.class );
        startActivity( intent );
    }
}
