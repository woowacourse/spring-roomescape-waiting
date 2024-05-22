package roomescape.dto.reservation;

import io.micrometer.common.lang.Nullable;
import roomescape.domain.member.Member;
import roomescape.domain.member.Name;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.MemberResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public record ReservationSaveRequest(
        @Nullable
        Long memberId,
        String date,
        Long timeId,
        Long themeId,
        String status
) {

    public Reservation toModel(
            final MemberResponse memberResponse,
            final ThemeResponse themeResponse,
            final ReservationTimeResponse timeResponse,
            final String status
    ) {
        final Member member = new Member(memberResponse.id(), new Name(memberResponse.name()), memberResponse.email());
        final ReservationTime time = new ReservationTime(timeResponse.id(), timeResponse.startAt());
        final Theme theme = new Theme(themeResponse.id(), themeResponse.name(), themeResponse.description(), themeResponse.thumbnail());
        final ReservationStatus reservationStatus = ReservationStatus.valueOf(status);
        final LocalDate date = convertToLocalDate(this.date);
        validateDate(date);
        return new Reservation(member, date, time, theme, reservationStatus);
    }

    private static LocalDate convertToLocalDate(final String date) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("예약 날짜가 비어있습니다.");
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("유효하지 않은 예약 날짜입니다.");
        }
    }

    private void validateDate(final LocalDate date) {
        if (date.isBefore(LocalDate.now()) || date.equals(LocalDate.now())) {
            throw new IllegalArgumentException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }
}
