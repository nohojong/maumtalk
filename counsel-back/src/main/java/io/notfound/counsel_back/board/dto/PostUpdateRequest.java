package io.notfound.counsel_back.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostUpdateRequest { // 수정용 DTO를 분리하거나 기존 DTO에 필드 추가
    private String title;
    private String content;
    private List<MultipartFile> newAttachments; // 새로 추가할 파일
    private List<String> deletedAttachmentUrls; // 삭제할 기존 파일의 URL 목록
}