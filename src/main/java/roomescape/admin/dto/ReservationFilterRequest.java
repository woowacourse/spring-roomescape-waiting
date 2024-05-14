package roomescape.admin.dto;

import java.time.LocalDate;
import roomescape.admin.domain.FilterInfo;

public record ReservationFilterRequest(Long memberId, Long themeId, LocalDate from, LocalDate to) {
    public FilterInfo toFilterInfo() {
        return new FilterInfo(memberId, themeId, from, to);
    }
}
