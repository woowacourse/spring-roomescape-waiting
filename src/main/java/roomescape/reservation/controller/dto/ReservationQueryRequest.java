package roomescape.reservation.controller.dto;

import java.time.LocalDate;

public class ReservationQueryRequest {
    private final Long themeId;
    private final Long memberId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ReservationQueryRequest(Long themeId, Long memberId, LocalDate startDate, LocalDate endDate) {
        this.themeId = themeId;
        this.memberId = memberId;
        this.startDate = startDate == null ? LocalDate.now() : startDate;
        this.endDate = endDate == null ? LocalDate.now().plusMonths(2) : endDate;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
