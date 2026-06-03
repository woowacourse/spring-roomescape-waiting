package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Reservation {
    private final Long id;
    private final ReservationStatus status;
    private final Long waitingRank;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public static Reservation create(String name,
                                     LocalDate date,
                                     ReservationTime time,
                                     Theme theme,
                                     LocalDateTime now) {
        validate(now, name, date, time, theme);
        return new Reservation(null, null, null, name, date, time, theme);
    }

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, null, null, name, date, time, theme);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this(id, null, null, name, date, time, theme);
    }

    public Reservation(
            Long id,
            ReservationStatus status,
            Long waitingRank,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme) {
        validateName(name);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);

        this.id = id;
        this.status = status;
        this.waitingRank = waitingRank;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation withId(Long id) {
        validateId(id);

        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 예약입니다.");
        }

        return new Reservation(id, status, waitingRank, name, date, time, theme);
    }

    public Reservation cancel() {
        return new Reservation(id, ReservationStatus.CANCELED, null, name, date, time, theme);
    }

    private static void validate(
            LocalDateTime now,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme) {
        validateName(name);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validatePastDateTime(now, date, time);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("예약자 이름은 비어 있을 수 없습니다.");
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new InvalidRequestException("예약 날짜는 비어 있을 수 없습니다.");
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new InvalidRequestException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new InvalidRequestException("테마 정보는 비어 있을 수 없습니다.");
        }
    }

    private static void validatePastDateTime(LocalDateTime now, LocalDate date, ReservationTime time) {
        if (isReservationDateTimeBefore(now, date, time)) {
            throw new InvalidRequestException("현재 시각 이후의 날짜와 시간을 선택해주세요.");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("예약 id는 비어 있을 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return isReservationDateTimeBefore(now, this.date, this.time);
    }

    private static boolean isReservationDateTimeBefore(LocalDateTime now, LocalDate date, ReservationTime time) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isReservedBy(String name) {
        return Objects.equals(this.name, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

