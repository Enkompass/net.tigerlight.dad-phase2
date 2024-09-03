package net.tigerlight.dad.util;

import net.tigerlight.dad.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


/**
 * Web Service utility class to call web urls. And returns response.
 */
public class WSUtil {
    private Context mContext;


    public String callServiceHttpPost(final Context mContext, final String url, final RequestBody requestBody) {
        this.mContext = mContext;
        Log.d(WSUtil.class.getSimpleName(), "Request Url : " + url);
        Log.d(WSUtil.class.getSimpleName(), String.format("Request String : %s", requestBody.toString()));
        String responseString;
        try {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging )
                    .connectTimeout(WsConstants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(WsConstants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            final Response response = okHttpClient.newCall(request).execute();

            responseString = response.body().string();

            Log.d(this.getClass().getSimpleName(), "Response String : " + responseString);
            if (TextUtils.isEmpty(responseString) || !isJSONValid(responseString)) {
                responseString = getNetWorkError();
            }
        } catch (IOException e) {
            e.printStackTrace();
            responseString = getNetWorkError();
        }
        return responseString;

    }

    public String callServiceHttpGet(final Context mContext, final String url) {
        this.mContext = mContext;
        MediaType JSON;
        Log.d(WSUtil.class.getSimpleName(), "Request Url : " + url);
        String responseString;
        try {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);

//            OkHttpClient okClient = new OkClient(SelfSigningClientBuilder.createClient());


//            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                    .tlsVersions(TlsVersion.TLS_1_2)
//                    .cipherSuites(
//                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
//                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
//                    .build();

//              .connectionSpecs(Collections.singletonList(spec))


            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(WsConstants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(WsConstants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .get()

                    .build();


//            Request request = new Request.Builder().url(url).head().build();

            final Response response = okHttpClient.newCall(request).execute();

            Headers responseHeaders = response.headers();

            for (int i = 0; i < responseHeaders.size(); i++) {
                Log.d("Header", responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }


            responseString = response.body().string();

//            InputStream is = response.body().byteStream();
//            Log.d("is", String.valueOf(is));


            response.body().close();
            Log.d("request_Res", String.valueOf(request));
            Log.d(this.getClass().getSimpleName(), "Response String : " + responseString);
            if (TextUtils.isEmpty(responseString) || !isJSONValid(responseString)) {
                responseString = getNetWorkError();
            }
        } catch (IOException e) {
            e.printStackTrace();
            responseString = getNetWorkError();
        }
        return responseString;
    }


    private String getNetWorkError() {
        final JSONObject jsonObject = new JSONObject();
        try {
            final WsConstants wsConstants = new WsConstants();
            final JSONObject jsonObjectSettings = new JSONObject();
            jsonObjectSettings.put(wsConstants.PARAMS_SUCCESS, 0);
            jsonObjectSettings.put(wsConstants.PARAMS_MESSAGE, (mContext != null) ? mContext.getString(R.string.alert_network_error) : "Network error, please try again later"); //TODO:  Band-aid (per Rod) for unknown NPE
            jsonObject.put(wsConstants.PARAMS_SETTINGS, jsonObjectSettings);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
