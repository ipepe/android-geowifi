package pl.ipepe.android.geowifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by Patryk on 31.08.2016.
 */
public class HttpPostTask extends AsyncTask<Object, Void, Integer> {
    private final String deviceId;
    private List<WifiObservation> wifiObservationList;
    private Context context;
    private String url;
    public HttpPostTask(String url, Context context, String deviceId){
        super();
        this.url = url;
        this.deviceId = deviceId;
        this.context = context;
    }
    protected Integer doInBackground(Object... params) {
        JSONArray array = new JSONArray();
        wifiObservationList = new Select().
                from(WifiObservation.class).
                where("is_exported = ?", false).
                limit(3000).
                execute();
        for (WifiObservation wo: wifiObservationList) {
            array.put(wo.toJson());
        }
        JSONObject exportedJson = new JSONObject();
        try{
            exportedJson.
                    accumulate("wifi_observation_receiver", new JSONObject().
                            accumulate("wifi_observations", array).
                            accumulate("meta", new JSONObject().
                                    accumulate("source", "android_"+this.deviceId)));
        }catch(JSONException ex){
            return null;
        }

        try {
            return HttpRequest.
                    post(this.url).
                    contentType(HttpRequest.CONTENT_TYPE_JSON).
                    send(exportedJson.toString()).
                    code();
        } catch (HttpRequest.HttpRequestException exception) {
            return 0;
        }
    }

    protected void onPostExecute(Integer http_code) {
        if(http_code == 204){
            Toast.makeText(context, String.format(context.getString(R.string.export_successful), wifiObservationList.size()), Toast.LENGTH_LONG).show();
            ActiveAndroid.beginTransaction();
            try {
                for (WifiObservation wifiObservation: wifiObservationList) {
                    wifiObservation.is_exported = true;
                    wifiObservation.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        } else {
            Toast.makeText(context, String.format(context.getString(R.string.export_unsuccessful), http_code), Toast.LENGTH_LONG).show();
        }
        wifiObservationList=null;
        Log.i("HttpPostResult",http_code.toString());
    }
}