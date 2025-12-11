package io.notfound.counsel_back.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostRequest {
    private String title;
    private String content;
    private List<MultipartFile> attachments;
}
