package com.nocturnal.ironman.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import org.junit.Test;
import static org.junit.Assert.*;

public class WikiServiceTest
{
    @Test
    public void questionUsesNamedPlaceAsWikiSearchTopic()
    {
        assertEquals("Waterbirth Island", WikiService.bestSearchTopic(
            "How do I travel to Waterbirth Island from Rellekka?"));
        assertEquals("Zulrah", WikiService.bestSearchTopic(
            "Which quests are required to fight Zulrah?"));
        assertEquals("farming herb patches", WikiService.bestSearchTopic("farming herb patches"));
    }

    @Test
    public void wikiSearchResultsPreferMatchingArticleOverTangentialMatches()
    {
        JsonArray hits = new JsonArray();
        for (String title : new String[]{"2005", "Demonic Pacts League/Areas/Fremennik",
            "Waterbirth Island Dungeon", "Waterbirth Island"})
        {
            JsonObject hit = new JsonObject();
            hit.addProperty("title", title);
            hits.add(hit);
        }
        List<String> ranked = WikiService.rankTitles("Waterbirth Island", hits);
        assertEquals("Waterbirth Island", ranked.get(0));
        assertEquals(4, ranked.size());
    }

    @Test
    public void wikiSearchRetrievesTheBestMatchingArticleAndCachesIt() throws Exception
    {
        class StubWiki extends WikiService
        {
            final List<String> requests = new ArrayList<>();

            StubWiki() { super(new OkHttpClient()); }

            @Override
            String get(String address)
            {
                requests.add(address);
                if (address.contains("list=search"))
                    return "{\"query\":{\"search\":[{\"title\":\"2005\"},{\"title\":\"Waterbirth Island Dungeon\"},"
                        + "{\"title\":\"Waterbirth Island\"}]}}";
                if (address.contains("titles=Waterbirth+Island&"))
                    return "{\"query\":{\"pages\":{\"1\":{\"title\":\"Waterbirth Island\","
                        + "\"extract\":\"Jarvald's boat sails from Rellekka.\"}}}}";
                return "{\"query\":{\"pages\":{}}}";
            }
        }

        StubWiki wiki = new StubWiki();
        List<String> first = wiki.search("How do I travel to Waterbirth Island from Rellekka?");
        assertEquals(1, first.size());
        assertTrue(first.get(0).startsWith("Wiki page: Waterbirth Island\n"));
        assertTrue(first.get(0).contains("/w/Waterbirth_Island"));
        assertTrue(first.get(0).contains("Jarvald's boat sails from Rellekka"));
        assertTrue(wiki.requests.get(0).contains("srsearch=Waterbirth+Island"));
        assertTrue(wiki.requests.get(1).contains("titles=Waterbirth+Island&"));

        int count = wiki.requests.size();
        assertEquals(first, wiki.search("HOW do I travel to waterbirth island from rellekka?"));
        assertEquals(count, wiki.requests.size());
    }
}
