package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.PastReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public static ReservationTime create(long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public boolean isPast(LocalDate date, LocalDateTime now) {
        return date.atTime(startAt).isBefore(now);
    }

    public void validateNotPast(LocalDate date, LocalDateTime now) {
        if (isPast(date, now)) {
            throw new PastReservationException("지나간 시간에는 예약할 수 없습니다.");
        }
    }

    public LocalTime startAt() {
        return startAt;
    }

    public Long id() {
        return id;
    }
}
