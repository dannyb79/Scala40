package com.plmtmobile.scala40se;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by daniele on 02/11/13.
 */
public class Uploader extends AsyncTask<PermanentData,Void,Long> {

    private ProgressDialog  pDialog;
    private Context         mContext;
    private boolean         verbose = false;

    Uploader( Context context, boolean verbose_mode ) {
        mContext    = context;
        verbose     = verbose_mode;
    }

    protected void onPreExecute() {
        if( verbose ) {
            pDialog = new ProgressDialog( mContext );
            pDialog.setMessage( "Aggiornamento" );
            pDialog.setIndeterminate( true );
            pDialog.show();
        }
    }

    protected Long doInBackground(PermanentData... permanentData) {
        String  username     = "";
        String  userhash     = "";
        String  userscore    = "";
        String  usertotalgames = "";
        String  userwongames = "";
        long    result = 0;

        try {

            try {
                username        += URLEncoder.encode( permanentData[ 0 ].get_user_name(), "UTF-8" );
                userhash        += URLEncoder.encode( permanentData[ 0 ].get_user_hash(), "UTF-8" );
                userscore       += URLEncoder.encode( String.format( "%d", permanentData[ 0 ].get_user_score() ), "UTF-8" );
                usertotalgames  += URLEncoder.encode( String.format( "%d", permanentData[ 0 ].get_user_totalgames() ), "UTF-8" );
                userwongames    += URLEncoder.encode( String.format( "%d", permanentData[ 0 ].get_user_wongames() ), "UTF-8" );
            } catch ( UnsupportedEncodingException ex ) {
                // teoricamente non dovrebbe mai essere lanciata un'eccezione del genere poiche'
                // qualunque versione di android supporta l'UTF-8
            }

            // lo script si trova in www.plmtmobile.altervista.org/scala40/updateaccount.php
            String  basepath    = "http://www.plmtmobile.altervista.org/scala40/v2/updateaccount.php?";
            String  params      = "uname="+username+"&uhash="+userhash+"&uscore="+userscore+"&utotalgames="+usertotalgames+"&uwongames="+userwongames;
            String  finalURL    = basepath + params;
            URL url = new URL( finalURL );

            try {
                // connessione al server
                URLConnection connection = url.openConnection();
                connection.connect();
                // lettura risposta
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String  inputLine, firstline = "";
                int     linecount = 0;
                while((inputLine = in.readLine()) != null) {
                    linecount += 1;
                    if( linecount == 1 ) {
                        firstline = inputLine;
                    }
                }
                in.close();

                // verifica risposta ("OK")
                if( ( linecount == 1 ) && ( firstline.compareTo( "OK" ) == 0 ) ) {
                    result = 1;
                }

            } catch ( IOException ex ) {

            }
        } catch ( MalformedURLException ex ) {
            //Log.v ( "CONNECT", ex.getMessage() );
        }
        return result;
    }

    protected void onPostExecute(Long result) {
        if( verbose ) {
            if( pDialog.isShowing() ) {
                pDialog.dismiss();
            }
            if( result > 0 ) {
                Toast toast = Toast.makeText( mContext, "Nome aggiornato correttamente", Toast.LENGTH_SHORT );
                toast.show();
            } else {
                Toast toast = Toast.makeText( mContext, "Errore o connessione assente", Toast.LENGTH_SHORT );
                toast.show();
            }
        }
    }

}
