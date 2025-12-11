package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.attachments",
            countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllWithAuthorAndAttachments(Pageable pageable);

    /** 제목/내용 대소문자 무시 부분검색 + 페이지네이션 (수정된 버전) */
    @Query(value = "SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT count(p.id) FROM Post p WHERE " +
                    "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> findByKeywordContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    /** 상세 조회 시 작성자/첨부까지 한 번에 가져오기 (N+1 방지) */
    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

    /** 조회수 +1 (원자적 업데이트) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    int incrementViews(@Param("postId") Long postId);

    /** 댓글 수 증감 (원자적 업데이트) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    int changeCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    /* --- (옵션) 필요 시 사용할 수 있는 fetch join 기반 목록/검색 --- */

    /** 모든 게시글을 작성자/첨부와 함께 조회 (필요시 사용) */
    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments")
    List<Post> findAllWithAuthorAndAttachments();

    /** 키워드로 제목/내용 검색 시 작성자/첨부까지 fetch (필요시 사용) */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.attachments " +
            "WHERE p.title LIKE CONCAT('%', :keyword, '%') " +
            "   OR p.content LIKE CONCAT('%', :keyword, '%')")
    List<Post> findByTitleOrContentWithAuthorAndAttachments(@Param("keyword") String keyword);
}
