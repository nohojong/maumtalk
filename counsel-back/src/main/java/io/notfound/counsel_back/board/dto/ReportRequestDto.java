package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.ReportTargetType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequestDto {
    private Long targetId;
    private ReportTargetType targetType; // POST / COMMENT
    private String reason;
}
