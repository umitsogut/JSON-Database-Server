package client;

import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;

    public static void main(String[] args) throws IOException {
        String request = buildRequest(args);

        System.out.println("Client started!");

        try (
                Socket socket = new Socket(ADDRESS, PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            output.writeUTF(request);
            System.out.println("Sent: " + request);

            String response = input.readUTF();
            System.out.println("Received: " + response);
        }
    }

    private static String buildRequest(String[] args) throws IOException {
        String type = null;
        String key = null;
        String value = null;
        String inputFile = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t" -> type = args[++i];
                case "-k" -> key = args[++i];
                case "-v" -> value = args[++i];
                case "-in" -> inputFile = args[++i];
            }
        }

        if (inputFile != null) {
            return Files.readString(Path.of("src/client/data/" + inputFile));
        }

        JsonObject request = new JsonObject();
        request.addProperty("type", type);

        if (key != null) {
            request.addProperty("key", key);
        }

        if (value != null) {
            request.addProperty("value", value);
        }

        return request.toString();
    }
}