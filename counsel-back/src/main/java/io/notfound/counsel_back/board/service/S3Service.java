package io.notfound.counsel_back.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // 파일 업로드를 위한 PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            // 파일을 RequestBody로 변환하여 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
        // 업로드된 파일의 URL을 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toExternalForm();
    }

    // [추가] 파일 삭제 메서드
    public void deleteFile(String fileUrl) {
        try {
            String fileKey = getKeyFromUrl(fileUrl);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            // S3 파일 삭제 실패 시 로깅만 하고 다음 로직으로 넘어갈 수 있도록 처리 (선택사항)
            log.error("S3 파일 삭제에 실패했습니다. URL: {}", fileUrl, e);
        }
    }

    // [추가] 파일 URL에서 key(파일 경로/이름)를 추출하는 헬퍼 메서드
    private String getKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            // URL 경로에서 첫 '/'를 제외한 부분을 key로 사용
            // 예: https://bucket-name.s3.region.amazonaws.com/uuid_filename.jpg -> /uuid_filename.jpg -> uuid_filename.jpg
            return url.getPath().substring(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 파일 URL입니다.", e);
        }
    }
}