package company.tap.tapnfccardreaderkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.TextView;

import company.tap.nfcreader.internal.library.model.EmvCard;
import company.tap.nfcreader.open.reader.TapNfcCardReader;
import company.tap.nfcreader.open.utils.TapCardUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private TapNfcCardReader tapNfcCardReader;
    private Disposable cardReadDisposable = Disposables.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tapNfcCardReader = new TapNfcCardReader(this);
        textView = findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        tapNfcCardReader.enableDispatch();
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tapNfcCardReader.isSuitableIntent(intent)) {
            textView.setText(R.string.reading);
            cardReadDisposable = tapNfcCardReader
                    .readCardRx2(intent)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::showCardInfo,
                            throwable -> displayError(throwable.getMessage()));
        }
    }

    @Override
    protected void onPause() {
        cardReadDisposable.dispose();
        tapNfcCardReader.disableDispatch();
        super.onPause();
    }

    private void showCardInfo(EmvCard emvCard) {
        String text = TextUtils.join("\n", new Object[]{
                TapCardUtils.formatCardNumber(emvCard.getCardNumber(), emvCard.getType()),
                DateFormat.format("M/y", emvCard.getExpireDate()),
                "---",
                "Bank info (probably): ",
                emvCard.getAtrDescription(),
                "---",
                emvCard.toString().replace(", ", ",\n")
        });
        textView.setText(text);
    }

    private void displayError(String message) {
        textView.setText(message);
    }
}
