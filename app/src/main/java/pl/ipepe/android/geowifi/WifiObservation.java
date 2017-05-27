package pl.ipepe.android.geowifi;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

@Table(name = "wifi_observations")
public class WifiObservation extends Model {

    @Column(name = "ssid", index=true)
    public String ssid;

    @Column(name = "bssid", index=true)
    public String bssid;

    @Column(name = "signal_level")
    public int signal_level;

    @Column(name = "capabilities")
    public String capabilities;

    @Column(name = "observed_at")
    public Date observed_at;

    @Column(name = "channel_frequency")
    public int channel_frequency;

    @Column(name = "latitude")
    public double latitude;

    @Column(name = "longitude")
    public double longitude;

    @Column(name = "geolocated_at")
    public Date geolocated_at;

    @Column(name = "geolocation_accuracy")
    public float geolocation_accuracy;

    @Column(name = "is_exported")
    public boolean is_exported;

    public WifiObservation() {
        super();
    }

    public WifiObservation(ScanResult scanResult, Location location){
        super();
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.signal_level = scanResult.level;
        this.capabilities = scanResult.capabilities;
        this.channel_frequency = scanResult.frequency;

        this.observed_at = new Date();

        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.geolocated_at = new Date(location.getTime());
        this.geolocation_accuracy = location.getAccuracy();

        this.is_exported = false;
    }

    public JSONObject toJson(){
        try{
            return new JSONObject()
                    .accumulate("id", this.getId())
                    .accumulate("ssid", this.ssid)
                    .accumulate("bssid", this.bssid)
                    .accumulate("signal_level", this.signal_level)
                    .accumulate("capabilities",this.capabilities)
                    .accumulate("channel_frequency", this.channel_frequency)
                    .accumulate("observed_at", this.observed_at)
                    .accumulate("latitude",this.latitude)
                    .accumulate("longitude", this.longitude)
                    .accumulate("geolocated_at", this.geolocated_at)
                    .accumulate("geolocation_accuracy", this.geolocation_accuracy);
        }catch(JSONException ex){
            return null;
        }
    }

    @Override
    public String toString(){
        return String.format("WIFI: %d %s %s", this.signal_level, this.ssid, this.bssid);
    }

    public static int count(){
        return new Select().from(WifiObservation.class).execute().size();
    }
    public static void exportToServer(Context context, String url, String deviceId) {
        new HttpPostTask(url, context, deviceId).execute();
    }

    public static void resetIsExportedFlag(Context context) {
        new Update(WifiObservation.class).
                set("is_exported = ?", true).
                where("is_exported = ?", true).
                execute();
        Toast.makeText(context, context.getString(R.string.success_text), Toast.LENGTH_LONG).show();
    }

    public static void destroyDatabase(Context context) {
        new Delete().from(WifiObservation.class).execute();
        Toast.makeText(context, context.getString(R.string.success_text), Toast.LENGTH_LONG).show();
    }
}