package company.tap.nfcreader.open.reader;

import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;

import company.tap.nfcreader.internal.library.enums.SwEnum;
import company.tap.nfcreader.internal.library.exception.CommunicationException;
import company.tap.nfcreader.internal.library.parser.IProvider;
import company.tap.nfcreader.internal.library.utils.BytesUtils;
import company.tap.nfcreader.internal.library.utils.TlvUtil;

/**
 * Provider used to communicate with EMV card
 */
class TapNfcProvider implements IProvider {

    /**
     * TAG for logger
     */
    private static final String TAG = TapNfcProvider.class.getName();

    /**
     * Tag comm
     */
    private IsoDep mTagCom;

    private boolean debugMode = false;

    @Override
    public byte[] transceive(final byte[] pCommand) throws CommunicationException {
        if (debugMode) {
            log("send: " + BytesUtils.bytesToString(pCommand));
        }

        byte[] response;
        try {
            // send command to emv card
            response = mTagCom.transceive(pCommand);
        } catch (IOException e) {
            throw new CommunicationException(e.getMessage());
        }

        log("resp: " + BytesUtils.bytesToString(response));
        try {
            log("resp: " + TlvUtil.prettyPrintAPDUResponse(response));
            SwEnum val = SwEnum.getSW(response);
            if (val != null) {
                log("resp: " + val.getDetail());
            }
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }

        return response;
    }

    /**
     * Enable ro disable debug info logging
     */
    public TapNfcProvider setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    /**
     * Setter for the field mTagCom
     *
     * @param mTagCom the mTagCom to set
     */
    void setmTagCom(final IsoDep mTagCom) {
        this.mTagCom = mTagCom;
    }

    private void log(String logLine) {
        if (debugMode) {
            Log.d(TAG, logLine);
        }
    }
}
