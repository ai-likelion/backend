package com.likelion.ai_teacher_a.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.public.url}")
    private String r2PublicUrl;


    public String upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String rawFileName = UUID.randomUUID() + "_" + originalName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(rawFileName)
                        .contentType(file.getContentType())
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );

        return generatePublicUrl(rawFileName);
    }


    public void delete(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        System.out.println("🧹 Deleting S3 key: " + key); // 로그 확인용

        s3Client.deleteObject(builder ->
                builder.bucket(bucket).key(key)
        );
    }

    private String generatePublicUrl(String rawFileName) {
        String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        return r2PublicUrl + "/" + bucket + "/" + encodedFileName;
    }


    private String extractKeyFromUrl(String fileUrl) {
        String baseUrl = r2PublicUrl + "/" + bucket + "/";
        String encodedKey = fileUrl.replace(baseUrl, "");
        return URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
    }


}
