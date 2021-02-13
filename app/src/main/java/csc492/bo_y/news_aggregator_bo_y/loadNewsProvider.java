package csc492.bo_y.news_aggregator_bo_y;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_OK;

public class loadNewsProvider implements Runnable{
    String TAG = "providers";
    String baseURL = "https://newsapi.org/v2/sources?apiKey=";
    String apiKey;
    MainActivity mainActivity;
    public loadNewsProvider(String apiKey,MainActivity mainActivity){
        this.apiKey = apiKey;
        this.mainActivity = mainActivity;

    }

    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            String urlString = baseURL + apiKey;
            Uri.Builder buildURL = Uri.parse(urlString).buildUpon();

            Log.d(TAG, "run: Initial URL: " + urlString);

            String urlToUse = buildURL.build().toString();

            URL url = new URL(urlToUse);
            Log.d(TAG, "run: Full URL: " + url);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent","");
            connection.connect();


            final StringBuilder sb = new StringBuilder();
            if (connection.getResponseCode() == HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    sb.append(line);
                }

            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    sb.append(line);
                }
            }

            JSONObject response = new JSONObject(sb.toString());
            String sources = response.getString("sources");
            Set<Provider> s = parseJSON(sources);
            if(s!=null){
                mainActivity.runOnUiThread(()->mainActivity.loadResult(s));
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "doInBackground: Invalid URL: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream: " + e.getMessage());
                }
            }
        }

    }

    private Set<Provider> parseJSON(String s) {

        Set<Provider> set = new HashSet();

        try {
            JSONArray jObjMain = new JSONArray(s);

            // Here we only want to regions and subregions
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jsonObject = (JSONObject) jObjMain.get(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String category = jsonObject.getString("category");
                String language = jsonObject.getString("language");
                String country = jsonObject.getString("country");

                Provider p = new Provider(id,name,category,language,country);
                set.add(p);

            }
            return set;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
