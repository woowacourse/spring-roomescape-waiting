package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final LocalDate reservationDate;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationWaiting(Long id, String name, LocalDateTime createdAt, LocalDate reservationDate, ReservationTime time, Theme theme) {
        Objects.requireNonNull(name, "예약 대기자명은 필수값 입니다.");
        Objects.requireNonNull(createdAt, "예약 대기 생성일자는 필수값 입니다.");
        Objects.requireNonNull(reservationDate, "예약 일자는 필수값 입니다.");
        Objects.requireNonNull(time, "예약 대기할 시간은 필수값 입니다.");
        Objects.requireNonNull(theme, "예약 대기할 테마는 필수값 입니다.");
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.reservationDate = reservationDate;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationWaiting createWithoutId(String name, LocalDateTime createdAt, LocalDate reservationDate, ReservationTime time, Theme theme) {
        return new ReservationWaiting(null, name, createdAt, reservationDate, time, theme);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ReservationWaiting reservationWaiting = (ReservationWaiting) object;
        if (id != null && reservationWaiting.id != null) {
            return Objects.equals(id, reservationWaiting.id);
        }
        return Objects.equals(name, reservationWaiting.name)
                && Objects.equals(createdAt, reservationWaiting.createdAt) && Objects.equals(reservationDate, reservationWaiting.reservationDate)
                && Objects.equals(time, reservationWaiting.time) && Objects.equals(theme, reservationWaiting.theme);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, createdAt, reservationDate, time, theme);
    }
}
