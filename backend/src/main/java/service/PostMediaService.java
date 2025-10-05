package service;

import coredata_module.Post;
import coredata_module.PostMedia;
import org.springframework.stereotype.Service;
import repository.PostMediaRepository;

import java.util.List;

@Service
public class PostMediaService {

    private final PostMediaRepository postMediaRepository;

    public PostMediaService(PostMediaRepository postMediaRepository) {
        this.postMediaRepository = postMediaRepository;
    }
    ///Chức năng của User
    public List<PostMedia> getPostMedia(Post post){
        return  postMediaRepository.findByPost(post);
    }
    public void addPostMediaToPost(PostMedia postMedia, Post post){
        postMedia.setPost(post);
        postMediaRepository.save(postMedia);
    }
    public void addReactionToPost
    public void removePostMediaFromPost(PostMedia postMedia, Post post){
        postMediaRepository.delete(postMedia);
    }


    ///Chức năng của admin
    public List<PostMedia> getPostMediaByPostId(int postId){
        return postMediaRepository.findByPostId(postId);
    };
}
