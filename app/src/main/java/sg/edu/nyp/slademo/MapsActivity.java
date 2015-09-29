package sg.edu.nyp.slademo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;


public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SensorEventListener {

    public static String uid;
    public static double latitude, longitude;
    public static float numOfSteps;

    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;

    private PendingIntent pendingIntent;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    GoogleApiClient mGoogleApiClient;


    public static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uid = telephonyManager.getDeviceId();

        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        Intent alarmIntent = new Intent(MapsActivity.this, AlarmReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, alarmIntent, 0);

        mSensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        //Handle item selection

        switch (item.getItemId()){
            case R.id.startTrack10sec:
                startTracking10sec();
                return true;
            case R.id.startTrack:
                startTracking();
                return true;
            case R.id.stopTrack:
                stopTracking();
                return true;
            case R.id.checkSteps:
                checkStep();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void checkStep(){

        Toast.makeText(this, "Your current amount of steps is : " + numOfSteps, Toast.LENGTH_SHORT ).show();
    }

    public void startTracking10sec(){

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Tracking Started, Updating to DynamoDB in every " + interval + " ms", Toast.LENGTH_LONG).show();
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);


    }

    public void startTracking(){

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = (10000 * 6)*5 ;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Tracking Started, Updating to DynamoDB in every " + interval + " ms", Toast.LENGTH_LONG).show();
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stopTracking(){

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this,"Tracking Stopped!", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location Services connected!");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            //
        } else {
            addNewLocation(location);
        }

    }

    private long currentTime() {
        Calendar c = Calendar.getInstance();

        int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Log.w("TIME:", String.valueOf(currentTime));
        return (currentTime);
    }


    private void addNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        latitude = lat;
        longitude = lng;
        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Marker" + currentTime());
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

    }


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }
    };


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location Services suspended!");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location change to " + location.toString());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "Status of " + provider + " change to " + status);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, provider + " enabled!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider + " disabled!");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed : " + connectionResult.toString());

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0){
            value = (int) values[0];
        }

        if (sensor.getType()==Sensor.TYPE_STEP_COUNTER){
            numOfSteps = value;
        }

        else if (sensor.getType()==Sensor.TYPE_STEP_DETECTOR){
            // do something also.
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
