package com.likelion.ai_teacher_a.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
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

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String rawFileName = UUID.randomUUID() + "_" + originalName;

        getS3Client().putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(rawFileName)
                        .acl("public-read")
                        .contentType(file.getContentType())
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );

        return generatePublicUrl(rawFileName);
    }


    public void delete(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        System.out.println("🧹 Deleting S3 key: " + key); // 로그 확인용

        getS3Client().deleteObject(builder ->
                builder.bucket(bucket).key(key)
        );
    }


    private S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                ).build();
    }

    private String generatePublicUrl(String rawFileName) {
        String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + encodedFileName;
    }


    private String extractKeyFromUrl(String fileUrl) {
        String baseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        String encodedKey = fileUrl.replace(baseUrl, "");
        return URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
    }


}
