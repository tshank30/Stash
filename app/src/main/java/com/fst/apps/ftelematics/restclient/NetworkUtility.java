package com.fst.apps.ftelematics.restclient;

import android.text.TextUtils;
import android.util.Log;

import com.fst.apps.ftelematics.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by welcome on 1/12/2016.
 */
public class NetworkUtility {

    private HttpURLConnection connection=null;
    private URL url;
    private final String TAG=NetworkUtility.class.getSimpleName();
    private final String USER_AGENT = "Mozilla/5.0";

    /*public NetworkUtility(String endpoint){
        try {
            url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }*/

    public String sendGet(String endpoint) throws Exception {

        String url = AppConstants.BASE_SERVICE_URL+endpoint;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        if(responseCode==HttpURLConnection.HTTP_OK){
            String json=readStream(con.getInputStream());
            if(con!=null){
                con.disconnect();
            }
            return json;
        }

        //print result
       return null;

    }

    public String sendPost(String endpoint,String params){
        try {

            String url = AppConstants.BASE_SERVICE_URL+endpoint;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            OutputStream os = con.getOutputStream();
            os.write(params.getBytes("UTF-8"));
            os.flush();

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + con.getResponseCode());
            }else {
                String json=readStream(con.getInputStream());
                if(con!=null){
                    con.disconnect();
                }
                return json;
            }

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    public static int sendPost(String endpoint, Map<String, String> params)
            throws IOException {

        URL url;
        int status;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v("NetworkUtility", "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            Log.e("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            status = conn.getResponseCode();
            Log.v("SERVER RESPONSE", conn.getResponseMessage());
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return status;
    }


    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        if(!TextUtils.isEmpty(response.toString())){
            String json=getJsonPart(response.toString());
            return json;
        }
        return null;
    }

    public String getJsonPart(String response){
        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray jsonArray=mainObject.getJSONArray("json");
            if(jsonArray.length()>0){
                return jsonArray.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
