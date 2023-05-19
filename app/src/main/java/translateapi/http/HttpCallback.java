package translateapi.http;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;

public abstract class HttpCallback<T> implements Callback {
    protected void onSuccess(T response) {

    }

    protected void onFailure(Throwable e) {

    }

    @Override
    public final void onFailure(Call call, final IOException e) {
        e.printStackTrace();
        sendFailureMessage(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

    }

    final void sendFailureMessage(final Exception e) {
        onFailure(e);
    }

    final void sendSuccessMessage(final T response) {
        onSuccess(response);
    }
}
