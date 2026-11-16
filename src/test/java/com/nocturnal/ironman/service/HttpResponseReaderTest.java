package com.nocturnal.ironman.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpResponseReaderTest
{
    private ByteArrayInputStream input(String text)
    {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void retainsWhitespaceAndUnicodeWithinTheLimit() throws Exception
    {
        assertEquals("a\nβ", HttpResponseReader.read(input("a\nβ"), 3, "Test"));
        assertEquals("", HttpResponseReader.read(null, 2, "Test"));
    }

    @Test
    public void rejectsAReallyLongSingleLineWithoutAccumulatingIt() throws Exception
    {
        try
        {
            HttpResponseReader.read(input(new String(new char[10000]).replace('\0', 'A')), 1000, "Test");
            fail("Expected a capped response");
        }
        catch (IllegalStateException expected)
        {
            assertTrue(expected.getMessage().contains("1000 character limit"));
        }
    }
}
