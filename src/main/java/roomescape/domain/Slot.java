package roomescape.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.custom.InvalidDomainValueException;

@Embeddable
public class Slot {

    private LocalDate reservationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    public Slot() {
    }

    public Slot(LocalDate reservationDate, ReservationTime time, Theme theme) {
        validate(reservationDate, time, theme);
        this.reservationDate = reservationDate;
        this.time = time;
        this.theme = theme;
    }

    public boolean isPast(LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (reservationDate.isBefore(nowDate)) {
            return true;
        }
        if (reservationDate.isAfter(nowDate)) {
            return false;
        }
        return time.isPast(nowTime);
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme) {
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Slot slot = (Slot) object;
        return Objects.equals(reservationDate, slot.reservationDate) && Objects.equals(time, slot.time)
                && Objects.equals(theme, slot.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationDate, time, theme);
    }
}
