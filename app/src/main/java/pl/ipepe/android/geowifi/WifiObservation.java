package pl.ipepe.android.geowifi;

import android.annotation.TargetApi;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.SystemClock;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by patrykptasinski on 12/06/16.
 */
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

    public WifiObservation() {
        super();
    }

    public WifiObservation(ScanResult scanResult){
        super();
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.signal_level = scanResult.level;
        this.capabilities = scanResult.capabilities;
        this.seen_at = new Date();
        this.channel_frequency = scanResult.frequency;
    }

    @Override
    public String toString(){
        return String.format("WO: %d %s %s", this.signal_level, this.ssid, this.bssid);
    }
}

