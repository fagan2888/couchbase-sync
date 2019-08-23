package com.infy.couchsync;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private MutableDocument mutableDoc;
    private DatabaseConfiguration config;
    private Database database;

    private char key = 'A';
    private char val = 'a';
    private int count = 0;
    private Replicator replicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initSync();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "DATA UPDATE: " + replicator.getConfig().getDocumentIDs());
                try {
                    mutableDoc = database.getDocument("5555").toMutable();
                    ;

//                mutableDoc.setString((char) (key + count) + "", (char) (val + count) + "");
//                count++;
//                try {
//                    database.save(mutableDoc);
//                } catch (CouchbaseLiteException e) {
//                    e.printStackTrace();
//                }
                    Log.i(TAG, "DATA UPDATE: " + mutableDoc.toMap());

//                replicator.start();
                }
                catch (Exception e){

                }
            }
        }, 1000, 5000);
    }

    private void initSync() throws CouchbaseLiteException, URISyntaxException {
        config = new DatabaseConfiguration(getApplicationContext());
        database = new Database("hackathon", config);
//        Document document = database.getDocument("6666");
//
//        if (document == null) {
//            mutableDoc = new MutableDocument("6666");
//            database.save(mutableDoc);
//        } else
//            mutableDoc = document.toMutable();

        Endpoint targetEndpoint = new URLEndpoint(new URI("ws://192.168.1.101:4984/hackathon"));
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(database, targetEndpoint);
        replConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
        replConfig.setAuthenticator(new BasicAuthenticator("esquido", "esquido"));
        replConfig.setPullFilter((document, flags) -> document.getId().equals("6666"));
//        replConfig.setDocumentIDs(Arrays.asList("4444","6666","17c3ea19-661f-4758-82b0-59fb196609ac"));
        replConfig.setContinuous(true);



        replicator = new Replicator(replConfig);



        replicator.addChangeListener(change -> {
            Log.i(TAG, "DBCHANGE SYNC");
            if (change.getStatus().getError() != null) {
                Log.i(TAG, "Error code ::  " + change.getStatus().getError().getCode());
            }
        });

        replicator.start();


    }
}
