package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostMediaResponse;
import com.example.SocialMedia.exception.UnsupportedFileTypeException;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.PostMedia;
import com.example.SocialMedia.repository.PostMediaRepository;
import com.example.SocialMedia.service.InteractableItemService;
import com.example.SocialMedia.service.PostMediaService;
import com.example.SocialMedia.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    public PostMediaResponse[] findByPost(Post post, Pageable pageable){
        return postMediaRepository.findByPost(post, pageable)
                .getContent().stream()
                .map(media -> new PostMediaResponse.PostMediaResponseBuilder()
                        .id(media.getPostMediaId())
                        .mediaType(media.getMediaType())
                        .mediaURL(media.getMediaURL())
                        .sortOrder(media.getSortOrder())
                        .build())
                .toArray(PostMediaResponse[]::new);
    }

    public void deleteByPost(Post post) {
        List<PostMedia> postMedia = postMediaRepository.findByPost(post);
        postMedia.forEach(media -> deletePostMedia(media.getPostMediaId()));
    }

    public void createPostMedia(MultipartFile file, Post post){
        String fileName = file.getOriginalFilename();

        String type = classify(file);
        if (type.equals("Unknown")) {
            throw new UnsupportedFileTypeException("Your file" + fileName + "type is: " + file.getContentType() + " and it's fucking sucks");
        }

        String urlPath;
        try {
            urlPath = storageService.uploadImage(file);
        } catch (IOException e){
            throw new DataIntegrityViolationException("Could not upload image:" + e.getMessage());
        }

        PostMedia postMedia = new PostMedia();
        postMedia.setPost(post);
        postMedia.setMediaURL(urlPath);
        postMedia.setMediaType(type);
        postMedia.setInteractableItem(interactableItemService.createInteractableItems("MEDIA", post.getCreatedLocalDateTime()));

        postMediaRepository.save(postMedia);
    }

    public void deletePostMedia(int id) {
        PostMedia postMedia = postMediaRepository.findPostMediaByPostMediaId(id);
        String path = postMedia.getMediaURL();
        String result = path.replace("uploads/", "");
        try {
            storageService.deleteImage(result);
            postMedia.setMediaURL("");
            postMediaRepository.save(postMedia);
        } catch (IOException e) {
            // Optionally, throw a runtime exception if you want to fail the operation
            throw new DataIntegrityViolationException("Error while deleting post media: " + e.getMessage());
        }
    }
}
