package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글(postId)의 모든 댓글을 찾는 메서드
    List<Comment> findAllByPostId(Long postId);
}