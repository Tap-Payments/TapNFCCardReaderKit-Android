package company.tap.tapnfccardreaderkit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.net.SocketException;

import company.tap.nfcreader.open.reader.TapEmvCard;
import company.tap.nfcreader.open.reader.TapNfcCardReader;
import company.tap.nfcreader.open.utils.TapCardUtils;
import company.tap.nfcreader.open.utils.TapNfcUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class MainActivity extends AppCompatActivity {

    private TapNfcCardReader tapNfcCardReader;
    private Disposable cardReadDisposable = Disposable.empty();
    private LinearLayout cardreadContent;
    private TextView scancardContent;
    private TextView cardnumberText;
    private TextView expiredateText;
    private TextView cardHolderNameText;
    private TextView cardType;
    TextView noNfcText;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tapNfcCardReader = new TapNfcCardReader(this);
        noNfcText = findViewById(android.R.id.candidatesArea);
        if (tapNfcCardReader == null)
            noNfcText.setVisibility(View.VISIBLE);
        scancardContent = findViewById(R.id.content_putCard);
        cardreadContent = findViewById(R.id.content_cardReady);
        cardnumberText = findViewById(android.R.id.text1);
        expiredateText = findViewById(android.R.id.text2);
        cardHolderNameText = findViewById(R.id.text4);
        cardType = findViewById(R.id.text3);
        createProgressDialog();
    }

    @Override
    protected void onResume() {
        if (TapNfcUtils.isNfcAvailable(this)) {
            if (TapNfcUtils.isNfcEnabled(this)) {
                tapNfcCardReader.enableDispatch();//Activates NFC  to read NFC Card details .
                scancardContent.setVisibility(View.VISIBLE);
            } else
                enableNFC();
        } else {
            scancardContent.setVisibility(View.GONE);
            cardreadContent.setVisibility(View.GONE);
            noNfcText.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mProgressDialog.show();
        if (tapNfcCardReader.isSuitableIntent(intent)) {
            mProgressDialog.show();
            cardReadDisposable = tapNfcCardReader
                    .readCardRx2(intent)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::showCardInfo,
                            throwable -> RxJavaPlugins.setErrorHandler(e -> {
                                if (e instanceof UndeliverableException) {
                                    e = e.getCause();
                                }
                                if ((e instanceof IOException) || (e instanceof SocketException)) {
                                    // fine, irrelevant network problem or API that throws on cancellation
                                    return;
                                }
                                if (e instanceof InterruptedException) {
                                    // fine, some blocking code was interrupted by a dispose call
                                    return;
                                }
                                if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                                    // that's likely a bug in the application
                                    System.out.println("eee"+e);
                                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                    return;
                                }
                                if (e instanceof IllegalStateException) {
                                    // that's a bug in RxJava or in a custom operator
                                    Thread.currentThread().getUncaughtExceptionHandler().toString();
                                          //  .handleException(Thread.currentThread(), e);
                                    return;
                                }
                                Log.e("warn","Undeliverable exception received, not sure what to do", e);
                            }));

        }

    }

    @Override
    protected void onPause() {

        cardReadDisposable.dispose();
        tapNfcCardReader.disableDispatch();
        super.onPause();
    }

    private void enableNFC() {
        noNfcText.setVisibility(View.VISIBLE);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.msg_info));
        alertDialog.setMessage(getString(R.string.enable_nfc));
        alertDialog.setPositiveButton(getString(R.string.msg_ok), (dialog, which) -> {
            noNfcText.setVisibility(View.GONE);
            dialog.dismiss();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        });
        alertDialog.setNegativeButton(getString(R.string.msg_dismiss), (dialog, which) -> {
            dialog.dismiss();
            onBackPressed();
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (cardreadContent.isShown()) {
            scancardContent.setVisibility(View.VISIBLE);
            cardreadContent.setVisibility(View.GONE);
        } else super.onBackPressed();

    }

    private void showCardInfo(TapEmvCard emvCard) {
        String text = TextUtils.join("\n", new Object[]{
                TapCardUtils.formatCardNumber(emvCard.getCardNumber(), emvCard.getType()),
                DateFormat.format("M/y", emvCard.getExpireDate()),
                "---",
                "Bank info (probably): ",
                emvCard.getAtrDescription(),
                "---",
                emvCard.toString().replace(", ", ",\n")
        });
        Log.e("showCardInfo:", text);
        scancardContent.setVisibility(View.GONE);
        cardreadContent.setVisibility(View.VISIBLE);
        cardnumberText.setText(emvCard.getCardNumber());
        expiredateText.setText(DateFormat.format("M/y", emvCard.getExpireDate()));
        cardHolderNameText.setText(emvCard.getHolderFirstname());
        cardType.setText(emvCard.getApplicationLabel());
        mProgressDialog.dismiss();
    }

    private void displayError(String message) {
        noNfcText.setText(message);
    }

    private void createProgressDialog() {
        String title = getResources().getString(R.string.ad_progressBar_title);
        String mess = getString(R.string.ad_progressBar_mess);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(mess);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }
}
