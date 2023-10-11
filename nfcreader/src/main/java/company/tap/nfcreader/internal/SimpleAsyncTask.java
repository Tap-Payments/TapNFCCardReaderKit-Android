package company.tap.nfcreader.internal;

import android.os.AsyncTask;
import android.util.Log;

public abstract class SimpleAsyncTask extends AsyncTask<Void, Void, Object> {

    @Override
    protected Object doInBackground(final Void... params) {

        Object result = null;

        try {
            doInBackground();
        } catch (Exception e) {
            result = e;
            Log.e(SimpleAsyncTask.class.getName(), e.getMessage(), e);
        }

        return result;
    }

    protected abstract void doInBackground();

}
