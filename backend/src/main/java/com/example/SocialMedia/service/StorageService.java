package com.example.SocialMedia.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface StorageService {
    String uploadImage(MultipartFile file) throws IOException;
    String deleteImage(String fileName) throws IOException;
}
