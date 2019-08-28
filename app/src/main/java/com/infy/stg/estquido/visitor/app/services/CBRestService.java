package com.infy.stg.estquido.visitor.app.services;

import android.location.Location;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.infy.stg.estquido.visitor.app.This;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CBRestService {

    public interface Callback {
        public void onError(VolleyError error);

        public void onResponse(JSONObject response);
    }

    private static final RequestQueue REQUEST_QUEUE;

    static {
        REQUEST_QUEUE = Volley.newRequestQueue(This.CONTEXT.get());
        REQUEST_QUEUE.start();
    }


    public void request(String url, Callback callback, JSONObject post) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, post, response -> {
            callback.onResponse(response);
        }, error -> {
            callback.onError(error);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Basic " + Base64.encodeToString("estquido:estquido".getBytes(), Base64.NO_WRAP));
                return headers;
            }
        };
        REQUEST_QUEUE.add(request);
    }

    public static JSONObject centerRequest(Location location) {

        try {
            return new JSONObject("{\n" +
                    "  \"from\": 0,\n" +
                    "  \"size\": 1,\n" +
                    "  \"query\": {\n" +
                    "    \"location\": {\n" +
                    "      \"lat\": " + location.getLatitude() + ",\n" +
                    "      \"lon\": " + location.getLongitude() + "\n" +
                    "     },\n" +
                    "      \"distance\": \"100000mi\",\n" +
                    "      \"field\": \"geo\"\n" +
                    "    },\n" +
                    "  \"sort\": [\n" +
                    "    {\n" +
                    "      \"by\": \"geo_distance\",\n" +
                    "      \"field\": \"geo\",\n" +
                    "      \"unit\": \"mi\",\n" +
                    "      \"location\": {\n" +
                    "      \"lat\": " + location.getLatitude() + ",\n" +
                    "      \"lon\": " + location.getLongitude() + "\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
