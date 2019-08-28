package com.infy.stg.estquido.visitor.app.services;//package com.infy.stg.estquido.admin.app.services;
//
//import android.location.Location;
//import android.util.Base64;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.fasterxml.jackson.core.JsonParser;
//import com.infy.stg.estquido.admin.app.This;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class VolleyRequest {
//
//    public static String inferCenters(Location location) {
//
//        try {
//            RequestQueue requestQueue = Volley.newRequestQueue(This.CONTEXT.get());
//
//            StringRequest stringRequest = new StringRequest(Request.Method.POST, This.Static.QUERY_CENTER_URL,
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            Log.d("Volley", "response = " + response);
//                            Toast.makeText(This.CONTEXT.get(), response, Toast.LENGTH_LONG).show();
//
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d("Volley", "Error = " + error);
//                    Toast.makeText(This.CONTEXT.get(), error.toString(), Toast.LENGTH_LONG).show();
//
//                }
//            }) {
//                //
//                @Override
//                public Map<String, String> getHeaders() {
//                    HashMap<String, String> headers = new HashMap<>();
//                    headers.put("Accept", "application/json");
//                    headers.put("Content-Type", "application/json");
//                    String credentials = "estquido:estquido";
//                    String auth = "Basic "
//                            + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                    headers.put("Accept", "application/json");
//                    headers.put("Content-Type", "application/json");
//                    headers.put("Authorization", auth);
//                    return headers;
//                }
//
//                ////
//                @Override
//                public Map<String, String> getParams() {
//                    Map<String, String> params = new HashMap<>();
//
//                    JSONObject obj = new JSONObject();
//                    try {
//                        obj.put("from",0);
//                        obj.put("from",0);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//
//
//                    GeoSpatialRequest geoSpatialRequest = new GeoSpatialRequest();
//                    Query query = new Query();
//
//                    com.infy.estquido.app.model.Location loc = new com.infy.estquido.app.model.Location();
//                    loc.setLat((float) location.getLatitude());
//                    loc.setLon((float) location.getLongitude());
//
//                    query.setLocation(loc);
//                    query.setDistance("200mi");
//                    query.setField("geo");
//
//                    geoSpatialRequest.setFrom(0);
//                    geoSpatialRequest.setSize(1);
//                    geoSpatialRequest.setQuery(query);
//
//                    params = geoSpatialRequest.toMap();
//                    Log.d("volley", params.toString());
//
//                    return params; //return the parameters
//                }
//            };
//
//            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
//                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//            //Add the request to the RequestQueue.
//            requestQueue.add(stringRequest);
//            requestQueue.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "bangalore";
//    }
//}
