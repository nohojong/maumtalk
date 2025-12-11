package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Report;
import io.notfound.counsel_back.board.entity.ReportTargetType;
import io.notfound.counsel_back.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    Optional<Report> findByReporterAndTargetIdAndTargetType(
            User reporter,
            Long targetId,
            ReportTargetType targetType
    );
}
