package roomescape.reservation.domain;

import java.time.LocalDate;

public class ReservationSearch {

    private final Long themeId;
    private final Long memberId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ReservationSearch(Long themeId, Long memberId, LocalDate startDate, LocalDate endDate) {
        if (themeId == null && memberId == null && startDate == null && endDate == null) {
            throw new IllegalArgumentException("검색 조건은 1개 이상 있어야 합니다.");
        }
        this.themeId = themeId;
        this.memberId = memberId;
        this.startDate = startDate;
        this.endDate = endDate;
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
