package roomescape.dto.reservation;

import java.time.LocalDate;

public class ReservationFilter {

    private Long memberId;
    private Long themeId;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReservationFilter() {
    }

    public boolean existFilter() {
        return memberId != null || themeId != null || startDate != null || endDate != null;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
