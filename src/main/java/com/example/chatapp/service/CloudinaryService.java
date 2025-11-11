package com.example.chatapp.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    @Value("${app.cloudinary.cloud_name}")
    private String cloudName;

    @Value("${app.cloudinary.cloud_name}")
    private String apiKey;

    @Value("${app.cloudinary.upload_preset}")
    private String preset;

    private final RestTemplate restTemplate = new RestTemplate();

    public CloudinaryService() {}

    public String storeFile(MultipartFile file) {
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("upload_preset", preset);
        body.add("api_key", apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        return response.getBody().get("secure_url").toString();
    }
}
