package com.example.dilo.DiloBackend.service.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueFileName = folder + "/" + UUID.randomUUID().toString() + extension;

        String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + uniqueFileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.setContentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uniqueFileName;
        } else {
            throw new RuntimeException("Error al subir el archivo a Supabase: " + response.getBody());
        }
    }
}