package roomescape.dto.request;

import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

public record ReservationRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {
    public ReservationRequest {
        isValid(memberId, date, timeId, themeId);
    }

    public Reservation toEntity(final Long id, final Member member, final TimeSlot time, final Theme theme) {
        return new Reservation(id, member, date, time, theme);
    }

    private void isValid(final Long memberId, final LocalDate date, final Long timeId, final Long themeId) {
        if (memberId == null) {
            throw new IllegalArgumentException("[ERROR] 예약자는 비워둘 수 없습니다.");
        }

        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 예약 날짜입니다.");
        }

        if (timeId == null) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 예약 시간입니다.");
        }

        if (themeId == null) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 테마 입니다.");
        }
    }
}
