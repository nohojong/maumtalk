package io.notfound.counsel_back.board.dto;

import lombok.Data;

@Data // Getter, Setter, toString 등을 포함
public class AIModerationResponse {
    // AI의 결정: "APPROVE_REPORT"(신고 승인), "REJECT_REPORT"(신고 반려)
    private String decision;
    // AI가 왜 그렇게 판단했는지에 대한 간단한 근거
    private String justification;
}