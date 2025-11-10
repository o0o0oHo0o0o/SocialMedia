package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.PostMedia;
import com.example.SocialMedia.repository.PostMediaRepository;
import com.example.SocialMedia.service.InteractableItemService;
import com.example.SocialMedia.service.PostMediaService;
import com.example.SocialMedia.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PostMediaServiceImpl implements PostMediaService {

    private final StorageService storageService;
    private final PostMediaRepository postMediaRepository;
    private final InteractableItemService interactableItemService;

    @Autowired
    public PostMediaServiceImpl(@Qualifier("localStorageServiceImpl") StorageService storageService,
                                PostMediaRepository postMediaRepository,
                                InteractableItemService interactableItemService) {
        this.storageService = storageService;
        this.postMediaRepository = postMediaRepository;
        this.interactableItemService = interactableItemService;
    }
    private String classify(MultipartFile file){
        String mimeType = file.getContentType();
        if (mimeType != null && mimeType.startsWith("video/")) {
            return "VIDEO";  // It's a video file
        } else if (mimeType != null && mimeType.startsWith("image/")) {
            return "IMAGE";  // It's an image file
        }

        return "Unknown";  // If MIME type is not recognized
    }

    public String createPostMedia(MultipartFile file, Post post) throws IOException {
        String type = classify(file);
        if (type.equals("Unknown")) {
            return "Your file type is: " + file.getContentType() + " and it's fucking sucks";
        }

        String urlPath = storageService.uploadImage(file);

        PostMedia postMedia = new PostMedia();
        postMedia.setPost(post);
        postMedia.setMediaURL(urlPath);
        postMedia.setMediaType(type);
        postMedia.setInteractableItem(interactableItemService.createInteractableItems("MEDIA", post.getCreatedLocalDateTime()));

        postMediaRepository.save(postMedia);
        return "Successfully uploaded post media";
    }

    public String deletePostMedia(PostMedia postMedia) {
        String path = postMedia.getMediaURL();
        String result = path.replace("uploads/", "");
        try {
            if (storageService.deleteImage(result).equals("deleted")){
                postMedia.setMediaURL("");
                postMediaRepository.save(postMedia);
            }
            return "Successfully deleted post media";
        } catch (java.io.IOException e) {
            // Handle the exception: log, rethrow, or return an error message
            System.err.println("Error while deleting post media: " + e.getMessage());
            // Optionally, throw a runtime exception if you want to fail the operation
            throw new RuntimeException("Error while handling media file delete", e);
        }
    }
}
