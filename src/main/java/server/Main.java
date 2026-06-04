package server;

import com.google.gson.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 23456;
    private static final Gson gson = new Gson();

    private static final JsonObject database = new JsonObject();

    private static volatile boolean running = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");

        serverSocket = new ServerSocket(PORT);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket));
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }

        executor.shutdown();
        serverSocket.close();
    }

    private static void handleClient(Socket socket) {
        try (
                socket;
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            JsonObject request = JsonParser.parseString(input.readUTF()).getAsJsonObject();

            String type = request.get("type").getAsString();
            JsonObject response;

            switch (type) {
                case "set" -> response = set(request.get("key"), request.get("value"));
                case "get" -> response = get(request.get("key"));
                case "delete" -> response = delete(request.get("key"));
                case "exit" -> {
                    response = ok();
                    running = false;
                    serverSocket.close();
                }
                default -> response = error();
            }

            output.writeUTF(gson.toJson(response));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized JsonObject set(JsonElement keyElement, JsonElement value) {
        List<String> path = getPath(keyElement);

        JsonObject current = database;

        for (int i = 0; i < path.size() - 1; i++) {
            String key = path.get(i);

            if (!current.has(key) || !current.get(key).isJsonObject()) {
                current.add(key, new JsonObject());
            }

            current = current.getAsJsonObject(key);
        }

        current.add(path.get(path.size() - 1), value);

        return ok();
    }

    private static synchronized JsonObject get(JsonElement keyElement) {
        List<String> path = getPath(keyElement);

        JsonElement current = database;

        for (String key : path) {
            if (!current.isJsonObject()) {
                return error();
            }

            JsonObject object = current.getAsJsonObject();

            if (!object.has(key)) {
                return error();
            }

            current = object.get(key);
        }

        JsonObject response = ok();
        response.add("value", current);
        return response;
    }

    private static synchronized JsonObject delete(JsonElement keyElement) {
        List<String> path = getPath(keyElement);

        JsonObject current = database;

        for (int i = 0; i < path.size() - 1; i++) {
            String key = path.get(i);

            if (!current.has(key) || !current.get(key).isJsonObject()) {
                return error();
            }

            current = current.getAsJsonObject(key);
        }

        String lastKey = path.get(path.size() - 1);

        if (!current.has(lastKey)) {
            return error();
        }

        current.remove(lastKey);

        return ok();
    }

    private static List<String> getPath(JsonElement keyElement) {
        List<String> path = new ArrayList<>();

        if (keyElement.isJsonArray()) {
            for (JsonElement element : keyElement.getAsJsonArray()) {
                path.add(element.getAsString());
            }
        } else {
            path.add(keyElement.getAsString());
        }

        return path;
    }

    private static JsonObject ok() {
        JsonObject response = new JsonObject();
        response.addProperty("response", "OK");
        return response;
    }

    private static JsonObject error() {
        JsonObject response = new JsonObject();
        response.addProperty("response", "ERROR");
        response.addProperty("reason", "No such key");
        return response;
    }
}