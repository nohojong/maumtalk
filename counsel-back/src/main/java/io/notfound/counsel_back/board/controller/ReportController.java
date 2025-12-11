package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.ReportRequestDto;
import io.notfound.counsel_back.board.dto.ReportResponseDto;
import io.notfound.counsel_back.board.entity.Report;
import io.notfound.counsel_back.board.entity.ReportStatus;
import io.notfound.counsel_back.board.entity.ReportTargetType;
import io.notfound.counsel_back.board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @RequestBody ReportRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        ReportResponseDto saved = reportService.saveReport(requestDto, email);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportResponseDto>> getReportsForAdmin(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReportResponseDto> reports = reportService.getReportsForAdmin(status, targetType, pageable);
        return ResponseEntity.ok(reports);
    }

    // 관리자용 신고 상태 변경 엔드포인트
    @PatchMapping("/admin/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponseDto> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status
    ) {
        ReportResponseDto updatedReport = reportService.updateReportStatus(reportId, status);
        return ResponseEntity.ok(updatedReport);
    }
}
