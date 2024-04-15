package org.talos.teltestspringbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.talos.teltestspringbot.model.Anime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

        String url_str = System.getenv("ANI_API_URL")
                + message
                + "&filter=id,names.ru,description,posters.original,player.episodes&include=raw_poster&limit=3";
        URL url = new URL(url_str);

        Scanner scanner = new Scanner((InputStream) url.getContent());

        StringBuilder result = new StringBuilder();
        while (scanner.hasNextLine()) result.append(scanner.nextLine());

        JSONObject object = new JSONObject(result.toString());

        return object.optJSONArray("list");
    }
}
