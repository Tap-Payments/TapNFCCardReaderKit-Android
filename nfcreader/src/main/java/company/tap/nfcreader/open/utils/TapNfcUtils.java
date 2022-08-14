package company.tap.nfcreader.open.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Build;

/**
 * Utils class used to manager NFC Adapter
 */
public class TapNfcUtils {

	/**
	 * Check if NFC is available on the device
	 * 
	 * @return true if the device has NFC available
	 */
	public static boolean isNfcAvailable(final Context pContext) {
		boolean nfcAvailable = true;
		try {
			NfcAdapter adapter = NfcAdapter.getDefaultAdapter(pContext);
			if (adapter == null) {
				nfcAvailable = false;
			}
		} catch (UnsupportedOperationException e) {
			nfcAvailable = false;
		}
		return nfcAvailable;
	}
	
	/**
	 * Check if NFC is enabled on the device
	 * 
	 * @return true if the device has NFC enabled
	 */
	public static boolean isNfcEnabled(final Context pContext) {
		boolean nfcEnabled;
		try {
			NfcAdapter adapter = NfcAdapter.getDefaultAdapter(pContext);
			nfcEnabled = adapter.isEnabled();
		} catch (UnsupportedOperationException e) {
			nfcEnabled = false;
		}
		return nfcEnabled;
	}

	/**
	 * NFC adapter
	 */
	private final NfcAdapter mNfcAdapter;
	/**
	 * Intent sent
	 */
	private final PendingIntent mPendingIntent;

	/**
	 * Parent Activity
	 */
	private final Activity mActivity;

	/**
	 * Inetnt filter
	 */
	private static final IntentFilter[] INTENT_FILTER = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) };

	/**
	 * Tech List
	 */
	private static final String[][] TECH_LIST = new String[][] { { IsoDep.class.getName() } };

	/**
	 * Constructor of this class
	 * 
	 * @param pActivity
	 *            activity context
	 */
	public TapNfcUtils(final Activity pActivity) {
		mActivity = pActivity;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			mPendingIntent = PendingIntent.getActivity(mActivity, 0,
					new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

		}else {
			mPendingIntent = PendingIntent.getActivity(mActivity, 0,
					new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		}

	/*	mPendingIntent = PendingIntent.getActivity(mActivity, 0,
				new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);*/

	}

	/**
	 * Disable dispacher Remove the most important priority for foreground application
	 */
	public void disableDispatch() {
		if (mNfcAdapter != null) {
			mNfcAdapter.disableForegroundDispatch(mActivity);
		}
	}

	/**
	 * Activate NFC dispacher to read NFC Card Set the most important priority to the foreground application
	 */
	public void enableDispatch() {
		if (mNfcAdapter != null) {
			mNfcAdapter.enableForegroundDispatch(mActivity, mPendingIntent, INTENT_FILTER, TECH_LIST);
		}
	}

	/**
	 * Getter mNfcAdapter
	 * 
	 * @return the mNfcAdapter
	 */
	public NfcAdapter getmNfcAdapter() {
		return mNfcAdapter;
	}
}
