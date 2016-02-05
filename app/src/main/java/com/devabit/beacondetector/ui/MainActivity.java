package com.devabit.beacondetector.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.devabit.beacondetector.R;
import com.devabit.beacondetector.util.LogUtil;
import org.altbeacon.beacon.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private Logger mLogger = LoggerFactory.getLogger(MainActivity.class);

    private TextView mBeaconCountTextView;
    private TextView mNearestBeaconTextView;
    private BeaconManager mBeaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView deviceBrandTextView = (TextView) findViewById(R.id.device_brand_text_view);
        deviceBrandTextView.setText(Html.fromHtml(getString(R.string.device_brand, Build.BRAND)));

        TextView deviceModelTextView = (TextView) findViewById(R.id.device_model_text_view);
        deviceModelTextView.setText(Html.fromHtml(getString(R.string.device_name, Build.MODEL)));

        TextView versionTextView = (TextView) findViewById(R.id.android_version_text_view);
        versionTextView.setText(Html.fromHtml(getString(R.string.android_version, Build.VERSION.SDK_INT)));

        TextView wifiTextView = (TextView) findViewById(R.id.wifi_text_view);
        boolean isWifiEnable = checkWifiInternetConn();
        wifiTextView.setText(Html.fromHtml(getString(R.string.wifi_enable, "" + isWifiEnable)));
        mLogger.info("Wifi is enable: " + isWifiEnable);

        findViewById(R.id.send_log_button).setOnClickListener(mSendClickListener);

        mBeaconCountTextView = (TextView) findViewById(R.id.beacon_count_range_text_view);
        mNearestBeaconTextView = (TextView) findViewById(R.id.nearest_beacon_id_text_view);

        verifyBluetooth();

        mBeaconManager = BeaconManager.getInstanceForApplication(MainActivity.this);
        mBeaconManager.setBackgroundScanPeriod(5000l);
        mBeaconManager.setBackgroundBetweenScanPeriod(5000l);
        BeaconManager.setAndroidLScanningDisabled(true);
        mBeaconManager.bind(MainActivity.this);
    }

    public boolean checkWifiInternetConn() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    private final View.OnClickListener mSendClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendLog();
        }
    };

    private void sendLog() {
        Intent intent = new Intent((Intent.ACTION_SEND));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vitman8@gmail.com", "vartemjev@outlook.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Beacon Detector LOG");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(LogUtil.LOG_FILE_NAME)));
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Send mail..."));
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth.");
                builder.setCancelable(false);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothAdapter.getDefaultAdapter().enable();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }
            });
            builder.show();
        }
    }

    private boolean isNoBeaconsInRegion;

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    isNoBeaconsInRegion = false;
                    int beaconSize = beacons.size();
                    mLogger.info(" >>>>> Beacons in range: " + beaconSize);
                    Beacon beacon = getNearestBeacon(beacons);
//                    mLogger.info("The nearest beacon: " + beacon.getId2());
                    logToDisplay("" + beaconSize, beacon.getId2().toString());
                } else {
                    logToDisplay("0", "none");
//                    if(isNoBeaconsInRegion) return;
                    mLogger.info("No beacons in range. <<<<<<");
//                    isNoBeaconsInRegion = true;
                }
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException ignored) {
        }
    }

    private Beacon getNearestBeacon(Collection<Beacon> beacons) {
        Beacon nearestBeacon = beacons.iterator().next();
        for (Beacon beacon : beacons) {
            if (beacon.getDistance() < nearestBeacon.getDistance()) {
                nearestBeacon = beacon;
            }
        }
        return nearestBeacon;
    }

    public void logToDisplay(final String beaconCount, final String nearestBeaconId) {
        runOnUiThread(new Runnable() {
            public void run() {
                mBeaconCountTextView.setText(beaconCount);
                mNearestBeaconTextView.setText(nearestBeaconId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBeaconManager.isBound(this)) mBeaconManager.setBackgroundMode(false);
        mLogger.info("================== In FOREGROUND mode. ==================");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBeaconManager.isBound(this)) mBeaconManager.setBackgroundMode(false);
        mLogger.info("================== In BACKGROUND mode. ==================");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.unbind(MainActivity.this);
    }
}
