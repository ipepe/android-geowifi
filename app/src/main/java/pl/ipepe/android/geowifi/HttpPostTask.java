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
    private List<WifiObservation> wifiObservationList;
    private Context context;
    private String url;
    public HttpPostTask(String url, Context context){
        super();
        this.url = url;
        this.context = context;
    }
    protected Integer doInBackground(Object... params) {
        JSONArray array = new JSONArray();
        wifiObservationList = new Select().from(WifiObservation.class).where("is_exported = ?", false).execute();
        for (WifiObservation wo: wifiObservationList) {
            array.put(wo.toJson());
        }
        JSONObject exportedJson = new JSONObject();
        try{
            exportedJson.
                    accumulate("wifi_observations_receiver", new JSONObject().
                            accumulate("wifi_observations", array).
                            accumulate("meta", new JSONObject().
                                    accumulate("device_id", UUID.randomUUID().toString())));
        }catch(JSONException ex){
            return null;
        }

        try {
            return HttpRequest.
                    post(this.url).
                    contentType(HttpRequest.CONTENT_TYPE_JSON).
                    send(array.toString()).
                    code();
        } catch (HttpRequest.HttpRequestException exception) {
            return 0;
        }
    }

    protected void onPostExecute(Integer http_code) {
        if(http_code == 200){
            Toast.makeText(context, "Exported "+wifiObservationList.size()+" WIFIs successfuly", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, "Export unsuccessful with code: " + http_code.toString(), Toast.LENGTH_LONG).show();
        }
        wifiObservationList=null;
        Log.i("HttpPostResult",http_code.toString());
    }
}