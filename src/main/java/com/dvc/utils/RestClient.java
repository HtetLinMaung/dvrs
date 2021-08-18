package com.dvc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {
    public static Map<String, Object> fetch(String httpUrl, String methods, Map<String, Object> body,
            Map<String, String> options) throws IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        for (Entry<String, String> pair : options.entrySet()) {
            connection.setRequestProperty(pair.getKey(), pair.getValue());
        }
        connection.setRequestMethod(methods);
        connection.setDoOutput(true);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(body);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return mapper.readValue(response.toString(), Map.class);
        }

    }

    public static Map<String, Object> get(String httpUrl, Map<String, String> options)
            throws UnsupportedEncodingException, IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        for (Entry<String, String> pair : options.entrySet()) {
            connection.setRequestProperty(pair.getKey(), pair.getValue());
        }
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        ObjectMapper mapper = new ObjectMapper();
        // String json = mapper.writeValueAsString(body);
        // try (OutputStream os = connection.getOutputStream()) {
        // byte[] input = json.getBytes("utf-8");
        // os.write(input, 0, input.length);
        // }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return mapper.readValue(response.toString(), Map.class);
        }
    }

    public static Map<String, Object> post(String httpUrl, Map<String, Object> body, Map<String, String> options)
            throws IOException {
        return fetch(httpUrl, "POST", body, options);
    }

    public static Map<String, Object> put(String httpUrl, Map<String, Object> body, Map<String, String> options)
            throws IOException {
        return fetch(httpUrl, "PUT", body, options);
    }

    public static Map<String, Object> delete(String httpUrl, Map<String, String> options) throws IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        for (Entry<String, String> pair : options.entrySet()) {
            connection.setRequestProperty(pair.getKey(), pair.getValue());
        }
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return mapper.readValue(response.toString(), Map.class);
        }
    }
}
