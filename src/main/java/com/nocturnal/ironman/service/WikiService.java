package com.nocturnal.ironman.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nocturnal.ironman.data.BossCatalog;
import com.nocturnal.ironman.data.DestinationCatalog;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

public class WikiService
{
    private static final String API = "https://oldschool.runescape.wiki/api.php";
    private static final String USER_AGENT = "IronmanCompanion/1.2 (RuneLite plugin; wiki-assisted account planning)";
    private static final int MAX_RESULTS = 4;
    private static final int SEARCH_CANDIDATES = 12;
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

        String topic = bestSearchTopic(searchTerm);
        String address = API + "?action=query&list=search&srnamespace=0&srlimit=" + SEARCH_CANDIDATES
            + "&srsearch=" + URLEncoder.encode(topic, "UTF-8") + "&format=json";
        JsonObject root = new JsonParser().parse(get(address)).getAsJsonObject();
        JsonObject queryObject = root.getAsJsonObject("query");
        JsonArray hits = queryObject == null ? null : queryObject.getAsJsonArray("search");
        if (hits == null || hits.size() == 0) return Collections.emptyList();

        List<String> results = new ArrayList<>();
        for (String title : rankTitles(topic, hits))
        {
            // Whole-article extracts have an API limit of one page per request.
            String pageAddress = API + "?action=query&prop=extracts&titles="
                + URLEncoder.encode(title, "UTF-8")
                + "&explaintext=1&exsectionformat=plain&format=json";
            JsonObject pageRoot = new JsonParser().parse(get(pageAddress)).getAsJsonObject();
            JsonObject pageQuery = pageRoot.getAsJsonObject("query");
            JsonObject pages = pageQuery == null ? null : pageQuery.getAsJsonObject("pages");
            if (pages == null) continue;
            for (Map.Entry<String, JsonElement> entry : pages.entrySet())
            {
                if (!entry.getValue().isJsonObject()) continue;
                JsonObject page = entry.getValue().getAsJsonObject();
                if (!page.has("extract") || page.get("extract").isJsonNull()) continue;
                String extract = page.get("extract").getAsString().replaceAll("\\s+", " ").trim();
                if (extract.isEmpty()) continue;
                if (extract.length() > EXTRACT_CHARS)
                    extract = extract.substring(0, EXTRACT_CHARS) + " [Extract truncated]";
                String pageTitle = page.get("title").getAsString();
                String wikiUrl = "https://oldschool.runescape.wiki/w/"
                    + URLEncoder.encode(pageTitle.replace(' ', '_'), "UTF-8");
                results.add("Wiki page: " + pageTitle + "\nURL: " + wikiUrl + "\n" + extract);
            }
        }

        List<String> immutable = Collections.unmodifiableList(new ArrayList<>(results));
        cache.put(normalized, new CacheEntry(immutable, now.plusSeconds(CACHE_SECONDS)));
        if (cache.size() > 100)
            cache.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
        return immutable;
    }

    static String bestSearchTopic(String question)
    {
        String normalized = question.toLowerCase(Locale.ROOT);
        String longest = "";
        for (DestinationCatalog.Destination destination : DestinationCatalog.ALL)
        {
            String name = destination.name;
            if (name.length() > longest.length() && name.length() >= 5
                && normalized.contains(name.toLowerCase(Locale.ROOT))) longest = name;
        }
        for (BossCatalog.Boss boss : BossCatalog.ALL)
        {
            String name = boss.name;
            if (name.length() > longest.length() && name.length() >= 5
                && normalized.contains(name.toLowerCase(Locale.ROOT))) longest = name;
        }
        return longest.isEmpty() ? question : longest;
    }

    static List<String> rankTitles(String topic, JsonArray hits)
    {
        Set<String> unique = new LinkedHashSet<>();
        for (JsonElement hit : hits)
            if (hit.isJsonObject() && hit.getAsJsonObject().has("title"))
                unique.add(hit.getAsJsonObject().get("title").getAsString());
        List<String> ranked = new ArrayList<>(unique);
        ranked.sort(Comparator.comparingInt((String title) -> relevance(topic, title)).reversed());
        if (ranked.size() > MAX_RESULTS) ranked.subList(MAX_RESULTS, ranked.size()).clear();
        return ranked;
    }

    private static int relevance(String topic, String title)
    {
        String needle = topic.toLowerCase(Locale.ROOT).trim();
        String haystack = title.toLowerCase(Locale.ROOT);
        if (needle.equals(haystack)) return 1000;
        int score = haystack.contains(needle) || needle.contains(haystack) ? 100 : 0;
        for (String word : needle.split("[^a-z0-9]+"))
            if (word.length() >= 4 && haystack.contains(word)) score += 10;
        if (haystack.contains("/")) score -= 15;
        return score;
    }

    String get(String address) throws Exception
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
            String response = HttpResponseReader.read(stream, 1_500_000, "OSRS Wiki");
            if (status < 200 || status >= 300) throw new IllegalStateException("OSRS Wiki returned HTTP " + status);
            return response;
        }
        finally
        {
            connection.disconnect();
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
