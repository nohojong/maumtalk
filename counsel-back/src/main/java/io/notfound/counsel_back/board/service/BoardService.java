package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.PostLikeResponse;
import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.entity.PostLike;
import io.notfound.counsel_back.board.entity.PostView;
import io.notfound.counsel_back.board.repository.PostLikeRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.board.repository.PostViewRepository;
import io.notfound.counsel_back.common.exception.PostNotFoundException;
import io.notfound.counsel_back.common.exception.UnauthorizedActionException;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;

    // 게시글 생성
    @Transactional
    public PostResponse createPost(PostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다: " + email));

        Post post = Post.builder()
                .title(request.getTitle())
                .author(user)
                .content(request.getContent())
                .build();

        final Post savedPost = postRepository.save(post);

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (MultipartFile file : request.getAttachments()) {
                String fileUrl = s3Service.uploadFile(file);
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(savedPost)
                        .build();
                savedPost.addAttachment(attachment);
            }
        }

        return PostResponse.from(savedPost);
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostResponse getPostWithLikeInfo(Long id, String email) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new PostNotFoundException("게시글이 존재하지 않습니다."));

        boolean liked = false;
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                liked = postLikeRepository.existsByPostAndUser(post, user);
            }
        }

        return PostResponse.from(post, liked);
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(String search, Pageable pageable, String email) {
        Page<Post> posts;
        if (search == null || search.trim().isEmpty()) {
            posts = postRepository.findAllWithAuthorAndAttachments(pageable);
        } else {
            posts = postRepository.findByKeywordContainingIgnoreCase(search.trim(), pageable);
        }

        final User finalUser = (email != null && !email.isBlank())
                ? userRepository.findByEmail(email).orElse(null)
                : null;

        return posts.map(post -> {
            boolean liked = finalUser != null && postLikeRepository.existsByPostAndUser(post, finalUser);
            return PostResponse.from(post, liked);
        });
    }

    // 댓글순 정렬 전용: commentCount 기준 + 정렬 방향(direction) 반영
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPostsOrderByCommentCount(String search, Pageable pageable, String direction) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(dir, "commentCount")
        );

        Page<Post> posts;
        if (search == null || search.trim().isEmpty()) {
            posts = postRepository.findAllWithAuthorAndAttachments(sorted);
        } else {
            String q = search.trim();
            posts = postRepository.findByKeywordContainingIgnoreCase(search.trim(), sorted);
        }

        return posts.map(PostResponse::from);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        if (post.getAuthor() == null || post.getAuthor().getEmail() == null
                || !post.getAuthor().getEmail().equals(email)) {
            throw new UnauthorizedActionException("게시글을 수정할 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());

        if (request.getDeletedAttachmentUrls() != null) {
            List<Attachment> attachmentsToDelete = post.getAttachments().stream()
                    .filter(att -> request.getDeletedAttachmentUrls().contains(att.getFileUrl()))
                    .toList();
            for (Attachment attachment : attachmentsToDelete) {
                try {
                    s3Service.deleteFile(attachment.getFileUrl());
                } catch (Exception ex) {
                    System.err.println("[WARN] S3 파일 삭제 실패: " + attachment.getFileUrl() + " - " + ex.getMessage());
                }
                post.removeAttachment(attachment);
            }
        }

        if (request.getNewAttachments() != null) {
            for (MultipartFile file : request.getNewAttachments()) {
                String fileUrl = s3Service.uploadFile(file);
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(post)
                        .build();
                post.addAttachment(attachment);
            }
        }

        return PostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        if (post.getAuthor() == null || post.getAuthor().getEmail() == null
                || !email.equals(post.getAuthor().getEmail())) {
            throw new UnauthorizedActionException("게시글을 삭제할 권한이 없습니다.");
        }

        // 유니크 뷰 기록 먼저 삭제 (FK 충돌 방지)
        try {
            postViewRepository.deleteByPostId(id);
        } catch (Exception ex) {
            System.err.println("[WARN] PostView 삭제 중 문제 발생(postId=" + id + "): " + ex.getMessage());
        }

        // S3 파일 삭제
        if (post.getAttachments() != null) {
            for (Attachment attachment : post.getAttachments()) {
                String url = attachment.getFileUrl();
                if (url == null || url.isBlank()) continue;
                try {
                    s3Service.deleteFile(url);
                } catch (Exception ex) {
                    System.err.println("[WARN] S3 파일 삭제 실패: " + url + " - " + ex.getMessage());
                }
            }
        }

        // 3) 게시글 삭제 (attachments/comments 는 cascade + orphanRemoval 가정)
        postRepository.delete(post);
    }

    // 댓글 수 증감
    @Transactional
    public void increaseCommentCount(Long postId) {
        int updated = postRepository.changeCommentCount(postId, +1);
        if (updated == 0) throw new PostNotFoundException("해당 게시글이 존재하지 않습니다.");
    }

    @Transactional
    public void decreaseCommentCount(Long postId) {
        int updated = postRepository.changeCommentCount(postId, -1);
        if (updated == 0) throw new PostNotFoundException("해당 게시글이 존재하지 않습니다.");
    }

    // 유니크 조회수 기록
    @Transactional
    public void recordUniqueView(Long postId, String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        User viewer = userRepository.findByEmail(email)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다: " + email));

        boolean seen = postViewRepository.existsByPostIdAndViewerUser_Id(postId, viewer.getId());
        if (seen) return;

        PostView pv = PostView.builder()
                .post(post)
                .viewerUser(viewer)
                .build();
        postViewRepository.save(pv);

        postRepository.incrementViews(postId);
    }

    // 게시글 좋아요 토글
    @Transactional
    public PostLikeResponse toggleLike(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다: " + email));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글이 존재하지 않습니다: " + postId));

        boolean exists = postLikeRepository.existsByPostAndUser(post, user);
        if (exists) {
            // 이미 좋아요를 눌렀다면 좋아요 취소
            postLikeRepository.deleteByPostAndUser(post, user);
            post.decreaseLikeCount(); // 좋아요 수 감소
        } else {
            // 아니라면 좋아요 추가
            postLikeRepository.save(PostLike.builder()
                    .post(post)
                    .user(user)
                    .build());
            post.increaseLikeCount();
        }
        Post savedPost = postRepository.save(post);

        return PostLikeResponse.builder()
                .liked(!exists)
                .likeCount(savedPost.getLikeCount())
                .build();
    }

    /** 게시글 좋아요 상태 가져오기 */
    @Transactional(readOnly = true)
    public Map<String, Object> getLikeStatus(Long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글이 존재하지 않습니다: " + postId));

        long likeCount = postLikeRepository.countByPost(post); // 좋아요 수 카운트

        boolean liked = false;
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                liked = postLikeRepository.existsByPostAndUser(post, user); // 현재 사용자가 좋아요를 눌렀는지 확인
            }
        }

        return Map.of("liked", liked, "likeCount", (int) likeCount);
    }
}
