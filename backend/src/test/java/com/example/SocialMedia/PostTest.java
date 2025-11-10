package com.example.SocialMedia;


import com.example.SocialMedia.controller.PostController;
import com.example.SocialMedia.exception.FileTooLargeException;
import com.example.SocialMedia.exception.TooManyFileException;
import com.example.SocialMedia.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PostControllerTest {

    private static final int MAX_FILE = 10; // maximum number of files allowed
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    private PostController postController;

    @BeforeEach
    void setUp() {
        // Mock the PostService (no need to use it for testing checkUploadedFile method)
        PostService postService = mock(PostService.class);
        postController = new PostController(postService); // Instantiate PostController
    }

    @Test
    void testCheckUploadedFile_withTooManyFiles() {
        // Simulate the scenario where the number of files exceeds the allowed MAX_FILE limit
        MultipartFile[] files = new MultipartFile[MAX_FILE + 1]; // More than MAX_FILE
        for (int i = 0; i < files.length; i++) {
            files[i] = mock(MultipartFile.class); // Mock each file
        }

        // Assert that TooManyFileException is thrown
        TooManyFileException exception = assertThrows(TooManyFileException.class, () -> {
            postController.checkUploadedFile(files); // Call the method directly
        });

        assertEquals("File exceeds maximum number of 10", exception.getMessage());
    }

    @Test
    void testCheckUploadedFile_withFileExceedsMaxSize() {
        // Simulate a file whose size exceeds the MAX_FILE_SIZE
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(MAX_FILE_SIZE + 1); // Simulate a file size greater than MAX_FILE_SIZE

        MultipartFile[] files = {file}; // One file larger than the max size

        // Assert that FileTooLargeException is thrown
        FileTooLargeException exception = assertThrows(FileTooLargeException.class, () -> {
            postController.checkUploadedFile(files); // Call the method directly
        });

        assertEquals("File exceeds maximum size limit of 10MB", exception.getMessage());
    }

    @Test
    void testCheckUploadedFile_withValidFiles() {
        // Simulate valid files that don't exceed the size or number limits
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);

        // File sizes are within the valid range (less than MAX_FILE_SIZE)
        when(file1.getSize()).thenReturn((long) (5 * 1024 * 1024)); // 5MB
        when(file2.getSize()).thenReturn((long) (8 * 1024 * 1024)); // 8MB

        MultipartFile[] files = {file1, file2};

        // No exception should be thrown
        assertDoesNotThrow(() -> postController.checkUploadedFile(files)); // Call the method directly
    }
}
