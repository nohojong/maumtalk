package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.entity.PostLike;
import io.notfound.counsel_back.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 게시글에 대한 좋아요 수 조회
    long countByPost(Post post);

    // 해당 게시글에 특정 사용자가 좋아요를 눌렀는지 확인
    boolean existsByPostAndUser(Post post, User user);

    // 특정 게시글에 대해 특정 사용자의 좋아요 취소
    void deleteByPostAndUser(Post post, User user);

    // 게시글 삭제 시 해당 게시글에 관련된 모든 좋아요 삭제 (옵션)
    void deleteByPost(Post post);
}
