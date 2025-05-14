package roomescape.domain.reservation.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import roomescape.domain.reservation.model.dto.ReservationDetails;
import roomescape.domain.reservation.model.exception.ReservationException.InvalidReservationTimeException;

@Getter
public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final ReservationTheme theme;
    private final Long memberId;

    @Builder
    public Reservation(Long id, String name, LocalDate date, ReservationTime time, ReservationTheme theme,
            Long memberId) {
        validateNotBlank(name, date);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.memberId = memberId;
    }

    public Reservation(String name, LocalDate date, ReservationTime time, ReservationTheme theme) {
        this(null, name, date, time, theme, null);
    }

    public static Reservation createFutureReservation(ReservationDetails details) {
        LocalDateTime requestedDateTime = LocalDateTime.of(details.date(), details.reservationTime().getStartAt());
        validateFutureTime(requestedDateTime);
        return details.toReservation();
    }

    private static void validateFutureTime(LocalDateTime requestedDateTime) {
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationTimeException("예약시간이 과거시간이 될 수 없습니다. 미래시간으로 입력해주세요.");
        }
    }

    public Reservation assignId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("할당할 id는 null이 될 수 없습니다.");
        }
        return new Reservation(id, name, date, time, theme, memberId);
    }

    private void validateNotBlank(String name, LocalDate date) {
        if (name == null) {
            throw new IllegalArgumentException("예약자명은 null이 될 수 없습니다.");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("예약자명은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null이 될 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
