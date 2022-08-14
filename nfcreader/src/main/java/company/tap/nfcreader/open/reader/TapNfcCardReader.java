package company.tap.nfcreader.open.reader;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import company.tap.nfcreader.BuildConfig;
import company.tap.nfcreader.internal.AnalyticsHelper;
import company.tap.nfcreader.internal.library.log.Logger;
import company.tap.nfcreader.internal.library.log.LoggerFactory;
import company.tap.nfcreader.internal.library.parser.EmvParser;
import company.tap.nfcreader.internal.library.utils.AtrUtils;
import company.tap.nfcreader.internal.library.utils.BytesUtils;
import company.tap.nfcreader.open.utils.TapNfcUtils;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static company.tap.nfcreader.internal.AnalyticsHelper.EVENT_INTENT;

@SuppressWarnings({"WeakerAccess", "unused"})
public class TapNfcCardReader {
    private TapNfcUtils tapNfcUtils;
    private TapNfcProvider provider;
    private Logger logger;

    public TapNfcCardReader(Activity activity) {
        tapNfcUtils = new TapNfcUtils(activity);
        provider = new TapNfcProvider();
        logger = LoggerFactory.getLogger(TapNfcCardReader.class);
        String app_name = activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString();
        // Capture sdkVersion info
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("sdkVersion", "1.0");
        AnalyticsHelper.logEvent(AnalyticsHelper.APP_DETAILS, parameters, true);
    }

    /**
     * Begin waiting for bank card been tapped to NFC module of phone
     */
    public void enableDispatch() {
        tapNfcUtils.enableDispatch();
    }

    /**
     * Stop waiting for bank card
     */
    public void disableDispatch() {
        tapNfcUtils.disableDispatch();
    }

    /**
     * Checks that intent is suitable for NFC card reading
     *
     * @param intent intent for check
     * @return true - intent is good. false - this is not the intent you're looking for
     */
    public boolean isSuitableIntent(Intent intent) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        boolean isSuitable;
        if (tag == null) {
            isSuitable = false;
            logger.debug("No TAG in intent");
        } else {
            isSuitable = true;
        }
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Suitable Intent", String.valueOf(isSuitable));
        AnalyticsHelper.logEvent(EVENT_INTENT, parameters, true);
        if(tag==null){
            logger.debug("IsoDep was not enumerated in getTechList()");

            return false;
        }else{
            IsoDep tagComm = IsoDep.get(tag);

            if (tagComm == null) {
                logger.debug("IsoDep was not enumerated in getTechList()");
                return false;
            }
        }

        return true;
    }

    /**
     * Read card data from given intent.
     * <p>Note that this method is blocking. You should not use it as is.
     * Instead - wrap it into some async framework: RxJava, AsyncTask, etc</p>
     * <p>
     * <p>Intent by itself does not contain all data. It contains metadata of NFC card.
     * To read card data, library will open NFC connection and transfer some bytes.</p>
     * <p>
     * <p>You should check that this intent contain right data with {@link #isSuitableIntent(Intent)}
     * before calling this method</p>
     *
     * @param intent intent with initial card information.
     * @return Ready for use card data
     * @throws IOException          may be thrown during NFC data transfer
     * @throws WrongIntentException thrown if intent does not contain {@link NfcAdapter#EXTRA_TAG}
     * @throws WrongTagTech         thrown when this NFC tech is not supported:
     *                              not enumerated in {@link Tag#getTechList}.
     */
    public TapEmvCard readCardBlocking(Intent intent)
            throws IOException, WrongIntentException, WrongTagTech {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            throw new WrongIntentException("No TAG in intent");
        }

        IsoDep tagComm = IsoDep.get(tag);
        if (tagComm == null) {
            throw new WrongTagTech();
        }

        try {
            tagComm.connect();
            provider.setmTagCom(tagComm);

            EmvParser parser = new EmvParser(provider, true);
            final TapEmvCard emvCard = parser.readEmvCard();

            emvCard.setAtrDescription(extractAtsDescription(tagComm));
            return emvCard;
        } finally {
            tagComm.close();
        }
    }

    /**
     * Get ATS from isoDep and find matching description
     */
    private Collection<String> extractAtsDescription(final IsoDep pIso) {
        byte[] pAts = null;
        if (pIso.isConnected()) {
            // Extract ATS from NFC-A
            pAts = pIso.getHistoricalBytes();
            if (pAts == null) {
                // Extract ATS from NFC-B
                pAts = pIso.getHiLayerResponse();
            }
        }
        return AtrUtils.getDescriptionFromAts(BytesUtils.bytesToString(pAts));
    }

    /**
     * Read card data from given intent.
     * <p>Intent by itself does not contain all data. It contains metadata of NFC card.
     * To read card data, library will open NFC connection and transfer some bytes.</p>
     * <p>You should check that this intent contain right data with {@link #isSuitableIntent(Intent)}
     * before calling this method</p>
     * <p>Operates on IO scheduler</p>
     *
     * @param intent intent with initial card information.
     */
    public Single<TapEmvCard> readCardRx1(final Intent intent) {
        return readCardRx1(intent, Schedulers.io());
    }

    /**
     * Read card data from given intent.
     * <p>Intent by itself does not contain all data. It contains metadata of NFC card.
     * To read card data, library will open NFC connection and transfer some bytes.</p>
     * <p>You should check that this intent contain right data with {@link #isSuitableIntent(Intent)}
     * before calling this method</p>
     * <p>Operates on given scheduler</p>
     *
     * @param intent    intent with initial card information.
     * @param scheduler scheduler for operating
     */
    public Single<TapEmvCard> readCardRx1(final Intent intent, Scheduler scheduler) {
        return Single
                .fromCallable(new Callable<TapEmvCard>() {
                    @Override
                    public TapEmvCard call() throws Exception {
                        return readCardBlocking(intent);
                    }
                })
                .subscribeOn(scheduler);
    }

    /**
     * Read card data from given intent.
     * <p>Intent by itself does not contain all data. It contains metadata of NFC card.
     * To read card data, library will open NFC connection and transfer some bytes.</p>
     * <p>You should check that this intent contain right data with {@link #isSuitableIntent(Intent)}
     * before calling this method</p>
     * <p>Operates on IO scheduler</p>
     *
     * @param intent intent with initial card information.
     */
    public io.reactivex.Single<TapEmvCard> readCardRx2(final Intent intent) {
        return readCardRx2(intent, io.reactivex.schedulers.Schedulers.io());
    }

    /**
     * Read card data from given intent.
     * <p>Intent by itself does not contain all data. It contains metadata of NFC card.
     * To read card data, library will open NFC connection and transfer some bytes.</p>
     * <p>You should check that this intent contain right data with {@link #isSuitableIntent(Intent)}
     * before calling this method</p>
     * <p>Operates on given scheduler</p>
     *
     * @param intent    intent with initial card information.
     * @param scheduler scheduler for operating
     */
    public io.reactivex.Single<TapEmvCard> readCardRx2(final Intent intent, io.reactivex.Scheduler scheduler) {
        return io.reactivex.Single.
                fromCallable(new Callable<TapEmvCard>() {
                    @Override
                    public TapEmvCard call() throws Exception {
                        return readCardBlocking(intent);
                    }
                })
                .subscribeOn(scheduler);
    }

    public static class WrongIntentException extends Exception {
        WrongIntentException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class WrongTagTech extends Exception {
        WrongTagTech() {
            super("IsoDep was not enumerated in getTechList()");
        }
    }
}
