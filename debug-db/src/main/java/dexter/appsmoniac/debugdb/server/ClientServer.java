package dexter.appsmoniac.debugdb.server;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class ClientServer implements Runnable {

    private static final String TAG = "ClientServer";
    private final int mPort;
    private final RequestHandler mRequestHandler;
    private boolean mIsRunning;
    private ServerSocket mServerSocket;

    public ClientServer(Context context, int port) {
        mRequestHandler = new RequestHandler(context);
        mPort = port;
    }

    public void start() {
        //being called from DebugDB initialize(which is called from DebugDBinitprovider which is a content provider, being
        //registered in manifest with <provider> tag
        mIsRunning = true;
        new Thread(this).start();
    }

    public void stop() {
        try {
            mIsRunning = false;
            if (null != mServerSocket) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing the server socket.", e);
        }
    }

    @Override
    public void run() {
        try {
            //The server instantiates a ServerSocket object, denoting which port number communication is to occur on.
            mServerSocket = new ServerSocket(mPort);
            while (mIsRunning) {

                //This will ensure that server is accepting requests and demanding requests
                //ServerSocket is at server side
                Socket socket = mServerSocket.accept();
                mRequestHandler.handle(socket);
                socket.close();
            }
        } catch (SocketException e) {

            //This may also occur, if the port number is booked for some other process
            Log.e(TAG, "Server has stopped");
        } catch (IOException e) {
            Log.e(TAG, "Server error.", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception.", e);
        }
    }

    //called from DebugDB class, and brings HashMap of databaseFiles from there
    public void setCustomDatabaseFiles(HashMap<String, Pair<File, String>> customDatabaseFiles) {
        mRequestHandler.setCustomDatabaseFiles(customDatabaseFiles);
    }

    public boolean isRunning() {
        return mIsRunning;
    }
}
