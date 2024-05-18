package roomescape.reservation.domain;

import java.time.LocalDate;
import roomescape.exception.BadArgumentRequestException;

public record ReservationSearch(Long themeId, Long memberId, LocalDate startDate, LocalDate endDate) {
    public ReservationSearch {
        if (themeId == null && memberId == null && startDate == null && endDate == null) {
            throw new BadArgumentRequestException("검색 조건은 1개 이상 있어야 합니다.");
        }
    }
}
