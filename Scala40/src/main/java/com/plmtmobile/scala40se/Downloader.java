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
import java.util.ArrayList;

/**
 * Created by daniele on 03/11/13.
 */
public class Downloader extends AsyncTask<ArrayList<RankListItem>,Void,ArrayList<RankListItem>> {
    private ProgressDialog  pDialog;
    private Context         mContext;
    ArrayList<RankListItem> targetlist;
    CustomAdapter           adapter;
    private boolean         connection_success = false;

    Downloader( Context context, ArrayList<RankListItem> targetlist, CustomAdapter adapter ) {
        mContext            = context;
        this.targetlist     = targetlist;
        this.adapter        = adapter;
        connection_success  = false;
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog( mContext );
        pDialog.setMessage( "Recupero classifica..." );
        pDialog.setIndeterminate( true );
        pDialog.show();
    }

    protected ArrayList<RankListItem> doInBackground( ArrayList<RankListItem>... lists) {
        ArrayList<RankListItem> tmplist = new ArrayList<RankListItem>();
        tmplist.clear();

        try {
            // lo script si trova in www.plmtmobile.altervista.org/scala40/v2/fullrankapp.php
            String  basepath    = "http://www.plmtmobile.altervista.org/scala40/v2/fullrankapp.php";
            String  params      = "";
            String  finalURL    = basepath + params;
            URL url = new URL( finalURL );

            try {
                connection_success = false;
                // connessione al server
                URLConnection connection = url.openConnection();
                connection.connect();


                // lettura risposta
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String  totalitemsstring;
                if( ( totalitemsstring = in.readLine() ) != null ) {
                    connection_success = true;
                    // totalitems contiene il numero (in stringa) dei record trovati
                    int totalitems = Integer.parseInt( totalitemsstring );

                    if( totalitems > 0 ) {
                        String username;
                        String score;
                        String totalgames;
                        String wongames;
                        String medals;
                        do {
                            username = in.readLine();
                            if( username == null ) {
                                // fine lista ?
                                // TODO controllare con totalitem
                                break;
                            }
                            score = in.readLine();
                            if( score == null ) {
                                // errore!
                                break;
                            }
                            wongames = in.readLine();
                            if( wongames == null ) {
                                // errore!
                                break;
                            }
                            totalgames = in.readLine();
                            if( totalgames == null ) {
                                // errore!
                                break;
                            }
                            medals = in.readLine();
                            if( medals == null ) {
                                // errore!
                                break;
                            }
                            // aggiungere il record alla lista temporanea
                            tmplist.add( new RankListItem( username, Integer.parseInt( score ), Integer.parseInt( totalgames ), Integer.parseInt( wongames ), Integer.parseInt( medals ) ) );
                        } while( true );
                    }
                }
                in.close();
            } catch ( IOException ex ) {

            }
        } catch ( MalformedURLException ex ) {
            //Log.v ( "CONNECT", ex.getMessage() );
        }
        return tmplist;
    }

    protected void onPostExecute(ArrayList<RankListItem> result) {
        if( pDialog.isShowing() ) {
            pDialog.dismiss();
        }

        for( int i = 0; i < result.size(); i++ ) {
            targetlist.add( result.get( i ) );
        }
        adapter.notifyDataSetChanged();

        if( connection_success == false ) {
            Toast toast = Toast.makeText( mContext, "Errore o connessione assente", Toast.LENGTH_SHORT );
            toast.show();
        } else {
            if( targetlist.size() == 0 ) {
                Toast toast = Toast.makeText( mContext, "Classifica vuota", Toast.LENGTH_SHORT );
                toast.show();
            }
        }
    }

}
