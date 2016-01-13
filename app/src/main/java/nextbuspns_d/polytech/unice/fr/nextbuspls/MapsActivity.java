package nextbuspns_d.polytech.unice.fr.nextbuspls;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String getBusUrl;
    private String getBusStopsUrl;
    private String getUserUrl;
    private GoogleMap mMap;
    private Button buttonGetBusLocation;
    private Button buttonStartTracking;
    private Button buttonGetBusStops;
    private Button buttonGetUser;
    private TextView textViewUrl;
    private Marker bus;
    private Marker user;
    private ArrayList stopList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getBusUrl = getResources().getString(R.string.bus_url);
        getBusStopsUrl = getResources().getString(R.string.busStops_url);
        getUserUrl = getResources().getString(R.string.user_url);

        buttonGetBusLocation = (Button) findViewById(R.id.button_getBusLocation);
        buttonGetBusStops = (Button) findViewById(R.id.button_getBusStop);
        buttonGetUser = (Button) findViewById(R.id.button_getUser);

        buttonStartTracking = (Button) findViewById(R.id.button_startTracking);

        textViewUrl = (TextView) findViewById(R.id.textView_url2);
        textViewUrl.setText(getBusUrl);

        buttonGetBusLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonGetBusLocation.setEnabled(false);
                new RESTClient(new RESTClient.AsyncResponse() {
                    @Override
                    public void processFinish(JSONObject json) {
                        try {
                            json = (JSONObject) json.get("geolocation");
                            if (!bus.isVisible()) {
                                bus.setVisible(true);
                                bus.setPosition(new LatLng((Double) json.get("latitude"), (Double) json.get("longitude")));
                            } else {
                                MarkerAnimation.animateMarker(bus, new LatLng((Double) json.get("latitude"), (Double) json.get("longitude")), new LatLngInterpolator.Spherical());
                            }
                            buttonGetBusLocation.setEnabled(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).execute(RequestMethod.GET, getBusUrl);
            }
        });

        buttonStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        buttonGetBusStops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonGetBusStops.setEnabled(false);
                new RESTClient(new RESTClient.AsyncResponse() {
                    @Override
                    public void processFinish(JSONObject json) {
                        //buttonGetBusStops.setEnabled(true);
                        JSONArray stops;
                        try {
                            stops = (JSONArray) json.get("stops");
                            if (stops != null) {
                                for (int i = 0; i < stops.length(); i++) {
                                    try {
                                        JSONObject stop = (JSONObject) stops.get(i);
                                        LatLng location = new LatLng(Double.parseDouble((String) stop.get("stop_lat")), Double.parseDouble((String) stop.get("stop_lon")));
                                        mMap.addMarker(new MarkerOptions().position(location)
                                                .title((String) stop.get("stop_name"))
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop_logo))
                                                .alpha(0.7f)
                                                .anchor(0.5f, 0.5f));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).execute(RequestMethod.GET, getBusStopsUrl);
            }
        });

        buttonGetUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonGetUser.setEnabled(false);
                new RESTClient(new RESTClient.AsyncResponse() {
                    @Override
                    public void processFinish(JSONObject json) {
                        buttonGetUser.setEnabled(true);

                        LatLng location = null;
                        try {
                            JSONArray usersInBus = (JSONArray) json.get("usersInBus");
                            for (int i = 0; i < usersInBus.length(); i++) {
                                JSONObject locationJson = (JSONObject) ((JSONObject) usersInBus.get(i)).get("geolocation");
                                location = new LatLng((Double) locationJson.get("latitude"), (Double) locationJson.get("longitude"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (location != null) {
                            user.setVisible(true);
                            MarkerAnimation.animateMarker(user, location, new LatLngInterpolator.Spherical());
                        } else {
                            user.setVisible(false);
                        }


                    }
                }).execute(RequestMethod.GET, getBusUrl);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        buttonGetBusLocation.setEnabled(true);
        LatLng polytech = new LatLng(43.617014, 7.074173);
        bus = mMap.addMarker(new MarkerOptions().position(polytech)
                .title("Le bus magique :)")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.logo_bus))
                .alpha(0.7f)
                .anchor(0.5f, 0.5f)
                .visible(false));
        user = mMap.addMarker(new MarkerOptions().position(polytech)
                .title("Le user magique :)")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.user_logo))
                .alpha(0.7f)
                .anchor(0.5f, 0.5f)
                .visible(false));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polytech, 14.0f));
    }
}