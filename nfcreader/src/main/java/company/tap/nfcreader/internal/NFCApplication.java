package company.tap.nfcreader.internal;

import android.app.Application;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryPerformance;


/**
 * Created by AhlaamK on 4/5/20.
 * <p>
 * Copyright (c) 2020    Tap Payments.
 * All rights reserved.
 **/
public class NFCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withDataSaleOptOut(false) //CCPA - the default value is false
                .withCaptureUncaughtExceptions(true)
                .withIncludeBackgroundSessionsInMetrics(true)
                .withLogLevel(Log.VERBOSE)
                .withPerformanceMetrics(FlurryPerformance.ALL)
                .build(this, "PTT9D949M57SP7WS45MJ");

    }

}
