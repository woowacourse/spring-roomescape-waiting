package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return Reservation.builder()
                .id(id).name(name).date(date).time(time).theme(theme)
                .build();
    }

    public Reservation reschedule(LocalDate date, ReservationTime time, Clock clock) {
        Reservation changed = Reservation.builder()
                .id(id)
                .name(this.name)
                .date(date)
                .time(time)
                .theme(this.theme)
                .build();
        if (changed.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_TIME_RESERVATION);
        }
        return changed;
    }

    public boolean isPast(Clock clock) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now(clock));
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
