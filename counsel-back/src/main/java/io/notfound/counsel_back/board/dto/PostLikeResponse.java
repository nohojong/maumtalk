package io.notfound.counsel_back.board.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostLikeResponse {
    private boolean liked;
    private int likeCount;
}
