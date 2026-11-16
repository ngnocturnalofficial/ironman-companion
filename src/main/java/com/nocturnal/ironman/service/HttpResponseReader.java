package com.nocturnal.ironman.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class HttpResponseReader
{
    private HttpResponseReader() {}

    static String read(InputStream stream, int maxChars, String service) throws IOException
    {
        if (stream == null) return "";
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
        {
            StringBuilder result = new StringBuilder();
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1)
            {
                if (count > maxChars - result.length())
                    throw new IllegalStateException(service + " response exceeded the " + maxChars + " character limit.");
                result.append(buffer, 0, count);
            }
            return result.toString();
        }
    }
}
