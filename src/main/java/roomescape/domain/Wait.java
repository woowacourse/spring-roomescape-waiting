package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.custom.InvalidDomainValueException;

public class Wait {

    private final Long id;
    private final LocalDateTime createdAt;
    private final String name;
    private final LocalDate reservationDate;
    private final ReservationTime time;
    private final Theme theme;

    public Wait(Long id, LocalDateTime createdAt, String name, LocalDate reservationDate, ReservationTime time,
                Theme theme) {
        validate(createdAt, name, reservationDate, time, theme);
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.reservationDate = reservationDate;
        this.time = time;
        this.theme = theme;
    }

    public Wait(LocalDateTime createdAt, String name, LocalDate reservationDate, ReservationTime time, Theme theme) {
        this(null, createdAt, name, reservationDate, time, theme);
    }

    public static Wait withId(Long id, Wait wait) {
        return new Wait(id, wait.createdAt, wait.name, wait.reservationDate, wait.time, wait.theme);
    }

    public boolean isSameUser(String name) {
        return this.name.equals(name);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Wait wait = (Wait) object;
        return Objects.equals(createdAt, wait.createdAt) && Objects.equals(name, wait.name)
                && Objects.equals(reservationDate, wait.reservationDate) && Objects.equals(time,
                wait.time) && Objects.equals(theme, wait.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, name, reservationDate, time, theme);
    }

    private void validate(LocalDateTime createdAt, String name, LocalDate date, ReservationTime time, Theme theme) {
        if (createdAt == null) {
            throw new InvalidDomainValueException("대기 신청 시간은 비어 있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDomainValueException("대기자 이름은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidDomainValueException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new InvalidDomainValueException("예약 시간은 비어 있을 수 없습니다.");
        }
        if (theme == null) {
            throw new InvalidDomainValueException("테마는 비어 있을 수 없습니다.");
        }
    }
}
