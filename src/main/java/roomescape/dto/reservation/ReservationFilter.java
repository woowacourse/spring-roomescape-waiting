package roomescape.dto.reservation;

import java.time.LocalDate;

public class ReservationFilter {

    private Long memberId;
    private Long themeId;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public ReservationFilter() {
    }

    public boolean existFilter() {
        return memberId != null || themeId != null || dateFrom != null || dateTo != null;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }
}
