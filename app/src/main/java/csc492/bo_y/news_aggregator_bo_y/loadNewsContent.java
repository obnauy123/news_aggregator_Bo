package csc492.bo_y.news_aggregator_bo_y;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static java.net.HttpURLConnection.HTTP_OK;

public class loadNewsContent implements Runnable {

    String TAG = "providers";
    String baseURL = "https://newsapi.org/v2/top-headlines?sources=";
    String apiKey;
    String id;
    MainActivity mainActivity;
    public loadNewsContent(String apiKey,MainActivity mainActivity,String id){
        this.apiKey = apiKey;
        this.mainActivity = mainActivity;
        this.id =id;

    }

    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            String urlString = baseURL + id + "&apiKey="+ apiKey;
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
            String sources = response.getString("articles");
            ArrayList<Content> s = parseJSON(sources);
            if(s!=null){
                mainActivity.runOnUiThread(()->mainActivity.loadContent(s));
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

    private ArrayList<Content> parseJSON(String s) {

        ArrayList<Content> set = new ArrayList<>();

        try {
            JSONArray jObjMain = new JSONArray(s);

            // Here we only want to regions and subregions
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jsonObject = (JSONObject) jObjMain.get(i);
                String author = jsonObject.getString("author");
                String title= jsonObject.getString("title");
                String description= jsonObject.getString("description");
                String url= jsonObject.getString("url");
                String urlToImage= jsonObject.getString("urlToImage");
                String publishedAt= jsonObject.getString("publishedAt");
                Content c = new Content(author,title,description,url,urlToImage,publishedAt);
                set.add(c);

            }
            return set;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
