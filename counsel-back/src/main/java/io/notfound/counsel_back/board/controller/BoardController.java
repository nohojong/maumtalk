package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.PostLikeResponse;
import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> createPost(
            @ModelAttribute PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String email = userDetails.getUsername();
        PostResponse response = boardService.createPost(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 게시글 상세 조회 (조회수 증가 없음) */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        final String email = (userDetails != null) ? userDetails.getUsername() : null;
        PostResponse response = boardService.getPostWithLikeInfo(id, email);
        return ResponseEntity.ok(response);
    }

    /** 유니크 조회수 증가 전용 (로그인 사용자만 카운트, 비로그인은 no-op) */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordUniqueView(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        final String email = (userDetails != null) ? userDetails.getUsername() : null;
        boardService.recordUniqueView(id, email);
        return ResponseEntity.ok().build();
    }

    /** 게시글 목록 조회 (검색 + 페이지네이션 + 좋아요 포함) */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails

    ) {
        final String email = (userDetails != null) ? userDetails.getUsername() : null;
        final String key = (sortBy == null) ? "latest" : sortBy.trim().toLowerCase();

        // 댓글순은 COUNT/필드 기반 정렬 전용 서비스로 위임 (방향 포함)
        if ("comments".equals(key)) {
            Page<PostResponse> responses =
                    boardService.getAllPostsOrderByCommentCount(search, pageable, direction);
            return ResponseEntity.ok(responses);
        }

        // 최신/조회수는 엔티티 필드 정렬
        String sortProperty = switch (key) {
            case "views" -> "views";       // Post 엔티티의 조회수 필드
            default -> "createdAt";   // 최신순
        };

        Sort.Direction dir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable effectivePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(dir, sortProperty)
        );

        Page<PostResponse> responses = boardService.getAllPosts(search, effectivePageable, email);
        return ResponseEntity.ok(responses);
    }

    /** 게시글 수정 */
    @PutMapping(value = "/{postId}", consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @ModelAttribute PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String email = userDetails.getUsername();

        PostResponse response = boardService.updatePost(postId, request, email);
        return ResponseEntity.ok(response);
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String email = userDetails.getUsername();

        boardService.deletePost(id, email);
        return ResponseEntity.noContent().build();
    }

    // 게시글 좋아요 토글
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponse> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername(); // 로그인한 유저의 이메일
        PostLikeResponse response = boardService.toggleLike(postId, email);

        return ResponseEntity.ok(response);
    }

    // 게시글 좋아요 상태 조회
    @GetMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        final String email = (userDetails != null) ? userDetails.getUsername() : null;
        Map<String, Object> likeStatus = boardService.getLikeStatus(postId, email);
        return ResponseEntity.ok(likeStatus);
    }
}
