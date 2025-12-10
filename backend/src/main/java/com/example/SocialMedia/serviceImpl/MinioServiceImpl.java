package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.message.FileUploadResponse;
import com.example.SocialMedia.service.IMinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements IMinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        try {
            // 1. Tạo bucket nếu chưa có
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 2. Tạo tên file duy nhất
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID() + extension;

            // 3. Upload file lên MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 4. Generate presigned URL ngay lập tức
            String presignedUrl = getFileUrl(fileName);

            // 5. Detect media type từ tên file
            String mediaType = detectMediaType(originalFilename);

            // 6. Trả về DTO với tất cả thông tin
            return FileUploadResponse.builder()
                    .mediaId(null)
                    .mediaUrl(presignedUrl)        // ← Presigned URL
                    .thumbnailUrl(null)
                    .fileName(fileName)            // ← Tên file trong MinIO
                    .fileSize((int) file.getSize()) // ← SIZE CHÍNH XÁC
                    .mediaType(mediaType)          // ← AUDIO, IMAGE, VIDEO, FILE
                    .build();

        } catch (Exception e) {
            log.error("MinIO Upload Error: ", e);
            throw new RuntimeException("Upload file failed");
        }
    }

    /**
     * Detect media type từ file extension
     */
    private String detectMediaType(String fileName) {
        if (fileName == null) return "FILE";

        String name = fileName.toLowerCase();

        // Audio extensions
        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")
                || name.endsWith(".ogg") || name.endsWith(".aac") || name.endsWith(".wma")
                || name.endsWith(".flac")) {
            return "AUDIO";
        }

        // Image extensions
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                || name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp")) {
            return "IMAGE";
        }

        // Video extensions
        if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv")
                || name.endsWith(".mov") || name.endsWith(".flv") || name.endsWith(".webm")) {
            return "VIDEO";
        }

        return "FILE";
    }

    /**
     * Hàm tạo Presigned URL (để FE xem file)
     */
    @Override
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Get Presigned URL Error: ", e);
            return null;
        }
    }
}