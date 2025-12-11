package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.CommentRequest;
import io.notfound.counsel_back.board.dto.CommentResponse;
import io.notfound.counsel_back.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        CommentResponse response = commentService.createComment(postId, request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentResponse> responses = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        CommentResponse response =
                commentService.updateComment(commentId, request, email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        commentService.deleteComment(commentId, email);
        return ResponseEntity.noContent().build();
    }
}