package com.mauricelam.transit;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.mauricelam.moreviews.VerticalLoader;
import com.mauricelam.transit.stopdatabase.StopDatabase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * User: mauricelam
 * Date: 15/5/13
 * Time: 10:37 AM
 */

public class PlacesMap2 extends FragmentActivity implements LocationListModel.LocationListDelegate {

    private static final String TAG = "Transit PlacesMap";
    private final int DEFAULTZOOM = 17;

    private String query;
    private Map<String, Stop> stopList;
    private BitmapDescriptor stopMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.placesmap2);

        SupportMapFragment mapFragment = getMapView();

        if (savedInstanceState == null) {
            mapFragment.setRetainInstance(true);
            GoogleMap map = getMap();
            if (map == null) {
                this.finish();
                return;
            }
            map.setMyLocationEnabled(true);
            UiSettings ui = map.getUiSettings();
            ui.setAllGesturesEnabled(true);
            ui.setMyLocationButtonEnabled(true);

            BitmapDrawable stop = (BitmapDrawable) this.getResources().getDrawable(R.drawable.icon_stop);
            stopMarker = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(stop.getBitmap(), 50, 50, true));
            stopList = new HashMap<String, Stop>();

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    String id = marker.getId();
                    Stop stop = stopList.get(id);
                    if (stop != null) {
                        selectStop(stop);
                    }
                }
            });

            Intent intent = getIntent();
            query = intent.getStringExtra("query");
            int lat = intent.getIntExtra("latitude", 0);
            int lng = intent.getIntExtra("longitude", 0);
            setCenterPlace(map, intent, lat, lng);

            if (intent.getBooleanExtra("mylocation", false)) {
                // move the camera to my location
                moveToMyLocation();
            }

            LocationListModel model = new LocationListModel(this);
            model.getAllStops();
            setLoading(true);
        }

        setTitle();
    }

    private GoogleMap getMap() throws NullPointerException {
        try{
            return getMapView().getMap();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void moveToMyLocation () {
        final GoogleMap map = getMap();
        if (map == null) {
            return;
        }

        Location myLocation = map.getMyLocation();
        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULTZOOM));
        } else {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULTZOOM));
                    }
                }
            });
            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    map.setOnMyLocationChangeListener(null);
                }
            });
        }
    }

    // For back button to call
    public void back(View view) {
        this.finish();
    }

    @Override
    public void resultsReady(final Stop[] stops) {
        final GoogleMap map = getMap();
        if (map == null) {
            return;
        }

        String title = getIntent().getStringExtra("bigText");
        LatLng camera = map.getCameraPosition().target;
        include.GeoPoint cameraLocation = new include.GeoPoint(camera.latitude, camera.longitude);
        Cursor cursor = StopDatabase.sharedInstance().getAllStopsByDistance(cameraLocation);
        final Queue< Pair<MarkerOptions, Stop> > pairs = new LinkedList< Pair<MarkerOptions, Stop> >();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            final Stop stop = StopDatabase.getStop(cursor);
            if (stop != null && !stop.getName().equals(title)) {
                MarkerOptions item = new MarkerOptions();
                include.GeoPoint location = stop.getLocation();
                item.position(new LatLng(location.getLatitudeE6() / 1e6, location.getLongitudeE6() / 1e6));
                item.title(stop.getName());
                item.snippet("MTD " + stop.getCodeString());
                item.icon(stopMarker);
                item.anchor(0.5f, 0.5f);
                pairs.offer(new Pair<MarkerOptions, Stop>(item, stop));
            }
        }
        cursor.close();

        final Handler handler = new Handler();
        final Runnable dequeue = new Runnable() {
            @Override
            public void run() {
                Pair<MarkerOptions, Stop> p = pairs.poll();
                if (p != null) {
                    Marker marker = map.addMarker(p.first);
                    stopList.put(marker.getId(), p.second);
                    handler.postDelayed(this, 10);
                }
            }
        };
        dequeue.run();

        setLoading(false);
    }

    private void setLoading(boolean loading) {
        VerticalLoader loader = (VerticalLoader) findViewById(R.id.ProgressBar01);
        if (loading) {
            loader.setVisibility(View.VISIBLE);
        } else {
            loader.setVisibility(View.GONE);
        }
    }

    private SupportMapFragment getMapView() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private void setTitle() {
        String bigText = getIntent().getStringExtra("bigText");
        TextView title = (TextView) findViewById(R.id.title);
        if (bigText != null && bigText.length() > 0) {
            title.setText(bigText);
        }
    }

    private void setCenterPlace(GoogleMap map, Intent intent, int lat, int lng) {
        MarkerOptions marker = new MarkerOptions();
        String source = intent.getStringExtra("source");
        Log.d(TAG, "Source: " + source);
        if (source.equals("placesSearch")) {
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
            marker.anchor(0.5f, 0.0f);
            // TODO: shadow
        } else if (source.equals("staticMaps")) {
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_stop));
            marker.anchor(0.5f, 0.5f);
        } else {
            Log.e(TAG, "Unrecognized source");
            return;
        }

        String bigText = intent.getStringExtra("bigText");
        String smallText = intent.getStringExtra("smallText");
        if (!intent.getBooleanExtra("mylocation", false)) {
            marker.position(new LatLng(lat / 1e6, lng / 1e6));
            marker.title(bigText);
            marker.snippet(smallText);
            map.addMarker(marker);
        } // don't draw flag if showing my location

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat / 1e6, lng / 1e6), DEFAULTZOOM));
    }

    private void selectStop(Stop stop) {
        selectStop(stop.getName(), stop.getCode());
    }

    private void selectStop(String name, int code) {
        String routeName = name + " (MTD" + String.format("%04d", code) + ")";
        String suggestionType = "- Nearby stop";
        Suggester.saveRecentQuery(getApplicationContext(), routeName, suggestionType);

        Intent displayStop = new Intent(this, Cards.class);
        displayStop.putExtra("stopName", name);
        displayStop.putExtra("stopCode", code);
        displayStop.putExtra("referrer", query);
        startActivity(displayStop);
    }

}
