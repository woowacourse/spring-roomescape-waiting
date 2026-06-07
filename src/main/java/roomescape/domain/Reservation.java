package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.InvalidInputException;
import roomescape.domain.exception.PastReservationException;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final LocalDateTime createdAt;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme) {
        validateFields(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(String name, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme) {
        this(null, name, date, createdAt, time, theme);
        validateNotPast(date, time, createdAt);
    }

    public Reservation withUpdated(LocalDate date, ReservationTime newTime, LocalDateTime now) {
        validateModifiable(this.date, this.time, now);
        validateNotPast(date, newTime, now);
        return new Reservation(id, name, date, this.createdAt, newTime, theme);
    }

    public void validateCancellable(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isOwnedBy(String username) {
        return Objects.equals(name, username);
    }

    public boolean isSameSlot(LocalDate date, long timeId) {
        return this.date.equals(date) && this.time.getId().equals(timeId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return Objects.equals(name, that.name)
                && Objects.equals(date, that.date)
                && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        return Objects.hash(name, date, time, theme);
    }

    private void validateFields(String name, LocalDate date, ReservationTime time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("예약자 이름은 비어있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidInputException("예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new InvalidInputException("예약 시간은 필수입니다.");
        }
        if (theme == null) {
            throw new InvalidInputException("테마는 필수입니다.");
        }
    }

    private static void validateNotPast(LocalDate date, ReservationTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new PastReservationException("과거 날짜로는 예약할 수 없습니다.");
        }
    }

    private static void validateModifiable(LocalDate date, ReservationTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new PastReservationException("과거 예약은 변경할 수 없습니다.");
        }
    }
}
