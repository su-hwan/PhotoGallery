package com.su.photogallery.service;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.su.photogallery.GalleryItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    private static final String TAG = "FlickrFetch";
    private static final String API_KEY = "aefb932c3eed118a451ab53de389b30c";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        String jsonString = null;
Log.i(TAG,"Fetch URL: " + url);
        try {
            jsonString = getUrlString(url);
            //Log.i(TAG,"Received JSON: " + jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items: " + e);
        }

        if (jsonString != null) {
            Gson gson = new Gson();

            JsonElement jsonElement = JsonParser.parseString(jsonString);
            JsonObject object = jsonElement.getAsJsonObject();
            Type typeOfT = new TypeToken<List<GalleryItem>>() {
            }.getType();
            JsonObject jsonObject = object.getAsJsonObject("photos");
            if (!jsonObject.isJsonNull()
                    && !jsonObject.get("photo").isJsonNull()) {
                items = gson.fromJson(jsonObject.get("photo"), typeOfT);
                items.removeIf(item -> item.getUrl() == null || item.getUrl().isEmpty());

                //int iPage = gson.fromJson(jsonObject.get("page"), Integer.class);
                //int iPerpage = gson.fromJson(jsonObject.get("perpage"), Integer.class);
                //items.forEach(item -> {item.setPage(iPage);item.setPerPage(iPerpage);});
            }
        }
        return items;
    }

    private String buildUrl(String method, String query) {
        Log.i(TAG, "method: "+ method + ", query: "+ query);
        Uri.Builder builder = ENDPOINT.buildUpon();
        builder.appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            builder.appendQueryParameter("text", query);
        }
        return builder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos() {
        String urlString = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadGalleryItems(urlString);
    }

    public List<GalleryItem> searchPhotos(String query) {
        String urlString = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(urlString);
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with" + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}
