package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.CommentRequest;
import io.notfound.counsel_back.board.dto.CommentResponse;
import io.notfound.counsel_back.board.entity.Comment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.repository.CommentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;

    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found with id: " + postId));

        // 이메일로 작성자 찾기
        User writer = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));

        // 컬렉션 편의 메서드(post.addComment) 대신 직접 연관만 세팅 (중복 카운트 방지)
        Comment comment = Comment.builder()
                .content(request.getContent())
                .writer(writer)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // DB에서 commentCount = commentCount + 1 (경합에 안전)
        boardService.increaseCommentCount(post.getId());

        return CommentResponse.from(savedComment);
    }

    // 특정 게시글의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // 특정 게시글이 존재하는지 먼저 확인
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with id: " + commentId));

        if (!comment.getWriter().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글을 수정할 권한이 없습니다.");
        }
        comment.update(request.getContent());

        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with id: " + commentId));

        if (!comment.getWriter().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글을 삭제할 권한이 없습니다.");
        }

        Long postId = comment.getPost() != null ? comment.getPost().getId() : null;

        // 먼저 삭제
        commentRepository.delete(comment);

        // 그 다음 DB에서 commentCount - 1 (post가 있을 때만)
        if (postId != null) {
            boardService.decreaseCommentCount(postId);
        }
    }
}
