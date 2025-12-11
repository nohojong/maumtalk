package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.AIModerationResponse;
import io.notfound.counsel_back.board.dto.ReportRequestDto;
import io.notfound.counsel_back.board.dto.ReportResponseDto;
import io.notfound.counsel_back.board.entity.*;
import io.notfound.counsel_back.board.repository.CommentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.board.repository.ReportRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AIModerationService aiModerationService;

    @Transactional
    public ReportResponseDto saveReport(ReportRequestDto requestDto, String email) {

        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email
                        )
                );

        // 1. 동일한 유저가 동일한 대상을 이미 신고했는지 확인
        reportRepository.findByReporterAndTargetIdAndTargetType(
                reporter, requestDto.getTargetId(), requestDto.getTargetType()
        ).ifPresent(report -> {
            throw new IllegalStateException("이미 신고한 대상입니다.");
        });

        // 2. 신고 대상(게시글/댓글)이 DB에 존재하는지 확인
        validateTargetExists(requestDto.getTargetId(), requestDto.getTargetType());

        // 3. Report 엔티티 생성 및 저장
        Report report = Report.builder()
                .reporter(reporter)
                .targetId(requestDto.getTargetId())
                .targetType(requestDto.getTargetType())
                .reason(requestDto.getReason())
                .status(ReportStatus.PENDING) // 초기 상태는 '대기중'
                .build();

        Report savedReport = reportRepository.save(report);

        processReportWithAI(savedReport);

        return new ReportResponseDto(savedReport);
    }

    @Async
    @Transactional
    public void processReportWithAI(Report report) {
        try {
            // AI 분석에 필요한 데이터 조회
            Comment comment = commentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new EntityNotFoundException("신고된 댓글을 찾을 수 없습니다."));

            if (comment.isBlinded()) {
                return;
            }

            Post post = comment.getPost();

            // AI 서비스 호출
            AIModerationResponse aiResponse = aiModerationService.moderateComment(
                    post.getContent(), comment.getContent(), report.getReason());

            report.updateJustification(aiResponse.getJustification());

            boolean isApproved = "APPROVE_REPORT".equalsIgnoreCase(aiResponse.getDecision());

            if (isApproved) {
                report.updateStatus(ReportStatus.APPROVED);

                comment.blind();
                commentRepository.save(comment);
            } else {
                report.updateStatus(ReportStatus.REJECTED);
            }

            reportRepository.save(report);

        } catch (Exception e) {
             log.error("AI 신고 처리 중 에러 발생. Report ID: {}", report.getId(), e);
        }
    }

    private void validateTargetExists(Long targetId, ReportTargetType targetType) {
        if (targetType == ReportTargetType.POST) {
            postRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("신고 대상을 찾을 수 없습니다. (게시글 ID: " + targetId + ")"));
        } else if (targetType == ReportTargetType.COMMENT) {
            commentRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("신고 대상을 찾을 수 없습니다. (댓글 ID: " + targetId + ")"));
        }
    }

    /**
     * 관리자용 - 특정 신고의 상태를 변경
     * @param reportId 신고 ID
     * @param newStatus 새로운 상태 (APPROVED / REJECTED)
     * @return 상태가 변경된 Report 엔티티
     */
    @Transactional
    public ReportResponseDto updateReportStatus(Long reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 신고를 찾을 수 없습니다: " + reportId));

        // PENDING 상태일 때만 변경 가능하도록 하거나, 다른 비즈니스 로직 추가 가능
        if (newStatus == ReportStatus.PENDING) {
            throw new IllegalArgumentException("신고 상태를 PENDING으로 변경할 수 없습니다.");
        }

        // Lombok의 @Builder나 별도의 setter를 이용해 status 필드 수정
        // Report 엔티티에 status를 변경할 수 있는 메서드를 만드는 것이 가장 객체지향적임
        report.updateStatus(newStatus); // 이 메서드를 Report 엔티티에 추가해야 함
        Report savedReport = reportRepository.save(report);

        return new ReportResponseDto(savedReport);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponseDto> getReportsForAdmin(
            @Nullable ReportStatus status,
            @Nullable ReportTargetType targetType,
            Pageable pageable) {

        Specification<Report> spec = (root, query, cb) -> {
            return cb.conjunction(); // 모든 조건에 맞는 경우 (기본 조건 없음)
        };

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (targetType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("targetType"), targetType));
        }

        Page<Report> reportPage = reportRepository.findAll(spec, pageable);

        return reportPage.map(ReportResponseDto::new);
    }
}