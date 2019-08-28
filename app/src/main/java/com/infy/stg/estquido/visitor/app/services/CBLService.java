package com.infy.stg.estquido.visitor.app.services;

import androidx.annotation.NonNull;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.infy.stg.estquido.visitor.app.This;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;


public class CBLService {

    private static final String TAG = CBLService.class.getName();

    public interface Callback {
        public void onError(ReplicatorChange change);

        public void onUpdate(ReplicatorChange change);
    }

    private String url;
    private String dbname;
    private String username;
    private String password;


    private static Replicator replicator;

    private final Database database;

    public CBLService(String url, String dbname, String username, String password) {
        this.url = url;
        this.dbname = dbname;
        this.username = username;
        this.password = password;

        Database database = null;
        try {
            DatabaseConfiguration dbConfig = new DatabaseConfiguration(This.CONTEXT.get());
            database = new Database(dbname, new DatabaseConfiguration(This.CONTEXT.get()));

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    private ReplicatorConfiguration createConfig(ReplicatorConfiguration.ReplicatorType type, boolean async) {
        ReplicatorConfiguration reConfig = null;
        try {
            reConfig = new ReplicatorConfiguration(database, new URLEndpoint(new URI(url)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        reConfig.setAuthenticator(new BasicAuthenticator(username, password));
        reConfig.setReplicatorType(type);
        reConfig.setContinuous(async);
        return reConfig;
    }


    public Replicator sync(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        return _sync(type, callback, documents);
//        ReplicatorChangeListener listener = new ReplicatorChangeListener() {
//            @Override
//            public void changed(@NonNull ReplicatorChange change) {
//                if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED)) {
//                    _sync(type, callback, documents);
//                }
//            }
//        };
//        try {
//            replicator.addChangeListener(listener);
//            replicator.stop();
//        } catch (NullPointerException e) {
//            _sync(type, callback, documents);
//        }
//        return replicator;
    }

    public Replicator _sync(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        ReplicatorConfiguration reConfig = createConfig(type, false);
        if (documents.length > 0)
            reConfig.setDocumentIDs(Arrays.asList(documents));
        replicator = new Replicator(reConfig);
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                callback.onError(change);
            } else {
                callback.onUpdate(change);
            }
        });
        replicator.start();
        return replicator;
    }


    public Replicator async(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        ReplicatorChangeListener listener = new ReplicatorChangeListener() {
            @Override
            public void changed(@NonNull ReplicatorChange change) {
                if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED)) {
                    _sync(type, new Callback() {
                        @Override
                        public void onError(ReplicatorChange change) {

                        }

                        @Override
                        public void onUpdate(ReplicatorChange change) {
                            if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED)) {
                                _async(type, callback, documents);
                            }
                        }
                    }, documents);
                }
            }
        };
        try {
            replicator.addChangeListener(listener);
            replicator.stop();
        } catch (NullPointerException e) {
            _async(type, callback, documents);
        }
        return replicator;
    }

    public Replicator _async(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        ReplicatorConfiguration reConfig = createConfig(type, true);
        if (documents.length > 0) {
            reConfig.setPullFilter((document, flags) -> Arrays.asList(documents).contains(document.getId()));
            reConfig.setPushFilter((document, flags) -> Arrays.asList(documents).contains(document.getId()));
        }
        replicator = new Replicator(reConfig);
        replicator.addChangeListener(ch -> {
            if (ch.getStatus().getError() != null) {
                callback.onError(ch);
            } else {
                callback.onUpdate(ch);
            }
        });
        replicator.start();
        return replicator;
    }
}
