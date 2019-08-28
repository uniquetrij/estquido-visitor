package com.infy.stg.estquido.visitor.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.infy.stg.estquido.visitor.R;
import com.infy.stg.estquido.visitor.app.This;
import com.infy.stg.estquido.visitor.app.services.CBLService;
import com.infy.stg.estquido.visitor.app.services.CBRestService;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final int QR_REQUEST_CODE = 0;


    private FusedLocationProviderClient mFusedLocationClient;
    private AutoCompleteTextView tvSpotName;
    private Map<String, Object> spots;
    private Button btnIndoorDirections;
    private Button btnOutdoorDirections;

    private Date ttl = new Date(Instant.now().plusSeconds(60 * 60 * 2).toEpochMilli());

    private Uri mGoogleMapsIntentURI;

    private int counter = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());

        tvSpotName = findViewById(R.id.tvSpotName);
        tvSpotName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                ((TextView) findViewById(R.id.textView)).setText("");
                return false;
            }
        });
        btnIndoorDirections = findViewById(R.id.btnIndoorDirections);
        btnOutdoorDirections = findViewById(R.id.btnOutdoorDirections);

        if (!haveNetworkConnection()) {
            Toast.makeText(getApplicationContext(), "Please enable internet", Toast.LENGTH_LONG).show();
            finishAffinity();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                    Log.d("LOCATION", "DEFAULT: " + location);
                    if (location != null) {
                        This.LOCATION.set(location);
                        new CBRestService().request(This.Static.QUERY_CENTER_URL, new CBRestService.Callback() {
                            @Override
                            public void onError(VolleyError error) {
                                This.GPS_CENTER.set(null);
                            }

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    This.GPS_CENTER.set(response.getJSONArray("hits").getJSONObject(0).getString("id").replace("center_", ""));
                                    Log.d(TAG, "CBL " + This.GPS_CENTER.get());

                                    if (This.CBL_DATABASE.get() == null) {
                                        This.CBL_DATABASE.set(new CBLService(This.Static.COUCHBASE_DATABASE_URL, This.Static.COUCHBASE_DATABASE, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
                                    }
                                    CBLService cblDatabase = This.CBL_DATABASE.get();
                                    cblDatabase.sync(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
                                        @Override
                                        public void onError(ReplicatorChange change) {

                                        }

                                        @Override
                                        public void onUpdate(ReplicatorChange change) {
                                            Log.d(TAG, "CBL " + change.getStatus().getProgress());
                                            try {
                                                Log.d(TAG, "CBL " + cblDatabase.getDatabase().getDocument("spots_" + This.GPS_CENTER.get()).toMutable().toMap());
                                                cblDatabase.getDatabase().setDocumentExpiration("spots_" + This.GPS_CENTER.get(), ttl);

                                                spots = cblDatabase.getDatabase().getDocument("spots_" + This.GPS_CENTER.get()).toMutable().toMap();

                                                String[] array = spots.keySet().toArray(new String[0]);
                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, array);
                                                tvSpotName.setAdapter(adapter);

                                            } catch (Exception ex) {
                                                Log.d(TAG, "CBL " + ex.toString());
                                            }
                                        }
                                    }, "spots_" + This.GPS_CENTER.get());


                                } catch (JSONException | ArrayIndexOutOfBoundsException e) {
                                    This.GPS_CENTER.set(null);
                                }
                            }
                        }, CBRestService.centerRequest(location));
                        timer.cancel();
                    } else {

                        if (counter-- <= 0) {
                            Toast.makeText(getApplicationContext(), "Please enable location & permission", Toast.LENGTH_SHORT).show();
                            counter = 10;
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public void btnOutdoorDirectionsOnClick(View view) {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mGoogleMapsIntentURI);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void btnIndoorDirectionsOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), QRScannerActivity.class);
        startActivityForResult(intent, QR_REQUEST_CODE);

//        Intent intent = new Intent(MainActivity.this, NavigatorActivity.class);
//        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                Log.d(TAG, "QR" + contents);
//                mReplicator.stop();
                Log.d(TAG, "REPLICATOR " + "STOP");
                Intent intent = new Intent(getApplicationContext(), NavigatorActivity.class);
                startActivity(intent);
            }
        }
        if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "QR" + "CANCELLED");
        }

    }

    public void btnSearchSpotOnCLick(View view) {
        Log.d(TAG, "CBL " + "CLICK");
        ((TextView) findViewById(R.id.textView)).setText("");
        String spot = tvSpotName.getText().toString().trim();
        if (spots.containsKey(spot)) {
            btnOutdoorDirections.setVisibility(View.VISIBLE);
            btnIndoorDirections.setVisibility(View.VISIBLE);
            String building = (String) ((Map<String, Object>) spots.get(spot)).get("building");
            Log.d(TAG, "CBL " + "building_" + This.GPS_CENTER.get() + "_" + building);
            This.BUILDING.set(building);
            ((TextView) findViewById(R.id.textView)).setText("Destination is in building " + building);
            if (This.CBL_DATABASE.get() == null) {
                This.CBL_DATABASE.set(new CBLService(This.Static.COUCHBASE_DATABASE_URL, This.Static.COUCHBASE_DATABASE, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
            }
            CBLService cblDatabase = This.CBL_DATABASE.get();
            cblDatabase.sync(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
                @Override
                public void onError(ReplicatorChange change) {

                }

                @Override
                public void onUpdate(ReplicatorChange change) {
                    Log.d(TAG, "CBL " + change.getStatus().getProgress());
                    try {
                        Log.d(TAG, "CBL " + cblDatabase.getDatabase().getDocument("building_" + This.GPS_CENTER.get() + "_" + building).toMutable().toMap());
                        cblDatabase.getDatabase().setDocumentExpiration("building_" + This.GPS_CENTER.get() + "_" + building, ttl);
                        Map<String, Object> map = cblDatabase.getDatabase().getDocument("building_" + This.GPS_CENTER.get() + "_" + building).toMutable().toMap();
                        ArrayList<Double> location = (ArrayList<Double>) map.get("location");
                        mGoogleMapsIntentURI = Uri.parse("geo:0,0?q=" + location.get(0) + ", " + location.get(1));
                    } catch (Exception ex) {
                        Log.d(TAG, "CBL " + ex.toString());
                    }
                }
            }, "building_" + This.GPS_CENTER.get() + "_" + building);
        } else {
            btnOutdoorDirections.setVisibility(View.GONE);
            btnIndoorDirections.setVisibility(View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
