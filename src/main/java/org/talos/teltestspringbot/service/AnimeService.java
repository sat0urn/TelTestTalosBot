package org.talos.teltestspringbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.talos.teltestspringbot.model.Anime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Service
public class AnimeService {
    public static List<Anime> getAnimeTitle(
            String message) throws IOException {
        Anime anime;

        JSONArray list = getObjects(message);

        List<Anime> anime_list = new ArrayList<>();

        for (int i = 0; i < list.length(); i++) {
            anime = new Anime();

            JSONObject animeJson = list.getJSONObject(i);
            int id = animeJson.getInt("id");
            String name_ru = animeJson.getJSONObject("names").optString("ru");
            String description = animeJson.optString("description");
            String posterUrl = animeJson.getJSONObject("posters")
                    .getJSONObject("original")
                    .getString("url");

            anime.setId(id);
            anime.setNameRu(name_ru);
            anime.setDescription(description);
            anime.setPosterUrl(posterUrl);

            anime_list.add(anime);
        }

        return anime_list;
    }

    private static JSONArray getObjects(String message) throws IOException {
        message = URLEncoder.encode(message, StandardCharsets.UTF_8);

        String url_str = "https://api.anilibria.tv/v3/title/search?search="
                + message
                + "&filter=id,names.ru,description,posters.original,player.episodes&include=raw_poster&limit=3";

        System.out.println("Gathering data from URL: " + url_str);

        URL url = new URL(url_str);

        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        System.out.println("------------------------------------------------------------------");
        uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        uc.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        uc.setRequestProperty("Accept", "*/*");
        uc.setRequestProperty("Connection", "keep-alive");
        uc.setRequestProperty("Access-Control-Allow-Origin", "*");
        uc.setRequestProperty("Access-Control-Allow-Credentials", "true");
        uc.setRequestProperty("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE");
        uc.setRequestProperty("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept");
        uc.setRequestProperty("Access-Control-Expose-Headers", "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        uc.setRequestProperty("Authorization", "Bearer vXWcNmhQb0NLvNCus8VWUeukSzWmCOLn");

        System.out.println("------------------------------------------------------------------");

        Map<String, java.util.List<String>> requestHeaders = uc.getRequestProperties();
        for (Map.Entry<String, java.util.List<String>> entry : requestHeaders.entrySet()) {
            String headerName = entry.getKey();
            for (String value : entry.getValue()) {
                System.out.println(headerName + ": " + value);
            }
        }
        System.out.println("------------------------------------------------------------------");
        // Get the response code
        int responseCode = uc.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Log response headers
        Map<String, List<String>> headers = uc.getHeaderFields();
        for (Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            for (String value : entry.getValue()) {
                System.out.println(headerName + ": " + value);
            }
        }

        System.out.println("------------------------------------------------------------------");

        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }

        System.out.println(result);

        in.close();
        uc.disconnect();

        JSONObject object = new JSONObject(result.toString());

        return object.optJSONArray("list");
    }
}
