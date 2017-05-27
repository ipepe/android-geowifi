package pl.ipepe.android.geowifi;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import android.provider.Settings.Secure;

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

    @Column(name = "seen_at")
    public Date seen_at;

    @Column(name = "channel_frequency")
    public int channel_frequency;

    @Column(name = "latitude")
    public double latitude;

    @Column(name = "longitude")
    public double longitude;

    @Column(name = "gps_updated_at")
    public Date gps_updated_at;

    @Column(name = "gps_accurancy")
    public float gps_accurancy;

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

        this.seen_at = new Date();

        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.gps_updated_at = new Date(location.getTime());
        this.gps_accurancy = location.getAccuracy();

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
                    .accumulate("seen_at", this.seen_at)
                    .accumulate("latitude",this.latitude)
                    .accumulate("longitude", this.longitude)
                    .accumulate("gps_updated_at", this.gps_updated_at)
                    .accumulate("gps_accurancy", this.gps_accurancy);
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
    public static void markAsExported(List<WifiObservation> wifi_observation_list){

    }
    public static void exportToServer(Context context, String url) {
        new HttpPostTask(url, context).execute();
    }
}