package com.plmtmobile.scala40se;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by daniele on 21/09/16.
 */
public class GetOnlinePlayers extends AsyncTask<Integer, Integer, Integer> {

    private Context mContext;

    GetOnlinePlayers( Context context ) {
        mContext = context;
    }

    protected void onPreExecute() {
        // nessuna azione da eseguire prima del tentativo di connessione
    }

    @Override
    protected Integer doInBackground(Integer... integers) {

        // il numero totale di giocatori online e' 1 per default (l'utente che visualizza e' connesso)
        int total_online_players = 1;

        try {
            // percorso dello script php che ritorna il numero di giocatori online
            String  basepath    = "http://www.plmtmobile.altervista.org/scala40/online/onlineplayers.php";
            String  params      = "";
            String  finalURL    = basepath + params;
            URL url = new URL( finalURL );

            try {
                // connessione al server
                URLConnection connection = url.openConnection();
                connection.connect();

                // lettura risposta
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String  response;
                if( ( response = in.readLine() ) != null ) {
                    try {
                        // totalitems contiene il numero (in formato testo) di giocatori online
                        total_online_players = Integer.parseInt( response );
                        if( total_online_players <= 0 ) {
                            total_online_players = 1;
                        }
                    } catch ( NumberFormatException e ) {
                        total_online_players = 1;
                    }
                }
                in.close();
            } catch ( IOException ex ) {

            }
        } catch ( MalformedURLException ex ) {
            // nessuna azione
        }

        return total_online_players;
    }


    protected void onPostExecute(Integer result) {
        ((TextView)((MPActivity)mContext).findViewById( R.id.tvOnlinePlayers )).setText( "Giocatori online : " + result.toString() );
    }

}