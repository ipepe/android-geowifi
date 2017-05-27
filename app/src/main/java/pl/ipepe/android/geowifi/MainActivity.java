package pl.ipepe.android.geowifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends ActivityWithSettings {

    ListView list_view;
    TextView text_view;
    WifiManager wifi_manager;
    ArrayList<String> wifis;
    WifiScanReceiver wifi_scan_reciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        list_view =(ListView)findViewById(R.id.listView);
        text_view =(TextView)findViewById(R.id.textView);

        wifi_manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi_scan_reciever = new WifiScanReceiver();
        startWifiScan();

    }

    public void startWifiScan(){
        wifi_manager.startScan();
    }

    protected void onPause() {
        unregisterReceiver(wifi_scan_reciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifi_scan_reciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi_manager.getScanResults();
            text_view.setText(String.format("Last scan time: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));

            wifis = new ArrayList<String>();
            if (wifiScanList.size() == 0){
                wifis.add("Brak sieci : (");
            }else {
                ActiveAndroid.beginTransaction();
                try {
                    for(ScanResult wifi : wifiScanList) {
                        WifiObservation wifiObservation = new WifiObservation(wifi);
                        wifis.add(wifiObservation.toString());
                        wifiObservation.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }
            list_view.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.row,wifis));
            wifi_manager.startScan();
        }
    }
}
