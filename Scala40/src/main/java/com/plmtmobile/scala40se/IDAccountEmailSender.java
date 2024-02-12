package com.plmtmobile.scala40se;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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
 * Created by daniele on 28/07/14.
 */
public class IDAccountEmailSender extends AsyncTask<Void, Void, Long> {
    private ProgressDialog  pDialog;
    private Context         mContext;
    private String          umail;
    private String          uhash;

    IDAccountEmailSender( Context context, String usermail, String userhash ) {
        mContext    = context;
        try {
            umail           = URLEncoder.encode( usermail, "UTF-8");
            uhash           = URLEncoder.encode( userhash, "UTF-8" );
        } catch ( UnsupportedEncodingException ex ) {
            // teoricamente non dovrebbe mai essere lanciata un'eccezione del genere poiche'
            // qualunque versione di android supporta l'UTF-8
        }
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog( mContext );
        pDialog.setMessage( "Richiesta invio email..." );
        pDialog.setIndeterminate( true );
        pDialog.show();
    }

    protected Long doInBackground(Void... value) {
        long    result      = -1;

        try {
            String  basepath    = "http://www.plmtmobile.altervista.org/scala40/v2/idaccsendmail.php?";
            String  params      = "umail="+umail+"&uhash="+uhash;
            String  finalURL    = basepath + params;
            URL url = new URL( finalURL );

            try {
                // connessione al server
                URLConnection connection = url.openConnection();
                connection.connect();
                // lettura risposta
                BufferedReader  in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String          response = in.readLine();

                if( response != null ) {
                    if( response.compareTo( "1" ) == 0 ) {
                        result = 1;
                    } else {
                        result = 0;
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
        } else {
            if( result == 1 ) {
                Toast toast = Toast.makeText( mContext, "OK! Richiesta invio email accettata", Toast.LENGTH_LONG );
                toast.show();
            } else {
                Toast toast = Toast.makeText( mContext, "Richiesta rifiutata. Riprova piu' tardi.", Toast.LENGTH_SHORT );
                toast.show();
            }
        }
    }

}
