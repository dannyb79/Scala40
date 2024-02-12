package com.plmtmobile.scala40se;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by daniele on 09/11/13.
 */
public class Retriever extends AsyncTask<PermanentData,Void,Long>  {
    private Context mContext;
    private ProgressDialog  pDialog;
    private boolean         connection_success = false;
    private String          account;
    private LinearLayout    hidelayout;


    Retriever( Context context, String accountid, LinearLayout l ) {
        mContext    = context;
        account     = accountid;
        hidelayout  = l;
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog( mContext );
        pDialog.setMessage( "Recupero dati..." );
        pDialog.setIndeterminate( true );
        pDialog.show();
    }

    protected Long doInBackground(PermanentData... permanentData) {
        long result = -1;
        try {
            String  basepath    = "http://www.plmtmobile.altervista.org/scala40/v2/retrieveaccount.php?uhash=";
            String  finalURL    = basepath + account;
            URL url = new URL( finalURL );

            try {
                connection_success = false;
                // connessione al server
                URLConnection connection = url.openConnection();
                connection.connect();


                // lettura risposta
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String  totalitemsfound;
                result = 0;
                if( ( totalitemsfound = in.readLine() ) != null ) {
                    connection_success = true;
                    // totalitems contiene il numero (in stringa) dei record trovati
                    int totalitems = Integer.parseInt( totalitemsfound );

                    if( totalitems == 1 ) {
                        String username;
                        String score;
                        String wongames;
                        String totalgames;
                        username = in.readLine();
                        if( username != null ) {
                            score = in.readLine();
                            if( score != null ) {
                                wongames = in.readLine();
                                if( wongames != null ) {
                                    totalgames = in.readLine();
                                    if( totalgames != null ) {
                                        result = 1;
                                        // aggiungere il record alla lista temporanea
                                        permanentData[ 0 ].set_hash( account );
                                        permanentData[ 0 ].save_username( username );
                                        permanentData[ 0 ].set_user_score( Integer.parseInt( score ) );
                                        permanentData[ 0 ].set_user_totalgames( Integer.parseInt( totalgames ) );
                                        permanentData[ 0 ].set_user_wongames(Integer.parseInt(wongames));
                                        permanentData[ 0 ].set_hash_is_set();
                                    }
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch ( IOException ex ) {

            }
        } catch ( MalformedURLException ex ) {
            //Log.v ( "CONNECT", ex.getMessage() );
        }
        return result;
    }

    protected void onPostExecute(Long result) {
        if( pDialog.isShowing() ) {
            pDialog.dismiss();
        }

        if( result < 0 ) {
            Toast toast = Toast.makeText( mContext, "Errore o connessione assente", Toast.LENGTH_SHORT );
            toast.show();
        } else if( result == 0 ) {
            Toast toast = Toast.makeText( mContext, "Account non trovato", Toast.LENGTH_SHORT );
            toast.show();
        } else {
            Toast toast = Toast.makeText( mContext, "Account recuperato!", Toast.LENGTH_SHORT );
            toast.show();
            hidelayout.setVisibility( View.INVISIBLE );
            ((MenuActivity)mContext).RetrieverPositiveResultCallback();
        }
    }

}
