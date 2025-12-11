package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    boolean existsByPostIdAndViewerUser_Id(Long postId, Long viewerUserId);
    void deleteByPostId(Long postId);
}
