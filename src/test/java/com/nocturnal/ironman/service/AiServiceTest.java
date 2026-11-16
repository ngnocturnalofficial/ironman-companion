package com.nocturnal.ironman.service;

import com.nocturnal.ironman.IronmanCompanionConfig;
import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.ProgressSnapshot;
import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import com.nocturnal.ironman.navigation.ScenePathfinder;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class AiServiceTest
{
    private IronmanCompanionConfig config(int port)
    {
        return new IronmanCompanionConfig()
        {
            @Override public boolean aiEnabled() { return true; }
            @Override public String apiEndpoint()
            {
                return "http://127.0.0.1:" + port + "/v1/chat/completions";
            }
            @Override public String apiKey() { return ""; }
            @Override public String model() { return "test-model"; }
            @Override public boolean wikiLookupsEnabled() { return false; }
            @Override public boolean includeBankInAi() { return false; }
            @Override public boolean includeProgressInAi() { return false; }
        };
    }

    private AiService service(int port)
    {
        return new AiService(config(port), new WikiService(),
            new PlannerService(new ScenePathfinder()));
    }

    private HttpServer provider(int status, String response, AtomicReference<String> received) throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange ->
        {
            byte[] request = exchange.getRequestBody().readAllBytes();
            received.set(new String(request, StandardCharsets.UTF_8));
            byte[] reply = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, reply.length);
            try (java.io.OutputStream out = exchange.getResponseBody()) { out.write(reply); }
        });
        server.start();
        return server;
    }

    @Test
    public void providerErrorsDoNotEchoRemoteBodyOrAccountData() throws Exception
    {
        AtomicReference<String> received = new AtomicReference<>();
        HttpServer server = provider(401,
            "{\"error\":\"Secret-account-345 and API key secret-abcdef\"}", received);
        try
        {
            try
            {
                service(server.getAddress().getPort()).ask("Where can I train?", null);
                fail("Expected HTTP 401 error");
            }
            catch (IllegalStateException expected)
            {
                assertTrue(expected.getMessage().contains("HTTP 401"));
                assertFalse(expected.getMessage().contains("Secret-account-345"));
                assertFalse(expected.getMessage().contains("secret-abcdef"));
            }
            assertNotNull(received.get());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    public void privateBankAndQuestDetailsRespectAiContextToggles() throws Exception
    {
        AtomicReference<String> received = new AtomicReference<>();
        HttpServer server = provider(200,
            "{\"choices\":[{\"message\":{\"content\":\"Try Catherby.\"}}]}", received);
        try
        {
            Map<Integer, Integer> inventory = Collections.singletonMap(2001, 1);
            Map<Integer, Integer> bank = Collections.singletonMap(2002, 25);
            Map<Integer, String> names = new HashMap<>();
            names.put(2001, "Lobster");
            names.put(2002, "PRIVATE_BANK_ITEM_XYZZY");
            ProgressSnapshot progress = new ProgressSnapshot(
                Collections.singletonMap("PRIVATE_QUEST_XYZZY", "FINISHED"), Collections.emptyMap());
            AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
                Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
                bank, 3, 10, "3200, 3200", null, false, progress,
                SlayerTaskSnapshot.empty(), null, names);

            assertEquals("Try Catherby.",
                service(server.getAddress().getPort()).ask("Where can I train?", account));
            assertNotNull(received.get());
            assertTrue(received.get().contains("Lobster"));
            assertFalse(received.get().contains("PRIVATE_BANK_ITEM_XYZZY"));
            assertFalse(received.get().contains("PRIVATE_QUEST_XYZZY"));
        }
        finally
        {
            server.stop(0);
        }
    }
}
