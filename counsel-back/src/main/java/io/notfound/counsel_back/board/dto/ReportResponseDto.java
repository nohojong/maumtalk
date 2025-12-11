package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Report;
import io.notfound.counsel_back.board.entity.ReportStatus;
import io.notfound.counsel_back.board.entity.ReportTargetType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportResponseDto {
    private final Long id;
    private final Long targetId;
    private final ReportTargetType targetType;
    private final String reason;
    private final ReportStatus status;
    private final LocalDateTime createdAt;
    private final String reporterEmail;
    private final String justification;

    public ReportResponseDto(Report report) {
        this.id = report.getId();
        this.targetId = report.getTargetId();
        this.targetType = report.getTargetType();
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
        // reporter가 null일 수 있는 경우를 대비 (탈퇴한 회원 등)
        this.reporterEmail = (report.getReporter() != null) ? report.getReporter().getEmail() : "알 수 없음";
        this.justification = report.getJustification();
    }
}