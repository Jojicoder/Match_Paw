package com.matchpaw.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchpaw.ApiClient;
import com.matchpaw.model.Animal;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimalService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient http = HttpClient.newHttpClient();

    public static List<Animal> getAll() throws Exception {
        var resp = ApiClient.get("/api/animals");
        if (resp.statusCode() != 200)
            throw new RuntimeException("Server error: " + resp.statusCode());
        return mapper.readValue(resp.body(), new TypeReference<List<Animal>>() {});
    }

    public static Animal create(Animal animal) throws Exception {
        var resp = ApiClient.post("/api/animals", mapper.writeValueAsString(animal));
        if (resp.statusCode() != 200 && resp.statusCode() != 201)
            throw new RuntimeException("Server error: " + resp.statusCode());
        if (resp.body() != null && !resp.body().isBlank()) {
            var node = mapper.readTree(resp.body());
            if (node.has("animalId")) animal.setAnimalId(node.get("animalId").asInt());
        }
        return animal;
    }

    public static Animal update(Animal animal) throws Exception {
        var resp = ApiClient.put("/api/animals/" + animal.getAnimalId(), mapper.writeValueAsString(animal));
        if (resp.statusCode() != 200 && resp.statusCode() != 204)
            throw new RuntimeException("Server error: " + resp.statusCode());
        if (resp.body() == null || resp.body().isBlank()) return animal;
        return mapper.readValue(resp.body(), Animal.class);
    }

    public static String uploadPhoto(File photo) throws Exception {
        if (!ApiClient.isLoggedIn())
            throw new RuntimeException("Please log in again before uploading photos.");

        var encodedName = URLEncoder.encode(photo.getName(), StandardCharsets.UTF_8);
        var sasResp = ApiClient.get("/api/blob/sas-url?fileName=" + encodedName);
        if (sasResp.statusCode() != 200) {
            return uploadPhotoDirect(photo, "Photo upload setup failed: "
                    + sasResp.statusCode() + responseDetail(sasResp.body()));
        }

        var node = mapper.readTree(sasResp.body());
        var sasUrl = node.get("sasUrl").asText();
        var blobUrl = node.get("blobUrl").asText();
        var contentType = Files.probeContentType(photo.toPath());
        if (contentType == null || contentType.isBlank()) contentType = "application/octet-stream";

        var uploadReq = HttpRequest.newBuilder()
                .uri(URI.create(sasUrl))
                .header("x-ms-blob-type", "BlockBlob")
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofFile(photo.toPath()))
                .build();
        var uploadResp = http.send(uploadReq, HttpResponse.BodyHandlers.ofString());
        if (uploadResp.statusCode() < 200 || uploadResp.statusCode() >= 300)
            throw new RuntimeException("Photo upload failed: " + uploadResp.statusCode() + responseDetail(uploadResp.body()));

        return blobUrl;
    }

    private static String uploadPhotoDirect(File photo, String setupFailure) throws Exception {
        var boundary = "----MatchPawBoundary" + UUID.randomUUID();
        var contentType = Files.probeContentType(photo.toPath());
        if (contentType == null || contentType.isBlank()) contentType = "application/octet-stream";

        var body = new ArrayList<byte[]>();
        body.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + photo.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(Files.readAllBytes(photo.toPath()));
        body.add(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        var request = HttpRequest.newBuilder()
                .uri(URI.create(ApiClient.BASE_URL + "/api/blob/upload"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", "Bearer " + ApiClient.getToken())
                .POST(HttpRequest.BodyPublishers.ofByteArrays(body))
                .build();
        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(setupFailure + "; fallback upload failed: "
                    + response.statusCode() + responseDetail(response.body()));
        }

        var node = mapper.readTree(response.body());
        return node.get("blobUrl").asText();
    }

    private static String responseDetail(String body) {
        return body == null || body.isBlank() ? "" : " - " + body;
    }
}
