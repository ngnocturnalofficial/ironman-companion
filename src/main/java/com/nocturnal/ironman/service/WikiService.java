package com.nocturnal.ironman.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

public class WikiService
{
    private static final String API = "https://oldschool.runescape.wiki/api.php";
    private static final String USER_AGENT = "IronmanCompanion/1.2 (RuneLite plugin; wiki-assisted account planning)";
    private static final int MAX_RESULTS = 4;
    private static final int MAX_QUERY_CHARS = 200;
    private static final int EXTRACT_CHARS = 6000;
    private static final long CACHE_SECONDS = 600;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Inject
    public WikiService() {}

    public List<String> search(String query) throws Exception
    {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        String searchTerm = query.trim();
        if (searchTerm.length() > MAX_QUERY_CHARS) searchTerm = searchTerm.substring(0, MAX_QUERY_CHARS);
        String normalized = searchTerm.toLowerCase(Locale.ROOT);
        CacheEntry cached = cache.get(normalized);
        Instant now = Instant.now();
        if (cached != null && cached.expiresAt.isAfter(now)) return cached.results;

        String address = API + "?action=query&generator=search&gsrnamespace=0&gsrlimit=" + MAX_RESULTS
            + "&gsrsearch=" + URLEncoder.encode(searchTerm, "UTF-8")
            + "&prop=extracts&explaintext=1&exsectionformat=plain&exchars=" + EXTRACT_CHARS + "&format=json";
        JsonObject root = JsonParser.parseString(get(address)).getAsJsonObject();
        JsonObject queryObject = root.getAsJsonObject("query");
        JsonObject pages = queryObject == null ? null : queryObject.getAsJsonObject("pages");
        if (pages == null) return Collections.emptyList();

        List<JsonObject> orderedPages = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : pages.entrySet())
            if (entry.getValue().isJsonObject()) orderedPages.add(entry.getValue().getAsJsonObject());
        orderedPages.sort(Comparator.comparingInt(page -> page.has("index") ? Integer.parseInt(page.get("index").getAsString()) : Integer.MAX_VALUE));

        List<String> results = new ArrayList<>();
        for (JsonObject page : orderedPages)
        {
            if (!page.has("title")) continue;
            String title = page.get("title").getAsString();
            String extract = page.has("extract") ? page.get("extract").getAsString() : "";
            extract = extract.replaceAll("\\s+", " ").trim();
            String wikiUrl = "https://oldschool.runescape.wiki/w/"
                + URLEncoder.encode(title.replace(' ', '_'), "UTF-8");
            if (!extract.isEmpty()) results.add("Wiki page: " + title + "\nURL: " + wikiUrl + "\n" + extract);
        }

        List<String> immutable = Collections.unmodifiableList(new ArrayList<>(results));
        cache.put(normalized, new CacheEntry(immutable, now.plusSeconds(CACHE_SECONDS)));
        if (cache.size() > 100)
            cache.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
        return immutable;
    }

    private String get(String address) throws Exception
    {
        HttpURLConnection connection = (HttpURLConnection) URI.create(address).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        try
        {
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            String response = readResponse(stream);
            if (status < 200 || status >= 300) throw new IllegalStateException("OSRS Wiki returned HTTP " + status);
            return response;
        }
        finally
        {
            connection.disconnect();
        }
    }

    private String readResponse(InputStream stream) throws Exception
    {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
        {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (result.length() + line.length() > 1_500_000)
                    throw new IllegalStateException("OSRS Wiki response exceeded the 1.5 MB safety limit.");
                result.append(line);
            }
            return result.toString();
        }
    }

    private static final class CacheEntry
    {
        private final List<String> results;
        private final Instant expiresAt;

        private CacheEntry(List<String> results, Instant expiresAt)
        {
            this.results = results;
            this.expiresAt = expiresAt;
        }
    }
}
