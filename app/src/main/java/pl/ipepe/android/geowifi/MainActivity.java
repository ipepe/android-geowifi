package pl.ipepe.android.geowifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

public class MainActivity extends AppCompatActivity {

//    elementy gui
    ListView current_wifis_list_view;
    TextView last_scan_time_text_view;
    TextView last_gps_position_text_view;
    TextView wifi_observations_count_text_view;
//    skanowanie wifi
    WifiManager wifi_manager;
    ArrayList<String> wifis;
    WifiScanReceiver wifi_scan_reciever;
//    lokalizowanie
    GpsLocationListener gps_location_listener;
    Location last_location = null;
    Date last_location_time = null;
    public static final String preferenceUniqueIdKey = "deviceid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        current_wifis_list_view = (ListView) findViewById(R.id.currentWifisListView);
        last_scan_time_text_view = (TextView) findViewById(R.id.lastScanTimeTextView);
        last_gps_position_text_view = (TextView) findViewById(R.id.lastGpsPositionTextView);
        wifi_observations_count_text_view = (TextView) findViewById(R.id.wifiObservationsCountTextView);

        wifi_manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi_scan_reciever = new WifiScanReceiver();
        gps_location_listener = new GpsLocationListener();
        startWifiScan();
        startGpsListener();
        updateWifiObservationsCount();
    }

    public String getUniqueDeviceId(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String device_id = sharedPref.getString(preferenceUniqueIdKey, null);
        if(device_id == null){
            SharedPreferences.Editor editor = sharedPref.edit();
            device_id = UUID.randomUUID().toString();
            editor.putString(preferenceUniqueIdKey, device_id);
            editor.apply();
        }
        return device_id;
    }

    public void startWifiScan() {
        wifi_manager.startScan();
    }

    public void startGpsListener(){
        SmartLocation.with(this).location().config(LocationParams.NAVIGATION).continuous().start(gps_location_listener);
    }

    protected void onPause() {
        unregisterReceiver(wifi_scan_reciever);
        SmartLocation.with(this).location().stop();
        super.onPause();
    }

    protected void onResume() {
        startGpsListener();
        registerReceiver(wifi_scan_reciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.export:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText editTextView = new EditText(this);
//                if (BuildConfig.DEBUG) {
//                    editTextView.setText("http://192.168.1.108:3000/api/v1/wifi_observation_receiver");
//                }else{
                    editTextView.setText("https://geowifi.science/api/v1/wifi_observation_receiver");
//                }
                alert.setCancelable(true);
                alert.setTitle("Cofirm server address:");
                alert.setView(editTextView);
                alert.setCancelable(true);
                alert.setPositiveButton("Send my observations database", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        WifiObservation.exportToServer(getApplicationContext(), editTextView.getText().toString(), getUniqueDeviceId());
                    }
                });


                alert.show();
                return true;
            case R.id.reset_exported_flag:
                WifiObservation.resetIsExportedFlag(getApplicationContext());
            case R.id.destroy_observation_database:
                WifiObservation.destroyDatabase(getApplicationContext());
                updateWifiObservationsCount();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GpsLocationListener implements OnLocationUpdatedListener {
        @Override
        public void onLocationUpdated(Location location) {
            last_location = location;
            last_gps_position_text_view.setText(
                    String.format("%s:\n%4.3f %4.3f\n%s",
                            getString(R.string.gps_scan_text),
                            location.getLatitude(),
                            location.getLongitude(),
                            new SimpleDateFormat("HH:mm:ss").format(new Date(location.getTime())))
            );
        }
    }

    private void updateWifiObservationsCount(){
        wifi_observations_count_text_view.setText(String.format("%s: %d", getString(R.string.wifi_observations_count_text), WifiObservation.count()));
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(last_location != null && ( (1000*10) > Calendar.getInstance().getTime().getTime() - last_location.getTime())){
                List<ScanResult> wifiScanList = wifi_manager.getScanResults();
                last_scan_time_text_view.setText(String.format("%s:\n%s",
                        getString(R.string.wifi_scan_text),
                        new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
                for (ScanResult wifi : wifiScanList) {
                    if( wifi.SSID.contains("_nomap") || wifi.SSID.contains("_optout") ){
                        wifiScanList.remove(wifi);
                    }
                }

                wifis = new ArrayList<String>();
                if (wifiScanList.size() == 0) {
                    wifis.add(getString(R.string.no_networks));
                } else {
                    ActiveAndroid.beginTransaction();
                    try {
                        for (ScanResult wifi : wifiScanList) {
                            WifiObservation wifiObservation = new WifiObservation(wifi, last_location);
                            wifis.add(wifiObservation.toString());
                            wifiObservation.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                }
                current_wifis_list_view.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.row, wifis));
                wifi_manager.startScan();
            }
            updateWifiObservationsCount();
        }
    }
}