package com.devabit.beacondetector;

import android.app.Application;
import android.os.Build;
import com.devabit.beacondetector.util.LogUtil;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Victor Artemyev on 05.02.2016.
 */
public class BeaconDetectorApp extends Application implements BootstrapNotifier {

    private static final String TAG = "BeaconDetectorApp";
    private static final String BEACON_ESTIMOTE_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;

    private Logger mLogger = LoggerFactory.getLogger(BeaconDetectorApp.class);

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.configureLog();
        mLogger.info("Start Application");
        mLogger.info("Brand: " + Build.BRAND);
        mLogger.info("Model: " + Build.MODEL);
        mLogger.info("Android version: " + Build.VERSION.SDK_INT);

        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BEACON_ESTIMOTE_LAYOUT));

        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }


    @Override
    public void didEnterRegion(Region region) {
//        mLogger.info("Beacon in region: " + region.toString());
    }

    @Override
    public void didExitRegion(Region region) {
//        mLogger.info("Beacon no longer seen.");
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
