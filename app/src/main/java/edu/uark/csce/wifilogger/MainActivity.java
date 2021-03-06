package edu.uark.csce.wifilogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WifiReceiver receiver = new WifiReceiver();
    private final long MIN_MILLISECONDS_BETWEEN_UPDATES = 5000;
    private long lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        receiver = new WifiReceiver();
        lastUpdate = 0;
        this.registerReceiver(receiver, filter);
    }

    public void writeScanResults(List<ScanResult> results) throws IOException {
        Long time = new GregorianCalendar().getTime().getTime();
        if (time - lastUpdate >= MIN_MILLISECONDS_BETWEEN_UPDATES) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "aps.txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            lastUpdate = time;
            writer.write(Long.toString(time));
            writer.newLine();
            for (ScanResult r : results) {
                writer.write(r.SSID + " " + r.level);
                writer.newLine();
            }
            writer.newLine();
            writer.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager conn = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> results = conn.getScanResults();
            try {
                writeScanResults(results);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
    }
}
