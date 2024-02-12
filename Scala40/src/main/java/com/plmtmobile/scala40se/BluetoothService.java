package com.plmtmobile.scala40se;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by daniele on 20/11/13.
 */
public class BluetoothService {

    private static BluetoothService mInstance = null;

    public static class ConnectionData {
        String      deviceName;
        String      deviceAddress;
        boolean     active;

        ConnectionData( String name, String address, boolean active ) {
            this.deviceName      = name;
            this.deviceAddress   = address;
            this.active          = active;
        }
    }

    /*
        tag per passaggio valori tra l'activity menu e l'activity di gioco
    */
    public static final String          INTENTEXTRA_BT_DEVICEADDRESS    = "INTENTEXTRA_BT_DEVICEADDRESS";
    public static final String          INTENTEXTRA_BT_ENABLED          = "INTENTEXTRA_BT_ENABLED";
    public static final String          INTENTEXTRA_BT_ACTIVE           = "INTENTEXTRA_BT_ACTIVE";


    /*
        definizione messaggi per l' handler
    */
    public static final int             MSG_BT_STOPPED              = 1;
    public static final int             MSG_BT_LISTENING            = 2;
    public static final int             MSG_BT_CONNECTING           = 3;
    public static final int             MSG_BT_CONNECTED            = 4;
    public static final int             MSG_BT_CONNECTION_FAILED    = 5;
    public static final int             MSG_BT_CONNECTION_LOST      = 6;
    public static final int             MSG_BT_EXCEPTION            = 7;
    public static final int             MSG_BT_NEW_DEVICE_FOUND     = 8;
    public static final int             MSG_BT_DISCOVERY_FINISHED   = 9;
    public static final int             MSG_BT_READ_DATA            = 10;


    /*
        definizione stati bluetooth
    */
    public static final int             BTSTATUS_STOPPED            = 0;    // we're doing nothing
    public static final int             BTSTATUS_LISTENING          = 1;    // now listening for incoming connections
    public static final int             BTSTATUS_CONNECTING         = 2;    // now initiating an outgoing connection
    public static final int             BTSTATUS_CONNECTED          = 3;    // now connected to a remote device

    // parametri socket : stringa e UUID unico per l'applicazione
    private static final String         NAME_SECURE     = "BluetoothCOMSecure";
    private static final UUID           MY_UUID_SECURE  = UUID.fromString( "fa87c0d0-afac-11de-8a39-0800200c9a66" );
    //private UUID                        MY_UUID_SECURE;

    private BluetoothAdapter            mAdapter;                       // se diverso da null = bluetooth supportato
    private int                         btstatus;                       // stato corrente servizio bluetooth
    private Handler                     mHandler;                       // handler

    private AcceptThread                mAcceptThread;                  // thread per attesa connessioni
    private ConnectThread               mConnectThread;                 // thread per tentativo connessione
    private CommThread                  mCommThread;                    // thread per mantenimento connessione

    private static boolean              createdinstance;

    /*
        creazione istanza servizio
    */
    BluetoothService() {
    }

    public static BluetoothService getInstance() {
        if( mInstance == null ) {
            mInstance       = new BluetoothService();
            createdinstance = false;
        }
        return mInstance;
    }

    /*
        imposta l'handler a cui comunicare gli eventi
    */
    public void setHandler( Handler handler ) {
        mHandler = handler;
    }

    /*
        flag istanza creata
    */
    public boolean isCreatedinstance() {
        return createdinstance;
    }

    /*
        registrazione per gli eventi di broadcast inerenti al discovery
    */
    public void create() {
        btstatus    = BTSTATUS_STOPPED;
        mAdapter    = BluetoothAdapter.getDefaultAdapter();
        createdinstance = true;
    }

    /*
        cancella i thread e i socket
    */
    public void delete() {
        // chiude tutti i socket e cancella i thread
        stop();
    }

    /*
        impostazione stato corrente del servizio
    */
    private synchronized void set_status( int new_status ) {
        btstatus = new_status;
    }

    /*
        ritorna lo stato corrente del servizio
    */
    public synchronized int get_status() {
        return btstatus;
    }

