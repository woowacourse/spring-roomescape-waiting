package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

public record ReservationRequest(@NotNull(message = "예약자는 비워둘 수 없습니다.") Long memberId,
                                 @NotNull(message = "예약 날짜는 비워둘 수 없습니다.") LocalDate date,
                                 @NotNull(message = "예약 시간은 비워둘 수 없습니다.") Long timeId,
                                 @NotNull(message = "테마는 비워둘 수 없습니다.") Long themeId) {

    public Reservation toEntity(Member member, TimeSlot time, Theme theme) {
        return new Reservation(null, member, date, time, theme, ReservationStatus.BOOKING);
    }
}
