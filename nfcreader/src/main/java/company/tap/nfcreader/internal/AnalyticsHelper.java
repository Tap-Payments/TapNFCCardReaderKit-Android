package company.tap.nfcreader.internal;



import java.util.Map;

/**
 * Created by AhlaamK on 4/5/20.
 * <p>
 * Copyright (c) 2020    Tap Payments.
 * All rights reserved.
 **/
public class AnalyticsHelper {
    public static final String EVENT_INTENT = "cameraIntentcall";
    public static final String APP_DETAILS = "AppDetails";
    /**
     * Logs an event for analytics.
     *
     * @param eventName     name of the event
     * @param eventParams   event parameters (can be null)
     * @param timed         <code>true</code> if the event should be timed, false otherwise
     */
    public static void logEvent(String eventName, Map<String, String> eventParams, boolean timed) {
    }
    /**
     * Logs an error.
     *
     * @param errorId           error ID
     * @param errorDescription  error description
     * @param throwable         a {@link Throwable} that describes the error
     */
    public static void logError(String errorId, String errorDescription, Throwable throwable) {
    }

}