    /*
        avvio del servizio: nel dettaglio crea un AcceptThread per l'ascolto di nuove connessioni
        Called by the Activity onResume()
    */
    public synchronized boolean start() {
        boolean error = false;
        // cancella ogni thread che tenta di eseguire una connessione
        if( mConnectThread != null ) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }
        // cancella ogni thread che gestisce una connessione
        if( mCommThread != null ) {
            mCommThread.cancel();
            mCommThread.interrupt();
            mCommThread = null;
        }
        // avvia il thread in ascolto di un BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
            error = mAcceptThread.getSocketError();
        }
        // imposta lo stato corrente (in ascolto)
        set_status( BTSTATUS_LISTENING );
        // segnala all'handler il cambio di stato
        if( mHandler != null ) {
            mHandler.obtainMessage( MSG_BT_LISTENING ).sendToTarget();
        }
        return error;
    }

    /*
    cancella tutti i thread attivi
    */
    public synchronized void stop() {
        // cancella un eventuale thread per nuove connessioni
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }
        // cancella un eventuale thread per la gestione della comunicazione
        if (mCommThread != null) {
            mCommThread.cancel();
            mCommThread.interrupt();
            mCommThread = null;
        }
        // cancella un eventuale thread per l'ascolto di nuove connessioni
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        // imposta lo stato corrente (servizio fermo)
        set_status( BTSTATUS_STOPPED );
        // segnala all'handler il cambio di stato
        if( mHandler != null ) {
            mHandler.obtainMessage( MSG_BT_STOPPED ).sendToTarget();
        }
    }



    /*
        crea il thread per iniziare una nuova connessione
    */
    public synchronized void connect( BluetoothDevice device ) {
        // cancella un eventuale altro thread che sta cercando di eseguire una nuova connessione
        if( btstatus == BTSTATUS_CONNECTING ) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread.interrupt();
                mConnectThread = null;
            }
        }
        // cancella un eventuale altro thread che gestisce una connessione
        if (mCommThread != null) {
            mCommThread.cancel();
            mCommThread.interrupt();
            mCommThread = null;
        }
        // avvia il thread per tentare una nuova connessione con il dispositivo
        mConnectThread = new ConnectThread( device );
        mConnectThread.start();
        // imposta lo stato corrente (connessione in corso)
        set_status( BTSTATUS_CONNECTING );
        // segnala all'handler il cambio di stato
        if( mHandler != null ) {
            mHandler.obtainMessage( MSG_BT_CONNECTING ).sendToTarget();
        }
    }

    /*
        avvia il thread che si occupa di gestire la comunciazione (lettura e scrittura)
        il parametro active indica se la connessione e' partita da questo dispositivo (active)
        o e' partita dall'esterno
     */
    public synchronized void connected( BluetoothSocket socket, BluetoothDevice device, boolean active ) {
        // cancella il thread che ha creato la connessione
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }
        // cancella un eventuale altro thread che attualmente gestisce una connessione
        if (mCommThread != null) {
            mCommThread.cancel();
            mCommThread.interrupt();
            mCommThread = null;
        }
        // cancella il thread di ascolto poiche' noi vogliamo connetterci solo a 1 dispositivo per volta
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        // avvia il thread che gestisce la connessione
        mCommThread = new CommThread( socket );
        mCommThread.start();

        // imposta lo stato corrente (connessione con altro dispositivo avvenuta con successo)
        set_status( BTSTATUS_CONNECTED );
        // segnala all'handler il cambio di stato
        if( mHandler != null ) {
            // insieme al messaggio vengono inviati i seguenti dati:
            // - nome del dispositivo
            // - MAC address del dispositivo
            // - connessione attiva (da tentativo di connessione) o passiva (da ascolto)
            ConnectionData data = new ConnectionData( device.getName(), device.getAddress(), active );
            mHandler.obtainMessage( MSG_BT_CONNECTED, data ).sendToTarget();
        }
    }

    /*
        Invio dati  attraverso il thead di comunicazione
    */
    public void sendmsg( byte[] out ) {
        // crea un oggetto temporaneo
        CommThread r;
        // copia sincronizzata del thread per la comunicazione
        synchronized (this) {
            if ( get_status() != BTSTATUS_CONNECTED )  return;
            r = mCommThread;
        }
        // scrittura asincrona
        r.write( out );
    }

    /*
        tentativo di connessione fallito
    */
    private void connectionFailed( BluetoothDevice device ) {
        if( mHandler != null ) {
            ConnectionData data = new ConnectionData( device.getName(), device.getAddress(), true );
            mHandler.obtainMessage( MSG_BT_CONNECTION_FAILED, data ).sendToTarget();
        }
        // riavvia il servizio in ascolto
        BluetoothService.this.start();
    }

    /*
        perdita della connessione
    */
    private void connectionLost() {
        if( mHandler != null ) {
            mHandler.obtainMessage( MSG_BT_CONNECTION_LOST ).sendToTarget();
        }
        // riavvia il servizio in ascolto
        BluetoothService.this.start();
    }

    /*
        comunica all'handler eventuali exception
    */
    private void signalException( String msg ) {
        if( mHandler != null ) {
            mHandler.obtainMessage( MSG_BT_EXCEPTION, msg ).sendToTarget();
        }
    }

    /*
        This thread runs while listening for incoming connections. It behaves
        like a server-side client. It runs until a connection is accepted
        (or until cancelled).
    */
    private class AcceptThread extends Thread {

        // server socket locale
        private final BluetoothServerSocket mmServerSocket;
        private boolean                     socketError = false;

        public AcceptThread() {

            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                socketError = false;
                tmp = mAdapter.listenUsingRfcommWithServiceRecord( NAME_SECURE, MY_UUID_SECURE );
            } catch (IOException e) {
                signalException( e.getMessage() );
            }
            if( tmp == null ) {
                socketError     = true;
                mmServerSocket  = null;
            } else {
                mmServerSocket = tmp;
            }
        }

        public boolean getSocketError() {
            return  socketError;
        }

        public void run() {

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (btstatus != BTSTATUS_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    if( mmServerSocket != null ) {
                        socket = mmServerSocket.accept();
                    }
                } catch (IOException e) {
                    // "Operation canceled"
                    // signalException( e.getMessage() );
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (btstatus) {
                            case BTSTATUS_LISTENING:
                            case BTSTATUS_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected( socket, socket.getRemoteDevice(), false );
                                break;
                            case BTSTATUS_STOPPED:
                            case BTSTATUS_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    signalException( e.getMessage() );
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                if( mmServerSocket != null ) {
                    mmServerSocket.close();
                }
            } catch (IOException e) {
                signalException( e.getMessage() );
            }
        }
    }


    /*
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord( MY_UUID_SECURE );
            } catch (IOException e) {
                signalException( e.getMessage() );
            }
            mmSocket = tmp;
        }

        public void run() {
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                if( mmSocket != null ) {
                    mmSocket.connect();
                }
            } catch (IOException e) {
                // Close the socket
                try {
                    if( mmSocket != null ) {
                        mmSocket.close();
                    }
                } catch (IOException e2) {
                }
                connectionFailed( mmDevice );
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            if( mmSocket != null ) {
                connected( mmSocket, mmDevice, true );
            }
        }

        public void cancel() {
            try {
                if( mmSocket != null ) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                signalException( e.getMessage() );
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class CommThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public CommThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                signalException( e.getMessage() );
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[]  buffer = new byte[ 2048 ];
            int     n;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    if( mmInStream != null ) {
                        // Read from the InputStream
                        n = mmInStream.read(buffer);
                        // segnala all'handler i dati ricevuti
                        if( n > 0 ) {
                            byte[] receiveddata = new byte[ n ];
                            for( int i = 0; i < n; i++ ) {
                                receiveddata[ i ] = buffer[ i ];
                            }
                            if( mHandler != null ) {
                                mHandler.obtainMessage( MSG_BT_READ_DATA, n, -1, receiveddata ).sendToTarget();
                            }
                        }
                    }
                } catch (IOException e) {

                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                if( mmOutStream != null ) {
                    mmOutStream.write(buffer);
                }
            } catch (IOException e) {
                signalException( e.getMessage() );
            }
        }

        public void cancel() {
            try {
                if( mmInStream != null ) {
                    mmInStream.close();
                }
                if( mmOutStream != null ) {
                    mmOutStream.close();
                }
                if( mmSocket != null ) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                signalException( e.getMessage() );
            }
        }
    }


}
