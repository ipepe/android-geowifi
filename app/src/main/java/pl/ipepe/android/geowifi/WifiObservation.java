package pl.ipepe.android.geowifi;

import android.net.wifi.ScanResult;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by patrykptasinski on 12/06/16.
 */
@Table(name = "WifiObservations")
public class WifiObservation extends Model {

    @Column(name = "SSID")
    public String SSID;

    @Column(name = "BSSID")
    public String BSSID;

    @Column(name = "RSSID_LEVEL")
    public int RSSID_LEVEL;

    public WifiObservation(){
        super();
    }

    public WifiObservation(String SSID, String BSSID, int RSSID_LEVEL){
        super();
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.RSSID_LEVEL = RSSID_LEVEL;
    }

    public WifiObservation(ScanResult scanResult){
        super();
        this.SSID = scanResult.SSID;
        this.BSSID = scanResult.BSSID;
        this.RSSID_LEVEL = scanResult.level;
    }

    @Override
    public String toString(){
        return String.format("WO: %d %s %s", this.RSSID_LEVEL, this.SSID, this.BSSID);
    }
}

