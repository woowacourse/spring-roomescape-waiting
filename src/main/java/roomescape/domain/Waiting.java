package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Waiting {
    private final Long id;
    private final String name;
    private final LocalDate reservationDate;
    private final ReservationTime reservationTime;
    private final Theme reservationTheme;
    private final LocalDateTime createAt;

    public static Waiting create(long id, String name, LocalDate reservationDate, ReservationTime reservationTime, Theme reservationTheme, LocalDateTime createdAt) {
        return new Waiting(id, name, reservationDate, reservationTime, reservationTheme, createdAt);
    }

    public boolean isPast(LocalDateTime now) {
        return reservationTime.isPast(reservationDate, now);
    }

    public void validateCancelable(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 시작된 게임의 예약대기는 취소할 수 없습니다.");
        }
    }

    public void validateOwnedBy(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException("타인의 예약대기는 취소할 수 없습니다.");
        }
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LocalDate reservationDate() {
        return reservationDate;
    }

    public ReservationTime reservationTime() {
        return reservationTime;
    }

    public Theme reservationTheme() {
        return reservationTheme;
    }

    public LocalDateTime createAt() {
        return createAt;
    }
}
