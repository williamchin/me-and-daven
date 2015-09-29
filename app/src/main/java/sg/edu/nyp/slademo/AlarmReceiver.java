package sg.edu.nyp.slademo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by L30912 on 22/09/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static double lat, lng;
    public static String uid;

    @Override
    public void onReceive(Context context, Intent intent) {

        MapsActivity activity = new MapsActivity();

         lat = activity.latitude;
         lng = activity.longitude;
         uid = activity.uid;


        LatLng latLng = new LatLng(lat, lng);
        new UpdateDynamoDBTask().execute(latLng);

        // For our recurring task, we'll just display a message
        Toast.makeText(context, lat + ", "  + lng, Toast.LENGTH_SHORT).show();
        Log.d("TAG", lat + ","  + lng);
    }

    public void updateLocation(LatLng latlng) {
/*
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:8d0c5c62-b62b-4c6e-b4d8-c4a83a7169d5", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
*/
        AWSCredentials credentialsProvider = new BasicAWSCredentials("AKIAJ2DCKYDQZ6NO27EA", "Hg7WvNut5R6DBFJ4AleSLqHZ3IGZQrnVIgjg/s0X");
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        ddbClient.setRegion(com.amazonaws.regions.Region.getRegion(Regions.AP_SOUTHEAST_1));
        TrackData data = new TrackData();
        data.setId(uid);
        data.setLat(latlng.latitude);
        data.setLng(latlng.longitude);
        data.setTimeStamp(System.currentTimeMillis());
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        mapper.save(data);
    }

    private class UpdateDynamoDBTask extends AsyncTask<LatLng, Void, String> {
        protected String doInBackground(LatLng... latlng) {
            updateLocation(latlng[0]);
            return "";
        }
    }
}
