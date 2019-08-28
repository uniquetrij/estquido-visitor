package com.infy.stg.estquido.visitor.app;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import com.infy.stg.estquido.visitor.app.services.CBLService;

import java.util.concurrent.atomic.AtomicReference;


public class This {

    public static final AtomicReference<Context> CONTEXT = new AtomicReference<>();
    public static final AtomicReference<Application> APPLICATION = new AtomicReference<>();

    public static final AtomicReference<String> GPS_CENTER = new AtomicReference<>();
    public static final AtomicReference<Location> LOCATION = new AtomicReference<>();
    public static final AtomicReference<String> BUILDING = new AtomicReference<>();

    public static final AtomicReference<CBLService> CBL_DATABASE = new AtomicReference<>();

    public static class Static {
        public static final String COUCHBASE_DATABASE_URL = "ws://192.168.1.101:4984/estquido";
        public static final String COUCHBASE_CENTERS_URL = "ws://192.168.1.101:4986/estquido";
        public static final String COUCHBASE_DATABASE = "estquido";
        public static final String COUCHBASE_CENTERS = "estquido-centers";
        public static final String COUCHBASE_USER = "estquido";
        public static final String COUCHBASE_PASS = "estquido";
        public static final String QUERY_CENTER_URL = "http://192.168.1.101:8094/api/index/estquido-centers-geo-search/query";
    }
}
