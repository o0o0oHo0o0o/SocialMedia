package repository;

import coredata_module.Post;
import coredata_module.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Integer> {
    public List<PostMedia> findByPostId(Integer postId);
    public List<PostMedia> findByPost(Post post);
}
