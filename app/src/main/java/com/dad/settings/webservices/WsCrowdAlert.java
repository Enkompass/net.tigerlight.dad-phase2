package com.dad.settings.webservices;

import android.content.Context;

import com.dad.registration.util.Constant;
import com.dad.util.Constants;
import com.dad.util.Preference;
import com.dad.util.WSUtil;
import com.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by M.T. on 7 Oct, 2016.
 * This class is making api call for user login
 */
public class WsCrowdAlert {
    private Context context;
    private String message;
    private boolean success;

    public WsCrowdAlert(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Calls the api user Login.
     *
     * @param alert
     * @return
     */
    public JSONObject executeService(final int alert) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(alert));
        return parseResponse(response);
    }

    /**
     * Parse the json response from {@link String} to {@link JSONArray}.
     *
     * @param response {@link String} response that is recived from the api request.
     * @return {@link JSONArray} for success or failure response of request
     */
    private JSONObject parseResponse(final String response) {
        if (response != null && response.trim().length() > 0) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                final WsConstants wsConstants = new WsConstants();
                if (jsonObject.length() > 0) {
                    success = jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("1");
                    message = jsonObject.optString(wsConstants.PARAMS_MESSAGE);

                    if (success) {
                        return jsonObject;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Generates RequestBody for making api call for okhttp
     *
     * @param alert {@link String} value for pin
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(final int alert) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_CROWD_ALERT);
        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_CROWD_STATUS + "=" + alert);
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        return builder.toString();
    }
}
