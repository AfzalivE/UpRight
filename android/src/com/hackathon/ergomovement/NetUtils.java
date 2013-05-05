package com.hackathon.ergomovement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NetUtils {

    private static final String BASE_URI = "http://z176907.infobox.ru/Posture/SubmitData";

    public static AsyncHttpClient client;

    public static synchronized AsyncHttpClient getClientInstance() {
        if (client == null) {
            client = new AsyncHttpClient();
        }

        client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        return client;
    }

    public static void postData(JsonHttpResponseHandler myResponseHandler, JSONArray array) {
        String uri = BASE_URI;
        RequestParams params = new RequestParams();
        params.put("str", array.toString());
        NetUtils.getClientInstance().post(uri, params, myResponseHandler);
//        NetUtils.getClientInstance().get(uri, myResponseHandler);
    }

    public static HttpResponse postSeconds(String param) {
        Map<String, String> comment = new HashMap<String, String>();
        comment.put("str", param);
        String json = new GsonBuilder().create().toJson(comment, Map.class);

        return makeRequest(BASE_URI, json);

    }

    public static HttpResponse makeRequest(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            return new DefaultHttpClient().execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}