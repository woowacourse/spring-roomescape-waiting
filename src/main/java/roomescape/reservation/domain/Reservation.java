package roomescape.reservation.domain;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import roomescape.error.ReservationException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
@AllArgsConstructor
public class Reservation {

    private final Long id;

    @NonNull
    private final LocalDate date;

    @NonNull
    private final ReservationTime time;

    @NonNull
    private final Theme theme;

    @NonNull
    private final Member member;

    public Reservation(@NonNull final LocalDate date,
                       @NonNull final ReservationTime reservationTime,
                       @NonNull final Theme theme, @NonNull final Member member) {
        validateFutureOrPresent(date, reservationTime.getStartAt());
        this.id = null;
        this.date = date;
        this.time = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    private void validateFutureOrPresent(final LocalDate date, final LocalTime time) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        final LocalDateTime currentDateTime = LocalDateTime.now();
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new ReservationException("예약은 현재 시간 이후로 가능합니다.");
        }
    }
}
